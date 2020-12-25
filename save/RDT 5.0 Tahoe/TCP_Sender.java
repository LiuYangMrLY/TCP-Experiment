package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.*;

public class TCP_Sender extends TCP_Sender_ADT {

    private TCP_PACKET tcpPack;  // 待发送的 TCP 数据报
    private volatile int flag = 1;

    private SenderSlidingWindow window = new SenderSlidingWindow(this.client);

    public TCP_Sender() {
        super();  // 调用超类构造函数
        super.initTCP_Sender(this);  // 初始化 TCP 发送端
    }

    /**
     * 应用层调用
     * 可靠发送: 封装应用层数据, 产生 TCP 数据报
     */
    @Override
    public void rdt_send(int dataIndex, int[] appData) {
        // 生成 TCP 数据报（设置序号、数据字段、校验和)，注意打包的顺序
        this.tcpH.setTh_seq(dataIndex * appData.length + 1);  // 包序号设置为字节流号
        this.tcpS.setData(appData);
        this.tcpPack = new TCP_PACKET(this.tcpH, this.tcpS, this.destinAddr);

        this.tcpH.setTh_sum(CheckSum.computeChkSum(this.tcpPack));
        this.tcpPack.setTcpH(this.tcpH);

        System.out.println();
        System.out.println("window size: " + this.window.cwnd);
        System.out.println();

        if (this.window.isFull()) {
            System.out.println();
            System.out.println("Sliding Window Full");
            System.out.println();

            this.flag = 0;
        }
        while (this.flag == 0) ;

        try {
            this.window.putPacket(this.tcpPack.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // 发送 TCP 数据报
        udt_send(this.tcpPack);
    }

    @Override
    public void waitACK() { }

    /**
     * 接收 ACK 报文段
     */
    @Override
    public void recv(TCP_PACKET recvPack) {
        if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {
            System.out.println();
            System.out.println("Receive ACK Number: " + recvPack.getTcpH().getTh_ack());
            System.out.println();

            this.window.receiveACK((recvPack.getTcpH().getTh_ack() - 1) / 100);
            if (!this.window.isFull()) {
                this.flag = 1;
            }
        }
    }

    /**
     * 不可靠发送
     * 将打包好的 TCP 数据报通过不可靠传输信道发送
     * 仅需修改错误标志
     */
    @Override
    public void udt_send(TCP_PACKET stcpPack) {
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
        this.client.send(stcpPack);
    }
}
