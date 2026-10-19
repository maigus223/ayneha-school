// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EcranAlphabet(ouvrir: (Ecran) -> Unit, retour: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Entete("L'alphabet", retour)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding()
        ) {
            Paragraphe(
                "L'AYNEHA s'écrit de droite à gauche. Il n'a ni majuscules ni minuscules : chaque lettre " +
                    "n'a qu'une seule forme. Voici les 27 lettres dans l'ordre alphabétique AYNEHA : " +
                    "d'abord les 5 voyelles, puis les 22 consonnes. Touche une lettre pour l'étudier."
            )
            TitreSection("Les 5 voyelles")
            Grille(Ayneha.VOYELLES, colonnes = 5, rtl = true) { l, m ->
                CaseGlyphe(Ayneha.str(l.base), l.nom, m, onClick = { ouvrir(Ecran.FicheLettre(l.cle)) })
            }
            TitreSection("Les 22 consonnes")
            Grille(Ayneha.CONSONNES, colonnes = 4, rtl = true) { l, m ->
                CaseGlyphe(Ayneha.str(l.base), l.nom, m, onClick = { ouvrir(Ecran.FicheLettre(l.cle)) })
            }
            Spacer(Modifier.height(10.dp))
            Paragraphe("On lit chaque rangée de droite à gauche, comme l'écriture AYNEHA.")
        }
    }
}

@Composable
fun EcranFicheLettre(cle: String, ouvrir: (Ecran) -> Unit, remplacer: (Ecran) -> Unit, retour: () -> Unit) {
    val l = Ayneha.lettre(cle)
    val toutes = Ayneha.LETTRES
    val i = toutes.indexOf(l)
    val test = BuildConfig.DEBUG
    val context = LocalContext.current
    val c = Teinte

    Column(Modifier.fillMaxSize()) {
        Entete("Lettre ${l.nom}", retour) {
            Text("${i + 1} / ${toutes.size}", color = Palette.TexteClair2, fontSize = 13.sp, modifier = Modifier.padding(end = 10.dp))
        }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            // Grande lettre
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Palette.SableClair)
                    .border(1.dp, Palette.SableFonce, RoundedCornerShape(22.dp))
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AynehaTexte(Ayneha.str(l.base), Modifier.fillMaxWidth().height(150.dp), taille = 0.6f)
                Text(l.nom, color = Palette.Encre, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "se dit « ${l.epellation} »",
                    color = Palette.TexteSombre2, fontSize = 13.sp
                )
            }

            TitreSection("Variantes")
            val formes = l.variantesOrdonnees
            if (formes.size == 1) {
                Paragraphe("La lettre H est la seule consonne qui ne prend aucun accent : elle garde toujours cette forme.")
            } else {
                Grille(formes, colonnes = formes.size.coerceAtMost(4), rtl = true) { (v, cp), m ->
                    CaseGlyphe(Ayneha.str(cp), v.nom, m, hauteurGlyphe = 54.dp, onClick = { ouvrir(Ecran.Tracer(cp)) })
                }
            }
            Spacer(Modifier.height(4.dp))
            Paragraphe(regleDeLaLettre(l))

            val exemples = Contenu.exemples(l, test)
            if (exemples.isNotEmpty()) {
                TitreSection(if (exemples.size > 1) "Exemples" else "Exemple")
                exemples.forEach { m ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Palette.SableClair)
                            .border(1.dp, Palette.SableFonce, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(m.latin, color = Palette.Encre, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(m.francais, color = Palette.TexteSombre2, fontSize = 12.sp)
                            if (!m.valide) BadgeAValider(Modifier.padding(top = 2.dp))
                        }
                        AynehaMot(m.points, Modifier.weight(1f).height(48.dp), taille = 0.62f)
                    }
                }
            }
        }

        // Navigation
        Row(
            Modifier
                .fillMaxWidth()
                .background(c.fond)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BoutonSecondaire("◀", { if (i > 0) remplacer(Ecran.FicheLettre(toutes[i - 1].cle)) }, Modifier.width(64.dp), actif = i > 0)
            Bouton("✍ Tracer", { ouvrir(Ecran.Tracer(l.base)) }, Modifier.weight(1f))
            BoutonSecondaire("▶", { if (i < toutes.size - 1) remplacer(Ecran.FicheLettre(toutes[i + 1].cle)) }, Modifier.width(64.dp), actif = i < toutes.size - 1)
        }
    }
}

/** Rappel des règles propres à la lettre. */
fun regleDeLaLettre(l: Lettre): String = when {
    l.cle == "E" || l.cle == "O" ->
        "${l.nom} peut être nasalisée, ouverte ou double. La forme ouverte se tape ${if (l.cle == "E") "ɛ ou è" else "ɔ ou ô"}."
    l.voyelle ->
        "${l.nom} peut être nasalisée (${l.nom.lowercase()} + n, ou ${Ayneha.latin(l.variantes.getValue(Variante.NASALISEE))}) ou double (${l.nom.lowercase()}${l.nom.lowercase()})."
    l.cle == "H" -> "H ne se trouve jamais en fin de mot en songhay."
    l.variantes.containsKey(Variante.MUETTE) ->
        "Double quand la consonne est doublée (elle est alors toujours suivie d'une voyelle). " +
            "Muette quand elle n'est pas suivie directement d'une voyelle : en fin de mot, ou devant une autre consonne."
    else ->
        "${l.nom} peut être double (toujours suivie d'une voyelle). Elle n'a pas de forme muette : " +
            "en songhay, ${l.nom} ne se trouve jamais en fin de mot."
}
