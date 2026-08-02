PROJECT: Dexter — offline-first native Android Pokédex app.

STACK:
- Kotlin, Jetpack Compose (Material 3), minSdk 26 / targetSdk latest
- Architecture: MVVM + simple layered structure (data / domain / ui), single-module app
  to start (multi-module is a non-goal for v1)
- DI: Hilt
- Navigation: Navigation Compose (type-safe routes)
- Local DB: Room (replaces PRD's expo-sqlite)
- Networking: Retrofit + OkHttp + kotlinx.serialization
- Async: Kotlin Coroutines + Flow
- Image loading/caching: Coil (Compose integration, disk cache enabled)
- Audio (cry playback): Media3 ExoPlayer
- Haptics: Compose HapticFeedback (view.performHapticFeedback equivalent)
- Lists: LazyVerticalGrid / LazyColumn with stable keys (Compose's built-in virtualization
  is the equivalent of FlashList — no extra library needed)
- Data source: PokeAPI (https://pokeapi.co/api/v2/) — free, no key required

DATA FLOW:
1. On first launch, sync from PokeAPI into Room (paginated, show progress).
2. All screens read from Room only (single source of truth) — never bind UI directly to
   network calls. Retrofit results always land in Room first via a Repository.
3. After initial sync, app must be 100% usable with airplane mode on.

NON-GOALS (do not build these unless explicitly asked):
- No breeding/egg mechanics of any kind
- No multiplayer/PvP or networking beyond the initial PokeAPI sync
- No user accounts / cloud login / backend server
- No ads, no IAP

CODE STYLE:
- Idiomatic Compose: state hoisting, unidirectional data flow (UiState sealed classes/data
  classes exposed via StateFlow from ViewModels)
- Prefer sealed interfaces for UI state (Loading/Success/Error) over booleans
- Use Hilt @HiltViewModel + @Inject constructor injection throughout
- Package by feature under ui/ (e.g. ui/home, ui/detail, ui/team, ui/quiz), with shared
  data/ and domain/ packages at the top level
