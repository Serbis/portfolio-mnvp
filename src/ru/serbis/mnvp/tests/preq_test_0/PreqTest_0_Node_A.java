package ru.serbis.mnvp.tests.preq_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.debugger.IncomingPacketInterceptor;
import ru.serbis.mnvp.debugger.NodeDebugger;
import ru.serbis.mnvp.debugger.NodeDebuggerConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.mix.NetworkUtils;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.structs.general.Packet;
import ru.serbis.mnvp.tests.TestThread;


public class PreqTest_0_Node_A extends TestThread implements Runnable, NetworkUtils, PacketUtils {
    private Node node;

    @Override
    public void run() {
        TcpGatewayConfig tcpGatewayConfig_5000 = new TcpGatewayConfig();
        tcpGatewayConfig_5000.setHost("127.0.0.1");
        tcpGatewayConfig_5000.setPort(5000);

        TcpGatewayConfig tcpGatewayConfig_5001 = new TcpGatewayConfig();
        tcpGatewayConfig_5001.setHost("127.0.0.1");
        tcpGatewayConfig_5001.setPort(5001);

        /*NodeDebuggerConfig nodeDebuggerConfig = new NodeDebuggerConfig();
        IncomingPacketInterceptor incomingPacketInterceptor = new IncomingPacketInterceptor();
        incomingPacketInterceptor.setPacketInterceptorCallback(packet -> {
            if (packet.getType() == Packet.Type.HELLO.ordinal() && packet.getSource() == 1)
                helloPacketCounter++;
            return true;
        });
        nodeDebuggerConfig.setIncomingPacketInterceptor(incomingPacketInterceptor);*/

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_A");
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5000);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5001);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_A.log");
        nodeConfig.setNetworkAddress(1);
        nodeConfig.setDebugMode(true);
        //nodeConfig.setNodeDebuggerConfig(nodeDebuggerConfig);
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }

    public void sendPreq() {
        node.getDebagger().sendPreqRequest(20, result -> {});
    }
}
