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

package org.netbeans.modules.java.freeform.ui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.spi.ProjectConstants;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.modules.java.freeform.JavaProjectNature;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Memory model of project. Used for creation or customization of project.
 *
 * @author David Konecny
 */
public class ProjectModel  {
    
    /** Original project base folder */
    private File baseFolder;
    
    /** Freeform Project base folder */
    private File nbProjectFolder;

    private PropertyEvaluator evaluator;
    
    private String sourceLevel;
    
    private String encoding;
    
    public static final String NO_ENCODING = 
            NbBundle.getBundle(org.netbeans.modules.java.freeform.ui.ProjectModel.class).getString("No_Encoding");
    
    /** List of JavaProjectGenerator.SourceFolders instances of type "java". */
    private List<JavaProjectGenerator.SourceFolder> sourceFolders;
    
    public List<JavaProjectGenerator.JavaCompilationUnit> javaCompilationUnitsList;

    private Set<String> addedSourceFolders;
    private Set<String> removedSourceFolders;
    
    public static final String TYPE_JAVA = "java"; // NOI18N
    public static final String CLASSPATH_MODE_COMPILE = "compile"; // NOI18N
    //Upper bound of sourse level supported by the java freeform project
    private static final SpecificationVersion JDK_MAX_SUPPORTED_VERSION = new SpecificationVersion ("1.5"); //NOI18N
    
    private ProjectModel(File baseFolder, File nbProjectFolder, PropertyEvaluator evaluator,
            List<JavaProjectGenerator.SourceFolder> sourceFolders, List<JavaProjectGenerator.JavaCompilationUnit> compUnits) {
        this.baseFolder = baseFolder;
        this.nbProjectFolder = nbProjectFolder;
        this.evaluator = evaluator;
        this.sourceFolders = sourceFolders;
        this.javaCompilationUnitsList = compUnits;
        if (javaCompilationUnitsList.size() > 0) {
            sourceLevel = javaCompilationUnitsList.get(0).sourceLevel;
        }
        if (sourceLevel == null) {
            setSourceLevel(getDefaultSourceLevel());
        }
        if (sourceFolders.size() > 0) {
            JavaProjectGenerator.SourceFolder sf = sourceFolders.get(0);
            this.encoding = sf.encoding == null ? null : Charset.forName(sf.encoding).name();
        }
        resetState();
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    /**
     * Notifies only about change in source folders and compilation units.
     */
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    private void resetState() {
        addedSourceFolders = new HashSet<String>();
        removedSourceFolders = new HashSet<String>();
    }

    /** Create empty project model. Useful for new project creation. */
    public static ProjectModel createEmptyModel(File baseFolder, File nbProjectFolder, PropertyEvaluator evaluator) {
        return new ProjectModel(baseFolder, nbProjectFolder, evaluator,
                new ArrayList<JavaProjectGenerator.SourceFolder>(),
                new ArrayList<JavaProjectGenerator.JavaCompilationUnit>());
    }

    /** Create project model of existing project. Useful for project customization. */
    public static ProjectModel createModel(final File baseFolder, final File nbProjectFolder, final PropertyEvaluator evaluator, final AntProjectHelper helper) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<ProjectModel>() {
            public ProjectModel run() {
                ProjectModel pm = new ProjectModel(
                        baseFolder, 
                        nbProjectFolder, 
                        evaluator,
                        // reads only "java" type because other types are not editable in UI
                        JavaProjectGenerator.getSourceFolders(helper, TYPE_JAVA),
                        JavaProjectGenerator.getJavaCompilationUnits(helper,
                            Util.getAuxiliaryConfiguration(helper))
                    );
                // only "java" type of sources was read so fix style to "pacakges" on all
                updateStyle(pm.sourceFolders);
                return pm;
            }
        });
    }

    /** Instantiate project model as new Java project. */
    public static void instantiateJavaProject(AntProjectHelper helper, ProjectModel model) throws IOException {
        List<JavaProjectGenerator.SourceFolder> sourceFolders = model.updatePrincipalSourceFolders(model.sourceFolders, true);
        
        updateSourceFolders(sourceFolders, model);
        if (sourceFolders.size() > 0) {
            JavaProjectGenerator.putSourceFolders(helper, sourceFolders, null);
        }
        if (sourceFolders.size() > 0) {
            JavaProjectGenerator.putSourceViews(helper, sourceFolders, null);
        }
        JavaProjectGenerator.putJavaCompilationUnits(helper, Util.getAuxiliaryConfiguration(helper), model.javaCompilationUnitsList);        
        List<JavaProjectGenerator.Export> exports = JavaProjectGenerator.guessExports(
                model.evaluator, model.baseFolder, JavaProjectGenerator.getTargetMappings(helper), model.javaCompilationUnitsList);
        if (exports.size() > 0) {
            JavaProjectGenerator.putExports(helper, exports);
        }
        List<String> subprojects = JavaProjectGenerator.guessSubprojects(model.evaluator, model.javaCompilationUnitsList, model.baseFolder, model.nbProjectFolder);
        if (subprojects.size() > 0) {
            JavaProjectGenerator.putSubprojects(helper, subprojects);
        }
        
        model.resetState();
    }
    
    /** Persist modifications of project. */
    public static void saveProject(final AntProjectHelper helper, final ProjectModel model) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                // stores only "java" type because other types was not read
                JavaProjectGenerator.putSourceFolders(helper, model.sourceFolders, TYPE_JAVA);
                JavaProjectGenerator.putSourceViews(helper, model.sourceFolders, JavaProjectNature.STYLE_PACKAGES);

                List<JavaProjectGenerator.SourceFolder> sourceFolders = JavaProjectGenerator.getSourceFolders(helper, null);
                sourceFolders = model.updatePrincipalSourceFolders(sourceFolders, false);
                updateSourceFolders(sourceFolders, model);
                JavaProjectGenerator.putSourceFolders(helper, sourceFolders, null);

                AuxiliaryConfiguration aux = Util.getAuxiliaryConfiguration(helper);
                JavaProjectGenerator.putJavaCompilationUnits(helper, aux, model.javaCompilationUnitsList);
                model.resetState();

                List<JavaProjectGenerator.Export> exports = JavaProjectGenerator.guessExports(model.getEvaluator(), model.baseFolder,
                    JavaProjectGenerator.getTargetMappings(helper), model.javaCompilationUnitsList);
                JavaProjectGenerator.putExports(helper, exports);

                List<String> subprojects = JavaProjectGenerator.guessSubprojects(model.getEvaluator(), 
                    model.javaCompilationUnitsList, model.baseFolder, model.nbProjectFolder);
                JavaProjectGenerator.putSubprojects(helper, subprojects);
                
                List<String> buildFolders = JavaProjectGenerator.guessBuildFolders(model.getEvaluator(), 
                    model.javaCompilationUnitsList, model.baseFolder, model.nbProjectFolder);
                JavaProjectGenerator.putBuildFolders(helper, buildFolders);
                
                List<String> buildFiles = JavaProjectGenerator.getBuildFiles(model.getEvaluator(), 
                    model.javaCompilationUnitsList, model.baseFolder, model.nbProjectFolder);
                JavaProjectGenerator.putBuildFiles(helper, buildFiles);
                
                return null;
            }
        });
    }
    
    // #120508: special source folder is added to save encoding for files directly under project folder
    private static void updateSourceFolders(List<JavaProjectGenerator.SourceFolder> list, ProjectModel model) {
        if (model.encoding != null) {
            for (JavaProjectGenerator.SourceFolder sf : list) {
                if (sf.location.equals(".")) { // NOI18N
                    sf.encoding = model.encoding;
                    return;
                }
            }
            JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
            Project project = null;
            try {
                project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(model.nbProjectFolder));
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            String label = project != null ? ProjectUtils.getInformation(project).getDisplayName() : "projectdir"; // NOI18N
            sf.label = label;
            sf.location = "."; // NOI18N
            sf.encoding = model.encoding;
            list.add(sf);
        }
    }
    
    /**
     * This method according to the state of removed/added source folders
     * will ensure that all added typed external source roots will have
     * corresponding principal source folder and that principal source
     * folders of all removed typed external source roots are removed too.
     * In addition it can add project base folder as principal root 
     * if it is external.
     *
     * It is expected that this method will be called before project instantiation
     * or before update of project's data. 
     *
     * @param allSourceFolders list of all source folders, i.e. typed and untyped.
     * @param checkProjectDir should project base folder be checked
     * and added as principal source folder if needed or not
     * @return copy of allSourceFolders items plus added principal source folders
     */
    /*private*/ List<JavaProjectGenerator.SourceFolder> updatePrincipalSourceFolders(
            List<JavaProjectGenerator.SourceFolder> allSourceFolders, boolean checkProjectDir) {
        List<JavaProjectGenerator.SourceFolder> allSF = new ArrayList<JavaProjectGenerator.SourceFolder>(allSourceFolders);
        for (String location : addedSourceFolders) {
            
            if (!isExternalSourceRoot(location)) {
                continue;
            }
            
            boolean exist = false;
            String label = ""; // NOI18N
            String includes = null, excludes = null;
            for (JavaProjectGenerator.SourceFolder _sf : allSF) {
                if (_sf.location.equals(location) && _sf.type == null) {
                    exist = true;
                    break;
                }
                if (_sf.location.equals(location) && _sf.type != null) {
                    // find some label to use
                    label = _sf.label;
                    includes = _sf.includes;
                    excludes = _sf.excludes;
                }
            }
            
            if (!exist) {
                JavaProjectGenerator.SourceFolder _sf = new JavaProjectGenerator.SourceFolder();
                _sf.location = location;
                _sf.label = label;
                _sf.includes = includes;
                _sf.excludes = excludes;
                allSF.add(_sf);
            }
        }

        for (String location : removedSourceFolders) {
            
            if (!isExternalSourceRoot(location)) {
                continue;
            }
            
            Iterator<JavaProjectGenerator.SourceFolder> it = allSF.iterator();
            while (it.hasNext()) {
                JavaProjectGenerator.SourceFolder _sf = it.next();
                if (_sf.location.equals(location) && _sf.type == null) {
                    it.remove();
                }
            }
        }
        
        if (checkProjectDir && !baseFolder.equals(nbProjectFolder)) {
            JavaProjectGenerator.SourceFolder gen = new JavaProjectGenerator.SourceFolder();
            gen.location = "${"+ProjectConstants.PROP_PROJECT_LOCATION+"}"; // NOI18N
            // XXX: uniquefy label
            gen.label = baseFolder.getName();
            allSF.add(gen);
        }
        
        return allSF;
    }

    private boolean isExternalSourceRoot(String location) {
        String baseFolder_ = baseFolder.getAbsolutePath();
        if (!baseFolder_.endsWith(File.separator)) {
            baseFolder_ += File.separatorChar;
        }
        String nbProjectFolder_ = nbProjectFolder.getAbsolutePath();
        if (!nbProjectFolder_.endsWith(File.separator)) {
            nbProjectFolder_ += File.separatorChar;
        }
        File f = Util.resolveFile(evaluator, baseFolder, location);
        if (f == null) {
            return false;
        }
        location = f.getAbsolutePath();
        return (!location.startsWith(baseFolder_) &&
                    !location.startsWith(nbProjectFolder_));
    }
    
    /** Original project base folder. */
    public File getBaseFolder() {
        return baseFolder;
    }
    
    /** NetBeans project folder. */
    public File getNBProjectFolder() {
        return nbProjectFolder;
    }
    
    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    public int getSourceFoldersCount() {
        return sourceFolders.size();
    }
    
    public JavaProjectGenerator.SourceFolder getSourceFolder(int index) {
        return sourceFolders.get(index);
    }
    
    public void moveSourceFolder(int fromIndex, int toIndex) {
        JavaProjectGenerator.SourceFolder sf = sourceFolders.remove(fromIndex);
        sourceFolders.add(toIndex, sf);
    }
    
    public void addSourceFolder(JavaProjectGenerator.SourceFolder sf, boolean isTests) {
        List<CompilationUnitKey> keys = createCompilationUnitKeys();
        boolean singleCU = isSingleCompilationUnit(keys);
        if (singleCU) {
            // Check that source being added is part of the compilation unit.
            // If it is not then switch to multiple compilation unit mode.
            JavaProjectGenerator.JavaCompilationUnit cu = javaCompilationUnitsList.get(0);
            if (cu.isTests != isTests) {
                updateCompilationUnits(true);
                singleCU = false;
            }
        }
        sourceFolders.add(sf);
        if (singleCU) {
            if (TYPE_JAVA.equals(sf.type)) {
                // update existing single compilation unit
                JavaProjectGenerator.JavaCompilationUnit cu = javaCompilationUnitsList.get(0);
                cu.packageRoots.add(sf.location);
            }
        } else {
            // make sure new compilation unit is created for the source folder
            for (CompilationUnitKey key : createCompilationUnitKeys()) {
                getCompilationUnit(key, isTests);
            }
        }
        // remember all added locations
        if (removedSourceFolders.contains(sf.location)) {
            removedSourceFolders.remove(sf.location);
        } else {
            addedSourceFolders.add(sf.location);
        }
        fireChangeEvent();
    }

    public void removeSourceFolder(int index) {
        JavaProjectGenerator.SourceFolder sf = sourceFolders.get(index);
        if (TYPE_JAVA.equals(sf.type)) {
            removeSourceLocation(sf.location);
        }
        sourceFolders.remove(index);
        // remember all removed locations
        if (addedSourceFolders.contains(sf.location)) {
            addedSourceFolders.remove(sf.location);
        } else {
            removedSourceFolders.add(sf.location);
        }
        fireChangeEvent();
    }
    
    public void clearSourceFolders() {
        sourceFolders.clear();
        javaCompilationUnitsList.clear();
        fireChangeEvent();
    }
    
    public String getSourceLevel() {
        return sourceLevel;
    }

    public void setSourceLevel(String sourceLevel) {
        if ((this.sourceLevel == null && sourceLevel == null) ||
            (this.sourceLevel != null && this.sourceLevel.equals(sourceLevel))) {
            return;
        }
        this.sourceLevel = sourceLevel;
        for (JavaProjectGenerator.JavaCompilationUnit cu : javaCompilationUnitsList) {
            cu.sourceLevel = sourceLevel;
        }
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String enc) {
        if (enc == null || enc.equals(NO_ENCODING)) {
            encoding = null;
        } else {
            encoding = enc;
        }
        for (JavaProjectGenerator.SourceFolder sf : sourceFolders) {
            sf.encoding = encoding;
        }
    }
    
    public boolean canHaveSeparateClasspath() {
        // if there is more than one source root or more than one
        // compilation unit then enable checkbox "Separate Classpath".
        return (sourceFolders.size() > 1 || javaCompilationUnitsList.size() > 1);
    }

    public boolean canCreateSingleCompilationUnit() {
        // if there are sources and test sources I cannot create
        // single compilation unit for them:
        boolean testCU = false;
        boolean sourceCU = false;
        for (JavaProjectGenerator.JavaCompilationUnit cu : javaCompilationUnitsList) {
            if (cu.isTests) {
                testCU = true;
            } else {
                sourceCU = true;
            }
        }
        return !(testCU && sourceCU);
    }

    public static boolean isSingleCompilationUnit(List<ProjectModel.CompilationUnitKey> compilationUnitKeys) {
        return compilationUnitKeys.size() == 1 && compilationUnitKeys.get(0).label == null;
    }

    /**
     * This method checks Java source folders and compilation units and returns
     * list of CompilationUnitKey which represent them. The problem solved by
     * this method is that although usually there is 1:1 mapping between
     * source folders and compilation units, there can be also N:1 mapping when
     * one classpath is used for all source folders. Also user's customization
     * of project.xml can result in other combinations and they cannot be
     * clobbered by opening such a project in UI.
     */
    public List<CompilationUnitKey> createCompilationUnitKeys() {
        // XXX: cache result of this method?
        List<CompilationUnitKey> l = new ArrayList<CompilationUnitKey>();
        for (JavaProjectGenerator.JavaCompilationUnit cu : javaCompilationUnitsList) {
            CompilationUnitKey cul = new CompilationUnitKey();
            cul.locations = cu.packageRoots;
            cul.label = null;
            l.add(cul);
        }
        for (JavaProjectGenerator.SourceFolder sf : sourceFolders) {
            if (!TYPE_JAVA.equals(sf.type)) {
                continue;
            }
            CompilationUnitKey cul = new CompilationUnitKey();
            cul.locations = new ArrayList<String>();
            cul.locations.add(sf.location);
            cul.label = sf.label;
            // try to find corresponding JavaCompilationUnit
            int index = l.indexOf(cul);
            if (index != -1) {
                // use this key intead because it has label
                CompilationUnitKey cul_ = l.get(index);
                cul_.label = sf.label;
                continue;
            }
            // check whether this SourceFolder.location is not part of an existing JavaCompilationUnit
            boolean found = false;
            for (JavaProjectGenerator.JavaCompilationUnit cu_ : javaCompilationUnitsList) {
                if (cu_.packageRoots.contains(sf.location)) {
                    // found: skip it
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            // add this source folder then:
            l.add(cul);
        }
        return l;
    }

    /**
     * Update compilation units to 1:1 or 1:N mapping to source folders.
     * The separateClasspath attribute if true says that each source folder
     * will have its own compilation unit. In opposite case all source
     * folders will have one compilation unit.
     */
    public void updateCompilationUnits(boolean separateClasspath) {
        if (separateClasspath) {
            // This means that there was one compilation unit for all sources.
            // So create compilation unit per source folder.
            String classpath = null;
            List<String> output = null;
            // Copy classpath and output from the first compilation unit
            // to all compilation units - should be easier to customize for user.
            if (javaCompilationUnitsList.size() > 0) {
                List<JavaProjectGenerator.JavaCompilationUnit.CP> classpaths = javaCompilationUnitsList.get(0).classpath;
                if (classpaths != null) {
                    // find first "compile" mode classpath and use it
                    for (JavaProjectGenerator.JavaCompilationUnit.CP cp : classpaths) {
                        if (cp.mode.equals(CLASSPATH_MODE_COMPILE)) {
                            classpath = cp.classpath;
                            break;
                        }
                    }
                }
                output = javaCompilationUnitsList.get(0).output;
            }
            javaCompilationUnitsList.clear();
            for (JavaProjectGenerator.SourceFolder sf : sourceFolders) {
                JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
                cu.packageRoots = new ArrayList<String>();
                cu.packageRoots.add(sf.location);
                if (classpath != null) {
                    JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
                    cp.mode = CLASSPATH_MODE_COMPILE;
                    cp.classpath = classpath;
                    cu.classpath = new ArrayList<JavaProjectGenerator.JavaCompilationUnit.CP>();
                    cu.classpath.add(cp);
                }
                if (output != null) {
                    cu.output = new ArrayList<String>();
                    cu.output.addAll(output);
                }
                cu.sourceLevel = sourceLevel;
                javaCompilationUnitsList.add(cu);
            }
        } else {
            // This means that there are some compilation units which should be
            // merged into one which will be used for all sources.
            List<String> packageRoots = new ArrayList<String>();
            // First list of source roots
            for (JavaProjectGenerator.SourceFolder sf : sourceFolders) {
                packageRoots.add(sf.location);
            }
            // Now try to merge all classpaths and outputs. Might be easier to customize
            Set<String> classpath = new LinkedHashSet<String>();
            Set<String> output = new LinkedHashSet<String>();
            for (JavaProjectGenerator.JavaCompilationUnit cu : javaCompilationUnitsList) {
                if (cu.output != null) {
                    output.addAll(cu.output);
                }
                if (cu.classpath != null) {
                    for (JavaProjectGenerator.JavaCompilationUnit.CP cp : cu.classpath) {
                        if (cp.mode.equals(CLASSPATH_MODE_COMPILE)) {
                            classpath.addAll(Arrays.asList(PropertyUtils.tokenizePath(cp.classpath)));
                        }
                    }
                }
            }
            javaCompilationUnitsList.clear();
            JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
            cu.packageRoots = packageRoots;
            JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
            if (classpath.size() > 0) {
                StringBuffer cp_ = new StringBuffer();
                Iterator<String> it = classpath.iterator();
                while (it.hasNext()) {
                    cp_.append(it.next());
                    if (it.hasNext()) {
                        cp_.append(File.pathSeparatorChar);
                    }
                }
                cp.classpath = cp_.toString();
                cp.mode = CLASSPATH_MODE_COMPILE;
                cu.classpath = new ArrayList<JavaProjectGenerator.JavaCompilationUnit.CP>();
                cu.classpath.add(cp);
            }
            cu.output = new ArrayList<String>(output);
            cu.sourceLevel = sourceLevel;
            javaCompilationUnitsList.add(cu);
        }
        fireChangeEvent();
    }

    /** Retrieve compilation unit or create empty one if it does not exist yet for the given 
     * key which is source package path(s).
     * The isTests is used only to initialize newly created compilation unit.
     */
    public JavaProjectGenerator.JavaCompilationUnit getCompilationUnit(CompilationUnitKey key, boolean isTests) {
        for (JavaProjectGenerator.JavaCompilationUnit cu : javaCompilationUnitsList) {
            if (cu.packageRoots.equals(key.locations)) {
                return cu;
            }
        }
        JavaProjectGenerator.JavaCompilationUnit cu = new JavaProjectGenerator.JavaCompilationUnit();
        cu.packageRoots = key.locations;
        cu.sourceLevel = sourceLevel;
        cu.isTests = isTests;
        javaCompilationUnitsList.add(cu);
        return cu;
    }

    private void removeSourceLocation(String location) {
        Iterator<JavaProjectGenerator.JavaCompilationUnit> it = javaCompilationUnitsList.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.JavaCompilationUnit cu = it.next();
            cu.packageRoots.remove(location);
            if (cu.packageRoots.size() == 0) {
                it.remove();
            }
        }
    }

    /** Update style of loaded source folders of type "java" to packages. */
    private static void updateStyle(List<JavaProjectGenerator.SourceFolder> sources) {
        for (JavaProjectGenerator.SourceFolder sf : sources) {
            assert sf.type.equals(TYPE_JAVA);
            sf.style = JavaProjectNature.STYLE_PACKAGES;
        }
    }
    
    // only for unit testing
    void setSourceFolders(List<JavaProjectGenerator.SourceFolder> list) {
        sourceFolders = list;
    }
    
    // only for unit testing
    List<JavaProjectGenerator.SourceFolder> getSourceFolders() {
        return sourceFolders;
    }
    
    // only for unit testing
    void setJavaCompilationUnits(List<JavaProjectGenerator.JavaCompilationUnit> list) {
        javaCompilationUnitsList = list;
    }
    
    // only for unit testing
    List<JavaProjectGenerator.JavaCompilationUnit> getJavaCompilationUnits() {
        return javaCompilationUnitsList;
    }
    
    
    /**
     * Helper method returning source level of the current platform.
     */
    public static String getDefaultSourceLevel() {
        JavaPlatform platform = JavaPlatform.getDefault();
        SpecificationVersion sv = platform.getSpecification().getVersion();
        if (sv.compareTo(JDK_MAX_SUPPORTED_VERSION)>0) {
            sv = JDK_MAX_SUPPORTED_VERSION;
        }
        return sv.toString();
    }
    
    public boolean isTestSourceFolder(int index) {
        return isTestSourceFolder(getSourceFolder(index));
    }
    
    public boolean isTestSourceFolder(JavaProjectGenerator.SourceFolder sf) {
        for (JavaProjectGenerator.JavaCompilationUnit cu : javaCompilationUnitsList) {
            if (cu.packageRoots.contains(sf.location)) {
                return cu.isTests;
            }
        }
        return false;
    }
    
    public static class CompilationUnitKey {
        public List<String> locations;
        public String label;
        
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof CompilationUnitKey)) {
                return false;
            }
            CompilationUnitKey cul = (CompilationUnitKey)o;
            return this.locations.equals(cul.locations);
        }
        
        public int hashCode() {
            return locations.hashCode()*7;
        }

        public String toString() {
            return "PM.CUK:[label="+label+", locations="+locations+", this="+super.toString()+"]"; // NOI18N
        }
    }
    
}
