package com.broll.networklib.client.auth;

import com.esotericsoftware.minlog.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TempFileAccess implements IFileAccess {

    private File file;

    public TempFileAccess(String fileName) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        file = new File(tmpdir + fileName);
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public String read() {
        try {
            return FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            Log.error("Failed to read file", e);
            return null;
        }
    }

    @Override
    public void write(String content) {
        try {
            FileUtils.writeStringToFile(file, content, "UTF-8");
        } catch (IOException e) {
            Log.error("Failed to write file", e);
        }
    }

    @Override
    public void delete() {
        file.delete();
    }
}
