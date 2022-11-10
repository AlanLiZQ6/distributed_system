package main;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class reader3 {

    private static String control = "";

    public static int workedNodes;

    private String[] message;

    public static int randomNum(int min, int max) {
        Random r = new Random();
        int a;
        do {
            a = r.nextInt(max + 1); // 0-7
        } while (a < min);
        System.out.println("start index is :" + a);
        return a;
    }

    private static boolean isUpdate(String[] string1, String[] information, int readNum) {
        for (int i = 0; i < information.length; i++) {
            if (information[i] != null && (string1[i].charAt(0) < information[i].charAt(0))) {
                /* higher element has been read, update */
                return true;
            }
        }
        /* nothing update */
        return false;
    }

    private static String todo(Socket reader, InputStream Message) throws IOException {

        BufferedReader input = new BufferedReader(new InputStreamReader(Message));
        //System.out.println("1-------");

        // the stream sends the message to node
        OutputStream outputStream = reader.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);
        //System.out.println("2-------");

        // the stream get the ACK message from node
        InputStream inputStream = reader.getInputStream();
        BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(inputStream));
        //System.out.println("3-------");
        boolean flag = true;
        String str = input.readLine();

        socketPrintStream.println("readInfo");

        /* read the message from nodes */

        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {

                String str = socketBufferReader.readLine();
                return str;
            }
        };
        ExecutorService exeservices = Executors.newSingleThreadExecutor();

        Future<String> future = exeservices.submit(task);
        String echo = "";
        try {

            echo = future.get(2, TimeUnit.SECONDS);

        } catch (TimeoutException ex) {
            echo = "noResponse";
        } catch (Exception e) {
            System.out.println("exception attention");
        }

        socketPrintStream.close();
        socketBufferReader.close();

        if (echo == null) {
            echo = "0nothing";
        }

        return echo;


    }

    private static int readerQuorum(int num) {
        int quorum = 0;
        if ((num + 1) % 2 != 0) {
            quorum = (num + 1) / 2 + 1;
        } else {
            quorum = (num + 1) / 2;
        }
        return quorum;
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        /* how many nodes */
        System.out.println("How many nodes will be read: ");
        Scanner scanInput = new Scanner(System.in);
        int num = scanInput.nextInt();

        System.out.println("How many times messages are sent: ");
        Scanner scanInput3 = new Scanner(System.in);
        String numOfMessage = scanInput.next();
        int numOfMessage2 = 0;
        if (numOfMessage.equals("n")) {
            numOfMessage2 = 10;
        } else {
            numOfMessage2 = Integer.parseInt(numOfMessage);
        }

        System.out.println("Set the time interval: ");
        Scanner scanInput2 = new Scanner(System.in);
        int num2 = scanInput2.nextInt();
        double time;
        if (num2 == 0) {
            /* time interval 10-20s */
            time = node3.randomTimeVariable();
            time = Math.round(time * 1000);
        } else {
            time = Math.round(num2 * 1000);
        }




        int tag = 0;
        String finalInfo = "";
        String[] tempInfo = new String[num];
        String[] information = new String[num];

        /* initialize the information */
        for (int j = 0; j < num; j++) {
            tempInfo[j] = "0nothing";
            information[j] = "0nothing";
            System.out.print(information[j] + " ");
        }
        System.out.print("\n");
        System.out.println();

        sleep(500);
        /* ask the message from node */
        String respond;
        InputStream message = new ByteArrayInputStream("readInfo".getBytes());
        int currentTag;
        int sendAll = 0;
        int count = 0;
        while (count < numOfMessage2) {

            /* start to read message from nodes */
            for (int i = 0; i < num; i++) {
                Socket socket = new Socket();
                socket.setSoTimeout(5000);
                socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + i)), 3000);
                //System.out.println("The reader connect successfully.");
                //System.out.println("reader"+ socket.getLocalAddress() + "P: " + socket.getLocalPort());
                //System.out.println("node" + socket.getInetAddress() + "P: " + socket.getPort());
                try {

                    respond = reader3.todo(socket, message);
                    System.out.println("number " + i + " respond " + respond);

                    if (!Objects.equals(respond, "malfunction") && !Objects.equals(respond, "received") &&
                            !Objects.equals(respond, "null") && !Objects.equals(respond, "noResponse")) {
                        information[i] = respond;
                        workedNodes++;
                    }

                } catch (Exception E) {
                    System.out.println("Something wrong");
                }


                /* represent the update step by step */
                if (isUpdate(tempInfo, information, num)) {
                        /* if higher tag appears in the information, then
                        store and show the updated array */
                    for (int m = 0; m < num; m++) {
                        tempInfo[m] = information[m];
                        System.out.print(information[m] + " ");
                    }
                    System.out.print("\n");


                    System.out.println("The current final information is " + finalInfo);
                    System.out.println();
                }

            }

            /* finish reading message and check the updated information */
            for (int n = 0; n < num; n++) {
                if ('0' <= information[n].charAt(0) && '9' >= information[n].charAt(0)) {
                    currentTag = information[n].charAt(0) - '0';
                    //System.out.println("current tag " + currentTag);
                    /* do the final update */
                    if (currentTag > tag) {
                        tag = currentTag;
                        finalInfo = information[n];
                        sendAll = 1;
                        System.out.println("finalInto should be " + finalInfo);
                    }
                    /** Here we have a situation:
                     * When the reader has sent the newest message to all nodes,
                     * some nodes may be malfunction so that they will not receive
                     * the new message. However, if the reader want to continue update
                     * it in the next time, the reader must need to read a higher tag so that
                     * the sendAll variable could be set to 1, or the malfunction nodes will
                     * not be updated.
                     * */
                } else {
                    System.out.println("the message's first char is not a number.");
                }
            }

            if (sendAll == 1) {
                /* send message to all nodes. */
                for (int j = 0; j < num; j++) {
                    InputStream message2 = new ByteArrayInputStream(finalInfo.getBytes());
                    Socket socket2 = new Socket();
                    socket2.setSoTimeout(3000);
                    socket2.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + j)), 3000);
                    System.out.println();
                    System.out.println("The writer connect successfully.");
                    //System.out.println("writer"+ socket.getLocalAddress() + "P: " + socket.getLocalPort());
                    System.out.println("node" + socket2.getInetAddress() + "P: " + socket2.getPort());
                    try {
                        writer3.todo(socket2, message2);
                        sleep(1000);
                    } catch (Exception E) {
                        System.out.println("Something wrong");
                    }
                }
                sendAll = 0;
            } else {
                System.out.println("no message received.");
                for (int i = 0; i < tempInfo.length; i++) {
                    System.out.print(tempInfo[i] + " ");
                }
                System.out.println("\n");
            }

            if (!numOfMessage.equals("n")) {
                count++;
            }
            sleep((int)time);
        }

    }
}