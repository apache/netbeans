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


package org.netbeans.modules.image;


import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.ImageIcon;


/**
 * ImageIcon with serialization.
 *
 * @author Petr Hamernik, Michael Wever
 * @author  Marian Petras
 */
class NBImageIcon extends ImageIcon implements Serializable {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -1730253055388017036L;

    /** Appropriate image data object */
    ImageDataObject obj;

    /**
     * Loads an image from an <code>ImageDataObject</code>.
     * If an error occures during reading the image, an exception is thrown.
     * If the image format is not supported, <code>null</code> is returned.
     *
     * @param  obj  <code>ImageDataObject</code> to load the image from
     * @return  loaded image if loaded successfully,
     *          or <code>null</code> if no registered <code>ImageReader</code>
     *          claims to be able to read the image
     * @exception  java.io.IOException
     *             if an error occurs during reading the image
     * @see  javax.imageio.ImageIO#read(java.io.InputStream)
     */
    public static NBImageIcon load(ImageDataObject obj) throws IOException {
        Image image = obj.getImage();
        return (image != null) ? new NBImageIcon(obj, image) : null;
    }
    
    /** Construct a new icon.
     * @param obj the data object to represent the image in
     */
    private NBImageIcon(ImageDataObject obj, Image image) {
        //super(obj.getImageURL()); // PENDING for the time URL is incorrectly cached (in Toolkit)
        super(image);  //mw
        this.obj = obj;
    }
    
    
    /** Get an object to be written to the stream instead of this object. */
    public Object writeReplace() {
        return new ResolvableHelper(obj);
    }

    
    /** Helper class for serialization. */
    static class ResolvableHelper implements Serializable {
        
        /** generated Serialized Version UID. */
        static final long serialVersionUID = -1120520132882774882L;
        
        /** serializable data object. */
        ImageDataObject obj;
        
        /** Constructs ResolvableHelper object for given ImageDataObject. */
        ResolvableHelper(ImageDataObject obj) {
            this.obj = obj;
        }

        /** Restore with the same data object. */
        public Object readResolve() {
            Image image;
            try {
                image = obj.getImage();
            } catch (IOException ex) {
                image = null;
            }
            return new NBImageIcon(
                    obj,
                    (image != null) ? image : new ImageIcon().getImage());
        }
    } // End of nested class ResolvableHelper.
}
