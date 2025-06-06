package mygame.systems;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.math.ColorRGBA;

public class Path {
    private final Vector3f[] waypoints;

    public Path(Vector3f[] waypoints) {
        this.waypoints = waypoints;
    }

    public Vector3f[] getWaypoints() {
        return waypoints;
    }

    public void debugDraw(AssetManager assetManager, Node rootNode) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);

        for (int i = 0; i < waypoints.length - 1; i++) {
            Line line = new Line(waypoints[i], waypoints[i + 1]);
            Geometry lineGeo = new Geometry("PathLine_" + i, line);
            lineGeo.setMaterial(mat);
            rootNode.attachChild(lineGeo);
        }
    }

    public float getTotalLength() {
        float length = 0;
        for (int i = 0; i < waypoints.length - 1; i++) {
            length += waypoints[i].distance(waypoints[i + 1]);
        }
        return length;
    }

    public Vector3f getPositionAlongPath(float t) {
        if (waypoints.length == 0) return Vector3f.ZERO;
        if (waypoints.length == 1) return waypoints[0];

        float totalLength = getTotalLength();
        float targetLength = t * totalLength;
        float accumulatedLength = 0;

        for (int i = 0; i < waypoints.length - 1; i++) {
            Vector3f start = waypoints[i];
            Vector3f end = waypoints[i + 1];
            float segmentLength = start.distance(end);

            if (accumulatedLength + segmentLength >= targetLength) {
                float segmentT = (targetLength - accumulatedLength) / segmentLength;
                // Corregido a interpolateLocal para no crear objetos nuevos
                return new Vector3f(start).interpolateLocal(end, segmentT);
            }

            accumulatedLength += segmentLength;
        }

        return waypoints[waypoints.length - 1];
    }
}
