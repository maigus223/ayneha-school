// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

/**
 * Les nombres en toutes lettres, en songhay (lettres latines), de 0 à 999 999.
 *
 * Règles tirées du document de l'auteur « Chiffres (Songhay) en toutes lettres » (17 oct. 2026) :
 * - unités : yaada, affo, ihĩka, ihĩza, itaaci, iggu, iddu, iyye, yaaha, yagga ;
 * - après « cĩdi » et après zãgu / jẽber, forme courte : fo, hĩka, hĩza, taaci, gu, iddu, iyye, yaaha, yagga ;
 * - dizaines : iwoy, warãka, warãza, woytaaci, woygu, woydu, woyye, woyyaaha, woyyagga (10 000 = jẽber woy) ;
 * - 11 à 99 : dizaine + « cĩdi » + unité courte (warãka cĩdi fo) ;
 * - centaines : zãgu, zãgu hĩka... ; milliers : jẽber fo, jẽber hĩka... ;
 * - les parties sont reliées par « nda » : jẽber fo nda zãgu nda iwoy (1 110).
 * Ce fichier ne dépend pas d'Android.
 */
object NombresSonghay {
    private val UNITES = listOf("yaada", "affo", "ihĩka", "ihĩza", "itaaci", "iggu", "iddu", "iyye", "yaaha", "yagga")
    private val COURTES = listOf("", "fo", "hĩka", "hĩza", "taaci", "gu", "iddu", "iyye", "yaaha", "yagga")
    private val DIZAINES = listOf("", "iwoy", "warãka", "warãza", "woytaaci", "woygu", "woydu", "woyye", "woyyaaha", "woyyagga")

    const val MAX = 999_999L

    /** 1 à 99, forme libre (après « nda » ou seul). */
    private fun deux(r: Int): String {
        if (r < 10) return UNITES[r]
        val t = r / 10; val u = r % 10
        return DIZAINES[t] + if (u == 0) "" else " cĩdi " + COURTES[u]
    }

    /** 1 à 99, multiplicateur de jẽber (jẽber fo, jẽber woy, jẽber warãka cĩdi fo...). */
    private fun multiplicateur(r: Int): String {
        if (r < 10) return COURTES[r]
        val t = r / 10; val u = r % 10
        return (if (t == 1) "woy" else DIZAINES[t]) + if (u == 0) "" else " cĩdi " + COURTES[u]
    }

    private fun centaine(h: Int): String = if (h == 1) "zãgu" else "zãgu " + COURTES[h]

    /** Le nombre en lettres, ou null au-delà de 999 999. */
    fun enLettres(n: Long): String? {
        if (n < 0 || n > MAX) return null
        if (n == 0L) return UNITES[0]
        val parties = ArrayList<String>()
        val milliers = (n / 1000).toInt()
        val reste = (n % 1000).toInt()
        if (milliers > 0) {
            val h = milliers / 100; val r = milliers % 100
            if (h > 0) {
                parties.add("jẽber " + centaine(h))
                if (r > 0) parties.add("jẽber " + multiplicateur(r))
            } else {
                parties.add("jẽber " + multiplicateur(r))
            }
        }
        val h = reste / 100; val r = reste % 100
        if (h > 0) parties.add(centaine(h))
        if (r > 0) parties.add(deux(r))
        return parties.joinToString(" nda ")
    }

    fun enLettres(nombre: String): String? = nombre.toLongOrNull()?.let { enLettres(it) }
}
