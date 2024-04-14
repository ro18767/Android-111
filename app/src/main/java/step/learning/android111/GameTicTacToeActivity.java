package step.learning.android111;

import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import step.learning.android111.orm.ChatMessage;
import step.learning.android111.services.Http;

public class GameTicTacToeActivity extends AppCompatActivity {

    private Handler handler;

    private static final String CHAT_URL = "https://www.random.org/integers/?num=10&min=0&max=2&col=1&base=10&format=plain&rnd=new&num=2";
    private long period = 1000;
    private static final String p0 = "";
    private static final String p1 = "O";
    private static final String p2 = "X";
    private static float p1Time = 0;
    private static float p2Time = 0;

    private static TextView tvP1Time;
    private static TextView tvP2Time;

    private static int netX = 0;
    private static int netY = 0;

    private boolean player_1_move = true;

    private int fieldWidth = 3;
    private int fieldHeight = 3;

    private Button[][] cells;

    private Button lastMoveCell = null;


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

        tvP1Time = findViewById( R.id.game_tic_tac_toe_player_1_time_tv );
        tvP2Time = findViewById( R.id.game_tic_tac_toe_player_2_time_tv );

        handler = new Handler();

        initField();

        findViewById(R.id.main).setOnTouchListener(
                new OnSwipeListener(getApplicationContext()) {
                    @Override
                    public void onSwipeLeft() {
                        if (lastMoveCell == null) return;

                        lastMoveCell.setText(p0);

                        player_1_move = !player_1_move;


                        lastMoveCell = null;
                    }

                });

        handler.postDelayed( this::update, period ) ;

        new Thread(this::loadChatMessages).start();
    }

    private void loadChatMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        try {
            String data = Http.getString( CHAT_URL );
            Scanner scanner = new Scanner(data);
            if(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                netX = Integer.parseInt(line);
            }
            if(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                netY = Integer.parseInt(line);
            }
            scanner.close();
        }
        catch (Exception ignore) {}
        runOnUiThread( this::updateMessagesView );
    }

    private void updateMessagesView() {
        cellClick(cells[netY][netX]);
    }
    private void update() {


        if (player_1_move) {
            p1Time += (float) period / 1000;
        } else {
            p2Time += (float) period / 1000;
        }

        updateLabels(true);
        handler.postDelayed(this::update, period);
    }

    private void updateLabels(boolean forceUpdate) {
        if (!forceUpdate) return;

        tvP1Time.setText(getString(R.string.game_time_template, (int) p1Time));
        tvP2Time.setText(getString(R.string.game_time_template, (int) p2Time));
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
        lastMoveCell = cell;

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

