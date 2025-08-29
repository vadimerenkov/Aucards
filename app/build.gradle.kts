plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	id("com.google.devtools.ksp")
	id("org.jetbrains.kotlin.plugin.serialization")
	id("androidx.room")
}

android {
	namespace = "vadimerenkov.aucards"
	compileSdk = 36

	defaultConfig {
		applicationId = "vadimerenkov.aucards"
		minSdk = 26
		targetSdk = 36
		versionCode = 9
		versionName = "2.0.4"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	androidResources {
		generateLocaleConfig = true
	}

	buildTypes {
		debug {
			isMinifyEnabled = false
		}
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		buildConfig = true
		compose = true
	}
	room {
		schemaDirectory("$projectDir/schemas")
	}
	bundle {
		language {
			enableSplit = false
		}
	}
	dependenciesInfo {
		// Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
		includeInApk = false
		// Disables dependency metadata when building Android App Bundles (for Google Play)
		includeInBundle = false
	}
}

dependencies {
	implementation(libs.androidx.lifecycle.viewmodel.compose)
	implementation(libs.androidx.datastore)
	implementation(libs.androidx.datastore.preferences)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(libs.androidx.material3)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.fragment)
	implementation(libs.activity)
	implementation(libs.androidx.activity.ktx)
	implementation(libs.androidx.lifecycle.viewmodel.savedstate)
	implementation(libs.androidx.media3.exoplayer)

	//Room
	implementation(libs.androidx.room.runtime)
	ksp(libs.androidx.room.compiler)

	//Navigation
	implementation(libs.androidx.navigation.compose)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.serialization.core)
	implementation(libs.androidx.adaptive)

	//Colorpicker Compose by skydoves
	implementation(libs.compose.colorpicker)

	//Reorderable by Calvin-LL
	implementation(libs.reorderable)

	testImplementation(libs.junit)
	testImplementation(libs.androidx.truth)
	testImplementation(libs.turbine)
	testImplementation(libs.kotlinx.coroutines.test)
	testImplementation(libs.androidx.lifecycle.viewmodel.testing)
	testImplementation(libs.androidx.ui.test.junit4)
	testImplementation(libs.androidx.navigation.testing)

	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)
	androidTestImplementation(libs.androidx.truth)
	androidTestImplementation(libs.turbine)
	androidTestImplementation(libs.kotlinx.coroutines.test)
	androidTestImplementation(libs.androidx.lifecycle.viewmodel.testing)

	debugImplementation(libs.androidx.ui.tooling)
	debugImplementation(libs.androidx.ui.test.manifest)
}