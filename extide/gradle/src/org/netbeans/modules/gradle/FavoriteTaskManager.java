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
package org.netbeans.modules.gradle;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleTask;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = FavoriteTaskManager.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public final class FavoriteTaskManager {

    Set<GradleTask> favorites;

    private static final String FAVORITE_TASKS_PROP = "favorite.tasks"; //NOI18N

    ChangeSupport support;
    PropertyChangeListener listener;
    private boolean loaded;
    final Project project;

    public FavoriteTaskManager(Project p) {
        project = p;
        listener = (e) -> updateFavorites(p);
        NbGradleProject.addPropertyChangeListener(p, WeakListeners.propertyChange(listener, NbGradleProject.get(p)));
    }

    public boolean isFavorite(GradleTask task) {
        return favorites() != null ? favorites().contains(task) : false;
    }

    public void setFavorite(GradleTask task, boolean favorite) {
        boolean changed = false;
        if (favorites == null) {
            changed = true;
            favorites = new TreeSet<>(Comparator.comparing(GradleTask::getName));
        }
        changed = favorite ? favorites.add(task) : favorites.remove(task);
        if (changed) {
            StringBuilder sb = new StringBuilder();
            String separator = "";
            for (GradleTask t : favorites) {
                sb.append(separator).append(t.getName());
                separator = ",";
            }
            Preferences prefs = NbGradleProject.getPreferences(project, false);
            prefs.put(FAVORITE_TASKS_PROP, sb.toString());
            fireChange();
        }
    }

    public Set<GradleTask> getFavoriteTasks() {
        return favorites() != null ? new LinkedHashSet<>(favorites) : Collections.emptySet();
    }

    public void addChangeListener(ChangeListener l) {
        if (support == null) {
            support = new ChangeSupport(this);
        }
        support.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        if (support != null) {
            support.removeChangeListener(l);
            if (!support.hasListeners()) {
                support = null;
            }
        }
    }

    private void fireChange() {
        if (support != null) {
            support.fireChange();
        }
    }

    private void updateFavorites(Project project) {
        Preferences prefs = NbGradleProject.getPreferences(project, false);
        String favoriteProp = prefs.get(FAVORITE_TASKS_PROP, ""); //NOI18N
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gbp != null) {
            Set<GradleTask> newFavorites = new TreeSet<>(Comparator.comparing(GradleTask::getName));
            for (String taskName : favoriteProp.split(",")) {
                GradleTask task = gbp.getTaskByName(taskName);
                if (task != null) {
                    newFavorites.add(task);
                }
            }
            if (!Objects.equals(favorites, newFavorites)) {
                favorites = newFavorites;
                fireChange();
            }
        } else {
            if (favorites != null) {
                favorites = null;
                fireChange();
            }
        }

    }

    private Set<GradleTask> favorites() {
        if (!loaded) {
            updateFavorites(project);
            loaded = true;
        }
        return favorites;
    }
}
