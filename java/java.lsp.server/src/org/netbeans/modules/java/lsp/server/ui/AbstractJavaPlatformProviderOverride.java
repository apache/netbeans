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
package org.netbeans.modules.java.lsp.server.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.java.platform.JavaPlatformFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public abstract class AbstractJavaPlatformProviderOverride implements JavaPlatformProvider {

    private static final JavaPlatform[] NO_PLATFORMS = new JavaPlatform[0];
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private FileObject defaultPlatformOverride;

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public JavaPlatform[] getInstalledPlatforms() {
        return NO_PLATFORMS;
    }

    @Override
    public JavaPlatform getDefaultPlatform() {
        FileObject override;

        synchronized (this) {
            override = defaultPlatformOverride;
        }

        if (override == null) {
            return null;
        }

        Set<String> existingNames = new HashSet<>();
        JavaPlatform found = null;

        for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if (platform.getInstallFolders().stream().anyMatch(folder -> folder.equals(override))) {
                found = platform;
                break;
            }
            existingNames.add(platform.getDisplayName());
        }

        if (found == null ){
            String newName = defaultPlatformOverride.getPath();

            while (existingNames.contains(newName)) {
                newName += "1";
            }

            for (JavaPlatformFactory.Provider provider : Lookup.getDefault().lookupAll(JavaPlatformFactory.Provider.class)) {
                JavaPlatformFactory factory = provider.forType("j2se");
                if (factory != null) {
                    try {
                        found = factory.create(override, newName, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        return found;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private void dosetDefaultPlatformOverride(String defaultPlatformOverride) {
        FileObject override = defaultPlatformOverride != null ? FileUtil.toFileObject(new File(defaultPlatformOverride))
                                                              : null;

        synchronized (this) {
            this.defaultPlatformOverride = override;
        }

        pcs.firePropertyChange(null, null, null);
    }

    public static void setDefaultPlatformOverride(String defaultPlatformOverride) {
        for (JavaPlatformProvider p : Lookup.getDefault().lookupAll(JavaPlatformProvider.class)) {
            if (p instanceof AbstractJavaPlatformProviderOverride) {
                ((AbstractJavaPlatformProviderOverride) p).dosetDefaultPlatformOverride(defaultPlatformOverride);
            }
        }
    }

}
