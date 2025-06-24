import org.gradle.kotlin.dsl.testImplementation

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	id("com.google.devtools.ksp")
	id("org.jetbrains.kotlin.plugin.serialization")
}

android {
	namespace = "vadimerenkov.aucards"
	compileSdk = 35

	defaultConfig {
		applicationId = "vadimerenkov.aucards"
		minSdk = 26
		targetSdk = 35
		versionCode = 1
		versionName = "1.0.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	androidResources {
		generateLocaleConfig = true
	}

	buildTypes {
		release {
			isMinifyEnabled = true
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

	//Room
	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.fragment)
	implementation(libs.activity)
	implementation(libs.androidx.activity.ktx)
	implementation(libs.androidx.navigation.testing)
	implementation(libs.androidx.lifecycle.viewmodel.savedstate)


	ksp(libs.androidx.room.compiler)

	//Navigation
	implementation(libs.androidx.navigation.compose)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.serialization.core)

	//Colorpicker Compose
	implementation(libs.compose.colorpicker)

	testImplementation(libs.junit)
	testImplementation(libs.androidx.truth)
	testImplementation(libs.turbine)
	testImplementation(libs.kotlinx.coroutines.test)
	testImplementation(libs.androidx.lifecycle.viewmodel.testing)
	testImplementation(libs.androidx.ui.test.junit4)

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