package Shared_Memory_Simulation;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class Writer_2 {

    private static int tag = 1;

    private static Message info;
    private static String control = "";

    private static int workedNode = 0;

    private Socket writer;

    private InputStream Message;

    private static int writerNum = 2;


    public Writer_2(Socket writer, InputStream Message) {
        this.writer = writer;
        this.Message = Message;
    }

    public Writer_2() {

    }

    public static int getTag(String str) {
        int tag = 0;
        if ("noResponse".equals(str)) {

        } else if ("".equals(str) || str == null || "null".equals(str)) {
            tag = 0;
        } else {
            tag = str.charAt(0) - '0';
        }
        return tag;
    }

    public static void main() throws IOException, InterruptedException {

        /**
         System.out.println("How many nodes will be used: ");
         Scanner scanInput = new Scanner(System.in);
         int num = scanInput.nextInt();

         System.out.println("How many times messages are sent: ");
         Scanner scanInput3 = new Scanner(System.in);
         String numOfMessage = scanInput3.next();
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
         }**/

        /* 10 nodes, 10 loops, and random interval time */
        int num = 10;
        String numOfMessage = "a";
        int numOfMessage2 = 10;
        double time;
        time = Nodes.randomTimeVariable();
        time = Math.round(time * 1000);

        int count = 0;
        int messageTag ;
        while (count < numOfMessage2) {
            //System.out.println("at here");
            /* find the tag */


            for (int i = 0; i < num; i++) {
                /* refresh the inputStream */
                Socket socket = new Socket();
                socket.setSoTimeout(3000);
                socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + i)), 3000);
                //System.out.println("The writer connect successfully.");
                //System.out.println("writer"+ socket.getLocalAddress() + "P: " + socket.getLocalPort());
                //System.out.println("node" + socket.getInetAddress() + "P: " + socket.getPort());

                /* get the current tag */
                Message askTag = new Message(0,0,"readInfo");
                messageTag = Writer_1.toRead(socket, askTag).getTag();
                //System.out.println("messageTag: " + messageTag);

                /* generate new message */
                info = new Message(messageTag+1, 2, "writer");

                /* writing method */
                Socket socketWrite = new Socket();
                socketWrite.setSoTimeout(3000);
                socketWrite.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + i)), 3000);
                Writer_1.todo(socketWrite, info, i+1,writerNum);
                sleep(1000);

            }
            System.out.println("Working Nodes: " + workedNode);
            workedNode = 0;


            count++;
            sleep((int) time);
        }
        //System.out.println("at here3");

    }

}
