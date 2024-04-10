package step.learning.android111.services;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Http {
    private static final byte[] buffer = new byte[4096];

    public static String getString(String url) {
        try( InputStream urlStream = new URL(url).openStream() ) {
            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            int len;
            while( (len = urlStream.read(buffer)) != -1 ) {
                byteBuilder.write(buffer, 0, len);
            }
            return new String( byteBuilder.toByteArray(), StandardCharsets.UTF_8 ) ;
        }
        catch (IOException | android.os.NetworkOnMainThreadException ex) {
            Log.e("Http::getString", "Ex: " + ex.getMessage() );
            throw new RuntimeException( ex ) ;
        }
    }
}
