package com.abarajithan.rxpermissions

import android.Manifest.permission.*
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TODO
 *
 * @author Abarajithan
 */
@RunWith(AndroidJUnit4::class)
class RxPermissionsTest {

    private val permissions = arrayOf(WRITE_EXTERNAL_STORAGE, READ_CONTACTS, READ_SMS)

    @get:Rule
    var activityTestRule = ActivityTestRule(TestActivity::class.java, true, true)

    var fragment = ShadowFragment()

    @Before
    fun setup() {
        activityTestRule.runOnUiThread {
            activityTestRule.activity.supportFragmentManager
                .beginTransaction()
                .add(fragment, "test_fragment")
                .commitNow()
        }
    }

    @After
    fun denyAllPermissions() {
        denyPermission(*permissions)
    }

    @Test
    fun requestPermissionTest_granted() {
        val permission = permissions[0]
        grantPermission(permission)

        val testObserver = RxPermissions.test(fragment)
            .request(permission)
            .test()

        testObserver.await()
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)
        testObserver.values()[0].forEach {
            assertEquals(permission[0], it.permission)
            assertEquals(true, it.granted)
        }

        testObserver.dispose()
    }

    @Test
    fun requestPermissionTest_denied() {
        val permission = permissions[0]

        val testObserver = RxPermissions.test(fragment)
            .request(permission)
            .test()

        testObserver.await()
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.values()[0].filter { !it.granted }.forEach {
            assertEquals(permission, it.permission)
            assertFalse(it.granted)
        }

        testObserver.dispose()
    }

    @Test
    fun multiPermissionTest() {
        val testObserver = RxPermissions.test(fragment)
            .request(*permissions)
            .test()

        grantPermission(permissions[1])
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.values()[0].forEach { assertTrue(it.granted) }

        denyPermission(permissions[0], permissions[2])

        testObserver.await()
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(2)
        testObserver.values()[0].forEach {
            assertFalse(it.granted)
        }

        testObserver.dispose()
    }

    @Test
    fun simpleRequestPermissionTest() {
        val permission = permissions[0]
        grantPermission(permission)

        val testObserver = RxPermissions.test(fragment)
            .requestSimple(permission)
            .test()

        testObserver.await()
        testObserver.assertNoErrors()
        testObserver.assertComplete()
        testObserver.assertValueCount(1)
        testObserver.values()[0].forEach {
            assertEquals(true, it)
        }

        testObserver.dispose()
    }

    /* Helpers */

    private fun grantPermission(vararg permissions: String) {
        val packageName = getInstrumentation().targetContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (perm in permissions) {
                getInstrumentation().uiAutomation.executeShellCommand(
                    "pm grant $packageName $perm"
                )
            }
        }
    }

    private fun denyPermission(vararg permissions: String) {
        val packageName = getInstrumentation().targetContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (perm in permissions) {
                getInstrumentation().uiAutomation.executeShellCommand(
                    "pm revoke $packageName $perm"
                )
            }
        }
    }
}
