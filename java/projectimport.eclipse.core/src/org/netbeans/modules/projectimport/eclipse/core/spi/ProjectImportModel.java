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

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.projectimport.eclipse.core.ClassPathContainerResolver;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.core.EclipseUtils;
import org.netbeans.modules.projectimport.eclipse.core.Workspace;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Data about Eclipse project to import.
 */
public final class ProjectImportModel {

    private static final Logger LOG =
            Logger.getLogger(ProjectImportModel.class.getName());
    
    private EclipseProject project;
    private File projectLocation;
    private JavaPlatform platform;
    private List<Project> alreadyImportedProjects;
    private List<WizardDescriptor.Panel<WizardDescriptor>> extraWizardPanels;

    public ProjectImportModel(EclipseProject project, File projectLocation, JavaPlatform platform, 
            List<Project> alreadyImportedProjects) {
        this(project, projectLocation, platform, alreadyImportedProjects, null);
    }
    
    public ProjectImportModel(EclipseProject project, File projectLocation, JavaPlatform platform, 
            List<Project> alreadyImportedProjects, List<WizardDescriptor.Panel<WizardDescriptor>> extraWizardPanels) {
        Parameters.notNull("project", project); // NOI18N
        this.project = project;
        assert projectLocation == null || projectLocation.equals(FileUtil.normalizeFile(projectLocation));
        this.projectLocation = projectLocation;
        this.platform = platform;
        this.alreadyImportedProjects = alreadyImportedProjects;
        this.extraWizardPanels = extraWizardPanels;
    }

    public Facets getFacets() {
        return project.getFacets();
    }
    
    public String getProjectName() {
        return project.getName();
    }

    public File getEclipseProjectFolder() {
        return project.getDirectory();
    }

    public File getEclipseWorkspaceFolder() {
        Workspace workspace = project.getWorkspace();
        return workspace != null ? workspace.getDirectory() : null;
    }

    /**
     * Folder in which to create NetBeans project. In case NetBeans projects are
     * imported into the same location as Eclipse one the folder will already exist.
     */
    public File getNetBeansProjectLocation() {
        return projectLocation;
    }
    
    public Set<String> getEclipseNatures() {
        return project.getNatures();
    }

    public List<DotClassPathEntry> getEclipseClassPathEntries() {
        return project.getClassPathEntries();
    }
    
    public List<DotClassPathEntry> getEclipseSourceRoots() {
        return filterSourceRootsForTests(false);
    }
    
    public File[] getEclipseSourceRootsAsFileArray() {
        return convertToFileArray(getEclipseSourceRoots());
    }
    
    public List<DotClassPathEntry> getEclipseTestSourceRoots() {
        return filterSourceRootsForTests(true);
    }
    
    public File[] getEclipseTestSourceRootsAsFileArray() {
        return convertToFileArray(getEclipseTestSourceRoots());
    }

    private final Map<File,Boolean> looksLikeTests = new HashMap<File,Boolean>();
    private List<DotClassPathEntry> filterSourceRootsForTests(boolean test) {
        List<DotClassPathEntry> all = project.getSourceRoots();
        // if project has just one source root then keep it as sources:
        if (!hasJUnitOnClassPath() || all.size() <= 1) {
            if (test) {
                return Collections.emptyList();
            } else {
                return all;
            }
        }
        List<DotClassPathEntry> result = new ArrayList<DotClassPathEntry>(all.size());
        for (DotClassPathEntry entry : all) {
            File r = new File(entry.getAbsolutePath());
            Boolean isTest;
            synchronized (looksLikeTests) {
                isTest = looksLikeTests.get(r);
                if (isTest == null) {
                    isTest = hasTests(r);
                    looksLikeTests.put(r, isTest);
                }
            }
            if (!test ^ isTest) {
                result.add(entry);
            }
        }
        return result;
    }

    private boolean hasJUnitOnClassPath() {
        for (DotClassPathEntry entry : getEclipseClassPathEntries()) {
            if (ClassPathContainerResolver.isJUnit(entry)) {
                return true;
            }
        }
        return false;
    }

    /** Crude heuristic to see if a source root contains some sort of JUnit tests. */
    private boolean hasTests(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            File[] kids = fileOrDir.listFiles();
            if (kids != null) {
                for (File kid : kids) {
                    if (hasTests(kid)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return isJUnitFile(fileOrDir);
        }
    }
    
    private boolean isJUnitFile(File f) {
        if (!(f.getName().endsWith("Test.java") || // NOI18N
                f.getName().endsWith("Case.java") || // NOI18N
                f.getName().endsWith("Suite.java"))) { // NOI18N
            return false;
        }
        FileObject fo = FileUtil.toFileObject(f);
        try {
            return readJUnitFileHeader(fo);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
        
    private boolean readJUnitFileHeader(FileObject fo) throws IOException {
        InputStream is = fo.getInputStream();
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(is, getEncoding()));
            String line;
            int maxLines = 100;
            while (null != (line = input.readLine()) && maxLines > 0) {
                maxLines--;
                if (line.contains("junit.framework.Test") || // NOI18N
                    line.contains("org.junit.Test") || // NOI18N
                    line.contains("junit.framework.*") || // NOI18N
                    line.contains("org.junit.*")) { // NOI18N
                    return true;
                }
                    
            }
        } finally {
            is.close();
        }
        return false;
    }

    public JavaPlatform getJavaPlatform() {
        return platform;
    }
    
    /*public*/ String getEclipseVersion() {
        // TODO: could be useful for client to fork their import of needed
        return null;
    }

    public DotClassPathEntry getOutput() {
        return project.getOutput();
    }

    /**
     * Returns list of already imported projects. Handy for resolving project
     * dependencies.
     */
    public List<Project> getAlreadyImportedProjects() {
        return Collections.<Project>unmodifiableList(alreadyImportedProjects);
    }
    
    private static File[] convertToFileArray(List<DotClassPathEntry> entries) {
        List<File> res = new ArrayList<File>();
        for (DotClassPathEntry entry : entries) {
            res.add(new File(entry.getAbsolutePath()));
        }
        return res.toArray(new File[0]);
    }

    /**
     * Gets the Java source level of the project/workspace.
     * @return the source level, e.g. "1.5"
     */
    public String getSourceLevel() {
        Properties p = getPreferences("org.eclipse.jdt.core"); // NOI18N
        String compliance = p.getProperty("org.eclipse.jdt.core.compiler.compliance", "1.5"); // NOI18N
        return p.getProperty("org.eclipse.jdt.core.compiler.source", compliance); // NOI18N
    }

    /**
     * Gets the Java target level of the project/workspace.
     * @return the target level, e.g. "1.5"
     */
    public String getTargetLevel() {
        Properties p = getPreferences("org.eclipse.jdt.core"); // NOI18N
        String compliance = p.getProperty("org.eclipse.jdt.core.compiler.compliance", "1.5"); // NOI18N
        return p.getProperty("org.eclipse.jdt.core.compiler.codegen.targetPlatform", compliance); // NOI18N
    }

    /**
     * Checks whether debug information should be generated.
     * @return true if the Eclipse project asked to generate debug info for lines, variables, or source
     */
    public boolean isDebug() {
        Properties p = getPreferences("org.eclipse.jdt.core"); // NOI18N
        for (String kind : new String[] {"lineNumber", "localVariable", "sourceFile"}) { // NOI18N
            if ("generate".equals(p.getProperty("org.eclipse.jdt.core.compiler.debug." + kind))) { // NOI18N
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether deprecation warnings should be enabled.
     * @return true if the Eclipse project asked to warn on deprecated usages
     */
    public boolean isDeprecation() {
        Properties p = getPreferences("org.eclipse.jdt.core"); // NOI18N
        return warningOrError(p.getProperty("org.eclipse.jdt.core.compiler.problem.deprecation")); // NOI18N
    }

    /**
     * Gets additional compiler arguments such as warning flags.
     * @return a (possibly empty, other space-separated) list of additional compiler arguments
     */
    public String getCompilerArgs() {
        Properties p = getPreferences("org.eclipse.jdt.core"); // NOI18N
        StringBuilder b = new StringBuilder();
        maybeAddWarning(b, p, "org.eclipse.jdt.core.compiler.problem.fallthroughCase", "-Xlint:fallthrough"); // NOI18N
        maybeAddWarning(b, p, "org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally", "-Xlint:finally"); // NOI18N
        maybeAddWarning(b, p, "org.eclipse.jdt.core.compiler.problem.missingSerialVersion", "-Xlint:serial"); // NOI18N
        maybeAddWarning(b, p, "org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation", "-Xlint:unchecked"); // NOI18N
        return b.toString();
    }

    private static boolean warningOrError(String val) {
        return "warning".equals(val) || "error".equals(val); // NOI18N
    }

    private static void maybeAddWarning(StringBuilder b, Properties p, String eclipseKey, String javacFlag) {
        if (warningOrError(p.getProperty(eclipseKey))) {
            if (b.length() > 0) {
                b.append(' ');
            }
            b.append(javacFlag);
        }
    }

    /**
     * Gets the encoding, if any.
     * @return the Eclipse project's specified encoding (UTF-8 is the default, so never null)
     */
    public String getEncoding() {
        Properties p = getPreferences("org.eclipse.core.resources"); // NOI18N
        String enc = p.getProperty("encoding/<project>"); // NOI18N
        if (enc != null) {
            return enc;
        } else {
            enc = p.getProperty("encoding"); // NOI18N
            if (enc != null) {
                return enc;
            } else {
                return "UTF-8"; // NOI18N
            }
        }
    }

    private Properties getPreferences(String plugin) {
        Properties p = new Properties();
        String settings = ".settings/" + plugin + ".prefs"; //NOI18N
        EclipseUtils.tryLoad(p, getEclipseWorkspaceFolder(), ".metadata/.plugins/org.eclipse.core.runtime/" + settings); // NOI18N
        EclipseUtils.tryLoad(p, getEclipseProjectFolder(),settings); // NOI18N
        return p;
    }

    /**
     * Finds any launch configurations associated with this project in the workspace.
     * @return a possibly empty collection of launch configurations
     */
    public Collection<LaunchConfiguration> getLaunchConfigurations() {
        Workspace workspace = project.getWorkspace();
        if (workspace == null) {
            return Collections.emptySet();
        }
        List<LaunchConfiguration> configs = new ArrayList<LaunchConfiguration>();
        for (LaunchConfiguration config : workspace.getLaunchConfigurations()) {
            if (getProjectName().equals(config.getProjectName())) {
                configs.add(config);
            }
        }
        return configs;
    }

    /**
     * Returns valid value only in import scenario when impor wizard is shown. 
     * During project update the value is null.
     * @return
     */
    public List<WizardDescriptor.Panel<WizardDescriptor>> getExtraWizardPanels() {
        return extraWizardPanels;
    }

}
