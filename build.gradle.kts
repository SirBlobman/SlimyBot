plugins {
    id("java")
    id("distribution")
}

repositories {
    mavenCentral()

    maven {
        name = "sirblobman-public"
        url = uri("https://nexus.sirblobman.xyz/repository/public/")
    }

    maven {
        name = "dv8tion"
        url = uri("https://m2.dv8tion.net/releases/")
    }
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.3") {
        exclude("club.minnced", "opus-java")
    }

    // Emoji Handler Fork
    implementation("com.github.minndevelopment:emoji-java:6.1.0")

    // Other Dependencies
    implementation("com.github.oshi:oshi-core:6.4.0")
    implementation("com.j2html:j2html:1.6.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.xerial:sqlite-jdbc:3.40.0.0")
    implementation("org.yaml:snakeyaml:1.33")
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

    val manifestDependencies = configurations.runtimeClasspath.get().joinToString(" ") { file ->
        file.name
    }

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
