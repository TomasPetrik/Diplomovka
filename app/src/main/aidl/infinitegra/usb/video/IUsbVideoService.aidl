/*
 * Copyright (c) Infinitegra Inc, All rights reserved.
 */
package infinitegra.usb.video;

import infinitegra.usb.video.IUsbVideoCallback;

/**
* AndCam-ULib Expressを制御するAPI一覧<br>
* この関数群を利用してUSBカメラの画像を取得することができる。<br>
* 画像取得のために、アプリ側で{@link infinitegra.usb.video.IUsbVideoCallback IUsbVideoCallback}の実装、および登録を行う必要がある。
* <br><br>
* API List for controling AndCam-ULib Express.<br>
* Be able to capture video from USB Camera by using these APIs.
* To capture video, the application have to implement {@link infinitegra.usb.video.IUsbVideoCallback IUsbVideoCallback} and register.<br>
*/
interface IUsbVideoService
{
	/**
	* メッセージの送信。 <br>
	* Sending message. <br><br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_BILLING CALL_GET_BILLING(4)}と
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_REGISTERED CALL_GET_REGISTERED(5)}
	* の戻り値が両方1の場合に制約が解除される。<br>
	* The restrictions in trial mode are removed is return 1 by both of
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_BILLING CALL_GET_BILLING(4)} and 
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_REGISTERED CALL_GET_REGISTERED(5)}
	* @param event 呼び出すイベント<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_STREAM_START CALL_VIDEO_STREAM_START(1)}:VIDEOの開始イベント<br>
	* VIDEOが既に開始されていたら無視される。<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_STREAM_STOP CALL_VIDEO_STREAM_STOP(2)}:VIDEOの終了イベント<br>
	* VIDEOが開始されていなければ無視される。<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_BILLING CALL_GET_BILLING(4)}:課金状態取得イベント<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_REGISTERED CALL_GET_REGISTERED(5)}:登録状態取得イベント<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_RECORD_START CALL_VIDEO_RECORD_START(7)}:VIDEOの録画開始イベント<br>
	* 音声は未サポート。<br>
	* VIDEOが開始されていなければ、エラーになる。{@link infinitegra.usb.video.UsbVideoErrorID#NOTIFY_ERROR_STOP_USB_VIDEO}<br>
	* 引数のstr1に保存するファイル名を指定する。<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_RECORD_STOP CALL_VIDEO_RECORD_STOP(8)}:VIDEOの録画停止イベント<br>
	* VIDEOの録画が開始されていなければ無視される。<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_SET_RECORD_DIR CALL_SET_RECORD_DIR(9)}:VIDEOの録画ディレクトリ設定イベント<br>
	* 録画ファイルが保存されるディレクトリを設定する。指定しない場合にはデフォルトのディレクトリを使用する。指定されたディレクトリが存在しなければ、エラーになる。<br>
	* 設定した値は、VIDEOの録画停止などでクリアされる。そのため、VIDEOの録画開始イベント開始前に設定するよう注意すること。<br>
	* デフォルトのディレクトリは、「/sdcard/DCIM/ift/usbcamera」である。
	* <br>
	* event - The event to be called.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_STREAM_START CALL_VIDEO_STREAM_START(1)}:Video starting event.<br>
	* It is ignored if video is already started.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_STREAM_STOP CALL_VIDEO_STREAM_STOP(2)}:Video stoping event.<br>
	* It is ignored is video is not started.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_BILLING CALL_GET_BILLING(4)}:Get pay status event.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_REGISTERED CALL_GET_REGISTERED(5)}:Get registration status event.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_RECORD_START CALL_VIDEO_RECORD_START(7)}:Video recording start event.<br>
	* Sound recording is not supported.<br>
	* It returns error is video is not started.{@link infinitegra.usb.video.UsbVideoErrorID#NOTIFY_ERROR_STOP_USB_VIDEO}<br>
	* Specify recording file name to str1.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_RECORD_STOP CALL_VIDEO_RECORD_STOP(8)}:Video recording stop event.<br>
	* It is ignored if video recording is not started.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_SET_RECORD_DIR CALL_SET_RECORD_DIR(9)}:Set directory for video recording event.<br>
	* Set the directory to be save the file for video recording. If not specified, the default directory is used to save video recording file.
	* It return error if specified directory is not exist.<br>
	* Specified directory path is cleared by the events like video recording stop, etc. 
	* Therefore, please care to set directory path before start video recording.<br>
	* Default directory path is "/sdcard/DCIM/ift/usbcamera"<br><br>
	* @param arg1 通常は0を設定しておくこと。<br>
	* arg1 - Usually set to 0.<br><br>
	* @param arg2 未使用。0を設定しておくこと。<br>
	* arg2 - Unused. Should set to 0.<br><br>
	* @param str1 通常はnullを設定しておくこと。<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_RECORD_START CALL_VIDEO_RECORD_START(7)}、
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_SET_RECORD_DIR CALL_SET_RECORD_DIR(9)}の場合に利用する。<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_RECORD_START CALL_VIDEO_RECORD_START(7)}<br>
	* VIDEOの録画するファイル名を指定する。ファイル名を指定しない場合や指定のファイルが存在する場合、指定のファイルを生成できない場合にはエラーになる。<br>
	* ファイル名は、全角文字、Windowsのファイル名として利用できない文字、#が含まれている場合はエラーとなる。<br>
	* また100文字を超えるファイル名の場合もエラーになる。
	* {@link infinitegra.usb.video.UsbVideoErrorID#NOTIFY_ERROR_INVALID_RECORD_FILE_NAME}<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_SET_RECORD_DIR CALL_SET_RECORD_DIR(9)}<br>
	* VIDEOの録画で保存するディレクトリを指定する。<br>
	* 指定したディレクトリが存在しない場合にはエラーになる。{@link infinitegra.usb.video.UsbVideoErrorID#NOTIFY_ERROR_INVALID_RECORD_DIRECTORY}<br>
	* ディレクトリ名は、全角文字、Windowsのディレクトリ名として利用できない文字、#が含まれている場合はエラーとなる。<br>
	* また150文字を超えるファイル名の場合もエラーになる。
	* <br>
	* str1 - Usually set to NULL.<br>
	* This parameter uses for {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_RECORD_START CALL_VIDEO_RECORD_START(7)}、
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_SET_RECORD_DIR CALL_SET_RECORD_DIR(9)}.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_VIDEO_RECORD_START CALL_VIDEO_RECORD_START(7)}<br>
	* Specisy file name for video recoding. It returns error is the file is already exist or failed to make the file.
	* {@link infinitegra.usb.video.UsbVideoErrorID#NOTIFY_ERROR_INVALID_RECORD_FILE_NAME}<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_SET_RECORD_DIR CALL_SET_RECORD_DIR(9)}<br>
	* Specify the directory path to save the file for video recording.<br>
	* It returns error is the specified directory is not exist.{@link infinitegra.usb.video.UsbVideoErrorID#NOTIFY_ERROR_INVALID_RECORD_DIRECTORY}<br><br>
	* @return 通常終了は0。エラーの場合は-1が戻る。<br>
	* eventが{@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_BILLING CALL_GET_BILLING(4)}、または
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_REGISTERED CALL_GET_REGISTERED(5)}の場合には下記の値が戻る。<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_BILLING CALL_GET_BILLING(4)}<br>
	* 1:課金済<br>0:未課金<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_REGISTERED CALL_GET_REGISTERED(5)}<br>
	* 1:登録済<br>0:未登録<br>
	* Usually return 0. This value is available if the event is {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_BILLING CALL_GET_BILLING(4)} or
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_REGISTERED CALL_GET_REGISTERED(5)}.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_BILLING CALL_GET_BILLING(4)}<br>
	* 1:paid<br>0:not paid<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#CALL_GET_REGISTERED CALL_GET_REGISTERED(5)}<br>
	* 1:Registered<br>0:Not registered
	*/
	int send(int event, int arg1, int arg2, String str1);

	/**
	* Callbackの登録。 <br>
	* Register Callback. <br><br>
	* ここで登録されたCallbackにAndCam-ULib ExpressからのNotifyが通知される。<br>
	* 通知されるNotifyは{@link infinitegra.usb.video.IUsbVideoCallback IUsbVideoCallback}を参照。<br>
	* Notify from AndCam-ULib Express to User App.
	* Please refer to {@link infinitegra.usb.video.IUsbVideoCallback IUsbVideoCallback}
	* @param cb 登録するCallback<br>
	* 実体は呼び出し元アプリで実装を行う<br>
	* cb - Callback to be registered.<br>
	* Actual callback function should be implemented in User App.
	* @see infinitegra.usb.video.IUsbVideoService#unregisterCallback
	*/
	void registerCallback(IUsbVideoCallback cb);

	/**
	* Callbackの解除。 <br>
	* Unregister Callback. <br><br>
	* ServiceのUnbind前に呼び出して停止する。<br>
	* Call this API to stop callback before unbinding Service.
	* @param cb 解除するCallback<br>
	* 実体は呼び出し元アプリで実装を行う<br>
	* cb - Callback to be unregistered.<br>
	* Actual callback function should be implemented in User App.
	* @see infinitegra.usb.video.IUsbVideoService#registerCallback
	*/
	void unregisterCallback(IUsbVideoCallback cb);

}

