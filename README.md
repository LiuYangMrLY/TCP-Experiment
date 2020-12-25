# TCP-Experiment

中国海洋大学计算机网络实验，实现 TCP 协议端到端的可靠传输。

## Version

RDT 1.0: 在可靠信道上进行可靠的数据传输

RDT 2.0: 信道上可能出现位错

RDT 2.1: 管理出错的 ACK/NAK

RDT 2.2: 无 NAK 的协议

RDT 3.0: 通道上可能出错和丢失数据

RDT 4.0: 流水线协议 Go-Back-N, Selective-Response

RDT 5.0: 拥塞控制 Tahoe, Reno

## Install

本项目使用 JetBrains IntelliJ IDEA 编写，IDEA 可通过 TCP.iml 导入项目。

Java version: 1.8

## Usage

将 jars 文件夹中对应平台的 jar 包放到 lib 文件夹中，Windows 使用 TCP_Win_TestSys.jar，MacOS 和 Linux 使用 TCP_TestSys_Linux.jar。

运行 TestRun.java，等待传输完毕，最后需手动结束进程。

运行日志存放于 Log.txt，接收的数据存放于 recvData.txt。

如果要从零编写代码实现 TCP 可靠传输，请下载 v1.0 版本 [TCP-Experiment-1.0](https://github.com/249606097/TCP-Experiment/releases/tag/v1.0)。

