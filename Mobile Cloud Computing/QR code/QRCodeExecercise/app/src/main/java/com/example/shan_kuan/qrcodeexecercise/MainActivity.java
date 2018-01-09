package com.example.shan_kuan.qrcodeexecercise;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class MainActivity extends AppCompatActivity {
    EditText txtInput;
    Button btnCreateBarcode;
    ImageView imgBarcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtInput = (EditText)findViewById(R.id.txtInput);
        btnCreateBarcode = (Button)findViewById(R.id.btnCreateBarcode);
        imgBarcode = (ImageView) findViewById(R.id.imgBarcode);
        imgBarcode.setImageResource(android.R.color.transparent);
        try {
            btnCreateBarcode.setOnClickListener(new Button.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 try {
                                                     imgBarcode.setImageBitmap(TextToImageEncode(txtInput.getText().toString()));
                                                 } catch (WriterException e) {
                                                     e.printStackTrace();
                                                 }
                                             }
                                         }
            );
        }
        catch (Exception e) {};
    }

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        int QR_width = 300;
        try {
            bitMatrix = new MultiFormatWriter().encode(Value, BarcodeFormat.DATA_MATRIX.QR_CODE, QR_width, QR_width, null);

        } catch (IllegalArgumentException Illegalargumentexception) {
            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];
        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(android.R.color.black):getResources().getColor(android.R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        try {
            bitmap.setPixels(pixels, 0, QR_width, 0, 0, bitMatrixWidth, bitMatrixHeight);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}

