package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.message.*;

public class TCP_Receiver extends TCP_Receiver_ADT {

    private TCP_PACKET ackPack;  // 回复的 ACK 报文段

    private ReceiverSlidingWindow window = new ReceiverSlidingWindow(this.client);

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
            int toACKSequence = -1;
            try {
                toACKSequence = this.window.receivePacket(recvPack.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            this.tcpH.setTh_ack(toACKSequence * 100 + 1);
            this.ackPack = new TCP_PACKET(this.tcpH, this.tcpS, recvPack.getSourceAddr());
            this.tcpH.setTh_sum(CheckSum.computeChkSum(this.ackPack));

            // 回复 ACK 报文段
            reply(this.ackPack);
        }
    }

    /**
     * 交付数据: 将数据写入文件
     */
    @Override
    public void deliver_data() { }

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
        this.tcpH.setTh_eflag((byte) 7);

        // 发送数据报
        this.client.send(replyPack);
    }

}
