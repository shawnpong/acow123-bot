package org.example;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.HashMap;


@Component
public class TelegramBot extends TelegramLongPollingBot {
    // zh, evan, mervyn, jy, pong, igy, raymond
    public static final String[] allowedIds = {"260987722", "951962899", "1373801804", "138693338", "773474769", "673595156", "181233098", "907338890"};
    public static final String adarshId = "1032794070";
//    public static final String adarshId = "773474769";
    public static final HashMap<String, String> groupPrefixes = new HashMap<String, String>();
    private final HashMap<String, String> userGroupMapping = new HashMap<>();
    private HashMap<String, String> pendingReplies = new HashMap<>();
    private HashMap<String, Integer> forwardedMessageIds = new HashMap<>();
    private HashMap<String, Long> forwardedChatIds = new HashMap<>();
    private Boolean isSilenced = false;
//    private Boolean isReplying = false;

    @Autowired
    private Properties properties;
    @PostConstruct
    public void post() {
//        groupPrefixes.put("sn", "-4183226315"); //testing
        groupPrefixes.put("divine", "-994335605");
        groupPrefixes.put("sn", "-1002065075801");
        groupPrefixes.put("recre", "-1001927647862");
        groupPrefixes.put("ihg", "-1002095927754");
        groupPrefixes.put("retirement", "-1002079578384");
        groupPrefixes.put("b3", "-1001953422725");
        groupPrefixes.put("enhao", "-1001767829534");


        System.out.println("acow123 bot service started");
    }

    @Override
    public String getBotUsername() {
        return "acow123_bot";
    }

    @Override
    public String getBotToken() {
        return properties.getTelegramAPIKey();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        String userId = message.getFrom().getId().toString();
        String user = message.getFrom().getUserName();
        String groupPrefix = userGroupMapping.get(user);
        String groupId = groupPrefixes.get(groupPrefix);
        Integer messageId = message.getMessageId();
        System.out.println("ChatID " + chatId);
        if (!isAuthorized(chatId) && !(userId.equals(adarshId))) {
            return;
        }
        if (userId.equals(adarshId) && (chatId.equals(groupPrefixes.get("sn")) || chatId.equals(groupPrefixes.get("retirement"))) && isSilenced) {
            System.out.println("Silencing Adarsh");
            if (message.hasText()) {
                String messageContent = message.getText();
                DeleteMessage toDelete = new DeleteMessage();
                toDelete.setChatId(chatId);
                toDelete.setMessageId(message.getMessageId());
                try {
                    execute(toDelete);
                } catch (TelegramApiException e) {
                    e.printStackTrace(); // Log the exception
                }
                sendResponse(chatId, messageContent);
            }
            return;
        }
        if (chatId.startsWith("-")) {
            System.out.println("Group message received from " + user);
            return;
        }
        if (pendingReplies.containsKey(chatId) && message.getForwardFrom() != null) {
//            System.out.println("pending");
            storeForwardedMessage(message);
            System.out.println(forwardedChatIds);
            return;
        }
        if (forwardedChatIds.containsKey(chatId)) {
            System.out.println("forwaded");
            sendResponseToOriginalMessage(message.getText(), chatId);
            return;
        }
//        if (pendingReplies.containsKey(chatId) && message.getForwardFrom() != null) {
////            Long replyToMessageId = pendingReplies.get(message.getChatId());
//            String forwardedMessageText = "What do you want to reply to message: " + message.getForwardFrom().getUserName() + "\n" + message.getText();
//            Long forwardedChatId = message.getForwardFromChat().getId();
//            Integer forwardedMessageId = message.getForwardFromMessageId();
//            sendResponse(chatId, forwardedMessageText);
//
//            // Add the forwarded message details to replyMap for the next stage
////            replyMap.put(message.getChatId(), (long) message.getMessageId());
//            forwardedChatIds.put(message.getChatId().toString(), forwardedChatId);
//            forwardedMessageIds.put((message.getChatId().toString()), forwardedMessageId);
//            pendingReplies.remove(message.getChatId());
//            System.out.println(forwardedMessageId + forwardedChatId);
//            return;
//        }


        if (message.hasText()) {
            String messageContent = message.getText();
            System.out.println("Text message received from " + chatId
                    + ", user: " + user
                    + ", message: " + messageContent);
            if (messageContent.startsWith("/")) {
                handleCommand(chatId, user, messageContent, message);
                return;
            }

            if (!userGroupMapping.containsKey(user)) {
                sendResponse(chatId, "Please select a group by using a command like /divine or /sn before sending messages.\n\n Do /help for more groups!");
                return;
            }

            sendResponse(groupId, messageContent);
        } else if (message.hasVideo()) {
            System.out.println("Video received from " + chatId
                    + ", user: " + user
                    + ", videoId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasVoice()) {
            System.out.println("Voice message received from " + chatId
                    + ", user: " + user
                    + ", voicemessageId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasSticker()) {
            System.out.println("Sticker received from " + chatId
                    + ", user: " + user
                    + ", stickerId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasDocument()) {
            System.out.println("Document received from " + chatId
                    + ", user: " + user
                    + ", documentId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasPhoto()) {
            System.out.println("Photo received from " + chatId
                    + ", user: " + user
                    + ", photoId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasVideoNote()) {
            System.out.println("Telebubble received from " + chatId
                    + ", user: " + user
                    + ", telebubbleId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasPoll()) {
            System.out.println("Poll received from " + chatId
                    + ", user: " + user
                    + ", pollId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        }
    }

    public void sendUserGuide(String chatId) {
        StringBuilder guide = new StringBuilder();
        guide.append("\uD83D\uDC76\uD83C\uDFFE **Welcome to the Adarsh is a lil nigger User Guide!** \uD83D\uDC76\uD83C\uDFFE\n\n");
        guide.append("Here are some commands you can use:\n");
        guide.append("/help - Display this user guide.\n");
        guide.append("/list - List out the groups for you to choose!\n");
        guide.append("/silence or /unsilence to MUTE ADARSH!");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(guide.toString());
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }

    public void sendGroupList(String chatId) {
        StringBuilder list = new StringBuilder();
        list.append("Available groups are:\n");
        groupPrefixes.keySet().forEach(group -> list.append("/").append(group).append("\n"));

        sendResponse(chatId, list.toString());
    }

    private void handleCommand(String chatId, String user, String command, Message message) {
        if (command.equals("/help")) {
            sendUserGuide(chatId);
            return;
        } else if (command.equals("/list")) {
            sendGroupList(chatId);
            return;
        } else if (command.equals("/silence")) {
            isSilenced = true;
            sendResponse(chatId, "Adarsh has been silenced!");
            return;
        } else if (command.equals("/unsilence")) {
            isSilenced = false;
            sendResponse(chatId, "Adarsh has been given a voice :(");
            return;
        } else if (command.equals("/reply")) {
            sendResponse(chatId, "Forward the message you want to from a valid group chat!");
            pendingReplies.put(chatId, "pending");
            System.out.println(pendingReplies);
            return;
        }


        String[] parts = command.split(" ");
        if (parts.length != 1) {
            sendResponse(chatId, "Invalid command format.");
            sendUserGuide(chatId);
            return;
        }

        String newGroup = parts[0].substring(1).toLowerCase(); // Remove the '/' character
        if (groupPrefixes.containsKey(newGroup)) {
            userGroupMapping.put(user, newGroup);
            sendResponse(chatId, "Group set to " + newGroup);
        } else {
            sendResponse(chatId, "Group not found. Available groups are: " + String.join(", ", groupPrefixes.keySet()));
        }
    }
    private void storeForwardedMessage(Message message) {
        System.out.println("pending");
        String chatId = message.getChatId().toString();
        Long forwardedChatId = message.getForwardFromChat().getLinkedChatId();
        Integer forwardedMessageId = message.getForwardFromMessageId();
        System.out.println(forwardedChatId);
        System.out.println(forwardedMessageId);
        forwardedChatIds.put(chatId, forwardedChatId);
        forwardedMessageIds.put(chatId, forwardedMessageId);
        pendingReplies.remove(chatId);
    }

    // Method to send the response as a reply to the original message
    private void sendResponseToOriginalMessage(String responseText, String chatId) {
        Long forwardedChatId = forwardedChatIds.get(chatId);
        Integer forwardedMessageId = forwardedMessageIds.get(chatId);
        sendResponse(forwardedChatId.toString(), responseText, forwardedMessageId);
        // Clean up
        forwardedChatIds.remove(chatId);
        forwardedMessageIds.remove(chatId);
//        pendingReplies.remove(chatId);
    }
    public void forwardMessage(String chatId, String fromChatId, String messageId) {
        CopyMessage copyMessage = new CopyMessage();
        copyMessage.setChatId(chatId);
        copyMessage.setFromChatId(fromChatId);
        copyMessage.setMessageId(Integer.parseInt(messageId)); // Convert messageId to integer

        try {
            execute(copyMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }

    public void sendResponse(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }
    public void sendResponse(String chatId, String messageText, Integer replyToMessageId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        message.setReplyToMessageId(replyToMessageId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }

    public boolean isAuthorized(String chatId) {
        for(String element : allowedIds) {
            if(element.equals(chatId)) {
                return true;
            }
        }
        return false;
    }
}

