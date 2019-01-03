package com.example.telegramdemo;

import com.github.badoualy.telegram.api.Kotlogram;
import com.github.badoualy.telegram.api.TelegramApp;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.api.utils.MediaInput;
import com.github.badoualy.telegram.api.utils.TLMediaUtilsKt;
import com.github.badoualy.telegram.tl.api.*;
import com.github.badoualy.telegram.tl.api.auth.TLAuthorization;
import com.github.badoualy.telegram.tl.api.auth.TLSentCode;
import com.github.badoualy.telegram.tl.api.messages.TLAbsDialogs;
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages;
import com.github.badoualy.telegram.tl.api.messages.TLStickers;
import com.github.badoualy.telegram.tl.core.TLObject;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TelegramDemoApplicationTests {
    // Get them from Telegram's console

    public static final int API_ID = 298275;
    public static final String API_HASH = "f9bf25fc540a870e8814fe32df9070c1";

    // What you want to appear in the "all sessions" screen
    public static final String APP_VERSION = "AppVersion";
    public static final String MODEL = "Model";
    public static final String SYSTEM_VERSION = "SysVer";
    public static final String LANG_CODE = "en";
    private static final File ROOT_DIR = new File("");

    public static TelegramApp application = new TelegramApp(API_ID, API_HASH, MODEL, SYSTEM_VERSION, APP_VERSION, LANG_CODE);

    // Phone number used for tests
    public static final String PHONE_NUMBER = "+861851855402"; // International format

    @Test
    public void contextLoads() {

    }

    @Test
    public void signIn() {
        // This is a synchronous client, that will block until the response arrive (or until timeout)
        TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorage());

        // You can start making requests
        try {
            // Send code to account
            TLSentCode sentCode = client.authSendCode(false, PHONE_NUMBER, true);
            System.out.println("Authentication code: ");
            String code = new Scanner(System.in).nextLine();

            // Auth with the received code
            TLAuthorization authorization = client.authSignIn(PHONE_NUMBER, sentCode.getPhoneCodeHash(), code);
            TLUser self = authorization.getUser().getAsUser();
            System.out.println("You are now signed in as " + self.getFirstName() + " " + self.getLastName() + " @" + self.getUsername());
        } catch (RpcErrorException | IOException e) {
            e.printStackTrace();
        } finally {
            client.close(); // Important, do not forget this, or your process won't finish
        }
    }

    @Test
    public void getRecentConversationList() {
        // This is a synchronous client, that will block until the response arrive (or until timeout)
        TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorage());

        // Number of recent conversation you want to get (Telegram has an internal max, your value will be capped)
        int count = 10;

        // You can start making requests
        try {
            TLAbsDialogs tlAbsDialogs = client.messagesGetDialogs(true, 0, 0, new TLInputPeerEmpty(), count);

            // Map peer id to displayable string
            HashMap<Integer, String> nameMap = createNameMap(tlAbsDialogs);

            // Map message id to message
            HashMap<Integer, TLAbsMessage> messageMap = new HashMap<>();
            tlAbsDialogs.getMessages().forEach(message -> messageMap.put(message.getId(), message));

            tlAbsDialogs.getDialogs().forEach(dialog -> {
                System.out.print(nameMap.get(getId(dialog.getPeer())) + ": ");
                TLAbsMessage topMessage = messageMap.get(dialog.getTopMessage());
                if (topMessage instanceof TLMessage) {
                    // The message could also be a file, a photo, a gif, ...
                    System.out.println(((TLMessage) topMessage).getMessage());
                } else if (topMessage instanceof TLMessageService) {
                    TLAbsMessageAction action = ((TLMessageService) topMessage).getAction();
                    // action defined the type of message (user joined group, ...)
                    System.out.println("Service message");
                }
            });
        } catch (RpcErrorException | IOException e) {
            e.printStackTrace();
        } finally {
            client.close(); // Important, do not forget this, or your process won't finish
        }
    }

    /**
     * @param tlAbsDialogs result from messagesGetDialogs
     * @return a map where the key is the peerId and the value is the chat/channel title or the user's name
     */
    public static HashMap<Integer, String> createNameMap(TLAbsDialogs tlAbsDialogs) {
        // Map peer id to name
        HashMap<Integer, String> nameMap = new HashMap<>();

        tlAbsDialogs.getUsers().stream()
                .map(TLAbsUser::getAsUser)
                .forEach(user -> nameMap.put(user.getId(),
                        user.getFirstName() + " " + user.getLastName()));

        tlAbsDialogs.getChats().stream()
                .forEach(chat -> {
                    if (chat instanceof TLChannel) {
                        nameMap.put(chat.getId(), ((TLChannel) chat).getTitle());
                    } else if (chat instanceof TLChannelForbidden) {
                        nameMap.put(chat.getId(), ((TLChannelForbidden) chat).getTitle());
                    } else if (chat instanceof TLChat) {
                        nameMap.put(chat.getId(), ((TLChat) chat).getTitle());
                    } else if (chat instanceof TLChatEmpty) {
                        nameMap.put(chat.getId(), "Empty chat");
                    } else if (chat instanceof TLChatForbidden) {
                        nameMap.put(chat.getId(), ((TLChatForbidden) chat).getTitle());
                    }
                });

        return nameMap;
    }

    public static int getId(TLAbsPeer peer) {
        if (peer instanceof TLPeerUser)
            return ((TLPeerUser) peer).getUserId();
        if (peer instanceof TLPeerChat)
            return ((TLPeerChat) peer).getChatId();

        return ((TLPeerChannel) peer).getChannelId();
    }

    @Test
    public void GetMessages() {
        // This is a synchronous client, that will block until the response arrive (or until timeout)
        TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorage());

        // How many messages we want to get (same than dialogs, there is a cap)
        int count = 10;

        // You can start making requests
        try {
            TLAbsDialogs tlAbsDialogs = client.messagesGetDialogs(true, 0, 0, new TLInputPeerEmpty(), 1);
            TLAbsInputPeer inputPeer = getInputPeer(tlAbsDialogs);

            TLAbsMessages tlAbsMessages = client.messagesGetHistory(inputPeer, 0, 0, 0, count, 0, 0);
            tlAbsMessages.getMessages().forEach(message -> {
                if (message instanceof TLMessage)
                    System.out.println(((TLMessage) message).getMessage());
                else
                    System.out.println("Service message");
            });
        } catch (RpcErrorException | IOException e) {
            e.printStackTrace();
        } finally {
            client.close(); // Important, do not forget this, or your process won't finish
        }
    }

    /**
     * Get the first peer and return it as an InputPeer to use with methods
     */
    public static TLAbsInputPeer getInputPeer(TLAbsDialogs tlAbsDialogs) {
        TLAbsPeer tlAbsPeer = tlAbsDialogs.getDialogs().get(0).getPeer();
        int peerId = getId(tlAbsPeer);
        TLObject peer = tlAbsPeer instanceof TLPeerUser ?
                tlAbsDialogs.getUsers().stream().filter(user -> user.getId() == peerId).findFirst().get()
                : tlAbsDialogs.getChats().stream().filter(chat -> chat.getId() == peerId).findFirst().get();

        if (peer instanceof TLChannel)
            return new TLInputPeerChannel(((TLChannel) peer).getId(), ((TLChannel) peer).getAccessHash());
        if (peer instanceof TLChat)
            return new TLInputPeerChat(((TLChat) peer).getId());
        if (peer instanceof TLUser)
            return new TLInputPeerUser(((TLUser) peer).getId(), ((TLUser) peer).getAccessHash());

        return new TLInputPeerEmpty();
    }

    @Test
    public void sendMessage() {
        // This is a synchronous client, that will block until the response arrive (or until timeout)
        TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorage());

        // You can start making requests
        try {
            TLAbsDialogs tlAbsDialogs = client.messagesGetDialogs(true, 0, 0, new TLInputPeerEmpty(), 1);
            TLAbsInputPeer inputPeer = getInputPeer(tlAbsDialogs);

            TLAbsUpdates tlAbsUpdates = client.messagesSendMessage(inputPeer, "Sent from Kotlogram :)", Math.abs(new Random().nextLong()));

            // tlAbsUpdates contains the id and date of the message in a TLUpdateShortSentMessage
        } catch (RpcErrorException | IOException e) {
            e.printStackTrace();
        } finally {
            client.close(); // Important, do not forget this, or your process won't finish
        }
    }

    @Test
    public void sendSticker() {
        // This is a synchronous client, that will block until the response arrive (or until timeout)
        TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorage());

        // You can start making requests
        try {
            TLAbsDialogs tlAbsDialogs = client.messagesGetDialogs(true, 0, 0, new TLInputPeerEmpty(), 1);
            TLAbsInputPeer inputPeer = getInputPeer(tlAbsDialogs);

            // Get the stickers available for emoji sunglass
//            TLStickers tlStickers = (TLStickers) client.messagesGetAllStickers(0);//.messagesGetStickers("\uD83D\uDE0E", "");
//            if (!tlStickers.getStickers().isEmpty()) {
//                // Take first available one
//                TLDocument tlDocument = tlStickers.getStickers().get(0).getAsDocument();
//                TLInputDocument tlInputDocument = new TLInputDocument(tlDocument.getId(), tlDocument.getAccessHash());
//
//                TLAbsUpdates tlAbsUpdates = client.messagesSendMedia(false, false, false,
//                        inputPeer, null, new TLInputMediaDocument(tlInputDocument, ""),
//                        Math.abs(new Random().nextLong()), null);
//                // tlAbsUpdates contains the id and date of the message in a TLUpdateShortSentMessage
//            } else {
//                System.err.println("No sticker found");
//            }
        } catch (RpcErrorException | IOException e) {
            e.printStackTrace();
        } finally {
            client.close(); // Important, do not forget this, or your process won't finish
        }
    }

    @Test
    public void download() {
        // This is a synchronous client, that will block until the response arrive (or until timeout)
        TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorage());

        // You can start making requests
        try {
            TLAbsDialogs tlAbsDialogs = client.messagesGetDialogs(true, 0, 0, new TLInputPeerEmpty(), 1);
            TLAbsInputPeer inputPeer = getInputPeer(tlAbsDialogs);

            // Get most recent message
            TLAbsMessages tlAbsMessages = client.messagesGetHistory(inputPeer, 0, 0, 0, 1, 0, 0);
            TLAbsMessage tlAbsMessage = tlAbsMessages.getMessages().get(0);

            if (tlAbsMessage instanceof TLMessage && ((TLMessage) tlAbsMessage).getMedia() != null) {
                TLAbsMessageMedia media = ((TLMessage) tlAbsMessage).getMedia();

                // Magic utils method from api module
                MediaInput mediaInput = TLMediaUtilsKt.getAbsMediaInput(media);

                if (mediaInput != null) {
                    String filename;
                    if (media instanceof TLMessageMediaPhoto || media instanceof TLMessageMediaWebPage) {
                        filename = "photo.jpg";
                    } else {
                        // Retrieve real name
                        TLDocument tlDocument = ((TLMessageMediaDocument) media).getDocument().getAsDocument();
                        filename = ((TLDocumentAttributeFilename) tlDocument.getAttributes().stream()
                                .filter(attr -> attr instanceof TLDocumentAttributeFilename)
                                .findFirst().get()).getFileName();
                    }

                    FileOutputStream fos = new FileOutputStream(new File(ROOT_DIR, filename));
                    client.downloadSync(mediaInput.getInputFileLocation(), mediaInput.getSize(), fos);
                    // downloadSync closes the stream automatically
                } else {
                    System.err.println("MessageMedia type not supported" + media.getClass().getSimpleName());
                }
            } else {
                System.err.println("Latest message has no media attached");
            }
        } catch (RpcErrorException | IOException e) {
            e.printStackTrace();
        } finally {
            client.close(); // Important, do not forget this, or your process won't finish
        }
    }
}

