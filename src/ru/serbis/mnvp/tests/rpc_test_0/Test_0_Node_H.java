package ru.serbis.mnvp.tests.rpc_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_H extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5030 = new TcpGatewayConfig();
        tcpGatewayConfig_5030.setHost("127.0.0.1");
        tcpGatewayConfig_5030.setPort(5030);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5020, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_H");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.setNetworkAddress(8);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5030);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_H.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
