package com.elearn.trainor.BaseAdapters;


import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.CourseModule.GetMoreCourses;
import com.elearn.trainor.CourseModule.OrderCourses;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.PropertyClasses.GetMoreCoursesProperty;
import com.elearn.trainor.R;
import java.util.ArrayList;
import java.util.List;


public class GetMoreCourseRecyclerViewAdapter extends RecyclerView.Adapter<GetMoreCourseRecyclerViewAdapter.ViewHolder> implements Filterable {
    Context context;
    List<GetMoreCoursesProperty> courseInfoList, filteredCoursesPropertyList, listForSpinner;
    boolean isSearchEnabled = false;
    String getSelectedLanguage;
    DataBaseHandlerSelect dbSelect;

    public GetMoreCourseRecyclerViewAdapter(Context context, List<GetMoreCoursesProperty> courseInfoList, boolean searchEnable) {
        this.context = context;
        this.courseInfoList = courseInfoList;
        this.filteredCoursesPropertyList = new ArrayList<>();
        this.listForSpinner = new ArrayList<>();
        this.isSearchEnabled = searchEnable;
        dbSelect = new DataBaseHandlerSelect(context);
        getSelectedLanguage = GetMoreCourses.selectedLanguage;


       /* SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String TexttobeSearched = preferences.getString("TexttobeSearched", "");*/
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout cardView = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_getmore_courses, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        GetMoreCoursesProperty courseInfo = null;
        if(isSearchEnabled){
            courseInfo = filteredCoursesPropertyList.get(position);
        }else{
            courseInfo = courseInfoList.get(position);
        }
        holder.courseName.setText(courseInfo.internal_name);
        holder.price.setText("NOK " + courseInfo.price_inc_vat);
        holder.course_row.setTag(courseInfo);
        holder.course_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout row_ll = (LinearLayout)v;
                String toBeSearched =  GetMoreCourses.searchText;
                GetMoreCoursesProperty info = (GetMoreCoursesProperty)row_ll.getTag();
                Intent intentOrderCourse = new Intent(context, OrderCourses.class);
                intentOrderCourse.putExtra("CourseID",info.course_id);
                intentOrderCourse.putExtra("ProductUUID",info.uuid);
                intentOrderCourse.putExtra("selectedlanguage",getSelectedLanguage);
                intentOrderCourse.putExtra("SearchText",toBeSearched);
                intentOrderCourse.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intentOrderCourse.putExtra("Flaglanguage",info.language);
                context.startActivity(intentOrderCourse);
            }
        });
    }

    @Override
    public int getItemCount() {
       if(isSearchEnabled){
           return filteredCoursesPropertyList.size();
       }else{
           return courseInfoList.size();
       }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout cardView;
        TextView courseName, price;
        LinearLayout course_row;

        public ViewHolder(RelativeLayout itemView) {
            super(itemView);
            cardView = itemView;
            courseName = (TextView) cardView.findViewById(R.id.courseName);
            price = (TextView) cardView.findViewById(R.id.price);
            course_row = (LinearLayout) cardView.findViewById(R.id.course_row);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<GetMoreCoursesProperty> filteredListToBeReturned = new ArrayList<>();
                String charString = charSequence.toString();
                if (!charString.isEmpty()) {
                    List<GetMoreCoursesProperty> filteredList = new ArrayList<>();
                    for (GetMoreCoursesProperty row : courseInfoList) {
                        if(row.internal_name.toLowerCase().contains(charString.toLowerCase())){
                            filteredList.add(row);
                        }
                    }
                    filteredListToBeReturned = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredListToBeReturned;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                List<GetMoreCoursesProperty> filterList = (ArrayList<GetMoreCoursesProperty>) filterResults.values;
                filteredCoursesPropertyList.clear();
                for(GetMoreCoursesProperty item : courseInfoList){
                    int indexOf = courseInfoList.indexOf(item);
                    if(filterList.contains(item)){
                        isSearchEnabled = true;
                        courseInfoList.set(indexOf,item);
                        filteredCoursesPropertyList.add(item);
                    }else{
                        isSearchEnabled = true;
                        courseInfoList.set(indexOf,item);
                    }
                }
                notifyDataSetChanged();
            }
        };
    }



}
