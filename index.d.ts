export interface PushMessage {
  /**
   * "notification":通知 或者 "message":消息
   */
  type: 'notification' | 'message';
  /**
   * 推送通知/消息标题
   */
  title: string;
  /**
   * 推送通知/消息具体内容
   */
  body: string;
  /**
   * "opened":用户点击了通知, "removed"用户删除了通知, 其他非空值:用户点击了自定义action（仅限ios）
   */
  actionIdentifier: string;
  /**
   * 用户附加的{key:value}的对象
   */
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
  /**
   * 设置桌面图标角标数字 (ios支持，android支持绝大部分手机)
   */
  static setApplicationIconBadgeNumber(num: number): void;
}
