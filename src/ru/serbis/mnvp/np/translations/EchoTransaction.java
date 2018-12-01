package ru.serbis.mnvp.np.translations;

import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.structs.general.Packet;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Описывает транзакцию эхо запроса
 */
public class EchoTransaction extends Transaction implements Runnable {
    /**
     * Обратный вызов совершаемый при завершении транзакции. Служит для
     * уведомления вызывающей стороны о результатах выполенния транзакции.
     */
    public interface TranslationFinisher {
        void finish(Result result);
    }

    /**
     * Обратный вызов совершаемый при завершении транзакции. Служит для
     * уведомления сетевого процессора о том, что транзакцию можно удалить
     * из пула транзакций.
     */
    public interface TranslationCleaner {
        void clean(int translationId);
    }

    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Коллбэк завершения транзакции */
    private TranslationFinisher translationFinisher;
    /** Коллбэк завершения транзакции для сетевого процессора */
    private TranslationCleaner translationCleaner;
    /** Статус транзакции*/
    private State state = State.NOT_STATED;
    /** Результат выполнения транзакции */
    private Result result = null;
    /** Стартовый пакет трансляции */
    private Packet startPacket;
    /** Метка времени начала операции */
    private long opTime;
    /** Результат выполнения PREQ запроса, если он имел место быть */
    private PreqTransaction.Result preqResult = null;
    /** Счетчик ошибко NETWORK_ERROR:0*/
    private int netErrorCouter = 0;
    
    /**
     * Конструктор трансляции
     *
     * @param incapsulator икапсулятор перифирии узла
     * @param startPacket стартовый пакет, который будет направлен целевому
     *                    узлу
     * @param translationFinisher обртные вызов по завершении трасляции
     */
    public EchoTransaction(Incapsulator incapsulator, Packet startPacket, TranslationFinisher translationFinisher, TranslationCleaner cleaner) {
        this.I = incapsulator;
        this.translationFinisher = translationFinisher;
        this.translationCleaner = cleaner;
        this.startPacket = startPacket;
        super.setId(startPacket.getMsgId());
    }

    @Override
    public void run() {
        while (super.isAlive()) {
            switch (state) {
                //Транслия еще не начата
                case NOT_STATED:
                    sendStartPacket();
                    break;
                //Ожидание завершения динамической маршрутизации
                case WAIT_PREQ_FINISH:
                    checkPreqFinish();
                    break;
                //Ожидание ответного пакета
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
     * Производит запус трансляции
     */
    public void start() {
        I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Запущена ECHO трансляция к узлу %d<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Выполняет отправку стартового пакета
     */
    private void sendStartPacket() {
        //Выполнить отправку пакета
        NetworkProcessor.PacketSendResult rs = I.np.sendPacket(startPacket);
        //Если пакет был корректно отправлен
        if (rs == NetworkProcessor.PacketSendResult.OK) {
            I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Отправлен ECHO пакет к узлу %d<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
            //Устновить статус FP_SEND
            state = State.WAIT_ACKS;
            //Установить метку времени отправки пакета в сеть
            opTime = new Date().getTime();

        //При отправке пакета, не был найден маршрут по таблице маршрутизации
        } else if (rs == NetworkProcessor.PacketSendResult.ROUTE_NOT_FOUND) {
            I.lc.log(String.format("[%s->echoTranslation_%d] <blue>При отправке ECHO пакет к узлу %d не удалось найти маршрут<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
            //Если preq ранее не выполнялся, запустить динамическую маршрутизацию
            if (preqResult == null) {
                state = State.WAIT_PREQ_FINISH;
                runPreq();
            //Если же preq уже ранее был выполнен, значит ошибка поиска маршрута
            } else {
                result = Result.ROUTE_NOT_FOUND;
                state = State.FINISH;
            }
        //При попытке отправки покета произошла внутренняя ошибка
        } else if (rs == NetworkProcessor.PacketSendResult.INTERNAL_ERROR) {
            I.lc.log(String.format("[%s->echoTranslation_%d] <blue>При отправке ECHO пакет к узлу %d возникла внутренняя ошибка<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
            result = Result.INTERNAL_ERROR;
            state = State.FINISH;
        }
    }

    /**
     * Запуска динамический поиск маршрута
     */
    private void runPreq() {
        I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Запущена динамическая маршрутизация к узлу %d<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
        I.np.sendPreqRequest(startPacket.getDest(), result -> preqResult = result);
    }

    /**
     * Проверяет завершился ли динамический поиск маршрута, и в зависимости от
     * резульата либо отпавляет пакет, либо устанавливает результат трансляции
     * в ROUTE_NOT_FOUND
     */
    private void checkPreqFinish() {
        if (preqResult == null)
            return;

        switch (preqResult) {
            case FOUND:
                I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Динамичкая маршрутизация к узлу %d успешно зевершена<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
                sendStartPacket();

                break;
            case NOT_FOUND:
                I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Динамичкая маршрутизация к узлу %d завершилась неудачей<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
                result = Result.ROUTE_NOT_FOUND;
                state = State.FINISH;

                break;
            default:
                I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Динамичкая маршрутизация к узлу %d завершилась внутренней ошибкой<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
                result = Result.INTERNAL_ERROR;
                state = State.FINISH;

                break;
        }
    }

    /**
     * Обрабатывает ожидание входящего пакета
     */
    private void waitProcess() {
        if (opTime < new Date().getTime() - 5000) {
            I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Таймаут ожадиня ответного ECHO пакета от узла %d<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
            result = Result.TIMEOUT;
            state = State.FINISH;
        }
    }

    /**
     * Обрабатывает входящий пакет. После начала трансляции, любой входящий
     * пакет, с MSGID равным идентификатору трансляции, поступает в данный
     * обработчик. Последний выполняет логику смены статусов трансляции и
     * в случае необходимости отправку допольнительных пакетов.
     *
     * @param packet входящий пакет
     */
    public void receivePacket(Packet packet) {
        switch (packet.getType()) {
            case 4: //ECHO
                //Если флаг ack установлен
                if (packet.getFlags() == 0x01) {
                    I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен ответный ECHO пакет<nc>", I.nv.nodeLabel, super.getId()), 3);
                    result = Result.OK;
                    state = State.FINISH;
                } else {
                    I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен неожиданный пакет -> %s<nc>", I.nv.nodeLabel, super.getId(), packet), 3);
                }

                break;
            case 1: //NETWORK_ERROR
                ByteBuffer bf = ByteBuffer.wrap(packet.getBody());


                int errCode = bf.getInt();

                if (errCode == 0) {
                    I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен пакет с NETWORK_ERROR:0 с кодом %d<nc>", I.nv.nodeLabel, super.getId(), errCode), 3);
                    netErrorCouter++;
                    if (netErrorCouter >= 3) {
                        I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Обнаружена сетевая аномалия. При успешных процедурах динмаической маршрутизации, сеть более трех раз вернула ошибку NETWORK_ERROR:0<nc>", I.nv.nodeLabel, super.getId()), 3);
                        result = Result.UNKNOWN_NETWORK_ERROR;
                        state = State.FINISH;
                    } else {
                        preqResult = null;
                        state = State.WAIT_PREQ_FINISH;
                        runPreq();
                    }
                } else {
                    I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен пакет с NETWORK_ERROR с ножиданным кодом ожибки %d<nc>", I.nv.nodeLabel, super.getId(), errCode), 3);
                    result = Result.UNKNOWN_NETWORK_ERROR;
                    state = State.FINISH;
                }

                break;
            default:
                I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Трансляцией получен неожиданный пакет -> %s<nc>", I.nv.nodeLabel, super.getId(), packet), 3);

                break;
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
        I.lc.log(String.format("[%s->echoTranslation_%d] <blue>Звершена ECHO трансляция к узлу %d с результатом - %s<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest(), result), 3);
    }

    /** Текущий этап транзакции */
    public enum State {
        /** Транзакции не запущена */
        NOT_STATED,
        /** Ожидание ECHO_ACK*/
        WAIT_ACKS,
        /** Ожадание завершение процедуры динамического поиска маршрута */
        WAIT_PREQ_FINISH,
        /** Транзакция завершена */
        FINISH
    }

    /** Результат выполнения транзакции*/
    public enum Result {
        /** Транзакция успешно завершена*/
        OK,
        /** Не поступил ответ от целевого узла в течении заданного таймаута */
        TIMEOUT,
        /** Не был найден маршрут до целевого узла */
        ROUTE_NOT_FOUND,
        /** Внутренняя ошибка при выполнении транзакции*/
        INTERNAL_ERROR,
        /** Неизвестная сетевая ошибка */
        UNKNOWN_NETWORK_ERROR
    }

}
