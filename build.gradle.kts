fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.11.0"
    id("org.jetbrains.changelog") version "2.0.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(11)
}

intellij {
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
//    localPath.set("/Users/dingshichen/Library/Application Support/JetBrains/Toolbox/apps/IDEA-U/ch-0/223.7571.182/IntelliJ IDEA.app")

    plugins.set(listOf("com.intellij.java", "com.intellij.database"))
}

changelog {
    path.set(file("CHANGELOG.md").canonicalPath)
}

tasks {

    buildSearchableOptions {
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

//    signPlugin {
//        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
//        privateKey.set(System.getenv("PRIVATE_KEY"))
//        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
//    }

//    publishPlugin {
//        token.set(System.getenv("PUBLISH_TOKEN"))
//    }
}
