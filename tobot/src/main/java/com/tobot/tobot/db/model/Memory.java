package com.tobot.tobot.db.model;

import com.tobot.tobot.sqlite.annotation.Column;
import com.tobot.tobot.sqlite.annotation.Id;
import com.tobot.tobot.sqlite.annotation.Table;

import java.io.Serializable;

/**
 * Created by Javen on 2017/12/15.
 */
@Table(name = "tab_tobot_memory")
public class Memory implements Serializable {
//    private static Memory memory;
//
//    private Memory(){}
//
//    public static synchronized Memory instance() {
//        if (memory == null) {
//            memory = new Memory();
//        }
//        return memory;
//    }

    @Id(name = "keyId")
    public String keyId = "memory";

    @Column(name = "global")
    private String global = "0";//当true对全局动作进行阻拦

    @Column(name = "orderType")
    private String orderType = "0";//当false对全局非命令动作进行抛弃

    @Column(name = "motion")
    private String motion = "0";//保存当前需记忆的动作

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getGlobal() {
        return global;
    }

    public void setGlobal(String global) {
        this.global = global;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getMotion() { return motion; }

    public void setMotion(String motion) {
        this.motion = motion;
    }


}
