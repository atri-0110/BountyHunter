plugins {
    id("java-library")
    id("org.allaymc.gradle.plugin") version "0.2.1"
}

group = "org.allaymc.bountyhunter"
description = "A player bounty hunting system for AllayMC servers"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allay {
    api = "0.24.0"

    plugin {
        entrance = ".BountyHunterPlugin"
        authors += "atri-0110"
        website = "https://github.com/atri-0110/BountyHunter"
    }
}

dependencies {
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.34")
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.34")
    implementation(group = "com.google.code.gson", name = "gson", version = "2.11.0")
}
