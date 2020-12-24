package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

public class SenderSlidingWindow {
    private Client client;
    private int size = 16;
    private int base = 0;
    private int nextIndex = 0;
    private TCP_PACKET[] packets = new TCP_PACKET[this.size];
    private UDT_Timer[] timers = new UDT_Timer[this.size];

    public SenderSlidingWindow(Client client) {
        this.client = client;
    }

    public boolean isFull() {
        return this.size <= this.nextIndex;
    }

    public void putPacket(TCP_PACKET packet) {
        this.packets[this.nextIndex] = packet;
        this.timers[this.nextIndex] = new UDT_Timer();
        this.timers[this.nextIndex].schedule(new UDT_RetransTask(this.client, packet), 3000, 3000);

        this.nextIndex++;
    }

    public void receiveACK(int currentSequence) {
        if (this.base <= currentSequence && currentSequence < this.base + this.size) {
            if (this.timers[currentSequence - this.base] == null) {
                return;
            }

            this.timers[currentSequence - this.base].cancel();
            this.timers[currentSequence - this.base] = null;

            if (currentSequence == this.base) {
                int maxACKedIndex = 0;
                while (maxACKedIndex + 1 < this.nextIndex
                        && this.timers[maxACKedIndex + 1] == null) {
                    maxACKedIndex++;
                }

                for (int i = 0; maxACKedIndex + 1 + i < this.size; i++) {
                    this.packets[i] = this.packets[maxACKedIndex + 1 + i];
                    this.timers[i] = this.timers[maxACKedIndex + 1 + i];
                }

                for (int i = this.size - (maxACKedIndex + 1); i < this.size; i++) {
                    this.packets[i] = null;
                    this.timers[i] = null;
                }

                this.base += maxACKedIndex + 1;
                this.nextIndex -= maxACKedIndex + 1;
            }
        }
    }
}
