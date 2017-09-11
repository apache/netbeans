/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.api.common.classpath;

import java.beans.PropertyChangeEvent;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.project.support.ProjectPlatform;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.WeakListeners;


/**
 * Represent a boot class path. It is possible to listen to the changes of
 * {@link ClassPathImplementation#PROP_RESOURCES}.
 * @author Tomas Zezula
 */
final class BootClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private static final String PLATFORM_ACTIVE = "platform.active"; // NOI18N

    private final Project project;
    private final PropertyEvaluator evaluator;
    private final String platformType;
    private JavaPlatformManager platformManager;
    // name of project active platform
    private String activePlatformName;
    // active platform is valid (not broken reference)
    private boolean isActivePlatformValid;
    private List<PathResourceImplementation> resourcesCache;
    private long eventId;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final ClassPath endorsedClassPath;

    BootClassPathImplementation(
            @NullAllowed final Project project,
            @NonNull final PropertyEvaluator evaluator,
            @NullAllowed final ClassPath endorsedClassPath,
            @NullAllowed final String platformType) {
        assert evaluator != null;
        this.project = project;
        this.endorsedClassPath = endorsedClassPath;
        this.evaluator = evaluator;
        this.platformType = platformType;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        if (endorsedClassPath != null) {
            endorsedClassPath.addPropertyChangeListener(this);
        }
    }
    
    /**
     * @see ClassPathImplementation#getResources()
     */
    @Override
    public List<PathResourceImplementation> getResources() {
        long currentId;
        synchronized (this) {
            if (resourcesCache != null) {
                return resourcesCache;
            }
            currentId = eventId;
        }

        final List<PathResourceImplementation> result = new ArrayList<>();
        if (endorsedClassPath != null) {
            for (ClassPath.Entry entry : endorsedClassPath.entries()) {
                result.add(ClassPathSupport.createResource(entry.getURL()));
            }
        }
        JavaPlatform jp = findActivePlatform();
        if (jp != null) {
            // TODO: may also listen on CP, but from Platform it should be fixed
            final ClassPath cp = jp.getBootstrapLibraries();
            assert cp != null : jp;
            for (ClassPath.Entry entry : cp.entries()) {
                result.add(ClassPathSupport.createResource(entry.getURL()));
            }
        }

        synchronized (this) {
            if (currentId == eventId) {
                if (resourcesCache == null) {
                    resourcesCache = Collections.unmodifiableList(result);
                }
                return resourcesCache;
            }
            return Collections.unmodifiableList(result);
        }
    }

    /**
     * Add {@link PropertyChangeListener}, see class description for more information.
     * @param listener a listener to add.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Remove {@link PropertyChangeListener}, see class description for more information.
     * @param listener a listener to remove.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * @see PropertyChangeListener#propertyChange()
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == evaluator && evt.getPropertyName().equals(PLATFORM_ACTIVE)) {
            // active platform was changed
            resetCache();
        } else if (evt.getSource() == platformManager
                && JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(evt.getPropertyName())
                && activePlatformName != null) {
            // platform definitions were changed, check if the platform was not resolved or deleted
            if (isActivePlatformValid) {
                if (CommonProjectUtils.getActivePlatform(activePlatformName, platformType) == null) {
                    // the platform was not removed
                    resetCache();
                }
            } else {
                if (CommonProjectUtils.getActivePlatform(activePlatformName, platformType) != null) {
                    resetCache();
                }
            }
        } else if (endorsedClassPath != null && evt.getSource() == endorsedClassPath) {
            resetCache();
        }
    }

    private JavaPlatform findActivePlatform() {
        if (platformManager == null) {
            platformManager = JavaPlatformManager.getDefault();
            platformManager.addPropertyChangeListener(WeakListeners.propertyChange(this, platformManager));
        }
        activePlatformName = evaluator.getProperty(PLATFORM_ACTIVE);
        JavaPlatform activePlatform = CommonProjectUtils.getActivePlatform(activePlatformName, platformType);
        if (activePlatform != null) {
            isActivePlatformValid = true;
        } else {
            activePlatform = createPerProjectPlatform();
            isActivePlatformValid = false;
        }
        return activePlatform;
    }
    
    @CheckForNull
    private JavaPlatform createPerProjectPlatform() {
        if (project == null) {
            return null;
        }
        return ProjectPlatform.forProject(project, evaluator, platformType);
    }

    private void resetCache() {
        synchronized (this) {
            resourcesCache = null;
            eventId++;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }
}
