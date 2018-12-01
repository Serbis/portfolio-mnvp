package ru.serbis.mnvp.acceptors.tcp;

import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.general.Incapsulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Поток акцептора. Запускается как в единственном так и во множественном
 * экземпляре. Задачей потока является прослушивание серверного соеката
 * в режиме блокировк в ожидании входящего подключения. Если последнее
 * имеет место быть, порождается и регистрируется новый tcp шлюз, после
 * чего поток переходит в свое естестенное состоянии блокирующего акцепта.
 */
public class TcpAcceptThread implements Runnable {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Метка полного останова потока */
    private boolean stopped = false;
    /** Флаг активности поток */
    private boolean alive = true;
    /** Серверный сокет */
    private ServerSocket serverSocket;
    /** Метка потока */
    private String label;

    public TcpAcceptThread(Incapsulator incapsulator) {
        this.I = incapsulator;
    }

    /**
     * Назначает потоку серверный сокет и метку
     *
     * @param serverSocket серверный сокет
     * @param label метка потока
     */
    public void init(ServerSocket serverSocket, String label) {
        this.label = label;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        I.lc.log(String.format("<blue>[%s] Запущен поток ацептора tcp соединений %s<nc>", label, label), 3);
        while (alive) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(10000);
                I.lc.log(String.format("<lblue>TcpAcceptThread -> %s ACCEPT<nc>", label), 10);
                TcpGatewayConfig tcpGatewayConfig = new TcpGatewayConfig();
                tcpGatewayConfig.setSocket(socket);
                I.gc.wrapGateway(tcpGatewayConfig);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        I.lc.log(String.format("<blue>[%s] Остановлен поток ацептора tcp соединений %s<nc>", label, label), 3);
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }


    /**
     * Устанавливает флаг завершения потока
     */
    public void stop() {
        alive = false;
    }
}
