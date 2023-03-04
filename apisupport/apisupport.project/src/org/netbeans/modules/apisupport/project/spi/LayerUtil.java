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
package org.netbeans.modules.apisupport.project.spi;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.layers.LayerFileSystem;
import org.netbeans.modules.apisupport.project.layers.SynchronousStatus;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.StatusDecorator;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Utilities;

/**
 * Utilities useful for {@link NbModuleProvider#getEffectiveSystemFilesystem}.
 */
public class LayerUtil {

    /**
     * Create a merged filesystem from one writable layer (may be null) and some read-only layers.
     */
    public static @NonNull FileSystem mergeFilesystems(FileSystem writableLayer, Collection<FileSystem> readOnlyLayers) {
        if (writableLayer == null) {
            writableLayer = new XMLFileSystem();
        }
        final FileSystem[] layers = new FileSystem[readOnlyLayers.size() + 1];
        layers[0] = writableLayer;
        Iterator<FileSystem> it = readOnlyLayers.iterator();
        for (int i = 1; it.hasNext(); i++) {
            layers[i] = it.next();
        }
        return new LayerFileSystem(layers);
    }

    /**
     * Lists any XML layers defined in a module JAR.
     * May include an explicit layer and/or a generated layer.
     * Layers from platform-specific modules are ignored automatically.
     * @param jar a module JAR file
     * @return from zero to two layer URLs
     */
    public static List<URL> layersOf(File jar) throws IOException {
        ManifestManager mm = ManifestManager.getInstanceFromJAR(jar, true);
        for (String tok : mm.getRequiredTokens()) {
            if (tok.startsWith("org.openide.modules.os.")) { // NOI18N
                // Best to exclude platform-specific modules, e.g. ide/applemenu, as they can cause confusion.
                return Collections.emptyList();
            }
        }
        String layer = mm.getLayer();
        String generatedLayer = mm.getGeneratedLayer();
        List<URL> urls = new ArrayList<URL>(2);
        URI juri = Utilities.toURI(jar);
        for (String path : new String[] {layer, generatedLayer}) {
            if (path != null) {
                urls.add(new URL("jar:" + juri + "!/" + path));
            }
        }
        if (layer != null) {
            urls.add(new URL("jar:" + juri + "!/" + layer));
        }
        if (generatedLayer != null) {
            urls.add(new URL("jar:" + juri + "!/" + generatedLayer));
        }
        return urls;
    }

    /**
     * Constructs suitable bundle key for localizing file object with given path.
     * Intended for localizing layer files/folders.
     * @param filePath Path as returned from {@link org.openide.filesystems.FileObject.getPath()}, i.e. with forwared slashes ('/')
     * @return Bundle key for given file path.
     */
    public static String generateBundleKeyForFile(String filePath) {
        // might result in the same key for filles that differ in replaced chars, but probably good enough;
        // otherwise may check for duplicates and add "_n" when properties are passed as param
        return filePath.replaceAll("[^-a-zA-Z0-9_./]", "");    // NOI18N
    }

    private static final Set<String> XML_LIKE_TYPES = new HashSet<String>();
    static {
        XML_LIKE_TYPES.add(".settings"); // NOI18N
        XML_LIKE_TYPES.add(".wstcref"); // NOI18N
        XML_LIKE_TYPES.add(".wsmode"); // NOI18N
        XML_LIKE_TYPES.add(".wsgrp"); // NOI18N
        XML_LIKE_TYPES.add(".wsmgr"); // NOI18N
    }
    /**
     * Find the name of the external file that will be generated for a given
     * layer path if it is created with contents.
     * @param parent parent folder, or null
     * @param layerPath full path in layer
     * @return a simple file name
     */
    public static String findGeneratedName(FileObject parent, String layerPath) {
        Matcher m = Pattern.compile("(.+/)?([^/.]+)(\\.[^/]+)?").matcher(layerPath); // NOI18N
        if (!m.matches()) {
            throw new IllegalArgumentException(layerPath);
        }
        String base = m.group(2);
        String ext = m.group(3);
        if (ext == null) {
            ext = "";
        } else if (ext.equals(".java")) { // NOI18N
            ext = "_java"; // NOI18N
        } else if (XML_LIKE_TYPES.contains(ext)) {
            String upper = ext.substring(1,2).toUpperCase(Locale.ENGLISH);
            base = base + upper + ext.substring(2);
            ext = ".xml"; // NOI18N
        }
        String name = base + ext;
        if (parent == null || parent.getFileObject(name) == null) {
            return name;
        } else {
            for (int i = 1; true; i++) {
                name = base + '_' + i + ext;
                if (parent.getFileObject(name) == null) {
                    return name;
                }
            }
        }
    }

    /**
     * Tries to use {@link org.openide.filesystems.FileSystem.Status#annotateName} on a file.
     * The key difference is that in the case of a layer entry, this will be synchronous,
     * whereas normally the display name and icon are calculated asynchronously and a change fired.
     * @param fo a file
     * @return its (possibly annotated) name according to {@link FileObject#getNameExt}
     */
    public static String getAnnotatedName(FileObject fo) {
        String name = fo.getNameExt();
        try {
            StatusDecorator status = fo.getFileSystem().getDecorator();
            if (status instanceof SynchronousStatus) {
                return ((SynchronousStatus) status).annotateNameSynch(name, Collections.singleton(fo));
            } else {
                return status.annotateName(name, Collections.singleton(fo));
            }
        } catch (FileStateInvalidException ex) {
            return name;
        }
    }

    /**
     * Suffix of hidden files.
     */
    public static final String HIDDEN = "_hidden"; //NOI18N

    public static final /*@StaticResource*/ String LAYER_ICON = "org/netbeans/modules/apisupport/project/spi/layerObject.gif";

    private LayerUtil() {}

}
