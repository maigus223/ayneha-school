// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class FicheAccent(
    val accent: Accent,
    val seuls: List<Int>,     // l'accent dessiné seul
    val explication: String,
)

@Composable
fun EcranAccents(ouvrir: (Ecran) -> Unit, retour: () -> Unit) {
    val fiches = listOf(
        FicheAccent(
            Accent.NASALISE,
            Ayneha.ACCENT_NASALISE.values.toList(),
            "Il se pose sur les 5 voyelles : la voyelle se prononce aussi par le nez. " +
                "En latin, on le tape avec voyelle + n (an, en, in, on, un) ou directement ã, ẽ, ĩ, õ, ũ."
        ),
        FicheAccent(
            Accent.OUVERT,
            Ayneha.ACCENT_OUVERT.values.toList(),
            "Il ne se pose que sur E et O : ɛ (aussi tapé è) et ɔ (aussi tapé ô)."
        ),
        FicheAccent(
            Accent.DOUBLE,
            listOf(Ayneha.ACCENT_DOUBLE),
            "Voyelle longue (aa, ee, ii, oo, uu) ou consonne doublée (bb, dd, kk...). " +
                "Une consonne double est toujours suivie d'une voyelle. Seule H n'a pas de forme double."
        ),
        FicheAccent(
            Accent.MUET,
            listOf(Ayneha.ACCENT_MUET),
            "Une consonne muette est une consonne qui n'est pas suivie directement d'une voyelle : " +
                "seule en fin de mot, ou devant une autre consonne. Ʃ, TƩ, J, Ɲ et H n'ont pas de forme muette, " +
                "car elles ne se trouvent jamais en fin de mot."
        ),
    )
    val combinaisons = Ayneha.COMBINAISONS

    Column(Modifier.fillMaxSize()) {
        Entete("Les accents", retour)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding()
        ) {
            Paragraphe(
                "L'AYNEHA a 4 accents : nasalisé, ouvert, double et muet. Avec les lettres, ils forment " +
                    "${combinaisons.size} combinaisons. Une lettre ne porte jamais plus d'un accent à la fois."
            )
            fiches.forEach { f ->
                val liste = combinaisons.filter { it.second == f.accent.variante }
                TitreSection("Accent ${f.accent.nom} - ${liste.size} combinaisons")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Teinte.carte)
                        .border(1.dp, Teinte.trait, RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.Sable)
                            .padding(6.dp)
                    ) {
                        f.seuls.forEach { cp ->
                            AynehaTexte(Ayneha.str(cp), Modifier.size(34.dp), taille = 0.62f)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(f.explication, color = Teinte.texte, fontSize = 13.sp, lineHeight = 19.sp)
                }
                Spacer(Modifier.height(8.dp))
                Grille(liste, colonnes = 5, rtl = true, espace = 6.dp) { (l, v), m ->
                    val cp = l.variantes.getValue(v)
                    CaseGlyphe(
                        Ayneha.str(cp), Ayneha.latin(cp), m,
                        hauteurGlyphe = 46.dp, taille = 0.62f,
                        onClick = { ouvrir(Ecran.FicheLettre(l.cle)) }
                    )
                }
            }

            TitreSection("Les règles à retenir")
            listOf(
                "Une lettre porte un seul accent à la fois.",
                "Une consonne double est toujours suivie d'une voyelle.",
                "Une consonne non suivie directement d'une voyelle est muette.",
                "Ʃ, TƩ, J, Ɲ et H ne se trouvent jamais en fin de mot.",
                "H est la seule consonne sans aucun accent.",
                "Pas de majuscules, pas de gras, pas de soulignement : ils cacheraient les accents placés sous les lettres.",
            ).forEach { r ->
                Row(Modifier.padding(bottom = 6.dp)) {
                    Text("•  ", color = Palette.Orange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Paragraphe(r)
                }
            }
        }
    }
}
