// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Réglages de l'appli, gardés sur le téléphone uniquement (SharedPreferences).
 * Chaque valeur est aussi un état Compose : un changement redessine l'appli tout de suite.
 */
class Reglages(context: Context) {
    private val p = context.getSharedPreferences("ayneha_school", Context.MODE_PRIVATE)

    /** 0 = automatique (comme le téléphone), 1 = clair, 2 = sombre (par défaut, comme le mockup). */
    var theme by mutableIntStateOf(p.getInt(THEME, 2))
        private set

    /** Facteur de taille du texte AYNEHA (0,8 à 1,6). L'emphase se fait par la taille, jamais en gras. */
    var tailleAyneha by mutableFloatStateOf(p.getFloat(TAILLE, 1f))
        private set

    /** Pseudo affiché en bas des images partagées (facultatif, 20 caractères au plus). */
    var pseudo by mutableStateOf(p.getString(PSEUDO, "") ?: "")
        private set

    /** Horodatage en bas des images partagées. */
    var horodatage by mutableStateOf(p.getBoolean(HORODATAGE, true))
        private set

    fun changerTheme(v: Int) { theme = v; p.edit().putInt(THEME, v).apply() }

    fun changerTaille(v: Float) {
        tailleAyneha = v.coerceIn(0.8f, 1.6f)
        p.edit().putFloat(TAILLE, tailleAyneha).apply()
    }

    fun changerPseudo(v: String) {
        pseudo = v.take(PSEUDO_MAX)
        p.edit().putString(PSEUDO, pseudo).apply()
    }

    fun changerHorodatage(v: Boolean) { horodatage = v; p.edit().putBoolean(HORODATAGE, v).apply() }

    /** Meilleurs scores des quiz et de la lecture. */
    fun record(cle: String): Int = p.getInt("record_$cle", 0)

    fun noterRecord(cle: String, score: Int) {
        if (score > record(cle)) p.edit().putInt("record_$cle", score).apply()
    }

    companion object {
        const val PSEUDO_MAX = 20
        private const val THEME = "theme"
        private const val TAILLE = "taille_ayneha"
        private const val PSEUDO = "pseudo"
        private const val HORODATAGE = "horodatage"
    }
}

val LocalReglages = staticCompositionLocalOf<Reglages> { error("Réglages non fournis") }
