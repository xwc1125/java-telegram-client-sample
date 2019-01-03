package com.example.telegramdemo;

import com.github.badoualy.telegram.api.TelegramApiStorage;
import com.github.badoualy.telegram.mtproto.auth.AuthKey;
import com.github.badoualy.telegram.mtproto.model.DataCenter;
import com.github.badoualy.telegram.mtproto.model.MTSession;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Description: <br>
 *
 * @author xwc1125 <br>
 * @Copyright: Copyright (c) 2018 <br>
 * @date 2018-12-26  18:30 <br>
 */
public class ApiStorage implements TelegramApiStorage {

    //Create File variable for auth.key and dc.save
    public static final File AUTH_KEY_FILE = new File("Properties/auth.key");
    public static final File NEAREST_DC_FILE = new File("Properties/dc.save");

    @Override
    public void saveAuthKey(AuthKey authKey) {
        try {
            FileUtils.writeByteArrayToFile(AUTH_KEY_FILE, authKey.getKey());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AuthKey loadAuthKey() {
        try {
            return new AuthKey(FileUtils.readFileToByteArray(AUTH_KEY_FILE));
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException))
                e.printStackTrace();
        }

        return null;
    }

    @Override
    public void saveDc(DataCenter dataCenter) {
        try {
            FileUtils.write(NEAREST_DC_FILE, dataCenter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DataCenter loadDc() {
        try {
            String[] infos = FileUtils.readFileToString(NEAREST_DC_FILE).split(":");
            return new DataCenter(infos[0], Integer.parseInt(infos[1]));
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException))
                e.printStackTrace();
        }

        return null;
    }

    @Override
    public void deleteAuthKey() {
        try {
            FileUtils.forceDelete(AUTH_KEY_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteDc() {
        try {
            FileUtils.forceDelete(NEAREST_DC_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSession(MTSession session) {

    }

    @Override
    public MTSession loadSession() {
        return null;
    }
}
