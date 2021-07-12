package com.example.connectify.updates;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.R;
import com.example.connectify.models.ApplicationUser;
import com.example.connectify.models.Post;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {
    private static final String TAG = "PostsAdapter";
    List<Post> mPostsList;
    ImageView imageView;
    ProgressBar progressBar;
    TextView textView;
    int i = 0;
    ApplicationUser applicationUser;
    onCardLickListener cardLickListener;

    public PostsAdapter(List<Post> mPostsList) {
        this.mPostsList = mPostsList;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mUsersReference = mFirebaseDatabase.getReference("users");
        Post post = mPostsList.get(position);
        holder.mTime.setText(post.getPostedOn());

        int i = 0;
        int j = 0;

        if (post.getPostImagesUrl() == null) {
            //View image = holder.mPostImage;
            //((ViewGroup)image.getParent()).removeView(image);
            holder.mPostText.setText(post.getPostText());


//            holder.mImagesNumber.setText(null);
//            Picasso.get().load(R.drawable.no_image).placeholder(R.drawable.no_image).into(holder.mPostImage);
//            holder.mPostTextNoImage.setText(post.getPostText());
        } else {
            if (holder.mRelativeLayout.findViewById(i) == null) {
                holder.mPostText.setText(post.getPostText());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(10, 0, 0, 0);
                textView = new TextView(holder.mRelativeLayout.getContext());
                textView.setLayoutParams(layoutParams);
                holder.mRelativeLayout.addView(textView);

                imageView = new ImageView(holder.mLayout.getContext());
                imageView.setId(i);
                holder.mLayout.addView(imageView, 400, 250);

                if (holder.mRelativeLayoutProgress.findViewById(j) == null) {
                    progressBar = new ProgressBar(holder.mLayout.getContext());
                    progressBar.setId(j);
                    progressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#4285F4")));
                    holder.mRelativeLayoutProgress.addView(progressBar, 30, 30);
                }
                Picasso.get().load(post.getPostImagesUrl().get(0)).fit().centerCrop().into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.mRelativeLayoutProgress.findViewById(j).setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
                int noImages = post.getPostImagesUrl().size();
                int extraImages = noImages - 1;
                if (extraImages == 0) {
                    textView.setText(null);
                } else {
                    String number = "+" + extraImages;
                    textView.setText(number);
                }

            }

            //holder.mPostTextNoImage.setText(null);


        }

        mUsersReference.child(post.getPostedBy()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applicationUser = snapshot.getValue(ApplicationUser.class);
                Picasso.get().load(applicationUser.getProfileImageUri()).placeholder(R.drawable.profile_holder).into(holder.mProfile);
                String names = applicationUser.getFirstName() + " " + applicationUser.getLastName();
                String[] strArray = names.split(" ");
                StringBuilder builder = new StringBuilder();
                for (String s : strArray) {
                    String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
                    builder.append(cap + " ");
                }
                holder.mName.setText(builder.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    @Override
    public int getItemCount() {
        return mPostsList.size();
    }

    public interface onCardLickListener{
        void onCardClick(int position);
    }

    public void setOnCardClickListener(onCardLickListener onCardClickListener){
        cardLickListener = onCardClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName, mTime, mPostText, mPostTextNoImage, mImagesNumber;
        CircleImageView mProfile;
        MaterialCardView mPostCardView;
        LinearLayout mLayout, mRelativeLayout, mRelativeLayoutProgress;
        //ImageView mPostImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.name);
            mTime = itemView.findViewById(R.id.time);
            mPostText = itemView.findViewById(R.id.text);
            // mPostTextNoImage = itemView.findViewById(R.id.text_no_image);
            mProfile = itemView.findViewById(R.id.post_creator_profile);
            mPostCardView = itemView.findViewById(R.id.post_card_layout);
            // mPostImage = itemView.findViewById(R.id.thumbnail);
            // mImagesNumber = itemView.findViewById(R.id.number);
            mLayout = itemView.findViewById(R.id.linear_layout);
            mRelativeLayout = itemView.findViewById(R.id.relative_layout);
            mRelativeLayoutProgress = itemView.findViewById(R.id.progress_bar);
            mPostCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cardLickListener != null) {
                        int itemPosition = getAdapterPosition();
                        if (itemPosition != RecyclerView.NO_POSITION) {
                            cardLickListener.onCardClick(itemPosition);
                        }
                    }
                }
            });
        }
    }

}
