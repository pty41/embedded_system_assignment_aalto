package com.example.shan_kuan.visionexercise;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.io.InputStream;

/*
class FaceOverlayView extends View {

    private Bitmap mBitmap;
    private SparseArray<FaceDetector.Face> mFaces;

    public FaceOverlayView(Context context) {
        this(context, null);
    }

    public FaceOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setBitmap( Bitmap bitmap ) {
        mBitmap = bitmap;
    }
}
*/

public class MainActivity extends AppCompatActivity {
    Button btnPickPhoto;
    ImageView imgBarcode;
    TextView txtNumPeople, txtBarcode;
    int REQUEST_IMAGE_LOAD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtBarcode = (TextView) findViewById(R.id.txtBarcode);
        txtNumPeople = (TextView) findViewById(R.id.txtNumPeople);
        btnPickPhoto = (Button) findViewById(R.id.btnPickPhoto);
        imgBarcode = (ImageView) findViewById(R.id.imgBarcode);
        imgBarcode.setImageResource(android.R.color.transparent);
        try {
            btnPickPhoto.setOnClickListener(new Button.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    try {
                                                        Intent intent = new Intent(Intent.ACTION_PICK);
                                                        intent.setType("image/*");
                                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                                        startActivityForResult(Intent.createChooser(intent, "Select Contact Image"), REQUEST_IMAGE_LOAD);
                                                        //startActivityForResult(Intent.createChooser(pickPhoto, "Select Contact Image"), REQUEST_IMAGE_LOAD);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
            );
        } catch (Exception e) {
        }

    }

    private Bitmap getBitmap(Intent data) {
        Uri uri = data.getData();
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 10000000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            int scale = 1;
            while ((options.outWidth * options.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Bitmap resultBitmap = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 2) {
                scale--;
                options = new BitmapFactory.Options();
                options.inSampleSize = scale;
                resultBitmap = BitmapFactory.decodeStream(in, null, options);
                int height = resultBitmap.getHeight();
                int width = resultBitmap.getWidth();
                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(resultBitmap, (int) x,
                        (int) y, true);
                resultBitmap.recycle();
                resultBitmap = scaledBitmap;

                System.gc();
            } else {
                //resultBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
                resultBitmap = BitmapFactory.decodeStream(in);
            }
            in.close();
            resultBitmap.getHeight();
            return resultBitmap;
        } catch (IOException e) {
            return null;
        }
    }
    private Bitmap ARGBBitmap(Bitmap img) {
        return img.copy(Bitmap.Config.ARGB_8888,true);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if(requestCode == REQUEST_IMAGE_LOAD && resultCode == RESULT_OK) {
            //Uri selectedFile = data.getData();

            try {









                BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
                bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                //InputStream stream = getResources().openRawResource( R.raw.face );
                Bitmap myBitmap = getBitmap(data);
                /*
                Bitmap myBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedFile), null,
                                bitmapFatoryOptions);
                /               */
                //Bitmap myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedFile);
                Paint myRectPaint = new Paint();
                myRectPaint.setStrokeWidth(5);
                myRectPaint.setColor(Color.GREEN);
                myRectPaint.setStyle(Paint.Style.STROKE);
                Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
                Canvas tempCanvas = new Canvas(tempBitmap);
                tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                FaceDetector faceDetector = new
                        FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(true)
                        .build();
                /*
                if(!faceDetector.isOperational()){
                    new AlertDialog.Builder(v.getContext()).setMessage("Could not set up the face detector!").show();
                    return;
                }
                */
                Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                SparseArray<Face> faces = faceDetector.detect(frame);
                if (faces.size() == 0) {
                    BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                                    .setBarcodeFormats(Barcode.QR_CODE)
                                    .build();
                    Frame frame2 = new Frame.Builder().setBitmap(ARGBBitmap(myBitmap)).build();
                    //SparseArray barcodes = detector.detect(frame2);
                    SparseArray<Barcode> barcodes = detector.detect(frame2);
                    try {
                        Barcode thisCode = barcodes.valueAt(0);
                        txtBarcode.setText(String.format("Yes"));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        txtBarcode.setText(String.format("No"));
                    }

                }
                else {

                    for (int i = 0; i < faces.size(); i++) {

                        Face thisFace = faces.valueAt(i);
                        float x1 = thisFace.getPosition().x;
                        float y1 = thisFace.getPosition().y;
                        float x2 = x1 + thisFace.getWidth();
                        float y2 = y1 + thisFace.getHeight();
                        tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
                    }
                    txtBarcode.setText(String.format("No"));
                }
                imgBarcode.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
                txtNumPeople.setText(String.format("%d", faces.size()));




                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedFile);
                //imgBarcode.setImageURI(selectedFile);
                //imgBarcode.setImageBitmap(bitmap);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

}

