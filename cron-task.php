<?php
// 1. On démarre le tampon de sortie pour ne rien afficher
ob_start();

// 2. IMPORTATION DU FICHIER QUI CONTIENT VOTRE LOGIQUE
// Remplacez 'votre-script-actuel.php' par le nom du fichier que vous exécutez actuellement
include('votre-script-actuel.php');

// 3. On nettoie tout ce qui a été généré et on termine
ob_end_clean();
exit(0);
?>