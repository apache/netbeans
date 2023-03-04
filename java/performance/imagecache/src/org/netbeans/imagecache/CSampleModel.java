/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * CSampleModel.java
 *
 * Created on February 17, 2004, 2:34 AM
 */

package org.netbeans.imagecache;

import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.nio.IntBuffer;

/**
 *
 * @author  tim
 */
public class CSampleModel extends SinglePixelPackedSampleModel {
    private IntBuffer buf;
    private int width;
    private int height;

    static final int[] MASKS = new int[] {
        0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000
    };

    /** Creates a new instance of CSampleModel */
    public CSampleModel(int width, int height) {
        super (DataBuffer.TYPE_INT, width, height, MASKS);
        this.buf = buf;
        this.width = width;
        this.height = height;
    }

    public int getSampleSize(int band) {
        return 8;
    }

    public int[] getPixels(int x, int y, int w, int h,
                           int iArray[], DataBuffer data) {
        System.err.println("GetPixels: " + x + "," + y + "," + w + "," + h + " iArray: " + iArray + " data" + data);
                               /*
        if ((x < 0) || (y < 0) || (x + w > width) || (y + h > height)) {
            throw new ArrayIndexOutOfBoundsException
                ("Coordinate out of bounds!");
        }
        int pixels[];
        if (iArray != null) {
           pixels = iArray;
        } else {
           pixels = new int [w*h*numBands];
        }
        int lineOffset = y*scanlineStride + x;
        int dstOffset = 0;

        for (int i = 0; i < h; i++) {
           for (int j = 0; j < w; j++) {
              int value = data.getElem(lineOffset+j);
              for (int k=0; k < numBands; k++) {
                  pixels[dstOffset++] =
                     ((value & bitMasks[k]) >>> bitOffsets[k]);
              }
           }
           lineOffset += scanlineStride;
        }
        return pixels;
                                */
        return super.getPixels (x,y,w,h,iArray, data);
    }    
    
    //XXX do some optimized implementations of fetching pixels    
}
