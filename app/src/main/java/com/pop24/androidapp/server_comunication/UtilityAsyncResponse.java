package com.pop24.androidapp.server_comunication;


public abstract class UtilityAsyncResponse implements IUtilityAsyncResponse {
	public abstract void setResponse(String response, Exception ex, Object state);
}
