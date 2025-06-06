package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import mygame.entities.Enemy;
import mygame.systems.EnemyManager;
import mygame.utils.Constants;

public class Tower {
    public enum TowerType {
        ATTACK, SLOW
    }
    
    private final Spatial model;
    private final TowerType type;
    private Vector3f position;
    private float cooldownTimer;
    private RigidBodyControl physicsControl;
    private BulletAppState bulletAppState;
    
    public Tower(AssetManager assetManager, TowerType type) {
        this.type = type;
        this.cooldownTimer = 0f;
        this.model = loadModelForTower(assetManager, type);
    }
    
    public Tower(AssetManager assetManager, TowerType type, BulletAppState bulletAppState) {
        this.type = type;
        this.cooldownTimer = 0f;
        this.bulletAppState = bulletAppState;
        this.model = loadModelForTower(assetManager, type);
    }
    
    private Spatial loadModelForTower(AssetManager assetManager, TowerType type) {
        String modelPath = type == TowerType.ATTACK ? 
            "Models/Towers/torre_disparo.j3o" : "Models/Towers/torre_relentizado.j3o";
        
        try {
            Spatial towerModel = assetManager.loadModel(modelPath);
            towerModel.setName("Tower_" + type.name());
            towerModel.setLocalScale(1f); // Ajusta este valor para el tamaño visual
            return towerModel;
        } catch (Exception e) {
            return new Node("EmptyTower_" + type.name());
        }
    }
    
    public void setPosition(Vector3f position) {
        this.position = position.clone();
        Vector3f adjustedPosition = position.clone();
        adjustedPosition.y += 0f;
        model.setLocalTranslation(adjustedPosition);
        
        if (bulletAppState != null && physicsControl == null) {
            setupPhysics();
        }
    }
    
    private void setupPhysics() {
        try {
            // Opción 1: Usar la forma exacta del modelo
            //CollisionShape towerShape = CollisionShapeFactory.createBoxShape(model);
            
            //Opción 2: Crear caja de colisión personalizada (descomenta para usar)
            Box customBox = new Box(0.01f, 0.01f, 0.3f);
            Geometry boxGeometry = new Geometry("TowerCollisionBox", customBox);
            // No necesitas material porque solo se usa para colisión
            CollisionShape towerShape = CollisionShapeFactory.createBoxShape(boxGeometry);
            
            physicsControl = new RigidBodyControl(towerShape, 0);
            model.addControl(physicsControl);
            bulletAppState.getPhysicsSpace().add(physicsControl);
        } catch (Exception e) {
            // Ignorar errores de física
        }
    }
    
    public Vector3f getPosition() {
        return position;
    }
    
    public Spatial getModel() {
        return model;
    }
    
    public TowerType getType() {
        return type;
    }
    
    public void update(float tpf, EnemyManager enemyManager) {
        if (position == null) return;
        
        cooldownTimer -= tpf;
        
        if (cooldownTimer <= 0) {
            if (type == TowerType.ATTACK) {
                attackNearestEnemy(enemyManager);
            } else if (type == TowerType.SLOW) {
                slowEnemiesInRange(enemyManager);
            }
        }
    }
    
    private void attackNearestEnemy(EnemyManager enemyManager) {
        Enemy nearestEnemy = null;
        float minDistance = Float.MAX_VALUE;
        
        for (Enemy enemy : enemyManager.getActiveEnemies()) {
            if (!enemy.isAlive()) continue;
            
            float distance = position.distance(enemy.getPosition());
            if (distance < Constants.ATTACK_TOWER_RANGE && distance < minDistance) {
                nearestEnemy = enemy;
                minDistance = distance;
            }
        }
        
        if (nearestEnemy != null) {
            nearestEnemy.takeDamage(Constants.ATTACK_TOWER_DAMAGE);
            cooldownTimer = Constants.ATTACK_TOWER_COOLDOWN;
        }
    }
    
    private void slowEnemiesInRange(EnemyManager enemyManager) {
        boolean hasSlowedEnemies = false;
        
        for (Enemy enemy : enemyManager.getActiveEnemies()) {
            if (!enemy.isAlive()) continue;
            
            float distance = position.distance(enemy.getPosition());
            if (distance < Constants.SLOW_TOWER_RANGE) {
                enemy.applySlow(Constants.SLOW_TOWER_FACTOR);
                hasSlowedEnemies = true;
            }
        }
        
        if (hasSlowedEnemies) {
            cooldownTimer = Constants.SLOW_TOWER_COOLDOWN;
        }
    }
}