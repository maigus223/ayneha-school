// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EcranAPropos(retour: () -> Unit) {
    val c = Teinte
    Column(Modifier.fillMaxSize()) {
        Entete("À propos", retour)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.Start
        ) {
            CarteSable(Modifier.fillMaxWidth()) {
                AynehaMot(Transcripteur.mot("ayneha"), Modifier.fillMaxWidth().height(70.dp), taille = 0.6f)
            }

            TitreSection("L'écriture AYNEHA")
            Paragraphe(
                "L'AYNEHA est un alphabet original pour écrire la langue songhay, conçu et créé par " +
                    "Mahamadou Issiaka MAIGA (MAIGUS), originaire de Gao. Il s'écrit de droite à gauche, " +
                    "sans majuscules ni minuscules : 5 voyelles, 22 consonnes, 4 accents, 10 chiffres."
            )

            TitreSection("Confidentialité")
            Paragraphe(
                "L'application ne demande aucune permission, n'utilise pas internet et ne collecte aucune donnée. " +
                    "Tes réglages et tes meilleurs scores restent sur ton téléphone. Les images que tu partages " +
                    "sont créées sur ton téléphone et envoyées uniquement là où tu le choisis."
            )

            TitreSection("Logiciel libre")
            Paragraphe(
                "AYNEHA School est un logiciel libre, développé par MAIGUS avec " +
                    "l'aide d'outils d'IA (Claude, Anthropic). Il est publié sous licence " +
                    "GPL v3 ou ultérieure : vous pouvez l'utiliser, l'étudier, le modifier " +
                    "et le partager. Toute version modifiée distribuée doit rester libre, " +
                    "sous la même licence. Polices : SIL Open Font License 1.1. Logos et " +
                    "images : CC BY-SA 4.0. Code source : https://github.com/maigus223/ayneha-school"
            )

            TitreSection("Contact")
            Paragraphe("Développé par MAIGUS : gwokmt2q@duck.com")

            Spacer(Modifier.height(24.dp))
            Text(
                "AYNEHA School ${BuildConfig.VERSION_NAME}  -  By MAIGUS",
                color = c.texte2, fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
