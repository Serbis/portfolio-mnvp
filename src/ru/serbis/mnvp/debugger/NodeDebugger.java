package ru.serbis.mnvp.debugger;

import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.np.translations.EchoTransaction;
import ru.serbis.mnvp.np.translations.PreqTransaction;
import ru.serbis.mnvp.rt.RoutingTable;
import ru.serbis.mnvp.structs.general.Packet;

/**
 * Контроллер отладчика узла
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class NodeDebugger {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Перехватчик пакетов */
    private IncomingPacketInterceptor incomingPacketInterceptor;

    /**
     * Конструктор
     *
     * @param incapsulator инкапсулятор перифирии узла
     */
    public NodeDebugger(Incapsulator incapsulator) {
        this.I = incapsulator;
    }

    public void setConfig(NodeDebuggerConfig config) {
        incomingPacketInterceptor = config.getIncomingPacketInterceptor();
    }

    public boolean intercrptIncomingPacket(Packet packet) {
        return incomingPacketInterceptor == null || incomingPacketInterceptor.intercept(packet);
    }

    /**
     * Дубликатор метода sendPreqRequest из сетевого процессора. Позволяет
     * врнучную инициировать произвольный preq запрос
     *
     * @param dest сетевой адрес искомого узла
     * @param finisherCallback обртный вызов, который будет совершен после
     *                         заврешния трансляции
     */
    public void sendPreqRequest(int dest, PreqTransaction.TranslationFinisher finisherCallback) {
        I.np.sendPreqRequest(dest, finisherCallback);
    }

    /**
     * Дубликатор метода sendEchoRequest из сетевого процессора. Позволяет
     * врнучную инициировать ehco запрос
     *
     * @param dest сетевой адрес искомого узла
     * @param finisherCallback обртный вызов, который будет совершен после
     *                         заврешния трансляции
     */
    public void sendEchoRequest(int dest, EchoTransaction.TranslationFinisher finisherCallback) {
        I.np.sendEchoRequest(dest, finisherCallback);
    }

    /**
     * Возвращает таблицу маршрутизации узла
     *
     * @return таблица маршрутизации
     */
    public RoutingTable getRoutingTable() {
        return I.np.getRoutingTableForDebug();
    }
}
