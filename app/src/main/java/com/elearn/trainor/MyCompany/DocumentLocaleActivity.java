package com.elearn.trainor.MyCompany;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.BaseAdapters.DocumentLocaleAdapter;
import com.elearn.trainor.DBHandler.DataBaseHandlerSelect;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HomePage;
import com.elearn.trainor.R;

import java.util.List;

public class DocumentLocaleActivity extends AppCompatActivity implements View.OnClickListener {
    private static DocumentLocaleActivity instance;
    RecyclerView locale_recycler_view;
    SharedPreferenceManager spManager;
    DataBaseHandlerSelect dbSelect;
    RelativeLayout tbl_actionbar;
    LinearLayout ll_back, llhome;
    TextView text_header, tv_no_document;
    String CustomerID, CompanyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_locale);
        text_header = (TextView) findViewById(R.id.text_header);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        ll_back.setOnClickListener(this);
        llhome = (LinearLayout) findViewById(R.id.llhome);
        llhome.setOnClickListener(this);
        int actionBarBackground = getResources().getColor(R.color.my_company);
        tbl_actionbar = (RelativeLayout) findViewById(R.id.tbl_actionbar);
        tv_no_document = (TextView) findViewById(R.id.tv_no_document);
        tbl_actionbar.setBackgroundColor(actionBarBackground);
        spManager = new SharedPreferenceManager(DocumentLocaleActivity.this);
        dbSelect = new DataBaseHandlerSelect(DocumentLocaleActivity.this);
        CustomerID = getIntent().getStringExtra("CustomerID");
        CompanyName = getIntent().getStringExtra("CompanyName");
        text_header.setText(CompanyName);
        List<String> localeList = dbSelect.documentLocaleList(CustomerID, spManager.getUserID());
        if(localeList.size()>0){
            tv_no_document.setVisibility(View.GONE);
        }else{
            tv_no_document.setVisibility(View.VISIBLE);
        }
        locale_recycler_view = (RecyclerView) findViewById(R.id.locale_recycler_view);
        locale_recycler_view.setLayoutManager(new LinearLayoutManager(DocumentLocaleActivity.this));
        DocumentLocaleAdapter adapter = new DocumentLocaleAdapter(DocumentLocaleActivity.this, localeList);
        locale_recycler_view.setAdapter(adapter);
        instance = this;
    }

    public static synchronized DocumentLocaleActivity getInstance() {
        if (instance == null) {
            instance = new DocumentLocaleActivity();
        }
        return instance;
    }

    public void commonIntentMethod(Class activity) {
        Intent intent = new Intent(DocumentLocaleActivity.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        this.finishAffinity();
    }

    public void goToNext(String locale) {
        Intent intent = new Intent(DocumentLocaleActivity.this, MessageAndDocumentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("CustomerID", CustomerID);
        intent.putExtra("CompanyName", CompanyName);
        intent.putExtra("BackKey", "LocalePage");
        intent.putExtra("Locale", locale);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    public void onBackPressed() {
        commonIntentMethod(CompanyList.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                onBackPressed();
                break;
            case R.id.llhome:
                commonIntentMethod(HomePage.class);
                break;
        }
    }
}
