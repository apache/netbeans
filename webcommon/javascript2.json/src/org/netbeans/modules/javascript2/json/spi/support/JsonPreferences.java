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
package org.netbeans.modules.javascript2.json.spi.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Json per project settings.
 * @author Tomas Zezula
 * @since 1.2
 */
public final class JsonPreferences {
    public static final String PROP_COMMENT_SUPPORTED = "commentSupported"; //NOI18N
    private static final String PREF_JSON_COMMENTS = "json.comments";   //NOI18N

    private final Project project;
    private final PropertyChangeSupport listeners;
    private final PreferenceChangeListener pcl = (e) -> fireChange(e.getKey());
    private final AtomicReference<Preferences> prefsRef;
    private final AtomicBoolean listens;

    private JsonPreferences(@NonNull final Project project) {
        this.project = project;
        this.listeners = new PropertyChangeSupport(this);
        this.prefsRef = new AtomicReference<>();
        this.listens = new AtomicBoolean();
    }

    /**
     * Returns true if comments are allowed for JSON files.
     * @return the comments status
     */
    public boolean isCommentSupported() {
        final Preferences prefs = getPreferences();
        return prefs.getBoolean(PREF_JSON_COMMENTS, false);
    }

    /**
     * Allow comments in JSON files.
     * @param supported the value to set
     */
    public void setCommentSupported(final boolean supported) {
        final Preferences prefs = getPreferences();
        if (supported) {
            prefs.putBoolean(PREF_JSON_COMMENTS, supported);
        } else {
            prefs.remove(PREF_JSON_COMMENTS);
        }
    }

    /**
     * Adds {@link PropertyChangeListener}.
     * @param listener the listener to be added
     * @since 1.5
     */
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        if (!listens.get() && listens.compareAndSet(false, true)) {
            final Preferences p = getPreferences();
            p.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, pcl, p));
        }
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Removes {@link PropertyChangeListener}.
     * @param listener the listener to be removed
     * @since 1.5
     */
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener); //NOI18N
        listeners.removePropertyChangeListener(listener);
    }

    private Preferences getPreferences() {
        //Needs to hold a reference to Preferences otherwise listeining does not work
        Preferences pref = prefsRef.get();
        if (pref == null) {
            pref = ProjectUtils.getPreferences(project, JsonPreferences.class, true);
            if (!prefsRef.compareAndSet(null, pref)) {
                pref = prefsRef.get();
                assert pref != null;
            }
        }
        return pref;
    }

    private void fireChange(@NonNull final String key) {
        String prop = null;
        switch (key) {
            case PREF_JSON_COMMENTS:
                prop = PROP_COMMENT_SUPPORTED;
                break;
        }
        if (prop != null) {
            listeners.firePropertyChange(key, null, null);
        }
    }

    /**
     * Returns {@link JsonPreferences} for given {@link Project}.
     * @param project the {@link Project} to return {@link JsonPreferences} for
     * @return the {@link JsonPreferences}
     */
    @NonNull
    public static JsonPreferences forProject(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        return new JsonPreferences(project);
    }
}
