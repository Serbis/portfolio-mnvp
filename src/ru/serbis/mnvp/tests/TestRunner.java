package ru.serbis.mnvp.tests;

import ru.serbis.mnvp.tests.echo_test_0.EchoTest_0;
import ru.serbis.mnvp.tests.hello_test_0.HelloTest_0;
import ru.serbis.mnvp.tests.preq_test_0.PreqTest_0;
import ru.serbis.mnvp.tests.rpc_test_0.RpcTest_0;

/**
 * Данный класс предназначен для запуска групп тестов.
 */
public class TestRunner {

    public void start() {
        /*HelloTest_0 helloTest_0 = new HelloTest_0();
        Thread helloTest_0_Thread = new Thread(helloTest_0);

        helloTest_0_Thread.start();*/


        //PreqTest_0 preqTest_0 = new PreqTest_0();
        //Thread preqTest_0_Thread = new Thread(preqTest_0);

        //preqTest_0_Thread.start();

        //EchoTest_0 echoTest_0 = new EchoTest_0();
        //Thread echoTest_0_Thread = new Thread(echoTest_0);

        RpcTest_0 rpcTest_0 = new RpcTest_0();
        Thread rpcTest_0_Thread = new Thread(rpcTest_0);

        rpcTest_0_Thread.start();
    }
}
