package main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class node3 {

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

    public static double randomTimeVariable(){
        double result, num;
        while(true){
            Random r = new Random();
            num = r.nextDouble(1);
            result = -10*Math.log(num);
            if (result < 10 || result > 20) {
                continue;
            }else{
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
        System.out.println("failMax " + failMax);

        /* find the fail number */
        Random r1 = new Random();
        int failNum;
        do {
            failNum = r1.nextInt(failMax + 1);
        } while (failNum < 1); //1-2
        System.out.println("failNum" + failNum);

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

        for (int n = 0; n < num; n++) {
            System.out.print(nodesStatus[n] + " ");
        }

        return nodesStatus;

    }

    public static void main(String[] args) throws IOException {
        /* generate the number of servers */
        System.out.println("Please input the scope of the number of nodes: ");
        Scanner scanInput = new Scanner(System.in);
        int numMin = scanInput.nextInt();
        int numMax = scanInput.nextInt();
        int numOfServers;
        numOfServers = node3.randomNum(numMin, numMax);
        System.out.println("here");
        ServerSocket[] servers = new ServerSocket[numOfServers];
        for (int j = 0; j < numOfServers; j++) {
            servers[j] = new ServerSocket(10000 + j);
        }

        /* set the status of server */
        status = new int[numOfServers];
        status = node3.malfunctionNodes(numOfServers);


        int count = 0;
        Socket[] clients = new Socket[numOfServers];


        /* We call the writer and reader are clients */
        //Socket[] clients = new Socket[numOfServers];

        //System.out.println("server "+ (count+1) + "linked successful");
        ClientHandler[] clientHandler = new ClientHandler[numOfServers];
        updateStatus update = new updateStatus(numOfServers);
        update.start();
        for (int i = 0; i < numOfServers; i++) {
            clientHandler[i] = new ClientHandler(servers[i], i + 1);
            //??????????????????
            clientHandler[i].start();

        }

    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private ServerSocket server;
        private boolean flag = true;

        private boolean closeSocket = false;

        private int tag;

        private int count;
        //private int status;

        private String message;

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
                System.out.println();
                //System.out.println("server " + count + " linked successful");
                //System.out.println("New client connected: " + socket.getInetAddress() + "P: " + socket.getPort());

                try {
                    // return data to client
                    PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                    //get input, receive message
                    BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    do {
                        String str = socketInput.readLine();
                        if (str != null && str.length() != 0) {

                            if ("disconnect".equalsIgnoreCase(str)) {
                                socketOutput.println("disconnect");
                                this.closeSocket = true;
                                socketInput.close();
                                socketOutput.close();
                                break;
                            }

                            if (status[count-1] == 1) {
                                if ("readInfo".equalsIgnoreCase(str)) {
                                    socketOutput.println(message);
                                    if ("end".equalsIgnoreCase(message)) {
                                        socketOutput.println("end");
                                        break;
                                    }

                                } else {
                                    int temp = str.charAt(0)-'0';
                                    if (temp > tag) {
                                        tag = temp;
                                        this.message = str;
                                        System.out.println("server " + count + " get " + str + " str length + " + str.length());
                                        socketOutput.println("received");
                                    }else{
                                        System.out.println("server " + count + " already the Newest");
                                        //System.out.println("server " + count + " get " + str + " str length + " + str.length());
                                        socketOutput.println("received");
                                    }
                                    sleep(1000);
                                }
                            } else {

                            }
                            flag = false;
                        }
                    } while (flag);


                } catch (Exception e) {
                    System.out.println("??????????????????");
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
            System.out.println("The reader and writer have disconnected.");


        }
    }

    private static class updateStatus extends Thread {

        private int numOfServers;

        public updateStatus(int numOfServers){
            this.numOfServers = numOfServers;
        }

        public void run(){
            super.run();
            while(end){

                double time = randomTimeVariable();
                time = Math.round(time*1000);
                try {
                    sleep((int)time);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                status = node3.malfunctionNodes(numOfServers);


            }


        }


    }
}

