package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.PropertyClasses.ToolsProperty;
import com.elearn.trainor.R;

import java.io.File;
import java.util.List;

import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.SettingModule.Downloads;

public class DownloadToolsRecyclerViewAdapter extends RecyclerView.Adapter<DownloadToolsRecyclerViewAdapter.MyViewHolder> {
    private List<ToolsProperty> toolsCardList;
    Context context;
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerUpdate dbUpdate;
    AlertDialog dialog;
    SharedPreferenceManager spManager;

    public DownloadToolsRecyclerViewAdapter(Context con, List<ToolsProperty> toolsCardList) {
        dbDelete = new DataBaseHandlerDelete(con);
        dbUpdate = new DataBaseHandlerUpdate(con);
        spManager = new SharedPreferenceManager(con);
        this.context = con;
        this.toolsCardList = toolsCardList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView itemView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download_tools_recycler_view, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final ToolsProperty toolsDetails = toolsCardList.get(position);
        holder.toolsTitle.setText(toolsDetails.name);
        if (!toolsDetails.file_size.equals("")) {
            double fileSize = Double.parseDouble(toolsDetails.file_size);
            double result_fileSize = (fileSize / 1048576);
            String formattedFileSize = "";
            if (result_fileSize > 1.0) {
                String stringFileSize = String.valueOf(result_fileSize);
                formattedFileSize = stringFileSize.substring(0, 4);
                //holder.toolsSize.setText(formattedFileSize + " MB");
            } else {
                if (result_fileSize == 0.0) {
                    formattedFileSize = "0.0";

                } else {
                    String stringFileSize = String.valueOf(result_fileSize);
                    formattedFileSize = stringFileSize.substring(0, 3);
                }
            }
            holder.toolsSize.setText(formattedFileSize + " MB");
        }

        holder.ll_delete.setTag(toolsDetails);
        holder.ll_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout ll_Delete = (LinearLayout) v;
                final ToolsProperty tools = (ToolsProperty) ll_Delete.getTag();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.custom_dialog_delete_tools, null, false);
                LinearLayout btnDismiss = (LinearLayout) view.findViewById(R.id.btnDismiss);
                LinearLayout btnDelete = (LinearLayout) view.findViewById(R.id.btnDelete);

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        File rootDir = android.os.Environment.getExternalStorageDirectory();
                        File root = new File(rootDir.getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/.tools/");
                        File downloadDir = new File(root.getAbsolutePath());
                        File downloadUnzippedDir = new File(root.getAbsolutePath() + "/UnZipped/");
                        getFilesFromDir(downloadDir, downloadUnzippedDir, tools.name, tools.id, position);
                        customtoast(context.getResources().getString(R.string.deleted_sucessfully));
                        notifyDataSetChanged();
                        if (toolsCardList.size() == 0) {
                            Downloads.getInstance().hideDevButtonToolSection(0);
                        }
                    }
                });
                btnDismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog = builder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setView(view);
                dialog.show();

            }
        });

    }


    @Override
    public int getItemCount() {
        return toolsCardList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView toolsTitle, toolsSize;
        CardView tools_cards;
        LinearLayout ll_delete;

        public MyViewHolder(CardView view) {
            super(view);
            tools_cards = view;
            toolsTitle = (TextView) tools_cards.findViewById(R.id.tools_title);
            toolsSize = (TextView) tools_cards.findViewById(R.id.tools_size);
            ll_delete = (LinearLayout) tools_cards.findViewById(R.id.ll_delete);
        }
    }

    public void getFilesFromDir(File filesFromSD, File fileUnZipped, String fileName, String tool_id, int position) {
        File listAllFiles[] = filesFromSD.listFiles();
        File listAllUnZippedFiles[] = fileUnZipped.listFiles();
        if (listAllFiles != null && listAllFiles.length > 0) {
            for (File currentFile : listAllFiles) {
                if (currentFile.getName().equals(fileName)) {
                    if (currentFile.delete()) {
                        if (toolsCardList.size() > position && toolsCardList.get(position) != null) {
                            //dbDelete.deleteValueFromTable("ToolBox", "ToolID", tool_id);
                            ToolsProperty info = new ToolsProperty();
                            info.id = tool_id;
                            dbUpdate.updateToolBoxDetails(info, "FileDownloadedUpdate");
                            toolsCardList.remove(position);
                        }
                        notifyDataSetChanged();
                    }
                }
            }
        }
        if (listAllUnZippedFiles != null && listAllUnZippedFiles.length > 0) {
            for (File currentFile : listAllUnZippedFiles) {
                if (currentFile.getName().equals(fileName)) {
                    if (currentFile.isDirectory()) {
                        boolean isDeleted = false;
                        File[] children = currentFile.listFiles();
                        for (int i = 0; i < children.length; i++) {
                            if (children[i].isDirectory()) {
                                isDeleted = delete(children[i], children[i].listFiles());
                            }
                            children[i].delete();
                        }
                        if (isDeleted) {
                            if (toolsCardList.size() > position && toolsCardList.get(position) != null) {
                                //dbDelete.deleteValueFromTable("ToolBox", "ToolID", tool_id);
                                ToolsProperty info = new ToolsProperty();
                                info.id = tool_id;
                                dbUpdate.updateToolBoxDetails(info, "FileDownloadedUpdate");
                                toolsCardList.remove(position);
                            }
                            notifyDataSetChanged();
                        }
                        currentFile.delete();
                    } else {
                        if (currentFile.delete()) {
                            if (toolsCardList.size() > position && toolsCardList.get(position) != null) {
                                //dbDelete.deleteValueFromTable("ToolBox", "ToolID", tool_id);
                                ToolsProperty info = new ToolsProperty();
                                info.id = tool_id;
                                dbUpdate.updateToolBoxDetails(info, "FileDownloadedUpdate");
                                toolsCardList.remove(position);
                            }
                            notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    public boolean delete(File currentFile, File[] fileList) {
        boolean isDeleted = false;
        if (currentFile.isDirectory()) {
            File[] children = currentFile.listFiles();
            for (int i = 0; i < children.length; i++) {
                if (children[i].isDirectory()) {
                    isDeleted = delete(children[i], children[i].listFiles());
                }
                children[i].delete();
            }
        } else {
            for (int i = 0; i < fileList.length; i++) {
                isDeleted = new File(currentFile, fileList[i].getName()).delete();
            }
        }
        return isDeleted;
    }

    // My custom toast
    public void customtoast(String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customToastroot = inflater.inflate(R.layout.custom_tost_deleted_sucess, null);
        TextView txtMessage = (TextView) customToastroot.findViewById(R.id.textView1);
        txtMessage.setText(message);
        Toast customtoast = new Toast(context);
        customtoast.setView(customToastroot);
        customtoast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        customtoast.setDuration(Toast.LENGTH_SHORT);
        customtoast.show();
    }


}