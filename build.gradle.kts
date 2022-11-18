import java.util.Properties

data class PropertiesVersion(val properties: Properties) {
    private val major = properties["build.version.major"]
    private val minor = properties["build.version.minor"]
    private val revision = properties["build.version.revision"]
    private val build = properties["build.number"]

    override fun toString(): String {
        return "${this.major}.${this.minor}.${this.revision}.${this.build}"
    }
}

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.0"
}

group = "org.codebrewer"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.codebrewer.intellij.platform:intellij-platform-plugin-utilities:1.0.2")
}

intellij {
    pluginName.set("DilbertDailyStrip")
    version.set("223-EAP-SNAPSHOT")
    type.set("IC")
}

tasks {
    register("updateBuildData") {
        doFirst {
            ant.withGroovyBuilder {
                "propertyfile"("file" to "src/main/resources/org/codebrewer/intellijplatform/plugin/dilbert/build/build.properties") {
                    "entry"("key" to "build.number", "type" to "int", "default" to "1", "operation" to "+")
                    "entry"("key" to "build.date", "type" to "date", "value" to "now", "pattern" to "MMMM d, yyyy")
                }
            }
        }
    }

    findByName("patchPluginXml")?.dependsOn("updateBuildData")

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("223")
        untilBuild.set("")

        val buildProperties = Properties()
        buildProperties.load(file("src/main/resources/org/codebrewer/intellijplatform/plugin/dilbert/build/build.properties").inputStream())
        version.set(PropertiesVersion(buildProperties).toString())
    }
}
