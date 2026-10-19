// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS
// AYNEHA School - logiciel conçu et développé par Mahamadou Issiaka MAIGA (MAIGUS)
package com.maigus.aynehaschool

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import kotlin.math.min

/**
 * Taille commune des glyphes dans les cases (fraction de la hauteur de la case). Tous les glyphes
 * d'une grille sont ainsi dessinés à la même taille de police : leurs traits ont la même épaisseur.
 */
const val TAILLE_CASE = 0.62f

/** Flèche de retour dessinée (pas de caractère : certains téléphones l'affichent mal). */
@Composable
fun FlecheRetour(modifier: Modifier = Modifier, couleur: Color = Palette.Orange) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val e = w * 0.11f
        drawLine(couleur, Offset(w * 0.12f, h / 2f), Offset(w * 0.88f, h / 2f), strokeWidth = e, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(couleur, Offset(w * 0.12f, h / 2f), Offset(w * 0.45f, h * 0.17f), strokeWidth = e, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(couleur, Offset(w * 0.12f, h / 2f), Offset(w * 0.45f, h * 0.83f), strokeWidth = e, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

/** Roue des réglages dessinée. */
@Composable
fun IconeReglages(modifier: Modifier = Modifier, couleur: Color = Palette.TexteClair2) {
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f
        for (k in 0 until 8) {
            val a = Math.toRadians(k * 45.0)
            val dx = kotlin.math.cos(a).toFloat()
            val dy = kotlin.math.sin(a).toFloat()
            drawLine(couleur, Offset(c.x + dx * r * 0.45f, c.y + dy * r * 0.45f), Offset(c.x + dx * r * 0.95f, c.y + dy * r * 0.95f), strokeWidth = r * 0.28f)
        }
        drawCircle(couleur, radius = r * 0.62f, center = c)
        drawCircle(Palette.NuitCarte2, radius = r * 0.28f, center = c)
    }
}

/**
 * Couleurs des champs de saisie : un fond bien distinct de la page (blanc en clair, bleu plus
 * clair en sombre) et un cadre orange, pour qu'on voie tout de suite où taper.
 */
@Composable
fun couleursChamp() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = Teinte.texte, unfocusedTextColor = Teinte.texte,
    focusedContainerColor = if (Teinte.sombre) Palette.NuitCarte2 else Palette.Blanc,
    unfocusedContainerColor = if (Teinte.sombre) Palette.NuitCarte2 else Palette.Blanc,
    focusedBorderColor = Palette.Orange, unfocusedBorderColor = Palette.Orange.copy(alpha = 0.6f),
    focusedLabelColor = Palette.Orange, unfocusedLabelColor = Teinte.etiquette,
    cursorColor = Palette.Orange,
)

/** Police AYNEHA (ayneha_pro.ttf) pour Compose. Toujours en poids normal : jamais de gras. */
val PoliceAyneha = FontFamily(Font(R.font.ayneha_pro, FontWeight.Normal))

/** Police AYNEHA pour le dessin direct (Canvas), chargée une seule fois. */
@Composable
fun rememberAynehaTypeface(): Typeface {
    val context = LocalContext.current
    return remember {
        ResourcesCompat.getFont(context, R.font.ayneha_pro) ?: Typeface.DEFAULT
    }
}

/**
 * Dessine du texte AYNEHA centré sur son encre réelle (les accents du haut et du bas compris).
 *
 * [visuel] doit déjà être dans l'ordre de dessin (voir [Ayneha.visuel]) : le premier caractère
 * lu est tout à droite.
 * - [taille] null : le texte est agrandi pour remplir la boîte ([remplissage] = part de la boîte).
 * - [taille] fixée (fraction de la hauteur de la boîte) : les glyphes gardent leurs proportions
 *   réelles les uns par rapport aux autres.
 */
@Composable
fun AynehaTexte(
    visuel: String,
    modifier: Modifier = Modifier,
    couleur: Color = Palette.Encre,
    remplissage: Float = 0.78f,
    taille: Float? = null,
) {
    val tf = rememberAynehaTypeface()
    Canvas(modifier) {
        if (visuel.isEmpty() || size.width <= 0f || size.height <= 0f) return@Canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = tf
            color = couleur.toArgb()
            textSize = 200f
            isFakeBoldText = false
        }
        val r = Rect()
        if (taille == null) {
            paint.getTextBounds(visuel, 0, visuel.length, r)
            if (r.width() <= 0 || r.height() <= 0) return@Canvas
            val s = min(size.width * remplissage / r.width(), size.height * remplissage / r.height())
            paint.textSize = 200f * s
        } else {
            paint.textSize = size.height * taille
            paint.getTextBounds(visuel, 0, visuel.length, r)
            if (r.width() > size.width * 0.96f) paint.textSize *= size.width * 0.96f / r.width()
        }
        paint.getTextBounds(visuel, 0, visuel.length, r)
        val x = (size.width - r.width()) / 2f - r.left
        val y = (size.height - r.height()) / 2f - r.top
        drawContext.canvas.nativeCanvas.drawText(visuel, x, y, paint)
    }
}

/** Un mot AYNEHA donné dans l'ordre de lecture (points de code). */
@Composable
fun AynehaMot(
    points: List<Int>,
    modifier: Modifier = Modifier,
    couleur: Color = Palette.Encre,
    remplissage: Float = 0.78f,
    taille: Float? = null,
) = AynehaTexte(Ayneha.visuel(points), modifier, couleur, remplissage, taille)

/**
 * Texte AYNEHA courant, sur plusieurs lignes, en RTL pur : chaque mot est inversé, et les mots
 * sont posés de droite à gauche ; les lignes vont de haut en bas.
 * [paragraphes] : liste de paragraphes, chacun une liste de mots (points de code, ordre de lecture).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AynehaParagraphes(
    paragraphes: List<List<List<Int>>>,
    modifier: Modifier = Modifier,
    tailleSp: Float = 34f,
    couleur: Color = Palette.Encre,
) {
    val facteur = LocalReglages.current.tailleAyneha
    val t = tailleSp * facteur
    Column(modifier, verticalArrangement = Arrangement.spacedBy((t * 0.25f).dp)) {
        paragraphes.forEach { mots ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((t * 0.35f).dp),
                    verticalArrangement = Arrangement.spacedBy((t * 0.15f).dp),
                ) {
                    mots.forEach { m ->
                        Text(
                            Ayneha.visuel(m),
                            fontFamily = PoliceAyneha,
                            fontWeight = FontWeight.Normal,
                            fontSize = t.sp,
                            lineHeight = (t * 1.5f).sp,
                            color = couleur,
                            softWrap = false,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/** Carte couleur sable : le seul fond sur lequel on lit l'AYNEHA (contraste doux). */
@Composable
fun CarteSable(modifier: Modifier = Modifier, contenu: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.Sable)
            .border(1.dp, Palette.SableFonce, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) { contenu() }
}

/** Frise de losanges orange, inspirée des nattes et des tissages songhay. */
@Composable
fun Frise(modifier: Modifier = Modifier, hauteur: Dp = 10.dp) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(hauteur)
    ) {
        val h = size.height
        val pas = h * 1.6f
        drawRect(Palette.NuitBarre)
        var x = 0f
        var i = 0
        while (x < size.width + pas) {
            val losange = Path().apply {
                moveTo(x + pas / 2f, h * 0.08f)
                lineTo(x + pas * 0.95f, h / 2f)
                lineTo(x + pas / 2f, h * 0.92f)
                lineTo(x + pas * 0.05f, h / 2f)
                close()
            }
            drawPath(losange, if (i % 4 == 3) Palette.Sable else Palette.Orange)
            x += pas
            i++
        }
    }
}

/** En-tête des écrans : bouton retour, titre, frise. Toujours bleu nuit. */
@Composable
fun Entete(titre: String, onRetour: (() -> Unit)?, droite: (@Composable () -> Unit)? = null) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Palette.NuitBarre)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onRetour != null) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onRetour() },
                    contentAlignment = Alignment.Center
                ) {
                    FlecheRetour(Modifier.size(24.dp))
                }
                Spacer(Modifier.width(4.dp))
            } else {
                Spacer(Modifier.width(12.dp))
            }
            Text(
                titre,
                color = Palette.Sable,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            droite?.invoke()
        }
        Frise()
    }
}

/** Gros bouton plein. */
@Composable
fun Bouton(
    texte: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fond: Color = Palette.Orange,
    couleurTexte: Color = Palette.Blanc,
    actif: Boolean = true,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (actif) fond else Teinte.trait)
            .clickable(enabled = actif) { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texte,
            color = if (actif) couleurTexte else Teinte.texte2,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Bouton secondaire (fond de carte). */
@Composable
fun BoutonSecondaire(texte: String, onClick: () -> Unit, modifier: Modifier = Modifier, actif: Boolean = true) =
    Bouton(texte, onClick, modifier, fond = Teinte.carte2, couleurTexte = Teinte.texte, actif = actif)

/** Puce de choix (onglet, filtre). */
@Composable
fun Puce(texte: String, actif: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (actif) Palette.Orange else Teinte.carte)
            .border(1.dp, if (actif) Palette.Orange else Teinte.trait, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texte,
            color = if (actif) Palette.Blanc else Teinte.texte,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

/** Petit titre de section. */
@Composable
fun TitreSection(texte: String, modifier: Modifier = Modifier) {
    Text(
        texte.uppercase(),
        modifier = modifier.padding(top = 18.dp, bottom = 8.dp),
        color = Teinte.etiquette,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        fontWeight = FontWeight.SemiBold
    )
}

/** Paragraphe d'explication. */
@Composable
fun Paragraphe(texte: String, modifier: Modifier = Modifier) {
    Text(
        texte,
        modifier = modifier,
        color = Teinte.texte,
        fontSize = 15.sp,
        lineHeight = 22.sp
    )
}

/** Case contenant un glyphe sur fond sable, avec une étiquette latine en dessous. */
@Composable
fun CaseGlyphe(
    visuel: String,
    etiquette: String?,
    modifier: Modifier = Modifier,
    hauteurGlyphe: Dp = 58.dp,
    selectionne: Boolean = false,
    taille: Float = TAILLE_CASE,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Sable)
            .border(
                if (selectionne) 2.5.dp else 1.dp,
                if (selectionne) Palette.Orange else Palette.SableFonce,
                RoundedCornerShape(14.dp)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AynehaTexte(
            visuel,
            Modifier
                .fillMaxWidth()
                .height(hauteurGlyphe),
            taille = taille
        )
        if (etiquette != null) {
            Text(
                etiquette,
                color = Palette.TexteSombre2,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/** Badge « à valider » pour le contenu pas encore validé par l'auteur (version de test seulement). */
@Composable
fun BadgeAValider(modifier: Modifier = Modifier) {
    Text(
        "à valider",
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Palette.Terre)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        color = Palette.Blanc,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold
    )
}

/** Grille simple en lignes de [colonnes] cases (sans défilement propre). */
@Composable
fun <T> Grille(
    elements: List<T>,
    colonnes: Int,
    modifier: Modifier = Modifier,
    espace: Dp = 8.dp,
    rtl: Boolean = false,
    case: @Composable (T, Modifier) -> Unit,
) {
    val sens = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides sens) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(espace)) {
            elements.chunked(colonnes).forEach { ligne ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(espace)) {
                    ligne.forEach { e -> case(e, Modifier.weight(1f)) }
                    repeat(colonnes - ligne.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
