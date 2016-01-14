# react-native-jpush

React Native的极光推送插件, react-native版本需要0.17.0及以上

## 如何安装

### 首先安装npm包

```bash
npm install react-native-jpush --save
```

### link
```bash
rnpm link
```

#### Note: rnpm requires node version 4.1 or higher


### iOS工程配置
在工程target的`Build Phases->Link Binary with Libraries`中加入`liz.tbd、CoreTelephony.framework、Security.framework`

### Android配置

在app的AndroidManifest.xml中`<application>`标签中添加如下代码，并将`"你的包名"`处修改为你的应用的包名：

```
<!--jpush-->
<service
    android:name="cn.jpush.android.service.DaemonService"
    android:enabled="true"
    android:exported="true">
    <intent-filter >
        <action android:name="cn.jpush.android.intent.DaemonService" />
        <category android:name="你的包名"/>
    </intent-filter>
</service>
<service
    android:name="cn.jpush.android.service.PushService"
    android:enabled="true"
    android:exported="false" >
    <intent-filter>
        <action android:name="cn.jpush.android.intent.REGISTER" />
        <action android:name="cn.jpush.android.intent.REPORT" />
        <action android:name="cn.jpush.android.intent.PushService" />
        <action android:name="cn.jpush.android.intent.PUSH_TIME" />
    </intent-filter>
</service>
<receiver
    android:name="cn.jpush.android.service.PushReceiver"
    android:enabled="true" >
    <intent-filter android:priority="1000">
        <action android:name="cn.jpush.android.intent.NOTIFICATION_RECEIVED_PROXY" />
        <category android:name="你的包名"/>
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.USER_PRESENT" />
        <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
    </intent-filter>
    <!-- Optional -->
    <intent-filter>
        <action android:name="android.intent.action.PACKAGE_ADDED" />
        <action android:name="android.intent.action.PACKAGE_REMOVED" />
        <data android:scheme="package" />
    </intent-filter>
</receiver>
<activity
    android:name="cn.jpush.android.ui.PushActivity"
    android:configChanges="orientation|keyboardHidden"
    android:exported="false" >
    <intent-filter>
        <action android:name="cn.jpush.android.ui.PushActivity" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="你的包名" />
    </intent-filter>
</activity>
<service
    android:name="cn.jpush.android.service.DownloadService"
    android:enabled="true"
    android:exported="false" >
</service>
<receiver android:name="cn.jpush.android.service.AlarmReceiver" />
<receiver
    android:name="cn.reactnative.modules.jpush.JPushModule$JPushReceiver"
    android:enabled="true">
    <intent-filter>
        <!--Required 用户注册SDK的intent-->
        <action android:name="cn.jpush.android.intent.REGISTRATION" />
        <action android:name="cn.jpush.android.intent.UNREGISTRATION" />
        <!--Required 用户接收SDK消息的intent-->
        <action android:name="cn.jpush.android.intent.MESSAGE_RECEIVED" />
        <!--Required 用户接收SDK通知栏信息的intent-->
        <action android:name="cn.jpush.android.intent.NOTIFICATION_RECEIVED" />
        <!--Required 用户打开自定义通知栏的intent-->
        <action android:name="cn.jpush.android.intent.NOTIFICATION_OPENED" />
        <!--Optional 用户接受Rich Push Javascript 回调函数的intent-->
        <action android:name="cn.jpush.android.intent.ACTION_RICHPUSH_CALLBACK" />
        <!-- 接收网络变化 连接/断开 since 1.6.3 -->
        <action android:name="cn.jpush.android.intent.CONNECTION" />
        <category android:name="你的包名" />
    </intent-filter>
</receiver>
<meta-data android:name="JPUSH_CHANNEL" android:value="${APP_CHANNEL}"/>
<meta-data android:name="JPUSH_APPKEY" android:value="${JPush_APPID}"/>

```

`android/app/build.gradle`里，defaultConfig栏目下添加如下代码：

```
manifestPlaceholders = [
    JPush_APPID: "JPush的APPID"		//在此修改JPush的APPID
    APP_CHANNEL: "应用渠道号"		//应用渠道号
]
```

## 如何使用

### 引入包

```
import JPush , {JpushEventReceiveMessage, JpushEventOpenMessage} from 'react-native-jpush'
```

在你的根Component中加入下面代码

```
componentDidMount() {
    JPush.requestPermissions()
    this.pushlisteners = [
        JPush.addEventListener(JpushEventReceiveMessage, this.onReceiveMessage.bind(this)),
        JPush.addEventListener(JpushEventOpenMessage, this.onOpenMessage.bind(this)),
    ]
}
componentWillUnmount() {
    this.pushlisteners.forEach(listener=> {
        JPush.removeEventListener(listener);
    });
}
onReceiveMessage(message) {
}
onOpenMessage(message) {
}
```
