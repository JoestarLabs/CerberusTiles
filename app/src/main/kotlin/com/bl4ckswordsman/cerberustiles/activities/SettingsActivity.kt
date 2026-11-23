package com.bl4ckswordsman.cerberustiles.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bl4ckswordsman.cerberustiles.Constants
import com.bl4ckswordsman.cerberustiles.ui.CerberusSubScreenScaffold
import com.bl4ckswordsman.cerberustiles.ui.SettingsScreen
import com.bl4ckswordsman.cerberustiles.ui.SettingsScreenParams
import com.bl4ckswordsman.cerberustiles.ui.createSharedParams

/**
 * Activity for displaying application settings, dialogs, and navigation to
 * sub-screens (Permissions and Licenses).
 */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CerberusSubScreenScaffold(
                title = Constants.SETTINGS_SCREEN,
                onNavigateBack = { finish() }
            ) { innerPadding ->
                val sharedParams = createSharedParams(
                    onNavigateToPermissions = {
                        startActivity(Intent(this, PermissionsActivity::class.java))
                    },
                    onNavigateToLicenses = {
                        startActivity(Intent(this, LicensesActivity::class.java))
                    }
                )
                SettingsScreen(
                    SettingsScreenParams(
                        paddingValues = innerPadding,
                        sharedParams = sharedParams
                    )
                )
            }
        }
    }
}
