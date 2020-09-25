package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.MyCompany.CompanyList;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.R;

import java.util.List;

public class MyCompanyListRecyclerViewAdapter extends RecyclerView.Adapter<MyCompanyListRecyclerViewAdapter.MyViewHolder> {
    private List<CustomerDetailsProperty> myCompanyList;
    Context context;

    public MyCompanyListRecyclerViewAdapter(Context con, List<CustomerDetailsProperty> myCompanyList) {
        this.context = con;
        this.myCompanyList = myCompanyList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_mycompany_list, parent, false);
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
                LinearLayout ll = (LinearLayout) view;
                CustomerDetailsProperty info = (CustomerDetailsProperty) ll.getTag();
                CompanyList.getInstance().goToNext(info);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myCompanyList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout activity_my_companylist;
        LinearLayout company_row;
        public TextView company_name;

        public MyViewHolder(RelativeLayout view) {
            super(view);
            activity_my_companylist = view;
            company_name = (TextView) activity_my_companylist.findViewById(R.id.company_name);
            //notificationCount = (TextView) activity_my_companylist.findViewById(R.id.notificationCount);
            company_row = (LinearLayout) activity_my_companylist.findViewById(R.id.company_row);

        }
    }
}