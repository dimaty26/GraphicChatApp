package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class BotClient extends Client {

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }



    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);

            Map<String, String> formats = new HashMap<String, String>() {
                {
                    put("дата", "d.MM.YYYY");
                    put("день", "d");
                    put("месяц", "MMMM");
                    put("год", "YYYY");
                    put("время", "H:mm:ss");
                    put("час", "H");
                    put("минуты", "m");
                    put("секунды", "s");
                }
            };

            if (message.contains(":")) {
                String[] strings = message.split(":");
                String userMessage = strings[1].trim();
                String userName = strings[0];

                formats.keySet().stream().filter(usMsg -> usMsg.equals(userMessage))
                        .forEach(s -> sendTextMessage(String.format("Информация для %s: %s",
                                userName, new SimpleDateFormat(formats.get(s)).format(Calendar.getInstance().getTime())
                        )));
            }
        }
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
