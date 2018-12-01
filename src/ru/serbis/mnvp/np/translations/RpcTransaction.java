package ru.serbis.mnvp.np.translations;

import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.rpc.RpcExecuteResult;
import ru.serbis.mnvp.structs.general.Packet;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Описывает трансляцию RPC запроса
 */
public class RpcTransaction extends Transaction implements Runnable {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    
    /**
     * Обратный вызов совершаемый при завершении трансляции. Служит для
     * уведомления вызывающей стороны о результатах выполенния трансляции.
     */
    public interface TranslationFinisher {
        void finish(RpcExecuteResult result);
    }

    /**
     * Обратный вызов совершаемый при завершении трансляции. Служит для
     * уведомления сетевого процессора о том, что трансляцию можно удалить
     * из пула трансляций.
     */
    public interface TranslationCleaner {
        void clean(int translationId);
    }


    /** Коллбэк завершения трансляции */
    private TranslationFinisher translationFinisher;
    /** Коллбэк завершения трансляции для сетевого процессора */
    private TranslationCleaner translationCleaner;
    /** Статус трансляции*/
    private State state = State.NOT_STATED;
    /** Результат выполнения трансляции */
    private RpcExecuteResult result = null;
    /** Стартовый пакет трансляции */
    private Packet startPacket;
    /** Метка времени начала операции */
    private long opTime;
    /** Результат выполнения PREQ запроса, если он имел место быть */
    private PreqTransaction.Result preqResult = null;
    /** Счетчик ошибкок NETWORK_ERROR:0*/
    private int netErrorCouter = 0;
    /** Таймаует ожидание первого ответного пакета */
    private int fpTimeout = 5000;
    
    /**
     * Конструктор
     *
     * @param incapsulator инкапсулятор перифирии узла
     */
    public RpcTransaction(Incapsulator incapsulator) {
        this.I = incapsulator;
    }
    
    /**
     * Конструктор трансляции
     *
     * @param startPacket стартовый пакет, который будет направлен целевому
     *                    узлу
     * @param nodeLabel метка узла
     * @param timeout таймаут ответного запроса от целевого узла. Данное
     *                значение распространяется только на неподтверждаемые
     *                вызовы. Это таймаут ожидания первого ответного пакета
     *                независимо от типа. Последний может быть как результатом
     *                так и подтверждение вызова.

     * @param translationFinisher обртные вызов по завершении трасляции
     */
    public RpcTransaction(Incapsulator incapsulator, Packet startPacket, int timeout, TranslationFinisher translationFinisher, TranslationCleaner cleaner) {
        this.I = incapsulator;
        this.translationFinisher = translationFinisher;
        this.translationCleaner = cleaner;
        this.startPacket = startPacket;
        this.fpTimeout = timeout;
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
                //Ожидание ответного пакета. Это может быть либо ACK, либо RECD пакеты
                case WAIT_ACKS:
                    waitProcess();
                    break;
                //Ожидание ACK пакета после ранее полученного RECD пакета
                case WAIT_RPC_FINISH:
 //                   waitRecd();
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
        I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Запущена RCP трансляция к узлу %d<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
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
            I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Отправлен RPC пакет к узлу %d<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
            //Устновить статус WAIT_ACKS
            state = State.WAIT_ACKS;
            //Установить метку времени отправки пакета в сеть
            opTime = new Date().getTime();

        //При отправке пакета, не был найден маршрут по таблице маршрутизации
        } else if (rs == NetworkProcessor.PacketSendResult.ROUTE_NOT_FOUND) {
            I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>При отправке RPC пакета к узлу %d не удалось найти маршрут<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
            //Если preq ранее не выполнялся, запустить динамическую маршрутизацию
            if (preqResult == null) {
                state = State.WAIT_PREQ_FINISH;
                runPreq();
            //Если же preq уже ранее был выполнен, значит ошибка поиска маршрута
            } else {
                result = new RpcExecuteResult(true, RpcExecuteResult.ErrorType.ROUTE_NOT_FOUND, null);
                state = State.FINISH;
            }
        //При попытке отправки покета произошла внутренняя ошибка
        } else if (rs == NetworkProcessor.PacketSendResult.INTERNAL_ERROR) {
            I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>При отправке RPC пакет к узлу %d возникла внутренняя ошибка<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
            result = new RpcExecuteResult(true, RpcExecuteResult.ErrorType.INTERNAL_ERROR, null);
            state = State.FINISH;
        }
    }

    /**
     * Запуска динамический поиск маршрута
     */
    private void runPreq() {
        I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Запущена динамическая маршрутизация к узлу %d<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
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
                I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Динамичкая маршрутизация к узлу %d успешно зевершена<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
                sendStartPacket();

                break;
            case NOT_FOUND:
                I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Динамичкая маршрутизация к узлу %d завершилась неудачей<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
                //result = Result.ROUTE_NOT_FOUND;
                state = State.FINISH;

                break;
            default:
                I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Динамичкая маршрутизация к узлу %d завершилась внутренней ошибкой<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
               // result = Result.INTERNAL_ERROR;
                state = State.FINISH;

                break;
        }
    }

    /**
     * Обрабатывает ожидание входящего пакета
     */
    private void waitProcess() {
        if (opTime < new Date().getTime() - fpTimeout) {
            I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Таймаут ожадиня RPC_ACK/RPC_RECD пакета от узла %d<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest()), 3);
            result = new RpcExecuteResult(true, RpcExecuteResult.ErrorType.ACK_TIMEOUT, null);
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
            case 5: //RPC
                //Если флаг ack установлен
                if ((packet.getFlags() & 1) == 0) { //Установлен флаг ACK (пакет является ответным)
                    if (((packet.getFlags() >> 1) & 1) == 0) { //Установлен флаг  RECD/RESULT = 0 RECD (пакет является подтверждением получения вызова)
                        I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Трансляцией получен RPC_RECD пакет<nc>", I.nv.nodeLabel, super.getId()), 3);
                        state = State.WAIT_RPC_FINISH;
                    } else { //Установлен флаг  RECD/RESULT = 1 RESULT (пакет является результатом выполнения вызова)
                        I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Трансляцией получен RPC_RESULT пакет<nc>", I.nv.nodeLabel, super.getId()), 3);
                        result = new RpcExecuteResult(false, null, packet.getBody());
                        state = State.FINISH;
                    }

                } else {
                    I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Трансляцией получен неожиданный пакет -> %s<nc>", I.nv.nodeLabel, super.getId(), packet), 3);
                }

                break;
            case 1: //NETWORK_ERROR
                ByteBuffer bf = ByteBuffer.wrap(packet.getBody());


                int errCode = bf.getInt();

                if (errCode == 0) {
                    I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Трансляцией получен пакет с NETWORK_ERROR:0 %d<nc>", I.nv.nodeLabel, super.getId(), errCode), 3);
                    //netErrorCouтter++;
                    /*if (netErrorCouтter >= 3) {
                        log(String.format("[%s->rpcTranslation_%d] <blue>Обнаружена сетевая аномалия. При успешных процедурах динмаической маршрутизации, сеть более трех раз вернула ошибку NETWORK_ERROR:0<nc>", super.getId()), 3);
                        result = Result.UNKNOWN_NETWORK_ERROR;
                        state = State.FINISH;
                    } else {
                        preqResult = null;
                        state = State.WAIT_PREQ_FINISH;
                        runPreq();
                    }*/
                } else {
                    I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Трансляцией получен пакет с NETWORK_ERROR с ножиданным кодом ожибки %d<nc>", I.nv.nodeLabel, super.getId(), errCode), 3);
                    //result = Result.UNKNOWN_NETWORK_ERROR;
                    state = State.FINISH;
                }

                break;
            default:
                I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Трансляцией получен неожиданный пакет -> %s<nc>", I.nv.nodeLabel, super.getId(), packet), 3);

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
        I.lc.log(String.format("[%s->rpcTranslation_%d] <blue>Звершена ECHO трансляция к узлу %d с результатом - %s<nc>", I.nv.nodeLabel, super.getId(), startPacket.getDest(), result), 3);
    }




    /** Текущий этап трансляции */
    public enum State {
        NOT_STATED, WAIT_ACKS, WAIT_RPC_FINISH, WAIT_PREQ_FINISH, FINISH
    }

    public enum Result {
        OK, TIMEOUT, ROUTE_NOT_FOUND, INTERNAL_ERROR, UNKNOWN_NETWORK_ERROR
    }

}
