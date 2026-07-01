package com.raf.fieldops.data.remote

import com.raf.fieldops.BuildConfig
import com.raf.fieldops.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

interface AddressLookupService {

    suspend fun findAddresses(query: String): List<String>
}

@Singleton
class GooglePlacesAddressLookup @Inject constructor() : AddressLookupService {

    private val apiKey = BuildConfig.PLACES_API_KEY
    private val baseUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json"

    companion object {
        private const val TAG = "AddressLookup"
    }

    override suspend fun findAddresses(query: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val cleanInput = query.trim()
                if (cleanInput.isEmpty()) return@withContext emptyList()

                val encodedInput = URLEncoder.encode(cleanInput, "UTF-8")
                val url = URL(
                    "$baseUrl?input=$encodedInput" +
                    "&components=country:gb" +
                    "&key=$apiKey"
                )

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorBody = try {
                        connection.errorStream?.bufferedReader()?.readText() ?: "No error body"
                    } catch (_: Exception) { "Could not read error" }
                    AppLogger.error(TAG, "API returned $responseCode: $errorBody")
                    return@withContext emptyList()
                }

                val responseBody = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(responseBody)
                val status = json.optString("status", "")

                if (status != "OK" && status != "ZERO_RESULTS") {
                    AppLogger.error(TAG, "Places API status: $status for '$query'")
                    return@withContext emptyList()
                }

                val predictions = json.optJSONArray("predictions")
                    ?: return@withContext emptyList()
                val addresses = mutableListOf<String>()

                for (i in 0 until predictions.length()) {
                    val prediction = predictions.getJSONObject(i)
                    val description = prediction.optString("description", "")
                    if (description.isNotBlank()) {

                        addresses.add(description.removeSuffix(", UK"))
                    }
                }

                AppLogger.debug(TAG, "Found ${addresses.size} suggestions for '$query'")
                addresses
            } catch (e: Exception) {
                AppLogger.error(TAG, "Failed to lookup address: ${e.message}", e)
                emptyList()
            }
        }
}
