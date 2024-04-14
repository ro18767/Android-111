package step.learning.android111;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameTicTacToeActivity extends AppCompatActivity {

    private static final String p0 = "";
    private static final String p1 = "O";
    private static final String p2 = "X";

    private boolean player_1_move = true;

    private int fieldWidth = 3;
    private int fieldHeight = 3;

    private Button[][] cells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_tic_tac_toe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initField();
    }

    private void initField() {
        cells = new Button[fieldHeight][fieldWidth];

        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                String buttonId = "game_tic_tac_toe_cell_btn_" + (fieldWidth * y + x);
                Button cell = findViewById(
                        getResources()   // R
                                .getIdentifier(
                                        buttonId,
                                        "id",
                                        getPackageName()
                                )
                );
                cell.setOnClickListener(this::cellClick);
                cells[y][x] = cell;
            }
        }
    }

    private void cellClick(View view) {
        Button cell = (Button) view;
        String text = cell.getText().toString();
        if (!text.equals(p0)) {
            return;
        }
        if (player_1_move) {
            cell.setText(p1);
        } else {
            cell.setText(p2);
        }
        player_1_move = !player_1_move;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);


        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                outState.putCharSequence("cellData[" + y + "][" + x + "]", cells[y][x].getText());
            }
        }


        outState.putBoolean("player_1_move", player_1_move);

    }

    @Override  // активність відновлюється, відтворюємо дані
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);


        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                cells[y][x].setText(savedInstanceState.getCharSequence("cellData[" + y + "][" + x + "]"));
            }
        }


        player_1_move = savedInstanceState.getBoolean("player_1_move");
    }
}

