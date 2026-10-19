// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Thème « Nuit du fleuve » :
 * - Bleu nuit : le ciel de Gao au-dessus du fleuve Niger.
 * - Orange : le soleil couchant sur les dunes, la couleur des actions.
 * - Sable : le fond sur lequel l'AYNEHA est toujours lu (contraste doux, moins de fatigue).
 * - Encre : la couleur des glyphes AYNEHA.
 * - Vert palmier doum et rouge terre : bonne réponse / à revoir.
 */
object Palette {
    val Nuit = Color(0xFF0E1A30)
    val NuitBarre = Color(0xFF0A1426)
    val NuitCarte = Color(0xFF182B4D)
    val NuitCarte2 = Color(0xFF20365E)
    val NuitTrait = Color(0xFF2A4373)
    val NuitAskia = Color(0xFF6F8FD0)
    val Orange = Color(0xFFF07C22)
    val OrangeDoux = Color(0xFFF6A15A)
    val OrangeFonce = Color(0xFFB4560F)
    val Sable = Color(0xFFF5EDDF)
    val SableFonce = Color(0xFFE6DAC3)
    val SableClair = Color(0xFFFFFBF4)
    val Encre = Color(0xFF18213A)
    val Vert = Color(0xFF3E9A72)
    val Terre = Color(0xFFC9472F)
    val Blanc = Color(0xFFFFFFFF)
    val TexteClair = Color(0xFFE9E3D6)
    val TexteClair2 = Color(0xFFA9B4C9)
    val TexteSombre2 = Color(0xFF6C6656)
    val Brun = Color(0xFF8A5A2B)
}

/** Les couleurs qui changent entre le thème sombre et le thème clair. */
@Immutable
data class Couleurs(
    val sombre: Boolean,
    val fond: Color,
    val carte: Color,
    val carte2: Color,
    val trait: Color,
    val texte: Color,
    val texte2: Color,
    val etiquette: Color,
    val lien: Color,
)

val CouleursSombres = Couleurs(
    sombre = true,
    fond = Palette.Nuit,
    carte = Palette.NuitCarte,
    carte2 = Palette.NuitCarte2,
    trait = Palette.NuitTrait,
    texte = Palette.TexteClair,
    texte2 = Palette.TexteClair2,
    etiquette = Palette.OrangeDoux,
    lien = Palette.OrangeDoux,
)

val CouleursClaires = Couleurs(
    sombre = false,
    fond = Color(0xFFF6F0E4),
    carte = Palette.SableClair,
    carte2 = Color(0xFFEFE5D2),
    trait = Palette.SableFonce,
    texte = Palette.Encre,
    texte2 = Palette.TexteSombre2,
    etiquette = Palette.Brun,
    lien = Palette.OrangeFonce,
)

val LocalCouleurs = staticCompositionLocalOf { CouleursSombres }

/** Raccourci : les couleurs du thème courant. */
val Teinte: Couleurs
    @Composable
    @ReadOnlyComposable
    get() = LocalCouleurs.current

@Composable
fun ThemeAyneha(sombre: Boolean, content: @Composable () -> Unit) {
    val c = if (sombre) CouleursSombres else CouleursClaires
    val schema = if (sombre) darkColorScheme(
        primary = Palette.Orange,
        onPrimary = Palette.Blanc,
        secondary = Palette.OrangeDoux,
        background = c.fond,
        onBackground = c.texte,
        surface = c.carte,
        onSurface = c.texte,
        surfaceVariant = c.carte2,
        onSurfaceVariant = c.texte2,
        outline = c.trait,
    ) else lightColorScheme(
        primary = Palette.Orange,
        onPrimary = Palette.Blanc,
        secondary = Palette.OrangeFonce,
        background = c.fond,
        onBackground = c.texte,
        surface = c.carte,
        onSurface = c.texte,
        surfaceVariant = c.carte2,
        onSurfaceVariant = c.texte2,
        outline = c.trait,
    )
    CompositionLocalProvider(LocalCouleurs provides c) {
        MaterialTheme(colorScheme = schema, content = content)
    }
}
