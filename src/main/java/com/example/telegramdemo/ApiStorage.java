package com.example.telegramdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.badoualy.telegram.api.TelegramApiStorage;
import com.github.badoualy.telegram.mtproto.auth.AuthKey;
import com.github.badoualy.telegram.mtproto.model.DataCenter;
import com.github.badoualy.telegram.mtproto.model.MTSession;

public class ApiStorage implements TelegramApiStorage {

    //Create File variable for auth.key and dc.save
    public static final File AUTH_KEY_FILE = new File("/data/auth.key");
    public static final File NEAREST_DC_FILE = new File("/data/dc.save");

    @Override
    public void saveAuthKey(@NotNull AuthKey authKey) {
        try {
            FileUtils.writeByteArrayToFile(AUTH_KEY_FILE, authKey.getKey());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
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
    public void saveDc(@NotNull DataCenter dataCenter) {
        try {
            FileUtils.write(NEAREST_DC_FILE, dataCenter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
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
    public void saveSession(@Nullable MTSession session) {

    }

    @Nullable
    @Override
    public MTSession loadSession() {
        return null;
    }
}
