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

import java.io.IOException;
import java.util.Objects;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.generic.project.ui.actions.GpActionProvider;
import org.netbeans.modules.generic.project.ui.actions.GpProjectOperations;
import org.netbeans.modules.generic.project.ui.customizer.GpCustomizerProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * A generic project. A directory is recognized as a generic project
 * if it contains the {@link #MARKER_FILE} or, for local
 * (private) projects, if it carries the {@link #MARKER_ATTRIBUTE}
 * FileObject attribute. The logical view of the project shows the
 * files contained in the project directory, subject to the IDE's
 * global "ignored files" filter (see
 * {@link #isGlobalFileFilterEnabled}) and the project's own
 * additional pattern (see {@link #getFilesFilterPattern}).
 *
 * <p>The marker file doubles as the project's auxiliary data file
 * (see {@link GpAuxiliaryProperties}): it marks the directory as a
 * project and stores what {@link ProjectUtils#getPreferences}
 * persists (e.g. the display name override). For local (marker-less)
 * projects the same data lives in the user configuration.
 */
public final class GenericProject implements Project {

    /**
     * Name of the marker file: identical to the auxiliary data file
     * ({@link GpAuxiliaryProperties#dataFileName}), which thus has a
     * dual role — it both marks the directory as a project and
     * stores the project's auxiliary data.
     */
    public static final String MARKER_FILE = GpAuxiliaryProperties.dataFileName();

    /**
     * Name of the FileObject attribute that marks a project directory
     * as a generic project without the marker file (local project).
     */
    public static final String MARKER_ATTRIBUTE = "org.netbeans.modules.generic.project"; // NOI18N

    /**
     * The project type key under which this project type is registered
     * in the layer filesystem:
     * <ul>
     * <li>{@code Projects/<key>/Actions} — the stock actions of the
     *     project node's popup menu (loaded via
     *     {@code CommonProjectActions.forType}),</li>
     * <li>{@code Projects/<key>/Lookup} — services contributed via
     *     {@code @ProjectServiceProvider} / {@code LookupProvider},
     *     mixed into the project's lookup (see
     *     {@code LookupProviderSupport}).</li>
     * </ul>
     */
    public static final String PROJECT_KEY = "org-netbeans-modules-generic-project"; // NOI18N

    @StaticResource
    public static final String ICON = "org/netbeans/modules/generic/project/gp.svg"; // NOI18N

    private final FileObject projectDirectory;
    private final Lookup lookup;

    /**
     * Whether this project instance has been explicitly deleted
     * (stock delete operation). Set before the marker file and the
     * recognition attribute are removed so that
     * {@link GpFactory#saveProject} never resurrects the project
     * (e.g. when a preferences flush saves it afterwards).
     */
    private volatile boolean deleted;

    /**
     * Marks this project instance as explicitly deleted, see
     * {@link #deleted}.
     */
    public void markDeleted() {
        deleted = true;
    }

    /**
     * Whether this project instance has been explicitly deleted,
     * see {@link #deleted}.
     */
    boolean isDeleted() {
        return deleted;
    }

    GenericProject(FileObject projectDirectory) {
        this.projectDirectory = projectDirectory;
        // mix the project's own services with the layer contributions
        // registered for this project type ("Projects/<key>/Lookup",
        // e.g. via @ProjectServiceProvider in other modules)
        this.lookup = LookupProviderSupport.createCompositeLookup(Lookups.fixed(
                new GpLogicalViewProvider(this),
                new GpInfo(this),
                new GpSubprojectProvider(this),
                new GpCustomizerProvider(this),
                new GpProjectOperations(this),
                new GpActionProvider(this),
                new GpAuxiliaryProperties(this),
                this
        ), "Projects/" + PROJECT_KEY + "/Lookup"); // NOI18N
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    static boolean isProject(FileObject projectDirectory) {
        return projectDirectory != null
            && (hasMarker(projectDirectory) || isAttributeProject(projectDirectory));
    }

    /**
     * Whether the given directory contains the marker file.
     */
    public static boolean hasMarker(FileObject dir) {
        return dir != null && dir.getFileObject(MARKER_FILE) != null;
    }

    /**
     * Whether the given directory is marked as a generic project by
     * the {@link #MARKER_ATTRIBUTE} FileObject attribute (local
     * project).
     */
    public static boolean isAttributeProject(FileObject dir) {
        return dir != null && Boolean.TRUE.equals(dir.getAttribute(MARKER_ATTRIBUTE));
    }

    /**
     * Creates the marker file in the given directory if it does not
     * exist yet, returning the marker.
     */
    static FileObject createMarker(FileObject dir) throws IOException {
        FileObject marker = dir.getFileObject(MARKER_FILE);
        if (marker == null) {
            marker = dir.createData(MARKER_FILE);
            marker.setAttribute("displayName", dir.getNameExt()); // NOI18N
        }
        return marker;
    }

    /**
     * Adjusts the directory so the marker is (or isn't) a file in the
     * working tree.
     *
     * <p>Turning the marker off stores the {@link #MARKER_ATTRIBUTE}
     * <em>before</em> deleting the marker file, and turning it on
     * creates the marker <em>before</em> clearing the attribute, so
     * the directory is never in a state where it is unrecognized.
     *
     * @param dir the project directory
     * @param markerFile whether the marker file should exist in the
     *        working tree afterwards
     */
    public static void setMarkerState(FileObject dir, boolean markerFile) throws IOException {
        if (markerFile) {
            createMarker(dir);
            if (isAttributeProject(dir)) {
                dir.setAttribute(MARKER_ATTRIBUTE, null);
            }
        } else {
            dir.setAttribute(MARKER_ATTRIBUTE, Boolean.TRUE);
            FileObject marker = dir.getFileObject(MARKER_FILE);
            if (marker != null) {
                marker.delete();
            }
        }
    }

    /**
     * Key of the display name override in the project's private
     * preferences ({@link ProjectUtils#getPreferences}).
     */
    static final String DISPLAY_NAME_KEY = "displayName"; // NOI18N

    /**
     * Returns the display name override of the project, or
     * {@code null} if none is stored, in which case the display name
     * defaults to the project folder name.
     */
    public static String getDisplayNameOverride(GenericProject prj) {
        Preferences prefs = ProjectUtils.getPreferences(prj, GenericProject.class, false);
        if (prefs == null) {
            return null;
        }
        String name = prefs.get(DISPLAY_NAME_KEY, null);
        return (name == null || name.isBlank()) ? null : name;
    }

    /**
     * Stores the display name override of the project, or clears it
     * for {@code null}. A name equal to the project folder name is
     * treated as no override.
     *
     * <p>Fires {@link ProjectInformation#PROP_DISPLAY_NAME} on the
     * project's {@link ProjectInformation} if the effective display
     * name changes, so that the project view updates its label.
     */
    public static void setDisplayNameOverride(GenericProject prj, String name) {
        if (name != null && name.equals(prj.getProjectDirectory().getNameExt())) {
            name = null;
        }
        if (Objects.equals(name, getDisplayNameOverride(prj))) {
            return;
        }
        Preferences prefs = ProjectUtils.getPreferences(prj, GenericProject.class, false);
        if (prefs == null) {
            return;
        }
        if (name == null) {
            prefs.remove(DISPLAY_NAME_KEY);
        } else {
            prefs.put(DISPLAY_NAME_KEY, name);
        }
        GpInfo info = (GpInfo) prj.getLookup().lookup(ProjectInformation.class);
        info.fireDisplayNameChanged();
    }

    /**
     * Key of the global file filter toggle in the project's private
     * preferences ({@link ProjectUtils#getPreferences}).
     */
    static final String GLOBAL_FILE_FILTER_KEY = "useGlobalFileFilter"; // NOI18N

    /**
     * Whether the project's logical view applies the IDE's global
     * "ignored files" filter (Tools/Options/File Types). Enabled by
     * default.
     */
    public static boolean isGlobalFileFilterEnabled(GenericProject prj) {
        Preferences prefs = ProjectUtils.getPreferences(prj, GenericProject.class, false);
        if (prefs == null) {
            return true;
        }
        return prefs.getBoolean(GLOBAL_FILE_FILTER_KEY, true);
    }

    /**
     * Stores the state of the global file filter toggle. The enabled
     * state is the default, so it is stored by removing the key and a
     * project with no other overrides keeps an empty data file.
     */
    public static void setGlobalFileFilterEnabled(GenericProject prj, boolean enabled) {
        if (isGlobalFileFilterEnabled(prj) == enabled) {
            return;
        }
        Preferences prefs = ProjectUtils.getPreferences(prj, GenericProject.class, false);
        if (prefs == null) {
            return;
        }
        if (enabled) {
            prefs.remove(GLOBAL_FILE_FILTER_KEY);
        } else {
            prefs.put(GLOBAL_FILE_FILTER_KEY, "false"); // NOI18N
        }
    }

    /**
     * Key of the additional files filter pattern in the project's
     * <em>shared</em> preferences ({@link ProjectUtils#getPreferences})
     * — the pattern is project content, like a {@code .gitignore}.
     */
    static final String FILES_FILTER_PATTERN_KEY = "fileFilterPattern"; // NOI18N

    /**
     * Returns the project's additional "ignored files" pattern — a
     * regular expression matched with {@code find()} on the file name
     * only — or {@code null} if none is stored.
     */
    public static String getFilesFilterPattern(GenericProject prj) {
        Preferences prefs = ProjectUtils.getPreferences(prj, GenericProject.class, true);
        if (prefs == null) {
            return null;
        }
        String pattern = prefs.get(FILES_FILTER_PATTERN_KEY, null);
        return (pattern == null || pattern.isBlank()) ? null : pattern;
    }

    /**
     * Stores the project's additional "ignored files" pattern, or
     * clears it for {@code null} or a blank string (the default, which
     * keeps the data file clean).
     */
    public static void setFilesFilterPattern(GenericProject prj, String pattern) {
        if (pattern != null && pattern.isBlank()) {
            pattern = null;
        }
        if (Objects.equals(pattern, getFilesFilterPattern(prj))) {
            return;
        }
        Preferences prefs = ProjectUtils.getPreferences(prj, GenericProject.class, true);
        if (prefs == null) {
            return;
        }
        if (pattern == null) {
            prefs.remove(FILES_FILTER_PATTERN_KEY);
        } else {
            prefs.put(FILES_FILTER_PATTERN_KEY, pattern);
        }
    }
}
