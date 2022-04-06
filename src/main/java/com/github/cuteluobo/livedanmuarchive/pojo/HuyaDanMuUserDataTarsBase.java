package com.github.cuteluobo.livedanmuarchive.pojo;

import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.protocol.tars.TarsStructBase;

/**
 * 虎牙弹幕-用户数据 Tars解析规则
 * @author CuteLuoBo
 * @date 2022/2/11 18:22
 */
public class HuyaDanMuUserDataTarsBase extends TarsStructBase {
    private HuYaUserInfo huYaUserInfo = null;
    @Override
    public void writeTo(TarsOutputStream os) {

    }

    @Override
    public void readFrom(TarsInputStream is) {
        //解析参数来源：https://github.com/759434091/danmu-crawler/blob/master/huya/pojo/Huya.js
        is.setServerEncoding("utf-8");
        huYaUserInfo = new HuYaUserInfo();
        //读此参数时无数据报错，故取消
//        huYaUserInfo.setlUid(is.read(0, 0, false));
        huYaUserInfo.setlImid(is.read(0, 1, false));
        huYaUserInfo.setNickName(is.read("", 2, false));
        huYaUserInfo.setiGender(is.read(0, 3, false));
        huYaUserInfo.setsAvatarUrl(is.read("", 4, false));
        huYaUserInfo.setiNobleLevel(is.read(0, 5, false));

    }

    public HuYaUserInfo getHuYaUserInfo() {
        return huYaUserInfo;
    }
}
