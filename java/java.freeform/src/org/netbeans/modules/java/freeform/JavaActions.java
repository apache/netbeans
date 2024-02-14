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

package org.netbeans.modules.java.freeform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.jdkselection.JdkConfiguration;
import org.netbeans.modules.java.freeform.ui.ProjectModel;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles providing implementations of some Java-oriented IDE-specific actions.
 * @see "issue #46886"
 */
final class JavaActions implements ActionProvider {
    
    /* Too problematic for importing <classpath> from existing <java>, since Ant would want NS on that too (oddly):
    private static final String NS_JPDA = "antlib:org.netbeans.modules.debugger.jpda.ant"; // NOI18N
     */

    static final String JAVA_FILE_PATTERN = "\\.java$";

    private static final String[] ACTIONS = {
        ActionProvider.COMMAND_COMPILE_SINGLE,
        ActionProvider.COMMAND_DEBUG,
        ActionProvider.COMMAND_PROFILE,
        ActionProvider.COMMAND_RUN_SINGLE,
        ActionProvider.COMMAND_DEBUG_SINGLE,
        ActionProvider.COMMAND_PROFILE_SINGLE
        // XXX more
    };
    
    /**
     * Script to hold file-sensitive generated targets like compile.single.
     * (Or for generated targets for debug which cannot reuse any existing target body.)
     * These pick up at least project.dir from project.xml and the entire
     * target body is fixed by the IDE, except for some strings determined
     * by information from project.xml like the classpath. The basedir
     * is set to the project directory so that properties match their
     * semantics in project.xml.
     */
    static final String FILE_SCRIPT_PATH = "nbproject/ide-file-targets.xml"; // NOI18N
    /**
     * Script to hold non-file-sensitive generated targets like debug.
     * These import the original build script and share its basedir, so that
     * properties match the semantics of build.xml.
     */
    static final String GENERAL_SCRIPT_PATH = "nbproject/ide-targets.xml"; // NOI18N
    
    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final AuxiliaryConfiguration aux;
    private boolean setOutputsNotified;
    
    public JavaActions(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, AuxiliaryConfiguration aux) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
    }

    public String[] getSupportedActions() {
        return ACTIONS;
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (command.equals(ActionProvider.COMMAND_COMPILE_SINGLE)) {
            return findPackageRoot(context) != null;
        } else if (command.equals(ActionProvider.COMMAND_DEBUG)) {
            return true;
        } else if (command.equals(ActionProvider.COMMAND_PROFILE)) {
            return true;
        } else if (command.equals(ActionProvider.COMMAND_RUN_SINGLE)) {
            return (findPackageRoot(context) != null) && isSingleJavaFileSelected(context);
        } else if (command.equals(ActionProvider.COMMAND_DEBUG_SINGLE)) {
            return (findPackageRoot(context) != null) && isSingleJavaFileSelected(context);
        } else if (command.equals(ActionProvider.COMMAND_PROFILE_SINGLE)) {
            return (findPackageRoot(context) != null) && isSingleJavaFileSelected(context);
        } else {
            throw new IllegalArgumentException(command);
        }
    }

    public void invokeAction(final String command, final Lookup context) throws IllegalArgumentException {
        try {
            project.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    try {
                        if (command.equals(ActionProvider.COMMAND_COMPILE_SINGLE)) {
                            handleCompileSingle(context);
                        } else if (command.equals(ActionProvider.COMMAND_DEBUG)) {
                            handleDebug();
                        } else if (command.equals(ActionProvider.COMMAND_PROFILE)) {
                            handleProfile();
                        } else if (command.equals(ActionProvider.COMMAND_RUN_SINGLE)) {
                            handleRunSingle(context);
                        } else if (command.equals(ActionProvider.COMMAND_DEBUG_SINGLE)) {
                            handleDebugSingle(context);
                        } else if (command.equals(ActionProvider.COMMAND_PROFILE_SINGLE)) {
                            handleProfileSingle(context);
                        } else {
                            throw new IllegalArgumentException(command);
                        }
                    } catch (SAXException e) {
                        throw (IOException) new IOException(e.toString()).initCause(e);
                    }
                }
            });
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    /**
     * Display an alert asking the user whether to really generate a target.
     * @param commandDisplayName the display name of the action to be bound
     * @param scriptPath the path that to the script that will be generated or written to
     * @return true if IDE should proceed
     */
    private boolean alert(String commandDisplayName, String scriptPath) {
        String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        String title = NbBundle.getMessage(JavaActions.class, "TITLE_generate_target_dialog", commandDisplayName, projectDisplayName);
        String body = NbBundle.getMessage(JavaActions.class, "TEXT_generate_target_dialog", commandDisplayName, scriptPath);
        NotifyDescriptor d = new NotifyDescriptor.Message(body, NotifyDescriptor.QUESTION_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        JButton generate = new JButton(NbBundle.getMessage(JavaActions.class, "LBL_generate"));
        generate.setDefaultCapable(true);
        d.setOptions(new Object[] {generate, NotifyDescriptor.CANCEL_OPTION});
        return DialogDisplayer.getDefault().notify(d) == generate;
    }
    
    /**
     * Warns the user about missing project outputs setting
     * @param commandDisplayName the display name of the action to be bound
     */
    private boolean alertOutputs (String commandDisplayName) {
        JButton setOutputOption = new JButton (NbBundle.getMessage(JavaActions.class,"CTL_SetOutput"));
        setOutputOption.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(JavaActions.class,"AD_SetOutput"));
        setOutputOption.setDefaultCapable(true);
        String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        String title = NbBundle.getMessage(JavaActions.class, "TITLE_set_outputs_dialog", commandDisplayName, projectDisplayName);
        String body = NbBundle.getMessage(JavaActions.class,"TEXT_set_outputs_dialog");
        NotifyDescriptor d = new NotifyDescriptor.Message (body, NotifyDescriptor.QUESTION_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);        
        d.setOptions(new Object[] {setOutputOption, NotifyDescriptor.CANCEL_OPTION});
        if (DialogDisplayer.getDefault().notify (d) == setOutputOption) {
            CustomizerProvider customizerProvider = project.getLookup().lookup(CustomizerProvider.class);
            assert customizerProvider != null;
            customizerProvider.showCustomizer();
            return true;
        }
        return false;
    }

    /**
     * Implementation of Compile File.
     */
    private void handleCompileSingle(Lookup context) throws IOException, SAXException {
        // XXX could also try copy + mod from build.xml? but less likely to have <compile> in an accessible place...
        if (!alert(NbBundle.getMessage(JavaActions.class, "ACTION_compile.single"), FILE_SCRIPT_PATH)) {
            return;
        }
        Document doc = readCustomScript(FILE_SCRIPT_PATH);
        ensurePropertiesCopied(doc.getDocumentElement());
        Comment comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_edit_target") + " ");
        doc.getDocumentElement().appendChild(comm);
        comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_more_info_x.single") + " ");
        doc.getDocumentElement().appendChild(comm);
        String propertyName = "files"; // NOI18N
        AntLocation root = findPackageRoot(context);
        assert root != null : context;
        Element target = createCompileSingleTarget(doc, context, propertyName, root);
        doc.getDocumentElement().appendChild(target);
        writeCustomScript(doc, FILE_SCRIPT_PATH);
        // XXX #53622: support also folders (i.e. just files w/o ext??):
        String targetName = target.getAttribute("name");
        addBinding(ActionProvider.COMMAND_COMPILE_SINGLE, FILE_SCRIPT_PATH, targetName, propertyName, root.virtual, JAVA_FILE_PATTERN, "relative-path", ","); // NOI18N
        jumpToBinding(ActionProvider.COMMAND_COMPILE_SINGLE);
        jumpToBuildScript(FILE_SCRIPT_PATH, targetName);
    }
    
    Element createCompileSingleTarget(Document doc, Lookup context, String propertyName, AntLocation root) {
        String targetName = "compile-selected-files-in-" + root.physical.getNameExt(); // NOI18N
        // XXX do a uniquification check
        Element target = doc.createElement("target"); // NOI18N
        addJdkInitDeps(target);
        target.setAttribute("name", targetName); // NOI18N
        Element fail = doc.createElement("fail"); // NOI18N
        fail.setAttribute("unless", propertyName); // NOI18N
        fail.appendChild(doc.createTextNode(NbBundle.getMessage(JavaActions.class, "COMMENT_must_set_property", propertyName)));
        target.appendChild(fail);
        String classesDir = findClassesOutputDir(root.virtual);
        if (classesDir == null) {
            target.appendChild(doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_must_set_build_classes_dir") + " "));
            classesDir = "${build.classes.dir}"; // NOI18N
        }
        Element mkdir = doc.createElement("mkdir"); // NOI18N
        mkdir.setAttribute("dir", classesDir); // NOI18N
        target.appendChild(mkdir);
        Element javac = doc.createElement("javac"); // NOI18N
        javac.setAttribute("srcdir", root.virtual); // NOI18N
        javac.setAttribute("destdir", classesDir); // NOI18N
        javac.setAttribute("includes", "${" + propertyName + "}"); // NOI18N
        String sourceLevel = findSourceLevel(root.virtual);
        if (sourceLevel != null) {
            javac.setAttribute("source", sourceLevel); // NOI18N
        }
        String cp = findCUClasspath(root.virtual, "compile");
        if (cp != null) {
            Element classpath = doc.createElement("classpath"); // NOI18N
            classpath.setAttribute("path", cp); // NOI18N
            javac.appendChild(classpath);
        }
        target.appendChild(javac);
        return target;
    }
    
    private void handleDebug() throws IOException, SAXException {                        
        if (!this.setOutputsNotified) {
            ProjectModel pm = ProjectModel.createModel(Util.getProjectLocation(this.helper, this.evaluator),
                FileUtil.toFile(project.getProjectDirectory()), this.evaluator, this.helper);        
            List<ProjectModel.CompilationUnitKey> cuKeys = pm.createCompilationUnitKeys();
            assert cuKeys != null;
            boolean hasOutputs = false;
            for (ProjectModel.CompilationUnitKey ck : cuKeys) {
                JavaProjectGenerator.JavaCompilationUnit cu = pm.getCompilationUnit(ck,false);
                if (cu.output != null && cu.output.size()>0) {
                    hasOutputs = true;
                    break;
                }
            }
            if (!hasOutputs) {
                alertOutputs (NbBundle.getMessage(JavaActions.class, "ACTION_debug"));            
                this.setOutputsNotified = true;
                return;
            }
        }        
        String[] bindings = findCommandBinding(ActionProvider.COMMAND_RUN);
        Element task = null;
        Element origTarget = null;
        if (bindings != null && bindings.length <= 2) {
            origTarget = findExistingBuildTarget(ActionProvider.COMMAND_RUN);
            //The origTarget may be null if the user has removed it from build.xml
            if (origTarget != null) {
                task = targetUsesTaskExactlyOnce(origTarget, "java"); // NOI18N
            }
        }
        
        if (!alert(NbBundle.getMessage(JavaActions.class, "ACTION_debug"), task != null ? GENERAL_SCRIPT_PATH : FILE_SCRIPT_PATH)) {
            return;
        }
        
        String generatedTargetName = "debug-nb"; // NOI18N
        String generatedScriptPath;
        Document doc;
        Element generatedTarget;
        if (task != null) {
            // We can copy the original run target with some modifications.
            generatedScriptPath = GENERAL_SCRIPT_PATH;
            doc = readCustomScript(GENERAL_SCRIPT_PATH);
            ensureImports(doc.getDocumentElement(), bindings[0]);
            generatedTarget = createDebugTargetFromTemplate(generatedTargetName, origTarget, task, doc);
        } else {
            // No info, need to generate a dummy debug target.
            generatedScriptPath = FILE_SCRIPT_PATH;
            doc = readCustomScript(FILE_SCRIPT_PATH);
            ensurePropertiesCopied(doc.getDocumentElement());
            generatedTarget = createDebugTargetFromScratch(generatedTargetName, doc);
        }
        Comment comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_edit_target") + " ");
        doc.getDocumentElement().appendChild(comm);
        comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_more_info_debug") + " ");
        doc.getDocumentElement().appendChild(comm);
        doc.getDocumentElement().appendChild(generatedTarget);
        writeCustomScript(doc, generatedScriptPath);
        addBinding(ActionProvider.COMMAND_DEBUG, generatedScriptPath, generatedTargetName, null, null, null, null, null);
        jumpToBinding(ActionProvider.COMMAND_DEBUG);
        jumpToBuildScript(generatedScriptPath, generatedTargetName);                
    }
    
    private Element createNbjpdastart(Document ownerDocument) {
        Element nbjpdastart = ownerDocument.createElement("nbjpdastart"); // NOI18N
        nbjpdastart.setAttribute("name", ProjectUtils.getInformation(project).getDisplayName()); // NOI18N
        nbjpdastart.setAttribute("addressproperty", "jpda.address"); // NOI18N
        nbjpdastart.setAttribute("transport", "dt_socket"); // NOI18N
        return nbjpdastart;
    }
    
    private static final String[] DEBUG_VM_ARGS = {
        "-agentlib:jdwp=transport=dt_socket,address=${jpda.address}", // NOI18N
    };
    private void addDebugVMArgs(Element java, Document ownerDocument) {
        //Add fork="true" if not alredy there
        NamedNodeMap attrs = java.getAttributes();
        boolean found = false;
        for (int i=0; i<attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            if ("fork".equals(attr.getName())) {        //NOI18N
                String value = attr.getValue();
                if ("on".equalsIgnoreCase (value) ||    //NOI18N
                    "true".equalsIgnoreCase(value) ||   //NOI18N
                    "yes".equalsIgnoreCase(value)) {    //NOI18N
                    found = true;
                }
                break;
            }
        }
        if (!found) {
            java.setAttribute("fork", "true");  //NOI18N
        }
        for (int i = 0; i < DEBUG_VM_ARGS.length; i++) {
            Element jvmarg = ownerDocument.createElement("jvmarg"); // NOI18N
            jvmarg.setAttribute("value", DEBUG_VM_ARGS[i]); // NOI18N
            java.appendChild(jvmarg);
        }
    }
    
    Element createDebugTargetFromTemplate(String generatedTargetName, Element origTarget, Element origTask, Document ownerDocument) {
        NodeList tasks = origTarget.getChildNodes();
        int taskIndex = -1;
        for (int i = 0; i < tasks.getLength(); i++) {
            if (tasks.item(i) == origTask) {
                taskIndex = i;
                break;
            }
        }
        assert taskIndex != -1;
        Element target = (Element) ownerDocument.importNode(origTarget, true);
        addJdkInitDeps(target);
        Element task = (Element) target.getChildNodes().item(taskIndex);
        target.setAttribute("name", generatedTargetName); // NOI18N
        Element nbjpdastart = createNbjpdastart(ownerDocument);
        String textualCp = task.getAttribute("classpath"); // NOI18N
        if (textualCp.length() > 0) {
            Element classpath = ownerDocument.createElement("classpath"); // NOI18N
            classpath.setAttribute("path", textualCp); // NOI18N
            nbjpdastart.appendChild(classpath);
        } else {
            NodeList origClasspath = task.getElementsByTagName("classpath"); // NOI18N
            if (origClasspath.getLength() == 1) {
                Element classpath = (Element) ownerDocument.importNode(origClasspath.item(0), true);
                nbjpdastart.appendChild(classpath);
            }
        }
        target.insertBefore(nbjpdastart, task);
        addDebugVMArgs(task, ownerDocument);
        return target;
    }
    
    Element createDebugTargetFromScratch(String generatedTargetName, Document ownerDocument) {
        Element target = ownerDocument.createElement("target");
        addJdkInitDeps(target);
        target.setAttribute("name", generatedTargetName); // NOI18N
        Element path = ownerDocument.createElement("path"); // NOI18N
        // XXX would be better to determine runtime CP from project.xml and put it here instead (if that is possible)...
        path.setAttribute("id", "cp"); // NOI18N
        path.appendChild(ownerDocument.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_set_runtime_cp") + " "));
        target.appendChild(path);
        Element nbjpdastart = createNbjpdastart(ownerDocument);
        Element classpath = ownerDocument.createElement("classpath"); // NOI18N
        classpath.setAttribute("refid", "cp"); // NOI18N
        nbjpdastart.appendChild(classpath);
        target.appendChild(nbjpdastart);
        target.appendChild(ownerDocument.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_set_main_class") + " "));
        Element java = ownerDocument.createElement("java"); // NOI18N
        java.setAttribute("classname", "some.main.Class"); // NOI18N
        classpath = ownerDocument.createElement("classpath"); // NOI18N
        classpath.setAttribute("refid", "cp"); // NOI18N
        java.appendChild(classpath);
        addDebugVMArgs(java, ownerDocument);
        target.appendChild(java);
        return target;
    }
    
    private void handleProfile() throws IOException, SAXException {                        
        if (!this.setOutputsNotified) {
            ProjectModel pm = ProjectModel.createModel(Util.getProjectLocation(this.helper, this.evaluator),
                FileUtil.toFile(project.getProjectDirectory()), this.evaluator, this.helper);        
            List<ProjectModel.CompilationUnitKey> cuKeys = pm.createCompilationUnitKeys();
            assert cuKeys != null;
            boolean hasOutputs = false;
            for (ProjectModel.CompilationUnitKey ck : cuKeys) {
                JavaProjectGenerator.JavaCompilationUnit cu = pm.getCompilationUnit(ck,false);
                if (cu.output != null && cu.output.size()>0) {
                    hasOutputs = true;
                    break;
                }
            }
            if (!hasOutputs) {
                alertOutputs (NbBundle.getMessage(JavaActions.class, "ACTION_profile")); // NOI18N           
                this.setOutputsNotified = true;
                return;
            }
        }        
        String[] bindings = findCommandBinding(ActionProvider.COMMAND_RUN);
        Element task = null;
        Element origTarget = null;
        if (bindings != null && bindings.length <= 2) {
            origTarget = findExistingBuildTarget(ActionProvider.COMMAND_RUN);
            //The origTarget may be null if the user has removed it from build.xml
            if (origTarget != null) {
                task = targetUsesTaskExactlyOnce(origTarget, "java"); // NOI18N
            }
        }
        
        if (!alert(NbBundle.getMessage(JavaActions.class, "ACTION_profile"), task != null ? GENERAL_SCRIPT_PATH : FILE_SCRIPT_PATH)) { // NOI18N
            return;
        }
        
        String generatedTargetName = "profile-nb"; // NOI18N
        String generatedScriptPath;
        Document doc;
        Element generatedTarget;
        if (task != null) {
            // We can copy the original run target with some modifications.
            generatedScriptPath = GENERAL_SCRIPT_PATH;
            doc = readCustomScript(GENERAL_SCRIPT_PATH);
            ensureImports(doc.getDocumentElement(), bindings[0]);
            generatedTarget = createProfileTargetFromTemplate(generatedTargetName, origTarget, task, doc);
        } else {
            // No info, need to generate a dummy profile target.
            generatedScriptPath = FILE_SCRIPT_PATH;
            doc = readCustomScript(FILE_SCRIPT_PATH);
            ensurePropertiesCopied(doc.getDocumentElement());
            generatedTarget = createProfileTargetFromScratch(generatedTargetName, doc);
        }
        Comment comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_edit_target") + " "); // NOI18N
        doc.getDocumentElement().appendChild(comm);
        comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_more_info_profile") + " "); // NOI18N
        doc.getDocumentElement().appendChild(comm);
        doc.getDocumentElement().appendChild(generatedTarget);
        writeCustomScript(doc, generatedScriptPath);
        addBinding(ActionProvider.COMMAND_PROFILE, generatedScriptPath, generatedTargetName, null, null, null, null, null);
        jumpToBinding(ActionProvider.COMMAND_PROFILE);
        jumpToBuildScript(generatedScriptPath, generatedTargetName);                
    }
    
    Element createProfileTargetFromTemplate(String generatedTargetName, Element origTarget, Element origTask, Document ownerDocument) {
        NodeList tasks = origTarget.getChildNodes();
        int taskIndex = -1;
        for (int i = 0; i < tasks.getLength(); i++) {
            if (tasks.item(i) == origTask) {
                taskIndex = i;
                break;
            }
        }
        assert taskIndex != -1;
        Element target = (Element) ownerDocument.importNode(origTarget, true);
        target.setAttribute("depends", "-profile-check"); // NOI18N
        target.setAttribute("if", "profiler.configured"); // NOI18N
        addJdkInitDeps(target);
        Element task = (Element) target.getChildNodes().item(taskIndex);
        target.setAttribute("name", generatedTargetName); // NOI18N
        addProfileInit(ownerDocument, ownerDocument.getDocumentElement());

        addProfileVMArgs(task, ownerDocument);
        return target;
    }
    
    Element createProfileTargetFromScratch(String generatedTargetName, Document ownerDocument) {
        Element target = ownerDocument.createElement("target"); // NOI18N
        target.setAttribute("depends", "-profile-check"); // NOI18N        
        target.setAttribute("if", "profiler.configured"); // NOI18N
        addJdkInitDeps(target);
        target.setAttribute("name", generatedTargetName); // NOI18N
        Element path = ownerDocument.createElement("path"); // NOI18N
        // XXX would be better to determine runtime CP from project.xml and put it here instead (if that is possible)...
        path.setAttribute("id", "cp"); // NOI18N
        path.appendChild(ownerDocument.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_set_runtime_cp") + " ")); // NOI18N
        target.appendChild(path);
        addProfileInit(ownerDocument, ownerDocument.getDocumentElement());

        target.appendChild(ownerDocument.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_set_main_class") + " ")); // NOI18N
        Element java = ownerDocument.createElement("java"); // NOI18N
        java.setAttribute("classname", "some.main.Class"); // NOI18N
        Element classpath = ownerDocument.createElement("classpath"); // NOI18N
        classpath.setAttribute("refid", "cp"); // NOI18N
        java.appendChild(classpath);
        addProfileVMArgs(java, ownerDocument);
        target.appendChild(java);
        return target;
    }
    
    private void addProfileInit(Document ownerDocument, Element parent) {
        NodeList nl = ownerDocument.getElementsByTagName("target");
        for(int i=0;i<nl.getLength();i++) {
            if ("-profile-check".equals(((Element)nl.item(i)).getAttribute("name"))) {
                return;
            }
        }
        Element init = ownerDocument.createElement("target"); // NOI18N
        init.setAttribute("name", "-profile-check");
        Element profilerStart = ownerDocument.createElement("startprofiler");
        profilerStart.setAttribute("freeform", "true");
        init.appendChild(profilerStart);
        
        parent.appendChild(init);
    }
    
    private void addProfileVMArgs(Element java, Document ownerDocument) {
        //Add fork="true" if not alredy there
        NamedNodeMap attrs = java.getAttributes();
        boolean found = false;
        for (int i=0; i<attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            if ("fork".equals(attr.getName())) {        //NOI18N
                String value = attr.getValue();
                if ("on".equalsIgnoreCase (value) ||    //NOI18N
                    "true".equalsIgnoreCase(value) ||   //NOI18N
                    "yes".equalsIgnoreCase(value)) {    //NOI18N
                    found = true;
                }
                break;
            }
        }
        if (!found) {
            java.setAttribute("fork", "true");  //NOI18N
        }
        Element jvmarg = ownerDocument.createElement("jvmarg"); // NOI18N
        jvmarg.setAttribute("line", "${agent.jvmargs}"); // NOI18N
        java.appendChild(jvmarg);
    }
    
    /**
     * Read a generated script if it exists, else create a skeleton.
     * Imports jdk.xml if appropriate.
     * @param scriptPath e.g. {@link #FILE_SCRIPT_PATH} or {@link #GENERAL_SCRIPT_PATH}
     */
    Document readCustomScript(String scriptPath) throws IOException, SAXException {
        // XXX if there is TAX support for rewriting XML files, use that here...
        FileObject script = helper.getProjectDirectory().getFileObject(scriptPath);
        Document doc;
        if (script != null) {
            InputStream is = script.getInputStream();
            try {
                doc = XMLUtil.parse(new InputSource(is), false, true, null, null);
            } finally {
                is.close();
            }
        } else {
            doc = XMLUtil.createDocument("project", /*XXX:"antlib:org.apache.tools.ant"*/null, null, null); // NOI18N
            Element root = doc.getDocumentElement();
            String projname = ProjectUtils.getInformation(project).getDisplayName();
            root.setAttribute("name", NbBundle.getMessage(JavaActions.class, "LBL_generated_script_name", projname));
        }
        if (helper.getProjectDirectory().getFileObject(JdkConfiguration.JDK_XML) != null) {
            JdkConfiguration.insertJdkXmlImport(doc);
        }
        return doc;
    }
    
    /**
     * Make sure that if the project defines ${project.dir} in project.xml that
     * a custom build script also defines this property.
     * Generally, copy any properties defined in project.xml to Ant syntax.
     * Used for generated targets which essentially copy Ant fragments from project.xml
     * (rather than the user's build.xml).
     * Also sets the basedir to the (IDE) project directory.
     * Idempotent, takes effect only once.
     * Use with {@link #FILE_SCRIPT_PATH}.
     * @param antProject XML of an Ant project (document element)
     */
    void ensurePropertiesCopied(Element antProject) {
        if (antProject.getAttribute("basedir").length() > 0) {
            // Do not do it twice to the same script.
            return;
        }
        antProject.setAttribute("basedir", /* ".." times count('/', FILE_SCRIPT_PATH) */".."); // NOI18N
        // Look for <properties> in project.xml and make corresponding definitions in the Ant script.
        Element data = Util.getPrimaryConfigurationData(helper);
        Element properties = XMLUtil.findElement(data, "properties", Util.NAMESPACE);
        if (properties != null) {
            for (Element el : XMLUtil.findSubElements(properties)) {
                Element nue = antProject.getOwnerDocument().createElement("property"); // NOI18N
                if (el.getLocalName().equals("property")) { // NOI18N
                    String name = el.getAttribute("name"); // NOI18N
                    assert name != null;
                    String text = XMLUtil.findText(el);
                    assert text != null;
                    nue.setAttribute("name", name);
                    nue.setAttribute("value", text);
                } else if (el.getLocalName().equals("property-file")) { // NOI18N
                    String text = XMLUtil.findText(el);
                    assert text != null;
                    nue.setAttribute("file", text);
                } else {
                    assert false : el;
                }
                antProject.appendChild(nue);
            }
        }
    }
    
    /**
     * Make sure that the custom build script imports the original build script
     * and is using the same base dir.
     * Used for generated targets which essentially copy Ant targets from build.xml.
     * Use with {@link #GENERAL_SCRIPT_PATH}.
     * Idempotent, takes effect only once.
     * @param antProject XML of an Ant project (document element)
     * @param origScriptPath Ant name of original build script's path
     */
    void ensureImports(Element antProject, String origScriptPath) throws IOException, SAXException {
        if (antProject.getAttribute("basedir").length() > 0) {
            // Do not do it twice to the same script.
            return;
        }
        String origScriptPathEval = evaluator.evaluate(origScriptPath);
        if (origScriptPathEval == null) {
            // Can't do anything, forget it.
            return;
        }
        String origScriptURI = Utilities.toURI(helper.resolveFile(origScriptPathEval)).toString();
        Document origScriptDocument = XMLUtil.parse(new InputSource(origScriptURI), false, true, null, null);
        String origBasedir = origScriptDocument.getDocumentElement().getAttribute("basedir"); // NOI18N
        if (origBasedir.length() == 0) {
            origBasedir = "."; // NOI18N
        }
        String basedir, importPath;
        File origScript = new File(origScriptPathEval);
        if (origScript.isAbsolute()) {
            // Use full path.
            importPath = origScriptPathEval;
            if (new File(origBasedir).isAbsolute()) {
                basedir = origBasedir;
            } else {
                basedir = PropertyUtils.resolveFile(origScript.getParentFile(), origBasedir).getAbsolutePath();
            }
        } else {
            // Import relative to that path.
            // Note that <import>'s path is always relative to the location of the importing script, regardless of the basedir.
            String prefix = /* ".." times count('/', FILE_SCRIPT_PATH) */"../"; // NOI18N
            importPath = prefix + origScriptPathEval;
            if (new File(origBasedir).isAbsolute()) {
                basedir = origBasedir;
            } else {
                int slash = origScriptPathEval.replace(File.separatorChar, '/').lastIndexOf('/');
                if (slash == -1) {
                    basedir = prefix + origBasedir;
                } else {
                    basedir = prefix + origScriptPathEval.substring(0, slash + 1) + origBasedir;
                }
                // Trim:
                basedir = basedir.replaceAll("/\\.$", ""); // NOI18N
            }
        }
        antProject.setAttribute("basedir", basedir); // NOI18N
        Element importEl = antProject.getOwnerDocument().createElement("import"); // NOI18N
        importEl.setAttribute("file", importPath); // NOI18N
        antProject.appendChild(importEl);
    }
    
    /**
     * Write a script with a new or modified document.
     * @param scriptPath e.g. {@link #FILE_SCRIPT_PATH} or {@link #GENERAL_SCRIPT_PATH}
     */
    void writeCustomScript(Document doc, String scriptPath) throws IOException {
        FileObject script = helper.getProjectDirectory().getFileObject(scriptPath);
        if (script == null) {
            script = FileUtil.createData(helper.getProjectDirectory(), scriptPath);
        }
        FileLock lock = script.lock();
        try {
            OutputStream os = script.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    private List<Element> compilationUnits() {
        Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
        if (java == null) {
            return Collections.emptyList();
        }
        return XMLUtil.findSubElements(java);
    }
    
    /**
     * Find a Java package root in which the selection is contained.
     * @param context lookup with Java source files and/or folders and/or junk
     * @return the package root if there is one, or null if the lookup is empty, has junk, or has multiple roots
     */
    AntLocation findPackageRoot(Lookup context) {
        for (Element compilationUnitEl : compilationUnits()) {
            assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
            List<String> packageRootNames = Classpaths.findPackageRootNames(compilationUnitEl);
            Map<String,FileObject> packageRootsByName = Classpaths.findPackageRootsByName(helper, evaluator, packageRootNames);
            for (Map.Entry<String,FileObject> entry : packageRootsByName.entrySet()) {
                FileObject root = entry.getValue();
                if (containsSelectedJavaSources(root, context)) {
                    return new AntLocation(entry.getKey(), root);
                }
            }
        }
        // Couldn't find it.
        return null;
    }
    
    /**
     * Check to see if a (node-like) selection contains one or more Java sources (or folders) inside the root.
     */
    static boolean containsSelectedJavaSources(FileObject root, Lookup context) {
        Set<FileObject> selection = new HashSet<FileObject>();
        for (DataObject dob : context.lookupAll(DataObject.class)) {
            selection.add(dob.getPrimaryFile());
        }
        if (selection.isEmpty()) {
            return false;
        }
        for (FileObject f : selection) {
            if (f.isData() && !f.hasExt("java")) { // NOI18N
                return false;
            }
            if (f != root && !FileUtil.isParentOf(root, f)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Represents a location as referred to by the Ant script.
     * Contains both its logical representation, and actual (current) location (or null).
     */
    static final class AntLocation {
        public final String virtual;
        public final FileObject physical;
        public AntLocation(String virtual, FileObject physical) {
            this.virtual = virtual;
            this.physical = physical;
        }
        public String toString() {
            return "AntLocation[" + virtual + "=" + physical + "]"; // NOI18N
        }
    }
    
    /**
     * Try to find the compilation unit containing a source root.
     * @param sources a source root in the project (as a virtual Ant name)
     * @return the compilation unit owning it, or null if not found
     */
    private Element findCompilationUnit(String sources) {
        for (Element compilationUnitEl : compilationUnits()) {
            for (Element packageRoot : XMLUtil.findSubElements(compilationUnitEl)) {
                if (packageRoot.getLocalName().equals("package-root")) { // NOI18N
                    if (XMLUtil.findText(packageRoot).equals(sources)) {
                        return compilationUnitEl;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Try to determine where classes from a given package root should be compiled to.
     * @param sources a source root in the project (as a virtual Ant name)
     * @return an output directory (never a JAR), as a virtual name, or null if none could be found
     */
    String findClassesOutputDir(String sources) {
        Element compilationUnitEl = findCompilationUnit(sources);
        if (compilationUnitEl != null) {
            return findClassesOutputDir(compilationUnitEl);
        } else {
            return null;
        }
    }
    
    /**
     * Find output classes given a compilation unit from project.xml.
     */
    private String findClassesOutputDir(Element compilationUnitEl) {
        // Look for an appropriate <built-to>.
        for (Element builtTo : XMLUtil.findSubElements(compilationUnitEl)) {
            if (builtTo.getLocalName().equals("built-to")) { // NOI18N
                String rawtext = XMLUtil.findText(builtTo);
                // Check that it is not an archive.
                String evaltext = evaluator.evaluate(rawtext);
                if (evaltext != null) {
                    File dest = helper.resolveFile(evaltext);
                    URL destU;
                    try {
                        destU = Utilities.toURI(dest).toURL();
                    } catch (MalformedURLException e) {
                        throw new AssertionError(e);
                    }
                    if (!FileUtil.isArchiveFile(destU)) {
                        // OK, dir, take it.
                        return rawtext;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Try to find the source level corresponding to a source root.
     * @param sources a source root in the project (as a virtual Ant name)
     * @return the source level, or null if none was specified or there was no such source root
     */
    String findSourceLevel(String sources) {
        Element compilationUnitEl = findCompilationUnit(sources);
        if (compilationUnitEl != null) {
            Element sourceLevel = XMLUtil.findElement(compilationUnitEl, "source-level", JavaProjectNature.NS_JAVA_LASTEST);
            if (sourceLevel != null) {
                return XMLUtil.findText(sourceLevel);
            }
        }
        return null;
    }
    
    /**
     * Try to find the compile-time classpath corresponding to a source root.
     * @param sources a source root in the project (as a virtual Ant name)
     * @return the classpath (in Ant form), or null if none was specified or there was no such source root
     */
    String findCUClasspath(String sources, String moud) {
        Element compilationUnitEl = findCompilationUnit(sources);
        if (compilationUnitEl != null) {
            for (Element classpath : XMLUtil.findSubElements(compilationUnitEl)) {
                if (classpath.getLocalName().equals("classpath")) { // NOI18N
                    String mode = classpath.getAttribute("mode"); // NOI18N
                    if (mode.equals(moud)) {
                        return XMLUtil.findText(classpath);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds all build-to elements in project.xml and returns their content as array
     * @param srcRoot source root in the project
     * @return array with content of all <built-to> elements built by CU containing the srcRoot
     */
    private String[] findCUOutputs(String srcRoot) {
        List<String> outputs = new ArrayList<String>();
        Element cuElem = findCompilationUnit(srcRoot);
        if (cuElem != null) {
            NodeList builts = cuElem.getElementsByTagName("built-to"); // NOI18N
            for (int i = 0; i < builts.getLength(); i++) {
                outputs.add(builts.item(i).getTextContent());
            }
        }
        return outputs.toArray(new String[0]);
    }

    //The order of the root elements as specified in the schema.
    //Used to add <ide-actions> at the correct place.
    private static final String[] rootElementsOrder = {"name", "properties", "folders", "ide-actions", "export", "view", "subprojects"}; // NOI18N
    
    /**
     * Add an action binding to project.xml.
     * If there is no required context, the action is also added to the context menu of the project node.
     * @param command the command name
     * @param scriptPath the path to the generated script
     * @param target the name of the target (in scriptPath)
     * @param propertyName a property name to hold the selection (or null for no context, in which case remainder should be null)
     * @param dir the raw text to use for the directory name
     * @param pattern the regular expression to match, or null
     * @param format the format to use
     * @param separator the separator to use for multiple files, or null for single file only
     */
    void addBinding(String command, String scriptPath, String target, String propertyName, String dir, String pattern, String format, String separator) throws IOException {
        // XXX cannot use FreeformProjectGenerator since that is currently not a public support SPI from ant/freeform
        // XXX should this try to find an existing binding? probably not, since it is assumed that if there was one, we would never get here to begin with
        Element data = Util.getPrimaryConfigurationData(helper);
        Element ideActions = XMLUtil.findElement(data, "ide-actions", Util.NAMESPACE); // NOI18N
        if (ideActions == null) {
            //fix for #58442:
            ideActions = data.getOwnerDocument().createElementNS(Util.NAMESPACE, "ide-actions"); // NOI18N
            XMLUtil.appendChildElement(data, ideActions, rootElementsOrder);
        }
        Document doc = data.getOwnerDocument();
        Element action = doc.createElementNS(Util.NAMESPACE, "action"); // NOI18N
        action.setAttribute("name", command); // NOI18N
        Element script = doc.createElementNS(Util.NAMESPACE, "script"); // NOI18N
        script.appendChild(doc.createTextNode(scriptPath));
        action.appendChild(script);
        Element targetEl = doc.createElementNS(Util.NAMESPACE, "target"); // NOI18N
        targetEl.appendChild(doc.createTextNode(target));
        action.appendChild(targetEl);
        if (propertyName != null) {
            Element context = doc.createElementNS(Util.NAMESPACE, "context"); // NOI18N
            Element property = doc.createElementNS(Util.NAMESPACE, "property"); // NOI18N
            property.appendChild(doc.createTextNode(propertyName));
            context.appendChild(property);
            Element folder = doc.createElementNS(Util.NAMESPACE, "folder"); // NOI18N
            folder.appendChild(doc.createTextNode(dir));
            context.appendChild(folder);
            if (pattern != null) {
                Element patternEl = doc.createElementNS(Util.NAMESPACE, "pattern"); // NOI18N
                patternEl.appendChild(doc.createTextNode(pattern));
                context.appendChild(patternEl);
            }
            Element formatEl = doc.createElementNS(Util.NAMESPACE, "format"); // NOI18N
            formatEl.appendChild(doc.createTextNode(format));
            context.appendChild(formatEl);
            Element arity = doc.createElementNS(Util.NAMESPACE, "arity"); // NOI18N
            if (separator != null) {
                Element separatorEl = doc.createElementNS(Util.NAMESPACE, "separated-files"); // NOI18N
                separatorEl.appendChild(doc.createTextNode(separator));
                arity.appendChild(separatorEl);
            } else {
                arity.appendChild(doc.createElementNS(Util.NAMESPACE, "one-file-only")); // NOI18N
            }
            context.appendChild(arity);
            action.appendChild(context);
        } else {
            // Add a context menu item, since it applies to the project as a whole.
            // Assume there is already a <context-menu> defined, which is quite likely.
            Element view = XMLUtil.findElement(data, "view", Util.NAMESPACE); // NOI18N
            if (view != null) {
                Element contextMenu = XMLUtil.findElement(view, "context-menu", Util.NAMESPACE); // NOI18N
                if (contextMenu != null) {
                    Element ideAction = doc.createElementNS(Util.NAMESPACE, "ide-action"); // NOI18N
                    ideAction.setAttribute("name", command); // NOI18N
                    contextMenu.appendChild(ideAction);
                }
            }
        }
        ideActions.appendChild(action);
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(project);
    }

    /**
     * Jump to a target in the editor.
     * @param scriptPath the script to open
     * @param target the name of the target (in scriptPath)
     */
    private void jumpToBuildScript(String scriptPath, String target) {
        jumpToFile(scriptPath, target, "target", "name"); // NOI18N
    }
    
    /**
     * Jump to an action binding in the editor.
     * @param command an {@link ActionProvider} command name found in project.xml
     */
    private void jumpToBinding(String command) {
        jumpToFile(AntProjectHelper.PROJECT_XML_PATH, command, "action", "name"); // NOI18N
    }

    /**
     * Jump to some line in an XML file.
     * @param path project-relative path to the file
     * @param match {@see #findLine}
     * @param elementLocalName {@see #findLine}
     * @param elementAttributeName {@see #findLine}
     */
    private void jumpToFile(String path, String match, String elementLocalName, String elementAttributeName) {
        FileObject file = helper.getProjectDirectory().getFileObject(path);
        if (file == null) {
            return;
        }
        int line;
        try {
            line = findLine(file, match, elementLocalName, elementAttributeName);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return;
        }
        if (line == -1) {
            // Just open it.
            line = 0;
        }
        DataObject fileDO;
        try {
            fileDO = DataObject.find(file);
        } catch (DataObjectNotFoundException e) {
            throw new AssertionError(e);
        }
        LineCookie lines = fileDO.getCookie(LineCookie.class);
        if (lines != null) {
            try {
                lines.getLineSet().getCurrent(line).show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
            } catch (IndexOutOfBoundsException e) {
                // XXX reproducibly thrown if the document was already open. Why?? (file.refresh() above does not help.)
                ErrorManager.getDefault().getInstance(JavaActions.class.getName()).log(ErrorManager.WARNING, e + " [file=" + file + " match=" + match + " line=" + line + "]"); // NOI18N
                lines.getLineSet().getCurrent(0).show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
            }
        }
    }
    
    /**
     * Find the line number of a target in an Ant script, or some other line in an XML file.
     * Able to find a certain element with a certain attribute matching a given value.
     * See also AntTargetNode.TargetOpenCookie.
     * @param file an Ant script or other XML file
     * @param match the attribute value to match (e.g. target name)
     * @param elementLocalName the (local) name of the element to look for
     * @param elementAttributeName the name of the attribute to match on
     * @return the line number (0-based), or -1 if not found
     */
    static final int findLine(FileObject file, final String match, final String elementLocalName, final String elementAttributeName) throws IOException, SAXException, ParserConfigurationException {
        InputSource in = new InputSource(file.toURL().toString());
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        final int[] line = new int[] {-1};
        class Handler extends DefaultHandler {
            private Locator locator;
            public void setDocumentLocator(Locator l) {
                locator = l;
            }
            public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                if (line[0] == -1) {
                    if (localname.equals(elementLocalName) && match.equals(attr.getValue(elementAttributeName))) { // NOI18N
                        line[0] = locator.getLineNumber() - 1;
                    }
                }
            }
        }
        parser.parse(in, new Handler());
        return line[0];
    }
    
    /**
     * Attempt to find the Ant build script target bound to a given IDE command.
     * @param command an {@link ActionProvider} command
     * @return the XML for the target if it could be found (and there was no more than one target bound), else null
     */
    Element findExistingBuildTarget(String command) throws IOException, SAXException {
        String[] binding = findCommandBinding(command);
        if (binding == null) {
            return null;
        }
        String scriptName = binding[0];
        assert scriptName != null;
        String targetName;
        if (binding.length == 1) {
            targetName = null;
        } else if (binding.length == 2) {
            targetName = binding[1];
        } else {
            // Too many bindings; we do not support this.
            return null;
        }
        String scriptPath = evaluator.evaluate(scriptName);
        if (scriptPath == null) {
            return null;
        }
        File scriptFile = helper.resolveFile(scriptPath);
        String scriptURI = Utilities.toURI(scriptFile).toString();
        Document doc = XMLUtil.parse(new InputSource(scriptURI), false, true, null, null);
        if (targetName == null) {
            targetName = doc.getDocumentElement().getAttribute("default"); // NOI18N
            if (targetName == null) {
                return null;
            }
        }
        for (Element target : XMLUtil.findSubElements(doc.getDocumentElement())) {
            if (target.getLocalName().equals("target") && targetName.equals(target.getAttribute("name"))) { // NOI18N
                return target;
            }
        }
        return null;
    }

    /**
     * Find the target binding for some command.
     * @param command an {@link ActionProvider} command
     * @return an array of a script name (Ant syntax, never null) and zero or more target names (none means default target)
     *         or null if no binding could be found for this command
     */
    String[] findCommandBinding(String command) {
        Element data = Util.getPrimaryConfigurationData(helper);
        Element ideActions = XMLUtil.findElement(data, "ide-actions", Util.NAMESPACE); // NOI18N
        if (ideActions == null) {
            return null;
        }
        String scriptName = "build.xml"; // NOI18N
        for (Element action : XMLUtil.findSubElements(ideActions)) {
            assert action.getLocalName().equals("action");
            if (action.getAttribute("name").equals(command)) {
                Element script = XMLUtil.findElement(action, "script", Util.NAMESPACE); // NOI18N
                if (script != null) {
                    scriptName = XMLUtil.findText(script);
                }
                List<String> scriptPlusTargetNames = new ArrayList<String>();
                scriptPlusTargetNames.add(scriptName);
                for (Element target : XMLUtil.findSubElements(action)) {
                    if (target.getLocalName().equals("target")) { // NOI18N
                        scriptPlusTargetNames.add(XMLUtil.findText(target));
                    }
                }
                if (scriptName.equals(JdkConfiguration.NBJDK_XML) && scriptPlusTargetNames.size() > 1) {
                    // Try to find the original script instead.
                    FileObject nbjdkFO = helper.getProjectDirectory().getFileObject(JdkConfiguration.NBJDK_XML);
                    if (nbjdkFO != null) {
                        try {
                            Document nbjdk = XMLUtil.parse(new InputSource(nbjdkFO.toURL().toString()), false, false, null, null);
                            NodeList nl = nbjdk.getElementsByTagName("target"); // NOI18N
                            for (int i = 0; i < nl.getLength(); i++) {
                                if (((Element) nl.item(i)).getAttribute("name").equals(scriptPlusTargetNames.get(1))) { // NOI18N
                                    NodeList nl2 = ((Element) nl.item(i)).getElementsByTagName("ant"); // NOI18N
                                    if (nl2.getLength() == 1) {
                                        String antfile = ((Element) nl2.item(0)).getAttribute("antfile"); // NOI18N
                                        if (antfile.length() == 0) {
                                            antfile = "build.xml"; // NOI18N
                                        }
                                        scriptPlusTargetNames.set(0, antfile);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception x) {
                            Exceptions.printStackTrace(x);
                        }
                    }
                }
                return scriptPlusTargetNames.toArray(new String[0]);
            }
        }
        return null;
    }
    
    /**
     * Check to see if a given Ant target uses a given task once (and only once).
     * @param target an Ant <code>&lt;target&gt;</code> element
     * @param taskName the (unqualified) name of an Ant task
     * @return a task element with that name, or null if there is none or more than one
     */
    Element targetUsesTaskExactlyOnce(Element target, String taskName) {
        // XXX should maybe also look for any other usage of the task in the same script in case there is none in the mentioned target
        Element foundTask = null;
        for (Element task : XMLUtil.findSubElements(target)) {
            if (task.getLocalName().equals(taskName)) {
                if (foundTask != null) {
                    // Duplicate.
                    return null;
                } else {
                    foundTask = task;
                }
            }
        }
        return foundTask;
    }

    private void handleRunSingle(Lookup context) throws IOException, SAXException {
        if (!alert(NbBundle.getMessage(JavaActions.class, "ACTION_run.single"), FILE_SCRIPT_PATH)) {
            return;
        }
        Document doc = readCustomScript(FILE_SCRIPT_PATH);
        AntLocation root = handleInitials(doc, context);
        assert root != null : context;
        String propertyName = "run.class"; // NOI18N
        String targetName = "run-selected-file-in-" + root.physical.getNameExt(); // NOI18N
        Element target = createRunSingleTargetElem(doc, targetName, propertyName, root);
        doc.getDocumentElement().appendChild(target);
        writeCustomScript(doc, FILE_SCRIPT_PATH);
        addBinding(ActionProvider.COMMAND_RUN_SINGLE, FILE_SCRIPT_PATH, targetName,
                propertyName, root.virtual, JAVA_FILE_PATTERN, "java-name", null); // NOI18N
        jumpToBinding(ActionProvider.COMMAND_RUN_SINGLE);
        jumpToBuildScript(FILE_SCRIPT_PATH, targetName);
    }

    private void handleDebugSingle(Lookup context) throws IOException, SAXException {
        if (!alert(NbBundle.getMessage(JavaActions.class, "ACTION_debug.single"), FILE_SCRIPT_PATH)) {
            return;
        }
        Document doc = readCustomScript(FILE_SCRIPT_PATH);
        AntLocation root = handleInitials(doc, context);
        assert root != null : context;
        String propertyName = "debug.class"; // NOI18N
        String targetName = "debug-selected-file-in-" + root.physical.getNameExt(); // NOI18N
        Element targetElem = createDebugSingleTargetElem(doc, targetName, propertyName, root);
        doc.getDocumentElement().appendChild(targetElem);
        writeCustomScript(doc, FILE_SCRIPT_PATH);
        addBinding(ActionProvider.COMMAND_DEBUG_SINGLE, FILE_SCRIPT_PATH, targetName,
                propertyName, root.virtual, JAVA_FILE_PATTERN, "java-name", null); // NOI18N
        jumpToBinding(ActionProvider.COMMAND_DEBUG_SINGLE);
        jumpToBuildScript(FILE_SCRIPT_PATH, targetName);
    }
    
    private void handleProfileSingle(Lookup context) throws IOException, SAXException {
        if (!alert(NbBundle.getMessage(JavaActions.class, "ACTION_profile.single"), FILE_SCRIPT_PATH)) { // NOI18N
            return;
        }
        Document doc = readCustomScript(FILE_SCRIPT_PATH);
        AntLocation root = handleInitials(doc, context);
        assert root != null : context;
        String propertyName = "profile.class"; // NOI18N
        String targetName = "profile-selected-file-in-" + root.physical.getNameExt(); // NOI18N
        Element targetElem = createProfileSingleTargetElem(doc, targetName, propertyName, root);
        doc.getDocumentElement().appendChild(targetElem);
        writeCustomScript(doc, FILE_SCRIPT_PATH);
        addBinding(ActionProvider.COMMAND_PROFILE_SINGLE, FILE_SCRIPT_PATH, targetName,
                propertyName, root.virtual, JAVA_FILE_PATTERN, "java-name", null); // NOI18N
        jumpToBinding(ActionProvider.COMMAND_PROFILE_SINGLE);
        jumpToBuildScript(FILE_SCRIPT_PATH, targetName);
    }

    Element createRunSingleTargetElem(Document doc, String tgName,
                String propName, AntLocation root) throws IOException, SAXException {

        Element targetElem = doc.createElement("target"); // NOI18N
        addJdkInitDeps(targetElem);
        targetElem.setAttribute("name", tgName); // NOI18N
        Element failElem = doc.createElement("fail"); // NOI18N
        failElem.setAttribute("unless", propName); // NOI18N
        failElem.appendChild(doc.createTextNode(NbBundle.getMessage(JavaActions.class,
                "COMMENT_must_set_property", propName)));
        targetElem.appendChild(failElem);

        String depends[] = getRunDepends();
        if (depends != null) {
            targetElem.appendChild(createAntElem(doc, depends[0], depends[1]));
        }

        Element javaElem = doc.createElement("java"); // NOI18N
        javaElem.setAttribute("classname", "${" + propName + "}"); // NOI18N
        javaElem.setAttribute("fork", "true"); // NOI18N
        javaElem.setAttribute("failonerror", "true"); // NOI18N

        Element cpElem = getPathFromCU(doc, root.virtual, "classpath");
        // add comment only if there is no definition
        if (cpElem.getChildNodes().getLength() == 0) {
            cpElem.appendChild(doc.createComment(" " + NbBundle.getMessage(JavaActions.class,
                    "COMMENT_set_runtime_cp") + " "));
        }
        javaElem.appendChild(cpElem);
        targetElem.appendChild(javaElem);
        return targetElem;
    }

    Element createDebugSingleTargetElem(Document doc, String tgName,
                String propName, AntLocation root) throws IOException, SAXException {

        Element targetElem = doc.createElement("target"); // NOI18N
        addJdkInitDeps(targetElem);
        targetElem.setAttribute("name", tgName); // NOI18N
        Element failElem = doc.createElement("fail"); // NOI18N
        failElem.setAttribute("unless", propName); // NOI18N
        failElem.appendChild(doc.createTextNode(NbBundle.getMessage(JavaActions.class,
                "COMMENT_must_set_property", propName)));
        targetElem.appendChild(failElem);

        String depends[] = getRunDepends();
        if (depends != null) {
            targetElem.appendChild(createAntElem(doc, depends[0], depends[1]));
        }

        Element pElem = getPathFromCU(doc, root.virtual, "path"); // NOI18N
        pElem.setAttribute("id", "cp"); // NOI18N
        // add comment only if there is no definition
        if (pElem.getChildNodes().getLength() == 0) {
            pElem.appendChild(doc.createComment(" " + NbBundle.getMessage(JavaActions.class,
                    "COMMENT_set_runtime_cp") + " "));
        }
        targetElem.appendChild(pElem);

        Element nbjpdastartElem = createNbjpdastart(doc);
        Element cpElem = doc.createElement("classpath"); // NOI18N
        cpElem.setAttribute("refid", "cp"); // NOI18N
        nbjpdastartElem.appendChild(cpElem);
        targetElem.appendChild(nbjpdastartElem);

        Element javaElem = doc.createElement("java"); // NOI18N
        javaElem.setAttribute("classname", "${" + propName + "}"); // NOI18N

        cpElem = doc.createElement("classpath"); // NOI18N
        cpElem.setAttribute("refid", "cp"); // NOI18N
        javaElem.appendChild(cpElem);
        addDebugVMArgs(javaElem, doc);

        targetElem.appendChild(javaElem);
        return targetElem;
    }
    
    Element createProfileSingleTargetElem(Document doc, String tgName,
                String propName, AntLocation root) throws IOException, SAXException {

        Element targetElem = doc.createElement("target"); // NOI18N
        addJdkInitDeps(targetElem);
        targetElem.setAttribute("name", tgName); // NOI18N
        targetElem.setAttribute("if", "profiler.configured"); // NOI18N
        targetElem.setAttribute("depends", "-profile-check"); // NOI18N
        Element failElem = doc.createElement("fail"); // NOI18N
        failElem.setAttribute("unless", propName); // NOI18N
        failElem.appendChild(doc.createTextNode(NbBundle.getMessage(JavaActions.class,
                "COMMENT_must_set_property", propName)));
        targetElem.appendChild(failElem);

        String depends[] = getRunDepends();
        if (depends != null) {
            targetElem.appendChild(createAntElem(doc, depends[0], depends[1]));
        }

        Element pElem = getPathFromCU(doc, root.virtual, "path"); // NOI18N
        pElem.setAttribute("id", "cp"); // NOI18N
        // add comment only if there is no definition
        if (pElem.getChildNodes().getLength() == 0) {
            pElem.appendChild(doc.createComment(" " + NbBundle.getMessage(JavaActions.class,
                    "COMMENT_set_runtime_cp") + " "));
        }
        targetElem.appendChild(pElem);

        Element cpElem = doc.createElement("classpath"); // NOI18N
        cpElem.setAttribute("refid", "cp"); // NOI18N

        Element javaElem = doc.createElement("java"); // NOI18N
        javaElem.setAttribute("classname", "${" + propName + "}"); // NOI18N

        cpElem = doc.createElement("classpath"); // NOI18N
        cpElem.setAttribute("refid", "cp"); // NOI18N
        javaElem.appendChild(cpElem);
        addProfileInit(doc, doc.getDocumentElement());
        addProfileVMArgs(javaElem, doc);

        targetElem.appendChild(javaElem);
        return targetElem;
    }

    private AntLocation handleInitials(Document doc, Lookup context) {
        ensurePropertiesCopied(doc.getDocumentElement());
        Comment comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_edit_target") + " ");
        doc.getDocumentElement().appendChild(comm);
        comm = doc.createComment(" " + NbBundle.getMessage(JavaActions.class, "COMMENT_more_info_run.single") + " ");
        doc.getDocumentElement().appendChild(comm);
        return findPackageRoot(context);
    }

    /**
     * Get value of depends attribute of target mapped as Run target by user
     */
    String[] getRunDepends() throws IOException, SAXException {
        String depends[] = null;
        Element targetElem = findExistingBuildTarget(ActionProvider.COMMAND_RUN);
        if (targetElem == null)
            return null;
        String[] bindings = findCommandBinding(ActionProvider.COMMAND_RUN);
        String dep = targetElem.getAttribute("depends"); // NOI18N
        if (bindings != null && bindings.length <= 2 && !"".equals(dep)) {
            depends = new String[2];
            depends[0] = bindings[0];
            depends[1] = dep;
        }
        return depends;
    }

    /**
     * Create Path like element (path or classpath) consisting of either execute cp
     * if it exists or compilation cp and build products
     */
    Element getPathFromCU(Document doc, String srcRoot, String type) {
        String cp = findCUClasspath(srcRoot, "execute"); // NOI18N
        Element pElem = null;
        if (cp != null) {
            // if EXECUTE cp exists use it
            pElem = createPathLikeElem(doc, type, null, new String[] {cp}, null, null, null);
        } else {
            // if not try to create run cp from COMPILE cp and all output locations
            cp = findCUClasspath(srcRoot, "compile"); // NOI18N
            String paths[] = cp == null ? null : new String[] {cp};
            String outputs[] = findCUOutputs(srcRoot);
            pElem = createPathLikeElem(doc, type, null, paths, outputs, null, null);
        }
        return pElem;
    }

    /**
     * Creates path or classpath element according to params
     */
    Element createPathLikeElem(Document doc, String type, String id,
                String[] paths, String[] locations, String refid, String comm) {

        Element pElem = doc.createElement(type); // NOI18N
        if (id != null)
            pElem.setAttribute("id", id); // NOI18N
        if (refid != null)
            pElem.setAttribute("refid", refid); // NOI18N
        if (comm != null)
            pElem.appendChild(doc.createComment(comm));
        if (paths != null && paths.length > 0) {
            for (int i = 0; i < paths.length; i++) {
                Element pathelElem = doc.createElement("pathelement"); // NOI18N
                pathelElem.setAttribute("path", paths[i]); // NOI18N
                pElem.appendChild(pathelElem);
            }
        }
        if (locations != null && locations.length > 0) {
            for (int j = 0; j < locations.length; j++) {
                Element pathelElem = doc.createElement("pathelement"); // NOI18N
                pathelElem.setAttribute("location", locations[j]); // NOI18N
                pElem.appendChild(pathelElem);
            }
        }
        return pElem;
    }

    /**
     * Creates ant element according to params
     */
    Element createAntElem(Document doc, String antFile, String deps) {
        assert antFile != null;
        Element antElem = doc.createElement("ant"); // NOI18N
        antElem.setAttribute("antfile", antFile);
        antElem.setAttribute("inheritall", "false"); // NOI18N
        StringTokenizer st = new StringTokenizer(deps, ","); // NOI18N
        if (st.countTokens() > 1) {
            while (st.hasMoreTokens()) {
                String dep = st.nextToken();
                Element tgElem = doc.createElement("target"); // NOI18N
                tgElem.setAttribute("name", dep.trim()); // NOI18N
                antElem.appendChild(tgElem);
            }
        } else {
            antElem.setAttribute("target", deps); // NOI18N
        }
        return antElem;
    }

    private boolean isSingleJavaFileSelected(Lookup context) {
        Collection<? extends DataObject> selectedDO = context.lookupAll(DataObject.class);
        if (selectedDO.size() == 1 && selectedDO.iterator().next().getPrimaryFile().hasExt("java")) {
            return true;
        }
        return false;
    }

    private void addJdkInitDeps(Element target) {
        if (helper.getProjectDirectory().getFileObject(JdkConfiguration.JDK_XML) != null) {
            String deps = target.getAttribute("depends"); // NOI18N
            target.setAttribute("depends", deps.length() == 0 ? "-jdk-init" : "-jdk-init," + deps); // NOI18N
        }
    }

}
