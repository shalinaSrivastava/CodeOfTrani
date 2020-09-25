package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.PropertyClasses.SafetyCardProperty;
import com.elearn.trainor.R;
import com.elearn.trainor.SafetyCards.SafetyCards;

import java.util.List;

//import static com.google.android.gms.analytics.internal.zzy.v;

public class SafetyCardRecyclerViewAdapter extends RecyclerView.Adapter<SafetyCardRecyclerViewAdapter.MyViewHolder> {
    private List<SafetyCardProperty> safetyCardList;
    Context context;
    int colour, imageVisibility;

    public SafetyCardRecyclerViewAdapter(Context con, List<SafetyCardProperty> safetyCardList) {
        this.context = con;
        this.safetyCardList = safetyCardList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_safety_cards, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final SafetyCardProperty safetyCards = safetyCardList.get(position);
        safetyCards.approval_status = safetyCards.approval_status == null ? "" : safetyCards.approval_status.equals("null") ? "" : safetyCards.approval_status;

        if (safetyCards.valid_to == null || safetyCards.valid_to.equals("null")) {
            //holder.validTo.setVisibility(View.INVISIBLE);
            holder.validTo_date.setVisibility(View.INVISIBLE);
        }
        if (safetyCards.company_name.toString().length() > 45) {
            safetyCards.company_name = safetyCards.company_name.substring(0, 39);
            safetyCards.company_name = safetyCards.company_name + "...";
        }
        holder.company.setText(safetyCards.company_name);

        try {
            if (safetyCards.approval_status.equals("") || safetyCards.approval_status.equals("false")) {
                holder.approved.setText(context.getResources().getString(R.string.expired));
                holder.company.setTextColor(Color.parseColor("#C0C0C0"));
                holder.txt_loc.setTextColor(Color.parseColor("#C0C0C0"));

            } else {
                holder.approved.setText(context.getResources().getString(R.string.approved));
                holder.approved.setTextColor(Color.parseColor("#27ae60"));
                holder.txt_loc.setTextColor(Color.parseColor("#555555"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.validTo_date.setText(context.getResources().getString(R.string.valid_to)+ " "+safetyCards.valid_to);
        holder.validCardImage.setVisibility(imageVisibility);
        holder.safetyCard_row.setTag(safetyCards);
        if(safetyCards.location_name.equals("") || safetyCards.location_name.equals("null")){
            holder.txt_loc.setVisibility(View.GONE);
        }else{
            holder.txt_loc.setText(safetyCards.location_name);
            holder.txt_loc.setVisibility(View.VISIBLE);
        }


        holder.safetyCard_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.safetyCard_row.setClickable(false);
                LinearLayout imgDownloadDiploma = (LinearLayout) view;
                SafetyCardProperty info = (SafetyCardProperty) imgDownloadDiploma.getTag();
                String card_id = info.card_id;
                SafetyCards.getInstance().startDownloadingWithPermission(info, card_id, holder.safetyCard_row);
            }
        });
    }

    @Override
    public int getItemCount() {
        return safetyCardList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView company, validTo_date, validTo, approved, txt_loc;
        RelativeLayout activity_safety_cards;
        public LinearLayout safetyCard_row;
        public ImageView validCardImage;

        public MyViewHolder(RelativeLayout view) {
            super(view);
            activity_safety_cards = view;
            approved = (TextView) activity_safety_cards.findViewById(R.id.id);
            validTo_date = (TextView) activity_safety_cards.findViewById(R.id.validTo_date);
            validTo = (TextView) activity_safety_cards.findViewById(R.id.validTo);
            company = (TextView) activity_safety_cards.findViewById(R.id.company_name);
            safetyCard_row = (LinearLayout) activity_safety_cards.findViewById(R.id.safetyCard_row);
            validCardImage = (ImageView) activity_safety_cards.findViewById(R.id.validCardImage);
            txt_loc = (TextView) activity_safety_cards.findViewById(R.id.txt_loc);
        }
    }
}