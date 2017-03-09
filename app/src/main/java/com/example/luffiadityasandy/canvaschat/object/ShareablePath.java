package com.example.luffiadityasandy.canvaschat.object;

import android.content.pm.PackageItemInfo;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by Luffi Aditya Sandy on 07/03/2017.
 */

public class ShareablePath {
    Paint paint;
    Path path;
    String firebaseKey;

    public ShareablePath(Paint paint, Path path, String firebaseKey) {
        this.paint = paint;
        this.path = path;
        this.firebaseKey = firebaseKey;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
