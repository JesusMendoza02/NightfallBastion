/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame.utils;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

/**
 *
 * @author Jesus
 */
public class AssetLoader {
    public static Spatial loadModel(AssetManager assetManager, String path) {
        return assetManager.loadModel(path); // Ej: "Models/Characters/KnightCharacter.glb"
    }
}
