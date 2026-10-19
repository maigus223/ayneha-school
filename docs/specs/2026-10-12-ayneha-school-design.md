# AYNEHA School - spécification (v1.0)

Application Android d'apprentissage de l'écriture AYNEHA (songhay), sur le modèle de NÈNÈ École.
Écriture AYNEHA et logiciel : Mahamadou Issiaka MAIGA (MAIGUS). Validée le 12 oct. 2026.

## 1. Cadre technique (repris de NÈNÈ École)

- Kotlin + Jetpack Compose, un seul `MainActivity`, navigation par pile d'écrans (`sealed class Ecran`), bouton retour géré.
- AGP 8.1.4, Kotlin 1.9.22, Compose BOM 2024.02.00, Gradle 8.4, compileSdk/targetSdk 34, minSdk 26, Java 17.
- Paquet `com.maigus.aynehaschool`, version 1.2.1 (versionCode 2).
- Hors connexion, aucune permission. Le partage d'image passe par un `FileProvider` (pas de permission).
- Police : `res/font/ayneha_pro.ttf`. Le texte AYNEHA utilise **uniquement la nouvelle plage U+10FF7C-U+10FFDD** (plan 16).
  - Un caractère = 2 unités UTF-16 (paire de substitution) : toute manipulation passe par des listes de points de code (`Int`), jamais par des `Char`.
  - Décalage ancienne → nouvelle plage : **+0x101F7C** (U+E000 → U+10FF7C).
- RTL pur : les points de code PUA sont « forts LTR » pour Android ; chaque mot est donc inversé avant dessin, et les mots sont posés de droite à gauche.
- **Jamais de gras** ni de soulignement sur l'AYNEHA ; l'emphase se fait par la taille.

## 2. Données AYNEHA

- Matrice (même ordre que la police) : chiffres 0-9, 22 consonnes, 5 voyelles, 50 combinaisons, 4 accents isolés (nasalisé ×5, ouvert ×2, double, muet), virgule et point-virgule RTL.
- Ordre lexicographique AYNEHA : **A E I O U**, puis **B P M F W D T N L R S Z Ʃ TƩ J DJ Ɲ Y G K H Ŋ** (27 lettres).
- Variantes de chaque lettre, dans l'ordre officiel : initiale, nasalisée, ouverte, double, muette (seules celles qui existent).
  - Voyelles : A, I, U = nasalisée + double ; E, O = nasalisée + ouverte + double.
  - Consonnes : double + muette, sauf Ʃ, TƩ, J, Ɲ (double seulement) et H (aucune).
- Épellation : voyelles telles quelles, consonnes + « a » (ba, pa, ma...).
- Règles affichées : un seul accent par caractère ; une consonne double est toujours suivie d'une voyelle ; consonne muette = consonne non suivie directement d'une voyelle ; Ʃ, TƩ, J, Ɲ, H jamais en fin de mot ; pas de majuscules ; pas de gras ni de soulignement ; point d'interrogation en miroir ; ordinal = I en haut après le nombre ; virgule décimale en bas.

## 3. Écrans

1. **Accueil** - tient sur un écran : glyphe H en logo, Tombeau des Askia en filigrane, « AYNEHA School », le mot AYNEHA écrit en AYNEHA, frise, 8 cartes + « À propos », bouton Réglages, « By MAIGUS ».
2. **L'alphabet** - 27 lettres dans l'ordre AYNEHA, en 2 groupes (voyelles, consonnes) ; toucher = fiche.
3. **Fiche lettre** - glyphe en grand, nom, épellation, variantes, exemples (mots validés seulement), Tracer, précédente/suivante.
4. **Les accents** - 4 accents isolés expliqués, tableau des 50 combinaisons par accent, règles.
5. **Le syllabaire** - chaque consonne × 5 voyelles (consonne à droite) ; choix de la forme de voyelle (simple, nasalisée, ouverte pour E et O, double).
6. **Les chiffres** - 10 chiffres, convertisseur (jusqu'à 9999 et au-delà), tableau de 0 à 9999 par centaines (page de 100), ordinal et virgule décimale.
7. **Tracer** - lettre, variante ou chiffre suivi au doigt, 0 à 3 étoiles (moteur NÈNÈ École).
8. **Quiz** - 6 jeux de 10 questions, meilleur score gardé : reconnaître, retrouver, quel accent, lire la syllabe, chiffres, remettre dans l'ordre AYNEHA.
9. **Atelier de transcription**
   - *Transcrire* : saisie latine → AYNEHA en direct (règles du transcripteur web : voyelle + n, lettre doublée, muette, tch/sh/dj/ny, ni/di + voyelle, v→W, ɛ̃→ẽ, ɔ̃→õ ; **è = ɛ et ô = ɔ**) ; touches rapides ŋ ɲ š ɛ ɔ ã ẽ ĩ õ ũ ; panneau « Pourquoi ? » ; Copier ; Partager en image.
   - *S'exercer* : un mot latin validé est proposé, l'élève compose la réponse avec un clavier de glyphes, l'appli corrige.
10. **Lecture** - niveaux : syllabes (générées), mots, noms (régions, patronymes), petites phrases et textes. Mot AYNEHA → 4 lectures latines au choix, ou « Révéler ». Les mots non validés ne s'affichent que dans la version de test (debug), avec un badge « à valider ».
11. **Réglages** - thème (sombre par défaut, comme le mockup ; automatique / clair au choix), taille du texte AYNEHA, pseudo (20 caractères max) et horodatage pour les images partagées.
12. **À propos** - l'écriture et son auteur, licences (code GPL v3 ou ultérieure, police SIL OFL 1.1, images et textes CC BY-SA 4.0), confidentialité, contact gwokmt2q@duck.com. Aucune mention d'association.

## 4. Thème « Nuit du fleuve »

- Bleu nuit #0E1A30 (fond sombre), #182B4D (cartes), orange #F07C22 (actions), sable #F5EDDF (lecture AYNEHA), encre #18213A (glyphes), vert palmier doum #3E9A72 (juste), terre #C9472F (à revoir).
- Thème clair : fond sable #F6F0E4, cartes #FFFBF4, barres en bleu nuit.
- L'AYNEHA est toujours dessiné en encre sur sable, quel que soit le thème (contraste doux).
- Frise de losanges orange (nattes et tissages), silhouette du Tombeau des Askia sur l'accueil.

## 5. Partage en image (comme le clavier AYNEHA, adapté)

- Fond blanc, texte AYNEHA en noir, RTL.
- En haut : **« AYNEHA School »** en orange #FF6600 (pour distinguer des images du clavier et du Carnet).
- En bas : pseudo (facultatif) et horodatage (activable), indépendants.

## 6. Contenu (validé par l'auteur le 13 oct. 2026)

- Mots : 32 mots du lexique (dont jiibi et hujja) (Carnet, Convertisseur), les 12 mois, les 7 jours (samedi en premier).
- Petites phrases : 11.
- Noms : la capitale et les 19 régions du Mali, 96 formes de patronymes songhay.
- Textes : l'hymne national (corrigé après comparaison avec le transcripteur), les jours, les mois, les régions.
- Toujours l'écriture AYNEHA exacte de l'auteur (pas la transcription automatique).
- Hymne : version corrigée par l'auteur le 13 oct. 2026 (Hymne_du_Mali_Songhay.docx).
- Nouvelles règles (13 oct. 2026) : gn + voyelle = G muette + N + voyelle (plus de Ɲ) ; ng = N muette + G (nga = N muette, G, A) ; Ŋ seulement par la touche rapide ŋ. Appliquées dans l'Atelier et l'hymne ; à reporter dans le transcripteur web et le SDK.
- Hymne : « Jama » corrigé en « Djama ».

## 7. Vérification

- La logique pure (glyphes, ordre, transcription, nombres) est testée en Kotlin et comparée mot à mot au transcripteur web de référence.
- Les écrans Compose sont écrits sur le modèle compilé de NÈNÈ École ; la compilation finale se fait dans Android Studio.
