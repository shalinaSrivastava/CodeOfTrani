package com.elearn.trainor.BaseAdapters;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elearn.trainor.CourseModule.GoogleMapActivity;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.DBHandler.DataBaseHandlerUpdate;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.PicasoImageLoader;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.PropertyClasses.DiplomaProperty;
import com.elearn.trainor.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import it.sephiroth.android.library.picasso.Callback;

public class CourseRecyclerViewAdapter extends RecyclerView.Adapter<CourseRecyclerViewAdapter.ViewHolder> {
    Context context;
    List<DiplomaProperty> coursePropertyList;
    public String FromRecyclerView;
    AlertDialog courseInfoDialog;
    ConnectionDetector connectionDetector;
    Handler handler, progressStatusHandler;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerUpdate dbUpdate;
    SharedPreferenceManager spManager;
    public static List<HashMap<String, Object>> statusMaintainList;
    DownloadManager.Query q;

    public CourseRecyclerViewAdapter(Context context, List<DiplomaProperty> courseList, String FromRecyclerView) {
        this.context = context;
        this.coursePropertyList = courseList;
        this.FromRecyclerView = FromRecyclerView;
        connectionDetector = new ConnectionDetector(context);
        handler = new Handler();
        progressStatusHandler = new Handler();
        dbSelect = new DataBaseHandlerSelect(context);
        dbUpdate = new DataBaseHandlerUpdate(context);
        spManager = new SharedPreferenceManager(context);
        statusMaintainList = new ArrayList<>();
        q = new DownloadManager.Query();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout cardView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_elearning_courses, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final DiplomaProperty courseInfo = coursePropertyList.get(position);
        String day_text = "", hour_text = "", minute_text = "";
        holder.ll_download.setVisibility(View.GONE);
        holder.btnStartCourse.setText(context.getResources().getString(R.string.navigate));
        holder.txtCourseName.setTag(courseInfo);
        holder.txtCourseName.setText(courseInfo.courseName);
        holder.img_download_buton.setTag(holder);
        holder.ll_btn_start_course.setTag(courseInfo);
        holder.start_time.setVisibility(View.VISIBLE);
        holder.course_location.setVisibility(View.GONE);
        holder.ll_scorm_details.setVisibility(View.GONE);
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date courseDate = inputFormat.parse(courseInfo.startDate);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd. MMM yyyy - HH:mm");
            String stringDate = sdf.format(courseDate);
            holder.start_time.setText(stringDate + "," + " " + courseInfo.courseCity);
        } catch (ParseException ex) {
            Log.d("Error", ex.getMessage());
        }
        if (courseInfo.image_URL == null || courseInfo.image_URL.equals("")) {
            holder.rl_image_background.setBackground(context.getResources().getDrawable(R.drawable.classroom_course));
        } else {
            PicasoImageLoader.getImagesFromURL(context, courseInfo.image_URL, holder.rl_image_background, 0, 0, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    holder.rl_image_background.setBackground(context.getResources().getDrawable(R.drawable.classroom_course));
                }
            });
        }
        String language = courseInfo.language;
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
        holder.txtCourseLenght.setText(courseInfo.courseDuration + " - " + language);
        holder.txtCourseName.setText(courseInfo.courseName);
        holder.llinfo.setTag(courseInfo);
        holder.llinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout courseInfoImgBtn = (LinearLayout) v;
                DiplomaProperty courseInfo = (DiplomaProperty) courseInfoImgBtn.getTag();
                openCourseInfoDialog(courseInfo.language, courseInfo.courseName, FromRecyclerView, position);
            }
        });

        holder.ll_btn_start_course.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btnStartCourse.getText().equals(context.getResources().getString(R.string.navigate))) {
                    if (connectionDetector.isConnectingToInternet()) {
                        Intent startMapActivity = new Intent(context, GoogleMapActivity.class);
                        startMapActivity.putExtra("CourseLocation", courseInfo.location);
                        startMapActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(startMapActivity);
                    } else {
                        AlertDialogManager.showDialog(context, context.getString(R.string.internetErrorTitle), context.getString(R.string.internetErrorMessage), false, null);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return coursePropertyList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardView, ll_download, ll_scorm_details, llinfo, ll_btn_start_course;
        TextView txtCourseName, txtCourseLenght, course_location, start_time, btnStartCourse;
        ImageButton img_download_buton, imgInfoCourse;
        ImageView rl_image_background;
        Button img_course_details;

        public ViewHolder(LinearLayout itemView) {
            super(itemView);
            cardView = itemView;
            txtCourseName = (TextView) cardView.findViewById(R.id.txtCourseName);
            txtCourseLenght = (TextView) cardView.findViewById(R.id.txtCourseLenght);
            course_location = (TextView) cardView.findViewById(R.id.course_status);
            btnStartCourse = (TextView) cardView.findViewById(R.id.btnStartCourse);
            img_download_buton = (ImageButton) cardView.findViewById(R.id.img_download_buton);
            imgInfoCourse = (ImageButton) cardView.findViewById(R.id.imgInfoCourse);
            ll_download = (LinearLayout) cardView.findViewById(R.id.ll_download);
            rl_image_background = (ImageView) cardView.findViewById(R.id.rl_image_background);
            start_time = (TextView) cardView.findViewById(R.id.start_time);
            img_course_details = (Button) cardView.findViewById(R.id.img_course_details);
            ll_scorm_details = (LinearLayout) cardView.findViewById(R.id.ll_scorm_details);
            llinfo = (LinearLayout) cardView.findViewById(R.id.llinfo);
            ll_btn_start_course = (LinearLayout) cardView.findViewById(R.id.ll_btn_start_course);
        }
    }

    public void openCourseInfoDialog(String language, String courseName, String courseType, final int position) {
        DiplomaProperty courseInfo = coursePropertyList.get(position);
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
        CourseType.setText(context.getResources().getString(R.string.classroom_course_heading));
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
        goal_heading.setText(context.getResources().getString(R.string.goal));
        target_heading.setText(context.getResources().getString(R.string.target_group));
        course_subject_heading.setText(context.getResources().getString(R.string.course_subject));
        txtBackCourses.setText(context.getResources().getString(R.string.back_to_courses));
        txtCourseName.setText(courseName);
        txtGoalBody.setText(courseInfo.info_goal);
        txtTargetBody.setText(courseInfo.info_targetGroup);
        String subjectMessage = courseInfo.info_content.replaceAll("<br>", "\n");
        String modifiedUnicodeCharater = subjectMessage.replaceAll("[*]", "\u25CF");
        txtCourseSubjectBody.setText(modifiedUnicodeCharater);
        if(courseInfo.courseDuration.equals("")||courseInfo.courseDuration.equals(null) || courseInfo.courseDuration.equals(" ")){
            txtDuration.setVisibility(View.GONE);
        }else{
            txtDuration.setText(context.getResources().getString(R.string.duration) + " " + courseInfo.courseDuration);
        }
        txtBackCourses.setText(context.getResources().getString(R.string.back_to_courses));
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
