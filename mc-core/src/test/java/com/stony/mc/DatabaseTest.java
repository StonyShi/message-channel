package com.stony.mc;

import com.stony.mc.dao.WorkerDao;
import com.stony.mc.manager.ServerInfo;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DatabaseTest {

    public static final String Url = "jdbc:h2:~/h2/mc;FILE_LOCK=NO;MODE=MySQL;ACCESS_MODE_DATA=rws;LOCK_MODE=3";
    @Test
    public void test_01() throws SQLException {
        Connection conn = DriverManager.getConnection(Url, "sa", "");
        System.out.println("---------------");
        System.out.println(conn);
        String sql = "insert into WORKER_REGISTER(host, port, create_time, update_time) values(?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "127.0.0.1");
        ps.setInt(2, 8215);
        ps.setLong(3, System.currentTimeMillis());
        ps.setLong(4, System.currentTimeMillis());

        System.out.println(ps.executeUpdate());
        conn.close();
    }

    @Test
    public void test_02() throws SQLException {
        WorkerDao dao = new WorkerDao();

        ServerInfo info = new ServerInfo();
        info.setServerName("localhost");
        info.setServerPort(9011);
        info.setMachineId("234-dfdfdxcvxv");
        info.setConnectionCount(1);
        info.setMaxConnectionCount(1);
        info.setRequestTotalCount(1);
        info.setRequestTotalBytes(36);
        System.out.println("update " + dao.updateWorkerInfo(info));

        System.out.println("------------------");
        System.out.println("update " + dao.updateWorkerInfo(info));
    }

    @Test
    public void test_03() throws SQLException {

        WorkerDao dao = new WorkerDao();
        ServerInfo info = new ServerInfo();
        info.setServerName("localhost");
        info.setServerPort(9012);
        info.setMachineId("23492b34340234a");
        info.setConnectionCount(1);
        info.setMaxConnectionCount(1);
        info.setRequestTotalCount(1);
        info.setRequestTotalBytes(36);
        info.setResponseTotalSucceedCount(1);
        info.setResponseTotalCount(1);
        info.setResponseTotalTimeMs(30);
        System.out.println("update " + dao.updateWorkerInfoALL(info));

        System.out.println("------------------");

    }

    @Test
    public void test_04() throws SQLException {
        WorkerDao dao = new WorkerDao();

        List list = dao.getWorkerInfoList(1, 30);

        System.out.println(list);
        System.out.println("-------------------------");


        list = dao.getWorkerInfoHistoryList(System.currentTimeMillis()-86400000, System.currentTimeMillis()+1000);
        System.out.println(list);
        System.out.println("________________________");
        list = dao.getWorkerInfoHistoryList(System.currentTimeMillis()-86400000, System.currentTimeMillis()+1000, "localhost", 9013);
        System.out.println(list);
        System.out.println("________________________");

    }
}
