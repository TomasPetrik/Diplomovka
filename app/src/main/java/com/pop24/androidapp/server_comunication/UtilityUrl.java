package com.pop24.androidapp.server_comunication;


import com.pop24.androidapp.Utility;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class UtilityUrl {

    public UtilityUrl(String url) {
        this(url, false);
    }
    public UtilityUrl(String url, boolean isPost) {
        this.m_url = url;
        this.m_isPost = isPost;
        this.m_parameters = new ArrayList<BasicNameValuePair>();
    }

    private boolean m_isPost = false;
    private String m_url = null;
    private ArrayList<BasicNameValuePair> m_parameters = null;
    private String m_data = null;

    public void add(String data) {
        this.m_data = data;
    }
    public void add(String key, String value) {
        this.m_parameters.add(new BasicNameValuePair(key, value));
    }
    public HttpUriRequest getHttp() {
        if (this.m_isPost) {
            return this.getHttpPost();
        }
        else {
            return this.getHttpGet();
        }
    }
    public HttpGet getHttpGet() {
        String params = URLEncodedUtils.format(this.m_parameters, HTTP.UTF_8);
        HttpGet get = new HttpGet(String.format("%s?%s", this.m_url, params));

        /*
        for (BasicNameValuePair field : this.m_headerFields) {
            get.addHeader(field.getName(), field.getValue());
        }
        */

        return get;
    }
    public HttpPost getHttpPost() {
        HttpPost post = new HttpPost(this.m_url);
        try {
            if (this.m_data != null) {
                StringEntity entity = new StringEntity(this.m_data.toString(), HTTP.UTF_8);
                post.setEntity(entity);
            }
            else {
                post.setEntity(new UrlEncodedFormEntity(this.m_parameters, HTTP.UTF_8));
            }
        } catch (UnsupportedEncodingException e) {
            Utility.log(e);
        }
        return post;
    }

    public String getParam(int index){
        return this.m_parameters.get(index).getValue();
    }

    public String getUrl(){
        return this.m_url;
    }
}
