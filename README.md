# react-native-jpush

React Native的极光推送插件, react-native版本需要0.17.0及以上

## 如何安装

### 首先安装npm包

```bash
npm install react-native-jpush --save
```

### link
```bash
rnpm link react-native-jpush
```

#### Note: 
* rnpm requires node version 4.1 or higher
* Android SDK Build-tools 23.0.2 or higher


### iOS工程配置
a.在工程target的`Build Phases->Link Binary with Libraries`中加入`libz.tbd、CoreTelephony.framework、Security.framework`

b.在你的工程中创建一个新的Property List文件，并将其命名为PushConfig.plist，文件所含字段如下：

```
CHANNEL
	指明应用程序包的下载渠道，为方便分渠道统计，具体值由你自行定义，如：App Store。
APP_KEY
	与JPush上申请的 AppKey 一致。
APS_FOR_PRODUCTION
	0 (默认值)表示采用的是开发证书，1 表示采用生产证书发布应用。
	注：此字段的值要与Build Settings的Code Signing配置的证书环境一致。
```
c.在`AppDelegate.m`中加入

```
#import "RCTJPush.h"

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  ...
  
  [RCTJPush application:application didFinishLaunchingWithOptions:launchOptions];
  
  ...
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
  [RCTJPush application:application didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification
{
  [RCTJPush application:application didReceiveRemoteNotification:notification];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
  [RCTJPush application:application didReceiveRemoteNotification:notification];
  completionHandler(UIBackgroundFetchResultNewData);
}
```

### Android配置

在`android/app/build.gradle`里，defaultConfig栏目下添加如下代码：

```
manifestPlaceholders = [
    JPush_APPID: "JPush的APPID",	//在此修改JPush的APPID
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
