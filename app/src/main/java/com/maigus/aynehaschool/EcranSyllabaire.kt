// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Forme des voyelles dans le syllabaire. */
enum class FormeVoyelle(val titre: String, val variante: Variante) {
    SIMPLE("Simples", Variante.INITIALE),
    NASALISEE("Nasalisées", Variante.NASALISEE),
    OUVERTE("Ouvertes", Variante.OUVERTE),
    DOUBLE("Doubles", Variante.DOUBLE),
}

/** Les voyelles qui existent sous cette forme (seules E et O ont une forme ouverte). */
fun FormeVoyelle.voyelles(): List<Lettre> = Ayneha.VOYELLES.filter { it.variantes.containsKey(variante) }

/** Syllabe consonne + voyelle et sa lecture latine (ba, bã, baa...). */
fun syllabeEtLecture(consonne: Lettre, voyelle: Lettre, forme: FormeVoyelle): Pair<List<Int>, String> {
    val v = voyelle.variantes.getValue(forme.variante)
    return Ayneha.syllabe(consonne, v) to (Ayneha.latin(consonne.base) + Ayneha.latin(v))
}

@Composable
fun EcranSyllabaire(retour: () -> Unit) {
    var forme by rememberSaveable { mutableStateOf(FormeVoyelle.SIMPLE) }

    Column(Modifier.fillMaxSize()) {
        Entete("Le syllabaire", retour)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding()
        ) {
            Paragraphe(
                "Chaque consonne suivie de chaque voyelle. On lit de droite à gauche : " +
                    "la consonne est à droite, la voyelle à gauche."
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormeVoyelle.values().forEach { f ->
                    Puce(f.titre, f == forme, { forme = f })
                }
            }
            if (forme == FormeVoyelle.OUVERTE) {
                Spacer(Modifier.height(6.dp))
                Text("Seules E et O ont une forme ouverte : ɛ (ou è) et ɔ (ou ô).", color = Teinte.texte2, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Ayneha.CONSONNES.forEach { c ->
                Text(
                    "${c.nom}  -  « ${c.epellation} »",
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                    color = Teinte.etiquette, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                )
                val syllabes = forme.voyelles().map { v -> syllabeEtLecture(c, v, forme) }
                Grille(syllabes, colonnes = 5, rtl = true, espace = 6.dp) { (pts, lecture), m ->
                    CaseGlyphe(Ayneha.visuel(pts), lecture, m, hauteurGlyphe = 46.dp, taille = 0.6f)
                }
            }
            Spacer(Modifier.fillMaxWidth().height(10.dp))
        }
    }
}
