/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
