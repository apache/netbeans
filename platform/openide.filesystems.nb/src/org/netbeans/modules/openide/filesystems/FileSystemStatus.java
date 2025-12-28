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

package org.netbeans.modules.openide.filesystems;

import java.awt.Image;
import java.beans.BeanInfo;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.ImageDecorator;
import org.openide.filesystems.StatusDecorator;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The default implementation of FileSystem status. For compatibility with 
 * NB &lt; 9.0, it must provide name and icon annotations.
 * 
 * @author sdedic
 */
@ServiceProvider(service = StatusDecorator.class)
public final class FileSystemStatus implements StatusDecorator, ImageDecorator {
    private static final Logger LOG = Logger.getLogger(FileSystemStatus.class.getName());
    
    @Override
    public String annotateName(String s, Set<? extends FileObject> files) {
        // Look for a localized file name.
        // Note: all files in the set are checked. But please only place the attribute
        // on the primary file, and use this primary file name as the bundle key.
        for (FileObject fo : files) {
            // annotate a name
            String displayName = annotateName(fo);
            if (displayName != null) {
                return displayName;
            }
        }
        return s;
    }

    private final String annotateName(FileObject fo) {
        String bundleName = (String) fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
        if (bundleName != null) {
            try {
                bundleName = BaseUtilities.translate(bundleName);
                ResourceBundle b = NbBundle.getBundle(bundleName);
                try {
                    return b.getString(fo.getPath());
                } catch (MissingResourceException ex) {
                    // ignore--normal
                }
            } catch (MissingResourceException ex) {
                Exceptions.attachMessage(ex, warningMessage(bundleName, fo));
                LOG.log(Level.INFO, null, ex);
                // ignore
            }
        }
        return (String) fo.getAttribute("displayName"); // NOI18N
    }

    private String warningMessage(String name, FileObject fo) {
        Object by = fo.getAttribute("layers"); // NOI18N
        if (by instanceof Object[]) {
            by = Arrays.toString((Object[]) by);
        }
        return "Cannot load " + name + " for " + fo + " defined by " + by; // NOI18N
    }

    public Image annotateIcon(Image im, int type, Set<? extends FileObject> files) {
        for (FileObject fo : files) {
            Image img = annotateIcon(fo, type);
            if (img != null) {
                return img;
            }
        }
        return im;
    }

    @Override
    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        return null;
    }

    private Image annotateIcon(FileObject fo, int type) {
        String attr = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            attr = "SystemFileSystem.icon"; // NOI18N
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            attr = "SystemFileSystem.icon32"; // NOI18N
        }
        if (attr != null) {
            Object value = fo.getAttribute(attr);
            if (value != null) {
                if (value instanceof URL) {
                    try {
                        return ImageUtilities.loadImage(((URL) value).toURI());
                    } catch (URISyntaxException e) {
                        LOG.log(Level.WARNING, "Annotation has invalid icon URI", e);
                    }
                } else if (value instanceof Image) {
                    // #18832
                    return (Image) value;
                } else {
                    LOG.warning("Attribute " + attr + " on " + fo + " expected to be a URL or Image; was: " + value);
                }
            }
        }
        String base = (String) fo.getAttribute("iconBase"); // NOI18N
        if (base != null) {
            if (type == BeanInfo.ICON_COLOR_16x16) {
                return ImageUtilities.loadImage(base, true);
            } else if (type == BeanInfo.ICON_COLOR_32x32) {
                return ImageUtilities.loadImage(insertBeforeSuffix(base, "_32"), true); // NOI18N
            }
        }
        return null;
    }

    private String insertBeforeSuffix(String path, String toInsert) {
        String withoutSuffix = path;
        String suffix = ""; // NOI18N
        if (path.lastIndexOf('.') >= 0) {
            withoutSuffix = path.substring(0, path.lastIndexOf('.'));
            suffix = path.substring(path.lastIndexOf('.'), path.length());
        }
        return withoutSuffix + toInsert + suffix;
    }
}
