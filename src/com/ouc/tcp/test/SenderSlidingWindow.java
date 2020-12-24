package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.util.Timer;

public class SenderSlidingWindow {
    private Client client;
    private int size = 16;
    private int base = 0;
    private int nextIndex = 0;
    private TCP_PACKET[] packets = new TCP_PACKET[this.size];

    private Timer timer;
    private TaskPacketsRetransmit task;

    public SenderSlidingWindow(Client client) {
        this.client = client;
    }

    public boolean isFull() {
        return this.size <= this.nextIndex;
    }

    public void putPacket(TCP_PACKET packet) {
        this.packets[this.nextIndex] = packet;
        if (this.base == this.nextIndex) {
            this.timer = new Timer();
            this.task = new TaskPacketsRetransmit(this.client, this.packets);
            this.timer.schedule(this.task, 3000, 3000);
        }

        this.nextIndex++;
    }

    public void receiveACK(int currentSequence) {
        if (this.base <= currentSequence && currentSequence < this.base + this.size) {
            for (int i = 0; currentSequence - this.base + 1 + i < this.size; i++) {
                this.packets[i] = this.packets[currentSequence - this.base + 1 + i];
                this.packets[currentSequence - this.base + 1 + i] = null;
            }

            this.nextIndex -=currentSequence - this.base + 1;
            this.base = currentSequence + 1;

            this.timer.cancel();
            if (this.base != this.nextIndex) {
                this.timer = new Timer();
                this.task = new TaskPacketsRetransmit(this.client, this.packets);
                this.timer.schedule(this.task, 3000, 3000);
            }
        }
    }
}
