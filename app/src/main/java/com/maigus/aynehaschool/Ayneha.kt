// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
// Écriture AYNEHA conçue et créée par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

/**
 * Données de l'écriture AYNEHA, sur la NOUVELLE plage U+10FF7C - U+10FFDD (plan 16, PUA-B).
 *
 * ATTENTION : ces points de code sont hors du plan multilingue de base. En Kotlin, un caractère
 * AYNEHA occupe DEUX unités UTF-16 (paire de substitution). On ne manipule donc jamais le texte
 * AYNEHA caractère par caractère (Char) : toujours par listes de points de code (Int).
 *
 * Ce fichier ne dépend pas d'Android : il est testé à part (voir test/).
 */

/** Les variantes d'une lettre, dans l'ordre officiel. */
enum class Variante(val nom: String) {
    INITIALE("initiale"),
    NASALISEE("nasalisée"),
    OUVERTE("ouverte"),
    DOUBLE("double"),
    MUETTE("muette"),
}

/** Les 4 accents de l'AYNEHA. */
enum class Accent(val nom: String, val variante: Variante) {
    NASALISE("nasalisé", Variante.NASALISEE),
    OUVERT("ouvert", Variante.OUVERTE),
    DOUBLE("double", Variante.DOUBLE),
    MUET("muet", Variante.MUETTE),
}

/** Une lettre de l'alphabet AYNEHA. */
data class Lettre(
    val cle: String,          // clé interne : "A", "B", "SH", "TS", "GN", "NG"...
    val nom: String,          // nom affiché : "A", "B", "Ʃ", "TƩ", "Ɲ", "Ŋ"...
    val epellation: String,   // "a", "ba", "Ʃa"...
    val voyelle: Boolean,
    val rang: Int,            // position dans l'ordre lexicographique AYNEHA (0 = A)
    val variantes: Map<Variante, Int>,
) {
    val base: Int get() = variantes.getValue(Variante.INITIALE)

    /** Les variantes présentes, dans l'ordre officiel. */
    val variantesOrdonnees: List<Pair<Variante, Int>>
        get() = Variante.values().mapNotNull { v -> variantes[v]?.let { v to it } }
}

/** Ce que représente un glyphe AYNEHA. */
sealed class Glyphe {
    data class DeLettre(val lettre: Lettre, val variante: Variante) : Glyphe()
    data class Chiffre(val valeur: Int) : Glyphe()
    data class AccentSeul(val accent: Accent, val surVoyelle: String?) : Glyphe()
    data class Ponctuation(val nom: String) : Glyphe()
}

object Ayneha {

    const val DEBUT = 0x10FF7C
    const val FIN = 0x10FFDD

    /** Décalage entre l'ancienne plage (U+E000-U+E061) et la nouvelle : U+E000 + 0x101F7C = U+10FF7C. */
    const val DECALAGE_ANCIENNE_PLAGE = 0x101F7C

    private const val CHIFFRE_0 = 0x10FF7C
    private const val CONSONNE_0 = 0x10FF86
    private const val VOYELLE_0 = 0x10FF9C
    private const val COMBINAISON_0 = 0x10FFA1

    /** Accents isolés (tels qu'ils sont dessinés seuls, sans lettre). */
    val ACCENT_NASALISE: Map<String, Int> = mapOf(
        "A" to 0x10FFD3, "E" to 0x10FFD4, "I" to 0x10FFD5, "O" to 0x10FFD6, "U" to 0x10FFD7
    )
    val ACCENT_OUVERT: Map<String, Int> = mapOf("E" to 0x10FFD8, "O" to 0x10FFD9)
    const val ACCENT_DOUBLE = 0x10FFDA
    const val ACCENT_MUET = 0x10FFDB
    const val VIRGULE = 0x10FFDC
    const val POINT_VIRGULE = 0x10FFDD

    private val VOYELLES_CLES = listOf("A", "E", "I", "O", "U")

    /** Les 22 consonnes, dans l'ordre de la police (qui est aussi leur ordre lexicographique). */
    private val CONSONNES_CLES = listOf(
        "B", "P", "M", "F", "W", "D", "T", "N", "L", "R", "S", "Z",
        "SH", "TS", "J", "DJ", "GN", "Y", "G", "K", "H", "NG"
    )
    private val NOMS = mapOf("SH" to "Ʃ", "TS" to "TƩ", "GN" to "Ɲ", "NG" to "Ŋ")

    /** Consonnes sans forme muette (elles ne sont jamais en fin de mot). */
    private val SANS_MUETTE = setOf("SH", "TS", "J", "GN", "H")

    /** Consonnes sans aucune combinaison. */
    private val SANS_COMBINAISON = setOf("H")

    /** Les 27 lettres dans l'ordre lexicographique AYNEHA : les voyelles, puis les consonnes. */
    val LETTRES: List<Lettre>
    val VOYELLES: List<Lettre>
    val CONSONNES: List<Lettre>

    private val parCle: Map<String, Lettre>
    private val parPoint: Map<Int, Glyphe>

    init {
        // Construction de la table dans l'ordre exact de la matrice officielle (98 glyphes).
        val variantes = LinkedHashMap<String, MutableMap<Variante, Int>>()
        VOYELLES_CLES.forEachIndexed { i, k -> variantes[k] = mutableMapOf(Variante.INITIALE to VOYELLE_0 + i) }
        CONSONNES_CLES.forEachIndexed { i, k -> variantes[k] = mutableMapOf(Variante.INITIALE to CONSONNE_0 + i) }

        var p = COMBINAISON_0
        for (v in VOYELLES_CLES) {
            variantes.getValue(v)[Variante.DOUBLE] = p++
            variantes.getValue(v)[Variante.NASALISEE] = p++
            if (v == "E" || v == "O") variantes.getValue(v)[Variante.OUVERTE] = p++
        }
        for (c in CONSONNES_CLES) {
            if (c in SANS_COMBINAISON) continue
            variantes.getValue(c)[Variante.DOUBLE] = p++
            if (c !in SANS_MUETTE) variantes.getValue(c)[Variante.MUETTE] = p++
        }
        check(p == 0x10FFD3) { "Table AYNEHA incohérente : ${p.toString(16)}" }

        val cles = VOYELLES_CLES + CONSONNES_CLES
        LETTRES = cles.mapIndexed { rang, k ->
            val voy = k in VOYELLES_CLES
            val nom = NOMS[k] ?: k
            Lettre(
                cle = k,
                nom = nom,
                epellation = if (voy) nom.lowercase() else nom.lowercase() + "a",
                voyelle = voy,
                rang = rang,
                variantes = variantes.getValue(k).toMap(),
            )
        }
        VOYELLES = LETTRES.filter { it.voyelle }
        CONSONNES = LETTRES.filter { !it.voyelle }
        parCle = LETTRES.associateBy { it.cle }

        val m = HashMap<Int, Glyphe>()
        for (l in LETTRES) for ((v, cp) in l.variantes) m[cp] = Glyphe.DeLettre(l, v)
        for (d in 0..9) m[CHIFFRE_0 + d] = Glyphe.Chiffre(d)
        for ((v, cp) in ACCENT_NASALISE) m[cp] = Glyphe.AccentSeul(Accent.NASALISE, v)
        for ((v, cp) in ACCENT_OUVERT) m[cp] = Glyphe.AccentSeul(Accent.OUVERT, v)
        m[ACCENT_DOUBLE] = Glyphe.AccentSeul(Accent.DOUBLE, null)
        m[ACCENT_MUET] = Glyphe.AccentSeul(Accent.MUET, null)
        m[VIRGULE] = Glyphe.Ponctuation("virgule")
        m[POINT_VIRGULE] = Glyphe.Ponctuation("point-virgule")
        parPoint = m
        check(parPoint.size == 98) { "La matrice doit compter 98 glyphes, trouvé ${parPoint.size}" }
    }

    fun lettre(cle: String): Lettre = parCle.getValue(cle)

    fun glyphe(cp: Int): Glyphe? = parPoint[cp]

    fun estAyneha(cp: Int): Boolean = cp in DEBUT..FIN

    fun chiffre(d: Int): Int {
        require(d in 0..9)
        return CHIFFRE_0 + d
    }

    /** Toutes les combinaisons (lettre + accent) : 50 glyphes. */
    val COMBINAISONS: List<Pair<Lettre, Variante>>
        get() = LETTRES.flatMap { l ->
            l.variantesOrdonnees.filter { it.first != Variante.INITIALE }.map { l to it.first }
        }

    // ---------------------------------------------------------------------
    // Texte : points de code <-> chaînes
    // ---------------------------------------------------------------------

    /** Un point de code -> chaîne (2 unités UTF-16 pour l'AYNEHA). */
    fun str(cp: Int): String = String(Character.toChars(cp))

    /** Points de code d'une chaîne, sans jamais couper une paire de substitution. */
    fun points(s: String): List<Int> {
        val out = ArrayList<Int>()
        var i = 0
        while (i < s.length) {
            val cp = s.codePointAt(i)
            out.add(cp)
            i += Character.charCount(cp)
        }
        return out
    }

    /** Chaîne dans l'ordre naturel (ordre de lecture), pour copier ou enregistrer. */
    fun texte(cps: List<Int>): String = buildString { cps.forEach { appendCodePoint(it) } }

    /**
     * Chaîne à DESSINER de gauche à droite : l'ordre est inversé, pour que le premier caractère lu
     * se retrouve tout à droite (RTL pur). Android traite les PUA comme du texte « gauche à droite »,
     * d'où cette inversion manuelle, mot par mot.
     */
    fun visuel(cps: List<Int>): String = texte(cps.asReversed())

    fun visuel(cp: Int): String = str(cp)

    // ---------------------------------------------------------------------
    // Nombres
    // ---------------------------------------------------------------------

    /** Chiffres AYNEHA d'un nombre écrit en chiffres latins, dans l'ordre de lecture. */
    fun chiffres(nombre: String): List<Int> = nombre.filter { it in '0'..'9' }.map { chiffre(it - '0') }

    /** Nombre prêt à dessiner : le premier chiffre lu (le plus grand rang) est tout à droite. */
    fun nombreVisuel(nombre: String): String = visuel(chiffres(nombre))

    fun nombreVisuel(n: Int): String = nombreVisuel(n.toString())

    // ---------------------------------------------------------------------
    // Syllabes et noms
    // ---------------------------------------------------------------------

    /** Syllabe consonne + voyelle (ordre de lecture). */
    fun syllabe(consonne: Lettre, voyelle: Int): List<Int> = listOf(consonne.base, voyelle)

    /** Nom latin d'un glyphe, pour les explications (« B double », « E ouverte », « chiffre 7 »...). */
    fun description(cp: Int): String = when (val g = glyphe(cp)) {
        is Glyphe.DeLettre ->
            if (g.variante == Variante.INITIALE) g.lettre.nom
            else g.lettre.nom + " " + accorde(g.variante, g.lettre.voyelle)
        is Glyphe.Chiffre -> "chiffre ${g.valeur}"
        is Glyphe.AccentSeul -> "accent ${g.accent.nom}"
        is Glyphe.Ponctuation -> g.nom
        null -> str(cp)
    }

    private fun accorde(v: Variante, voyelle: Boolean): String = when (v) {
        Variante.INITIALE -> ""
        Variante.NASALISEE -> "nasalisée"
        Variante.OUVERTE -> "ouverte"
        Variante.DOUBLE -> "double"
        Variante.MUETTE -> "muette"
    }

    /** Lecture latine simple d'un glyphe (pour les exercices) : a, ã, aa, ɛ, bb, b... */
    fun latin(cp: Int): String = when (val g = glyphe(cp)) {
        is Glyphe.DeLettre -> {
            val b = when (g.lettre.cle) {
                "SH" -> "š"; "TS" -> "c"; "GN" -> "ɲ"; "NG" -> "ŋ"; "DJ" -> "dj"
                else -> g.lettre.cle.lowercase()
            }
            when (g.variante) {
                Variante.INITIALE, Variante.MUETTE -> b
                Variante.DOUBLE -> b + b
                Variante.NASALISEE -> when (b) {
                    "a" -> "ã"; "e" -> "ẽ"; "i" -> "ĩ"; "o" -> "õ"; else -> "ũ"
                }
                Variante.OUVERTE -> if (b == "e") "ɛ" else "ɔ"
            }
        }
        is Glyphe.Chiffre -> g.valeur.toString()
        else -> ""
    }

    // ---------------------------------------------------------------------
    // Ordre lexicographique AYNEHA
    // ---------------------------------------------------------------------

    /**
     * Clé de tri d'un glyphe : d'abord la lettre (voyelles puis consonnes), puis la variante dans
     * l'ordre officiel (initiale, nasalisée, ouverte, double, muette). Les chiffres viennent avant
     * les lettres, les autres signes après.
     */
    fun cleTri(cp: Int): Int = when (val g = glyphe(cp)) {
        is Glyphe.Chiffre -> g.valeur
        is Glyphe.DeLettre -> 100 + g.lettre.rang * 10 + g.variante.ordinal
        else -> 10_000 + cp - DEBUT
    }

    /** Compare deux mots AYNEHA (listes de points de code, ordre de lecture). */
    val ORDRE: Comparator<List<Int>> = Comparator { a, b ->
        val n = minOf(a.size, b.size)
        for (i in 0 until n) {
            val c = cleTri(a[i]).compareTo(cleTri(b[i]))
            if (c != 0) return@Comparator c
        }
        a.size.compareTo(b.size)
    }
}
