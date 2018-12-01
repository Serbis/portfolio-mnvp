package ru.serbis.mnvp.general;

import ru.serbis.mnvp.acceptors.AcceptorsController;
import ru.serbis.mnvp.debugger.NodeDebugger;
import ru.serbis.mnvp.gateways.GatewaysController;
import ru.serbis.mnvp.np.NetworkProcessor;
import ru.serbis.mnvp.rpc.RpcController;

/**
 * Инкапсулирет служебную переферию узла (контроллеры, процессора и т. п.) что
 * бы ее не было видно со стороны прикладного кода
 */
public class Incapsulator {
    /** Контроллер системы логгирования */
    public LogsController lc;
    /** Глобальнные переменные узла */
    public NodeVars nv;
    /** Контроллер системы RPC */
    public RpcController rc;
    /** Отладчик узла */
    public NodeDebugger nd;
    /** Контроллер акцепторов */
    public AcceptorsController ac;
    /** Контроллер шлюзов */
    public GatewaysController gc;
    /** Сетевой процессор */
    public NetworkProcessor np;
}
