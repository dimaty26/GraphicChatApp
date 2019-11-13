package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST, "name please"));
                Message message = connection.receive();
                String userName = message.getData();
                if (message.getType() == MessageType.USER_NAME &&
                        !userName.isEmpty() &&
                        !connectionMap.containsKey(userName)) {
                    connectionMap.put(userName, connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED, "accepted"));
                    return userName;
                }
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            connectionMap.forEach((name, connect) -> {
                try {
                    if (name != userName)
                    connection.send(new Message(MessageType.USER_ADDED, name));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                } else {
                    ConsoleHelper.writeMessage("Specify correct data");
                }
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Connection established via: " + socket.getRemoteSocketAddress());
            String newUser = null;

            try (Connection connection = new Connection(socket)) {
                newUser = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, newUser));
                notifyUsers(connection, newUser);
                serverMainLoop(connection, newUser);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Error has appeared in remote connection");
            } finally {
                if (newUser!=null) {
                    connectionMap.remove(newUser);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, newUser));
                }
            }
            ConsoleHelper.writeMessage("Connection has been closed");
        }
    }

    public static void sendBroadcastMessage(Message message) {
        connectionMap.forEach((name, connection) -> {
            try {
                connection.send(message);
            } catch (IOException e) {
//                connection.send(new Message(MessageType.TEXT, "Failed to send message: " + e.getMessage()));
                ConsoleHelper.writeMessage("Failed to send message");
            }
        });
    }

    public static void main(String[] args) throws IOException {
        int port = ConsoleHelper.readInt();
        ServerSocket server = new ServerSocket(port);
        System.out.println("Сервер запущен!");

        while (true) {
            try {
                Socket clientSocket = server.accept();
                Handler handler = new Handler(clientSocket);
                handler.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                server.close();
                break;
            }
        }
    }
}
