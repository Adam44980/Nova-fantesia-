const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.set('view engine', 'ejs');
app.use(express.static('public'));

app.get('/', async (req, res) => {
    try {
        // On interroge l'API mcsrvstat
        const response = await fetch('https://api.mcsrvstat.us/3/novafantasia.falixsrv.me');
        const data = await response.json();

        // On prépare les données proprement
        const serverStats = {
            ip: 'novafantasia.falixsrv.me',
            online: data.online,
            // S'il y a des joueurs, on affiche le nombre, sinon 0
            currentPlayers: data.online ? (data.players?.online ?? 0) : 0,
            maxPlayers: data.online ? (data.players?.max ?? 0) : 0,
            // Récupération de la version
            version: data.version || "Inconnue"
        };

        res.render('index', { stats: serverStats });
    } catch (error) {
        console.error("Erreur lors de la récupération des stats :", error);
        // En cas d'échec, on envoie des valeurs par défaut pour ne pas faire planter la page
        res.render('index', { 
            stats: { 
                ip: 'novafantasia.falixsrv.me', 
                online: false, 
                currentPlayers: 0, 
                maxPlayers: 0, 
                version: "Hors ligne" 
            } 
        });
    }
});

app.listen(PORT, () => {
    console.log(`Site en ligne sur le port ${PORT}`);
});