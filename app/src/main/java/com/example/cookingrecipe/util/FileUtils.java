package com.example.cookingrecipe.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {
    public static File copyToTempFile(Context context, Uri uri, String nameHint) throws Exception {
        File temp = File.createTempFile("upload_", nameHint, context.getCacheDir());
        try (InputStream input = context.getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(temp)) {
            if (input == null) throw new IllegalStateException("Unable to open file");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = input.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
        }
        return temp;
    }
}
