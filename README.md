[comment]: <> (<p align="center">)

[comment]: <> (  <img src="../logo.gif" height="300" />)

[comment]: <> (</p>)
<p align="center">
  直播弹幕录制&存档
</p>
<p align="center">
  <a href="https://github.com/cuteluobo/live-danmu-archive/blob/main/LICENSE">
    <img alt="GitHub" src="https://img.shields.io/github/license/cuteluobo/live-danmu-archive?style=flat-square"/>
  </a>
  <a href="https://github.com/misitebao/standard-repository">
    <img alt="GitHub" src="https://cdn.jsdelivr.net/gh/misitebao/standard-repository@main/assets/badge_flat-square.svg"/>
  </a>
  <a href="https://github.com/cuteluobo/live-danmu-archive">
    <img alt="GitHub Repo stars" src="https://img.shields.io/github/stars/cuteluobo/live-danmu-archive?style=flat-square"/>
  </a>
  <a href="https://github.com/CuteLuoBo/live-danmu-archive/releases">
    <img alt="GitHub release (latest by date including pre-releases)" src="https://img.shields.io/github/v/release/cuteluobo/live-danmu-archive?include_prereleases&sort=semver&style=flat-square">
  </a>
  <a href="https://github.com/cuteluobo">
    <img alt="GitHub user" src="https://img.shields.io/badge/author-cuteluobo-brightgreen?style=flat-square"/>
  </a>

[comment]: <> (  <a href="https://github.com/cuteluobo/live-danmu-archive/actions/workflows/pre-build.yml">)

[comment]: <> (    <img alt="Pre Build" src="https://img.shields.io/github/workflow/status/cuteluobo/live-danmu-archive/Pre%20Build%20%7C%20预构建/main?style=flat-square&logo=github"/>)

[comment]: <> (  </a>)
</p>

<span id="nav-1"></span>


## 内容目录

<details>
  <summary>点我 打开/关闭 目录列表</summary>

- [内容目录](#nav-1)
- [项目介绍](#nav-3)
    - [背景](#nav-3-1)
- [功能特色](#nav-5)
- [TODO](#nav-6)
- [新手入门](#nav-7)
- [维护者](#nav-8)
- [贡献者](#nav-9)
- [更新日志](#nav-10)
- [特别感谢](#nav-11)
- [版权许可](#nav-15)

</details>

<span id="nav-3"></span>

## 项目介绍

 此项目用于将各直播平台的弹幕进行录制存档，方便用于后续处理
- 开发环境：`OpenJdk11`
- 关键词: `直播弹幕录制`、`直播弹幕处理`
- 当前录制性能(v0.2.1)：
  - 运行时CPU和IO性能占用不高，主要是内存占用
  - 22-04-27 本地测试 未限制JVM内存参数时，
    - 基础运行内存120MB+;
    - 单B站头部主播（平均30弹幕/s）20任务,占用内存260MB+;
    - B站（平均30弹幕/s）20任务+虎牙赛事（平均50弹幕/s）10任务，平均占用内存400M

<span id="nav-3-1"></span>


### 背景

- 目前在直播录播时，通常只能存档视频画面，无法同步储存直播弹幕，打算另外存档，并考虑其他方式输入进视频.
- 在Github找到一些Python的弹幕获取项目，但没有可用的存档方式，同时因为开发语言差异，无法方便二次开发，于是制作此项目

<span id="nav-5"></span>

## 功能特色

- 多平台直播弹幕源支持（虎牙、哔哩哔哩、斗鱼，更多平台后续添加）
- 以SQLITE数据库储存的弹幕信息
- 提供储存弹幕的格式化导出（ASS文件/B站BAS弹幕等）
- 提供对视频平台的弹幕转发

<span id="nav-6"></span>

## 需求列表

- [x] 对程序增加命令行操作界面和正常结束方法
- [x] 增加读取存档弹幕，根据输入时间轴，匹配并导出ASS格式弹幕功能
- [x] 增加斗鱼弹幕录制
- [x] 增加存档弹幕后，对B站指定UP主的特定视频输出直播弹幕功能
- [ ] 对弹幕词/弹幕用户屏蔽输出


<span id="nav-7"></span>

## 新手入门

###程序运行
 0. 项目建议运行环境：`Java11`，可在 [Eclipse Temurin](https://adoptium.net/temurin/releases/) 中下载对应版本
 1. 在 [Releases](https://github.com/CuteLuoBo/live-danmu-archive/releases) 中下载最新发行包
 2. 使用 `java -jar {fileName.jar}`命令运行程序，可保存为shell脚本方便后续执行
    ![img_1.png](img_1.png)
    ![img.png](img.png)
 3. 程序第一次执行后会生成模板配置文件并中止运行，需要配置文件后重新执行程序。
    ![img_3.png](img_3.png)

###配置文件示例(`1.2.0`):
 #### 1. 配置弹幕录制示例
当前支持B站/虎牙/斗鱼的弹幕保存，填入直播间房间号后可直接匹配，此处的saveName(保存文件名)将会在`弹幕发送、指令系统`中用到
- exportPatten(导出文件存档模式)：目前两种模式均不会影响功能使用
  - allCollect-始终使用同一个数据库文件保存
  - dayFolder-将会在启动时按当前日期，创建新的数据库保存(后续可能会实现运行时自动切割)，
- danMuExportType(导出类型模式)：请始终保持为`sqlite`，功能都将在此模式中进行开发，json已放弃
 ```yaml
# 录制记录
Record:
  # 录制列表
  recordList:
    # 单个录制分块
    -
      roomUrl: "https://live.bilibili.com/6" # 直播间录制url
      recordType: danmu # 录制类型（弹幕/礼物/视频等，待拓展此类暂时无效）
      saveName: B站-LPL # 保存文件名称
      exportPatten: dayFolder # 导出文件存档模式 (allCollect,dayFolder)
      danMuExportType: sqlite # 导出类型模式 (sqlite,json)
      danmuRecordRetryTime: 300 # 重试时间(单位：秒，设置为-1时不进行重试)
   ```
#### 2.1 配置弹幕自动发送任务示例
当前版本只支持B站投稿，同时需要视频稿件的分P（文件名）中带有时间文本，例：`2023-03-31T12_00_00`，以解析并根据视频持续时间匹配弹幕。
其他格式时间可更改配置以匹配

```yaml
 # 弹幕自动发送任务设置
 DanMuAutoSendTask:
   videoPTimeFormat: "yyyy-MM-dd'T'HH_mm_ss" # 视频分P时间解析格式
   videoPTimeRegular: "\s*([0-9]{4,}-[0-1]*[0-9]-[0-3]*[0-9]T[0-2][0-9]_[0-6][0-9]_[0-6][0-9])" # 视频分P时间正则匹配格式()
   # 部署列表
   deployList:
     -
       videoPlatform: bili # 视频平台(bili-B站)
       listenUpUid: 0 # 监听的上传者UID
       titleMatch: 直播回放 # 标题匹配字符
       tagMatch: autoDanMu,直播回放 # 标签匹配字符，以英文逗号(,)分割
       linkDanMuSaveName: B站-LPL # 链接的弹幕保存名称
   ```
#### 2.2 配置弹幕自动发送账户示例
当前版本只支持B站，只需要保持平台名称并填入账户的Cookies即可，建议使用LV2及以上的活跃账户，否则大概率会吞弹幕，其他选项暂时无效果(待实现自动登录)

目前对于单个IP最适合的账户数量还在摸索，基础的弹幕发送延迟为5s，但发送过多会导致频繁发送，单弹幕预期发送时间为30s~45s/条，用户可按单次直播的大概弹幕数量配置账户，但已知单IP登录的账户过多可能触发异常，须小心

```yaml
 # 弹幕自动发送账号设置
 DanMuAutoSendAccount:
   # 账号列表
   accountList:
     -
       videoPlatform: bili # 视频平台 (bili-B站)
       nickName: null
       userName: null
       password: null
       cookies: SESSDATA=xxx; bili_jct=xxx; DedeUserID=xxx; DedeUserID__ckMd5=xxx; # 登录cookies
       accessKey: null
       appKey: null
   ```
 


    
>遇到其他问题欢迎提交issue


## 维护者

感谢这些项目的维护者：

<a href="https://github.com/cuteluobo"><img src="https://github.com/cuteluobo.png" width="40" height="40" alt="cuteluobo" title="cuteluobo"/></a>

<details>
  <summary>点我 打开/关闭 维护者列表</summary>

- [cuteluobo](https://github.com/cuteluobo) - 项目作者。

</details>

<span id="nav-9"></span>

## 贡献者

Nothing.

[comment]: <> (感谢所有参与 live-danmu-archive 开发的贡献者。[贡献者列表]&#40;https://github.com/cuteluobo/live-danmu-archive/graphs/contributors&#41;)

[comment]: <> (<a href="https://github.com/{userid}"><img src="https://github.com/{userid}.png" width="40" height="40" alt="{userid}" title="{userid}"/></a>)

<span id="nav-10"></span>

## 更新日志
- v0.5.5 增加弹幕重复信息合并功能，增加AppKey接口
- v0.5.4 修复B站API请求错误问题
- v0.5.3 实现B站弹幕转发，根据配置对指定UP动态监听并匹配视频和分P时间自动发送弹幕
- v0.4.0 实现斗鱼弹幕获取，调整不同平台获取的弹幕样式储存格式
- v0.3.0 增加指令系统，添加弹幕导出到ASS指令功能
- v0.2.1 丢弃Spring框架依赖，发布测试发行版本
- v0.2.0 实现哔哩哔哩弹幕获取，完善录制中断重续模式，增加配置文件
  - 弹幕录制源参考[real-url](https://github.com/wbt5/real-url) 、[bilibili-API-collect](https://github.com/SocialSisterYi/bilibili-API-collect)
- v0.1.1 增加Sqlite储存格式
- v0.1.0 实现虎牙弹幕获取
  - 弹幕录制源参考：[real-url](https://github.com/wbt5/real-url) 、[danmu-crawler](https://github.com/759434091/danmu-crawler)
- v0.0.1 项目起始，建立JSON储存格式


<span id="nav-11"></span>


## 特别感谢
- [real-url](https://github.com/wbt5/real-url) - 各直播源&弹幕解析的集合项目
- [danmu-crawler](https://github.com/759434091/danmu-crawler) - 解析思路参考 


<span id="nav-15"></span>

## 版权许可

[License GNU General Public License v3.0](LICENSE)