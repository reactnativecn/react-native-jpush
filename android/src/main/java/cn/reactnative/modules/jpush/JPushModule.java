package cn.reactnative.modules.jpush;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.BuildConfig;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

/**
 * Created by lvbingru on 10/23/15.
 */
public class JPushModule extends ReactContextBaseJavaModule {

    private static Boolean registered = false;
    private static JPushModule gModules = null;
    private static String holdMessage = null;

    public JPushModule(ReactApplicationContext reactContext) {
        super(reactContext);

        if (!registered) {
            JPushInterface.init(reactContext);
            if (BuildConfig.DEBUG) {
                JPushInterface.setDebugMode(true);
            }
            registered = true;
        }
    }

    @Override
    public String getName() {
        return "JPush";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        if (holdMessage != null) {
            constants.put("initialNotification", holdMessage);
        }
        return constants;
    }

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

    private static void sendEvent(String eventName, Bundle bundle) {
        if (gModules != null){
            WritableMap message = Arguments.fromBundle(bundle);
            DeviceEventManagerModule.RCTDeviceEventEmitter emitter = gModules.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
            emitter.emit(eventName, message);
            return;
        }
    }

    private static final String TAG = "JPushReceiver";
    public static void onReceive(Context context, Intent intent) {

//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "接受到推送下来的自定义消息");
            JPushModule.sendEvent("kJPFNetworkDidReceiveCustomMessageNotification", bundle);

            String title = bundle.getString(JPushInterface.EXTRA_TITLE);
            String msg = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            long ID = bundle.getLong(JPushInterface.EXTRA_NOTIFICATION_ID);

            if (title == null) {
                title = "";
            }
            if (msg == null) {
                msg = "";
            }
            if (extras == null) {
                extras = "";
            }

            JPushLocalNotification ln = new JPushLocalNotification();
            ln.setBuilderId(0);
            ln.setContent(msg);
            ln.setTitle(title);
            ln.setNotificationId(ID);
            ln.setExtras(extras);
            ln.setNotificationId(System.currentTimeMillis());

            JPushInterface.addLocalNotification(context.getApplicationContext(), ln);

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "接受到推送下来的通知");
            JPushModule.sendEvent("kJPFNetworkDidReceiveMessageNotification", bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "用户点击打开了通知");
            JPushModule.sendEvent("kJPFNetworkDidOpenMessageNotification", bundle);

            if (gModules != null && gModules.getCurrentActivity()!=null) {
                Intent mIntent = new Intent(context, gModules.getCurrentActivity().getClass());
                mIntent.putExtras(bundle);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mIntent);
            }
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
        callback.invoke(registrationID == null ? "" : registrationID);
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

    @ReactMethod
    public void setPushTime
            (ReadableArray weaks, int startHour, int endHour) {
        JPushInterface.setPushTime(getReactApplicationContext(), _intArrayToSet(weaks), startHour, endHour);
    }

    @ReactMethod
    public void setSilenceTime(int startHour, int startMinute, int endHour, int endMinute) {
        JPushInterface.setSilenceTime(getReactApplicationContext(), startHour, startMinute, endHour, endMinute);
    }

    @ReactMethod
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

    public static class JPushReceiver extends BroadcastReceiver {

        public JPushReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {

//            Log.e(TAG, "onReceive - " + intent.getAction());
//            Log.e(TAG, "onReceive - " + intent.getExtras().toString());

            boolean isAppRunning = _isApplicationRunning(context);
//            Log.e("onReceive", isAppRunning ? "running" : "not running");

            if (isAppRunning) {
                JPushModule.onReceive(context, intent);
            }
            else {
                if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
                    Bundle bundle = intent.getExtras();

                    JSONObject json = new JSONObject();
                    Set<String> keys = bundle.keySet();
                    for (String key : keys) {
                        try {
                            json.put(key, bundle.get(key));
                        } catch(JSONException e) {
                        }
                    }
                    JPushModule.holdMessage = json.toString();

                    String packageName = context.getApplicationContext().getPackageName();
                    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    context.startActivity(launchIntent);
                }
            }
        }

        private boolean _isApplicationRunning(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
                if (processInfo.processName.equals(context.getApplicationContext().getPackageName())) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (String d: processInfo.pkgList) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}