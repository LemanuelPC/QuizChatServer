package io.codeforall.javatars.Server.ClientHandlers;

import io.codeforall.javatars.Server.QuizChatServer;

import java.io.*;
import java.net.*;

public abstract class ClientHandler implements Runnable{

    Socket socket;
    QuizChatServer server;
    PrintWriter out;
    BufferedReader in;
    String nickname;


    public ClientHandler(Socket socket, QuizChatServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public abstract void closeResources();

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public abstract void run();
}
