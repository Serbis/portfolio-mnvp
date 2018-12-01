package ru.serbis.mnvp.tests.echo_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.mix.NetworkUtils;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;


public class Test_0_Node_C extends TestThread implements Runnable, NetworkUtils, PacketUtils {
    private Node node;

    @Override
    public void run() {
       /*NodeDebuggerConfig nodeDebuggerConfig = new NodeDebuggerConfig();
        IncomingPacketInterceptor incomingPacketInterceptor = new IncomingPacketInterceptor();
        incomingPacketInterceptor.setPacketInterceptorCallback(packet -> {
            if (packet.getType() == Packet.Type.HELLO.ordinal() && packet.getSource() == 1)
                helloPacketCounter++;
            return true;
        });
        nodeDebuggerConfig.setIncomingPacketInterceptor(incomingPacketInterceptor);*/

        TcpGatewayConfig tcpGatewayConfig_5000 = new TcpGatewayConfig();
        tcpGatewayConfig_5000.setHost("127.0.0.1");
        tcpGatewayConfig_5000.setPort(5000);

        TcpGatewayConfig tcpGatewayConfig_5011 = new TcpGatewayConfig();
        tcpGatewayConfig_5011.setHost("127.0.0.1");
        tcpGatewayConfig_5011.setPort(5011);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5001, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_C");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5000);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig_5011);
        nodeConfig.setNetworkAddress(3);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_C.log");
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }

    public void sendPreq() {
        node.getDebagger().sendPreqRequest(8, result -> {});
    }
}
