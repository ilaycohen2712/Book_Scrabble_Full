package Model;

import Model.gameClasses.ClientHandler;
import Model.gameClasses.MyServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

public class HostServer extends Observable {
    HashMap<Integer, Socket> clientsSockets;
    ClientHandler clientHandler;
    private volatile boolean stop;
    private final int myPort;
    private final int bookScrabbleServerPort;
    private final String bookScrabbleServerIp;
    private final List<String> bookNames;
    ServerSocket server = null;

    /***
     * HostServer constructor --> create a new HostServer.
     * @param myPort --> the port of the HostServer
     * @param gameServerIp --> the ip of the game server
     * @param gameServerPort --> the port of the game server
     */
    public HostServer(int myPort, ClientHandler ch, String gameServerIp, int gameServerPort) {
        //do something
        clientsSockets = new HashMap<>();
        clientHandler = ch;
        stop = false;

        this.myPort = myPort;
        this.bookScrabbleServerIp = gameServerIp;
        this.bookScrabbleServerPort = gameServerPort;
        bookNames = new ArrayList<>();
        bookNames.add("Frank Herbert - Dune.txt");
        bookNames.add("mobydick.txt");
        startServer();
    }

    public List<String> getBookNames() {
        return bookNames;
    }
    /***
     * runServer function --> to run the server and handle clients
     */
    private void runServer() {

        try {
            server = new ServerSocket(myPort);
            new Thread(this::checkForMessages).start();
            server.setSoTimeout(1000);
            while (!stop) {
                try {
                    Socket aClient = server.accept();
                    HostModel.getHost().addPlayer(aClient);

                    new Thread(()->{
                        try {
                            clientHandler.handleClient(aClient.getInputStream(), aClient.getOutputStream());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (SocketTimeoutException e) {
                }//blocking call
            }
            close();
        } catch (IOException e) {}

    }

    /***
     * startServer --> start the server in a thread
     */
    public void startServer() {
        new Thread(this::runServer).start();
    }

    public void stop() {
        stop = true;
    }

    /***
     * close --> close the server and all the sockets
     */
    public void close() {
        stop();
        clientHandler.close();
        for (Socket socket : clientsSockets.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            server.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * checkForMessages --> check if there are messages from the clients in a loop inside a thread
     */
    private void checkForMessages() {
        while (!stop) {
            for (Socket socket : clientsSockets.values()) {
                try {
                    if (socket.getInputStream().available() > 0) {
                        clientHandler.handleClient(socket.getInputStream(), socket.getOutputStream());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /***
     * addSocket --> add a socket to the clientsSockets map
     * @param id --> the id of the Player
     * @param socket --> the socket of the Player
     */
    public void addSocket(int id, Socket socket) {
        clientsSockets.put(id, socket);
    }


    /***
     * sendToGameServer --> send a query or a challenge to the game server
     * @param type --> "Q" for query or "C" for challenge
     * @param word --> the word to be queried or challenged
     */
    public Socket sendToBookScrabbleServer(String type, String word) {
        try {
            Socket bookScrabbleServer = new Socket(bookScrabbleServerIp, bookScrabbleServerPort);
            PrintWriter out = new PrintWriter(bookScrabbleServer.getOutputStream());
            StringBuilder msg = new StringBuilder();
            msg.append(type + ",");
            for (String bookName : bookNames) {
                msg.append(bookName + ",");
            }
            msg.append(word);
            out.println(msg.toString());
            out.flush();
            return bookScrabbleServer;
        } catch (IOException e){
            return null;
        }

    }

    /***
     * sendToSpecificPlayer --> send a message to a specific player
     * @param playerID --> the id of the player
     * @param methodName --> the method to notify the players about
     * @param output --> the output of the method
     */
    public void sendToSpecificPlayer(int playerID, String methodName, String output) {
        Socket socket = clientsSockets.get(playerID);
        if (socket == null) {
            throw new RuntimeException("Player with id " + playerID + " is not connected to the server");
        }
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(playerID + ":" + methodName + ":" + output);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (RuntimeException e){
            System.out.println(e.getMessage());
        }
    }

    /***
     * sendToAllPlayers --> send a message to all players
     * @param playerID --> the id of the player to notify about
     * @param methodName --> the method to notify the players about
     * @param output --> the output of the method
     */
    public void sendToAllPlayers(int playerID, String methodName, String output) {
        for (Socket socket : clientsSockets.values()) {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.println(playerID+ ":" + methodName + ":" + output);
                out.flush();
            } catch (IOException e) {}
        }
    }

    public void setChanged(){
        super.setChanged();
    }

}
