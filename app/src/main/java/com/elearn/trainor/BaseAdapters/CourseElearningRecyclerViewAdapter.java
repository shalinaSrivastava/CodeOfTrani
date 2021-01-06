
package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.HelperClasses.*;
import com.elearn.trainor.PropertyClasses.DiplomaProperty;
import com.elearn.trainor.R;
import com.elearn.trainor.CourseModule.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseElearningRecyclerViewAdapter extends RecyclerView.Adapter<CourseElearningRecyclerViewAdapter.ViewHolder> {
    Context context;
    List<DiplomaProperty> coursePropertyList;
    public String FromRecyclerView;
    AlertDialog courseInfoDialog;
    ConnectionDetector connectionDetector;
    Long freeSpaceMB;
    boolean permissionGranted = false;
    Handler handler, progressStatusHandler;
    DataBaseHandlerSelect dbSelect;
    DataBaseHandlerUpdate dbUpdate;
    DataBaseHandlerInsert dbInsert;
    SharedPreferenceManager spManager;
    String downloadVideoSize;
    AlertDialog tempDialog;

    public CourseElearningRecyclerViewAdapter(Context context, List<DiplomaProperty> courseList, String FromRecyclerView, boolean permissionGranted) {
        this.context = context;
        this.coursePropertyList = courseList;
        this.FromRecyclerView = FromRecyclerView;
        connectionDetector = new ConnectionDetector(context);
        handler = new Handler();
        progressStatusHandler = new Handler();
        dbInsert = new DataBaseHandlerInsert(context);
        dbSelect = new DataBaseHandlerSelect(context);
        dbUpdate = new DataBaseHandlerUpdate(context);
        spManager = new SharedPreferenceManager(context);
        this.permissionGranted = permissionGranted;
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        freeSpaceMB = availableSpace / SIZE_MB;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout cardView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_elearning_courses, parent, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final DiplomaProperty courseInfo = this.coursePropertyList.get(position);
        String day_text = "", hour_text = "", minute_text = "";
        courseInfo.downloadedStatus = dbSelect.getDataFromCourseDownloadTable("IfNull(Status,'No')as dwStatus", spManager.getUserID(), courseInfo.courseId, courseInfo.licenseId);
        courseInfo.CompletionDateStatus = dbSelect.getDataFromCourseDownloadTable("IfNull(CompletionDateStatus,'No')as completionDateStatus", spManager.getUserID(), courseInfo.courseId, courseInfo.licenseId);
        courseInfo.cmiProgressPercentage = dbSelect.getSCORMStatusFromSCORMTable(spManager.getUserID(), courseInfo.licenseId, "cmiProgressMeasure");
        holder.ll_scorm_details.setVisibility(View.GONE);
        File file = null;
        try {
            File rootDir = Environment.getExternalStorageDirectory();
            File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.Course/" + courseInfo.licenseId);
            String filePath = root.getAbsolutePath();
            file = new File(filePath);
            if (file.exists() && courseInfo.downloadedStatus.equals("Yes")) {
                checkDownloadTime(courseInfo.courseId, courseInfo.licenseId);
                holder.ll_download.setVisibility(View.GONE);
                holder.start_time.setVisibility(View.GONE);
                //holder.start_time.setText(context.getResources().getString(R.string.available_offline));
            } else if (courseInfo.CompletionDateStatus.equals("Yes")) {
                holder.ll_download.setVisibility(View.GONE);
            } else {
                DeleteCoursesFile(file);
                holder.ll_download.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        } finally {
            if (!FromRecyclerView.equals("Classroom") && courseInfo.courseType.equals("E-Learning")) {
                if (courseInfo.cmiProgressPercentage.equals("") || courseInfo.cmiProgressPercentage.equals("undefined")) {
                    // added new below
                    holder.course_status.setText(context.getResources().getString(R.string.not_started));
                    // new implemented as per req (Start button text)
                    if (file.exists() && courseInfo.downloadedStatus.equals("Yes")) {
                        holder.btnStartCourse.setText(context.getResources().getString(R.string.start_offline));
                    }
                } else {
                    if ((Double.valueOf(courseInfo.cmiProgressPercentage) < 1.0 && Double.valueOf(courseInfo.cmiProgressPercentage) > 0)) {
                        holder.btnStartCourse.setText(context.getResources().getString(R.string.kontinue));
                        double percentage_double = Double.parseDouble(courseInfo.cmiProgressPercentage) * 100;
                        //String percentage = String.valueOf(Math.ceil(percentage_double));
                        String percentage = String.valueOf(percentage_double);
                        percentage = percentage.substring(0, percentage.indexOf("."));
                        holder.course_status.setText((percentage + "% " + context.getResources().getString(R.string.completed)).replace(".0", ""));
                    }else if((Double.valueOf(courseInfo.cmiProgressPercentage) == 1.0)){
                        holder.course_status.setText(context.getResources().getString(R.string.completed_capital));
                        if (file.exists() && courseInfo.downloadedStatus.equals("Yes")) {
                            holder.btnStartCourse.setText(context.getResources().getString(R.string.start_offline));
                        }
                    }
                }

                holder.ll_btn_start_course.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LinearLayout btn_start_course = (LinearLayout) view;
                        DiplomaProperty info = (DiplomaProperty) btn_start_course.getTag();
                        if (permissionGranted) {
                            File rootDir = android.os.Environment.getExternalStorageDirectory();
                            File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.Course/");
                            String filePath = root.getAbsolutePath() + "/" + info.licenseId + "/UnZipped";
                            File file = new File(filePath);
                            if (file.exists()) {
                                Intent intent = new Intent(context, CourseWebActivity.class);
                                intent.putExtra("CourseUrl", info.startCourseUrl);
                                intent.putExtra("CourseID", info.courseId);
                                intent.putExtra("LicenceID", info.licenseId);
                                intent.putExtra("CourseFolderName", info.courseName);
                                intent.putExtra("NetworkMode", "Offline");
                                context.startActivity(intent);
                            } else {
                                if (connectionDetector.isConnectingToInternet()) {
                                    Intent intent = new Intent(context, CourseWebActivity.class);
                                    intent.putExtra("CourseUrl", info.startCourseUrl);
                                    intent.putExtra("CourseID", info.courseId);
                                    intent.putExtra("LicenceID", info.licenseId);
                                    intent.putExtra("CourseFolderName", info.courseName);
                                    intent.putExtra("NetworkMode", "Online");
                                    context.startActivity(intent);
                                } else {
                                    AlertDialogManager.showDialog(context, context.getString(R.string.internetErrorTitle), context.getString(R.string.internetErrorMessage), false, null);
                                }
                            }
                        } else {
                            Courses.getInstance().askForWritePermission(info);
                        }
                    }
                });

                if (courseInfo.image_URL == null || courseInfo.image_URL.equals("")) {
                    holder.rl_image_background.setBackground(context.getResources().getDrawable(R.drawable.elarning_course));
                } else {
                   Glide.with(context).load(courseInfo.image_URL).listener(new RequestListener<Drawable>() {
                       @Override
                       public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                           return false;
                       }

                       @Override
                       public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                           return false;
                       }
                   }).into(holder.rl_image_background);
                }

                holder.txtCourseName.setTag(courseInfo);
                holder.txtCourseName.setText(courseInfo.courseName);
                if (courseInfo.cmiProgressPercentage == null || courseInfo.cmiProgressPercentage.equals("") || courseInfo.cmiProgressPercentage.equals("undefined")) {

                } else {
                    if ((Double.valueOf(courseInfo.cmiProgressPercentage) < 1.0 && Double.valueOf(courseInfo.cmiProgressPercentage) > 0.1) && file.exists()) {
                        if (file.exists() && courseInfo.downloadedStatus.equals("Yes")) {
                            holder.start_time.setVisibility(View.GONE);
                        }
                    } else {
                        holder.start_time.setVisibility(View.GONE);
                    }
                }
                holder.ll_download.setTag(holder);
                holder.ll_btn_start_course.setTag(courseInfo);

                String language = courseInfo.language;
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
                if (courseInfo.courseDuration.equals("")) {
                    holder.txtCourseDuration.setText(language);
                } else {
                    holder.txtCourseDuration.setText(courseInfo.courseDuration + " - " + language);
                }

                holder.txtCourseName.setText(courseInfo.courseName);
                holder.llinfo.setTag(courseInfo);
                if (courseInfo.availableOffline.equals("false")) {
                    holder.ll_download.setVisibility(View.GONE);
                }
                holder.llinfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinearLayout courseInfoImgBtn = (LinearLayout) v;
                        DiplomaProperty courseInfo = (DiplomaProperty) courseInfoImgBtn.getTag();
                        openCourseInfoDialog(courseInfo.language, courseInfo.courseName, context.getResources().getString(R.string.elearn_course_heading), position);
                    }
                });
                holder.ll_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinearLayout downloadImageButton = (LinearLayout) v;
                        ViewHolder hlder = (ViewHolder) downloadImageButton.getTag();
                        DiplomaProperty course_info = (DiplomaProperty) hlder.txtCourseName.getTag();
                        if (connectionDetector.isConnectingToInternet()) {
                            if (Courses.getInstance().checkWriteExternalPermission()) {
                                try {
                                    holder.ll_download.setClickable(false);
                                    getVideoFileSize(course_info.courseId, course_info.licenseId, hlder);
                                } catch (Exception ex) {
                                    holder.ll_download.setClickable(true);
                                }
                            } else {
                                Courses.getInstance().askForPermission(course_info.courseId, course_info.licenseId, hlder, position);
                            }
                        } else {
                            AlertDialogManager.showDialog(context, context.getString(R.string.internetErrorTitle), context.getString(R.string.internetErrorMessage), false, null);
                        }
                    }
                });

                holder.img_course_details.setTag(courseInfo);
                holder.img_course_details.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Button scormDetailnfo = (Button) v;
                        DiplomaProperty scormInfo = (DiplomaProperty) scormDetailnfo.getTag();
                        List<String> scormlist = dbSelect.tempSCORMData(spManager.getUserID(), scormInfo.licenseId, "LicenceID,cmiLocation,cmiProgressMeasure,cmiCompletionStatus,cmiSuccessStatus");
                        tempDialog = courseDetails(scormlist);
                    }
                });
            }else{
                System.out.println(" Class room Course");
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.coursePropertyList.size();
    }

    public void getVideoFileSize(final String courseid, final String licenceid, final ViewHolder hldr) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.Video_File_Size_URL + courseid + "/" + licenceid + "/size", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && !response.equals("")) {
                        DiplomaProperty courseProperty = new DiplomaProperty();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject.getString("quality").equals("360p")) {
                                courseProperty.fileSize = jsonObject.getString("size") == null ? "" : jsonObject.getString("size").equals("null") ? "" : jsonObject.getString("size");
                                //downloadCoursePopUpDialog = downloadCoursePopUpDialog(hldr, courseProperty.fileSize);  Lov Tyagi 26-10-2017 17:37
                                downloadCoursePopUpDialog(hldr, courseProperty.fileSize);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.d("", ex.getMessage());
                } finally {
                    hldr.ll_download.setClickable(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hldr.ll_download.setClickable(true);
                AlertDialogManager.showDialog(context, context.getResources().getString(R.string.server_error_title), VolleyErrorHandler.getErrorMessage(context, error), false, null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(context);
        stringRequest.setShouldCache(false);
        requestQueue11.add(stringRequest);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardView, ll_company_name_elearn, ll_download, ll_btn_start_course, ll_diploma_button, llinfo, ll_scorm_details;
        TextView txtCourseName, txtCourseDuration, course_status, start_time, btnStartCourse;
        Button img_course_details;
        ImageButton imgInfoCourse, img_diploma_when_finished;
        ImageView rl_image_background;

        public ViewHolder(LinearLayout itemView) {
            super(itemView);
            cardView = itemView;
            txtCourseName = (TextView) cardView.findViewById(R.id.txtCourseName);
            txtCourseDuration = (TextView) cardView.findViewById(R.id.txtCourseLenght);
            course_status = (TextView) cardView.findViewById(R.id.course_status);
            btnStartCourse = (TextView) cardView.findViewById(R.id.btnStartCourse);
            imgInfoCourse = (ImageButton) cardView.findViewById(R.id.imgInfoCourse);
            ll_company_name_elearn = (LinearLayout) cardView.findViewById(R.id.ll_company_name_elearn);
            rl_image_background = (ImageView) cardView.findViewById(R.id.rl_image_background);
            start_time = (TextView) cardView.findViewById(R.id.start_time);
            ll_btn_start_course = (LinearLayout) cardView.findViewById(R.id.ll_btn_start_course);
            ll_diploma_button = (LinearLayout) cardView.findViewById(R.id.ll_diploma_button);
            img_diploma_when_finished = (ImageButton) cardView.findViewById(R.id.img_diploma_when_finished);
            img_course_details = (Button) cardView.findViewById(R.id.img_course_details);
            llinfo = (LinearLayout) cardView.findViewById(R.id.llinfo);
            ll_download = (LinearLayout) cardView.findViewById(R.id.ll_download);
            ll_scorm_details = (LinearLayout) cardView.findViewById(R.id.ll_scorm_details);
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
        CourseType.setText(courseType);
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
        if (courseInfo.courseDuration.equals("") || courseInfo.courseDuration.equals(null) || courseInfo.courseDuration.equals(" ")) {
            txtDuration.setVisibility(View.GONE);
        } else {
            txtDuration.setText(context.getResources().getString(R.string.duration) + " " + courseInfo.courseDuration);
        }

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

    public void startCourse(DiplomaProperty info) {
        File rootDir = android.os.Environment.getExternalStorageDirectory();
        File root = new File(rootDir.getAbsolutePath() + "/MyTrainor/" + spManager.getUserID() + "/.Course/");
        String filePath = root.getAbsolutePath() + "/" + info.licenseId + "/UnZipped";
        File file = new File(filePath);
        if (file.exists()) {
            Intent intent = new Intent(context, CourseWebActivity.class);
            intent.putExtra("CourseUrl", info.startCourseUrl);
            intent.putExtra("CourseID", info.courseId);
            intent.putExtra("LicenceID", info.licenseId);
            intent.putExtra("CourseFolderName", info.licenseId);
            intent.putExtra("NetworkMode", "Offline");
            context.startActivity(intent);
        } else {
            if (connectionDetector.isConnectingToInternet()) {
                Intent intent = new Intent(context, CourseWebActivity.class);
                intent.putExtra("CourseUrl", info.startCourseUrl);
                intent.putExtra("CourseID", info.courseId);
                intent.putExtra("LicenceID", info.licenseId);
                intent.putExtra("CourseFolderName", info.licenseId);
                intent.putExtra("NetworkMode", "Online");
                context.startActivity(intent);
            } else {
                AlertDialogManager.showDialog(context, context.getString(R.string.internetErrorTitle), context.getString(R.string.internetErrorMessage), false, null);
            }
        }
    }

    public void checkDownloadTime(String courseID, String licenceID) {
        String dwTime = dbSelect.getDataFromCourseDownloadTable("IfNull(DownloadTime,'')as dwtime", spManager.getUserID(), courseID, licenceID);
        if (!dwTime.equals("")) {
            dbUpdate.updateTable("CoursesTable", spManager.getUserID(), licenceID, "DownloadTime", dwTime);
        }
    }

    void DeleteCoursesFile(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                DeleteCoursesFile(child);
            }
        }
        fileOrDirectory.delete();
    }

    public AlertDialog courseDetails(List<String> scormlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_download_course_popup, null, false);
        TextView txt_scorminfo = (TextView) view.findViewById(R.id.txt_scorminfo);
        if (scormlist.size() == 0) {
            txt_scorminfo.setText("");
        } else {
            txt_scorminfo.setText("licenseID: " + scormlist.get(0) + "\ncmiLocation: " + scormlist.get(1) + "\ncmiProgressMeasure: " + scormlist.get(2) + "\ncmiCompletionStatus: " + scormlist.get(3) + "\ncmiSuccessStatus: " + scormlist.get(4));
        }
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (tempDialog != null && tempDialog.isShowing()) {
                    tempDialog.dismiss();
                }
            }
        });
        tempDialog = builder.create();
        tempDialog.setCanceledOnTouchOutside(false);
        tempDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        tempDialog.setView(view);
        tempDialog.show();
        return tempDialog;
    }

    public void downloadCoursePopUpDialog(final ViewHolder hldr, final String size) {
        boolean enoughSpace = false;
        final long fileSizeToCheck = Long.parseLong(size) / (1024 * 1024);
        final long mbSize = 1024 * 1024;
        if (Long.parseLong(size) >= mbSize) {
            downloadVideoSize = (Long.parseLong(size) / (mbSize)) + " " + context.getResources().getString(R.string.mb);
        } else {
            downloadVideoSize = (Double.parseDouble(size) / (mbSize)) + " " + context.getResources().getString(R.string.mb);
            if (downloadVideoSize.startsWith("0.")) {
                downloadVideoSize = downloadVideoSize.substring(0, 3) + " " + context.getResources().getString(R.string.mb);
            }
        }
        String message = context.getResources().getString(R.string.this_will_take) + " " + downloadVideoSize + " " + context.getResources().getString(R.string.of_space_you_have) + " " + freeSpaceMB + " " + context.getResources().getString(R.string.mb_available_space);
        if (fileSizeToCheck <= freeSpaceMB) {
            enoughSpace = true;
        } else {
            enoughSpace = false;
        }
        AlertDialogManager.alternateCustomDialog(context, context.getResources().getString(R.string.save_course_offline), message, enoughSpace, true, new IClickListener() {
            @Override
            public void onClick() {
                if (connectionDetector.isConnectingToInternet()) {
                    if (dbSelect.getNotificationData("NotificationTable", "IsEnabled", "DownloadOverWifi", spManager.getUserID(), "").equals("Yes")) {
                        if (connectionDetector.isConnectedToWifi()) {
                            if (dbSelect.getNotificationData("NotificationTable", "IsEnabled", "DownloadOverWifi", spManager.getUserID(), "").equals("Yes")) {
                                if (connectionDetector.isConnectedToWifi()) {
                                    DiplomaProperty info = (DiplomaProperty) hldr.txtCourseName.getTag();
                                    Intent intent = new Intent(context, CourseDownloadingActivity.class);
                                    intent.putExtra("FileName", info.courseName);
                                    intent.putExtra("ImageUrl", info.image_URL);
                                    intent.putExtra("CourseId", info.courseId);
                                    intent.putExtra("LisenceId", info.licenseId);
                                    intent.putExtra("fileSize", size);
                                    intent.putExtra("fileSizeInMB", downloadVideoSize + "");
                                    context.startActivity(intent);
                                } else {
                                    AlertDialogManager.showDialog(context, context.getResources().getString(R.string.internetErrorTitle), context.getResources().getString(R.string.wifi_not_not_ebabled_course), false, null);
                                }
                            } else {
                                DiplomaProperty info = (DiplomaProperty) hldr.txtCourseName.getTag();
                                Intent intent = new Intent(context, CourseDownloadingActivity.class);
                                intent.putExtra("FileName", info.courseName);
                                intent.putExtra("ImageUrl", info.image_URL);
                                intent.putExtra("CourseId", info.courseId);
                                intent.putExtra("LisenceId", info.licenseId);
                                intent.putExtra("fileSize", size);
                                intent.putExtra("fileSizeInMB", downloadVideoSize + "");
                                context.startActivity(intent);
                            }
                        } else {
                            AlertDialogManager.showDialog(context, context.getResources().getString(R.string.internetErrorTitle), context.getResources().getString(R.string.wifi_not_not_ebabled_course), false, null);
                        }
                    } else {
                        if (dbSelect.getNotificationData("NotificationTable", "IsEnabled", "DownloadOverWifi", spManager.getUserID(), "").equals("Yes")) {
                            if (connectionDetector.isConnectedToWifi()) {
                                DiplomaProperty info = (DiplomaProperty) hldr.txtCourseName.getTag();
                                Intent intent = new Intent(context, CourseDownloadingActivity.class);
                                intent.putExtra("FileName", info.courseName);
                                intent.putExtra("ImageUrl", info.image_URL);
                                intent.putExtra("CourseId", info.courseId);
                                intent.putExtra("LisenceId", info.licenseId);
                                intent.putExtra("fileSize", size);
                                intent.putExtra("fileSizeInMB", downloadVideoSize + "");
                                context.startActivity(intent);
                            } else {
                                AlertDialogManager.showDialog(context, context.getResources().getString(R.string.internetErrorTitle), context.getResources().getString(R.string.wifi_not_not_ebabled_course), false, null);
                            }
                        } else {
                            DiplomaProperty info = (DiplomaProperty) hldr.txtCourseName.getTag();
                            Intent intent = new Intent(context, CourseDownloadingActivity.class);
                            intent.putExtra("FileName", info.courseName);
                            intent.putExtra("ImageUrl", info.image_URL);
                            intent.putExtra("CourseId", info.courseId);
                            intent.putExtra("LisenceId", info.licenseId);
                            intent.putExtra("fileSize", size);
                            intent.putExtra("fileSizeInMB", downloadVideoSize + "");
                            context.startActivity(intent);
                        }
                    }
                } else {
                    AlertDialogManager.showDialog(context, context.getString(R.string.internetErrorTitle), context.getString(R.string.internetErrorMessage), false, null);
                }
            }
        }, new IClickListener() {
            @Override
            public void onClick() {
                hldr.ll_download.setClickable(true);
            }
        }, context.getResources().getString(R.string.download), context.getResources().getString(R.string.cancel), "Blue", "CourseDownload");
    }
}
