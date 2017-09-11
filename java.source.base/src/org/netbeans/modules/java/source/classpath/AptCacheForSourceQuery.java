/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
