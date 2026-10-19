// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Les différents jeux du quiz. */
enum class ModeQuiz(val titre: String, val consigne: String) {
    RECONNAITRE("Reconnaître la lettre", "Quel est le nom de cette lettre ?"),
    RETROUVER("Retrouver la lettre", "Touche la bonne lettre."),
    ACCENT("Quel accent ?", "Quel accent porte cette lettre ?"),
    SYLLABES("Lire la syllabe", "Comment se lit cette syllabe ?"),
    CHIFFRES("Les chiffres", "Quel est ce nombre ?"),
    ORDRE("L'ordre AYNEHA", "Touche ces lettres dans l'ordre alphabétique AYNEHA."),
}

/** Une question : ce qu'on montre, les 4 propositions et l'indice de la bonne. */
private data class Question(
    val montreGlyphe: String?,       // glyphe(s) affiché(s) en grand, déjà dans l'ordre de dessin
    val montreTexte: String?,        // texte latin affiché en grand
    val propositions: List<String>,  // glyphes (ordre de dessin) ou textes latins
    val propositionsGlyphes: Boolean,
    val bonne: Int,
    val lettresOrdre: List<Lettre> = emptyList(), // jeu ORDRE seulement
)

private const val NB_QUESTIONS = 10

private fun creerQuestions(mode: ModeQuiz): List<Question> = (1..NB_QUESTIONS).map {
    when (mode) {
        ModeQuiz.RECONNAITRE, ModeQuiz.RETROUVER -> {
            val choix = Ayneha.LETTRES.shuffled().take(4)
            val bonne = (0..3).random()
            val l = choix[bonne]
            if (mode == ModeQuiz.RECONNAITRE)
                Question(Ayneha.str(l.base), null, choix.map { it.nom }, false, bonne)
            else
                Question(null, l.nom, choix.map { Ayneha.str(it.base) }, true, bonne)
        }
        ModeQuiz.ACCENT -> {
            val (l, v) = Ayneha.COMBINAISONS.random()
            val accents = Accent.values().toList()
            Question(
                Ayneha.str(l.variantes.getValue(v)), null,
                accents.map { it.nom }, false, accents.indexOfFirst { it.variante == v }
            )
        }
        ModeQuiz.SYLLABES -> {
            val c = Ayneha.CONSONNES.random()
            val formes = FormeVoyelle.values()
            val possibles = formes.flatMap { f -> f.voyelles().map { v -> syllabeEtLecture(c, v, f) } }
                .distinctBy { it.second }
                .shuffled()
                .take(4)
            val bonne = (0..3).random()
            Question(Ayneha.visuel(possibles[bonne].first), null, possibles.map { it.second }, false, bonne)
        }
        ModeQuiz.CHIFFRES -> {
            // Nombres de 0 à 9999. Les 4 propositions ont toujours le même nombre de chiffres
            // que le nombre montré (sinon la bonne réponse se devinerait à sa longueur).
            val plage = listOf((0..9), (10..99), (100..999), (1000..9999)).random()
            val ns = plage.shuffled().take(4)
            val bonne = (0..3).random()
            Question(Ayneha.nombreVisuel(ns[bonne]), null, ns.map { it.toString() }, false, bonne)
        }
        ModeQuiz.ORDRE -> {
            val lettres = Ayneha.LETTRES.shuffled().take(4)
            Question(null, null, emptyList(), true, -1, lettresOrdre = lettres)
        }
    }
}

@Composable
fun EcranQuiz(retour: () -> Unit) {
    val reglages = LocalReglages.current
    val c = Teinte
    var mode by remember { mutableStateOf<ModeQuiz?>(null) }
    var questions by remember { mutableStateOf(listOf<Question>()) }
    var index by remember { mutableIntStateOf(0) }
    var reponse by remember { mutableStateOf<Int?>(null) }        // jeux à 4 choix
    var suite by remember { mutableStateOf(listOf<Lettre>()) }     // jeu ORDRE : lettres touchées
    var score by remember { mutableIntStateOf(0) }
    var record by remember { mutableIntStateOf(0) }

    fun commencer(m: ModeQuiz) {
        mode = m
        questions = creerQuestions(m)
        index = 0; reponse = null; suite = emptyList(); score = 0
        record = reglages.record("quiz_${m.name}")
    }

    // Pendant une partie, le bouton retour du téléphone ramène au choix du jeu.
    BackHandler(enabled = mode != null) { mode = null }

    Column(Modifier.fillMaxSize()) {
        Entete(mode?.titre ?: "Quiz", if (mode == null) retour else ({ mode = null }))
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding()
        ) {
            val m = mode
            if (m == null) {
                // ---- Choix du jeu ----
                Paragraphe("Choisis un jeu. Chaque partie compte $NB_QUESTIONS questions.")
                Spacer(Modifier.height(12.dp))
                ModeQuiz.values().forEach { jm ->
                    val rec = reglages.record("quiz_${jm.name}")
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(c.carte)
                            .border(1.dp, c.trait, RoundedCornerShape(16.dp))
                            .clickable { commencer(jm) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f).padding(16.dp)) {
                            Text(jm.titre, color = c.texte, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text(jm.consigne, color = c.texte2, fontSize = 13.sp)
                            if (rec > 0) Text("Meilleur score : $rec / $NB_QUESTIONS", color = c.etiquette, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("▶", color = Palette.Orange, fontSize = 18.sp, modifier = Modifier.padding(end = 16.dp))
                    }
                }
            } else if (index >= questions.size) {
                // ---- Fin de partie ----
                Spacer(Modifier.height(30.dp))
                Text(
                    "$score / $NB_QUESTIONS",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Palette.Orange, fontSize = 52.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    when {
                        score == NB_QUESTIONS -> "Parfait !"
                        score >= 7 -> "Très bien !"
                        score >= 4 -> "Pas mal, continue à t'entraîner."
                        else -> "Courage, revois l'alphabet et réessaie."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = c.texte,
                    fontSize = 17.sp
                )
                if (score > record) {
                    Text(
                        "Nouveau record !",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Palette.Vert, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(24.dp))
                Bouton("Rejouer", { commencer(m) }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                BoutonSecondaire("Choisir un autre jeu", { mode = null }, Modifier.fillMaxWidth())
            } else {
                // ---- Question ----
                val q = questions[index]
                val repondu = if (m == ModeQuiz.ORDRE) suite.size == q.lettresOrdre.size else reponse != null
                Text(
                    "Question ${index + 1} / $NB_QUESTIONS  -  Score : $score",
                    color = c.texte2, fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    m.consigne,
                    color = c.texte, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))

                if (m == ModeQuiz.ORDRE) {
                    QuestionOrdre(q.lettresOrdre, suite) { l ->
                        if (suite.size < q.lettresOrdre.size && l !in suite) {
                            suite = suite + l
                            if (suite.size == q.lettresOrdre.size && suite == q.lettresOrdre.sortedBy { it.rang }) score++
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Palette.Sable),
                        contentAlignment = Alignment.Center
                    ) {
                        if (q.montreGlyphe != null) {
                            AynehaTexte(q.montreGlyphe, Modifier.fillMaxSize().padding(18.dp), taille = 0.62f)
                        } else {
                            Text(q.montreTexte ?: "", color = Palette.Encre, fontSize = 56.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    q.propositions.chunked(2).forEachIndexed { ligne, paire ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            paire.forEachIndexed { j, prop ->
                                val i = ligne * 2 + j
                                val fond: Color = when {
                                    reponse == null -> Palette.SableClair
                                    i == q.bonne -> Palette.Vert
                                    i == reponse -> Palette.Terre
                                    else -> Palette.SableClair
                                }
                                val surFond = if (fond == Palette.SableClair) Palette.Encre else Palette.Blanc
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(fond)
                                        .border(1.5.dp, Palette.SableFonce, RoundedCornerShape(16.dp))
                                        .clickable(enabled = reponse == null) {
                                            reponse = i
                                            if (i == q.bonne) score++
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (q.propositionsGlyphes) {
                                        AynehaTexte(prop, Modifier.fillMaxSize().padding(10.dp), couleur = surFond, taille = 0.7f)
                                    } else {
                                        Text(prop, color = surFond, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                if (repondu) {
                    Spacer(Modifier.height(6.dp))
                    Bouton(
                        if (index + 1 < questions.size) "Question suivante" else "Voir mon score",
                        {
                            index++
                            reponse = null
                            suite = emptyList()
                            if (index >= questions.size) reglages.noterRecord("quiz_${m.name}", score)
                        },
                        Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** Jeu de l'ordre : 4 lettres mélangées en haut, la suite touchée en bas, puis la correction. */
@Composable
private fun QuestionOrdre(lettres: List<Lettre>, suite: List<Lettre>, toucher: (Lettre) -> Unit) {
    val c = Teinte
    val fini = suite.size == lettres.size
    val juste = lettres.sortedBy { it.rang }
    Grille(lettres, colonnes = 4, rtl = true) { l, m ->
        CaseGlyphe(
            Ayneha.str(l.base), null, m,
            hauteurGlyphe = 64.dp,
            selectionne = l in suite,
            onClick = { toucher(l) }
        )
    }
    Spacer(Modifier.height(14.dp))
    Text("Ta réponse (de droite à gauche) :", color = c.texte2, fontSize = 13.sp)
    Spacer(Modifier.height(6.dp))
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (k in 0 until lettres.size) {
                val l = suite.getOrNull(k)
                val bon = fini && l == juste[k]
                Box(
                    Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            when {
                                !fini -> Palette.Sable
                                bon -> Palette.Vert
                                else -> Palette.Terre
                            }
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (l != null) AynehaTexte(Ayneha.str(l.base), Modifier.fillMaxSize(), couleur = if (fini) Palette.Blanc else Palette.Encre, taille = 0.7f)
                }
            }
        }
    }
    if (fini && suite != juste) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Le bon ordre : " + juste.joinToString("  ") { it.nom },
            color = c.texte, fontSize = 14.sp
        )
    }
}
