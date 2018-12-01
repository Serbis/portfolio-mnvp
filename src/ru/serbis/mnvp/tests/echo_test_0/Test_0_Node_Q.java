package ru.serbis.mnvp.tests.echo_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_Q extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5041 = new TcpGatewayConfig();
        tcpGatewayConfig_5041.setHost("127.0.0.1");
        tcpGatewayConfig_5041.setPort(5041);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5033, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_Q");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5041);
        nodeConfig.setNetworkAddress(17);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_Q.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
