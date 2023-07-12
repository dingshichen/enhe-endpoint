fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.jetbrains.intellij") version "1.13.3"
    id("org.jetbrains.changelog") version "2.0.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

intellij {
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
//    localPath.set(System.getenv("APP_PATH"))

    plugins.set(listOf("com.intellij.java", "com.intellij.database"))
}

changelog {
    path.set(file("CHANGELOG.md").canonicalPath)
    header.set(provider { version.get() })
}

tasks {

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
