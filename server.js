const express = require('express');
const minestat = require('minestat');
const app = express();
const PORT = process.env.PORT || 3000;

// Configuration du moteur de rendu HTML (EJS)
app.set('view engine', 'ejs');
app.use(express.static('public'));

// CONFIGURATION DE TON SERVEUR FALIX (MISE À JOUR COMPLET)
const MC_IP = 'eu18-free.falixserver.net'; 
const MC_PORT = 21365; 

app.get('/', (req, res) => {
    // Récupération en temps réel des joueurs connectés
    minestat.init(MC_IP, MC_PORT, () => {
        
        // On affiche l'adresse personnalisée pour tes joueurs sur le site
        const displayIP = 'novafantasia.falixsrv.me';

        const serverStats = {
            ip: displayIP,
            online: minestat.online,
            currentPlayers: minestat.current_players || 0,
            maxPlayers: minestat.max_players || 0
        };
        
        res.render('index', { stats: serverStats });
    });
});

app.listen(PORT, () => {
    console.log(`Site de Nova Fantasia en ligne sur le port ${PORT}`);
});