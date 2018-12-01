package ru.serbis.mnvp.acceptors.ws;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Реализация потока данных от сокета WS соединения. Данный класс является
 * оберткой реализованной над onMessage экземплятра WS сервера
 *
 */
public class WsByteInputStream {
    /** Буфер данных */
    private List<Byte> buffer;
    /** Разрешительный семафор доступа к буферу данных */
    private Semaphore bufferAccessSemaphore;

    /**
     * Стандартный конструктор
     */
    public WsByteInputStream() {
        this.buffer = new ArrayList<>();
        this.bufferAccessSemaphore = new Semaphore(1);
    }

    /**
     * Производит запись блока данных в буфер
     *
     * @param bytes блок данных
     */
    public void write(byte bytes[]) {
        try {
            bufferAccessSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        for (byte b: bytes) {
            buffer.add(b);
        }

        bufferAccessSemaphore.release();
    }

    /**
     * Производит чтение нового байта из буфера
     *
     * @param block флаг блокирующего режима работы. Если он установлен в true
     *              метод заблокирует текущий поток до того момента, пока от
     *              ws сокета не будет получен хотя бы один новый байт данных.
     * @return байт
     */
    public int read(boolean block) {
        if (buffer.size() == 0) {
            if (block) {
                while (buffer.size() == 0) {}
            } else {
                return -1;
            }
        }

        try {
            bufferAccessSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }

        int b = buffer.get(0);
        buffer.remove(0);
        bufferAccessSemaphore.release();

        return b;
    }
}
