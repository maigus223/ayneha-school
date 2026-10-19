// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val NOMBRE_MAX_CHIFFRES = 12

/** Groupe un nombre latin par milliers pour l'affichage (1 234 567). */
fun milliers(n: String): String = n.reversed().chunked(3).joinToString(" ").reversed()

@Composable
fun EcranChiffres(retour: () -> Unit) {
    var saisie by rememberSaveable { mutableStateOf("2026") }
    var page by rememberSaveable { mutableIntStateOf(0) } // centaine affichée : 0 -> 0..99, 99 -> 9900..9999
    val c = Teinte

    Column(Modifier.fillMaxSize()) {
        Entete("Les chiffres", retour)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding()
        ) {
            Paragraphe(
                "Les 10 chiffres AYNEHA. Un nombre s'écrit aussi de droite à gauche : le premier chiffre " +
                    "lu est tout à droite. 10 s'écrit donc avec le 1 à droite et le 0 à gauche."
            )
            TitreSection("Les 10 chiffres")
            Grille((0..9).toList(), colonnes = 5, rtl = true, espace = 6.dp) { d, m ->
                CaseGlyphe(Ayneha.str(Ayneha.chiffre(d)), d.toString(), m, hauteurGlyphe = 50.dp, taille = 0.6f)
            }

            // ---- Convertisseur ----
            TitreSection("Écrire un nombre")
            OutlinedTextField(
                value = saisie,
                onValueChange = { v -> saisie = v.filter { it.isDigit() }.take(NOMBRE_MAX_CHIFFRES) },
                label = { Text("Nombre en chiffres latins") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = couleursChamp(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            val nombre = saisie.trimStart('0').ifEmpty { if (saisie.isEmpty()) "" else "0" }
            CarteSable(Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                    if (nombre.isEmpty()) {
                        Text("Tape un nombre.", color = Palette.TexteSombre2, fontSize = 14.sp)
                    } else {
                        AynehaTexte(
                            Ayneha.nombreVisuel(nombre),
                            Modifier.fillMaxWidth().height(70.dp),
                            taille = 0.62f
                        )
                        Text(milliers(nombre), color = Palette.TexteSombre2, fontSize = 14.sp)
                    }
                }
            }

            // ---- Le nombre en toutes lettres (songhay) ----
            if (nombre.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                val enLettres = NombresSonghay.enLettres(nombre)
                CarteSable(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("En toutes lettres", color = Palette.TexteSombre2, fontSize = 12.sp)
                        if (enLettres == null) {
                            Text("Jusqu'à 999 999 pour l'instant.", color = Palette.TexteSombre2, fontSize = 14.sp)
                        } else {
                            Text(enLettres, color = Palette.Encre, fontSize = 16.sp)
                            Spacer(Modifier.height(6.dp))
                            AynehaParagraphes(
                                listOf(Transcripteur.transcrire(enLettres).flatten().map { it.points }),
                                Modifier.fillMaxWidth(),
                                tailleSp = 26f
                            )
                        }
                    }
                }
            }

            // ---- Ordinaux et virgule ----
            TitreSection("Ordinaux et nombres à virgule")
            Paragraphe(
                "Pour un ordinal (1er, 2e, 3e...), on place la lettre I en haut, juste après le nombre. " +
                    "La virgule décimale est la virgule AYNEHA, placée en bas."
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEach { n ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Palette.Sable)
                            .border(1.dp, Palette.SableFonce, RoundedCornerShape(14.dp))
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Ordinal(n)
                        Text(if (n == 1) "1er" else "${n}e", color = Palette.TexteSombre2, fontSize = 12.sp)
                    }
                }
                Column(
                    Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Palette.Sable)
                        .border(1.dp, Palette.SableFonce, RoundedCornerShape(14.dp))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AynehaMot(Transcripteur.mot("3,5"), Modifier.fillMaxWidth().height(46.dp), taille = 0.62f)
                    Text("3,5", color = Palette.TexteSombre2, fontSize = 12.sp)
                }
            }

            // ---- Tableau de 0 à 9999 ----
            TitreSection("Compter de 0 à 9999")
            val debut = page * 100
            Row(verticalAlignment = Alignment.CenterVertically) {
                BoutonSecondaire("◀ -100", { if (page > 0) page-- }, Modifier.weight(1f), actif = page > 0)
                Text(
                    "${milliers(debut.toString())} à ${milliers((debut + 99).toString())}",
                    color = c.texte, fontSize = 14.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.4f)
                )
                BoutonSecondaire("+100 ▶", { if (page < 99) page++ }, Modifier.weight(1f), actif = page < 99)
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0, 25, 50, 99).forEach { p ->
                    Puce(milliers((p * 100).toString()), p == page, { page = p }, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
            // 20 rangées de 5, lues de droite à gauche (le plus petit nombre en haut à droite).
            Grille((debut until debut + 100).toList(), colonnes = 5, rtl = true, espace = 5.dp) { n, m ->
                CaseGlyphe(Ayneha.nombreVisuel(n), n.toString(), m, hauteurGlyphe = 34.dp, taille = 0.62f)
            }
        }
    }
}

/**
 * Nombre ordinal : le nombre, puis la lettre I en petit et en haut, collée juste à sa gauche
 * (juste après lui en lecture RTL). Dessiné d'un seul tenant pour garder les deux bien proches.
 */
@Composable
private fun Ordinal(n: Int) {
    val tf = rememberAynehaTypeface()
    Canvas(Modifier.fillMaxWidth().height(46.dp)) {
        val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf; color = Palette.Encre.toArgb(); textSize = size.height * 0.62f
        }
        val petit = android.graphics.Paint(p).apply { textSize = size.height * 0.36f }
        val nombre = Ayneha.nombreVisuel(n)
        val i = Ayneha.str(Ayneha.lettre("I").base)
        val rn = android.graphics.Rect(); p.getTextBounds(nombre, 0, nombre.length, rn)
        val ri = android.graphics.Rect(); petit.getTextBounds(i, 0, i.length, ri)
        val ecart = size.height * 0.05f
        val total = ri.width() + ecart + rn.width()
        val x0 = (size.width - total) / 2f
        val yN = (size.height - rn.height()) / 2f - rn.top
        drawContext.canvas.nativeCanvas.drawText(i, x0 - ri.left, yN + rn.top - ri.top, petit)
        drawContext.canvas.nativeCanvas.drawText(nombre, x0 + ri.width() + ecart - rn.left, yN, p)
    }
}
