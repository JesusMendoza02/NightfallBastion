package mygame.systems;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import mygame.entities.Castle;
import mygame.entities.Enemy;
import mygame.entities.Enemy.EnemyType;
import mygame.entities.Boss;
import mygame.ui.GameHealthDisplay;
import mygame.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyManager {
    private final AssetManager assetManager;
    private final Node rootNode;
    private final Castle castle;
    private final Path[] paths;
    private final List<Enemy> activeEnemies = new ArrayList<>();
    private Wave currentWave;
    private int currentWaveIndex = -1;
    private float waveTimer = 0;
    private float spawnTimer = 0;
    private boolean waveInProgress = false;
    private boolean bossSpawned = false;
    private boolean bossDefeated = false;
    private boolean allWavesCompleted = false;
    private boolean paused = false;
    private final Random random = new Random();
    private GameHealthDisplay healthDisplay;
    
    // Variables para el countdown
    private float countdownTimer = 0;
    private boolean inCountdown = false;
    private int nextWaveIndex = 0;
    
    private TowerManager towerManager;

    public EnemyManager(AssetManager assetManager, Node rootNode, Castle castle) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.castle = castle;
        this.paths = setupPaths();
        for (Path path : paths) {
            path.debugDraw(assetManager, rootNode);
        }
        startCountdown(0, Constants.WAVE_START_DELAY);
    }

    public void setHealthDisplay(GameHealthDisplay healthDisplay) {
        this.healthDisplay = healthDisplay;
    }
    
    public void setTowerManager(TowerManager towerManager) {
        this.towerManager = towerManager;
    }

    private Path[] setupPaths() {
        Vector3f[] leftPathPoints = {
            new Vector3f(-7, 0, 2),
            new Vector3f(-3, 0, 2),
            new Vector3f(-0.2f, 0, 2),
            new Vector3f(-0.2f, 0, 10)
        };

        Vector3f[] centerPathPoints = {
            new Vector3f(-0.2f, 0, -7f),
            new Vector3f(-0.2f, 0, -5),
            new Vector3f(-0.2f, 0, 0),
            new Vector3f(-0.2f, 0, 10)
        };

        Vector3f[] rightPathPoints = {
            new Vector3f(6.5f, 0, -1),
            new Vector3f(3, 0, -1),
            new Vector3f(-0.2f, 0, -1),
            new Vector3f(-0.2f, 0, 10)
        };

        return new Path[] {
            new Path(leftPathPoints),
            new Path(centerPathPoints),
            new Path(rightPathPoints)
        };
    }

    public void update(float tpf) {
        if (paused || castle.isDestroyed()) return;

        if (inCountdown) {
            countdownTimer -= tpf;
            if (countdownTimer <= 0) {
                inCountdown = false;
                startNextWave();
            }
            return;
        }

        if (!waveInProgress) return;

        waveTimer += tpf;
        spawnTimer += tpf;

        if (spawnTimer >= currentWave.spawnInterval && currentWave.enemiesToSpawn > 0) {
            spawnEnemy();
            spawnTimer = 0;
            currentWave.enemiesToSpawn--;
        }

        updateActiveEnemies(tpf);
        checkWaveCompletion();
    }

    private void updateActiveEnemies(float tpf) {
        for (int i = 0; i < activeEnemies.size(); i++) {
            Enemy enemy = activeEnemies.get(i);
            if (!paused) {
                enemy.update(tpf);
            }

            if (!enemy.isAlive()) {
                if (enemy instanceof Boss && bossSpawned && !bossDefeated) {
                    bossDefeated = true;
                    System.out.println("¡BOSS FINAL DERROTADO!");
                    if (healthDisplay != null) {
                        healthDisplay.setBoss(null);
                    }
                }
                
                rootNode.detachChild(enemy.getNode());
                activeEnemies.remove(i);
                i--;
            }
        }
    }

    private void checkWaveCompletion() {
        if (currentWave.enemiesToSpawn == 0 && activeEnemies.isEmpty()) {
            handleWaveCompletion();
        }
    }

    private void handleWaveCompletion() {
        waveInProgress = false;
        System.out.println("Wave " + (currentWaveIndex + 1) + " completada!");

        if (currentWaveIndex == 2 && !bossSpawned) {
            System.out.println("Preparando para spawnear boss...");
            startCountdown(3, Constants.BOSS_COUNTDOWN_DURATION);
            return;
        }

        if (bossSpawned && bossDefeated) {
            allWavesCompleted = true;
            System.out.println("¡VICTORIA TOTAL!");
            return;
        }

        if (currentWaveIndex < 2) {
            startCountdown(currentWaveIndex + 1, Constants.WAVE_COUNTDOWN_DURATION);
        }
    }

    private void startCountdown(int nextWave, float duration) {
        this.nextWaveIndex = nextWave;
        this.countdownTimer = duration;
        this.inCountdown = true;
        System.out.println("Siguiente oleada en " + countdownTimer + " segundos...");
    }

    private void startNextWave() {
        currentWaveIndex = nextWaveIndex;
        
        // Resetear torres al comenzar nueva oleada (excepto para el boss)
        if (towerManager != null && currentWaveIndex < 3) {
            towerManager.resetTowersForNewWave();
        }
        
        if (currentWaveIndex == 3) {
            spawnBoss();
            return;
        }
        
        switch (currentWaveIndex) {
            case 0:
                currentWave = new Wave(
                    new EnemyType[]{EnemyType.BLUE_DEMON, EnemyType.DEMON},
                    new float[]{0.5f, 0.5f},
                    60,
                    0.6f,
                    new int[]{0, 1, 2}
                );
                System.out.println("¡Oleada 1 iniciada! Solo enemigos normales");
                break;

            case 1:
                currentWave = new Wave(
                    new EnemyType[]{
                        EnemyType.BLUE_DEMON,
                        EnemyType.DEMON,
                        EnemyType.ORC_SKULL,
                        EnemyType.GHOST_SKULL
                    },
                    new float[]{
                        0.475f,
                        0.472f,
                        0.025f,
                        0.025f
                    },
                    70,
                    0.6f,
                    new int[]{0, 1, 2}
                );
                System.out.println("¡Oleada 2 iniciada! Enemigos normales + Mini-Bosses");
                break;

            case 2:
                currentWave = new Wave(
                    new EnemyType[]{
                        EnemyType.BLUE_DEMON,
                        EnemyType.DEMON,
                        EnemyType.ORC_SKULL,
                        EnemyType.GHOST_SKULL
                    },
                    new float[]{
                        0.4f,
                        0.4f,
                        0.1f,
                        0.1f
                    },
                    80,
                    0.5f,
                    new int[]{0, 1, 2}
                );
                System.out.println("¡Oleada 3 iniciada! Enemigos fuertes");
                break;

            default:
                System.out.println("No hay más oleadas definidas.");
                waveInProgress = false;
                return;
        }
        
        waveInProgress = true;
    }

    private void spawnBoss() {
        if (paths == null || paths.length < 2) {
            System.err.println("ERROR: No se encontró el path central para el boss");
            return;
        }

        Path bossPath = paths[1];
        Boss boss = new Boss(assetManager, bossPath, castle);
        activeEnemies.add(boss);
        rootNode.attachChild(boss.getNode());
        
        if (healthDisplay != null) {
            healthDisplay.setBoss(boss);
        }

        bossSpawned = true;
        waveInProgress = true;
        System.out.println("¡BOSS FINAL HA APARECIDO EN EL CAMINO CENTRAL!");
    }

    private void spawnEnemy() {
        EnemyType type = selectRandomEnemyType();
        int pathIndex = currentWave.spawnPathIndices[random.nextInt(currentWave.spawnPathIndices.length)];
        Path path = paths[pathIndex];

        Enemy enemy = new Enemy(assetManager, type, path, castle);
        activeEnemies.add(enemy);
        rootNode.attachChild(enemy.getNode());

        System.out.println("Generado " + type + " en camino " + pathIndex);
    }

    private EnemyType selectRandomEnemyType() {
        float total = 0;
        for (float prob : currentWave.enemyTypeProbabilities) {
            total += prob;
        }

        float randomValue = random.nextFloat() * total;
        float cumulative = 0;

        for (int i = 0; i < currentWave.enemyTypes.length; i++) {
            cumulative += currentWave.enemyTypeProbabilities[i];
            if (randomValue <= cumulative) {
                return currentWave.enemyTypes[i];
            }
        }

        return currentWave.enemyTypes[0];
    }

    public boolean isGameWon() {
        return bossSpawned && bossDefeated && allWavesCompleted;
    }

    public void pause() {
        paused = true;
        System.out.println("EnemyManager pausado");
    }

    public void resume() {
        paused = false;
        System.out.println("EnemyManager reanudado");
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isInCountdown() {
        return inCountdown;
    }

    public float getCountdownTimer() {
        return countdownTimer;
    }

    public int getNextWaveIndex() {
        return nextWaveIndex;
    }

    public int getCurrentWaveIndex() { 
        return currentWaveIndex; 
    }
    
    public boolean isWaveInProgress() { 
        return waveInProgress; 
    }
    
    public List<Enemy> getActiveEnemies() { 
        return new ArrayList<>(activeEnemies); 
    }
    
    public boolean isBossDefeated() { 
        return bossDefeated; 
    }
    
    public boolean isBossSpawned() { 
        return bossSpawned; 
    }

    public void cleanup() {
        paused = true;
        
        for (Enemy enemy : activeEnemies) {
            if (enemy.getNode().getParent() != null) {
                rootNode.detachChild(enemy.getNode());
            }
        }
        
        activeEnemies.clear();
        currentWaveIndex = -1;
        waveTimer = 0;
        spawnTimer = 0;
        waveInProgress = false;
        bossSpawned = false;
        bossDefeated = false;
        allWavesCompleted = false;
        inCountdown = false;
        paused = false;
        
        System.out.println("EnemyManager limpiado y reseteado");
    }

    private static class Wave {
        final EnemyType[] enemyTypes;
        final float[] enemyTypeProbabilities;
        int enemiesToSpawn;
        final float spawnInterval;
        final int[] spawnPathIndices;

        Wave(EnemyType[] enemyTypes, float[] enemyTypeProbabilities, 
             int enemiesToSpawn, float spawnInterval, int[] spawnPathIndices) {
            this.enemyTypes = enemyTypes;
            this.enemyTypeProbabilities = enemyTypeProbabilities;
            this.enemiesToSpawn = enemiesToSpawn;
            this.spawnInterval = spawnInterval;
            this.spawnPathIndices = spawnPathIndices;
        }
    }
}