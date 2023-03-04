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

package org.netbeans.api.java.project.classpath;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.project.classpath.ProjectClassPathModifierAccessor;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.java.project.classpath.ProjectModulesModifier;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * An API for project's classpaths modification.
 * An client can use this interface to add/remove classpath element (folder, archive, library, subproject)
 * to/from the project's classpath. Not all operations on all project's classpath are supported, if the project
 * type does not support a modification of a given classpath the UnsupportedOperationException is thrown.
 * @since org.netbeans.modules.java.project/1 1.10
 */
public class ProjectClassPathModifier {
    private static final Logger LOG = Logger.getLogger(ProjectClassPathModifier.class.getName());
    
    private ProjectClassPathModifier() {}
    
    /**
     * Adds libraries into the project's classpath if the
     * libraries are not already included.
     * @param libraries to be added
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, @see ClassPath
     * @return true in case the classpath was changed (at least one library was added to the classpath),
     * the value false is returned when all the libraries are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a library to the classpath of the given type.
     */
    @SuppressWarnings("deprecation")    //NOI18N
    public static  boolean addLibraries (final Library[] libraries, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            boolean r1 = ProjectClassPathModifierAccessor.INSTANCE.addLibraries (libraries, extensible.pcmi, extensible.sg, extensible.classPathType);
            boolean r2 = !extensible.classPathType.equals(classPathType) && 
                    addRootsToModinfo(classPathType, projectArtifact, toURLs(libraries));
            return r1 || r2;
        } else {
            boolean result = false;
            for (Library library : libraries) {
                result |= extensible.pcpe.addLibrary(library);
            }
            return result;
        }
    }
    
    /**
     * Removes libraries from the project's classpath if the
     * libraries are included on it.
     * @param libraries to be removed
     * @param projectArtifact a file from whose classpath the libraries should be removed
     * @param classPathType the type of classpath, @see ClassPath
     * @return true in case the classpath was changed, (at least one library was removed from the classpath),
     * the value false is returned when none of the libraries was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a library from the classpath of the given type.
     */
    public static boolean removeLibraries (final Library[] libraries, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            boolean r1 = ProjectClassPathModifierAccessor.INSTANCE.removeLibraries (
                    extensible.uniqueLibraries(libraries), extensible.pcmi, extensible.sg, extensible.classPathType);
            boolean r2 = !extensible.classPathType.equals(classPathType) && 
                    removeRootsFromModinfo(classPathType, projectArtifact, toURLs(libraries));
            return r1 || r2;
        } else {
            throw new UnsupportedOperationException("Cannot remove libraries using " + extensible); // NOI18N
        }
    }
    
    /**
     * Adds archive files or folders into the project's classpath if the
     * entries are not already there.
     * @param classPathRoots roots to be added, each root has to be either a root of an archive or a folder url
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, @see ClassPath
     * @return true in case the classpath was changed, (at least one classpath root was added to the classpath),
     * the value false is returned when all the classpath roots are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a root to the classpath of the given type.
     */
    @SuppressWarnings("deprecation")        //NOI18N
    public static boolean addRoots (final URL[] classPathRoots, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        Parameters.notNull("classPathRoots", classPathRoots);
        final Extensible extensible = findExtensible(projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            boolean r1 = ProjectClassPathModifierAccessor.INSTANCE.addRoots (classPathRoots, extensible.pcmi, extensible.sg, extensible.classPathType);
            boolean r2 = !extensible.classPathType.equals(classPathType) && addRootsToModinfo(classPathType, projectArtifact, Arrays.asList(classPathRoots));
            return r1 || r2;
        } else {
            boolean result = false;
            for (URL urlToAdd : classPathRoots) {
                Parameters.notNull("classPathRoots", urlToAdd);
                if ("jar".equals(urlToAdd.getProtocol())) {
                    urlToAdd = FileUtil.getArchiveFile (urlToAdd);
                }
                final FileObject fo = URLMapper.findFileObject(urlToAdd);
                if (fo == null) {
                    throw new UnsupportedOperationException ("Adding of a non existent root is not supported by project.");  //NOI18N
                }
                result |= extensible.pcpe.addArchiveFile (fo);
            }
            return result;
        }
    }
    
    /**
     * Adds archive files or folders into the project's classpath if the
     * entries are not already there.
     * @param classPathRoots roots to be added, each root has to be either a root of an archive or a folder url; URI can be relative
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, @see ClassPath
     * @return true in case the classpath was changed, (at least one classpath root was added to the classpath),
     * the value false is returned when all the classpath roots are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a root to the classpath of the given type.
     * @since org.netbeans.modules.java.project/1 1.16
     */
    @SuppressWarnings("deprecation")
    public static boolean addRoots (final URI[] classPathRoots, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible(projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            boolean r1 = ProjectClassPathModifierAccessor.INSTANCE.addRoots (classPathRoots, extensible.pcmi, extensible.sg, extensible.classPathType);
            boolean r2 = !extensible.classPathType.equals(classPathType) && 
                            addRootsToModinfo(classPathType, projectArtifact, toURLs(Arrays.asList(classPathRoots)));
            return r1 || r2;
        } else {
            boolean result = false;
            final Project project = FileOwnerQuery.getOwner(projectArtifact);
            final File projectFolderFile = FileUtil.toFile(project.getProjectDirectory());
            for (URI uri : classPathRoots) {
                URI urlToAdd = LibrariesSupport.getArchiveFile(uri);
                if (urlToAdd == null) {
                    urlToAdd = uri;
                }
                final FileObject fo;
                if (urlToAdd.isAbsolute()) {
                    fo = FileUtil.toFileObject(BaseUtilities.toFile(urlToAdd));
                } else {
                    File f = PropertyUtils.resolveFile(projectFolderFile, LibrariesSupport.convertURIToFilePath(urlToAdd));
                    fo = FileUtil.toFileObject(f);
                }
                if (fo == null) {
                    throw new UnsupportedOperationException ("Adding of a non existent root is not supported by project.");  //NOI18N
                }
                result |= extensible.pcpe.addArchiveFile (fo);
            }
            return result;
        }
    }
    
    /**
     * Removes archive files or folders from the project's classpath if the
     * entries are included on it.
     * @param classPathRoots roots to be removed, each root has to be either a root of an archive or a folder
     * @param projectArtifact a file from whose classpath the roots should be removed
     * @param classPathType the type of classpath, @see ClassPath
     * @return true in case the classpath was changed, (at least one classpath root was removed from the classpath),
     * the value false is returned when none of the classpath roots was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a root from the classpath of the given type.
     */
    public static boolean removeRoots (final URL[] classPathRoots, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            boolean r1 = ProjectClassPathModifierAccessor.INSTANCE.removeRoots (extensible.uniqueLocations(classPathRoots), extensible.pcmi, extensible.sg, extensible.classPathType);
            boolean r2 = !extensible.classPathType.equals(classPathType) && 
                    removeRootsFromModinfo(classPathType, projectArtifact, Arrays.asList(classPathRoots));
            return r1 || r2;
        } else {
            throw new UnsupportedOperationException("Cannot remove roots from " + extensible); // NOI18N
        }
    }
    
    /**
     * Removes archive files or folders from the project's classpath if the
     * entries are included on it.
     * @param classPathRoots roots to be removed, each root has to be either a root of an archive or a folder; URI can be relative
     * @param projectArtifact a file from whose classpath the roots should be removed
     * @param classPathType the type of classpath, @see ClassPath
     * @return true in case the classpath was changed, (at least one classpath root was removed from the classpath),
     * the value false is returned when none of the classpath roots was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a root from the classpath of the given type.
     * @since org.netbeans.modules.java.project/1 1.16
     */
    public static boolean removeRoots (final URI[] classPathRoots, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.removeRoots (classPathRoots, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            throw new UnsupportedOperationException("Cannot remove roots from " + extensible); // NOI18N
        }
    }
    
    /**
     * Adds artifacts (e.g. subprojects) into project's classpath if the
     * artifacts are not already on it.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts. 
     * (must be owned by the artifact and be relative to it)
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, @see ClassPath
     * @return true in case the classpath was changed, (at least one artifact was added to the classpath),
     * the value false is returned when all the artifacts are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of an artifact to the classpath of the given type.
     */
    @SuppressWarnings("deprecation")        //NOI18N
    public static boolean addAntArtifacts (final AntArtifact[] artifacts, final URI[] artifactElements,
            final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        assert artifacts.length == artifactElements.length;
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            boolean r1 = ProjectClassPathModifierAccessor.INSTANCE.addAntArtifacts (artifacts, artifactElements, extensible.pcmi, extensible.sg, extensible.classPathType);
            boolean r2 = !extensible.classPathType.equals(classPathType) && 
                            addRootsToModinfo(classPathType, projectArtifact, toURLs(Arrays.asList(artifactElements)));
            return r1 || r2;
        } else {
            boolean result = false;
            for (int i=0; i< artifacts.length; i++) {
                result |= extensible.pcpe.addAntArtifact (artifacts[i], artifactElements[i]);
            }
            return result;
        }
    }

    /**
     * Adds projects into project's classpath if the
     * artifacts are not already on it.
     * <p>
     *  It's not guaranteed that the source and target project will connect in cases when each is of different class of project. Eg.
     * Ant-based vs Maven project types. A way to check is to attempt to retrieve AntArtifact from the source and target projects..
     *
     * @param projects to be added
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, see {@link org.netbeans.api.java.classpath.ClassPath}
     * @return true in case the classpath was changed, (at least one artifact was added to the classpath),
     * the value false is returned when all the artifacts are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of an artifact to the classpath of the given type.
     * @since org.netbeans.modules.java.project/1 1.24
     */
    public static boolean addProjects (final Project[] projects,
            final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            boolean r1 = ProjectClassPathModifierAccessor.INSTANCE.addProjects (projects, extensible.pcmi, extensible.sg, extensible.classPathType);
            boolean r2 = !extensible.classPathType.equals(classPathType) && 
                            addRootsToModinfo(classPathType, projectArtifact, toURLs(projects));
            return r1 || r2;
        } else {
            throw new UnsupportedOperationException("Cannot add project as dependency. Missing ProjectClassPathModifierImplementation service in project type.");
        }
    }

    /**
     * Removes artifacts (e.g. subprojects) from project's classpath if the
     * artifacts are included on it.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param projectArtifact a file from whose classpath the dependent projects should be removed
     * @param classPathType the type of classpath, {@link  org.netbeans.api.java.classpath.ClassPath}
     * @return true in case the classpath was changed, (at least one artifact was removed from the classpath),
     * the value false is returned when none of the artifacts was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of an artifact from the classpath of the given type.
     */
    public static boolean removeAntArtifacts (final AntArtifact[] artifacts, final URI[] artifactElements,
            final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            boolean r1 = ProjectClassPathModifierAccessor.INSTANCE.removeAntArtifacts (artifacts, artifactElements, extensible.pcmi, extensible.sg, extensible.classPathType);
            boolean r2 = !extensible.classPathType.equals(classPathType) && 
                            removeRootsFromModinfo(classPathType, projectArtifact, toURLs(Arrays.asList(artifactElements)));
            return r1 || r2;
        } else {
            throw new UnsupportedOperationException("Cannot remove artifacts from " + extensible); // NOI18N
        }
    }

    /**
     * Returns {@link ProjectClassPathModifier#Extensible} for given project artifact and classpath type. 
     * An Extensible implies a classpath to be extended. Different project type may provide different types
     * of Extensible.
     * @param projectArtifact a file owned by SourceGroup whose classpath should be changed
     * @param classPathType a classpath type, @see ClassPath
     * @return an Extensible. In case when the project supports the {@link ProjectClassPathModifierImplementation},
     * this interface is used to find an Extensible. If this interface is not provided, but project provides
     * the deprecated {@link org.netbeans.spi.java.project.classpath.ProjectClassPathExtender} interface and classpath type is {@link ClassPath@COMPILE} the
     * single Extensible, without assigned SourceGroup, is returned.
     * @throws UnsupportedOperationException In case when neither {@link ProjectClassPathModifierImplementation} nor
     *                                       {@link org.netbeans.spi.java.project.classpath.ProjectClassPathExtender}
     * is supported, or no project can be associated with the project artifact.
     */
    @SuppressWarnings("deprecation")        //NOI18N
    private static Extensible findExtensible(final FileObject projectArtifact, final String classPathType) throws UnsupportedOperationException {
        assert projectArtifact != null;
        assert classPathType != null;
        final Project project = FileOwnerQuery.getOwner(projectArtifact);
        if (project == null) {
            throw new UnsupportedOperationException("No project found to correspond to " + FileUtil.getFileDisplayName(projectArtifact)); // NOI18N
        }
        final ProjectClassPathModifierImplementation pm = project.getLookup().lookup(ProjectClassPathModifierImplementation.class);
        final ProjectModulesModifier pmm = Lookup.getDefault().lookup(ProjectModulesModifier.class);
        
        String substModulePath = pmm == null ? null : pmm.provideModularClasspath(projectArtifact, classPathType);
        final String _classPathType = substModulePath == null ? classPathType : substModulePath;
        if (pm != null) {            
            final SourceGroup[] sgs = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleSourceGroups(pm);
            assert sgs != null   : "Class: " + pm.getClass() + " returned null as source groups.";    //NOI18N
            for (SourceGroup sg : sgs) {
                if ((projectArtifact == sg.getRootFolder() || FileUtil.isParentOf(sg.getRootFolder(),projectArtifact)) && sg.contains(projectArtifact)) {
                    final String[] types = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleClassPathTypes(pm,sg);
                    assert types != null : "Class: " + pm.getClass() + " returned null as classpath types.";    //NOI18N
                    boolean originalFound = false;
                    for (String type : types) {
                        if (_classPathType.equals(type)) {
                            String label = "ProjectClassPathModifierImplementation for " + _classPathType + " on " + FileUtil.getFileDisplayName(sg.getRootFolder()); // NOI18N
                            return new Extensible(project, pm, sg, type,label);
                        } else if (classPathType.equals(type)) {
                            originalFound = true;
                        }
                    }
                    if (originalFound) {
                        String label = "ProjectClassPathModifierImplementation for " + classPathType + " on " + FileUtil.getFileDisplayName(sg.getRootFolder()); // NOI18N
                        return new Extensible(project, pm, sg, classPathType,label);
                    }
                }
            }
            throw new UnsupportedOperationException("Project in " + FileUtil.getFileDisplayName(project.getProjectDirectory()) + " of " + project.getClass() +
                    " has a ProjectClassPathModifierImplementation but it will not handle " + _classPathType + " for " + FileUtil.getFileDisplayName(projectArtifact) +
                    " extensible source groups: " + sourceGroupsToString(sgs)); // NOI18N
        } else {
            final org.netbeans.spi.java.project.classpath.ProjectClassPathExtender pe =
                    project.getLookup().lookup(org.netbeans.spi.java.project.classpath.ProjectClassPathExtender.class);
            if (pe != null) {
                if (_classPathType.equals(ClassPath.COMPILE)) {
                    return new Extensible(project, pe, "ProjectClassPathExtender for " + FileUtil.getFileDisplayName(project.getProjectDirectory())); // NOI18N
                } else {
                    throw new UnsupportedOperationException("Project in " + FileUtil.getFileDisplayName(project.getProjectDirectory()) + " of " + project.getClass() +
                            " has a ProjectClassPathExtender in its lookup but no ProjectClassPathModifierImplementation to handle " + _classPathType); // NOI18N
                }
            } else {
                throw new UnsupportedOperationException("Project in " + FileUtil.getFileDisplayName(project.getProjectDirectory()) + " of " + project.getClass() +
                        " has neither a ProjectClassPathModifierImplementation nor a ProjectClassPathExtender in its lookup"); // NOI18N
            }
        }
    }

    private static String sourceGroupsToString(final SourceGroup[] sgs) {
        final StringBuilder sb = new StringBuilder();
        for(SourceGroup sg : sgs) {
            if (sb.length()!=0) {
                sb.append(':'); //NOI18N
            }
            sb.append(FileUtil.getFileDisplayName(sg.getRootFolder()));
        }
        return sb.toString();
    }

    /**
     * Extensible represents a classpath which may be changed by the
     * {@link ProjectClassPathModifier}. It encapsulates the compilation
     * unit and class path type, @see ClassPath.
     */
    private static final class Extensible {
        private final Project project;
        private final String classPathType;       
        private final SourceGroup sg;
        private final ProjectClassPathModifierImplementation pcmi;
        @SuppressWarnings("deprecation")        //NOI18N
        private final org.netbeans.spi.java.project.classpath.ProjectClassPathExtender pcpe;
        /** for error messages only */
        private final String label;
        
        private Extensible(final Project project, final ProjectClassPathModifierImplementation pcmi, final SourceGroup sg, final String classPathType, String label) {
            assert pcmi != null;
            assert sg != null;
            assert classPathType != null;
            this.project = project;
            this.pcmi = pcmi;
            this.sg = sg;
            this.classPathType = classPathType;
            this.pcpe = null;
            this.label = label;
        }
        
        @SuppressWarnings("deprecation")        //NOI18N
        private Extensible(final Project project, final org.netbeans.spi.java.project.classpath.ProjectClassPathExtender pcpe, String label) {
            assert pcpe != null;
            this.project = project;
            this.pcpe = pcpe;
            this.pcmi = null;
            this.sg = null;
            this.classPathType = ClassPath.COMPILE;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
        
        URL[] uniqueLocations(URL[] locations) {
            ProjectModulesModifier pmm = Lookup.getDefault().lookup(ProjectModulesModifier.class);
            if (pmm == null) {
                return locations;
            }
            Map<URL, Collection<ClassPath>> usages = pmm.findModuleUsages(sg.getRootFolder(), Arrays.asList(locations));
            if (usages.isEmpty()) {
                return locations;
            }
            ClassPath myPath = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE);
            return usages.keySet().stream()
                    .filter((u) -> usages.get(u).size() == 1 &&
                            Arrays.equals(usages.get(u).iterator().next().getRoots(), myPath.getRoots()))
                    .toArray((l) -> new URL[l]);
        }
        
        Library[] uniqueLibraries(Library[] libraries) {
            Map<URL, Library> libraryMap = new HashMap<>();
            URL[] locations = Arrays.stream(libraries)
                    .flatMap((l) -> l.getContent("classpath").stream().map((x) -> { // NOI18N
                        libraryMap.put(x, l);
                        return x;
                     })
                )
                .toArray((s) -> new URL[s]);
            return Arrays.stream(uniqueLocations(locations)).map((u) -> libraryMap.get(u))
                    .distinct()
                    .toArray((s) -> new Library[s]);
        }
    }

    /**
     * Translates the new SPI into an instance of the old SPI.
     * Useful to place in project lookup so that you need not bother implementing the old SPI.
     * (Since there was not previously a matching API, old clients may directly look for the old SPI
     * interface in a project's lookup.)
     * Corresponding methods are called using the first reported source group (if any) and extensible classpath type (if any).
     * @param pcpmi the new SPI
     * @return a proxy fitting the old SPI
     * @see #extenderForModifier(Project)
     * @since 1.41
     */
    @SuppressWarnings("deprecation") // XXX seems ineffective against return type
    public static org.netbeans.spi.java.project.classpath.ProjectClassPathExtender extenderForModifier(final ProjectClassPathModifierImplementation pcpmi) {
        return new org.netbeans.spi.java.project.classpath.ProjectClassPathExtender() {
            @Override public boolean addLibrary(Library library) throws IOException {
                SourceGroup[] sgs = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleSourceGroups(pcpmi);
                if (sgs.length == 0) {
                    return false;
                }
                String[] types = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleClassPathTypes(pcpmi, sgs[0]);
                if (types.length == 0) {
                    return false;
                }
                try {
                    return ProjectClassPathModifierAccessor.INSTANCE.addLibraries(new Library[] {library}, pcpmi, sgs[0], types[0]);
                } catch (UnsupportedOperationException x) {
                    return false;
                }
            }
            @Override public boolean addArchiveFile(FileObject archiveFile) throws IOException {
                SourceGroup[] sgs = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleSourceGroups(pcpmi);
                if (sgs.length == 0) {
                    return false;
                }
                String[] types = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleClassPathTypes(pcpmi, sgs[0]);
                if (types.length == 0) {
                    return false;
                }
                URL r = archiveFile.getURL();
                if (FileUtil.isArchiveFile(r)) { // ought to always be true, but Javadoc is vague
                    r = FileUtil.getArchiveRoot(r);
                }
                try {
                    return ProjectClassPathModifierAccessor.INSTANCE.addRoots(new URL[] {r}, pcpmi, sgs[0], types[0]);
                } catch (UnsupportedOperationException x) {
                    return false;
                }
            }
            @Override public boolean addAntArtifact(AntArtifact artifact, URI artifactElement) throws IOException {
                SourceGroup[] sgs = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleSourceGroups(pcpmi);
                if (sgs.length == 0) {
                    return false;
                }
                String[] types = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleClassPathTypes(pcpmi, sgs[0]);
                if (types.length == 0) {
                    return false;
                }
                try {
                    return ProjectClassPathModifierAccessor.INSTANCE.addAntArtifacts(new AntArtifact[] {artifact}, new URI[] {artifactElement}, pcpmi, sgs[0], types[0]);
                } catch (UnsupportedOperationException x) {
                    return false;
                }
            }

        };
    }

    /**
     * Similar to {@link #extenderForModifier(ProjectClassPathModifierImplementation)} but permits the new SPI to be created lazily.
     * This is useful if the project is using {@link LookupMergerSupport#createClassPathModifierMerger} and it is thus impossible to get the final SPI instance
     * during construction of the project's lookup.
     * The new SPI is located at runtime on each call; if not present, false is returned from all methods.
     * @param p a project whose lookup may contain a {@link ProjectClassPathModifierImplementation}
     * @return an SPI equivalent
     * @since 1.41
     */
    @SuppressWarnings("deprecation")
    public static org.netbeans.spi.java.project.classpath.ProjectClassPathExtender extenderForModifier(final Project p) {
        return new org.netbeans.spi.java.project.classpath.ProjectClassPathExtender() {
            @Override public boolean addLibrary(Library library) throws IOException {
                ProjectClassPathModifierImplementation pcpmi = p.getLookup().lookup(ProjectClassPathModifierImplementation.class);
                return pcpmi != null ? extenderForModifier(pcpmi).addLibrary(library) : false;
            }
            @Override public boolean addArchiveFile(FileObject archiveFile) throws IOException {
                ProjectClassPathModifierImplementation pcpmi = p.getLookup().lookup(ProjectClassPathModifierImplementation.class);
                return pcpmi != null ? extenderForModifier(pcpmi).addArchiveFile(archiveFile) : false;
            }
            @Override public boolean addAntArtifact(AntArtifact artifact, URI artifactElement) throws IOException {
                ProjectClassPathModifierImplementation pcpmi = p.getLookup().lookup(ProjectClassPathModifierImplementation.class);
                return pcpmi != null ? extenderForModifier(pcpmi).addAntArtifact(artifact, artifactElement) : false;
            }
        };
    }
    
    private static Collection<URL> toURLs(Collection<URI> libs)  {
        URL[] urls = new URL[libs.size()];
        int index = 0;
        for (URI u : libs) {
            try {
                urls[index++] = u.toURL();
            } catch (MalformedURLException ex) {
                return Collections.emptyList();
            }
        }
        return Arrays.asList(urls);
    }
    
    private static Collection<URL> toURLs(Project[] prjs) {
        return
            toURLs(
            Arrays.asList(prjs).stream().flatMap(
                (prj) -> Arrays.asList(
                        AntArtifactQuery.findArtifactsByType(prj, JavaProjectConstants.ARTIFACT_TYPE_JAR)
                ).stream()).
                flatMap((a) -> Arrays.asList(a.getArtifactLocations()).stream()).
                collect(Collectors.toList())
            );
    }
    
    private static Collection<URL> toURLs(Library[] libraries) {
        return Arrays.stream(libraries).flatMap((l) -> l.getContent("classpath").stream()).
                collect(Collectors.toList());
    }
    
    private static boolean removeRootsFromModinfo(String originalPathType, FileObject artifact, Collection<URL> libs) {
        ProjectModulesModifier pmm = Lookup.getDefault().lookup(ProjectModulesModifier.class);
        if (pmm == null) {
            return false;
        } else {
            try {
                return pmm.removeRequiredModules(originalPathType, artifact, libs);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Could not remove module requires", ex);
                return false;
            }
        }
    }
    
    private static boolean addRootsToModinfo(String originalPathType, FileObject artifact, Collection<URL> libs) {
        ProjectModulesModifier pmm = Lookup.getDefault().lookup(ProjectModulesModifier.class);
        if (pmm == null) {
            return false;
        } else {
            try {
                return pmm.addRequiredModules(originalPathType, artifact, libs);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Could not declare module requires", ex);
                return false;
            }
        }
    }
}
