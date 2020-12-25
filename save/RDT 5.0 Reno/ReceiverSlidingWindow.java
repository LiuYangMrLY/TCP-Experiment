package com.ouc.tcp.test;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceiverSlidingWindow {
    private Client client;
    private LinkedList<TCP_PACKET> packets = new LinkedList<>();
    private int expectedSequence = 0;
    Queue<int[]> dataQueue = new LinkedBlockingQueue();

    public ReceiverSlidingWindow(Client client) {
        this.client = client;
    }

    public int receivePacket(TCP_PACKET packet) {
        int currentSequence = (packet.getTcpH().getTh_seq() - 1) / 100;

        if (currentSequence >= this.expectedSequence) {
            putPacket(packet);
        }

        slid();

        return this.expectedSequence - 1;
    }

    private void putPacket(TCP_PACKET packet) {
        int currentSequence = (packet.getTcpH().getTh_seq() - 1) / 100;

        int index = 0;
        while (index < this.packets.size()
                && currentSequence > (this.packets.get(index).getTcpH().getTh_seq() - 1) / 100) {
            index++;
        }

        if (index == this.packets.size()
                || currentSequence != (this.packets.get(index).getTcpH().getTh_seq() - 1) / 100) {
            this.packets.add(index, packet);
        }
    }

    private void slid() {
        while (!this.packets.isEmpty()
                && (this.packets.getFirst().getTcpH().getTh_seq() - 1) / 100 == this.expectedSequence) {
            this.dataQueue.add(this.packets.poll().getTcpS().getData());
            this.expectedSequence++;
        }

        if (this.dataQueue.size() >= 20 || this.expectedSequence == 1000) {
            this.deliver_data();
        }
    }

    /**
     * 交付数据: 将数据写入文件
     */
    public void deliver_data() {
        // 检查 this.dataQueue，将数据写入文件
        try {
            File file = new File("recvData.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            while (!this.dataQueue.isEmpty()) {
                int[] data = this.dataQueue.poll();

                // 将数据写入文件
                for (int i = 0; i < data.length; i++) {
                    writer.write(data[i] + "\n");
                }

                writer.flush();  // 清空输出缓存
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
