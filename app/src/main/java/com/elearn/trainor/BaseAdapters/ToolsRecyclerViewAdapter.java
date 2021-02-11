package com.elearn.trainor.BaseAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.elearn.trainor.HelperClasses.*;
import android.widget.TextView;
import com.elearn.trainor.ToolBoxModule.*;
import com.elearn.trainor.PropertyClasses.*;
import com.elearn.trainor.R;
import com.elearn.trainor.DBHandler.*;
import java.io.File;
import java.util.List;

import it.sephiroth.android.library.picasso.Callback;

public class ToolsRecyclerViewAdapter extends RecyclerView.Adapter<ToolsRecyclerViewAdapter.ViewHolder> {
    Context context;
    List<ToolsProperty> toolsPropertyList;
    String From;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerInsert dbInsert;
    DataBaseHandlerUpdate dbUpdate;
    ConnectionDetector connectionDetector;
    SharedPreferenceManager spManager;
    public ToolsRecyclerViewAdapter(Context context, List<ToolsProperty> toolsList, String From) {
        this.context = context;
        this.toolsPropertyList = toolsList;
        this.From = From;
        dbSelect = new DataBaseHandlerSelect(context);
        dbInsert = new DataBaseHandlerInsert(context);
        dbUpdate = new DataBaseHandlerUpdate(context);
        connectionDetector = new ConnectionDetector(context);
        spManager = new SharedPreferenceManager(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout cardView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.tools_recyclerview, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ToolsProperty property = toolsPropertyList.get(position);
        holder.ll_table_row.setBackgroundColor(Color.parseColor(property.background_color));
        holder.txt_tool_name.setText(property.name);
        holder.img_left.setTag(property);
        if (connectionDetector.isConnectingToInternet()) {
            PicasoImageLoader.getImagesFromURL(context, property.iconURL, holder.img_left, 0, 0, new Callback() {
                @Override
                public void onSuccess() {
                    ToolsProperty info = (ToolsProperty) holder.img_left.getTag();
                    updateToolIcon(info, holder.img_left);
                    holder.cardView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {

                }
            });
        } else {
            ToolsProperty info = (ToolsProperty) holder.img_left.getTag();
            holder.img_left.setImageBitmap(ImageConverter.StringToBitmap(info.iconString));
            holder.cardView.setVisibility(View.VISIBLE);
        }
        holder.ll_table_row.setTag(property);
        holder.ll_table_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout rl = (LinearLayout) v;
                ToolsProperty property = (ToolsProperty) rl.getTag();
                String fileDownloded = dbSelect.getFileDownloadedFromToolTable(property.id);
                File rootDir = android.os.Environment.getExternalStorageDirectory();
                File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/.tools/");
                String filePath = root.getAbsolutePath() + "/" + property.name;
                File file = new File(filePath);
                if (file.exists() && (fileDownloded != null && fileDownloded.equals("Yes"))) {
                    Intent intent = new Intent(context, Tools_Content_Activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("ZipFile", property.name);
                    intent.putExtra("BGColor", property.background_color);
                    intent.putExtra("Tool_ID", property.id);
                    intent.putExtra("Force_Download", property.force_download);
                    intent.putExtra("From", From);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                } else {
                    if (connectionDetector.isConnectingToInternet()) {
                        if (!From.equals("Login")) {
                            if (dbSelect.getNotificationData("NotificationTable","IsEnabled","DownloadOverWifi",spManager.getUserID(),"").equals("Yes")) {
                                if (connectionDetector.isConnectedToWifi()) {
                                    Intent intent = new Intent(context, ToolboxLoadingActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("Tool_ID", property.id);
                                    intent.putExtra("Force_Download", property.force_download);
                                    intent.putExtra("Tool_Icon_URL", property.iconURL);
                                    intent.putExtra("ZipFileName", property.name);
                                    intent.putExtra("BGColor", property.background_color);
                                    intent.putExtra("From", From);
                                    intent.putExtra("ZipFileURL", property.file);
                                    intent.putExtra("ZipFileSize", property.file_size);
                                    context.startActivity(intent);
                                    ((Activity) context).overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                                    ToolBox.getInstance().finishActivity();
                                } else {
                                    AlertDialogManager.showDialog(context, context.getResources().getString(R.string.internetErrorTitle), context.getResources().getString(R.string.wifi_not_not_ebabled_tools), false, null);
                                }
                            } else {
                                Intent intent = new Intent(context, ToolboxLoadingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("Tool_ID", property.id);
                                intent.putExtra("Force_Download", property.force_download);
                                intent.putExtra("Tool_Icon_URL", property.iconURL);
                                intent.putExtra("ZipFileName", property.name);
                                intent.putExtra("BGColor", property.background_color);
                                intent.putExtra("From", From);
                                intent.putExtra("ZipFileURL", property.file);
                                intent.putExtra("ZipFileSize", property.file_size);
                                context.startActivity(intent);
                                ((Activity) context).overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                                ToolBox.getInstance().finishActivity();
                            }
                        } else {
                            Intent intent = new Intent(context, ToolboxLoadingActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("Tool_ID", property.id);
                            intent.putExtra("Force_Download", property.force_download);
                            intent.putExtra("Tool_Icon_URL", property.iconURL);
                            intent.putExtra("ZipFileName", property.name);
                            intent.putExtra("BGColor", property.background_color);
                            intent.putExtra("From", From);
                            intent.putExtra("ZipFileURL", property.file);
                            intent.putExtra("ZipFileSize", property.file_size);
                            context.startActivity(intent);
                            ((Activity) context).overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                            ToolBox.getInstance().finishActivity();
                        }
                    } else {
                        AlertDialogManager.showDialog(context, context.getResources().getString(R.string.internetErrorTitle), context.getResources().getString(R.string.internetErrorMessage), false, null);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return toolsPropertyList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardView;
        ImageView img_left, img_right;
        TextView txt_tool_name;
        LinearLayout ll_table_row;

        public ViewHolder(LinearLayout itemView) {
            super(itemView);
            cardView = itemView;
            ll_table_row = (LinearLayout) cardView.findViewById(R.id.rl_table_row);
            img_left = (ImageView) cardView.findViewById(R.id.img_left);
            img_right = (ImageView) cardView.findViewById(R.id.img_right);
            txt_tool_name = (TextView) cardView.findViewById(R.id.txt_tool_name);
        }
    }

    public void updateToolIcon(ToolsProperty info, ImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        String Base64String = ImageConverter.BitmapToBase64StringConversion(bitmap);
        info.iconString = Base64String;
        dbUpdate.updateToolBoxDetails(info, "Image");
    }
}
