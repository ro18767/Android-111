package step.learning.android111;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends AppCompatActivity {

    private int fieldWidth = 15;
    private int fieldHeight = 28;
    private TextView[][] cells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.game_main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.game_main_layout).setOnTouchListener(
                new OnSwipeListener(getApplicationContext()) {
                    @Override
                    public void onSwipeBottom() {
                        Toast.makeText(GameActivity.this, "onSwipeBottom", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSwipeLeft() {
                        Toast.makeText(GameActivity.this, "onSwipeLeft", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSwipeRight() {
                        Toast.makeText(GameActivity.this, "onSwipeRight", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSwipeTop() {
                        Toast.makeText(GameActivity.this, "onSwipeTop", Toast.LENGTH_SHORT).show();
                    }
                });
        initField();
        startGame();
    }

    private void startGame() {
        cells[5][5].setText("A");
        cells[5][5].setBackgroundColor(
                getResources().getColor(
                        R.color.main_calc_button, getTheme()));
    }
    private void initField() {
        cells = new TextView[fieldWidth][fieldHeight];

        TableLayout gameField = findViewById( R.id.game_field );

        TableLayout.LayoutParams rowLayoutParams = new TableLayout.LayoutParams();
        rowLayoutParams.weight = 1;
        rowLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        rowLayoutParams.height = 0;

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.weight = 1;
        layoutParams.setMargins(2,2,2,2);
        layoutParams.height =  ViewGroup.LayoutParams.MATCH_PARENT;

        for (int j = 0; j < fieldHeight; j++) {
            TableRow row = new TableRow(getApplicationContext());
            row.setLayoutParams( rowLayoutParams );
            for (int i = 0; i < fieldWidth; i++) {
                TextView textView = new TextView(getApplicationContext());
                textView.setBackgroundColor(
                        getResources().getColor(
                                R.color.calc_equal_button_color, getTheme()));
                //textView.setText(R.string.calc_btn_0);
                textView.setLayoutParams(layoutParams);
                textView.setWidth(0);
                row.addView(textView);
                cells[i][j] = textView;
            }
            gameField.addView(row);
        }
    }
}
/*
Д.З. Реалізувати у грі "хрестики-нолики" скасування останнього ходу
за допомогою жесту "свайп ліворуч"
 */