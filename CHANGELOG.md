# Changelog
This project follows [Pride Versioning](https://pridever.org/) ;3

# v0.2.0
###### Dec 9, 2025
- added mc-21.11 support
- added bird flying behaviour
  - very configurable - let me know if you find good settings!
  - flocking with standard boids alignment, cohesion, and separation rules
  - will steer towards the ground when high up
  - will steer away from blocks or land on them if approaching from above
  - will land after a configurable period
- changed bird landing & perching behaviour
  - can now perch anywhere instead of pre-defined blocks
  - will favor locations higher up
  - will continue flying if over water
  - will take flight again after a configurable period
- changed bird spawning behaviour
  - will now spawn on the ground and begin flying instead of spawning in the air and landing
  - will spawn randomly within a configured distance
  - now spawns within a predefined biome tag instead of a predefined block
- tweaked bird flying animation to allow for periods of gliding
- fixed critters spawning in non-overworld dimensions
- refactors and code changes:
  - replaced Yet Another Config Library with the config screen functionality already present in the base game
  - reverted implementation of `ParticleGroup`/`ParticleLimit` due to changes in mc 21.9 making it impossible to create multiple groups with the same limit value, leading to confusing config behaviour
  - replaced modstitch with split build scripts

# v0.1.2
###### Oct 12, 2025
- support minecraft 21.9
- tweak default particle limits
- fix birds spawning near other entities and flying away repeatedly

# v0.1.1
###### Sep 6, 2025
- fix birds continuing to perch on blocks that no longer exist
- fix birds getting stuck on blocks
- birds will now react to all entities and block updates
- add farmland to the list of blocks birds will land on
- add config options for the speed and distance at which birds react

# v0.1.0
###### Sep 3, 2025
- fix crash when birds try to land on a block without collision (ty sametersoylu)
- fix crash when spawning hat man
- fix moths and birds spawning in areas that aren't exposed to the sky
- add support for multiversion neo/forge via Modstitch & Stonecutter
- add yacl config GUI 
  - accessible via `/cosycritters` command or mod menu
- moths now spawn at any light source
- add checking bird behaviour: birds now look around when perched
- add directional sprites: birds now visually face left or right depending on the direction theyre flying
- replace particle tracking with minecraft's built in `ParticleGroup`s
- remove compatibility workaround for sodium's 'animate only visible textures' feature
  - the mod no longer uses mcmeta animations itself so this was not worth maintaining. resource packs that add animated textures to the mod will not function properly unless this sodium feature is disabled.

# v0.0.3a
###### May 16, 2025
- decrease minimum version to 1.21.1
- fix config reloading
- workaround for particle tracking issue (ty suerion)

# v0.0.3
###### May 15, 2025
- Fix spawning limits
- Add basic json config at config/cosycritters.json (ty Suerion)

# v0.0.2
###### Jan 28, 2025
- fix compat with sodium's 'animate only visible textures' feature
- fix crash on minecraft versions 1.21.2 - 1.12.4

# v0.0.1
###### Jan 22, 2025
first release...
- add crows
- add spiders
- add moths
- add hatman
