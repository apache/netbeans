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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.SharabilityQuery;
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
 * FileObject attribute. The logical view of the project simply shows
 * all files contained in the project directory.
 */
public final class GenericProject implements Project {

    public static final String MARKER_FILE = ".nbgp"; // NOI18N

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
                new GpProjectContainerProvider(this),
                new GpCustomizerProvider(this),
                new GpProjectOperations(this),
                new GpActionProvider(this),
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
     * Promotes the given directory to a local (attribute-based)
     * generic project and returns its canonical project instance.
     *
     * <p>If the directory already is a project (of any type), the
     * existing project is returned without modification. A directory
     * that the {@code SharabilityQuery} marks as {@code NOT_SHARABLE}
     * is never promoted; {@code null} is returned in that case.
     *
     * @param dir the directory to promote (never {@code null})
     * @return the project for the directory, or {@code null} if the
     *         directory cannot be promoted
     * @throws IOException if the recognition attribute could not be set
     */
    public static Project promoteToProject(FileObject dir) throws IOException {
        Project prj = ProjectManager.getDefault().findProject(dir);
        if (prj != null) {
            // the directory is already a project (of any type):
            // idempotent, nothing to do
            return prj;
        }
        if (SharabilityQuery.getSharability(dir) == SharabilityQuery.Sharability.NOT_SHARABLE) {
            // the flip side of the NOT_SHARABLE check in the container
            // provider (which is about reading projects out of a
            // non-sharable directory): a per-user promotion must not
            // touch a directory that is not meant to be shared
            return null;
        }
        setMarkerState(dir, false);
        // drop the negative cache entry the lookup above stored for a
        // directory that was not a project yet
        ProjectManager.getDefault().clearNonProjectCache();
        return ProjectManager.getDefault().findProject(dir);
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
}
