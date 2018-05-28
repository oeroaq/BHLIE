package project.tpi.oroa.bhie.common.global;

import android.content.Context;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.google.ar.core.Anchor;

import java.io.IOException;

import project.tpi.oroa.bhie.common.rendering.ObjectRenderer;

public class ObjectRendering {
    public  Anchor anchor;
    public  String nameObject;
    private final float[] centerVertexOfCube = {0f, 0f, 0f, 1};
    private final float[] vertexResult = new float[4];
    private final float objectHitAreaRadius = 0.2f;
    private ObjectRenderer virtualObject;

    public boolean isTapInObject(MotionEvent tap, ObjectRenderer virtualObject){
        return isMVPMatrixHitMotionEvent(virtualObject.getModelViewProjectionMatrix(),  tap);
    }

    public void setVirtualObject(Context context) throws IOException {
        this.virtualObject = new ObjectRenderer();
        this.virtualObject.createOnGlThread(context, "models/"+ this.nameObject + ".obj", "models/"+ this.nameObject + ".png");
        this.virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);
    }

    public ObjectRenderer getVirtualObject() {
        return virtualObject;
    }

    private boolean isMVPMatrixHitMotionEvent(float[] ModelViewProjectionMatrix, MotionEvent event) {
        if (event == null) {
            return false;
        }
        Matrix.multiplyMV(vertexResult, 0, ModelViewProjectionMatrix, 0, centerVertexOfCube, 0);
        float radius = (GlobalClass.viewWidth / 2) * (objectHitAreaRadius / vertexResult[3]);
        float dx = event.getX() - (GlobalClass.viewWidth / 2) * (1 + vertexResult[0] / vertexResult[3]);
        float dy = event.getY() - (GlobalClass.viewHeight / 2) * (1 - vertexResult[1] / vertexResult[3]);
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < radius;
    }
}
