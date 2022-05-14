package soketio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;


public class EchoServer {


    private int port;
    private Connection connection;
    private ArrayBlockingQueue<SimpleMessage> blockingQueue = new ArrayBlockingQueue<>(10, true);
    private CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<>();


    public EchoServer(int port) {
        this.port = port;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }


    public void start() throws IOException, ClassNotFoundException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started...");
            new SendToClients().start();
            while (true) {
                Socket socket = serverSocket.accept();
                connection = new Connection(socket);
                connections.add(connection);
                new Clients().start();
            }
        }
    }

    class Clients extends Thread {


        @Override
        public void run() {
            SimpleMessage message = null;
            try {
                message = connection.readMessage();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            blockingQueue.add(message);
            printMessage(message);
            try {
                connection.sendMessage(SimpleMessage.getMessage("server", message.getText()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class SendToClients extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                SimpleMessage simpleMessage = null;
                try {
                    simpleMessage = blockingQueue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (Connection connection1 : connections) {
                    try {
                        connection1.sendMessage(simpleMessage);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void printMessage(SimpleMessage message) {
        System.out.println("получено сообщение: " + message);
    }

    public static void main(String[] args) {
        int port = 8090;
        EchoServer messageServer = new EchoServer(port);
        try {
            messageServer.start();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}












