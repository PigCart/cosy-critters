plugins {
    id("dev.isxander.modstitch.base") version "0.7.0-unstable"
    id("dev.kikugie.stonecutter")
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

val minecraft = property("deps.minecraft") as String;

modstitch {
    minecraftVersion = minecraft

    parchment {
        prop("deps.parchment") { mappingsVersion = it }
    }

    metadata {
        modId = "cosycritters"
        modName = "Cosy Critters & Creepy Crawlies"
        modDescription = "Adds adorable atmospheric animals"
        modVersion = "0.1.2+$name"
        modGroup = "pigcart"
        modAuthor = "PigCart"
        modLicense = "MIT"

        fun <K, V> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            // https://github.com/isXander/modstitch/pull/36
            // will still crash if another modstitch 0.7 mod is present
            put("refmap", if (isModDevGradleLegacy) ",\"refmap\": \"unnamed_mod.refmap.json\"" else "")

            put("forge_or_neoforge", if (isModDevGradleLegacy) "forge" else "neoforge")
            put("mod_issue_tracker", "https://github.com/pigcart/cosy-critters/issues")
            put("mod_icon", "assets/cosycritters/textures/particle/crow_right.png")
            put("version_range", property("version_range") as String)
        }
    }

    loom {
        fabricLoaderVersion = "0.17.2"
    }

    moddevgradle {
        prop("deps.forge") { forgeVersion = it }
        prop("deps.neoform") { neoFormVersion = it }
        prop("deps.neoforge") { neoForgeVersion = it }
        prop("deps.mcp") { mcpVersion = it }

        // Configures client and server runs for MDG, it is not done by default
        defaultRuns()

        // This block configures the `neoforge` extension that MDG exposes by default,
        // you can configure MDG like normal from here
        configureNeoForge {
            runs.all {
                disableIdeRun()
            }
        }
    }

    mixin {
        addMixinsToModManifest = true
        configs.register("cosycritters")
    }
}

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
var constraint: String = name.split("-")[1]
stonecutter {
    consts(
        "fabric" to constraint.equals("fabric"),
        "neoforge" to constraint.equals("neoforge"),
        "forge" to constraint.equals("forge")
    )
}

dependencies {
    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabricapi")}")
        modstitchModImplementation("com.terraformersmc:modmenu:${property("modmenu")}")
    }
    // forge
    if (modstitch.isModDevGradleLegacy) {
        compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1")!!)
        "io.github.llamalad7:mixinextras-forge:0.4.1".let {
            modstitchModImplementation(it)
            modstitchJiJ(it)
        }
    }

    // Anything else in the dependencies block will be used for all platforms.
    modstitchModImplementation("dev.isxander:yet-another-config-lib:${property("yacl")}") {
        // weirdness with kotlinforforge-neoforge
        isTransitive = false
    }
    // yacl moment
    implementation("org.quiltmc.parsers:gson:0.2.1")
}