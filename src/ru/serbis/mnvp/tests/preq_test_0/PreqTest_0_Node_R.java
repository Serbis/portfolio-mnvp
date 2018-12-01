package ru.serbis.mnvp.tests.preq_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class PreqTest_0_Node_R extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5041 = new TcpGatewayConfig();
        tcpGatewayConfig_5041.setHost("127.0.0.1");
        tcpGatewayConfig_5041.setPort(5041);

        TcpGatewayConfig tcpGatewayConfig_5050 = new TcpGatewayConfig();
        tcpGatewayConfig_5050.setHost("127.0.0.1");
        tcpGatewayConfig_5050.setPort(5050);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5040, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_R");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.setNetworkAddress(18);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5041);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5050);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_R.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
