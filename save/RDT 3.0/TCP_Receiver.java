package com.ouc.tcp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.message.*;

public class TCP_Receiver extends TCP_Receiver_ADT {

    private TCP_PACKET ackPack;  // 回复的 ACK 报文段
    private int lastSequence = -1;

    public TCP_Receiver() {
        super();  // 调用超类构造函数
        super.initTCP_Receiver(this);  // 初始化 TCP 接收端
    }

    /**
     * 接收数据报
     */
    @Override
    public void rdt_recv(TCP_PACKET recvPack) {
        if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {
            // 生成 ACK 报文段（设置确认号）
            this.tcpH.setTh_ack(recvPack.getTcpH().getTh_seq());
            this.ackPack = new TCP_PACKET(this.tcpH, this.tcpS, recvPack.getSourceAddr());
            this.tcpH.setTh_sum(CheckSum.computeChkSum(this.ackPack));

            System.out.println();
            System.out.println("ACK: " + recvPack.getTcpH().getTh_seq());
            System.out.println();

            // 回复 ACK 报文段
            reply(this.ackPack);

            int currentSequence = (recvPack.getTcpH().getTh_seq() - 1) / 100;
            if (currentSequence != this.lastSequence) {
                this.lastSequence = currentSequence;

                // 将接收到的正确有序的数据插入 data 队列，准备交付
                this.dataQueue.add(recvPack.getTcpS().getData());

                // 交付数据（每 20 组数据交付一次）
                if (this.dataQueue.size() == 20)
                    deliver_data();
            }
        }
    }

    /**
     * 交付数据: 将数据写入文件
     */
    @Override
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

    /**
     * 回复 ACK 报文段
     * 不可靠发送
     * 仅需修改错误标志
     */
    @Override
    public void reply(TCP_PACKET replyPack) {
        // 设置错误控制标志
        // 0: 信道无差错
        // 1: 只出错
        // 2: 只丢包
        // 3: 只延迟
        // 4: 出错 / 丢包
        // 5: 出错 / 延迟
        // 6: 丢包 / 延迟
        // 7: 出错 / 丢包 / 延迟
        this.tcpH.setTh_eflag((byte) 4);

        // 发送数据报
        this.client.send(replyPack);
    }

}
