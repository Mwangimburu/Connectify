package com.example.connectify.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.annotations.Nullable;

public class ApplicationUser implements Parcelable {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String accountType;
    private String county;
    private String constituency;
    private String ward;

    @Nullable
    private String profileImageUri;


    public ApplicationUser() {
    }

    public ApplicationUser(String userId,
                           String firstName,
                           String lastName,
                           String accountType,
                           String county,
                           String constituency,
                           String ward, @Nullable String profileImageUri) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountType = accountType;
        this.county = county;
        this.constituency = constituency;
        this.ward = ward;
        this.profileImageUri = profileImageUri;
    }

    protected ApplicationUser(Parcel in) {
        userId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        accountType = in.readString();
        county = in.readString();
        constituency = in.readString();
        ward = in.readString();
        profileImageUri = in.readString();
    }

    public static final Creator<ApplicationUser> CREATOR = new Creator<ApplicationUser>() {
        @Override
        public ApplicationUser createFromParcel(Parcel in) {
            return new ApplicationUser(in);
        }

        @Override
        public ApplicationUser[] newArray(int size) {
            return new ApplicationUser[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getConstituency() {
        return constituency;
    }

    public void setConstituency(String constituency) {
        this.constituency = constituency;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    @Override
    public String toString() {
        return "ApplicationUser{" +
                "userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", county='" + county + '\'' +
                ", constituency='" + constituency + '\'' +
                ", ward='" + ward + '\'' +
                ", profileImageUri='" + profileImageUri + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(accountType);
        dest.writeString(county);
        dest.writeString(constituency);
        dest.writeString(ward);
        dest.writeString(profileImageUri);
    }
}
