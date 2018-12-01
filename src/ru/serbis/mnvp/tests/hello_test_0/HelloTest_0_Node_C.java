package ru.serbis.mnvp.tests.hello_test_0;

import ru.serbis.mnvp.debugger.IncomingPacketInterceptor;
import ru.serbis.mnvp.debugger.NodeDebuggerConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.structs.general.Packet;
import ru.serbis.mnvp.tests.TestThread;

/**
 * Created by serbis on 27.11.17.
 */
public class HelloTest_0_Node_C extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig = new TcpGatewayConfig();
        tcpGatewayConfig.setHost("127.0.0.1");
        tcpGatewayConfig.setPort(5000);

        NodeDebuggerConfig nodeDebuggerConfig = new NodeDebuggerConfig();
        IncomingPacketInterceptor incomingPacketInterceptor = new IncomingPacketInterceptor();
        incomingPacketInterceptor.setPacketInterceptorCallback(packet -> {
            if (packet.getType() == Packet.Type.HELLO.ordinal() && packet.getSource() == 1)
                helloPacketCounter++;
            return true;
        });
        nodeDebuggerConfig.setIncomingPacketInterceptor(incomingPacketInterceptor);

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_C");
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_C.log");
        nodeConfig.setNetworkAddress(3);
        nodeConfig.setDebugMode(true);
        nodeConfig.setNodeDebuggerConfig(nodeDebuggerConfig);
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }

    public int getHelloPacketCounter() {
        return helloPacketCounter;
    }
}
