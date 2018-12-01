package ru.serbis.mnvp.tests.hello_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;


public class HelloTest_0_Node_A extends TestThread implements Runnable {


    @Override
    public void run() {
        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5000, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_A");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.setNetworkAddress(1);
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }
}
