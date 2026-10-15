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
package org.netbeans.api.java.platform;

import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class TestJavaPlatformProvider implements JavaPlatformProvider {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private List<JavaPlatform> platforms = new ArrayList<JavaPlatform>();


    public static TestJavaPlatformProvider getDefault () {
        return Lookup.getDefault().lookup(TestJavaPlatformProvider.class);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        assert listener != null;
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        assert listener != null;
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public JavaPlatform[] getInstalledPlatforms() {
        return this.platforms.toArray(new JavaPlatform[0]);
    }

    public void addPlatform (JavaPlatform platform) {
        this.platforms.add (platform);
        this.firePropertyChange ();
    }

    public void removePlatform (JavaPlatform platform) {
        this.platforms.remove (platform);
        this.firePropertyChange ();
    }

    public void insertPlatform(JavaPlatform before, JavaPlatform platform) {
        int index = platforms.indexOf(before);
        if (index < 0) {
            index = platforms.size();
        }
        this.platforms.add(index,platform);
        this.firePropertyChange ();
    }

    public void reset() {
        this.platforms.clear();
        firePropertyChange();
    }

    private void firePropertyChange () {
        pcs.firePropertyChange(PROP_INSTALLED_PLATFORMS, null, null);
    }

    @Override
    public JavaPlatform getDefaultPlatform() {
        if (platforms.size()>0)
            return platforms.get(0);
        else
            return null;
    }

    }
