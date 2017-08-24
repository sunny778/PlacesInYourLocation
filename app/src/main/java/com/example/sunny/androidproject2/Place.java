package com.example.sunny.androidproject2;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by jbt on 11/05/2017.
 */

public class Place implements Parcelable{

    private long id;
    private String name;
    private String placeId;
    private String address;
    private String image;
    private String phone;
    private double rating;
    private String distance;
    private double latitude;
    private double longitude;


    // Place ctors
    public Place(String name, String address, String image, double latitude, double longitude, String placeId, String distance) {
        this.name = name;
        this.address = address;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeId = placeId;
        this.distance = distance;
        this.rating = rating;
    }

    public Place(String name, String address, String image, double latitude, double longitude, long id, String placeId, String distance) {
        this.name = name;
        this.address = address;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.placeId = placeId;
        this.distance = distance;
        this.rating = rating;
    }
    // Parcelable method
    protected Place(Parcel in) {
        id = in.readLong();
        name = in.readString();
        address = in.readString();
        image = in.readString();
        distance = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }


    // Parcelable method
    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    // getters/setters
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public long getId() {
        return id;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Parcelable method
    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable method
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(placeId);
        dest.writeString(address);
        dest.writeString(image);
        dest.writeString(phone);
        dest.writeDouble(rating);
        dest.writeString(distance);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
