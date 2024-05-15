package com.example.nearbynest;

import android.annotation.SuppressLint;
import android.app.LauncherActivity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<Integer> iconResIds;
    private List<String> texts;
    private List<Boolean> switchStates;
    private Context context;


    public ListAdapter(List<Integer> iconResIds, List<String> texts, List<Boolean> switchStates, Context context) {
        this.iconResIds = iconResIds;
        this.texts = texts;
        this.context = context;
        this.switchStates = switchStates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.icon.setImageResource(iconResIds.get(position));
        holder.text.setText(texts.get(position));
        holder.toggleSwitch.setChecked(switchStates.get(position));

        holder.toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Toggle switch is checked, update TextView text to "On"
                    holder.text.setText(texts.get(position) + " On");
                    holder.toggleSwitch.setThumbResource(R.drawable.switch_thumb_on);

                    //activate the geofences again:

                } else {
                    // Toggle switch is unchecked, update TextView text to "Off"
                    holder.text.setText(texts.get(position) + " Off");
                    holder.toggleSwitch.setThumbResource(R.drawable.switch_thumb_off);

                    //remove the geofences on it:
                    List<String> geofenceIds = new ArrayList<>();
                    if(position == 0){
                        geofenceIds.add("2525 S King Dr, Chicago");
                        geofenceIds.add("E 35th St");
                    }
                    else if(position == 1){
                        geofenceIds.add("3506 S State St, Chicago");
                    }
                    Geofencing geofencing = new Geofencing();
                    geofencing.removeAllGeofences(geofenceIds, context);
                }
            }
        });
    }

    public void updateList(int iconResId, String text, boolean switchState){
        iconResIds.add(iconResId);
        texts.add(text);
        switchStates.add(switchState);
        notifyItemInserted(iconResIds.size() - 1);
    }

    public void updateSwitchState(int position, boolean isChecked) {
        switchStates.set(position, isChecked);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return texts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView text;
        Switch toggleSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            text = itemView.findViewById(R.id.text);
            toggleSwitch = itemView.findViewById(R.id.toggleSwitch);
        }
    }
}
