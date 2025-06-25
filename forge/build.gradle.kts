@file:Suppress("PropertyName", "UnstableApiUsage", "SpellCheckingInspection")

plugins {
    id("com.github.johnrengelman.shadow")
    id("com.modrinth.minotaur").version("2.+")
    id("net.darkhax.curseforgegradle").version("1.+")
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
        archiveAppendix.set(mod.minecraft_version)
        archiveVersion.set("v${mod.version}")
        dependsOn(shadowJar)
    }
}

tasks.register<net.darkhax.curseforgegradle.TaskPublishCurseForge>("curseforge") {
    group = "publishing"

    val curseforgeToken: String = env.fetch("CF_TOKEN", "").trim()
    val curseforgeId: String = mod.prop("curseforge_id")
    if (curseforgeId.isEmpty() || curseforgeToken.isEmpty()) {
        isEnabled = false
        return@register
    }

    apiToken = curseforgeToken
    val mainFile = upload(curseforgeId, tasks.remapJar.flatMap { it.archiveFile })
    mainFile.releaseType = mod.release_type
    mainFile.gameVersions.addAll(mod.game_version_supports)
    mainFile.addModLoader(loom.platform.get().displayName())
    mainFile.addOptional("cloth-config")
    mainFile.changelog = ""
}

val modrinthToken: String = env.fetch("MODRINTH_TOKEN", "").trim()
val modrinthId: String = mod.prop("modrinth_id")
if (modrinthId.isNotEmpty() && modrinthToken.isNotEmpty()) {
    modrinth {
        token.set(modrinthToken)
        projectId.set(modrinthId)

        val readme = rootProject.file("README.md").readText()
        if (readme.isNotEmpty()) syncBodyFrom.set(readme)

        versionNumber.set(mod.version)
        versionType.set(mod.release_type)
        uploadFile.set(tasks.remapJar.flatMap { it.archiveFile })
        gameVersions.addAll(mod.game_version_supports)
        loaders.add(loom.platform.get().id())
        dependencies {
            optional.project("cloth-config")
        }
    }
}
