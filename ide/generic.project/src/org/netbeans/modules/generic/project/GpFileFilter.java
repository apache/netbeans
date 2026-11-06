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

package org.netbeans.modules.generic.project;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * The data filter of the project's logical view: it hides the files
 * matching the project's additional "ignored files" pattern (see
 * {@link GenericProject#getFilesFilterPattern}) and, unless disabled
 * per project (see {@link GenericProject#isGlobalFileFilterEnabled}),
 * the files hidden by the IDE's global "ignored files" pattern.
 *
 * <p>Reacts to changes of the global pattern ({@link VisibilityQuery})
 * and of the project's settings (the private and shared preferences
 * nodes) by notifying its listeners on the EDT, so the project node
 * rescans the directory. The query listener is registered weakly; the
 * {@link #privatePrefs} and {@link #sharedPrefs} fields pin the nodes,
 * which the preferences provider references only weakly.
 */
final class GpFileFilter implements ChangeableDataFilter, DataFilter.FileBased,
        ChangeListener, PreferenceChangeListener {

    private static final Logger LOG = Logger.getLogger(GpFileFilter.class.getName()); // NOI18N

    private final GenericProject project;
    private final Preferences privatePrefs;
    private final Preferences sharedPrefs;

    private volatile boolean globalFilterEnabled;
    /** The additional pattern; {@code null} means no additional filter. */
    private volatile Pattern extraPattern;
    /** The stored additional pattern text, for change detection. */
    private volatile String extraPatternText;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    GpFileFilter(GenericProject project) {
        this.project = project;
        this.privatePrefs = ProjectUtils.getPreferences(project, GenericProject.class, false);
        this.sharedPrefs = ProjectUtils.getPreferences(project, GenericProject.class, true);
        this.globalFilterEnabled = GenericProject.isGlobalFileFilterEnabled(project);
        this.extraPatternText = GenericProject.getFilesFilterPattern(project);
        this.extraPattern = compileFilesFilterPattern(GenericProject.getFilesFilterPattern(project));

        VisibilityQuery.getDefault().addChangeListener(WeakListeners.change(this, VisibilityQuery.getDefault()));
        // change events are delivered asynchronously, on the
        // preferences event thread (not the EDT)
        if (privatePrefs != null) {
            privatePrefs.addPreferenceChangeListener(this);
        }
        if (sharedPrefs != null) {
            sharedPrefs.addPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean acceptDataObject(DataObject obj) {
        FileObject fo = obj.getPrimaryFile();
        return fo != null && acceptFileObject(fo);
    }

    @Override
    public boolean acceptFileObject(FileObject fo) {
        if (extraPattern != null && extraPattern.matcher(fo.getNameExt()).find()) {
            return false;
        }
        if (!globalFilterEnabled) {
            return true;
        }
        return VisibilityQuery.getDefault().isVisible(fo);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        String key = evt.getKey();
        boolean changed = false;
        if (GenericProject.GLOBAL_FILE_FILTER_KEY.equals(key)) {
            boolean nue = GenericProject.isGlobalFileFilterEnabled(project);
            if (nue != globalFilterEnabled) {
                globalFilterEnabled = nue;
                changed = true;
            }
        } else if (GenericProject.FILES_FILTER_PATTERN_KEY.equals(key)) {
            String nue = GenericProject.getFilesFilterPattern(project);
            if (!Objects.equals(nue, extraPatternText)) {
                extraPatternText = nue;
                extraPattern = compileFilesFilterPattern(nue);
                changed = true;
            }
        }
        if (changed) {
            stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // forward on the EDT: the global pattern may change from any
        // thread (e.g. the options dialog) and the preferences event
        // arrives on the preferences event thread
        if (SwingUtilities.isEventDispatchThread()) {
            changeSupport.fireChange();
        } else {
            RequestProcessor.getDefault().post(changeSupport::fireChange);
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    /**
     * Compiles the given additional files filter pattern, returning
     * {@code null} for blank input or an invalid pattern (a warning is
     * logged for the latter, which thus acts as no additional filter).
     */
    private static Pattern compileFilesFilterPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return null;
        }
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException ex) {
            LOG.log(Level.WARNING, "Invalid files filter pattern \"{0}\": {1}",
                    new Object[] { pattern, ex.getMessage() });
            return null;
        }
    }

}