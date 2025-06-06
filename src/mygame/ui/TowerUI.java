package mygame.ui;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import mygame.systems.TowerManager;
public class TowerUI {
    private final Node guiNode;
    private final BitmapText instructionsText;
    private final BitmapText placementText;
    private final BitmapText controlsText;
    private boolean canPlace = false;
    private Vector3f placementPoint = null;
    private TowerManager towerManager;
    
    public TowerUI(Node guiNode, BitmapFont font, AppSettings settings, TowerManager towerManager) {
        this.guiNode = guiNode;
        this.towerManager = towerManager;
        
        // Texto de instrucciones principales
        instructionsText = new BitmapText(font, false);
        instructionsText.setSize(font.getCharSet().getRenderedSize() * 0.8f);
        instructionsText.setColor(ColorRGBA.White);
        instructionsText.setLocalTranslation(10, settings.getHeight() - 10, 0);
        guiNode.attachChild(instructionsText);
        
        // Texto de colocación de torres
        placementText = new BitmapText(font, false);
        placementText.setSize(font.getCharSet().getRenderedSize() * 1.2f);
        placementText.setColor(ColorRGBA.Green);
        placementText.setLocalTranslation(10, settings.getHeight() - 80, 0);
        guiNode.attachChild(placementText);
        
        // Texto de controles
        controlsText = new BitmapText(font, false);
        controlsText.setSize(font.getCharSet().getRenderedSize() * 0.7f);
        controlsText.setColor(ColorRGBA.Yellow);
        controlsText.setText("Controles:\n" +
                           "WASD - Mover\n" +
                           "Z - Atacar\n" +
                           "1 - Torre de Ataque\n" +
                           "2 - Torre de Ralentización");
        controlsText.setLocalTranslation(10, 150, 0);
        guiNode.attachChild(controlsText);
        
        updateInstructions();
    }
    
    public void update(boolean canPlace, Vector3f placementPoint) {
        this.canPlace = canPlace;
        this.placementPoint = placementPoint;
        updateInstructions();
    }
    
    private void updateInstructions() {
        if (canPlace && placementPoint != null) {
            placementText.setText("¡Puedes colocar una torre aquí!\n" +
                                "Presiona 1 para Torre de Ataque\n" +
                                "Presiona 2 para Torre de Ralentización");
            placementText.setColor(ColorRGBA.Green);

            instructionsText.setText("Torres disponibles:\n" +
                                   "• Torre de Ataque: Daña enemigos\n" +
                                   "• Torre de Ralentización: Reduce velocidad\n" +
                                   "Torres restantes: " + (towerManager.getMaxTotalTowers() - towerManager.getTotalTowersPlaced()));
        } else {
            placementText.setText("No se pueden colocar torres ahora\n" +
                                "Espera al siguiente intervalo entre oleadas");
            placementText.setColor(ColorRGBA.Gray);

            instructionsText.setText("Busca los puntos de construcción\n" +
                                   "cerca de los caminos de enemigos\n" +
                                   "Torres totales: " + towerManager.getTotalTowersPlaced() + "/" + towerManager.getMaxTotalTowers());
        }
    }
    
    public void cleanup() {
        guiNode.detachChild(instructionsText);
        guiNode.detachChild(placementText);
        guiNode.detachChild(controlsText);
    }
}