package ru.serbis.mnvp.tests.echo_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_G extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5023 = new TcpGatewayConfig();
        tcpGatewayConfig_5023.setHost("127.0.0.1");
        tcpGatewayConfig_5023.setPort(5023);

        TcpGatewayConfig tcpGatewayConfig_5024 = new TcpGatewayConfig();
        tcpGatewayConfig_5024.setHost("127.0.0.1");
        tcpGatewayConfig_5024.setPort(5024);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5012, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_G");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5023);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5024);
        nodeConfig.setNetworkAddress(7);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_G.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
