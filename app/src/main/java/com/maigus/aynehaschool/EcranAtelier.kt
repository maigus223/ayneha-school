// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EcranAtelier(retour: () -> Unit) {
    var onglet by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Entete("Atelier de transcription", retour)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Teinte.carte)
                .padding(4.dp)
        ) {
            listOf("Transcrire", "S'exercer").forEachIndexed { i, t ->
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (onglet == i) Teinte.carte2 else Teinte.carte)
                        .clickable { onglet = i }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        t,
                        color = if (onglet == i) Teinte.texte else Teinte.texte2,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        if (onglet == 0) OngletTranscrire() else OngletExercer()
    }
}

// =====================================================================
// Transcrire : latin -> AYNEHA en direct
// =====================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OngletTranscrire() {
    val context = LocalContext.current
    val presse = LocalClipboardManager.current
    val reglages = LocalReglages.current
    val c = Teinte
    var champ by remember { mutableStateOf(TextFieldValue("ayneha sɔŋɔy")) }
    var pourquoi by rememberSaveable { mutableStateOf(true) }
    val resultat = remember(champ.text) { Transcripteur.transcrire(champ.text) }
    val paragraphes = resultat.map { p -> p.map { it.points } }

    fun inserer(s: String) {
        val t = champ.text
        val a = champ.selection.min
        val b = champ.selection.max
        val nouveau = t.substring(0, a) + s + t.substring(b)
        champ = TextFieldValue(nouveau, TextRange(a + s.length))
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .navigationBarsPadding()
    ) {
        OutlinedTextField(
            value = champ,
            onValueChange = { champ = it },
            label = { Text("Texte en latin") },
            minLines = 2,
            colors = couleursChamp(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        // Touches rapides
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Transcripteur.TOUCHES_RAPIDES.forEach { k ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(c.carte2)
                        .clickable { inserer(k) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(k, color = c.texte, fontSize = 17.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        CarteSable(Modifier.fillMaxWidth().heightIn(min = 120.dp)) {
            if (paragraphes.all { it.isEmpty() }) {
                Text("Le texte AYNEHA s'affichera ici.", color = Palette.TexteSombre2, fontSize = 14.sp)
            } else {
                AynehaParagraphes(paragraphes.filter { it.isNotEmpty() }, Modifier.fillMaxWidth(), tailleSp = 34f)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BoutonSecondaire(
                "Copier",
                {
                    presse.setText(AnnotatedString(Transcripteur.texteNaturel(resultat)))
                    Toast.makeText(context, "Texte AYNEHA copié.", Toast.LENGTH_SHORT).show()
                },
                Modifier.weight(1f)
            )
            Bouton("Partager en image", { Partage.partager(context, paragraphes, reglages) }, Modifier.weight(1.3f))
        }

        Spacer(Modifier.height(12.dp))
        Text(
            if (pourquoi) "Pourquoi ?  (masquer)" else "Pourquoi ?",
            color = c.lien,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { pourquoi = !pourquoi }
                .padding(vertical = 6.dp)
        )
        if (pourquoi) {
            resultat.flatten().filter { it.explications.isNotEmpty() }.forEach { bloc ->
                Text(bloc.latin, color = c.texte, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    bloc.explications.forEach { e ->
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(c.carte)
                                .border(1.dp, c.trait, RoundedCornerShape(10.dp))
                                .padding(start = 8.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${e.brut} : ${e.raison}", color = c.texte, fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Palette.Sable)
                                    .padding(2.dp)
                            ) {
                                AynehaTexte(Ayneha.str(e.cp), Modifier.size(28.dp), taille = 0.7f)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Règles : voyelle + n = nasalisée ; lettre doublée = double ; consonne sans voyelle juste après = muette ; " +
                    "c = TƩ, sh ou š = Ʃ, ŋ = Ŋ, ng = N muette + G, ny ou ɲ = Ɲ, gn = G muette + N, dj = DJ, v = W ; è = ɛ et ô = ɔ (voyelles ouvertes).",
                color = c.texte2, fontSize = 12.sp, lineHeight = 17.sp
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

// =====================================================================
// S'exercer : composer soi-même le mot en AYNEHA
// =====================================================================

@Composable
private fun OngletExercer() {
    val c = Teinte
    val test = BuildConfig.DEBUG
    val reserve = remember {
        Contenu.visibles(Contenu.TOUS_LES_MOTS + Contenu.NOMS, test).filter { it.composable }
    }
    if (reserve.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Paragraphe(
                "Les mots d'exercice arrivent bientôt : ils sont en cours de validation par l'auteur de l'AYNEHA. " +
                    "En attendant, entraîne-toi avec l'onglet « Transcrire »."
            )
        }
        return
    }
    var numero by rememberSaveable { mutableIntStateOf(reserve.indices.random()) }
    var compose by remember { mutableStateOf(listOf<Int>()) }
    var lettreOuverte by remember { mutableStateOf<Lettre?>(null) }
    var verifie by remember { mutableStateOf(false) }
    val mot = reserve[numero % reserve.size]
    val juste = mot.points
    val bon = verifie && compose == juste

    fun ajouter(cp: Int) { if (!verifie) compose = compose + cp; lettreOuverte = null }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .navigationBarsPadding()
    ) {
        Paragraphe("Écris ce mot en AYNEHA avec le clavier ci-dessous :")
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(mot.latin, color = c.texte, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(10.dp))
            Text("(${mot.francais})", color = c.texte2, fontSize = 14.sp)
            if (!mot.valide) { Spacer(Modifier.width(8.dp)); BadgeAValider() }
        }
        Spacer(Modifier.height(10.dp))

        // Ta réponse
        Box(
            Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    when {
                        !verifie -> Palette.Sable
                        bon -> Palette.Vert
                        else -> Palette.Terre
                    }
                )
                .padding(10.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (compose.isEmpty()) Text("Ta réponse", color = Palette.TexteSombre2, fontSize = 14.sp)
            else AynehaMot(compose, Modifier.fillMaxSize(), couleur = if (verifie) Palette.Blanc else Palette.Encre, taille = 0.62f)
        }
        if (verifie && !bon) {
            Spacer(Modifier.height(8.dp))
            Text("La bonne écriture :", color = c.texte2, fontSize = 13.sp)
            CarteSable(Modifier.fillMaxWidth()) {
                Column {
                    AynehaMot(juste, Modifier.fillMaxWidth().height(60.dp), taille = 0.62f)
                    Text(
                        Transcripteur.bloc(Transcripteur.normaliser(mot.latin)).explications.joinToString(" · ") { "${it.brut} : ${it.raison}" },
                        color = Palette.TexteSombre2, fontSize = 12.sp, lineHeight = 17.sp
                    )
                }
            }
        }
        if (verifie && bon) {
            Spacer(Modifier.height(6.dp))
            Text("Bravo, c'est juste !", color = Palette.Vert, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))

        // Clavier de glyphes : une lettre, puis sa forme
        val ouverte = lettreOuverte
        if (ouverte != null) {
            Text("Choisis la forme de ${ouverte.nom} :", color = c.texte2, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Grille(ouverte.variantesOrdonnees, colonnes = 5, rtl = true, espace = 6.dp) { (v, cp), m ->
                CaseGlyphe(Ayneha.str(cp), v.nom, m, hauteurGlyphe = 46.dp, taille = 0.62f, onClick = { ajouter(cp) })
            }
            Spacer(Modifier.height(10.dp))
        }
        Grille(Ayneha.LETTRES, colonnes = 7, rtl = true, espace = 5.dp) { l, m ->
            CaseGlyphe(
                Ayneha.str(l.base), null, m,
                hauteurGlyphe = 36.dp,
                selectionne = l == ouverte,
                onClick = {
                    if (verifie) return@CaseGlyphe
                    if (l.variantes.size == 1) ajouter(l.base) else lettreOuverte = l
                }
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BoutonSecondaire("Effacer", { if (!verifie) compose = compose.dropLast(1) }, Modifier.weight(1f))
            if (!verifie) {
                Bouton("Vérifier", { verifie = true; lettreOuverte = null }, Modifier.weight(1f), actif = compose.isNotEmpty())
            } else {
                Bouton(
                    "Mot suivant",
                    {
                        numero = (numero + 1 + (0 until reserve.size.coerceAtLeast(1)).random()) % reserve.size
                        compose = emptyList(); verifie = false; lettreOuverte = null
                    },
                    Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
