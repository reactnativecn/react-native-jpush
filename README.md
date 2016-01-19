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
