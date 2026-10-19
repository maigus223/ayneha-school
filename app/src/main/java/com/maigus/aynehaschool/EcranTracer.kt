// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/** Épaisseur des traits de la police ayneha_pro, mesurée pour une taille de 1000 (≈ 65 unités). */
private const val EPAISSEUR_TRAIT = 70f

/** Contour du glyphe, mis à l'échelle et centré dans une zone de [w] x [h] pixels. */
private fun cheminGlyphe(tf: Typeface, texte: String, w: Float, h: Float): Pair<android.graphics.Path, Float> {
    val paint = Paint().apply { typeface = tf; textSize = 1000f }
    val p = android.graphics.Path()
    paint.getTextPath(texte, 0, texte.length, 0f, 0f, p)
    val b = RectF()
    p.computeBounds(b, true)
    if (b.width() <= 0f || b.height() <= 0f) return p to 1f
    // Échelle commune à tous les glyphes (traits de même épaisseur), réduite seulement si le
    // glyphe ne tient pas dans la zone.
    val s = minOf(h * 0.62f / 580f, w * 0.85f / b.width(), h * 0.85f / b.height())
    val m = Matrix()
    m.setTranslate(-b.left, -b.top)
    m.postScale(s, s)
    m.postTranslate((w - b.width() * s) / 2f, (h - b.height() * s) / 2f)
    p.transform(m)
    return p to s
}

/**
 * Note le tracé (moteur de NÈNÈ École) : part de la lettre recouverte par le doigt, pénalisée par
 * ce qui déborde trop loin. Calcul fait sur de petites images (1/4 de la taille) pour rester rapide.
 */
private fun noter(glyphe: android.graphics.Path, traits: List<List<Offset>>, pinceau: Float, w: Int, h: Int): Float {
    val f = 0.25f
    val bw = (w * f).toInt().coerceAtLeast(1)
    val bh = (h * f).toInt().coerceAtLeast(1)
    val m = Matrix().apply { setScale(f, f) }
    val g = android.graphics.Path(glyphe).apply { transform(m) }

    fun image(dessin: (android.graphics.Canvas) -> Unit): IntArray {
        val bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
        dessin(android.graphics.Canvas(bmp))
        val px = IntArray(bw * bh)
        bmp.getPixels(px, 0, bw, 0, 0, bw, bh)
        bmp.recycle()
        return px
    }

    val plein = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.BLACK; style = Paint.Style.FILL }
    val marge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK; style = Paint.Style.FILL_AND_STROKE; strokeWidth = pinceau * f * 1.6f
    }
    val trait = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK; style = Paint.Style.STROKE; strokeWidth = pinceau * f
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    val lettre = image { it.drawPath(g, plein) }
    val zone = image { it.drawPath(g, marge) }
    val doigt = image { c ->
        traits.forEach { t ->
            if (t.size == 1) c.drawPoint(t[0].x * f, t[0].y * f, trait)
            for (i in 1 until t.size) c.drawLine(t[i - 1].x * f, t[i - 1].y * f, t[i].x * f, t[i].y * f, trait)
        }
    }
    var nLettre = 0; var couvert = 0; var nDoigt = 0; var dehors = 0
    for (i in lettre.indices) {
        val l = (lettre[i] ushr 24) > 100
        val d = (doigt[i] ushr 24) > 100
        val z = (zone[i] ushr 24) > 100
        if (l) { nLettre++; if (d) couvert++ }
        if (d) { nDoigt++; if (!z) dehors++ }
    }
    if (nLettre == 0 || nDoigt == 0) return 0f
    val couverture = couvert.toFloat() / nLettre
    val debord = dehors.toFloat() / nDoigt
    return (couverture * (1f - debord)).coerceIn(0f, 1f)
}

@Composable
fun EcranTracer(pointInitial: Int?, retour: () -> Unit) {
    val tf = rememberAynehaTypeface()
    val c = Teinte
    var point by rememberSaveable { mutableIntStateOf(pointInitial ?: Ayneha.lettre("A").base) }
    var traits by remember { mutableStateOf(listOf<List<Offset>>()) }
    var note by remember { mutableStateOf<Float?>(null) }
    var taille by remember { mutableStateOf(IntSize.Zero) }

    val g = Ayneha.glyphe(point)
    val lettre = (g as? Glyphe.DeLettre)?.lettre
    val etiquette = when (g) {
        is Glyphe.Chiffre -> "le chiffre ${g.valeur}"
        is Glyphe.DeLettre -> if (g.variante == Variante.INITIALE) "la lettre ${g.lettre.nom}" else "${g.lettre.nom} ${g.variante.nom}"
        else -> "ce signe"
    }
    val texte = Ayneha.str(point)

    fun effacer() { traits = emptyList(); note = null }

    Column(Modifier.fillMaxSize()) {
        Entete("Tracer", retour)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding()
        ) {
            // Choix du modèle : les 27 lettres puis les 10 chiffres
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Ayneha.LETTRES.forEach { l ->
                    Puce(l.nom, lettre == l, { point = l.base; effacer() })
                }
                (0..9).forEach { d ->
                    Puce(d.toString(), g is Glyphe.Chiffre && g.valeur == d, { point = Ayneha.chiffre(d); effacer() })
                }
            }
            // Ses formes accentuées
            if (lettre != null && lettre.variantes.size > 1) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    lettre.variantesOrdonnees.forEach { (v, cp) ->
                        Puce(v.nom, cp == point, { point = cp; effacer() })
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Suis $etiquette avec ton doigt, sur la forme claire.",
                color = c.texte,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            // Zone de tracé
            val chemin = remember(texte, taille) {
                if (taille.width > 0) cheminGlyphe(tf, texte, taille.width.toFloat(), taille.height.toFloat()) else null
            }
            val pinceau = (chemin?.second ?: 0.3f) * EPAISSEUR_TRAIT

            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Palette.Sable)
                    .border(2.dp, Palette.Orange, RoundedCornerShape(22.dp))
                    .onSizeChanged { taille = it }
                    .pointerInput(texte) {
                        detectDragGestures(
                            onDragStart = { o -> traits = traits + listOf(listOf(o)); note = null },
                            onDrag = { change, _ ->
                                change.consume()
                                val dernier = traits.lastOrNull() ?: emptyList()
                                traits = traits.dropLast(1) + listOf(dernier + change.position)
                            }
                        )
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    chemin?.first?.let { p ->
                        val cp = p.asComposePath()
                        drawPath(cp, Palette.SableFonce)
                        drawPath(cp, Palette.OrangeDoux.copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))
                    }
                    traits.forEach { t ->
                        if (t.size == 1) drawCircle(Palette.Encre, radius = pinceau / 2f, center = t[0])
                        for (i in 1 until t.size) {
                            drawLine(Palette.Encre, t[i - 1], t[i], strokeWidth = pinceau, cap = StrokeCap.Round)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BoutonSecondaire("Effacer", { effacer() }, Modifier.weight(1f))
                Bouton(
                    "Vérifier",
                    {
                        val ch = chemin
                        if (ch != null) note = noter(ch.first, traits, pinceau, taille.width, taille.height)
                    },
                    Modifier.weight(1f),
                    actif = traits.isNotEmpty()
                )
            }

            note?.let { n ->
                val etoiles = when {
                    n >= 0.70f -> 3
                    n >= 0.50f -> 2
                    n >= 0.30f -> 1
                    else -> 0
                }
                val message = when (etoiles) {
                    3 -> "Excellent !"
                    2 -> "Très bien, encore un petit effort."
                    1 -> "Pas mal. Suis bien toute la forme, accents compris."
                    else -> "Essaie encore en suivant la forme claire."
                }
                Spacer(Modifier.height(12.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (etoiles >= 2) Palette.Vert else c.carte2)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("★".repeat(etoiles) + "☆".repeat(3 - etoiles), color = Palette.OrangeDoux, fontSize = 30.sp)
                    Text(message, color = if (etoiles >= 2) Palette.Blanc else c.texte, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.width(1.dp))
        }
    }
}
