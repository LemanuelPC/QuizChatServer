package io.codeforall.javatars.Server.ClientHandlers;

import io.codeforall.javatars.Server.QuizChatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClientHandler extends ClientHandler{
    public ChatClientHandler(Socket socket, QuizChatServer server) throws IOException {
        super(socket, server);
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
        server.getChatClients().remove(this);
    }

    @Override
    public void run() {
        try {

            // Initial communication with client for nickname
            requestAndSetNickname();

            while (!socket.isClosed()) {
                if (in.ready()) {
                    String input = in.readLine();
                    if ("/join".equals(input.trim())) {
                        if (!server.hasGameStarted()) {
                            // The client should include their token in the join request; this is a conceptual line
                            //String clientToken = extractTokenFromJoinRequest(input);
                            //server.registerForQuiz(clientToken, this);
                            server.registerForQuiz("xxx", this);
                        } else {
                            sendMessage("The quiz has already started. You cannot join at this time.");
                        }
                    } else {
                        // Normal chat processing
                        System.out.println(nickname + ": " + input); // Display on server console for projection
                        server.broadcastChatMessage(nickname + ": " + input); // Broadcast to all clients
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client " + nickname);
        } finally {
            closeResources();
        }
    }

    private void requestAndSetNickname() throws IOException {
        out.println("Enter your nickname:");
        nickname = in.readLine();
        System.out.println(nickname + " has joined.");
        server.broadcastChatMessage(nickname + " has joined.");
    }

}
