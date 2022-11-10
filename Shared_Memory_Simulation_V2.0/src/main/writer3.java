
package main;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class writer3 {

    private static int tag = 1;

    private static String info = "";
    private static String control = "";

    private static int workedNode = 0;

    private Socket writer;

    private InputStream Message;

    public writer3(Socket writer, InputStream Message) {
        this.writer = writer;
        this.Message = Message;
    }

    public writer3() {

    }

    static void todo(Socket writer, InputStream Message) throws IOException {

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
        //System.out.println("4-------");
        String str = input.readLine();
        /* send the message to one node */
        //System.out.println("5-------");
        socketPrintStream.println(str);
        //System.out.println("6-------");
        // read the message from nodes

        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                //设置执行响应时间的方法体
                String str = socketBufferReader.readLine();
                return str;
            }
        };
        ExecutorService exeservices = Executors.newSingleThreadExecutor();

        Future<String> future = exeservices.submit(task);
        String echo = "";
        try {
            //设置我最多等 1s，1s后无论如何我都要响应前端
            echo = future.get(2, TimeUnit.SECONDS);

        } catch (Exception e) {
            //e.printStackTrace();
            //System.err.println("我在规定时间内没返回FAIL呢：这里是异常处理的方法");
        }

        if ("received".equalsIgnoreCase(echo)) {
            System.out.println("Node has received the message.");
            workedNode++;

        } else {
            System.out.println("no response");

        }

        socketPrintStream.close();
        socketBufferReader.close();
        System.out.println("disconnect successful");

    }

    public static void main(String[] args) throws IOException, InterruptedException {


        System.out.println("How many nodes will be used: ");
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

        int count = 0;
        while (count < numOfMessage2) {
            //System.out.println("at here");
            info = tag + "message";
            for (int i = 0; i < num; i++) {
                /* refresh the inputStream */
                InputStream message = new ByteArrayInputStream(info.getBytes());
                Socket socket = new Socket();
                socket.setSoTimeout(3000);
                socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), (10000 + i)), 3000);
                System.out.println();
                System.out.println("The writer connect successfully.");
                //System.out.println("writer"+ socket.getLocalAddress() + "P: " + socket.getLocalPort());
                System.out.println("node" + socket.getInetAddress() + "P: " + socket.getPort());
                try {
                    writer3.todo(socket, message);
                    sleep(1000);
                } catch (Exception E) {
                    System.out.println("Something wrong");
                }
            }
            System.out.println("Working Nodes: " + workedNode);
            workedNode = 0;
            tag++;

            if (!numOfMessage.equals("n")) {
               count++;
            }
            sleep((int) time);
        }
        //System.out.println("at here3");

    }

}
