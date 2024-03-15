package io.codeforall.javatars.Server;

import io.codeforall.javatars.Server.ClientHandlers.ChatClientHandler;
import io.codeforall.javatars.Server.ClientHandlers.ClientHandler;
import io.codeforall.javatars.Server.ClientHandlers.QuizClientHandler;
import io.codeforall.javatars.Server.Questions.MultipleChoiceQuestion;
import io.codeforall.javatars.Server.Questions.OpenEndedQuestion;
import io.codeforall.javatars.Server.Questions.Question;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class QuizChatServer {

    private int chatPort = 8080; // Example port for chat service
    private int quizPort = 8081; // Example port for quiz service
    private ExecutorService maximumEffort = Executors.newCachedThreadPool();

    // Map to associate session tokens with client handlers
    private final ConcurrentHashMap<String, ClientHandler> sessionTokens = new ConcurrentHashMap<>();

    // List to track all clients connected to the chat
    private final List<ClientHandler> chatClients = Collections.synchronizedList(new ArrayList<>());

    // Queue for clients who want to join the quiz
    private final Queue<ClientHandler> quizQueue = new ConcurrentLinkedQueue<>();

    // List to track all clients connected to the quiz
    private Set<ClientHandler> quizParticipants = ConcurrentHashMap.newKeySet();

    // Questions for the quiz
    private List<Question> questions = new ArrayList<>();

    private volatile boolean isGameStarted = false;


    public static void main(String[] args) {
        QuizChatServer server = new QuizChatServer();
        server.startServices(); // Start both chat and quiz services
    }

    public void startServices() {
        // Load quiz questions here
        loadQuizQuestions();

        // Start Chat Service
        new Thread(() -> startChatService(chatPort)).start();

        // Start Quiz Service
        new Thread(() -> startQuizService(quizPort)).start();
    }

    // Generate a unique session token
    public String generateSessionToken() {
        return UUID.randomUUID().toString();
    }

    // Register the client with the generated token
    public void registerClient(String token, ClientHandler client) {
        sessionTokens.put(token, client);
    }

    // Method to retrieve a client by token
    public ClientHandler getClientByToken(String token) {
        return sessionTokens.get(token);
    }

    private void assignTokenToChatClient(ClientHandler chatClient) {
        String token = generateSessionToken();
        chatClient.sendMessage("Your session token: " + token);
        registerClient(token, chatClient);
    }

    // Method to handle /join command from clients
    public void registerForQuiz(String token, ClientHandler client) {
        ClientHandler registeredClient = getClientByToken(token);
        if (registeredClient != null && !hasGameStarted()) {
            quizQueue.add(registeredClient);
            registeredClient.sendMessage("You've joined the quiz queue. Connect to port " + quizPort + " for quiz participation.");
        } else {
            client.sendMessage("Unable to join the quiz. Either the game has started or the token is invalid.");
        }
    }

    private void startChatService(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat service running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ChatClientHandler(clientSocket, this);
                chatClients.add(clientHandler);
                assignTokenToChatClient(clientHandler);
                maximumEffort.execute(clientHandler);
            }
        } catch (IOException e) {
            System.out.println("Exception in chat service: " + e.getMessage());
        }
    }

    private void startQuizService(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Quiz service running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                QuizClientHandler clientHandler = new QuizClientHandler(clientSocket, this);
                String token = clientHandler.receiveInitialToken();
                if (validateQuizParticipantToken(token, clientHandler)) {
                    quizParticipants.add(clientHandler); // Validated client becomes an active participant
                    maximumEffort.execute(clientHandler);
                } else {
                    clientHandler.sendMessage("Invalid session token. Connection denied.");
                    clientHandler.closeResources();
                }
            }
        } catch (IOException e) {
            System.out.println("Exception in quiz service: " + e.getMessage());
        }
    }

    public boolean validateQuizParticipantToken(String token, QuizClientHandler clientHandler) {
        ClientHandler registeredClient = getClientByToken(token);
        if (registeredClient != null && quizQueue.remove(registeredClient)) {
            // Client was in the queue and is now validated for quiz participation
            return true;
        }
        return false;
    }

    private void loadQuizQuestions() {
        // Load or define quiz questions here
        questions.add(new MultipleChoiceQuestion("What is 2+2?", Arrays.asList("2", "3", "4", "5"), "4"));
        questions.add(new OpenEndedQuestion("Name the largest ocean on Earth.", "Pacific Ocean"));
    }

    public synchronized void startQuiz() {
        if (!hasGameStarted()) {
            isGameStarted = true;
            System.out.println("Quiz has started!");
            broadcastQuizMessage("Server: The quiz has started!");

            // Transition clients from queue to active participants
            quizParticipants.forEach(participant ->
                    participant.sendMessage("The quiz is now starting. Good luck!"));

            // Start handling quiz questions
            //handleQuizQuestion();
        }
    }

    public void broadcastChatMessage(String message) {
        for (ClientHandler client : chatClients) {
            client.sendMessage(message);
        }
    }

    public void broadcastQuizMessage(String message) {
        for (ClientHandler client : quizParticipants) {
            client.sendMessage(message);
        }
    }

    public synchronized boolean hasGameStarted() {
        return isGameStarted;
    }

    public List<ClientHandler> getChatClients() {
        return chatClients;
    }

    public Set<ClientHandler> getQuizClients() {
        return quizParticipants;
    }

}
