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
package org.netbeans.modules.javascript.bower.misc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.javascript.bower.file.BowerrcJson;
import org.netbeans.modules.javascript.bower.options.BowerOptions;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

public final class SharabilityQueryImpl  implements SharabilityQueryImplementation2, PropertyChangeListener, PreferenceChangeListener {

    final BowerrcJson bowerrcJson;

    private volatile URI bowerComponentsDir = null;
    private volatile Boolean versioningIgnored;


    private SharabilityQueryImpl(Project project) {
        assert project != null;
        bowerrcJson = new BowerrcJson(project.getProjectDirectory());
    }

    private static SharabilityQueryImplementation2 create(Project project) {
        SharabilityQueryImpl sharabilityQuery = new SharabilityQueryImpl(project);
        sharabilityQuery.bowerrcJson.addPropertyChangeListener(WeakListeners.propertyChange(sharabilityQuery, sharabilityQuery.bowerrcJson));
        BowerOptions bowerOptions = BowerOptions.getInstance();
        bowerOptions.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, sharabilityQuery, bowerOptions));
        return sharabilityQuery;
    }

    @ProjectServiceProvider(service = SharabilityQueryImplementation2.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
    public static SharabilityQueryImplementation2 forHtml5Project(Project project) {
        return create(project);
    }

    @ProjectServiceProvider(service = SharabilityQueryImplementation2.class, projectType = "org-netbeans-modules-php-project") // NOI18N
    public static SharabilityQueryImplementation2 forPhpProject(Project project) {
        return create(project);
    }

    @ProjectServiceProvider(service = SharabilityQueryImplementation2.class, projectType = "org-netbeans-modules-web-project") // NOI18N
    public static SharabilityQueryImplementation2 forWebProject(Project project) {
        return create(project);
    }

    @ProjectServiceProvider(service = SharabilityQueryImplementation2.class, projectType = "org-netbeans-modules-maven") // NOI18N
    public static SharabilityQueryImplementation2 forMavenProject(Project project) {
        return create(project);
    }

    @Override
    public SharabilityQuery.Sharability getSharability(URI uri) {
        if (isVersioningIgnored()
                && uri.equals(getBowerComponentsDir())) {
            return SharabilityQuery.Sharability.NOT_SHARABLE;
        }
        return SharabilityQuery.Sharability.UNKNOWN;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (BowerrcJson.PROP_DIRECTORY.equals(evt.getPropertyName())) {
            bowerComponentsDir = null;
        }
    }

    private URI getBowerComponentsDir() {
        if (bowerComponentsDir == null) {
            bowerComponentsDir = Utilities.toURI(bowerrcJson.getBowerComponentsDir());
        }
        return bowerComponentsDir;
    }

    public boolean isVersioningIgnored() {
        if (versioningIgnored == null) {
            versioningIgnored = BowerOptions.getInstance().isIgnoreBowerComponents();
        }
        return versioningIgnored;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (BowerOptions.IGNORE_BOWER_COMPONENTS.equals(evt.getKey())) {
            versioningIgnored = null;
        }
    }

}
