plugins {
    kotlin("jvm") version "1.9.21"
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jgrapht", "jgrapht-core", "1.5.0")
    implementation("com.brunomnsilva:smartgraph:2.0.0")
   // implementation("ai.hypergraph:kaliningraph:0.1.8")
}

javafx {
    version = "17.0.8"
    modules("javafx.controls")
}


kotlin {
    jvmToolchain(17)
}
