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
 * CRaster.java
 *
 * Created on February 17, 2004, 3:20 AM
 */

package org.netbeans.imagecache;

import java.awt.Point;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import sun.awt.image.SunWritableRaster;

/**
 *
 * @author  tim
 */
public class CRaster extends SunWritableRaster {

    /** Creates a new instance of CRaster */
    public CRaster(IntBuffer buf, int width, int height) {
        super (new CSampleModel(width, height), new CDataBuffer(buf, width, height), new Point(0,0));
    }

    public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray) {
        System.err.println("GET SAMPLES: " + x + "," + y);
        return super.getSamples(x,y,w,h,b,iArray);
    }

}
