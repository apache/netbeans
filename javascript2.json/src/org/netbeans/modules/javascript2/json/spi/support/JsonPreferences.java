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
