package com.stony.mc.session;

import com.alibaba.fastjson.JSONObject;
import com.stony.mc.Utils;
import com.stony.mc.dao.WorkerDao;
import com.stony.mc.ids.IdGenerator;
import com.stony.mc.ids.SimpleIdGenerator;
import com.stony.mc.listener.BusinessHandler;
import com.stony.mc.manager.ChannelManager;
import com.stony.mc.manager.RegisterInfo;
import com.stony.mc.manager.ServerInfo;
import com.stony.mc.protocol.*;

import java.sql.SQLException;

/**
 * <p>message-channel
 * <p>com.stony.mc.session
 *
 * @author stony
 * @version 下午5:24
 * @since 2019/1/15
 */
public class MasterServer extends BaseServer<MasterServer> implements BusinessHandler {

    IdGenerator idWorker = SimpleIdGenerator.getInstance();
    WorkerDao workerDao = new WorkerDao();
    public MasterServer(String serverName, int serverPort) {
        super(serverName, serverPort);
    }
    public MasterServer(int serverPort) {
        super(serverPort);
        subscribeListener(this);
    }

    @Override
    public boolean support(ExchangeTypeEnum typeEnum) {
        return ExchangeTypeEnum.PING == typeEnum || ExchangeTypeEnum.REGISTER == typeEnum;
    }

    @Override
    public ExchangeProtocol handle(ExchangeProtocolContext request) {
        if (request.getBody() == null || request.getBody().getValue() == null) {
            return ExchangeProtocol.ack(request.getId()).status(ExchangeStatus.BAD_REQUEST);
        } else {
            if(ExchangeTypeEnum.PING == request.getType()) {
                getExecutor().submit(() -> processServerInfo(request));
            } else {
                processDeviceRegister(request);
            }
            return ExchangeProtocol.ack(request.getId());
        }
    }
    //TODO 设备注册逻辑处理
    protected void processDeviceRegister(ExchangeProtocolContext request) {
        RegisterInfo registerInfo = JSONObject.parseObject(request.getBody().getValue(), RegisterInfo.class);
        System.out.println(registerInfo);
    }
    //TODO 心跳信息维度加强， 系统信息、版本、内存占用， 细化每个连接监控信息
    protected void processServerInfo(ExchangeProtocolContext request) {
        ServerInfo serverInfo = JSONObject.parseObject(request.getBody().getValue(), ServerInfo.class);

        final ChannelManager channelManager = getChannelManager();

        if (Utils.isEmpty(serverInfo.getServerName())) {
            serverInfo.setServerName(request.getRemoteAddress().getHost());
        }

        try {
            workerDao.updateWorkerInfoALL(serverInfo);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logger.trace("Server {}, connections: {}.",serverInfo.getAddress() ,serverInfo.getConnectionCount());

        long avg = channelManager.updateServerInfo(serverInfo);
        if(avg > 0 && request.getCtx() != null) {
            logger.debug("BALANCE {} >>> {}", request.getCtx().channel(), avg);
            request.getCtx().writeAndFlush(ExchangeProtocol.create(idWorker.nextId())
                    .type(ExchangeTypeEnum.BALANCE)
                    .text(String.valueOf(avg), null, null));
        }
    }

}