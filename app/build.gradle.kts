import org.gradle.internal.impldep.bsh.commands.dir

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.busmap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.busmap"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)
    implementation(libs.recyclerview)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
//    implementation(fileTree(mapOf(
//        "dir" to "E:\\Nghiên cứu khoa học\\zalopay",
//        "include" to listOf("*.aar", "*.jar"),
//        "exclude" to listOf("")
//    )))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.squareup.okhttp3:okhttp:4.6.0")
    implementation("commons-codec:commons-codec:1.14")
    //implementation ("com.google.android.gms:play-services-maps:19.0.0")
    implementation ("com.google.android.gms:play-services-maps:18.0.2")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation ("com.google.maps.android:android-maps-utils:2.2.3")
    implementation ("com.google.android.libraries.places:places:3.4.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation ("com.google.firebase:firebase-database:20.0.5")
    implementation ("com.google.firebase:firebase-auth:21.1.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
}