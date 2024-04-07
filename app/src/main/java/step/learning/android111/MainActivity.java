package step.learning.android111;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById( R.id.main_button_game_tic_tac_toe )
                .setOnClickListener( this::onGameTicTacToeButtonClick ) ;
        findViewById( R.id.main_button_calc )
                .setOnClickListener( this::onCalcButtonClick ) ;
        findViewById( R.id.main_button_game )
                .setOnClickListener( this::onGameButtonClick ) ;
    }

    private void onGameTicTacToeButtonClick( View view ) {
        Intent intent = new Intent(
                this.getApplicationContext(),
                GameTicTacToeActivity.class ) ;
        startActivity( intent ) ;
    }
    private void onCalcButtonClick( View view ) {
        Intent intent = new Intent(
                this.getApplicationContext(),
                CalcActivity.class ) ;
        startActivity( intent ) ;
    }
    private void onGameButtonClick( View view ) {
        Intent intent = new Intent(
                this.getApplicationContext(),
                GameActivity.class ) ;
        startActivity( intent ) ;
    }
}