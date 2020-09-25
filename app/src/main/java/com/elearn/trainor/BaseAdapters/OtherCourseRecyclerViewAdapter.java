package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elearn.trainor.CourseModule.Courses;
import com.elearn.trainor.PropertyClasses.DiplomaProperty;
import com.elearn.trainor.R;

import java.util.Arrays;
import java.util.List;

public class OtherCourseRecyclerViewAdapter extends RecyclerView.Adapter<OtherCourseRecyclerViewAdapter.ViewHolder> {
    Context context;
    List<DiplomaProperty> coursePropertyList;
    AlertDialog courseInfoDialog;
    public OtherCourseRecyclerViewAdapter(Context context, List<DiplomaProperty> courseList) {
        this.context = context;
        this.coursePropertyList = courseList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout cardView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_other_course, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        DiplomaProperty courseInfo = coursePropertyList.get(position);
        holder.txtCourseName.setText(courseInfo.courseName);
        holder.txtCourseName.setTag(courseInfo);
        holder.img_popup.setTag(holder);
        if (courseInfo.diplomaStatus.equals("Completed Offline")) {
            holder.txtCourseStatus.setText(context.getResources().getString(R.string.completed_offline));
        } else {
            if (courseInfo.diplomaStatus.equals("active")) {
                holder.txtCourseStatus.setText(context.getResources().getString(R.string.completed_capital));
            } else {
                holder.txtCourseStatus.setText(context.getResources().getString(R.string.expired));
            }
        }
        holder.img_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = (ImageView) v;
                ViewHolder hldr = (ViewHolder) img.getTag();
                final DiplomaProperty diplomainfo = (DiplomaProperty) hldr.txtCourseName.getTag();
                PopupMenu popup = new PopupMenu(context, holder.img_popup);
                popup.getMenuInflater().inflate(R.menu.main, popup.getMenu());
                Menu popupMenu = popup.getMenu();
                popupMenu.findItem(R.id.txtViewDiploma).setTitle(context.getResources().getString(R.string.view_diploma));
                popupMenu.findItem(R.id.txtCourseInfo).setTitle(context.getResources().getString(R.string.course_info));
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals(context.getResources().getString(R.string.view_diploma))) {
                            String fileName = diplomainfo.licenseId;
                            Courses.getInstance().startDownloadingWithPermission(diplomainfo, fileName);
                        } else if (item.getTitle().equals(context.getResources().getString(R.string.course_info))) {
                            openCourseInfoDialog(diplomainfo, diplomainfo.language, diplomainfo.courseName, "");
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return coursePropertyList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardView;
        TextView txtCourseName, txtCourseStatus;
        ImageView img_popup;

        public ViewHolder(LinearLayout itemView) {
            super(itemView);
            cardView = itemView;
            txtCourseName = (TextView) cardView.findViewById(R.id.txtCourseName);
            txtCourseStatus = (TextView) cardView.findViewById(R.id.txtCourseStatus);
            img_popup = (ImageView) cardView.findViewById(R.id.img_popup);
        }
    }

    public void openCourseInfoDialog(DiplomaProperty courseInfo, String language, String courseName, String courseType) {
        String day_text = "", hour_text = "", minute_text = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_course_info_dialog, null, false);
        ImageView imgBtnCloseDialog = (ImageView) view.findViewById(R.id.imgBtnCloseDialog);
        TextView txtGoalBody = (TextView) view.findViewById(R.id.txtGoalBody);
        TextView txtTargetBody = (TextView) view.findViewById(R.id.txtTargetBody);
        TextView txtCourseSubjectBody = (TextView) view.findViewById(R.id.txtCourseSubjectBody);
        TextView txtBackCourses = (TextView) view.findViewById(R.id.txtBackCourses);
        TextView txtCourseName = (TextView) view.findViewById(R.id.txtCourseName);
        TextView CourseType = (TextView) view.findViewById(R.id.CourseType);
        TextView txtDuration = (TextView) view.findViewById(R.id.txtDuration);
        TextView txtLanguage = (TextView) view.findViewById(R.id.txtLanguage);
        TextView goal_heading = (TextView) view.findViewById(R.id.goal_heading);
        TextView target_heading = (TextView) view.findViewById(R.id.target_heading);
        TextView course_subject_heading = (TextView) view.findViewById(R.id.course_subject_heading);
        goal_heading.setText(context.getResources().getString(R.string.goal));
        target_heading.setText(context.getResources().getString(R.string.target_group));
        course_subject_heading.setText(context.getResources().getString(R.string.course_subject));
        txtBackCourses.setText(context.getResources().getString(R.string.back_to_courses));
        CourseType.setText(courseType + " " + context.getResources().getString(R.string.elearn_course_heading));
        if (language.startsWith("nb")) {
            language = context.getResources().getString(R.string.norwegian);
        } else if (language.startsWith("en")) {
            language = context.getResources().getString(R.string.english);
        } else if (language.startsWith("ko")) {
            language = context.getResources().getString(R.string.korean);
        } else if (language.startsWith("pl")) {
            language = context.getResources().getString(R.string.polish);
        } else if (language.startsWith("sv")) {
            language = context.getResources().getString(R.string.swedish);
        }else if (language.startsWith("pt")) {
            language = context.getResources().getString(R.string.portuguese);
        }
        txtLanguage.setText(context.getResources().getString(R.string.language) + " " + language);
        txtCourseName.setText(courseName);
        txtGoalBody.setText(courseInfo.info_goal);
        txtTargetBody.setText(courseInfo.info_targetGroup);
        //txtCourseSubjectBody.setText(courseInfo.info_content);
        String subjectMessage = courseInfo.info_content.replaceAll("<br>", "\n");
        String modifiedUnicodeCharater = subjectMessage.replaceAll("[*]", "\u25CF");
        txtCourseSubjectBody.setText(modifiedUnicodeCharater);
        try {
            if (courseInfo.info_goal.equals("")) {
                goal_heading.setVisibility(View.GONE);
                txtGoalBody.setVisibility(View.GONE);
            }
            if (courseInfo.info_targetGroup.equals("")) {
                txtTargetBody.setVisibility(View.GONE);
                target_heading.setVisibility(View.GONE);
            }
            if (courseInfo.info_content.equals("")) {
                txtCourseSubjectBody.setVisibility(View.GONE);
                course_subject_heading.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!courseInfo.courseDuration.equals("")) {
            List<String> durationTextList = Arrays.asList(courseInfo.courseDuration.split(" "));
            if (durationTextList.size() > 0) {
                for (int i = 0; i < durationTextList.size(); i++) {
                    if (durationTextList.get(i).equals("day") || durationTextList.get(i).equals("days")) {
                        String day = durationTextList.get(i).equals("day") ? context.getResources().getString(R.string.day) : context.getResources().getString(R.string.days);
                        day_text = durationTextList.get(i - 1) + " " + day;
                        courseInfo.courseDuration = day_text;
                    } else if (durationTextList.get(i).equals("hour") || durationTextList.get(i).equals("hours")) {
                        String hour = durationTextList.get(i).equals("hour") ? context.getResources().getString(R.string.hour) : context.getResources().getString(R.string.hours);
                        hour_text = durationTextList.get(i - 1) + " " + hour + " ";
                        courseInfo.courseDuration = hour_text;
                    } else if (durationTextList.get(i).equals("minute") || durationTextList.get(i).equals("minutes")) {
                        String minute = durationTextList.get(i).equals("minute") ? context.getResources().getString(R.string.minute) : context.getResources().getString(R.string.minutes);
                        minute_text = durationTextList.get(i - 1) + " " + minute;
                        if (!hour_text.equals("")) {
                            hour_text += minute_text;
                            courseInfo.courseDuration = hour_text;
                        } else {
                            courseInfo.courseDuration = minute_text;
                        }
                    }
                }
            }
        }
        if (courseInfo.courseDuration.equals("") || courseInfo.courseDuration.equals(null) || courseInfo.courseDuration.equals(" ")) {
            txtDuration.setVisibility(View.GONE);
        } else {
            txtDuration.setText(context.getResources().getString(R.string.duration) + " " + courseInfo.courseDuration);
        }
        //txtDuration.setText(context.getResources().getString(R.string.duration) + " " + courseInfo.courseDuration);
        txtBackCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (courseInfoDialog != null && courseInfoDialog.isShowing()) {
                    courseInfoDialog.dismiss();
                }
            }
        });
        imgBtnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (courseInfoDialog != null && courseInfoDialog.isShowing()) {
                    courseInfoDialog.dismiss();
                }
            }
        });
        builder.setView(view);
        courseInfoDialog = builder.create();
        courseInfoDialog.show();
    }
}
