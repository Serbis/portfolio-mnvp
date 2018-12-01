package ru.serbis.mnvp.gateways.ws;

import org.java_websocket.WebSocket;
import ru.serbis.mnvp.acceptors.ws.WsByteInputStream;
import ru.serbis.mnvp.gateways.GatewayConfig;

/**
 * Конфигурация щлюза WS соединения
 */
public class WsGatewayConfig extends GatewayConfig {
    /** Представление сокета */
    private WebSocket socket;
    /** Входящий поток данных */
    private WsByteInputStream inputStream;

    public WebSocket getSocket() {
        return socket;
    }

    public void setSocket(WebSocket socket) {
        this.socket = socket;
    }

    public WsByteInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(WsByteInputStream inputStream) {
        this.inputStream = inputStream;
    }
}
