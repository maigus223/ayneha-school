# AYNEHA School - application Android

Application d'apprentissage de l'écriture **AYNEHA** (langue songhay), conçue et créée par
**Mahamadou Issiaka MAIGA (MAIGUS)**. Logiciel conçu et développé par MAIGUS.

Entièrement hors connexion, aucune permission. Construite sur le modèle de NÈNÈ École.

## Ouvrir et compiler

1. Android Studio : **File → Open** → choisir le dossier `AynehaSchool`.
2. Laisser Gradle se synchroniser (même configuration que NÈNÈ École : AGP 8.1.4, Kotlin 1.9.22,
   Compose BOM 2024.02.00, Gradle 8.4).
3. **Build → Clean Project**, puis lancer sur un téléphone (Android 8.0 / API 26 minimum).

Paquet : `com.maigus.aynehaschool` - version 1.3.0 (versionCode 3).

## Version de test et version publiée

- **Debug** (bouton ▶ d'Android Studio) : tous les mots de lecture s'affichent, avec un badge
  rouge « à valider » sur ceux qui ne sont pas encore validés.
- **Release** (APK signé pour Naabiga et le site) : seuls les mots validés (`valide = true` dans
  `Contenu.kt`) s'affichent. Tout le contenu actuel a été validé par l'auteur le 13 oct. 2026 :
  51 mots (dont jours et mois), 11 petites phrases, 20 noms de régions, 96 patronymes, 4 textes.

## Contenu

| Écran | Ce qu'il contient |
|---|---|
| Accueil | Tient sur un écran : logo (lettre H), Tombeau des Askia, « AYNEHA School », 8 sections + À propos, Réglages, « By MAIGUS » |
| L'alphabet | Les 27 lettres dans l'ordre AYNEHA (5 voyelles puis 22 consonnes), lues de droite à gauche |
| Fiche lettre | La lettre en grand, son épellation, ses formes (initiale, nasalisée, ouverte, double, muette), sa règle, des exemples validés, Tracer |
| Les accents | Les 4 accents, les 50 combinaisons, les règles |
| Le syllabaire | Chaque consonne avec les 5 voyelles (simples, nasalisées ou doubles) |
| Les chiffres | Les 10 chiffres, un convertisseur, ordinaux et virgule décimale, un tableau de 0 à 9999 |
| Tracer | Suivre au doigt une lettre, une de ses formes ou un chiffre (0 à 3 étoiles) |
| Quiz | 6 jeux de 10 questions, meilleur score gardé |
| Atelier | Transcrire (latin → AYNEHA en direct, « Pourquoi ? », Copier, Partager en image) et S'exercer |
| Lecture | Syllabes, mots, noms (régions, patronymes), petites phrases, textes (hymne, jours, mois, régions) |
| Réglages | Thème automatique / clair / sombre, taille de l'AYNEHA, pseudo et horodatage des images |
| À propos | L'écriture, l'application, les licences, la confidentialité, le contact |

## Choix importants

- **Nouvelle plage uniquement** : U+10FF7C-U+10FFDD (police `res/font/ayneha_pro.ttf`).
  Ces caractères sont hors du plan de base : en Kotlin, chacun occupe deux unités UTF-16. Tout le
  code passe par des listes de points de code (`Ayneha.points`, `Ayneha.texte`), jamais par des `Char`.
- **Décalage ancienne → nouvelle plage : +0x101F7C** (U+E000 → U+10FF7C), vérifié glyphe par glyphe.
- **RTL pur** : Android traite les caractères PUA comme du texte « gauche à droite » ; chaque mot est
  donc inversé avant le dessin (`Ayneha.visuel`) et les mots sont posés de droite à gauche.
- **Jamais de gras ni de soulignement sur l'AYNEHA** : l'emphase se fait par la taille.
- **Transcription** : portage fidèle du Transcripteur AYNEHA web (vérifié mot à mot sur 17 lignes de
  test), avec en plus **è = ɛ**, **ô = ɔ**, **gn + voyelle = G muette + N** et **ng = N muette + G** (règles du 13 oct. 2026 ; Ŋ seulement avec la touche ŋ).
- **Images partagées** : fond blanc, « AYNEHA School » en orange #FF6600 en haut, pseudo et horodatage en bas.
- **Thème « Nuit du fleuve »** : bleu nuit, orange, et l'AYNEHA toujours en encre sur fond sable.

## Structure

- `Ayneha.kt` : la matrice des 98 glyphes, l'ordre lexicographique, les nombres, l'inversion RTL.
- `Transcripteur.kt` : les règles latin → AYNEHA, avec les explications « Pourquoi ? ».
- `Contenu.kt` : mots, phrases, patronymes et textes de lecture (avec l'indicateur `valide`).
- `Theme.kt`, `Reglages.kt`, `Components.kt`, `Partage.kt` : thème, réglages, éléments communs, image partagée.
- `Ecran*.kt` : un fichier par écran ; `MainActivity.kt` : navigation.
- `docs/specs/` : la spécification validée.

## Licences

Voir le fichier `NOTICE`. En résumé :

- Code : GNU GPL v3 ou ultérieure (fichier `LICENSE`, texte officiel non modifié).
- Police `ayneha_pro` : SIL Open Font License 1.1 (fichier `app/src/main/assets/OFL.txt`).
  Il est dans `assets/` et non à côté de la police, car Android refuse tout fichier autre
  qu'une police dans `res/font/`. Il est ainsi aussi livré dans l'APK avec la police.
- Logos, icônes, images : CC BY-SA 4.0.
- Le nom « AYNEHA » et le logo restent réservés aux applications officielles de MAIGUS.

Code source : https://github.com/maigus223/ayneha-school

Contact : gwokmt2q@duck.com

## Version 1.3.0 (17 oct. 2026)

- **Lexique** (Lecture > Lexique) : le lexique global de l'auteur, près de 400 mots rangés par thème
  (famille, corps, maison, nourriture, animaux, nature, village, actions, couleurs, expressions,
  points cardinaux, nombres...), avec recherche en songhay ou en français, filtre par thème et
  quiz de lecture « S'entraîner ». Écriture AYNEHA par le transcripteur. Même liste que le site.
- **Nombres en toutes lettres** (Les chiffres > Écrire un nombre) : sous le nombre, son nom en
  songhay (lettres latines et AYNEHA), de 0 à 999 999 (`NombresSonghay.kt`, règles du document
  « Chiffres (Songhay) en toutes lettres » de l'auteur).
- `versionCode` 3 / `versionName` 1.3.0.
