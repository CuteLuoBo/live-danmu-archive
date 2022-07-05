package com.github.cuteluobo.livedanmuarchive;

import com.github.cuteluobo.livedanmuarchive.builder.DanMuServiceBuilder;
import com.github.cuteluobo.livedanmuarchive.enums.DanMuExportType;
import com.github.cuteluobo.livedanmuarchive.enums.ExportPattern;
import com.github.cuteluobo.livedanmuarchive.exception.ServiceException;
import com.github.cuteluobo.livedanmuarchive.manager.FileExportManager;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuData;
import com.github.cuteluobo.livedanmuarchive.pojo.DanMuUserInfo;
import com.github.cuteluobo.livedanmuarchive.service.DanMuService;
import com.github.cuteluobo.livedanmuarchive.service.Impl.SqliteDanMuExportServiceImpl;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class ApplicationTests{

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestUnitByDanMu.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println(result.wasSuccessful());
    }

    void test1() {
        ArrayList<DanMuUserInfo> danMuUserInfoArrayList = new ArrayList<>();
        DanMuUserInfo danMuUserInfo1 = new DanMuUserInfo();
        danMuUserInfo1.setUid("1");
        danMuUserInfo1.setNickName("N1");
        danMuUserInfoArrayList.add(danMuUserInfo1);

        DanMuUserInfo danMuUserInfo2 = new DanMuUserInfo();
        danMuUserInfo2.setUid("2");
        danMuUserInfo2.setNickName("N2");
        danMuUserInfoArrayList.add(danMuUserInfo2);

        DanMuUserInfo danMuUserInfo3 = new DanMuUserInfo();
        danMuUserInfo3.setUid("1");
        danMuUserInfo3.setNickName("N3");
        danMuUserInfoArrayList.add(danMuUserInfo3);

        DanMuUserInfo danMuUserInfo4 = new DanMuUserInfo();
        danMuUserInfo4.setUid("1");
        danMuUserInfo4.setNickName("N4");
        danMuUserInfoArrayList.add(danMuUserInfo4);

        danMuUserInfoArrayList.stream().collect(Collectors.toMap(DanMuUserInfo::getUid, DanMuUserInfo::getNickName, (v1, v2) -> v1+','+v2)).forEach((k,v) ->{
            System.out.println("k:"+k+", v:"+v);
        });
    }
}
