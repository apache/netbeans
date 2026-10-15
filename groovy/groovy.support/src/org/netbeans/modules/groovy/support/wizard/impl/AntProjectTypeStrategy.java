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

package org.netbeans.modules.groovy.support.wizard.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import org.netbeans.api.java.classpath.ClassPath;
import static org.netbeans.api.java.classpath.ClassPath.COMPILE;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.groovy.support.wizard.JUnit;
import org.netbeans.modules.groovy.support.wizard.ProjectTypeStrategy;
import static org.netbeans.modules.groovy.support.wizard.impl.Bundle.*;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import static org.openide.ErrorManager.ERROR;
import static org.openide.ErrorManager.WARNING;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Martin Janicek
 */
public class AntProjectTypeStrategy extends ProjectTypeStrategy {

    private static final String JUNIT4_LIB = "junit_4"; // NOI18N
    private static final String JUNIT3_LIB = "junit";   // NOI18N

    /** full name of a file specific for the JUnit 3.8.x library */
    private static final String JUNIT3_SPECIFIC = "junit/awtui/TestRunner.class";       //NOI18N
    /** full name of a file specific for the JUnit 4.x library */
    private static final String JUNIT4_SPECIFIC = "org/junit/Test.class";               //NOI18N



    public AntProjectTypeStrategy(Project project) {
        super(project);
    }


    @Override
    public void addJUnitLibrary(JUnit jUnit) {
        storeProjectSettingsJUnitVer(jUnit);
    }

    @Override
    public boolean existsGroovyTestFolder(List<SourceGroup> groups) {
        return existsFolder(groups, "test"); // NOI18N
    }

    @Override
    public boolean existsGroovySourceFolder(List<SourceGroup> groups) {
        return existsFolder(groups, "src"); // NOI18N
    }

    private boolean existsFolder(List<SourceGroup> groups, String folderName) {
        for (SourceGroup group : groups) {
            final String groupPath = group.getRootFolder().getPath();

            // Two check because of issue #221727
            if (groupPath.endsWith("/" + folderName) || groupPath.contains("/" + folderName + "/")) { // NOI18N
                return true;
            }
        }
        return false;
    }

    @Override
    public void createGroovyTestFolder() {
        createFolder(project.getProjectDirectory(), "test"); // NOI18N
    }

    @Override
    public void createGroovySourceFolder() {
        createFolder(project.getProjectDirectory(), "src"); // NOI18N
    }

    /**
     * This method basically just move the /test folder to the beginning of the
     * list and leave the rest of the source groups in the same order as it was before.
     *
     * @param groups to regroup
     * @return reordered source groups
     */
    @Override
    public List<SourceGroup> moveTestFolderAsFirst(List<SourceGroup> groups) {
        return moveAsFirst(groups, "/test").subList(0, 1); // #219766
    }

    @Override
    public List<SourceGroup> moveSourceFolderAsFirst(List<SourceGroup> groups) {
        return moveAsFirst(groups, "/src");
    }

    @Override
    public JUnit findJUnitVersion() {
        final boolean hasJUnit3;
        final boolean hasJUnit4;
        final ClassPath classPath = getTestClassPath(project);
        if (classPath != null) {
            hasJUnit3 = (classPath.findResource(JUNIT3_SPECIFIC) != null);
            hasJUnit4 = (classPath.findResource(JUNIT4_SPECIFIC) != null);
        } else {
            hasJUnit3 = false;
            hasJUnit4 = false;
        }

        if (hasJUnit4) {
            return JUnit.JUNIT4;
        } else if (hasJUnit3) {
            return JUnit.JUNIT3;
        } else {
            return JUnit.NOT_DECLARED;
        }
    }

    private ClassPath getTestClassPath(final Project project) {
        final ClassPathProvider cpProvider = project.getLookup().lookup(ClassPathProvider.class);
        if (cpProvider == null) {
            return null;
        }

        for (FileObject testRoot : getTestFolders(project)) {
            ClassPath testClassPath = cpProvider.findClassPath(testRoot, COMPILE);
            if (testClassPath != null) {
                return testClassPath;
            }
        }

        return null;
    }

    private List<FileObject> getTestFolders(Project project) {
        List<FileObject> testFolders = new ArrayList<FileObject>();

        FileObject[] children = project.getProjectDirectory().getChildren();
        for (FileObject child : children) {
            if (child.getNameExt().startsWith("test")) {
                testFolders.add(child);
            }
        }
        return testFolders;
    }


    @Messages({
        "junitlib_confirm_title=Create Tests",
        "junitlib_confirm_text=<html><p>To create JUnit tests, the IDE needs to download and install the JUnit library.</p><p>Do you want to proceed?<p>",
        "junitlib_confirm_accept=Download and Install JUnit"
    })
    private boolean storeProjectSettingsJUnitVer(JUnit jUnit) {
        String version = "";
        if (jUnit == JUnit.JUNIT3) {
            version = JUNIT3_LIB;
        } else if (jUnit == JUnit.JUNIT4) {
            version = JUNIT4_LIB;
        }
        if (LibraryManager.getDefault().getLibrary(version) == null) {
            for (BrokenReferencesSupport.LibraryDefiner definer : Lookup.getDefault().lookupAll(BrokenReferencesSupport.LibraryDefiner.class)) {
                Callable<Library> download = definer.missingLibrary(version);
                if (download != null) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(junitlib_confirm_text(), junitlib_confirm_title());
                    JButton accept = new JButton(junitlib_confirm_accept());
                    accept.setDefaultCapable(true);
                    nd.setOptions(new Object[] {accept, NotifyDescriptor.CANCEL_OPTION});
                    if (DialogDisplayer.getDefault().notify(nd) == accept) {
                        try {
                            download.call();
                        } catch (Exception x) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    break;
                }
            }
        }

        final boolean hasJUnit3;
        final boolean hasJUnit4;
        final ClassPath classPath = getTestClassPath(project);
        if (classPath != null) {
            hasJUnit3 = (classPath.findResource(JUNIT3_SPECIFIC) != null);
            hasJUnit4 = (classPath.findResource(JUNIT4_SPECIFIC) != null);
        } else {
            hasJUnit3 = false;
            hasJUnit4 = false;
        }

        final Pattern pattern = Pattern.compile("^junit(?:_|\\W)+([34])(?:\\b|_).*");   //NOI18N

        JUnitLibraryComparator libraryComparator = null;

        Library libraryToAdd = null;
        Collection<Library> librariesToRemove = null;

        Library[] libraries = LibraryManager.getDefault().getLibraries();
        for (Library library : libraries) {
            String name = library.getName().toLowerCase();
            if (!name.startsWith("junit")) {                            //NOI18N
                continue;
            }

            boolean add    = false;
            boolean remove = false;
            Matcher matcher;
            final String verNumToAdd;
            if ((jUnit == JUnit.JUNIT3) && !hasJUnit3) {
                verNumToAdd = "3";                                      //NOI18N
            } else if ((jUnit == JUnit.JUNIT4) && !hasJUnit4) {
                verNumToAdd = "4";                                      //NOI18N
            } else {
                verNumToAdd = null;
            }
            String verNumToRemove = (jUnit == JUnit.JUNIT3) ? "4" : "3"; //NOI18N
            if (name.equals("junit")) {                                  //NOI18N
                add    = (verNumToAdd    == "3");                        //NOI18N
                remove = (verNumToRemove == "3");                        //NOI18N
            } else if ((matcher = pattern.matcher(name)).matches()) {
                String verNum = matcher.group(1);
                add    = verNum.equals(verNumToAdd);
                remove = verNum.equals(verNumToRemove);
            }
            if (add) {
                if (libraryToAdd == null) {
                    libraryToAdd = library;
                } else {
                    /*
                     * If there are multiple conforming libraries, we only want
                     * to add one - the most recent one (i.e. having the highest
                     * version number).
                     */
                    if (libraryComparator == null) {
                        libraryComparator = new JUnitLibraryComparator();
                    }
                    if (libraryComparator.compare(libraryToAdd, library) > 0) {
                        libraryToAdd = library;
                    }
                }
            }
            if (remove) {
                if (librariesToRemove == null) {
                    librariesToRemove = new ArrayList<Library>(2);
                }
                librariesToRemove.add(library);
            }
        }
        if ((libraryToAdd == null) && (librariesToRemove == null)) {
            return true;
        }

        final List<FileObject> projectArtifacts = getProjectTestArtifacts(project);
        if (projectArtifacts.isEmpty()) {
            return true;
        }

        final Library[] libsToAdd, libsToRemove;
        if (libraryToAdd != null) {
            libsToAdd = new Library[] {libraryToAdd};
        } else {
            libsToAdd = null;
        }
        if (librariesToRemove != null) {
            libsToRemove = librariesToRemove.toArray(new Library[0]);
        } else {
            libsToRemove = null;
        }
        assert (libsToAdd != null) || (libsToRemove != null);

        class LibrarySetModifier implements Runnable {
            @Override
            public void run() {
                boolean modified = false;
                try {
                    if (libsToAdd != null) {
                        for (FileObject prjArtifact : projectArtifacts) {
                            modified |= ProjectClassPathModifier.addLibraries(libsToAdd, prjArtifact, COMPILE);
                        }
                    }
                    if (libsToRemove != null) {
                        for (FileObject prjArtifact : projectArtifacts) {
                            modified |= ProjectClassPathModifier.removeLibraries(libsToRemove, prjArtifact, COMPILE);
                        }
                    }
                } catch (UnsupportedOperationException ex) {
                    String prjName = ProjectUtils.getInformation(project).getDisplayName();
                    ErrorManager.getDefault().log(
                            WARNING,
                            "Project " + prjName                             //NOI18N
                            + ": Could not modify set of JUnit libraries"    //NOI18N
                            + " - operation not supported by the project."); //NOI18N
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ERROR, ex);
                }
                if (modified) {
                    try {
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ERROR, ex);
                    }
                }
            }
        }
        ProjectManager.mutex().writeAccess(new LibrarySetModifier());
        return true;
    }

    private List<FileObject> getProjectTestArtifacts(final Project project) {
        final ClassPathProvider cpProvider = project.getLookup().lookup(ClassPathProvider.class);
        final Collection<FileObject> testFolders = getTestFolders(project);

        List<FileObject> result = null;
        for (FileObject testRoot : testFolders) {
            ClassPath testClassPath = cpProvider.findClassPath(testRoot, ClassPath.COMPILE);
            if (testClassPath != null) {
                if (result == null) {
                    if (testFolders.size() == 1) {
                        return Collections.<FileObject>singletonList(testRoot);
                    } else {
                        result = new ArrayList<FileObject>(3);
                    }
                }
                result.add(testRoot);
            }
        }
        return (result != null) ? result : Collections.<FileObject>emptyList();
    }

    private static class JUnitLibraryComparator implements Comparator<Library>, Serializable {

        @Override
        public int compare(Library l1, Library l2) {
            String name1 = l1.getName().toLowerCase();
            String name2 = l2.getName().toLowerCase();

            if (name1.equals(name2)) {
                return 0;
            } else if (name1.equals("junit")) {                             //NOI18N
                return -1;
            } else if (name2.equals("junit")) {                             //NOI18N
                return 1;
            }

            final String[] parts1 = name1.substring(5).split("_|\\W");      //NOI18N
            final String[] parts2 = name2.substring(5).split("_|\\W");      //NOI18N
            final int min = Math.min(parts1.length, parts2.length);
            for (int i = 0; i < min; i++) {
                int partCmp = parts1[i].compareTo(parts2[i]);
                if (partCmp != 0) {
                    return partCmp;
                }
            }
            return parts2.length - parts1.length;
        }
    }
}
