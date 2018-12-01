package ru.serbis.mnvp.tests.hello_test_0;

import ru.serbis.mnvp.tests.NodeTest;

import java.util.Date;

/**
 * Данный тест создает топологию обычной крест и проверяет, что каждый узел
 * в течении двух минут получил не менее семи hello пакетов от соседних
 * узлов.
 *
 * Топология сети:
 *          D
 *          |
 *       B--A--C
 *          |
 *          E
 *
 */
public class HelloTest_0 implements Runnable {
    @Override
    public void run() {
        HelloTest_0_Node_A helloTest_0_node_a = new HelloTest_0_Node_A();
        Thread helloTest_0_node_a_thread = new Thread(helloTest_0_node_a);

        HelloTest_0_Node_B helloTest_0_node_b = new HelloTest_0_Node_B();
        Thread helloTest_0_node_b_thread = new Thread(helloTest_0_node_b);

        HelloTest_0_Node_C helloTest_0_node_c = new HelloTest_0_Node_C();
        Thread helloTest_0_node_c_thread = new Thread(helloTest_0_node_c);

        HelloTest_0_Node_D helloTest_0_node_d = new HelloTest_0_Node_D();
        Thread helloTest_0_node_d_thread = new Thread(helloTest_0_node_d);

        HelloTest_0_Node_E helloTest_0_node_e = new HelloTest_0_Node_E();
        Thread helloTest_0_node_e_thread = new Thread(helloTest_0_node_e);

        helloTest_0_node_a_thread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        helloTest_0_node_b_thread.start();
        helloTest_0_node_c_thread.start();
        helloTest_0_node_d_thread.start();
        helloTest_0_node_e_thread.start();

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (helloTest_0_node_b.getHelloPacketCounter() < 3 ||
                helloTest_0_node_c.getHelloPacketCounter() < 3 ||
                helloTest_0_node_d.getHelloPacketCounter() < 3 ||
                helloTest_0_node_e.getHelloPacketCounter() < 3) {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ТЕСТ RpcTest_0 ЗАВАЛЕН");
        } else {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ТЕСТ RpcTest_0 ПРОЙДЕН");
        }

        helloTest_0_node_a.setAlive(false);
        helloTest_0_node_b.setAlive(false);
        helloTest_0_node_c.setAlive(false);
        helloTest_0_node_d.setAlive(false);
        helloTest_0_node_e.setAlive(false);


    }
}


