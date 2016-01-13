package cn.reactnative.modules.jpush;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by lvbingru on 10/23/15.
 */
public class JPushModule extends ReactContextBaseJavaModule {

    private static Boolean registered = false;
    private static JPushModule gModules = null;
    private static WritableMap holdMessage = null;

    public JPushModule(ReactApplicationContext reactContext) {
        super(reactContext);

        if (!registered) {
            JPushInterface.init(reactContext);
//            if (BuildConfig.DEBUG) {
                JPushInterface.setDebugMode(true);
//            }
            registered = true;
        }

    }

    @Override
    public String getName() {
        return "JPush";
    }

//    @Override
//    public Map<String, Object> getConstants() {
//        final Map constants = new HashMap<>();
//        constants.put("initialNotification", holdMessage);
//        return constants;
//    }

    @Override
    public void initialize() {
        super.initialize();
        gModules = this;
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        gModules = null;
    }

    private static void sendEvent(String eventName, WritableMap message) {
        if (gModules != null){
            DeviceEventManagerModule.RCTDeviceEventEmitter emitter = gModules.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
            emitter.emit(eventName, message);
            return;
        }

        if (eventName.equals("kJPFNetworkDidOpenMessageNotification")) {
            holdMessage = message;
        }
    }

    private static final String TAG = "JPushReceiver";
    public static void onReceive(Context context, Intent intent, Class ActivityClass) {

//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Bundle bundle = intent.getExtras();
        WritableMap map = Arguments.fromBundle(bundle);
        Log.d(TAG, "onReceive - " + intent.getAction());

        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "接受到推送下来的自定义消息");
            JPushModule.sendEvent("kJPFNetworkDidReceiveCustomMessageNotification", map);

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "接受到推送下来的通知");
            JPushModule.sendEvent("kJPFNetworkDidReceiveMessageNotification", map);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "用户点击打开了通知");
            JPushModule.sendEvent("kJPFNetworkDidOpenMessageNotification", map);

            Intent mIntent = new Intent(context, ActivityClass);
            mIntent.putExtras(bundle);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mIntent);
        } else {
            Log.d(TAG, "Unhandled intent - " + intent.getAction());
        }
    }

    @ReactMethod
    public void stopPush(){
        JPushInterface.stopPush(getReactApplicationContext());
    }

    @ReactMethod
    public void resumePush(){
        JPushInterface.resumePush(getReactApplicationContext());
    }

    @ReactMethod
    public void setAlias(String alias) {
        JPushInterface.setAlias(getReactApplicationContext(), alias, null);
    }
    @ReactMethod
    public void setTags(ReadableArray tags, String alias) {
        JPushInterface.setAliasAndTags(getReactApplicationContext(), alias, _stringArrayToSet(tags));
    }

    @ReactMethod
    public void getRegistrationID(Callback callback) {
        String registrationID = JPushInterface.getRegistrationID(getReactApplicationContext());
        callback.invoke(registrationID==null?"":registrationID);
    }

    @ReactMethod
    public void clearLocalNotifications() {
        JPushInterface.clearLocalNotifications(getReactApplicationContext());
    }

    @ReactMethod
    public void clearAllNotifications() {
        JPushInterface.clearAllNotifications(getReactApplicationContext());
    }

    @ReactMethod
    public void clearNotificationById(int notificationId) {
        JPushInterface.clearNotificationById(getReactApplicationContext(), notificationId);
    }

    public void setPushTime(ReadableArray weaks, int startHour, int endHour) {
        JPushInterface.setPushTime(getReactApplicationContext(), _intArrayToSet(weaks), startHour, endHour);
    }

    public void setSilenceTime(int startHour, int startMinute, int endHour, int endMinute) {
        JPushInterface.setSilenceTime(getReactApplicationContext(), startHour, startMinute, endHour, endMinute);
    }

    public void setLatestNotificationNumber(int maxNum) {
        JPushInterface.setLatestNotificationNumber(getReactApplicationContext(), maxNum);
    }

    private Set _stringArrayToSet(ReadableArray array) {
        Set<String> set = new HashSet<String>();
        if (array != null) {
            int size = array.size();
            for (int i=0;i<size;i++) {
                String obj = array.getString(i);
                set.add(obj);
            }
        }
        return set;
    }
    private Set _intArrayToSet(ReadableArray array) {
        Set<Integer> set = new HashSet<Integer>();
        if (array != null) {
            int size = array.size();
            for (int i=0;i<size;i++) {
                Integer obj = array.getInt(i);
                set.add(obj);
            }
        }
        return set;
    }
}