package ru.serbis.mnvp.acceptors.ws;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import ru.serbis.mnvp.gateways.ws.WsGateway;
import ru.serbis.mnvp.general.Incapsulator;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Реализация логики сервера WS. Данный объект используется для запуска и
 * обработки событий сервера. Создается акцептором WS соединений. В задачу
 * данного класса входят:
 *      -Уведомление акцептора о создании нового соединения
 *      -Передача блока полученных от клиента данных шлюз за которм закреплена
 *       сесси из которой пришли данные
 */
public class WsServerInstance extends WebSocketServer  {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Ссылка на акцептор который породил данный экземпляр*/
    private WsAcceptor acceptor;


    public WsServerInstance(Incapsulator incapsulator, int port, WsAcceptor acceptor) throws UnknownHostException {
        super(new InetSocketAddress( port ));
        this.acceptor = acceptor;
        this.I = incapsulator;
    }

    /**
     * Обрабатывает получение нового входящего соединения. Вызывает метод
     * акцепции нового сокета в привзанном к экземпляру сервера акцепторе
     *
     * @param webSocket сокет
     * @param clientHandshake хрен знает что это такое
     */
    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        acceptor.acceptNewConnection(webSocket);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {

    }

    /**
     * Обрабатывает получение ового блока данных. Записывает даный блок в
     * поток данных соединения.
     *
     * @param conn соединение
     * @param message блок данных
     */
    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        super.onMessage(conn, message);

        WsGateway gateway = I.gc.getWsGatewayBySocket(conn);
        if (gateway == null) {
            I.lc.log(String.format("<yellow>[%s->%s] При полчениии блока данный из ws соединения, не был обнаружен отвечеющий за него шлюз. Невозможно передать данные в шлюз на обработку<nc>",I.nv.nodeLabel, acceptor.getLabel()), 2);
            return;
        }

        WsByteInputStream is = gateway.getInputStream();
        if (is == null) {
            I.lc.log(String.format("<yellow>[%s->%s] При полчениии блока данный из ws соединения, поток данных шлюза = null. Невозможно передать данные в шлюз на обработку<nc>",I.nv.nodeLabel, acceptor.getLabel()), 2);
            return;
        }

        is.write(message.array());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {

    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    @Override
    public void onStart() {

    }
}
