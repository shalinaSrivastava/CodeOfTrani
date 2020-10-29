package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.elearn.trainor.PropertyClasses.ReportEntryProperty;
import com.elearn.trainor.R;
import com.elearn.trainor.SafetyCards.CheckedInFacility;
import com.elearn.trainor.SafetyCards.NotifyExit;
import com.elearn.trainor.SafetyCards.ReportEntry;
import com.elearn.trainor.SafetyCards.UpdateHours;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckedInFacilityAdapter extends RecyclerView.Adapter<CheckedInFacilityAdapter.ViewHolder> {
    Context context;
    List<ReportEntryProperty> checkedInFacilityList;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerUpdate dbUpdate;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;

    public CheckedInFacilityAdapter(Context context, List<ReportEntryProperty> checkedInFacilityList) {
        this.context = context;
        this.checkedInFacilityList = checkedInFacilityList;
        dbSelect = new DataBaseHandlerSelect(context);
        dbInsert = new DataBaseHandlerInsert(context);
        dbUpdate = new DataBaseHandlerUpdate(context);
        connectionDetector = new ConnectionDetector(context);
        spManager = new SharedPreferenceManager(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout cardView = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_checked_in_facilities, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ReportEntryProperty property = checkedInFacilityList.get(position);

        /*long timeStampSeconds = seconds(property.timestamp);
        long durationSeconds = Long.parseLong(property.estimatedDurationOfVisitInSeconds);
        long checkOutDurationSeconds = timeStampSeconds+durationSeconds;
        //long currentTimeMillis = System.currentTimeMillis()/1000;
        long leftTime = (checkOutDurationSeconds - durationSeconds)*1000;
        int mins = (int) ((leftTime/(1000*60)) % 60);
        System.out.println("Spent minute = "+mins+"");*/
        String spentTime = spentTime(property.timestamp);
        holder.txt_hour_spent.setText(spentTime+context.getString(R.string.since_entry));
        String guestCount = "0";
        if (property.numberOfGuests.equals("0") || property.numberOfGuests.equals("") || property.numberOfGuests.equals("null")) {
            holder.txt_guest.setVisibility(View.GONE);
        } else {
            holder.txt_guest.setVisibility(View.VISIBLE);
            if (property.numberOfGuests.equals("1")) {
                guestCount = property.numberOfGuests + " guest";
            } else {
                guestCount = property.numberOfGuests + " guests";
            }
            holder.txt_guest.setText(guestCount);
        }
        holder.txt_facility_name.setText(property.facilityName);
        if (property.securityServicePhone.equals("0") || property.securityServicePhone.equals("") || property.securityServicePhone.equals("null")) {
            holder.txt_security_contact.setVisibility(View.GONE);
        } else {
            holder.txt_security_contact.setVisibility(View.VISIBLE);
            holder.txt_security_contact.setText(context.getString(R.string.phone_number_security) + property.securityServicePhone);
        }
        holder.rl_extend_time_layout.setTag(property);
        holder.rl_notify_exit.setTag(property);
        holder.rl_extend_time_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout rl = (RelativeLayout) v;
                ReportEntryProperty property = (ReportEntryProperty) rl.getTag();
                String spentTime = spentTime(property.timestamp);
                String leftTime = leftTime(property.timestamp,property.estimatedDurationOfVisitInSeconds);
                Intent intent = new Intent(context, UpdateHours.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("FacilityName", property.facilityName);
                intent.putExtra("EntryId", property.entryId);
                intent.putExtra("SpentTime", spentTime);
                intent.putExtra("LeftTime", leftTime);
                intent.putExtra("ActualDuration",property.estimatedDurationOfVisitInSeconds);
                context.startActivity(intent);
            }
        });
        holder.rl_notify_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout rl = (RelativeLayout) view;
                ReportEntryProperty property = (ReportEntryProperty) rl.getTag();
                String spentTime = spentTime(property.timestamp);
                Intent intent = new Intent(context, NotifyExit.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("FacilityName", property.facilityName);
                intent.putExtra("EntryId", property.entryId);
                intent.putExtra("SpentTime", spentTime);
                intent.putExtra("Latitude", property.facilityLatitude);
                intent.putExtra("Longitude", property.facilityLongitude);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkedInFacilityList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rl_extend_time_layout, rl_notify_exit;
        TextView txt_facility_name, txt_hour_spent, txt_guest, txt_security_contact;

        public ViewHolder(RelativeLayout itemView) {
            super(itemView);
            rl_extend_time_layout = itemView.findViewById(R.id.rl_extend_time_layout);
            txt_hour_spent = itemView.findViewById(R.id.txt_hour_spent);
            txt_facility_name = itemView.findViewById(R.id.txt_companyName);
            txt_guest = itemView.findViewById(R.id.txt_guest);
            txt_security_contact = itemView.findViewById(R.id.txt_security_contact);
            rl_notify_exit = itemView.findViewById(R.id.rl_notify_exit);
        }
    }

    public String leftTime(String checkInTime, String workDuration) {
        String leftTime = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
        try {
            Date mDate = sdf.parse(checkInTime);
            long durationMillis = Long.parseLong(workDuration)*1000;
            long toBeCompletedMillis = durationMillis + mDate.getTime();
            Date currentDate = sdf.parse(sdf.format(new Date()));
            long differenceMillis = toBeCompletedMillis - currentDate.getTime();
            long mins = (differenceMillis / (1000 * 60)) % 60;
            long difference_In_Hours = (differenceMillis / (1000 * 60 * 60)) % 24;
            leftTime = difference_In_Hours + "," + mins +"";
           /* if (!(difference_In_Hours + "").equals("0")) {
                leftTime = difference_In_Hours + "," + mins +"";
            } else {
                leftTime = mins + "min since entry";
            }*/

            //System.out.println("left time = " + difference_In_Hours + "hr " + mins + "min ");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return leftTime;
    }

    public String spentTime(String checkedInTimeStamp) {
        String spentTime = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
        try {
            Date mDate = sdf.parse(checkedInTimeStamp);
            Date currentDate = sdf.parse(sdf.format(new Date()));
            long differenceMillis = currentDate.getTime() - mDate.getTime();
            long mins = (differenceMillis / (1000 * 60)) % 60;
            long difference_In_Hours = (differenceMillis / (1000 * 60 * 60)) % 24;
            if (!(difference_In_Hours + "").equals("0")) {
                spentTime = difference_In_Hours +" "+ context.getString(R.string.hour)+" "+ mins +" "+ context.getString(R.string.minute);
            } else {
                spentTime = mins +" "+ context.getString(R.string.minute);
            }

            //System.out.println("Spent time = " + difference_In_Hours + "hour " + mins + "min ");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return spentTime;
    }
}
