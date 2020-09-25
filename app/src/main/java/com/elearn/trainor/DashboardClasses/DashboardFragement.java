package com.elearn.trainor.DashboardClasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.elearn.trainor.ConcoPhilips.ConcoPhilips;
import com.elearn.trainor.Diploma.Diploma;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.CourseModule.*;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.MyCompany.CompanyList;
import com.elearn.trainor.NeedSupport;
import com.elearn.trainor.PropertyClasses.CustomerDetailsProperty;
import com.elearn.trainor.SafetyCards.SafetyCards;
import com.elearn.trainor.R;
import com.elearn.trainor.ToolBoxModule.ToolBox;

import com.elearn.trainor.DBHandler.*;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class DashboardFragement extends Fragment implements View.OnClickListener {
    public static DashboardFragement instance = new DashboardFragement();
    public boolean isFragmentAcitve = false;
    public LinearLayout lltrionor_popup, lltools;
    public RelativeLayout llcourses, llsafety_cards, llmy_company, rl_diploma,rl_conco;
    DataBaseHandlerSelect dbSelect;
    private ProgressDialog pDialog;
    SharedPreferenceManager spManager;
    FirebaseAnalytics analytics;
    View view;
    String concoUser;
    List<CustomerDetailsProperty> copUserList;



    public DashboardFragement() {

    }

    @Override
    public void onStart() {
        super.onStart();
        isFragmentAcitve = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentAcitve = true;
        instance = this;
    }

    @Override
    public void onStop() {
        super.onStop();
        isFragmentAcitve = false;
    }

    public static DashboardFragement getInstance() {
        if (instance == null) {
            instance = new DashboardFragement();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        analytics = FirebaseAnalytics.getInstance(getActivity());

        dbSelect = new DataBaseHandlerSelect(getActivity());
        spManager = new SharedPreferenceManager(getActivity());

        copUserList = new ArrayList<>();
        copUserList.clear();
        copUserList = dbSelect.getCompUser("true");
        if(copUserList.size()>0){
            view = inflater.inflate(R.layout.cono_fragment_dashboard_fragement, container, false);
            rl_conco = (RelativeLayout) view.findViewById(R.id.rl_conco);
            rl_conco.setOnClickListener(this);
        }else{
            view = inflater.inflate(R.layout.fragment_dashboard_fragement, container, false);
        }

      /*  badgeCourse = (NotificationBadge) view.findViewById(R.id.badgeCourse);
        badge_safetycards = (NotificationBadge) view.findViewById(R.id.badge_safetycards);
        badge_diploma = (NotificationBadge) view.findViewById(R.id.badge_diploma);*/
        // courses
        llcourses = (RelativeLayout) view.findViewById(R.id.llcourses);
        llmy_company = (RelativeLayout) view.findViewById(R.id.llmy_company);
        llsafety_cards = (RelativeLayout) view.findViewById(R.id.llsafety_cards);
        rl_diploma = (RelativeLayout) view.findViewById(R.id.rl_diploma);

        //llSettings = (LinearLayout) view.findViewById(R.id.llsettings);
        lltools = (LinearLayout) view.findViewById(R.id.lltools);
        lltrionor_popup = (LinearLayout) view.findViewById(R.id.lltrionor_popup);
        llcourses.setOnClickListener(this);
        //llSettings.setOnClickListener(this);
        lltools.setOnClickListener(this);
        llmy_company.setOnClickListener(this);
        llsafety_cards.setOnClickListener(this);
        rl_diploma.setOnClickListener(this);
        isFragmentAcitve = true;
        lltrionor_popup.setOnClickListener(this);
        if (getArguments().getString("ShowAnimation") != null && getArguments().getString("ShowAnimation").equals("Yes")) {
            Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.dashboard_bottom_up_anim);
            bottomUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    final HomePage page = HomePage.getInstance();
                    if (page != null) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                page.txtProfile.setText(getResources().getString(R.string.profile));
                                page.txt_setting.setText(getResources().getString(R.string.setting));
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                //page.saveImageIntoDatabase();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                            }
                        }.execute();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.setAnimation(bottomUp);
            view.startAnimation(bottomUp);
            view.setVisibility(View.VISIBLE);
        } else {
            HomePage.getInstance().txtProfile.setText(getResources().getString(R.string.profile));
            HomePage.getInstance().txt_setting.setText(getResources().getString(R.string.setting));
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lltrionor_popup:
                Bundle bundle = new Bundle();
                bundle.putString("Contact_Us", "Yes");
                analytics.logEvent("Contact_Us", bundle);
                commonIntentMethod(getActivity(), NeedSupport.class, "HomePage");
                break;
            case R.id.llsafety_cards:
                commonIntentMethod(getActivity(), SafetyCards.class, "");
                break;
            case R.id.lltools:
                lltools.setClickable(false);
                DashboardFragementPermissionsDispatcher.goToToolBoxPageWithPermissionCheck(DashboardFragement.this);
                break;
            case R.id.llmy_company:
                commonIntentMethod(getActivity(), CompanyList.class, "");
                break;
            case R.id.llcourses:
                commonIntentMethod(getActivity(), Courses.class, "");
                break;
            case R.id.rl_diploma:
                commonIntentMethod(getActivity(), Diploma.class, "HomePage");
                break;
            case R.id.rl_conco:
                commonIntentMethod(getActivity(), ConcoPhilips.class, "HomePage");
                break;
        }
    }

    @NeedsPermission({android.Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void goToToolBoxPage() {
        commonIntentMethod(getActivity(), ToolBox.class, "HomePage");
    }

    public void commonIntentMethod(Context con, Class activity, String From) {
        Intent intent = new Intent(con, activity);
        intent.putExtra("From", From);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }


    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DashboardFragementPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        final String permission = permissions[0];
        switch (permissions[0]) {
            case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    lltools.setClickable(true);
                    commonIntentMethod(getActivity(), ToolBox.class, "HomePage");
                } else {
                    lltools.setClickable(true);
                    boolean neverAskAgainIsEnabled = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (!neverAskAgainIsEnabled) {
                        AlertDialogManager.showCustomDialog(getActivity(), getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                intent.setData(uri);
                                getActivity().startActivity(intent);
                            }
                        }, null, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                    } else {
                        AlertDialogManager.showCustomDialog(getActivity(), getString(R.string.permissionTitle), getString(R.string.writePermissionMessage), true, new IClickListener() {
                            @Override
                            public void onClick() {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
                            }
                        }, null, getResources().getString(R.string.Retry), getResources().getString(R.string.deny), "");
                    }
                }
                break;
        }
    }
}
