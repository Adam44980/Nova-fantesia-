const express = require('express');
const minestat = require('minestat');
const app = express();
const PORT = process.env.PORT || 3000;

// Configuration du moteur de rendu HTML (EJS)
app.set('view engine', 'ejs');
app.use(express.static('public'));

// CONFIGURATION DE TON SERVEUR FALIX
// Si Falix te donne un port différent de 25565 (ex: 31542), change le nombre ci-dessous !
const MC_IP = 'novafantasia.falixsrv.me'; 
const MC_PORT = 25565; 

app.get('/', (req, res) => {
    // Récupération en temps réel des joueurs connectés
    minestat.init(MC_IP, MC_PORT, () => {
        
        // Si le port est 25565, on affiche juste l'IP, sinon on affiche IP:PORT
        const displayIP = MC_PORT === 25565 ? MC_IP : `${MC_IP}:${MC_PORT}`;

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