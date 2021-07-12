package com.example.connectify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.models.ApplicationUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TeamMembersAdapter extends RecyclerView.Adapter<TeamMembersAdapter.ViewHolder> {

    List<ApplicationUser> teamsMembers;
    OnItemClickListener mListener;

    public TeamMembersAdapter(List<ApplicationUser> teamsMembers) {
        this.teamsMembers = teamsMembers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.team_member_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationUser applicationUser = teamsMembers.get(position);
        Picasso.get().load(applicationUser.getProfileImageUri()).placeholder(R.drawable.profile_holder).into(holder.mTeamProfilePicture);
        String teamName = applicationUser.getFirstName() + " " + applicationUser.getLastName();
        String[] strArray = teamName.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
        holder.mTeamName.setText(builder.toString());

    }

    @Override
    public int getItemCount() {
        return teamsMembers.size();
    }

    public interface  OnItemClickListener{
        void itemClickListener(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView mTeamProfilePicture;
        TextView mTeamName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTeamProfilePicture = itemView.findViewById(R.id.team_profile);
            mTeamName = itemView.findViewById(R.id.team_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null){
                        int itemPosition = getAdapterPosition();
                        if (itemPosition != RecyclerView.NO_POSITION){
                            mListener.itemClickListener(itemPosition);
                        }
                    }
                }
            });

        }
    }

}
