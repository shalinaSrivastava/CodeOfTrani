package com.elearn.trainor.BaseAdapters;

import android.content.Context;

import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.elearn.trainor.DBHandler.DataBaseHandlerDelete;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.PropertyClasses.DiplomaProperty;
import com.elearn.trainor.R;
import com.elearn.trainor.SettingModule.Downloads;
import java.io.File;
import java.util.List;

public class DownloadCourseRecyclerViewAdapter extends RecyclerView.Adapter<DownloadCourseRecyclerViewAdapter.MyViewHolder> {
    private List<DiplomaProperty> courseCardList;
    Context context;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerDelete dbDelete;
    DataBaseHandlerUpdate dbUpdate;
    SharedPreferenceManager spManager;

    public DownloadCourseRecyclerViewAdapter(Context con, List<DiplomaProperty> courseCardList) {
        this.context = con;
        this.courseCardList = courseCardList;
        spManager = new SharedPreferenceManager(con);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        dbSelect = new DataBaseHandlerSelect(context);
        dbDelete = new DataBaseHandlerDelete(context);
        dbUpdate = new DataBaseHandlerUpdate(context);
        CardView itemView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download_tools_recycler_view, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final DiplomaProperty courseDetails = courseCardList.get(position);
        holder.courseTitle.setText(courseDetails.courseName);
        holder.courseSize.setText(courseDetails.fileSize);
        holder.ll_delete.setTag(courseDetails);
        holder.ll_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout ll_Delete = (LinearLayout) v;
                final DiplomaProperty courseInfo = (DiplomaProperty) ll_Delete.getTag();
                AlertDialogManager.alternateCustomDialog(context, context.getResources().getString(R.string.delete) + " " + courseInfo.courseName, context.getResources().getString(R.string.delete_offline_des), false, true, new IClickListener() {
                    @Override
                    public void onClick() {
                        File rootDir = android.os.Environment.getExternalStorageDirectory();
                        File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.Course/" + courseInfo.licenseId);
                        String filePath = root.getAbsolutePath();
                        File dir = new File(filePath);
                        DeleteCoursesFile(dir);
                        customtoast(context.getResources().getString(R.string.deleted_sucessfully));
                        courseCardList.remove(position);
                        if (courseCardList.size() == 0) {
                            Downloads.getInstance(). hideDevButtonCourseSection(0);
                        }
                        dbUpdate.updateTable("CoursesTable", spManager.getUserID(), courseInfo.licenseId, "DownloadTime", "");
                        dbDelete.deleteValueFromTable("CourseDownload", "licenseId", courseInfo.licenseId);
                        notifyDataSetChanged();
                    }
                }, null, context.getResources().getString(R.string.delete_courses), context.getResources().getString(R.string.keep_courses), "Blue", "");

            }
        });
    }

    @Override
    public int getItemCount() {
        return courseCardList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView courseTitle, courseSize;
        CardView course_cards;
        LinearLayout ll_delete;

        public MyViewHolder(CardView view) {
            super(view);
            course_cards = view;
            courseTitle = (TextView) course_cards.findViewById(R.id.tools_title);
            courseSize = (TextView) course_cards.findViewById(R.id.tools_size);
            ll_delete = (LinearLayout) course_cards.findViewById(R.id.ll_delete);
        }
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

    void DeleteCoursesFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                DeleteCoursesFile(child);
            }
        fileOrDirectory.delete();
    }
}