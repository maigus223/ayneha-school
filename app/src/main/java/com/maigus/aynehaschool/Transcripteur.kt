// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import java.text.Normalizer

/**
 * Transcription latin -> AYNEHA.
 *
 * Portage fidèle des règles du Transcripteur AYNEHA web (index.html, version corrigée du 25 sept.),
 * avec en plus : « è » = ɛ (E ouvert) et « ô » = ɔ (O ouvert), règle de saisie d'oct. 2026.
 * Sortie sur la nouvelle plage U+10FF7C - U+10FFDD. Ce fichier ne dépend pas d'Android.
 *
 * Règles :
 * - voyelle + n simple (non suivi d'une voyelle, non doublé) = voyelle nasalisée ;
 *   exception : voyelle + n en fin d'un mot de plus de 3 lettres -> n consonne (muette) ;
 *   voyelle + ng -> voyelle nasalisée suivie de g ;
 * - lettre doublée = double (aa, bb, shsh...) ;
 * - consonne non suivie directement d'une voyelle = muette (si la forme muette existe) ;
 * - tch / c -> TƩ, sh / š -> Ʃ, ŋ -> Ŋ (ng -> N muette + G, règle du 13 oct. 2026), dj / j -> DJ / J, ny / ɲ (+ voyelle) -> Ɲ,
 *   gn + voyelle -> G muette + N (règle du 13 oct. 2026),
 *   ni + voyelle -> Ɲ (sauf nii), di + voyelle -> DJ (sauf dii), v -> W ;
 * - ɛ / è -> E ouvert, ɔ / ô -> O ouvert, ã ẽ ĩ õ ũ -> voyelles nasalisées, ɛ̃ -> ẽ, ɔ̃ -> õ ;
 * - « ? » en miroir, parenthèses et crochets inversés, virgule et point-virgule AYNEHA.
 */
object Transcripteur {

    enum class Mod { DOUBLE, NASALISE, OUVERT, OUVERT_DOUBLE, MUET }

    /** Une unité du mot latin (une lettre AYNEHA). */
    data class Jeton(val voyelle: Boolean, val base: String, var mod: Mod?, val brut: String)

    /** Explication d'un glyphe produit, pour le panneau « Pourquoi ? ». */
    data class Explication(val brut: String, val cp: Int, val raison: String)

    /** Un bloc = un « mot » entre deux espaces (avec sa ponctuation et ses chiffres collés). */
    data class Bloc(
        val latin: String,
        val points: List<Int>,              // ordre de lecture (naturel)
        val explications: List<Explication>,
    )

    private val CONSONNE_SIMPLE = mapOf(
        'b' to "B", 'p' to "P", 'm' to "M", 'f' to "F", 'w' to "W", 'v' to "W", 'd' to "D", 't' to "T",
        'n' to "N", 'l' to "L", 'r' to "R", 's' to "S", 'z' to "Z", 'c' to "TS", 'j' to "J", 'y' to "Y",
        'g' to "G", 'k' to "K", 'h' to "H", 'š' to "SH", 'ŋ' to "NG", 'ɲ' to "GN"
    )
    private val VOYELLES = setOf('a', 'e', 'i', 'o', 'u')
    private val OUVERTES = mapOf('ɛ' to "E", 'ɔ' to "O")
    private val NASALES_DIRECTES = mapOf('ã' to "A", 'ẽ' to "E", 'ĩ' to "I", 'õ' to "O", 'ũ' to "U")
    private val SPECIALES = setOf('š', 'ŋ', 'ɲ', 'ɛ', 'ɔ', 'ã', 'ẽ', 'ĩ', 'õ', 'ũ')

    private val MIROIR = mapOf(
        '?' to '؟', '(' to ')', ')' to '(', '[' to ']', ']' to '[', '{' to '}', '}' to '{'
    )

    /** Touches rapides de l'atelier. */
    val TOUCHES_RAPIDES = listOf("ŋ", "ɲ", "š", "ɛ", "ɔ", "ã", "ẽ", "ĩ", "õ", "ũ")

    fun estLettre(ch: Char): Boolean {
        val c = ch.lowercaseChar()
        return c in 'a'..'z' || c in SPECIALES
    }

    private fun estVoyelle(c: Char?): Boolean =
        c != null && (c in VOYELLES || c in OUVERTES || c in NASALES_DIRECTES)

    /** Préparation : NFC, ɛ̃ -> ẽ, ɔ̃ -> õ, è -> ɛ, ô -> ɔ. */
    fun normaliser(texte: String): String =
        Normalizer.normalize(texte, Normalizer.Form.NFC)
            .replace("ɛ̃", "ẽ")
            .replace("ɔ̃", "õ")
            .replace('è', 'ɛ').replace('È', 'Ɛ')
            .replace('ô', 'ɔ').replace('Ô', 'Ɔ')

    // ---------------------------------------------------------------------
    // Découpage d'un mot en jetons
    // ---------------------------------------------------------------------

    fun jetons(mot: String): List<Jeton> {
        val w = mot.lowercase()
        val n = w.length
        val out = ArrayList<Jeton>()
        fun at(k: Int): Char? = if (k < n) w[k] else null
        var i = 0

        // Consonne écrite sur [len] lettres, éventuellement doublée (ex. « shsh »).
        fun consonne(len: Int, nom: String): Int {
            val lu = w.substring(i, i + len)
            val suite = if (i + 2 * len <= n) w.substring(i + len, i + 2 * len) else ""
            return if (suite == lu) {
                out.add(Jeton(false, nom, Mod.DOUBLE, mot.substring(i, i + 2 * len))); 2 * len
            } else {
                out.add(Jeton(false, nom, null, mot.substring(i, i + len))); len
            }
        }

        while (i < n) {
            val c0 = w[i]
            val c1 = at(i + 1)
            val c2 = at(i + 2)

            // Voyelle nasalisée saisie directement
            val nasale = NASALES_DIRECTES[c0]
            if (nasale != null) {
                out.add(Jeton(true, nasale, Mod.NASALISE, mot.substring(i, i + 1)))
                i += 1
                continue
            }
            // Voyelle ouverte (ɛ, ɔ, et donc è, ô après normalisation)
            val ouverte = OUVERTES[c0]
            if (ouverte != null) {
                val b = ouverte
                if (c1 == c0) {
                    out.add(Jeton(true, b, Mod.OUVERT_DOUBLE, mot.substring(i, i + 2))); i += 2
                } else {
                    out.add(Jeton(true, b, Mod.OUVERT, mot.substring(i, i + 1))); i += 1
                }
                continue
            }
            // Digrammes et trigrammes de consonnes (du plus long au plus court)
            if (c0 == 't' && c1 == 'c' && c2 == 'h') { i += consonne(3, "TS"); continue }
            if (c0 == 's' && c1 == 'h') { i += consonne(2, "SH"); continue }
            // ng : N muette + G (règle de l'auteur, 13 oct. 2026). Ŋ ne s'obtient qu'avec ŋ.
            if (c0 == 'd' && c1 == 'j') { i += consonne(2, "DJ"); continue }
            // gn + voyelle : G muette + N (règle de l'auteur, 13 oct. 2026) - plus de Ɲ ici.
            if (c0 == 'n' && c1 == 'y' && estVoyelle(c2)) { i += consonne(2, "GN"); continue }
            if (c0 == 'n' && c1 == 'i' && estVoyelle(c2) && c2 != 'i') { i += consonne(2, "GN"); continue }
            if (c0 == 'd' && c1 == 'i' && estVoyelle(c2) && c2 != 'i') { i += consonne(2, "DJ"); continue }

            // Voyelle simple
            if (c0 in VOYELLES) {
                val b = c0.uppercase()
                if (c1 == c0) {
                    out.add(Jeton(true, b, Mod.DOUBLE, mot.substring(i, i + 2))); i += 2; continue
                }
                if (c1 == 'n') {
                    val nnDouble = c2 == 'n'
                    val suiviVoyelle = estVoyelle(c2)
                    val nFinal = i + 2 == n
                    val exception = nFinal && n > 3
                    if (!nnDouble && !suiviVoyelle && !exception) {
                        out.add(Jeton(true, b, Mod.NASALISE, mot.substring(i, i + 2))); i += 2; continue
                    }
                }
                out.add(Jeton(true, b, null, mot.substring(i, i + 1))); i += 1
                continue
            }

            // Consonne simple
            val nom = CONSONNE_SIMPLE[c0]
            if (nom != null) {
                if (c1 == c0) {
                    out.add(Jeton(false, nom, Mod.DOUBLE, mot.substring(i, i + 2))); i += 2
                } else {
                    out.add(Jeton(false, nom, null, mot.substring(i, i + 1))); i += 1
                }
                continue
            }
            i += 1 // caractère inconnu : ignoré
        }

        // Seconde passe : consonne non suivie directement d'une voyelle = muette.
        for (k in out.indices) {
            val t = out[k]
            if (!t.voyelle && t.mod == null) {
                val suivant = out.getOrNull(k + 1)
                if (suivant == null || !suivant.voyelle) t.mod = Mod.MUET
            }
        }
        return out
    }

    /** Glyphe d'un jeton (avec repli sur la forme initiale quand la variante n'existe pas). */
    fun point(t: Jeton): Int {
        val l = Ayneha.lettre(t.base)
        val v = when (t.mod) {
            null -> Variante.INITIALE
            Mod.DOUBLE -> Variante.DOUBLE
            Mod.NASALISE -> Variante.NASALISEE
            Mod.OUVERT, Mod.OUVERT_DOUBLE -> Variante.OUVERTE
            Mod.MUET -> Variante.MUETTE
        }
        return l.variantes[v] ?: l.base
    }

    private fun raison(t: Jeton, cp: Int): String {
        val nom = Ayneha.lettre(t.base).nom
        val existe = Ayneha.description(cp) != nom
        return when (t.mod) {
            null -> nom
            Mod.DOUBLE -> if (existe) "lettre doublée → $nom double" else "$nom n'a pas de forme double"
            Mod.NASALISE -> if (t.brut.length > 1) "voyelle + n → $nom nasalisée" else "$nom nasalisée"
            Mod.OUVERT -> "$nom ouverte"
            Mod.OUVERT_DOUBLE -> "$nom ouverte (pas de forme double)"
            Mod.MUET -> if (existe) "pas de voyelle après → $nom muette" else "$nom (pas de forme muette)"
        }
    }

    // ---------------------------------------------------------------------
    // Blocs, paragraphes
    // ---------------------------------------------------------------------

    private enum class Genre { LETTRE, CHIFFRE, AUTRE }

    fun bloc(latin: String): Bloc {
        val pts = ArrayList<Int>()
        val expl = ArrayList<Explication>()
        // Découpage en suites homogènes : lettres / chiffres / autres signes.
        var i = 0
        while (i < latin.length) {
            val genre = genre(latin[i])
            var j = i
            while (j < latin.length && genre(latin[j]) == genre) j++
            val morceau = latin.substring(i, j)
            when (genre) {
                Genre.LETTRE -> for (t in jetons(morceau)) {
                    val cp = point(t)
                    pts.add(cp)
                    expl.add(Explication(t.brut, cp, raison(t, cp)))
                }
                Genre.CHIFFRE -> for (ch in morceau) pts.add(Ayneha.chiffre(ch - '0'))
                Genre.AUTRE -> {
                    var k = 0
                    while (k < morceau.length) {
                        val cp = morceau.codePointAt(k)
                        k += Character.charCount(cp)
                        pts.add(
                            when (cp) {
                                ','.code -> Ayneha.VIRGULE
                                ';'.code -> Ayneha.POINT_VIRGULE
                                else -> (if (cp < 0x10000) MIROIR[cp.toChar()]?.code else null) ?: cp
                            }
                        )
                    }
                }
            }
            i = j
        }
        return Bloc(latin, pts, expl)
    }

    private fun genre(c: Char): Genre = when {
        estLettre(c) -> Genre.LETTRE
        c in '0'..'9' -> Genre.CHIFFRE
        else -> Genre.AUTRE
    }

    /** Texte complet : liste de paragraphes, chacun une liste de blocs (mots). */
    fun transcrire(texte: String): List<List<Bloc>> =
        normaliser(texte).split('\n').map { para ->
            para.split(Regex("\\s+")).filter { it.isNotEmpty() }.map { bloc(it) }
        }

    /** Raccourci : un seul mot -> ses points de code (ordre de lecture). */
    fun mot(latin: String): List<Int> =
        transcrire(latin).flatten().flatMap { it.points }

    /** Texte AYNEHA en ordre naturel (pour Copier), paragraphes séparés par des retours à la ligne. */
    fun texteNaturel(paragraphes: List<List<Bloc>>): String =
        paragraphes.joinToString("\n") { p -> p.joinToString(" ") { Ayneha.texte(it.points) } }
}
