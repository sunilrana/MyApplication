package com.sunilrana.myapplication;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class PlaceInfo {
    private String name;
    private String address;
    private String phonenumber;
    private String id;
    private Uri webSite;
    private LatLng latlng;
    private float rating;
    private String attribution;

    public PlaceInfo(String name, String address, String phonenumber, String id, Uri webSite,
                     LatLng latlng, float rating, String attribution) {
        this.name = name;
        this.address = address;
        this.phonenumber = phonenumber;
        this.id = id;
        this.webSite = webSite;
        this.latlng = latlng;
        this.rating = rating;
        this.attribution = attribution;
    }

    public PlaceInfo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getWebSite() {
        return webSite;
    }

    public void setWebSite(Uri webSite) {
        this.webSite = webSite;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    @Override
    public String toString() {
        return "PlaceInfo{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phonenumber='" + phonenumber + '\'' +
                ", id='" + id + '\'' +
                ", webSite=" + webSite +
                ", latlng=" + latlng +
                ", rating=" + rating +
                ", attribution='" + attribution + '\'' +
                '}';
    }
}
