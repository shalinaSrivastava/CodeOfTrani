package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.PropertyClasses.FacilityProperty;
import com.elearn.trainor.PropertyClasses.VerifyUnverifyProperty;
import com.elearn.trainor.R;
import com.elearn.trainor.SafetyCards.ProcessVerifyInfo;
import com.elearn.trainor.SafetyCards.ReportEntry;

import java.util.List;

public class VerifiedContactFacilityAdapter extends RecyclerView.Adapter<VerifiedContactFacilityAdapter.ViewHolder> {
    Context context;
    List<VerifyUnverifyProperty> verifiedFacilityList;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    public VerifiedContactFacilityAdapter(Context context, List<VerifyUnverifyProperty> verifiedFacilityList) {
        this.context = context;
        this.verifiedFacilityList = verifiedFacilityList;
        connectionDetector = new ConnectionDetector(context);
        spManager = new SharedPreferenceManager(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout cardView = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_verified_unverified, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final VerifyUnverifyProperty property = verifiedFacilityList.get(position);
        holder.txt_type_category.setText(property.typeCategory);
        holder.txt_type_name.setText(property.typeName);
        holder.txt_customer_name.setText(property.customerName);
        holder.ll_table_row.setTag(property);
       /* holder.ll_table_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout rl = (LinearLayout) v;
                VerifyUnverifyProperty property = (VerifyUnverifyProperty) rl.getTag();
                Intent intent = new Intent(context, ProcessVerifyInfo.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("CustomerId",property.customer_id);
                intent.putExtra("Type",property.typeName);
                context.startActivity(intent);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return verifiedFacilityList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_type_category,txt_type_name,txt_customer_name;
        LinearLayout ll_table_row;
        public ViewHolder(RelativeLayout itemView) {
            super(itemView);
            ll_table_row = itemView.findViewById(R.id.ll_contact_card);
            txt_type_category = itemView.findViewById(R.id.txt_type_category);
            txt_type_name = itemView.findViewById(R.id.txt_type_name);
            txt_customer_name = itemView.findViewById(R.id.txt_customer_name);
        }
    }
}
