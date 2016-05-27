package com.pop24.androidapp.server_comunication;


import android.os.AsyncTask;
import android.util.Log;

import com.pop24.androidapp.Utility;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class UtilityAsyncRequest extends AsyncTask<String, String, Object> {

	private static final String TAG = "UtilityAsyncRequest";

	public UtilityAsyncRequest(IUtilityAsyncResponse handler) {
		this(handler, null);
	}
	public UtilityAsyncRequest(IUtilityAsyncResponse handler, Object state) {
		this.m_handler = handler;
		this.m_state = state;
	}
	

	private IUtilityAsyncResponse m_handler = null;
	private Object m_state = null;
	

    @Override
    protected Object doInBackground(String... uri) {
        Log.d(TAG, "HttpRequest0: " + uri[0]);
        final HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri[0]);

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;


        Log.d(TAG, "HttpRequest1: " + uri[0]);

        try {
            final ResponseHandler<String> responseHandler = new BasicResponseHandler();
            responseString = httpClient.execute(httpGet, responseHandler);
            return responseString;

            /*
            response = httpclient.execute(uri[0].getHttp());
            StatusLine statusLine = response.getStatusLine();
            if((statusLine.getStatusCode() == HttpStatus.SC_OK) || (statusLine.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else{
            	Log.e(TAG, String.format("HttpRequest: %s", statusLine.getStatusCode()));
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
            return responseString;
            */
        } 
        catch (IOException e) {
        	return e;
        }
    }
    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        if (this.m_handler != null) {
        	if (result != null) {
        		try {
	        		if ( result instanceof Exception) {
                        Utility.log(String.format("HttpRequest error: %s", (Exception) result));
	        			this.m_handler.setResponse(null, (Exception)result, this.m_state);
	        		}
	        		else {
                        Utility.log(String.format("HttpResponse: %s", result.toString()));
	        			this.m_handler.setResponse(result.toString(), null, this.m_state);
	        		}
        		}
    			catch (Exception ex) {
                    Utility.log(ex);
    			}
        	}
        }
    }
}
