package step.learning.android111;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.ArrayList;
import java.util.List;

import step.learning.android111.orm.ChatMessage;
import step.learning.android111.services.Http;

public class ChatActivity extends AppCompatActivity {
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private LinearLayout messagesContainer;
    private EditText etAuthor;
    private EditText etMessage;

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
        new Thread(this::loadChatMessages).start();
        messagesContainer = findViewById( R.id.chat_messages_container );
        findViewById(R.id.chat_btn_send).setOnClickListener( this::sendMessageClick );
        etAuthor = findViewById( R.id.chat_et_name );
        etMessage = findViewById( R.id.chat_et_message );
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
        for( ChatMessage message : chatMessages ) {
            if( message.getTag() == null ) {  // відсутність тегу - нове / ще не представлено
                TextView tv = new TextView(this);
                tv.setText(message.getText());
                messagesContainer.addView(tv);
                message.setTag(tv);   // як тег ставимо посилання на View даного повідомлення
            }
        }
    }

    private void sendMessageClick(View view) {
        ChatMessage message = new ChatMessage();
        message.setAuthor( etAuthor.getText().toString() );
        message.setText( etMessage.getText().toString() );

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
                runOnUiThread( () ->
                        Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show() );
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
}
/*
Д.З.(хрестики або ноліки) Реалізувати надсилання повідомлення про виграш
у чат. Після виграшу з'являється (серед іншого) кнопка "поділитись",
натиск на яку надсилає до чату повідомлення "Я виграв у ХО"
* та переходить на активність чату
 */