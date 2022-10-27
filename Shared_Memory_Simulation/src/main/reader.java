package main;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class reader {

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
            if (information[i] != null && !string1[i].equalsIgnoreCase(information[i])) {
                /* new element has been read, update */
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
        String echo = socketBufferReader.readLine();


        if (echo == null) {
            return "nothing";
        }

        socketPrintStream.close();
        socketBufferReader.close();

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
        int readNum = readerQuorum(num);
        System.out.println("will read number " + readNum);
        System.out.println();
        String[] information = new String[readNum];
        String[] tempInfo = new String[readNum];
        int startIndex = randomNum(0, num - readNum);
        int tag = 0;
        String finalInfo = "";

        /* initialize the information */
        for (int j = 0; j < readNum; j++) {
            tempInfo[j] = "nothing";
            information[j] = "nothing";
            System.out.print(information[j] + " ");
        }
        System.out.print("\n");
        System.out.println();

        boolean isEnd = true;
        while (isEnd) {
            sleep(500);
            /* ask the message from node */
            String respond;
            InputStream message = new ByteArrayInputStream("readInfo".getBytes());
            for (int i = startIndex; i < startIndex + readNum; i++) {
                Socket socket = new Socket();
                socket.setSoTimeout(5000);
                socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + i)), 3000);
                //System.out.println("The reader connect successfully.");
                //System.out.println("reader"+ socket.getLocalAddress() + "P: " + socket.getLocalPort());
                //System.out.println("node" + socket.getInetAddress() + "P: " + socket.getPort());
                try {

                    respond = reader.todo(socket, message);
                    if ("end".equalsIgnoreCase(respond)) {
                        isEnd = false;
                        break;
                    }
                    if (!Objects.equals(respond, "malfunction") && !Objects.equals(respond, "received") && !Objects.equals(respond, "null")) {
                        information[i - startIndex] = respond;
                    }

                } catch (Exception E) {
                    System.out.println("Something wrong");
                }


                /* represent the update step by step */
                if (isUpdate(tempInfo, information, readNum)) {
                    /* update */
                    for (int m = 0; m < readNum; m++) {
                        tempInfo[m] = information[m];
                        System.out.print(information[m] + " ");
                    }

                    System.out.print("\n");

                    for (int n = 0; n < readNum; n++) {
                        if ('0' < information[n].charAt(0) && '9' >= information[n].charAt(0)) {
                            int currentTag = information[n].charAt(0) - '0';
                            //System.out.println("current tag " + currentTag);
                            /* do the final update */
                            if (currentTag > tag) {
                                finalInfo = information[n];
                                //System.out.println("finalInto should be " + finalInfo);
                                tag = currentTag;
                            }
                        }
                    }
                    System.out.println("The current final information is " + finalInfo);
                    System.out.println();
                }

            }




        }
    }
}
