package io.codeforall.javatars.Server.ClientHandlers;

import io.codeforall.javatars.Server.QuizChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class QuizClientHandler extends ClientHandler{

    private int score = 0;


    public QuizClientHandler(Socket socket, QuizChatServer server) throws IOException {
        super(socket, server);
    }

    public String receiveInitialToken() throws IOException {
        // Assuming 'in' is the BufferedReader for client input
        if (in.ready()) {
            return in.readLine(); // Read the first line as the token
        }
        return null;
    }

    @Override
    public void closeResources() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.getQuizClients().remove(this);
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (!socket.isClosed()) {



            }
        } catch (IOException e) {
            System.out.println("Error handling client " + nickname);
        } finally {
            closeResources();
        }
    }
}
