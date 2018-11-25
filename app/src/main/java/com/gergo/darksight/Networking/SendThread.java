package com.gergo.darksight.Networking;

import java.io.PrintWriter;

public class SendThread extends Thread {

    private PrintWriter outPut = null;
    private String messeage = null;
    public SendThread(PrintWriter outPut, String messeage) {
        this.outPut =outPut;
        this.messeage = messeage;
    }

    @Override
    public void run() {
        outPut.println(messeage);
        super.run();
    }
}
