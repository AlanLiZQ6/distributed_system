package Shared_Memory_Simulation;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class Writer_1 {

    private static Message info;

    private static int workedNode = 0;

    private Socket writer;

    private InputStream Message;

    private static int writerNum = 1;

    public Writer_1(Socket writer, InputStream Message) {
        this.writer = writer;
        this.Message = Message;
    }

    public Writer_1() {

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

    static void todo(Socket writer, Message message, int nodeNum, int writerNum) throws IOException {
        try {
            /* initialize the IO */
            OutputStream outputStream = writer.getOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            InputStream inputStream = writer.getInputStream();
            ObjectInputStream is = new ObjectInputStream(inputStream);

            /* send message to node */
            os.writeObject(message);


            Callable<Message> task = new Callable<Message>() {
                @Override
                public Message call() throws Exception {

                    return (Message) is.readObject();

                }
            };
            ExecutorService exeservices = Executors.newSingleThreadExecutor();

            Future<Message> future = exeservices.submit(task);
            Message echo = new Message(-2, -2, "");
            try {

                echo = future.get(2, TimeUnit.SECONDS);

            } catch (Exception e) {

            }

            if ("received".equalsIgnoreCase(echo.getMessage())) {
                //System.out.println("Node has received the message.");
                workedNode++;

            } else {
                System.out.println("Writer " + writerNum + "-> Node " + nodeNum + " : no response");
            }

            writer.close();
            //System.out.println("disconnect successful");
        }catch (EOFException e){

        }

    }

    public static Message toRead(Socket reader, Message message) throws IOException {
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
                echo = new Message(0, 0, "noResponse");
            } catch (Exception e) {
                System.out.println("exception attention");
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
                info = new Message(messageTag+1, 1, "writer");

                /* writing method */
                Socket socketWrite = new Socket();
                socketWrite.setSoTimeout(3000);
                socketWrite.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + i)), 3000);
                Writer_1.todo(socketWrite, info, i+1, writerNum);
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
