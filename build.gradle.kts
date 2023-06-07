val baseVersion = findProperty("version.base") ?: ""
val jenkinsBuildNumber = System.getenv("BUILD_NUMBER") ?: "Unknown"
version = "$baseVersion-$jenkinsBuildNumber"

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
    // Java Dependencies
    compileOnly("org.jetbrains:annotations:24.0.1") // JetBrains Annotations

    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.9") {
        exclude("club.minnced", "opus-java") // Exclude Opus
    }

    // Other Dependencies
    implementation("com.github.MinnDevelopment:emoji-java:v6.1.0") // Emoji Handler Fork
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4") // MariaDB Java Client
    implementation("com.github.oshi:oshi-core:6.4.3") // Operating System Hardware Information Core
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0") // Log4J SLF4J2 Implementation
    implementation("org.xerial:sqlite-jdbc:3.42.0.0") // SQLite
    implementation("org.yaml:snakeyaml:2.0") // SnakeYAML
}

distributions {
    main {
        contents {
            into("/target") {
                from(tasks.named("jar")) // Main Jar File
                from(configurations["runtimeClasspath"]) // Dependency Jar Files
            }
        }
    }
}

tasks {

    named<Jar>("jar") {
        version = baseVersion
        archiveBaseName.set("SlimyBot")
        val mainClassName = findProperty("discord.bot.main") ?: "Main"
        val manifestDependencies = configurations.runtimeClasspath.get().joinToString(" ") { it.name }

        manifest {
            attributes["Main-Class"] = mainClassName
            attributes["Class-Path"] = manifestDependencies
        }
    }

    named<Tar>("distTar") {
        compression = Compression.GZIP
        archiveExtension.set(".tar.gz")
        archiveBaseName.set("SlimyBot")
    }

    named<Zip>("distZip") {
        enabled = false
    }
}
