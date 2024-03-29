package step.learning.android111;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory;
    private TextView tvResult;
    private final static int MAX_DIGITS = 10 ;
    private boolean needClear = false;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        for (int i = 0; i < 10; i++) {
            String buttonId = "calc_btn_" + i;
            findViewById(
                    getResources()   // R
                            .getIdentifier(
                                    buttonId,
                                    "id",
                                    getPackageName()
                            )
            ).setOnClickListener( this::digitClick ) ;
        }

        tvHistory = findViewById( R.id.calc_tv_history );
        tvResult  = findViewById( R.id.calc_tv_result  );

        findViewById( R.id.calc_btn_c ).setOnClickListener( this::cClick );
        findViewById( R.id.calc_btn_inverse ).setOnClickListener( this::inverseClick );

        cClick(null);
    }

    private void inverseClick( View view ) {
        needClear = true ;
        String result = tvResult.getText().toString();
        double x ;
        try {
            x = Double.parseDouble(result.replaceAll(
                    getString(R.string.calc_btn_0), "0"));
        }
        catch (Exception ignore) {
            return;
        }
        if( x == 0.0 ) {
            tvResult.setText( R.string.calc_zero_division ) ;
            Toast.makeText(this, R.string.calc_zero_division_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        x = 1.0 / x ;
        String res = x == (int)x ? String.valueOf( (int)x ) : String.valueOf( x ) ;
        if( res.length() > MAX_DIGITS ) {
            res = res.substring(0, MAX_DIGITS);
        }
        res = res.replaceAll("0", getString(R.string.calc_btn_0) ) ;
        tvResult.setText( res ) ;
        res = "1 / " + result + " =" ;
        tvHistory.setText( res ) ;
    }

    private void digitClick( View view ) {
        String result;
        if(needClear) {
            result = getString( R.string.calc_btn_0 );
            tvHistory.setText("");
            needClear = false ;
        }
        else {
            result = tvResult.getText().toString();
        }
        if(result.length() >= MAX_DIGITS) {
            return;
        }
        if( result.equals( getString( R.string.calc_btn_0 ) ) ) {
            result = "";
        }
        result += ((Button)view).getText();
        tvResult.setText( result );
    }

    private void cClick( View view ) {
        tvHistory.setText("");
        tvResult.setText( R.string.calc_btn_0 );
    }

    // Події, що відповідають за зміну конфігурації

    @Override   // onSaveInstanceState - активність руйнується, дані слід зберігати
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // Bundle outState ---> Bundle savedInstanceState
        super.onSaveInstanceState(outState);
        outState.putCharSequence( "savedResult", tvResult.getText() );
        // забезпечити збереження історії та needClear
        outState.putBoolean("needClear", needClear);
    }

    @Override  // активність відновлюється, відтворюємо дані
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText( savedInstanceState.getCharSequence( "savedResult" ) );
        needClear = savedInstanceState.getBoolean("needClear");
    }
}
/*
Д.З. Для гри "хрестики-нолики" реалізувати дизайн для ландшафтної
орієнтації пристрою, забезпечити збереження та відновлення
ігрової ситуації при зміні орієнтації.
 */