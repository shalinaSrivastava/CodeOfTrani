package com.elearn.trainor.HelperClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

public class
DBCopierToSD {
    // function to copy database into sdcard
    public void CopyDBToSDCard() {
        String sourceLocation = "/data/data/com.elearn.trainor/databases/TrainorDB.db";// Your database path
        String destLocation = "TrainorDB.db";
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                File source = new File(sourceLocation);
                File dest = new File(sd + "/" + destLocation);
                if (!dest.exists()) {
                    dest.createNewFile();
                }
                if (source.exists()) {
                    InputStream src = new FileInputStream(source);
                    OutputStream dst = new FileOutputStream(dest);
                    // Copy the bits from instream to outstream
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = src.read(buf)) > 0) {
                        dst.write(buf, 0, len);
                    }
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
