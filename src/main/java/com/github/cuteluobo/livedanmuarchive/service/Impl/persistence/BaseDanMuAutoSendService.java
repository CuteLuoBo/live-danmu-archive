package com.github.cuteluobo.livedanmuarchive.service.Impl.persistence;

import com.github.cuteluobo.livedanmuarchive.pojo.DanMuSenderAccountData;
import com.github.cuteluobo.livedanmuarchive.pojo.danmusender.ProcessVideoData;
import com.github.cuteluobo.livedanmuarchive.service.DanMuAutoSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础弹幕自动发送实现类，在此层封装账户信息读取
 * @author CuteLuoBo
 * @date 2023/3/8 15:05
 */
public abstract class BaseDanMuAutoSendService<T extends DanMuSenderAccountData,V extends ProcessVideoData> implements DanMuAutoSendService<V> {
    private final Logger logger = LoggerFactory.getLogger(BaseDanMuAutoSendService.class);
    /**
     * 账户列表
     */
    private List<T> accountList = new ArrayList<>();

    public BaseDanMuAutoSendService() {
    }

    public BaseDanMuAutoSendService(List<T> accountList) {
        setAccountList(accountList);
    }

    /**
     * 检查与更新账户列表
     */
    protected void checkAccountList() {
        if (accountList == null) {
            accountList = new ArrayList<>(10);
        } else {
            for (int i = 0; i < accountList.size(); i++) {
                T ac = accountList.get(i);
                ac = checkAccountLogin(ac);
                if (ac != null) {
                    //实现后再更新账户数据
//                    accountList.set(i, updateAccount(ac));
                    accountList.set(i, ac);
                } else {
                    logger.warn("第{}个账户登录状态校验失败，抛弃",i+1);
                }
            }
        }
    }

    /**
     * 检查账号是否登录
     * @param danMuSenderAccountData 弹幕发送者账户数据
     * @return 处理后的账户数据
     */
    abstract T checkAccountLogin(T danMuSenderAccountData);

    /**
     * 更新账户登录信息(CK/accessKey)
     * @param danMuSenderAccountData 弹幕发送者账户数据
     * @return 更新完成后的账户信息
     */
    abstract T updateAccount(T danMuSenderAccountData);

    public List<T> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<T> accountList) {
        this.accountList = accountList;
        checkAccountList();
    }
}
