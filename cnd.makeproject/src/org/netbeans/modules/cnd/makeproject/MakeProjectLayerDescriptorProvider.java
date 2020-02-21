/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.spi.project.NativeProjectLayerDescriptorProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = NativeProjectLayerDescriptorProvider.class, position = 1000)
public final class MakeProjectLayerDescriptorProvider implements NativeProjectLayerDescriptorProvider {

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private static final ConcurrentHashMap<NativeProject, List<URI>> cache = new ConcurrentHashMap<>();
    private static final PropertyChangeListener listener = new ProjectsListener();

    static {
        NativeProjectRegistry.getDefault().addPropertyChangeListener(listener);
    }

    @Override
    public List<URI> getLayerDescriptors(NativeProject nativeproject) {
        if (nativeproject != null) {
            List<URI> result = cache.get(nativeproject);
            if (result == null) {
                Lookup.Provider lookupProvider = nativeproject.getProject();
                if (lookupProvider != null) {
                    Project project = lookupProvider.getLookup().lookup(Project.class);
                    if (project != null) {
                        FileObject projectDirectory = project.getProjectDirectory();
                        result = getCacheLocations(projectDirectory);
                        List<URI> prevResult = cache.putIfAbsent(nativeproject, result);
                        if (prevResult != null) {
                            result = prevResult;
                        }
                    }
                }
            }
            if (result != null && !result.isEmpty()) {
                return result;
            }
        }
        return Collections.<URI>emptyList();
    }

    /**
     * Tries getting cache path from project.properties - first private, then
     * public
     */
    private static List<URI> getCacheLocations(FileObject projectDirectory) {

        String[] propertyPaths = new String[]{
            MakeProjectHelper.PROJECT_PROPERTIES_PATH,
            MakeProjectHelper.PRIVATE_PROPERTIES_PATH
        };

        SortedMap<String, String> map = new TreeMap<>();

        for (int i = 0; i < propertyPaths.length; i++) {
            FileObject propsFO = projectDirectory.getFileObject(propertyPaths[i]);
            if (propsFO != null && propsFO.isValid()) {
                Properties props = new Properties();
                InputStream is = null;
                try {
                    is = propsFO.getInputStream();
                    props.load(is);
                    for (String key : props.stringPropertyNames()) {
                        if (isCacheLocationKey(key)) { //NOI18N                            
                            map.put(key, props.getProperty(key));
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, "Error closing " + propsFO.getPath(), ex);
                        }
                    }
                }
            }
        }
        if (!map.isEmpty()) {
            List<URI> res = new ArrayList<>();
            map.values().forEach((uriString) -> {
                try {
                    URI uri = new URI(uriString);
                    String scheme = uri.getScheme();
                    boolean changed = false;
                    if (scheme == null) {
                        scheme = "file"; //NOI18N
                        changed = true;
                    }
                    String path = uri.getPath();
                    if (!CndPathUtilities.isPathAbsolute(path)) {
                        FileObject fo = FileUtil.createFolder(projectDirectory, path);
                        path = fo.getPath();
                        changed = true;
                    }
                    String fragment = uri.getFragment();
                    if (fragment == null && !res.isEmpty()) {
                        fragment = "r/o"; //NOI18N
                        changed = true;
                    }
                    if (changed) {
                        uri = new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(), fragment);
                    }
                    res.add(uri);
                } catch (URISyntaxException ex) {
                    ex.printStackTrace(System.err);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            });
            return res;
        }
        return Collections.<URI>emptyList();
    }

    private static boolean isCacheLocationKey(String key) {
        String prefix = "cache.location"; //NOI18N
        if (key.equals(prefix)) {
            return true;
        }
        if (key.startsWith(prefix) && (key.length() > prefix.length() + 1)) {
            if (key.charAt(prefix.length()) == '.') {
                for (int i = prefix.length() + 1; i < key.length(); i++) {
                    char c = key.charAt(i);
                    if (!Character.isDigit(c)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static final class ProjectsListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NativeProjectRegistry.PROPERTY_CLOSE_NATIVE_PROJECT.equals(evt.getPropertyName()) || 
                NativeProjectRegistry.PROPERTY_DELETE_NATIVE_PROJECT.equals(evt.getPropertyName())) {
                cache.remove((NativeProject)evt.getSource());
            }
        }
    }
}
