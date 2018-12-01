package ru.serbis.mnvp.general;

import ru.serbis.mnvp.acceptors.AcceptorConfig;
import ru.serbis.mnvp.acceptors.AcceptorsController;
import ru.serbis.mnvp.debugger.NodeDebugger;
import ru.serbis.mnvp.exceptions.MnvpInitializeException;
import ru.serbis.mnvp.gateways.GatewayConfig;
import ru.serbis.mnvp.gateways.GatewaysController;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.np.PacketPool;
import ru.serbis.mnvp.np.translations.EchoTransaction;
import ru.serbis.mnvp.np.translations.PreqTransaction;
import ru.serbis.mnvp.rpc.RpcCallDef;
import ru.serbis.mnvp.rpc.RpcController;
import ru.serbis.mnvp.rpc.RpcExecuteResult;
import ru.serbis.mnvp.structs.general.NodeConfig;
import ru.serbis.mnvp.ticks.FifteenThread;

import java.io.File;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Класс реализующий точку входы функциональности сервера. Запускает узел,
 * с заданной кофигурацией.
 */
public class Node {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Тикер сети */
    private  Timer fifteenTimer;

    /**
     * Конструктор по умолчанию
     */
    public Node() {
    }

    public void start(NodeConfig config) {
        //Если не задан конфиг, то это ошибка инициализации узла
        if (config == null) {
            System.out.println("Критическая ошибка при запуске узла. Не задана основная конфигурация");
            throw new MnvpInitializeException("Критическая ошибка при запуске узла. Не задана основная конфигурация");
        }

        I = new Incapsulator();

        //Инициализация логгирвоания
        LogsController logsController = new LogsController(I);
        if (config.getLogFilePath() != null)
            logsController.setLogFile(new File(config.getLogFilePath()));
        logsController.setLogLevel(config.getLogLevel());
        I.lc = logsController;

        I.lc.log("-----------------------------------------------------------------------------------------------------\n\n", 0);

        if (config.getLabel() == null)
            config.setLabel(String.format("Node_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));

        if (config.getNetworkAddress() <= 0) {
            I.lc.log("<red>Критическая ошибка при запуске узла. Не задан адрес узла или он имеет отрицательное значение", 0);
            throw new MnvpInitializeException("Критическая ошибка при запуске узла. Не задан адрес узла или он имеет отрицательное значение");
        }

        //Создание объекта глабальных переменных
        NodeVars nodeVars = new NodeVars();
        nodeVars.networkAddress = config.getNetworkAddress();
        nodeVars.nodeLabel = config.getLabel();
        I.nv = nodeVars;

        //Запуск системы RPC
        if (config.getRpcConfig() != null) {
            RpcController rpcController = new RpcController(I);
            I.rc = rpcController;
            rpcController.run(config.getRpcConfig());
        } else {
            I.lc.log("<yellow>Предупреждение! Не знадана конигурация RPC<nc>", 2);
        }

        //Определение отладочного режима
        if (config.isDebugMode())
            I.nv.debugMode = config.isDebugMode();

        NodeDebugger nodeDebugger = new NodeDebugger(I);
        if (config.getNodeDebuggerConfig() != null)
            nodeDebugger.setConfig(config.getNodeDebuggerConfig());
        I.nd = nodeDebugger;


        PacketPool packetPool = new PacketPool();
        AcceptorsController acceptorsController = new AcceptorsController(I);
        I.ac = acceptorsController;

        GatewaysController gatewaysController = new GatewaysController(I);
        gatewaysController.setPacketPool(packetPool);
        I.gc = gatewaysController;

        //Запуск сетевого процессора
        NetworkProcessor networkProcessor = new NetworkProcessor(I);
        networkProcessor.setLabel(String.format("NetworkProcessor_%s", ThreadLocalRandom.current().nextInt(10000, 20000)));
        networkProcessor.setPacketPool(packetPool);
        I.np = networkProcessor;
        networkProcessor.run(config.getNetworkProcessorConfig());


        //Запуск акцепторов
        for (AcceptorConfig ac: config.getAcceptorConfigs()) {
            I.ac.createAcceptor(ac);
        }

        //Запуск шлюзов
        for (GatewayConfig gc: config.getGatewayConfigs()) {
            I.gc.generateGateway(gc);
        }

        //Запуск тикера сети
        FifteenThread fifteenThread = new FifteenThread(I, String.format("%s->FifteenThread_%s", config.getLabel(), ThreadLocalRandom.current().nextInt(10000, 20000)));
        fifteenTimer = new Timer(false);
        fifteenTimer.scheduleAtFixedRate(fifteenThread, 0, 15000);
    }

    /**
     * Производит остановку узла
     */
    public void stop() {
        //Остановить тикер
        fifteenTimer.cancel();

        //Остановить шлюзы
        I.gc.stopAllGateways();

        //Основить акцепторы
        I.ac.stopAllAcceptors();

        //Останновить сетевой процессор
        I.np.stop();

        I.lc.log(String.format("<lblue>[%s] Узел успешно остановлен", I.nv.nodeLabel), 3);
    }

    /**
     * Выполняет RPC вызов к удаленному узлу
     *
     * @param callDef определение вызова
     * @return результат выполннения процедуры
     */
    public RpcExecuteResult executeRpc(RpcCallDef callDef) {
        final RpcExecuteResult[] rs = {null};
        I.np.sendRpcRequest(callDef, result -> rs[0] = result);

        while (rs[0] == null) {}

        return rs[0];
    }

    /**
     * Выполняет PREQ запрос на поиск некоторого узла
     *
     * @param dest адрес искомого узла
     * @return результат выполненной транзакции
     */
    public PreqTransaction.Result executePreq(int dest) {
        final PreqTransaction.Result[] rs = {null};
        I.np.sendPreqRequest(dest, result -> rs[0] = result);

        while (rs[0] == null) {}

        return rs[0];
    }

    /**
     * Выполняет ECHO запрос на поиск некоторого узла
     *
     * @param dest адрес искомого узла
     * @return результат выполненной транзакции
     */
    public EchoTransaction.Result executeEcho(int dest) {
        final EchoTransaction.Result[] rs = {null};
        I.np.sendEchoRequest(dest, result -> rs[0] = result);

        while (rs[0] == null) {}

        return rs[0];
    }

    /**
     * Возвращает отладчик узла
     *
     * @return объект отладчика
     */
    public NodeDebugger getDebagger() {
        return I.nd;
    }
}
