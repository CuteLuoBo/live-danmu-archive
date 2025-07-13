package com.github.cuteluobo.livedanmuarchive.command.impl;

import com.github.cuteluobo.livedanmuarchive.command.base.AbstractCompositeCommand;
import com.github.cuteluobo.livedanmuarchive.command.base.action.SubCommandReg;
import com.github.cuteluobo.livedanmuarchive.enums.danmu.send.VideoPlatform;
import com.github.cuteluobo.livedanmuarchive.service.Impl.BiliVideoUpdateListenServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author CuteLuoBo
 * @date 2023/3/17 14:41
 */
public class DanMuSenderCommand extends AbstractCompositeCommand {
    Logger logger = LoggerFactory.getLogger(DanMuSenderCommand.class);

    public DanMuSenderCommand() {
        super("danMuSender", new String[]{"dms"}, "弹幕自动发送配置,使用dms help获取帮助");
    }

    @SubCommandReg(subCommandName = {"","help"})
    public boolean help(String... args) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("功能说明：\r\n")
                .append("当前为自动从配置文件中读取数据，自动监听并发送弹幕数据\n")
                .append("可用指令\n")
                .append("danMuSender addVideoId/addv <平台英文> <视频BV号>\n")
                .append("支持的平台：")
                .append(Arrays.stream(VideoPlatform.values())
                        .map(v -> v.getName() + "-" + v.getCommit())
                        .collect(Collectors.joining(","))).append("\n")
                .append("完整示例A：danMuSender addVideoId bili BVxxxxxx\n")
                .append("简化示例A：dms addv bili BVxxxxxx\n")
        ;
        System.out.println(stringBuilder);
        logger.debug(stringBuilder.toString());
        return true;
    }

    @SubCommandReg(subCommandName = {"addVideoId","addv"})
    public boolean addVideoId(String... args) {
        if (args.length > 1) {
            String platform = args[0];
            VideoPlatform videoPlatform = VideoPlatform.getEnumByName(platform);
            if (videoPlatform == null) {
                System.out.printf("输入的视频平台：%s不支持，当前支持平台：%s%n",platform, Arrays.stream(VideoPlatform.values()).map(u -> u.getName()+"-"+u.getCommit()).collect(Collectors.joining(",")));
                return false;
            }
            String videoId = args[1];
            if (videoId.trim().isEmpty()) {
                System.out.println("输入的视频ID不能为空");
                return false;
            }
            return BiliVideoUpdateListenServiceImpl.getInstance().addVideo(videoId.trim());
        } else {
            System.out.println("输入的参数数量不正确，请检查");
        }

        return true;
    }
}
