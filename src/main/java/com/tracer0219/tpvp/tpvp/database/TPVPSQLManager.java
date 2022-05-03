package com.tracer0219.tpvp.tpvp.database;

import com.tracer0219.tpvp.tpvp.TPVP;
import mc.obliviate.bloksqliteapi.SQLHandler;
import mc.obliviate.bloksqliteapi.sqlutils.DataType;
import mc.obliviate.bloksqliteapi.sqlutils.SQLTable;
import org.flywaydb.core.internal.dbsupport.hsql.HsqlTable;

public class TPVPSQLManager extends SQLHandler {

    public TPVPSQLManager(TPVP instance) {
        super(instance.getDataFolder().getAbsolutePath());
        super.connect("TPVPdb");
    }

    private SQLTable table;
    private static final int DEFAULT_SIN = 0, DEFAULT_TIME = 0;


    @Override
    public void onConnect() {
        table = createTable();
        TPVP.getInstance().getLogger().info("connected to the db file");
    }

    public int getSin(String uuid) {
        if (!table.exist(uuid)) {
            insertDefault(uuid);
        }
        return table.getInteger(uuid, "sin");
    }

    public int getTime(String uuid) {
        if (!table.exist(uuid)) {
            insertDefault(uuid);
        }
        return table.getInteger(uuid, "time");
    }

    public void setSin(String uuid, int sin) {
        if (table.exist(uuid)) {
            table.update(table.createUpdate(uuid).putData("sin", sin));
        } else {
            insertDefault(uuid);
            setSin(uuid,sin);
        }
    }

    public void setTime(String uuid, int time) {
        if (table.exist(uuid)) {
            table.update(table.createUpdate(uuid).putData("time", time));
        } else {
            insertDefault(uuid);
            setTime(uuid,time);
        }
    }

    public void insertDefault(String uuid) {
        table.insert(table.createUpdate(uuid).putData("uuid", uuid).putData("sin", DEFAULT_SIN).putData("time", DEFAULT_TIME));
    }

    private SQLTable createTable() {

        final SQLTable sqlTable = new SQLTable("recm_top", "uuid")
                //                       isNotNull, isUnique, isPrimaryKey
                .addField("uuid", DataType.TEXT, true, true, true)

                //Other params is false as default
                .addField("sin", DataType.INTEGER)
                .addField("time", DataType.INTEGER);

        return sqlTable.create();
    }
}
