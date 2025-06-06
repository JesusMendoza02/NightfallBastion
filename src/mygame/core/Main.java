package mygame.core;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.*;
import com.jme3.scene.Spatial;
import mygame.entities.Castle;
import mygame.entities.Player;
import mygame.entities.Tower;
import mygame.systems.EnemyManager;
import mygame.systems.TowerManager;
import mygame.ui.GameHealthDisplay;
import mygame.ui.GameOverState;
import mygame.ui.VictoryState;
import mygame.ui.TowerUI;
import mygame.ui.WaveCountdownUI;
import mygame.utils.Constants;

public class Main extends SimpleApplication {
    private EnemyManager enemyManager;
    private TowerManager towerManager;
    private Player player;
    private GamePhysics gamePhysics;
    private Castle castle;
    private GameHealthDisplay gameHealthDisplay;
    private GameOverState gameOverState;
    private VictoryState victoryState;
    private TowerUI towerUI;
    private WaveCountdownUI waveCountdownUI;

    private boolean moveUp, moveDown, moveLeft, moveRight;
    private Vector3f walkDirection = new Vector3f();
    private Vector3f currentPlayerPos = new Vector3f();
    private boolean gameOver = false;
    private boolean gameWon = false;

   
    @Override
    public void simpleInitApp() {
        initializeGame();
    }

    private void initializeGame() {
        gameOver = false;
        gameWon = false;
        moveUp = moveDown = moveLeft = moveRight = false;

        rootNode.detachAllChildren();
        guiNode.detachAllChildren();
        rootNode.getLocalLightList().clear();

        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.7f, 1.0f, 1.0f));
        flyCam.setEnabled(false);

        setupLighting();
        setupInput();

        if (gamePhysics != null) gamePhysics.cleanup();
        gamePhysics = new GamePhysics(this, assetManager, rootNode);

        castle = new Castle(assetManager);
        rootNode.attachChild(castle.getModel());
        gamePhysics.getBulletAppState().getPhysicsSpace().add(castle.getPhysicsControl());

        player = new Player(assetManager);
        player.getModel().setLocalTranslation(0, 1f, 0);
        rootNode.attachChild(player.getModel());
        gamePhysics.getBulletAppState().getPhysicsSpace().add(player.getControl());

        setupOptimalCamera();

        if (enemyManager != null) enemyManager.cleanup();
        enemyManager = new EnemyManager(assetManager, rootNode, castle);

        if (towerManager != null) towerManager.cleanup();
        towerManager = new TowerManager(assetManager, rootNode);

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        gameHealthDisplay = new GameHealthDisplay(castle, guiNode, font, assetManager, settings);
        enemyManager.setHealthDisplay(gameHealthDisplay);
        towerUI = new TowerUI(guiNode, font, settings, towerManager);
        waveCountdownUI = new WaveCountdownUI(guiNode, font, settings);

        if (gameOverState == null) {
            gameOverState = new GameOverState(this);
            stateManager.attach(gameOverState);
        }
        gameOverState.setEnabled(false);

        if (victoryState == null) {
            victoryState = new VictoryState(this);
            stateManager.attach(victoryState);
        }
        victoryState.setEnabled(false);
    }

    private void setupOptimalCamera() {
        float radians = FastMath.DEG_TO_RAD * Constants.CAMERA_ANGLE_DEGREES;
        float distance = Constants.CAMERA_HEIGHT / FastMath.sin(radians);
        float height = Constants.CAMERA_HEIGHT;
        float horizontalOffset = distance * FastMath.cos(radians);

        Vector3f cameraOffset = new Vector3f(0, height, horizontalOffset);
        Vector3f lookAtTarget = new Vector3f(0, 0, 0);
        cam.setLocation(cameraOffset);
        cam.lookAt(lookAtTarget, Vector3f.UNIT_Y);

        float mapSize = Math.max(
            Math.abs(Constants.MAP_MAX_X - Constants.MAP_MIN_X),
            Math.abs(Constants.MAP_MAX_Z - Constants.MAP_MIN_Z)
        );
        float optimalFOV = 2 * FastMath.atan(mapSize / (2 * Constants.CAMERA_HEIGHT)) * FastMath.RAD_TO_DEG * 1.1f;
        optimalFOV = FastMath.clamp(optimalFOV, 30f, 90f);
        cam.setFrustumPerspective(optimalFOV, (float) cam.getWidth() / cam.getHeight(), 0.1f, 1000f);
    }

    private void setupLighting() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0, -1, 0).normalizeLocal());
        sun.setColor(ColorRGBA.White.mult(2.2f));
        rootNode.addLight(sun);

        DirectionalLight fillLight = new DirectionalLight();
        fillLight.setDirection(new Vector3f(-0.3f, -0.7f, -0.3f).normalizeLocal());
        fillLight.setColor(ColorRGBA.White.mult(0.8f));
        rootNode.addLight(fillLight);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White.mult(0.6f));
        rootNode.addLight(ambient);
    }

    private void setupInput() {
        inputManager.clearMappings();
        inputManager.clearRawInputListeners();

        inputManager.addMapping("MoveUp", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("MoveDown", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("MoveRight", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Attack", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping("PlaceAttackTower", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("PlaceSlowTower", new KeyTrigger(KeyInput.KEY_2));

        inputManager.addListener(actionListener,
            "MoveUp", "MoveDown", "MoveLeft", "MoveRight",
            "Jump", "Attack", "PlaceAttackTower", "PlaceSlowTower");
    }

    private final ActionListener actionListener = (name, isPressed, tpf) -> {
        if (gameOver || gameWon) return;

        switch (name) {
            case "MoveUp" -> moveUp = isPressed;
            case "MoveDown" -> moveDown = isPressed;
            case "MoveLeft" -> moveLeft = isPressed;
            case "MoveRight" -> moveRight = isPressed;
            case "Jump" -> {
                if (isPressed && player != null && player.getControl().onGround()) {
                    player.getControl().jump();
                }
            }
            case "Attack" -> {
                if (isPressed && player != null) {
                    player.attack();
                }
            }
            case "PlaceAttackTower" -> {
                if (isPressed && towerManager != null) {
                    towerManager.placeTower(Tower.TowerType.ATTACK);
                }
            }
            case "PlaceSlowTower" -> {
                if (isPressed && towerManager != null) {
                    towerManager.placeTower(Tower.TowerType.SLOW);
                }
            }
        }
    };

    @Override
    public void simpleUpdate(float tpf) {
        if (gameOver || gameWon || player == null) return;

        if (castle.isDestroyed()) {
            triggerGameOver();
            return;
        }

        if (enemyManager.isGameWon() && !gameWon) {
            triggerVictory();
            return;
        }

        // Actualizar el countdown UI si está activo
        if (enemyManager.isInCountdown()) {
            waveCountdownUI.startCountdown(enemyManager.getNextWaveIndex(), enemyManager.getCountdownTimer());
            
            // Mostrar torres disponibles al comenzar nueva oleada
            if (towerManager != null) {
                int towersAvailable = towerManager.getMaxTowersPerWave() - towerManager.getTowersPlacedThisWave();
                waveCountdownUI.setTowersAvailable(towersAvailable);
            }
        }
        
        // Actualizar el contador de oleadas
        if (enemyManager.getCurrentWaveIndex() >= 0) {
            waveCountdownUI.setCurrentWave(enemyManager.getCurrentWaveIndex() + 1);
        }
        
        // Actualizar torres disponibles durante el juego
        if (towerManager != null && !enemyManager.isInCountdown()) {
            int towersAvailable = towerManager.getMaxTowersPerWave() - towerManager.getTowersPlacedThisWave();
            waveCountdownUI.setTowersAvailable(towersAvailable);
        }
        
        waveCountdownUI.update(tpf, settings);

        updatePlayerMovement(tpf);
        updateCamera(tpf);
        player.update(tpf);
        enemyManager.update(tpf);

        if (towerManager != null) {
            towerManager.update(tpf, player.getModel().getWorldTranslation(), enemyManager);
        }
        
        gameHealthDisplay.update(tpf);
        if (towerUI != null && towerManager != null) {
            towerUI.update(towerManager.canPlaceTower(), towerManager.getCurrentPlacementPoint());
        }
    }

    private void triggerGameOver() {
        gameOver = true;
        if (enemyManager != null) enemyManager.pause();
        if (gameOverState != null) gameOverState.setEnabled(true);
        System.out.println("¡GAME OVER! El castillo ha sido destruido.");
    }

    private void triggerVictory() {
        gameWon = true;
        if (enemyManager != null) enemyManager.pause();
        if (victoryState != null) victoryState.setEnabled(true);
        System.out.println("¡VICTORIA! Has derrotado al boss y completado todas las oleadas.");
    }

    public void restartGame() {
        if (gameOverState != null) gameOverState.setEnabled(false);
        if (victoryState != null) victoryState.setEnabled(false);
        initializeGame();
    }

    private void updatePlayerMovement(float tpf) {
        if (player.isAttacking()) {
            player.getControl().setWalkDirection(Vector3f.ZERO);
            return;
        }

        walkDirection.set(0, 0, 0);
        float moveSpeed = Constants.PLAYER_MOVE_SPEED * tpf;

        if (moveUp) walkDirection.z -= 1;
        if (moveDown) walkDirection.z += 1;
        if (moveLeft) walkDirection.x -= 1;
        if (moveRight) walkDirection.x += 1;

        if (walkDirection.length() > 0) {
            walkDirection.normalizeLocal().multLocal(moveSpeed);
            player.getControl().setWalkDirection(walkDirection);
            float angle = FastMath.atan2(walkDirection.x, walkDirection.z);
            player.getModel().setLocalRotation(new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y));
            player.setAnimation("Run");
        } else {
            player.getControl().setWalkDirection(Vector3f.ZERO);
            player.setAnimation("Idle");
        }

        enforceMapBounds();
    }

    private void enforceMapBounds() {
        Vector3f pos = player.getModel().getWorldTranslation();
        Vector3f clamped = new Vector3f(
            FastMath.clamp(pos.x, Constants.MAP_MIN_X, Constants.MAP_MAX_X),
            pos.y,
            FastMath.clamp(pos.z, Constants.MAP_MIN_Z, Constants.MAP_MAX_Z)
        );

        if (!pos.equals(clamped)) {
            player.getModel().setLocalTranslation(clamped);
            player.getControl().setPhysicsLocation(clamped);
        }
    }

    private void updateCamera(float tpf) {
        currentPlayerPos.set(player.getModel().getWorldTranslation());
        float radians = FastMath.DEG_TO_RAD * Constants.CAMERA_ANGLE_DEGREES;
        float distance = Constants.CAMERA_HEIGHT / FastMath.sin(radians);
        float horizontalOffset = distance * FastMath.cos(radians);

        Vector3f desiredCamPos = new Vector3f(
            currentPlayerPos.x,
            Constants.CAMERA_HEIGHT,
            currentPlayerPos.z + horizontalOffset
        );
        Vector3f newPos = cam.getLocation().interpolateLocal(desiredCamPos, Constants.CAMERA_FOLLOW_SPEED * tpf);

        newPos.x = FastMath.clamp(newPos.x, Constants.MAP_MIN_X, Constants.MAP_MAX_X);
        newPos.z = FastMath.clamp(newPos.z, Constants.MAP_MIN_Z, Constants.MAP_MAX_Z);

        cam.setLocation(newPos);
        cam.lookAt(currentPlayerPos, Vector3f.UNIT_Y);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
        
        app.setDisplayStatView(false);
    }
}