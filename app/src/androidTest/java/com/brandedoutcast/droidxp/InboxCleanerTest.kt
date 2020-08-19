package com.brandedoutcast.droidxp

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.runner.RunWith
import org.junit.Before
import org.junit.Test
import java.lang.Thread.sleep

private const val PACKAGE_NAME = "com.google.android.apps.messaging"
private const val LAUNCH_TIMEOUT = 5000L

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class InboxCleanerTest {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun launchAppFromHome() {
        device.pressHome()

        val launcherName = device.launcherPackageName
        assertThat(launcherName, notNullValue())
        device.wait(
            Until.hasObject(By.pkg(launcherName).depth(0)),
            LAUNCH_TIMEOUT
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        device.wait(
            Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)),
            LAUNCH_TIMEOUT
        )
    }

    @Test
    fun deleteAllMessages_ReturnsTrue() {
        while (true) {
            val visibleMessages =
                device.findObjects(By.res("com.google.android.apps.messaging:id/swipeableContainer"))

            if (visibleMessages.size < 1) break

            val lastMsgTxt = visibleMessages.last()
                .findObject(By.res("com.google.android.apps.messaging:id/conversation_snippet"))?.text
                ?: continue

            visibleMessages.first().click(2000)
            visibleMessages.drop(1).forEach { it.click() }

            val deleteIcon = device.findObject(
                UiSelector().resourceId("com.google.android.apps.messaging:id/action_delete")
            )

            deleteIcon.waitForExists(LAUNCH_TIMEOUT)

            deleteIcon.click()

            val deleteBtn = device.findObject(
                UiSelector().text("Delete").className("android.widget.Button")
            )

            deleteBtn.waitForExists(LAUNCH_TIMEOUT)

            deleteBtn.click()
            sleep(3000)
            device.wait(
                Until.gone(By.text(lastMsgTxt)),
                LAUNCH_TIMEOUT
            )
        }
    }
}