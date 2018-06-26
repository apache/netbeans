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
package org.netbeans.modules.groovy.grailsproject.classpath;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.config.BuildConfig;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.WeakListeners;

final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private final SourceRoots sourceRoots;

    private List<PathResourceImplementation> resources;

    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     */
    private SourcePathImplementation(SourceRoots sourceRoots) {
        assert sourceRoots != null;
        this.sourceRoots = sourceRoots;
    }

    public static SourcePathImplementation forProject(GrailsProject project, SourceRoots sourceRoots) {
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        SourcePathImplementation impl = new SourcePathImplementation(sourceRoots);

        BuildConfig build = ((GrailsProject) config.getProject()).getBuildConfig();
        build.addPropertyChangeListener(WeakListeners.propertyChange(impl, config));

        return impl;
    }

    public List<PathResourceImplementation> getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return this.resources;
            }
        }
        List<URL> roots = this.sourceRoots.getRootURLs();
        synchronized (this) {
            if (this.resources == null) {
                List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(roots.size());
                for (URL url : roots) {
                    PathResourceImplementation res = ClassPathSupport.createResource(url);
                    result.add(res);
                }
                this.resources = Collections.unmodifiableList(result);
            }
            return this.resources;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (BuildConfig.BUILD_CONFIG_PLUGINS.equals(evt.getPropertyName())) {
            synchronized (this) {
                this.resources = null;
            }
            this.support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

}
