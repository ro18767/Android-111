package step.learning.android111;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import step.learning.android111.orm.NbuRate;

public class RatesActivity extends AppCompatActivity {
    private TextView tvContent;
    private LinearLayout ratesContainer;
    private static final byte[] buffer = new byte[2048];
    private List<NbuRate> rates ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rates);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvContent = findViewById(R.id.rates_tv_date);
        ratesContainer = findViewById(R.id.rates_container);
        new Thread( this::loadRates ).start();
    }

    private void loadRates() {
        String url = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
        try( InputStream urlStream = new URL(url).openStream() ) {
            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            int len;
            while( (len = urlStream.read(buffer)) != -1 ) {
                byteBuilder.write(buffer, 0, len);
            }
            String str = new String( byteBuilder.toByteArray(), StandardCharsets.UTF_8 ) ;
            rates = mapJson( str ) ;
            runOnUiThread( this::showRates );
        }
        catch (MalformedURLException ex) {
            Log.d("loadRates", "MalformedURLException");
        }
        catch (IOException ex) {
            Log.d("loadRates", "IOException" + ex.getMessage());
        }
        catch (android.os.NetworkOnMainThreadException ex) {
            Log.d("loadRates", "NetworkOnMainThreadException" + ex.getMessage());
        }
    }

    private void showRates() {
        tvContent.setText( rates.get(0).getExchangeDate() );
        Drawable rateBg = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.rate_bg
        );
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(5, 10, 5, 10 );

        for( NbuRate rate : rates ) {
            TextView tv = new TextView(this);
            tv.setText( rate.getTxt() );
            tv.setBackground( rateBg );
            tv.setLayoutParams(layoutParams);
            tv.setPadding(15, 7, 15, 7);
            tv.setTag( rate );   // Tag - поле для даних користувача (додаткових даних)
            tv.setOnClickListener( this::rateClick );
            ratesContainer.addView( tv );
        }
    }

    private void rateClick(View view) {
        NbuRate rate = (NbuRate) view.getTag() ;
        // Toast.makeText(this, rate.getRate() + "", Toast.LENGTH_SHORT).show();
        // Вивести повні дані про курс (txt, r030, ...) у форми Alert-діалога
        new AlertDialog.Builder(   // формування модального діалогу за паттерном "Builder"
                RatesActivity.this,
                androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert   // Widget_AppCompat_ButtonBar_AlertDialog
        )
                .setTitle(rate.getTxt())
                .setMessage(String.format( Locale.UK, "%s (%d): %f ₴",
                        rate.getCc(), rate.getR030(), rate.getRate() ))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Закрити", null)
                .show();
    }

    private List<NbuRate> mapJson(String jsonString) {
        try {
            // org.json - входить до складу Андроїд
            // при роботі з JSON важливо знати структуру даних -
            // у даному разі це масив об'єктів
            JSONArray array = new JSONArray( jsonString ) ;
            List<NbuRate> result = new ArrayList<>();
            for (int i = 0; i < array.length(); ++i) {
                result.add( NbuRate.fromJson( array.getJSONObject(i) ) ) ;
            }
            return result;
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
/*
Робота з мережею Інтернет.
1. Для роботи необхідно вказати дозвіл (у файлі-маніфесті)
    <uses-permission android:name="android.permission.INTERNET"/>
2. Основу передачі даних становить URL. Аналогічно до файлів створення
    об'єкту new URL(url) не призводить до мережної активності, для
    утворення комунікації слід відкрити або з'єднання або потік даних.
3. Правилами ОС Андроїд заборонено відкривати мережні з'єднання
    з основного потоку. Це спричинює android.os.NetworkOnMainThreadException
4. З іншого боку, заборонено звернення до UI-елементів з інших потоків
    android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
--------------------
5. ORM - відображення одержаних даних на об'єкти мови програмування

Д.З. Реалізувати відображення курсів валют у стилі "чат" - одні по лівому,
інші по правому краю. Змінюються кути форми та її колір.
 */