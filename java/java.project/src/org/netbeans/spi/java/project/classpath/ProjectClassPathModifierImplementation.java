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

package org.netbeans.spi.java.project.classpath;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.java.project.classpath.ProjectClassPathModifierAccessor;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * An SPI for project's classpaths modification.
 * A project can provide subclass of this class in its {@link Project#getLookup lookup} to
 * allow clients to add or remove new classpath elements (JAR, folder, dependent project, or library) to its 
 * classpaths.
 * @since org.netbeans.modules.java.project/1 1.10
 */
public abstract class ProjectClassPathModifierImplementation {
    
    static {
        ProjectClassPathModifierAccessor.INSTANCE = new Accessor ();
    }    
    
    protected ProjectClassPathModifierImplementation () {
    }
    
    
    /**
     * Returns the {@link SourceGroup}s providing classpath(s)
     * which may be modified.
     * @return (possibly empty) array of {@link SourceGroup}, never returns null
     */
    protected abstract SourceGroup [] getExtensibleSourceGroups ();
    
    
    /**
     * Returns the types of classpaths for given {@link SourceGroup} which may be modified.
     * @param sourceGroup for which the classpath types should be returned
     * @return (possibly empty) array of classpath types, never returns null
     */
    protected abstract String[] getExtensibleClassPathTypes (SourceGroup sourceGroup);
            
    /**
     * Adds libraries into the project's classpath if the
     * libraries are not already included.
     * @param libraries to be added
     * @param sourceGroup of type {@link JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the library should be added to,
     * e.g. {@link ClassPath#COMPILE}
     * @return true in case the classpath was changed (at least one library was added to the classpath),
     * the value false is returned when all the libraries are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a library to the classpath of the given type.
     */
    protected abstract boolean addLibraries (Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    
    /**
     * Removes libraries from the project's classpath if the
     * libraries are included on it.
     * @param libraries to be removed
     * @param sourceGroup of type {@link JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the library should be removed from,
     * e.g. {@link ClassPath#COMPILE}
     * @return true in case the classpath was changed, (at least one library was removed from the classpath),
     * the value false is returned when none of the libraries was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a library from the classpath of the given type.
     */
    protected abstract boolean removeLibraries (Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    /**
     * Adds archive files or folders into the project's classpath if the
     * entries are not already there.
     * @param classPathRoots roots to be added, each root has to be either a root of an archive or a folder
     * @param sourceGroup of type {@link JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the root should be added to,
     * e.g. {@link ClassPath#COMPILE}
     * @return true in case the classpath was changed, (at least one classpath root was added to the classpath),
     * the value false is returned when all the classpath roots are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a root to the classpath of the given type.
     */
    protected abstract boolean addRoots (URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    /**
     * Adds archive files or folders into the project's classpath if the
     * entries are not already there.
     * <p>This method is not abstract only for backward compatibility and therefore should be always
     * overrriden. Default implementation converts given URIs to URLs and calls
     * {@link #addRoots(URL[], SourceGroup, String)}. It throws UnsupportedOperationException
     * if URIs are not absolute.
     * @param classPathRoots roots to be added, each root has to be either a root of an archive or a folder; URI can be relative
     * @param sourceGroup of type {@link JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the root should be added to,
     * e.g. {@link ClassPath#COMPILE}
     * @return true in case the classpath was changed, (at least one classpath root was added to the classpath),
     * the value false is returned when all the classpath roots are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a root to the classpath of the given type.
     * @since org.netbeans.modules.java.project/1 1.16
     */
    protected boolean addRoots (URI[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        // for backward compatiblity try to convert URI to URL:
        return addRoots(convertURIsToURLs(classPathRoots), sourceGroup, type);
    }
    
    private static URL[] convertURIsToURLs(URI[] uris) {
        List<URL> content = new ArrayList<URL>();
        for (URI uri : uris) {
            if (!uri.isAbsolute()) {
                throw new UnsupportedOperationException("default modifier handles only absolute URIs - "+uri); // NOI18N            }
            }
            try {
                content.add(uri.toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return content.toArray(new URL[0]);
    }

    /**
     * Converts array of URLs to array of URIs.
     * 
     * @param entry list of URLs to convert
     * @return URIs
     * @since org.netbeans.modules.java.project/1 1.16
     */
    protected static URI[] convertURLsToURIs(URL[] entry) {
        List<URI> content = new ArrayList<URI>();
        for (URL url : entry) {
            content.add(URI.create(url.toExternalForm()));
        }
        return content.toArray(new URI[0]);
    }

    /**
     * Removes archive files or folders from the project's classpath if the
     * entries are included on it.
     * @param classPathRoots roots to be removed, each root has to be either a root of an archive or a folder
     * @param sourceGroup of type {@link JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the root should be removed from,
     * e.g. {@link ClassPath#COMPILE}
     * @return true in case the classpath was changed, (at least one classpath root was removed from the classpath),
     * the value false is returned when none of the classpath roots was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a root from the classpath of the given type.
     */
    protected abstract boolean removeRoots (URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    /**
     * Removes archive files or folders from the project's classpath if the
     * entries are included on it.
     * <p>This method is not abstract only for backward compatibility and therefore should be always
     * overrriden. Default implementation converts given URIs to URLs and calls
     * {@link #removeRoots(URL[], SourceGroup, String)}. It throws UnsupportedOperationException
     * if URIs are not absolute.
     * @param classPathRoots roots to be removed, each root has to be either a root of an archive or a folder; URI can be relative
     * @param sourceGroup of type {@link JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the root should be removed from,
     * e.g. {@link ClassPath#COMPILE}
     * @return true in case the classpath was changed, (at least one classpath root was removed from the classpath),
     * the value false is returned when none of the classpath roots was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a root from the classpath of the given type.
     */
    protected boolean removeRoots (URI[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        // for backward compatiblity try to convert URI to URL:
        return removeRoots(convertURIsToURLs(classPathRoots), sourceGroup, type);
    }
    /**
     * Adds artifacts (e.g. subprojects) into project's classpath if the
     * artifacts are not already on it.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts. 
     * (must be owned by the artifact and be relative to it)
     * @param sourceGroup of type {@link JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the artifact should be added to,
     * e.g. {@link ClassPath#COMPILE}
     * @return true in case the classpath was changed, (at least one artifact was added to the classpath),
     * the value false is returned when all the artifacts are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of an artifact to the classpath of the given type.
     */
    protected abstract boolean addAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    /**
     * Removes artifacts (e.g. subprojects) from project's classpath if the
     * artifacts are included on it.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param sourceGroup of type {@link JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the artifact should be removed from,
     * e.g. {@link ClassPath#COMPILE}
     * @return true in case the classpath was changed, (at least one artifact was removed from the classpath),
     * the value false is returned when none of the artifacts was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of an artifact from the classpath of the given type.
     */
    protected abstract boolean removeAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;


    /**
     * Adds projects as dependencies into project's classpath if the
     * artifacts are not already on it. The default behaviour will behave as {@link ProjectClassPathModifierImplementation#addAntArtifacts(org.netbeans.api.project.ant.AntArtifact[], java.net.URI[], org.netbeans.api.project.SourceGroup, java.lang.String)}
     * Other project types can override the behaviour.
     * @param projects to be added
     * (must be owned by the artifact and be relative to it)
     * @param sourceGroup of type {@link JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the artifact should be added to,
     * e.g. {@link ClassPath#COMPILE}
     * @return true in case the classpath was changed, (at least one artifact was added to the classpath),
     * the value false is returned when all the artifacts are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of an artifact to the classpath of the given type.
     * @since org.netbeans.modules.java.project/1 1.24
     */
    protected boolean addProjects(Project[] projects, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        List<AntArtifact> ants = new ArrayList<AntArtifact>();
        List<URI> antUris = new ArrayList<URI>();
        for (Project prj : projects) {
            AntArtifact[] antArtifacts = AntArtifactQuery.findArtifactsByType(prj, JavaProjectConstants.ARTIFACT_TYPE_JAR);
            for (AntArtifact aa : antArtifacts) {
                ants.add(aa);
                antUris.add(aa.getArtifactLocations()[0]);
            }
        }
        return addAntArtifacts(ants.toArray(new AntArtifact[0]), antUris.toArray(new URI[0]), sourceGroup, type);
    }

    /**
     * Takes a classpath root and tries to figure the best way to reference that file for that particular project.
     * The possible actions include relativization of path, copying to sharable libraries folder etc.
     * @param classpathRoot passed in through the <code>addRoots()</code> and <code>removeRoots()</code> methods
     * @param helper
     * @return a relative or absolute path to the original jar/folder or to a copy of it.
     * @throws java.net.URISyntaxException
     * @since org.netbeans.modules.java.project/1 1.15
     */
    protected final String performSharabilityHeuristics(URI classpathRoot, AntProjectHelper helper) throws URISyntaxException, IOException {
        assert classpathRoot != null;
        assert classpathRoot.toString().endsWith("/") : "Folder URI must end with '/'. Was: "+ classpathRoot;    //NOI18N
        URI toAdd = LibrariesSupport.getArchiveFile(classpathRoot);
        if (toAdd == null) {
            toAdd = classpathRoot;
        }
        File prjRoot = FileUtil.toFile(helper.getProjectDirectory());
        final File file = PropertyUtils.resolveFile(prjRoot, LibrariesSupport.convertURIToFilePath(toAdd));
        String f;
        if (CollocationQuery.areCollocated(file, prjRoot)) {
            //colocated get always relative path
            f = PropertyUtils.relativizeFile(prjRoot, file);
        } else {
            if (helper.isSharableProject()) {
                //library location is not collocated..
                //-> use absolute path
                // sort of heuristics to have the
                File library = PropertyUtils.resolveFile(prjRoot, helper.getLibrariesLocation());
                boolean fileLibraryCol = CollocationQuery.areCollocated(library.getParentFile(), file);
                boolean libraryAbsolute = LibrariesSupport.convertFilePathToURI(helper.getLibrariesLocation()).isAbsolute();
                // when library location is absolute, we are most probably dealing with the famous X: drive location
                // since the library is absolute, it shoudl be safe to reference everything under it as absolute as well.
                if (libraryAbsolute && fileLibraryCol) {
                    f = file.getAbsolutePath();
                } else if (libraryAbsolute && !fileLibraryCol) {
                    File fl = copyFile(file, FileUtil.toFileObject(library.getParentFile()));
                    f = fl.getAbsolutePath();
                } else if (!libraryAbsolute && fileLibraryCol) {
                    f = PropertyUtils.relativizeFile(prjRoot, file);
                } else { // if (!libraryAbsolute && !fileLibraryCol)
                    File fl = copyFile(file, FileUtil.toFileObject(library.getParentFile()));
                    f = PropertyUtils.relativizeFile(prjRoot, fl);
                }
            } else {
                //nonsharable project are ok with absolute path
                f = file.getAbsolutePath();
            }
        }
        return f;
    }

    
    private File copyFile(File file, FileObject newRoot) throws IOException {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo.isFolder()) {
            return FileUtil.toFile(copyFolderRecursively(fo, newRoot));
        } else {
            FileObject foExists = newRoot.getFileObject(fo.getName(), fo.getExt());
            if (foExists != null) {
                foExists.delete();
            }
            return FileUtil.toFile(FileUtil.copyFile(fo, newRoot, fo.getName(), fo.getExt()));
        }
    }
    
    
    //copied from FileChooserAccessory
    private FileObject copyFolderRecursively(FileObject sourceFolder, FileObject destination) throws IOException {
        assert sourceFolder.isFolder() : sourceFolder;
        assert destination.isFolder() : destination;
        FileObject destinationSubFolder = destination.getFileObject(sourceFolder.getName());
        if (destinationSubFolder == null) {
            destinationSubFolder = destination.createFolder(sourceFolder.getName());
        }
        for (FileObject fo : sourceFolder.getChildren()) {
            if (fo.isFolder()) {
                copyFolderRecursively(fo, destinationSubFolder);
            } else {
                FileObject foExists = destinationSubFolder.getFileObject(fo.getName(), fo.getExt());
                if (foExists != null) {
                    foExists.delete();
                }
                FileUtil.copyFile(fo, destinationSubFolder, fo.getName(), fo.getExt());
            }
        }
        return destinationSubFolder;
    }
    
    private static class Accessor extends ProjectClassPathModifierAccessor {        
        
        public SourceGroup[] getExtensibleSourceGroups(final ProjectClassPathModifierImplementation m) {
            assert m != null;
            return m.getExtensibleSourceGroups();
        }
        
        public String[] getExtensibleClassPathTypes (final ProjectClassPathModifierImplementation m, SourceGroup sg) {
            assert m != null;
            assert sg != null;
            return m.getExtensibleClassPathTypes(sg);
        }
        
        public boolean removeLibraries(Library[] libraries, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.removeLibraries(libraries, sourceGroup, type);
        }

        public boolean removeAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.removeAntArtifacts(artifacts, artifactElements, sourceGroup, type);
        }

        public boolean addLibraries (Library[] libraries, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.addLibraries(libraries, sourceGroup, type);
        }

        public boolean addAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.addAntArtifacts (artifacts, artifactElements, sourceGroup, type);
        }

        public boolean removeRoots (URL[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.removeRoots(classPathRoots, sourceGroup, type);
        }

        public boolean removeRoots (URI[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.removeRoots(classPathRoots, sourceGroup, type);
        }

        public boolean addRoots (URL[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.addRoots (classPathRoots, sourceGroup, type);
        }       
                        
        public boolean addRoots (URI[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.addRoots (classPathRoots, sourceGroup, type);
        }

        public boolean addProjects(Project[] projects, ProjectClassPathModifierImplementation pcmi, SourceGroup sg, String classPathType) throws IOException, UnsupportedOperationException {
            assert pcmi != null;
            return pcmi.addProjects(projects, sg, classPathType);
        }
    }
}
