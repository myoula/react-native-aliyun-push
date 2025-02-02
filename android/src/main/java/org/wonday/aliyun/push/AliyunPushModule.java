/**
 * Copyright (c) 2017-present, Wonday (@wonday.org)
 * All rights reserved.
 *
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.wonday.aliyun.push;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import android.graphics.Color;
import android.os.Build;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Promise;
import com.facebook.react.common.ReactConstants;
import com.facebook.common.logging.FLog;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.CommonCallback;
import me.leolin.shortcutbadger.ShortcutBadger;

import org.wonday.aliyun.push.MIUIUtils;

public class AliyunPushModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private final ReactApplicationContext context;
    private int badgeNumber;

    public AliyunPushModule(ReactApplicationContext reactContext) {

        super(reactContext);
        this.context = reactContext;
        this.badgeNumber = 0;
        AliyunPushMessageReceiver.context = reactContext;
        ThirdPartMessageActivity.context = reactContext;

        context.addLifecycleEventListener(this);

    }

    //module name
    @Override
    public String getName() {
        return "AliyunPush";
    }

    @ReactMethod
    public void initCloudChannel(String key, String secret, final Promise promise) {

        // 创建notificaiton channel
        this.createNotificationChannel();
        PushServiceFactory.init(this.context);
        CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.setLogLevel(CloudPushService.LOG_INFO);
        //pushService.setNotificationSmallIcon(R.mipmap.ic_launcher);//设置通知栏小图标， 需要自行添加
        pushService.register(this.context, new CommonCallback() {
            @Override
            public void onSuccess(String resp) {
                // success
                promise.resolve(resp);
            }
            @Override
            public void onFailed(String code, String message) {
                // failed
                promise.reject(message);
            }
        });

        // 关于第三方推送通道的设置，请仔细阅读阿里云文档
        // https://help.aliyun.com/document_detail/30067.html?spm=a2c4g.11186623.6.589.598b7fa8vf9qWF

        // 注册方法会自动判断是否支持小米系统推送，如不支持会跳过注册。
        // MiPushRegister.register(applicationContext, "小米AppID", "小米AppKey");

        // // 注册方法会自动判断是否支持华为系统推送，如不支持会跳过注册。
        // HuaWeiRegister.register(this);

        // // 接入FCM/GCM初始化推送
        // GcmRegister.register(applicationContext, "send_id", "application_id");

        // // OPPO通道注册
        // OppoRegister.register(applicationContext, "appKey", "appSecret"); // appKey/appSecret在OPPO通道开发者平台获取

        // // 魅族通道注册
        // MeizuRegister.register(applicationContext, "appId", "appkey"); // appId/appkey在魅族开发者平台获取

        // // VIVO通道注册
        // VivoRegister.register(applicationContext);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
            // 通知渠道的id
            String id = "1";
            // 用户可以看到的通知渠道的名字.
            CharSequence name = "notification channel";
            // 用户可以看到的通知渠道的描述
            String description = "notification description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // 配置通知渠道的属性
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @ReactMethod
    public void getDeviceId(final Promise promise) {
        String deviceID = PushServiceFactory.getCloudPushService().getDeviceId();
        if (deviceID!=null && deviceID.length()>0) {
            promise.resolve(deviceID);
        } else {
            // 或许还没有初始化完成，等3秒钟再次尝试
            try{
                Thread.sleep(3000);
                deviceID = PushServiceFactory.getCloudPushService().getDeviceId();

                if (deviceID!=null && deviceID.length()>0) {
                    promise.resolve(deviceID);
                    return;
                }
            } catch (Exception e) {

            }

            promise.reject("getDeviceId() failed.");
        }
    }

    @ReactMethod
    public void setApplicationIconBadgeNumber(int badgeNumber, final Promise promise) {

        if (MIUIUtils.isMIUI(getReactApplicationContext())) { //小米特殊处理
            FLog.d(ReactConstants.TAG, "setApplicationIconBadgeNumber for xiaomi");

            if (badgeNumber==0) {
                promise.resolve("");
                return;
            }

            try {

                MIUIUtils.setBadgeNumber(this.context, getCurrentActivity().getClass(), badgeNumber);
                this.badgeNumber = badgeNumber;
                promise.resolve("");

            } catch (Exception e) {

                promise.reject(e.getMessage());

            }


        } else {
            FLog.d(ReactConstants.TAG, "setApplicationIconBadgeNumber for normal");

            try {
                ShortcutBadger.applyCount(this.context, badgeNumber);
                this.badgeNumber = badgeNumber;
                promise.resolve("");
            } catch (Exception e){
                promise.reject(e.getMessage());
            }
        }

    }

    @ReactMethod
    public void getApplicationIconBadgeNumber(Callback callback) {
        callback.invoke(this.badgeNumber);
    }

    @ReactMethod
    public void bindAccount(String account, final Promise promise) {
        PushServiceFactory.getCloudPushService().bindAccount(account, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void unbindAccount(final Promise promise) {
        PushServiceFactory.getCloudPushService().unbindAccount(new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void bindTag(int target, ReadableArray tags, String alias, final Promise promise) {

        String[] tagStrs = new String[tags.size()];
        for(int i=0; i<tags.size();i++) tagStrs[i] = tags.getString(i);

        PushServiceFactory.getCloudPushService().bindTag(target, tagStrs, alias, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void unbindTag(int target, ReadableArray  tags, String alias, final Promise promise) {

        String[] tagStrs = new String[tags.size()];
        for(int i=0; i<tags.size();i++) tagStrs[i] = tags.getString(i);

        PushServiceFactory.getCloudPushService().unbindTag(target, tagStrs, alias, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void listTags(int target, final Promise promise) {
        PushServiceFactory.getCloudPushService().listTags(target, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void addAlias(String alias, final Promise promise) {
        PushServiceFactory.getCloudPushService().addAlias(alias, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void removeAlias(String alias, final Promise promise) {
        PushServiceFactory.getCloudPushService().removeAlias(alias, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @ReactMethod
    public void listAliases(final Promise promise) {
        PushServiceFactory.getCloudPushService().listAliases(new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                promise.resolve(response);
            }
            @Override
            public void onFailed(String code, String message) {
                promise.reject(code, message);
            }
        });
    }

    @Override
    public void onHostResume() {
        ThirdPartMessageActivity.mainClass = getCurrentActivity().getClass();
    }

    @Override
    public void onHostPause() {

        //小米特殊处理, 处于后台时更新角标， 否则会被系统清除，看不到
        if (MIUIUtils.isMIUI(getReactApplicationContext())) {
            FLog.d(ReactConstants.TAG, "onHostPause:setBadgeNumber for xiaomi");
            MIUIUtils.setBadgeNumber(this.context, getCurrentActivity().getClass(), badgeNumber);
        }

    }

    @Override
    public void onHostDestroy() {

        //小米特殊处理, 处于后台时更新角标， 否则会被系统清除，看不到
        if (MIUIUtils.isMIUI(getReactApplicationContext())) {
            FLog.d(ReactConstants.TAG, "onHostDestroy:setBadgeNumber for xiaomi");
            MIUIUtils.setBadgeNumber(this.context, getCurrentActivity().getClass(), badgeNumber);
        }

    }

    @ReactMethod
    public void getInitialMessage(final Promise promise){
        promise.resolve(AliyunPushMessageReceiver.initialMessage);
    }
}