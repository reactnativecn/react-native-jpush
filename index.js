/**
 * Created by lvbingru on 10/23/15.
 */
import {NativeModules, DeviceEventEmitter} from 'react-native';

const nativeModule = NativeModules.JPush;
const invariant = require('invariant');

const _notifHandlers = [];
let _initialNotification = nativeModule &&
    nativeModule.initialNotification;

export const JpushEventReceiveMessage = 'kJPFNetworkDidReceiveMessageNotification'
export const JpushEventOpenMessage = 'kJPFNetworkDidOpenMessageNotification'
export const JpushEventReceiveCustomMessage = 'kJPFNetworkDidReceiveCustomMessageNotification'

export default class JPushNotification {

    _data;

    constructor(nativeNotif) {
        this._data = {};

        if (typeof nativeNotif === 'string') {
            nativeNotif = JSON.parse(nativeNotif)
        }

        if (nativeNotif) {
            this._data = nativeNotif
        }
    }

    static popInitialNotification() {
        const initialNotification = _initialNotification &&
            new JPushNotification(_initialNotification);
        _initialNotification = null;
        return initialNotification;
    }

    static setAlias(alias){
        if (!alias) {
            alias = ''
        }
        nativeModule.setAlias(alias)
    }

    static setTags(tags, alias){
        if (!alias) {
            alias = ''
        }
        nativeModule.setTags(tags, alias)
    }

    static getRegistrationID(){
        return new Promise(resolve=>{
            nativeModule.getRegistrationID(resolve)
        })
    }

    static addEventListener(type: string, handler: Function) {
        checkListenerType(type)

        if (type === JpushEventOpenMessage && _initialNotification) {
            handler(this.popInitialNotification())
        }
        const listener = DeviceEventEmitter.addListener(
            type,
            (note) => {
                handler(note && new JPushNotification(note));
            }
        );
        _notifHandlers.push(listener)
        return listener;
    }

    static removeEventListener(listener) {
        const index = _notifHandlers.indexOf(listener)
        if (index >=0) {
            _notifHandlers.splice(index,1)
            listener.remove()
        }
    }

    // ios only
    static resetBadge(){
        nativeModule.resetBadge && nativeModule.resetBadge()
    }

    static requestPermissions(permissions){
        nativeModule.requestPermissions && nativeModule.requestPermissions(permissions)
    }

    static abandonPermissions(){
        nativeModule.abandonPermissions && nativeModule.abandonPermissions()
    }

    static cancelAllLocalNotifications(){
        nativeModule.cancelAllLocalNotifications && nativeModule.cancelAllLocalNotifications()
    }

    static setLocalNotification(notification){
        nativeModule.setLocalNotification && nativeModule.setLocalNotification(notification)
    }

    static beginLogPageView(page, duration){
        nativeModule.beginLogPageView && nativeModule.beginLogPageView(page, duration)
    }

    static setLogOFF(){
        nativeModule.setLogOFF && nativeModule.setLogOFF()
    }

    static crashLogON(){
        nativeModule.crashLogON && nativeModule.crashLogON()
    }

    static setLocation(location){
        nativeModule.setLocation && nativeModule.setLocation(location.latitude, location.longitude)
    }

    static startLogPageView(page){
        nativeModule.startLogPageView && nativeModule.startLogPageView(page)
    }

    static stopLogPageView(page){
        nativeModule.stopLogPageView && nativeModule.stopLogPageView(page)
    }

    // android only
    static clearNotificationById(notificationId : number){
        nativeModule.clearNotificationById && nativeModule.clearNotificationById(notificationId)
    }

    static clearAllNotifications(){
        nativeModule.clearAllNotifications && nativeModule.clearAllNotifications()
    }

    static clearLocalNotifications(){
        nativeModule.clearLocalNotifications && nativeModule.clearLocalNotifications()
    }

    static stopPush(){
        nativeModule.stopPush && nativeModule.stopPush()
    }

    static resumePush(){
        nativeModule.resumePush && nativeModule.resumePush()
    }

    static setLatestNotificationNumber(maxNum : number){
        nativeModule.setLatestNotificationNumber && nativeModule.setLatestNotificationNumber(maxNum)
    }

    static setPushTime(weaks : array, startHour : number, endHour : number){
        nativeModule.setPushTime && nativeModule.setPushTime(weaks, startHour, endHour)
    }

    static setSilenceTime(startHour, startMinute, endHour, endMinute) {
        nativeModule.setSilenceTime && nativeModule.setSilenceTime(startHour, startMinute, endHour, endMinute)
    }
}

function checkListenerType(type) {
    invariant(
        type === JpushEventReceiveMessage || type === JpushEventOpenMessage || type === JpushEventReceiveCustomMessage,
        'JPushNotification only supports `JpushEventReceiveMessage` ,`JpushEventOpenMessage`, `JpushEventReceiveCustomMessage`, events'
    );
}


