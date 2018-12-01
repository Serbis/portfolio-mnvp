package ru.serbis.mnvp.np;

/**
 * Конфигурация сетевого процессора
 */
public class NetworkProcessorConfig {
    /** Количество потоков ресивера пакетов */
    private int packetReceiverThreadCount = 1;

    public int getPacketReceiverThreadCount() {
        return packetReceiverThreadCount;
    }

    public void setPacketReceiverThreadCount(int packetReceiverThreadCount) {
        this.packetReceiverThreadCount = packetReceiverThreadCount;
    }
}
