package mygame.systems;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mygame.entities.Tower;
import mygame.utils.Constants;
import mygame.systems.EnemyManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TowerManager {
    private final AssetManager assetManager;
    private final Node rootNode;
    private final List<Tower> towers;
    private Vector3f currentPlacementPoint;
    
    private final Map<Vector3f, Spatial> placementPointers;
    private final Node pointersNode;
    private Material availablePointerMaterial;
    private Material unavailablePointerMaterial;
    private Material inactivePointerMaterial;
    
    private float pointerAnimationTimer = 0f;
    private static final float POINTER_PULSE_SPEED = 3.0f;
    private static final float POINTER_HEIGHT_OFFSET = 0.1f;
    
    // Sistema de torres mejorado
    private int towersPlacedThisWave = 0;
    private int totalTowersPlaced = 0;
    private final int maxTowersPerWave = 2;
    private final int maxTotalTowers = 6; // Límite total de torres en toda la partida
    private boolean allowTowerPlacement = false;
    
    // Tracking de oleadas para debugging
    private int lastWaveIndex = -1;
    private boolean towerPlacementWindowOpen = false;
    private boolean wasInCountdown = false; // Para detectar cuando INICIA el countdown

    public TowerManager(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.towers = new ArrayList<>();
        this.currentPlacementPoint = null;
        this.placementPointers = new HashMap<>();
        this.pointersNode = new Node("TowerPointers");
        
        rootNode.attachChild(pointersNode);
        initializeMaterials();
        createPlacementPointers();
        
        // Al inicio del juego, permitir colocar las primeras 2 torres
        towerPlacementWindowOpen = true;
        System.out.println("TowerManager inicializado - Puedes colocar las primeras 2 torres");
    }
    
    private void initializeMaterials() {
        availablePointerMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        availablePointerMaterial.setColor("Color", new ColorRGBA(0.0f, 1.0f, 0.0f, 0.8f));
        availablePointerMaterial.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        
        unavailablePointerMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        unavailablePointerMaterial.setColor("Color", new ColorRGBA(1.0f, 0.0f, 0.0f, 0.6f));
        unavailablePointerMaterial.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
        
        inactivePointerMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        inactivePointerMaterial.setColor("Color", new ColorRGBA(0.0f, 0.6f, 0.0f, 0.3f));
        inactivePointerMaterial.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
    }
    
    private void createPlacementPointers() {
        for (Vector3f point : Constants.TOWER_PLACEMENT_POINTS) {
            Spatial pointerModel = assetManager.loadModel("Models/Pointers/Pointer.j3o");
            if (pointerModel != null) {
                Vector3f pointerPosition = point.clone();
                pointerPosition.y += POINTER_HEIGHT_OFFSET;
                pointerModel.setLocalTranslation(pointerPosition);
                pointerModel.setMaterial(inactivePointerMaterial);
                pointerModel.setCullHint(Spatial.CullHint.Never);
                pointersNode.attachChild(pointerModel);
                placementPointers.put(point, pointerModel);
            }
        }
    }
    
    public void update(float tpf, Vector3f playerPosition, EnemyManager enemyManager) {
        // Detectar cuando INICIA el countdown para abrir ventanas de colocación
        checkForCountdownStart(enemyManager);
        
        // Actualizar lógica de colocación de torres
        updateTowerPlacementLogic(enemyManager);
        
        checkPlacementPoints(playerPosition);
        updatePointerStates(playerPosition);
        updatePointerAnimation(tpf);
        
        for (Tower tower : towers) {
            tower.update(tpf, enemyManager);
        }
    }
    
    private void checkForCountdownStart(EnemyManager enemyManager) {
        boolean currentlyInCountdown = enemyManager.isInCountdown();
        
        // Detectar cuando INICIA el countdown (transición de false a true)
        if (currentlyInCountdown && !wasInCountdown) {
            int nextWaveIndex = enemyManager.getNextWaveIndex();
            
            // Abrir ventana de colocación para las oleadas apropiadas
            // nextWaveIndex 1 = después de completar oleada 0
            // nextWaveIndex 2 = después de completar oleada 1  
            // nextWaveIndex 3 = después de completar oleada 2 (para boss)
            if (nextWaveIndex == 1 || nextWaveIndex == 2) {
                openTowerPlacementWindow();
            }
        }
        
        // Actualizar el estado anterior
        wasInCountdown = currentlyInCountdown;
    }
    
    private void updateTowerPlacementLogic(EnemyManager enemyManager) {
        // Permitir colocar torres en estas condiciones:
        // 1. Durante el countdown entre oleadas Y ventana abierta
        // 2. Al inicio del juego (antes de que comience la primera oleada)
        // 3. Que no hayamos alcanzado el límite de torres para esta ventana
        // 4. Que no hayamos alcanzado el límite total de torres
        
        boolean inCountdown = enemyManager.isInCountdown();
        boolean beforeFirstWave = enemyManager.getCurrentWaveIndex() == -1;
        boolean hasRemainingTowersThisWindow = towersPlacedThisWave < maxTowersPerWave;
        boolean hasRemainingTowersTotal = totalTowersPlaced < maxTotalTowers;
        
        allowTowerPlacement = (inCountdown || beforeFirstWave) && 
                            towerPlacementWindowOpen && 
                            hasRemainingTowersThisWindow && 
                            hasRemainingTowersTotal;
        
        // Cerrar la ventana si ya no hay countdown y la oleada está en progreso
        if (!inCountdown && enemyManager.isWaveInProgress() && towerPlacementWindowOpen) {
            closeTowerPlacementWindow();
        }
    }
    
    private void openTowerPlacementWindow() {
        towerPlacementWindowOpen = true;
        towersPlacedThisWave = 0; // Reset del contador para esta ventana
        System.out.println("=== VENTANA DE TORRES ABIERTA (COUNTDOWN INICIADO) ===");
        System.out.println("Puedes colocar hasta " + maxTowersPerWave + " torres");
        System.out.println("Torres totales colocadas: " + totalTowersPlaced + "/" + maxTotalTowers);
    }
    
    private void closeTowerPlacementWindow() {
        towerPlacementWindowOpen = false;
        System.out.println("=== VENTANA DE TORRES CERRADA ===");
        System.out.println("Torres colocadas en esta ventana: " + towersPlacedThisWave);
        System.out.println("Torres totales: " + totalTowersPlaced + "/" + maxTotalTowers);
    }
    
    private void checkPlacementPoints(Vector3f playerPosition) {
        currentPlacementPoint = null;
        
        for (Vector3f point : Constants.TOWER_PLACEMENT_POINTS) {
            float distanceToPlayer = playerPosition.distance(point);
            
            if (distanceToPlayer < Constants.TOWER_PLACEMENT_RANGE && !isTowerAtPoint(point)) {
                currentPlacementPoint = point.clone();
                break;
            }
        }
    }
    
    private void updatePointerStates(Vector3f playerPosition) {
        for (Map.Entry<Vector3f, Spatial> entry : placementPointers.entrySet()) {
            Vector3f point = entry.getKey();
            Spatial pointer = entry.getValue();
            float distanceToPlayer = playerPosition.distance(point);
            
            if (isTowerAtPoint(point)) {
                pointer.setMaterial(unavailablePointerMaterial);
            } else if (distanceToPlayer < Constants.TOWER_PLACEMENT_RANGE && allowTowerPlacement) {
                pointer.setMaterial(availablePointerMaterial);
            } else {
                pointer.setMaterial(inactivePointerMaterial);
            }
        }
    }
    
    private void updatePointerAnimation(float tpf) {
        pointerAnimationTimer += tpf * POINTER_PULSE_SPEED;
        float pulseScale = 1.0f + 0.1f * (float)Math.sin(pointerAnimationTimer);
        
        for (Map.Entry<Vector3f, Spatial> entry : placementPointers.entrySet()) {
            Vector3f point = entry.getKey();
            Spatial pointer = entry.getValue();
            
            if (!isTowerAtPoint(point) && currentPlacementPoint != null && 
                currentPlacementPoint.distance(point) < 0.1f && allowTowerPlacement) {
                pointer.setLocalScale(pulseScale);
            } else {
                pointer.setLocalScale(1.0f);
            }
        }
    }
    
    public boolean canPlaceTower() {
        return currentPlacementPoint != null && 
               allowTowerPlacement && 
               !isTowerAtPoint(currentPlacementPoint);
    }
    
    public boolean placeTower(Tower.TowerType type) {
        if (!canPlaceTower()) {
            // Debug: explicar por qué no se puede colocar
            System.out.println("No se puede colocar torre:");
            System.out.println("- Punto de colocación: " + (currentPlacementPoint != null));
            System.out.println("- Colocación permitida: " + allowTowerPlacement);
            System.out.println("- Torres esta ventana: " + towersPlacedThisWave + "/" + maxTowersPerWave);
            System.out.println("- Torres totales: " + totalTowersPlaced + "/" + maxTotalTowers);
            System.out.println("- Ventana abierta: " + towerPlacementWindowOpen);
            return false;
        }
        
        Tower newTower = new Tower(assetManager, type);
        newTower.setPosition(currentPlacementPoint);
        rootNode.attachChild(newTower.getModel());
        towers.add(newTower);
        
        towersPlacedThisWave++;
        totalTowersPlaced++;
        
        System.out.println("¡Torre colocada!");
        System.out.println("Torres en esta ventana: " + towersPlacedThisWave + "/" + maxTowersPerWave);
        System.out.println("Torres totales: " + totalTowersPlaced + "/" + maxTotalTowers);
        
        updatePointerForPoint(currentPlacementPoint);
        
        // Si se alcanzó el límite total, cerrar la ventana
        if (totalTowersPlaced >= maxTotalTowers) {
            towerPlacementWindowOpen = false;
            System.out.println("¡Límite máximo de torres alcanzado!");
        }
        
        return true;
    }
    
    private void updatePointerForPoint(Vector3f point) {
        Spatial pointer = placementPointers.get(point);
        if (pointer != null) {
            pointer.setMaterial(unavailablePointerMaterial);
            pointer.setLocalScale(1.0f);
        }
    }
    
    private boolean isTowerAtPoint(Vector3f point) {
        for (Tower tower : towers) {
            if (tower.getPosition() != null && tower.getPosition().distance(point) < 1f) {
                return true;
            }
        }
        return false;
    }
    
    // Este método ya no se usa de la misma manera, pero lo mantenemos por compatibilidad
    public void resetTowersForNewWave() {
        // Ya no reseteamos aquí, el reseteo se maneja en openTowerPlacementWindow()
        System.out.println("resetTowersForNewWave llamado - pero el manejo se hace automáticamente");
    }
    
    public int getTowersPlacedThisWave() {
        return towersPlacedThisWave;
    }
    
    public int getMaxTowersPerWave() {
        return maxTowersPerWave;
    }
    
    public int getTotalTowersPlaced() {
        return totalTowersPlaced;
    }
    
    public int getMaxTotalTowers() {
        return maxTotalTowers;
    }
    
    public boolean isTowerPlacementWindowOpen() {
        return towerPlacementWindowOpen;
    }
    
    public List<Tower> getTowers() {
        return towers;
    }
    
    public Vector3f getCurrentPlacementPoint() {
        return currentPlacementPoint;
    }
    
    public void cleanup() {
        for (Tower tower : towers) {
            if (tower.getModel().getParent() != null) {
                rootNode.detachChild(tower.getModel());
            }
        }
        towers.clear();
        towersPlacedThisWave = 0;
        totalTowersPlaced = 0;
        towerPlacementWindowOpen = true; // Resetear para permitir colocar al inicio
        lastWaveIndex = -1;
        wasInCountdown = false; // Resetear el tracking del countdown
        pointersNode.detachAllChildren();
        placementPointers.clear();
        currentPlacementPoint = null;
        createPlacementPointers();
        System.out.println("TowerManager limpiado y reseteado");
    } 
}