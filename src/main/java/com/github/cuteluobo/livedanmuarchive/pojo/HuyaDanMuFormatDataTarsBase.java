package com.github.cuteluobo.livedanmuarchive.pojo;

import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.tars.TarsStructBase;

/**
 * 虎牙弹幕-颜色数据 Tars解析规则
 * @author CuteLuoBo
 * @date 2022/2/12 11:25
 */
public class HuyaDanMuFormatDataTarsBase extends TarsStructBase {
    private HuyaDanMuFormatData danMuFormatData;
    @Override
    public void writeTo(TarsOutputStream os) {

    }

    @Override
    public void readFrom(TarsInputStream is) {
        is.setServerEncoding("utf-8");
        danMuFormatData = new HuyaDanMuFormatData();
        danMuFormatData.setFontColor(is.read(0, 0, false));
        danMuFormatData.setFontSize(is.read(0, 1, false));
        danMuFormatData.setTextSpeed(is.read(0, 2, false));
        danMuFormatData.setTransitionType(is.read(0, 3, false));
        danMuFormatData.setPopupStyle(is.read(0, 4, false));
    }

    public HuyaDanMuFormatData getDanMuFormatData() {
        return danMuFormatData;
    }
}
