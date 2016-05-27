package com.pop24.androidapp.internal_dbs;

/**
 * Created by Tomas on 19. 11. 2015.
 */
public class RouteLiveData {
    String name;
    Integer idRoute;
    Float avgSpeed;
    Float maxSpeed;
    Integer avgHr;
    Integer maxHr;
    Float maxEe;
    Integer elevation;
    Float distance;
    Long duration;   //if recording route > 0, otherwise is null
    Float burnedCalories = 0f;
    Integer hrZone = 0;
    String videoName;
    Long when;

    PointStruct pointStruct;

    public RouteLiveData(String name, Integer idRoute, Float avgSpeed, Float maxSpeed, Integer avgHr, Integer maxHr, Float maxEe, Integer elevation, Float distance, Long duration, Float burnedCalories, Integer hrZone, PointStruct pointStruct, String videoName, Long when) {
        this.name = name;
        this.idRoute = idRoute;
        this.avgSpeed = avgSpeed;
        this.maxSpeed = maxSpeed;
        this.avgHr = avgHr;
        this.maxHr = maxHr;
        this.maxEe = maxEe;
        this.elevation = elevation;
        this.distance = distance;
        this.duration = duration;
        this.burnedCalories = burnedCalories;
        this.hrZone = hrZone;
        this.pointStruct = pointStruct;
        this.videoName = videoName;
        this.when = when;
    }

    public Float getMaxEe() {
        return maxEe;
    }

    public void setMaxEe(Float maxEe) {
        this.maxEe = maxEe;
    }

    public Long getWhen() {
        return when;
    }

    public void setWhen(Long when) {
        this.when = when;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAvgHr() {
        return avgHr;
    }

    public void setAvgHr(Integer avgHr) {
        this.avgHr = avgHr;
    }

    public Integer getMaxHr() {
        return maxHr;
    }

    public void setMaxHr(Integer maxHr) {
        this.maxHr = maxHr;
    }

    public Integer getElevation() {
        return elevation;
    }

    public void setElevation(Integer elevation) {
        this.elevation = elevation;
    }

    public PointStruct getPointStruct() {
        return pointStruct;
    }

    public void setPointStruct(PointStruct pointStruct) {
        this.pointStruct = pointStruct;
    }

    public Integer getIdRoute() {
        return idRoute;
    }

    public void setIdRoute(Integer idRoute) {
        this.idRoute = idRoute;
    }

    public Float getBurnedCalories() {
        return burnedCalories;
    }

    public void setBurnedCalories(Float burnedCalories) {
        this.burnedCalories = burnedCalories;
    }

    public Integer getHrZone() {
        return hrZone;
    }

    public void setHrZone(Integer hrZone) {
        this.hrZone = hrZone;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public RouteLiveData() {
    }

    public Float getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(Float avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public Float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(Float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public Long getDuration() {
        return this.duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }
}
