package mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

public class Castle {
    private Spatial model;
    private RigidBodyControl physicsControl;
    private int maxHealth = 1000;
    private int currentHealth = maxHealth;
    private boolean destroyed = false;

    public Castle(AssetManager assetManager) {
        model = assetManager.loadModel("Models/Environment/Castillo.j3o");
        model.setLocalScale(1f);
        model.setLocalTranslation(-1f, 0.1f, 10.7f);
        model.setLocalRotation(new Quaternion().fromAngles(
            0f,
            FastMath.DEG_TO_RAD * 90f,
            0f
        ));
        
        // Configurar física del castillo
        CollisionShape castleShape = CollisionShapeFactory.createMeshShape(model);
        physicsControl = new RigidBodyControl(castleShape, 0); // masa 0 = estático
        model.addControl(physicsControl);
    }

    public void takeDamage(int damage) {
        if (destroyed) return;
        
        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            destroyed = true;
            if (model instanceof Geometry) {
                ((Geometry)model).getMaterial().setColor("Color", ColorRGBA.Red);
            }
            System.out.println("¡El castillo ha sido destruido!");
        }
    }

    public Spatial getModel() {
        return model;
    }

    public RigidBodyControl getPhysicsControl() {
        return physicsControl;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}