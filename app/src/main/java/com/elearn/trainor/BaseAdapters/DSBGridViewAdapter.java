package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.MyCompany.DSBActivity;
import com.elearn.trainor.PropertyClasses.DSBProperty;
import com.elearn.trainor.R;

import java.util.ArrayList;
import java.util.List;

public class DSBGridViewAdapter extends BaseAdapter {
    List<DSBProperty> dsbMagzineList = new ArrayList();
    LayoutInflater inflater;
    Context context;
    ConnectionDetector connectionDetector;


    public DSBGridViewAdapter(Context context, List<DSBProperty> dsbMagzineList) {
        this.dsbMagzineList = dsbMagzineList;
        this.context = context;
        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return dsbMagzineList.size();
    }

    @Override
    public Object getItem(int position) {
        return dsbMagzineList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        connectionDetector = new ConnectionDetector(context);
        final CustomViewHolder mViewHolder;
        final DSBProperty magazineInfo = this.dsbMagzineList.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gridview_adapter_dsb, parent, false);
            mViewHolder = new CustomViewHolder(convertView);
            convertView.setTag(mViewHolder);

        } else {
            mViewHolder = (CustomViewHolder) convertView.getTag();
        }

        mViewHolder.tvTitle.setText(magazineInfo.name);
        mViewHolder.releaseDate.setText(magazineInfo.release_date);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.dsb_placeholder);
        requestOptions.error(R.drawable.dsb_placeholder);

        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(magazineInfo.imageURL).into(mViewHolder.gridBackground);

        mViewHolder.singleGridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(magazineInfo.fileURL.equals("") || magazineInfo.fileURL.equals(null))) {
                    DSBActivity.getInstance().startDownloadingWithPermission(magazineInfo.fileURL,magazineInfo.name);
                }

            }
        });

        return convertView;

    }

    private class CustomViewHolder {
        RelativeLayout singleGridView;
        TextView tvTitle, releaseDate;
        ImageView gridBackground;

        public CustomViewHolder(View item) {
            tvTitle = (TextView) item.findViewById(R.id.title);
            gridBackground = (ImageView) item.findViewById(R.id.backgroudImage);
            releaseDate = (TextView) item.findViewById(R.id.releaseDate);
            singleGridView = (RelativeLayout) item.findViewById(R.id.rl_mainlayout);
        }
    }
}