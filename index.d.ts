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
  static getDeviceId(): Promise<string>;
}
