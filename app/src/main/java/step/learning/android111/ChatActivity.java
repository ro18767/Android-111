package step.learning.android111;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import step.learning.android111.orm.ChatMessage;
import step.learning.android111.services.Http;

public class ChatActivity extends AppCompatActivity {
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private LinearLayout messagesContainer;
    private ScrollView messagesScroller;
    private EditText etAuthor;
    private EditText etMessage;
    private Drawable bgOwn, bgOther;
    private final SimpleDateFormat datetimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.UK);
    private final ExecutorService pool = Executors.newFixedThreadPool(3);
    private final Handler handler = new Handler();
    private MediaPlayer incomingMessage;
    private String author = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // new Thread(this::loadChatMessages).start();
        handler.post(this::timer);
        messagesContainer = findViewById( R.id.chat_messages_container );
        messagesScroller = findViewById( R.id.chat_messages_scroller ) ;
        findViewById(R.id.chat_btn_send).setOnClickListener( this::sendMessageClick );
        etAuthor = findViewById( R.id.chat_et_name );
        etMessage = findViewById( R.id.chat_et_message );
        bgOwn = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.chat_bg_own);
        bgOther = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.chat_bg_other);
        incomingMessage = MediaPlayer.create(this, R.raw.income);
        // incomingMessage.start();
    }

    private void timer() {
        try { pool.submit(this::loadChatMessages); }
        catch (Exception ignored) { }
        handler.postDelayed(this::timer, 3000);
    }
    private void loadChatMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject( Http.getString( CHAT_URL ) );
            if( jsonObject.getInt("status" ) == 1 ) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    messages.add( ChatMessage.fromJson( jsonArray.getJSONObject(i) ) ) ;
                }
            }
        }
        catch (Exception ignore) {}
        messages.sort( Comparator.comparing( ChatMessage::getMoment ) );
        boolean needUpdate = false;
        for(ChatMessage message : messages) {
            if(chatMessages.stream().noneMatch( m -> m.getId().equals( message.getId() )) ) {
                // нове повідомлення
                chatMessages.add( message ) ;
                needUpdate = true;
            }
        }
        if( needUpdate ) {
            // запуск перерисовування колекції повідомлень
            runOnUiThread( this::updateMessagesView );
        }
    }

    private void updateMessagesView() {
        // будемо вважати власними ті повідомлення, у яких автор збігається з нашим полем
        String author = etAuthor.getText().toString();
        boolean needSound = false;
        boolean isFirst = messagesContainer.getChildCount() == 0;
        for( ChatMessage message : chatMessages ) {
            if( message.getTag() == null ) {  // відсутність тегу - нове / ще не представлено
                boolean isOwn = author.equals( message.getAuthor() ) ;
                needSound |=! isOwn;  // needSound = needSound || ! isOwn;
                View view = messageView(message, isOwn);
                messagesContainer.addView(view);
                message.setTag(view);   // як тег ставимо посилання на View даного повідомлення
            }
        }
        if(needSound && !isFirst) {
            incomingMessage.start();
        }
        // Прокрутка scroll view: формування контенту (відображення) відбувається
        // асинхронно. Якщо подати команду скролінгу прямо,
        // messagesScroller.fullScroll( View.FOCUS_DOWN ) ;
        // то буде здійснено
        // прокрутку до того місця, яке встигло прорисуватись. Дуже імовірно, що
        // це не буде повним контентом.
        // Такі команди подаються через канал повідомлень (подійний цикл)
        messagesScroller.post( () -> messagesScroller.fullScroll( View.FOCUS_DOWN ) ) ;
    }

    private View messageView(ChatMessage message, boolean isOwn) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = isOwn ? Gravity.END : Gravity.START;
        layoutParams.setMargins(10, 15, 10, 15);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackground( isOwn ? bgOwn : bgOther );
        linearLayout.setPadding(15, 5, 15, 5);

        TextView textView = new TextView(this);
        textView.setText( getString(
                R.string.chat_message_line1,
                datetimeFormat.format( message.getMoment() ),
                message.getAuthor() ) );
        linearLayout.addView( textView ) ;
        textView = new TextView(this);
        textView.setText( message.getText() ) ;
        linearLayout.addView( textView );
        return linearLayout ;
        /*
        12:34 Author
        Text of the message
         */
    }

    private void sendMessageClick(View view) {
        if(author == null) {  // перше надсилання - зберігаємо дані про автора та блокуємо зміни
            author = etAuthor.getText().toString();
            if(author.isEmpty()) {
                Toast.makeText(this, R.string.chat_no_author_alert, Toast.LENGTH_SHORT).show();
                author = null;
                return;
            }
            else {
                etAuthor.setEnabled(false);
            }
        }
        String msg = etMessage.getText().toString();
        if(msg.isEmpty()) {
            Toast.makeText(this, R.string.chat_no_message_alert, Toast.LENGTH_SHORT).show();
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setAuthor( author );
        message.setText( msg );

        new Thread( () -> sendChatMessage(message) ).start();

    }

    private void sendChatMessage(ChatMessage message) {
        /*
        Бекенд чату приймає  нові повідомлення за схемою HTML-форми
        author -> Author, msg -> Text of message
        Це має відбитись у наступному запиті
        | POST /
        | Content-Type: application/x-www-form-urlencoded
        |
        | author=Author&msg=Text of message
         */
        try {
            // 1. Відкриваємо та налаштовуємо з'єднання
            URL url = new URL(CHAT_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput( true );  // запит матиме тіло (у запит можна виводити)
            connection.setDoInput( true );   // очікується відповідь (читання рез-тів запиту)
            connection.setRequestMethod( "POST" );
            // заголовки проходять як RequestProperty
            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Connection", "close" );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setChunkedStreamingMode( 0 );   // не ділити на чанки - на частини

            // 2. Output - запис тіла
            OutputStream connectionOutput = connection.getOutputStream();
            String body = String.format("author=%s&msg=%s",   // "author=The Author&msg=Text of message"
                    URLEncoder.encode( message.getAuthor(), StandardCharsets.UTF_8.name() ), // The%20Author
                    URLEncoder.encode( message.getText(), StandardCharsets.UTF_8.name() )  // Text%20of%20message
            );
            connectionOutput.write( body.getBytes( StandardCharsets.UTF_8 ) );
            connectionOutput.flush();  // надсилаємо дані
            connectionOutput.close();  // або try(res) або не забути закрити

            // 3. Одержання відповіді
            int statusCode = connection.getResponseCode();
            if( statusCode == 201 ) {  // успішно доставлено (тіла немає)
                loadChatMessages();    // запускаємо зчитування та оновлення чату
                runOnUiThread( () -> {
                    Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show();
                    etMessage.setText("");
                });
            }
            else {  // помилка, деталі у тілі
                InputStream connectionInput = connection.getErrorStream();
                body = Http.readStream( connectionInput ) ;
                Log.e("sendChatMessage", "statusCode: " + statusCode + " body: " + body );
                connectionInput.close();
            }

            // 4. Закриваємо з'єднання
            connection.disconnect();
        }
        catch (Exception ex) {
            Log.e("sendChatMessage", "ex: " + ex.getClass().getName() + " " + ex.getMessage() );
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        pool.shutdownNow();
        super.onDestroy();
    }
}
/*
Звуки: зберігаються у спеціальній ресурсній директорії raw
Як і для всіх ресурсів є вимоги до імен файлів: тільки маленькі літери, через "_"
(З імені файлу будується ідентифікатор)
Задача: програвати звук якщо є нові повідомлення, але не свої
При надсиланні першого повідомлення поле з іменем автора блокується і не дозволяє зміни
* а також зберігається у файлі і з наступним запуском підставляється з нього.
Забезпечити перевірку на порожні повідомлення/авторство -- видавати попередження, дані не надсилати
 */
/*
Д.З.(хрестики або ноліки) Реалізувати звукове оформлення
- звуки натиснення (різні для різних знаків)
- звуки скасування ходу
- звук виграшу
 */