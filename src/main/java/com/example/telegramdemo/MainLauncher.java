package com.example.telegramdemo;


import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;

import com.github.badoualy.telegram.api.Kotlogram;
import com.github.badoualy.telegram.api.TelegramApp;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.api.UpdateCallback;
import com.github.badoualy.telegram.mtproto.model.DataCenter;
import com.github.badoualy.telegram.tl.api.TLAbsMessage;
import com.github.badoualy.telegram.tl.api.TLAbsMessageAction;
import com.github.badoualy.telegram.tl.api.TLAbsPeer;
import com.github.badoualy.telegram.tl.api.TLAbsUser;
import com.github.badoualy.telegram.tl.api.TLChannel;
import com.github.badoualy.telegram.tl.api.TLChannelForbidden;
import com.github.badoualy.telegram.tl.api.TLChat;
import com.github.badoualy.telegram.tl.api.TLChatEmpty;
import com.github.badoualy.telegram.tl.api.TLChatForbidden;
import com.github.badoualy.telegram.tl.api.TLInputPeerEmpty;
import com.github.badoualy.telegram.tl.api.TLMessage;
import com.github.badoualy.telegram.tl.api.TLMessageService;
import com.github.badoualy.telegram.tl.api.TLPeerChannel;
import com.github.badoualy.telegram.tl.api.TLPeerChat;
import com.github.badoualy.telegram.tl.api.TLPeerUser;
import com.github.badoualy.telegram.tl.api.TLUpdateShort;
import com.github.badoualy.telegram.tl.api.TLUpdateShortChatMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateShortMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateShortSentMessage;
import com.github.badoualy.telegram.tl.api.TLUpdates;
import com.github.badoualy.telegram.tl.api.TLUpdatesCombined;
import com.github.badoualy.telegram.tl.api.TLUser;
import com.github.badoualy.telegram.tl.api.auth.TLAuthorization;
import com.github.badoualy.telegram.tl.api.auth.TLSentCode;
import com.github.badoualy.telegram.tl.api.messages.TLAbsDialogs;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;

import com.example.telegramdemo.ApiStorage;

/**
 * @author Ruben Bermudez
 * @version 1.0
 * @brief TODO
 * @date 16 of October of 2016
 */
public class MainLauncher {
    public static final int API_ID = 298275;
    public static final String API_HASH = "f9bf25fc540a870e8814fe32df9070c1";
    private static final String PHONE_NUMBER = "+8618518554062"; // Your phone number

    // What you want to appear in the "all sessions" screen
    public static final String APP_VERSION = "AppVersion";
    public static final String MODEL = "Model";
    public static final String SYSTEM_VERSION = "SysVer";
    public static final String LANG_CODE = "en";

    public static TelegramApp application = new TelegramApp(API_ID, API_HASH, MODEL, SYSTEM_VERSION, APP_VERSION, LANG_CODE);

    //  public static TelegramApp application = new TelegramApp(API_ID, API_HASH, MODEL, SYSTEM_VERSION, APP_VERSION, LANG_CODE);
    public static void test() {
//        DataCenter prodDC = new DataCenter("149.154.167.50", 443);
        DataCenter prodDC = new DataCenter("149.154.167.51", 443);
//        DataCenter prodDC = new DataCenter("149.154.175.100", 443);
//        DataCenter prodDC = new DataCenter("149.154.175.50", 443);
//        DataCenter prodDC = new DataCenter("149.154.167.91", 443);
//        DataCenter prodDC = new DataCenter("149.154.171.5", 443);
        //TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorage(),null,prodDC4);

        TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorage(), prodDC, new UpdateCallback() {

            @Override
            public void onShortChatMessage(TelegramClient arg0, TLUpdateShortChatMessage arg1) {
            }

            @Override
            public void onShortMessage(TelegramClient arg0, TLUpdateShortMessage arg1) {

                System.out.println("New Message: \n" +
                        arg1.getMessage());
            }

            @Override
            public void onShortSentMessage(TelegramClient arg0, TLUpdateShortSentMessage arg1) {
            }

            @Override
            public void onUpdateShort(TelegramClient arg0, TLUpdateShort arg1) {
            }

            @Override
            public void onUpdateTooLong(TelegramClient arg0) {
            }

            @Override
            public void onUpdates(TelegramClient arg0, TLUpdates arg1) {
            }

            @Override
            public void onUpdatesCombined(TelegramClient arg0, TLUpdatesCombined arg1) {
            }

        });

        TLSentCode sentCode;
        try {
            sentCode = client.authSendCode(false, PHONE_NUMBER, true);
            System.out.println("Authentication code: ");
            // String code = new Scanner(System.in).nextLine();
            String code = JOptionPane.showInputDialog("Authentication code (Sent in your telegram app):");
            // Auth with the received code
            // You can start making requests
            try {
                // Send code to account
                TLAuthorization authorization = client.authSignIn(PHONE_NUMBER, sentCode.getPhoneCodeHash(), code);

                TLUser self = authorization.getUser().getAsUser();
                System.out.println("You are now signed in as " + self.getFirstName() + " " + self.getLastName() + " @" + self.getUsername());

            } catch (RpcErrorException | IOException e) {
                if (((RpcErrorException) e).getCode() == 400) {
                    String name = JOptionPane.showInputDialog("Your Name");
                    String surname = JOptionPane.showInputDialog("Your Surname");
                    TLAuthorization authorization = client.authSignUp(PHONE_NUMBER, sentCode.getPhoneCodeHash(), code, name, surname);

                    TLUser self = authorization.getUser().getAsUser();
                    System.out.println("You are now signed in as " + self.getFirstName() + " " + self.getLastName() + " @" + self.getUsername());

                }

                e.printStackTrace();
            } finally {

            }
        } catch (RpcErrorException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    public static void readMessages(TelegramClient client) {

        // Number of recent conversation you want to get (Telegram has an internal max, your value will be capped)
        int count = 10;

        // You can start making requests
        try {
            TLAbsDialogs tlAbsDialogs = client.messagesGetDialogs(false, 0, 0, new TLInputPeerEmpty(), count);

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
                    // JOptionPane.showMessageDialog(null, ((TLMessage) topMessage).getMessage());
                } else if (topMessage instanceof TLMessageService) {
                    TLAbsMessageAction action = ((TLMessageService) topMessage).getAction();
                    // action defined the type of message (user joined group, ...)
                    System.out.println("Service message");
                }
            });
        } catch (RpcErrorException | IOException e) {
            e.printStackTrace();
        } finally {
            //client.close(); // Important, do not forget this, or your process won't finish
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
}
