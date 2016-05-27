package com.pop24.androidapp.internal_dbs;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Tomas on 19. 11. 2015.
 */
public class PointStruct {

    private LatLng latLng;
    private Float speed;
    private Integer hr;
    private Float ee;
    private Long _when;

    public PointStruct(LatLng latLng, Float speed, Integer hr, Float ee, Long _when) {
        this.latLng = latLng;
        this.speed = speed;
        this.hr = hr;
        this.ee = ee;
        this._when = _when;
    }

    public PointStruct(){}

    public Float getEe() {
        return ee;
    }

    public void setEe(Float ee) {
        this.ee = ee;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Integer getHr() {
        return hr;
    }

    public void setHr(Integer hr) {
        this.hr = hr;
    }

    public Long get_when() {
        return _when;
    }

    public void set_when(Long _when) {
        this._when = _when;
    }
}
