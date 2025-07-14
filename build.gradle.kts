plugins {
    kotlin("jvm") version "2.1.20-Beta2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("application") // Плагин application
}

group = "dev.loveeev"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.glaremasters.me/repository/towny/") {
        name = "glaremasters repo"
    }
    maven("https://repo.mikeprimm.com/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.ktor:ktor-server-cors:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.2.4")
    implementation("io.ktor:ktor-serialization-gson:2.2.4")
    implementation("com.google.code.gson:gson:2.8.9")
    testImplementation("io.ktor:ktor-server-tests:2.2.4")
    implementation("io.ktor:ktor-server-content-negotiation:2.2.4")
    implementation("io.ktor:ktor-server-content-negotiation:2.2.4")
    compileOnly("com.palmergames.bukkit.towny:towny:0.101.1.0")
    compileOnly("us.dynmap:DynmapCoreAPI:3.7-beta-6")
}

val targetJavaVersion = 17
kotlin {
    jvmToolchain(targetJavaVersion)
}
application {
    mainClass.set("dev.loveeev.townyPainter.TownyPainterKt")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
