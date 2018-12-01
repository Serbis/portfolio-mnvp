package ru.serbis.mnvp.gateways.tcp;

import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.PacketPool;
import ru.serbis.mnvp.structs.general.Packet;

import java.io.IOException;
import java.net.Socket;

/**
 *  Шлюз tcp соединения
 */
public class TcpGateway extends Gateway implements PacketUtils {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Сокет соединения */
    private Socket socket;
    /** Поток обработки входящих данных */
    private TcpGatewayThread tcpGatewayThread;

    /**
     * Конструктор
     *
     * @param incapsulator инкапсулятор перифирии узла
     */
    public TcpGateway(Incapsulator incapsulator) {
        super(incapsulator);
        this.I = incapsulator;
    }

    public TcpGateway(Incapsulator incapsulator, Socket socket, String label, PacketPool packetPool) {
        super(incapsulator);

        super.setLabel(label);
        this.socket = socket;
        super.setPacketPool(packetPool);

        this.I = incapsulator;
    }

    /**
     * Запускает процесс обработки данных входящего потока
     */
    @Override
    public void run() {
        tcpGatewayThread = new TcpGatewayThread(I, socket, String.format("%s->%s->%s", I.nv.nodeLabel,  super.getLabel(), "GatewayThread"), super.getLabel(), super.getPacketPool());
        Thread thread = new Thread(tcpGatewayThread);
        thread.start();
    }

    /**
     * Останавливает поток шлюза тем самым реализую процедуру закрытия
     * последнего
     */
    @Override
    public void stop() {
        tcpGatewayThread.stop();
        while (!tcpGatewayThread.isStopped()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        I.lc.log(String.format("<blue>[%s->%s] Остановлен TCP шлюз %s<nc>", I.nv.nodeLabel, super.getLabel(), super.getLabel()), 3);
    }

    /**
     * Выполяняет отправку пакета в шлюз. В задачу метода входит преобразование
     * пакета в бинарное представление, проверка работоспособности выходного
     * потока данных TCP сокета и отправку в него данных.
     *
     * @param packet пакет
     * @return успеность выполнения операции
     */
    @Override
    public boolean send(Packet packet) {
        if (socket == null) {
            I.lc.log(String.format("<red>[%s->%s] Ошика отправки пакета в TCP шлюз, socket == null -> %s<nc>", I.nv.nodeLabel, super.getLabel(), packet.toString()), 1);
            super.getSendSemaphore().release();
            return false;
        }

        if (socket.isClosed()) {
            I.lc.log(String.format("<red>[%s->%s] Ошика отправки пакета в TCP шлюз, сокет закрыт -> %s<nc>", I.nv.nodeLabel, super.getLabel(), packet.toString()), 1);
            super.getSendSemaphore().release();
            return false;
        }

        try {
            socket.getOutputStream().write(packetToByteArray(packet));
        } catch (IOException e) {
            I.lc.log(String.format("<red>[%s->%s] Ошика отправки пакета в TCP шлюз, ошибка ввода-вывода -> %s<nc>", I.nv.nodeLabel, super.getLabel(), packet.toString()), 1);
            e.printStackTrace();
        }

        super.getSendSemaphore().release();

        return true;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}