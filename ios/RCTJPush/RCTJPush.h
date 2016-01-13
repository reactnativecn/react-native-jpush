//
//  RCTJPush.h
//  RCTJPush
//
//  Created by LvBingru on 1/12/16.
//  Copyright © 2016 erica. All rights reserved.
//
#import <UIKit/UIKit.h>

#import "RCTBridgeModule.h"


@interface RCTJPush : NSObject<RCTBridgeModule>

+ (void)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions;
+ (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
+ (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification;

@end
