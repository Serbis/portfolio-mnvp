package ru.serbis.mnvp.gateways.ws;

import org.java_websocket.WebSocket;
import ru.serbis.mnvp.acceptors.ws.WsByteInputStream;
import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayThread;
import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.PacketPool;
import ru.serbis.mnvp.structs.general.Packet;

import java.io.IOException;
import java.net.Socket;

/**
 * Шлюз ws соединения
 */
public class WsGateway extends Gateway implements PacketUtils {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Сокет соединения */
    private WebSocket socket;
    /** Входящий поток данных соединения */
    private WsByteInputStream inputStream;
    /** Поток обработки входящих данных */
    private WsGatewayThread wsGatewayThread;

    public WsGateway(Incapsulator incapsulator, WsByteInputStream is, WebSocket socket, String label, PacketPool packetPool) {
        super(incapsulator);
        super.setLabel(label);
        super.setPacketPool(packetPool);
        this.socket = socket;
        this.I = incapsulator;
        this.inputStream = is;
    }

    /**
     * Запускает процесс обработки данных входящего потока
     */
    @Override
    public void run() {
        wsGatewayThread = new WsGatewayThread(I, inputStream, String.format("%s->%s->%s", I.nv.nodeLabel,  super.getLabel(), "GatewayThread"), super.getLabel(), super.getPacketPool());
        Thread thread = new Thread(wsGatewayThread);
        thread.start();
    }

    /**
     * Останавливает поток шлюза тем самым реализую процедуру закрытия
     * последнего
     */
    @Override
    public void stop() {
        socket.close();
        wsGatewayThread.stop();
        while (!wsGatewayThread.isStopped()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        I.lc.log(String.format("<blue>[%s->%s] Остановлен WS шлюз %s<nc>", I.nv.nodeLabel, super.getLabel(), super.getLabel()), 3);
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
            I.lc.log(String.format("<red>[%s->%s] Ошика отправки пакета в WS шлюз, socket == null -> %s<nc>", I.nv.nodeLabel, super.getLabel(), packet.toString()), 1);
            return false;
        }

        if (socket.isClosed()) {
            I.lc.log(String.format("<red>[%s->%s] Ошика отправки пакета в WS шлюз, сокет закрыт -> %s<nc>", I.nv.nodeLabel, super.getLabel(), packet.toString()), 1);
            return false;
        }

        try {
            socket.send(packetToByteArray(packet));
        } catch (Exception e) {
            I.lc.log(String.format("<red>[%s->%s] Ошика отправки пакета в WS шлюз -> %s<nc>", I.nv.nodeLabel, super.getLabel(), packet.toString()), 1);
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Возращает поток данных шлюза
     *
     * @return представление потока
     */
    public WsByteInputStream getInputStream() {
        return wsGatewayThread.getIs();
    }

    /**
     * Возвращает хеш сокета
     *
     * @return хещ объекта
     */
    public int getSocketHash() {
        return socket.hashCode();
    }
}
