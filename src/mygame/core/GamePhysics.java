package mygame.core;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

public class GamePhysics {
    private BulletAppState bulletAppState;
    private Node physicsNode;

    public GamePhysics(SimpleApplication app, AssetManager assetManager, Node rootNode) {
        bulletAppState = new BulletAppState();
        bulletAppState.setDebugEnabled(false);
        app.getStateManager().attach(bulletAppState);
        
        bulletAppState.getPhysicsSpace().setGravity(new com.jme3.math.Vector3f(0, -15f, 0));
        
        physicsNode = new Node("PhysicsNode");
        rootNode.attachChild(physicsNode);
        
        loadPhysicsObjects(assetManager);
    }

    private void loadPhysicsObjects(AssetManager assetManager) {
        try {
            // Solo cargar el mapa ahora
            Spatial mapModel = assetManager.loadModel("Models/Environment/Mapa.j3o");
            if (mapModel != null) {
                mapModel.setLocalScale(1f);
                mapModel.setLocalTranslation(0, 0, 0);
                
                CollisionShape mapShape = CollisionShapeFactory.createMeshShape(mapModel);
                RigidBodyControl mapPhysics = new RigidBodyControl(mapShape, 0);
                
                mapModel.addControl(mapPhysics);
                bulletAppState.getPhysicsSpace().add(mapPhysics);
                physicsNode.attachChild(mapModel);
                
                System.out.println("✅ Mapa cargado y configurado con física");
            } else {
                System.out.println("❌ No se pudo cargar Models/Environment/Mapa.j3o");
            }
        } catch (Exception e) {
            System.out.println("❌ Error cargando el mapa: " + e.getMessage());
        }

        createInvisibleGround();
    }

    private void createInvisibleGround() {
        try {
            Box groundBox = new Box(50f, 0.1f, 50f);
            Geometry groundGeom = new Geometry("InvisibleGround", groundBox);
            
            Material groundMat = new Material(bulletAppState.getApplication().getAssetManager(), 
                "Common/MatDefs/Misc/Unshaded.j3md");
            groundMat.setColor("Color", com.jme3.math.ColorRGBA.Blue);
            groundMat.getAdditionalRenderState().setWireframe(true);
            groundGeom.setMaterial(groundMat);
            
            groundGeom.setLocalTranslation(0, -2f, 0);
            
            CollisionShape groundShape = CollisionShapeFactory.createBoxShape(groundGeom);
            RigidBodyControl groundPhysics = new RigidBodyControl(groundShape, 0);
            
            groundGeom.addControl(groundPhysics);
            bulletAppState.getPhysicsSpace().add(groundPhysics);
            
            groundGeom.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            
            physicsNode.attachChild(groundGeom);
            
            System.out.println("✅ Suelo de emergencia creado");
        } catch (Exception e) {
            System.out.println("❌ Error creando suelo de emergencia: " + e.getMessage());
        }
    }

    public void cleanup() {
        if (bulletAppState != null) {
            bulletAppState.getPhysicsSpace().removeAll(physicsNode);
            physicsNode.removeFromParent();
        }
    }

    public void setDebugEnabled(boolean enabled) {
        if (bulletAppState != null) {
            bulletAppState.setDebugEnabled(enabled);
        }
    }

    public BulletAppState getBulletAppState() {
        return bulletAppState;
    }

    public Node getPhysicsNode() {
        return physicsNode;
    }
}