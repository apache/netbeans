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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;

/**
 *
 * @author  tom
 */
public final class JavaPlatformProviderImpl implements JavaPlatformProvider {


    private PropertyChangeSupport support;
    private List<JavaPlatform> platforms;
    private JavaPlatform defaultPlatform;

    /** Creates a new instance of JavaPlatformProviderImpl */
    public JavaPlatformProviderImpl() {
        this.support = new PropertyChangeSupport (this);
        this.platforms = new ArrayList<JavaPlatform>();
        this.addPlatform (this.createDefaultPlatform());
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }    
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }    
    
    public void addPlatform (JavaPlatform platform) {
        this.platforms.add (platform);
        this.support.firePropertyChange(PROP_INSTALLED_PLATFORMS, null, null);
    }
    
    public void removePlatform (JavaPlatform platform) {
        this.platforms.remove(platform);
        this.support.firePropertyChange(PROP_INSTALLED_PLATFORMS, null, null);
    }
        
    @Override
    public JavaPlatform[] getInstalledPlatforms() {
        return this.platforms.toArray(new JavaPlatform[0]);
    }

    @Override
    public JavaPlatform getDefaultPlatform() {
        return createDefaultPlatform ();
    }
    
    private synchronized JavaPlatform createDefaultPlatform () {
        if (this.defaultPlatform == null) {
            System.getProperties().put("jdk.home",System.getProperty("java.home"));     //NOI18N
            this.defaultPlatform = DefaultPlatformImpl.create (null,null,null);
        }
        return defaultPlatform;
    }

}
