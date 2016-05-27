/*
 * Copyright (c) Infinitegra Inc, All rights reserved.
 */
package infinitegra.usb.video;

import android.os.ParcelFileDescriptor;

/**
* AndCam-ULib Expressから呼び出されるCallback関数<br>
* このインターフェイスはAndCam-ULib Expressを呼び出すアプリで実装して、{@link infinitegra.usb.video.IUsbVideoService#registerCallback registerCallback}で登録する。<br><br>
* Callback function for AndCam-ULib Express.<br>
* This interface is implemented in AndCam-ULib Express and register by {@link infinitegra.usb.video.IUsbVideoService#registerCallback registerCallback}
**/
interface IUsbVideoCallback
{
	/**
	* AndCam-ULib Expressが1フレーム分の取得した映像を渡せる状態になった時に呼び出す。 <br>
	* Call when 1 frame data of video is ready on AndCam-ULib Express. <br><br>
	* 実装元で書き出し用、読み出し用のParcelFileDescriptorを作成し、書き出し用のParcelFileDescriptorを
	* 戻り値で戻す。<br>AndCam-ULib Expressで書き出し用のParcelFileDescriptorに1フレーム情報を書き出すので、アプリ側では
	* 書き出された情報を読み出すことで、USBカメラの映像が取得できる。<br>
	* 実装はサンプルアプリを参照のこと。
	* <br>
	* User app should prepare 2 ParceFileDescriptor for read and write, and return ParceFileDescriptor for write.<br>
	* To get video data from USB camera, User app reads ParceFileDescriptor for write since AndCam-ULib Express write 1 frame data of video to ParceFileDescriptor for write.<br>
	* Please refer Sample app for implementation.
	* @param size 書き出すbyteのサイズ<br>
	* size - data size for write.(in byte)<br>
	* @return AndCam-ULib Express側で書き出すためのParcelFileDescriptor<br>
	* ParceFileDescriptor to be written by AndCam-ULib Express.
	*/
	ParcelFileDescriptor renderStream(int size);

	/**
	* AndCam-ULib Expressで発生した現象について通知する。 <br>
	* Notify event in AndCam-ULib Express. <br>
	* <br>
	* <table border="1">
	* <tr align="center"><td align="left">event</td><td>arg1</td><td>arg2</td><td>arg3</td></tr>
	* <tr align="center"><td align="left">{@link infinitegra.usb.video.UsbVideoConstructs#RECV_NOTIFY_ERROR RECV_NOTIFY_ERROR(1)}</td>
	* <td>{@link infinitegra.usb.video.UsbVideoErrorID Error ID}を参照</td><td>-</td><td>Error Message</td></tr>
	* <tr align="center"><td align="left">{@link infinitegra.usb.video.UsbVideoConstructs#RECV_IMAGE_SIZE RECV_IMAGE_SIZE(2)}</td>
	* <td>width</td><td>height</td><td>-</td></tr>
	* <tr align="center"><td align="left">{@link infinitegra.usb.video.UsbVideoConstructs#RECV_IMAGE_TEXTURE_SIZE RECV_IMAGE_TEXTURE_SIZE(3)}</td>
	* <td>width of texture</td><td>height of texture</td><td>-</td></tr>
	* <tr align="center"><td align="left">{@link infinitegra.usb.video.UsbVideoConstructs#RECV_POST_START_STREAM RECV_POST_START_STREAM(4)}</td>
	* <td>-</td><td>-</td><td>-</td></tr>
	* <tr align="center"><td align="left">{@link infinitegra.usb.video.UsbVideoConstructs#RECV_RUNNING RECV_RUNNING(5)}</td>
	* <td>1: stream running</td><td>-</td><td>-</td></tr>
	* </table>
	* <br><br>
	* <table border="1">
	* <tr align="center"><td align="left">event</td><td>arg1</td><td>arg2</td><td>arg3</td></tr>
	* <tr align="center"><td align="left">Please refer {@link infinitegra.usb.video.UsbVideoConstructs#RECV_NOTIFY_ERROR RECV_NOTIFY_ERROR(1)}</td>
	* <td>{@link infinitegra.usb.video.UsbVideoErrorID Error ID}</td><td>-</td><td>Error Message</td></tr>
	* <tr align="center"><td align="left">{@link infinitegra.usb.video.UsbVideoConstructs#RECV_IMAGE_SIZE RECV_IMAGE_SIZE(2)}</td>
	* <td>width</td><td>height</td><td>-</td></tr>
	* <tr align="center"><td align="left">{@link infinitegra.usb.video.UsbVideoConstructs#RECV_IMAGE_TEXTURE_SIZE RECV_IMAGE_TEXTURE_SIZE(3)}</td>
	* <td>width of texture</td><td>height of texture</td><td>-</td></tr>
	* <tr align="center"><td align="left">{@link infinitegra.usb.video.UsbVideoConstructs#RECV_POST_START_STREAM RECV_POST_START_STREAM(4)}</td>
	* <td>-</td><td>-</td><td>-</td></tr>
	* <tr align="center"><td align="left">{@link infinitegra.usb.video.UsbVideoConstructs#RECV_RUNNING RECV_RUNNING(5)}</td>
	* <td>1: stream running</td><td>-</td><td>-</td></tr>
	* </table>
	* @param event 呼び出されるイベント<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_NOTIFY_ERROR RECV_NOTIFY_ERROR(1)} 
	* 問題が発生した場合にエラーメッセージを渡す<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_IMAGE_SIZE RECV_IMAGE_SIZE(2)} 
	* USBカメラが読み出す画像のサイズを渡す<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_IMAGE_TEXTURE_SIZE RECV_IMAGE_TEXTURE_SIZE(3)} 
	* AndCam-ULib Expressが書き込むバッファのサイズを渡す<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_POST_START_STREAM RECV_POST_START_STREAM(4)} 
	* Streamの読出しが開始されたら呼び出される<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_RUNNING RECV_RUNNING(5)} 
	* Streamの読出し状態が変更されたら呼び出される<br>
	* ただし、Unbindされた時には呼び出されない。
	* <br>
	* event - event that is called.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_NOTIFY_ERROR RECV_NOTIFY_ERROR(1)} 
	* Pass error message when issue occurr.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_IMAGE_SIZE RECV_IMAGE_SIZE(2)} 
	* Pass image size  for capturing from USB camera.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_IMAGE_TEXTURE_SIZE RECV_IMAGE_TEXTURE_SIZE(3)} 
	* Pass buffer size for AndCam-ULib Express.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_POST_START_STREAM RECV_POST_START_STREAM(4)} 
	* Called when reading Stream data is started.<br>
	* {@link infinitegra.usb.video.UsbVideoConstructs#RECV_RUNNING RECV_RUNNING(5)} 
	* Called when reading status for Stream data is changed.<br>
	* However, this isn't called for unbind.<br><br>
	* @param arg1 各イベントで利用されるパラメータ<br>
	* arg1 - parameter by using each event<br><br>
	* @param arg2 各イベントで利用されるパラメータ<br>
	* arg2 - parameter by using each event<br><br>
	* @param arg3 各イベントで利用されるパラメータ <br>
	* arg3 - parameter by using each event
	*/
	void onNotifyMessage(int event, int arg1, int arg2, String arg3);

	/**
	* 未サポート<br>
	* unsupported.
	*/
	ParcelFileDescriptor encodeVideo(int size);
}
