package com.example.barcodescanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

   private SurfaceView surfaceView;
   private BarcodeDetector barcodeDetector;
   private CameraSource cameraSource;
   private static final int REQUEST_PERMISSION_CODE=201;
   private ToneGenerator toneGenerator;
   private String barCodeText;
   private TextView barcodetextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toneGenerator= new ToneGenerator(AudioManager.STREAM_MUSIC,100);
        surfaceView=findViewById(R.id.surfaceview);
        barcodetextView=findViewById(R.id.barcode_text);
        initializeDetector();
    }

    private void initializeDetector(){
        barcodeDetector=new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        cameraSource=new CameraSource.Builder(this,barcodeDetector).setRequestedPreviewSize(1920,1080).setAutoFocusEnabled(true).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED)){
                        cameraSource.start(surfaceView.getHolder());
                    }
                    else {
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},201);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes=detections.getDetectedItems();
                if (barcodes.size()!=0){
                    barcodetextView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (barcodes.valueAt(0).email!=null){
                                barcodetextView.removeCallbacks(null);
                                barCodeText=barcodes.valueAt(0).email.address;
                                barcodetextView.setText(barCodeText);
                                toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP,150);
                            }else {
                                barCodeText=barcodes.valueAt(0).displayValue;
                                barcodetextView.setText(barCodeText);
                                toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP,150);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportActionBar().hide();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().hide();
        initializeDetector();
    }
}
