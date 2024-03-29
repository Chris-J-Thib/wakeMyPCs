package com.wakeMyPCs;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class SSHManager {


    private static String executeTask(String host, String username, String password, int port, String command) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(15000);

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            InputStream in = channel.getInputStream();

            StringBuilder result = new StringBuilder();

            CompletableFuture.runAsync(() -> {
                try {
                    channel.connect();
                    byte[] tmp = new byte[1024];
                    while (true) {
                        while (in.available() > 0) {
                            int i = in.read(tmp, 0, 1024);
                            if (i < 0)
                                break;
                            result.append(new String(tmp, 0, i));
                        }
                        if (channel.isClosed()) {
                            if (in.available() > 0)
                                continue;
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    channel.disconnect();
                    session.disconnect();
                }
            }).get();

            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    public static String executeCommand(String host, String username,
                                                   String password, int port,
                                                   String command) {
        return executeTask(host,username, password, port, command);
    }

}
