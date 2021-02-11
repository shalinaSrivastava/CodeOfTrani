package com.elearn.trainor.BaseAdapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elearn.trainor.HelperClasses.SharedPreferenceManager;
import com.elearn.trainor.MyCompany.MessageAndDocumentActivity;
import com.elearn.trainor.PropertyClasses.MyCompanyProperty;
import com.elearn.trainor.R;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyCmpnyDocumentRecyclerViewAdapter extends RecyclerView.Adapter<MyCmpnyDocumentRecyclerViewAdapter.MyViewHolder> {
    private List<MyCompanyProperty> documentCardList;
    Context context;
    public String type;
    SharedPreferenceManager spManager;

    public MyCmpnyDocumentRecyclerViewAdapter(Context con, List<MyCompanyProperty> documentCardList, String type) {
        this.context = con;
        this.documentCardList = documentCardList;
        this.type = type;
        spManager = new SharedPreferenceManager(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout itemView = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_my_company_document, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final MyCompanyProperty documentCardsProperty = documentCardList.get(position);
        if (type.equals("Documents")) {
            holder.specificDocumentCard.setVisibility(View.VISIBLE);
            holder.fileName.setText(documentCardsProperty.name);

            // new 27-04-2020
            if(!documentCardsProperty.lastModified.equals("")){
                try {
                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                    DateFormat outputFormat = new SimpleDateFormat("dd/MM-yyyy");
                    Date date = inputFormat.parse(documentCardsProperty.lastModified);
                    String outputDateStr = outputFormat.format(date);
                    holder.fileSize.setText(context.getResources().getString(R.string.date_documents) + " " + outputDateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else{
                holder.fileSize.setText("");
            }
            //holder.fileSize.setText("");
            holder.specificDocumentCard.setTag(documentCardsProperty);
            holder.ll_document_view.setTag(documentCardsProperty);
            if (documentCardsProperty.fileName.contains(".pdf")) {
                renderImage(holder, documentCardsProperty.fileName);
            }
            if (documentCardsProperty.description.equals("") || documentCardsProperty.description.equals("null")) {
                holder.discription.setVisibility(View.GONE);
            } else {
                holder.discription.setText(documentCardsProperty.description);
            }
            holder.specificDocumentCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout cardView = (LinearLayout) view;
                    MyCompanyProperty info = (MyCompanyProperty) cardView.getTag();
                    String fileName = info.fileName;
                    MessageAndDocumentActivity.getInstance().startDownloadingWithPermission(documentCardsProperty, info.downloadUrl, fileName, true);
                    if (documentCardsProperty.fileName.contains(".pdf")) {
                        renderImage(holder, documentCardsProperty.fileName);
                    }
                }
            });
            holder.ll_document_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout ll_View = (LinearLayout) view;
                    MyCompanyProperty iinfo = (MyCompanyProperty) ll_View.getTag();
                    String file_name = iinfo.fileName;
                    MessageAndDocumentActivity.getInstance().startDownloadingWithPermission(documentCardsProperty, iinfo.downloadUrl, file_name, true);
                    if (documentCardsProperty.fileName.contains(".pdf")) {
                        renderImage(holder, documentCardsProperty.fileName);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return documentCardList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView fileName, fileSize, date, discription;
        LinearLayout specificDocumentCard, ll_document_view;
        RelativeLayout documentCard;
        ImageView img_document_view;

        public MyViewHolder(RelativeLayout view) {
            super(view);
            documentCard = view;
            fileName = (TextView) documentCard.findViewById(R.id.fileName);
            fileSize = (TextView) documentCard.findViewById(R.id.fileSize);
            specificDocumentCard = (LinearLayout) documentCard.findViewById(R.id.documentCard);
            discription = (TextView) documentCard.findViewById(R.id.discription);
            ll_document_view = (LinearLayout) documentCard.findViewById(R.id.ll_document_view);
            img_document_view = (ImageView) documentCard.findViewById(R.id.img_document_view);
        }
    }

    public void renderImage(final MyViewHolder holder, final String fileName) {
        new AsyncTask<Bitmap, Bitmap, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Bitmap... voids) {
                File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.elearn.trainor/files/MyTrainor/" + spManager.getUserID() + "/.MyCompany/" + fileName);
                if (file.exists()) {
                    ArrayList<Bitmap> bitmaps = new ArrayList<>();
                    bitmaps = pdfToBitmap(file);
                    if (bitmaps.size() > 0) {
                        return bitmaps.get(0);
                    }
                    return null;
                } else {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) {
                    holder.img_document_view.setImageBitmap(bitmap);
                }
            }
        }.execute();
    }

    public ArrayList<Bitmap> pdfToBitmap(File pdfFile) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
            Bitmap bitmap;
            PdfRenderer.Page page = renderer.openPage(0);
            /*int width = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
            int height = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();*/
            bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_4444);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            bitmaps.add(bitmap);
            page.close();
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bitmaps;
    }
}