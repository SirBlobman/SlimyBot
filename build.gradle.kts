plugins {
    id("java")
    id("distribution")
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases/")
    maven("https://nexus.sirblobman.xyz/proxy-jitpack/")
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.5") {
        exclude("club.minnced", "opus-java")
    }

    // Emoji Handler Fork
    implementation("com.github.MinnDevelopment:emoji-java:v6.1.0")

    // Other Dependencies
    implementation("com.github.oshi:oshi-core:6.4.0")
    implementation("com.j2html:j2html:1.6.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("org.xerial:sqlite-jdbc:3.41.0.0")
    implementation("org.yaml:snakeyaml:2.0")
}

distributions {
    main {
        contents {
            into("/target") {
                from(tasks.named("jar"))
                from(configurations["runtimeClasspath"])
            }
        }
    }
}

tasks {
    val baseVersion = findProperty("version.base") ?: ""
    val jenkinsBuildNumber = System.getenv("BUILD_NUMBER") ?: "Unknown"
    val calculatedVersion = "$baseVersion-$jenkinsBuildNumber"

    val mainClassName = findProperty("discord.bot.main") ?: "Main"
    val manifestDependencies = configurations.runtimeClasspath.get().joinToString(" ") { it.name }

    named<Jar>("jar") {
        manifest {
            attributes["Main-Class"] = mainClassName
            attributes["Class-Path"] = manifestDependencies
        }

        archiveFileName.set("SlimyBot-$baseVersion.jar")
    }

    named<Tar>("distTar") {
        enabled = false
    }

    named<Zip>("distZip") {
        archiveFileName.set("SlimyBot-$calculatedVersion.zip")
    }
}
