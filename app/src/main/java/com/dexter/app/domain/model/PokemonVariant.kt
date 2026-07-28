package com.dexter.app.domain.model

import androidx.compose.runtime.Immutable
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonForm

@Immutable
sealed interface PokemonVariant {
    val id: String
    val label: String

    @Immutable
    data object Official : PokemonVariant {
        override val id: String = "official"
        override val label: String = "Official"
    }

    @Immutable
    data object Shiny : PokemonVariant {
        override val id: String = "shiny"
        override val label: String = "Shiny"
    }

    @Immutable
    data object Home : PokemonVariant {
        override val id: String = "home"
        override val label: String = "HOME"
    }

    @Immutable
    data object Animated : PokemonVariant {
        override val id: String = "animated"
        override val label: String = "Animated"
    }

    @Immutable
    data object Pixel : PokemonVariant {
        override val id: String = "pixel"
        override val label: String = "Pixel"
    }

    @Immutable
    data class FormVariant(
        val form: PokemonForm,
        val formKind: FormKind,
        private val customLabel: String? = null
    ) : PokemonVariant {
        override val id: String = "form_${form.id}"
        override val label: String = customLabel ?: formKind.label
    }

    enum class FormKind(val label: String) {
        MEGA_X("Mega X"),
        MEGA_Y("Mega Y"),
        GIGANTAMAX("Gigantamax"),
        OTHER("Other")
    }

    fun getThumbnailUrl(pokemon: Pokemon): String? {
        return when (this) {
            Official -> pokemon.officialArtworkUrl ?: pokemon.spriteUrl
            Shiny -> pokemon.shinyArtworkUrl ?: pokemon.shinySpriteUrl ?: pokemon.spriteUrl
            Home -> pokemon.homeArtworkUrl ?: pokemon.officialArtworkUrl ?: pokemon.spriteUrl
            Animated -> pokemon.animatedSpriteUrl ?: pokemon.spriteUrl
            Pixel -> pokemon.pixelSpriteUrl ?: pokemon.spriteUrl
            is FormVariant -> form.officialArtworkUrl ?: form.spriteUrl
        }
    }

    fun getMainArtworkUrl(pokemon: Pokemon): String? {
        return when (this) {
            Official -> pokemon.officialArtworkUrl ?: pokemon.spriteUrl
            Shiny -> pokemon.shinyArtworkUrl ?: pokemon.shinySpriteUrl ?: pokemon.spriteUrl
            Home -> pokemon.homeArtworkUrl ?: pokemon.officialArtworkUrl ?: pokemon.spriteUrl
            Animated -> pokemon.animatedSpriteUrl ?: pokemon.spriteUrl
            Pixel -> pokemon.pixelSpriteUrl ?: pokemon.spriteUrl
            is FormVariant -> form.officialArtworkUrl ?: form.spriteUrl
        }
    }

    companion object {
        fun buildVariantsForPokemon(
            pokemon: Pokemon,
            forms: List<PokemonForm>
        ): List<PokemonVariant> {
            val variants = mutableListOf<PokemonVariant>(
                Official,
                Shiny,
                Home,
                Animated,
                Pixel
            )

            // Items 6-8: Mega X, Mega Y, Gigantamax (only if present)
            val megaXForm = forms.find {
                it.formName.contains("mega-x", ignoreCase = true) ||
                (it.formName.endsWith("-mega", ignoreCase = true) && forms.none { f -> f.formName.contains("mega-y", ignoreCase = true) }) ||
                (it.formName.contains("mega", ignoreCase = true) && !it.formName.contains("mega-y", ignoreCase = true))
            }
            if (megaXForm != null) {
                variants.add(FormVariant(megaXForm, FormKind.MEGA_X))
            }

            val megaYForm = forms.find {
                it.formName.contains("mega-y", ignoreCase = true)
            }
            if (megaYForm != null) {
                variants.add(FormVariant(megaYForm, FormKind.MEGA_Y))
            }

            val gmaxForm = forms.find {
                it.formName.contains("gmax", ignoreCase = true) ||
                it.formName.contains("gigantamax", ignoreCase = true)
            }
            if (gmaxForm != null) {
                variants.add(FormVariant(gmaxForm, FormKind.GIGANTAMAX))
            }

            forms.forEach { form ->
                if (form != megaXForm && form != megaYForm && form != gmaxForm) {
                    val label = if (form.displayName.startsWith(pokemon.capitalizedName, ignoreCase = true)) {
                        form.displayName.substring(pokemon.capitalizedName.length).trim().ifEmpty { form.displayName }
                    } else {
                        form.displayName
                    }
                    variants.add(FormVariant(form, FormKind.OTHER, customLabel = label))
                }
            }

            return variants
        }
    }
}
