package com.bl4ckswordsman.cerberustiles.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.bl4ckswordsman.cerberustiles.Constants as label
import com.bl4ckswordsman.cerberustiles.ui.SettingsScreen
import com.bl4ckswordsman.cerberustiles.ui.SettingsScreenParams
import com.bl4ckswordsman.cerberustiles.ui.createSharedParams
import com.bl4ckswordsman.cerberustiles.ui.theme.CerberusTilesTheme

/**
 * Activity for displaying application settings, dialogs, and navigation to
 * sub-screens (Permissions and Licenses).
 */
class SettingsActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CerberusTilesTheme {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        MediumTopAppBar(
                            title = { Text(label.SETTINGS_SCREEN) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
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
}
