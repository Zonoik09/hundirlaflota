package com.project.hundirlaflota;

import org.java_websocket.server.WebSocketServer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

public class Server extends WebSocketServer {

    private static final List<String> CHARACTER_NAMES = Arrays.asList(
        "Mario", "Luigi", "Peach", "Toad", "Bowser", "Wario", "Zelda", "Link"
    );

    private Map<WebSocket, String> clients;
    private List<String> availableNames;

    public Server(InetSocketAddress address) {
        super(address);
        clients = new ConcurrentHashMap<>();
        resetAvailableNames();
    }

    private void resetAvailableNames() {
        availableNames = new ArrayList<>(CHARACTER_NAMES);
        Collections.shuffle(availableNames);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String clientName = getNextAvailableName();
        clients.put(conn, clientName);
        System.out.println("WebSocket client connected: " + clientName);
        sendClientsList();
    }

    private String getNextAvailableName() {
        if (availableNames.isEmpty()) {
            resetAvailableNames();
        }
        return availableNames.remove(0);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String clientName = clients.get(conn);
        clients.remove(conn);
        availableNames.add(clientName);
        System.out.println("WebSocket client disconnected: " + clientName);
        sendClientsList();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String clientName = clients.get(conn);
        JSONObject obj = new JSONObject(message);

        if (obj.has("type")) {
            String type = obj.getString("type");
            if (type.equals("bounce")) {
                JSONObject rst = new JSONObject();
                rst.put("type", "bounce");
                rst.put("message", obj.getString("message"));
                conn.send(rst.toString());

            } else if (type.equals("broadcast")) {
                JSONObject rst = new JSONObject();
                rst.put("type", "broadcast");
                rst.put("origin", clientName);
                rst.put("message", obj.getString("message"));

                broadcastMessage(rst.toString(), conn);

            } else if (type.equals("private")) {
                String destination = obj.getString("destination");
                JSONObject rst = new JSONObject();
                rst.put("type", "private");
                rst.put("origin", clientName);
                rst.put("destination", destination);
                rst.put("message", obj.getString("message"));

                sendPrivateMessage(destination, rst.toString(), conn);
            }
        }
    }

    private void broadcastMessage(String message, WebSocket sender) {
        for (Map.Entry<WebSocket, String> entry : clients.entrySet()) {
            WebSocket conn = entry.getKey();
            if (conn != sender) {
                try {
                    conn.send(message);
                } catch (WebsocketNotConnectedException e) {
                    System.out.println("Client " + entry.getValue() + " not connected.");
                    clients.remove(conn);
                    availableNames.add(entry.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendPrivateMessage(String destination, String message, WebSocket senderConn) {
        boolean found = false;

        for (Map.Entry<WebSocket, String> entry : clients.entrySet()) {
            if (entry.getValue().equals(destination)) {
                found = true;
                try {
                    entry.getKey().send(message);
                    JSONObject confirmation = new JSONObject();
                    confirmation.put("type", "confirmation");
                    confirmation.put("message", "Message sent to " + destination);
                    senderConn.send(confirmation.toString());
                } catch (WebsocketNotConnectedException e) {
                    System.out.println("Client " + destination + " not connected.");
                    clients.remove(entry.getKey());
                    availableNames.add(destination);
                    notifySenderClientUnavailable(senderConn, destination);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        if (!found) {
            System.out.println("Client " + destination + " not found.");
            notifySenderClientUnavailable(senderConn, destination);
        }
    }

    private void notifySenderClientUnavailable(WebSocket sender, String destination) {
        JSONObject rst = new JSONObject();
        rst.put("type", "error");
        rst.put("message", "Client " + destination + " not available.");

        try {
            sender.send(rst.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendClientsList() {
        JSONArray clientList = new JSONArray();
        for (String clientName : clients.values()) {
            clientList.put(clientName);
        }

        Iterator<Map.Entry<WebSocket, String>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WebSocket, String> entry = iterator.next();
            WebSocket conn = entry.getKey();
            String clientName = entry.getValue();

            JSONObject rst = new JSONObject();
            rst.put("type", "clients");
            rst.put("id", clientName);
            rst.put("list", clientList);

            try {
                conn.send(rst.toString());
            } catch (WebsocketNotConnectedException e) {
                System.out.println("Client " + clientName + " not connected.");
                iterator.remove();
                availableNames.add(clientName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port: " + getPort());
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    public static void main(String[] args) {
        int port = 3000; 
        Server server = new Server(new InetSocketAddress(port));
        server.start();

        LineReader reader = LineReaderBuilder.builder().build();

        System.out.println("Server running. Type 'exit' to gracefully stop it.");

        try {
            while (true) {
                String line = null;
                try {
                    line = reader.readLine("> ");
                } catch (UserInterruptException e) {
                    continue;
                } catch (EndOfFileException e) {
                    break;
                }

                line = line.trim();

                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Stopping server...");
                    try {
                        server.stop(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                } else {
                    System.out.println("Unknown command. Type 'exit' to stop server gracefully.");
                }
            }
        } finally {
            System.out.println("Server stopped.");
        }
    }
}