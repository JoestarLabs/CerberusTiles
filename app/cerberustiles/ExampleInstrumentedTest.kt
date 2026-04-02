package com.bl4ckswordsman.cerberustiles

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test

            /**
             * Instrumented test to verify the application context.
             *
             * This test checks if the context of the app under test is correct
             * by comparing the package name with the expected value.
             */
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.bl4ckswordsman.customtiles", appContext.packageName)
    }
}
