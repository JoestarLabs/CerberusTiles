plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.jaredsburrows.license") version "0.9.8"
}

android {
    namespace = "com.bl4ckswordsman.cerberustiles"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bl4ckswordsman.cerberustiles"
        minSdk = 24
        targetSdk = 34
        versionCode = 20
        versionName = "0.4.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            //signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    licenseReport {
        // Generate reports
        generateCsvReport = false
        generateHtmlReport = true
        generateJsonReport = false
        generateTextReport = false

        // Copy reports - These options are ignored for Java projects
        copyCsvReportToAssets = false
        copyHtmlReportToAssets = false
        copyJsonReportToAssets = true
        copyTextReportToAssets = false
        useVariantSpecificAssetDirs = false

        // Ignore licenses for certain artifact patterns
        // ignoredPatterns = []

        // Show versions in the report - default is false
        showVersions = true
    }
}

composeCompiler {
    enableStrongSkippingMode = true

    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-android:1.7.4")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.4")
    implementation("androidx.compose.runtime:runtime-rxjava2:1.7.4")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation ("io.noties.markwon:core:4.6.2")
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.03"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}