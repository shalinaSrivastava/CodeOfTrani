package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.Diploma.Diploma;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.PropertyClasses.DiplomaProperty;
import com.elearn.trainor.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiplomaRecyclerViewAdapter extends RecyclerView.Adapter<DiplomaRecyclerViewAdapter.MyViewHolder> {
    private List<DiplomaProperty> activeDiplomasList;
    private List<DiplomaProperty> expiredDiplomasList;
    List<DiplomaProperty> combinedList;
    Context context;
    SharedPreferenceManager spManager;
    ConnectionDetector connectionDetector;

    public DiplomaRecyclerViewAdapter(Context con, List<DiplomaProperty> activeDiplomaList, List<DiplomaProperty> expiredDiplomaList) {
        this.context = con;
        this.activeDiplomasList = activeDiplomaList;
        this.expiredDiplomasList = expiredDiplomaList;
        spManager = new SharedPreferenceManager(context);
        connectionDetector = new ConnectionDetector(context);
        combinedList = new ArrayList<DiplomaProperty>();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_diploma, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (activeDiplomasList != null) {
            bindDataOnHolder(holder, activeDiplomasList, position);
        } else if (expiredDiplomasList != null) {
            bindDataOnHolder(holder, expiredDiplomasList, position);
        }
    }

    @Override
    public int getItemCount() {
        if (expiredDiplomasList != null) {
            return expiredDiplomasList.size();
        } else if (activeDiplomasList != null) {
            return activeDiplomasList.size();
        } else {
            return 0;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView diploma_name, expiry_date;
        RelativeLayout diploma_cards, rl_root;
        LinearLayout llImage_view, diploma_row;
        ImageView imgDownloadDiploma;

        public MyViewHolder(RelativeLayout view) {
            super(view);
            diploma_cards = view;
            diploma_name = (TextView) diploma_cards.findViewById(R.id.diploma_name);
            expiry_date = (TextView) diploma_cards.findViewById(R.id.expiry_date);
            imgDownloadDiploma = (ImageView) diploma_cards.findViewById(R.id.imgDownloadDiploma);
            llImage_view = (LinearLayout) diploma_cards.findViewById(R.id.llImage_view);
            diploma_row = (LinearLayout) diploma_cards.findViewById(R.id.safetyCard_row);
            rl_root = (RelativeLayout) diploma_cards.findViewById(R.id.rl_root);
        }
    }

    public void bindDataOnHolder(MyViewHolder holder, List<DiplomaProperty> list, int pos) {
        final DiplomaProperty diplomaProperty = list.get(pos);
        try {
            if (diplomaProperty.courseName.toString().length() > 45) {
                diplomaProperty.courseName = diplomaProperty.courseName.substring(0, 43);
                diplomaProperty.courseName = diplomaProperty.courseName + "...";
            }
            holder.diploma_name.setText(diplomaProperty.courseName);
            if (diplomaProperty.diplomaStatus.equals("Completed Offline")) {
                holder.expiry_date.setText(context.getResources().getString(R.string.completed_offline));
            } else {
                if (diplomaProperty.validUntil.equals("")) {
                    if(!diplomaProperty.completionDate.equals("")){
                        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                        DateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy");
                        Date date = inputFormat.parse(diplomaProperty.completionDate);
                        String outputDateStr = outputFormat.format(date);
                        holder.expiry_date.setText(context.getResources().getString(R.string.completed_date) + " " + outputDateStr);
                    }else{
                        holder.expiry_date.setText("");
                    }
                   // holder.imgDownloadDiploma.setImageResource(R.drawable.diploma_expired);
                } else {
                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                    DateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy");
                    if (diplomaProperty.diplomaStatus.equals("expired")) {
                        Date date = inputFormat.parse(diplomaProperty.validUntil);
                        String outputDateStr = outputFormat.format(date);
                        holder.expiry_date.setText(context.getResources().getString(R.string.expired) + " " + outputDateStr);
                        holder.imgDownloadDiploma.setImageResource(R.drawable.diploma_expired);
                    } else {
                        Date date = inputFormat.parse(diplomaProperty.completionDate);
                        String outputDateStr = outputFormat.format(date);
                        holder.expiry_date.setText(context.getResources().getString(R.string.completed_date) + " " + outputDateStr);
                    }
                }
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
        holder.diploma_row.setTag(diplomaProperty);
        holder.llImage_view.setTag(diplomaProperty);
        holder.diploma_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout imgDownloadDiploma = (LinearLayout) v;
                DiplomaProperty info = (DiplomaProperty) imgDownloadDiploma.getTag();
                String lisenceId = info.licenseId;
                Diploma.getInstance().startDownloadingWithPermission(info, lisenceId);
            }
        });
        holder.llImage_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout imgDownloadDiploma = (LinearLayout) v;
                DiplomaProperty info = (DiplomaProperty) imgDownloadDiploma.getTag();
                String lisenceId = info.licenseId;
                Diploma.getInstance().startDownloadingWithPermission(info, lisenceId);
            }
        });
    }
}