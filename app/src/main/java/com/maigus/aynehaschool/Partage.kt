// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Partage d'un texte AYNEHA en image (comme le clavier AYNEHA, adapté à l'appli) :
 * - fond blanc, texte AYNEHA en noir, de droite à gauche ;
 * - en haut : « AYNEHA School » en orange chaud #FF6600 (pour distinguer ces images de celles
 *   du clavier et du Carnet) ;
 * - en bas : le pseudo (facultatif) et l'horodatage (activable), indépendants l'un de l'autre.
 * L'image est écrite dans le cache de l'appli et partagée par FileProvider : aucune permission.
 */
object Partage {

    private const val LARGEUR = 1080
    private const val MARGE = 72f
    private const val ORANGE_PARTAGE = 0xFFFF6600.toInt()

    /** [paragraphes] : paragraphes -> mots -> points de code (ordre de lecture). */
    fun partager(context: Context, paragraphes: List<List<List<Int>>>, reglages: Reglages) {
        if (paragraphes.all { it.isEmpty() }) {
            Toast.makeText(context, "Rien à partager pour l'instant.", Toast.LENGTH_SHORT).show()
            return
        }
        val image = dessiner(context, paragraphes, reglages.pseudo.trim(), reglages.horodatage)
        val dossier = File(context.cacheDir, "partage").apply { mkdirs() }
        dossier.listFiles()?.forEach { it.delete() } // on ne garde que la dernière image
        val nom = "ayneha_school_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date()) + ".png"
        val fichier = File(dossier, nom)
        FileOutputStream(fichier).use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()

        val uri = FileProvider.getUriForFile(context, context.packageName + ".partage", fichier)
        val envoi = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(envoi, "Partager l'image AYNEHA").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun dessiner(context: Context, paragraphes: List<List<List<Int>>>, pseudo: String, horodatage: Boolean): Bitmap {
        val police = ResourcesCompat.getFont(context, R.font.ayneha_pro) ?: Typeface.DEFAULT
        val ay = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = police; color = Color.BLACK; textSize = 78f; isFakeBoldText = false
        }
        val titre = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ORANGE_PARTAGE; textSize = 54f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val pied = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF6B6B6B.toInt(); textSize = 32f }

        // Mise en lignes, de droite à gauche : chaque ligne = liste de mots (déjà inversés pour le dessin).
        val largeurUtile = LARGEUR - 2 * MARGE
        val espace = ay.measureText(" ") * 1.4f
        val lignes = ArrayList<List<String>>()
        for (p in paragraphes) {
            var courante = ArrayList<String>()
            var largeur = 0f
            for (m in p) {
                val v = Ayneha.visuel(m)
                val w = ay.measureText(v)
                if (courante.isNotEmpty() && largeur + espace + w > largeurUtile) {
                    lignes.add(courante); courante = ArrayList(); largeur = 0f
                }
                largeur += (if (courante.isEmpty()) 0f else espace) + w
                courante.add(v)
            }
            lignes.add(courante)
        }

        val fm = ay.fontMetrics
        val hauteurLigne = (fm.descent - fm.ascent) * 1.35f
        val hautTitre = 150f
        val textesPied = listOfNotNull(
            pseudo.takeIf { it.isNotEmpty() },
            if (horodatage) SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date()) else null
        )
        val hautPied = if (textesPied.isEmpty()) 60f else 130f
        val hauteur = (hautTitre + lignes.size * hauteurLigne + hautPied + 40f).toInt()

        val bmp = Bitmap.createBitmap(LARGEUR, hauteur, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)

        // En-tête
        val t = "AYNEHA School"
        c.drawText(t, (LARGEUR - titre.measureText(t)) / 2f, 96f, titre)
        val trait = Paint().apply { color = ORANGE_PARTAGE; strokeWidth = 4f }
        c.drawLine(LARGEUR * 0.3f, 122f, LARGEUR * 0.7f, 122f, trait)

        // Texte AYNEHA, aligné à droite
        var y = hautTitre + 20f - fm.ascent
        for (l in lignes) {
            var x = LARGEUR - MARGE
            for (v in l) {
                val w = ay.measureText(v)
                x -= w
                c.drawText(v, x, y, ay)
                x -= espace
            }
            y += hauteurLigne
        }

        // Pied de page : pseudo à droite, horodatage à gauche
        if (textesPied.isNotEmpty()) {
            val yp = hauteur - 50f
            c.drawLine(MARGE, yp - 50f, LARGEUR - MARGE, yp - 50f, Paint().apply { color = 0xFFDDDDDD.toInt(); strokeWidth = 2f })
            if (pseudo.isNotEmpty()) c.drawText(pseudo, LARGEUR - MARGE - pied.measureText(pseudo), yp, pied)
            if (horodatage) c.drawText(textesPied.last(), MARGE, yp, pied)
        }
        return bmp
    }
}
