package com.stony.mc.manager;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import io.netty.channel.ChannelHandlerContext;

import java.util.Objects;

/**
 * <p>message-channel
 * <p>com.stony.mc.listener
 *
 * @author stony
 * @version 上午11:16
 * @since 2019/4/14
 */
public class ChatSession {
    @JSONField(name = "chat_id")
    private String chatId;

    private transient ChannelContextHolder leader;
    private transient ChannelContextHolder follower;

    private transient volatile boolean createLeader;

    @JSONField(name = "leader_worker")
    private String leaderWorker;
    @JSONField(name = "follower_worker")
    private String followerWorker;

    @JSONField(name = "created_time")
    private long createdTime;

    @JSONField(name = "update_time")
    private long updateTime;

    @JSONCreator
    public ChatSession(@JSONField(name = "chat_id") String chatId) {
        this.chatId = chatId;
    }
    public ChatSession() {
        this.createdTime = System.currentTimeMillis();
    }

    public String getLeaderAddress() {
        return leader != null ? leader.getAddress() : null;
    }
    public String getFollowerAddress() {
        return follower != null ? follower.getAddress() : null;
    }
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ChannelContextHolder getLeader() {
        return leader;
    }

    public void setLeader(ChannelContextHolder leader) {
        this.leader = leader;
    }

    public ChannelContextHolder getFollower() {
        return follower;
    }

    public void setFollower(ChannelContextHolder follower) {
        this.follower = follower;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }


    public boolean already() {
        return (leader != null && follower != null);
    }

    /**
     * 校验是否在一台Worker
     * @return
     */
    public boolean consistency() {
        if(already()) {
            return leader.getHostPort().getHost().equals(follower.getHostPort().getHost());
        }
        return false;
    }
    public boolean consistencyWorker() {
        return leaderWorker != null && leaderWorker.equals(followerWorker);
    }

    public String getLeaderWorker() {
        return leaderWorker;
    }

    public void setLeaderWorker(String leaderWorker) {
        this.leaderWorker = leaderWorker;
    }

    public String getFollowerWorker() {
        return followerWorker;
    }

    public void setFollowerWorker(String followerWorker) {
        this.followerWorker = followerWorker;
    }
    public void updateWorker(String worker) {
        if(leaderWorker == null) {
            setLeaderWorker(worker);
        } else {
            setFollowerWorker(worker);
        }
    }

    public boolean isCreateLeader() {
        return createLeader;
    }

    public void setCreateLeader(boolean createLeader) {
        this.createLeader = createLeader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatSession that = (ChatSession) o;
        return Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }

    @Override
    public String toString() {
        return "ChatSession{" +
                "chatId='" + chatId + '\'' +
                ", leader=" + getLeaderAddress() +
                ", follower=" + getFollowerAddress() +
                ", leaderWorker=" + leaderWorker +
                ", followerWorker=" + followerWorker +
                ", createdTime=" + createdTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public ChannelContextHolder toChannel(ChannelHandlerContext from) {
        return leader.getCtx().equals(from) ? follower : leader;
    }
    public ChannelContextHolder fromChannel(ChannelHandlerContext from) {
        return leader.getCtx().equals(from) ? leader : follower;
    }


    public enum ChatStatus {
        CREATE,SESSION,DESTROY
    }

}