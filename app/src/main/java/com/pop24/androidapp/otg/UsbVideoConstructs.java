/*
 * Copyright (c) Infinitegra Inc, All rights reserved.
 */
package com.pop24.androidapp.otg;

/**
 * AndCam-ULib Expressで利用する定数を定義する
 * <br><br>
 * Define constants in AndCam-ULib Express.
 */
public class UsbVideoConstructs
{
	/**
	 * {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}
	 * で利用する。 <br>
	 * Use in {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}. <br>
	 * <br>
	 * VIDEO STREAMの開始イベント<br>
	 * 値は1
	 * <br>
	 * Event to start video streaming.<br>
	 * Defined as 1.
	 */
	public static final int CALL_VIDEO_STREAM_START = 1;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}
	 * で利用する。 <br>
	 * Use in {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}. <br>
	 * <br>
	 * VIDEO STREAMの終了イベント<br>
	 * 値は2
	 * <br>
	 * Event to stop video streaming.<br>
	 * Defined as 2.
	 */
	public static final int CALL_VIDEO_STREAM_STOP = 2;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}
	 * で利用する。 <br>
	 * Use in {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}. <br>
	 * <br>
	 * VIDEO STREAMの動作状況取得イベント<br>
	 * 値は3
	 * <br>
	 * Event to get video streaming status.<br>
	 * Defined as 3.
	 */
	public static final int CALL_GET_RUNNING = 3;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}
	 * で利用する。 <br>
	 * Use in {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}. <br>
	 * <br>
	 * 課金状況取得イベント<br>
	 * 値は4
	 * <br>
	 * Event to get pay status.<br>
	 * Defined as 4.
	 */
	public static final int CALL_GET_BILLING = 4;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}
	 * で利用する。 <br>
	 * Use in {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}. <br>
	 * <br>
	 * 登録状況取得イベント<br>
	 * 値は5
	 * <br>
	 * Event to get registration status.<br>
	 * Defined as 5.
	 */
	public static final int CALL_GET_REGISTERED = 5;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}
	 * で利用する。 <br>
	 * Use in {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}. <br>
	 * <br>
	 * VIDEOの録画開始イベント<br>
	 * 値は7
	 * <br>
	 * Event to start video recording.<br>
	 * Defined as 7.
	 */
	public static final int CALL_VIDEO_RECORD_START = 7;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}
	 * で利用する。 <br>
	 * Use in {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}. <br>
	 * <br>
	 * VIDEOの録画停止イベント<br>
	 * 値は8
	 * <br>
	 * Event to stop video recording.<br>
	 * Defined as 8.
	 */
	public static final int CALL_VIDEO_RECORD_STOP = 8;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}
	 * で利用する。 <br>
	 * Use in {@link infinitegra.usb.video.IUsbVideoService#send(int, int, int, String)}. <br>
	 * <br>
	 * VIDEOの録画ディレクトリ設定イベント<br>
	 * 値は9
	 * <br>
	 * Event to set directory path for recording file.<br>
	 * Defined as 9.
	 */
	public static final int CALL_SET_RECORD_DIR = 9;

	/**
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}
	 * で利用される。 <br>
	 * Use in 
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}. <br>
	 * <br>
	 * エラー通知<br>
	 * 値は1
	 * <br>
	 * Notify error.<br>
	 * Defined as 1.
	 */
	public static final int RECV_NOTIFY_ERROR = 1;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}
	 * で利用される。 <br>
	 * Use in 
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}. <br>
	 * <br>
	 * 画像サイズ通知<br>
	 * 値は2
	 * <br>
	 * Notify image size.<br>
	 * Defined as 2.
	 */
	public static final int RECV_IMAGE_SIZE = 2;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}
	 * で利用される。 <br>
	 * Use in 
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}. <br>
	 * <br>
	 * Bufferサイズ通知<br>
	 * 値は3
	 * <br>
	 * Notify Buffer size.<br>
	 * Defined as 3.
	 */
	public static final int RECV_IMAGE_TEXTURE_SIZE = 3;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}
	 * で利用される。 <br>
	 * Use in 
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}. <br>
	 * <br>
	 * Stream開始完了通知<br>
	 * 値は4
	 * <br>
	 * Notify video streaming is started.<br>
	 * Defined as 4.
	 */
	public static final int RECV_POST_START_STREAM = 4;
	/**
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}
	 * で利用される。 <br>
	 * Use in 
	 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}. <br>
	 * <br>
	 * Stream実行状態通知<br>
	 * 値は5
	 * <br>
	 * Notify streaming status.<br>
	 * Defined as 5.
	 */
	public static final int RECV_RUNNING = 5;
}
