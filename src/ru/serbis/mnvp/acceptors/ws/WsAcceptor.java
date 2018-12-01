package ru.serbis.mnvp.acceptors.ws;

import org.java_websocket.WebSocket;
import ru.serbis.mnvp.acceptors.Acceptor;
import ru.serbis.mnvp.acceptors.AcceptorConfig;
import ru.serbis.mnvp.acceptors.tcp.TcpAcceptorConfig;
import ru.serbis.mnvp.gateways.tcp.TcpGatewayConfig;
import ru.serbis.mnvp.gateways.ws.WsGatewayConfig;
import ru.serbis.mnvp.general.Incapsulator;

import java.io.IOException;

/**
 * Реализация акцептора TCP соединенй. Данный акцетор создает WS сервер на
 * определенномм порту. Сервер представлен имплементаций WsServerInstance,
 * которая имеет обратную ссылку на данный класс. Как только WS сервер
 * акцептирует новое соединение, он обращается к методу acceptNewConnection
 * для обработки нового соединения.
 */
public class WsAcceptor extends Acceptor {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Объект экземплята ws сервера */
    private WsServerInstance serverInstance;

    /**
     * Конструктор с параметром конфигурации акцептора
     *
     * @param config конфигурация акцептора
     */
    public WsAcceptor(Incapsulator incapsulator, AcceptorConfig config) {
        super(config);
        this.I = incapsulator;
    }

    /**
     * Выполяет запуск акцептора. Создает новый экземляр WS сервера и
     * производит его запуск.
     */
    @Override
    public void run() {
        if (super.getConfig() == null) {
            I.lc.log(String.format("<yellow>[%s] Ошибка запуска ацептора. Не задана конфигурация", I.nv.nodeLabel), 2);

            return;
        }

        WsAcceptorConfig config = (WsAcceptorConfig) super.getConfig();
        super.setLabel(config.getLabel());

        try {
            serverInstance = new WsServerInstance(I, config.getPort(), this);
        } catch (Exception e) {
            I.lc.log(String.format("<yellow>[%s->%s] Ошибка при создание ацептора WS соединений. Невозможно создать сервер", I.nv.nodeLabel, super.getConfig().getLabel()), 2);
            e.printStackTrace();
        }

        serverInstance.start();
    }


    /**
     * Выполняет остановку акцептора. Останавливает WS сервер.
     */
    @Override
    public void stop() {
        try {
            serverInstance.stop();
        } catch (IOException | InterruptedException e) {
            I.lc.log(String.format("<yellow>[%s->%s] Ошибка при остановке ацептора WS соединений. Невозможно остановить сервер", I.nv.nodeLabel, super.getConfig().getLabel()), 2);
            e.printStackTrace();
        }
    }

    /**
     * Обрабатывает прием нового соединения от экземпляра ws сервера.
     * Непосредственно занимается порождение нового шлюза из входящего
     * соединения.
     *
     * @param socket представление сокета
     */
    public void acceptNewConnection(WebSocket socket) {
        WsGatewayConfig wsGatewayConfig = new WsGatewayConfig();
        wsGatewayConfig.setSocket(socket);
        wsGatewayConfig.setInputStream(new WsByteInputStream());
        I.gc.wrapGateway(wsGatewayConfig);
    }
}
