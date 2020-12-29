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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
