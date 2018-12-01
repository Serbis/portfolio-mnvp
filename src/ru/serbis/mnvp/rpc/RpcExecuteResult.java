package ru.serbis.mnvp.rpc;

/**
 * Описывает результат выполнения процедуры на стороне источика. Иными словами
 * этот объект возвращается в качестве результата выполнения RPC трансляции.
 */
public class RpcExecuteResult {
    private boolean error;
    private ErrorType errorType;
    private byte[] resultBody;

    public RpcExecuteResult() {
    }

    public RpcExecuteResult(boolean error, ErrorType errorType, byte[] resultBody) {
        this.error = error;
        this.errorType = errorType;
        this.resultBody = resultBody;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public byte[] getResultBody() {
        return resultBody;
    }

    public void setResultBody(byte[] resultBody) {
        this.resultBody = resultBody;
    }

    public enum ErrorType {
        /** В процессе отпавки пакета не был найден маршрут до цели */
        ROUTE_NOT_FOUND,
        /** При отиправке пакета, возникла внутренняя программная ошибка */
        INTERNAL_ERROR,
        /** Таймаут ожадиния ответного пакета. Означает что целевой узел в
         * течении заданного таймаута не прислал вообще никаких ответных
         * пакетов. Это свидетельствует о том, что узел по каким-то неизвестным
         * причинам принимял, но не смог отработать вызов (не смогу даже
         * вернуть ошибку)
         * */
        ACK_TIMEOUT,
        /** Таймаут ожидания результата выполнения вызова. Означает что целевой
         * узел прислал подтверждающий пакет, но в течении указанного временного
         * интервала из данного пакета, не прислал результат выполнения вызова.
         * Это свидетельствует о том, что либо целевой узел не успел выполнить
         * процедуру или же что в обработчике процедуры возникла некая
         * критическая ошибка, которая привела к краху обрабаотывающего потока
         * процедуры, что привело в программному сбою на стороне исполнителя */
        RECD_TIMEOUT
    }
}
