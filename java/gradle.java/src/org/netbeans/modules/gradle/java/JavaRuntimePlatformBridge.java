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
package org.netbeans.modules.gradle.java;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lkishalmi
 */
@ServiceProvider(service = JavaRuntimeManager.class, position = 0)
public class JavaRuntimePlatformBridge implements JavaRuntimeManager {
    private static final String PROP_PLATFORM_ID = "platform.ant.name"; //NOI18N

    private Map<String, JavaRuntime> cachedRuntimes = null;
    private boolean listenerInitialized = false;
    private final PropertyChangeListener clearCache;

    private final ChangeSupport support;

    public JavaRuntimePlatformBridge() {
        support = new ChangeSupport(this);
        clearCache =  (evt) -> {
            cachedRuntimes = null;
            support.fireChange();
        };
    }

    @Override
    public Map<String, JavaRuntime> getAvailableRuntimes() {
        synchronized (this) {
            if (cachedRuntimes == null) {

                JavaPlatformManager jpm = JavaPlatformManager.getDefault();

                if (!listenerInitialized) {
                    jpm.addPropertyChangeListener(WeakListeners.propertyChange(clearCache, jpm));
                    listenerInitialized = true;
                }

                Map<String, JavaRuntime> runtimes = new HashMap<>();
                for (JavaPlatform platform : jpm.getInstalledPlatforms()) {
                    String id = platform.getProperties().get(PROP_PLATFORM_ID);
                    if (platform.isValid() && (id != null)) {
                        FileObject javaInstallDir = platform.getInstallFolders().iterator().next();
                        runtimes.put(id, JavaRuntimeManager.createJavaRuntime(id, platform.getDisplayName(), FileUtil.toFile(javaInstallDir)));
                    }
                }
                cachedRuntimes = Collections.unmodifiableMap(runtimes);
            }
            return cachedRuntimes;
        }
    }

    @Override
    public Optional<Runnable> manageRuntimesAction() {
        return Optional.of(() -> PlatformsCustomizer.showCustomizer(null));
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }

}
