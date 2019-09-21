package project.tpi.oroa.bhie;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import project.tpi.oroa.bhie.common.global.ObjectRendering;
import project.tpi.oroa.bhie.common.rendering.BackgroundRenderer;
import project.tpi.oroa.bhie.common.rendering.PlaneRenderer;
import project.tpi.oroa.bhie.common.rendering.PointCloudRenderer;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import project.tpi.oroa.bhie.common.global.GlobalClass;
import project.tpi.oroa.bhie.common.helpers.RotationGestureDetectorHelper;
import project.tpi.oroa.bhie.common.helpers.DisplayRotationHelper;
import project.tpi.oroa.bhie.common.helpers.ScaleGesturesHelper;
import project.tpi.oroa.bhie.common.helpers.CameraPermissionHelper;

import java.util.concurrent.BlockingQueue;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3d model of the Android robot.
 */
public class MainActivity extends Activity implements GLSurfaceView.Renderer, RotationGestureDetectorHelper.OnRotationGestureListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;

    private boolean installRequested;

    private Session session;
    private GestureDetector gestureDetector;
    private Snackbar messageSnackbar;
    private DisplayRotationHelper displayRotationHelper;

    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloud = new PointCloudRenderer();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];
    private final int maxObjects = 1;
    private int objectNumber = 0;
    private int scorePositive = 0;
    private int scoreNegative = 0;
    // Tap handling and UI.
    private final BlockingQueue<MotionEvent> queuedSingleTaps = new ArrayBlockingQueue<>(maxObjects);
    private final ArrayList<ObjectRendering> anchors = new ArrayList<>();
    private RotationGestureDetectorHelper mRotationDetector;
    private int mPtrCount = 0;
    private MotionEvent motionEvent;
    private boolean isObjReplaced;
    private ScaleGesturesHelper scaleGestureDetector;


    private TextView scorePositiveText;
    private TextView scoreNegativeText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scorePositiveText = findViewById(R.id.scorePositiveText);
        scoreNegativeText = findViewById(R.id.scoreNegativeText);
        final ImageButton button = findViewById(R.id.MenuLevel);
        surfaceView = findViewById(R.id.surfaceview);
        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);
        mRotationDetector = new RotationGestureDetectorHelper(this);
        // Set up tap listener.
        gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        queuedSingleTaps.offer(e);
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (GlobalClass.scaleFactor < GlobalClass.maxScaleFctor)
                            GlobalClass.scaleFactor += GlobalClass.scaleFactor;
                        else
                            GlobalClass.scaleFactor = GlobalClass.minEscaleFactor;
                        return true;
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        if (mPtrCount < 2) {
                            queuedSingleTaps.offer(motionEvent);
                            return true;
                        } else
                            return false;
                    }
                });

        scaleGestureDetector = new ScaleGesturesHelper(this);

        surfaceView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        scaleGestureDetector.SetView(v);
                        int action = (event.getAction() & MotionEvent.ACTION_MASK);
                        switch (action) {
                            case MotionEvent.ACTION_POINTER_DOWN:
                                mPtrCount++;
                                break;
                            case MotionEvent.ACTION_POINTER_UP:
                                mPtrCount--;
                                break;
                            case MotionEvent.ACTION_DOWN:
                                mPtrCount++;
                                break;
                            case MotionEvent.ACTION_UP:
                                mPtrCount--;
                                break;
                        }
                        motionEvent = event;
                        if (!gestureDetector.onTouchEvent(event))
                            if (!mRotationDetector.onTouchEvent(event))
                                scaleGestureDetector.onTouch(v, event);

                        return true;
                    }
                });

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        installRequested = false;
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            if (session == null) {
                Exception exception = null;
                String message = null;
                try {
                    switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                        case INSTALL_REQUESTED:
                            installRequested = true;
                            return;
                        case INSTALLED:
                            break;
                    }

                    // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                    // permission on Android M and above, now is a good time to ask the user for it.
                    if (!CameraPermissionHelper.hasCameraPermission(this)) {
                        CameraPermissionHelper.requestCameraPermission(this);
                        return;
                    }

                    session = new Session(/* context= */ this);
                } catch (UnavailableArcoreNotInstalledException
                        | UnavailableUserDeclinedInstallationException e) {
                    message = "Please install ARCore";
                    exception = e;
                } catch (UnavailableApkTooOldException e) {
                    message = "Please update ARCore";
                    exception = e;
                } catch (UnavailableSdkTooOldException e) {
                    message = "Please update this app";
                    exception = e;
                } catch (Exception e) {
                    message = "This device does not support AR";
                    exception = e;
                }

                if (message != null) {
                    showSnackbarMessage(message, true);
                    Log.e(TAG, "Exception creating session", exception);
                    return;
                }

                // Create default config and check if supported.
                Config config = new Config(session);
                if (!session.isSupported(config)) {
                    showSnackbarMessage("This device does not support AR", true);
                }
                session.configure(config);
            }

            showLoadingMessage();
            // Note that order matters - see the note in onPause(), the reverse applies here.

            session.resume();
            surfaceView.onResume();
            displayRotationHelper.onResume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onSingleTap(MotionEvent e) {
        // Queue tap if there is space. Tap is lost if queue is full.
        queuedSingleTaps.offer(e);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        try {
            backgroundRenderer.createOnGlThread(this);
            planeRenderer.createOnGlThread(this, "models/trigrid.png");
            pointCloud.createOnGlThread(this);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
        GlobalClass.viewHeight = height;
        GlobalClass.viewWidth = width;
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.

            MotionEvent tap = queuedSingleTaps.poll();
            if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
                for (HitResult hit : frame.hitTest(tap)) {
                    Trackable trackable = hit.getTrackable();
                    if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                            || (trackable instanceof Point
                            && ((Point) trackable).getOrientationMode()
                            == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                        if (anchors.size() < maxObjects) {
                            Anchor anchor = hit.createAnchor();
                            ObjectRendering object = new ObjectRendering();
                            object.nameObject = "word_" + objectNumber;
                            object.numberObject = objectNumber;
                            if (objectNumber == 0) {
                                object.nameObject = "andy";
                            }
                            object.anchor = anchor;
                            object.setVirtualObject(this);
                            anchors.add(object);
                            objectNumber++;

                        } else {
                            final MediaPlayer mp = MediaPlayer.create(this, R.raw.hola);
                            for (ObjectRendering object : anchors) {
                                Log.d("myTag", "numero es "+ object.numberObject);
                                if (object.numberObject == 0 && object.isTapInObject(tap) ) {
                                    Log.d("myTag", "Tocando a la muñeca sin if");
                                    mp.start();
                                }
                                else if (object.numberObject != 0) {
                                    if (object.isTapInObject(tap)) {
                                        if (object.numberObject % 2 != 0)
                                            scorePositive++;
                                        else
                                            scoreNegative++;

                                        final String positivo = "Palabras correctas: " + scorePositive;
                                        final String negativo = "Palabras incorrectas: " + scoreNegative;
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                scorePositiveText.setText(positivo);
                                                scoreNegativeText.setText(negativo);
                                            }
                                        });
                                    }
                                }
                            }

                        }

                        break;
                    }
                }
            }

            // Draw background.
            backgroundRenderer.draw(frame);

            // If not tracking, don't draw 3d objects.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            final float[] lightIntensity = new float[4];
            frame.getLightEstimate().getColorCorrection(lightIntensity, 0);

            // Visualize tracked points.
            PointCloud pointCloud = frame.acquirePointCloud();
            this.pointCloud.update(pointCloud);
            this.pointCloud.draw(viewmtx, projmtx);

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release();
            Collection<Plane> planes = session.getAllTrackables(Plane.class);
            // Check if we detected at least one plane. If so, hide the loading message.
            if (messageSnackbar != null) {
                for (Plane plane : planes) {
                    if (plane.getType() == com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING
                            && plane.getTrackingState() == TrackingState.TRACKING) {
                        hideLoadingMessage();
                        break;
                    }
                }
            }

            // Visualize planes.
            planeRenderer.drawPlanes(
                    planes, camera.getDisplayOrientedPose(), projmtx);

            for (ObjectRendering object : anchors) {
                if (object.anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                object.anchor.getPose().toMatrix(anchorMatrix, 0);

                // Update and draw the model and its shadow.
                float scale = GlobalClass.scaleFactor;
                if (object.numberObject == 0)
                    scale = 0.0009f;

                object.getVirtualObject().updateModelMatrix(anchorMatrix, scale);
                object.getVirtualObject().draw(viewmtx, projmtx, lightIntensity);


            }


        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    private void showSnackbarMessage(String message, boolean finishOnDismiss) {
        messageSnackbar =
                Snackbar.make(
                        MainActivity.this.findViewById(android.R.id.content),
                        message,
                        Snackbar.LENGTH_INDEFINITE);
        messageSnackbar.getView().setBackgroundColor(0xbf323232);
        if (finishOnDismiss) {
            messageSnackbar.setAction(
                    "Dismiss",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            messageSnackbar.dismiss();
                        }
                    });
            messageSnackbar.addCallback(
                    new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            finish();
                        }
                    });
        }
        messageSnackbar.show();
    }

    private void showLoadingMessage() {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        showSnackbarMessage("Buscando superficies...", false);
                    }
                });
    }

    private void hideLoadingMessage() {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (messageSnackbar != null) {
                            messageSnackbar.dismiss();
                        }
                        messageSnackbar = null;
                    }
                });
    }


    @Override
    public void OnRotation(RotationGestureDetectorHelper rotationDetector) {
        float angle = rotationDetector.getAngle();
        GlobalClass.rotateF = GlobalClass.rotateF + angle / 10;
    }

    void onClickMenu(View v) {
        anchors.clear();
        objectNumber = 0;
        scorePositive = 0;
        scoreNegative = 0;
    }
}
