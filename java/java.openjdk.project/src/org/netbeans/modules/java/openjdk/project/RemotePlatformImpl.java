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
package org.netbeans.modules.java.openjdk.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.spi.java.source.RemoteEditorPlatform;

/**
 *
 * @author lahvac
 */
public class RemotePlatformImpl implements RemoteEditorPlatform {

    private static final Map<FileObject, RemotePlatformImpl> jdkRoot2Platform = new HashMap<>();

    public static @NonNull RemotePlatformImpl getProvider(FileObject jdkRoot, ConfigurationImpl.ProviderImpl configurations) {
        return jdkRoot2Platform.computeIfAbsent(jdkRoot, r -> new RemotePlatformImpl(configurations));
    }

    private final ChangeSupport cs = new ChangeSupport(this);
    private final ConfigurationImpl.ProviderImpl configurations;
    private File modules;
    private FileChangeListener modulesListener = new FileChangeAdapter() {
        @Override
        public void fileChanged(FileEvent fe) {
            cs.fireChange();
        }
    };

    public RemotePlatformImpl(ConfigurationImpl.ProviderImpl configurations) {
        this.configurations = configurations;
        this.configurations.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if (ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE.equals(evt.getPropertyName()) ||
                    evt.getPropertyName() == null) {
                    listenOnModules();
                    cs.fireChange();
                }
            }
        });
        listenOnModules();
    }

    private synchronized void listenOnModules() {
        if (modules != null) {
            FileUtil.removeFileChangeListener(modulesListener, modules);
        }
        modules = FileUtil.normalizeFile(new File(configurations.getActiveConfiguration().getLocation(),
                                         "images/jdk/lib/modules".replace("/", System.getProperty("file.separator"))));
        FileUtil.addFileChangeListener(modulesListener, modules);
    }

    @Override
    public String getJavaCommand() {
        return new File(configurations.getActiveConfiguration().getLocation(),
                        "images/jdk/bin/java".replace("/", System.getProperty("file.separator")))
               .getAbsolutePath();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    public static Lookup createProvider(FileObject jdkRoot, JDKProject project) {
        Provider provider = new Provider() {
            @Override
            public RemoteEditorPlatform findPlatform(FileObject file) {
                switch (project.getLookup().lookup(Settings.class).getUseRemotePlatform()) {
                    case JAVA_COMPILER:
                        if (!"java.compiler".equals(project.getProjectDirectory().getNameExt())) {
                            return null;
                        }
                        //fall-through:
                    case ALWAYS:
                        return RemotePlatformImpl.getProvider(jdkRoot, project.configurations);
                    default:
                    case NEVER: return null;
                }
            }
        };

        return Lookups.singleton(provider);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
