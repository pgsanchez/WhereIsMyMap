package com.pgsanchez.whereismymap.presentation;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgsanchez.whereismymap.R;
import com.pgsanchez.whereismymap.domain.Map;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemMapAdapter extends RecyclerView.Adapter<ItemMapAdapter.ViewHolder>{
    private List<Map> mapList; // lista de mapas que vamos a mostrar
    File imgsPath;

    public ItemMapAdapter(List<Map> miLista, File imgsPath) {
        this.imgsPath = imgsPath;
        this.mapList = miLista;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageMap;
        public TextView tvNameItem, tvDistanceItem, tvCategoryItem, tvRaceDateItem;


        public ViewHolder(View itemView) {
            super(itemView);
            imageMap = itemView.findViewById(R.id.imageMap);
            tvNameItem = itemView.findViewById(R.id.tvNameItem);
            tvDistanceItem = itemView.findViewById(R.id.tvDistanceItem);
            tvCategoryItem = itemView.findViewById(R.id.tvCategoryItem);
            tvRaceDateItem = itemView.findViewById(R.id.tvRaceDateItem);
        }

        public void personaliza(Map myMap, File imgsPath){
            imageMap.setImageURI(Uri.parse(imgsPath + "/" + myMap.getImgFileName()));
            //imageMap.setImageURI(null);
            tvNameItem.setText(myMap.getName());
            tvDistanceItem.setText(myMap.getDistance());
            tvCategoryItem.setText(myMap.getCategory());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            tvRaceDateItem.setText(dateFormat.format(myMap.getRaceDate()));
        }
    }

    @NonNull
    @Override
    public ItemMapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maps_list, parent, false);
        v.setOnClickListener(onClickListener);
        return new ItemMapAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemMapAdapter.ViewHolder holder, int position) {
        Map map = mapList.get(position);
        holder.personaliza(map, imgsPath);
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    protected View.OnClickListener onClickListener;
    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
