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
package org.netbeans.modules.java.source.remote.projects;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.spi.java.source.RemoteEditorPlatform;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author lahvac
 */
public class RemotePlatformImpl implements RemoteEditorPlatform {

    private static final String KEY_ENABLE_REMOTING = "enable.remoting";
    
    public static boolean isEnabled(Project prj) {
        return getPreferences(prj)
                           .getBoolean(KEY_ENABLE_REMOTING, false);
    }

    public static void setEnabled(Project prj, boolean enabled) {
        getPreferences(prj)
                    .putBoolean(KEY_ENABLE_REMOTING, enabled);
    }

    private static Preferences getPreferences(Project prj) {
        return ProjectUtils.getPreferences(prj, EditorProjectPanel.class, false);
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    private JavaPlatform platform;

    public RemotePlatformImpl() {}

    @Override
    public synchronized boolean isEnabled() {
        return platform != null;
    }

    public synchronized void setPlatform(JavaPlatform platform) {
        if (this.platform != platform) {
            this.platform = platform;
            cs.fireChange();
        }
    }
    @Override
    public synchronized String getJavaCommand() {
        return FileUtil.toFile(platform.findTool("java")).getAbsolutePath();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    @ServiceProvider(service=Provider.class)
    public static final class ProviderImpl implements Provider {
        private final Map<Project, RemotePlatformImpl> project2Platform = new WeakHashMap<>();

        @Override
        public RemoteEditorPlatform findPlatform(FileObject source) {
            Project prj = FileOwnerQuery.getOwner(source);
            if (prj == null) {
                return null;
            }
            RemotePlatformImpl rpi = project2Platform.computeIfAbsent(prj, p -> new RemotePlatformImpl());
            JavaPlatform foundPlatform = null;
            if (isEnabled(prj)) {
                SourceGroup[] sg = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (sg != null && sg.length > 0) {
                    ClassPath boot = ClassPath.getClassPath(sg[0].getRootFolder(), ClassPath.BOOT);
                    FileObject jlObject = boot.findResource("java/lang/Object.class");
                    for (JavaPlatform jp : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                        if (Objects.equals(jp.getBootstrapLibraries().findResource("java/lang/Object.class"), jlObject)) {
                            if (hasUsableJavac(jp.getSpecification().getVersion())) {
                                foundPlatform = jp;
                            }
                            break;
                        }
                    }
                }
            }
            rpi.setPlatform(foundPlatform);
            return rpi;
        }
        
        private static final SpecificationVersion MINIMAL_JAVAC_VERSION = new SpecificationVersion("17");

        private static boolean hasUsableJavac(SpecificationVersion sv) {
            return sv.compareTo(MINIMAL_JAVAC_VERSION) >= 0;
        }

    }
}
