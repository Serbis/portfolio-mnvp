package ru.serbis.mnvp.gateways.ws;

import ru.serbis.mnvp.acceptors.ws.WsByteInputStream;
import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.mix.ArrayUtils;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.PacketPool;
import ru.serbis.mnvp.structs.general.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Поток обработки входящего потока данных из WS шлюза
 */
public class WsGatewayThread implements Runnable, PacketUtils, ArrayUtils {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Входящий поток даных соединения */
    private WsByteInputStream is;
    /** Метка полного останова потока */
    private boolean stopped = false;
    /** Текстовая метка */
    private String label;
    /** Текстовая метка шлюза от которого была запущен поток */
    private String gatewayLabel;
    /** Флаг звершения потока */
    private boolean alive = true;
    /** Экземплярр обрабатываемого пакета */
    private Packet packet;
    /** Режим парсинга тела пакета*/
    private boolean bodyMode;
    /** Позиция указателя записи при сборке тела пакета */
    private int bodyPos;
    /** Буфер данных входящего потока*/
    private List<Byte> buffer;
    /** Пул пакетов */
    private PacketPool packetPool;

    /**
     * Конструктор потока
     *
     * @param incapsulator инкапсулятор перифирии узла
     * @param is входящий поток данных соединения
     * @param label метка поток
     * @param gatewayLabel метка шлюза
     * @param packetPool ссылка на пул пакетов узла
     */
    public WsGatewayThread(Incapsulator incapsulator, WsByteInputStream is, String label, String gatewayLabel, PacketPool packetPool) {
        this.I = incapsulator;
        this.is = is;
        this.label = label;
        this.gatewayLabel = gatewayLabel;
        this.packetPool = packetPool;

        packet = new Packet();
    }

    public WsByteInputStream getIs() {
        return is;
    }

    /**
     * Обрабатывает входящий поток данных из TCP сокета, формирует из него
     * представления пакетов и передает на обработку сетевого процессора
     */
    @Override
    public void run() {
        I.lc.log(String.format("<blue>[%s] Запущен поток обработки входищх данных ws шлюза<nc>", label), 3);

        buffer = new ArrayList<>();
        while (alive) {
            //System.out.println("---> (" + new Date() + ") --- Вход в режим чтения буфера в потоке захвата с threadId " + threadId);
            for (int b = 0; ((b = is.read(false)) >= 0);) {
                buffer.add((byte) b);
                    if (b == 0x3f) { //Если получена метка SN2
                        // System.out.println("Получена метка SN2");
                        if (buffer.size() >= 28) { //Если в буфере есть данные размером с заголовок или более
                            //System.out.println("Буфер имеет размер заголовка или более");
                            if (buffer.get(buffer.size() - 28) == 0x1f) { //Если в буфере на позиции -30 есть метка SN1
                                //System.out.println("Обнаружена метка SN1 на позиции -30");
                                packet = parsePacketHeader(packet, buffer);
                                bodyMode = true;
                                bodyPos = 0;
                                buffer.clear();
                            } else {
                                completeBody();
                            }
                        } else{
                            completeBody();
                        }
                    } else {
                        completeBody();
                    }
                }

            /*
                Тут есть одна проблема. Считывание из потока являет не блокирующим, поскольку нужно
                 ввести таймаут на ожидание заполнения буфера стрима. Но пока это не реализовано,
                 что бы не душить поток, ниже стоит слип на одну мс. Если его не будет, поток
                 невозможно будет остановить, так как он сожрет все процессорное время не давая
                 усновить alive = false. Нужно сделать что бы вызов is.read был блокирующим с
                 исключением по таймауту.






             */
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        stopped = true;
        I.lc.log(String.format("<blue>[%s] Остановлен поток обработки входищх данных ws шлюза<nc>", label), 3);
    }

    private void completeBody() {
        if (bodyMode) {
            bodyPos++;
            if (bodyPos >= packet.getLength()) {
                bodyMode = false;
                packet.setBody(byteListToArray(buffer));
                packet.setGatewayLabel(gatewayLabel);
                packet.setTs((int) (Math.random() * 1000000));
                buffer.clear();
                bodyPos = 0;

                I.lc.log(String.format("<green>[%s->%s] Получен пакет данных через WS шлюз -> %s<nc>", I.nv.nodeLabel, label, packet.toString()), 3);
                Packet newPacket = new Packet(packet);
                packetPool.put(newPacket);

                packet = new Packet();
            }
        }
    }

    /**
     * Ставит метку останова потока
     */
    public void stop() {
        alive = false;
    }

    public boolean isStopped() {
        return stopped;
    }
}
