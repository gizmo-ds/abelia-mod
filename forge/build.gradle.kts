@file:Suppress("PropertyName", "UnstableApiUsage")

plugins {
    id("com.github.johnrengelman.shadow")
    id("me.shedaniel.unified-publishing") version "0.1.+"
}

loom {
    forge {
        mixinConfig("abelia.mixins.json")
        mixinConfig("abelia-common.mixins.json")
    }
}

architectury {
    platformSetupLoomIde()
    forge()
}

val common: Configuration by configurations.creating
val shadowBundle: Configuration by configurations.creating
val developmentForge: Configuration by configurations.getting

configurations {
    compileOnly.configure { extendsFrom(common) }
    runtimeOnly.configure { extendsFrom(common) }
    developmentForge.extendsFrom(common)

    shadowBundle.isCanBeResolved = true
    shadowBundle.isCanBeConsumed = false
}

dependencies {
    forge("net.minecraftforge:forge:${mod.prop("forge_version")}")

    shadowBundle("org.yaml:snakeyaml:${mod.dep("snakeyaml_version")}")
    forgeRuntimeLibrary("org.yaml:snakeyaml:${mod.dep("snakeyaml_version")}")

    modImplementation(
        group = "me.shedaniel.cloth", name = "cloth-config-forge",
        version = mod.dep("cloth_config")
    ) { exclude(group = "org.yaml", module = "snakeyaml") }

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowBundle(project(path = ":common", configuration = "transformProductionForge"))
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
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

        relocate("org.yaml.snakeyaml", "${mod.group}.libs.snakeyaml")
    }

    remapJar {
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
    }
}

unifiedPublishing {
    project {
        version.set(mod.version)
        displayName.set("v${mod.version}")
        gameVersions.add(mod.minecraft_version)
        gameLoaders.add("forge")
        releaseType.set(mod.release_type)

        mainPublication.set(tasks.remapJar.flatMap { it.archiveFile })

        val modrinthToken: String = env.fetch("MODRINTH_TOKEN", "").trim()
        val modrinthId: String = mod.prop("modrinth_id")
        if (modrinthId.isNotEmpty() && modrinthToken.isNotEmpty()) {
            modrinth {
                token.set(modrinthToken)
                id.set(modrinthId)

                relations {
                    optionals.add("cloth-config")
                }
            }
        }

        val curseforgeToken: String = env.fetch("CF_TOKEN", "").trim()
        val curseforgeId: String = mod.prop("curseforge_id")
        if (curseforgeId.isNotEmpty() && curseforgeToken.isNotEmpty()) {
            curseforge {
                token.set(curseforgeToken)
                id.set(curseforgeId)

                relations {
                    optionals.add("cloth-config")
                }
            }
        }
    }
}
