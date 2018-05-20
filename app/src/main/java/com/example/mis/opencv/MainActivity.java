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
    private CascadeClassifier    mEyeCascadeClassifier = null;
    private CascadeClassifier    mMouthCascadeClassiefier = null;
    private CascadeClassifier    mNoseCascadeClassifier = null;
    private Mat                  mMat = null;
    private MatOfRect            mRect = null;
    private VideoCapture         videoDevice = null;
    private Scalar               mScalar = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mEyeCascadeClassifier = new CascadeClassifier();
                    mEyeCascadeClassifier.load(initAssetFile("haarcascade_eye.xml"));
                    mMouthCascadeClassiefier = new CascadeClassifier();
                    mMouthCascadeClassiefier.load(initAssetFile("haarcascade_smile.xml"));
                    mNoseCascadeClassifier = new CascadeClassifier();
                    mNoseCascadeClassifier.load(initAssetFile("nose.xml"));
                    mScalar = new Scalar(0, 0, 0, 0);

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

        //return inputFrame.rgba();
        /*
        Mat col  = inputFrame.rgba();
        Rect foo = new Rect(new Point(100,100), new Point(200,200));
        Imgproc.rectangle(col, foo.tl(), foo.br(), new Scalar(0, 0, 255), 3);
        return col;
        */

        mRect = new MatOfRect();
        mMat = new Mat();
        videoDevice = new VideoCapture();


        Mat gray = inputFrame.gray();
        Mat col  = inputFrame.rgba();

        mNoseCascadeClassifier.detectMultiScale(
                gray, mRect, 1.1, 2,0,
                new Size(100, 100), new Size(0, 0)
        );
        for (Rect rect : mRect.toArray()) {
            Imgproc.rectangle(col, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(50, 255, 50), 2);
        }

        /*
        for(int x = 0; x < mRect.width(); x++){
            for(int y = 0; y < mRect.height(); y++){
                mRect
            }
        }
        */


//        Mat tmp = gray.clone();
        // Imgproc.Canny(gray, tmp, 30, 100);
//        Imgproc.cvtColor(tmp, col, Imgproc.COLOR_GRAY2RGBA, 4);

//        videoDevice.read(mMat);
//        mEyeCascadeClassifier.detectMultiScale(mMat, mRect);
//        for (Rect rect : mRect.toArray()) {
//            Imgproc.rectangle(mMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), mScalar, 3);
//        }
//
//
//        Mat tmp = gray.clone();
//        Imgproc.Canny(gray, tmp, 30, 100);
//        Imgproc.cvtColor(tmp, col, Imgproc.COLOR_GRAY2RGBA, 4);

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
