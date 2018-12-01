package ru.serbis.mnvp.np;

import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.translations.EchoTransaction;
import ru.serbis.mnvp.np.translations.PreqTransaction;
import ru.serbis.mnvp.np.translations.Transaction;
import ru.serbis.mnvp.structs.general.Packet;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

/**
 * Поток реализующий логику обработки входящего пакета
 */
public class PacketReceiverThread implements Runnable, PacketUtils {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Метка полного останова потока */
    private boolean stopped = false;
    /** Флаг активности потока */
    private boolean alive = true;
    /** Текстовая метка потока */
    private String label;
    /** Пул пакетов */
    private PacketPool packetPool;


    public PacketReceiverThread(Incapsulator incapsulator, String label, PacketPool packetPool) {
        this.I = incapsulator;
        this.label = label;
        this.packetPool = packetPool;
    }

    @Override
    public void run() {
       I.lc.log(String.format("<blue>[%s] Запущен поток ресивера пакетов %s<nc>", label, label), 3);
        while (alive) {

            Packet packet = null;
            try {
                //try {
                    /*NetworkProcessor np = NetworkProcessor.getInstance(nodeLabel);
                    if (np != null) {
                        np.getPacketPoolSemaphore().acquire();
                        packet = np.getNewPacket();
                    } else {
                        I.lc.log(String.format("<blue>[%s] NP_NULL<nc>", label), 100);

                    }*/
                packet = packetPool.get();


                //} catch (InterruptedException e) {
                //    e.printStackTrace();
               // }
            } catch (NullPointerException e) {
                System.out.println("AAAAAAAAAA");
                e.printStackTrace();
                //return;
            }

            if (packet != null) {
                processIncomingPacket(packet);

            } else {
                //I.lc.log(String.format("<blue>[%s] PACKET NULL<nc>", label), 3);
            }
        }
       I.lc.log(String.format("<blue>[%s] Остановлен поток ресивера пакетов %s<nc>", label, label), 3);
        stopped = true;
    }

    /**
     * Останавливает поток
     */
    public void stop() {
        alive = false;
    }

    public boolean isStopped() {
        return stopped;
    }

    /**
     * Производит обработку входящего пакета данныхъ
     *
     * @param packet входящий пакета данных
     */
    private void processIncomingPacket(Packet packet) {

        //Если узел находится в режиме отладки
        /*if (NodeVars.getInstance(nodeLabel).isDebugMode()) {
            if (!NodeDebugger.getInstance(nodeLabel).intercrptIncomingPacket(packet))
                return;
        }*/

        //Уменьшаем ttl пакета независимо от типа
        packet.setTtl((short) (packet.getTtl() - 1));

        //Заносим новый маршрут в таблицу маршрутизации
        int source = packet.getSource();
        int gateway = I.gc.getGatewayNetworkAddress(packet.getGatewayLabel());
        short dest = (short) (packet.getStartTtl() - packet.getTtl());
        //Занести в таблицу маршрутищации все за исключением пакетов источником которых является сам узел
        if (source != I.nv.networkAddress)
            I.np.updateRoute(source, gateway, dest);

        //Определяем являет ли пакет транзитнымтранзитным
        if (packet.getDest() != I.nv.networkAddress && packet.getType() != 0) {
            //Если ttl пакета меньше или рано нулу, паке дальше не пройдет
            if (packet.getTtl() <= 0)
                return;

            switch (packet.getType()) {
                case 1: //NETWORK_ERROR
                    processTransitNEPacket(packet);
                    break;
                case 2: //PREQ
                    processTransitPreqPacket(packet);
                    break;
                case 4: //ECHO
                    processTransitEchoPacket(packet);
                    break;
                case 5: //RPC
                    processTransitRpcPacket(packet);
                    break;
            }

        } else {
            switch (packet.getType()) {
                case 0: //HELLO
                    processTargetHelloPacket(packet);
                    break;
                case 1: //NETWORK_ERROR
                    processTargetNEPacket(packet);
                    break;
                case 2: //PREQ
                    processTargetPreqPacket(packet);
                    break;
                case 4: //ECHO
                    processTargetEchoPacket(packet);
                    break;
                case 5: //RPC
                    processTargetRpcPacket(packet);
                    break;
            }
        }


    }

    /**
     * Процессируют hello сообщения от узла. Задача процессирования состоит
     * в следующем.
     *      -Обновлении флага последней активности шлюза
     *      -Поиске в таблице маршрутизации записи с данным адресом и
     *       добавлении ее туда если она не была найдена.
     */
    private void processTargetHelloPacket(Packet packet) {
        //Обновить метку активности шлюза
        I.gc.updateGatewayActivity(packet.getGatewayLabel());
        I.gc.setGatewayNetworkAddress(packet.getGatewayLabel(), (int) packet.getSource());

        //Обновить запись в таблице маршрутизации
        I.np.updateRoute(packet.getSource(), packet.getSource(), 1);
    }

    private void processTransitPreqPacket(Packet packet) {
        if (packet.getFlags() == 0x00) {
            I.lc.log(String.format("<blue>[%s] Получен транзитный PREQ пакет от узла %d к %d c ttl <nc>", label, packet.getSource(), packet.getDest(), packet.getTtl()), 3);

            //int com = Integer.parseInt(new String(packet.getBody()));
            //com++;
            //packet.setBody(String.valueOf(com).getBytes());
            //packet.setLength((short) packet.getBody().length);

            Iterator<Map.Entry<String, Gateway>> iterator = I.gc.getGatewaysPoolIterator();

            while (iterator.hasNext()) {
                Gateway gateway = iterator.next().getValue();
                if (!gateway.getLabel().equals(packet.getGatewayLabel())) {
                    try {
                        gateway.getSendSemaphore().acquire();
                        gateway.send(packet);
                        gateway.getSendSemaphore().release();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    I.lc.log(String.format("[%s] <blue>Отправлен транзитный PREQ пакет к узлу %d через шлюз %s с сетевым адресом %d<nc>", label, packet.getDest(), gateway.getLabel(), gateway.getNetworkAddress()), 3);
                }
            }
        } else {
            I.lc.log(String.format("<blue>[%s] Получен транзитный PREQ_ACK пакет от узла %d к %d<nc>", label, packet.getSource(), packet.getDest()), 3);

            I.np.sendPacket(packet);
        }
    }

    private void processTargetPreqPacket(Packet packet) {
        if (packet.getFlags() == 0x00) {
            I.lc.log(String.format("<blue>[%s] Получен целевой PREQ пакет от узла %d<nc>", label, packet.getSource()), 3);

            int source = packet.getSource();
            int dest = packet.getDest();
            packet.setSource(dest);
            packet.setDest(source);
            packet.setTtl(packet.getStartTtl());
            packet.setFlags((byte) 0x01);

            I.np.sendPacket(packet);
            I.lc.log(String.format("<blue>[%s] Произведен ответ на PREQ пакет от узла %d<nc>", label, source), 3);
        } else {
            I.lc.log(String.format("<blue>[%s] Получен целевой PREQ_ACK пакет от узла %d к %d<nc>", label, packet.getSource(), packet.getDest()), 3);

            Transaction transaction = I.np.getTranslation(packet.getMsgId());

            if (transaction == null) {
                I.lc.log(String.format("<blue>[%s] Невозможно обработать целевой PREQ_ACK пакет от узла %d по причине отутствия трансляции<nc>", label, packet.getDest()), 3);
                return;
            }

            PreqTransaction preqTranslation = (PreqTransaction) transaction;
            preqTranslation.receivePacket(packet/*, GatewaysController.getInstance(nodeLabel).getGatewayNetworkAddress(packet.getGatewayLabel())*/);
        }
    }

    /**
     * Обрабатывает транизтный ECHO пакет
     *
     * @param packet пакет
     */
    private void processTransitEchoPacket(Packet packet) {
        I.lc.log(String.format("<blue>[%s] Получен транзитный ECHO пакет от узла %d к %d<nc>", label, packet.getSource(), packet.getDest()), 3);

        NetworkProcessor.PacketSendResult result = I.np.sendPacket(packet);

        if (result == NetworkProcessor.PacketSendResult.ROUTE_NOT_FOUND) {
            I.lc.log(String.format("<blue>[%s] При отправке транзитный ECHO пакета от узла %d к %d не был найден необходимый маршрут. Выполняется возврат ошибки NETWORK_ERROR:0<nc>", label, packet.getSource(), packet.getDest()), 3);
            I.np.sendPacket(createReverseNetError(packet, 0));
        } else if (result == NetworkProcessor.PacketSendResult.INTERNAL_ERROR) {
            I.lc.log(String.format("<blue>[%s] При отправке транзитный ECHO пакета от узла %d к %d возникла внутренняя ошибка. Выполняется возврат ошибки NETWORK_ERROR:1<nc>", label, packet.getSource(), packet.getDest()), 3);
            I.np.sendPacket(createReverseNetError(packet, 1));
        }
    }

    /**
     * Обрабатывает целевой ECHO пакет. Механика обработки:
     *  -Если получн ECHO пакет, происходит отправка ECHO_ACK
     *   источнику пакета.
     *  -Если получена ECHO_ACK модификация пакета, происходит
     *   передача полученного пакета управляющей трансляции.
     *
     * @param packet паект
     */
    private void processTargetEchoPacket(Packet packet) {
        if (packet.getFlags() == 0x00) {
            I.lc.log(String.format("<blue>[%s] Получен целевой ECHO пакет от узла %d<nc>", label, packet.getSource()), 3);

            int source = packet.getSource();
            int dest = packet.getDest();
            packet.setSource(dest);
            packet.setDest(source);
            packet.setTtl(packet.getStartTtl());
            packet.setFlags((byte) 0x01);

            NetworkProcessor.PacketSendResult result = I.np.sendPacket(packet);

            if (result == NetworkProcessor.PacketSendResult.INTERNAL_ERROR) {
                I.lc.log(String.format("<blue>[%s] При отправки ответа на ECHO пакета от узла %d возникла внутренняя ошибка. Выполняется возврат ошибки NETWORK_ERROR:1<nc>", label, packet.getSource()), 3);
                I.np.sendPacket(createReverseNetError(packet, 1));
            } else {
                I.lc.log(String.format("<blue>[%s] Произведен ответ на ECHO пакет от узла %d<nc>", label, source), 3);
            }

        } else {
            I.lc.log(String.format("<blue>[%s] Получен целевой ECHO_ACK пакет от узла %d к %d<nc>", label, packet.getSource(), packet.getDest()), 3);

            Transaction transaction = I.np.getTranslation(packet.getMsgId());

            if (transaction == null) {
                I.lc.log(String.format("<blue>[%s] Невозможно обработать целевой ECHO_ACK пакет от узла %d по причине отутствия трансляции<nc>", label, packet.getDest()), 3);
                return;
            }

            EchoTransaction echoTranslation = (EchoTransaction) transaction;
            echoTranslation.receivePacket(packet);
        }
    }

    /**
     * Обрабатывает транизтный NETWORK_ERROR пакет
     *
     * @param packet пакет
     */
    private void processTransitNEPacket(Packet packet) {
        I.lc.log(String.format("<blue>[%s] Получен транзитный NETWORK_ERROR пакет от узла %d к %d<nc>", label, packet.getSource(), packet.getDest()), 3);

        NetworkProcessor.PacketSendResult result = I.np.sendPacket(packet);

        if (result == NetworkProcessor.PacketSendResult.ROUTE_NOT_FOUND) {
            I.lc.log(String.format("<blue>[%s] При отправке транзитного NETWORK_ERROR пакета от узла %d к %d не был найден необходимый маршрут. Нельзя отправить пакет<nc>", label, packet.getSource(), packet.getDest()), 3);
        } else if (result == NetworkProcessor.PacketSendResult.INTERNAL_ERROR) {
            I.lc.log(String.format("<blue>[%s] При отправке транзитного NETWORK_ERROR пакета от узла %d к %d возникла внутренняя ошибка. Нельзя отправить пакет<nc>", label, packet.getSource(), packet.getDest()), 3);
        }
    }

    /**
     * Обрабатывает целевой NETWORK_ERROR пакет. Механика обработки:
     *  - Поиск отслеживающей транслции и передача ей пакета на обработку
     *
     * @param packet пакет
     */
    private void processTargetNEPacket(Packet packet) {
        ByteBuffer bf = ByteBuffer.wrap(packet.getBody());
        int code = bf.getInt();

        I.lc.log(String.format("<blue>[%s] Получен целевой NETWORK_ERROR:%d пакет от узла %d к %d<nc>", label, code, packet.getSource(), packet.getDest()), 3);

        Transaction transaction = I.np.getTranslation(packet.getMsgId());

        if (transaction == null) {
            I.lc.log(String.format("<blue>[%s] Невозможно обработать целевой NETWORK_ERROR:%d пакет от узла %d по причине отутствия трансляции<nc>", label, code, packet.getDest()), 3);
            return;
        }

        transaction.receivePacket(packet);
    }

    /**
     * Обрабатывает транизтный RPC пакет
     *
     * @param packet пакет
     */
    private void processTransitRpcPacket(Packet packet) {
        I.lc.log(String.format("<blue>[%s] Получен транзитный RPC пакет от узла %d к %d<nc>", label, packet.getSource(), packet.getDest()), 3);

        NetworkProcessor.PacketSendResult result = I.np.sendPacket(packet);

        if (result == NetworkProcessor.PacketSendResult.ROUTE_NOT_FOUND) {
            I.lc.log(String.format("<blue>[%s] При отправке транзитного RPC пакета от узла %d к %d не был найден необходимый маршрут. Выполняется возврат ошибки NETWORK_ERROR:0<nc>", label, packet.getSource(), packet.getDest()), 3);
            I.np.sendPacket(createReverseNetError(packet, 0));
        } else if (result == NetworkProcessor.PacketSendResult.INTERNAL_ERROR) {
            I.lc.log(String.format("<blue>[%s] При отправке транзитного RPC пакета от узла %d к %d возникла внутренняя ошибка. Выполняется возврат ошибки NETWORK_ERROR:1<nc>", label, packet.getSource(), packet.getDest()), 3);
            I.np.sendPacket(createReverseNetError(packet, 1));
        }
    }

    /**
     * Обрабатывает целевой RPC пакет. Механика обработки:
     *  ?????????????????????????????????????????????????
     *
     * @param packet пакет
     */
    private void processTargetRpcPacket(Packet packet) {
        if ((packet.getFlags() & 1) == 0) { //Установлен флаг ACK
            I.lc.log(String.format("<blue>[%s] Получен целевой RPC пакет от узла %d<nc>", label, packet.getSource()), 3);

            //RpcController.getInstance(nodeLabel, );
        } else {
            I.lc.log(String.format("<blue>[%s] Получен целевой RPC_ACK пакет от узла %d к %d<nc>", label, packet.getSource(), packet.getDest()), 3);

            /*Transaction translation = NetworkProcessor.getInstance(nodeLabel).getTranslation(packet.getMsgId());

            if (translation == null) {
                I.lc.log(String.format("<blue>[%s] Невозможно обработать целевой ECHO_ACK пакет от узла %d по причине отутствия трансляции<nc>", label, packet.getDest()), 3);
                return;
            }

            EchoTransaction echoTranslation = (EchoTransaction) translation;
            echoTranslation.receivePacket(packet);*/
        }
    }

    /**
     * Создает реверсивный (предназначенный отправителю оригинального пакета)
     * пакет типа NETWORK_ERROR с заданным кодом.
     *
     * @param origPacket пакет спровоцировавший ошибку
     * @param code код ошибки
     * @return пакет типа NETWORK_ERROR
     */
    private Packet createReverseNetError(Packet origPacket, int code) {
        Packet packet = new Packet(origPacket);
        packet.setTtl(origPacket.getStartTtl());
        packet.setDest(origPacket.getSource());
        packet.setSource(I.nv.networkAddress);
        packet.setType((byte) Packet.Type.NETWORK_ERROR.ordinal());

        ByteBuffer bf = ByteBuffer.allocate(4);
        bf.putInt(code);
        packet.setBody(bf.array());
        packet.setLength((short) packet.getBody().length);

        return packet;
    }
}
