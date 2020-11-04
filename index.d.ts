export interface PushMessage {
  type: 'notification' | 'message';
  title: string;
  body: string;
  actionIdentifier: string;
  extras: Record<string, any>;
}

type Callback = (message: PushMessage) => void;

export default class AliyunPuth {
  static addListener(callback: Callback): void;
  static removeListener(callback: Callback): void;
  /**
   * 同步角标数到阿里云服务端 (仅ios支持)
   */
  static syncBadgeNum(num: number): void;
  /**
   * 获取初始消息
   */
  static getInitialMessage(): Promise<PushMessage>;
  /**
   * 获取设备Id
   */
  static getDeviceId(): Promise<string>;
  /**
   * 绑定账号
   * @param account 账号
   */
  static bindAccount(account: string): Promise<string>;
  /**
   * 解绑账号
   */
  static unbindAccount(): Promise<string>;
}
