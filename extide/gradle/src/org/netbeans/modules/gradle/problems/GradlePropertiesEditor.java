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
package org.netbeans.modules.gradle.problems;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;

/**
 * A helper class that loads + merges property files. It also tracks which property comes
 * from which file (project properties, settings, user properties). The loaded properties
 * are cached, and the cache is checked on each call to {@link #loadProperties}. 
 * <p>
 * {@link #ensureGetProperties()} only ensures the properties are loaded, not that the
 * current state is not stale. Make sure that {@link #loadProperties} are called after
 * each suspected change to refresh the state.
 * 
 * @author sdedic
 */
public class GradlePropertiesEditor {
    private static final Logger LOG = Logger.getLogger(GradlePropertiesEditor.class.getName());
    
    private final Project project;

    public GradlePropertiesEditor(Project project) {
        this.project = project;
    }

    // @GuardedBy(this)
    private CachedProperties loadedProperties = new CachedProperties(Collections.emptyMap());
    
    static class CachedProperties extends Properties {
        private final Map<File, Long> timestamps;
        private final Map<String, FileObject> origins = new HashMap<>();
        private final Map<Path, PropertiesEditor> editables = new HashMap<>();
        
        public CachedProperties(Map<File, Long> timestamps) {
            this.timestamps = timestamps;
        }
        
        boolean valid(Collection<File> files) {
            if (!timestamps.keySet().containsAll(files) || timestamps.size() != files.size()) {
                return false;
            }
            for (File k : files) {
                Long l = timestamps.get(k);
                if (l == null || l.longValue() != k.lastModified()) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public FileObject getPropertyOrigin(String propName) {
        return ensureGetProperties().origins.get(propName);
    }
    
    public PropertiesEditor getEditorFor(String property) {
        FileObject fo = getPropertyOrigin(property);
        if (fo == null) {
            return null;
        } else {
            return getEditor(fo, null);
        }
    }
    
    public PropertiesEditor getEditor(FileObject origin, GradleFiles.Kind kind) {
        GradleFiles gf = NbGradleProject.get(project).getGradleFiles();
        if (origin != null) {
            File f = FileUtil.toFile(origin);
            if (f != null) {
                synchronized (this) {
                    PropertiesEditor ep = ensureGetProperties().editables.get(f.toPath());
                    if (ep != null) {
                        return ep;
                    }
                }
            }
        }
        if (kind == null) {
            return null;
        }
        File f = gf.getFile(kind);
        if (f == null) {
            return null;
        }
        Map<Path, PropertiesEditor> editables = ensureGetProperties().editables;
        
        PropertiesEditor ed;
        synchronized (this) {
            ed = editables.get(f.toPath());
            if (ed != null) {
                return ed;
            }
            ed = new PropertiesEditor(f.toPath());
            editables.put(f.toPath(), ed);
        }
        return ed;
    }
    
    /**
     * Loads properties from project and global files.
     * @throws IOException 
     */
    public Properties loadGradleProperties() {
        return loadGradleProperties0();
    }
    
    CachedProperties loadGradleProperties0() {
        GradleFiles gf = NbGradleProject.get(project).getGradleFiles();
        List<File> files = gf.getPropertyFiles();
        CachedProperties cached;
        synchronized (this) {
            cached = loadedProperties;
            if (cached.valid(files)) {
                return cached;
            }
        }
        return doLoadProperties(cached, files);
    }
    
    CachedProperties ensureGetProperties() {
        synchronized (this) {
            if (loadedProperties != null) {
                return loadedProperties;
            }
        }
        return loadGradleProperties0();
    }
    
    private CachedProperties doLoadProperties(CachedProperties cached, List<File> files) {
        Map<File, Long> stamps = new HashMap<>();
        for (File f : files) {
            stamps.put(f, f.lastModified());
        }
        CachedProperties merged = new CachedProperties(stamps);
        Map<Path, PropertiesEditor> propertyMap = new HashMap<>();
        for (int i = files.size() - 1; i >= 0; i--) {
            File f = files.get(i);
            Path path = f.toPath();
            if (propertyMap.containsKey(path)) {
                continue;
            }
            FileObject fo = FileUtil.toFileObject(f);
            
            try {
                PropertiesEditor pe = fo == null ? new PropertiesEditor(path) : new PropertiesEditor(fo);
                propertyMap.put(path, pe);
                EditableProperties p = pe.open();
                for (Object k : p.keySet()) {
                    String ks = k.toString();
                    if (!merged.containsKey(ks)) {
                        merged.put(ks, p.getProperty(ks));
                        merged.origins.put(ks, fo);
                    }
                }
                merged.editables.put(path, pe);
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Could not read properties file {0}", f);
            }
        }
        synchronized (this) {
            if (this.loadedProperties == cached) {
                this.loadedProperties = merged;
            }
        }
        return merged;
    }
}
