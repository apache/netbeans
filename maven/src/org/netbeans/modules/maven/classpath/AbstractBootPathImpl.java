/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
        String newVal = project.getAuxProps().get(Constants.HINT_JDK_PLATFORM, true);
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
            String val = project.getAuxProps().get(Constants.HINT_JDK_PLATFORM, true);
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
