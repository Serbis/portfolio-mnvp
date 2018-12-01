package ru.serbis.mnvp.tests.rpc_test_0;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.rpc.RpcCallConfig;
import ru.serbis.mnvp.rpc.RpcConfig;
import ru.serbis.mnvp.rpc.RpcProcessResult;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestThread;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by serbis on 27.11.17.
 */
public class Test_0_Node_T extends TestThread implements Runnable {
    /** Счетчик принятых HELLO покетов */
    private int helloPacketCounter;

    @Override
    public void run() {
        //RpcCallConfig proc1 = new RpcCallConfig("rpcOne():V", this::rpcOne);
        //RpcCallConfig proc2 = new RpcCallConfig("rpcTwo(S,L,I,F,B,D,B,B):V", this::rpcOne);
        RpcCallConfig proc3 = new RpcCallConfig("rpcThree(I,S):V", this::rpcOne);

        List<RpcCallConfig> rpcCallConfigList = new ArrayList<>();
        //rpcCallConfigList.add(proc1);
        //rpcCallConfigList.add(proc2);
        rpcCallConfigList.add(proc3);

        RpcConfig rpcConfig = new RpcConfig();
        rpcConfig.setRpcCallConfigList(rpcCallConfigList);

        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5050, 10000, 1, "acceptor_1");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_T");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.setNetworkAddress(20);
        nodeConfig.setLogFilePath(super.getLogPath() + "node_T.log");
        nodeConfig.setRpcConfig(rpcConfig);
        Node node = new Node();
        node.start(nodeConfig);

        while (super.isAlive()) {}

        node.stop();
    }

    private RpcProcessResult rpcOne(Object ... args) {
        return new RpcProcessResult();
    }
}
