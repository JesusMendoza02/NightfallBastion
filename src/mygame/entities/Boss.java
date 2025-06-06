package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import mygame.systems.Path;
import mygame.utils.Constants;

public class Boss extends Enemy {
   
    
    public Boss(AssetManager assetManager, Path path, Castle castle) {
        super(assetManager, EnemyType.DRAGON_EVOLVED, path, castle);     
        // Ajustar parámetros para hacerlo más difícil
        this.currentHealth = type.maxHealth * Constants.BOSS_HEALTH_MULTIPLIER;
    }
    public int getCurrentHealth() {
        return currentHealth;
    }
    
    public int getMaxHealth() {
        return type.maxHealth * Constants.BOSS_HEALTH_MULTIPLIER;
    }
    
    public float getHealthPercentage() {
        return (float) currentHealth / getMaxHealth();
    }
    


}