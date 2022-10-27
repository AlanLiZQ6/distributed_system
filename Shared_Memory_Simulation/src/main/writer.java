package main;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class writer {

    static void todo(Socket writer, InputStream Message) throws IOException{

        BufferedReader input = new BufferedReader(new InputStreamReader(Message));
        //System.out.println("1-------");
        // the stream sends the message to node
        OutputStream outputStream = writer.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);
        //System.out.println("2-------");
        // the stream get the ACK message from node
        InputStream inputStream = writer.getInputStream();
        BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(inputStream));
        //System.out.println("3-------");
        boolean flag = true;
        do {
            //System.out.println("4-------");
            String str = input.readLine();
            /* send the message to one node */
            //System.out.println("5-------");
            socketPrintStream.println(str);
            //System.out.println("6-------");
            // read the message from nodes
            String echo = socketBufferReader.readLine();

            if ("received".equalsIgnoreCase(echo)) {
                System.out.println("Node has received the message.");
                flag = false;
            }else{
                System.out.println(echo);
                flag = false;
            }

        }while (flag);

        socketPrintStream.close();
        socketBufferReader.close();


    }

    public static void main(String[] args) throws IOException, InterruptedException {

        /* how many nodes */
        System.out.println("How many nodes will be used: ");
        Scanner scanInput = new Scanner(System.in);
        int num = scanInput.nextInt();

        /* how many messages to send  */
        System.out.println("How many messages to send: ");
        Scanner scanInput2 = new Scanner(System.in);
        int num2 = scanInput2.nextInt();
        String[] messageSet = new String[num2];

        /* generate the message */
        for (int i = 0; i < num2; i++) {
            System.out.println("Please input the " + (i+1) + " message in this time: ");
            Scanner in = new Scanner(System.in);
            messageSet[i] = in.next();
        }

        int w = 0;
        while (w < num2){
            sleep(1000);
            for (int i = 0; i < num; i++) {
                /* refresh the inputStream */
                InputStream message = new ByteArrayInputStream(messageSet[w].getBytes());

                Socket socket = new Socket();
                socket.setSoTimeout(3000);
                socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + i)), 3000);
                System.out.println();
                System.out.println("The writer connect successfully.");
                //System.out.println("writer"+ socket.getLocalAddress() + "P: " + socket.getLocalPort());
                System.out.println("node" + socket.getInetAddress() + "P: " + socket.getPort());
                try {
                    writer.todo(socket, message);
                } catch (Exception E) {
                    System.out.println("Something wrong");
                }
            }
            w++;

        }


    }

}
