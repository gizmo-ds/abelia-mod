@file:Suppress("PropertyName", "SpellCheckingInspection", "UnstableApiUsage")

import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    java
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.10-SNAPSHOT" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    id("maven-publish")
}

architectury {
    minecraft = mod.minecraft_version
}

allprojects {
    group = mod.group
    version = mod.version
}

subprojects {
    apply(plugin = "architectury-plugin")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "maven-publish")

    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")

    loom.silentMojangMappingsLicense()

    base.archivesName.set("${mod.id}-${project.name}")

    repositories {
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
        maven {
            name = "Cloth Config"
            url = uri("https://maven.shedaniel.me/")
        }
        maven {
            url = uri("https://cursemaven.com")
            content { includeGroup("curse.maven") }
        }
        maven {
            name = "Modrinth"
            url = uri("https://api.modrinth.com/maven")
            content { includeGroup("maven.modrinth") }
        }
        maven {
            name = "Gitea"
            url = uri("https://git.aika.dev/api/packages/gizmo/maven")
        }
    }

    dependencies {
        "minecraft"("net.minecraft:minecraft:${mod.minecraft_version}")
        "mappings"(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${mod.minecraft_version}:${mod.prop("parchment_version")}@zip")
        })

        compileOnly("org.projectlombok:lombok:1.18.38")
        annotationProcessor("org.projectlombok:lombok:1.18.38")
    }

    java {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.processResources {
        from(rootProject.file("LICENSE"))
        from(rootProject.file("third-party-licenses")) { into("third-party-licenses") }
    }

    publishing {
        publications {
            create<MavenPublication>("gitea") {
                artifactId = base.archivesName.get()
                version = mod.version

                from(components["java"])
            }
        }

        repositories {
            val giteaToken: String = env.fetch("GITEA_TOKEN", "").trim()
            if (giteaToken.isNotEmpty()) {
                maven {
                    name = "Gitea"
                    url = uri("https://git.aika.dev/api/packages/gizmo/maven")

                    credentials(HttpHeaderCredentials::class) {
                        name = "Authorization"
                        value = "token $giteaToken"
                    }
                    authentication { create("header", HttpHeaderAuthentication::class) }
                }
            }
        }
    }
}
