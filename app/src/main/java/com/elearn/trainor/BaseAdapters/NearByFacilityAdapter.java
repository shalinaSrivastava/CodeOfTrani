package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.elearn.trainor.DBHandler.DataBaseHandlerInsert;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.PropertyClasses.FacilityProperty;
import com.elearn.trainor.R;
import com.elearn.trainor.SafetyCards.AwaitingApproval;
import com.elearn.trainor.SafetyCards.CheckedInFacility;
import com.elearn.trainor.SafetyCards.ReportEntry;

import java.util.List;

public class NearByFacilityAdapter extends RecyclerView.Adapter<NearByFacilityAdapter.ViewHolder> {
    Context context;
    List<FacilityProperty> nearByFacilityList;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerUpdate dbUpdate;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    public NearByFacilityAdapter(Context context, List<FacilityProperty> nearByFacilityList) {
        this.context = context;
        this.nearByFacilityList = nearByFacilityList;
        dbSelect = new DataBaseHandlerSelect(context);
        dbInsert = new DataBaseHandlerInsert(context);
        dbUpdate = new DataBaseHandlerUpdate(context);
        connectionDetector = new ConnectionDetector(context);
        spManager = new SharedPreferenceManager(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout cardView = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_search_by_latlong, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FacilityProperty property = nearByFacilityList.get(position);
        holder.txt_facility_name.setText(property.name);
        holder.txt_company_name.setText(property.customerName);
        //holder.txt_tool_name.setText(property.name);
        holder.ll_table_row.setTag(property);
        holder.ll_table_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout rl = (LinearLayout) v;
                FacilityProperty property = (FacilityProperty) rl.getTag();
                if(property.employeeCheckInState.equals("checked_in")){
                    Intent intent = new Intent(context, CheckedInFacility.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }else{
                    Intent intent = new Intent(context, ReportEntry.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("CompanyName",property.customerName);
                    intent.putExtra("FacilityName",property.name);
                    intent.putExtra("FacilityId", property.id);
                    intent.putExtra("FacilityCustomerId", property.customerId);
                    intent.putExtra("AllowGuest", property.allowGuests);
                    intent.putExtra("RequireProjectNum", property.requireProjectNumber); //added on 02-03-2021
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return nearByFacilityList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_facility_name,txt_company_name;
        LinearLayout ll_table_row;
        public ViewHolder(RelativeLayout itemView) {
            super(itemView);
            ll_table_row = itemView.findViewById(R.id.ll_facility_card);
            txt_facility_name = itemView.findViewById(R.id.txt_facility_name);
            txt_company_name = itemView.findViewById(R.id.txt_company_name);
        }
    }
}
