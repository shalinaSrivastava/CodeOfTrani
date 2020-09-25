package com.elearn.trainor.SettingModule;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elearn.trainor.HelperClasses.WebServicesURL;
import com.elearn.trainor.HelperClasses.AlertDialogManager;
import com.elearn.trainor.HelperClasses.ConnectionDetector;
import com.elearn.trainor.HelperClasses.IClickListener;
import com.elearn.trainor.HelperClasses.ImageConverter;
import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.HelperClasses.VolleyErrorHandler;
import com.elearn.trainor.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImageCropActivity extends AppCompatActivity {
    ImageCropView imgCrop;
    boolean isWindowActiviated = false;
    ProgressDialog pDialog;
    ConnectionDetector connectionDetector;
    String networkError, internetErrorTitle, internetErrorMessage, ImagePath;
    SharedPreferenceManager spManager;
    LinearLayout ll_btn_root;
    Button crop_btn, btn_cancel;
    ProgressBar loading_spinner;
    TextView txtLoading;
    FirebaseAnalytics analytics;

    //Uri fileImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        getControls();
        cropImage();
    }

    public void getControls() {
        analytics = FirebaseAnalytics.getInstance(this);
        internetErrorTitle = getResources().getString(R.string.internetErrorTitle);
        internetErrorMessage = getResources().getString(R.string.internetErrorMessage);
        networkError = getResources().getString(R.string.networkError);
        isWindowActiviated = true;
        spManager = new SharedPreferenceManager(ImageCropActivity.this);
        connectionDetector = new ConnectionDetector(ImageCropActivity.this);
        //fileImageUri = (Uri) getIntent().getParcelableExtra("ImageURI");
        ImagePath = getIntent().getStringExtra("ImagePath");
        imgCrop = (ImageCropView) findViewById(R.id.image);
        ll_btn_root = (LinearLayout) findViewById(R.id.ll_btn_root);
        crop_btn = (Button) findViewById(R.id.crop_btn);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        loading_spinner = (ProgressBar) findViewById(R.id.loading_spinner);
        txtLoading = (TextView) findViewById(R.id.txtLoading);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isWindowActiviated = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isWindowActiviated = true;
        analytics.setCurrentScreen(this, "ImageCrop", this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isWindowActiviated = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isWindowActiviated = false;
    }

    public void cropImage() {
        File imageFile = new File(ImagePath);
        if (imageFile.exists()) {
            imgCrop.setImageFilePath(ImagePath);
            if (isPossibleCrop(16, 9)) {
                imgCrop.setAspectRatio(16, 9);
            } else if (isPossibleCrop(1, 1)) {
                imgCrop.setAspectRatio(1, 1);
            }
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissWaitDialog();
                    Intent intent = new Intent(ImageCropActivity.this, Settings.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
            crop_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imgCrop.setScaleEnabled(false);
                    imgCrop.setActivated(false);
                    imgCrop.setEnabled(false);
                    imgCrop.setScrollEnabled(false);
                    loading_spinner.setVisibility(View.VISIBLE);
                    ll_btn_root.setVisibility(View.GONE);
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            loading_spinner.setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            if (!imgCrop.isChangingScale()) {
                                Bitmap b = imgCrop.getCroppedImage();
                                if (connectionDetector.isConnectingToInternet()) {
                                    byte[] byteArray = ImageConverter.convertBitmapToByteArray(b);
                                    Bitmap compressedImage = ImageConverter.decodeByteArray(byteArray);
                                    String ImgStr = ImageConverter.BitmapToBase64StringConversion(compressedImage);
                                    updateProfileImage(ImgStr);
                                   /* File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Trainor Profile Image");
                                    DeleteRecursive(mediaStorageDir);*/
                                } else {
                                    dismissWaitDialog();
                                    AlertDialogManager.showDialog(ImageCropActivity.this, internetErrorTitle, internetErrorMessage, false, new IClickListener() {
                                        @Override
                                        public void onClick() {
                                            loading_spinner.setVisibility(View.GONE);
                                            ll_btn_root.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            } else {
                                dismissWaitDialog();
                                AlertDialogManager.showDialog(ImageCropActivity.this, getResources().getString(R.string.image_compression_error), getResources().getString(R.string.image_not_supported), false, new IClickListener() {
                                    @Override
                                    public void onClick() {
                                        Intent intent = new Intent(ImageCropActivity.this, Settings.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }
                    }.execute();
                }
            });
        } else {
            AlertDialogManager.showDialog(ImageCropActivity.this, getResources().getString(R.string.file_not_found), "", false, new IClickListener() {
                @Override
                public void onClick() {
                    dismissWaitDialog();
                    Intent intent = new Intent(ImageCropActivity.this, Settings.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }
    }

    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                DeleteRecursive(child);
            }
        fileOrDirectory.delete();
        // customtoast();
    }

    private boolean isPossibleCrop(int widthRatio, int heightRatio) {
        Bitmap bitmap = imgCrop.getViewBitmap();
        if (bitmap == null) {
            return false;
        }
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        return !(bitmapWidth < widthRatio && bitmapHeight < heightRatio);
    }

    public void updateProfileImage(final String baseStringImage) {
        final String contentType = "image/png";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, WebServicesURL.Update_Profile_Pic_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    dismissWaitDialog();
                    JSONObject jsonObject = new JSONObject(response);
                    Intent intent = new Intent(ImageCropActivity.this, Settings.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("From", "ImageCrop");
                    intent.putExtra("ImageURL", jsonObject.getString("url"));
                    startActivity(intent);
                } catch (Exception e) {
                    dismissWaitDialog();
                    AlertDialogManager.showDialog(ImageCropActivity.this, getResources().getString(R.string.image_crop_exception), e.getMessage().toString(), false, new IClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(ImageCropActivity.this, Settings.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissWaitDialog();
                AlertDialogManager.showDialog(ImageCropActivity.this, networkError, VolleyErrorHandler.getErrorMessage(ImageCropActivity.this, error), false, new IClickListener() {
                    @Override
                    public void onClick() {
                        Intent intent = new Intent(ImageCropActivity.this, Settings.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
            }
        })

        {
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
        RequestQueue requestQueue11 = Volley.newRequestQueue(ImageCropActivity.this);
        requestQueue11.add(stringRequest);
    }

    public void dismissWaitDialog() {
        if (isWindowActiviated) {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() {

    }
}


