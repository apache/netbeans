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
package org.netbeans.modules.profiler.spi;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class IconsProvider {
    
    /**
     * Returns an Image instance according to the provided key.
     * 
     * @param key image key
     * @return Image instance according to the provided key or null if no image is provided for the key by this provider
     */
    public abstract Image getImage(String key);
    
    /**
     * Returns path to image resource without leading slash according to the provided key.
     * 
     * @param key image key
     * @return path to image resource without leading slash according to the provided key or null if no image is provided for the key by this provider
     */
    public abstract String getResource(String key);
    
    
    /**
     * Basic implementation of a simple IconsProvider supporting statically defined and dynamically generated images.
     */
    public abstract static class Basic extends IconsProvider {
        
        private Map<String, String> images;

        @Override
        public final Image getImage(String key) {
            String resource = getResource(key);
            if (resource == null) return getDynamicImage(key);
            else return ImageUtilities.loadImage(resource, true);
        }

        @Override
        public final String getResource(String key) {
            return getImageCache().get(key);
        }

        private Map<String, String> getImageCache() {
            synchronized (this) {
                if (images == null) {
                    images = new HashMap<String, String>() {
                        public String put(String key, String value) {
                            return super.put(key, getImagePath(value));
                        }
                    };
                    initStaticImages(images);
                }
            }
            return images;
        }
        
        protected String getImagePath(String imageFile) {
            String packagePrefix = getClass().getPackage().getName().
                                   replace('.', '/') + "/"; // NOI18N
            return packagePrefix + imageFile;
        }

        protected void initStaticImages(Map<String, String> cache) {}

        protected Image getDynamicImage(String key) { return null; }
        
    }
    
}
