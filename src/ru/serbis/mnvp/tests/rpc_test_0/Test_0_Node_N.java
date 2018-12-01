package ru.serbis.mnvp.tests.rpc_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_N extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5040 = new TcpGatewayConfig();
        tcpGatewayConfig_5040.setHost("127.0.0.1");
        tcpGatewayConfig_5040.setPort(5040);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5030, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_N");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5040);
        nodeConfig.setNetworkAddress(14);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_N.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
