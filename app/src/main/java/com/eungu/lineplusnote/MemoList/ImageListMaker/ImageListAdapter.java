package com.eungu.lineplusnote.MemoList.ImageListMaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eungu.lineplusnote.R;

import java.util.ArrayList;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder>{
    private ArrayList<ImageListItem> aData = null;
    Context context;
    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv;
        ViewHolder(View itemView) {
            super(itemView) ;

            iv = itemView.findViewById(R.id.image_list_item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    public  ImageListAdapter(Context context, ArrayList<ImageListItem> list){
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
////        viewHolder.iv.setImageBitmap();
//        final int idx = i;
//        final DBManager dbManager = new DBManager(context);
//        final DBData data = dbManager.getData(idx);
//        if(data == null) return;
//        String title = aData.get(i).getTitle();
//        String content = aData.get(i).getContent();
//        String time = aData.get(i).getDate();
//        viewHolder.text_title.setText(title);
//        viewHolder.text_content.setText(content);
//        viewHolder.text_date.setText(time);
    }

    @Override
    public int getItemCount() {
        return aData.size();
    }
}
