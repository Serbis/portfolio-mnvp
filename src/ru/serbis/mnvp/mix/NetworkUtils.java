package ru.serbis.mnvp.mix;

import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.general.NodeVars;

/**
 * Утилитные методы для работы с сетью
 */
public interface NetworkUtils {

    /**
     * Возвращает новый идентификатор сообщения инкрементируя счетчик сообщений
     * узла контролем переполнения.
     *
     * @return идетификатор
     */
    default int getNewMsgId(Incapsulator incapsulator) {
        int nm = incapsulator.nv.msgCounter;
        incapsulator.nv.msgCounter++;

        return nm;
    }
}
