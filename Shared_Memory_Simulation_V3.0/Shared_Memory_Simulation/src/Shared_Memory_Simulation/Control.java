package Shared_Memory_Simulation;

import java.io.IOException;

public class Control {
    public static void main(String[] args) throws IOException, InterruptedException {
        ReaderBranch nodes = new ReaderBranch();
        Writer1Branch writer1Branch = new Writer1Branch();
        Writer2Branch writer2Branch = new Writer2Branch();
        Nodes.main();
        nodes.start();
        writer1Branch.start();
        writer2Branch.start();





    }

}

class ReaderBranch extends Thread {

    public ReaderBranch() {

    }

    public void run(){
        super.run();
        try {
            Reader.main();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
class Writer1Branch extends Thread {

    public Writer1Branch() {

    }

    public void run(){
        super.run();
        try {
            Writer_1.main();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
class Writer2Branch extends Thread {

    public Writer2Branch() {

    }

    public void run(){
        super.run();
        try {
            Writer_2.main();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}




