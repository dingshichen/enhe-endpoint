fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenLocal()
    mavenCentral()
}

intellij {
    val appPath = System.getenv("APP_PATH")
    if (appPath == null) {
        version.set(properties("platformVersion"))
        type.set(properties("platformType"))
    } else {
        localPath.set(appPath)
    }
    updateSinceUntilBuild.set(false)
    sameSinceUntilBuild.set(false)
    plugins.set(listOf("com.intellij.java", "com.intellij.database", "markdown"))
}

dependencies {
    implementation("com.enhe.maotai:core:1.3.0")
}

changelog {
    path.set(file("CHANGELOG.md").canonicalPath)
    header.set(provider { version.get() })
}

tasks {

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    buildSearchableOptions {
        enabled = false
    }

    jarSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        sinceBuild.set(properties("pluginSinceBuild"))
        changeNotes.set(provider {
            with(changelog) {
                renderItem(
                    getOrNull(properties("pluginVersion")) ?: getLatest(),
                    org.jetbrains.changelog.Changelog.OutputType.HTML,
                )
            }
        })
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
