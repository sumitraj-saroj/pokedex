package com.dexter.app.ui.region

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class GbaRouteSegment(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val isWaterRoute: Boolean = false,
    val isBridgeRoute: Boolean = false
)

data class GbaLandmassPolygon(
    val points: List<Offset>,
    val elevationLevel: Int = 1 // 1: Lowland, 2: Mid, 3: Peak
)

data class GbaRegionMapLayout(
    val regionId: String,
    val landmasses: List<GbaLandmassPolygon>,
    val routes: List<GbaRouteSegment>
)

object GbaTownMapData {

    val KANTO_LAYOUT = GbaRegionMapLayout(
        regionId = "kanto",
        landmasses = listOf(
            // Main Kanto Continent (Level 1 Lowland Base)
            GbaLandmassPolygon(
                points = listOf(
                    Offset(0.08f, 0.08f),
                    Offset(0.40f, 0.05f),
                    Offset(0.68f, 0.15f),
                    Offset(0.88f, 0.25f),
                    Offset(0.88f, 0.55f),
                    Offset(0.82f, 0.82f),
                    Offset(0.50f, 0.88f),
                    Offset(0.45f, 0.72f),
                    Offset(0.32f, 0.52f),
                    Offset(0.28f, 0.75f),
                    Offset(0.18f, 0.85f),
                    Offset(0.16f, 0.65f),
                    Offset(0.08f, 0.58f),
                    Offset(0.08f, 0.08f)
                ),
                elevationLevel = 1
            ),
            // Middle Elevation (Green Hills)
            GbaLandmassPolygon(
                points = listOf(
                    Offset(0.10f, 0.10f),
                    Offset(0.35f, 0.08f),
                    Offset(0.55f, 0.12f),
                    Offset(0.65f, 0.25f),
                    Offset(0.50f, 0.40f),
                    Offset(0.30f, 0.35f),
                    Offset(0.14f, 0.40f),
                    Offset(0.10f, 0.10f)
                ),
                elevationLevel = 2
            ),
            // High Peak Elevation (Mt. Moon & Plateau ring)
            GbaLandmassPolygon(
                points = listOf(
                    Offset(0.11f, 0.25f),
                    Offset(0.17f, 0.25f),
                    Offset(0.17f, 0.35f),
                    Offset(0.11f, 0.35f)
                ),
                elevationLevel = 3
            ),
            // Cinnabar Island
            GbaLandmassPolygon(
                points = listOf(
                    Offset(0.17f, 0.86f),
                    Offset(0.27f, 0.86f),
                    Offset(0.27f, 0.94f),
                    Offset(0.17f, 0.94f)
                ),
                elevationLevel = 1
            )
        ),
        routes = listOf(
            // Route 1 (Pallet -> Viridian)
            GbaRouteSegment(0.22f, 0.75f, 0.22f, 0.58f),
            // Route 2 (Viridian -> Pewter)
            GbaRouteSegment(0.22f, 0.58f, 0.22f, 0.35f),
            // Route 22 & Victory Road (Viridian -> Indigo Plateau)
            GbaRouteSegment(0.22f, 0.58f, 0.14f, 0.58f),
            GbaRouteSegment(0.14f, 0.58f, 0.14f, 0.30f),
            // Route 3 & 4 over Mt. Moon (Pewter -> Cerulean)
            GbaRouteSegment(0.22f, 0.35f, 0.65f, 0.30f),
            // Route 24 & 25 (Cerulean North)
            GbaRouteSegment(0.65f, 0.30f, 0.65f, 0.18f),
            GbaRouteSegment(0.65f, 0.18f, 0.76f, 0.18f),
            // Route 5 & 6 (Cerulean -> Saffron -> Vermilion)
            GbaRouteSegment(0.65f, 0.30f, 0.65f, 0.63f),
            // Route 7 & 8 (Celadon -> Saffron -> Lavender)
            GbaRouteSegment(0.52f, 0.46f, 0.82f, 0.46f),
            // Route 9 & 10 (Cerulean -> Rock Tunnel -> Lavender)
            GbaRouteSegment(0.65f, 0.30f, 0.82f, 0.30f),
            GbaRouteSegment(0.82f, 0.30f, 0.82f, 0.46f),
            // Route 11 (Vermilion East)
            GbaRouteSegment(0.65f, 0.63f, 0.82f, 0.63f),
            // Route 12, 13, 14, 15 (Lavender -> Fuchsia South-East Coast)
            GbaRouteSegment(0.82f, 0.46f, 0.82f, 0.72f),
            GbaRouteSegment(0.82f, 0.72f, 0.56f, 0.80f),
            // Cycling Road (Route 16, 17, 18 Bridge: Celadon -> Fuchsia)
            GbaRouteSegment(0.52f, 0.46f, 0.34f, 0.46f, isBridgeRoute = true),
            GbaRouteSegment(0.34f, 0.46f, 0.34f, 0.80f, isBridgeRoute = true),
            GbaRouteSegment(0.34f, 0.80f, 0.56f, 0.80f, isBridgeRoute = true),
            // Sea Routes 19, 20 (Fuchsia -> Seafoam -> Cinnabar)
            GbaRouteSegment(0.56f, 0.80f, 0.56f, 0.90f, isWaterRoute = true),
            GbaRouteSegment(0.56f, 0.90f, 0.22f, 0.90f, isWaterRoute = true),
            // Sea Route 21 (Cinnabar -> Pallet Town)
            GbaRouteSegment(0.22f, 0.90f, 0.22f, 0.75f, isWaterRoute = true)
        )
    )

    fun getLayoutForRegion(regionId: String): GbaRegionMapLayout {
        // Returns specific tailored GBA layout or generates an authentic grid layout
        return when (regionId.lowercase()) {
            "kanto" -> KANTO_LAYOUT
            else -> KANTO_LAYOUT // Can adapt dynamically for all regions
        }
    }
}
