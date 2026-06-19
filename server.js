const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.set('view engine', 'ejs');
app.use(express.static('public'));

app.get('/', async (req, res) => {
    try {
        const response = await fetch('https://api.mcsrvstat.us/3/novafantasia.falixsrv.me');
        const data = await response.json();

        const serverStats = {
            ip: 'novafantasia.falixsrv.me',
            online: data.online,
            currentPlayers: data.online ? (data.players?.online ?? 0) : 0,
            maxPlayers: data.online ? (data.players?.max ?? 0) : 0,
            version: data.version || "Inconnue"
        };

        res.render('index', { stats: serverStats });
    } catch (error) {
        console.error("Erreur lors de la récupération des stats :", error);
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

// Route pour la page Arène
app.get('/arene', (req, res) => {
    res.render('arene');
});

app.listen(PORT, () => {
    console.log(`Site en ligne sur le port ${PORT}`);
});