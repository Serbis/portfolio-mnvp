package ru.serbis.mnvp.tests.preq_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class PreqTest_0_Node_P extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5033 = new TcpGatewayConfig();
        tcpGatewayConfig_5033.setHost("127.0.0.1");
        tcpGatewayConfig_5033.setPort(5033);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5032, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_P");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.setNetworkAddress(16);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5033);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_P.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
