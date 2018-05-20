package com.example.mis.opencv;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    private CascadeClassifier    mFaceCascadeClassifier = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                    // HaarCascade Library source
                    // https://github.com/opencv/opencv/tree/3.4.1/data/haarcascades
                    mFaceCascadeClassifier = new CascadeClassifier();
                    mFaceCascadeClassifier.load(initAssetFile("haarcascade_frontalface_default.xml"));
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        MatOfRect mRect = new MatOfRect();
        double mFaceValue = 150.0;
        double faceWidth = 2/3 * mFaceValue;
        double faceHeight = mFaceValue;

        Mat gray = inputFrame.gray();
        Mat col  = inputFrame.rgba();

        mFaceCascadeClassifier.detectMultiScale( gray, mRect, 1.2, 6,0,
                new Size(faceWidth, faceHeight), new Size(0, 0));

        // source face ratio's
        // http://www.artyfactory.com/portraits/pencil-portraits/proportions-of-a-head.html
        for (Rect rect : mRect.toArray()) {
            Imgproc.circle(
                    col,
                    new Point(rect.x + rect.width / 2, rect.y + rect.height / 1.65),
                    rect.width / 7,
                    new Scalar(255, 50, 50),
                    -1
            );
        }

        return col;
    }


    public String initAssetFile(String filename)  {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) try {
            InputStream is = getAssets().open(filename);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data); os.write(data); is.close(); os.close();
        } catch (IOException e) { e.printStackTrace(); }
        Log.d(TAG,"prepared local file: "+filename);
        return file.getAbsolutePath();
    }
}
