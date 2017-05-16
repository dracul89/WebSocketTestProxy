import okhttp3.*;
import okio.ByteString;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public final class WebSocketEcho extends WebSocketListener {
    public static void main(String... args) {
        new WebSocketEcho().run();
    }

    private void run() {
        OkHttpClient client = new OkHttpClient.Builder()
                .proxyAuthenticator(getAuthentication())
                .proxy(getProxy())
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url("ws://echo.websocket.org")
                .build();
        client.newWebSocket(request, this);

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
    }

    private Authenticator getAuthentication() {
        return (route, response) -> {
            String credentials = Credentials.basic("", "");
            return response.request()
                    .newBuilder()
                    .header("Proxy-Authorization", credentials)
                    .build();
        };
    }

    private Proxy getProxy() {
        SocketAddress socketAddress = new InetSocketAddress("", 8080);
        return new Proxy(Proxy.Type.HTTP, socketAddress);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send("Hello...");
        webSocket.send("...World!");
        webSocket.send(ByteString.decodeHex("deadbeef"));
        webSocket.close(1000, "Goodbye, World!");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("MESSAGE: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println("MESSAGE: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, null);
        System.out.println("CLOSE: " + code + " " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
    }
}