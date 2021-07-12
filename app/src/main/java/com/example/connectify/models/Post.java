package com.example.connectify.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Post implements Parcelable {

    @ServerTimestamp
    private Date date;
    private String postText;
    @Nullable
    private List<String> postImagesUrl;
    private String postedOn;
    private String postedBy;

    public Post() {
    }

    public Post(String postText, List<String> postImagesUrl, String postedOn, String postedBy) {
        this.postText = postText;
        this.postImagesUrl = postImagesUrl;
        this.postedOn = postedOn;
        this.postedBy = postedBy;
    }



    protected Post(Parcel in) {
        postText = in.readString();
        postImagesUrl = in.createStringArrayList();
        postedOn = in.readString();
        postedBy = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public List<String> getPostImagesUrl() {
        return postImagesUrl;
    }

    public void setPostImagesUrl(List<String> postImagesUrl) {
        this.postImagesUrl = postImagesUrl;
    }

    public String getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(String postedOn) {
        this.postedOn = postedOn;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postText='" + postText + '\'' +
                ", postImagesUrl=" + postImagesUrl +
                ", postedOn='" + postedOn + '\'' +
                ", postedBy='" + postedBy + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postText);
        dest.writeStringList(postImagesUrl);
        dest.writeString(postedOn);
        dest.writeString(postedBy);
    }
}
