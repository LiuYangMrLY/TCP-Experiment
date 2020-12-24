package com.ouc.tcp.test;

import com.ouc.tcp.message.TCP_PACKET;

public class CheckSum {

	/**
	 * 计算 TCP 报文段校验和: 只需校验 TCP 首部中的 seq, ack 和 sum, 以及 TCP 数据部分
	 */
	public static short computeChkSum(TCP_PACKET tcpPack) {
		int checkSum = 0;
		
		return (short) checkSum;
	}
	
}
