# 🏆 Dexter Pokédex — Detailed AI-Guided Design & UX Upgrade Roadmap

This document outlines a complete, step-by-step technical specification for transforming **Dexter Pokédex** into a modern, flagship-grade Android app. Each phase is structured so an AI coding assistant can execute it with zero ambiguity.

---

## 📅 Roadmap Overview

| Phase | Module / Target | Focus Area | Key Target Files |
| :--- | :--- | :--- | :--- |
| **Phase 1** | Custom Typography & Font System | Google Outfit Font integration, Type.kt scale update | `res/font/*`, `ui/theme/Type.kt` |
| **Phase 2** | Color Palette Refinement | Purging template colors, Pokédex accent palette | `ui/theme/Color.kt`, `ui/theme/Theme.kt` |
| **Phase 3** | Micro-Animations & Interactivity | Bouncy scale toggles, section slide-ins, card press effects | `ui/common/AnimationEffects.kt`, `ui/home/*`, `ui/detail/*`, `ui/quiz/*` |
| **Phase 4** | Complete String Extraction | Centralizing all hardcoded text into strings.xml | `res/values/strings.xml`, `ui/*` |
| **Phase 5** | Architectural Clean-up & Refactoring | Decomposing monolithic composables | `ui/team/*` |
| **Phase 6** | Performance & State Polish | Image request memoization, background thread offloading, `@Immutable` state annotations | `ui/home/*`, `ui/*/*UiState.kt` |

---

## 🔤 Phase 1: Custom Typography (Outfit Font Integration)

### 🎯 Objective
Replace generic system fonts with Google's **Outfit** geometric font family across display, title, body, and label text, while preserving monospace formatting for stat values and Pokédex numbers.

### 📝 AI Execution Instructions

1. **Download & Place Font Assets**:
   - Save font files in `app/src/main/res/font/` (all filenames lowercase with underscores):
     - `outfit_regular.ttf` (Weight 400)
     - `outfit_medium.ttf` (Weight 500)
     - `outfit_semibold.ttf` (Weight 600)
     - `outfit_bold.ttf` (Weight 700)
     - `outfit_extrabold.ttf` (Weight 800)

2. **Define `OutfitFontFamily` in [`Type.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/theme/Type.kt)**:
   ```kotlin
   val OutfitFontFamily = FontFamily(
       Font(R.font.outfit_regular, FontWeight.Normal),
       Font(R.font.outfit_medium, FontWeight.Medium),
       Font(R.font.outfit_semibold, FontWeight.SemiBold),
       Font(R.font.outfit_bold, FontWeight.Bold),
       Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
   )
   ```

3. **Update Typography Scale**:
   - In [`Type.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/theme/Type.kt), replace `fontFamily = FontFamily.Default` with `fontFamily = OutfitFontFamily` for:
     - `displaySmall`, `headlineLarge`, `headlineMedium`, `titleMedium`, `titleSmall`, `bodyLarge`, `bodyMedium`, `labelLarge`, `labelMedium`, `labelSmall`, and `MeasurementNumberStyle`.
   - **DO NOT** modify `bodySmall`, `StatNumberStyle`, or `PokedexIdStyle` (must retain `FontFamily.Monospace`).

### 🧪 Verification Criteria
- [ ] App compiles cleanly with `./gradlew assembleDebug`.
- [ ] Titles and body copy display the modern Outfit typeface.
- [ ] Stats and Pokédex IDs maintain tabular monospace alignment.

---

## 🎨 Phase 2: Color Palette Refinement

### 🎯 Objective
Eliminate starter template color tokens (`Purple80`, `Pink40`, etc.) and establish a thematic secondary/tertiary color scheme based on Pokémon Great Ball blue and Ultra Ball gold.

### 📝 AI Execution Instructions

1. **Update [`Color.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/theme/Color.kt)**:
   - Remove `Purple80`, `PurpleGrey80`, `Pink80`, `Purple40`, `PurpleGrey40`, `Pink40`.
   - Add new token constants:
     ```kotlin
     // Great Ball Blue (Secondary)
     val GreatBallBlueLight = Color(0xFF2E6BB0)
     val GreatBallBlueDark = Color(0xFF8EC4FF)
     val GreatBallBlueContainerLight = Color(0xFFD4E8FF)
     val OnGreatBallBlueContainerLight = Color(0xFF001C38)
     val GreatBallBlueContainerDark = Color(0xFF164D84)
     val OnGreatBallBlueContainerDark = Color(0xFFD4E8FF)

     // Ultra Ball Gold (Tertiary)
     val UltraBallGoldLight = Color(0xFF7C5800)
     val UltraBallGoldDark = Color(0xFFF5BF48)
     val UltraBallGoldContainerLight = Color(0xFFFFDEA6)
     val OnUltraBallGoldContainerLight = Color(0xFF271900)
     val UltraBallGoldContainerDark = Color(0xFF5C4200)
     val OnUltraBallGoldContainerDark = Color(0xFFFFDEA6)
     ```

2. **Update [`Theme.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/theme/Theme.kt)**:
   - Map `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer`, `tertiary`, `onTertiary` in `DarkColorScheme` and `LightColorScheme` to the new Great Ball and Ultra Ball tokens.

### 🧪 Verification Criteria
- [ ] No remaining references to template purple/pink colors in the codebase.
- [ ] Non-dynamically themed surfaces display cohesive blue and gold secondary accents.

---

## ✨ Phase 3: Micro-Animations & Interactivity

### 🎯 Objective
Introduce expressive tactile feedback and entrance animations to make UI interactions feel alive and polished.

### 📝 AI Execution Instructions

1. **Add Animation Modifiers to [`AnimationEffects.kt`](file:///home/sumit/Github/Pokemon/pokedex/app/src/main/java/com/dexter/app/ui/common/AnimationEffects.kt)**:
   - **`bounceOnStateChange(isActive: Boolean)`**: Overshooting spring scale animation for toggles.
   - **`scaleOnPress()`**: Press-to-shrink touch effect (0.96f scale on press) with spring return.
   - **`AnimatedSectionHeader(title: String)`**: Slide-in + fade entrance for detail section titles.

2. **Integrate Modifiers across Screens**:
   - Attach `.scaleOnPress()` to card containers in `HomeScreen.kt` and `QuizScreen.kt`.
   - Attach `.bounceOnStateChange()` to Pokéball caught and heart favorite icons in `DetailScreen.kt`.
   - Replace static detail section titles with `AnimatedSectionHeader` in `DetailScreen.kt`.

### 🧪 Verification Criteria
- [ ] Tapping grid items produces a subtle tactile shrink.
- [ ] Toggling caught/favorite status triggers a bouncy icon scale effect.
- [ ] Section titles in detail view slide smoothly into view upon loading.

---

## 🌍 Phase 4: Complete String Resource Extraction

### 🎯 Objective
Extract all raw string literals into `res/values/strings.xml` to support localization and clean software engineering standards.

### 📝 AI Execution Instructions

1. **Create `app/src/main/res/values/strings.xml`**:
   - Define structured string keys for all UI modules (Home, Detail, Team, Compare, Quiz, Profile, Achievements, Filters, Navigation).

2. **Update Composables**:
   - Replace raw string text in all composables with `stringResource(R.string.xxx)`.
   - Use parameterized string formatting (e.g., `stringResource(R.string.quiz_streak, streakCount)`) where dynamic values are injected.

### 🧪 Verification Criteria
- [ ] No hardcoded user-visible text literals remaining in composable code.
- [ ] All text renders properly without missing argument format specifiers.

---

## 🧹 Phase 5: Architectural Clean-up & Refactoring

### 🎯 Objective
Decompose oversized composable files (e.g., `TeamBuilderScreen.kt` at ~1,400+ lines) into modular, single-responsibility components.

### 📝 AI Execution Instructions

1. **Refactor `TeamBuilderScreen.kt`**:
   - Extract smaller component files under `app/src/main/java/com/dexter/app/ui/team/`:
     - `TeamRosterSection.kt`: Roster grid with drag-and-drop support.
     - `TeamMemberCard.kt`: Individual team member card.
     - `TeamSynergySection.kt`: Radar chart and balance metrics.
     - `TeamCoverageSection.kt`: Type coverage matrix and overlap warnings.
   - Reduce `TeamBuilderScreen.kt` to a lightweight orchestration layout.

### 🧪 Verification Criteria
- [ ] `TeamBuilderScreen.kt` is under 250 lines of code.
- [ ] Drag-and-drop team reordering and team synergy calculations function without regression.

---

## ⚡ Phase 6: Performance & State Polish

### 🎯 Objective
Eliminate redundant compositions, optimize image loading, and safeguard background data filtering.

### 📝 AI Execution Instructions

1. **Memoize Coil Image Requests**:
   - Wrap `ImageRequest.Builder` instances in `remember(url)` across grid items and quiz cards to avoid rebuilding requests during scroll recompositions.

2. **Background Thread Filtering**:
   - Ensure list filtering and sorting in `HomeViewModel.kt` run on `Dispatchers.Default` using `.flowOn(Dispatchers.Default)`.

3. **Stability Annotations**:
   - Verify all `*UiState` classes are marked `@Immutable` or `@Stable`.

### 🧪 Verification Criteria
- [ ] Grid scrolling maintains 60/120 FPS performance with zero frame drops.
- [ ] Search input filtering operates asynchronously off the UI thread.
