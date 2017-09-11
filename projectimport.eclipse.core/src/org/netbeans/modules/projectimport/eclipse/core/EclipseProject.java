/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;
import org.netbeans.modules.projectimport.eclipse.core.spi.Facets;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory.ProjectDescriptor;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents Eclipse project structure.
 *
 * @author mkrauskopf
 */
public final class EclipseProject implements Comparable {

    /** Logger for this class. */
    private static final Logger logger =
            Logger.getLogger(EclipseProject.class.getName());
    
    private static final Lookup.Result<? extends ProjectTypeFactory> projectTypeFactories =
        Lookup.getDefault().lookupResult (ProjectTypeFactory.class);
    
    private Boolean importSupported;
    private ProjectTypeFactory projectFactory;
    private Set<EclipseProject> projectsWeDependOn;
    
    static final String PROJECT_FILE = ".project"; // NOI18N
    static final String CLASSPATH_FILE = ".classpath"; // NOI18N
    
    private Workspace workspace;
    
    private String name;
    private boolean internal = true;
    private DotClassPath cp;
    private Set<String> natures;
    private List<Link> links;
    
    private final File projectDir;
    private final File cpFile;
    private final File prjFile;
    private String jdkDirectory;
    
    private List<String> importProblems = new ArrayList<String>();
    
    private Facets projectFacets;
    
    /**
     * Returns <code>EclipseProject</code> instance representing Eclipse project
     * found in the given <code>projectDir</code>. If a project is not found in
     * the specified directory, <code>null</code> is returned.
     *
     * @return either a <code>EclipseProject</code> instance or null if a given
     *      <code>projectDir</code> doesn't contain valid Eclipse project.
     */
    static EclipseProject createProject(File projectDir) {
        if (!EclipseUtils.isRegularProject(projectDir)) {
            logger.fine(projectDir + " doesn't contain regular Eclipse project."); // NOI18N
            return null;
        }
        return new EclipseProject(projectDir);
    }
    
    /** Sets up a project directory. */
    /*private*/ EclipseProject(File projectDir) {
        this.projectDir = projectDir;
        File f = new File(projectDir, CLASSPATH_FILE);
        this.cpFile = f.exists() ? f : null;
        this.prjFile = new File(projectDir, PROJECT_FILE);
    }

    void removeInvalidSourceRoots(List<String> projectImportProblems) {
        List<DotClassPathEntry> projectSources = new ArrayList<DotClassPathEntry>();
        for (DotClassPathEntry e : getSourceRoots()) {
            if (new File(e.getAbsolutePath()).exists()) {
                projectSources.add(e);
            } else {
                projectImportProblems.add(
                    NbBundle.getMessage(EclipseProject.class, "MSG_InvalidSourceRoot", e.getRawPath())); //NOI18N
            }
        }
        cp.updateSourceRoots(projectSources);
    }

    void setFacets(Facets projectFacets) {
        this.projectFacets = projectFacets;
    }

    public Facets getFacets() {
        return projectFacets;
    }

    void setLinks(List<Link> links) {
        this.links = links;
    }

    List<Link> getLinks() {
        return links;
    }


    void setNatures(Set<String> natures) {
        this.natures = natures;
    }
    
    void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
    
    public Workspace getWorkspace() {
        return workspace;
    }
    
    void setClassPath(DotClassPath cp) {
        this.cp = cp;
        calculateAbsolutePaths();
        convertFileVariablesToFolderVariables();
        updateSourcePathAttribute();
        updateJavadocLocationAttribute();
        try {
            resolveContainers(new ArrayList<String>(), false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public List<DotClassPathEntry> getClassPathEntries() {
        return cp.getClassPathEntries();
    }
    
    /**
     * Returns project's name.
     */
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        this.name = name;
    }
    
    void setInternal(boolean internal) {
        this.internal = internal;
    }
    
    public boolean isInternal() {
        return internal;
    }
    
    public File getDirectory() {
        return projectDir;
    }
    
    /**
     * Returns metadata file containing information about this projects. I.e.
     * normally <em>.project</em> file withing the project's directory. See
     * {@link #PROJECT_FILE}.
     */
    File getProjectFile() {
        return prjFile;
    }
    
    /**
     * Returns metadata file containing information about this projects. I.e.
     * normally <em>.classpath</em> file withing the project's directory. See
     * {@link #CLASSPATH_FILE}.
     */
    File getClassPathFile() {
        return cpFile;
    }
    
    public Set<String> getNatures() {
        return natures;
    }
    
    /**
     * Can this Eclipse project be converted to NetBeans or not?
     */
    public boolean isImportSupported() {
        performRecognitionIfNeeded();
        return importSupported.booleanValue();
    }
    
    public ProjectTypeFactory getProjectTypeFactory() {
        performRecognitionIfNeeded();
        return projectFactory;
    }

    private ProjectDescriptor getProjectDescriptor() {
        return new ProjectTypeFactory.ProjectDescriptor(getDirectory(), natures, projectFacets);
    }
    
    File  getProjectFileLocation(String token) {
        if (!isImportSupported()) {
            return null;
        }
        return getProjectTypeFactory().getProjectFileLocation(getProjectDescriptor(), token);
    }
    
    private void performRecognitionIfNeeded() {
        if (importSupported == null) {
            importSupported = Boolean.FALSE;
            for (ProjectTypeFactory factory : projectTypeFactories.allInstances()) {
                if (factory.canHandle(getProjectDescriptor())) {
                    this.projectFactory = factory;
                    importSupported = Boolean.TRUE;
                    break;
                }
            }
        }
    }
    
    /**
     * Returns JDK directory for platform this project uses. Can be null in a
     * case when a JDK was set for an eclipse project in Eclipse then the
     * directory with JDK was deleted from filesystem and then a project is
     * imported to NetBeans.
     *
     * @return JDK directory for the project
     */
    public String getJDKDirectory() {
        if (jdkDirectory == null && workspace != null && cp.getJREContainer() != null) {
            logger.finest("Getting JDK directory for project " + this.getName()); // NOI18N
            jdkDirectory = workspace.getJDKDirectory(cp.getJREContainer().getRawPath());
            logger.finest("Resolved JDK directory: " + jdkDirectory); // NOI18N
            // jdkDirectory = workspace.getJDKDirectory(projectDir.getName());
        }
        return jdkDirectory;
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public List<DotClassPathEntry> getSourceRoots() {
        return cp.getSourceRoots();
    }
    
    /**
     * Returns collection of <code>EclipseProject</code> this project requires.
     */
    public Set<EclipseProject> getProjects() {
        if (workspace != null && projectsWeDependOn == null) {
            projectsWeDependOn = new HashSet<EclipseProject>();
            for (DotClassPathEntry entry : getClassPathEntries()) {
                if (entry.getKind() != DotClassPathEntry.Kind.PROJECT) {
                    continue;
                }
                EclipseProject prj = workspace.getProjectByRawPath(entry.getRawPath());
                if (prj != null) {
                    projectsWeDependOn.add(prj);
                }
            }
        }
        return projectsWeDependOn == null ?
            Collections.<EclipseProject>emptySet() : projectsWeDependOn;
    }

    void resolveContainers(List<String> importProblems, boolean importInProgress) throws IOException {
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            if (entry.getKind() != DotClassPathEntry.Kind.CONTAINER) {
                continue;
            }
            ClassPathContainerResolver.resolve(workspace, entry, importProblems, importInProgress);
        }
    }
    
    void replaceContainers() {
        List<DotClassPathEntry> newCp = new ArrayList<DotClassPathEntry>();
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            if (entry.getKind() != DotClassPathEntry.Kind.CONTAINER) {
                newCp.add(entry);
                continue;
            }
            List<DotClassPathEntry> repl = ClassPathContainerResolver.replaceContainerEntry(this, workspace, entry, importProblems);
            if (repl != null) {
                newCp.addAll(repl);
            } else {
                newCp.add(entry);
            }
        }
        cp.updateClasspath(newCp);
    }
    
    void setupEnvironmentVariables(List<String> importProblems) throws IOException {
        if (workspace == null) {
            return;
        }
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        boolean changed = false;
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            if (entry.getKind() != DotClassPathEntry.Kind.VARIABLE) {
                continue;
            }
            String s = EclipseUtils.splitVariable(entry.getRawPath())[0];
            Workspace.Variable v = getVariable(s);
            if (v != null) {
                s = "var."+PropertyUtils.getUsablePropertyName(s); //NOI18N
                if (ep.getProperty(s) == null) {
                    ep.setProperty(s, v.getLocation());
                    changed = true;
                } else if (!ep.getProperty(s).equals(v.getLocation())) {
                    importProblems.add(org.openide.util.NbBundle.getMessage(EclipseProject.class, "MSG_IDEVariableMismatch", s, ep.getProperty(s), v.getLocation())); //NOI18N
                }
            } else {
                importProblems.add(org.openide.util.NbBundle.getMessage(EclipseProject.class, "MSG_IDEVariableNotFound", s)); //NOI18N
                ep.setProperty(s, ""); //NOI18N
                changed = true;
            }
        }
        if (changed) {
            PropertyUtils.putGlobalProperties(ep);
        }
    }
    
    void convertFileVariablesToFolderVariables() {
        if (workspace == null) {
            return;
        }
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            if (entry.getKind() != DotClassPathEntry.Kind.VARIABLE) {
                continue;
            }
            String s = EclipseUtils.splitVariable(entry.getRawPath())[0];
            Workspace.Variable v = getVariable(s);
            if (v != null && v.isFileVariable()) {
                // convert eclipse file variable to folder variable
                entry.updateVariableValue(v.getName()+'/'+v.getFileName());
            }
        }
    }
    
    private void updateSourcePathAttribute() {
        if (workspace == null) {
            return;
        }
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            String sourcePath = entry.getProperty(DotClassPathEntry.ATTRIBUTE_SOURCEPATH);
            if (sourcePath == null) {
                continue;
            }
            String resolvedPath = resolveSourcePathAttribute(sourcePath, workspace, true);
            if (resolvedPath != null) {
                entry.updateSourcePath(resolvedPath);
            }
        }
    }
    
    static String resolveSourcePathAttribute(String sourcePath, Workspace workspace, boolean useVariables) {
        String resolvedPath = resolvePath(sourcePath, workspace);
        if (resolvedPath != null) {
            return resolvedPath;
        }
        // test whether sourcePath starts with variable:
        for (Workspace.Variable v : workspace.getVariables()) {
            if (sourcePath.startsWith(v.getName())) {
                String s[] = EclipseUtils.splitVariable(sourcePath);
                // change value to Ant style property directly usable in NB:
                if (useVariables) {
                    return "${"+s[0]+"}"+s[1]; // NOI18N
                } else {
                    return v.getLocation() + s[1];
                }
            }
        }
        return null;
    }
    
    private void updateJavadocLocationAttribute() {
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            String javadoc = entry.getProperty(DotClassPathEntry.ATTRIBUTE_JAVADOC);
            if (javadoc == null) {
                continue;
            }
            entry.updateJavadoc(resolveJavadocLocationAttribute(javadoc, workspace, importProblems, false));
        }
    }
    
    static String resolveJavadocLocationAttribute(String javadoc, Workspace workspace, List<String> importProblems, boolean returnURL) {
        String jarPath = null;
        // strip off jar protocol; because of 'platform' protocol which is 
        // undefined in NB the FileUtil.getArchiveFile will not be used here
        if (javadoc.startsWith("jar:")) { // NOI18N
            jarPath = javadoc.substring(javadoc.indexOf("!/")); //NOI18N
            if (!jarPath.endsWith("/")) { //NOI18N
                jarPath += "/"; //NOI18N
            }
            javadoc = javadoc.substring(4, javadoc.indexOf("!/")); //NOI18N
        }
        if (javadoc.startsWith("platform:/resource")) { // NOI18N
            if (workspace == null) {
                importProblems.add(org.openide.util.NbBundle.getMessage(EclipseProject.class, "MSG_WorkspaceDependentProtocol", javadoc)); //NOI18N
                return null;
            }
            String s = resolvePath(javadoc.substring(18), workspace);
            if (s != null) {
                if (returnURL) {
                    try {
                        s = Utilities.toURI(new File(s)).toURL().toExternalForm();
                        if (jarPath != null) {
                            s = "jar:"+s+jarPath;
                        }
                        return s;
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                        return null;
                    }
                } else {
                    if (jarPath != null) {
                        s += jarPath;
                    }
                    return s;
                }
            }
        }
        if (javadoc.startsWith("platform")) { // NOI18N
            importProblems.add(org.openide.util.NbBundle.getMessage(EclipseProject.class, "MSG_UnsupportedProtocol", javadoc)); //NOI18N
            return null;
        }
        // copied from FileUtil.getArchiveFile
        if (javadoc.indexOf("file://") > -1 && javadoc.indexOf("file:////") == -1) {  //NOI18N
            /* Replace because JDK application classloader wrongly recognizes UNC paths. */
            javadoc = javadoc.replaceFirst("file://", "file:////");  //NOI18N
        }
        URL u;
        try {
            u = new URL(javadoc);
        } catch (MalformedURLException ex) {
            importProblems.add(org.openide.util.NbBundle.getMessage(EclipseProject.class, "MSG_UnsupportedJavadocLocation", javadoc));
            return null;
        }
        if (!"file".equals(u.getProtocol())) { // NOI18N
            // XXX this is just a warning rather then import problem
            // Perhaps instead of List<String> for import problems define
            // a class which could have two lists: import problems and import warnings
            importProblems.add(org.openide.util.NbBundle.getMessage(EclipseProject.class, "MSG_UnsupportedProtocol", u.toExternalForm()));
            return null;
        }
        try {
            if (returnURL) {
                if (jarPath != null) {
                    return "jar:"+u.toExternalForm()+jarPath;
                } else {
                    return u.toExternalForm();
                }
            } else {
                File f = Utilities.toFile(u.toURI());
                String path = f.getPath();
                if (jarPath != null) {
                    path += jarPath;
                }
                return path;
            }
        } catch (URISyntaxException ex) {
            importProblems.add(org.openide.util.NbBundle.getMessage(EclipseProject.class, "MSG_JavadocCannotBeResolved", u.toExternalForm()));
        }
        return null;
    }

    public List<String> getImportProblems() {
        return importProblems;
    }
    
    private void calculateAbsolutePaths() {
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            setAbsolutePathForEntry(entry);
        }
        for (DotClassPathEntry entry : cp.getSourceRoots()) {
            setAbsolutePathForEntry(entry);
        }
        setAbsolutePathForEntry(cp.getOutput());
    }
    
    public DotClassPathEntry getOutput() {
        return cp.getOutput();
    }
    
    /**
     * Inteligently sets absolute path for a given entry with recongnizing of
     * links, projects, variables, relative and absolute entries.
     * If it is not possible (e.g. workspace Varible is not found) sets abs.
     * path to null.
     */
    private void setAbsolutePathForEntry(DotClassPathEntry entry) {
        if (entry == null) {
            return;
        }
        // set abs. path default (null)
        entry.setAbsolutePath(null);
        
        // try to resolve entry as a CONTAINER
        if (entry.getKind() == DotClassPathEntry.Kind.CONTAINER) {
            // we don't support CONTAINERs so we don't care about them here
            // (we support JRE/JDK containers but those are solved elsewhere)
            return;
        }
        
        // try to resolve entry as a VARIABLE
        if (entry.getKind() == DotClassPathEntry.Kind.VARIABLE) {
            String var[] = EclipseUtils.splitVariable(entry.getRawPath());
            Workspace.Variable variable = getVariable(var[0]);
            if (variable != null) {
                entry.setAbsolutePath(variable.getLocation() +var[1]);
            } else {
                logger.warning("cannot resolve variable '"+var[0]+"'. used in project "+ //NOI18N
                        getProjectFile().getPath()+" in entry "+entry); //NOI18N
            }
            return;
        }
        
        // try to resolve entry as a PROJECT
        if (entry.getKind() == DotClassPathEntry.Kind.PROJECT) {
            if (workspace != null) {
                EclipseProject ep = workspace.getProjectByName(entry.getRawPath().substring(1));
                if (ep != null) {
                    entry.setAbsolutePath(ep.getDirectory().getAbsolutePath());
                }
            }
            //            else {
            //                ErrorManager.getDefault().log(ErrorManager.WARNING, "workspace == null");
            //            }
            return;
        }
        
        // not VARIABLE, not PROJECT, not LINK -> either source root or library
        boolean resolved = false;
        String path = entry.getRawPath();
        // first check whether path starts with a project name:
        if (entry.getKind() == DotClassPathEntry.Kind.LIBRARY && workspace != null) {
            String s = resolvePath(path, workspace);
            if (s != null) {
                path = s;
                resolved = true;
            }
        }
        if (!resolved) {
            // convert possibly relative path to absolute:
            if (!Util.isAbsolutePath(path)) {
                path = projectDir.getAbsolutePath()+File.separator+path;
                resolved = true;
            }
        }
        entry.setAbsolutePath(path);
    }
    
    private static String resolvePath(String path, Workspace workspace) {
       File f = new File(path);
        // test whether it is file within a project, e.g. "/some-project/lib/file.jar"
        if (path.startsWith("/") && !f.exists()) { // NOI18N
            String s[] = EclipseUtils.splitProject(path);
            EclipseProject ep = workspace.getProjectByName(s[0]);
            if (ep != null) {
                String projectPath = ep.getDirectory().getAbsolutePath();
                String resolvedPath = resolveLinkPath(s[1], ep.links);
                if (resolvedPath != null) {
                    return resolvedPath;
                }
                return projectPath + s[1];
            }
        }
        return null;
    }
    
    private static String resolveLinkPath(String path, List<Link> links) {
        if (path.startsWith("/") || path.startsWith("\\")) {
            // get rid of first slash
            path = path.substring(1);
        }
        for (Link l : links) {
            if (path.startsWith(l.getName())) {
                return l.getLocation() + path.substring(l.getName().length());
            }
        }
        return null;
    }

    /**
     * Find variable for the given variable rawPath. Note that this method
     * returns <code>null</code> if workspace wasn't set for the project.
     */
    private Workspace.Variable getVariable(String rawPath) {
        if (workspace == null) {
            // workspace wasn't set for this project
            logger.fine("Workspace wasn't set for the project \"" + getName() + "\""); // NOI18N
            return null;
        }
        return workspace.getVariable(rawPath);
    }
    
    @Override
    public String toString() {
        return "EclipseProject[" + getName() + ", " + getDirectory() + "]"; // NOI18N
    }
    
    /* name is enough for now */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EclipseProject)) return false;
        final EclipseProject ePrj = (EclipseProject) obj;
        if (!name.equals(ePrj.name)) return false;
        return true;
    }
    
    /* name is enough for now */
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + System.identityHashCode(name);
        return result;
    }
    
    /**
     * Compares projects based on theirs <code>name</code>s. Projects which has
     * null-name will be last.
     */
    public int compareTo(Object o) {
        String name1 = getName();
        String name2 = null;
        if (o instanceof EclipseProject) {
            name2 = ((EclipseProject) o).getName();
        }
        if (name2 == null) {
            return (name1 == null ? 0 : -1);
        }
        return (name1 == null ? 1 : name1.compareToIgnoreCase(name2));
    }
}
