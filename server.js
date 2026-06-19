const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

// Configuration du moteur de rendu HTML (EJS)
app.set('view engine', 'ejs');
app.use(express.static('public'));

app.get('/', (req, res) => {
    // On envoie de fausses stats ou juste l'IP pour éviter que le HTML ne plante
    const serverStats = {
        ip: 'novafantasia.falixsrv.me',
        online: true,
        currentPlayers: 0,
        maxPlayers: 100
    };
    
    res.render('index', { stats: serverStats });
});

app.listen(PORT, () => {
    console.log(`Site de Nova Fantasia en ligne sur le port ${PORT}`);
});