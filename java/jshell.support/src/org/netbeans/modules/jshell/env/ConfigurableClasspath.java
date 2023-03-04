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
package org.netbeans.modules.jshell.env;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author sdedic
 */
final class ConfigurableClasspath implements ClassPathImplementation {
    private List<FileObject>    classpathResources;
    private List<PathResourceImplementation>    resCache;
    private PropertyChangeSupport   propSupport = new PropertyChangeSupport(this);
    
    public void append(FileObject resource) {
        synchronized (this) {
            if (classpathResources == null) {
                classpathResources = new ArrayList<>();
            }
            classpathResources.add(resource);
            resCache = null;
        }
        propSupport.firePropertyChange(PROP_RESOURCES, null, null);
    }

    @Override
    public List<? extends PathResourceImplementation> getResources() {
        List<FileObject> res;
        synchronized (this) {
            if (resCache != null) {
                return resCache;
            }
            if (classpathResources == null) {
                return Collections.emptyList();
            }
            res = new ArrayList<>(classpathResources);
        }
        
        List<PathResourceImplementation> resources = new ArrayList<>(res.size());
        for (FileObject f : res) {
            if (FileUtil.isArchiveFile(f)) {
                f = FileUtil.getArchiveRoot(f);
            }
            URL u = URLMapper.findURL(f, URLMapper.EXTERNAL);
            if (u != null) {
                resources.add(ClassPathSupport.createResource(u));
            }
        }
        synchronized (this) {
            if (classpathResources.equals(res)) {
                resCache = resources;
            }
        }
        return resources;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }
}
