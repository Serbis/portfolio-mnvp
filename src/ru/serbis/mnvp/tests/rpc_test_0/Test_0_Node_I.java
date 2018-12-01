package ru.serbis.mnvp.tests.rpc_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_I extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5022 = new TcpGatewayConfig();
        tcpGatewayConfig_5022.setHost("127.0.0.1");
        tcpGatewayConfig_5022.setPort(5022);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5021, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_I");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.setNetworkAddress(9);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5022);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_I.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
