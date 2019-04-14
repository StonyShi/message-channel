package com.stony.mc.dao;

import com.stony.mc.manager.ServerInfo;
import com.stony.mc.session.HostPort;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>message-channel
 * <p>com.stony.mc.dao
 *
 * @author stony
 * @version 上午9:35
 * @since 2019/4/12
 */
public class WorkerDao {
    private WorkerDatabaseFactory factory;
    public WorkerDao(){
        factory = new WorkerDatabaseFactory();
    }
    public List<ServerInfo> getWorkerInfoList(int pageNo, int pageSize) throws SQLException {
        String sql = "select MACHINE_ID,SERVER_NAME,SERVER_PORT," +
                "CONNECTION_COUNT,CONNECTION_ERROR_COUNT,MAX_CONNECTION_COUNT," +
                "REQUEST_TOTAL_COUNT,REQUEST_TOTAL_BYTES,RESPONSE_TOTAL_COUNT," +
                "RESPONSE_TOTAL_SUCCEED_COUNT,RESPONSE_TOTAL_FAILED_COUNT," +
                "RESPONSE_TOTAL_TIME_MS,RESPONSE_TOTAL_BYTES,SUBSCRIBER_COUNT," +
                "SUBSCRIBER_MESSAGE_COUNT,CREATE_TIME,UPDATE_TIME  " +
                "from WORKER_INFO order by UPDATE_TIME " +
                "limit ?,?";
        ConnectionHolder connectionHolder = factory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = connectionHolder.getConnection();
        List<ServerInfo> list = new ArrayList<>(32);
        try {
            //(pageNumber-1)*pageSize,pageSize
            ps = conn.prepareStatement(sql);
            int offset = (pageNo - 1) * pageSize;
            ps.setInt(1, offset);
            ps.setInt(2, pageSize);
            rs = ps.executeQuery();
            ServerInfo info;
            while (rs.next()) {
                info = new ServerInfo();
                info.setMachineId(rs.getString("MACHINE_ID"));
                info.setServerName(rs.getString("SERVER_NAME"));
                info.setServerPort(rs.getInt("SERVER_PORT"));
                info.setConnectionCount(rs.getLong("CONNECTION_COUNT"));
                info.setConnectionErrorCount(rs.getLong("CONNECTION_ERROR_COUNT"));
                info.setMaxConnectionCount(rs.getLong("MAX_CONNECTION_COUNT"));
                info.setRequestTotalCount(rs.getLong("REQUEST_TOTAL_COUNT"));
                info.setRequestTotalBytes(rs.getLong("REQUEST_TOTAL_BYTES"));
                info.setResponseTotalCount(rs.getLong("RESPONSE_TOTAL_COUNT"));
                info.setResponseTotalSucceedCount(rs.getLong("RESPONSE_TOTAL_SUCCEED_COUNT"));
                info.setResponseTotalFailedCount(rs.getLong("RESPONSE_TOTAL_FAILED_COUNT"));
                info.setResponseTotalTimeMs(rs.getLong("RESPONSE_TOTAL_TIME_MS"));
                info.setResponseTotalBytes(rs.getLong("RESPONSE_TOTAL_BYTES"));
                info.setSubscriberCount(rs.getLong("SUBSCRIBER_COUNT"));
                info.setSubscriberMessageCount(rs.getLong("SUBSCRIBER_MESSAGE_COUNT"));
                info.setCreatedTime(rs.getLong("CREATE_TIME"));
                info.setUpdateTime(rs.getLong("UPDATE_TIME"));
                list.add(info);
            }
        } finally {
            close(rs);
            close(ps);
            factory.releaseConnection();
        }
        return list;
    }
    public List<ServerInfo> getWorkerInfoHistoryList(long beginTime, long endTime) throws SQLException {
        return getWorkerInfoHistoryList(beginTime, endTime, null, -1);
    }
    public List<ServerInfo> getWorkerInfoHistoryList(long beginTime, long endTime, String serverName, int serverPort) throws SQLException {
        String where = " ";
        boolean enable = false;
        if(serverName != null && serverPort > 0) {
            enable = true;
            where = " SERVER_NAME = ? and SERVER_PORT = ? and ";
        }
        String sql = "select MACHINE_ID,SERVER_NAME,SERVER_PORT," +
                "CONNECTION_COUNT,CONNECTION_ERROR_COUNT,MAX_CONNECTION_COUNT," +
                "REQUEST_TOTAL_COUNT,REQUEST_TOTAL_BYTES,RESPONSE_TOTAL_COUNT," +
                "RESPONSE_TOTAL_SUCCEED_COUNT,RESPONSE_TOTAL_FAILED_COUNT," +
                "RESPONSE_TOTAL_TIME_MS,RESPONSE_TOTAL_BYTES,SUBSCRIBER_COUNT," +
                "SUBSCRIBER_MESSAGE_COUNT,CREATE_TIME  " +
                "from WORKER_INFO_HISTORY " +
                "where " + where + " CREATE_TIME >= ? and CREATE_TIME < ? " +
                "order by CREATE_TIME ";
        ConnectionHolder connectionHolder = factory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = connectionHolder.getConnection();
        List<ServerInfo> list = new ArrayList<>(32);
        try {
            ps = conn.prepareStatement(sql);
            if (enable) {
                ps.setString(1, serverName);
                ps.setInt(2, serverPort);
                ps.setLong(3, beginTime);
                ps.setLong(4, endTime);
            } else {
                ps.setLong(1, beginTime);
                ps.setLong(2, endTime);
            }
            rs = ps.executeQuery();
            ServerInfo info;
            while (rs.next()) {
                info = new ServerInfo();
                info.setMachineId(rs.getString("MACHINE_ID"));
                info.setServerName(rs.getString("SERVER_NAME"));
                info.setServerPort(rs.getInt("SERVER_PORT"));
                info.setConnectionCount(rs.getLong("CONNECTION_COUNT"));
                info.setConnectionErrorCount(rs.getLong("CONNECTION_ERROR_COUNT"));
                info.setMaxConnectionCount(rs.getLong("MAX_CONNECTION_COUNT"));
                info.setRequestTotalCount(rs.getLong("REQUEST_TOTAL_COUNT"));
                info.setRequestTotalBytes(rs.getLong("REQUEST_TOTAL_BYTES"));
                info.setResponseTotalCount(rs.getLong("RESPONSE_TOTAL_COUNT"));
                info.setResponseTotalSucceedCount(rs.getLong("RESPONSE_TOTAL_SUCCEED_COUNT"));
                info.setResponseTotalFailedCount(rs.getLong("RESPONSE_TOTAL_FAILED_COUNT"));
                info.setResponseTotalTimeMs(rs.getLong("RESPONSE_TOTAL_TIME_MS"));
                info.setResponseTotalBytes(rs.getLong("RESPONSE_TOTAL_BYTES"));
                info.setSubscriberCount(rs.getLong("SUBSCRIBER_COUNT"));
                info.setSubscriberMessageCount(rs.getLong("SUBSCRIBER_MESSAGE_COUNT"));
                info.setCreatedTime(rs.getLong("CREATE_TIME"));
                list.add(info);
            }
        } finally {
            close(rs);
            close(ps);
            factory.releaseConnection();
        }
        return list;
    }

    public boolean updateWorkerInfoALL(ServerInfo info) throws SQLException {
        ConnectionHolder connectionHolder = factory.getConnection();
        connectionHolder.increaseRef();
        boolean result = false;
        try {
            boolean r1 = updateWorkerInfo(info);
            boolean r2 = updateWorkerInfoHistory(info);
            result = r1 && r2;
        } finally {
            factory.releaseConnection();
        }
        return result;
    }
    public boolean updateWorkerInfo(ServerInfo info) throws SQLException {
        ConnectionHolder connectionHolder = factory.getConnection();
        PreparedStatement ps = null;
        String sql = "insert into WORKER_INFO(MACHINE_ID,SERVER_NAME,SERVER_PORT," +
                "CONNECTION_COUNT,CONNECTION_ERROR_COUNT,MAX_CONNECTION_COUNT," +
                "REQUEST_TOTAL_COUNT,REQUEST_TOTAL_BYTES,RESPONSE_TOTAL_COUNT," +
                "RESPONSE_TOTAL_SUCCEED_COUNT,RESPONSE_TOTAL_FAILED_COUNT," +
                "RESPONSE_TOTAL_TIME_MS,RESPONSE_TOTAL_BYTES,SUBSCRIBER_COUNT," +
                "SUBSCRIBER_MESSAGE_COUNT,CREATE_TIME,UPDATE_TIME) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        int index = 1;
        Connection conn = connectionHolder.getConnection();
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(index++, info.getMachineId());
            ps.setString(index++, info.getServerName());
            ps.setInt(index++, info.getServerPort());
            ps.setLong(index++, info.getConnectionCount());
            ps.setLong(index++, info.getConnectionErrorCount());
            ps.setLong(index++, info.getMaxConnectionCount());
            ps.setLong(index++, info.getRequestTotalCount());
            ps.setLong(index++, info.getRequestTotalBytes());
            ps.setLong(index++, info.getResponseTotalCount());
            ps.setLong(index++, info.getResponseTotalSucceedCount());
            ps.setLong(index++, info.getResponseTotalFailedCount());
            ps.setLong(index++, info.getResponseTotalTimeMs());
            ps.setLong(index++, info.getResponseTotalBytes());
            ps.setLong(index++, info.getSubscriberCount());
            ps.setLong(index++, info.getSubscriberMessageCount());
            ps.setLong(index++, System.currentTimeMillis());
            ps.setLong(index++, System.currentTimeMillis());
            return ps.executeUpdate() == 1;
        }catch (SQLException e) {
            if(e.getMessage().contains("Unique")) {
                sql = "update WORKER_INFO set MACHINE_ID = ?, CONNECTION_COUNT = ?, CONNECTION_ERROR_COUNT = ?, " +
                        "MAX_CONNECTION_COUNT = ?, REQUEST_TOTAL_COUNT = ?, REQUEST_TOTAL_BYTES = ?, " +
                        "RESPONSE_TOTAL_COUNT = ?, RESPONSE_TOTAL_SUCCEED_COUNT = ?, " +
                        "RESPONSE_TOTAL_FAILED_COUNT = ?, RESPONSE_TOTAL_TIME_MS = ?, " +
                        "RESPONSE_TOTAL_BYTES = ?, SUBSCRIBER_COUNT = ?, " +
                        "SUBSCRIBER_MESSAGE_COUNT = ?, UPDATE_TIME = ?  where SERVER_NAME = ? and SERVER_PORT = ?";
                ps = conn.prepareStatement(sql);
                index = 1;
                ps.setString(index++, info.getMachineId());
                ps.setLong(index++, info.getConnectionCount());
                ps.setLong(index++, info.getConnectionErrorCount());
                ps.setLong(index++, info.getMaxConnectionCount());
                ps.setLong(index++, info.getRequestTotalCount());
                ps.setLong(index++, info.getRequestTotalBytes());
                ps.setLong(index++, info.getResponseTotalCount());
                ps.setLong(index++, info.getResponseTotalSucceedCount());
                ps.setLong(index++, info.getResponseTotalFailedCount());
                ps.setLong(index++, info.getResponseTotalTimeMs());
                ps.setLong(index++, info.getResponseTotalBytes());
                ps.setLong(index++, info.getSubscriberCount());
                ps.setLong(index++, info.getSubscriberMessageCount());
                ps.setLong(index++, System.currentTimeMillis());
                ps.setString(index++, info.getServerName());
                ps.setInt(index++, info.getServerPort());
                return ps.executeUpdate() == 1;
            }
            throw e;
        }finally {
            close(ps);
            factory.releaseConnection();
        }
    }
    public boolean updateWorkerInfoHistory(ServerInfo info) throws SQLException {
        ConnectionHolder connectionHolder = factory.getConnection();
        PreparedStatement ps = null;
        String sql = "insert into WORKER_INFO_HISTORY (MACHINE_ID,SERVER_NAME,SERVER_PORT," +
                "CONNECTION_COUNT,CONNECTION_ERROR_COUNT,MAX_CONNECTION_COUNT," +
                "REQUEST_TOTAL_COUNT,REQUEST_TOTAL_BYTES,RESPONSE_TOTAL_COUNT," +
                "RESPONSE_TOTAL_SUCCEED_COUNT,RESPONSE_TOTAL_FAILED_COUNT," +
                "RESPONSE_TOTAL_TIME_MS,RESPONSE_TOTAL_BYTES,SUBSCRIBER_COUNT," +
                "SUBSCRIBER_MESSAGE_COUNT,CREATE_TIME) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        int index = 1;
        Connection conn = connectionHolder.getConnection();
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(index++, info.getMachineId());
            ps.setString(index++, info.getServerName());
            ps.setInt(index++, info.getServerPort());
            ps.setLong(index++, info.getConnectionCount());
            ps.setLong(index++, info.getConnectionErrorCount());
            ps.setLong(index++, info.getMaxConnectionCount());
            ps.setLong(index++, info.getRequestTotalCount());
            ps.setLong(index++, info.getRequestTotalBytes());
            ps.setLong(index++, info.getResponseTotalCount());
            ps.setLong(index++, info.getResponseTotalSucceedCount());
            ps.setLong(index++, info.getResponseTotalFailedCount());
            ps.setLong(index++, info.getResponseTotalTimeMs());
            ps.setLong(index++, info.getResponseTotalBytes());
            ps.setLong(index++, info.getSubscriberCount());
            ps.setLong(index++, info.getSubscriberMessageCount());
            ps.setLong(index++, System.currentTimeMillis());
            return ps.executeUpdate() == 1;
        }finally {
            close(ps);
            factory.releaseConnection();
        }
    }
    public boolean registerWorker(HostPort hostPort) throws SQLException {
        ConnectionHolder connectionHolder = factory.getConnection();
        PreparedStatement ps = null;
        String sql = "insert into WORKER_REGISTER(host, port, create_time, update_time) values(?,?,?,?)";
        Connection conn = connectionHolder.getConnection();
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, hostPort.getHost());
            ps.setInt(2, hostPort.getPort());
            ps.setLong(3, System.currentTimeMillis());
            ps.setLong(4, System.currentTimeMillis());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            if(e.getMessage().contains("Unique")) {
                sql = "UPDATE WORKER_REGISTER SET update_time = ? WHERE host = ? and port = ?";
                ps = conn.prepareStatement(sql);
                ps.setLong(1, System.currentTimeMillis());
                ps.setString(2, hostPort.getHost());
                ps.setInt(3, hostPort.getPort());
                ps = conn.prepareStatement(sql);
                return ps.executeUpdate() == 1;
                //throw new DuplicateKeyException(e);
            }
            throw  e;
        } finally {
            close(ps);
            factory.releaseConnection();
        }
    }
//    private static final String URL = "jdbc:h2:~/h2/mc;FILE_LOCK=SOCKET;MODE=MySQL;ACCESS_MODE_DATA=rws";
    private static final String URL = "jdbc:h2:file:~/h2/mc;FILE_LOCK=NO;MODE=MySQL;ACCESS_MODE_DATA=rws";
    class WorkerDatabaseFactory {
        ThreadLocal<ConnectionHolder> Cache = new ThreadLocal<>();
        public synchronized ConnectionHolder getConnection() throws SQLException {
            ConnectionHolder connectionHolder = Cache.get();
            if(connectionHolder == null) {
                Connection conn = DriverManager.getConnection(URL, "sa", "");
                connectionHolder = new ConnectionHolder(conn);
                Cache.set(connectionHolder);
            }
            return connectionHolder;
        }
        public synchronized void releaseConnection() throws SQLException {
            ConnectionHolder conn = Cache.get();
            if(conn != null) {
                if(conn.close()) {
                    Cache.remove();
                }
            }
        }
    }
    class ConnectionHolder{
        Connection conn;
        int refCount;
        public ConnectionHolder(Connection conn) {
            this.conn = conn;
        }
        public Connection getConnection() {
            increaseRef();
            return conn;
        }
        public void increaseRef() {
            refCount++;
        }
        public void decreaseRef() {
            refCount--;
        }
        public boolean close() {
            decreaseRef();
            if(refCount <= 0) {
                WorkerDao.close(conn);
                return true;
            }
            return false;
        }
    }

    private static void close(Connection conn) {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
//                e.printStackTrace();
                //pass
            }
        }
    }

    static void close(ResultSet rs) {
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
//                e.printStackTrace();
                //pass
            }
        }
    }
    static void close(PreparedStatement ps) {
        if(ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
//                e.printStackTrace();
                //pass
            }
        }
    }
}
