package ru.serbis.mnvp.general;

/**
 * Класс окружения узла. В нем глобальные переменные узла.
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class NodeVars {
    /** Сетевой адрес узла */
    public int networkAddress;
    /** Счетчик номеров сообщений */
    public int msgCounter;
    /** Флаг отладочного режима узла */
    public boolean debugMode;
    /** Метка узла */
    public String nodeLabel;
}
