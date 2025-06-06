package mygame.ui;

import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import mygame.core.Main;

public class GameOverState extends BaseAppState {
    private Main main;
    private BitmapText gameOverText;
    private BitmapText restartText;
    private boolean initialized = false;

    public GameOverState(Main main) {
        this.main = main;
    }

    @Override
    protected void initialize(com.jme3.app.Application app) {
        try {
            BitmapFont font = main.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
            
            gameOverText = new BitmapText(font, false);
            gameOverText.setSize(font.getCharSet().getRenderedSize() * 3);
            gameOverText.setColor(ColorRGBA.Red);
            gameOverText.setText("GAME OVER");
            
            restartText = new BitmapText(font, false);
            restartText.setSize(font.getCharSet().getRenderedSize() * 1.5f);
            restartText.setColor(ColorRGBA.White);
            restartText.setText("Presiona R para reiniciar");
            
            updateTextPositions();
            initialized = true;
        } catch (Exception e) {
            System.err.println("Error initializing GameOverState: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTextPositions() {
        try {
            if (main.getContext() == null || main.getContext().getSettings() == null) {
                // Usar valores por defecto si no hay configuración disponible
                int width = 800;  // Valor por defecto
                int height = 600; // Valor por defecto
                
                if (gameOverText != null) {
                    gameOverText.setLocalTranslation(
                        width / 2 - gameOverText.getLineWidth() / 2,
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
            
            if (gameOverText != null) {
                gameOverText.setLocalTranslation(
                    width / 2 - gameOverText.getLineWidth() / 2,
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
            if (gameOverText != null && main.getGuiNode().hasChild(gameOverText)) {
                main.getGuiNode().detachChild(gameOverText);
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
            if (gameOverText != null && !main.getGuiNode().hasChild(gameOverText)) {
                main.getGuiNode().attachChild(gameOverText);
            }
            if (restartText != null && !main.getGuiNode().hasChild(restartText)) {
                main.getGuiNode().attachChild(restartText);
            }
            
            // Hacer visible el cursor
            main.getInputManager().setCursorVisible(true);
            
            updateTextPositions();
        } catch (Exception e) {
            System.err.println("Error enabling GameOverState: " + e.getMessage());
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
            if (gameOverText != null && main.getGuiNode().hasChild(gameOverText)) {
                main.getGuiNode().detachChild(gameOverText);
            }
            if (restartText != null && main.getGuiNode().hasChild(restartText)) {
                main.getGuiNode().detachChild(restartText);
            }
            
            // Ocultar cursor
            main.getInputManager().setCursorVisible(false);
        } catch (Exception e) {
            System.err.println("Error disabling GameOverState: " + e.getMessage());
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
        if (gameOverText != null && restartText != null) {
            updateTextPositions();
        }
    }
}