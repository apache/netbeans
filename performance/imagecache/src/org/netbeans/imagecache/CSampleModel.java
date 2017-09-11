/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
