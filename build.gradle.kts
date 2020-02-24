import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	val kotlinVer = "1.3.60"
	id("org.jetbrains.kotlin.jvm").version(kotlinVer)
	id("org.jetbrains.kotlin.plugin.serialization").version(kotlinVer)

	id("io.gitlab.arturbosch.detekt").version("1.5.1")
}

repositories {
	jcenter()
	mavenCentral()
}

dependencies {
	//
	// Main dependency
	//
	implementation(kotlin("stdlib-jdk8"))
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.11.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.1")
	implementation("com.github.ajalt:clikt:2.1.0")
	implementation("com.google.guava:guava:23.0")
	val log4jVer = "2.12.1"
	implementation("org.apache.logging.log4j:log4j-api:$log4jVer")
	implementation("org.apache.logging.log4j:log4j-core:$log4jVer")
	implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVer")
	implementation("io.github.microutils:kotlin-logging:1.7.6")

	//
	// Test dependency
	//
	testImplementation(kotlin("test-junit"))
	val spekVer = "2.0.6"
	testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVer")
	testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVer")
	testImplementation("io.strikt:strikt-core:0.21.1")
	testImplementation("io.mockk:mockk:1.9")
	val testfxVer = "4.0.16-alpha"
	testImplementation("org.testfx:testfx-core:$testfxVer")
	testImplementation("org.testfx:testfx-junit5:$testfxVer")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
}

tasks.clean {
	delete("$rootDir/out")
	delete("$rootDir/ygomaker.jar")
}

tasks.register<Detekt>("lint") {
	config.setFrom(".detekt.yml")
	source = fileTree("src")
	include("**/*.kt")
}

tasks.compileKotlin {
	kotlinOptions {
		jvmTarget = "1.8"
		freeCompilerArgs = listOf(
				"-XXLanguage:+InlineClasses"
		)
	}
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
		exceptionFormat = TestExceptionFormat.FULL
	}
}

tasks.check {
	// Exlucde the default :detekt task from :check task
	setDependsOn(dependsOn.filterNot {
		it is TaskProvider<*> && it.name == "detekt"
	})
}

tasks.register<Jar>("pack") {
	dependsOn(configurations.runtimeClasspath)

	manifest {
		attributes["Main-Class"] = "me.nekomatamune.ygomaker.MainKt"
	}

	destinationDirectory.set(rootDir)
	from(sourceSets.main.get().output)
	from({
		configurations.runtimeClasspath.get().filter {
			it.name.endsWith("jar")
		}.map { zipTree(it) }
	})
}
