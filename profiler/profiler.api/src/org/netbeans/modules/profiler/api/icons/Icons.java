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
package org.netbeans.modules.profiler.api.icons;

import java.awt.Image;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.profiler.spi.IconsProvider;
import org.openide.util.Lookup;

/**
 * Support for predefined icons and images.
 *
 * @author Jiri Sedlacek
 */
public final class Icons {
    
    /**
     * Returns an Icon instance according to the provided key.
     * 
     * @param key icon key
     * @return Icon instance according to the provided key
     */
    public static Icon getIcon(String key) {
        return getImageIcon(key);
    }
    
    /**
     * Returns an ImageIcon instance according to the provided key.
     * 
     * @param key icon key
     * @return ImageIcon instance according to the provided key
     */
    public static ImageIcon getImageIcon(String key) {
        Image image = getImage(key);
        if (image == null) return null;
        else return new ImageIcon(image);
    }
    
    /**
     * Returns an Image instance according to the provided key.
     * 
     * @param key image key
     * @return Image instance according to the provided key
     */
    public static Image getImage(String key) {
        Collection<? extends IconsProvider> ps = providers();
        for (IconsProvider p : ps) {
            Image image = p.getImage(key);
            if (image != null) return image;
        }
        return null;
    }
    
    /**
     * Returns path to image resource without leading slash according to the provided key.
     * 
     * @param key image key
     * @return path to image resource without leading slash according to the provided key
     */
    public static String getResource(String key) {
        Collection<? extends IconsProvider> ps = providers();
        for (IconsProvider p : ps) {
            String resource = p.getResource(key);
            if (resource != null) return resource;
        }
        return null;
    }
    
    private static Collection<? extends IconsProvider> providers() {
        return Lookup.getDefault().lookupAll(IconsProvider.class);
    }
    
    
    public static interface Keys {}
    
}
