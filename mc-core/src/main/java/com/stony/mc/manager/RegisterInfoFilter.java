package com.stony.mc.manager;

import com.alibaba.fastjson.annotation.JSONField;
import com.stony.mc.Utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * <p>message-channel
 * <p>com.stony.mc.manager
 *
 * @author stony
 * @version 上午11:56
 * @since 2019/1/24
 */
public class RegisterInfoFilter implements Serializable, Predicate<RegisterInfo> {
    private static final long serialVersionUID = 5590049673873580022L;

    @JSONField(name = "uid")
    private String uid;
    private String device;

    @JSONField(name = "filter_tags")
    private Set<String> filterTags;
    @JSONField(name = "filter_alias")
    private Set<String> filterAlias;

    @JSONField(name = "match_tags")
    private Set<String> matchTags;
    @JSONField(name = "match_alias")
    private Set<String> matchAlias;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Set<String> getFilterTags() {
        return filterTags;
    }

    public void setFilterTags(Set<String> filterTags) {
        this.filterTags = filterTags;
    }

    public Set<String> getFilterAlias() {
        return filterAlias;
    }

    public void setFilterAlias(Set<String> filterAlias) {
        this.filterAlias = filterAlias;
    }

    public Set<String> getMatchTags() {
        return matchTags;
    }

    public void setMatchTags(Set<String> matchTags) {
        this.matchTags = matchTags;
    }

    public Set<String> getMatchAlias() {
        return matchAlias;
    }

    public void setMatchAlias(Set<String> matchAlias) {
        this.matchAlias = matchAlias;
    }
//    AtomicBoolean init = new AtomicBoolean();
//    Set<Predicate> filters = new HashSet<>(16);
//    Set<Predicate> matches = new HashSet<>(16);
    public boolean isAllEmpty() {
        return Utils.isEmpty(uid)
                && Utils.isEmpty(device)
                && matchTags == null
                && matchAlias == null
                && filterTags == null
                && filterAlias == null;
    }
    @Override
    public boolean test(RegisterInfo info) {

        //filter 或过滤 ||
        if(filterTags != null && !filterTags.isEmpty() && info.getTags() != null) {
            for (String tag : info.getTags()) {
                if(filterTags.contains(tag)) {
                    return false;
                }
            }
        }
        if(filterAlias != null && !filterAlias.isEmpty() && info.getAlias() != null) {
            for (String tag : info.getAlias()) {
                if(filterAlias.contains(tag)) {
                    return false;
                }
            }
        }

        //match 与匹配 &&
        boolean test = false;
        if(Utils.isNotEmpty(uid) && Utils.isNotEmpty(info.getUid())) {
            if (!uid.equals(info.getUid())) {
                return false;
            }
        }
        if(Utils.isNotEmpty(device) && Utils.isNotEmpty(info.getDevice())) {
            if (!device.equals(info.getDevice())) {
                return false;
            }
        }
        if(matchTags != null && !matchTags.isEmpty() && info.getTags() != null) {
            boolean tag_test = false;
            for (String tag : info.getTags()) {
                if(matchTags.contains(tag)) {
                    tag_test = true;
                    break;
                }
            }
            test = tag_test;
        }
        if(matchAlias != null && !matchAlias.isEmpty() && info.getTags() != null) {
            boolean alias_test = false;
            for (String tag : info.getTags()) {
                if(matchAlias.contains(tag)) {
                    alias_test = true;
                    break;
                }
            }
            test = test && alias_test;
        }
        return test;
    }

}