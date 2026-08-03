# 🚀 Dexter Pokédex — Premium UI/UX Implementation Roadmap

This document outlines a phased implementation roadmap to elevate **Dexter** into a flagship, world-class Android application using **Jetpack Compose**, **Material 3 Expressive Design**, **Advanced Canvas Graphics**, **Custom Motion Physics**, and **Adaptive Layouts**.

Each phase is written with high technical granularity so an AI coding assistant (e.g. Gemini / Antigravity) can implement them cleanly and step-by-step.

---

## 📅 Roadmap Overview

| Phase | Feature Module | Focus Area | Key Target Files |
| :--- | :--- | :--- | :--- |
| **Phase 1** | Design System & Glassmorphism | Ambient Gradients, Glassmorphic Modifiers, Tabular Typography | `ui/theme/*` |
| **Phase 2** | Shared Element Navigation | Seamless Hero Image & Card Morphing Transitions | `navigation/*`, `ui/home/*`, `ui/detail/*` |
| **Phase 3** | Home Screen UX & Hero Carousel | Featured Pokémon Card, Inline Quick Filter Chips, Staggered Grid Motion | `ui/home/*`, `ui/common/*` |
| **Phase 4** | Hexagonal Radar & Detailed Evolution | Canvas Hexagonal Radar Chart, Animated Number Counters, Enhanced Evolution Tree | `ui/detail/*`, `ui/common/*` |
| **Phase 5** | Multi-Layer 3D Holographic Card | 3D Depth Cutout, Gyro Parallax, Specular Light Sheen | `ui/detail/Interactive3DTradingCard.kt`, `ui/common/*` |
| **Phase 6** | Team Builder Synergy & Drag-Drop | Battle Synergy Coverage Radar, Drag-and-Drop Reordering | `ui/team/*` |
| **Phase 7** | Compare Screen Dual Radar & Deltas | Dual Overlaid Stat Polygons, Numeric Stat Delta Badges | `ui/compare/*` |
| **Phase 8** | Audio Visualizer & Victory Particles | Live Canvas Audio Soundwave, Particle Confetti System | `ui/quiz/*` |
| **Phase 9** | Adaptive Multi-Pane Layouts | Foldable & Tablet Split-Pane Support (`WindowSizeClass`) | `MainActivity.kt`, `navigation/*` |

---

## 💎 Phase 1: Design System & Glassmorphism Upgrade

### 🎯 Objective
Elevate the visual foundation of Dexter by creating custom glassmorphic layout modifiers, dynamic radial/mesh gradients based on element types, and tabular typography for smooth numerical transitions.

### 📝 Step-by-Step Instructions

1. **Create Glassmorphic Custom Modifiers**:
   - Open [`app/src/main/java/com/dexter/app/ui/theme/Color.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/theme/Color.kt).
   - Add glassmorphism color constants for subtle frost borders and glass background tints.
   - Create a reusable modifier `Modifier.glassmorphicContainer()` in `ui/common/AnimationEffects.kt` (or a dedicated `ui/common/Glassmorphism.kt` file):
     - Support customizable `backgroundColor: Color`, `borderColor: Color`, `borderWidth: Dp = 1.dp`, and `shape: Shape = RoundedCornerShape(20.dp)`.
     - Implement an inner linear gradient border (`Brush.verticalGradient`) with `Color.White.copy(alpha = 0.25f)` at the top fading to `Color.White.copy(alpha = 0.05f)` at the bottom.

2. **Enhance `TypeThemingEngine` with Ambient Mesh Gradients**:
   - Inspect [`app/src/main/java/com/dexter/app/ui/theme/TypeThemingEngine.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/theme/TypeThemingEngine.kt).
   - Extend `PokemonTypeColorScheme` to expose an `ambientGradient: Brush` property combining primary, secondary, and surface background tones into a radial ambient background glow.
   - Add utility functions to extract contrasting text colors over type surface backgrounds for accessibility compliance.

3. **Configure Tabular Numerals & Custom Typography**:
   - Open [`app/src/main/java/com/dexter/app/ui/theme/Type.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/theme/Type.kt).
   - Update `StatNumberStyle` and number-display text styles to explicitly include `fontFeatureSettings = "tnum"` (Tabular Figures).
   - Ensure stat numbers, Pokédex IDs (e.g., `#0025`), weight, and height maintain fixed character widths during animations.

### 🧪 Verification Criteria
- [ ] Cards display sleek semi-transparent frosted glass borders on both light and dark themes.
- [ ] Stat numbers animate without causing horizontal text layout jitter thanks to tabular figures.
- [ ] No layout performance regression (`ComposeLint` clean).

---

## 🎬 Phase 2: Shared Element Navigation & Smooth Transitions

### 🎯 Objective
Replace standard screen slide/fade animations with Compose **Shared Element Transitions**, morphing Pokémon cards and images seamlessly between the Home Grid and the Detail view.

### 📝 Step-by-Step Instructions

1. **Wrap Navigation Host with `SharedTransitionLayout`**:
   - Open [`app/src/main/java/com/dexter/app/navigation/DexterNavHost.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/navigation/DexterNavHost.kt).
   - Wrap the `NavHost` inside `SharedTransitionLayout { ... }`.
   - Pass `SharedTransitionScope` down to destination composables (`HomeScreen`, `DetailScreen`).

2. **Add Shared Element Bounds to Home Grid Items**:
   - Open [`app/src/main/java/com/dexter/app/ui/home/HomeScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/home/HomeScreen.kt).
   - Locate `PokemonGridItem`.
   - Apply `Modifier.sharedElement()` to the `AsyncImage` sprite using key `"pokemon_image_${pokemon.id}"`.
   - Apply `Modifier.sharedBounds()` to the Card container using key `"pokemon_card_${pokemon.id}"`.

3. **Link Destination Bounds in Detail Screen**:
   - Open [`app/src/main/java/com/dexter/app/ui/detail/DetailScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/detail/DetailScreen.kt).
   - Apply matching `sharedElement` keys to the hero artwork image and detail card background.
   - Configure spring physics (`spatialExpressiveSpring()`) for smooth size and position interpolations.

### 🧪 Verification Criteria
- [ ] Tapping a Pokémon in the home grid smoothly expands the sprite artwork directly into the detail header.
- [ ] Pressing back smoothly morphs the detail header back to its exact grid slot.

---

## ⚡ Phase 3: Home Screen UX & Hero Carousel

### 🎯 Objective
Transform the home screen into an engaging discovery dashboard with a "Pokémon of the Day" hero card, animated inline quick-filter chips, and staggered item entrance animations.

### 📝 Step-by-Step Instructions

1. **Build "Pokémon of the Day" Hero Card**:
   - Open [`app/src/main/java/com/dexter/app/ui/home/HomeScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/home/HomeScreen.kt).
   - Create a new composable `PokemonOfDayHeroCard(pokemon: Pokemon, onClick: () -> Unit)`.
   - Design with a dynamic type-tinted radial gradient background, large high-resolution sprite artwork, "Featured Pokémon" gold badge, base stat summary, and a quick catch button.

2. **Add Scrollable Quick-Filter Chips Bar**:
   - Open [`app/src/main/java/com/dexter/app/ui/common/SearchFilterBar.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/common/SearchFilterBar.kt).
   - Add an inline horizontally scrollable `LazyRow` beneath the search bar containing pill chips for:
     - All Types (`Fire`, `Water`, `Grass`, `Electric`, etc.)
     - Rapid Category toggles (`Legendary`, `Mythical`, `Mega Evolutions`)
     - Generation shortcuts (`Gen I`, `Gen II`, `Gen IX`)
   - Animate chip selection states with spring scale animations and primary container colors.

3. **Implement Staggered Grid Motion**:
   - In `HomeScreen.kt`, update `LazyVerticalGrid` to use staggered enter animations:
     ```kotlin
     AnimatedVisibility(
         visible = true,
         enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300, delayMillis = index * 20))
     )
     ```

### 🧪 Verification Criteria
- [ ] Hero card displays at the top of the home feed with responsive gradient glow.
- [ ] Users can filter by element types directly from the home bar without opening a bottom sheet.
- [ ] Scrolling down gracefully collapses the hero card and keeps the search bar sticky.

---

## 📊 Phase 4: Hexagonal Radar Chart & Detailed Evolution Flowchart

### 🎯 Objective
Replace standard linear stat bars with an interactive 6-axis **Canvas Hexagonal Stat Radar Chart** and rebuild the evolution chain with animated flow arrows and level-up badges.

### 📝 Step-by-Step Instructions

1. **Develop Canvas Hexagonal Stat Radar Chart**:
   - Open [`app/src/main/java/com/dexter/app/ui/common/StatBar.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/common/StatBar.kt).
   - Create a standalone composable `PokemonStatRadarChart(stats: List<Int>, typeColor: Color, modifier: Modifier = Modifier)`:
     - Render 6 radial axes corresponding to **HP, Attack, Defense, Sp. Atk, Sp. Def, Speed**.
     - Draw background concentric grid polygons at 25%, 50%, 75%, and 100% capacity (scaled to 255 max stat).
     - Calculate vertices for the Pokémon's stats and draw a filled path using radial gradient brushes (`typeColor.copy(alpha = 0.5f)` to `typeColor.copy(alpha = 0.15f)`).
     - Animate vertex growth using `Animatable` with `EaseOutCubic` easing.
     - Add stat text labels with `StatNumberStyle` at vertex endpoints.

2. **Add Toggle View (Radar vs Bar Chart)**:
   - In [`app/src/main/java/com/dexter/app/ui/detail/DetailScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/detail/DetailScreen.kt), add a segment toggle button allowing users to switch between the Hexagonal Radar Chart and Traditional Linear Stat Bars.

3. **Elevate Evolution Flowchart**:
   - Open [`app/src/main/java/com/dexter/app/ui/detail/EvolutionTreeSection.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/detail/EvolutionTreeSection.kt).
   - Redesign evolution connections into node cards with curved connecting stroke lines.
   - Display trigger badges (e.g. "Lv. 16", "Thunder Stone", "Trade", "High Friendship") inside glowing pill chips placed on the connecting arrows.

### 🧪 Verification Criteria
- [ ] Radar chart smooth-animates on screen load.
- [ ] Switching between Radar and Bar chart preserves state seamlessly.
- [ ] Evolution flow items navigate directly to target Pokémon detail views when clicked.

---

## 🎴 Phase 5: Multi-Layer Parallax 3D Holographic Card

### 🎯 Objective
Transform [`Interactive3DTradingCard.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/detail/Interactive3DTradingCard.kt) into a multi-layered 3D card experience with depth separation between background, sprite artwork, and holographic foil sheen.

### 📝 Step-by-Step Instructions

1. **Refactor 3D Surface into 3 Distinct Layers**:
   - Inspect [`Interactive3DTradingCard.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/detail/Interactive3DTradingCard.kt).
   - Layer 1 (**Base Layer**): Card frame, background texture, HP/type headers, and attack details.
   - Layer 2 (**Pop-Out Sprite Layer**): Pokémon sprite offset along the Z-axis (`graphicsLayer { translationX = tiltX * 15f; translationY = tiltY * 15f; shadowElevation = 16.dp.toPx() }`).
   - Layer 3 (**Holographic Foil Overlay**): Rainbow sheen shader overlay driven by touch position and gyro sensors via [`SensorEffects.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/common/SensorEffects.kt).

2. **Add Gyro-Assisted Sensor Motion**:
   - Ensure pitch and roll from device gyroscope hardware subtly alter `tiltX` and `tiltY` when the user tilts their physical phone.
   - Fall back smoothly to touch drag input when gyro sensors are unavailable.

3. **Incorporate Spring Return Physics**:
   - Use `remember { Animatable(...) }` with `Spring.DampingRatioMediumBouncy` so the card snaps back smoothly to neutral orientation upon touch release.

### 🧪 Verification Criteria
- [ ] Pokémon sprite visually pops out from the card frame when tilted.
- [ ] Holographic rainbow reflections shift dynamically with drag touch and device tilting.

---

## ⚔️ Phase 6: Team Builder Synergy Radar & Drag-Drop Reordering

### 🎯 Objective
Enhance [`TeamBuilderScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/team/TeamBuilderScreen.kt) with an interactive battle synergy rating matrix, coverage analysis, and drag-and-drop team reordering.

### 📝 Step-by-Step Instructions

1. **Build Team Battle Synergy Radar Chart**:
   - Open [`app/src/main/java/com/dexter/app/ui/team/TeamBuilderScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/team/TeamBuilderScreen.kt).
   - Add a "Team Synergy & Coverage" section displaying:
     - Offensive Type Coverage count (how many types your 6 Pokémon can hit for Super Effective damage).
     - Weakness Overlap Warning badges (alerting when 3+ team members share a common weakness like Water or Ice).
     - Overall Team Balance Score (S, A, B, C rank rating).

2. **Implement Touch Drag-and-Drop Reordering**:
   - In `TeamBuilderScreen.kt`, add pointer gesture modifiers (`pointerInput` and `detectDragGesturesAfterLongPress`) to reorder the 6 roster slots.
   - Swap array indices dynamically in [`TeamViewModel.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/team/TeamViewModel.kt) with haptic tick feedback on slot switch.

### 🧪 Verification Criteria
- [ ] Long-pressing a team slot allows dragging to swap positions with another team member.
- [ ] Adding/removing team members recalculates team synergy radar and weakness warnings instantly.

---

## ⚖️ Phase 7: Compare Screen Dual Radar & Delta Indicators

### 🎯 Objective
Rebuild [`CompareScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/compare/CompareScreen.kt) to display overlapping stat polygons and colored numeric delta chips (`+20`, `-15`).

### 📝 Step-by-Step Instructions

1. **Construct Overlaid Dual Radar Chart**:
   - Open [`app/src/main/java/com/dexter/app/ui/compare/CompareScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/compare/CompareScreen.kt).
   - Create `DualPokemonStatRadarChart` that draws both Pokémon's base stats onto a single 6-axis Canvas.
   - Use Pokémon 1's type primary color for Polygon 1, and Pokémon 2's type primary color for Polygon 2.

2. **Add Visual Delta Indicators**:
   - Next to each stat bar/value comparison, render a comparison chip:
     - Green pill chip `+25` for higher stat.
     - Red pill chip `-10` for lower stat.
     - Neutral gray chip `=` for equal stats.

### 🧪 Verification Criteria
- [ ] Comparing two Pokémon clearly displays both stat polygons overlaid on one radar graph.
- [ ] Stat difference chips calculate correctly for all 6 base stats.

---

## 🔊 Phase 8: Quiz Audio Soundwave & Victory Confetti

### 🎯 Objective
Add a live Canvas soundwave visualizer during Pokémon cry playback in [`QuizScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/quiz/QuizScreen.kt) and celebratory confetti particles on high streaks.

### 📝 Step-by-Step Instructions

1. **Build Live Audio Waveform Canvas**:
   - Open [`app/src/main/java/com/dexter/app/ui/quiz/QuizScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/quiz/QuizScreen.kt).
   - Create `AudioWaveformVisualizer(isPlaying: Boolean)` composable.
   - Render animated vertical frequency bars driven by random sine-wave animations when [`QuizAudioPlayer.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/quiz/QuizAudioPlayer.kt) is playing audio.

2. **Create Canvas Confetti Particle System**:
   - Add a lightweight `ConfettiParticleSystem(trigger: Boolean)` composable.
   - Spawn 50+ animated colored shapes (circles, squares, pokeballs) with physics gravity and rotation velocity when the user achieves a correct answer or streak milestone.

### 🧪 Verification Criteria
- [ ] Soundwave visualizer pulses smoothly while audio is active.
- [ ] Confetti bursts across the screen upon correct guess.

---

## 📱 Phase 9: Multi-Pane & Adaptive Screen Support

### 🎯 Objective
Ensure Dexter adapts layout structure dynamically for Tablets, Foldables (e.g. Pixel Fold, Galaxy Z Fold), and Landscape orientation.

### 📝 Step-by-Step Instructions

1. **Add `calculateWindowSizeClass` Support**:
   - Open [`app/src/main/java/com/dexter/app/MainActivity.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/MainActivity.kt).
   - Add `androidx.compose.material3.windowsizeclass` dependency and extract `WindowWidthSizeClass`.

2. **Build Split-Pane Layout for `Expanded` Width**:
   - In [`HomeScreen.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/home/HomeScreen.kt), if `widthSizeClass == WindowWidthSizeClass.Expanded`:
     - Render a 2-column layout:
       - Left Pane (40% width): Scrollable search bar, quick filters, and Pokémon grid.
       - Right Pane (60% width): Selected Pokémon live Inspector (3D Card, Stat Radar, Evolution tree, Movesets).

### 🧪 Verification Criteria
- [ ] App runs seamlessly on phones (compact single pane) and tablets (split dual pane).
- [ ] Tapping a Pokémon on a tablet updates the right pane inspector immediately without losing grid scroll position.

---

## 💡 Guidelines for Gemini / AI Implementation Agents

When implementing any phase above:
1. **Always run code checks** after edits using `./gradlew assembleDebug` or `./gradlew test`.
2. **Preserve existing domain models** and logic contracts in `domain/` and `data/`.
3. **Verify dark & light theme contrast** across all custom Canvas drawings and Glassmorphic containers.
