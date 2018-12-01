package ru.serbis.mnvp.gateways;

import org.java_websocket.WebSocket;
import ru.serbis.mnvp.gateways.tcp.TcpGateway;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.gateways.ws.WsGateway;
import ru.serbis.mnvp.gateways.ws.WsGatewayConfig;
import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.np.PacketPool;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Контроллер шлюзов. Задачей данного класса, явялется создание,
 * содержание и управление всему существующими у узла шлюзами. Любой
 * шдюз, порождается, управляется и уничтожается тольк в этом классе.
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class GatewaysController implements PacketUtils {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Пул шлюзов */
    private  Map<String, Gateway> gatewaysPool = new ConcurrentHashMap<>();
    /** Пул входящих пакетов*/
    private PacketPool packetPool;

    /**
     * Конструктор
     *
     * @param incapsulator инкапсулятор перифирии узла
     */
    public GatewaysController(Incapsulator incapsulator) {
        this.I = incapsulator;
    }

    /**
     * Останавливает все отырытые шлюзы
     */
    public void stopAllGateways() {
        gatewaysPool.entrySet().forEach(stringGatewayEntry -> stringGatewayEntry.getValue().stop());
    }

    /**
     * Создает шлюз методом инициации нового соединения.
     *
     * @param config конфигурация шлюза
     */
    public void generateGateway(GatewayConfig config) {
        if (config instanceof TcpGatewayConfig) {
            generateTcpGateway((TcpGatewayConfig) config);
        }
    }

    /**
     * Создает новый шлюз методом инициации. Метод требует что бы в
     * конфигурации шлюза были заполнены поля типа G. Устанавливает соединение
     * с хостом и размерщает новый шлюз в пул шлюзов.
     *
     * @param config конфигурация
     */
    private void generateTcpGateway(TcpGatewayConfig config) {
        if (config.getHost() == null) {
            I.lc.log(String.format("<red>[%s] Ошибка при создании TCP шлюза методом инициации соединения. Не задат объект хост<nc>", I.nv.nodeLabel), 1);
            return;
        }

        if (config.getPort() == -1) {
            I.lc.log(String.format("<red>[%s] Ошибка при создании TCP шлюза методом инициации соединения. Не задат целевой порт<nc>", I.nv.nodeLabel), 1);
            return;
        }

        if (config.getLabel() == null)
            config.setLabel(String.format("Gateway_TCP_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));

        int retryCount = 1;
        Socket socket = null;

        while (retryCount <= config.getHostConnectRetryCount()) {
            try {
                socket = new Socket(config.getHost(), config.getPort());
                break;
            } catch (IOException e) {
                I.lc.log(String.format("<yellow>[%s] Ошибка при создании TCP шлюза методом инициации соединения. Не удается установить соединения с хостом %s:%d. Поптытка - %d<nc>", I.nv.nodeLabel, config.getHost(), config.getPort(), retryCount), 2);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                retryCount++;
            }
        }

        if (socket == null) {
            I.lc.log(String.format("<red>[%s] Ошибка при создании TCP шлюза методом инициации соединения. Не удалось установить соединение с хостом %s:%d.<nc>", I.nv.nodeLabel, config.getHost(), config.getPort()), 1);
            return;
        }

        try {
            socket.setSoTimeout(10000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        TcpGateway tcpGateway = new TcpGateway(I, socket, config.getLabel(), packetPool);
        tcpGateway.setLastIncomingActivity(new Date().getTime());
        I.lc.log(String.format("<blue>[%s] Создан новый шлюз TCP методом иницииации к хосту %s:%d с меткой %s<nc>",I.nv.nodeLabel, config.getHost(), config.getPort(), config.getLabel()), 3);
        tcpGateway.run();
        gatewaysPool.put(config.getLabel(), tcpGateway);
    }

    /**
     * Создает шлюз методо оборачивания существуеющего соединения
     *
     * @param config конфигурация шлюза
     */
    public void wrapGateway(GatewayConfig config) {
        if (config instanceof TcpGatewayConfig) {
            wrapTcpGateway((TcpGatewayConfig) config);
        } else if (config instanceof WsGatewayConfig) {
            wrapWsGateway((WsGatewayConfig) config);
        }
    }

    /**
     * Возвращает итератор пула шлюзов
     *
     * @return итератор
     */
    public synchronized Iterator<Map.Entry<String, Gateway>> getGatewaysPoolIterator() {
        return gatewaysPool.entrySet().stream().iterator();
    }

    /**
     * Обновляет временную метку активности шлюза
     *
     * @param gatewayLabel метка шлюза
     */
    public void updateGatewayActivity(String gatewayLabel) {
        Gateway gateway = gatewaysPool.get(gatewayLabel);
        if (gateway == null) {
            I.lc.log(String.format("[%s] <yellow>Ошибка обновления активности шлюза. Шлюз с меткой %s не найден<nc>", I.nv.nodeLabel, gatewayLabel), 2);
            return;
        }
        gateway.setLastIncomingActivity(new Date().getTime());
    }

    /**
     * Назначает щлюзу указанный в параметре сетевой адрес
     *
     * @param gatewayLabel метка шлюза
     * @param networkAddress сетевой адрес
     */
    public void setGatewayNetworkAddress(String gatewayLabel, int networkAddress) {
        Gateway gateway = gatewaysPool.get(gatewayLabel);
        if (gateway == null) {
            I.lc.log(String.format("[%s] <yellow>Ошибка при назначении сетевого адреса шлюзу. Шлюз с меткой %s не найден<nc>", I.nv.nodeLabel, gatewayLabel), 2);
            return;
        }

        gateway.setNetworkAddress(networkAddress);
    }

    /**
     * Возвращает сетевой адрес узла к которому ведет шлюз с указанной в
     * параметре меткой
     *
     * @param gatewayLabel метка шлюза
     * @return  адрес узла к которому ведет шлюз
     */
    public int getGatewayNetworkAddress(String gatewayLabel) {
        Gateway gateway = gatewaysPool.get(gatewayLabel);
        if (gateway == null) {
            I.lc.log(String.format("[%s] <yellow>Ошибка при получении сетевого адреса шлюза. Шлюз с меткой %s не найден<nc>", I.nv.nodeLabel, gatewayLabel), 2);
            return -1;
        }

        return gateway.getNetworkAddress();
    }


    /**
     * Возвращает шлюз по его сетевому адресу
     *
     * @param networkAddress сетевой адрес искомого шлюза
     * @return шлюз или null если последний не был найден
     */
    public Gateway getGatewayByNetworkAddress(int networkAddress) {
        Iterator<Map.Entry<String, Gateway>> iterator = gatewaysPool.entrySet().stream().iterator();

        while (iterator.hasNext()) {
            Gateway gateway = iterator.next().getValue();
            if (gateway.getNetworkAddress() == networkAddress)
                return gateway;
        }

        return null;
    }

    /**
     * Возвращает шлюз типа WsGateway у которого хеш объекта сокета
     * идентичен хешу объекта из параметра.
     *
     * @param socket объект для сравнения
     * @return шлюиз или null если последний не был найден
     */
    public WsGateway getWsGatewayBySocket(WebSocket socket) {
        Iterator<Map.Entry<String, Gateway>> iterator = gatewaysPool.entrySet().stream().iterator();

        int inputHash = socket.hashCode();

        while (iterator.hasNext()) {
            Gateway gateway = iterator.next().getValue();
            if (gateway instanceof WsGateway) {
                WsGateway wsGateway = (WsGateway) gateway;
                if (wsGateway.getSocketHash() == inputHash)
                    return wsGateway;
            }
        }

        return null;
    }

    /**
     * Создает новый tcp шлюз методом оборачиваня объекта socket и добавляет
     * его в пул шлюзов
     *
     * @param config конфигурация шлюза
     */
    private void wrapTcpGateway(TcpGatewayConfig config) {
        if (config.getSocket() == null) {
            I.lc.log(String.format("<yellow>[%s] Ошибка при создании TCP шлюза методом оборачивания соединения. Не задат объект Socket<nc>", I.nv.nodeLabel), 2);
            return;
        }

        if (config.getLabel() == null)
            config.setLabel(String.format("Gateway_TCP_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));

        TcpGateway tcpGateway = new TcpGateway(I, config.getSocket(), config.getLabel(), packetPool);
        tcpGateway.setLastIncomingActivity(new Date().getTime());
        I.lc.log(String.format("<blue>[%s] Создан новый шлюз TCP методом обертывания с меткой %s<nc>",I.nv.nodeLabel, config.getLabel()), 3);
        tcpGateway.run();
        gatewaysPool.put(config.getLabel(), tcpGateway);

    }

    /**
     * Создает новый ws шлюз методом оборачиваня объекта WebSocket и добавляет
     * его в пул шлюзов
     *
     * @param config конфигурация шлюза
     */
    private void wrapWsGateway(WsGatewayConfig config) {
        if (config.getSocket() == null) {
            I.lc.log(String.format("<yellow>[%s] Ошибка при создании WS шлюза методом оборачивания соединения. Не задат объект Socket<nc>", I.nv.nodeLabel), 2);
            return;
        }

        if (config.getLabel() == null)
            config.setLabel(String.format("Gateway_WS_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));

        WsGateway wsGateway = new WsGateway(I, config.getInputStream(), config.getSocket(), config.getLabel(), packetPool);
        wsGateway.setLastIncomingActivity(new Date().getTime());
        I.lc.log(String.format("<blue>[%s] Создан новый шлюз WS методом обертывания с меткой %s<nc>",I.nv.nodeLabel, config.getLabel()), 3);
        wsGateway.run();
        gatewaysPool.put(config.getLabel(), wsGateway);

    }

    /**
     * Удаляет шлюз из пула. Данная процедура помимо удаления самого шлюза,
     * задействует процедуру удаления записей в таблице маршрутиизации
     * связанных с сетевым адром шлюза.
     */
    public void removeGateway(String gatewayLabel) {
        //Найти шлюз
        Gateway gateway = gatewaysPool.get(gatewayLabel);
        if (gateway == null) {
            I.lc.log(String.format("[%s] <yellow>Ошибка при удалении сетевого адреса шлюза. Шлюз с меткой %s не найден<nc>", I.nv.nodeLabel, gatewayLabel), 2);
            return;
        }

        int gatewayAddress = gateway.getNetworkAddress();
        //Остановить и удалить
        gateway.stop();
        gatewaysPool.remove(gatewayLabel);
        I.lc.log(String.format("<blue>[%s] Удален шлюз с меткой %s<nc>",I.nv.nodeLabel, gatewayLabel), 3);

        //Очистить таблицу маршрутизации от записей с данным шлюзом
        I.np.removeRoutesByGateway(gatewayAddress);
    }

    public void setPacketPool(PacketPool packetPool) {
        this.packetPool = packetPool;
    }
}
