package com.example.connectify.updates;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectify.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImagesAdapter extends  RecyclerView.Adapter<ImagesAdapter.ViewHolder>{

    List<Uri> imagesUrl;

    public ImagesAdapter(List<Uri> imagesUrl) {
        this.imagesUrl = imagesUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(imagesUrl.get(position)).into(holder.mImage);
    }

    @Override
    public int getItemCount() {
        return imagesUrl.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.thumbnail);
        }
    }


}
