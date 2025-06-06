package mygame.ui;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import mygame.utils.Constants;

public class WaveCountdownUI {
    private final BitmapText countdownText;
    private final BitmapText waveAnnouncementText;
    private final BitmapText bossWarningText;
    private final BitmapText waveCounterText;
    private final BitmapText towersAvailableText;
    
    private float countdownTimer = 0;
    private boolean active = false;
    private boolean bossWarningActive = false;
    private float bossWarningTimer = 0;
    private float pulseEffect = 0;
    private int currentWave = 0;
    private int totalWaves = 3;
    private int towersAvailable = 0;
    
    public WaveCountdownUI(Node guiNode, BitmapFont font, AppSettings settings) {
        // Texto del contador
        countdownText = new BitmapText(font, false);
        countdownText.setSize(font.getCharSet().getRenderedSize() * 2f);
        countdownText.setColor(new ColorRGBA(1f, 0.8f, 0f, 1f));
        
        // Texto del anuncio de oleada
        waveAnnouncementText = new BitmapText(font, false);
        waveAnnouncementText.setSize(font.getCharSet().getRenderedSize() * 2.5f);
        waveAnnouncementText.setColor(new ColorRGBA(0.9f, 0.1f, 0.1f, 1f));
        
        // Texto de advertencia de boss
        bossWarningText = new BitmapText(font, false);
        bossWarningText.setSize(font.getCharSet().getRenderedSize() * 3f);
        bossWarningText.setColor(new ColorRGBA(0.8f, 0f, 0.8f, 1f));
        bossWarningText.setText("¡BOSS INMINENTE!");
        
        // Texto del contador de oleadas
        waveCounterText = new BitmapText(font, false);
        waveCounterText.setSize(font.getCharSet().getRenderedSize() * 1.5f);
        waveCounterText.setColor(new ColorRGBA(1f, 1f, 1f, 0.8f));
        
        // Texto de torres disponibles
        towersAvailableText = new BitmapText(font, false);
        towersAvailableText.setSize(font.getCharSet().getRenderedSize() * 1.2f);
        towersAvailableText.setColor(new ColorRGBA(0.2f, 0.8f, 0.2f, 0.9f));
        
        guiNode.attachChild(countdownText);
        guiNode.attachChild(waveAnnouncementText);
        guiNode.attachChild(bossWarningText);
        guiNode.attachChild(waveCounterText);
        guiNode.attachChild(towersAvailableText);
        
        setVisible(false);
        bossWarningText.setCullHint(Spatial.CullHint.Always);
        waveCounterText.setCullHint(Spatial.CullHint.Never);
        
        centerTexts(settings);
        updateWaveCounterText();
        updateTowersAvailableText();
    }
    
    private void centerTexts(AppSettings settings) {
        float centerX = settings.getWidth() / 2;
        float announcementY = settings.getHeight() * 0.8f;
        float countdownY = settings.getHeight() * 0.7f;
        float bossY = settings.getHeight() * 0.85f;
        float waveCounterY = settings.getHeight() * 0.05f;
        float towersAvailableY = settings.getHeight() * 0.05f;
        
        waveAnnouncementText.setLocalTranslation(
            centerX - waveAnnouncementText.getLineWidth() / 2,
            announcementY,
            0
        );
        
        countdownText.setLocalTranslation(
            centerX - countdownText.getLineWidth() / 2,
            countdownY,
            0
        );
        
        bossWarningText.setLocalTranslation(
            centerX - bossWarningText.getLineWidth() / 2,
            bossY,
            0
        );
        
        waveCounterText.setLocalTranslation(
            settings.getWidth() - waveCounterText.getLineWidth() - 20,
            waveCounterY,
            0
        );
        
        towersAvailableText.setLocalTranslation(
            20,
            towersAvailableY,
            0
        );
    }
    
    private void updateWaveCounterText() {
        waveCounterText.setText("Oleada: " + currentWave + "/" + totalWaves);
    }
    
    public void setCurrentWave(int wave) {
        this.currentWave = wave;
        updateWaveCounterText();
    }
    
    public void setTowersAvailable(int available) {
        this.towersAvailable = available;
        updateTowersAvailableText();
    }
    
    private void updateTowersAvailableText() {
        if (towersAvailable > 0) {
            towersAvailableText.setText("Puedes colocar " + towersAvailable + " torres más");
            towersAvailableText.setColor(new ColorRGBA(0.2f, 0.8f, 0.2f, 0.9f));
        } else {
            towersAvailableText.setText("Límite de torres alcanzado");
            towersAvailableText.setColor(new ColorRGBA(0.8f, 0.2f, 0.2f, 0.9f));
        }
        
        // Efecto de parpadeo cuando hay torres disponibles
        if (towersAvailable > 0) {
            float alpha = 0.7f + 0.3f * FastMath.sin(pulseEffect * 2f);
            towersAvailableText.setAlpha(alpha);
        } else {
            towersAvailableText.setAlpha(0.9f);
        }
    }
    
    public void startCountdown(int nextWaveIndex, float duration) {
        this.countdownTimer = duration;
        this.active = true;
        
        if (nextWaveIndex == 3) { // Pantalla de boss separada
            waveAnnouncementText.setText("¡BOSS FINAL!");
            waveAnnouncementText.setColor(new ColorRGBA(0.8f, 0f, 0.8f, 1f));
            startBossWarning(Constants.BOSS_WARNING_DURATION);
        } else {
            waveAnnouncementText.setText("Oleada " + (nextWaveIndex+1) + " Iniciando");
            waveAnnouncementText.setColor(new ColorRGBA(0.9f, 0.1f, 0.1f, 1f));
        }
        
        setVisible(true);
    }
    
    public void startBossWarning(float duration) {
        this.bossWarningTimer = duration;
        this.bossWarningActive = true;
        bossWarningText.setCullHint(Spatial.CullHint.Never);
    }
    
    public void update(float tpf, AppSettings settings) {
        pulseEffect += tpf * 5f;
        
        if (bossWarningActive) {
            updateBossWarning(tpf, settings);
        }
        
        if (!active) return;
        
        updateCountdown(tpf, settings);
    }
    
    private void updateBossWarning(float tpf, AppSettings settings) {
        bossWarningTimer -= tpf;
        
        float alpha = 0.7f + 0.3f * FastMath.sin(pulseEffect * 2f);
        bossWarningText.setAlpha(alpha);
        
        float scale = 1f + 0.1f * FastMath.sin(pulseEffect);
        bossWarningText.setSize(bossWarningText.getFont().getCharSet().getRenderedSize() * 3f * scale);
        centerTexts(settings);
        
        if (bossWarningTimer <= 0) {
            bossWarningActive = false;
            bossWarningText.setCullHint(Spatial.CullHint.Always);
        }
    }
    
    private void updateCountdown(float tpf, AppSettings settings) {
        countdownTimer -= tpf;
        
        if (countdownTimer <= 3f) {
            float blinkSpeed = FastMath.clamp(4f - countdownTimer, 1f, 4f);
            float alpha = 0.5f + 0.5f * FastMath.sin(pulseEffect * blinkSpeed);
            countdownText.setAlpha(alpha);
            waveAnnouncementText.setAlpha(alpha);
        } else {
            countdownText.setAlpha(1f);
            waveAnnouncementText.setAlpha(1f);
        }
        
        if (countdownTimer <= 0) {
            active = false;
            setVisible(false);
            return;
        }
        
        countdownText.setText(String.format("%.1f", countdownTimer));
        centerTexts(settings);
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setVisible(boolean visible) {
        countdownText.setCullHint(visible ? Spatial.CullHint.Never : Spatial.CullHint.Always);
        waveAnnouncementText.setCullHint(visible ? Spatial.CullHint.Never : Spatial.CullHint.Always);
    }
    
    public void cleanup() {
        countdownText.removeFromParent();
        waveAnnouncementText.removeFromParent();
        bossWarningText.removeFromParent();
        waveCounterText.removeFromParent();
        towersAvailableText.removeFromParent();
    }
}