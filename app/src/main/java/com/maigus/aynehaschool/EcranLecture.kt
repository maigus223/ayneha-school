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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField

/** Les niveaux de lecture. */
enum class NiveauLecture(val titre: String, val detail: String) {
    SYLLABES("Syllabes", "Consonne + voyelle, toutes les formes"),
    MOTS("Mots", "Mots du quotidien, jours et mois"),
    LEXIQUE("Lexique", "Près de 400 mots par thème : chercher, lire et s'entraîner"),
    NOMS("Noms", "Régions du Mali et patronymes songhay"),
    PHRASES("Petites phrases", "Deux mots ou plus"),
    TEXTES("Textes", "Lire un texte entier"),
}

/** Un élément à lire : l'AYNEHA (mots) et la bonne lecture latine. */
private data class ALire(val mots: List<List<Int>>, val latin: String, val sens: String?, val valide: Boolean)

private const val NB_LECTURES = 10

@Composable
fun EcranLecture(retour: () -> Unit) {
    val test = BuildConfig.DEBUG
    var niveau by remember { mutableStateOf<NiveauLecture?>(null) }
    var texteOuvert by remember { mutableStateOf<TexteLecture?>(null) }
    var entrainement by remember { mutableStateOf(false) }

    BackHandler(enabled = niveau != null || texteOuvert != null) {
        when {
            texteOuvert != null -> texteOuvert = null
            entrainement -> entrainement = false
            else -> niveau = null
        }
    }

    val titre = texteOuvert?.titre ?: niveau?.titre ?: "Lecture"
    Column(Modifier.fillMaxSize()) {
        Entete(titre, {
            when {
                texteOuvert != null -> texteOuvert = null
                entrainement -> entrainement = false
                niveau != null -> niveau = null
                else -> retour()
            }
        })
        val n = niveau
        val t = texteOuvert
        when {
            t != null -> LireTexte(t)
            n == null -> ChoixNiveau(test) { niveau = it }
            n == NiveauLecture.TEXTES -> ListeTextes(test) { texteOuvert = it }
            n == NiveauLecture.LEXIQUE && !entrainement -> ListeLexique(test) { entrainement = true }
            else -> Exercice(n, test)
        }
    }
}

@Composable
private fun ChoixNiveau(test: Boolean, choisir: (NiveauLecture) -> Unit) {
    val c = Teinte
    val reglages = LocalReglages.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .navigationBarsPadding()
    ) {
        Paragraphe("Lis l'AYNEHA, puis choisis la bonne lecture en latin. Commence par les syllabes.")
        // Bandeau seulement s'il reste vraiment des mots non validés (aujourd'hui : aucun).
        val nonValides = (Contenu.TOUS_LES_MOTS + Contenu.LEXIQUE + Contenu.NOMS + Contenu.PHRASES).count { !it.valide } +
            Contenu.TEXTES.count { !it.valide }
        if (test && nonValides > 0) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgeAValider()
                Text(
                    "  Version de test : $nonValides élément(s) pas encore validé(s) s'affichent aussi, avec ce badge.",
                    color = c.texte2, fontSize = 12.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        NiveauLecture.values().forEach { n ->
            val nombre = when (n) {
                NiveauLecture.SYLLABES -> null
                NiveauLecture.MOTS -> Contenu.visibles(Contenu.TOUS_LES_MOTS, test).size
                NiveauLecture.LEXIQUE -> Contenu.visibles(Contenu.LEXIQUE, test).size
                NiveauLecture.NOMS -> Contenu.visibles(Contenu.NOMS, test).size
                NiveauLecture.PHRASES -> Contenu.visibles(Contenu.PHRASES, test).size
                NiveauLecture.TEXTES -> Contenu.textesVisibles(test).size
            }
            val ouvert = nombre == null || nombre > 0
            val rec = reglages.record("lecture_${n.name}")
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(c.carte)
                    .border(1.dp, c.trait, RoundedCornerShape(16.dp))
                    .clickable(enabled = ouvert) { choisir(n) }
                    .padding(16.dp)
            ) {
                Text(n.titre, color = if (ouvert) c.texte else c.texte2, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(n.detail, color = c.texte2, fontSize = 13.sp)
                when {
                    !ouvert -> Text("Bientôt : en attente de validation par l'auteur.", color = c.etiquette, fontSize = 13.sp)
                    rec > 0 -> Text("Meilleur score : $rec / $NB_LECTURES", color = c.etiquette, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun reserve(n: NiveauLecture, test: Boolean): List<ALire> = when (n) {
    NiveauLecture.SYLLABES -> Ayneha.CONSONNES.flatMap { c ->
        Ayneha.VOYELLES.flatMap { v ->
            FormeVoyelle.values().filter { v.variantes.containsKey(it.variante) }.map { f ->
                val (pts, lecture) = syllabeEtLecture(c, v, f)
                ALire(listOf(pts), lecture, null, true)
            }
        }
    }
    NiveauLecture.MOTS -> Contenu.visibles(Contenu.TOUS_LES_MOTS, test).map { ALire(it.mots, it.latin, it.francais, it.valide) }
    NiveauLecture.LEXIQUE -> Contenu.visibles(Contenu.LEXIQUE, test).map { ALire(it.mots, it.latin, it.francais, it.valide) }
    NiveauLecture.NOMS -> Contenu.visibles(Contenu.NOMS, test).map { ALire(it.mots, it.latin, it.francais, it.valide) }
    NiveauLecture.PHRASES -> Contenu.visibles(Contenu.PHRASES, test).map { ALire(it.mots, it.latin, it.francais, it.valide) }
    NiveauLecture.TEXTES -> emptyList()
}

@Composable
private fun Exercice(n: NiveauLecture, test: Boolean) {
    val c = Teinte
    val reglages = LocalReglages.current
    val tout = remember(n) { reserve(n, test) }
    var partie by remember(n) { mutableIntStateOf(0) }
    val questions = remember(n, partie) { tout.shuffled().take(NB_LECTURES) }
    var index by remember(n, partie) { mutableIntStateOf(0) }
    var score by remember(n, partie) { mutableIntStateOf(0) }
    var reponse by remember(n, partie, index) { mutableStateOf<Int?>(null) }
    var revele by remember(n, partie, index) { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .navigationBarsPadding()
    ) {
        if (questions.isEmpty()) {
            Paragraphe("Rien à lire pour l'instant.")
        } else if (index >= questions.size) {
            Spacer(Modifier.height(30.dp))
            Text("$score / ${questions.size}", Modifier.fillMaxWidth(), color = Palette.Orange, fontSize = 52.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Bouton("Rejouer", { partie++ }, Modifier.fillMaxWidth())
        } else {
            QuestionLecture(n, questions[index], tout, partie, index, questions.size, score, reponse, revele,
                repondre = { i, juste -> reponse = i; if (juste) score++ },
                reveler = { revele = true },
                suivant = {
                    index++
                    if (index >= questions.size) reglages.noterRecord("lecture_${n.name}", score)
                })
        }
    }
}

@Composable
private fun QuestionLecture(
    n: NiveauLecture,
    q: ALire,
    tout: List<ALire>,
    partie: Int,
    index: Int,
    total: Int,
    score: Int,
    reponse: Int?,
    revele: Boolean,
    repondre: (Int, Boolean) -> Unit,
    reveler: () -> Unit,
    suivant: () -> Unit,
) {
    val c = Teinte
    val reglages = LocalReglages.current
    Column {
        // 4 propositions : la bonne et 3 autres lectures du même niveau
        val propositions = remember(n, partie, index) {
            (listOf(q.latin) + tout.map { it.latin }.filter { it != q.latin }.distinct().shuffled().take(3)).shuffled()
        }
        val bonne = propositions.indexOf(q.latin)

        Text(
            "${index + 1} / $total  -  Score : $score",
            color = c.texte2, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        CarteSable(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                if (q.mots.size == 1) {
                    AynehaMot(q.mots[0], Modifier.fillMaxWidth().height(110.dp), taille = 0.55f * reglages.tailleAyneha)
                } else {
                    AynehaParagraphes(listOf(q.mots), Modifier.fillMaxWidth(), tailleSp = 40f)
                }
                if (!q.valide) BadgeAValider()
            }
        }
        Spacer(Modifier.height(14.dp))
        propositions.chunked(2).forEachIndexed { ligne, paire ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                paire.forEachIndexed { j, p ->
                    val i = ligne * 2 + j
                    val fond: Color = when {
                        reponse == null && !revele -> c.carte
                        i == bonne -> Palette.Vert
                        i == reponse -> Palette.Terre
                        else -> c.carte
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(fond)
                            .border(1.dp, c.trait, RoundedCornerShape(14.dp))
                            .clickable(enabled = reponse == null && !revele) { repondre(i, i == bonne) }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            p,
                            color = if (fond == c.carte) c.texte else Palette.Blanc,
                            fontSize = if (p.length > 12) 15.sp else 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        if (reponse != null || revele) {
            q.sens?.let { Text("Sens : $it", color = c.texte2, fontSize = 14.sp) }
            Spacer(Modifier.height(8.dp))
            Bouton(if (index + 1 < total) "Suivant" else "Voir mon score", suivant, Modifier.fillMaxWidth())
        } else {
            BoutonSecondaire("Révéler la lecture", reveler, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ListeTextes(test: Boolean, ouvrir: (TexteLecture) -> Unit) {
    val c = Teinte
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .navigationBarsPadding()
    ) {
        Contenu.textesVisibles(test).forEach { t ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(c.carte)
                    .border(1.dp, c.trait, RoundedCornerShape(16.dp))
                    .clickable { ouvrir(t) }
                    .padding(16.dp)
            ) {
                Text(t.titre, color = c.texte, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text("${t.lignes.size} lignes", color = c.texte2, fontSize = 13.sp)
                if (!t.valide) BadgeAValider(Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun LireTexte(t: TexteLecture) {
    val c = Teinte
    val context = LocalContext.current
    val reglages = LocalReglages.current
    var latin by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .navigationBarsPadding()
    ) {
        if (!t.valide) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgeAValider()
                t.note?.let { Text("  $it", color = c.texte2, fontSize = 12.sp) }
            }
            Spacer(Modifier.height(8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Puce(if (latin) "Masquer le latin" else "Afficher le latin", latin, { latin = !latin })
            Puce("Partager en image", false, { Partage.partager(context, t.lignes.map { it.mots }, reglages) })
        }
        Spacer(Modifier.height(10.dp))
        CarteSable(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                t.lignes.forEach { l ->
                    AynehaParagraphes(listOf(l.mots), Modifier.fillMaxWidth(), tailleSp = 30f)
                    if (latin) Text(l.latin, color = Palette.TexteSombre2, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                }
            }
        }
    }
}


/** Normalisation pour la recherche : sans accents ni casse. */
private fun sansAccents(t: String): String =
    java.text.Normalizer.normalize(t, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "").lowercase()

/**
 * Le lexique : recherche (songhay ou français), filtre par thème, et pour chaque mot
 * l'écriture AYNEHA, la lecture latine et le sens. Bouton « S'entraîner » : quiz de lecture.
 */
@Composable
private fun ListeLexique(test: Boolean, entrainer: () -> Unit) {
    val c = Teinte
    val tous = remember(test) { Contenu.visibles(Contenu.LEXIQUE, test) }
    val themes = remember(tous) { tous.map { it.theme }.filter { it.isNotEmpty() }.distinct() }
    var recherche by remember { mutableStateOf("") }
    var theme by remember { mutableStateOf<String?>(null) }
    val q = sansAccents(recherche.trim())
    val liste = tous.filter { m ->
        (theme == null || m.theme == theme) &&
            (q.isEmpty() || sansAccents(m.latin + " " + m.francais).contains(q))
    }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(10.dp))
            Bouton("S'entraîner à lire ces mots", entrainer, Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = recherche,
                onValueChange = { recherche = it.take(40) },
                label = { Text("Chercher un mot (songhay ou français)") },
                singleLine = true,
                colors = couleursChamp(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Puce("Tout", theme == null, { theme = null })
                themes.forEach { t -> Puce(t, theme == t, { theme = if (theme == t) null else t }) }
            }
            Spacer(Modifier.height(4.dp))
            Text("${liste.size} mot(s)", color = c.texte2, fontSize = 12.sp)
        }
        items(liste) { m ->
            CarteSable(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth()) {
                    AynehaParagraphes(listOf(m.mots), Modifier.fillMaxWidth(), tailleSp = 28f)
                    Spacer(Modifier.height(4.dp))
                    Text(m.latin, color = Palette.Encre, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(m.francais, color = Palette.TexteSombre2, fontSize = 14.sp)
                    if (test && !m.valide) BadgeAValider()
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}
