package com.github.cuteluobo.livedanmuarchive.model;

import com.github.cuteluobo.livedanmuarchive.pojo.DanMuFormat;

import java.util.Objects;

/**
 * 弹幕格式模型
 * @author CuteLuoBo
 * @date 2022/4/6 10:40
 */
public class DanMuFormatModel extends DanMuFormat {
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DanMuFormatModel)) {
            return false;
        }
        DanMuFormatModel that = (DanMuFormatModel) o;
        return super.equals(o) && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFontColor(), getFontSize(), getTextSpeed(), getTransitionType(), getPopupStyle());
    }
}
