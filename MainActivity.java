package com.asistent.tirea;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private static final int PERMISSION_REQUEST = 100;
    private static final String[] PERMISSIONS_NECESARE = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        cerePermisiuni();

        // Deseneaza iconita power pe butonul de repornire
        Button btnRestart = findViewById(R.id.btnRestart);
        btnRestart.post(() -> {
            int size = btnRestart.getWidth();
            btnRestart.setCompoundDrawables(null, null, null, null);
            btnRestart.setBackground(new PowerButtonDrawable(size));
        });

        // Buton Apel Normal
        findViewById(R.id.btnNormal).setOnClickListener(v -> {
            if (arePermisiune(Manifest.permission.CALL_PHONE)) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 1);
            } else {
                Toast.makeText(this, "Necesita permisiunea de apel!", Toast.LENGTH_LONG).show();
                cerePermisiuni();
            }
        });

        // Buton WhatsApp
        findViewById(R.id.btnWhatsApp).setOnClickListener(v -> {
            try {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
                if (intent != null) startActivity(intent);
                else Toast.makeText(this, "WhatsApp nu este instalat", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp nu este instalat", Toast.LENGTH_SHORT).show();
            }
        });

        // Buton Facebook
        findViewById(R.id.btnFacebook).setOnClickListener(v -> {
            try {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
                if (intent != null) startActivity(intent);
                else startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com")));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com")));
            }
        });

        // Buton Repornire
        findViewById(R.id.btnRestart).setOnClickListener(v -> {
            new AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle("Repornire telefon")
                .setMessage("Esti sigur ca vrei sa repornesti telefonul?")
                .setPositiveButton("DA", (dialog, which) -> {
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
                    } catch (Exception e) {
                        Toast.makeText(this, "Necesita acces root!", Toast.LENGTH_LONG).show();
                        tvStatus.setText("Eroare: telefon fara root");
                    }
                })
                .setNegativeButton("NU", null)
                .show();
        });
    }

    // Drawable personalizat pentru butonul power
    static class PowerButtonDrawable extends Drawable {
        private final Paint circlePaint;
        private final Paint powerPaint;
        private final int size;

        PowerButtonDrawable(int size) {
            this.size = size;

            circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setStyle(Paint.Style.FILL);

            powerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            powerPaint.setColor(Color.parseColor("#E53935"));
            powerPaint.setStyle(Paint.Style.STROKE);
            powerPaint.setStrokeWidth(size * 0.07f);
            powerPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            float cx = size / 2f;
            float cy = size / 2f;
            float r = size / 2f;

            // Gradient gri metalic
            android.graphics.RadialGradient gradient = new android.graphics.RadialGradient(
                cx * 0.7f, cy * 0.6f, r,
                Color.parseColor("#9E9E9E"),
                Color.parseColor("#5A5A5A"),
                android.graphics.Shader.TileMode.CLAMP
            );
            circlePaint.setShader(gradient);
            canvas.drawCircle(cx, cy, r, circlePaint);

            // Arc rosu (cadran)
            float arcR = size * 0.28f;
            RectF arcRect = new RectF(cx - arcR, cy - arcR, cx + arcR, cy + arcR);
            powerPaint.setStyle(Paint.Style.STROKE);
            canvas.drawArc(arcRect, 225f, 270f, false, powerPaint);

            // Linie verticala rosie
            powerPaint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(cx, cy - arcR - size * 0.04f, cx, cy, powerPaint);
        }

        @Override
        public void setAlpha(int alpha) {}
        @Override
        public void setColorFilter(android.graphics.ColorFilter colorFilter) {}
        @Override
        public int getOpacity() { return android.graphics.PixelFormat.OPAQUE; }
        @Override
        public int getIntrinsicWidth() { return size; }
        @Override
        public int getIntrinsicHeight() { return size; }
    }

    private boolean arePermisiune(String permisiune) {
        return ContextCompat.checkSelfPermission(this, permisiune) == PackageManager.PERMISSION_GRANTED;
    }

    private void cerePermisiuni() {
        List<String> lipsa = new ArrayList<>();
        for (String p : PERMISSIONS_NECESARE) {
            if (!arePermisiune(p)) lipsa.add(p);
        }
        if (!lipsa.isEmpty()) {
            ActivityCompat.requestPermissions(this, lipsa.toArray(new String[0]), PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            List<String> refuzate = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) refuzate.add(permissions[i]);
            }
            if (refuzate.isEmpty()) {
                Toast.makeText(this, "Toate permisiunile acordate!", Toast.LENGTH_LONG).show();
            } else {
                StringBuilder msg = new StringBuilder("Permisiuni refuzate:\n");
                for (String p : refuzate) {
                    if (p.contains("CALL_PHONE")) msg.append("- Apel telefonic\n");
                    if (p.contains("READ_CONTACTS")) msg.append("- Citire contacte\n");
                    if (p.contains("SEND_SMS")) msg.append("- Trimitere SMS\n");
                    if (p.contains("READ_PHONE_STATE")) msg.append("- Stare telefon\n");
                }
                new AlertDialog.Builder(this)
                    .setTitle("Permisiuni necesare")
                    .setMessage(msg.toString())
                    .setPositiveButton("Incearca din nou", (d, w) -> cerePermisiuni())
                    .setNegativeButton("Continua oricum", null)
                    .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String number = cursor.getString(0);
                cursor.close();
                if (arePermisiune(Manifest.permission.CALL_PHONE)) {
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
                }
            }
        }
    }
}
