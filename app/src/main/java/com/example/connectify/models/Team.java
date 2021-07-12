package com.example.connectify.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.annotations.Nullable;

public class Team implements Parcelable {

    private String teamCode;
    private String teamName;
    private String teamCreatorId;
    private String teamDesc;
    private String teamLocation;

    @Nullable
    private String teamProfilePicture;

    public Team() {
    }

    public Team(String teamCode, String teamName, String teamCreatorId, String teamDesc, String teamLocation, String teamProfilePicture) {
        this.teamCode = teamCode;
        this.teamName = teamName;
        this.teamCreatorId = teamCreatorId;
        this.teamDesc = teamDesc;
        this.teamLocation = teamLocation;
        this.teamProfilePicture = teamProfilePicture;
    }

    protected Team(Parcel in) {
        teamCode = in.readString();
        teamName = in.readString();
        teamCreatorId = in.readString();
        teamDesc = in.readString();
        teamLocation = in.readString();
        teamProfilePicture = in.readString();
    }

    public static final Creator<Team> CREATOR = new Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel in) {
            return new Team(in);
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }
    };

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamCreatorId() {
        return teamCreatorId;
    }

    public void setTeamCreatorId(String teamCreatorId) {
        this.teamCreatorId = teamCreatorId;
    }

    public String getTeamDesc() {
        return teamDesc;
    }

    public void setTeamDesc(String teamDesc) {
        this.teamDesc = teamDesc;
    }

    public String getTeamLocation() {
        return teamLocation;
    }

    public void setTeamLocation(String teamLocation) {
        this.teamLocation = teamLocation;
    }

    public String getTeamProfilePicture() {
        return teamProfilePicture;
    }

    public void setTeamProfilePicture(String teamProfilePicture) {
        this.teamProfilePicture = teamProfilePicture;
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamCode='" + teamCode + '\'' +
                ", teamName='" + teamName + '\'' +
                ", teamCreatorId='" + teamCreatorId + '\'' +
                ", teamDesc='" + teamDesc + '\'' +
                ", teamLocation='" + teamLocation + '\'' +
                ", teamProfilePicture='" + teamProfilePicture + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(teamCode);
        dest.writeString(teamName);
        dest.writeString(teamCreatorId);
        dest.writeString(teamDesc);
        dest.writeString(teamLocation);
        dest.writeString(teamProfilePicture);
    }
}
