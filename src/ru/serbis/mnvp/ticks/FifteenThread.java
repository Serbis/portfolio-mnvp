package ru.serbis.mnvp.ticks;

import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.mix.NetworkUtils;
import ru.serbis.mnvp.mix.PacketUtils;
import ru.serbis.mnvp.structs.general.Packet;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

/**
 * Поток реализуеющий пятнадцатисекундное тактирование сети. В его задачу
 * входит:
 *
 * --1. Проверка животси шлюзов с последующими в случае обнаружение мертвого
 *      шлюза:
 *      --1.1 Удаление записи в таблице маршрутизации об узле которому
 *            принадлежал шлюз
 *      --1.2 Удаление шлюза
 * --2. Отсылка hello пакетов оставшимся в живых шлюзам
 */
public class FifteenThread extends TimerTask implements NetworkUtils, PacketUtils {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Текстовая метка*/
    private String label;

    public FifteenThread(Incapsulator incapsulator, String label) {
        this.I = incapsulator;
        this.label = label;
    }

    @Override
    public void run() {
        I.lc.log(String.format("<blue>[%s] Запуск процедуры тикера сети", label), 3);
        sendHello();
        changeActivity();
    }

    /**
     * Реализует отправку hello сообщений по всейм живым шлюзам
     */
    private void sendHello() {
        Iterator<Map.Entry<String, Gateway>> iterator = I.gc.getGatewaysPoolIterator();

        while (iterator.hasNext()) {
            Gateway gateway = iterator.next().getValue();
            I.lc.log(String.format("<blue>[%s] Отправка hello пакета шлюз %s с адресом %d<nc>", label, gateway.getLabel(), gateway.getNetworkAddress()), 3);
            Packet helloPacket = createHelloPacket(getNewMsgId(I), I.nv.networkAddress, gateway.getNetworkAddress());
            try {
                gateway.getSendSemaphore().acquire();
                gateway.send(helloPacket);

            } catch (InterruptedException e) {
                gateway.getSendSemaphore().release();
                e.printStackTrace();
            }
            gateway.getSendSemaphore().release();
        }
    }

    /**
     * Проверяет метку последней активности шлюза и если шлюз не активен
     * более x секунд, удаляет шлюз и очищает таблицу маршрутизации от
     * записей по нему.
     */
    private synchronized void changeActivity() {
        Iterator<Map.Entry<String, Gateway>> iterator = I.gc.getGatewaysPoolIterator();

        while (iterator.hasNext()) {
            Gateway gateway = iterator.next().getValue();
            if (gateway.getLastIncomingActivity() < new Date().getTime() - 30000) {
                I.lc.log(String.format("<blue>[%s] Шлюз %s более не является активным, инициация процедуры удаления шлюза<nc>", label, gateway.getLabel()), 3);
                I.gc.removeGateway(gateway.getLabel());
            }
        }
    }
}
