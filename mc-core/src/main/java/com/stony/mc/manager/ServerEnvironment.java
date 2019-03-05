package com.stony.mc.manager;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * <p>message-channel
 * <p>com.stony.mc.manager
 *
 * @author stony
 * @version 下午3:08
 * @since 2019/1/24
 */
public class ServerEnvironment implements Serializable {
    private static final long serialVersionUID = -4544680558225014984L;

    @JSONField(name = "java_vm_name")
    String javaVmName;
    @JSONField(name = "java_version")
    String javaVersion;
    @JSONField(name = "os_arch")
    String osArch;
    @JSONField(name = "os_name")
    String osName;
    @JSONField(name = "os_version")
    String osVersion;


    public String getJavaVmName() {
        return javaVmName;
    }

    public void setJavaVmName(String javaVmName) {
        this.javaVmName = javaVmName;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    @Override
    public String toString() {
        return "ServerEnvironment{" +
                "javaVmName='" + javaVmName + '\'' +
                ", javaVersion='" + javaVersion + '\'' +
                ", osArch='" + osArch + '\'' +
                ", osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                '}';
    }
}