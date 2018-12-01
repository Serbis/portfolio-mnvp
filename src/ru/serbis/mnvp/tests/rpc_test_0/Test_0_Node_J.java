package ru.serbis.mnvp.tests.rpc_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_J extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5031 = new TcpGatewayConfig();
        tcpGatewayConfig_5031.setHost("127.0.0.1");
        tcpGatewayConfig_5031.setPort(5031);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5022, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_J");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.setNetworkAddress(10);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5031);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_J.log");
        Node node = new Node();
        node.start(nodeConfig);
        while (super.isAlive()) {}

        node.stop();
    }


}
