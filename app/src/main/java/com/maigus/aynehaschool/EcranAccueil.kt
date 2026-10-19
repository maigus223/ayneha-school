// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Section(
    val titre: String,
    val detail: String,
    val glyphe: String, // déjà dans l'ordre de dessin
    val ecran: Ecran,
)

@Composable
fun EcranAccueil(ouvrir: (Ecran) -> Unit) {
    val a = Ayneha
    val sections = listOf(
        Section("L'alphabet", "27 lettres", a.str(a.lettre("A").base), Ecran.Alphabet),
        Section("Les accents", "4 accents", a.str(a.lettre("E").variantes.getValue(Variante.OUVERTE)), Ecran.Accents),
        Section("Le syllabaire", "Consonne + voyelle", a.visuel(listOf(a.lettre("B").base, a.lettre("A").base)), Ecran.Syllabaire),
        Section("Les chiffres", "De 0 à 9999", a.nombreVisuel("10"), Ecran.Chiffres),
        Section("Tracer", "Écrire avec le doigt", a.str(a.lettre("I").base), Ecran.Tracer()),
        Section("Quiz", "6 jeux", a.str(a.lettre("K").base), Ecran.Quiz),
        Section("Atelier", "Transcrire du latin", a.str(a.lettre("S").base), Ecran.Atelier),
        Section("Lecture", "Vrais mots songhay", a.visuel(Transcripteur.mot("gao")), Ecran.Lecture),
    )
    val c = Teinte

    // Tout l'accueil tient sur un seul écran, sans défilement : les cartes se partagent la hauteur.
    Column(Modifier.fillMaxSize()) {
        // ---- Bandeau d'accueil ----
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Palette.NuitCarte2, Palette.NuitBarre)))
                .statusBarsPadding()
        ) {
            TombeauAskia(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.72f)
                    .height(96.dp)
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AynehaTexte(a.str(a.lettre("H").base), Modifier.size(54.dp), couleur = Palette.Orange, remplissage = 0.9f)
                Text(
                    buildAnnotatedString {
                        append("AYNEHA ")
                        withStyle(SpanStyle(color = Palette.Orange)) { append("School") }
                    },
                    color = Palette.Sable,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
                AynehaMot(
                    Transcripteur.mot("ayneha"),
                    Modifier
                        .fillMaxWidth(0.4f)
                        .height(30.dp),
                    couleur = Palette.OrangeDoux,
                    remplissage = 0.9f
                )
            }
            // Réglages
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Palette.Blanc.copy(alpha = 0.08f))
                    .clickable { ouvrir(Ecran.Reglages) },
                contentAlignment = Alignment.Center
            ) {
                IconeReglages(Modifier.size(20.dp))
            }
        }
        Frise(hauteur = 12.dp)

        // ---- Sections : 4 rangées + À propos ----
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            sections.chunked(2).forEach { ligne ->
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    ligne.forEach { s ->
                        CarteSection(s, Modifier.weight(1f).fillMaxHeight()) { ouvrir(s.ecran) }
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, c.trait, RoundedCornerShape(14.dp))
                    .clickable { ouvrir(Ecran.APropos) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("À propos", color = c.texte, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("L'auteur, les licences, la confidentialité", color = c.texte2, fontSize = 11.sp)
            }
        }

        // ---- Pied de page ----
        Text(
            "By MAIGUS",
            color = c.texte2,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun CarteSection(s: Section, modifier: Modifier, onClick: () -> Unit) {
    val c = Teinte
    Row(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.carte)
            .border(1.dp, c.trait, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Palette.Sable),
            contentAlignment = Alignment.Center
        ) {
            AynehaTexte(s.glyphe, Modifier.size(34.dp), taille = 0.7f)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                s.titre,
                color = c.texte,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                s.detail,
                color = c.texte2,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Liseré orange
        Box(
            Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(Palette.Orange)
        )
    }
}

/** Silhouette du Tombeau des Askia (Gao) : pyramide de banco hérissée de ses pieux (toron). */
@Composable
fun TombeauAskia(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val coul = Palette.NuitAskia.copy(alpha = 0.22f)
        val cx = w / 2f
        val corps = Path().apply {
            moveTo(cx - h * 0.85f, h)
            lineTo(cx - h * 0.5f, h * 0.32f)
            lineTo(cx + h * 0.5f, h * 0.32f)
            lineTo(cx + h * 0.85f, h)
            close()
        }
        val tete = Path().apply {
            moveTo(cx - h * 0.3f, h * 0.32f)
            lineTo(cx - h * 0.22f, h * 0.06f)
            lineTo(cx + h * 0.22f, h * 0.06f)
            lineTo(cx + h * 0.3f, h * 0.32f)
            close()
        }
        drawPath(corps, coul)
        drawPath(tete, coul)
        // Pieux en bois qui dépassent des murs
        listOf(0.42f, 0.58f, 0.76f).forEach { f ->
            val y = h * f
            val dx = h * (0.5f + (f - 0.32f) / 0.68f * 0.35f)
            drawLine(coul, Offset(cx - dx - h * 0.14f, y), Offset(cx - dx + h * 0.06f, y), strokeWidth = h * 0.035f)
            drawLine(coul, Offset(cx + dx - h * 0.06f, y), Offset(cx + dx + h * 0.14f, y), strokeWidth = h * 0.035f)
        }
        listOf(0.16f, 0.25f).forEach { f ->
            val y = h * f
            drawLine(coul, Offset(cx - h * 0.36f, y), Offset(cx - h * 0.18f, y), strokeWidth = h * 0.03f)
            drawLine(coul, Offset(cx + h * 0.18f, y), Offset(cx + h * 0.36f, y), strokeWidth = h * 0.03f)
        }
        drawRect(coul, topLeft = Offset(0f, h * 0.96f), size = androidx.compose.ui.geometry.Size(w, h * 0.04f))
    }
}
