package com.dexter.app.data.repository

import com.dexter.app.domain.model.PokemonType
import com.dexter.app.domain.model.region.GymLeader
import com.dexter.app.domain.model.region.LegendaryEncounter
import com.dexter.app.domain.model.region.LocationType
import com.dexter.app.domain.model.region.Region
import com.dexter.app.domain.model.region.RegionLocation
import com.dexter.app.domain.model.region.WildSpawn

object RegionData {

    val KANTO = Region(
        id = "kanto",
        number = 1,
        name = "Kanto",
        japaneseName = "カントー地方 (Kantō-chihō)",
        tagline = "The Origin of All Adventures",
        description = "A temperate region located east of Johto. Connected by scenic routes, dense forests, deep subterranean caves, and sea channels. Home to Professor Oak's research laboratory and the prestigious Indigo Plateau.",
        professor = "Professor Samuel Oak",
        villainTeam = "Team Rocket (Giovanni)",
        starterIds = listOf(1, 4, 7), // Bulbasaur, Charmander, Squirtle
        legendaryIds = listOf(144, 145, 146, 150, 151), // Articuno, Zapdos, Moltres, Mewtwo, Mew
        musicTheme = "Nostalgic 8-bit fanfares, acoustic guitar, and vibrant brass trumpets.",
        locations = listOf(
            RegionLocation(
                id = "pallet_town",
                name = "Pallet Town",
                type = LocationType.TOWN,
                description = "A quiet, tranquil hometown where budding Trainers receive their first partner Pokémon from Professor Oak.",
                normalizedX = 0.22f,
                normalizedY = 0.82f,
                connectedToIds = listOf("route_1", "route_21"),
                wildSpawns = listOf(
                    WildSpawn(16, "Pidgey", 2, 5, "Tall Grass", "Common"),
                    WildSpawn(19, "Rattata", 2, 4, "Tall Grass", "Common"),
                    WildSpawn(129, "Magikarp", 5, 10, "Old Rod Fishing", "Very Common"),
                    WildSpawn(60, "Poliwag", 10, 15, "Good Rod Fishing", "Uncommon")
                ),
                musicThemeDescription = "Gentle acoustic guitar and soft woodwinds representing the start of a journey."
            ),
            RegionLocation(
                id = "route_1",
                name = "Route 1",
                type = LocationType.ROUTE,
                description = "A grassy path with small ledges connecting Pallet Town and Viridian City.",
                normalizedX = 0.22f,
                normalizedY = 0.70f,
                connectedToIds = listOf("pallet_town", "viridian_city"),
                wildSpawns = listOf(
                    WildSpawn(16, "Pidgey", 2, 5, "Tall Grass", "Very Common"),
                    WildSpawn(19, "Rattata", 2, 4, "Tall Grass", "Very Common")
                ),
                musicThemeDescription = "Bright, upbeat brass marching melody."
            ),
            RegionLocation(
                id = "viridian_city",
                name = "Viridian City",
                type = LocationType.CITY,
                description = "The gateway to the Indigo Plateau. Houses the mysterious 8th Gym led by Giovanni.",
                normalizedX = 0.22f,
                normalizedY = 0.58f,
                connectedToIds = listOf("route_1", "viridian_forest", "route_22"),
                gymLeader = GymLeader(
                    name = "Giovanni",
                    title = "The Grounded Boss",
                    badgeName = "Earth Badge",
                    badgeEmoji = "🌍",
                    specialtyType = PokemonType.GROUND,
                    acePokemonId = 34,
                    acePokemonName = "Nidoking"
                ),
                wildSpawns = listOf(
                    WildSpawn(60, "Poliwag", 5, 15, "Surfing", "Common"),
                    WildSpawn(54, "Psyduck", 15, 25, "Surfing", "Rare")
                ),
                musicThemeDescription = "Stately orchestral strings welcoming travelers into the metropolis."
            ),
            RegionLocation(
                id = "viridian_forest",
                name = "Viridian Forest",
                type = LocationType.FOREST,
                description = "A deep, verdant labyrinth of trees buzzing with Bug-type Pokémon and wild Pikachu.",
                normalizedX = 0.22f,
                normalizedY = 0.44f,
                connectedToIds = listOf("viridian_city", "pewter_city"),
                wildSpawns = listOf(
                    WildSpawn(10, "Caterpie", 3, 6, "Tall Grass", "Very Common"),
                    WildSpawn(13, "Weedle", 3, 6, "Tall Grass", "Very Common"),
                    WildSpawn(11, "Metapod", 4, 7, "Tall Grass", "Common"),
                    WildSpawn(14, "Kakuna", 4, 7, "Tall Grass", "Common"),
                    WildSpawn(25, "Pikachu", 3, 5, "Tall Grass", "Rare")
                ),
                musicThemeDescription = "Mysterious plucked harps and ambient insect chitters in the canopy."
            ),
            RegionLocation(
                id = "pewter_city",
                name = "Pewter City",
                type = LocationType.CITY,
                description = "A stony town resting at the base of Mt. Moon. Home to the Pewter Museum of Science and Brock's Rock Gym.",
                normalizedX = 0.22f,
                normalizedY = 0.30f,
                connectedToIds = listOf("viridian_forest", "mt_moon"),
                gymLeader = GymLeader(
                    name = "Brock",
                    title = "The Rock-Solid Pokémon Trainer",
                    badgeName = "Boulder Badge",
                    badgeEmoji = "🪨",
                    specialtyType = PokemonType.ROCK,
                    acePokemonId = 95,
                    acePokemonName = "Onix"
                ),
                musicThemeDescription = "Solid, grounded rhythmic march echoing off stone buildings."
            ),
            RegionLocation(
                id = "mt_moon",
                name = "Mt. Moon",
                type = LocationType.CAVE,
                description = "A mystical cavern renowned for frequent meteor falls and Clefairy Moon Dance rituals.",
                normalizedX = 0.40f,
                normalizedY = 0.30f,
                connectedToIds = listOf("pewter_city", "cerulean_city"),
                wildSpawns = listOf(
                    WildSpawn(41, "Zubat", 6, 11, "Cave Floor", "Very Common"),
                    WildSpawn(74, "Geodude", 7, 10, "Cave Floor", "Common"),
                    WildSpawn(46, "Paras", 8, 12, "Cave Floor", "Uncommon"),
                    WildSpawn(35, "Clefairy", 8, 12, "Cave Floor", "Rare")
                ),
                musicThemeDescription = "Eerie, echoing synth reverberating off subterranean stalactites."
            ),
            RegionLocation(
                id = "cerulean_city",
                name = "Cerulean City",
                type = LocationType.CITY,
                description = "A floral city enclosed by waterways. Home to Misty's Water Gym and the entrance to Cerulean Cave.",
                normalizedX = 0.58f,
                normalizedY = 0.30f,
                connectedToIds = listOf("mt_moon", "cerulean_cave", "saffron_city", "vermilion_city"),
                gymLeader = GymLeader(
                    name = "Misty",
                    title = "The Tomboyish Mermaid",
                    badgeName = "Cascade Badge",
                    badgeEmoji = "💧",
                    specialtyType = PokemonType.WATER,
                    acePokemonId = 121,
                    acePokemonName = "Starmie"
                ),
                musicThemeDescription = "Flowing harp and aquatic mallet chimes."
            ),
            RegionLocation(
                id = "cerulean_cave",
                name = "Cerulean Cave",
                type = LocationType.LEGENDARY_LAIR,
                description = "A forbidden cavern filled with extremely ferocious Pokémon. At its deepest floor rests the ultimate genetic Pokémon.",
                normalizedX = 0.54f,
                normalizedY = 0.22f,
                connectedToIds = listOf("cerulean_city"),
                legendary = LegendaryEncounter(
                    pokemonId = 150,
                    pokemonName = "Mewtwo",
                    encounterType = "Static Boss Encounter",
                    level = 70,
                    requirementText = "Defeat the Elite Four and become Kanto Champion to enter."
                ),
                wildSpawns = listOf(
                    WildSpawn(64, "Kadabra", 55, 60, "Cave Floor", "Common"),
                    WildSpawn(82, "Magneton", 55, 60, "Cave Floor", "Common"),
                    WildSpawn(112, "Rhydon", 58, 62, "Cave Floor", "Uncommon"),
                    WildSpawn(113, "Chansey", 56, 60, "Cave Floor", "Rare"),
                    WildSpawn(130, "Gyarados", 55, 65, "Surfing", "Uncommon")
                ),
                musicThemeDescription = "Tense, pulsing techno baseline with dread-inducing string climaxes."
            ),
            RegionLocation(
                id = "vermilion_city",
                name = "Vermilion City",
                type = LocationType.CITY,
                description = "A sunny southern seaport where the luxury liner S.S. Anne docks. Lt. Surge commands the Electric Gym.",
                normalizedX = 0.58f,
                normalizedY = 0.58f,
                connectedToIds = listOf("cerulean_city", "saffron_city", "lavender_town", "power_plant"),
                gymLeader = GymLeader(
                    name = "Lt. Surge",
                    title = "The Lightning American",
                    badgeName = "Thunder Badge",
                    badgeEmoji = "⚡",
                    specialtyType = PokemonType.ELECTRIC,
                    acePokemonId = 26,
                    acePokemonName = "Raichu"
                ),
                wildSpawns = listOf(
                    WildSpawn(96, "Drowzee", 11, 15, "Tall Grass", "Common"),
                    WildSpawn(50, "Diglett", 15, 22, "Diglett's Cave", "Very Common"),
                    WildSpawn(51, "Dugtrio", 29, 31, "Diglett's Cave", "Rare")
                ),
                musicThemeDescription = "Bright coastal acoustic rhythm with ocean horn fanfares."
            ),
            RegionLocation(
                id = "power_plant",
                name = "Abandoned Power Plant",
                type = LocationType.LEGENDARY_LAIR,
                description = "An abandoned industrial generator facility now overgrown with electric sparks and mechanical traps.",
                normalizedX = 0.78f,
                normalizedY = 0.38f,
                connectedToIds = listOf("vermilion_city", "lavender_town"),
                legendary = LegendaryEncounter(
                    pokemonId = 145,
                    pokemonName = "Zapdos",
                    encounterType = "Static Boss Encounter",
                    level = 50,
                    requirementText = "Surf down Route 10 to reach the remote turbine facility."
                ),
                wildSpawns = listOf(
                    WildSpawn(81, "Magnemite", 21, 24, "Floor", "Common"),
                    WildSpawn(100, "Voltorb", 21, 25, "Floor", "Very Common"),
                    WildSpawn(101, "Electrode", 32, 40, "Fake Item Traps", "Uncommon"),
                    WildSpawn(125, "Electabuzz", 33, 36, "Floor", "Rare")
                ),
                musicThemeDescription = "Buzzing synthesizer sparks and industrial metallic percussion."
            ),
            RegionLocation(
                id = "lavender_town",
                name = "Lavender Town",
                type = LocationType.TOWN,
                description = "A somber, mystical town shrouded in purple fog. Home to the Pokémon Tower where departed Pokémon rest.",
                normalizedX = 0.82f,
                normalizedY = 0.48f,
                connectedToIds = listOf("power_plant", "celadon_city", "fuchsia_city"),
                wildSpawns = listOf(
                    WildSpawn(92, "Gastly", 18, 24, "Pokémon Tower", "Very Common"),
                    WildSpawn(93, "Haunter", 20, 25, "Pokémon Tower", "Uncommon"),
                    WildSpawn(104, "Cubone", 17, 22, "Pokémon Tower", "Rare")
                ),
                musicThemeDescription = "The iconic haunting chiptune melody in C Minor that echoed through gaming history."
            ),
            RegionLocation(
                id = "celadon_city",
                name = "Celadon City",
                type = LocationType.CITY,
                description = "The bustling commercial jewel of Kanto, featuring the massive Department Store, Game Corner, and Erika's Grass Gym.",
                normalizedX = 0.46f,
                normalizedY = 0.48f,
                connectedToIds = listOf("saffron_city", "lavender_town", "fuchsia_city"),
                gymLeader = GymLeader(
                    name = "Erika",
                    title = "The Nature-Loving Princess",
                    badgeName = "Rainbow Badge",
                    badgeEmoji = "🌈",
                    specialtyType = PokemonType.GRASS,
                    acePokemonId = 45,
                    acePokemonName = "Vileplume"
                ),
                wildSpawns = listOf(
                    WildSpawn(43, "Oddish", 22, 26, "Tall Grass", "Common"),
                    WildSpawn(69, "Bellsprout", 22, 26, "Tall Grass", "Common"),
                    WildSpawn(133, "Eevee", 25, 25, "Celadon Mansion Gift", "Unique")
                ),
                musicThemeDescription = "Upbeat, energetic urban jazz with accordion melodies."
            ),
            RegionLocation(
                id = "saffron_city",
                name = "Saffron City",
                type = LocationType.CITY,
                description = "The sprawling golden metropolis at the central crossroads of Kanto. Dominated by the Silph Co. skyscraper and Sabrina's Psychic Gym.",
                normalizedX = 0.58f,
                normalizedY = 0.48f,
                connectedToIds = listOf("cerulean_city", "vermilion_city", "celadon_city", "lavender_town"),
                gymLeader = GymLeader(
                    name = "Sabrina",
                    title = "The Master of Psychic Pokémon",
                    badgeName = "Marsh Badge",
                    badgeEmoji = "🔮",
                    specialtyType = PokemonType.PSYCHIC,
                    acePokemonId = 65,
                    acePokemonName = "Alakazam"
                ),
                wildSpawns = listOf(
                    WildSpawn(106, "Hitmonlee", 30, 30, "Fighting Dojo Choice", "Unique"),
                    WildSpawn(107, "Hitmonchan", 30, 30, "Fighting Dojo Choice", "Unique"),
                    WildSpawn(131, "Lapras", 25, 25, "Silph Co. Employee Gift", "Unique")
                ),
                musicThemeDescription = "High-tech futuristic synth beats pulsing through modern transit."
            ),
            RegionLocation(
                id = "fuchsia_city",
                name = "Fuchsia City",
                type = LocationType.CITY,
                description = "A historic ninja settlement nestled on the southern coast. Home to Koga's Poison Gym and the vast Safari Zone.",
                normalizedX = 0.58f,
                normalizedY = 0.76f,
                connectedToIds = listOf("celadon_city", "lavender_town", "seafoam_islands"),
                gymLeader = GymLeader(
                    name = "Koga",
                    title = "The Poisonous Shadow",
                    badgeName = "Soul Badge",
                    badgeEmoji = "☠️",
                    specialtyType = PokemonType.POISON,
                    acePokemonId = 110,
                    acePokemonName = "Weezing"
                ),
                wildSpawns = listOf(
                    WildSpawn(115, "Kangaskhan", 25, 28, "Safari Zone Area 1", "Rare"),
                    WildSpawn(123, "Scyther", 23, 28, "Safari Zone Area 1", "Rare"),
                    WildSpawn(127, "Pinsir", 23, 28, "Safari Zone Area 1", "Rare"),
                    WildSpawn(128, "Tauros", 25, 28, "Safari Zone Area 3", "Very Rare"),
                    WildSpawn(147, "Dratini", 10, 15, "Super Rod (Safari Zone)", "Rare")
                ),
                musicThemeDescription = "Traditional Japanese bamboo flute and wooden clappers."
            ),
            RegionLocation(
                id = "seafoam_islands",
                name = "Seafoam Islands",
                type = LocationType.LEGENDARY_LAIR,
                description = "A twin pair of glacial sea caverns with turbulent whirlpools and chilling ice flows.",
                normalizedX = 0.44f,
                normalizedY = 0.88f,
                connectedToIds = listOf("fuchsia_city", "cinnabar_island"),
                legendary = LegendaryEncounter(
                    pokemonId = 144,
                    pokemonName = "Articuno",
                    encounterType = "Static Boss Encounter",
                    level = 50,
                    requirementText = "Solve the boulder drop puzzles to stop the rushing currents."
                ),
                wildSpawns = listOf(
                    WildSpawn(86, "Seel", 28, 33, "Water / Ice Floors", "Common"),
                    WildSpawn(87, "Dewgong", 32, 38, "Ice Floors", "Uncommon"),
                    WildSpawn(90, "Shellder", 25, 30, "Super Rod", "Common"),
                    WildSpawn(116, "Horsea", 25, 30, "Super Rod", "Common"),
                    WildSpawn(79, "Slowpoke", 26, 31, "Surfing", "Common")
                ),
                musicThemeDescription = "Chilling crystalline bells and deep subterranean rushing water."
            ),
            RegionLocation(
                id = "cinnabar_island",
                name = "Cinnabar Island",
                type = LocationType.CITY,
                description = "A volcanic island housing the Pokémon Research Lab, the scorched Pokémon Mansion, and Blaine's Fire Gym.",
                normalizedX = 0.22f,
                normalizedY = 0.88f,
                connectedToIds = listOf("seafoam_islands", "pallet_town"),
                gymLeader = GymLeader(
                    name = "Blaine",
                    title = "The Hotheaded Quiz Master",
                    badgeName = "Volcano Badge",
                    badgeEmoji = "🔥",
                    specialtyType = PokemonType.FIRE,
                    acePokemonId = 59,
                    acePokemonName = "Arcanine"
                ),
                wildSpawns = listOf(
                    WildSpawn(77, "Ponyta", 30, 36, "Pokémon Mansion", "Common"),
                    WildSpawn(126, "Magmar", 34, 38, "Pokémon Mansion Basement", "Rare"),
                    WildSpawn(88, "Grimer", 28, 32, "Pokémon Mansion", "Common"),
                    WildSpawn(109, "Koffing", 28, 32, "Pokémon Mansion", "Common")
                ),
                musicThemeDescription = "Island marimba and warm tropical steel drums."
            ),
            RegionLocation(
                id = "indigo_plateau",
                name = "Indigo Plateau",
                type = LocationType.POKEMON_LEAGUE,
                description = "The ultimate pinnacle of strength where the Elite Four (Lorelei, Bruno, Agatha, Lance) and Champion Blue await challengers.",
                normalizedX = 0.12f,
                normalizedY = 0.22f,
                connectedToIds = listOf("viridian_city"),
                legendary = LegendaryEncounter(
                    pokemonId = 146,
                    pokemonName = "Moltres",
                    encounterType = "Static Boss Encounter (Victory Road)",
                    level = 50,
                    requirementText = "Found deep within the cavernous trials of Victory Road."
                ),
                wildSpawns = listOf(
                    WildSpawn(67, "Machoke", 40, 45, "Victory Road", "Common"),
                    WildSpawn(75, "Graveler", 40, 45, "Victory Road", "Common"),
                    WildSpawn(95, "Onix", 42, 48, "Victory Road", "Uncommon"),
                    WildSpawn(148, "Dragonair", 45, 52, "Victory Road Water", "Rare")
                ),
                musicThemeDescription = "Triumphant, heroic orchestral symphony fit for the Champion."
            )
        )
    )

    val JOHTO = Region(
        id = "johto",
        number = 2,
        name = "Johto",
        japaneseName = "ジョウト地方 (Jōto-chihō)",
        tagline = "Ancient Lore & Sacred Shrines",
        description = "A region steeped in history, ancient myths, and spiritual reverence. Famous for traditional wooden pagodas, cherry blossom groves, and the twin guardian birds of storm and sea.",
        professor = "Professor Elm",
        villainTeam = "Team Rocket Remnants (Archer & Petrel)",
        starterIds = listOf(152, 155, 158), // Chikorita, Cyndaquil, Totodile
        legendaryIds = listOf(243, 244, 245, 249, 250, 251), // Raikou, Entei, Suicune, Lugia, Ho-Oh, Celebi
        musicTheme = "Traditional shamisen, shakuhachi flutes, and soothing acoustic strings.",
        locations = listOf(
            RegionLocation(
                id = "new_bark_town",
                name = "New Bark Town",
                type = LocationType.TOWN,
                description = "The Town Where the Winds of a New Beginning Blow. Home of Professor Elm's laboratory.",
                normalizedX = 0.85f,
                normalizedY = 0.70f,
                connectedToIds = listOf("cherrygrove_city", "route_27"),
                wildSpawns = listOf(
                    WildSpawn(161, "Sentret", 2, 4, "Tall Grass", "Very Common"),
                    WildSpawn(163, "Hoothoot", 2, 4, "Tall Grass (Night)", "Very Common"),
                    WildSpawn(165, "Ledyba", 2, 4, "Tall Grass (Morning)", "Common"),
                    WildSpawn(167, "Spinarak", 2, 4, "Tall Grass (Night)", "Common")
                ),
                musicThemeDescription = "Gentle acoustic guitar and warm oboe playing a melody of fresh beginnings."
            ),
            RegionLocation(
                id = "violet_city",
                name = "Violet City",
                type = LocationType.CITY,
                description = "An ancient city preserving old traditions. Features Sprout Tower and Falkner's Flying Gym.",
                normalizedX = 0.58f,
                normalizedY = 0.45f,
                connectedToIds = listOf("cherrygrove_city", "ruins_of_alph", "azalea_town"),
                gymLeader = GymLeader(
                    name = "Falkner",
                    title = "The Elegant Master of Flying Pokémon",
                    badgeName = "Zephyr Badge",
                    badgeEmoji = "🪶",
                    specialtyType = PokemonType.FLYING,
                    acePokemonId = 17,
                    acePokemonName = "Pidgeotto"
                ),
                wildSpawns = listOf(
                    WildSpawn(69, "Bellsprout", 3, 6, "Sprout Tower", "Very Common"),
                    WildSpawn(92, "Gastly", 3, 7, "Sprout Tower (Night)", "Common"),
                    WildSpawn(179, "Mareep", 6, 8, "Route 32", "Common")
                ),
                musicThemeDescription = "Serene wooden bells and traditional plucked strings."
            ),
            RegionLocation(
                id = "ruins_of_alph",
                name = "Ruins of Alph",
                type = LocationType.DUNGEON,
                description = "Ancient stone chambers carved with mysterious Unown hieroglyphs and cryptic radio signals.",
                normalizedX = 0.48f,
                normalizedY = 0.48f,
                connectedToIds = listOf("violet_city", "union_cave"),
                wildSpawns = listOf(
                    WildSpawn(201, "Unown", 5, 5, "Ruins Chamber", "Very Common"),
                    WildSpawn(194, "Wooper", 4, 8, "Surfing", "Common"),
                    WildSpawn(177, "Natu", 18, 24, "Grass Outskirts", "Uncommon")
                ),
                musicThemeDescription = "Mysterious, otherworldly synthesizer tones echoing ancient transmissions."
            ),
            RegionLocation(
                id = "azalea_town",
                name = "Azalea Town",
                type = LocationType.TOWN,
                description = "A secluded town where people live in harmony with Slowpoke. Kurt crafts custom Poké Balls from Apricorns.",
                normalizedX = 0.44f,
                normalizedY = 0.72f,
                connectedToIds = listOf("ruins_of_alph", "ilex_forest"),
                gymLeader = GymLeader(
                    name = "Bugsy",
                    title = "The Walking Bug Pokémon Encyclopedia",
                    badgeName = "Hive Badge",
                    badgeEmoji = "🐝",
                    specialtyType = PokemonType.BUG,
                    acePokemonId = 123,
                    acePokemonName = "Scyther"
                ),
                wildSpawns = listOf(
                    WildSpawn(79, "Slowpoke", 6, 10, "Slowpoke Well", "Very Common"),
                    WildSpawn(41, "Zubat", 6, 12, "Slowpoke Well", "Common"),
                    WildSpawn(214, "Heracross", 10, 10, "Headbutt Trees", "Rare")
                ),
                musicThemeDescription = "Rustic, playful woodwind rhythm with countryside charm."
            ),
            RegionLocation(
                id = "ilex_forest",
                name = "Ilex Forest",
                type = LocationType.FOREST,
                description = "A dense, canopy-covered sacred forest containing the wooden shrine of Celebi, the Voice of the Forest.",
                normalizedX = 0.32f,
                normalizedY = 0.72f,
                connectedToIds = listOf("azalea_town", "goldenrod_city"),
                legendary = LegendaryEncounter(
                    pokemonId = 251,
                    pokemonName = "Celebi",
                    encounterType = "Sacred Shrine Summon",
                    level = 30,
                    requirementText = "Present the GS Ball at the wooden forest shrine to trigger the time-travel event."
                ),
                wildSpawns = listOf(
                    WildSpawn(46, "Paras", 5, 8, "Tall Grass", "Common"),
                    WildSpawn(102, "Exeggcute", 10, 10, "Headbutt Trees", "Uncommon"),
                    WildSpawn(215, "Sneasel", 10, 15, "Tall Grass (Night)", "Rare")
                ),
                musicThemeDescription = "Damp, mystical echoing piano and soft nocturnal flute."
            ),
            RegionLocation(
                id = "goldenrod_city",
                name = "Goldenrod City",
                type = LocationType.CITY,
                description = "The bustling commercial epicenter of Johto, boasting the Radio Tower, Magnet Train station, and Whitney's Normal Gym.",
                normalizedX = 0.28f,
                normalizedY = 0.52f,
                connectedToIds = listOf("ilex_forest", "national_park", "ecruteak_city"),
                gymLeader = GymLeader(
                    name = "Whitney",
                    title = "The Incredibly Pretty Girl",
                    badgeName = "Plain Badge",
                    badgeEmoji = "🥛",
                    specialtyType = PokemonType.NORMAL,
                    acePokemonId = 241,
                    acePokemonName = "Miltank"
                ),
                wildSpawns = listOf(
                    WildSpawn(63, "Abra", 10, 12, "Route 34", "Uncommon"),
                    WildSpawn(39, "Jigglypuff", 12, 14, "Route 34", "Uncommon"),
                    WildSpawn(137, "Porygon", 15, 15, "Game Corner Prize", "Unique")
                ),
                musicThemeDescription = "Lively, upbeat urban swing and brass fanfare."
            ),
            RegionLocation(
                id = "ecruteak_city",
                name = "Ecruteak City",
                type = LocationType.CITY,
                description = "A historical city where past meets present. Home to the legendary Burned Tower and the 9-story Bell Tower.",
                normalizedX = 0.44f,
                normalizedY = 0.32f,
                connectedToIds = listOf("goldenrod_city", "bell_tower", "olivine_city", "mahogany_town"),
                gymLeader = GymLeader(
                    name = "Morty",
                    title = "The Mystic Seer of the Future",
                    badgeName = "Fog Badge",
                    badgeEmoji = "👻",
                    specialtyType = PokemonType.GHOST,
                    acePokemonId = 94,
                    acePokemonName = "Gengar"
                ),
                wildSpawns = listOf(
                    WildSpawn(93, "Haunter", 20, 24, "Burned Tower", "Common"),
                    WildSpawn(109, "Koffing", 14, 16, "Burned Tower", "Common"),
                    WildSpawn(216, "Teddiursa", 13, 15, "Route 45", "Rare")
                ),
                musicThemeDescription = "Solemn, beautiful shamisen strings echoing centuries of lore."
            ),
            RegionLocation(
                id = "bell_tower",
                name = "Bell Tower & Burned Tower",
                type = LocationType.LEGENDARY_LAIR,
                description = "The sacred perches where the legendary beasts (Raikou, Entei, Suicune) awoke, and where the Rainbow Bird Ho-Oh descends.",
                normalizedX = 0.46f,
                normalizedY = 0.22f,
                connectedToIds = listOf("ecruteak_city"),
                legendary = LegendaryEncounter(
                    pokemonId = 250,
                    pokemonName = "Ho-Oh",
                    encounterType = "Static Boss Encounter",
                    level = 45,
                    requirementText = "Present the Rainbow Wing and Clear Bell at the pinnacle of the Bell Tower."
                ),
                wildSpawns = listOf(
                    WildSpawn(19, "Rattata", 20, 22, "Tower Interior", "Common"),
                    WildSpawn(92, "Gastly", 21, 24, "Tower Interior", "Common"),
                    WildSpawn(243, "Raikou", 40, 40, "Roaming Johto Grasslands", "Legendary Roamer"),
                    WildSpawn(244, "Entei", 40, 40, "Roaming Johto Grasslands", "Legendary Roamer"),
                    WildSpawn(245, "Suicune", 40, 40, "North of Cerulean Cape", "Legendary Encounter")
                ),
                musicThemeDescription = "Intense, mystical taiko drums and cinematic horn fanfares."
            ),
            RegionLocation(
                id = "whirl_islands",
                name = "Whirl Islands",
                type = LocationType.LEGENDARY_LAIR,
                description = "A mysterious four-island archipelago guarded by violent whirlpools. Deep beneath the waterfalls rests the guardian of the seas.",
                normalizedX = 0.14f,
                normalizedY = 0.62f,
                connectedToIds = listOf("olivine_city", "cianwood_city"),
                legendary = LegendaryEncounter(
                    pokemonId = 249,
                    pokemonName = "Lugia",
                    encounterType = "Static Boss Encounter",
                    level = 45,
                    requirementText = "Acquire the Silver Wing and Tidal Bell from the Kimono Girls."
                ),
                wildSpawns = listOf(
                    WildSpawn(41, "Zubat", 22, 26, "Cave Floor", "Very Common"),
                    WildSpawn(86, "Seel", 22, 26, "Surfing", "Common"),
                    WildSpawn(116, "Horsea", 20, 24, "Surfing", "Common"),
                    WildSpawn(98, "Krabby", 20, 24, "Rock Smash", "Uncommon")
                ),
                musicThemeDescription = "Deep oceanic brass and resonant choir chords."
            ),
            RegionLocation(
                id = "lake_of_rage",
                name = "Lake of Rage & Mahogany",
                type = LocationType.DUNGEON,
                description = "A flooded crater lake stirred into chaos by Team Rocket's radio waves, causing the emergence of the Shiny Red Gyarados.",
                normalizedX = 0.68f,
                normalizedY = 0.22f,
                connectedToIds = listOf("mahogany_town", "blackthorn_city"),
                gymLeader = GymLeader(
                    name = "Pryce",
                    title = "The Teacher of Winter's Harshness",
                    badgeName = "Glacier Badge",
                    badgeEmoji = "❄️",
                    specialtyType = PokemonType.ICE,
                    acePokemonId = 221,
                    acePokemonName = "Piloswine"
                ),
                wildSpawns = listOf(
                    WildSpawn(130, "Gyarados", 30, 30, "Shiny Red Gyarados Boss", "Special Boss"),
                    WildSpawn(129, "Magikarp", 10, 25, "Fishing", "Very Common"),
                    WildSpawn(195, "Quagsire", 22, 26, "Surfing", "Common"),
                    WildSpawn(179, "Mareep", 15, 18, "Route 43", "Common")
                ),
                musicThemeDescription = "Dramatic, surging strings and thundering rain effects."
            ),
            RegionLocation(
                id = "mt_silver",
                name = "Mt. Silver & Dragon's Den",
                type = LocationType.MOUNTAIN,
                description = "The highest, most treacherous peak in all of Johto. At its frozen summit stands the legendary Champion Red.",
                normalizedX = 0.88f,
                normalizedY = 0.40f,
                connectedToIds = listOf("blackthorn_city", "indigo_plateau"),
                wildSpawns = listOf(
                    WildSpawn(246, "Larvitar", 15, 20, "Mt. Silver Cave", "Rare"),
                    WildSpawn(247, "Pupitar", 40, 45, "Mt. Silver Peak", "Rare"),
                    WildSpawn(215, "Sneasel", 42, 48, "Mt. Silver Outside", "Uncommon"),
                    WildSpawn(207, "Gligar", 42, 45, "Mt. Silver Outside", "Uncommon"),
                    WildSpawn(231, "Phanpy", 40, 44, "Mt. Silver Outside", "Uncommon")
                ),
                musicThemeDescription = "Chilling wind effects and solemn, monumental battle brass."
            )
        )
    )

    val HOENN = Region(
        id = "hoenn",
        number = 3,
        name = "Hoenn",
        japaneseName = "ホウエン地方 (Hōen-chihō)",
        tagline = "Land, Sea, and Sky",
        description = "A tropical region teeming with natural diversity: steaming active volcanoes, pristine reefs, dense rain forests, and floating island settlements.",
        professor = "Professor Birch",
        villainTeam = "Team Magma (Maxie) & Team Aqua (Archie)",
        starterIds = listOf(252, 255, 258), // Treecko, Torchic, Mudkip
        legendaryIds = listOf(377, 378, 379, 380, 381, 382, 383, 384, 385, 386),
        musicTheme = "Vibrant trumpets, French horns, acoustic guitar, and dynamic tropical percussion.",
        locations = listOf(
            RegionLocation(
                id = "littleroot_town",
                name = "Littleroot Town",
                type = LocationType.TOWN,
                description = "A town that can't be shaded any hue. Professor Birch's lab is nestled among tall grass.",
                normalizedX = 0.25f,
                normalizedY = 0.78f,
                connectedToIds = listOf("oldale_town"),
                wildSpawns = listOf(
                    WildSpawn(261, "Poochyena", 2, 4, "Tall Grass", "Very Common"),
                    WildSpawn(263, "Zigzagoon", 2, 4, "Tall Grass", "Very Common"),
                    WildSpawn(265, "Wurmple", 2, 4, "Tall Grass", "Common")
                ),
                musicThemeDescription = "Warm horn fanfare with cheerful acoustic guitar strums."
            ),
            RegionLocation(
                id = "rustboro_city",
                name = "Rustboro City",
                type = LocationType.CITY,
                description = "The metropolis of Devon Corporation technology and Roxanne's Rock Gym.",
                normalizedX = 0.18f,
                normalizedY = 0.44f,
                connectedToIds = listOf("petalburg_woods", "granite_cave"),
                gymLeader = GymLeader(
                    name = "Roxanne",
                    title = "The Rock-Loving Honors Student",
                    badgeName = "Stone Badge",
                    badgeEmoji = "🗿",
                    specialtyType = PokemonType.ROCK,
                    acePokemonId = 299,
                    acePokemonName = "Nosepass"
                ),
                wildSpawns = listOf(
                    WildSpawn(280, "Ralts", 3, 5, "Route 102", "Rare"),
                    WildSpawn(276, "Taillow", 4, 7, "Route 104", "Common"),
                    WildSpawn(285, "Shroomish", 5, 6, "Petalburg Woods", "Uncommon")
                ),
                musicThemeDescription = "Confident marching trumpets echoing architectural progress."
            ),
            RegionLocation(
                id = "meteor_falls",
                name = "Meteor Falls & Mt. Chimney",
                type = LocationType.CAVE,
                description = "A breathtaking waterfall cavern carved by meteorites. High above, Mt. Chimney's volcanic caldera rumbles.",
                normalizedX = 0.28f,
                normalizedY = 0.24f,
                connectedToIds = listOf("rustboro_city", "lavaridge_town", "fallarbor_town"),
                wildSpawns = listOf(
                    WildSpawn(371, "Bagon", 30, 35, "Deep Waterfall Chamber", "Rare"),
                    WildSpawn(337, "Lunatone", 14, 18, "Cave Floor", "Common"),
                    WildSpawn(338, "Solrock", 14, 18, "Cave Floor", "Common"),
                    WildSpawn(41, "Zubat", 15, 20, "Cave Floor", "Very Common")
                ),
                musicThemeDescription = "Surging orchestral strings and tumbling waterfall harmonies."
            ),
            RegionLocation(
                id = "cave_of_origin",
                name = "Cave of Origin (Sootopolis)",
                type = LocationType.LEGENDARY_LAIR,
                description = "A sunken volcanic crater city accessible only by diving. Deep inside the sacred cave, primal forces awaken.",
                normalizedX = 0.74f,
                normalizedY = 0.52f,
                connectedToIds = listOf("sky_pillar", "mossdeep_city"),
                legendary = LegendaryEncounter(
                    pokemonId = 382,
                    pokemonName = "Kyogre & Groudon",
                    encounterType = "Primal Weather Cataclysm",
                    level = 45,
                    requirementText = "Awakened by the Blue Orb / Red Orb during the Team Aqua/Magma climax."
                ),
                wildSpawns = listOf(
                    WildSpawn(302, "Sableye", 30, 35, "Cave of Origin", "Common"),
                    WildSpawn(303, "Mawile", 30, 35, "Cave of Origin", "Common")
                ),
                musicThemeDescription = "Thunderous apocalyptic brass and driving percussion."
            ),
            RegionLocation(
                id = "sky_pillar",
                name = "Sky Pillar",
                type = LocationType.LEGENDARY_LAIR,
                description = "An ancient, crumbling stone tower piercing the ozone layer where the Dragon Master Rayquaza sleeps.",
                normalizedX = 0.78f,
                normalizedY = 0.74f,
                connectedToIds = listOf("pacifidlog_town", "cave_of_origin"),
                legendary = LegendaryEncounter(
                    pokemonId = 384,
                    pokemonName = "Rayquaza",
                    encounterType = "Sky Ruler Boss",
                    level = 70,
                    requirementText = "Scale the crumbling floor tiles with a high-speed Mach Bike."
                ),
                wildSpawns = listOf(
                    WildSpawn(356, "Dusclops", 38, 42, "Upper Floors", "Common"),
                    WildSpawn(354, "Banette", 38, 42, "Upper Floors", "Common"),
                    WildSpawn(277, "Swellow", 40, 44, "Tower Apex", "Uncommon")
                ),
                musicThemeDescription = "Wind-swept celestial synth and epic, soaring horn fanfare."
            )
        )
    )

    val SINNOH = Region(
        id = "sinnoh",
        number = 4,
        name = "Sinnoh",
        japaneseName = "シンオウ地方 (Shin'ō-chihō)",
        tagline = "The Creation of Space & Time",
        description = "A vast mountainous northern realm split in two by Mt. Coronet. Origin of the creation trio (Dialga, Palkia, Giratina) and Arceus.",
        professor = "Professor Rowan",
        villainTeam = "Team Galactic (Cyrus)",
        starterIds = listOf(387, 390, 393), // Turtwig, Chimchar, Piplup
        legendaryIds = listOf(480, 481, 482, 483, 484, 485, 486, 487, 488, 489, 490, 491, 492, 493),
        musicTheme = "Melodic piano solos, dynamic brass, jazz fusion basslines, and dramatic strings.",
        locations = listOf(
            RegionLocation(
                id = "twinleaf_town",
                name = "Twinleaf Town & Lake Verity",
                type = LocationType.TOWN,
                description = "A fresh, snow-dusted town where your journey begins alongside Lake Verity's guardian Mesprit.",
                normalizedX = 0.16f,
                normalizedY = 0.72f,
                connectedToIds = listOf("sandgem_town", "jubilife_city"),
                wildSpawns = listOf(
                    WildSpawn(396, "Starly", 2, 4, "Tall Grass", "Very Common"),
                    WildSpawn(399, "Bidoof", 2, 4, "Tall Grass", "Very Common"),
                    WildSpawn(403, "Shinx", 3, 5, "Route 202", "Common")
                ),
                musicThemeDescription = "Intimate, warm solo piano playing a gentle nostalgic lullaby."
            ),
            RegionLocation(
                id = "spear_pillar",
                name = "Spear Pillar (Mt. Coronet)",
                type = LocationType.LEGENDARY_LAIR,
                description = "The ancient mountaintop temple ruins above the clouds where the Red Chain opens the fabric of space and time.",
                normalizedX = 0.50f,
                normalizedY = 0.38f,
                connectedToIds = listOf("eterna_city", "hearthome_city", "celestic_town"),
                legendary = LegendaryEncounter(
                    pokemonId = 483,
                    pokemonName = "Dialga & Palkia",
                    encounterType = "Creation Apex Summon",
                    level = 47,
                    requirementText = "Stop Cyrus from creating a universe without spirit at Spear Pillar."
                ),
                wildSpawns = listOf(
                    WildSpawn(436, "Bronzor", 35, 40, "Mt. Coronet", "Common"),
                    WildSpawn(444, "Gabite", 40, 45, "Wayward Cave Secret Area", "Rare"),
                    WildSpawn(449, "Hippopotas", 22, 25, "Ruin Maniac Cave", "Common"),
                    WildSpawn(460, "Abomasnow", 38, 42, "Snow Peaks", "Uncommon")
                ),
                musicThemeDescription = "Frantic, complex piano arpeggios over monumental brass polyphony."
            ),
            RegionLocation(
                id = "distortion_world",
                name = "Distortion World & Turnback Cave",
                type = LocationType.DUNGEON,
                description = "A bizarre anti-matter dimension where time does not flow and space is not stable. The throne of Giratina.",
                normalizedX = 0.50f,
                normalizedY = 0.20f,
                connectedToIds = listOf("spear_pillar", "sendoff_spring"),
                legendary = LegendaryEncounter(
                    pokemonId = 487,
                    pokemonName = "Giratina",
                    encounterType = "Origin Forme Boss Encounter",
                    level = 47,
                    requirementText = "Navigate the gravity-defying floating platforms of the Distortion World."
                ),
                wildSpawns = listOf(
                    WildSpawn(442, "Spiritomb", 25, 25, "Hallowed Tower Odd Keystone", "Unique"),
                    WildSpawn(434, "Stunky", 14, 18, "Route 206", "Common")
                ),
                musicThemeDescription = "Dissonant synth pulses and inverted chromatic chord progressions."
            )
        )
    )

    val UNOVA = Region(
        id = "unova",
        number = 5,
        name = "Unova",
        japaneseName = "イッシュ地方 (Isshu-chihō)",
        tagline = "Ideals, Truth, and Modernity",
        description = "A metropolitan region inspired by the New York area, featuring grand suspension bridges, subway battle networks, and seasonal changes.",
        professor = "Professor Aurea Juniper",
        villainTeam = "Team Plasma (N & Ghetsis)",
        starterIds = listOf(495, 498, 501), // Snivy, Tepig, Oshawott
        legendaryIds = listOf(638, 639, 640, 641, 642, 643, 644, 645, 646),
        musicTheme = "Dynamic dynamic fusion, electric guitars, smooth jazz saxophones, and season-shifting orchestration.",
        locations = listOf(
            RegionLocation(
                id = "castelia_city",
                name = "Castelia City",
                type = LocationType.CITY,
                description = "The bustling skyscraper capital of Unova, with five ocean piers, art galleries, and Burgh's Bug Gym.",
                normalizedX = 0.50f,
                normalizedY = 0.65f,
                connectedToIds = listOf("skyarrow_bridge", "nimbasa_city"),
                gymLeader = GymLeader(
                    name = "Burgh",
                    title = "The Premier Insect Artist",
                    badgeName = "Insect Badge",
                    badgeEmoji = "🎨",
                    specialtyType = PokemonType.BUG,
                    acePokemonId = 542,
                    acePokemonName = "Leavanny"
                ),
                wildSpawns = listOf(
                    WildSpawn(506, "Lillipup", 14, 17, "Castelia Park", "Common"),
                    WildSpawn(570, "Zorua", 25, 25, "Event / Rood Gift", "Rare"),
                    WildSpawn(529, "Drilbur", 18, 22, "Relic Passage", "Uncommon")
                ),
                musicThemeDescription = "Bustling brass street-jazz and percussion mimicking sidewalk crowds."
            ),
            RegionLocation(
                id = "dragonspiral_tower",
                name = "Dragonspiral Tower & Giant Chasm",
                type = LocationType.LEGENDARY_LAIR,
                description = "The oldest structure in Unova where the legendary Dragons of Truth and Ideals (Reshiram/Zekrom) and the frozen beast Kyurem roost.",
                normalizedX = 0.50f,
                normalizedY = 0.22f,
                connectedToIds = listOf("icirrus_city", "giant_chasm"),
                legendary = LegendaryEncounter(
                    pokemonId = 643,
                    pokemonName = "Reshiram & Zekrom",
                    encounterType = "Dragon Hero Awakening",
                    level = 50,
                    requirementText = "Awakened using the Light Stone / Dark Stone at the summit of Dragonspiral Tower."
                ),
                wildSpawns = listOf(
                    WildSpawn(621, "Druddigon", 30, 34, "Tower Floor", "Common"),
                    WildSpawn(633, "Deino", 38, 42, "Victory Road Cave", "Rare"),
                    WildSpawn(624, "Pawniard", 31, 35, "Route 9", "Uncommon")
                ),
                musicThemeDescription = "Solemn cathedral bells and sweeping cinematic strings."
            )
        )
    )

    val KALOS = Region(
        id = "kalos",
        number = 6,
        name = "Kalos",
        japaneseName = "カロス地方 (Karosu-chihō)",
        tagline = "Beauty, Life, and Mega Evolution",
        description = "A star-shaped region inspired by France, celebrated for its haute couture fashion, opulent palaces, and the secrets of Mega Evolution.",
        professor = "Professor Augustine Sycamore",
        villainTeam = "Team Flare (Lysandre)",
        starterIds = listOf(650, 653, 656), // Chespin, Fennekin, Froakie
        legendaryIds = listOf(716, 717, 718, 719, 720, 721), // Xerneas, Yveltal, Zygarde...
        musicTheme = "Romantic accordion melodies, lush classical string quartets, and French brass fanfares.",
        locations = listOf(
            RegionLocation(
                id = "lumiose_city",
                name = "Lumiose City & Prism Tower",
                type = LocationType.CITY,
                description = "The City of Light, crowned by the towering Prism Tower gym, stylish cafés, and chic fashion boutiques.",
                normalizedX = 0.50f,
                normalizedY = 0.45f,
                connectedToIds = listOf("camphrier_town", "laverre_city"),
                gymLeader = GymLeader(
                    name = "Clemont",
                    title = "The Inventor of the Future",
                    badgeName = "Voltage Badge",
                    badgeEmoji = "⚡",
                    specialtyType = PokemonType.ELECTRIC,
                    acePokemonId = 695,
                    acePokemonName = "Heliolisk"
                ),
                wildSpawns = listOf(
                    WildSpawn(661, "Fletchling", 3, 5, "Route 2", "Common"),
                    WildSpawn(667, "Litleo", 12, 15, "Route 22", "Common"),
                    WildSpawn(679, "Honedge", 11, 14, "Route 6", "Rare")
                ),
                musicThemeDescription = "Charming French accordion waltz and elegant acoustic strings."
            ),
            RegionLocation(
                id = "terminus_cave",
                name = "Geosenge & Terminus Cave",
                type = LocationType.LEGENDARY_LAIR,
                description = "The ancient megalith ruins where the Ultimate Weapon was buried, and the deep mines where Zygarde maintains balance.",
                normalizedX = 0.75f,
                normalizedY = 0.55f,
                connectedToIds = listOf("lumiose_city", "anistar_city"),
                legendary = LegendaryEncounter(
                    pokemonId = 716,
                    pokemonName = "Xerneas & Yveltal",
                    encounterType = "Tree of Life / Cocoon of Destruction",
                    level = 50,
                    requirementText = "Awakened deep inside the secret Team Flare subterranean base."
                ),
                wildSpawns = listOf(
                    WildSpawn(704, "Goomy", 30, 32, "Route 14 Swamp", "Rare"),
                    WildSpawn(708, "Phantump", 34, 38, "Route 20 Winding Woods", "Common"),
                    WildSpawn(714, "Noibat", 44, 48, "Terminus Cave", "Uncommon")
                ),
                musicThemeDescription = "Mysterious choral harmonies with dramatic orchestral percussion."
            )
        )
    )

    val ALOLA = Region(
        id = "alola",
        number = 7,
        name = "Alola",
        japaneseName = "アローラ地方 (Arōra-chihō)",
        tagline = "Island Trials & Ancient Guardians",
        description = "A sun-kissed tropical paradise of four natural islands and one artificial island. Trainers embark on the Island Challenge instead of standard Gyms.",
        professor = "Professor Kukui",
        villainTeam = "Team Skull (Guzma) & Aether Foundation (Lusamine)",
        starterIds = listOf(722, 725, 728), // Rowlet, Litten, Popplio
        legendaryIds = listOf(785, 786, 787, 788, 791, 792, 793, 794, 800),
        musicTheme = "Hawaiian slack-key guitar, ukulele, tropical log drums, and bright choir chants.",
        locations = listOf(
            RegionLocation(
                id = "melemele_island",
                name = "Melemele Island & Iki Town",
                type = LocationType.TOWN,
                description = "The island of starting journeys, guarded by the guardian deity Tapu Koko at the Ruins of Conflict.",
                normalizedX = 0.25f,
                normalizedY = 0.35f,
                connectedToIds = listOf("akala_island"),
                legendary = LegendaryEncounter(
                    pokemonId = 785,
                    pokemonName = "Tapu Koko",
                    encounterType = "Guardian Deity Trial",
                    level = 60,
                    requirementText = "Touch the altar at the Ruins of Conflict after becoming Alola Champion."
                ),
                wildSpawns = listOf(
                    WildSpawn(731, "Pikipek", 2, 4, "Route 1", "Very Common"),
                    WildSpawn(734, "Yungoos", 2, 4, "Route 1", "Very Common"),
                    WildSpawn(736, "Grubbin", 3, 5, "Route 1", "Uncommon"),
                    WildSpawn(744, "Rockruff", 10, 13, "Ten Carat Hill", "Common")
                ),
                musicThemeDescription = "Warm ukulele rhythms, ocean surf sounds, and welcoming vocal harmonies."
            ),
            RegionLocation(
                id = "altar_of_sunne_moone",
                name = "Altar of the Sunne / Moone",
                type = LocationType.LEGENDARY_LAIR,
                description = "The sacred summit of Vast Poni Canyon where the Sun and Moon flutes summon Solgaleo and Lunala through Ultra Wormholes.",
                normalizedX = 0.75f,
                normalizedY = 0.65f,
                connectedToIds = listOf("melemele_island", "aether_paradise"),
                legendary = LegendaryEncounter(
                    pokemonId = 791,
                    pokemonName = "Solgaleo & Lunala",
                    encounterType = "Cosmog Evolution Catalyst",
                    level = 55,
                    requirementText = "Play both Sun and Moon Flutes alongside Lillie at the ancient altar."
                ),
                wildSpawns = listOf(
                    WildSpawn(782, "Jangmo-o", 41, 44, "Vast Poni Canyon", "Rare"),
                    WildSpawn(747, "Mareanie", 15, 20, "SOS Battle with Corsola", "Rare"),
                    WildSpawn(778, "Mimikyu", 30, 33, "Thrifty Megamart Abandoned Site", "Rare")
                ),
                musicThemeDescription = "Epic celestial choir chants and celestial synths of the cosmos."
            )
        )
    )

    val GALAR = Region(
        id = "galar",
        number = 8,
        name = "Galar",
        japaneseName = "ガラル地方 (Gararu-chihō)",
        tagline = "Dynamax Stadiums & Heroic Legends",
        description = "A long, industrial region inspired by Great Britain, renowned for colossal stadium battles, Dynamax energy hotspots, and the untamed Wild Area.",
        professor = "Professor Magnolia & Sonia",
        villainTeam = "Team Yell (Piers) & Macro Cosmos (Chairman Rose)",
        starterIds = listOf(810, 813, 816), // Grookey, Scorbunny, Sobble
        legendaryIds = listOf(888, 889, 890, 891, 892, 898),
        musicTheme = "British brass bands, crowd stadium chants, electronic rock, and bagpipe harmonies.",
        locations = listOf(
            RegionLocation(
                id = "wild_area",
                name = "The Wild Area (Rolling Fields)",
                type = LocationType.ROUTE,
                description = "A massive expanse of untamed wilderness with dynamic weather and glowing Pokémon Dens.",
                normalizedX = 0.50f,
                normalizedY = 0.60f,
                connectedToIds = listOf("motostoke", "wedgehurst"),
                wildSpawns = listOf(
                    WildSpawn(821, "Rookidee", 7, 10, "Overworld", "Common"),
                    WildSpawn(827, "Nickit", 7, 10, "Overworld", "Common"),
                    WildSpawn(831, "Wooloo", 5, 8, "Overworld", "Very Common"),
                    WildSpawn(885, "Dreepy", 50, 52, "Lake of Outrage (Fog/Rain)", "Very Rare")
                ),
                musicThemeDescription = "Expansive orchestral adventure theme shifting with dynamic weather."
            ),
            RegionLocation(
                id = "slumbering_weald",
                name = "Slumbering Weald & Energy Plant",
                type = LocationType.LEGENDARY_LAIR,
                description = "A foggy, sacred ancient forest hiding the rusted sword and shield of the legendary wolves Zacian and Zamazenta.",
                normalizedX = 0.35f,
                normalizedY = 0.85f,
                connectedToIds = listOf("wild_area"),
                legendary = LegendaryEncounter(
                    pokemonId = 888,
                    pokemonName = "Zacian & Zamazenta",
                    encounterType = "Heroic Wolf Awakening",
                    level = 70,
                    requirementText = "Return the Rusted Sword / Shield to the forest shrine during the post-game crisis."
                ),
                wildSpawns = listOf(
                    WildSpawn(840, "Applin", 15, 18, "Route 5 Trees", "Uncommon"),
                    WildSpawn(848, "Toxel", 20, 20, "Pokémon Nursery Gift", "Unique"),
                    WildSpawn(863, "Perrserker", 45, 48, "Route 9", "Common")
                ),
                musicThemeDescription = "Chilling, mystical bagpipe drone with eerie piano notes in the fog."
            )
        )
    )

    val PALDEA = Region(
        id = "paldea",
        number = 9,
        name = "Paldea",
        japaneseName = "パルデア地方 (Parudea-chihō)",
        tagline = "Terastallization & The Great Crater",
        description = "A vast, seamless open-world region based on the Iberian Peninsula, encircled by vibrant landscapes, culinary traditions, and the enigmatic Area Zero.",
        professor = "Professor Sada & Professor Turo",
        villainTeam = "Team Star (Penny / Cassiopeia)",
        starterIds = listOf(906, 909, 912), // Sprigatito, Fuecoco, Quaxly
        legendaryIds = listOf(1001, 1002, 1003, 1004, 1007, 1008, 1017, 1024, 1025),
        musicTheme = "Spanish flamenco guitars, castanets, electronic dance beats, and grand academic orchestral themes.",
        locations = listOf(
            RegionLocation(
                id = "mesagoza",
                name = "Mesagoza (Naranja / Uva Academy)",
                type = LocationType.CITY,
                description = "The grand central academy hub where Trainers embark on their Treasure Hunt across Paldea.",
                normalizedX = 0.50f,
                normalizedY = 0.60f,
                connectedToIds = listOf("cabo_poco", "cortondo", "levincia"),
                wildSpawns = listOf(
                    WildSpawn(915, "Lechonk", 2, 4, "Poco Path", "Very Common"),
                    WildSpawn(917, "Tarountula", 2, 4, "Poco Path", "Very Common"),
                    WildSpawn(921, "Pawmi", 3, 5, "South Province Area 1", "Common"),
                    WildSpawn(928, "Smoliv", 8, 11, "Olive Groves", "Common")
                ),
                musicThemeDescription = "Stately brass fanfares and energetic student marching rhythms."
            ),
            RegionLocation(
                id = "area_zero",
                name = "Area Zero (The Great Crater)",
                type = LocationType.LEGENDARY_LAIR,
                description = "A classified subterranean biosphere inside the Great Crater of Paldea, teeming with prehistoric and futuristic Paradox Pokémon.",
                normalizedX = 0.50f,
                normalizedY = 0.45f,
                connectedToIds = listOf("mesagoza"),
                legendary = LegendaryEncounter(
                    pokemonId = 1007,
                    pokemonName = "Koraidon & Miraidon",
                    encounterType = "Apex Paradox Partner",
                    level = 72,
                    requirementText = "Explore the deepest depths of the Zero Lab beneath the crystalline cave."
                ),
                wildSpawns = listOf(
                    WildSpawn(984, "Great Tusk", 58, 62, "Area Zero (Past)", "Common"),
                    WildSpawn(990, "Iron Treads", 58, 62, "Area Zero (Future)", "Common"),
                    WildSpawn(996, "Frigibax", 35, 38, "Glaseado Mountain", "Rare"),
                    WildSpawn(1005, "Roaring Moon", 59, 63, "Secret Cave Floor", "Very Rare"),
                    WildSpawn(1006, "Iron Valiant", 59, 63, "Secret Cave Floor", "Very Rare")
                ),
                musicThemeDescription = "Ethereal, haunting vocal chorus and driving EDM synth produced by Toby Fox."
            )
        )
    )

    val ALL_REGIONS: List<Region> = listOf(
        KANTO,
        JOHTO,
        HOENN,
        SINNOH,
        UNOVA,
        KALOS,
        ALOLA,
        GALAR,
        PALDEA
    )

    fun getRegionByNumber(number: Int): Region? = ALL_REGIONS.find { it.number == number }
    fun getRegionById(id: String): Region? = ALL_REGIONS.find { it.id.equals(id, ignoreCase = true) }
}
