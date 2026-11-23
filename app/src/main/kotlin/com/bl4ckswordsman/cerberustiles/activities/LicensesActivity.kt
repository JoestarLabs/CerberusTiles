package com.bl4ckswordsman.cerberustiles.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bl4ckswordsman.cerberustiles.R
import com.bl4ckswordsman.cerberustiles.ui.CerberusSubScreenScaffold
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer

/**
 * Activity displaying open-source software license notices.
 */
class LicensesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CerberusSubScreenScaffold(
                title = "Open Source Licenses",
                onNavigateBack = { finish() }
            ) { innerPadding ->
                val libraries by produceLibraries(R.raw.aboutlibraries)
                LibrariesContainer(
                    libraries = libraries,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}
