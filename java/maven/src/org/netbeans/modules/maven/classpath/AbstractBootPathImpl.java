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
package org.netbeans.modules.maven.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Stupka
 */
public abstract class AbstractBootPathImpl implements ClassPathImplementation, PropertyChangeListener {

    @NonNull 
    protected final NbMavenProjectImpl project;
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private boolean activePlatformValid = true;
    //lock for this class and EndorsedCPI
    final Object LOCK = new Object();
    private JavaPlatformManager platformManager;
    
    private List<? extends PathResourceImplementation> resourcesCache;
    protected String lastHintValue = null;

    public AbstractBootPathImpl(NbMavenProjectImpl project) {
        this.project = project;
    }
    
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public @Override List<? extends PathResourceImplementation> getResources() {
        synchronized (LOCK) {
            if (this.resourcesCache == null) {
                resourcesCache = Collections.unmodifiableList (createResources());
            }
            return this.resourcesCache;
        }
    }

    protected abstract List<PathResourceImplementation> createResources();

    /**
     * Resets the cache and firesPropertyChange
     */
    protected void resetCache() {
        synchronized (LOCK) {
            resourcesCache = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }

    /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     */
    public static JavaPlatform getActivePlatform(final String activePlatformId) {
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
        if (activePlatformId == null) {
            return pm.getDefaultPlatform();
        } else {
            JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification(CommonProjectUtils.J2SE_PLATFORM_TYPE, null)); //NOI18N
            for (int i = 0; i < installedPlatforms.length; i++) {
                String antName = installedPlatforms[i].getProperties().get("platform.ant.name"); //NOI18N
                if (antName != null && antName.equals(activePlatformId)) {
                    return installedPlatforms[i];
                }
            }
            return null;
        }
    }

    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        String newVal = project.getHintJavaPlatform();
        if (evt.getSource() == platformManager && 
                JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(evt.getPropertyName()) 
                && lastHintValue != null) {
            lastHintValue = newVal;
            //Platform definitions were changed, check if the platform was not resolved or deleted
            if (activePlatformValid) {
                if (getActivePlatform (lastHintValue) == null) {
                    //the platform was removed
                    resetCache();
                }
            }
            else {
                if (getActivePlatform (lastHintValue) != null) {
                    //platform was added
                    resetCache();
                }
            }
        } 
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }

    @NonNull
    JavaPlatform findActivePlatform() {
        synchronized (LOCK) {
            activePlatformValid = true;
            if (platformManager == null) {
                platformManager = JavaPlatformManager.getDefault();
                platformManager.addPropertyChangeListener(WeakListeners.propertyChange(this, platformManager));
                NbMavenProject watch = project.getProjectWatcher();
                watch.addPropertyChangeListener(this);
            }
            //TODO ideally we would handle this by toolchains in future.
            //only use the default auximpl otherwise we get recursive calls problems.
            String val = project.getHintJavaPlatform();
            JavaPlatform plat = getActivePlatform(val);
            if (plat == null) {
                //TODO report how?
                Logger.getLogger(BootClassPathImpl.class.getName()).log(Level.FINE, "Cannot find java platform with id of ''{0}''", val); //NOI18N
                plat = platformManager.getDefaultPlatform();
                activePlatformValid = false;
            }
            //Invalid platform ID or default platform
            return plat;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BootClassPathImpl && project.equals(((BootClassPathImpl) obj).project);
    }

    @Override
    public int hashCode() {
        return project.hashCode() ^ 191;
    }

}
