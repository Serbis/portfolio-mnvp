package ru.serbis.mnvp.acceptors.ws;

import ru.serbis.mnvp.acceptors.AcceptorConfig;

/**
 * Конфигурация WS акцептора
 */
public class WsAcceptorConfig extends AcceptorConfig {
    /** Порт на котором будет создан WS сервер */
    private int port = -1;

    /**
     * Конструктор
     *
     * @param port порт на котором будет создан ws сервер
     * @param label метка акцептора
     */
    public WsAcceptorConfig(int port,  String label) {
        this.port = port;
        super.setLabel(label);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
