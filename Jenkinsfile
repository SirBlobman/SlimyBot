pipeline {
    agent any

    options {
        githubProjectProperty(projectUrlStr: "https://github.com/SirBlobman/SlimyBot")
    }

    triggers {
        githubPush()
    }

    tools {
        jdk "JDK 17"
    }

    stages {
        stage("Gradle: Build") {
            steps {
                withGradle {
                    sh("./gradlew clean build --refresh-dependencies --no-daemon")
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'build/distributions/SlimyBot-*.tar.gz,build/libs/SlimyBot-*.jar', fingerprint: true
        }
    }
}
