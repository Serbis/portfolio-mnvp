package ru.serbis.mnvp.tests.echo_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_D extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;
    private Node node;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5020 = new TcpGatewayConfig();
        tcpGatewayConfig_5020.setHost("127.0.0.1");
        tcpGatewayConfig_5020.setPort(5020);

        TcpGatewayConfig tcpGatewayConfig_5021 = new TcpGatewayConfig();
        tcpGatewayConfig_5021.setHost("127.0.0.1");
        tcpGatewayConfig_5021.setPort(5021);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5010, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_D");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5020);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5021);
        nodeConfig.setNetworkAddress(4);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_D.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }

    /**
     * Удаляет из таблицы маршрутизации узла все маршруты до зула 20
     */
    public void cutRouts() {
        node.getDebagger().getRoutingTable().removeAllByDest(20);
    }


}
