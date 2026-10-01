package com.example.endoquest

import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.endoquest.audio.SoundManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Android tests executing on device/emulator to verify
 * resource loading, application context integrity, custom cartoon obstacle assets,
 * and audio manager lifecycle.
 */
@RunWith(AndroidJUnit4::class)
class EndoQuestInstrumentedTest {

    @Test
    fun testApplicationContextAndPackage() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("Package name must match production applicationId", "com.example.endoquest", appContext.packageName)
    }

    @Test
    fun testCustomObstacleDrawablesAreLoaded() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Decayed Tooth obstacle drawable (Slide Under)
        val decayedTooth = ContextCompat.getDrawable(appContext, R.drawable.obs_decayed_tooth)
        assertNotNull("obs_decayed_tooth drawable must be resolvable and loadable", decayedTooth)
        assertTrue("obs_decayed_tooth must have valid dimensions", (decayedTooth?.intrinsicWidth ?: 0) > 0)

        // Gum Socket obstacle drawable (Jump Over)
        val gumSocket = ContextCompat.getDrawable(appContext, R.drawable.obs_gum_socket)
        assertNotNull("obs_gum_socket drawable must be resolvable and loadable", gumSocket)
        assertTrue("obs_gum_socket must have valid dimensions", (gumSocket?.intrinsicWidth ?: 0) > 0)

        // Rotating Bur obstacle drawable (Change Lane / Dodge)
        val dentalBur = ContextCompat.getDrawable(appContext, R.drawable.obs_dental_bur)
        assertNotNull("obs_dental_bur drawable must be resolvable and loadable", dentalBur)
        assertTrue("obs_dental_bur must have valid dimensions", (dentalBur?.intrinsicWidth ?: 0) > 0)
    }

    @Test
    fun testSoundManagerLifecycleAndMuteToggle() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as android.app.Application
        val soundManager = SoundManager(appContext)

        // Initial state: not muted
        assertFalse("SoundManager should default to unmuted", soundManager.isMuted)

        // Toggle mute to true
        val isMuted = soundManager.toggleMute()
        assertTrue("SoundManager should report muted after toggle", isMuted)
        assertTrue(soundManager.isMuted)

        // Toggle back to unmuted
        val isUnmuted = soundManager.toggleMute()
        assertFalse("SoundManager should return to unmuted", isUnmuted)
        assertFalse(soundManager.isMuted)
    }
}
