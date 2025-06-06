package mygame.entities;

import com.jme3.anim.AnimComposer;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Set;
import mygame.systems.Path;

public class Enemy {
    public enum EnemyType {
        BLUE_DEMON(150, 2f, 0.3f, "Models/Characters/BlueDemon.j3o", 8),
        DEMON(90, 3f, 0.3f, "Models/Characters/Demon.j3o", 12),
        ORC_SKULL(400, 3f, 0.6f, "Models/Characters/Orc_Skull.j3o", 25),
        GHOST_SKULL(200, 4f, 0.6f, "Models/Characters/Ghost_Skull.j3o", 50),
        DRAGON_EVOLVED(1000, 3f, 1.5f, "Models/Characters/Dragon_Evolved.j3o", 100);

        public final int maxHealth;
        public final float speed;
        public final float scale;
        public final String modelPath;
        public final int damage;

        EnemyType(int maxHealth, float speed, float scale, String modelPath, int damage) {
            this.maxHealth = maxHealth;
            this.speed = speed;
            this.scale = scale;
            this.modelPath = modelPath;
            this.damage = damage;
        }
    }

    private final Node enemyNode;
    private final Spatial model;
    private final Path path;
    final EnemyType type;
    private final Castle targetCastle;

    int currentHealth;
    private float pathT = 0;
    private boolean alive = true;
    private boolean attackingCastle = false;
    private float attackCooldown = 0;
    private static final float ATTACK_INTERVAL = 1.5f;

    // Variables para el sistema de torres
    private boolean isSlowed = false;
    private float currentSpeed;

    private RigidBodyControl physicsControl;
    private AnimComposer animator;
    private Vector3f lastPosition;

    public Enemy(AssetManager assetManager, EnemyType type, Path path, Castle castle) {
        this.type = type;
        this.path = path;
        this.targetCastle = castle;
        this.currentHealth = type.maxHealth;
        this.currentSpeed = type.speed; // Velocidad inicial
        this.lastPosition = path.getPositionAlongPath(0);

        enemyNode = new Node("EnemyNode");
        model = assetManager.loadModel(type.modelPath);
        model.setLocalScale(type.scale);
        enemyNode.attachChild(model);

        Vector3f startPos = path.getPositionAlongPath(0);
        enemyNode.setLocalTranslation(startPos);

        float radius = 0.3f * type.scale;
        float height = 1.5f * type.scale;
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(radius, height);
        physicsControl = new RigidBodyControl(capsule, 1f);
        enemyNode.addControl(physicsControl);
        physicsControl.setPhysicsLocation(startPos);

        setupAnimations();
    }

    private void setupAnimations() {
        animator = findAnimComposer(model);
        if (animator != null) {
            String walkAnim = findWalkAnimation(animator);
            if (walkAnim != null) {
                animator.setCurrentAction(walkAnim);
            }
        }
    }

    private String findWalkAnimation(AnimComposer animator) {
        Set<String> animClips = animator.getAnimClipsNames();
        if (animClips.isEmpty()) {
            return null;
        }

        String[] possibleWalkAnims = {"Walk", "Run", "walk", "run", "Fast_Flying", "moving"};

        for (String animName : possibleWalkAnims) {
            if (animClips.contains(animName)) {
                return animName;
            }
        }

        return animClips.iterator().next();
    }

    public void update(float tpf) {
        if (!alive) return;

        if (attackingCastle) {
            attackCooldown -= tpf;
            if (attackCooldown <= 0) {
                attackCastle();
                attackCooldown = ATTACK_INTERVAL;
            }
            return;
        }

        // Usar la velocidad actual (que puede estar afectada por torres)
        pathT += tpf * currentSpeed / path.getTotalLength();

        if (pathT >= 1.0f) {
            startAttackingCastle();
            return;
        }

        Vector3f newPos = path.getPositionAlongPath(pathT);
        Vector3f direction = newPos.subtract(lastPosition);
        if (direction.length() > 0) {
            direction.normalizeLocal();
            enemyNode.lookAt(newPos.add(direction), Vector3f.UNIT_Y);
        }
        
        enemyNode.setLocalTranslation(newPos);
        physicsControl.setPhysicsLocation(newPos);
        lastPosition = newPos;
    }

    private void startAttackingCastle() {
        attackingCastle = true;
        physicsControl.setLinearVelocity(Vector3f.ZERO);
        
        if (animator != null) {
            String[] possibleAttackAnims = {"Attack", "attack", "Punch", "Bite"};
            for (String animName : possibleAttackAnims) {
                if (animator.getAnimClipsNames().contains(animName)) {
                    animator.setCurrentAction(animName);
                    break;
                }
            }
        }
    }

    private void attackCastle() {
        if (targetCastle != null && !targetCastle.isDestroyed()) {
            targetCastle.takeDamage(type.damage);
        }
    }

    public void takeDamage(int damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            die();
        }
    }

    private void die() {
        alive = false;
        physicsControl.setLinearVelocity(Vector3f.ZERO);
        
        if (animator != null) {
            String[] possibleDeathAnims = {"Die", "Death", "die", "death"};
            for (String animName : possibleDeathAnims) {
                if (animator.getAnimClipsNames().contains(animName)) {
                    animator.setCurrentAction(animName);
                    break;
                }
            }
        }
    }

    public void applySlow(float slowFactor) {
        if (!isSlowed) {
            isSlowed = true;
            currentSpeed = type.speed * slowFactor;
        }
    }

    public void removeSlow() {
        if (isSlowed) {
            isSlowed = false;
            currentSpeed = type.speed;
        }
    }

    public Vector3f getPosition() {
        return enemyNode.getWorldTranslation();
    }

    // Getters existentes
    public boolean isAlive() {
        return alive;
    }

    public boolean isAttackingCastle() {
        return attackingCastle;
    }

    public Node getNode() {
        return enemyNode;
    }

    private AnimComposer findAnimComposer(Spatial spatial) {
        if (spatial instanceof Node) {
            AnimComposer composer = ((Node) spatial).getControl(AnimComposer.class);
            if (composer != null) return composer;
            
            for (Spatial child : ((Node) spatial).getChildren()) {
                composer = findAnimComposer(child);
                if (composer != null) return composer;
            }
        }
        return null;
    }
}