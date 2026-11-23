package com.bl4ckswordsman.cerberustiles.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bl4ckswordsman.cerberustiles.ui.CerberusSubScreenScaffold
import com.bl4ckswordsman.cerberustiles.ui.PermissionsScreen

/**
 * Activity displaying the Permissions & Privacy screen and rationale.
 */
class PermissionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CerberusSubScreenScaffold(
                title = "Permissions & Privacy",
                onNavigateBack = { finish() }
            ) { innerPadding ->
                PermissionsScreen(paddingValues = innerPadding)
            }
        }
    }
}
