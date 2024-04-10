package step.learning.android111;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import step.learning.android111.orm.ChatMessage;
import step.learning.android111.services.Http;

public class ChatActivity extends AppCompatActivity {
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private LinearLayout messagesContainer;

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
            TextView tv = new TextView(this);
            tv.setText( message.getText() );
            messagesContainer.addView( tv ) ;
        }
    }

}
/*
Д.З. Реалізувати випадковий перший хід (хрестики або ноліки)
через запит випадкового числа з незалежного постачальника
на кшталт random.org
 */