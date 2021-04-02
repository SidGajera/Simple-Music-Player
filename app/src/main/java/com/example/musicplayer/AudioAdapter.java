package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

    Context context;
    ArrayList<ModelAudio> audioArrayList;
    public OnItemClickListener onItemClickListener;

    public AudioAdapter(Context context, ArrayList<ModelAudio> audioArrayList) {
        this.context = context;
        this.audioArrayList = audioArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.title.setText(audioArrayList.get(position).getAudioTitle());
        holder.artist.setText(audioArrayList.get(position).getAudioArtist());
    }

    @Override
    public int getItemCount() {
        return audioArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, artist;
        ImageView whatsapp, facebook, instagram;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            artist = itemView.findViewById(R.id.artist);
            whatsapp = itemView.findViewById(R.id.whatsApp);
            facebook = itemView.findViewById(R.id.facebook);
            instagram = itemView.findViewById(R.id.instagram);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(getAdapterPosition(), v);
                }
            });

            whatsapp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String apppackage = "com.whatsapp.w4b";

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("audio/*");
                    File file = new File(String.valueOf(audioArrayList.get(getAdapterPosition()).getAudioUri()));
                    Uri uri = Uri.fromFile(file);
                    intent.setPackage(apppackage);
                    intent.putExtra(Intent.EXTRA_STREAM, audioArrayList.get(getAdapterPosition()).getAudioUri());
                    context.startActivity(Intent.createChooser(intent, "Share Audio File"));

                }
            });

            facebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("audio/*");
                    File file = new File(String.valueOf(audioArrayList.get(getAdapterPosition()).getAudioUri()));
                    Uri uri = Uri.fromFile(file);
                    intent.setPackage("com.facebook.orca");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    context.startActivity(Intent.createChooser(intent, "Share Audio File"));
                }
            });

            instagram.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("audio/mp3");
                    File file = new File(String.valueOf(audioArrayList.get(getAdapterPosition()).getAudioUri()));
                    Uri uri = Uri.fromFile(file);
                    intent.setPackage("com.instagram.android");
                    intent = context.getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    context.startActivity(Intent.createChooser(intent, "Share Audio File"));
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int pos, View v);
    }
}