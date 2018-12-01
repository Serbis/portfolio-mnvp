package ru.serbis.mnvp.tests.echo_test_0;

import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_E extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5022 = new TcpGatewayConfig();
        tcpGatewayConfig_5022.setHost("127.0.0.1");
        tcpGatewayConfig_5022.setPort(5022);

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_E");
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5022);
        nodeConfig.setNetworkAddress(5);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_E.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
