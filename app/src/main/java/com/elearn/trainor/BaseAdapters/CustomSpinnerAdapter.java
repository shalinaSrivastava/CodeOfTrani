package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.elearn.trainor.R;

public class CustomSpinnerAdapter extends BaseAdapter {
    public Context context;
    public int flags[];
    public String[] countryNames;
    public LayoutInflater inflter;

    public CustomSpinnerAdapter(Context applicationContext, int[] flag, String[] countryName) {
        this.context = applicationContext;
        this.flags = flag;
        this.countryNames = countryName;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return flags.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }



    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflter.inflate(R.layout.custom_spinner, null);
        }
        ImageView icon = (ImageView) view.findViewById(R.id.imgLanguage);
        TextView names = (TextView) view.findViewById(R.id.tvLanguage);
        icon.setImageResource(flags[position]);
        String countryNamenames = countryNames[position];
        //names.setText(countryNames[position]);
        if (countryNamenames.equals("English")) {
            names.setText(context.getResources().getString(R.string.english));
        } else if (countryNamenames.equals("Norwegian")) {
            names.setText(context.getResources().getString(R.string.norwegian));
        } else if (countryNamenames.equals("Swedish")) {
            names.setText(context.getResources().getString(R.string.swedish));
        } else if (countryNamenames.equals("Korean")) {
            names.setText(context.getResources().getString(R.string.korean));
        } else if (countryNamenames.equals("Polish")) {
            names.setText(context.getResources().getString(R.string.polish));
        }else if (countryNamenames.equals("Portuguese")) {
            names.setText(context.getResources().getString(R.string.portuguese));
        }
        return view;
    }
}