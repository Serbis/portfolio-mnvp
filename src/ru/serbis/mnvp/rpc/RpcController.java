package ru.serbis.mnvp.rpc;

import ru.serbis.mnvp.general.Incapsulator;
import ru.serbis.mnvp.mix.PacketUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ????????????????????????????????????????????????????????????????
 *
 */
public class RpcController implements PacketUtils {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Список зергистрированных RPC процедур */
    private List<RpcProcedure> rpcProcedures = new ArrayList<>();

    /**
     * Конструктор
     *
     * @param incapsulator инкапсулятор перифирии узла
     */
    public RpcController(Incapsulator incapsulator) {
        this.I = incapsulator;
    }

    /**
     * Устанавливает (читай инициализирует) конфигурацию контроллера
     *
     * @param config конфигурация системы RPC
     */
    public void run(RpcConfig config) {
        for (RpcCallConfig rcc: config.getRpcCallConfigList()) {
            RpcProcedure rpcProcedure = new RpcProcedure();
            if (rcc.getRpcCallback() == null) {
                I.lc.log(String.format("<yellow>[%s->RpcController] Ошибка при конфигурировании RPC процедуры %s, не задан обратный вызов<nc>", I.nv.nodeLabel, rcc.getSemantic()), 2);
                continue;
            }
            rpcProcedure.setRpcCallback(rcc.getRpcCallback());
            boolean semiticAnalyzeResult = rpcProcedure.setSemantic(rcc.getSemantic());
            if (!semiticAnalyzeResult) {
                I.lc.log(String.format("<yellow>[%s->RpcController] Ошибка при конфигурировании RPC процедуры %s. Ошибка обработки семантики сигнатуры<nc>", I.nv.nodeLabel, rcc.getSemantic()), 2);
                continue;
            }
            rpcProcedure.setConfirmed(rcc.isConfirmed());

            rpcProcedures.add(rpcProcedure);
        }

        I.lc.log(String.format("<blue>[%s->RpcController] Создан новый RPC контроллер<nc>", I.nv.nodeLabel), 3);
    }

    /**
     * Выполняет обратку входящего rpc вызова. Данный метод вызывается из
     * ресивера. В его задачу входит поиск rpc процедурры по заданной сигнатуре.
     * Если процедура не найдена, возвращает NOT_FOUND, на что ресивер возвращает
     * отправителю ошибку. Если найдена, порождает поток обработчик, и отдает
     * управление ресиверу вернув OK. Последний в свою очередь должен
     * завршить обработку пакета и перейти в режим ожиданния. Если сигнатура
     * найдена, но у нее не соотвествует количество аргументом, то возвращается
     * INCORRECT_ARG_COUNT. Если аргумент имеют некорректный типа, возвращается
     * INCORRECT_ARG_TYPE.
     *
     * @param source сетевой адрес источника вызова
     * @param translationId идентификатор порождающей трансляции на стороне
     *                      источника
     * @param call строка вызова
     * @return результат анализа сигнатуры
     *
     */
    public SignatureCheckResult processIncomingCall(int source, int translationId, String call) {
        SignatureCheckResult result = SignatureCheckResult.NOT_FOUND;
        RpcProcedure rpcProcedure = null;

        for (RpcProcedure rc: rpcProcedures) {
            SignatureCheckResult rs = rc.analizeSignature(call);

            if (rs != SignatureCheckResult.INCORRECT_PROC_NAME) {
                result = rs;
                if (rs == SignatureCheckResult.OK)
                    rpcProcedure = rc;

                break;
            }
        }

        if (rpcProcedure == null)
            return result;

        RpcCallExecutor executor = new RpcCallExecutor();
        Thread thread = new Thread(executor);
        thread.start();

        return result;
    }


}