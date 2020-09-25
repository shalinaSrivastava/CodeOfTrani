package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.CourseModule.OrderCourses;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.R;

import java.util.List;

public class Buy_companyListRecyclerViewAdapter extends RecyclerView.Adapter<Buy_companyListRecyclerViewAdapter.MyViewHolder> {
    private List<CustomerDetailsProperty> myCompanyList;
    Context context;

    public Buy_companyListRecyclerViewAdapter(Context con, List<CustomerDetailsProperty> myCompanyList) {
        this.context = con;
        this.myCompanyList = myCompanyList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_choose_cmpny, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final CustomerDetailsProperty myCompanyListProperty = myCompanyList.get(position);
        holder.company_name.setText(myCompanyListProperty.customerName);
        holder.company_row.setTag(myCompanyListProperty);
        holder.company_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RelativeLayout rl = (RelativeLayout) view;
                CustomerDetailsProperty info = (CustomerDetailsProperty) rl.getTag();
                OrderCourses.getInstance().paymentMethod(info.customer_id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myCompanyList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout activity_my_companylist, company_row;
        public TextView company_name;

        public MyViewHolder(RelativeLayout view) {
            super(view);
            activity_my_companylist = view;
            company_name = (TextView) activity_my_companylist.findViewById(R.id.compny_name);
            company_row = (RelativeLayout) activity_my_companylist.findViewById(R.id.compny_row);

        }
    }
}