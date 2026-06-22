package com.example.playerstats;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerStatsPlugin extends JavaPlugin {

    private HttpServer server;
    private int port = 8181;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.port = getConfig().getInt("port", 8181);

        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext("/api/players", new PlayersHandler());
            server.setExecutor(null);
            server.start();
            getLogger().info("API joueurs démarrée sur le port " + port);
        } catch (IOException e) {
            getLogger().severe("Impossible de démarrer le serveur HTTP : " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop(0);
            getLogger().info("API joueurs arrêtée.");
        }
    }

    private class PlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");

            String path = exchange.getRequestURI().getPath();
            
            // LOG : On regarde ce que le site web demande exactement
            getLogger().info("Requête reçue sur l'URL : " + path);

            String json;
            if (path.equals("/api/players") || path.equals("/api/players/")) {
                json = handleListPlayers();
            } else {
                String pseudo = path.substring("/api/players/".length());
                json = handlePlayerDetail(pseudo);
            }

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private String handleListPlayers() {
        AtomicReference<String> result = new AtomicReference<>("[]");
        runSyncAndWait(() -> {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(p.getName())).append("\"");
                first = false;
            }
            sb.append("]");
            result.set(sb.toString());
        });
        return result.get();
    }

    private String handlePlayerDetail(String pseudo) {
        if (pseudo.endsWith("/")) {
            pseudo = pseudo.substring(0, pseudo.length() - 1);
        }
        String pseudoPropre = pseudo.trim();

        // LOG : On vérifie le pseudo nettoyé
        getLogger().info("Recherche des stats pour le pseudo : '" + pseudoPropre + "'");

        AtomicReference<String> result = new AtomicReference<>("{\"error\":\"Une erreur inconnue est survenue\"}");
        
        runSyncAndWait(() -> {
            try {
                Player player = Bukkit.getPlayer(pseudoPropre);
                
                if (player == null) {
                    getLogger().warning("Bukkit ne trouve aucun joueur en ligne avec le nom : " + pseudoPropre);
                    result.set("{\"error\":\"Joueur '" + pseudoPropre + "' introuvable (Script Java)\"}");
                    return;
                }

                if (!player.isOnline()) {
                    getLogger().warning("Le joueur " + player.getName() + " est détecté hors ligne.");
                    result.set("{\"error\":\"Joueur '" + player.getName() + "' hors ligne (Script Java)\"}");
                    return;
                }

                double health = player.getHealth();
                double maxHealth = player.getMaxHealth();
                int hunger = player.getFoodLevel();
                int maxHunger = 20;

                StringBuilder inv = new StringBuilder("[");
                boolean first = true;
                
                // Utilisation de getContents() plus stable sur toutes les versions de Minecraft
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item == null || item.getType().isAir()) continue;
                    if (!first) inv.append(",");
                    String displayName = item.getType().name().toLowerCase().replace("_", " ");
                    inv.append("{\"name\":\"").append(escapeJson(displayName))
                       .append("\",\"count\":").append(item.getAmount()).append("}");
                    first = false;
                }
                inv.append("]");

                String json = "{"
                    + "\"pseudo\":\"" + escapeJson(player.getName()) + "\","
                    + "\"health\":" + health + ","
                    + "\"maxHealth\":" + maxHealth + ","
                    + "\"hunger\":" + hunger + ","
                    + "\"maxHunger\":" + maxHunger + ","
                    + "\"inventory\":" + inv
                    + "}";

                result.set(json);
                getLogger().info("Stats de " + player.getName() + " générées avec succès !");

            } catch (Throwable t) {
                // SÉCURITÉ CRASH : Si Minecraft bloque sur une fonction, on capture l'erreur exacte
                getLogger().severe("CRASH INTERNE lors de la récupération des stats de " + pseudoPropre);
                t.printStackTrace();
                result.set("{\"error\":\"Crash Java : " + escapeJson(t.getClass().getSimpleName() + " - " + t.getMessage()) + "\"}");
            }
        });
        
        return result.get();
    }

    private void runSyncAndWait(Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } finally {
                    future.complete(null);
                }
            }
        }.runTask(this);

        try {
            future.get();
        } catch (Exception e) {
            getLogger().warning("Erreur de synchronisation : " + e.getMessage());
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}