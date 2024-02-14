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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Adds module dependencies that seem to correspond to some more visible artifact.
 * <ol>
 * <li>A library containing a classpath volume of the form jar:nbinst://code.name.base/...!/
 *     will be added as a dep on code.name.base.
 * </ol>
 */
public final class ModuleProjectClassPathExtender extends ProjectClassPathModifierImplementation {

    private final NbModuleProject project;

    public ModuleProjectClassPathExtender(NbModuleProject project) {
        this.project = project;
    }

    protected SourceGroup[] getExtensibleSourceGroups() {
        List<SourceGroup> sgs = new ArrayList<SourceGroup>(2);
        for (SourceGroup g : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (g.getRootFolder() == project.getSourceDirectory()
                    || g.getRootFolder() == project.getTestSourceDirectory("unit")) {
                sgs.add(g);
            }
        }
        return sgs.toArray(new SourceGroup[0]);
    }

    protected String[] getExtensibleClassPathTypes(SourceGroup sourceGroup) {
        // XXX EXECUTE could be supported if desired (just add a runtime-only module dep)
        return new String[] {ClassPath.COMPILE};
    }

    protected boolean addLibraries(Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        boolean cpChanged = false;
        for (Library library : libraries) {
            if (library.getName().matches("junit(_4)?")) {
                ModuleEntry entry = project.getModuleList().getEntry("org.netbeans.libs.junit4");
                if (entry != null) {
                    ProjectXMLManager pxm = new ProjectXMLManager(project);
                    cpChanged |= pxm.addTestDependency("unit", new TestModuleDependency(entry, false, false, true)); // NOI18N
                }
                continue;
            }
            for (URL u : library.getContent("classpath")) { // NOI18N
                URL jar = FileUtil.getArchiveFile(u);
                if (jar != null && jar.getProtocol().equals("nbinst")) { // NOI18N
                    String cnb = jar.getHost();
                    if (cnb != null && cnb.length() > 0) {
                        ModuleEntry entry = project.getModuleList().getEntry(cnb);
                        if (entry != null) {
                            if (sourceGroup.getRootFolder() == project.getSourceDirectory()) {
                                cpChanged |= ApisupportAntUtils.addDependency(project, cnb, null, null, true, null);
                            } else if (sourceGroup.getRootFolder() == project.getTestSourceDirectory("unit")) { // NOI18N
                                ProjectXMLManager pxm = new ProjectXMLManager(project);
                                cpChanged |= pxm.addTestDependency("unit", new TestModuleDependency(entry, false, false, true)); // NOI18N
                            } else {
                                throw new UnsupportedOperationException("wrong source group");
                            }
                            continue;
                        } else {
                            IOException e = new IOException("no module " + cnb); // NOI18N
                            Exceptions.attachLocalizedMessage(e,
                                    NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_could_not_find_module", cnb));
                            throw e;
                        }
                    }
                }
                IOException e = new IOException("unknown lib " + library.getName()); // NOI18N
                Exceptions.attachLocalizedMessage(e,
                        NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_unsupported_library", library.getDisplayName()));
                throw e;
            }
        }
        if (cpChanged) {
            ProjectManager.getDefault().saveProject(project);
        }
        return cpChanged;
    }

    protected boolean removeLibraries(Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException(); // XXX could be supported
    }

    protected boolean addRoots(URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        if (sourceGroup.getRootFolder() != project.getSourceDirectory()) {
            throw new UnsupportedOperationException("cannot add raw JARs to test roots");
        }
        ProjectXMLManager pxm = new ProjectXMLManager(project);
        Map<String,String> origCpexts = pxm.getClassPathExtensions();
        Map<String,String> cpexts = new LinkedHashMap<String,String>(origCpexts);
        boolean cpChanged = false;
        for (URL root : classPathRoots) {
            File jar = FileUtil.archiveOrDirForURL(root);
            if (jar == null || !jar.isFile()) {
                throw new UnsupportedOperationException("cannot add: " + root);
            }
            String relativePath = ApisupportAntUtils.CPEXT_RUNTIME_RELATIVE_PATH + jar.getName();
            String binaryOrigin = ApisupportAntUtils.CPEXT_BINARY_PATH + jar.getName();
            File target = project.getHelper().resolveFile(binaryOrigin);
            if (jar.equals(target)) { // already in place
                cpexts.put(relativePath, binaryOrigin);
            } else {
                if (!target.isFile() || target.length() != jar.length()) {
                    cpChanged = true; // probably fresh JAR; XXX check contents
                }
                String[] result = ApisupportAntUtils.copyClassPathExtensionJar(FileUtil.toFile(project.getProjectDirectory()), jar);
                assert result != null : jar;
                assert result[0].equals(relativePath) : result[0];
                assert result[1].equals(binaryOrigin) : result[1];
                cpexts.put(relativePath, binaryOrigin);
            }
        }
        if (!cpexts.equals(origCpexts)) {
            pxm.replaceClassPathExtensions(cpexts);
            ProjectManager.getDefault().saveProject(project);
            cpChanged = true;
        }
        return cpChanged;
    }

    protected boolean removeRoots(URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    protected boolean addAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        // XXX ideally would check to see if it was owned by a NBM project in this universe...
        UnsupportedOperationException e = new UnsupportedOperationException("not implemented: " + Arrays.asList(artifactElements)); // NOI18N
        // XXX handle >1 args
        String displayName = artifactElements.length > 0 ? artifactElements[0].toString() : "<nothing>";
        if (artifactElements.length > 0 && "file".equals(artifactElements[0].getScheme())) { // NOI18N
            displayName = Utilities.toFile(artifactElements[0]).getAbsolutePath();
        }
        Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_jar", displayName));
        throw e;
    }

    protected boolean removeAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
