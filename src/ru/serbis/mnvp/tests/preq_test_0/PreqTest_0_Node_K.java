package ru.serbis.mnvp.tests.preq_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class PreqTest_0_Node_K extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5032 = new TcpGatewayConfig();
        tcpGatewayConfig_5032.setHost("127.0.0.1");
        tcpGatewayConfig_5032.setPort(5032);

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_K");
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5032);
        nodeConfig.setNetworkAddress(11);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_K.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
