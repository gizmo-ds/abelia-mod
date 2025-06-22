@file:Suppress("LocalVariableName", "PropertyName")

architectury {
    common(mod.enabled_platforms)
}

loom {
    val accessWidenerFile: File = file("src/main/resources/${mod.id}.accesswidener")
    if (accessWidenerFile.exists()) {
        accessWidenerPath.set(accessWidenerFile)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.yaml:snakeyaml:${mod.dep("snakeyaml_version")}")

    modImplementation("net.fabricmc:fabric-loader:${mod.prop("fabric_loader")}")
    modImplementation("me.shedaniel.cloth:cloth-config:${mod.dep("cloth_config")}")
}

tasks.test {
    useJUnitPlatform()
}