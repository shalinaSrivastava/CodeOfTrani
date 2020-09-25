package com.elearn.trainor.HelperClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import it.sephiroth.android.library.picasso.Callback;
import it.sephiroth.android.library.picasso.NetworkPolicy;
import it.sephiroth.android.library.picasso.Picasso;

public class PicasoImageLoader {
    public static final PicasoImageLoader instance = new PicasoImageLoader();

    private PicasoImageLoader() {
    }

    public static PicasoImageLoader getInstance(Context con) {
        return instance;
    }

    public static Drawable getImagesFromURL(Context context, String imageURL, ImageView itemImageView, final int width, final int height, Callback callback) {
        if (width != 0 && height != 0) {
            Picasso.with(context)
                    .load(imageURL).resize(width, height)
                    .into(itemImageView, callback);
        } else {
            Picasso.with(context)
                    .load(imageURL)
                    .into(itemImageView, callback);
        }
        return itemImageView.getDrawable();
    }

    public static void setOfflineImage(Context context, final String imageURL, ImageView itemImageView,Callback callback) {
        Picasso.with(context).
                load(imageURL)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .resize(400, 400)
                .centerCrop()
                .into(itemImageView,callback);
    }
}
