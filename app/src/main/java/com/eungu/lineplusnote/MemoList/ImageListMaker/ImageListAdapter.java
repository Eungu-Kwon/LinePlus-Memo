package com.eungu.lineplusnote.MemoList.ImageListMaker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eungu.lineplusnote.ImageCompute;
import com.eungu.lineplusnote.MemoList.ImageViewActivity;
import com.eungu.lineplusnote.R;

import java.io.File;
import java.util.ArrayList;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder>{
    private ArrayList<File> aData = null;
    Context context;
    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv;
        ViewHolder(final View itemView) {
            super(itemView) ;

            iv = itemView.findViewById(R.id.image_list_item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), ImageViewActivity.class);
                    i.putExtra("path", aData.get(getAdapterPosition()).getAbsolutePath());
                    v.getContext().startActivity(i);
                }
            });
        }
    }

    public  ImageListAdapter(Context context, ArrayList<File> list){
        this.context = context;
        aData = list;
    }

    @NonNull
    @Override
    public ImageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.image_list_item, viewGroup, false);
        ImageListAdapter.ViewHolder vh = new ImageListAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageListAdapter.ViewHolder viewHolder, int i) {
        viewHolder.iv.setImageBitmap(ImageCompute.getBmpFromUriWithResize(aData.get(i).getAbsolutePath(), 100));
    }

    @Override
    public int getItemCount() {
        return aData.size();
    }
}
