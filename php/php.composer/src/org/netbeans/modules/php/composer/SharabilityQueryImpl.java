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
package org.netbeans.modules.php.composer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.php.composer.files.ComposerJson;
import org.netbeans.modules.php.composer.options.ComposerOptions;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

public final class SharabilityQueryImpl implements SharabilityQueryImplementation2, PropertyChangeListener, PreferenceChangeListener {

    final ComposerJson composerJson;

    private volatile URI vendorDir = null;
    private volatile Boolean versioningIgnored;


    private SharabilityQueryImpl(Project project) {
        assert project != null;
        composerJson = new ComposerJson(project.getProjectDirectory());
    }

    @ProjectServiceProvider(service = SharabilityQueryImplementation2.class, projectType = "org-netbeans-modules-php-project") // NOI18N
    public static SharabilityQueryImplementation2 create(Project project) {
        SharabilityQueryImpl sharabilityQuery = new SharabilityQueryImpl(project);
        sharabilityQuery.composerJson.addPropertyChangeListener(WeakListeners.propertyChange(sharabilityQuery, sharabilityQuery.composerJson));
        ComposerOptions composerOptions = ComposerOptions.getInstance();
        composerOptions.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, sharabilityQuery, composerOptions));
        return sharabilityQuery;
    }

    @Override
    public SharabilityQuery.Sharability getSharability(URI uri) {
        if (isVersioningIgnored()
                && uri.equals(getVendorDir())) {
            return SharabilityQuery.Sharability.NOT_SHARABLE;
        }
        return SharabilityQuery.Sharability.UNKNOWN;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ComposerJson.PROP_VENDOR_DIR.equals(evt.getPropertyName())) {
            vendorDir = null;
        }
    }

    private URI getVendorDir() {
        if (vendorDir == null) {
            vendorDir = Utilities.toURI(composerJson.getVendorDir());
        }
        return vendorDir;
    }

    public boolean isVersioningIgnored() {
        if (versioningIgnored == null) {
            versioningIgnored = ComposerOptions.getInstance().isIgnoreVendor();
        }
        return versioningIgnored;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (ComposerOptions.IGNORE_VENDOR.equals(evt.getKey())) {
            versioningIgnored = null;
        }
    }

}
