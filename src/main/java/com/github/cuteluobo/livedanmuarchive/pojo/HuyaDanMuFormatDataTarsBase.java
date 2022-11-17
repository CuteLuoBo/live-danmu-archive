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
        int color = is.read(0, 0, false);
        danMuFormatData.setFontColor(color == -1 ? 0 : color);
        //danMuFormatData.setFontSize(is.read(0, 1, false));
        //虎牙平台弹幕字体大小有额外标准，目前暂时抛弃使用默认=25
        //TODO 增加虎牙平台弹幕的字体大小解析
        danMuFormatData.setFontSize(25);
        danMuFormatData.setTextSpeed(is.read(0, 2, false));
        danMuFormatData.setTransitionType(is.read(0, 3, false));
        danMuFormatData.setPopupStyle(is.read(0, 4, false));
    }

    public HuyaDanMuFormatData getDanMuFormatData() {
        return danMuFormatData;
    }
}
