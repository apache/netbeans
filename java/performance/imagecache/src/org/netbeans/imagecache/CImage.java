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
 * CImage.java
 *
 * Created on February 17, 2004, 3:47 AM
 */

package org.netbeans.imagecache;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.Vector;

/**  XXX this class is only for diagnostics - delete it when stabilized.
 *
 * @author  tim
 */
public class CImage extends BufferedImage {
    private Raster raster;
    /** Creates a new instance of CImage */
    public CImage (ColorModel cm,
                          WritableRaster raster,
                          boolean isRasterPremultiplied,
                          Hashtable properties) {
       super (cm, raster, isRasterPremultiplied, properties);
       this.raster = raster;
    }

    public Raster getData() {
        return raster;
    }

    public Vector getSources() {
        System.err.println("GetSources...");
        Thread.dumpStack();
        return null;
    }    
    
    public ImageProducer getSource() {
        System.err.println("GET SOURCE");
        Thread.dumpStack();
        return null;
    }
}
