package soketio;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private int port;
    private String ip;
    private Scanner scanner;

    public Client(int port, String ip) {
        this.port = port;
        this.ip = ip;
        scanner = new Scanner(System.in);
    }

    public void start() throws Exception {
        System.out.println("Введите имя");
        String name = scanner.nextLine();
        Connection connection = new Connection(getSocket());

        Thread sender = new Thread(() -> {
            try {
                while (true) {
                    System.out.println("Введите сообщение");
                    String messageText = scanner.nextLine();
                    if (messageText.equalsIgnoreCase("exit")) {
                        connection.close();
                        break;
                    }
                    connection.sendMessage(SimpleMessage.getMessage(name, messageText));
                    System.out.println(Thread.currentThread().getName());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread getter = new Thread(() -> {
            try {
                while (!sender.isInterrupted()) {
                    SimpleMessage fromServer = connection.readMessage();
                    System.out.println(fromServer + " " + Thread.currentThread().getName());
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        sender.start();
        getter.start();


    }


    private Socket getSocket() throws IOException {
        Socket socket = new Socket(ip, port);
        return socket;
    }


    public static void main(String[] args) {
        int port = 8090;
        String ip = "127.0.0.1";

        try {
            new Client(port, ip).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}