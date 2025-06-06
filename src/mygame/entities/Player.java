package mygame.entities;

import com.jme3.anim.AnimComposer;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Player {
    private Spatial model;
    private AnimComposer animComposer;
    private String currentAction = "";
    private CharacterControl characterControl;
    
    // Variables para controlar el ataque
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private static final float ATTACK_DURATION = 1.0f; // Duración del ataque en segundos
    
    // Configuración mejorada de físicas
    private static final float CAPSULE_RADIUS = 0.02f;
    private static final float CAPSULE_HEIGHT = 0.02f;
    private static final float STEP_HEIGHT = 0.1f;
    private static final float JUMP_SPEED = 8f;
    private static final float FALL_SPEED = 20.0f;
    private static final float GRAVITY = 20.0f;

    public Player(AssetManager assetManager) {
        loadModel(assetManager);
        setupPhysics();
        setupAnimations();
    }
    
    private void loadModel(AssetManager assetManager) {
        model = assetManager.loadModel("Models/Characters/KnightCharacter.j3o");
        model.setLocalScale(0.2f);
        model.setLocalTranslation(0, 1, 0);
    }
    
    private void setupPhysics() {
        // Crear cápsula de colisión más realista
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(CAPSULE_RADIUS, CAPSULE_HEIGHT, 1);
        characterControl = new CharacterControl(capsule, STEP_HEIGHT);
        
        // Configurar parámetros de movimiento
        characterControl.setJumpSpeed(JUMP_SPEED);
        characterControl.setFallSpeed(FALL_SPEED);
        characterControl.setGravity(GRAVITY);
        characterControl.setPhysicsLocation(new Vector3f(0, 3f, 0));
        
        // Mejorar la respuesta del movimiento
        characterControl.setMaxSlope(FastMath.PI / 6); // 30 grados máximo de pendiente
        
        model.addControl(characterControl);
        
        System.out.println("✅ Físicas del jugador configuradas correctamente");
    }
    
    private void setupAnimations() {
        animComposer = findAnimComposer(model);
        
        if (animComposer != null) {
            setAnimation("Idle");
            System.out.println("✅ Animaciones del jugador configuradas");
            
            // Imprimir todas las animaciones disponibles para debug
            System.out.println("Animaciones disponibles:");
            for (String animName : animComposer.getAnimClipsNames()) {
                System.out.println("- " + animName);
            }
        } else {
            System.out.println("❌ AnimComposer no encontrado en el modelo");
        }
    }

    public Spatial getModel() {
        return model;
    }

    public CharacterControl getControl() {
        return characterControl;
    }

    private AnimComposer findAnimComposer(Spatial spatial) {
        if (spatial instanceof Node node) {
            AnimComposer composer = node.getControl(AnimComposer.class);
            if (composer != null) return composer;
            
            for (Spatial child : node.getChildren()) {
                composer = findAnimComposer(child);
                if (composer != null) return composer;
            }
        } else {
            AnimComposer composer = spatial.getControl(AnimComposer.class);
            if (composer != null) return composer;
        }
        return null;
    }

    public void setAnimation(String action) {
        if (animComposer != null && !action.equals(currentAction)) {
            try {
                animComposer.setCurrentAction(action);
                currentAction = action;
                System.out.println("✅ Animación cambiada a: " + action);
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo reproducir la animación: " + action);
                System.out.println("Error: " + e.getMessage());
                fallbackToIdle();
            }
        }
    }
    
    private void fallbackToIdle() {
        if (!currentAction.equals("Idle")) {
            try {
                animComposer.setCurrentAction("Idle");
                currentAction = "Idle";
            } catch (Exception e2) {
                System.out.println("❌ Error crítico con animaciones: " + e2.getMessage());
            }
        }
    }

    public void attack() {
        if (!isAttacking && animComposer != null) {
            isAttacking = true;
            attackTimer = 0f;
            
            // Intentar diferentes nombres posibles para la animación de ataque
            String[] possibleAttackAnims = {
                "swordAttackJump",
                "SwordAttackJump", 
                "sword_attack_jump",
                "Attack",
                "attack"
            };
            
            boolean attackAnimFound = false;
            for (String animName : possibleAttackAnims) {
                try {
                    if (animComposer.getAnimClipsNames().contains(animName)) {
                        setAnimation(animName);
                        attackAnimFound = true;
                        System.out.println("✅ Ataque ejecutado con animación: " + animName);
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            
            if (!attackAnimFound) {
                System.out.println("⚠️ No se encontró animación de ataque. Animaciones disponibles:");
                for (String animName : animComposer.getAnimClipsNames()) {
                    System.out.println("- " + animName);
                }
                isAttacking = false;
            }
        }
    }
    
    /**
     * Actualizar el estado del jugador (llamar desde el loop principal)
     */
    public void update(float tpf) {
        if (isAttacking) {
            attackTimer += tpf;
            if (attackTimer >= ATTACK_DURATION) {
                isAttacking = false;
                attackTimer = 0f;
                // Volver a idle después del ataque
                setAnimation("Idle");
                System.out.println("✅ Ataque completado, volviendo a Idle");
            }
        }
    }
    
    /**
     * Verifica si el jugador está atacando
     */
    public boolean isAttacking() {
        return isAttacking;
    }
    
    /**
     * Verifica si el jugador está en el suelo
     */
    public boolean isOnGround() {
        return characterControl.onGround();
    }
    
    /**
     * Obtiene la velocidad actual del jugador
     */
    public Vector3f getVelocity() {
        return characterControl.getWalkDirection();
    }
    
    /**
     * Resetea la posición del jugador
     */
    public void resetPosition(Vector3f position) {
        model.setLocalTranslation(position);
        characterControl.setPhysicsLocation(position);
        characterControl.setWalkDirection(Vector3f.ZERO);
    }
}