package Shared_Memory_Simulation;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Nodes {

    public static int[] status;
    public static boolean end = true;

    public static int randomNum(int min, int max) {
        Random r = new Random();
        int a;
        do {
            a = r.nextInt(max + 1); // 0-7
        } while (a < min);
        System.out.println(a + " servers has been created");
        return a;
    }

    public static double randomTimeVariable() {
        double result, num;
        while (true) {
            Random r = new Random();
            num = r.nextDouble(1);
            result = -10 * Math.log(num);
            if (result < 10 || result > 20) {
                continue;
            } else {
                break;
            }
        }
        return result;
    }

    public static int[] malfunctionNodes(int num) {

        int[] nodesStatus = new int[num];
        for (int i = 0; i < num; i++) {
            nodesStatus[i] = 1;
        }
        int failMax;
        if ((num + 1) % 2 != 0) {
            failMax = num - (((num + 1) / 2) + 1);
        } else {
            failMax = num - (((num + 1) / 2));
        }
        //System.out.println("failMax " + failMax);

        /* find the fail number */
        Random r1 = new Random();
        int failNum;
        do {
            failNum = r1.nextInt(failMax + 1);
        } while (failNum < 1); //1-2
        System.out.println("Fail Nodes Number" + failNum);

        int a;
        int j = 0;

        while (j < failNum) {
            Random r = new Random();
            a = r.nextInt(num);
            if (nodesStatus[a] != 0) {
                nodesStatus[a] = 0;
                j++;
            } else {
                continue;
            }
        }

        System.out.print("Status of nodes: ");
        for (int n = 0; n < num; n++) {
            System.out.print(nodesStatus[n] + " ");
        }
        System.out.print("\n");


        return nodesStatus;

    }

    public static void main() throws IOException {
        /* generate the number of servers */
        System.out.println("Please input the scope of the number of nodes: ");
        Scanner scanInput = new Scanner(System.in);
        int numMin = scanInput.nextInt();
        int numMax = scanInput.nextInt();
        int numOfServers;
        numOfServers = Nodes.randomNum(numMin, numMax);
        System.out.println("here");
        ServerSocket[] servers = new ServerSocket[numOfServers];
        for (int j = 0; j < numOfServers; j++) {
            servers[j] = new ServerSocket(10000 + j);
        }

        /* set the status of server */
        status = new int[numOfServers];
        status = Nodes.malfunctionNodes(numOfServers);


        /* We call the writer and reader are clients */
        ClientHandler[] clientHandler = new ClientHandler[numOfServers];
        updateStatus update = new updateStatus(numOfServers);
        update.start();
        for (int i = 0; i < numOfServers; i++) {
            clientHandler[i] = new ClientHandler(servers[i], i + 1);
            clientHandler[i].start();

        }

    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private ServerSocket server;
        private boolean closeSocket = false;
        private int count;
        private Message messageObject = new Message(0, 0, "nothing");

        ClientHandler(ServerSocket server, int count) {
            this.server = server;
            this.count = count;
        }

        public void run() {
            super.run();

            while (true) {
                try {
                    this.socket = server.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {

                    /* Output and Input */
                    OutputStream outputStream = socket.getOutputStream();
                    ObjectOutputStream os = new ObjectOutputStream(outputStream);
                    InputStream inputStream = socket.getInputStream();
                    ObjectInputStream is = new ObjectInputStream(inputStream);

                    /* read the message from reader and writer */
                    Message temp = (Message) is.readObject();

                    /* if the status of the node is ok, then it will respond the reader and writer */
                    if (status[count - 1] == 1) {
                        if ("readInfo".equalsIgnoreCase(temp.getMessage())) {
                            os.writeObject(new Message(messageObject.getTag(), messageObject.getWriterNum(), messageObject.getMessage()));

                        } else {

                            if (temp.getTag() > messageObject.getTag()) {
                                this.messageObject = temp;
                                //System.out.println("server " + count + " get " + temp.getMessage() + " str length + " + temp.getMessage().length());
                                os.writeObject(new Message(-1, -1, "received"));
                            } else if (temp.getTag() == messageObject.getTag() && (temp.getWriterNum() != messageObject.getWriterNum())) {
                                this.messageObject = temp;
                                //System.out.println("server " + count + " get " + temp.getMessage() + " str length + " + temp.getMessage().length());
                                os.writeObject(new Message(-1, -1, "received"));
                            } else {
                                //System.out.println("server " + count + " already the Newest");
                                //System.out.println("server " + count + " get " + str + " str length + " + str.length());
                                os.writeObject(new Message(-1, -1, "received but not saved"));

                            }

                            sleep(1000);
                        }
                    }

                } catch (EOFException e) {

                } catch (Exception e) {
                    System.out.println("linked exception");
                    e.printStackTrace();
                }

                if (closeSocket) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }

            }


        }
    }

    private static class updateStatus extends Thread {

        private int numOfServers;

        public updateStatus(int numOfServers) {
            this.numOfServers = numOfServers;
        }

        public void run() {
            super.run();
            while (end) {

                double time = randomTimeVariable();
                time = Math.round(time * 1000);
                try {
                    sleep((int) time);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                status = Nodes.malfunctionNodes(numOfServers);


            }


        }


    }
}


