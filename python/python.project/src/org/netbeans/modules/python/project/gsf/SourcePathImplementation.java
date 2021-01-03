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
package org.netbeans.modules.python.project.gsf;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.modules.python.project.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
//import org.netbeans.modules.gsfpath.spi.classpath.ClassPathImplementation;
//import org.netbeans.modules.gsfpath.spi.classpath.PathResourceImplementation;
//import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
import org.openide.util.WeakListeners;

/**
 * Source class path implementation
 */
final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<PathResourceImplementation> resources;
    private final SourceRoots src;

    public SourcePathImplementation(SourceRoots sources) {
        assert sources != null;
        this.src = sources;
        this.src.addPropertyChangeListener(WeakListeners.propertyChange(this, this.src));
    }

    @Override
    public List<PathResourceImplementation> getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return this.resources;
            }
        }
        final URL[] urls = this.src.getRootURLs();      
        synchronized (this) {
            if (this.resources == null) {
                List<PathResourceImplementation> result = new ArrayList<>(urls.length);
                for (URL root : urls) {
                    result.add(ClassPathSupport.createResource(root));
                }
                this.resources = Collections.unmodifiableList(result);
            }
            return this.resources;
        }                        
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener (listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener);
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
       this.resources = null;
    }
}
