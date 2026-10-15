/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.filesystems;

import java.awt.Image;
import java.util.Set;

/**
 * Swing UI -oriented FileSystem utilities. 
 * 
 * @author sdedic
 */
public final class FileUIUtils {
    private static final ImageDecorator DUMMY_DECORATOR = new ImageDecorator() {
        @Override
        public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
            return icon;
        }
    };
    
    private FileUIUtils() {}
    
    /**
     * Gets an ImageDecorator for the filesystem. Returns non-null dummy instance
     * if the FileSystem's decorator does not support {@link ImageDecorator} interface.
     * <p>
     * This utility should be used in place of former {@code fileSystem.getStatus().annotateIcon(...)}.
     * @param f the FileSystem
     * @return decorator instance.
     */
    public static ImageDecorator getImageDecorator(FileSystem f) {
        StatusDecorator deco = f.getDecorator();
        if (deco instanceof ImageDecorator) {
            return ((ImageDecorator)deco);
        }
        
        return DUMMY_DECORATOR;
    }
}
