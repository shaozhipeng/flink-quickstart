package me.icocoro.hbase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * HBaseClientTest
 * By shaozhipeng
 */
public class HBaseClientTest {

    static Configuration conf = null;
    static Connection conn = null;

    static {
        try {
            conf = HBaseConfiguration.create();
            // default port 2181
            conf.set("hbase.zookeeper.property.clientPort", "2181");
            // set multi zk eg: zoo1,zoo2,zoo2
            conf.set("hbase.zookeeper.quorum", "localhost");
            // set by hbase server config
            conf.set("zookeeper.znode.parent", "/hbase");
            conn = ConnectionFactory.createConnection(conf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // create hbase table with name webtable, and three column family: contents anchor people
        createTable("webtable", new String[]{"contents", "anchor", "people"});
        // insert into data
        putData("https://icocoro.me", "webtable", Bytes.toBytes("contents"), Bytes.toBytes("html"),
                Bytes.toBytes("webtable-https://icocoro.me-contents:html=<html><head></head>...</html>"));
        // list data from table with specific rowkey
        listCells("webtable", "https://icocoro.me");
    }

    /**
     * create table
     *
     * @param tableName
     * @param family
     * @throws Exception
     */
    public static void createTable(String tableName, String[] family)
            throws Exception {
        if (StringUtils.isEmpty(tableName)) {
            return;
        }

        if (ArrayUtils.isEmpty(family)) {
            return;
        }

        HBaseAdmin admin = (HBaseAdmin) conn.getAdmin();

        // HTableDescriptor and HColumnDescriptor
        HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(tableName));
        for (int i = 0; i < family.length; i++) {
            desc.addFamily(new HColumnDescriptor(family[i]));
        }
        if (admin.tableExists(tableName)) {
            System.out.println("table Exists!");
        } else {
            admin.createTable(desc);
            System.out.println("create table Success!");
        }
        admin.close();
    }

    /**
     * insert or update data
     *
     * @param rowKey
     * @param tableName
     * @param family
     * @param qualifier
     * @param value
     * @throws IOException
     */
    public static void putData(String rowKey, String tableName, byte[] family, byte[] qualifier, byte[] value) throws IOException {
        if (StringUtils.isEmpty(rowKey) || StringUtils.isEmpty(tableName)) {
            return;
        }

        if (ArrayUtils.isEmpty(family) || ArrayUtils.isEmpty(qualifier)) {
            return;
        }

        Put put = new Put(Bytes.toBytes(rowKey));
        HTable table = (HTable) conn.getTable(TableName.valueOf(tableName));
        // byte[] family, byte[] qualifier, byte[] value
        put.addColumn(family, qualifier, value);
        table.put(put);
        System.out.println("add data Success!");
        table.close();
    }

    /**
     * fetch data with tableName and rowKey
     *
     * @param tableName
     * @param rowKey
     * @return Result
     * @throws IOException
     */
    public static Result listCells(String tableName, String rowKey) throws IOException {
        Get get = new Get(Bytes.toBytes(rowKey));
        HTable table = (HTable) conn.getTable(TableName.valueOf(tableName));
        Result result = table.get(get);

        System.out.println("result.current(): " + result.current());

        CellScanner cellScanner = result.cellScanner();

        System.out.println("cellScanner: " + cellScanner.toString());
        System.out.println("cellScanner.current(): " + cellScanner.current());

        System.out.println("---------------------------------------------");

        for (Cell cell : result.listCells()) {
            System.out.println("cell: " + cell.toString());
            System.out.println("family: " + Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength()));
            System.out.println("qualifier: " + Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()));
            System.out.println("value: " + Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
            System.out.println("timestamp: " + cell.getTimestamp());
            System.out.println("---------------------------------------------");
        }

        return result;
    }
}
