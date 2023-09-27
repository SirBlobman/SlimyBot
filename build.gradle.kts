val baseVersion = findProperty("version.base") ?: ""
val jenkinsBuildNumber = System.getenv("BUILD_NUMBER") ?: "Unknown"
version = "$baseVersion-$jenkinsBuildNumber"

plugins {
    id("java")
    id("distribution")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
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
    implementation("net.dv8tion:JDA:5.0.0-beta.13") {
        exclude(module = "opus-java") // Exclude Opus
    }

    // Other Dependencies
    implementation("com.github.MinnDevelopment:emoji-java:v6.1.0") // Emoji Handler Fork
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4") // MariaDB Java Client
    implementation("com.github.oshi:oshi-core:6.4.6") // Operating System Hardware Information Core
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0") // Log4J SLF4J2 Implementation
    implementation("org.yaml:snakeyaml:2.1") // SnakeYAML
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
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:unchecked")
    }

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
        archiveExtension.set("tar.gz")
        archiveBaseName.set("SlimyBot")
    }

    named<Zip>("distZip") {
        enabled = false
    }
}
