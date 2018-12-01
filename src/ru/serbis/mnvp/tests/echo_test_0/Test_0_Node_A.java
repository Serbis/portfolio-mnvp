package ru.serbis.mnvp.tests.echo_test_0;

import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.mix.NetworkUtils;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.translations.EchoTransaction;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;


public class Test_0_Node_A extends TestThread implements Runnable, NetworkUtils, PacketUtils {
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
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }

    public void sendEcho(int dest, EchoTransaction.TranslationFinisher finisher) {
        node.getDebagger().sendEchoRequest(dest, finisher);
    }
}
