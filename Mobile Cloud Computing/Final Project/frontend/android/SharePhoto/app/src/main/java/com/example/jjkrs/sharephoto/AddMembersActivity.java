package com.example.jjkrs.sharephoto;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.acl.Group;
import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import static com.example.jjkrs.sharephoto.MainMenu.key_used;


public class AddMembersActivity extends AppCompatActivity {


    private ImageView barcode;
    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members);
        barcode = (ImageView) findViewById(R.id.imgBarcode);

        if(!key_used) {
            try {

                // Generate QR-code
                Bitmap barcode_bitmap = encodeAsBitmap(GroupManagementActivity.joiner_token,BarcodeFormat.QR_CODE, 800, 600);

                // Add QR-code to imageview
                barcode.setImageBitmap(barcode_bitmap);

                key_used = true;

            } catch (WriterException io) {
                // Couldn't write barcode
                io.printStackTrace();
            }

        } else {

            // One time token already used, generate a new one.

            String string_backend = "https://mcc-fall-2017-g10.appspot.com/token";
            new getToken().execute(string_backend);
        }
    }


    // Barcode creation using zxing library

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }


    private class getToken extends AsyncTask<String, Void, String> {

        // Get group token through HTTP from the backend (in a background thread)

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String result = "";
            try {

                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));


                String line;

                while ((line = in.readLine()) != null) {
                    result += line;
                }
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                // Generate QR-code
                Bitmap barcode_bitmap = encodeAsBitmap(result,BarcodeFormat.QR_CODE, 800, 600);

                // Add QR-code to imageview
                barcode.setImageBitmap(barcode_bitmap);

            } catch (WriterException io) {
                // Couldn't write barcode
                io.printStackTrace();
            }

        }

    }

}

