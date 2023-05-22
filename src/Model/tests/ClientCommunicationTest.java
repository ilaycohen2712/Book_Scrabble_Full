package Model.tests;

import Model.ClientCommunication;
import Model.gameClasses.BookScrabbleHandler;
import Model.gameClasses.ClientHandler;
import Model.gameClasses.DictionaryManager;
import Model.gameClasses.MyServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class ClientCommunicationTest {

    public static void testSend() {
        ServerSocket server = null;
        ClientCommunication client = null;
        Test test = null;
        try{
            server = new ServerSocket(1234);
            client = new ClientCommunication("localhost", 1234);
            Socket socket = server.accept();
            Thread.sleep(1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = in.readLine();
            if(line == null || !line.equals("-1:connect:")){
                System.out.println("expected: -1:connect: -> received: " + line);
                System.out.println("ERROR: testSend failed (1)");
            }else {
                System.out.println("testSend passed (1)");
            }
            client.send(1, "testMethod", "input1", "input2");
            line = in.readLine();
            if(line == null || !line.equals("1:testMethod:input1,input2")){
                System.out.println("expected: 1:testMethod:input1,input2 -> received: " + line);
                System.out.println("ERROR: testSend failed (2)");
            }else {
                System.out.println("testSend passed (2)");
            }
            client.close();

        }catch (Exception e) {
            System.out.println("ERROR: " + e);
            throw new RuntimeException("testSend failed");
        }
    }

    public static void testCheckForMessage() {
        ServerSocket server = null;
        ClientCommunication client = null;
        Test test = null;
        try {
            server = new ServerSocket(1235);
            client = new ClientCommunication("localhost", 1235);
            test = new Test(client);
            Socket socket = server.accept();
            socket.getOutputStream().write("test1\n".getBytes());
            Thread.sleep(1000);
            if(test.lastMessage == null || !test.lastMessage.equals("test1")) {
                System.out.println("ERROR: testCheckForMessage failed -100pts");
                throw new RuntimeException("testCheckForMessage failed");
            }else {
                System.out.println("testCheckForMessage test1 passed");
            }

            socket.getOutputStream().write("test2\n".getBytes());
            Thread.sleep(1000);
            if(test.lastMessage == null || !test.lastMessage.equals("test2")) {
                System.out.println("ERROR: testCheckForMessage failed -100pts");
                throw new RuntimeException("testCheckForMessage failed");
            }else {
                System.out.println("testCheckForMessage test2 passed");
            }

            socket.getOutputStream().write("test3\n".getBytes());
            Thread.sleep(1000);
            if(test.lastMessage == null || !test.lastMessage.equals("test3")) {
                System.out.println("ERROR: testCheckForMessage failed -100pts");
                throw new RuntimeException("testCheckForMessage failed");
            }
            else {
                System.out.println("testCheckForMessage test3 passed");
            }

            client.close();

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage() + " -100pts");
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println("- - - - Testing ClientCommunication - - - -");
        System.out.println("> > > checkForMassage < < <");
        testCheckForMessage();
        System.out.println("CheckForMessage passed!\n");
        System.out.println("> > > send < < <");
        testSend();
        System.out.println("Send passed!");

        System.out.println("All tests passed!");
    }

    public static class Test implements Observer {

        public String lastMessage = null;

        public Test(Observable o) {
            o.addObserver(this);
        }

        public void update(java.util.Observable o, Object arg) {
            lastMessage = (String) arg;
        }
    }
}
