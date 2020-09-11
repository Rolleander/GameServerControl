package com.broll.networklib.client.auth;

public class LastConnection {
    private static IFileAccess fileAccess = new TempFileAccess("BRollLastNetworkConnection.dat");

    public static void setFileAccess(IFileAccess fileAccess) {
        LastConnection.fileAccess = fileAccess;
    }

    public static void clear() {
        fileAccess.delete();
    }

    public static String getLastConnection() {
        if (fileAccess.exists()) {
            return fileAccess.read();
        }
        return null;
    }

    public static void setLastConnection(String connection) {
        fileAccess.write(connection);
    }
}
