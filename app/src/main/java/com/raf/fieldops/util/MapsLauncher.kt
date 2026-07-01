package com.raf.fieldops.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object MapsLauncher {

    fun openDirections(context: Context, address: String): String? {
        val encodedAddress = Uri.encode(address)

        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$encodedAddress"))
        mapsIntent.setPackage("com.google.android.apps.maps")

        @Suppress("DEPRECATION")
        if (mapsIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapsIntent)
            return null
        }

        val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encodedAddress"))
        @Suppress("DEPRECATION")
        if (geoIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(geoIntent)
            return null
        }

        return "No maps app available"
    }
}
