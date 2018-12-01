package ru.serbis.mnvp.acceptors;

import ru.serbis.mnvp.acceptors.tcp.TcpAcceptor;
import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.acceptors.ws.WsAcceptor;
import ru.serbis.mnvp.acceptors.ws.WsAcceptorConfig;
import ru.serbis.mnvp.acceptors.ws.WsServerInstance;
import ru.serbis.mnvp.general.Incapsulator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Контроллер акцепторов. Задачей данного класса, явялется создание,
 * содержание и управление всему существующими у узла акцепторами. Любой
 * акцетор, порождается, управляется и уничтожается тольк в этом классе.
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class AcceptorsController {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Пул акцепторов */
    private Map<String, Acceptor> acceptorsPool = new HashMap<>();

    /**
     * Конструктор
     *
     * @param incapsulator инкапсулятор перифирии узла
     */
    public AcceptorsController(Incapsulator incapsulator) {
        this.I = incapsulator;
    }


    /**
     * Создает новый акцтопр из входящей конфигураци. Размещает его в пуле
     * и запускает.
     *
     * @param config конфигурация создаваемого акцептора
     */
    public void createAcceptor(AcceptorConfig config) {
        if (config instanceof TcpAcceptorConfig) {
            TcpAcceptorConfig tcpAcceptorConfig = (TcpAcceptorConfig) config;

            if (tcpAcceptorConfig.getLabel() == null)
                tcpAcceptorConfig.setLabel(String.format("TpcAcceptor_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));


            TcpAcceptor tcpAcceptor = new TcpAcceptor(I, tcpAcceptorConfig);
            acceptorsPool.put(tcpAcceptorConfig.getLabel(),tcpAcceptor); //Внести акцептор в пул акцепторов
            I.lc.log(String.format("<blue>[%s] Создан новый акцетор TCP с меткой %s<nc>", I.nv.nodeLabel, config.getLabel()), 3);
            tcpAcceptor.run();
        } else  if (config instanceof WsAcceptorConfig) {
            WsAcceptorConfig wsAcceptorConfig = (WsAcceptorConfig) config;

            if (wsAcceptorConfig.getLabel() == null)
                wsAcceptorConfig.setLabel(String.format("WsAcceptor_%d", ThreadLocalRandom.current().nextInt(10000, 20000)));

            WsAcceptor wsAcceptor = new WsAcceptor(I, wsAcceptorConfig);
            acceptorsPool.put(wsAcceptorConfig.getLabel(), wsAcceptor);
            I.lc.log(String.format("<blue>[%s] Создан новый акцетор WS с меткой %s<nc>", I.nv.nodeLabel, config.getLabel()), 3);
            wsAcceptor.run();
        }
    }

    /**
     * Останавливает все работающие акцепторы
     */
    public void stopAllAcceptors() {
        acceptorsPool.entrySet().forEach(stringAcceptorEntry -> stringAcceptorEntry.getValue().stop());
    }
}
