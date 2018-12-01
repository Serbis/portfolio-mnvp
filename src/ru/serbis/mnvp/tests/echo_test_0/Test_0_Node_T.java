package ru.serbis.mnvp.tests.echo_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_T extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5050, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_T");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.setNetworkAddress(20);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_T.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }


}
