plugins {
    id("net.neoforged.moddev.legacyforge")
}

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

    val props = HashMap<String, String>().apply {
        this["mod_id"] =        prop("mod.id"           )
        this["mod_name"] =      prop("mod.name"         )
        this["mod_version"] =   prop("mod.version"      )
        this["mod_description"]=prop("mod.description"  )
        this["mod_author"] =    prop("mod.author"       )
        this["mod_sources"] =   prop("mod.sources"      )
        this["mod_issues"] =    prop("mod.issues"       )
        this["mod_homepage"] =  prop("mod.homepage"     )
        this["mod_license"] =   prop("mod.license"      )
        this["mod_icon"] =      prop("mod.icon"         )
        this["version_range"] = prop("version_range"    )
    }

    filesMatching("META-INF/mods.toml") {
        expand(props)
    }
}

version = "${property("mod.version")}+${property("deps.minecraft")}-forge"
base.archivesName = property("mod.id") as String

repositories {
    mavenLocal()
    maven("https://maven.minecraftforge.net") { name = "Minecraft Forge" }
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
}

legacyForge {
    version = property("deps.forge") as String
    validateAccessTransformers = true

    if (hasProperty("deps.parchment")) parchment {
        val (mc, ver) = (property("deps.parchment") as String).split(':')
        mappingsVersion = ver
        minecraftVersion = mc
    }

    runs {
        register("client") {
            gameDirectory = file("run/")
            client()
        }
        register("server") {
            gameDirectory = file("run/")
            server()
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.0")!!)
    implementation(jarJar("io.github.llamalad7:mixinextras-forge:0.5.0")!!)
}

tasks {
    processResources {
        exclude("**/fabric.mod.json", "**/*.accesswidener", "**/neoforge.mods.toml")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    withSourcesJar()
    val javaCompat = JavaVersion.VERSION_17
    sourceCompatibility = javaCompat
    targetCompatibility = javaCompat
}