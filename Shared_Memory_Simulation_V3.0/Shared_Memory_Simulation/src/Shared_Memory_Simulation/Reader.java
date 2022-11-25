package Shared_Memory_Simulation;


import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class Reader {

    public static int workedNodes;

    public static int writerNum = -1;

    public static int randomNum(int min, int max) {
        Random r = new Random();
        int a;
        do {
            a = r.nextInt(max + 1); // 0-7
        } while (a < min);
        System.out.println("start index is :" + a);
        return a;
    }

    public static boolean isUpdate(Message[] tempInfo, Message[] information, int readNum) {
        for (int i = 0; i < information.length; i++) {
            if (information[i] != null && (tempInfo[i].getTag() < information[i].getTag())) {
                /* higher element has been read, update */
                return true;
            } else if (information[i] != null && (tempInfo[i].getTag() == information[i].getTag())) {
                if (tempInfo[i].getWriterNum() != (information[i].getWriterNum())) {
                    return true;
                }
            }
        }
        /* nothing update */
        return false;
    }

    public static Message todo(Socket reader, Message message) throws IOException {
        Message echo = new Message(-2, -2, "");

        try {
            /* initialize the IO */
            OutputStream outputStream = reader.getOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            InputStream inputStream = reader.getInputStream();
            ObjectInputStream is = new ObjectInputStream(inputStream);

            /* send the asking message to nodes */
            os.writeObject(message);

            /* read the message from nodes */
            Callable<Message> task = new Callable<Message>() {
                @Override
                public Message call() throws Exception {

                    return (Message) is.readObject();
                }
            };
            ExecutorService exeservices = Executors.newSingleThreadExecutor();
            Future<Message> future = exeservices.submit(task);
            try {

                echo = future.get(2, TimeUnit.SECONDS);

            } catch (TimeoutException ex) {
                echo = new Message(-2, -2, "noResponse");
            } catch (Exception e) {
                System.out.println("exception attention");
                e.printStackTrace();
            }

            reader.close();

            if (echo == null) {
                echo = new Message(0, 0, "Nothing");
            }

            return echo;

        } catch (EOFException e) {

        }

        return echo;


    }

    public static void main() throws IOException, InterruptedException {

        /* debugging mode */
        /**
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
         time = Nodes.randomTimeVariable();
         time = Math.round(time * 1000);
         } else {
         time = Math.round(num2 * 1000);
         }
         **/

        /* 10 nodes, 10 loops, and random interval time */
        int num = 10;
        String numOfMessage = "a";
        int numOfMessage2 = 10;
        double time;
        time = Nodes.randomTimeVariable();
        time = Math.round(time * 1000);


        int tag = 0;
        Message finalInfo = new Message(0, 0, "");
        Message[] tempInfo = new Message[num];
        Message[] information = new Message[num];

        /* initialize the information */
        for (int j = 0; j < num; j++) {
            tempInfo[j] = new Message(0, 0, "nothing");
            information[j] = new Message(0, 0, "nothing");
            System.out.print(information[j].getTag() + information[j].getMessage() + information[j].getWriterNum() + " ");
        }
        System.out.print("\n");

        sleep(500);
        /* ask the message from node */
        Message respond;
        Message message = new Message(0, 0, "readInfo");
        int currentTag;
        int sendAll = 0;
        int count = 0;
        while (count < numOfMessage2) {

            /* start to read message from nodes */
            for (int i = 0; i < num; i++) {
                Socket socket = new Socket();
                socket.setSoTimeout(5000);
                socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + i)), 3000);
                try {

                    respond = Reader.todo(socket, message);
                    //System.out.println("number " + i + " respond " + respond);

                    if (!Objects.equals(respond.getMessage(), "malfunction") && !Objects.equals(respond.getMessage(), "received") &&
                            !Objects.equals(respond.getMessage(), "null") && !Objects.equals(respond.getMessage(), "noResponse")) {
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
                        System.out.print(information[m].getTag() + information[m].getMessage() + information[m].getWriterNum() + " ");
                    }
                    System.out.print("\n");


                    //System.out.println("The current final information is " + finalInfo.getTag() + finalInfo.getMessage() + finalInfo.getWriterNum());
                }

            }

            /* finish reading message and check the updated information */
            for (int n = 0; n < num; n++) {
                currentTag = information[n].getTag();
                if (currentTag > tag) {
                    tag = currentTag;
                    finalInfo = information[n];
                    sendAll = 1;
                    System.out.println("finalInto should be " + finalInfo.getTag() + finalInfo.getMessage() + finalInfo.getWriterNum());
                }

            }


            if (sendAll == 1) {
                System.out.println("Reader start to update the message in Nodes");
                /* send message to all nodes. */
                for (int j = 0; j < num; j++) {
                    Socket socket2 = new Socket();
                    socket2.setSoTimeout(3000);
                    socket2.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + j)), 3000);
                    //System.out.println("The writer connect successfully.");
                    //System.out.println("node" + socket2.getInetAddress() + "P: " + socket2.getPort());
                    try {
                        Writer_1.todo(socket2, finalInfo,j+1, writerNum);
                        sleep(1000);
                    } catch (Exception E) {
                        System.out.println("Something wrong");
                    }
                }
                sendAll = 0;
            } else {
                System.out.println("no message received.");
                for (int i = 0; i < tempInfo.length; i++) {
                    System.out.print(tempInfo[i].getTag()+tempInfo[i].getMessage()+tempInfo[i].getWriterNum() + " ");
                }
                System.out.println("\n");
            }

            count++;
            sleep((int) time);
        }
    }

}



