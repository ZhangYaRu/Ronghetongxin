package com.zhbd.beidoucommunication.domain;

import java.io.Serializable;

/**
 * Created by zhangyaru on 2017/9/8.
 */

public class ReceiverMessage implements Serializable {
    /**
     * 消息来自哪里
     */
    private int from;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    /**
     * 文字消息序号
     */
    private int textMsgNumber;

    /**
     * 发送者ID
     */
    private int senderUserId;

    /**
     * 信息bit长度
     */
    private int contentBitLen;

    /**
     * 加密标识
     */
    private boolean isEncrypt;

    /**
     * 目的地用户ID
     */
    private int destinationUserId;

    /**
     * 是否是群组消息
     */
    private boolean isGroup;

    /**
     * 内容
     */
    private String content;


    public int getTextMsgNumber() {
        return textMsgNumber;
    }

    public void setTextMsgNumber(int textMsgNumber) {
        this.textMsgNumber = textMsgNumber;
    }

    public int getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(int senderUserId) {
        this.senderUserId = senderUserId;
    }

    public int getContentBitLen() {
        return contentBitLen;
    }

    public void setContentBitLen(int contentBitLen) {
        this.contentBitLen = contentBitLen;
    }

    public boolean isEncrypt() {
        return isEncrypt;
    }

    public void setEncrypt(boolean encrypt) {
        isEncrypt = encrypt;
    }

    public int getDestinationUserId() {
        return destinationUserId;
    }

    public void setDestinationUserId(int destinationUserId) {
        this.destinationUserId = destinationUserId;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ReceiverMessage{" +
                "textMsgNumber=" + textMsgNumber +
                ", senderUserId=" + senderUserId +
                ", contentBitLen=" + contentBitLen +
                ", isEncrypt=" + isEncrypt +
                ", destinationUserId=" + destinationUserId +
                ", isGroup=" + isGroup +
                ", content='" + content + '\'' +
                '}';
    }
}
