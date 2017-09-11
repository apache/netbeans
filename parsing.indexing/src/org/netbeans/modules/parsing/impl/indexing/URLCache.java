/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl.indexing;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.BaseUtilities;

/**
 * Cache for {@link URLMapper#findFileObject} for source roots.
 * @author Tomas Zezula
 * @author Vita Stejskal
 */
public final class URLCache {

    private static final Logger LOG = Logger.getLogger(URLCache.class.getName());

    public static synchronized URLCache getInstance() {
        if (instance == null) {
            instance = new URLCache();
        }
        return instance;
    }

    @CheckForNull
    public FileObject findFileObject(
            final @NonNull URL url,
            final boolean validate) {
        URI uri = null;
        try {
            uri  = url.toURI();
        } catch (URISyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        FileObject f = null;
        if (uri != null) {
            Reference<FileObject> ref = cache.get(uri);
            if (ref != null) {
                f = ref.get();
            }
            if (f != null && f.isValid() && (!validate || f.toURI().equals(uri))) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(
                        Level.FINEST,
                        "Found: {0} in cache for: {1}", //NOI18N
                        new Object[]{
                            f,
                            url
                        });
                }
                return f;
            }
        }

        f = URLMapper.findFileObject(url);

        if (uri != null && f != null && f.isValid()) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(
                   Level.FINEST,
                   "Caching: {0} for: {1}", //NOI18N
                   new Object[]{
                       f,
                       url
                   });
            }
            cache.put(uri, new CleanReference(f,uri));
        }

        return f;
    }

    private static URLCache instance = null;
    private final Map<URI, Reference<FileObject>> cache = Collections.synchronizedMap(
            new HashMap<URI, Reference<FileObject>>());

    private URLCache() {
    }
    
    private final class CleanReference extends WeakReference<FileObject> implements Runnable {        
        
        private final URI uri;
        
        CleanReference(
                @NonNull FileObject referent,
                @NonNull URI uri) {            
            super(referent, BaseUtilities.activeReferenceQueue());
            assert referent != null;
            assert uri != null;
            this.uri = uri;
        }

        @Override
        public void run() {
            cache.remove(uri);
        }
    }
}
