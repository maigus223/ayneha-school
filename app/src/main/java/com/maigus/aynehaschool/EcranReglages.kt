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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EcranReglages(retour: () -> Unit) {
    val r = LocalReglages.current
    val c = Teinte
    Column(Modifier.fillMaxSize()) {
        Entete("Réglages", retour)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding()
        ) {
            TitreSection("Thème")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Automatique", "Clair", "Sombre").forEachIndexed { i, t ->
                    Puce(t, r.theme == i, { r.changerTheme(i) }, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("« Automatique » suit le réglage du téléphone.", color = c.texte2, fontSize = 12.sp)

            TitreSection("Taille du texte AYNEHA")
            Text(
                "L'AYNEHA ne s'écrit jamais en gras : pour le mettre en valeur, on l'agrandit.",
                color = c.texte2, fontSize = 12.sp
            )
            Slider(
                value = r.tailleAyneha,
                onValueChange = { r.changerTaille(it) },
                valueRange = 0.8f..1.6f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = Palette.Orange,
                    activeTrackColor = Palette.Orange,
                    inactiveTrackColor = c.trait,
                ),
            )
            CarteSable(Modifier.fillMaxWidth()) {
                AynehaParagraphes(listOf(Transcripteur.transcrire("ayneha sɔŋɔy")[0].map { it.points }), Modifier.fillMaxWidth())
            }

            TitreSection("Images partagées")
            Paragraphe("En haut de chaque image : « AYNEHA School ». En bas, si tu le souhaites :")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = r.pseudo,
                onValueChange = { r.changerPseudo(it) },
                label = { Text("Pseudo (facultatif, ${Reglages.PSEUDO_MAX} caractères au plus)") },
                singleLine = true,
                colors = couleursChamp(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Horodatage", color = c.texte, fontSize = 15.sp)
                    Text("Date et heure en bas de l'image", color = c.texte2, fontSize = 12.sp)
                }
                Switch(
                    checked = r.horodatage,
                    onCheckedChange = { r.changerHorodatage(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Palette.Blanc,
                        checkedTrackColor = Palette.Orange,
                    )
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
