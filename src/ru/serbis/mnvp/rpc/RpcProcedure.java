package ru.serbis.mnvp.rpc;

import ru.serbis.mnvp.rpc.lexer.Lexer;
import ru.serbis.mnvp.rpc.lexer.Tag;
import ru.serbis.mnvp.rpc.lexer.Token;
import ru.serbis.mnvp.rpc.signatures.RpcArg;
import ru.serbis.mnvp.rpc.signatures.RpcSignature;
import ru.serbis.mnvp.rpc.signatures.RpcType;

import java.util.ArrayList;
import java.util.List;

/**
 * Определяет RPC вызов. Задачи данного объекта:
 *      -Проверять, имеет ли ходящий RPC отношение к данному типу вызова, а
 *      именно. Определять название процедуры, сверять количество аргеметов
 *      и проводить проверку их типов.
 *      -Производить является ли вызов проверяемым
 *      -Порождать поток обработчик процедуры для ее выполнения
 *
 * Данный объект задействуется сразу после получения валидного RPC пакета
 * сетевым ресивером, после чего он порождает поток обработчик процедуры и
 * освобождает ресивер.
 */
public class RpcProcedure {
    /** Семантическая формула RCP процедуры вида funcName(t,t,t), где t моожет
     * принимать значения :
     *      i - целое число (4 байта)
     *      l - целое число (8 байт)
     *      f - дробное число (одинарной точности)
     *      d - дробное число (двойной точности)
     *      s - литерал
     * Данная формула используется при поиске и проверке вызовов входящего RPC
     * вызова
     **/
    private RpcSignature signature;
    /** Реализация обратного вызова, который выступает в качестве внешнего
     * обработчика rpc вызова */
    private RpcCallback rpcCallback;
    /** Флаг подтверждаемого RPС вызова. Если данный флаг установленн, то при
     * получении RPC запроса, узел до передачи управляения в процедуру выполнит
     * отправку RPC_ACCEPT пакета, означающего что запрос был принят и следует
     * ожидать подтверждения в течении заданного таймаута (указан в теле
     * сообщения) */
    private boolean confirmed;

    /**
     * Устанавливает семантику сигнатуры выхова. А именно - прозводит
     * лексико-семантический анализ определения сигнатуы процедуры и создает
     * объект описание семантики RpcSignature.
     *
     * @param semantic сигнатура процедуры
     * @return false если при обработке сигнатуры возникли ошибки иначе true
     */
    public boolean setSemantic(String semantic) {
        //Получить список токенов
        Lexer lexer = new Lexer(semantic);
        List<Token> tokens = lexer.getList();

        RpcSignature signature = new RpcSignature();

        //Проверяем что имя процедуры это идентификатор
        if (tokens.get(0).tag != Tag.ID)
            return false;

        signature.setProcName(tokens.get(0).lexeme);

        //Определяем что за именем процедуры идет открывающая скобка
        if (tokens.get(1).tag != Tag.LBRACKET)
            return false;

        int ptr = 1;

        if (tokens.get(2).tag != Tag.RBRACKET) {
            ptr = 2;
            //Парсим аргументы
            while (true) {
                Token t = tokens.get(ptr);

                switch (t.lexeme) {
                    case "I":
                        signature.getArgsList().add(new RpcArg(RpcType.INT));
                        break;
                    case "L":
                        signature.getArgsList().add(new RpcArg(RpcType.LONG));
                        break;
                    case "F":
                        signature.getArgsList().add(new RpcArg(RpcType.FLOAT));
                        break;
                    case "D":
                        signature.getArgsList().add(new RpcArg(RpcType.DOUBLE));
                        break;
                    case "B":
                        signature.getArgsList().add(new RpcArg(RpcType.BOOLEAN));
                        break;
                    case "S":
                        signature.getArgsList().add(new RpcArg(RpcType.STRING));
                        break;
                    case "V":
                        signature.getArgsList().add(new RpcArg(RpcType.STRING));
                        break;
                    default:
                        return false;
                }

                if (tokens.get(ptr + 1).tag != Tag.COMMA && tokens.get(ptr + 1).tag != Tag.RBRACKET)
                    return false;

                if (tokens.get(ptr + 1).tag == Tag.RBRACKET)
                    break;

                if (ptr + 2 > tokens.size() - 1)
                    break;

                ptr += 2;
            }
        }

        ptr += 2;

        //Проверяем что после закрывающей скобки идет двоеточие
        if (!tokens.get(ptr).lexeme.equals(":"))
            return false;

        ptr++;

        switch (tokens.get(ptr).lexeme) {
            case "I":
                signature.setReturnType(RpcType.INT);
                break;
            case "L":
                signature.setReturnType(RpcType.LONG);
                break;
            case "F":
                signature.setReturnType(RpcType.FLOAT);
                break;
            case "D":
                signature.setReturnType(RpcType.DOUBLE);
                break;
            case "B":
                signature.setReturnType(RpcType.BOOLEAN);
                break;
            case "S":
                signature.setReturnType(RpcType.STRING);
                break;
            case "V":
                signature.setReturnType(RpcType.VOID);
                break;
            default:
                return false;
        }


        this.signature = signature;

        return true;

    }

    /**
     * Анализирует входящую троку вызова на соответсвение сигнатуре вызова.
     * Если сигнатура полностью соотвествует вызову, возвращается OK. Елси
     * имя процедуры не соответствуе имени из сигнатуры, возвращается
     * INCORRECT_PROC_NAME. Если у вызова недостаточное количество аргементов, возвращается
     * INCORRECT_ARG_COUNT. Если при анализе агумента из вызова, его тип
     * расходится с типом из внутренний сигнатуры, возвращается
     * INCORRECT_ARG_TYPE. Если при обработке вызова происходит ошибка
     * парсинга, возращается PARSE_ERROR. В процессе анализа, просходит
     * заполнение внутренни полей сигнатуры вызова, соотвественно если данный
     * метод возвращает OK, он будет иметь полностью заполненный сигнатурны
     * объект.
     *
     * @param call строка RPC вызова
     * @return результат анализа строки вызова
     */
    public SignatureCheckResult analizeSignature(String call) {
        //Получить список токенов
        Lexer lexer = new Lexer(call);
        List<Token> tokens = lexer.getList();

        //Сравниваем имя процедуры
        if (!tokens.get(0).lexeme.equals(signature.getProcName()))
            return SignatureCheckResult.INCORRECT_PROC_NAME;

        //Определяем что за именем процедуры идет открывающая скобка
        if (tokens.get(1).tag != Tag.LBRACKET)
            return SignatureCheckResult.PARSE_ERROR;

        //Определяем что процедура заканивается закрывающей скобкой
        if (tokens.get(tokens.size() - 1).tag != Tag.RBRACKET)
            return SignatureCheckResult.PARSE_ERROR;

        List<RpcArg> args = new ArrayList<>();
        int ptr = 1;

        if (tokens.get(2).tag != Tag.RBRACKET) {
            ptr = 2;
            //Парсим аргументы
            while (true) {
                Token t = tokens.get(ptr);
                RpcType type = matchType(t.tag);
                if (type == RpcType.NAT)
                    return SignatureCheckResult.PARSE_ERROR;
                args.add(new RpcArg(type, t.lexeme));

                if (tokens.get(ptr + 1).tag != Tag.COMMA && tokens.get(ptr + 1).tag != Tag.RBRACKET)
                    return SignatureCheckResult.PARSE_ERROR;

                if (ptr + 2 > tokens.size() - 1)
                    break;

                ptr += 2;
            }
        }

        //Если количество полученных аргументов не соответвует их количеству в сигнатуре
        if (signature.getArgsList().size() != args.size()) {
            return SignatureCheckResult.INCORRECT_ARG_COUNT;
        } else {
            //Проверяем и нжектируем аргументы в сигнатуру если они соотносятся по типу
            for (int i = 0; i < args.size(); i++) {
                RpcArg argFromCall = args.get(i);
                RpcArg argFromSig =  signature.getArgsList().get(i);

                if (argFromCall.getRpcType() != argFromSig.getRpcType()) {
                    return SignatureCheckResult.INCORRECT_ARG_TYPE;
                } else {
                    signature.getArgsList().set(i, argFromCall);
                }
            }
        }

        return SignatureCheckResult.OK;
    }


    /**
     * Определяет тип данный по значения тега лексемы. Если тип не обнурежн
     * возвращает NAT
     *
     * @param tag тэг лексемы
     * @return тип данных
     */
    private RpcType matchType(Tag tag) {
        switch (tag) {
            case INT:
                return RpcType.INT;
            case LONG:
                return RpcType.LONG;
            case FLOAT:
                return RpcType.FLOAT;
            case DOUBLE:
                return RpcType.DOUBLE;
            case BOOL:
                return RpcType.BOOLEAN;
            case STRING:
                return RpcType.STRING;
            default:
                return RpcType.NAT;
        }
    }



    public RpcCallback getRpcCallback() {
        return rpcCallback;
    }

    public void setRpcCallback(RpcCallback rpcCallback) {
        this.rpcCallback = rpcCallback;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

}
