package mygame.utils;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import java.util.Arrays;
import java.util.List;

public class Constants {
    // Límites del mapa para el jugador (ajusta según tu mapa)
    public static final float MAP_MIN_X = -8f;
    public static final float MAP_MAX_X = 8f;
    public static final float MAP_MIN_Z = -8f;
    public static final float MAP_MAX_Z = 10f;
    // Movimiento del jugador
    public static final float PLAYER_MOVE_SPEED = 8.0f;
    public static final float PLAYER_ROTATION_SPEED = 10.0f;

    // Cámara
    public static final float CAMERA_HEIGHT = 8f;
    public static final float CAMERA_FOLLOW_SPEED = 5.0f;
    public static final float CAMERA_ANGLE_DEGREES = 50f;

    // Luz del jugador
    public static final float PLAYER_LIGHT_RADIUS = 18f;
    public static final ColorRGBA PLAYER_LIGHT_COLOR = new ColorRGBA(0.6f, 0.6f, 1f, 1f).mult(0.6f);
    
    //Controaldor de Waves
    public static final float WAVE_START_DELAY = 30f; // Tiempo antes de la primera oleada
    public static final float WAVE_COUNTDOWN_DURATION = 20f; // Tiempo entre oleadas normales
    public static final float BOSS_COUNTDOWN_DURATION = 8f; // Tiempo más corto para el boss
    public static final float BOSS_WARNING_DURATION = 8f; // Duración del aviso de boss
    
    // Controlador de Ataque de jugador 
    public static final float PLAYER_ATTACK_RANGE = 3f;
    public static final float ENEMY_DETECTION_RANGE = 10f;
    
     // Multiplicador de salud para el boss
    public static final int BOSS_HEALTH_MULTIPLIER = 3;
    
    // Configuración de gameplay
    public static final float PLAYER_SPAWN_X = 0.0f;
    public static final float PLAYER_SPAWN_Y = 1.0f;
    public static final float PLAYER_SPAWN_Z = 0.0f;
    
        
    
    // Configuración mejorada de físicas persoanje
    public static final float ATTACK_DURATION = 1.0f; 
    public static final float CAPSULE_RADIUS = 0.02f;
    public static final float CAPSULE_HEIGHT = 0.02f;
    public static final float STEP_HEIGHT = 0.1f;
    public static final float JUMP_SPEED = 8f;
    public static final float FALL_SPEED = 20.0f;
    public static final float GRAVITY = 20.0f;
    
    // Configuración de cámara
    public static final float CAMERA_MIN_HEIGHT = 15.0f;
    public static final float CAMERA_MAX_HEIGHT = 35.0f;
    
    // Configuración de físicas
    public static final float WORLD_GRAVITY = -20.0f;
    
    // Configuración de torres
    public static final float TOWER_PLACEMENT_RANGE = 2.5f;
    public static final float ATTACK_TOWER_RANGE = 3.0f;
    public static final float SLOW_TOWER_RANGE = 2.0f;
    public static final int ATTACK_TOWER_DAMAGE = 50;
    public static final float ATTACK_TOWER_COOLDOWN = 0.4f;
    public static final float SLOW_TOWER_COOLDOWN = 0.2f;
    public static final float SLOW_TOWER_FACTOR = 0.5f; // Reduce velocidad más significativamente
    
    // Puntos de colocación de torres (evitando los caminos de enemigos)
    public static final Vector3f[] TOWER_PLACEMENT_POINTS = {
        // Defensa del camino izquierdo (2 torres)
        new Vector3f(-3f, 0f, 1.2f),      
        new Vector3f(-6f, 0f, 3f),    

        // Defensa del camino derecho (2 torres)
        new Vector3f(2f, 0f, -0f),       
        new Vector3f(3.5f, 0f, -1.8f),     

        // Defensa del camino central (3 torres)
        new Vector3f(0.7f, 0f, -3.5f),     
        new Vector3f(-1f, 0f, -5f),
        new Vector3f(-1f, 0f, -1f),

        // Defensa final cerca del castillo (3 torres) - punto crítico donde convergen
        new Vector3f(-1f, 0f, 7f),      
        new Vector3f(0.7f, 0f, 9f),      
        new Vector3f(0.7f, 0f, 5f), 
    };
    
    
    /**
     * Calcula el tamaño total del mapa en X
     */
    public static float getMapSizeX() {
        return MAP_MAX_X - MAP_MIN_X;
    }
    
    /**
     * Calcula el tamaño total del mapa en Z
     */
    public static float getMapSizeZ() {
        return MAP_MAX_Z - MAP_MIN_Z;
    }
    
    /**
     * Obtiene el centro del mapa en X
     */
    public static float getMapCenterX() {
        return (MAP_MAX_X + MAP_MIN_X) / 2.0f;
    }
    
    /**
     * Obtiene el centro del mapa en Z
     */
    public static float getMapCenterZ() {
        return (MAP_MAX_Z + MAP_MIN_Z) / 2.0f;
    }
    
    
    // Configuración común barar de vida
    public static final float BAR_WIDTH = 350f;
    public static final float BAR_HEIGHT = 25f;
    public static final float CASTLE_BAR_Y = 30f;
    public static final float BOSS_BAR_Y = 70f;
}