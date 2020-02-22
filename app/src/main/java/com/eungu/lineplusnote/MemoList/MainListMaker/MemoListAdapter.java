package com.eungu.lineplusnote.MemoList.MainListMaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eungu.lineplusnote.DBManager.DBData;
import com.eungu.lineplusnote.DBManager.DBManager;
import com.eungu.lineplusnote.StaticMethod.ImageCompute;
import com.eungu.lineplusnote.Activities.AddMemoActivity;
import com.eungu.lineplusnote.Activities.MainActivity;
import com.eungu.lineplusnote.R;

import java.util.ArrayList;

public class MemoListAdapter extends RecyclerView.Adapter<MemoListAdapter.ViewHolder> {
    private ArrayList<MemoListItem> aData = null;
    Context context;
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView text_title, text_content, text_date;
        ImageView thumbnailImage;
        ViewHolder(View itemView) {
            super(itemView) ;

            thumbnailImage = itemView.findViewById(R.id.l_item_image);
            text_title = itemView.findViewById(R.id.l_item_title);
            text_content = itemView.findViewById(R.id.l_item_content);
            text_date = itemView.findViewById(R.id.l_item_date);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), AddMemoActivity.class);
                    intent.putExtra("ADD", false);
                    intent.putExtra("idx", getAdapterPosition());
                    ((Activity)v.getContext()).startActivityForResult(intent, MainActivity.ADD_REQUEST_CODE);
                }
            });
        }
    }

    public  MemoListAdapter(Context context, ArrayList<MemoListItem> list){
        this.context = context;
        aData = list;
    }

    @NonNull
    @Override
    public MemoListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.memo_list_item, viewGroup, false);
        MemoListAdapter.ViewHolder vh = new MemoListAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final int idx = i;
        final DBManager dbManager = new DBManager(context);
        final DBData data = dbManager.getData(idx);
        if(data == null) return;

        String title = aData.get(i).getTitle();
        String content = aData.get(i).getContent();
        String time = aData.get(i).getDate();

        viewHolder.thumbnailImage.setImageBitmap(ImageCompute.getBmpFromPathWithRotate(aData.get(i).getThumbnailPath()));
        viewHolder.text_title.setText(title);
        viewHolder.text_content.setText(content);
        viewHolder.text_date.setText(time);
    }

    @Override
    public int getItemCount() {
        return aData.size();
    }
}
