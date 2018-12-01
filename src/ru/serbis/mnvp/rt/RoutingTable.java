package ru.serbis.mnvp.rt;

import ru.serbis.mnvp.general.Incapsulator;

import java.util.*;

/**
 * Инкапслирует таблицу маршрутизации и логиуку работы с ней
 */
public class RoutingTable {
    /** Инкапсулятор перифирии узла */
    private Incapsulator I;
    /** Таблица маршрутов */
    private List<RouteEntry> table = new ArrayList<>();


    public RoutingTable(Incapsulator incapsulator) {
        this.I = incapsulator;
    }

    /**
     * Обновляет маршрут в таблице. Если маршрут не был найден в таблице,
     * создает новую маршрутную запись.
     *
     * @param dest сетевой адрес узла наначения
     * @param gateway сетевой адрес шлюза
     * @param distance дистанция до целевого узла
     */
    public synchronized void updateRoute(int dest, int gateway, int distance) {
        if (gateway == 0)
            return;

        RouteEntry entry = null;
        for (RouteEntry e: table) {
            if (e.getDest() == dest && e.getGateway() == gateway && e.getDistance() == distance)
                entry = e;
        }

        if (entry != null) {
            if (entry.getDest() != dest) {
                entry.setDistance(distance);
                I.lc.log(String.format("<blue>[%s] Обновлена запись в таблице маршрутизации Dest=%d Gateway=%d Distance=%d<nc>", I.nv.nodeLabel, dest, gateway, distance), 3);
            }
        } else {
            RouteEntry ne = new RouteEntry();
            ne.setDest(dest);
            ne.setGateway(gateway);
            ne.setDistance(distance);

            table.add(ne);
            I.lc.log(String.format("<blue>[%s] Созданна запись в таблице маршрутизации Dest=%d Gateway=%d Distance=%d<nc>", I.nv.nodeLabel, dest, gateway, distance), 3);
        }

        //log(String.format("<blue>[%s] UPDATE ROUTE - dest = %d, gateway = %d, distance = %d<nc>", nodeLabel, dest, gateway, distance), 10, nodeLabel);
    }

    /**
     * Удаляет все маршруты из таблицы, шлюзом в которых является адрес
     * указанный в параметре.
     *
     * @param gateway сетевой адрес искомого для удаления шлюза
     */
    public synchronized void removeAllRoutesByGateway(int gateway) {
        Iterator<RouteEntry> iterator = table.iterator();

        while (iterator.hasNext()) {
            RouteEntry e = iterator.next();
            if (e.getGateway() == gateway) {
                iterator.remove();
                I.lc.log(String.format("<blue>[%s] Удалена запись в таблице маршрутизации Dest=%d Gateway=%d Distance=%d<nc>", I.nv.nodeLabel, e.getDest(), e.getGateway(), e.getDistance()), 3);
            }
        }
    }

    /**
     * Удаляет все маршруты из таблицы, цельую в которых является адрес
     * указанный в параметре.
     *
     * @param dest сетевой адрес искомой для удаления цели
     */
    public synchronized void removeAllByDest(int dest) {
        Iterator<RouteEntry> iterator = table.iterator();

        while (iterator.hasNext()) {
            RouteEntry e = iterator.next();
            if (e.getDest() == dest) {
                iterator.remove();
                I.lc.log(String.format("<blue>[%s] Удалена запись в таблице маршрутизации Dest=%d Gateway=%d Distance=%d<nc>", I.nv.nodeLabel, e.getDest(), e.getGateway(), e.getDistance()), 3);
            }
        }
    }

    /**
     * Ищет шлюз до целвого узла и возвращает его номер, или -1 в том случае
     * если последний не был найден.
     *
     * @param dest искомый сетевой вдрес цели
     * @return сетевой адрес шлюза или -1 если маршрут не был найден
     */
    public synchronized int findRoute(int dest) {
        Iterator<RouteEntry> iterator = table.iterator();

        int min = 1000000;
        RouteEntry re = null;

        while (iterator.hasNext()) {
            RouteEntry entry = iterator.next();
            if (entry.getDest() == dest && entry.getDistance() < min) {
                re = entry;
                min = entry.getDistance();
            }
        }

        if (re != null)
            return re.getGateway();

        return -1;
    }
}
