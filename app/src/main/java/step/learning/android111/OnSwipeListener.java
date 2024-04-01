package step.learning.android111;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public OnSwipeListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
    // Методи для переозначення нащадками
    public void onSwipeBottom() {}
    public void onSwipeLeft() {}
    public void onSwipeRight() {}
    public void onSwipeTop() {}

    // Nested class - клас, оголошений в іншому класі
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int MIN_DISTANCE = 100;
        private static final int MIN_VELOCITY = 100;
        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean isDispatched = false;
            // основа алгоритму - аналіз координат двох точок е1 та е2, а також швидкості проведення
            try {
                assert e1 != null;
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                // визначаємо який це рух. Ідеально горизонтальних чи верт. не буває, тому
                // обидва числа (dx, dy) не є нульовими, слід визначити яке є "більшим"
                if( Math.abs(dx) > Math.abs(dy) ) {   // цей жест швидше горизонтальний
                    // на граничні значення перевіряємо Х координати
                    if( Math.abs(dx) >= MIN_DISTANCE && Math.abs(velocityX) >= MIN_VELOCITY ) {
                        // горизонтальні ліміти задоволені, визначаємо ліво чи право
                        if( dx > 0 ) {   // e1 ----> e2 - right swipe
                            onSwipeRight();
                        }
                        else {   // e2 <---- e1 - left swipe
                            onSwipeLeft();
                        }
                        // у будь-якому разі повідомляє про те, що подія оброблена
                        isDispatched = true;
                    }
                }
                else {   // цей жест швидше вертикальний
                    if( Math.abs(dy) >= MIN_DISTANCE && Math.abs(velocityY) >= MIN_VELOCITY ) {
                        if( dy > 0 ) {    // bottom
                            onSwipeBottom();
                        }
                        else {   // top
                            onSwipeTop();
                        }
                        isDispatched = true;
                    }
                }
            }
            catch(Exception ignore){}

            return isDispatched;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;  // упереджуємо події натиснення
        }
    }
}
/*
Swipe - жест проведення від однієї точки до іншої
У системі немає вбудованих засобів такого жесту, тому
слід створювати власний.
Як правило, свайпи поділяють за напрямом:
горизонтальні
 лівий
 правий
вертикальний
 верхній
 нижній
Бувають діагональні, але їх важче детектувати через не-квадратність пристроїв
Для сучасних пристроїв з великою щільністю пікселів майже всі
торкання сприймаються як проведення, оскільки "пляма дотику" деформується,
зміщуючи центр.
Для відокремлення свайпів слід задати достатню відстань проведення та
бажано достатню швидкість.
 */