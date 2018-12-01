package ru.serbis.mnvp.np.translations;

import ru.serbis.mnvp.gateways.Gateway;
import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.structs.general.Packet;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Трансляция PREQ запроса. Данная трансляция инкапсулирует механику выполнения
 * PREQ запроса.
 */
public class PreqTransaction extends Transaction implements Runnable {
    /**
     * Обратный вызов совершаемый при завершении трансляции. Служит для
     * уведомления вызывающей стороны о результатах выполенния трансляции.
     */
    public interface TranslationFinisher {
        void finish(Result result);
    }

    /**
     * Обратный вызов совершаемый при завершении трансляции. Служит для
     * уведомления сетевого процессора о том, что трансляцию можно удалить
     * из пула трансляций.
     */
    public interface TranslationCleaner {
        void clean(int translationId);
    }

    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Коллбэк завершения трансляции для инициатора */
    private TranslationFinisher translationFinisher;
    /** Коллбэк завершения трансляции для сетевого процессора */
    private TranslationCleaner translationCleaner;
    /** Статус трансляции */
    private State state = State.NOT_STATED;
    /** Результат выполнения трансляции */
    private Result result = Result.NO_RESULT;
    /** Стартовый пакет трансляции */
    private Packet startPacket;
    /** Метка времени начала операции */
    private long opTime;
    /** Граница количества полкченных PREQ_ACK, после которых ожидание новых ответов
     *  будет прекращено */
    private int maxPreqAck = 100;
    /** Время ожиданния PREQ_ACK*/
    private long preqAckTime = 5000;
    /** Количество принятых PREQ_ACK */
    private int preqAckReceive = 0;
    
    /**
     * Конструктор трансляции
     *
     * @param incapsulator нкапсулятор перифирии узла
     * @param startPacket стартовый пакет preq запроса
     * @param translationFinisher обртные вызов по завершении трасляции
     */
    public PreqTransaction(Incapsulator incapsulator, Packet startPacket, TranslationFinisher translationFinisher, TranslationCleaner translationCleaner) {
        this.I = incapsulator;
        this.translationFinisher = translationFinisher;
        this.startPacket = startPacket;
        this.translationCleaner = translationCleaner;
    }

    @Override
    public void run() {
        while (super.isAlive()) {
            switch (state) {
                case NOT_STATED:
                    sendPreqPacks();

                    break;
                case WAIT_ACKS:
                    waitProcess();

                    break;
                case FINISH:
                    finish();

                    break;
            }
        }
    }

    /**
     * Производит запуск трансляции
     */
    public void start() {
        I.lc.log(String.format("[%s] <blue>Запущена PREQ трансляция к узлу %d<nc>", I.nv.nodeLabel, startPacket.getDest()), 3);
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Выпроняет отправку стартого пакета во все имеющиеся у укзла шлюзы
     */
    private void sendPreqPacks() {
        I.lc.log(String.format("[%s] <blue>Запуск процедуры динамического поиска маршрута к узлу %d<nc>", I.nv.nodeLabel, startPacket.getDest()), 3);

        result = Result.NOT_FOUND;
        Iterator<Map.Entry<String, Gateway>> iterator = I.gc.getGatewaysPoolIterator();
        boolean gwExist = false;

        while(iterator.hasNext()) {
            gwExist = true;
            Gateway gateway = iterator.next().getValue();
            try {
                gateway.getSendSemaphore().acquire();
                gateway.send(startPacket);
                gateway.getSendSemaphore().release();
            }  catch (InterruptedException e) {
                e.printStackTrace();
            }
            I.lc.log(String.format("[%s] <blue>Отправлен PREQ пакет к узлу %d через шлюз %s с сетевым адресом %d<nc>", I.nv.nodeLabel, startPacket.getDest(), gateway.getLabel(), gateway.getNetworkAddress()), 3);
        }

        if (!gwExist) {
            I.lc.log(String.format("[%s] <blue>Не найдено активнымх шлюзов для отправки PREQ пакета к узлу %d<nc>", I.nv.nodeLabel, startPacket.getDest()), 3);
            state = State.FINISH;
        } else {
            opTime = new Date().getTime();
            state = State.WAIT_ACKS;
        }
    }

    /**
     * Обрабатывает режим ожания ответных PREQ пакетов. Выходит из него по
     * достижении таймаута или при получении достаточного количества ответных
     * пакетов.
     *
     */
    private void waitProcess() {
        if (preqAckReceive >= maxPreqAck) {
            I.lc.log(String.format("[%s] <blue>Завершена процедура PREQ запроса к узлу %d по ватрелинии найденных маршрутов. Найденно маршрутов -  %d<nc>", I.nv.nodeLabel, startPacket.getDest(), preqAckReceive), 3);
            state = State.FINISH;

            return;
        }

        if (opTime < new Date().getTime() - preqAckTime) {
            I.lc.log(String.format("[%s] <blue>Завершена процедура PREQ запроса к узлу %d по таймауту ожидания ответов. Найденно маршрутов -  %d<nc>", I.nv.nodeLabel, startPacket.getDest(), preqAckReceive), 3);
            state = State.FINISH;
        }
    }

    /**
     * Обрабатывает завершене трансляции. Совершает обратный вызов финишера,
     * после чего завершает поток трансляции.
     */
    private void finish() {
        if (translationFinisher != null) {
            translationFinisher.finish(result);
        }

        if (translationCleaner != null) {
            translationCleaner.clean(super.getId());
        }

        super.setAlive(false);
        I.lc.log(String.format("[%s] <blue>Звершена PREQ трансляция %d к узлу %d с результатом - %s<nc>", I.nv.nodeLabel, startPacket.getDest(), super.getId(), result), 3);
    }

    /**
     * Обрабатывает входящий ответ. Инерементирует счетчик PREQ_ACK и
     * устаналивает результат трансляции в FOUND
     *
     * Внимание: процедуру врнесения найденного маршрута, выполняет ресивер
     * сетевого процессора. Так же PREQ_ACK из нулевого шлюза не считается валидным и пропускается
     *
     * @param packet полученный пакет
     */
    @Override
    public void receivePacket(Packet packet) {
        int adr = this.I.gc.getGatewayNetworkAddress(packet.getGatewayLabel());
        if (adr != -1) {
            if (adr == 0)
                return;
            //А если был получен NETWORK_ERROR пакет????
            preqAckReceive++;
            result = Result.FOUND;

            I.lc.log(String.format("[%s] <blue>PREQ трансляцией %d принят отвеный PREQ пакет по запросу к узлу %d через  шлюз c сетевым адресом %d<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest(), adr), 3);
        } else {
            I.lc.log(String.format("[%s] <blue>Ошибка приема пакета PREQ трансляцией %d, не найден сзязанный с пакетом шлюз<nc>", I.nv.nodeLabel, super.getId()), 3);
        }
    }

    /** Текущий этап трансляции */
    public enum State {
        /** Транзакция не запущена */
        NOT_STATED,
        /** PREQ пакеты были разосланы по все активны шлюзам узла. Трансляция
         * находится в режме ожидания PREQ_ACK */
        WAIT_ACKS,
        /** Транзакция завршена */
        FINISH
    }

    /** Результат выполнения трансяции*/
    public enum Result {
        /** Нет результата (трансляция находится в процессе выполнения */
        NO_RESULT,
        /** По результату PREQ запроса были найдены валидные маршруты */
        FOUND,
        /** По результату PREQ запроса валидные маршруты не были обнаружены */
        NOT_FOUND
    }
}
