package com.example.connectify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MenuRecyclerViewAdapter extends RecyclerView.Adapter<MenuRecyclerViewAdapter.ViewHolder> {

    Context context;
    String[] itemText;
    int[] itemIcons;
    OnItemClickListener mOnItemClickListener;

    public MenuRecyclerViewAdapter(Context context, String[] itemText, int[] itemIcons) {
        this.context = context;
        this.itemText = itemText;
        this.itemIcons = itemIcons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.icon.setImageResource(itemIcons[position]);
        holder.textView.setText(itemText[position]);

    }



    @Override
    public int getItemCount() {
        return itemText.length;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView textView;
        RelativeLayout mRelayRelativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.list_item_icon);
            textView = itemView.findViewById(R.id.item_text);
            mRelayRelativeLayout = itemView.findViewById(R.id.menu_layout);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        int itemPosition = getAdapterPosition();
                        if (itemPosition != RecyclerView.NO_POSITION) {
                            mOnItemClickListener.onItemClick(itemPosition);
                        }
                    }

                }
            });
        }
    }

}
