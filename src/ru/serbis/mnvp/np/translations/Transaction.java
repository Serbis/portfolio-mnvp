package ru.serbis.mnvp.np.translations;

import ru.serbis.mnvp.structs.general.Packet;

/**
 * Описывает базовый объект транзакции
 */
public class Transaction {
    /** Идентификатор транзакции */
    private int id;
    /** Флаг жизни потока транзакции */
    private boolean alive = true;


    public void receivePacket(Packet packet) {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
