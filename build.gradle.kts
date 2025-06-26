// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.kotlin.android) apply false
	alias(libs.plugins.kotlin.compose) apply false
	id("com.google.devtools.ksp") version "2.1.21-2.0.1" apply false
	alias(libs.plugins.kotlin.serialization)
	id("androidx.room") version "2.7.1" apply false
}