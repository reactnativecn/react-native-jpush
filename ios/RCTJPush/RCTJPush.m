//
//  RCTJPush.m
//  RCTJPush
//
//  Created by LvBingru on 1/12/16.
//  Copyright © 2016 erica. All rights reserved.
//

#import "RCTJPush.h"
#import "APService.h"
#import "RCTEventDispatcher.h"
#import "RCTUtils.h"

NSString *const kJPFNetworkDidReceiveApnsMessageNotification = @"kJPFNetworkDidReceiveApnsMessageNotification";
NSString *const kJPFNetworkDidOpenApnsMessageNotification = @"kJPFNetworkDidOpenApnsMessageNotification";

@implementation RCTJPush

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (instancetype)init
{
    if ((self = [super init])) {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleNetworkDidReceiveMessageNotification:)
                                                     name:kJPFNetworkDidReceiveMessageNotification
                                                   object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleNetworkDidReceiveAPNSMessageNotification:)
                                                     name:kJPFNetworkDidReceiveApnsMessageNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleNetworkDidOpenAPNSMessageNotification:)
                                                     name:kJPFNetworkDidOpenApnsMessageNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleAppEnterForeground:) name:UIApplicationWillEnterForegroundNotification object:nil];
        
        [self resetBadge];
    }
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (NSDictionary<NSString *, id> *)constantsToExport
{
    NSDictionary<NSString *, id> *initialNotification = [_bridge.launchOptions[UIApplicationLaunchOptionsRemoteNotificationKey] copy];
    return @{@"initialNotification": RCTNullIfNil(initialNotification)};
}

+ (void)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        [APService setupWithOption:launchOptions];
    });
    
#ifdef DEBUG
    [APService setDebugMode];
#endif
}

+ (void)application:(__unused UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    [APService registerDeviceToken:deviceToken];
}

+ (void)application:(__unused UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification
{
    [APService handleRemoteNotification:notification];
    
    if (application.applicationState == UIApplicationStateInactive) {
        [[NSNotificationCenter defaultCenter] postNotificationName:kJPFNetworkDidOpenApnsMessageNotification object:nil userInfo:notification];
    }
    else {
        [[NSNotificationCenter defaultCenter] postNotificationName:kJPFNetworkDidReceiveApnsMessageNotification object:nil userInfo:notification];
    }
}

+ (void)_requestPermissions:(NSDictionary *)permissions
{
#if __IPHONE_OS_VERSION_MIN_REQUIRED < __IPHONE_8_0
    UIRemoteNotificationType types = UIRemoteNotificationTypeNone;
    if (permissions) {
        if ([permissions[@"alert"] boolValue]) {
            types |= UIRemoteNotificationTypeAlert;
        }
        if ([permissions[@"badge"] boolValue]) {
            types |= UIRemoteNotificationTypeBadge;
        }
        if ([permissions[@"sound"] boolValue]) {
            types |= UIRemoteNotificationTypeSound;
        }
    } else {
        types = UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound;
    }
    [APService registerForRemoteNotificationTypes:types categories:nil];
    
#else
    UIUserNotificationType types = UIUserNotificationTypeNone;
    if (permissions) {
        if ([permissions[@"alert"] boolValue]) {
            types |= UIUserNotificationTypeAlert;
        }
        if ([permissions[@"badge"] boolValue]) {
            types |= UIUserNotificationTypeBadge;
        }
        if ([permissions[@"sound"] boolValue]) {
            types |= UIUserNotificationTypeSound;
        }
    } else {
        types = UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound;
    }
    
    [APService registerForRemoteNotificationTypes:types categories:nil];
#endif
}

- (void)handleNetworkDidReceiveMessageNotification:(NSNotification *)notification
{
    [_bridge.eventDispatcher sendDeviceEventWithName:@"kJPFNetworkDidReceiveCustomMessageNotification"
                                                body:notification.userInfo];
}

- (void)handleNetworkDidReceiveAPNSMessageNotification:(NSNotification *)notification
{
    [_bridge.eventDispatcher sendDeviceEventWithName:@"kJPFNetworkDidReceiveMessageNotification"
                                                body:notification.userInfo];
}
- (void)handleNetworkDidOpenAPNSMessageNotification:(NSNotification *)notification
{
    [_bridge.eventDispatcher sendDeviceEventWithName:@"kJPFNetworkDidOpenMessageNotification"
                                                body:notification.userInfo];
}

- (void)handleAppEnterForeground:(NSNotification *)notification
{
    [self resetBadge];
}

RCT_EXPORT_METHOD(setAlias:(NSString *)alias)
{
    [APService setAlias:alias callbackSelector:NULL object:nil];
}

RCT_EXPORT_METHOD(setTags:(NSArray *)tags alias:(NSString *)alias)
{
    [APService setTags:[NSSet setWithArray:tags] alias:alias callbackSelector:NULL object:nil];
}

RCT_EXPORT_METHOD(cancelAllLocalNotifications)
{
    [APService clearAllLocalNotifications];
}

RCT_EXPORT_METHOD(setLocalNotification:(NSDictionary *)notification)
{
    double date = [notification[@"date"] doubleValue];
    NSString *alertBody = notification[@"alertBody"];
    int badge = [notification[@"badge"] intValue];
    NSString *alertAction = notification[@"alertAction"];
    NSString *identifierKey = notification[@"identifierKey"];
    NSDictionary *userInfo = notification[@"userInfo"];
    NSString *soundName = notification[@"identifierKey"];

#if __IPHONE_OS_VERSION_MIN_REQUIRED < __IPHONE_8_0
    [APService setLocalNotification:[NSDate dateWithTimeIntervalSince1970:date] alertBody:alertBody badge:badge alertAction:alertAction identifierKey:identifierKey userInfo:userInfo soundName:soundName];
#else
    CLRegion *region = nil;
    BOOL regionTriggersOnce = [notification[@"regionTriggersOnce"] boolValue];
    NSString *category = notification[@"category"];
    
    [APService setLocalNotification:[NSDate dateWithTimeIntervalSince1970:date] alertBody:alertBody badge:badge alertAction:alertAction identifierKey:identifierKey userInfo:userInfo soundName:soundName region:region regionTriggersOnce:regionTriggersOnce category:category];
#endif

}

RCT_EXPORT_METHOD(resetBadge)
{
    RCTSharedApplication().applicationIconBadgeNumber = 1;
    RCTSharedApplication().applicationIconBadgeNumber = 0;
    [APService resetBadge];
}

RCT_EXPORT_METHOD(setBadge:(int)badge)
{
    RCTSharedApplication().applicationIconBadgeNumber = badge;
    [APService setBadge:badge];
}

RCT_EXPORT_METHOD(getBadge:(RCTResponseSenderBlock)callback)
{
    callback(@[@(RCTSharedApplication().applicationIconBadgeNumber)]);
}

RCT_EXPORT_METHOD(getRegistrationID:(RCTResponseSenderBlock)callback)
{
    NSString *registrationID = [APService registrationID];
    callback(@[RCTNullIfNil(registrationID)]);
}

RCT_EXPORT_METHOD(setLogOFF)
{
    [APService setLogOFF];
}

RCT_EXPORT_METHOD(crashLogON)
{
    [APService crashLogON];
}

RCT_EXPORT_METHOD(setLocation:(double)latitude
                  :(double)longitude)
{
    [APService setLatitude:latitude longitude:longitude];
}

RCT_EXPORT_METHOD(startLogPageView:(NSString *)logPageView)
{
    [APService startLogPageView:logPageView];
}

RCT_EXPORT_METHOD(stopLogPageView:(NSString *)logPageView)
{
    [APService stopLogPageView:logPageView];
}

RCT_EXPORT_METHOD(beginLogPageView:(NSString *)logPageView duration:(int)duration)
{
    [APService beginLogPageView:logPageView duration:duration];
}

RCT_EXPORT_METHOD(requestPermissions:(NSDictionary *)permissions)
{
    if (RCTRunningInAppExtension()) {
        return;
    }
    
    [RCTJPush _requestPermissions:permissions];
}

RCT_EXPORT_METHOD(abandonPermissions)
{
    [RCTSharedApplication() unregisterForRemoteNotifications];
}

@end
