package step.learning.android111;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private static final Random random = new Random();
    private int fieldWidth = 15;
    private int fieldHeight = 28;
    private long period = 500;
    private TextView[][] cells;
    private Handler handler;
    private final LinkedList<Vector2> snake = new LinkedList<>() ;
    private Vector2 food ;
    private Direction moveDirection;

    private int  cellColorRes;
    private int fieldColorRes;
    private int snakeColorRes;
    private int  foodColorRes;
    private String foodSymbol = new String( Character.toChars(0x1f34e) );
    private boolean isPlaying;
    private float time, bestTime, score, bestScore;
    private TextView tvTime, tvBestTime, tvScore, tvBestScore;
    private final String gameDataFilename = "saves.game";
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
                        if(moveDirection == Direction.left || moveDirection == Direction.right) {
                            moveDirection = Direction.bottom;
                        }
                    }
                    @Override
                    public void onSwipeLeft() {
                        if(moveDirection == Direction.top || moveDirection == Direction.bottom) {
                            moveDirection = Direction.left;
                        }
                    }
                    @Override
                    public void onSwipeRight() {
                        if(moveDirection == Direction.top || moveDirection == Direction.bottom) {
                            moveDirection = Direction.right;
                        }
                    }
                    @Override
                    public void onSwipeTop() {
                        if(moveDirection == Direction.left || moveDirection == Direction.right) {
                            moveDirection = Direction.top;
                        }
                    }
                });
        cellColorRes  = getResources().getColor( R.color.game_cell, getTheme() ) ;
        fieldColorRes = getResources().getColor( R.color.game_background, getTheme() ) ;
        snakeColorRes = getResources().getColor( R.color.game_snake, getTheme() ) ;
        foodColorRes  = getResources().getColor( R.color.game_food, getTheme() ) ;

        tvTime = findViewById( R.id.game_tv_time );
        tvBestTime = findViewById( R.id.game_tv_best_time );
        tvScore = findViewById( R.id.game_tv_score );
        tvBestScore = findViewById( R.id.game_tv_best_score );

        handler = new Handler();
        initField();
        startGame();
    }
    private void loadGameData() {
        try( FileInputStream fis = openFileInput( gameDataFilename ) ) {
            DataInputStream reader = new DataInputStream( fis ) ;
            bestScore = reader.readFloat() ;
            bestTime = reader.readFloat() ;
            reader.close();
            updateLabels(true);
            Log.i("loadGameData", "Loaded " + bestScore + " " + bestTime ) ;
        }
        catch (IOException e) {
            Log.e("loadGameData", e.getMessage() ) ;
        }
    }
    private void saveGameData() {
        /* Файлова система Андроїд "видає" кожному застосунку приватну директорію.
        *  Доступ до неї для програми не обмежений, але можна її видаляти з налаштувань
        *  пристрою. Доступ для інших програм - тільки з правами адміністратора */
        try( FileOutputStream fos = openFileOutput( gameDataFilename, Context.MODE_PRIVATE ) ) {
            DataOutputStream writer = new DataOutputStream( fos ) ;
            writer.writeFloat( bestScore );
            writer.writeFloat( bestTime );
            writer.flush();
            writer.close();
            Log.i("saveGameData", "Saved " + bestScore + " " + bestTime ) ;
        }
        catch (IOException e) {
            Log.e("saveGameData", e.getMessage() ) ;
        }
    }
    private void update() {
        Vector2 newHead = snake.getFirst().copy();

        // перераховуємо нову позицію голови в залежності від напряму руху
        switch (moveDirection){
            case top:    newHead.setY( newHead.getY() - 1 ) ; break;
            case bottom: newHead.setY( newHead.getY() + 1 ) ; break;
            case left:   newHead.setX( newHead.getX() - 1 ) ; break;
            case right:  newHead.setX( newHead.getX() + 1 ) ; break;
        }
        // перевіряємо, що не вийшли за межі поля
        if( newHead.getX() < 0 || newHead.getX() >= fieldWidth
         || newHead.getY() < 0 || newHead.getY() >= fieldHeight ) {
            gameOver();
            return;
        }

        // вставляємо нову голову
        snake.addFirst( newHead );
        // та зарисовуємо комірку поля
        cells[newHead.getX()][newHead.getY()].setBackgroundColor( snakeColorRes );

        // перевіряємо що нова позиція - це їжа
        if(newHead.getX() == food.getX() && newHead.getY() == food.getY()) {
            cells[food.getX()][food.getY()].setText("");
            do {   // перегенеровуємо позицію їжі
                food.setX( random.nextInt( fieldWidth ) );
                food.setY( random.nextInt( fieldHeight ) ) ;
            } while( isInSnake(food) );

            cells[food.getX()][food.getY()].setText(foodSymbol);
            score += 100;
        }
        else {
            Vector2 tail = snake.removeLast();
            // стираємо старий хвіст - зафарбовуємо у колір комірки
            cells[tail.getX()][tail.getY()].setBackgroundColor( cellColorRes );
            score += 1;
        }
        time += (float)period / 1000;
        updateLabels(false);
        if(isPlaying) {
            handler.postDelayed(this::update, period);
        }
    }
    private void updateLabels(boolean forceUpdate) {
        tvScore.setText( String.valueOf( (int)score ) );
        tvTime.setText( getString( R.string.game_time_template, (int)time ) ) ;
        if( time > bestTime ) {
            bestTime = time ;
            forceUpdate = true;
        }
        if( score > bestScore ) {
            bestScore = score;
            forceUpdate = true;
        }
        if( forceUpdate ) {
            tvBestTime.setText( getString( R.string.game_time_template, (int)bestTime ) ) ;
            tvBestScore.setText( String.valueOf( (int)bestScore ) );
        }
    }
    private void startGame() {
        loadGameData();
        // стираємо залишкові асети
        if(food != null) {
            cells[food.getX()][food.getY()].setText("");
        }
        for( Vector2 tail : snake ) {
            cells[tail.getX()][tail.getY()].setBackgroundColor( cellColorRes ) ;
        }
        // Відновлюємо стартову позицію
        food = new Vector2(5,5);
        cells[food.getX()][food.getY()].setText(foodSymbol);
        snake.clear();
        snake.add( new Vector2(5, 15 ) ) ;
        snake.add( new Vector2(5, 16 ) ) ;
        snake.add( new Vector2(5, 17 ) ) ;
        for( Vector2 tail : snake ) {
            cells[tail.getX()][tail.getY()].setBackgroundColor( snakeColorRes ) ;
        }
        isPlaying = true;
        moveDirection = Direction.left;
        handler.postDelayed( this::update, period ) ;
    }
    private void initField() {
        cells = new TextView[fieldWidth][fieldHeight];

        TableLayout gameField = findViewById( R.id.game_field );
        gameField.setBackgroundColor( fieldColorRes ) ;

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
                textView.setBackgroundColor( cellColorRes );
                textView.setGravity(Gravity.CENTER);
                textView.setLayoutParams(layoutParams);
                textView.setWidth(0);
                row.addView(textView);
                cells[i][j] = textView;
            }
            gameField.addView(row);
        }
    }
    private boolean isInSnake(Vector2 vector) {
        return snake.stream().anyMatch(v -> v.getX() == vector.getX() && v.getY() == vector.getY());
    }
    private void gameOver() {
        new AlertDialog.Builder(   // формування модального діалогу за паттерном "Builder"
                GameActivity.this,
                androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert   // Widget_AppCompat_ButtonBar_AlertDialog
                )
                .setTitle(R.string.game_final_title)
                .setMessage(R.string.game_final_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)   // неможна закрити без натиску кнопки
                .setPositiveButton(R.string.game_final_positive, (dialog, buttonIndex) -> startGame())
                .setNegativeButton(R.string.game_final_negative, (dialog, buttonIndex) -> finish())
                .show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Log.d("snakeActivity", "onPause");
        isPlaying = false;
        saveGameData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Log.d("snakeActivity", "onResume");
        isPlaying = true;
    }


    static class Vector2 {
        int x;
        int y;

        public Vector2 copy() {
            return new Vector2(x,y);
        }

        public Vector2(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    enum Direction {
        left,
        right,
        top,
        bottom
    }
}
/*
Д.З. Реалізувати у грі "хрестики-нолики"
повідомлення (Alert Dialogs) про переможців та про продовження гри
якщо активність стає на паузу, то зберігати поточний стан гри,
а при відновленні активності - повертати його.
Якщо стартує нова гра при наявності збереженої старої, то видавати
повідомлення: продовжити стару / почати нову.
 */