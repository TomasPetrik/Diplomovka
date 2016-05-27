/*
 * Copyright (c) Infinitegra Inc, All rights reserved.
 */
package com.pop24.androidapp.otg;

/**
 * {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}
 * で {@link infinitegra.usb.video.UsbVideoConstructs#RECV_NOTIFY_ERROR
 * RECV_NOTIFY_ERROR(1)}が呼び出された際の
 * エラーメッセージの一覧
 * <br><br>
 * The list of error message when called 
 * {@link infinitegra.usb.video.UsbVideoConstructs#RECV_NOTIFY_ERROR RECV_NOTIFY_ERROR(1)} 
 * in {@link infinitegra.usb.video.IUsbVideoCallback#onNotifyMessage(int, int, int, String)}
 */
public class UsbVideoErrorID
{
	/**
	 * Stream開始時に失敗したエラー <br>
	 * Error if failed to start streaming. <br>
	 * <br>
	 * 値は1<br>
	 * Defined as 1.
	 */
	public static final int NOTIFY_ERROR_STREAM_START_EXCEPTION = 1;
	/**
	 * USBカメラデバイスの準備中 <br>
	 * Error if USB camera device is not ready yet. <br><br>
	 * 少し待って再実行をする必要がある<br>
	 * 値は2
	 * <br>
	 * Should retry after waiting for a moment.<br>
	 * Defined as 2.
	 */
	public static final int NOTIFY_ERROR_DEVICE_PREPARE = 2;
	/**
	 * USBカメラデバイスの開始に失敗 <br>
	 * Error if failed to start USB camera device. <br><br>
	 * 値は3
	 * <br>
	 * Defined as 3.
	 */
	public static final int NOTIFY_ERROR_START_DEVICE = 3;
	/**
	 * USBカメラデバイスの終了に失敗 <br>
	 * Error if failed to stop USB camera device. <br><br>
	 * 値は4
	 * <br>
	 * Defined as 4.
	 */
	public static final int NOTIFY_ERROR_CLOSE_DEVICE = 4;
	/**
	 * USBカメラ映像の取得中の失敗 <br>
	 * Error if failed to capture video data. <br><br>
	 * 値は5
	 * <br>
	 * Defined as 5.
	 */
	public static final int NOTIFY_ERROR_USB_EXCEPTION = 5;
	/**
	 * 無料版での実行回数制限を超えた場合のエラー <br>
	 * Error if exceed the number of times for start limitation. <br><br>
	 * 値は6
	 * <br>
	 * Defined as 6.
	 */
	public static final int NOTIFY_ERROR_TRIAL_EXECUTE_OVERFLOW = 6;
	/**
	 * 無料版での利用制限時間が過ぎた場合のエラー <br>
	 * Error if exceed the duration for displaying video in trial version. <br><br>
	 * 値は7
	 * <br>
	 * Defined as 7.
	 */
	public static final int NOTIFY_ERROR_OUT_OF_PERIOD = 7;
	/**
	 * USB通信の転送エラー <br>
	 * Error if occurred transfer error in USB transaction. <br><br>
	 * 値は8
	 * <br>
	 * Defined as 8.
	 */
	public static final int NOTIFY_ERROR_TRANSFER = 8;
	/**
	 * 非サポートデバイス<br>
	 * Error if connected device is not supported. <br><br>
	 * 接続されているUSBカメラデバイスがサポート対象外 <br>
	 * 値は9
	 * <br>
	 * Defined as 9.
	 */
	public static final int NOTIFY_ERROR_UNSUPPORTED_DEVICE = 9;
	/**
	 * 録画を保存するディレクトリが不正 <br>
	 * Error if specified recording directory is invalid. <br><br>
	 * 値は100
	 * <br>
	 * Defined as 100.
	 */
	public static final int NOTIFY_ERROR_INVALID_RECORD_DIRECTORY = 100;
	/**
	 * 録画を保存するデフォルトのディレクトリを作成できない <br>
	 * Error if failed to make default directory for recording. <br><br>
	 * 値は101
	 * <br>
	 * Defined as 101.
	 */
	public static final int NOTIFY_ERROR_DEFAULT_DIR_RECORD_CREATE = 101;
	/**
	 * 録画するファイル名が不正(値が設定されていない、既に存在している等) <br>
	 * Error if the recording file name is invalid.(no file name, already exist, etc). <br><br>
	 * 値は102
	 * <br>
	 * Defined as 102.
	 */
	public static final int NOTIFY_ERROR_INVALID_RECORD_FILE_NAME = 102;
	/**
	 * 録画するファイルへの書き込みができない <br>
	 * Error if failed to write recording file. <br><br>
	 * 値は103
	 * <br>
	 * Defined as 103.
	 */
	public static final int NOTIFY_ERROR_FILE_CAN_NOT_WRITE = 103;
	/**
	 * 録画時にUSBカメラが止まっているため録画できない <br>
	 * Error if failed to record due to USB camera is stopping. <br><br>
	 * 値は104
	 * <br>
	 * Defined as 104.
	 */
	public static final int NOTIFY_ERROR_STOP_USB_VIDEO = 104;
	/**
	 * 録画開始時に発生するエラー <br>
	 * Error if failed at recording start. <br><br>
	 * 値は105
	 * <br>
	 * Defined as 105.
	 */
	public static final int NOTIFY_ERROR_RECORD_START = 105;
	/**
	 * 既に録画が開始されている場合に発生するエラー <br>
	 * Error if recording is already started. <br><br>
	 * 値は106
	 * <br>
	 * Defined as 106.
	 */
	public static final int NOTIFY_ERROR_RECORD_STARTED = 106;
	/**
	 * 録画するファイル名の長さが大きすぎるエラー <br>
	 * Error if recording file name is too long. <br><br>
	 * 値は107
	 * <br>
	 * Defined as 107.
	 */
	public static final int NOTIFY_ERROR_RECORDING_NAME_LENGTH_OVER = 107;
	/**
	 * 録画するディレクトリ名の長さが大きすぎるエラー <br>
	 * Error if directory name for recording file is too long. <br><br>
	 * 値は108
	 * <br>
	 * Defined as 108.
	 */
	public static final int NOTIFY_ERROR_DIRECTORY_LENGTH_OVER = 108;
	/**
	 * 録画ファイル名に不正な文字が含まれている <br>
	 * Error if recording file name has an illegal character. <br><br>
	 * 値は109
	 * <br>
	 * Defined as 109.
	 */
	public static final int NOTIFY_ERROR_INVALID_FILE_NAME = 109;

	/**
	 * その他のエラー <br>
	 * Other errors. <br><br>
	 * 値は1000
	 * <br>
	 * Defined as 1000.
	 */
	public static final int NOTIFY_ERROR_OTHER = 1000;

}
