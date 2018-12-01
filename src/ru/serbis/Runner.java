package ru.serbis;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.acceptors.ws.WsAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Node;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.tests.TestRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    !!! -- Эти вещи  нужно вписывать в документацию -- !!!
    0.1 Описать как работает тикер и что обязан делать пакет
    1. Поиск маршрута СИТУАЦИЯ - есть шлюз
    2. Прохождение транитных пакетов
        2.1 Штатное прохождение (шлюз найден)
        2.2 Отправка ответа об обрыве на машруте пакета (транзитный узел не знает куда дальше отправлять пакет
    3. Поиск маршрута СИТУАЦИЯ - нет шлюза (preq)





 */
public class Runner {
    String abc = "";
    Map<String, List<String>> messagePool = new HashMap<>();

    public void run() throws IOException {


        //WsTestEp wsTestEp = new WsTestEp(5000);
        //wsTestEp.start();

        Node node1 = startNode_1();
        /*/startNode_2();
        startNode_4();

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        node1.executeEcho(4);/*
        //startNode_5();
        //startNode_6();

        while (true) {}
       /* PacketReceiveAction action = new PacketReceiveAction();
        action.setExpectedPacketType("=HELLO");
        action.setExpectedSource("(=1),(=2)");
        action.setActionCallback(object -> System.out.println((String) object));*/





       /* TestRunner testRunner = new TestRunner();
        testRunner.start();

        testMst("{abc},");
        testMst("{def}");
*/


        /*try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Socket socket = new Socket("127.0.0.1", 5000);*/



        /*ServerSocket serverSocket = new ServerSocket(5000); //Создать новый серверный сокет
        serverSocket.setSoTimeout(10000);

        InputStream is;
        OutputStream os;
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                is = socket.getInputStream();
                os = socket.getOutputStream();
                break;
            } catch (SocketTimeoutException e) {

            }
        }

        System.out.println("CONNECTED");
        os.write("HELLO".getBytes());*/

        /*while (true) {
            for (int b = 0; ((b = is.read()) >= 0);) {
                abc += (char) b;
                //if (abc.equals("ABC")) {
                    os.write("HELLO".getBytes());
                //    abc = "";
                //}
                System.out.print((char) b);
            }
        }*/
    }

    private void testMst(String msg) {


        String incoming_msg = msg;
        String nodeId = "1957";

        //Если полученное сообщение является концом последовательноси
        //(Имеет место терминатор }) склеить все сообщения и отправить
        //в очередь
        if (incoming_msg.charAt(incoming_msg.length() - 1) == '}') {
            StringBuilder sb = new StringBuilder();
            List<String> mPool = messagePool.get(nodeId);

            mPool.add(incoming_msg);

            for (String fr: mPool) {
                sb.append(fr);
            }


            //Иначе размещаем полученное сообещние в очередь
        } else {

            List<String> mPool = messagePool.get(nodeId);
            if (mPool == null)
                mPool = new ArrayList<>();
            mPool.add(incoming_msg.substring(0, incoming_msg.length() - 1));
            messagePool.put(nodeId, mPool);
        }
    }
    private Node startNode_1() {
        TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5000, 10000, 1, "acceptor_TCP");
        //WsAcceptorConfig wsAcceptorConfig = new WsAcceptorConfig(5001, "acceptor_2");
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_1");
        nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        //nodeConfig.getAcceptorConfigs().add(wsAcceptorConfig);
        nodeConfig.setNetworkAddress(333);
        nodeConfig.setLogFilePath("/home/serbis/tmp/node_1.log");

        Node node = new Node();
        node.start(nodeConfig);

        return node;
    }

    private void startNode_2() {
        //TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5001, 10000, 1, "acceptor_1");


        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_2");
        //nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        nodeConfig.setLogFilePath("/home/serbis/tmp/node_2.log");
        nodeConfig.setNetworkAddress(2);

        WsAcceptorConfig wsAcceptorConfig = new WsAcceptorConfig(5002, "acceptor_WS");
        nodeConfig.getAcceptorConfigs().add(wsAcceptorConfig);

        TcpGatewayConfig tcpGatewayConfig = new TcpGatewayConfig();
        tcpGatewayConfig.setHost("127.0.0.1");
        tcpGatewayConfig.setPort(5001);
        nodeConfig.getGatewayConfigs().add(tcpGatewayConfig);

        Node node = new Node();
        node.start(nodeConfig);


        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        node.stop();*/
    }

    private void startNode_4() {
        //TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5001, 10000, 1, "acceptor_1");
        //TcpGatewayConfig tcpGatewayConfig = new TcpGatewayConfig();
        //tcpGatewayConfig.setHost("127.0.0.1");
       // tcpGatewayConfig.setPort(5000);
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_4");
        //nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        //nodeConfig.getGatewayConfigs().add(tcpGatewayConfig);
        nodeConfig.setLogFilePath("/home/serbis/tmp/node_4.log");
        nodeConfig.setNetworkAddress(4);

        WsAcceptorConfig wsAcceptorConfig = new WsAcceptorConfig(5004, "acceptor_WS");
        nodeConfig.getAcceptorConfigs().add(wsAcceptorConfig);

        Node node = new Node();
        node.start(nodeConfig);


        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        node.stop();*/
    }

    private void startNode_5() {
        //TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5001, 10000, 1, "acceptor_1");
        //TcpGatewayConfig tcpGatewayConfig = new TcpGatewayConfig();
        //tcpGatewayConfig.setHost("127.0.0.1");
        // tcpGatewayConfig.setPort(5000);
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_5");
        //nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        //nodeConfig.getGatewayConfigs().add(tcpGatewayConfig);
        nodeConfig.setLogFilePath("/home/serbis/tmp/node_5.log");
        nodeConfig.setNetworkAddress(4);

        WsAcceptorConfig wsAcceptorConfig = new WsAcceptorConfig(5005, "acceptor_WS");
        nodeConfig.getAcceptorConfigs().add(wsAcceptorConfig);

        Node node = new Node();
        node.start(nodeConfig);


        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        node.stop();*/
    }

    private void startNode_6() {
        //TcpAcceptorConfig tcpAcceptorConfig = new TcpAcceptorConfig(5001, 10000, 1, "acceptor_1");
        //TcpGatewayConfig tcpGatewayConfig = new TcpGatewayConfig();
        //tcpGatewayConfig.setHost("127.0.0.1");
        // tcpGatewayConfig.setPort(5000);
        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setLabel("node_6");
        //nodeConfig.getAcceptorConfigs().add(tcpAcceptorConfig);
        //nodeConfig.getGatewayConfigs().add(tcpGatewayConfig);
        nodeConfig.setLogFilePath("/home/serbis/tmp/node_6.log");
        nodeConfig.setNetworkAddress(4);

        WsAcceptorConfig wsAcceptorConfig = new WsAcceptorConfig(5006, "acceptor_WS");
        nodeConfig.getAcceptorConfigs().add(wsAcceptorConfig);

        Node node = new Node();
        node.start(nodeConfig);


        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        node.stop();*/
    }
}
