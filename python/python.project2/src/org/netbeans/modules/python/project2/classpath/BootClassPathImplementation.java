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
package org.netbeans.modules.python.project2.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.project2.PythonProject2;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Parameters;

final class BootClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {
    private List<PathResourceImplementation> resourcesCache;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final PythonProject2 project;

    public BootClassPathImplementation (final PythonProject2 project) {
        assert project != null;
        this.project = project;
//        this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
    }

    @Override
    public synchronized List<PathResourceImplementation> getResources() {
        if (this.resourcesCache == null) {
            List<URL> urls = getUrls(project);
            List<PathResourceImplementation> result = new ArrayList<>(1);
            for (URL url : urls) {
                result.add(ClassPathSupport.createResource(url));
            }
            resourcesCache = Collections.unmodifiableList(result);
        }
        return this.resourcesCache;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);
        this.support.addPropertyChangeListener (listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);
        this.support.removePropertyChangeListener (listener);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
//        if (evt.getSource() == this.eval &&
//            (evt.getPropertyName() == null || evt.getPropertyName().equals(PythonProjectProperties.ACTIVE_PLATFORM))) {
//            //Active platform was changed
//            RequestProcessor.getDefault().post(new Runnable() {
//                @Override
//              public void run() {
//                resetCache ();
//              }
//            }) ;
//        }
    }
    
    private void resetCache () {
        synchronized (this) {
            resourcesCache = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }

    private List<URL> getUrls(PythonProject2 project) {
        PythonPlatform activePlatform = project.getActivePlatform();
        if (activePlatform == null) {
            final PythonPlatformManager manager = PythonPlatformManager.getInstance();
            final String platformName = manager.getDefaultPlatform();
            activePlatform = manager.getPlatform(platformName);
        }
        return activePlatform.getUrls();
    }
}
