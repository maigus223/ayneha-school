// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
// Écriture AYNEHA conçue et créée par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/** Les écrans de l'appli. */
sealed class Ecran {
    object Accueil : Ecran()
    object Alphabet : Ecran()
    data class FicheLettre(val cle: String) : Ecran()
    object Accents : Ecran()
    object Syllabaire : Ecran()
    object Chiffres : Ecran()
    data class Tracer(val point: Int? = null) : Ecran()
    object Quiz : Ecran()
    object Atelier : Ecran()
    object Lecture : Ecran()
    object Reglages : Ecran()
    object APropos : Ecran()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val reglages = Reglages(applicationContext)
        setContent {
            val sombre = when (reglages.theme) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }
            // Barres système transparentes : la barre du haut est toujours bleu nuit (icônes claires),
            // celle du bas suit le thème.
            DisposableEffect(sombre) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
                    navigationBarStyle = if (sombre) SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    else SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                )
                onDispose { }
            }
            CompositionLocalProvider(LocalReglages provides reglages) {
                ThemeAyneha(sombre) { Appli() }
            }
        }
    }
}

@Composable
fun Appli() {
    val pile = remember { mutableStateListOf<Ecran>(Ecran.Accueil) }
    val ouvrir: (Ecran) -> Unit = { pile.add(it) }
    val retour: () -> Unit = { if (pile.size > 1) pile.removeAt(pile.lastIndex) }
    // Remplace l'écran courant (fiche précédente / suivante sans empiler).
    val remplacer: (Ecran) -> Unit = { pile[pile.lastIndex] = it }

    BackHandler(enabled = pile.size > 1) { retour() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Teinte.fond)
    ) {
        when (val e = pile.last()) {
            Ecran.Accueil -> EcranAccueil(ouvrir)
            Ecran.Alphabet -> EcranAlphabet(ouvrir, retour)
            is Ecran.FicheLettre -> EcranFicheLettre(e.cle, ouvrir, remplacer, retour)
            Ecran.Accents -> EcranAccents(ouvrir, retour)
            Ecran.Syllabaire -> EcranSyllabaire(retour)
            Ecran.Chiffres -> EcranChiffres(retour)
            is Ecran.Tracer -> EcranTracer(e.point, retour)
            Ecran.Quiz -> EcranQuiz(retour)
            Ecran.Atelier -> EcranAtelier(retour)
            Ecran.Lecture -> EcranLecture(retour)
            Ecran.Reglages -> EcranReglages(retour)
            Ecran.APropos -> EcranAPropos(retour)
        }
    }
}
