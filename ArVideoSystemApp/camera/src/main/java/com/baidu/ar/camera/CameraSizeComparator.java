/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.ar.camera;

import android.hardware.Camera;

import java.util.Comparator;

class CameraSizeComparator implements Comparator<Camera.Size> {

    private boolean mOrderByASC = true;

    public CameraSizeComparator(boolean orderByASC) {
        mOrderByASC = orderByASC;
    }

    public int compare(Camera.Size size1, Camera.Size size2) {
        if (mOrderByASC) {
            return size1.width - size2.width;
        } else {
            return size2.width - size1.width;
        }
    }
}
