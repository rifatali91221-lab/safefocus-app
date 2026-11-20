package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SafeFocusDatabase
import com.example.data.repository.SafeFocusRepository
import com.example.ui.viewmodel.PermissionType
import com.example.ui.viewmodel.SafeFocusViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `app_name is SafeFocus`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SafeFocus", appName)
    }

    @Test
    fun `repository seeds only three supported apps`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = SafeFocusDatabase.getInstance(context)
        val repo = SafeFocusRepository(context, db.safeFocusDao())
        repo.ensureSeeded()
        val apps = repo.getApps()
        assertEquals(3, apps.size)
        assertEquals(listOf("YouTube", "Facebook", "Instagram"), apps.map { it.displayName })
        assertTrue(repo.getQuestions().isNotEmpty())
    }

    @Test
    fun `permission wizard shows restricted setting guide after understood for accessibility and overlay`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = SafeFocusViewModel(app)
        runCurrent()

        vm.onPermissionSwitchClicked(PermissionType.ACCESSIBILITY, true)
        assertEquals(PermissionType.ACCESSIBILITY, vm.uiState.value.pendingPermissionDialog)
        assertNull(vm.uiState.value.pendingRestrictedGuideFor)

        vm.confirmPermissionUnderstood()
        assertNull(vm.uiState.value.pendingPermissionDialog)
        assertEquals(PermissionType.ACCESSIBILITY, vm.uiState.value.pendingRestrictedGuideFor)

        vm.dismissRestrictedGuide()
        assertNull(vm.uiState.value.pendingRestrictedGuideFor)

        vm.onPermissionSwitchClicked(PermissionType.BACKGROUND, true)
        assertEquals(PermissionType.BACKGROUND, vm.uiState.value.pendingPermissionDialog)
        vm.confirmPermissionUnderstood()
        runCurrent()
        assertNull(vm.uiState.value.pendingRestrictedGuideFor)

        vm.viewModelScope.cancel()
    }
}
