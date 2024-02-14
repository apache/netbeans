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

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.spi.project.SourceGroupModifierImplementation;
import org.netbeans.spi.project.SourceGroupRelativeModifierImplementation;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

// XXX should perhaps be legal to call add* methods at any time (should update things)
// and perhaps also have remove* methods
// and have code names for each source dir?

// XXX should probably all be wrapped in ProjectManager.mutex

/**
 * Helper class to work with source roots and typed folders of a project.
 * @author Jesse Glick
 */
public final class SourcesHelper {
    
    private static final Logger LOG = Logger.getLogger(SourcesHelper.class.getName());

    private class Root {
        protected final String location;
        public Root(String location) {
            this.location = location;
        }
        public final File getActualLocation() {
            String val = evaluator.evaluate(location);
            if (val == null) {
                return null;
            }
            return aph.resolveFile(val);
        }
        public Collection<FileObject> getIncludeRoots(boolean minimalSubfolders) {
            File loc = getActualLocation();
            if (loc != null) {
                FileObject fo = FileUtil.toFileObject(loc);
                if (fo != null) {
                    return Collections.singleton(fo);
                }
            }
            return Collections.emptySet();
        }
        @Override
        public String toString() {
            return "Root[" + location + "]";
        }
    }
    
    private class SourceRoot extends Root {

        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;
        private final String includes;
        private final String excludes;
        private final String hint;
        private boolean removed;    // just for sanity checking
        private final String[] projectParts;
        
        public SourceRoot(String location, String includes, String excludes, String hint, String displayName, Icon icon, Icon openedIcon, String[] parts) {
            super(location);
            this.displayName = displayName;
            this.icon = icon;
            this.openedIcon = openedIcon;
            this.includes = includes;
            this.excludes = excludes;
            this.hint = hint;
            this.projectParts = parts;
            removed = false;
        }

        public final SourceGroup toGroup(FileObject loc) {
            assert loc != null;
            return new Group(loc);
        }

        public String getHint() {
            return hint;
        }

        public boolean isRemoved() {
            return removed;
        }

        @Override
        public String toString() {
            return "SourceRoot[" + location + "]"; // NOI18N
        }

        // Copied w/ mods from GenericSources.
        private final class Group implements SourceGroup, PropertyChangeListener {

            private final FileObject loc;
            private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

            Group(FileObject loc) {
                this.loc = loc;
                evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
            }

            public FileObject getRootFolder() {
                return loc;
            }

            public String getName() {
                return location.length() > 0 ? location : "generic"; // NOI18N
            }

            public String getDisplayName() {
                return displayName;
            }

            public Icon getIcon(boolean opened) {
                return opened ? icon : openedIcon;
            }

            @Override public boolean contains(FileObject file) {
                if (file == loc) {
                    return true;
                }
                String path = FileUtil.getRelativePath(loc, file);
                if (path == null) {
                    return false;
                }
                if (file.isFolder()) {
                    path += "/"; // NOI18N
                }
                if (!computeIncludeExcludePatterns().matches(path, true)) {
                    return false;
                }
                Project p = getProject();
                if (file.isFolder() &&
                        file != p.getProjectDirectory() &&
                        ProjectManager.getDefault().isProject(file) &&
                        !ProjectConvertors.isConvertorProject(FileOwnerQuery.getOwner(file))) {
                    // #67450: avoid actually loading the nested project.
                    return false;
                }
                if (!(SourceRoot.this instanceof TypedSourceRoot)) {
                    // XXX disabled for typed source roots; difficult to make fast (#97215)
                    Project owner = FileOwnerQuery.getOwner(file);
                    if (owner != null &&
                            owner != p &&
                            !ProjectConvertors.isConvertorProject(owner)) {
                        return false;
                    }
                    if (SharabilityQuery.getSharability(file) == SharabilityQuery.Sharability.NOT_SHARABLE) {
                        return false;
                    } // else MIXED, UNKNOWN, or SHARABLE
                }
                return true;
            }

            public void addPropertyChangeListener(PropertyChangeListener l) {
                pcs.addPropertyChangeListener(l);
            }

            public void removePropertyChangeListener(PropertyChangeListener l) {
                pcs.removePropertyChangeListener(l);
            }

            @Override
            public String toString() {
                return "SourcesHelper.Group[name=" + getName() + ",rootFolder=" + getRootFolder() + "]"; // NOI18N
            }

            public void propertyChange(PropertyChangeEvent ev) {
                assert ev.getSource() == evaluator : ev;
                String prop = ev.getPropertyName();
                if (prop == null ||
                        (includes != null && includes.contains("${" + prop + "}")) || // NOI18N
                        (excludes != null && excludes.contains("${" + prop + "}"))) { // NOI18N
                    resetIncludeExcludePatterns();
                    pcs.firePropertyChange(PROP_CONTAINERSHIP, null, null);
                }
                // XXX should perhaps react to ProjectInformation changes? but nothing to fire currently
            }

        }

        private String evalForMatcher(String raw) {
            if (raw == null) {
                return null;
            }
            String patterns = evaluator.evaluate(raw);
            if (patterns == null) {
                return null;
            }
            if (patterns.matches("\\$\\{[^}]+\\}")) { // NOI18N
                // Unevaluated single property, treat like null.
                return null;
            }
            return patterns;
        }

        private PathMatcher matcher;
        private PathMatcher computeIncludeExcludePatterns() {
            synchronized (this) {
                if (matcher != null) {
                    return matcher;
                }
            }
            String includesPattern = evalForMatcher(includes);
            String excludesPattern = evalForMatcher(excludes);
            PathMatcher _matcher = new PathMatcher(includesPattern, excludesPattern, getActualLocation());
            synchronized (this) {
                matcher = _matcher;
            }
            return _matcher;
        }
        private synchronized void resetIncludeExcludePatterns() {
            matcher = null;
        }

        @Override
        public Collection<FileObject> getIncludeRoots(boolean minimalSubfolders) {
            Collection<FileObject> supe = super.getIncludeRoots(minimalSubfolders);
            if (!minimalSubfolders) {
                return supe;
            }
            else if (supe.size() == 1) {
                Set<FileObject> roots = new HashSet<FileObject>();
                for (File r : computeIncludeExcludePatterns().findIncludedRoots()) {
                    FileObject subroot = FileUtil.toFileObject(r);
                    if (subroot != null) {
                        roots.add(subroot);
                    }
                }
                return roots;
            } else {
                assert supe.isEmpty();
                return supe;
            }
        }

    }
    
    private final class TypedSourceRoot extends SourceRoot {
        private final String type;
        public TypedSourceRoot(String type, String hint, String location, String includes, String excludes, String displayName, Icon icon, Icon openedIcon, String[] parts) {
            super(location, includes, excludes, hint, displayName, icon, openedIcon, parts);
            this.type = type;
        }
        public final String getType() {
            return type;
        }
    }
    
    private final AntProjectHelper aph;
    private final Project project;
    private final PropertyEvaluator evaluator;
    private final List<SourceRoot> principalSourceRoots = new ArrayList<SourceRoot>();
    private final List<Root> nonSourceRoots = new ArrayList<Root>();
    private final List<Root> ownedFiles = new ArrayList<Root>();
    private final List<TypedSourceRoot> typedSourceRoots = new ArrayList<TypedSourceRoot>();
    private int registeredRootAlgorithm;
    private boolean minimalSubfolders;
    /**
     * If not null, external roots that we registered the last time.
     * Used when a property change is encountered, to see if the set of external
     * roots might have changed. Hold the actual files (not e.g. URLs); see
     * {@link #registerExternalRoots} for the reason why.
     */
    private Set<FileObject> lastRegisteredRoots;
    private PropertyChangeListener propChangeL;
    
    /**
     * Create the helper object, initially configured to recognize only sources
     * contained inside the project directory.
     * @param aph an Ant project helper
     * @param evaluator a way to evaluate Ant properties used to define source locations
     * @deprecated Rather use {@link #SourcesHelper(Project, AntProjectHelper, PropertyEvaluator)}.
     */
    @Deprecated
    public SourcesHelper(AntProjectHelper aph, PropertyEvaluator evaluator) {
        this.project = null;
        this.aph = aph;
        this.evaluator = evaluator;
    }
    
    /**
     * Create the helper object, initially configured to recognize only sources
     * contained inside the project directory.
     * @param project the project object (need not yet be registered in {@link ProjectManager})
     * @param aph an Ant project helper
     * @param evaluator a way to evaluate Ant properties used to define source locations
     * @since org.netbeans.modules.project.ant/1 1.31
     */
    public SourcesHelper(Project project, AntProjectHelper aph, PropertyEvaluator evaluator) {
        Parameters.notNull("project", project);
        this.project = project;
        this.aph = aph;
        this.evaluator = evaluator;
    }

    /**
     * Helper class for configuring source roots.
     * <p>
     * A typical usage is to use it as "named parameters" idiom when adding source root:
     * <pre>
     * sourcesHelper.sourceRoot("${src.dir}").displayName("Source Packages").type("java").add();
     * </pre>
     * Note also that when adding {@link #type(String) typed} source root, principal (untyped) source
     * root usually needs to be added as well. You may reuse existing config object like this:
     * <pre>
     * sourcesHelper.sourceRoot("${src.dir}").displayName("Source Packages")
     * .add()  // adding as principal root, continuing configuration
     * .type("java").add(); // adding as typed root
     * </pre>
     * @since org.netbeans.modules.project.ant/1 1.33
     */
    public final class SourceRootConfig {
        private String location;
        private String displayName;
        private Icon icon;
        private Icon openedIcon;
        private String includes;
        private String excludes;
        private String type;
        private String hint;
        private String[] parts;

        private SourceRootConfig(String location) {
            this.location = location;
        }

        /**
         * Configures a display name (for {@link SourceGroup#getDisplayName})
         * @param value
         * @return <code>this</code>
         */
        public SourceRootConfig displayName(String value) {
            displayName = value;
            return this;
        }
        /**
         * Configures optional include list.
         * <p>
         * Value is evaluated and then treated as a comma- or space-separated pattern list,
         * as detailed in the Javadoc for {@link PathMatcher}.
         * (As a special convenience, a value consisting solely of an Ant property reference
         * which cannot be evaluated, e.g. <em>${undefined}</em>, is ignored.)
         * {@link SourceGroup#contains} will then reflect the includes and excludes for files, but note that the
         * semantics of that method requires that a folder be "contained" in case any folder or file
         * beneath it is contained, and in particular the root folder is always contained.
         * </p>
         * @param value Ant-style includes; may contain Ant property substitutions;
         *                 Only files and folders matching the pattern (or patterns),
         *                 and not specified in the {@link SourcesHelper.SourceRootConfig#excludes(String)} list,
         *                 will be {@link SourceGroup#contains included}.
         *                 Must not be <code>null</code>.
         * @return <code>this</code>
         * @throws IllegalArgumentException When <code>null</code> is passed as parameter.
         */
        public SourceRootConfig includes(String value) throws IllegalArgumentException {
            if (value == null)
                throw new IllegalArgumentException("Parameter 'value' must not be null.");    // NOI18N
            includes = value;
            return this;
        }

        /**
         * Configures optional exclude list.
         * See {@link #includes(java.lang.String)} for details.
         * @param value Ant-style excludes; may contain Ant property substitutions;
         *                 files and folders
         *                 matching the pattern (or patterns) will not be {@link SourceGroup#contains included},
         *                 even if specified in the includes list.
         *                 Must not be <code>null</code>.
         * @return <code>this</code>
         * @throws IllegalArgumentException When <code>null</code> is passed as parameter.
         */
        public SourceRootConfig excludes(String value) throws IllegalArgumentException {
            if (value == null)
                throw new IllegalArgumentException("Parameter 'value' must not be null.");    // NOI18N
            excludes = value;
            return this;
        }

        /**
         * Turns a root into typed source root which will be considered only in certain contexts.
         * When not set, root is considered to be principal source root. See class javadoc for details.
         * @param value a source root type such as
         * <a href="@org-netbeans-modules-java-project@/org/netbeans/api/java/project/JavaProjectConstants.html#SOURCES_TYPE_JAVA"><code>JavaProjectConstants.SOURCES_TYPE_JAVA</code></a>
         * @return <code>this</code>
         */
        public SourceRootConfig type(String value) {
            type = value;
            return this;
        }

        /**
         * Configures optional hint for {@link SourceGroupModifier} allowing creation of this
         * source root on demand.
         * @param value A hint
         * @return <code>this</code>
         * @see #createSourceGroupModifierImplementation()
         */
        public SourceRootConfig hint(String value) {
            hint = value;
            return this;
        }

        /**
         * Configures a regular icon for the source root, optional.
         * @param value
         * @return <code>this</code>
         */
        public SourceRootConfig icon(Icon value) {
            icon = value;
            return this;
        }

        /**
         * Configures an opened variant icon for the source root, optional.
         * @param value
         * @return <code>this</code>
         */
        public SourceRootConfig openedIcon(Icon value) {
            openedIcon = value;
            return this;
        }
        
        /**
         * Declares that the source root resides in some (hierarchical) project part.
         * The project can be partitioned on multiple levels, each source root may represent some
         * part of the project. Partitioning can be used to identify "sibling" roots
         * @param parts abstract location of this root 
         * @return {@code this}
         * @since 1.68
         */
        public SourceRootConfig inParts(String... parts) {
            this.parts = parts;
            return this;
        }

        /**
         * Adds configured source root to <code>SourcesHelper</code>.
         * @throws IllegalStateException if this method is called after either
         *                               {@link #createSources} or {@link #registerExternalRoots}
         *                               was called
         * @see SourcesHelper#registerExternalRoots
         * @return <code>this</code>
         */
        public SourceRootConfig add() throws IllegalStateException {
            if (lastRegisteredRoots != null) {
                throw new IllegalStateException("registerExternalRoots was already called"); // NOI18N
            }
            if (type != null) {
                typedSourceRoots.add(new TypedSourceRoot(type, hint, location, includes, excludes, displayName, icon, openedIcon, parts));
            } else {
                principalSourceRoots.add(new SourceRoot(location, includes, excludes, hint, displayName, icon, openedIcon, parts));
            }
            return this;
        }
    }

    /**
     * Creates a possible source root configuration.
     * Source root is a top-level folder which may
     * contain sources that should be considered part of the project.
     * <p>
     * If the actual value of the <code>location</code> parameter is inside the project directory
     * and the root is not {@link SourceRootConfig#type(String) typed},
     * this is simply ignored; so it safe to configure source roots
     * for any source directory which might be set to use an external path, even
     * if the common location is internal.
     * </p>
     * <p>
     * Source location need not to exist physically, when {@link SourceRootConfig#hint(String) hint} is specified
     * and {@link SourceGroupModifier} created by this helper is added to project
     * lookup, source root can be created on demand.
     * </p>
     * <p>
     * NOTE: don't forget to call {@link SourceRootConfig#add() add()} method
     * on initialized <code>SourceRootConfig</code> to add it
     * to <code>SourcesHelper</code>. See {@link SourceRootConfig} for details
     * of usage and other parameters.
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @return source root configuration, that may be added to <code>SourcesHelper</code>
     */
    public SourceRootConfig sourceRoot(String location) {
        return new SourceRootConfig(location);
    }

    /**
     * Add a possible principal source root, or top-level folder which may
     * contain sources that should be considered part of the project.
     * <p>
     * See {@link #sourceRoot(String location)} and {@link SourceRootConfig} for details,
     * consider using them instead of this method for better readability.
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     * @see #registerExternalRoots
     * @see Sources#TYPE_GENERIC
     * @deprecated Use {@link #sourceRoot(String location)} and {@link SourceRootConfig} instead.
     */
    @Deprecated
    public void addPrincipalSourceRoot(String location, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        addPrincipalSourceRoot(location, null, null, displayName, icon, openedIcon);
    }

    /**
     * Add a possible principal source root, or top-level folder which may
     * contain sources that should be considered part of the project, with
     * optional include and exclude lists.
     * <p>
     * See {@link #sourceRoot(String location)} and {@link SourceRootConfig} for details,
     * consider using them instead of this method for better readability.
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param includes Ant-style includes; may contain Ant property substitutions;
     *                 if not null, only files and folders
     *                 matching the pattern (or patterns), and not specified in the excludes list,
     *                 will be {@link SourceGroup#contains included}
     * @param excludes Ant-style excludes; may contain Ant property substitutions;
     *                 if not null, files and folders
     *                 matching the pattern (or patterns) will not be {@link SourceGroup#contains included},
     *                 even if specified in the includes list
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     * @see #registerExternalRoots
     * @see Sources#TYPE_GENERIC
     * @since org.netbeans.modules.project.ant/1 1.15
     * @deprecated Use {@link #sourceRoot(String location)} and {@link SourceRootConfig} instead.
     */
    @Deprecated
    public void addPrincipalSourceRoot(String location, String includes, String excludes, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        SourceRootConfig cfg = sourceRoot(location).displayName(displayName).icon(icon).openedIcon(openedIcon);
        if (includes != null)
            cfg.includes(includes);
        if (excludes != null)
            cfg.excludes(excludes);
        cfg.add();
    }

    /**
     * Similar to {@link #addPrincipalSourceRoot} but affects only
     * {@link #registerExternalRoots} and not {@link #createSources}.
     * <p class="nonnormative">
     * Useful for project type providers which have external paths holding build
     * products. These should not appear in {@link Sources}, yet it may be useful
     * for {@link FileOwnerQuery} to know the owning project (for example, in order
     * for a project-specific <a href="@org-netbeans-api-java-classpath@/org/netbeans/spi/java/queries/SourceForBinaryQueryImplementation.html"><code>SourceForBinaryQueryImplementation</code></a> to work).
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a non-source tree; may contain Ant property substitutions
     * @throws IllegalStateException if this method is called after
     *                               {@link #registerExternalRoots} was called
     */
    public void addNonSourceRoot(String location) throws IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called"); // NOI18N
        }
        nonSourceRoots.add(new Root(location));
    }
    
    /**
     * Add any file that is supposed to be owned by a given project 
     * via FileOwnerQuery, affects only {@link #registerExternalRoots} 
     * and not {@link #createSources}.
     * <p class="nonnormative">
     * Useful for project type providers which have external paths holding build
     * products. These should not appear in {@link Sources}, yet it may be useful
     * for {@link FileOwnerQuery} to know the owning project (for example, in order
     * for a project-specific <a href="@org-netbeans-api-java-classpath@/org/netbeans/spi/java/queries/SourceForBinaryQueryImplementation.html"><code>SourceForBinaryQueryImplementation</code></a> to work).
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a file; may contain Ant property substitutions
     * @throws IllegalStateException if this method is called after
     *                               {@link #registerExternalRoots} was called
     * @since org.netbeans.modules.project.ant/1 1.17
     */
    public void addOwnedFile(String location) throws IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called"); // NOI18N
        }
        ownedFiles.add(new Root(location));
    }
    
    /**
     * Add a typed source root which will be considered only in certain contexts.
     * <p>
     * See {@link #sourceRoot(String location)} and {@link SourceRootConfig} for details,
     * consider using them instead of this method for better readability.
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param type a source root type such as <a href="@org-netbeans-modules-java-project@/org/netbeans/api/java/project/JavaProjectConstants.html#SOURCES_TYPE_JAVA"><code>JavaProjectConstants.SOURCES_TYPE_JAVA</code></a>
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     * @deprecated Use {@link #sourceRoot(String location)} and {@link SourceRootConfig} instead.
     */
    @Deprecated
    public void addTypedSourceRoot(String location, String type, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        addTypedSourceRoot(location, null, null, type, displayName, icon, openedIcon);
    }
    
    /**
     * Add a typed source root with optional include and exclude lists.
     * <p>
     * See {@link #sourceRoot(String location)} and {@link SourceRootConfig} for details,
     * consider using them instead of this method for better readability.
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param includes an optional list of Ant-style includes
     * @param excludes an optional list of Ant-style excludes
     * @param type a source root type such as <a href="@org-netbeans-modules-java-project@/org/netbeans/api/java/project/JavaProjectConstants.html#SOURCES_TYPE_JAVA"><code>JavaProjectConstants.SOURCES_TYPE_JAVA</code></a>
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     * @since org.netbeans.modules.project.ant/1 1.15
     * @deprecated Use {@link #sourceRoot(String location)} and {@link SourceRootConfig} instead.
     */
    @Deprecated
    public void addTypedSourceRoot(String location, String includes, String excludes, String type, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        SourceRootConfig cfg = sourceRoot(location).type(type).displayName(displayName).icon(icon).openedIcon(openedIcon);
        if (includes != null)
            cfg.includes(includes);
        if (excludes != null)
            cfg.excludes(excludes);
        cfg.add();
    }

    private Project getProject() {
        return project != null ? project : AntBasedProjectFactorySingleton.getProjectFor(aph);
    }
    
    /**
     * Register all external source or non-source roots using {@link FileOwnerQuery#markExternalOwner}.
     * <p>
     * Only roots added by {@link #addPrincipalSourceRoot} and {@link #addNonSourceRoot}
     * are considered. They are registered if (and only if) they in fact fall
     * outside of the project directory, and of course only if the folders really
     * exist on disk. Currently it is not defined when this file existence check
     * is done (e.g. when this method is first called, or periodically) or whether
     * folders which are created subsequently will be registered, so project type
     * providers are encouraged to create all desired external roots before calling
     * this method.
     * </p>
     * <p>
     * If the actual value of the location changes (due to changes being
     * fired from the property evaluator), roots which were previously internal
     * and are now external will be registered, and roots which were previously
     * external and are now internal will be unregistered. The (un-)registration
     * will be done using the same algorithm as was used initially.
     * </p>
     * <p>
     * If an explicit include list is configured for a principal source root, only those
     * subfolders which are included (or folders directly containing included files)
     * will be registered. Note that the source root, or an included subfolder, will
     * be registered even if it contains excluded files or folders beneath it.
     * </p>
     * <p>
     * Calling this method causes the helper object to hold strong references to the
     * current external roots, which helps a project satisfy the requirements of
     * {@link FileOwnerQuery#EXTERNAL_ALGORITHM_TRANSIENT}.
     * </p>
     * <p>
     * If you used the old constructor form
     * {@link #SourcesHelper(AntProjectHelper, PropertyEvaluator)}
     * then you may <em>not</em> call this method inside the project's constructor, as
     * it requires the actual project to exist and be registered in {@link ProjectManager};
     * in this case you could still use {@link org.openide.util.Mutex#postWriteRequest} to run it
     * later, if you were creating the helper in your constructor, since the project construction
     * normally occurs in read access.
     * Better to use {@link #SourcesHelper(Project, AntProjectHelper, PropertyEvaluator)}.
     * </p>
     * @param algorithm an external root registration algorithm as per
     *                  {@link FileOwnerQuery#markExternalOwner}
     * @throws IllegalArgumentException if the algorithm is unrecognized
     * @throws IllegalStateException if this method is called more than once on a
     *                               given <code>SourcesHelper</code> object
     */
    public void registerExternalRoots(int algorithm) throws IllegalArgumentException, IllegalStateException {
        registerExternalRoots(algorithm, true);
    }
    
    /**
     * Register all external source or non-source roots using {@link FileOwnerQuery#markExternalOwner}.
     * <p>
     * Only roots added by {@link #addPrincipalSourceRoot} and {@link #addNonSourceRoot}
     * are considered. They are registered if (and only if) they in fact fall
     * outside of the project directory, and of course only if the folders really
     * exist on disk. Currently it is not defined when this file existence check
     * is done (e.g. when this method is first called, or periodically) or whether
     * folders which are created subsequently will be registered, so project type
     * providers are encouraged to create all desired external roots before calling
     * this method.
     * </p>
     * <p>
     * If the actual value of the location changes (due to changes being
     * fired from the property evaluator), roots which were previously internal
     * and are now external will be registered, and roots which were previously
     * external and are now internal will be unregistered. The (un-)registration
     * will be done using the same algorithm as was used initially.
     * </p>
     * <p>
     * If a minimalSubfolders is true and an explicit include list is configured 
     * for a principal source root, only those subfolders which are included 
     * (or folders directly containing included files)
     * will be registered, otherwise the whole source root is registered.
     * Note that the source root, or an included subfolder, will
     * be registered even if it contains excluded files or folders beneath it.
     * </p>
     * <p>
     * Calling this method causes the helper object to hold strong references to the
     * current external roots, which helps a project satisfy the requirements of
     * {@link FileOwnerQuery#EXTERNAL_ALGORITHM_TRANSIENT}.
     * </p>
     * <p>
     * You may <em>not</em> call this method inside the project's constructor, as
     * it requires the actual project to exist and be registered in {@link ProjectManager}.
     * Typically you would use {@link org.openide.util.Mutex#postWriteRequest} to run it
     * later, if you were creating the helper in your constructor, since the project construction
     * normally occurs in read access.
     * </p>
     * @param algorithm an external root registration algorithm as per
     *                  {@link FileOwnerQuery#markExternalOwner}
     * @param minimalSubfolders controls how the roots having an explicit include list 
     * are registered. When true only those subfolders which are included 
     * (or folders directly containing included files) will be registered,
     * otherwise the whole source root is registered.
     * @throws IllegalArgumentException if the algorithm is unrecognized
     * @throws IllegalStateException if this method is called more than once on a
     *                               given <code>SourcesHelper</code> object
     * @since 1.26
     */
    public void registerExternalRoots (int algorithm, boolean minimalSubfolders) throws IllegalArgumentException, IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called before"); // NOI18N
        }
        registeredRootAlgorithm = algorithm;
        this.minimalSubfolders = minimalSubfolders;
        remarkExternalRoots();
    }
    
    private void remarkExternalRoots() throws IllegalArgumentException {
        List<Root> allRoots = new ArrayList<Root>(principalSourceRoots);
        allRoots.addAll(nonSourceRoots);
        allRoots.addAll(ownedFiles);
        Project p = getProject();
        FileObject pdir = aph.getProjectDirectory();
        // First time: register roots and add to lastRegisteredRoots.
        // Subsequent times: add to newRootsToRegister and maybe add them later.
        if (lastRegisteredRoots == null) {
            // First time.
            lastRegisteredRoots = Collections.emptySet();
            propChangeL = new PropChangeL(); // hold a strong ref
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(propChangeL, evaluator));
        }
        Set<FileObject> newRegisteredRoots = new HashSet<FileObject>();
        // XXX might be a bit more efficient to cache for each root the actualLocation value
        // that was last computed, and just check if that has changed... otherwise we wind
        // up calling APH.resolveFileObject repeatedly (for each property change)
        for (Root r : allRoots) {
            for (FileObject loc : r.getIncludeRoots(minimalSubfolders)) {
                if (FileUtil.getRelativePath(pdir, loc) != null) {
                    // Inside projdir already. Skip it.
                    continue;
                }
                if (loc.isFolder()) {
                    try {
                        Project other = ProjectManager.getDefault().findProject(loc);
                        if (other != null && !ProjectConvertors.isConvertorProject(other)) {
                            // This is a foreign project; we cannot own it. Skip it.
                            continue;
                        }
                    } catch (IOException e) {
                        // Assume it is a foreign project and skip it.
                        continue;
                    }
                }
                // It's OK to go.
                newRegisteredRoots.add(loc);
            }
        }
        // Just check for changes since the last time.
        Set<FileObject> toUnregister = new HashSet<FileObject>(lastRegisteredRoots);
        toUnregister.removeAll(newRegisteredRoots);
        for (FileObject loc : toUnregister) {
            FileOwnerQuery.markExternalOwner(loc, null, registeredRootAlgorithm);
        }
        Set<FileObject> toRegister = new HashSet<FileObject>(newRegisteredRoots);
        toRegister.removeAll(lastRegisteredRoots);
        for (FileObject loc : toRegister) {
            FileOwnerQuery.markExternalOwner(loc, p, registeredRootAlgorithm);
        }
        lastRegisteredRoots = newRegisteredRoots;
    }

    // #143633: notify Sources impls that new source group has been created
    private WeakSet<SourcesImpl> knownSources = new WeakSet<SourcesImpl>();

    /**
     * Create a source list object.
     * <p>
     * All principal source roots are listed as {@link Sources#TYPE_GENERIC} unless they
     * are inside the project directory. The project directory itself is also listed
     * (with a display name according to {@link ProjectUtils#getInformation}), unless
     * it is contained by an explicit principal source root (i.e. ancestor directory).
     * Principal source roots should never overlap; if two configured
     * principal source roots are determined to have the same root folder, the first
     * configured root takes precedence (which only matters in regard to the display
     * name); if one root folder is contained within another, the broader
     * root folder subsumes the narrower one so only the broader root is listed.
     * </p>
     * <p>
     * Other source groups are listed according to the named typed source roots.
     * There is no check performed that these do not overlap (though a project type
     * provider should for UI reasons avoid this situation).
     * </p>
     * <p>
     * Any source roots which do not exist on disk are ignored, as if they had
     * not been configured at all. Currently it is not defined when this existence
     * check is performed (e.g. when this method is called, when the source root
     * is first accessed, periodically, etc.), so project type providers are
     * generally encouraged to make sure all desired source folders exist
     * before calling this method, if creating a new project.
     * </p>
     * <p>
     * Source groups are created according to the semantics described in
     * {@link org.netbeans.spi.project.support.GenericSources#group}. They are listed in the order they
     * were configured (for those roots that are actually used as groups).
     * </p>
     * <p>
     * You may call this method inside the project's constructor, but
     * {@link Sources#getSourceGroups} may <em>not</em> be called within the
     * constructor, as it requires the actual project object to exist and be
     * registered in {@link ProjectManager}.
     * </p>
     * @return a source list object suitable for {@link Project#getLookup}
     */
    public Sources createSources() {
        SourcesImpl si = new SourcesImpl();
        knownSources.add(si);
        return si;
    }
    
    private final class SourcesImpl implements Sources, PropertyChangeListener, FileChangeListener {
        
        private final ChangeSupport cs = new ChangeSupport(this);
        private boolean haveAttachedListeners;
        private final Set<File> rootsListenedTo = new HashSet<File>();
        /**
         * The root URLs which were computed last, keyed by group type.
         */
        private final Map<String,List<URL>> lastComputedRoots = new HashMap<String,List<URL>>();
        
        public SourcesImpl() {
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        }
        
        public SourceGroup[] getSourceGroups(String type) {
            List<SourceGroup> groups = new ArrayList<SourceGroup>();
            if (type.equals(Sources.TYPE_GENERIC)) {
                List<SourceRoot> roots = new ArrayList<SourceRoot>(principalSourceRoots);
                // Always include the project directory itself as a default:
                roots.add(new SourceRoot("", null, null, null, ProjectUtils.getInformation(getProject()).getDisplayName(), null, null, null));
                Map<FileObject,SourceRoot> rootsByDir = new LinkedHashMap<FileObject,SourceRoot>();
                // First collect all non-redundant existing roots.
                for (SourceRoot r : roots) {
                    File locF = r.getActualLocation();
                    if (locF == null) {
                        continue;
                    }
                    listen(locF);
                    FileObject loc = FileUtil.toFileObject(locF);
                    if (loc == null) {
                        continue;
                    }
                    if (!loc.isFolder()) {
                        LOG.log(Level.WARNING, "Group root: {0} is not a folder.", FileUtil.getFileDisplayName(loc));
                        continue;
                    }
                    if (rootsByDir.containsKey(loc)) {
                        continue;
                    }
                    rootsByDir.put(loc, r);
                }
                // Remove subroots.
                Iterator<FileObject> it = rootsByDir.keySet().iterator();
                while (it.hasNext()) {
                    FileObject loc = it.next();
                    FileObject parent = loc.getParent();
                    while (parent != null) {
                        if (rootsByDir.containsKey(parent)) {
                            // This is a subroot of something, so skip it.
                            rootsByDir.get(loc).removed = true;
                            it.remove();
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                // Everything else is kosher.
                for (Map.Entry<FileObject,SourceRoot> entry : rootsByDir.entrySet()) {
                    groups.add(entry.getValue().toGroup(entry.getKey()));
                }
            } else {
                Set<FileObject> dirs = new HashSet<FileObject>();
                LOG.log(Level.FINE, "Calculating groups of type: {0} for: {1}", //NOI18N
                        new Object[]{
                            type,
                            FileUtil.getFileDisplayName(aph.getProjectDirectory())
                        });
                for (TypedSourceRoot r : typedSourceRoots) {
                    if (!r.getType().equals(type)) {
                        continue;
                    }
                    File locF = r.getActualLocation();
                    if (locF == null) {
                        LOG.log(Level.FINE, "Cannot resolve file for: {0}", r.location);    //NOI18N
                        continue;
                    }
                    listen(locF);
                    FileObject loc = FileUtil.toFileObject(locF);
                    if (loc == null) {
                        LOG.log(Level.FINE, "Cannot resolve FileObject for: {0}", locF);    //NOI18N
                        continue;
                    }
                    if (!loc.isFolder()) {
                        LOG.log(Level.WARNING, "Group root: {0} is not a folder.", FileUtil.getFileDisplayName(loc));
                        continue;
                    }
                    if (!dirs.add(loc)) {
                        // Already had one.
                        continue;
                    }
                    final SourceGroup sg = r.toGroup(loc);
                    groups.add(sg);
                    LOG.log(Level.FINE, "Added group: {0}", sg);    //NOI18N
                }
            }
            // Remember what we computed here so we know whether to fire changes later.
            List<URL> rootURLs = new ArrayList<URL>(groups.size());
            for (SourceGroup g : groups) {
                rootURLs.add(g.getRootFolder().toURL());
            }
            lastComputedRoots.put(type, rootURLs);
            return groups.toArray(new SourceGroup[0]);
        }
        
        private synchronized void listen(File rootLocation) {
            // #40845. Need to fire changes if a source root is added or removed.
            if (rootsListenedTo.add(rootLocation) && /* be lazy */ haveAttachedListeners) {
                FileUtil.addFileChangeListener(this, rootLocation);
            }
        }
        
        public synchronized void addChangeListener(ChangeListener listener) {
            if (!haveAttachedListeners) {
                haveAttachedListeners = true;
                for (File rootLocation : rootsListenedTo) {
                    FileUtil.addFileChangeListener(this, rootLocation);
                }
            }
            cs.addChangeListener(listener);
        }
        
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }
        
        private void maybeFireChange() {
            // #47451: check whether anything really changed.
            boolean change = false;
            // Cannot iterate over entrySet, as the map will be modified by getSourceGroups.
            for (String type : new HashSet<String>(lastComputedRoots.keySet())) {
                List<URL> previous = new ArrayList<URL>(lastComputedRoots.get(type));
                getSourceGroups(type);
                List<URL> nue = lastComputedRoots.get(type);
                if (!nue.equals(previous)) {
                    change = true;
                    break;
                }
            }
            if (change) {
                cs.fireChange();
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            // Root might have been created on disk.
            maybeFireChange();
        }

        public void fileDataCreated(FileEvent fe) {
            maybeFireChange();
        }

        public void fileDeleted(FileEvent fe) {
            // Root might have been deleted.
            maybeFireChange();
        }

        public void fileChanged(FileEvent fe) {
            // ignore; generally should not happen (listening to dirs)
        }

        public void fileRenamed(FileRenameEvent fe) {
            maybeFireChange();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            // #164930 - ignore
        }

        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            // Properties may have changed so as cause external roots to move etc.
            maybeFireChange();
        }
    }

    /**
     * Creates new {@link SourceGroupModifierImplementation} that can be put into project lookup.
     * <p>
     * Only source roots added with a {@link SourceRootConfig#hint(String) hint} can be created
     * with this <tt>SourceGroupModifierImplementation</tt>.
     * </p>
     * @return <tt>SourceGroupModifierImplementation</tt> implementation.
     * @see #sourceRoot(String)
     * @since org.netbeans.modules.project.ant/1 1.33
     */
    public SourceGroupModifierImplementation createSourceGroupModifierImplementation() {
        return new SourceGroupModifierImpl();
    }
    
    private static final class Key implements Function<SourceRoot, Integer> {
        private SourceRoot  root;
        private String[]    parts;
        
        public Key(SourceRoot root, String[] parts) {
            this.root = root;
            this.parts = parts;
        }
        
        public Integer apply(SourceRoot r) {
            int result = 0;
            int start = 0;
            if (this.root != null) {
                if (root.projectParts != null && r.projectParts != null) {
                    for (int i = 0; i < root.projectParts.length && i < r.projectParts.length; i++) {
                        if (root.projectParts[i].equals(r.projectParts[i])) {
                            start++;
                        } else {
                            break;
                        }
                    }
                }
            }
            if (parts != null && r.projectParts != null) {
                for (int i = start; i < parts.length && i < r.projectParts.length; i++) {
                    if (parts[i].equals(r.projectParts[i])) {
                        result++;
                    } else {
                        break;
                    }
                }
            }
            return result + start;
        }
    }
    
    private class SourceGroupModifierImpl implements SourceGroupModifierImplementation, SourceGroupRelativeModifierImplementation {
        private final Function<SourceRoot, Integer>  similarity;

        public SourceGroupModifierImpl() {
            this(null);
        }
        
        public SourceGroupModifierImpl(Function<SourceRoot, Integer> similarity) {
            this.similarity = similarity;
        }
        
        public SourceGroup createSourceGroup(String type, String hint) {
            SourceRoot root = findRoot(type, hint, similarity);
            if (root == null)
                return null;
            if (root.isRemoved())
                return null;    // getSourceGroups wouldn't return it, neither will we

            File loc = root.getActualLocation();
            FileObject foloc;
            if (! loc.exists()) {
                try {
                    foloc = FileUtil.createFolder(loc);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Failed to create folder " + loc, ex);
                    return null;
                }
            } else {
                foloc = FileUtil.toFileObject(loc);
            }
            SourceGroup sg = root.toGroup(foloc);
            assert sg != null;
            for (SourcesImpl sourcesImpl : knownSources) {
                sourcesImpl.maybeFireChange();
            }
            return sg;
        }

        public boolean canCreateSourceGroup(String type, String hint) {
            return findRoot(type, hint, similarity) != null;
        }
        
        private SourceRoot findRoot(String type, String hint, Function<SourceRoot, Integer> similarity) {
            int maxSimilarity = -1;
            SourceRoot candidate = null;
            
            if (Sources.TYPE_GENERIC.equals(type)) {
                for (SourceRoot root : principalSourceRoots) {
                    if (root.getHint() != null
                            && root.getHint().equals(hint)
                            && ! root.isRemoved()) {
                        if (similarity == null) {
                            return root;
                        } else {
                            int sim = similarity.apply(root);
                            if (sim > maxSimilarity) {
                                candidate = root;
                                maxSimilarity = sim;
                            }
                        }
                    }
                }
            } else {
                for (TypedSourceRoot root : typedSourceRoots) {
                    if (root.getHint() != null
                            && root.getType().equals(type)
                            && root.getHint().equals(hint)) {
                        if (similarity == null) {
                            return root;
                        } else {
                            int sim = similarity.apply(root);
                            if (sim > maxSimilarity) {
                                candidate = root;
                                maxSimilarity = sim;
                            }
                        }
                    }
                }
            }
            return candidate;
        }

        public SourceGroupModifierImplementation relativeTo(SourceGroup existingGroup, String... projectPart) {
            SourceRoot origin = null;
            if (existingGroup != null) {
                FileObject fo = existingGroup.getRootFolder();
                File f = FileUtil.toFile(fo);
                for (SourceRoot r : principalSourceRoots) {
                    File loc = r.getActualLocation();
                    if (loc != null && loc.equals(f)) {
                        origin = r;
                        break;
                    }
                }
            }
            return new SourceGroupModifierImpl(new Key(origin, projectPart));
        }
    }

    private final class PropChangeL implements PropertyChangeListener {
        
        public PropChangeL() {}
        
        public void propertyChange(PropertyChangeEvent evt) {
            // Some properties changed; external roots might have changed, so check them.
            for (SourceRoot r : principalSourceRoots) {
                r.resetIncludeExcludePatterns();
            }
            remarkExternalRoots();
        }
        
    }
    
}
