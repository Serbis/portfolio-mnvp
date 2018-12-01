package ru.serbis.mnvp.np;

import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.mix.NetworkUtils;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.np.translations.*;
import ru.serbis.mnvp.np.translations.PreqTransaction;
import ru.serbis.mnvp.np.translations.RpcTransaction;
import ru.serbis.mnvp.rpc.RpcCallDef;
import ru.serbis.mnvp.rt.RoutingTable;
import ru.serbis.mnvp.structs.general.Packet;

import java.util.*;
import java.util.concurrent.*;

/**
 * Контроллер сети. Является опорным классом для запуска потоков обработки
 * входящих пакетов а так же предоставляет большую часть api для работы
 * с сетью
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class NetworkProcessor implements PacketUtils, NetworkUtils {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Пул потоков ресивера входящих пакетов */
    private ThreadPoolExecutor executor;
    /** Список запущенных потоков ресивера пакетов*/
    private List<PacketReceiverThread> receiverThreadList;
    /** Текстова метка */
    private String label;
    /** Таблица маршрутизации */
    private RoutingTable routingTable;
    /** Пул трансляций */
    private Map<Integer, Transaction> translationsPool = new HashMap<>();
    /** Пул пакетов */
    private PacketPool packetPool;

    /**
     * Конструктор
     *
     * @param incapsulator инкапсулятор перифирии узла
     */
    public NetworkProcessor(Incapsulator incapsulator) {
        this.I = incapsulator;
    }

    /**
     * Запускает сетевой процессор в работу. Инициализирует систему данными
     * из конфигурации. Запуска потоки ресиверы пакетов
     *
     * @param config конфигурация сетевого процессора
     */
    public void run(NetworkProcessorConfig config) {
        I.lc.log(String.format("<blue>[%s->%s] Создан новый сетевой процессор<nc>", label, label), 3);

        receiverThreadList = new ArrayList<>();
        routingTable = new RoutingTable(I);

        executor = new ThreadPoolExecutor(config.getPacketReceiverThreadCount(), config.getPacketReceiverThreadCount(), 1000000,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < config.getPacketReceiverThreadCount(); i++) {
            PacketReceiverThread packetReceiverThread = new PacketReceiverThread(I, String.format("%s->PacketReceiverThread_%d", label, ThreadLocalRandom.current().nextInt(10000, 20000)), packetPool);
            receiverThreadList.add(packetReceiverThread);
           executor.execute(packetReceiverThread);
        }
    }

    /**
     * Останавливает сетевой процессор
     */
    public void stop() {
        receiverThreadList.forEach(PacketReceiverThread::stop);
        receiverThreadList.forEach(receiverThread -> {
            while (!receiverThread.isStopped()) {}
        });
    }

    /**
     * Обновляет запись в таблице маршрутизации об узле
     *
     * @param dest адрес целевого узла
     * @param gateway шлюз к целвогму узлу
     * @param distance дистанция до целвого узла
     */
    public void updateRoute(int dest, int gateway, int distance) {
        routingTable.updateRoute(dest, gateway, distance);
    }

    /**
     * Выполняет эхо запрос к удаленному узлу.
     *
     * @param dest сетевой адрес искомого узла
     * @param finisherCallback обртный вызов, который будет совершен после
     *                         заврешния трансляции
     */
    public void sendEchoRequest(int dest, EchoTransaction.TranslationFinisher finisherCallback) {
        //Создать echo пакет
        Packet packet = createEchoPacket(getNewMsgId(I), I.nv.networkAddress, dest);

        //Создать трансляцию
        EchoTransaction echoTranslation = new EchoTransaction(I, packet, finisherCallback, (id) -> translationsPool.remove(id));

        //Внести трансляцию в пул трансляций
        translationsPool.put(packet.getMsgId(), echoTranslation);

        //Запустить трансляцию
        echoTranslation.start();
    }

    /**
     * Выполняет эхо запрос к удаленному узлу.
     *
     * @param callDef определение вызова
     * @param finisherCallback обртный вызов, который будет совершен после
     *                         заврешния трансляции
     */
    public void sendRpcRequest(RpcCallDef callDef, RpcTransaction.TranslationFinisher finisherCallback) {
        //Создать rpc пакет
        Packet packet = createRpcPacket(getNewMsgId(I), I.nv.networkAddress, callDef.getDestination(), callDef.getCall());

        //Создать трансляцию
        RpcTransaction rpcTranslation = new RpcTransaction(I, packet, callDef.getTimeout(), finisherCallback, (id) -> translationsPool.remove(id));

        //Внести трансляцию в пул трансляций
        translationsPool.put(packet.getMsgId(), rpcTranslation);

        //Запустить трансляцию
        rpcTranslation.start();
    }

    /**
     * Выполняет запрос на динамический поиск маршрута до целевого узла
     *
     * @param dest сетевой адрес искомого узла
     * @param finisherCallback обртный вызов, который будет совершен после
     *                         заврешния трансляции
     */
    public void sendPreqRequest(int dest, PreqTransaction.TranslationFinisher finisherCallback) {
        //Создать preq пакет
        Packet packet = createPreqPacket(getNewMsgId(I), I.nv.networkAddress, dest);

        //Создать трансляцию
        PreqTransaction preqTranslation = new PreqTransaction(I, packet, finisherCallback, (id) -> translationsPool.remove(id));

        //Внести трансляцию в пул трансляций
        translationsPool.put(packet.getMsgId(), preqTranslation);

        //Запустить трансляцию
        preqTranslation.start();
    }

    /**
     * Выполняет отправку пакета в сеть. В задачу входит поиск маршрута в
     * таблице маршрутизации и отправку пакета в найденый шлюз. Если маршрут
     * не был обнаружен в таблице, вовзращает статус ROUTE_NOT_FOUND. Если
     * отправка пакеты была выполнена успешно возвращает статус OK.
     *
     * @param packet пакет для отправки
     * @return статус отправки пакета
     */
    public PacketSendResult sendPacket(Packet packet) {
        int gatewayAddr = routingTable.findRoute(packet.getDest());
        if (gatewayAddr == -1) {
            I.lc.log(String.format("<blue>[%s->%s] Не удалось отправить пакет к узлу %d - не найден маршрут<nc>", this.I.nv.nodeLabel, label, packet.getDest()), 3);

            return PacketSendResult.ROUTE_NOT_FOUND;
        }

        Gateway gateway = I.gc.getGatewayByNetworkAddress(gatewayAddr);

        if (gateway == null) {
            I.lc.log(String.format("<red>[%s->%s] Не удалось отправить пакет к узлу %d - не найден объект шлюза<nc>", this.I.nv.nodeLabel, label, packet.getDest()), 1);

            return PacketSendResult.INTERNAL_ERROR;
        }

        boolean gwsr = false;
        try {
            gateway.getSendSemaphore().acquire();
            gwsr =  gateway.send(packet);
            gateway.getSendSemaphore().release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        if (!gwsr)
            return PacketSendResult.INTERNAL_ERROR;

        return PacketSendResult.OK;
    }

    /**
     * Возвращает объект транслции по ее идентификатору
     *
     * @param translationId идентификатор трансляции
     * @return объект трансяции, может вернуть null, если трансляция не
     *         найдена в пуле
     */
    public Transaction getTranslation(int translationId) {
        return translationsPool.get(translationId);
    }

    /**
     * Удаляет все записи в таблице маршрутизации с указанным шлюзом
     *
     * @param gateway сетевой адрес шлюза
     */
    public void removeRoutesByGateway(int gateway) {
        routingTable.removeAllRoutesByGateway(gateway);
    }

    /**
     * ВНИМАНИЕ!!! ОТЛАДОЧНЫЙ МЕТОД!
     *
     * Возвращает таблицу маршрутизации узла
     *
     * @return таблица маршрутизации
     */
    public RoutingTable getRoutingTableForDebug() {
        return routingTable;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    public void setPacketPool(PacketPool packetPool) {
        this.packetPool = packetPool;
    }

    /**
     * Перечисление результатов выполнения отправки пакета
     */
    public enum PacketSendResult {
        OK, ROUTE_NOT_FOUND, INTERNAL_ERROR
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkProcessor that = (NetworkProcessor) o;

        if (I != null ? !I.equals(that.I) : that.I != null) return false;
        if (executor != null ? !executor.equals(that.executor) : that.executor != null) return false;
        if (receiverThreadList != null ? !receiverThreadList.equals(that.receiverThreadList) : that.receiverThreadList != null)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (routingTable != null ? !routingTable.equals(that.routingTable) : that.routingTable != null) return false;
        if (translationsPool != null ? !translationsPool.equals(that.translationsPool) : that.translationsPool != null)
            return false;
        return packetPool != null ? packetPool.equals(that.packetPool) : that.packetPool == null;
    }

    @Override
    public int hashCode() {
        int result = I != null ? I.hashCode() : 0;
        result = 31 * result + (executor != null ? executor.hashCode() : 0);
        result = 31 * result + (receiverThreadList != null ? receiverThreadList.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (routingTable != null ? routingTable.hashCode() : 0);
        result = 31 * result + (translationsPool != null ? translationsPool.hashCode() : 0);
        result = 31 * result + (packetPool != null ? packetPool.hashCode() : 0);
        return result;
    }
}
