package ru.serbis.mnvp.np;

import ru.serbis.mnvp.structs.general.Packet;

import java.util.Stack;
import java.util.concurrent.Semaphore;

/**
 * Пул пакетов узла. Данный объект обеспечивает инапсуляцию передачи пакета
 * между компонентами узла обеспечивая потокбезовасную запись и изъятие
 * элементов.
 */
public class PacketPool {
    /** Буфер пакетов */
    private Stack<Packet> pool;
    /** Семафор регулирующий доступ к буферу пакетов */
    private Semaphore accessSemaphore;

    /**
     * Конструктор
     */
    public PacketPool() {
        pool = new Stack<>();
        accessSemaphore = new Semaphore(1);

    }

    /**
     * Размещает пакет на вершину пула
     *
     * @param packet пакет для размещения
     */
    public synchronized void put(Packet packet) {
        try {
            accessSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pool.push(packet);
        accessSemaphore.release();
    }

    /**
     * Возвращает первый пакет из пула
     *
     * @return пакет
     */
    public synchronized Packet get() {
        try {
            accessSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (pool.size() == 0) {
            accessSemaphore.release();

            return null;
        }


        Packet packet = pool.pop();

        accessSemaphore.release();


        return packet;
    }
}
