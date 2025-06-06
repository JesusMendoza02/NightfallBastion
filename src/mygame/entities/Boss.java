package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import mygame.systems.Path;

public class Boss extends Enemy {
    // Multiplicador de salud para el boss
    private static final int BOSS_HEALTH_MULTIPLIER = 3;
    
    public Boss(AssetManager assetManager, Path path, Castle castle) {
        super(assetManager, EnemyType.DRAGON_EVOLVED, path, castle);
        
        // Ajustar parámetros para hacerlo más difícil
        this.currentHealth = type.maxHealth * BOSS_HEALTH_MULTIPLIER;
    }
    
    // Métodos específicos para la barra de vida
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    public int getMaxHealth() {
        return type.maxHealth * BOSS_HEALTH_MULTIPLIER;
    }
    
    public float getHealthPercentage() {
        return (float) currentHealth / getMaxHealth();
    }
    
    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        
        // Puedes añadir efectos especiales al recibir daño si lo deseas
        // Por ejemplo:
        // if (getHealthPercentage() < 0.3f) {
        //     playEnragedAnimation();
        // }
    }
    // Método opcional para efectos visuales

}