package com.example.qrcodescannerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText etName, etAddress;
    private Button buttonScan;
    private ImageView qrIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        buttonScan = findViewById(R.id.buttonScan);
        qrIcon = findViewById(R.id.qr_icon);

        etAddress.setAutoLinkMask(Linkify.WEB_URLS);
        etAddress.setMovementMethod(LinkMovementMethod.getInstance());

        buttonScan.setOnClickListener(view -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan a QR Code");
            integrator.setCameraId(0);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(false);
            integrator.initiateScan();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String scannedData = result.getContents();
                Log.d(TAG, "Scanned Data: " + scannedData);
                try {
                    JSONObject jsonObject = new JSONObject(scannedData);
                    etName.setText(jsonObject.optString("title", "Unknown Title"));
                    String website = jsonObject.optString("website", "");
                    etAddress.setText(website);
                    makeLinkClickable(website);
                } catch (JSONException e) {
                    Log.e(TAG, "Invalid QR Code JSON", e);
                    etName.setText("Scanned Data");
                    etAddress.setText(scannedData);
                    makeLinkClickable(scannedData);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean isValidUrl(String url) {
        String urlPattern = "^(http|https)://.*$";
        return Pattern.matches(urlPattern, url);
    }

    private void makeLinkClickable(String data) {
        if (isValidUrl(data)) {
            etAddress.setAutoLinkMask(Linkify.WEB_URLS);
            etAddress.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            Toast.makeText(this, "Scanned content is not a valid URL", Toast.LENGTH_SHORT).show();
        }
    }
}
