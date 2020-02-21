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
