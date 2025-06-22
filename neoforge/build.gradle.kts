@file:Suppress("PropertyName", "UnstableApiUsage")

plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val common: Configuration by configurations.creating
val shadowBundle: Configuration by configurations.creating
val developmentNeoForge: Configuration by configurations.getting

configurations {
    compileOnly.configure { extendsFrom(common) }
    runtimeOnly.configure { extendsFrom(common) }
    developmentNeoForge.extendsFrom(common)

    shadowBundle.isCanBeResolved = true
    shadowBundle.isCanBeConsumed = false
}

repositories {
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
}

dependencies {
    neoForge("net.neoforged:neoforge:${mod.prop("neoforge_version")}")

    shadowBundle("org.yaml:snakeyaml:${mod.dep("snakeyaml_version")}")
    forgeRuntimeLibrary("org.yaml:snakeyaml:${mod.dep("snakeyaml_version")}")

    modImplementation(
        group = "me.shedaniel.cloth", name = "cloth-config-neoforge",
        version = mod.dep("cloth_config")
    ) { exclude(group = "org.yaml", module = "snakeyaml") }

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowBundle(project(path = ":common", configuration = "transformProductionNeoForge"))
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to project.version)
        }
        from(rootProject.file("assets/logo.png")) {
            rename { "${mod.id}-logo.png" }
        }
    }

    shadowJar {
        exclude("architectury.common.json", "META-INF/maven/**/*", "META-INF/versions/**/*")

        configurations = listOf(shadowBundle)
        archiveClassifier.set("dev-shadow")

        mergeServiceFiles()

        relocate("org.yaml.snakeyaml", "${mod.group}.${mod.id}.libs.snakeyaml")
    }

    remapJar {
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
    }
}