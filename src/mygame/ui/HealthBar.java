package mygame.ui;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;

public class HealthBar {
    private final Geometry background;
    private final Geometry foreground;
    private final BitmapText labelText;
    private final BitmapText valueText;
    private final float maxWidth;
    private final float height;
    private final Node rootNode;
    private final Material fgMaterial;
    private final AssetManager assetManager;
    private ColorRGBA currentColor; // Almacena el color actual
    
    private boolean pulseActive = false;
    private float pulseTimer = 0f;
    private ColorRGBA pulseColor;
    private float pulseSpeed;

    public HealthBar(AssetManager assetManager, Node parentNode, 
                   float maxWidth, float height, 
                   ColorRGBA bgColor, ColorRGBA fgColor,
                   BitmapFont font, String label) {
        
        this.assetManager = assetManager;
        this.maxWidth = maxWidth;
        this.height = height;
        this.currentColor = fgColor; // Inicializa con el color frontal
        this.rootNode = new Node("HealthBar_" + label);
        parentNode.attachChild(rootNode);

        // Crear fondo
        Quad bgQuad = new Quad(maxWidth, height);
        background = new Geometry("Background", bgQuad);
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", bgColor);
        background.setMaterial(bgMat);
        background.setLocalTranslation(0, 0, 0);

        // Crear barra de vida frontal
        Quad fgQuad = new Quad(maxWidth, height);
        foreground = new Geometry("Foreground", fgQuad);
        fgMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fgMaterial.setColor("Color", fgColor);
        foreground.setMaterial(fgMaterial);
        foreground.setLocalTranslation(0, 0, 1);

        // Crear texto de la etiqueta
        labelText = new BitmapText(font, false);
        labelText.setSize(height * 0.8f);
        labelText.setText(label);
        labelText.setLocalTranslation(-font.getCharSet().getRenderedSize() * label.length() - 10, height * 0.1f, 2);

        // Crear texto del valor
        valueText = new BitmapText(font, false);
        valueText.setSize(height * 0.8f);
        valueText.setLocalTranslation(maxWidth + 10, height * 0.1f, 2);

        // Añadir componentes al nodo raíz
        rootNode.attachChild(background);
        rootNode.attachChild(foreground);
        rootNode.attachChild(labelText);
        rootNode.attachChild(valueText);
    }

    public void update(float currentValue, float maxValue, ColorRGBA color) {
        float percentage = Math.max(0, Math.min(1, currentValue / maxValue));
        
        // Actualizar tamaño de la barra
        Quad newQuad = new Quad(maxWidth * percentage, height);
        foreground.setMesh(newQuad);
        
        // Actualizar color (a menos que esté pulsando)
        if (!pulseActive) {
            this.currentColor = color;
            fgMaterial.setColor("Color", color);
        }
        
        // Actualizar texto
        valueText.setText(String.format("%.0f/%.0f", currentValue, maxValue));
        
        // Actualizar efecto de pulso
        if (pulseActive) {
            updatePulseEffect();
        }
    }

    private void updatePulseEffect() {
        pulseTimer += pulseSpeed;
        float pulseValue = (float) (Math.sin(pulseTimer) * 0.5 + 0.5);
        ColorRGBA newColor = currentColor.clone();
        newColor.interpolateLocal(pulseColor, pulseValue);
        fgMaterial.setColor("Color", newColor);
    }

    public void setPosition(float x, float y) {
        rootNode.setLocalTranslation(x, y, 0);
    }

    public void setVisible(boolean visible) {
        rootNode.setCullHint(visible ? Spatial.CullHint.Dynamic : Spatial.CullHint.Always);
    }

    public void setPulseEffect(boolean active, ColorRGBA pulseColor, float pulseSpeed) {
        this.pulseActive = active;
        this.pulseColor = pulseColor;
        this.pulseSpeed = pulseSpeed;
        if (!active) {
            pulseTimer = 0f;
            fgMaterial.setColor("Color", currentColor); // Restaurar color original
        }
    }

    public void setPulseEffect(boolean active) {
        this.pulseActive = active;
        if (!active) {
            pulseTimer = 0f;
            fgMaterial.setColor("Color", currentColor); // Restaurar color original
        }
    }

    public void cleanup() {
        rootNode.removeFromParent();
    }
    
    public ColorRGBA getCurrentColor() {
        return currentColor;
    }
}