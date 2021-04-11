package com.hackillionis.talktalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hackillionis.talktalk.R;
import com.hackillionis.talktalk.data.ConnectionData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ConnectionsViewHolder> {

    Context context;
    ArrayList<ConnectionData> data;
    OnConnectionRequest onConnectionRequest;

    public ConnectionsAdapter(Context context, ArrayList<ConnectionData> data, OnConnectionRequest onConnectionRequest) {
        this.context = context;
        this.data = data;
        this.onConnectionRequest = onConnectionRequest;
    }

    @NonNull
    @Override
    public ConnectionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConnectionsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_connections,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionsViewHolder holder, int position) {
        if(data.get(position).getStatus().equals("1")) {
            holder.llStatus.setVisibility(View.GONE);
        }else{
            holder.llStatus.setVisibility(View.VISIBLE);
        }
        holder.txtName.setText(data.get(position).getName());
        holder.txtEmail.setText(data.get(position).getEmail());
        holder.txtMobile.setText(data.get(position).getMobile());
        Picasso.get().load(data.get(position).getImage_link())
                .error(R.drawable.ic_user_150).into(holder.imgCR);

        holder.imgAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConnectionRequest.onConnectionAccept(data.get(position));
            }
        });

        holder.imgReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConnectionRequest.onConnectionReject(data.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ConnectionsViewHolder extends RecyclerView.ViewHolder{

        TextView txtName, txtEmail, txtMobile;
        ImageView imgCR, imgAccept, imgReject;
        LinearLayout llStatus;

        public ConnectionsViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtCRName);
            txtEmail = itemView.findViewById(R.id.txtCREmail);
            txtMobile = itemView.findViewById(R.id.txtCRMobile);
            imgCR = itemView.findViewById(R.id.imgCR);
            imgAccept = itemView.findViewById(R.id.imgCRAccept);
            imgReject = itemView.findViewById(R.id.imgCRReject);
            llStatus = itemView.findViewById(R.id.llCRStatus);
        }
    }

    public interface OnConnectionRequest{
        void onConnectionAccept(ConnectionData connectionData);
        void onConnectionReject(ConnectionData connectionData);
    }
}
