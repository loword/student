# 网易云信娱乐直播 导读

网易云信娱乐直播工程集成网易云信 SDK、网易视频云 SDK，展示组合了直播(推拉流)与直播间功能的应用场景，为娱乐行业提供直播解决方案。

## <span id="源码导读"> 源码导读</span>

### 工程结构说明

源码主要分成四个 package ：base、im、thirdparty 和 entertainment。
- base：封装一些 UI 基类，工具类等。
- im：包含登录页面及登录、注册业务逻辑（ activity / business / config 子包下）、基础 UI 组件( ui 子包下)、 会话页面相关组件( session 子包下)。
- thirdparty：包含网易视频云直播(推流)、网易视频云播放器(拉流)相关的核心组件。
- entertainment：娱乐直播相关的页面和业务逻辑。

下面具体介绍 entertainment 包下的子包结构：
- activity：所有 Activity。
- adapter：聊天页面数据源适配器等。
- constant: 聊天页面数据常量。
- helper：直播间成员缓存，主播收到礼物缓存，礼物动画等帮助类。
- http: 网易云信直播间 Demo Http Client(与云信 Demo 应用服务器通信)
- model: 聊天界面数据实体。
- module：直播间收发消息模块、直播间自定义消息。
- ui: 直播间界面 ui 控件。
- viewholder：界面相关 ViewHolder。

### 重点类说明

- LivePlayerBaseActivity : 直播间基类。包括直播间的进入/离开的操作，监听直播间在线状态和监听直播间被踢出状态。
- LiveActivity：主播端 Activity。包含主播相关操作。
- AudienceActivity： 观众端 Activity。包含观众香港操作。
- IdentifyActivity： 选择主播或观众身份界面
- EnterRoomActivity： 观众身份填写主播创建的课堂id，进入直播间
- ChatRoomMsgViewHolderFactory:  直播间消息项展示ViewHolder工厂类。包括消息展示 ViewHolder 的注册操作。


/************************ Meeting *****************************/

# 网易云信在线课堂 导读

网易云信在线课堂工程集成网易云信 SDK，展示组合了在线课堂语音视频通话功能的应用场景。

## <span id="源码导读"> 源码导读</span>

### 工程结构说明

源码主要分成四个 package ：base、im、thirdparty 和 meeting。
- base：封装一些 UI 基类，工具类等。
- im：包含登录页面及登录、注册业务逻辑（ activity / business / config 子包下）、基础 UI 组件( ui 子包下)、 会话页面相关组件( session 子包下)。
- meeting：在线课堂相关的页面和业务逻辑。

下面具体介绍 meeting 包下的子包结构：
- activity：所有 Activity。
- adapter：聊天页面数据源适配器等。
- helper：聊天室成员缓存，控制消息发送，聊天室通知消息构造等帮助类。
- module：收发消息模块、自定义消息、网易云信在线课堂 Demo Http Client(与云信 Demo 应用服务器通信)。
- viewholder：界面相关 ViewHolder。

### 重点类说明

- ChatRoomActivity：在线课堂界面。封装了 ChatRoomFragment。包括聊天室的进入/离开的操作，监听聊天室在线状态和监听聊天室被踢出状态，。
- ChatRoomTabFragment：在线课堂内 tab fragment 的基类。
- ChatRoomFragment：在线课堂 Activity 包含的顶层 Fragment。包括加入/离开音视频频道的操作，音视频图像的显示与移除，监听音视频状态。
包括界面上方界面和下方的 viewpager。viewpager 包含3个 tab fragment，分别是：MessageTabFragment （讨论区 tab）， OnlinePeopleTabFragment （在线成员 tab），ApplyForSpeechTabFragment (举手发言 tab，这个tab只在观众端显示）。
- MessageTabFragment：讨论区基类 fragment。内嵌 ChatRoomMessageFragment （讨论区 fragment）。。
- OnlinePeopleTabFragment：在线成员基类 fragment。内嵌 OnlinePeopleFragment （在线成员 fragment）
- ApplyForSpeechTabFragmet：举手发言基类 fragment。内嵌ApplyForSpeechFragment (举手发言 fragment)
- ChatRoomMessageFragment：讨论区fragment。包括消息的收发相关操作。
- OnlinePeopleFragment：聊天室在线人数 fragment。包括开启音视频的控制。
- ApplyForSpeechFragment：举手发言 fragment。包括成员举手，正在发言的相关操作
- ChatRoomMsgViewHolderFactory:  聊天室消息项展示ViewHolder工厂类。包括消息展示 ViewHolder 的注册操作。
- IdentifyActivity： 选择主持人或观众身份界面
- EnterRoomActivity： 主持人创建聊天室和音视频频道进入课堂。观众搜索聊天室id进入课堂。


### 流程说明

主播：
1、创建聊天室
2、创建在线课堂课堂
3、进入聊天室
4、进入在线课堂课堂 同时 创建白板课堂并进入白板课堂

观众：
1、进入聊天室
2、进入在线课堂课堂 同时 进入白板课堂