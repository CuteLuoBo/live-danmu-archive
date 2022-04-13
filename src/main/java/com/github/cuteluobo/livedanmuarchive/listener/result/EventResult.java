package com.github.cuteluobo.livedanmuarchive.listener.result;

/**
 * 事件结果
 *
 * @author CuteLuoBo
 * @date 2022/4/7 18:45
 */
public class EventResult {
    /**
     * 事件信息
     */
    private String message;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EventResult{");
        sb.append("message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
