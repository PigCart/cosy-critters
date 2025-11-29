import org.gradle.kotlin.dsl.accessTransformers
import org.gradle.kotlin.dsl.from

plugins {
    id("net.neoforged.moddev")
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

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(props)
    }
}

version = "${property("mod.version")}+${property("deps.minecraft")}-neoforge"
base.archivesName = property("mod.id") as String

repositories {
    mavenLocal()
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
}

neoForge {
    version = property("deps.neoforge") as String

    val accessTransformer = project.file("src/main/resources/META-INF/accesstransformer.cfg")
    if (accessTransformer.exists()) {
        accessTransformers.from(accessTransformer.absolutePath)
    }

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
    //sourceSets["main"].resources.srcDir("src/main/generated")
}

tasks {
    processResources {
        exclude("**/fabric.mod.json", "**/*.accesswidener", "**/mods.toml")
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
    val javaCompat = JavaVersion.VERSION_21
    sourceCompatibility = javaCompat
    targetCompatibility = javaCompat
}