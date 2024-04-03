package step.learning.android111;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private final Vector2 food = new Vector2(5,5);
    private Direction moveDirection;

    private int  cellColorRes;
    private int fieldColorRes;
    private int snakeColorRes;
    private int  foodColorRes;
    private String foodSymbol = new String( Character.toChars(0x1f34e) );
    private boolean isPlaying;

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

        handler = new Handler();
        initField();
        startGame();
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
        }
        else {
            Vector2 tail = snake.removeLast();
            // стираємо старий хвіст - зафарбовуємо у колір комірки
            cells[tail.getX()][tail.getY()].setBackgroundColor( cellColorRes );
        }

        if(isPlaying) {
            handler.postDelayed(this::update, period);
        }
    }

    private void startGame() {
        cells[food.getX()][food.getY()].setText(foodSymbol);
        // cells[5][5].setBackgroundColor( foodColorRes ) ;

        snake.clear();
        snake.add( new Vector2(5, 15 ) ) ;
        snake.add( new Vector2(5, 16 ) ) ;
        snake.add( new Vector2(5, 17 ) ) ;
        for( Vector2 tail : snake ) {
            cells[tail.getX()][tail.getY()].setBackgroundColor( snakeColorRes ) ;
        }
        isPlaying = true;
        moveDirection = Direction.top;
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
    @Override
    protected void onPause() {
        super.onPause();
        // Log.d("snakeActivity", "onPause");
        isPlaying = false;
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
Д.З. Реалізувати у грі "хрестики-нолики" відображення часу, витраченного кожним з гравців
(ввести два годинники на UI)
 */