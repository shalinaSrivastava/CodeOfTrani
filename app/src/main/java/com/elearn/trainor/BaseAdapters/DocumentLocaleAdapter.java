package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.MyCompany.DocumentLocaleActivity;
import com.elearn.trainor.R;

import java.util.List;

public class DocumentLocaleAdapter extends RecyclerView.Adapter<DocumentLocaleAdapter.MyViewHolder> {
    List<String> localeList;
    Context context;

    public DocumentLocaleAdapter(Context con, List<String> _list) {
        this.context = con;
        this.localeList = _list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.document_locale_adapter, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int pos) {
        String locale = localeList.get(pos);
        String lang = "";
        if (locale.startsWith("en")) {
            lang = context.getResources().getString(R.string.english);
        } else if (locale.startsWith("nb")) {
            lang = context.getResources().getString(R.string.norwegian);
        } else if (locale.startsWith("pl")) {
            lang = context.getResources().getString(R.string.polish);
        } else if (locale.startsWith("ko")) {
            lang = context.getResources().getString(R.string.korean);
        } else if (locale.startsWith("sv")) {
            lang = context.getResources().getString(R.string.swedish);
        } else if (locale.startsWith("pt")) {
            lang = context.getResources().getString(R.string.portuguese);
        }
        holder.txt_locale.setText(lang);
        holder.rl_root.setTag(locale);
        holder.rl_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String val = (String) v.getTag();
                DocumentLocaleActivity.getInstance().goToNext(val);
            }
        });
    }

    @Override
    public int getItemCount() {
        return localeList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txt_locale;
        RelativeLayout rl_root;

        public MyViewHolder(@NonNull RelativeLayout itemView) {
            super(itemView);
            rl_root = itemView;
            txt_locale = (TextView) itemView.findViewById(R.id.txt_locale);
        }
    }
}
