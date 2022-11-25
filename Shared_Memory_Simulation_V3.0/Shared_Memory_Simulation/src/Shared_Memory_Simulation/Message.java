package Shared_Memory_Simulation;

import java.io.Serializable;

/**
 * This is the message class, and it mainly helps to
 * construct the message which will be used between the reader and writers.
 * */
public class Message implements Serializable {

    private int tag;

    private int writerNum;

    private String message;

    public Message() {
    }

    public Message(int tag, int writerNum, String message) {
        this.tag = tag;
        this.writerNum = writerNum;
        this.message = message;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getWriterNum() {
        return writerNum;
    }

    public void setWriterNum(int writerNum) {
        this.writerNum = writerNum;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



}
