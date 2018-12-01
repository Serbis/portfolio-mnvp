package ru.serbis.mnvp.tests.preq_test_0;

import ru.serbis.mnvp.tests.hello_test_0.HelloTest_0_Node_D;
import ru.serbis.mnvp.tests.hello_test_0.HelloTest_0_Node_E;

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
public class PreqTest_0 implements Runnable {
    @Override
    public void run() {
        PreqTest_0_Node_A preqTest_0_node_a = new PreqTest_0_Node_A();
        Thread preqTest_0_node_a_thread = new Thread(preqTest_0_node_a);

        PreqTest_0_Node_B preqTest_0_node_b = new PreqTest_0_Node_B();
        Thread preqTest_0_node_b_thread = new Thread(preqTest_0_node_b);

        PreqTest_0_Node_C preqTest_0_node_c = new PreqTest_0_Node_C();
        Thread preqTest_0_node_c_thread = new Thread(preqTest_0_node_c);

        PreqTest_0_Node_D preqTest_0_node_d = new PreqTest_0_Node_D();
        Thread preqTest_0_node_d_thread = new Thread(preqTest_0_node_d);

        PreqTest_0_Node_E preqTest_0_node_e = new PreqTest_0_Node_E();
        Thread preqTest_0_node_e_thread = new Thread(preqTest_0_node_e);

        PreqTest_0_Node_F preqTest_0_node_f = new PreqTest_0_Node_F();
        Thread preqTest_0_node_f_thread = new Thread(preqTest_0_node_f);

        PreqTest_0_Node_G preqTest_0_node_g = new PreqTest_0_Node_G();
        Thread preqTest_0_node_g_thread = new Thread(preqTest_0_node_g);

        PreqTest_0_Node_H preqTest_0_node_h = new PreqTest_0_Node_H();
        Thread preqTest_0_node_h_thread = new Thread(preqTest_0_node_h);

        PreqTest_0_Node_I preqTest_0_node_i = new PreqTest_0_Node_I();
        Thread preqTest_0_node_i_thread = new Thread(preqTest_0_node_i);

        PreqTest_0_Node_J preqTest_0_node_j = new PreqTest_0_Node_J();
        Thread preqTest_0_node_j_thread = new Thread(preqTest_0_node_j);

        PreqTest_0_Node_K preqTest_0_node_k = new PreqTest_0_Node_K();
        Thread preqTest_0_node_k_thread = new Thread(preqTest_0_node_k);

        PreqTest_0_Node_L preqTest_0_node_l = new PreqTest_0_Node_L();
        Thread preqTest_0_node_l_thread = new Thread(preqTest_0_node_l);

        PreqTest_0_Node_M preqTest_0_node_m = new PreqTest_0_Node_M();
        Thread preqTest_0_node_m_thread = new Thread(preqTest_0_node_m);

        PreqTest_0_Node_N preqTest_0_node_n = new PreqTest_0_Node_N();
        Thread preqTest_0_node_n_thread = new Thread(preqTest_0_node_n);

        PreqTest_0_Node_O preqTest_0_node_o = new PreqTest_0_Node_O();
        Thread preqTest_0_node_o_thread = new Thread(preqTest_0_node_o);

        PreqTest_0_Node_P preqTest_0_node_p = new PreqTest_0_Node_P();
        Thread preqTest_0_node_p_thread = new Thread(preqTest_0_node_p);

        PreqTest_0_Node_Q preqTest_0_node_q = new PreqTest_0_Node_Q();
        Thread preqTest_0_node_q_thread = new Thread(preqTest_0_node_q);

        PreqTest_0_Node_R preqTest_0_node_r = new PreqTest_0_Node_R();
        Thread preqTest_0_node_r_thread = new Thread(preqTest_0_node_r);

        PreqTest_0_Node_S preqTest_0_node_s = new PreqTest_0_Node_S();
        Thread preqTest_0_node_s_thread = new Thread(preqTest_0_node_s);

        PreqTest_0_Node_T preqTest_0_node_t = new PreqTest_0_Node_T();
        Thread preqTest_0_node_t_thread = new Thread(preqTest_0_node_t);

        try {
            //preqTest_0_node_h_thread.start();
           // Thread.sleep(1000);

           // preqTest_0_node_d_thread.start();
           // Thread.sleep(1000);


            preqTest_0_node_t_thread.start();
            Thread.sleep(1000);

            preqTest_0_node_s_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_r_thread.start();
            Thread.sleep(1000);

            preqTest_0_node_q_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_p_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_o_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_n_thread.start();
            Thread.sleep(1000);

            preqTest_0_node_m_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_l_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_k_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_j_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_i_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_h_thread.start();
            Thread.sleep(1000);




            preqTest_0_node_g_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_f_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_e_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_d_thread.start();
            Thread.sleep(1000);




            preqTest_0_node_b_thread.start();
            Thread.sleep(1000);
            preqTest_0_node_c_thread.start();
            Thread.sleep(1000);

            preqTest_0_node_a_thread.start();
            Thread.sleep(30000);

            //Отправляем PREQ
            preqTest_0_node_a.sendPreq();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }







    }
}


