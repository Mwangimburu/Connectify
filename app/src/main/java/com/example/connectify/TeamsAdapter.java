package com.example.connectify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.models.Team;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TeamsAdapter extends RecyclerView.Adapter<TeamsAdapter.ViewHolder> {

    List<Team> teamList;
    OnItemClickListener onItemClickListener;

    public TeamsAdapter(List<Team> teamList) {
        this.teamList = teamList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_team_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Team currentPosition = teamList.get(position);
        Picasso.get().load(currentPosition.getTeamProfilePicture()).placeholder(R.drawable.round_people).into(holder.mTeamProfile);
        holder.mTeamName.setText(currentPosition.getTeamName());
        holder.mTeamLocation.setText(currentPosition.getTeamLocation());
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    public interface OnItemClickListener {
        void itemClickListener(int position);
    }

    public void setItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView mTeamProfile;
        TextView mTeamName, mTeamLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTeamProfile = itemView.findViewById(R.id.team_profile);
            mTeamName = itemView.findViewById(R.id.team_name);
            mTeamLocation = itemView.findViewById(R.id.location);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int itemPosition = getAdapterPosition();
                        if (itemPosition != RecyclerView.NO_POSITION) {
                            onItemClickListener.itemClickListener(itemPosition);
                        }
                    }
                }
            });

        }
    }


}
