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

package org.netbeans.modules.gradle.java.queries;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleSourceLevelImpl implements SourceLevelQueryImplementation2 {

    final Project project;
    final Map<FileObject, Result2> cache = new WeakHashMap<>();

    public GradleSourceLevelImpl(Project project) {
        this.project = project;
    }

    SourceLevelQuery.Profile getSourceProfile(FileObject javaFile) {
        return SourceLevelQuery.Profile.DEFAULT;
    }

    String getSourceLevelString(FileObject javaFile) {
        String ret = null;
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if (gjp != null) {
            GradleJavaSourceSet sourceSet = gjp.containingSourceSet(FileUtil.toFile(javaFile));
            ret = sourceSet!= null ? sourceSet.getSourcesCompatibility() : null;
        }
        return ret;
    }

    @Override
    public Result getSourceLevel(FileObject javaFile) {
        synchronized(cache) {
            Result2 ret = cache.get(javaFile);
            if (ret == null) {
                ret = new ResultImpl(javaFile);
                cache.put(javaFile, ret);
            }
            return ret;
        }
    }

    private class ResultImpl implements SourceLevelQueryImplementation2.Result2, PropertyChangeListener {

        private final FileObject javaFile;
        private final ChangeSupport cs = new ChangeSupport(this);
        private final PropertyChangeListener pcl = WeakListeners.propertyChange(this, NbGradleProject.get(project));
        private String cachedLevel = null;
        private SourceLevelQuery.Profile cachedProfile;
        private final Object CACHE_LOCK = new Object();

        ResultImpl(FileObject javaFile) {
            this.javaFile = javaFile;
            NbGradleProject.addPropertyChangeListener(project, pcl);
        }

        @Override public String getSourceLevel() {
            synchronized (CACHE_LOCK) {
                if (cachedLevel == null) {
                    cachedLevel = getSourceLevelString(javaFile);
                }
                return cachedLevel;
            }
        }

        @Override public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                Project p = (Project) evt.getSource();
                if (p.getLookup().lookup(NbGradleProject.class).isUnloadable()) {
                    return; //let's just continue with the old value, rescanning classpath for broken project and re-creating it later serves no greater good.
                }
                synchronized (CACHE_LOCK) {
                    cachedLevel = null;
                    cachedProfile = null;
                }
                cs.fireChange();
            }
        }

        @Override
        public SourceLevelQuery.Profile getProfile() {
            synchronized (CACHE_LOCK) {
                if (cachedProfile == null) {
                    cachedProfile = getSourceProfile(javaFile);
                }
                return cachedProfile;
            }
        }

    }
}
