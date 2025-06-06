package mygame.ui;

import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import mygame.core.Main;

public class VictoryState extends BaseAppState {
    private Main main;
    private BitmapText victoryText;
    private BitmapText restartText;
    private boolean initialized = false;

    public VictoryState(Main main) {
        this.main = main;
    }

    @Override
    protected void initialize(com.jme3.app.Application app) {
        try {
            BitmapFont font = main.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
            
            victoryText = new BitmapText(font, false);
            victoryText.setSize(font.getCharSet().getRenderedSize() * 3);
            victoryText.setColor(ColorRGBA.Green);
            victoryText.setText("VICTORY!");
            
            restartText = new BitmapText(font, false);
            restartText.setSize(font.getCharSet().getRenderedSize() * 1.5f);
            restartText.setColor(ColorRGBA.White);
            restartText.setText("Presiona R para jugar de nuevo");
            
            updateTextPositions();
            initialized = true;
        } catch (Exception e) {
            System.err.println("Error initializing VictoryState: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTextPositions() {
        try {
            if (main.getContext() == null || main.getContext().getSettings() == null) {
                // Valores por defecto si no hay configuración
                int width = 800;
                int height = 600;
                
                if (victoryText != null) {
                    victoryText.setLocalTranslation(
                        width / 2 - victoryText.getLineWidth() / 2,
                        height / 2 + 50,
                        0
                    );
                }
                
                if (restartText != null) {
                    restartText.setLocalTranslation(
                        width / 2 - restartText.getLineWidth() / 2,
                        height / 2 - 50,
                        0
                    );
                }
                return;
            }
            
            int width = main.getContext().getSettings().getWidth();
            int height = main.getContext().getSettings().getHeight();
            
            if (victoryText != null) {
                victoryText.setLocalTranslation(
                    width / 2 - victoryText.getLineWidth() / 2,
                    height / 2 + 50,
                    0
                );
            }
            
            if (restartText != null) {
                restartText.setLocalTranslation(
                    width / 2 - restartText.getLineWidth() / 2,
                    height / 2 - 50,
                    0
                );
            }
        } catch (Exception e) {
            System.err.println("Error updating text positions: " + e.getMessage());
        }
    }

    @Override
    protected void cleanup(com.jme3.app.Application app) {
        try {
            if (victoryText != null && main.getGuiNode().hasChild(victoryText)) {
                main.getGuiNode().detachChild(victoryText);
            }
            if (restartText != null && main.getGuiNode().hasChild(restartText)) {
                main.getGuiNode().detachChild(restartText);
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    @Override
    protected void onEnable() {
        if (!initialized) return;
        
        try {
            // Configurar input para reinicio
            main.getInputManager().addMapping("Restart", new KeyTrigger(KeyInput.KEY_R));
            main.getInputManager().addListener(actionListener, "Restart");
            
            // Mostrar textos
            if (victoryText != null && !main.getGuiNode().hasChild(victoryText)) {
                main.getGuiNode().attachChild(victoryText);
            }
            if (restartText != null && !main.getGuiNode().hasChild(restartText)) {
                main.getGuiNode().attachChild(restartText);
            }
            
            // Mostrar cursor
            main.getInputManager().setCursorVisible(true);
            
            updateTextPositions();
        } catch (Exception e) {
            System.err.println("Error enabling VictoryState: " + e.getMessage());
        }
    }

    @Override
    protected void onDisable() {
        try {
            // Limpiar input
            if (main.getInputManager().hasMapping("Restart")) {
                main.getInputManager().deleteMapping("Restart");
            }
            main.getInputManager().removeListener(actionListener);
            
            // Ocultar textos
            if (victoryText != null && main.getGuiNode().hasChild(victoryText)) {
                main.getGuiNode().detachChild(victoryText);
            }
            if (restartText != null && main.getGuiNode().hasChild(restartText)) {
                main.getGuiNode().detachChild(restartText);
            }
            
            // Ocultar cursor
            main.getInputManager().setCursorVisible(false);
        } catch (Exception e) {
            System.err.println("Error disabling VictoryState: " + e.getMessage());
        }
    }

    private final ActionListener actionListener = (name, isPressed, tpf) -> {
        if (name.equals("Restart") && isPressed) {
            try {
                main.restartGame();
            } catch (Exception e) {
                System.err.println("Error restarting game: " + e.getMessage());
                e.printStackTrace();
            }
        }
    };

    @Override
    public void update(float tpf) {
        if (!initialized) return;
        
        // Actualizar posiciones si cambia el tamaño de la ventana
        if (victoryText != null && restartText != null) {
            updateTextPositions();
        }
    }
}