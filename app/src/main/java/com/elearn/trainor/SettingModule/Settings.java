package com.elearn.trainor.SettingModule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
//import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.elearn.trainor.HelperClasses.*;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.FullImage;
import com.elearn.trainor.Login;
import com.elearn.trainor.HomePage;

import com.elearn.trainor.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import it.sephiroth.android.library.picasso.Callback;
import permissions.dispatcher.RuntimePermissions;
import permissions.dispatcher.NeedsPermission;

import com.elearn.trainor.DBHandler.*;
import com.elearn.trainor.PropertyClasses.*;
import com.google.firebase.analytics.FirebaseAnalytics;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

@RuntimePermissions
public class Settings extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    Locale myLocale;
    public static int RESULT_LOAD_IMAGE = 100;
    public static int CAMERA_REQUEST = 200;
    SharedPreferenceManager spManager;
    TextView tv_settings, tv_save, tv_cancle, txt_change_photo;
    Dialog languageSelectionDIalog;
    AlertDialog profilePopUpDialog;
    CircleImageView circleImageView;
    LinearLayout change_password, ll_downloaded_courses, ll_notifications, ll_back, llhome, ll_save, ll_rootview, ll_logout;
    EditText first_name, last_name, email_id, phone_no, dob;
    ProgressDialog pDialog;
    ConnectionDetector connectionDetector;
    DataBaseHandlerSelect dataBaseHandlerSelect;
    DataBaseHandlerInsert dataBaseHandlerInsert;
    DataBaseHandlerDelete dataBaseHandlerDelete;
    DataBaseHandlerUpdate dataBaseHandlerUpdate;
    SwipeRefreshLayout swipelayout;
    String item, dobString, settings, english, norwegian, korean, polish, swedish, portuguese, alertMsgFirstName,
            alertMsgLasttName, alertMsgPhone, alertMsgEmail, internalServerError, networkError, waitMsg, internetErrorTitle, internetErrorMessage;
    Spinner spinner;
    public static boolean removePhoto = false;
    private Uri fileUri;
    boolean isWindowActiviated = false;
    String isFromRefresh, FromPage = "";
    FirebaseAnalytics analytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        isWindowActiviated = true;
        getControls();
        if (spManager.getSharedPreferenceExistence()) {
            Configuration config = getBaseContext().getResources().getConfiguration();
            myLocale = new Locale(spManager.getLanguage());
            Locale.setDefault(myLocale);
            config.locale = myLocale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        String FromPage = getIntent().getStringExtra("From");
        if (FromPage != null && FromPage.equals("ImageCrop")) {
            if (getIntent().getStringExtra("ImageURL") != null && !getIntent().getStringExtra("ImageURL").equals("")) {
                if (connectionDetector.isConnectingToInternet()) {
                    showWaitDialog();
                    getProfilePicFromURL(getIntent().getStringExtra("ImageURL"), "update");
                } else {
                    circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        isWindowActiviated = true;
        super.onStart();
    }

    @Override
    protected void onResume() {
        isWindowActiviated = true;
        super.onResume();
        analytics.setCurrentScreen(Settings.this, "SettingsPage", null);
    }

    @Override
    protected void onStop() {
        dismissWaitDialog();
        isWindowActiviated = false;
        super.onStop();
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(Settings.this);
        spManager = new SharedPreferenceManager(Settings.this);
        english = getResources().getString(R.string.english);
        norwegian = getResources().getString(R.string.norwegian);
        korean = getResources().getString(R.string.korean);
        polish = getResources().getString(R.string.polish);
        swedish = getResources().getString(R.string.swedish);
        portuguese = getResources().getString(R.string.portuguese);
        alertMsgEmail = getResources().getString(R.string.alertMsgEmail);
        alertMsgFirstName = getResources().getString(R.string.alertMsgFirstName);
        alertMsgLasttName = getResources().getString(R.string.alertMsgLasttName);
        alertMsgPhone = getResources().getString(R.string.alertMsgPhone);
        networkError = getResources().getString(R.string.networkError);
        internalServerError = getResources().getString(R.string.internalServerError);
        waitMsg = getResources().getString(R.string.waitMsg);
        internetErrorTitle = getResources().getString(R.string.internetErrorTitle);
        internetErrorMessage = getResources().getString(R.string.internetErrorMessage);
        settings = getResources().getString(R.string.setting);
        isFromRefresh = getIntent().getStringExtra("From");
        dataBaseHandlerDelete = new DataBaseHandlerDelete(Settings.this);
        dataBaseHandlerInsert = new DataBaseHandlerInsert(Settings.this);
        dataBaseHandlerSelect = new DataBaseHandlerSelect(Settings.this);
        dataBaseHandlerUpdate = new DataBaseHandlerUpdate(Settings.this);
        ll_rootview = (LinearLayout) findViewById(R.id.ll_rootview);
        ll_save = (LinearLayout) findViewById(R.id.ll_save);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipelayout.setColorSchemeResources(R.color.settings);
        connectionDetector = new ConnectionDetector(Settings.this);
        tv_settings = (TextView) findViewById(R.id.text_header);
        ll_downloaded_courses = (LinearLayout) findViewById(R.id.ll_downloaded_courses);
        change_password = (LinearLayout) findViewById(R.id.change_password);
        circleImageView = (CircleImageView) findViewById(R.id.circleImageView);
        txt_change_photo = (TextView) findViewById(R.id.txt_change_photo);
        first_name = (EditText) findViewById(R.id.first_name);
        last_name = (EditText) findViewById(R.id.last_name);
        email_id = (EditText) findViewById(R.id.email_id);
        phone_no = (EditText) findViewById(R.id.phone_no);
        dob = (EditText) findViewById(R.id.dob);
        ll_logout = (LinearLayout) findViewById(R.id.ll_logout);
        tv_save = (TextView) findViewById(R.id.tv_save);
        tv_cancle = (TextView) findViewById(R.id.tv_cancle);
        buttonVisibility("hide");
        ll_notifications = (LinearLayout) findViewById(R.id.ll_notifications);
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (connectionDetector.isConnectingToInternet()) {
                    getUserDetails();
                } else {
                    swipelayout.setRefreshing(false);
                    AlertDialogManager.showDialog(Settings.this, getString(R.string.internetErrorTitle), getString(R.string.internetErrorMessage), false, null);
                }
            }
        });
        tv_settings.setText(settings);
        String url = spManager.getProfileURL();
        removePhoto = url == null || url.equals("");
        if (isFromRefresh != null && isFromRefresh.equals("selfRefresh")) {
            if (spManager.getProfileURL().equals("invalid URL")) {
                circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
            } else {
                if (connectionDetector.isConnectingToInternet()) {
                    Glide.with(Settings.this).load(spManager.getProfileURL()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).into(circleImageView);

                } else {
                    if (!spManager.getProfileURL().equals("invalid URL")) {
                        PicasoImageLoader.setOfflineImage(Settings.this, spManager.getProfileURL(), circleImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                            }
                        });
                    } else {
                        circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                    }
                    dismissWaitDialog();
                }
                first_name.setText(spManager.getFirstname());
                last_name.setText(spManager.getLastname());
                email_id.setText(spManager.getEmail());
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dob.setText("");
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        String dobText = spManager.getDOB().toString().trim();
                        if (dobText != null && !dobText.equals("")) {
                            DateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd");
                            DateFormat newFormat = new SimpleDateFormat("dd MMM yyyy");
                            Date date = null;
                            try {
                                date = readFormat.parse(dobText);
                                dobText = newFormat.format(date);
                            } catch (Exception ex) {
                                Log.d("Error", ex.getMessage());
                            }
                            dob.setText(dobText);
                        }
                    }
                }.execute();

                phone_no.setText(spManager.getPhone());
            }
        } else {
            if (spManager.getProfileURL().equals("invalid URL")) {
                circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
            } else {
                if (spManager.getSharedPreferenceExistence()) {
                    if (spManager.getProfileURL() != null && !spManager.getProfileURL().equals("invalid URL")) {
                        PicasoImageLoader.setOfflineImage(Settings.this, spManager.getProfileURL(), circleImageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                            }
                        });
                    } else {
                        if (connectionDetector.isConnectingToInternet()) {

                            Glide.with(Settings.this).load(spManager.getProfileURL()).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            }).into(circleImageView);

                        } else {
                            PicasoImageLoader.setOfflineImage(Settings.this, spManager.getProfileURL(), circleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                                }
                            });
                            dismissWaitDialog();
                        }
                    }
                }
            }
        }
        first_name.setText(spManager.getFirstname());
        last_name.setText(spManager.getLastname());
        email_id.setText(spManager.getEmail());
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dob.setText("");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                String dobText = spManager.getDOB().trim();
                if (dobText != null && !dobText.equals("")) {
                    DateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd");
                    DateFormat newFormat = new SimpleDateFormat("dd MMM yyyy");
                    Date date = null;
                    try {
                        date = readFormat.parse(dobText);
                        dobText = newFormat.format(date);
                    } catch (Exception ex) {
                        Log.d("Error", ex.getMessage());
                    }
                    dob.setText(dobText);
                }
            }
        }.execute();
        phone_no.setText(spManager.getPhone());
        //tv_save.setOnClickListener(this);
        ll_save.setOnClickListener(this);
        tv_cancle.setOnClickListener(this);
        ll_notifications.setOnClickListener(this);
        change_password.setOnClickListener(this);
        ll_downloaded_courses.setOnClickListener(this);
        dob.setOnClickListener(this);
        ll_logout.setOnClickListener(this);
        circleImageView.setOnClickListener(this);
        txt_change_photo.setOnClickListener(this);
        llhome.setOnClickListener(this);
        ll_back.setOnClickListener(this);

        first_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("", "");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("", "");
            }

            @Override
            public void afterTextChanged(Editable s) {
                buttonVisibility("show");
                if (!s.toString().equals("")) {
                    if (s.toString().contains(" ")) {
                        String preValue = first_name.getText().toString().substring(0, first_name.getText().toString().indexOf(" "));
                        if (first_name.getText().length() > 1) {
                            String postValue = first_name.getText().toString().substring(first_name.getText().toString().indexOf(" ") + 1, first_name.getText().toString().length());
                            first_name.setText(preValue + postValue);
                            first_name.setSelection(preValue.length());
                        } else {
                            first_name.setText(preValue);
                        }
                    }
                }
            }
        });

        last_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }


            @Override
            public void afterTextChanged(Editable s) {
                buttonVisibility("show");
                if (!s.toString().equals("")) {
                    if (s.toString().contains(" ")) {
                        String preValue = last_name.getText().toString().substring(0, last_name.getText().toString().indexOf(" "));
                        if (last_name.getText().length() > 1) {
                            String postValue = last_name.getText().toString().substring(last_name.getText().toString().indexOf(" ") + 1, last_name.getText().toString().length());
                            last_name.setText(preValue + postValue);
                            last_name.setSelection(preValue.length());
                        } else {
                            last_name.setText(preValue);
                        }
                    }
                }
            }
        });

        email_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                buttonVisibility("show");
                if (!s.toString().equals("")) {
                    if (s.toString().contains(" ")) {
                        String preValue = email_id.getText().toString().substring(0, email_id.getText().toString().indexOf(" "));
                        if (email_id.getText().length() > 1) {
                            String postValue = email_id.getText().toString().substring(email_id.getText().toString().indexOf(" ") + 1, email_id.getText().toString().length());
                            email_id.setText(preValue + postValue);
                            email_id.setSelection(preValue.length());
                        } else {
                            email_id.setText(preValue);
                        }
                    }
                }
            }
        });

        phone_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("char", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                buttonVisibility("show");
                if (!s.toString().equals("")) {
                    if (s.toString().contains(" ")) {
                        String preValue = phone_no.getText().toString().substring(0, phone_no.getText().toString().indexOf(" "));
                        if (phone_no.getText().length() > 1) {
                            String postValue = email_id.getText().toString().substring(phone_no.getText().toString().indexOf(" ") + 1, phone_no.getText().toString().length());
                            phone_no.setText(preValue + postValue);
                            phone_no.setSelection(preValue.length());
                        } else {
                            phone_no.setText(preValue);
                        }
                    }
                }
            }
        });
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<String>();
        categories.add(english);
        categories.add(norwegian);
        categories.add(polish);
        categories.add(korean);
        categories.add(swedish);
        categories.add(portuguese);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, categories);
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        int width = getWindowManager().getDefaultDisplay().getWidth();
        spinner.setDropDownWidth(width);
        spinner.setAdapter(dataAdapter);
        if (spManager.getLanguage().startsWith("en")) {
            spinner.setSelection(0);
        } else if (spManager.getLanguage().startsWith("nb")) {
            spinner.setSelection(1);
        } else if (spManager.getLanguage().startsWith("pl")) {
            spinner.setSelection(2);
        } else if (spManager.getLanguage().startsWith("ko")) {
            spinner.setSelection(3);
        } else if (spManager.getLanguage().startsWith("sv")) {
            spinner.setSelection(4);
        } else if (spManager.getLanguage().startsWith("pt")) {
            spinner.setSelection(5);
        }
        first_name.setOnClickListener(this);
        last_name.setOnClickListener(this);
        email_id.setOnClickListener(this);
        phone_no.setOnClickListener(this);

        //stattnet functionality
        SharedPreferences sharedpreferences = getSharedPreferences("StattnetPref", Context.MODE_PRIVATE);
        if (sharedpreferences.contains("StattnetPrefValue")) {
            String loginType = sharedpreferences.getString("StattnetPrefValue", "");
            if (loginType != null && loginType.equals("StattnetLogin")) {
                ll_logout.setVisibility(View.GONE);
            } else {
                ll_logout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String language = spManager.getLanguage();
        if (getWindow().getCurrentFocus() != null) {
            getWindow().getCurrentFocus().clearFocus();
        }
        item = parent.getItemAtPosition(position).toString();
        if (spManager.getLanguage().startsWith("en")) {
            if (!item.equals(english)) {
                buttonVisibility("show");
            }
        } else if (spManager.getLanguage().startsWith("pl")) {
            if (!item.equals(polish)) {
                buttonVisibility("show");
            }
        } else if (spManager.getLanguage().startsWith("nb")) {
            if (!item.equals(norwegian)) {
                buttonVisibility("show");
            }
        } else if (spManager.getLanguage().startsWith("ko")) {
            if (!item.equals(korean)) {
                buttonVisibility("show");
            }
        } else if (spManager.getLanguage().startsWith("sv")) {
            if (!item.equals(swedish)) {
                buttonVisibility("show");
            }
        } else if (spManager.getLanguage().startsWith("pt")) {
            if (!item.equals(portuguese)) {
                buttonVisibility("show");
            }
        }
        return;
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    public void buttonVisibility(String showHide) {
        if (showHide.equals("show")) {
            ll_back.setVisibility(View.GONE);
            llhome.setVisibility(View.GONE);
            tv_save.setVisibility(View.VISIBLE);
            tv_cancle.setVisibility(View.VISIBLE);
        } else {
            ll_back.setVisibility(View.VISIBLE);
            llhome.setVisibility(View.VISIBLE);
            tv_save.setVisibility(View.GONE);
            tv_cancle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intentback = new Intent(Settings.this, HomePage.class);
        intentback.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentback);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void setLocale(String language) {
        myLocale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        if (languageSelectionDIalog != null && languageSelectionDIalog.isShowing()) {
            languageSelectionDIalog.dismiss();
        }
        Intent refreshIntent = new Intent(this, Settings.class);
        refreshIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        refreshIntent.putExtra("From", "selfRefresh");
        startActivity(refreshIntent);
    }

    public boolean validateCredentials() {
        if (first_name.getText().toString().isEmpty()) {
            AlertDialogManager.showDialog(Settings.this, "", alertMsgFirstName, false, null);
            return false;
        }
        if (last_name.getText().toString().isEmpty()) {
            AlertDialogManager.showDialog(Settings.this, "", alertMsgLasttName, false, null);
            return false;
        }
        if (email_id.getText().toString().isEmpty() || !email_id.getText().toString().contains("@") || !email_id.getText().toString().contains(".")) {
            AlertDialogManager.showDialog(Settings.this, "", alertMsgEmail, false, null);
            return false;
        }
        if (phone_no.getText().toString().isEmpty()) {
            AlertDialogManager.showDialog(Settings.this, "", alertMsgPhone, false, null);
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_notifications:
                commonIntentMethod(NotificationSettings.class);
                break;
            case R.id.tv_cancle:
                hideKeyboard();
                first_name.setText(spManager.getFirstname().toString());
                last_name.setText(spManager.getLastname().toString());
                email_id.setText(spManager.getEmail().toString());
                String dobDate = spManager.getDOB().toString().trim();
                if (dob != null && !dob.equals("")) {
                    if (dobDate.contains("-")) {
                        DateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd");
                        DateFormat newFormat = new SimpleDateFormat("dd MMM yyyy");
                        Date date = null;
                        try {
                            date = readFormat.parse(dobDate);
                            dobDate = newFormat.format(date);
                        } catch (Exception ex) {
                            Log.d("Error", ex.getMessage());
                        }
                    }
                    dob.setText(dobDate);
                }
                //dob.setText(spManager.getDOB().toString());
                phone_no.setText(spManager.getPhone().toString());
                if (spManager.getLanguage().startsWith("en")) {
                    spinner.setSelection(0);
                } else if (spManager.getLanguage().startsWith("nb")) {
                    spinner.setSelection(1);
                } else if (spManager.getLanguage().startsWith("pl")) {
                    spinner.setSelection(2);
                } else if (spManager.getLanguage().startsWith("ko")) {
                    spinner.setSelection(3);
                } else if (spManager.getLanguage().startsWith("sv")) {
                    spinner.setSelection(4);
                } else if (spManager.getLanguage().startsWith("pt")) {
                    spinner.setSelection(5);
                }
                buttonVisibility("hide");
                if (getWindow().getCurrentFocus() != null) {
                    getWindow().getCurrentFocus().clearFocus();
                }
                break;
            case R.id.llhome:
                onBackPressed();
                break;
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.header_back_button:
                onBackPressed();
                break;
            case R.id.circleImageView:
                hideKeyboard();
                profilePopUpDialog = profilePopUpDialog();
                break;
            case R.id.txt_change_photo:
                hideKeyboard();
                profilePopUpDialog = profilePopUpDialog();
                break;
            case R.id.change_password:
                commonIntentMethod(ChangePassword.class);
                break;
            case R.id.ll_downloaded_courses:
                FromPage = "Downloads";
                SettingsPermissionsDispatcher.goToDownloadsPageWithPermissionCheck(Settings.this);
                break;
            case R.id.ll_logout:
                logOutAlert();
                break;
            case R.id.ll_save:
                if (connectionDetector.isConnectingToInternet()) {
                    if (validateCredentials()) {
                        String dobText = dob.getText().toString().trim();
                        DateFormat readFormat = new SimpleDateFormat("dd MMM yyyy");
                        DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = null;
                        try {
                            date = readFormat.parse(dobText);
                        } catch (Exception ex) {
                            Log.d("Error", ex.getMessage());
                        }
                        String formattedDate = "";
                        if (date != null) {
                            formattedDate = writeFormat.format(date);
                            dobText = formattedDate;
                        }
                        if (item == english) {
                            if (connectionDetector.isConnectingToInternet()) {
                                showWaitDialog();
                                updateUserDetails(first_name.getText().toString(), last_name.getText().toString(), email_id.getText().toString(), dobText, "en_US", phone_no.getText().toString());
                            } else {
                                dismissWaitDialog();
                                AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                            }
                        } else if (item == polish) {
                            if (connectionDetector.isConnectingToInternet()) {
                                showWaitDialog();
                                updateUserDetails(first_name.getText().toString(), last_name.getText().toString(), email_id.getText().toString(), dobText, "pl_PL", phone_no.getText().toString());
                            } else {
                                dismissWaitDialog();
                                AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                            }
                        } else if (item == norwegian) {
                            if (connectionDetector.isConnectingToInternet()) {
                                showWaitDialog();
                                updateUserDetails(first_name.getText().toString(), last_name.getText().toString(), email_id.getText().toString(), dobText, "nb_NO", phone_no.getText().toString());
                            } else {
                                dismissWaitDialog();
                                AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                            }
                        } else if (item == korean) {
                            if (connectionDetector.isConnectingToInternet()) {
                                showWaitDialog();
                                updateUserDetails(first_name.getText().toString(), last_name.getText().toString(), email_id.getText().toString(), dobText, "ko_KR", phone_no.getText().toString());
                            } else {
                                dismissWaitDialog();
                                AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                            }
                        } else if (item == swedish) {
                            if (connectionDetector.isConnectingToInternet()) {
                                showWaitDialog();
                                updateUserDetails(first_name.getText().toString(), last_name.getText().toString(), email_id.getText().toString(), dobText, "sv_SE", phone_no.getText().toString());
                            } else {
                                dismissWaitDialog();
                                AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                            }
                        } else if (item == portuguese) {
                            if (connectionDetector.isConnectingToInternet()) {
                                showWaitDialog();
                                updateUserDetails(first_name.getText().toString(), last_name.getText().toString(), email_id.getText().toString(), dobText, "pt_BR", phone_no.getText().toString());
                            } else {
                                dismissWaitDialog();
                                AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                            }
                        }
                    }
                } else {
                    AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                }
                break;
            case R.id.dob:
                hideKeyboard();
                DatePickerFragment dFragment = DatePickerFragment.getInstance();
                DatePickerDialog datePickerDialog = dFragment.showDatePicker(Settings.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int dayOfMonth, int monthOfYear, int year) {
                        SimpleDateFormat format = new SimpleDateFormat("dd MM yyyy");
                        SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yyyy");
                        dobString = year + " " + (monthOfYear + 1) + " " + dayOfMonth;
                        try {
                            getWindow().getCurrentFocus().clearFocus();
                            first_name.setCursorVisible(false);
                            Date dateDOB = format.parse(dobString);
                            String dobText = newFormat.format(dateDOB);
                            dob.setText(dobText);
                            buttonVisibility("show");
                        } catch (Exception ex) {
                            Log.d("Error", ex.getMessage());
                        }
                    }
                });
                String[] dobArray = null;
                String dobText = dob.getText().toString().trim();
                if (!dobText.contains("-")) {
                    if (dobText != null && !dobText.equals("")) {
                        String[] newDOBArray = dobText.split(" ");
                        dobArray = getDOBArray(dobText, newDOBArray[1]);
                    }
                } else {
                    dobArray = dobText.split("-");
                }
                if (dobText == null || dobText.equals("") || dobText.equals("null")) {

                } else {
                    datePickerDialog.updateDate(Integer.parseInt(dobArray[2]), Integer.parseInt(dobArray[1]) - 1, Integer.parseInt(dobArray[0]));
                }
                datePickerDialog.show();
                break;
            case R.id.first_name:
                first_name.setCursorVisible(true);
                break;
        }
    }

    public void logOutAlert() {
        AlertDialogManager.alternateCustomDialog(this, getResources().getString(R.string.logout), getResources().getString(R.string.logout_alert), true, true, new IClickListener() {
            @Override
            public void onClick() {
                try {
                    dataBaseHandlerUpdate.updateTable("OfflineDownload", spManager.getUserID(), "", "isLoggedIn", "NO");
                    dataBaseHandlerDelete.deleteTableByName("SafetyCards", "");
                    dataBaseHandlerDelete.deleteTableByName("DiplomasTable", spManager.getUserID());
                    dataBaseHandlerDelete.deleteTableByName("MyCompanyTable", spManager.getUserID());
                    dataBaseHandlerDelete.deleteTableByName("CustomerDetails", "");
                    spManager.removeSharedPreferenceByName("TotalNotification");
                } catch (Exception ex) {
                    Log.d("Error", ex.getMessage());
                } finally {
                    spManager.removeSharedPreference();
                    Intent intentLogin = new Intent(Settings.this, Login.class);
                    intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentLogin);
                    finishAffinity();
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                }
            }
        }, null, getResources().getString(R.string.yes), getResources().getString(R.string.no), "Blue", "a");
    }

    public void customToastMethod() {
        Context context = getApplicationContext();
        LayoutInflater inflater = getLayoutInflater();
        View customToastroot = inflater.inflate(R.layout.custom_tost_setting_saved, null);
        Toast customtoast = new Toast(context);
        customtoast.setView(customToastroot);
        customtoast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        customtoast.setDuration(Toast.LENGTH_SHORT);
        customtoast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            if (selectedImage != null && !selectedImage.toString().isEmpty()) {
                if (Build.VERSION.SDK_INT >= 19) {
                    final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);
                }
                String filePath = FilePickUtils.getSmartFilePath(Settings.this, selectedImage);
                final File imageFile = new File(filePath);
                if (imageFile.exists()) {
                    Intent intent = new Intent(Settings.this, ImageCropActivity.class);
                    intent.putExtra("ImageURI", selectedImage);
                    intent.putExtra("ImagePath", filePath);
                    intent.putExtra("From", "Browse");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    AlertDialogManager.showDialog(Settings.this, getResources().getString(R.string.file_error), getResources().getString(R.string.pic_loading_error), false, null);
                }
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(Settings.this, ImageCropActivity.class);
            String filePath = FilePickUtils.getSmartFilePath(Settings.this, fileUri);
            intent.putExtra("ImageURI", fileUri);
            intent.putExtra("ImagePath", filePath);
            intent.putExtra("From", "Camera");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getResources().getString(R.string.picture_not_taken), Toast.LENGTH_SHORT);
            }
        }
    }

    @NeedsPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void browsePic() {
        Intent browseDoc;
        if (Build.VERSION.SDK_INT >= 19) {
            browseDoc = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            browseDoc.addCategory(Intent.CATEGORY_OPENABLE);
            browseDoc.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            browseDoc.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        } else {
            browseDoc = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        browseDoc.setType("image/*");
        startActivityForResult(browseDoc, RESULT_LOAD_IMAGE);
    }

    @NeedsPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void capturePic() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Trainor Profile Image");
        if (mediaStorageDir.exists()) {
            DeleteRecursive(mediaStorageDir);
        }
        fileUri = new ImageCaptureClass().getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    public AlertDialog profilePopUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_change_profile_popup, null, false);
        LinearLayout ll_use_camera = (LinearLayout) view.findViewById(R.id.ll_use_camera);
        LinearLayout ll_remove_pic = (LinearLayout) view.findViewById(R.id.ll_remove_pic);
        LinearLayout ll_pick_from_gallary = (LinearLayout) view.findViewById(R.id.ll_pick_from_gallary);
        LinearLayout ll_view_pic = (LinearLayout) view.findViewById(R.id.ll_view_pic);
        if (removePhoto) {
            ll_remove_pic.setVisibility(View.GONE);
            removePhoto = true;
        } else {
            ll_remove_pic.setVisibility(View.VISIBLE);
            removePhoto = false;
        }
        ll_use_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectionDetector.isConnectingToInternet()) {
                    if (profilePopUpDialog != null && profilePopUpDialog.isShowing()) {
                        profilePopUpDialog.dismiss();
                    }
                    SettingsPermissionsDispatcher.capturePicWithPermissionCheck(Settings.this);
                } else {
                    dismissWaitDialog();
                    if (profilePopUpDialog != null && profilePopUpDialog.isShowing()) {
                        profilePopUpDialog.dismiss();
                    }
                    AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                }
            }
        });
        ll_remove_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWaitDialog();
                if (profilePopUpDialog != null && profilePopUpDialog.isShowing()) {
                    profilePopUpDialog.dismiss();
                }
                if (connectionDetector.isConnectingToInternet()) {
                    removePhoto = true;
                    updateProfileImage(null, "remove");
                } else {
                    dismissWaitDialog();
                    if (profilePopUpDialog != null && profilePopUpDialog.isShowing()) {
                        profilePopUpDialog.dismiss();
                    }
                    AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                }
            }
        });
        ll_pick_from_gallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectionDetector.isConnectingToInternet()) {
                    if (profilePopUpDialog != null && profilePopUpDialog.isShowing()) {
                        profilePopUpDialog.dismiss();
                    }
                    SettingsPermissionsDispatcher.browsePicWithPermissionCheck(Settings.this);
                } else {
                    dismissWaitDialog();
                    if (profilePopUpDialog != null && profilePopUpDialog.isShowing()) {
                        profilePopUpDialog.dismiss();
                    }
                    AlertDialogManager.showDialog(Settings.this, internetErrorTitle, internetErrorMessage, false, null);
                }
            }
        });
        ll_view_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profilePopUpDialog != null && profilePopUpDialog.isShowing()) {
                    profilePopUpDialog.dismiss();
                }
                commonIntentMethod(FullImage.class);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setView(view);
        dialog.show();
        return dialog;
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            SettingsPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
            final String permission = permissions[0];
            switch (permissions[0]) {
                case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (FromPage.equals("Downloads")) {
                            commonIntentMethod(Downloads.class);
                        }
                    } else {
                        boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(Settings.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (!neverAskAgainIsEnabled) {
                            dismissWaitDialog();
                            AlertDialogManager.showCustomDialog(Settings.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                                @Override
                                public void onClick() {
                                    Intent intent = new Intent();
                                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", Settings.this.getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            }, null, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                        } else {
                            dismissWaitDialog();
                            AlertDialogManager.showCustomDialog(Settings.this, getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                                @Override
                                public void onClick() {
                                    ActivityCompat.requestPermissions(Settings.this, new String[]{permission}, requestCode);
                                }
                            }, null, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                        }
                    }
                    break;
            }
        } catch (Exception ex) {
            Log.d("", ex.getMessage());
        }
    }

    public void getProfilePicFromURL(final String ImageURL, final String Frompage) {
        if (Frompage.equals("remove")) {
            dismissWaitDialog();
            removePhoto = true;
            Toast.makeText(Settings.this, getResources().getString(R.string.image_removed), Toast.LENGTH_SHORT).show();
            circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
            if (spManager.getSharedPreferenceExistence()) {
                SharedPreferences.Editor editor = spManager.getProfileInfoSharedPreference();
                SharedPreferenceInfo sharedPreferenceInfo = new SharedPreferenceInfo();
                sharedPreferenceInfo.Token = spManager.getToken();
                sharedPreferenceInfo.ProfilePicURL = "";
                sharedPreferenceInfo.FirstName = spManager.getFirstname();
                sharedPreferenceInfo.LastName = spManager.getLastname();
                sharedPreferenceInfo.Email = spManager.getEmail();
                sharedPreferenceInfo.dob = spManager.getDOB();
                sharedPreferenceInfo.language = spManager.getLanguage();
                sharedPreferenceInfo.Phone_no = spManager.getPhone();
                sharedPreferenceInfo.UserName = spManager.getUsername();
                sharedPreferenceInfo.UserID = spManager.getUserID();
                //changes on 27-10-2020
                sharedPreferenceInfo.emailVerified = spManager.getProfileEmailVerified();
                sharedPreferenceInfo.phoneVerified = spManager.getProfilePhoneVerified();
                spManager.removeSharedPreference();
                SharedPreferenceManager.insertValuesIntoSharedPreference(editor, sharedPreferenceInfo);
            }
        } else {
            PicasoImageLoader.getImagesFromURL(Settings.this, ImageURL, circleImageView, 400, 400, new Callback() {
                @Override
                public void onSuccess() {
                    dismissWaitDialog();
                    if (!connectionDetector.isConnectingToInternet()) {
                        circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                        Toast.makeText(Settings.this, getResources().getString(R.string.image_not_loaded), Toast.LENGTH_SHORT).show();
                    } else {
                        if (Frompage.equals("update")) {
                            removePhoto = false;
                            Toast.makeText(Settings.this, getResources().getString(R.string.image_updated), Toast.LENGTH_SHORT).show();
                        } else {
                            removePhoto = true;
                            Toast.makeText(Settings.this, getResources().getString(R.string.image_removed), Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (spManager.getSharedPreferenceExistence()) {
                        SharedPreferences.Editor editor = spManager.getProfileInfoSharedPreference();
                        SharedPreferenceInfo sharedPreferenceInfo = new SharedPreferenceInfo();
                        sharedPreferenceInfo.Token = spManager.getToken();
                        sharedPreferenceInfo.ProfilePicURL = ImageURL;
                        sharedPreferenceInfo.FirstName = spManager.getFirstname();
                        sharedPreferenceInfo.LastName = spManager.getLastname();
                        sharedPreferenceInfo.Email = spManager.getEmail();
                        sharedPreferenceInfo.dob = spManager.getDOB();
                        sharedPreferenceInfo.language = spManager.getLanguage();
                        sharedPreferenceInfo.Phone_no = spManager.getPhone();
                        sharedPreferenceInfo.UserName = spManager.getUsername();
                        sharedPreferenceInfo.UserID = spManager.getUserID();
                        //changes on 27-10-2020
                        sharedPreferenceInfo.emailVerified = spManager.getProfileEmailVerified();
                        sharedPreferenceInfo.phoneVerified = spManager.getProfilePhoneVerified();
                        spManager.removeSharedPreference();
                        SharedPreferenceManager.insertValuesIntoSharedPreference(editor, sharedPreferenceInfo);
                    }
                }

                @Override
                public void onError() {
                    dismissWaitDialog();
                    circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                    if (spManager.getSharedPreferenceExistence()) {
                        SharedPreferences.Editor editor = spManager.getProfileInfoSharedPreference();
                        SharedPreferenceInfo sharedPreferenceInfo = new SharedPreferenceInfo();
                        sharedPreferenceInfo.Token = spManager.getToken();
                        sharedPreferenceInfo.ProfilePicURL = "";
                        sharedPreferenceInfo.FirstName = spManager.getFirstname();
                        sharedPreferenceInfo.LastName = spManager.getLastname();
                        sharedPreferenceInfo.Email = spManager.getEmail();
                        sharedPreferenceInfo.dob = spManager.getDOB();
                        sharedPreferenceInfo.language = spManager.getLanguage();
                        sharedPreferenceInfo.Phone_no = spManager.getPhone();
                        sharedPreferenceInfo.UserName = spManager.getUsername();
                        sharedPreferenceInfo.UserID = spManager.getUserID();
                        //changes on 27-10-2020
                        sharedPreferenceInfo.emailVerified = spManager.getProfileEmailVerified();
                        sharedPreferenceInfo.phoneVerified = spManager.getProfilePhoneVerified();
                        spManager.removeSharedPreference();
                        SharedPreferenceManager.insertValuesIntoSharedPreference(editor, sharedPreferenceInfo);
                    }
                }
            });
        }
    }

    public void updateProfileImage(final String baseStringImage, final String fromModule) {
        final String contentType = "image/png";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_Profile_Pic_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String URL = jsonObject.getString("url");
                    getProfilePicFromURL(URL, fromModule);
                } catch (JSONException e) {
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipelayout.setRefreshing(false);
                dismissWaitDialog();
                AlertDialogManager.showDialog(Settings.this, networkError, VolleyErrorHandler.getErrorMessage(Settings.this, error), false, null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json");
                params.put("Authorization", "Bearer " + spManager.getToken());
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String strParameters = "{\"imageContent\":\"" + baseStringImage + "\",\"contentType\":\"" + contentType + "\"}";
                return strParameters.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(Settings.this);
        requestQueue11.add(stringRequest);
    }

    public void updateUserDetails(final String firstName, final String lastName, final String emailAddress, final String birthDate, final String languagepart, final String phone) {
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, WebServicesURL.FetchDetail_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dismissWaitDialog();
                customToastMethod();
                try {
                    SharedPreferences.Editor editor = spManager.getProfileInfoSharedPreference();
                    SharedPreferenceInfo sharedPreferenceInfo = new SharedPreferenceInfo();
                    sharedPreferenceInfo.Token = spManager.getToken();
                    sharedPreferenceInfo.ProfilePicURL = spManager.getProfileURL();
                    sharedPreferenceInfo.FirstName = firstName;
                    sharedPreferenceInfo.LastName = lastName;
                    sharedPreferenceInfo.Email = emailAddress;
                    sharedPreferenceInfo.dob = birthDate;
                    sharedPreferenceInfo.language = languagepart;
                    sharedPreferenceInfo.Phone_no = phone;
                    sharedPreferenceInfo.UserName = spManager.getUsername();
                    sharedPreferenceInfo.UserID = spManager.getUserID();
                    //changes on 27-10-2020
                    sharedPreferenceInfo.emailVerified = spManager.getProfileEmailVerified();
                    sharedPreferenceInfo.phoneVerified = spManager.getProfilePhoneVerified();
                    spManager.removeSharedPreference();
                    SharedPreferenceManager.insertValuesIntoSharedPreference(editor, sharedPreferenceInfo);
                    if (languagepart.startsWith("nb")) {
                        setLocale("nb");
                    } else if (languagepart.startsWith("en")) {
                        setLocale("en");
                    } else if (languagepart.startsWith("ko")) {
                        setLocale("ko");
                    } else if (languagepart.startsWith("pl")) {
                        setLocale("pl");
                    } else if (languagepart.startsWith("sv")) {
                        setLocale("sv");
                    } else if (languagepart.startsWith("pt")) {
                        setLocale("pt");
                    }
                } catch (Exception ex) {
                    dismissWaitDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipelayout.setRefreshing(false);
                dismissWaitDialog();
                AlertDialogManager.showDialog(Settings.this, networkError, VolleyErrorHandler.getErrorMessage(Settings.this, error), false, null);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + spManager.getToken());
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String str = "{\"firstname\":\"" + firstName + "\",\"lastname\":\"" + lastName + "\",\"emailAddress\":\"" + emailAddress + "\",\"birthDate\":\"" + birthDate + "\",\"language\":\"" + languagepart + "\",\"phone\":\"" + phone + "\"}";
                Log.d("update", str);
                return str.getBytes();
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue11 = Volley.newRequestQueue(Settings.this);
        requestQueue11.add(stringRequest);
    }

    public void showWaitDialog() {
        if (isWindowActiviated) {
            if (pDialog == null) {
                pDialog = new ProgressDialog(Settings.this);
            }
            if (!pDialog.isShowing()) {
                pDialog.setMessage(waitMsg);
                pDialog.setCancelable(false);
                pDialog.show();
            }
        }
    }

    public void dismissWaitDialog() {
        if (isWindowActiviated) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

    public void getUserDetails() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WebServicesURL.FetchDetail_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("profilePictureUrl").toString() != null && !jsonObject.getString("profilePictureUrl").equals("")) {
                        String emailVerified = jsonObject.getString("emailVerified") == null ? "" : jsonObject.getString("emailVerified").equals("null") ? "" : jsonObject.getString("emailVerified");
                        String phoneVerified = jsonObject.getString("phoneVerified") == null ? "" : jsonObject.getString("phoneVerified").equals("null") ? "" : jsonObject.getString("phoneVerified");

                        getUserImageFromLive(jsonObject.getString("profilePictureUrl"), spManager.getUsername(),
                                jsonObject.getString("emailAddress"), jsonObject.getString("phone"), jsonObject.getString("birthDate"), jsonObject.getString("language"), jsonObject.getString("firstname"), jsonObject.getString("lastname"),emailVerified,phoneVerified);
                    } else {
                        swipelayout.setRefreshing(false);
                        if (spManager.getSharedPreferenceExistence()) {
                            String username = spManager.getUsername();
                            SharedPreferences.Editor editor = spManager.getProfileInfoSharedPreference();
                            SharedPreferenceInfo sharedPreferenceInfo = new SharedPreferenceInfo();
                            sharedPreferenceInfo.Token = spManager.getToken();
                            sharedPreferenceInfo.ProfilePicURL = "";
                            String firstName = jsonObject.getString("firstname").equals("null") ? "" : jsonObject.getString("firstname") == null ? "" : jsonObject.getString("firstname");
                            String lastName = jsonObject.getString("lastname").equals("null") ? "" : jsonObject.getString("lastname") == null ? "" : jsonObject.getString("lastname");
                            sharedPreferenceInfo.FirstName = firstName;
                            sharedPreferenceInfo.LastName = lastName;
                            sharedPreferenceInfo.Email = jsonObject.getString("emailAddress").equals("null") ? "" : jsonObject.getString("emailAddress") == null ? "" : jsonObject.getString("emailAddress");
                            sharedPreferenceInfo.dob = jsonObject.getString("birthDate").equals("null") ? "" : jsonObject.getString("birthDate") == null ? "" : jsonObject.getString("birthDate");
                            sharedPreferenceInfo.language = jsonObject.getString("language").equals("null") ? "en" : jsonObject.getString("language") == null ? "en" : jsonObject.getString("language");
                            sharedPreferenceInfo.Phone_no = jsonObject.getString("phone").equals("null") ? "" : jsonObject.getString("phone") == null ? "" : jsonObject.getString("phone");
                            sharedPreferenceInfo.UserName = username;
                            sharedPreferenceInfo.UserID = spManager.getUserID();
                            //changes on 27-10-2020
                            sharedPreferenceInfo.emailVerified = jsonObject.getString("emailVerified").equals("null") ? "" : jsonObject.getString("emailVerified") == null ? "" : jsonObject.getString("emailVerified");
                            sharedPreferenceInfo.phoneVerified = jsonObject.getString("phoneVerified").equals("null") ? "" : jsonObject.getString("phoneVerified") == null ? "" : jsonObject.getString("phoneVerified");
                            spManager.removeSharedPreference();
                            SharedPreferenceManager.insertValuesIntoSharedPreference(editor, sharedPreferenceInfo);
                            String language = jsonObject.getString("language").equals("null") ? "en" : jsonObject.getString("language") == null ? "en" : jsonObject.getString("language");
                            if (language.startsWith("nb")) {
                                setLocale("nb");
                            } else if (language.startsWith("en")) {
                                setLocale("en");
                            } else if (language.startsWith("ko")) {
                                setLocale("ko");
                            } else if (language.startsWith("pl")) {
                                setLocale("pl");
                            } else if (language.startsWith("sv")) {
                                setLocale("sv");
                            } else if (language.startsWith("pt")) {
                                setLocale("pt");
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipelayout.setRefreshing(false);
                dismissWaitDialog();
                AlertDialogManager.showDialog(Settings.this, networkError, VolleyErrorHandler.getErrorMessage(Settings.this, error), false, null);
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(Settings.this);
        requestQueue11.add(stringRequest);
    }

    public void getUserImageFromLive(final String ImageUrl, final String username, final String email, final String phone, final String birthdate, final String language, final String FirstName, final String LastName, final String emailVerified, final String phoneVerified) {
        Glide.with(Settings.this).load(ImageUrl).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                circleImageView.setImageResource(R.drawable.ic_default_profile_pic);
                swipelayout.setRefreshing(false);
                if (spManager.getSharedPreferenceExistence()) {
                    SharedPreferences.Editor editor = spManager.getProfileInfoSharedPreference();
                    SharedPreferenceInfo sharedPreferenceInfo = new SharedPreferenceInfo();
                    sharedPreferenceInfo.Token = spManager.getToken();
                    sharedPreferenceInfo.ProfilePicURL = "";
                    sharedPreferenceInfo.FirstName = FirstName.equals("null") ? "" : FirstName == null ? "" : FirstName;
                    sharedPreferenceInfo.LastName = LastName.equals("null") ? "" : LastName == null ? "" : LastName;
                    sharedPreferenceInfo.Email = email.equals("null") ? "" : email == null ? "" : email;
                    sharedPreferenceInfo.dob = birthdate.equals("null") ? "" : birthdate == null ? "" : birthdate;
                    sharedPreferenceInfo.language = language;
                    sharedPreferenceInfo.Phone_no = phone.equals("null") ? "" : phone == null ? "" : phone;
                    sharedPreferenceInfo.UserName = username;
                    sharedPreferenceInfo.UserID = spManager.getUserID();
                    //changes on 27-10-2020
                    sharedPreferenceInfo.emailVerified = emailVerified;
                    sharedPreferenceInfo.phoneVerified = phoneVerified;
                    spManager.removeSharedPreference();
                    SharedPreferenceManager.insertValuesIntoSharedPreference(editor, sharedPreferenceInfo);
                    if (language == null || language.equals("null") || language.equals("")) {
                        setLocale("en");
                    } else {
                        if (language.startsWith("nb")) {
                            setLocale("nb");
                        } else if (language.startsWith("en")) {
                            setLocale("en");
                        } else if (language.startsWith("ko")) {
                            setLocale("ko");
                        } else if (language.startsWith("pl")) {
                            setLocale("pl");
                        } else if (language.startsWith("sv")) {
                            setLocale("sv");
                        } else if (language.startsWith("pt")) {
                            setLocale("pt");
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                swipelayout.setRefreshing(false);
                if (spManager.getSharedPreferenceExistence()) {
                    SharedPreferences.Editor editor = spManager.getProfileInfoSharedPreference();
                    SharedPreferenceInfo sharedPreferenceInfo = new SharedPreferenceInfo();
                    sharedPreferenceInfo.Token = spManager.getToken();
                    sharedPreferenceInfo.ProfilePicURL = ImageUrl;
                    sharedPreferenceInfo.FirstName = FirstName.equals("null") ? "" : FirstName == null ? "" : FirstName;
                    sharedPreferenceInfo.LastName = LastName.equals("null") ? "" : LastName == null ? "" : LastName;
                    sharedPreferenceInfo.Email = email.equals("null") ? "" : email == null ? "" : email;
                    sharedPreferenceInfo.dob = birthdate.equals("null") ? "" : birthdate == null ? "" : birthdate;
                    sharedPreferenceInfo.language = language;
                    sharedPreferenceInfo.Phone_no = phone.equals("null") ? "" : phone == null ? "" : phone;
                    sharedPreferenceInfo.UserName = username;
                    sharedPreferenceInfo.UserID = spManager.getUserID();
                    //changes on 27-10-2020
                    sharedPreferenceInfo.emailVerified = emailVerified;
                    sharedPreferenceInfo.phoneVerified = phoneVerified;
                    spManager.removeSharedPreference();
                    SharedPreferenceManager.insertValuesIntoSharedPreference(editor, sharedPreferenceInfo);
                    if (language == null || language.equals("null") || language.equals("")) {
                        setLocale("en");
                    } else {
                        if (language.startsWith("nb")) {
                            setLocale("nb");
                        } else if (language.startsWith("en")) {
                            setLocale("en");
                        } else if (language.startsWith("ko")) {
                            setLocale("ko");
                        } else if (language.startsWith("pl")) {
                            setLocale("pl");
                        } else if (language.startsWith("sv")) {
                            setLocale("sv");
                        } else if (language.startsWith("pt")) {
                            setLocale("pt");
                        }
                    }
                }
                return false;
            }
        }).into(circleImageView);
        swipelayout.setRefreshing(false);
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(Settings.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm.isAcceptingText()) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
    }

    @NeedsPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void goToDownloadsPage() {
        dataBaseHandlerDelete.deleteCoursesFromTable("offline");
        dataBaseHandlerDelete.deleteCoursesFromTable("CourseDownload");
        dataBaseHandlerDelete.deleteCoursesFromTable("SCORMTable");
        commonIntentMethod(Downloads.class);
    }

    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                DeleteRecursive(child);
            }
        fileOrDirectory.delete();
    }

    public String[] getDOBArray(String dobText, String month) {
        String[] dobArray = null;
        DateFormat readFormat = null;
        DateFormat writeFormat = null;
        if (month.contains(".")) {
            dobText.replace(".", "");
        }
        readFormat = new SimpleDateFormat("dd MMM yyyy");
        writeFormat = new SimpleDateFormat("dd MM yyyy");
        Date date = null;
        try {
            date = readFormat.parse(dobText);
        } catch (Exception ex) {
            Log.d("Error", ex.getMessage());
        }
        String formattedDate = "";
        if (date != null) {
            formattedDate = writeFormat.format(date);
            dobArray = formattedDate.split(" ");
        }
        return dobArray;
    }
}