package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.elearn.trainor.CourseModule.Courses;
import com.elearn.trainor.Diploma.Diploma;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.MyCompany.CompanyList;
import com.elearn.trainor.PropertyClasses.NotificationProperty;
import com.elearn.trainor.R;
import com.elearn.trainor.SafetyCards.SafetyCards;

import java.util.List;

public class HomeNotificationRecyclerViewAdapter extends RecyclerView.Adapter<HomeNotificationRecyclerViewAdapter.MyViewHolder> {
    private List<NotificationProperty> notifications_list_properties;
    ConnectionDetector connectionDetector;
    Context context;

    public HomeNotificationRecyclerViewAdapter(Context con, List<NotificationProperty> notifications_list_properties) {
        this.context = con;
        this.notifications_list_properties = notifications_list_properties;
        connectionDetector = new ConnectionDetector(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_notifications_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final NotificationProperty notificationListProperty = notifications_list_properties.get(position);
        String notificationType = notificationListProperty.notification_category;
        if (notificationType.equals("E-Learning Course")) {
            holder.notification_type.setText(context.getResources().getString(R.string.elearn_course_heading));
        } else if (notificationType.equals("ClassRoomCourse")) {
            holder.notification_type.setText(context.getResources().getString(R.string.classroom_course_heading));
        } else if (notificationType.equals("Safety card")) {
            holder.notification_type.setText(context.getResources().getString(R.string.notification_safety_card));
        }
        /*else if(notificationType.equals("New safety card")){
            holder.notification_type.setText(context.getResources().getString(R.string.notification_safety_card));
        }else if(notificationType.equals("Safety card expired")){
            holder.notification_type.setText(context.getResources().getString(R.string.notification_expired_safety_card));
        }*/
        else if (notificationType.equals("New Diploma")) {
            holder.notification_type.setText(context.getResources().getString(R.string.notification_new_diploma));
        } else if (notificationType.equals("Documents")) {
            holder.notification_type.setText(context.getResources().getString(R.string.new_documents));
        }
        holder.notification_name.setText(notificationListProperty.notification_body);
        holder.notificationCard_row.setTag(notificationListProperty);
        holder.notificationCard_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout llClick = (LinearLayout) v;
                final NotificationProperty info = (NotificationProperty) llClick.getTag();
                if (notificationListProperty.notification_category.equals("E-Learning Course") || notificationListProperty.notification_category.equals("ClassRoomCourse")) {
                    if (connectionDetector.isConnectingToInternet()) {
                        goTo(Courses.class, info.notification_id);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.offline_notification_course_text), Toast.LENGTH_SHORT).show();
                        goTo(Courses.class, info.notification_id);
                    }
                }
                if (notificationListProperty.notification_category.equals("Safety card")) {
                    if (connectionDetector.isConnectingToInternet()) {
                        goTo(SafetyCards.class, info.notification_id);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.offline_notification_safetycard_text), Toast.LENGTH_SHORT).show();
                        goTo(SafetyCards.class, info.notification_id);
                    }
                }
                /*if (notificationListProperty.notification_category.equals("New safety card") || notificationListProperty.notification_category.equals("Safety card expired")) {
                    if (connectionDetector.isConnectingToInternet()) {
                        goTo(SafetyCards.class, info.notification_id);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.offline_notification_safetycard_text), Toast.LENGTH_SHORT).show();
                        goTo(SafetyCards.class, info.notification_id);
                    }
                }*/
                if (notificationListProperty.notification_category.equals("New Diploma")) {
                    if (connectionDetector.isConnectingToInternet()) {
                        goTo(Diploma.class, info.notification_id);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.offline_notification_diploma_text), Toast.LENGTH_SHORT).show();
                        goTo(Diploma.class, info.notification_id);
                    }
                }
                if (notificationListProperty.notification_category.equals("Documents")) {
                    if (connectionDetector.isConnectingToInternet()) {
                        goTo(CompanyList.class, info.notification_id);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.offline_notification_document_text), Toast.LENGTH_SHORT).show();
                        goTo(CompanyList.class, info.notification_id);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications_list_properties.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout notification_list;
        LinearLayout notificationCard_row;
        public TextView notification_type, notification_name, notificationCount;

        public MyViewHolder(RelativeLayout view) {
            super(view);
            notification_list = view;
            notification_type = (TextView) notification_list.findViewById(R.id.notification_type);
            notification_name = (TextView) notification_list.findViewById(R.id.notification_name);
            notificationCard_row = (LinearLayout) notification_list.findViewById(R.id.notificationCard_row);
        }
    }

    public void goTo(Class activity, String notificationID) {
        Intent intent = new Intent(context, activity);
        intent.putExtra("NotificationID", notificationID);
        intent.putExtra("From", "Notification");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}



