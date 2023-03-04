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

package org.netbeans.modules.java.source.classpath;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Zezula
 */
public final class AptCacheForSourceQuery {


    private AptCacheForSourceQuery() {}


    public static URL getAptFolder (@NonNull final URL sourceRoot) {
        Parameters.notNull("sourceRoot", sourceRoot); //NOI18N
        //Currently no SPI as single impl exists
        return getDefaultAptFolder (sourceRoot);
    }

    public static URL getSourceFolder(@NonNull final URL aptFolder) {
        Parameters.notNull("aptFolder", aptFolder); //NOI18N
        //Currently no SPI as single impl exists
        return getDefaultSourceFolder(aptFolder);
    }

    public static URL getClassFolder (@NonNull final URL aptFolder) {
        Parameters.notNull("aptFolder", aptFolder); //NOI18N
        //Currently no SPI as single impl exists
        return getDefaultCacheFolder (aptFolder);
    }

    // Default implementation
    private static Map<URI,URL> emittedAptFolders = new ConcurrentHashMap<>(); //todo: clean up if needed (should be small)


    private static URL getDefaultAptFolder (final URL sourceRoot) {
        try {
            final URI sourceRootURI = toURI(sourceRoot);
            if (sourceRootURI == null) {
                return null;
            }
            if (emittedAptFolders.containsKey(sourceRootURI)) {
                //apt folder is a apt folder for itself
                return sourceRoot;
            }
            final File aptFolder = JavaIndex.getAptFolder(sourceRoot, true);
            final URI uriResult = BaseUtilities.toURI(aptFolder);
            final URL result = uriResult.toURL();
            emittedAptFolders.put(uriResult,sourceRoot);
            return result;
        } catch (MalformedURLException e) {
            Exceptions.printStackTrace(e);
            return null;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    private static URL getDefaultSourceFolder(final URL aptFolder) {
        final URI uri = toURI(aptFolder);
        return uri == null ? null : emittedAptFolders.get(uri);
    }

    private static URL getDefaultCacheFolder (final URL aptFolder) {
        final URL sourceRoot = getDefaultSourceFolder(aptFolder);
        if (sourceRoot != null) {
            try {
                final File result = JavaIndex.getClassFolder(sourceRoot);
                return BaseUtilities.toURI(result).toURL();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    @CheckForNull
    private static URI toURI (@NonNull final URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

}
