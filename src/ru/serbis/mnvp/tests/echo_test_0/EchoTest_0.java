package ru.serbis.mnvp.tests.echo_test_0;

/**
 * Данный тест создает топологию ромб и выполняет preq запрос на поиск
 * маршрута от A к T. В тексте проверяется что по результатам запроса
 * будет найдено три маршрута через узлы B и C, что транзиные прямые
 * PREQ пакеты прошли по заданным узла, а при прохождении реверсного
 * PREQ пакета, они же сформировали маршрутные записи по данному пакету.
 *
 * Топология сети:
 *                           T
 *                         /  \
 *                        R  - S
 *                     /  |    |  \
 *                    N - O  - P - Q
 *                  / |   |    |   |  \
 *                H - I - J  - K - L - M
 *                 \  |   |    |   |  /
 *                    D - E  - F - G
 *                     \  |    |  /
 *                        B   C
 *                         \ /
 *                          A
 *
 */
public class EchoTest_0 implements Runnable {
    boolean f1 = false;

    @Override
    public void run() {
        Test_0_Node_A test_0_node_a = new Test_0_Node_A();
        Thread test_0_node_a_thread = new Thread(test_0_node_a);

        Test_0_Node_B test_0_node_b = new Test_0_Node_B();
        Thread test_0_node_b_thread = new Thread(test_0_node_b);

        Test_0_Node_C test_0_node_c = new Test_0_Node_C();
        Thread test_0_node_c_thread = new Thread(test_0_node_c);

        Test_0_Node_D test_0_node_d = new Test_0_Node_D();
        Thread test_0_node_d_thread = new Thread(test_0_node_d);

        Test_0_Node_E test_0_node_e = new Test_0_Node_E();
        Thread test_0_node_e_thread = new Thread(test_0_node_e);

        Test_0_Node_F test_0_node_f = new Test_0_Node_F();
        Thread test_0_node_f_thread = new Thread(test_0_node_f);

        Test_0_Node_G test_0_node_g = new Test_0_Node_G();
        Thread test_0_node_g_thread = new Thread(test_0_node_g);

        Test_0_Node_H test_0_node_h = new Test_0_Node_H();
        Thread test_0_node_h_thread = new Thread(test_0_node_h);

        Test_0_Node_I test_0_node_i = new Test_0_Node_I();
        Thread test_0_node_i_thread = new Thread(test_0_node_i);

        Test_0_Node_J test_0_node_j = new Test_0_Node_J();
        Thread test_0_node_j_thread = new Thread(test_0_node_j);

        Test_0_Node_K test_0_node_k = new Test_0_Node_K();
        Thread test_0_node_k_thread = new Thread(test_0_node_k);

        Test_0_Node_L test_0_node_l = new Test_0_Node_L();
        Thread test_0_node_l_thread = new Thread(test_0_node_l);

        Test_0_Node_M test_0_node_m = new Test_0_Node_M();
        Thread test_0_node_m_thread = new Thread(test_0_node_m);

        Test_0_Node_N test_0_node_n = new Test_0_Node_N();
        Thread test_0_node_n_thread = new Thread(test_0_node_n);

        Test_0_Node_O test_0_node_o = new Test_0_Node_O();
        Thread test_0_node_o_thread = new Thread(test_0_node_o);

        Test_0_Node_P test_0_node_p = new Test_0_Node_P();
        Thread test_0_node_p_thread = new Thread(test_0_node_p);

        Test_0_Node_Q test_0_node_q = new Test_0_Node_Q();
        Thread test_0_node_q_thread = new Thread(test_0_node_q);

        Test_0_Node_R test_0_node_r = new Test_0_Node_R();
        Thread test_0_node_r_thread = new Thread(test_0_node_r);

        Test_0_Node_S test_0_node_s = new Test_0_Node_S();
        Thread test_0_node_s_thread = new Thread(test_0_node_s);

        Test_0_Node_T test_0_node_t = new Test_0_Node_T();
        Thread test_0_node_t_thread = new Thread(test_0_node_t);

        try {
            //test_0_node_h_thread.start();
           // Thread.sleep(1000);

           // test_0_node_d_thread.start();
           // Thread.sleep(1000);


            test_0_node_t_thread.start();
            Thread.sleep(1000);

            test_0_node_s_thread.start();
            Thread.sleep(1000);
            test_0_node_r_thread.start();
            Thread.sleep(1000);

            test_0_node_q_thread.start();
            Thread.sleep(1000);
            test_0_node_p_thread.start();
            Thread.sleep(1000);
            test_0_node_o_thread.start();
            Thread.sleep(1000);
            test_0_node_n_thread.start();
            Thread.sleep(1000);

            test_0_node_m_thread.start();
            Thread.sleep(1000);
            test_0_node_l_thread.start();
            Thread.sleep(1000);
            test_0_node_k_thread.start();
            Thread.sleep(1000);
            test_0_node_j_thread.start();
            Thread.sleep(1000);
            test_0_node_i_thread.start();
            Thread.sleep(1000);
            test_0_node_h_thread.start();
            Thread.sleep(1000);




            test_0_node_g_thread.start();
            Thread.sleep(1000);
            test_0_node_f_thread.start();
            Thread.sleep(1000);
            test_0_node_e_thread.start();
            Thread.sleep(1000);
            test_0_node_d_thread.start();
            Thread.sleep(1000);




            test_0_node_b_thread.start();
            Thread.sleep(1000);
            test_0_node_c_thread.start();
            Thread.sleep(1000);

            test_0_node_a_thread.start();
            Thread.sleep(20000);



            //Отправить ECHO к соседнему узлу (проход пакета с изначально известным маршрутом)
            test_0_node_a.sendEcho(2, result -> {
                f1 = true;
            });

            //Отправить ECHO к удаленному узлу (проход пакета с preq запросом)
            test_0_node_a.sendEcho(20, result -> {});

            //Отправить ECHO к несуществующему узлу (preq вернет NOT_FOUND
            test_0_node_a.sendEcho(999, result -> {});

            Thread.sleep(20000);

            //Отправить ECHO к удаленному узлу (проход пакета с возвратом ошибки NETWORK_ERROR:0)
            test_0_node_d.cutRouts();
            test_0_node_a.sendEcho(20, result -> {});



            //Отправить ECHO к удаленному узлу но недостижимому узлу (на одном из узлов отсутвует маршрутная запись)

        } catch (InterruptedException e) {
            e.printStackTrace();
        }







    }
}


