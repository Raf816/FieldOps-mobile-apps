package com.raf.fieldops.ui.dispatcher.createjob

import com.raf.fieldops.data.model.User
import com.raf.fieldops.data.remote.AddressLookupService
import com.raf.fieldops.data.repo.AuthRepo
import com.raf.fieldops.data.repo.JobRepo
import com.raf.fieldops.data.repo.UserRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class CreateJobVMValidationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var vm: CreateJobVM

    private val jobRepo: JobRepo = mock()
    private val authRepo: AuthRepo = mock()
    private val userRepo: UserRepo = mock()
    private val addressLookupService: AddressLookupService = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        whenever(userRepo.getAllEngineers()).thenReturn(flowOf(emptyList()))

        whenever(jobRepo.lastSynced).thenReturn(MutableStateFlow(null))
        vm = CreateJobVM(jobRepo, authRepo, userRepo, addressLookupService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun titleError_emptyString_returnsError() {
        vm.title.value = ""
        assertNotNull(vm.titleError())
        assertEquals("Title must be at least 5 characters", vm.titleError())
    }

    @Test
    fun titleError_fourChars_returnsError() {
        vm.title.value = "Test"
        assertNotNull(vm.titleError())
    }

    @Test
    fun titleError_fiveChars_returnsNull() {
        vm.title.value = "Tests"
        assertNull(vm.titleError())
    }

    @Test
    fun titleError_longTitle_returnsNull() {
        vm.title.value = "Fix broadband at customer site"
        assertNull(vm.titleError())
    }

    @Test
    fun descriptionError_emptyString_returnsError() {
        vm.description.value = ""
        assertNotNull(vm.descriptionError())
        assertEquals("Description must be at least 10 characters", vm.descriptionError())
    }

    @Test
    fun descriptionError_nineChars_returnsError() {
        vm.description.value = "123456789"
        assertNotNull(vm.descriptionError())
    }

    @Test
    fun descriptionError_tenChars_returnsNull() {
        vm.description.value = "1234567890"
        assertNull(vm.descriptionError())
    }

    @Test
    fun descriptionError_longDescription_returnsNull() {
        vm.description.value = "Replace the faulty router at the customer premises"
        assertNull(vm.descriptionError())
    }

    @Test
    fun addressError_emptyString_returnsError() {
        vm.address.value = ""
        assertNotNull(vm.addressError())
        assertEquals("Address is required", vm.addressError())
    }

    @Test
    fun addressError_blankSpaces_returnsError() {
        vm.address.value = "   "
        assertNotNull(vm.addressError())
    }

    @Test
    fun addressError_validAddress_returnsNull() {
        vm.address.value = "123 High Street, London"
        assertNull(vm.addressError())
    }

    @Test
    fun engineerError_noSelection_returnsError() {
        vm.selectedEngineer.value = null
        assertNotNull(vm.engineerError())
        assertEquals("Please select an engineer", vm.engineerError())
    }

    @Test
    fun engineerError_engineerSelected_returnsNull() {
        vm.selectedEngineer.value = User(uid = "eng1", displayName = "John Smith")
        assertNull(vm.engineerError())
    }

    @Test
    fun scheduleError_startNull_returnsError() {
        vm.scheduledStartMillis.value = null
        vm.scheduledEndMillis.value = System.currentTimeMillis() + 7_200_000
        assertNotNull(vm.scheduleError())
        assertEquals("Start time is required", vm.scheduleError())
    }

    @Test
    fun scheduleError_endNull_returnsError() {
        vm.scheduledStartMillis.value = System.currentTimeMillis() + 3_600_000
        vm.scheduledEndMillis.value = null
        assertNotNull(vm.scheduleError())
        assertEquals("End time is required", vm.scheduleError())
    }

    @Test
    fun scheduleError_startInPast_returnsError() {
        val pastTime = System.currentTimeMillis() - 3_600_000
        vm.scheduledStartMillis.value = pastTime
        vm.scheduledEndMillis.value = pastTime + 3_600_000
        assertNotNull(vm.scheduleError())
        assertEquals("Start time cannot be in the past", vm.scheduleError())
    }

    @Test
    fun scheduleError_endBeforeStart_returnsError() {
        val future = System.currentTimeMillis() + 3_600_000
        vm.scheduledStartMillis.value = future
        vm.scheduledEndMillis.value = future - 3_600_000
        assertNotNull(vm.scheduleError())
        assertEquals("End time must be after start time", vm.scheduleError())
    }

    @Test
    fun scheduleError_endEqualsStart_returnsError() {
        val future = System.currentTimeMillis() + 3_600_000
        vm.scheduledStartMillis.value = future
        vm.scheduledEndMillis.value = future
        assertNotNull(vm.scheduleError())
    }

    @Test
    fun scheduleError_endAfterStart_returnsNull() {
        val future = System.currentTimeMillis() + 3_600_000
        vm.scheduledStartMillis.value = future
        vm.scheduledEndMillis.value = future + 3_600_000
        assertNull(vm.scheduleError())
    }

    @Test
    fun isValid_allFieldsEmpty_returnsFalse() {
        vm.title.value = ""
        vm.description.value = ""
        vm.address.value = ""
        vm.selectedEngineer.value = null
        assertFalse(vm.isValid())
    }

    @Test
    fun isValid_allFieldsValid_returnsTrue() {
        vm.title.value = "Fix router"
        vm.description.value = "Replace the faulty router at premises"
        vm.address.value = "123 High Street"
        vm.selectedEngineer.value = User(uid = "eng1", displayName = "John")

        assertTrue(vm.isValid())
    }

    @Test
    fun isValid_titleTooShort_returnsFalse() {
        vm.title.value = "Fix"
        vm.description.value = "Replace the faulty router at premises"
        vm.address.value = "123 High Street"
        vm.selectedEngineer.value = User(uid = "eng1", displayName = "John")
        assertFalse(vm.isValid())
    }

    @Test
    fun init_prefillsStartInFuture() {
        val startMillis = vm.scheduledStartMillis.value
        assertNotNull(startMillis)

        assertTrue(
            "Start time should be in the future",
            startMillis!! >= System.currentTimeMillis() - 60_000
        )

        val cal = Calendar.getInstance().apply { timeInMillis = startMillis }
        assertEquals(0, cal.get(Calendar.MINUTE))
    }

    @Test
    fun init_prefillsEndOneHourAfterStart() {
        val startMillis = vm.scheduledStartMillis.value
        val endMillis = vm.scheduledEndMillis.value
        assertNotNull(startMillis)
        assertNotNull(endMillis)

        assertEquals(3_600_000L, endMillis!! - startMillis!!)
    }

    @Test
    fun onAddressChange_updatesAddressState() {
        vm.onAddressChange("College Road, Stoke")
        assertEquals("College Road, Stoke", vm.address.value)
    }

    @Test
    fun onAddressChange_shortInput_clearsSuggestions() {

        vm.onAddressChange("Co")
        assertTrue(vm.addressSuggestions.value.isEmpty())
    }

    @Test
    fun selectAddress_fillsAddressField() {
        vm.selectAddress("De Burgh Street, Cardiff CF11 6LD")
        assertEquals("De Burgh Street, Cardiff CF11 6LD", vm.address.value)
    }

    @Test
    fun selectAddress_clearsSuggestions() {
        vm.selectAddress("De Burgh Street, Cardiff CF11 6LD")
        assertTrue(vm.addressSuggestions.value.isEmpty())
    }
}
