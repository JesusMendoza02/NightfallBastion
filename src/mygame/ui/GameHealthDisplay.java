package mygame.ui;

import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.asset.AssetManager;
import com.jme3.system.AppSettings;
import mygame.entities.Castle;
import mygame.entities.Boss;

public class GameHealthDisplay {
    private final HealthBar castleBar;
    private final HealthBar bossBar;
    private final Castle castle;
    private Boss boss;
    
    // Configuración común
    private static final float BAR_WIDTH = 350f;
    private static final float BAR_HEIGHT = 25f;
    private static final float CASTLE_BAR_Y = 30f;
    private static final float BOSS_BAR_Y = 70f;

    public GameHealthDisplay(Castle castle, Node guiNode, BitmapFont font, 
                           AssetManager assetManager, AppSettings settings) {
        this.castle = castle;
        
        // Reutilizamos el mismo constructor de HealthBar
        castleBar = createHealthBar(assetManager, guiNode, font, "Castillo", 
                                  new ColorRGBA(0.2f, 0.2f, 0.2f, 0.8f),
                                  new ColorRGBA(0, 0.8f, 1f, 1f));
        
        bossBar = createHealthBar(assetManager, guiNode, font, "Boss Final",
                                new ColorRGBA(0.3f, 0f, 0.3f, 0.8f),
                                new ColorRGBA(0.8f, 0f, 0.8f, 1f));
        
        positionBars(settings);
        bossBar.setVisible(false);
    }

    private HealthBar createHealthBar(AssetManager assetManager, Node guiNode, 
                                    BitmapFont font, String label, 
                                    ColorRGBA bgColor, ColorRGBA fgColor) {
        return new HealthBar(assetManager, guiNode, BAR_WIDTH, BAR_HEIGHT, 
                           bgColor, fgColor, font, label);
    }

    private void positionBars(AppSettings settings) {
        float centerX = (settings.getWidth() - BAR_WIDTH) / 2;
        castleBar.setPosition(centerX, CASTLE_BAR_Y);
        bossBar.setPosition(centerX, BOSS_BAR_Y);
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
        bossBar.setVisible(boss != null);
    }

    public void update(float tpf) {
        updateHealthBar(castleBar, castle.getCurrentHealth(), castle.getMaxHealth(), 
                       0.3f, 0.2f, ColorRGBA.Red, 0.5f);
        
        if (boss != null) {
            updateHealthBar(bossBar, boss.getCurrentHealth(), boss.getMaxHealth(), 
                          0.3f, 0.3f, ColorRGBA.Red, 0.7f);
        }
    }

    private void updateHealthBar(HealthBar bar, float current, float max, 
                               float warnThreshold, float pulseThreshold,
                               ColorRGBA pulseColor, float pulseSpeed) {
        float percent = current / max;
        ColorRGBA color = percent > warnThreshold ? 
                         bar.getCurrentColor() : // Mantener color actual si no es necesario cambiarlo
                         new ColorRGBA(1f, 0.2f, 0.2f, 1f); // Rojo cuando está bajo
        
        bar.update(current, max, color);
        bar.setPulseEffect(percent < pulseThreshold, pulseColor, pulseSpeed);
    }

    public void cleanup() {
        castleBar.cleanup();
        bossBar.cleanup();
    }
}