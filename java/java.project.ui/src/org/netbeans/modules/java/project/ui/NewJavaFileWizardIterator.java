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

package org.netbeans.modules.java.project.ui;

import com.sun.source.tree.ModuleTree;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.netbeans.spi.java.project.support.ui.templates.JavaFileWizardIteratorFactory;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Java file.
 */
@TemplateRegistrations({
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 100, content = "resources/Class.java.template", scriptEngine = "freemarker", displayName = "#Class.java", iconBase = JavaTemplates.JAVA_ICON, description = "resources/Class.html", category = {"java-classes", "java-classes-basic"}),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 200, content = "resources/Interface.java.template", scriptEngine = "freemarker", displayName = "#Interface.java", iconBase = JavaTemplates.INTERFACE_ICON, description = "resources/Interface.html", category = {"java-classes", "java-classes-basic"}),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 300, content = "resources/Enum.java.template", scriptEngine = "freemarker", displayName = "#Enum.java", iconBase = JavaTemplates.ENUM_ICON, description = "resources/Enum.html", category = {"java-classes", NewJavaFileWizardIterator.JDK_5}),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 400, content = "resources/AnnotationType.java.template", scriptEngine = "freemarker", displayName = "#AnnotationType.java", iconBase = JavaTemplates.ANNOTATION_TYPE_ICON, description = "resources/AnnotationType.html", category = {"java-classes", NewJavaFileWizardIterator.JDK_5}),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 600, content = "resources/Exception.java.template", scriptEngine = "freemarker", displayName = "#Exception.java", iconBase = JavaTemplates.JAVA_ICON, description = "resources/Exception.html", category = {"java-classes", "java-classes-basic"}),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 700, content = "resources/JApplet.java.template", scriptEngine = "freemarker", displayName = "#JApplet.java", iconBase = JavaTemplates.JAVA_ICON, description = "resources/JApplet.html", category = "java-classes"),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 800, content = "resources/Applet.java.template", scriptEngine = "freemarker", displayName = "#Applet.java", iconBase = JavaTemplates.JAVA_ICON, description = "resources/Applet.html", category = "java-classes"),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 900, content = "resources/Main.java.template", scriptEngine = "freemarker", displayName = "#Main.java", iconBase = "org/netbeans/modules/java/project/ui/resources/main-class.png", description = "resources/Main.html", category = "java-main-class"),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 950, content = "resources/Singleton.java.template", scriptEngine = "freemarker", displayName = "#Singleton.java", iconBase = JavaTemplates.JAVA_ICON, description = "resources/Singleton.html", category = "java-classes"),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 1000, content = "resources/Empty.java.template", scriptEngine = "freemarker", displayName = "#Empty.java", iconBase = JavaTemplates.JAVA_ICON, description = "resources/Empty.html", category = {"java-classes", "java-classes-basic"}),
    @TemplateRegistration(folder = NewJavaFileWizardIterator.FOLDER, position = 1150, content = "resources/Record.java.template", scriptEngine = "freemarker", displayName = "#Record.java", iconBase = JavaTemplates.JAVA_ICON, description = "resources/Record.html", category = {"java-classes", NewJavaFileWizardIterator.JDK_14}),
})
@Messages({
    "Class.java=Java Class",
    "Interface.java=Java Interface",
    "Enum.java=Java Enum",
    "AnnotationType.java=Java Annotation Type",
    "Exception.java=Java Exception",
    "JApplet.java=JApplet",
    "Applet.java=Applet",
    "Main.java=Java Main Class",
    "Singleton.java=Java Singleton Class",
    "Empty.java=Empty Java File",
    "Record.java=Java Record"
})
public class NewJavaFileWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    private static final String SOURCE_TYPE_GROOVY = "groovy"; // NOI18N
    private static final String SUPERCLASS = "superclass"; // NOI18N
    private static final String INTERFACES = "interfaces"; // NOI18N

    static final String FOLDER = "Classes";

    static final String JDK_5 = "jdk5";
    static final String JDK_9 = "jdk9";
    static final String JDK_14 = "jdk14";
    
    private static final long serialVersionUID = 1L;

    public enum Type {FILE, PACKAGE, PKG_INFO, MODULE_INFO}
    
    private final Type type;
    
    /** Create a new wizard iterator. */
    public NewJavaFileWizardIterator() {
        this(Type.FILE);
    }
    
    
    private NewJavaFileWizardIterator(Type type) {
        this.type = type;
    }    
    
    @TemplateRegistration(folder = FOLDER, id="Package", position = 1100, displayName = "#packageWizard", iconBase = PackageDisplayUtils.PACKAGE, description = "resources/Package.html", category = {"java-classes", "java-classes-basic"})
    @Messages("packageWizard=Java Package")
    public static NewJavaFileWizardIterator packageWizard() {
        return new NewJavaFileWizardIterator(Type.PACKAGE);
    }
    
    @TemplateRegistration(folder = FOLDER, position = 650, content = "resources/package-info.java.template", scriptEngine = "freemarker", displayName = "#packageInfoWizard", iconBase = JavaTemplates.JAVA_ICON, description = "resources/package-info.html", category = {"java-classes", JDK_5})
    @Messages("packageInfoWizard=Java Package Info")
    public static NewJavaFileWizardIterator packageInfoWizard () {
        return new NewJavaFileWizardIterator(Type.PKG_INFO);
    }
            
    @TemplateRegistration(folder = FOLDER, position = 670, content = "resources/module-info.java.template", scriptEngine = "freemarker", displayName = "#moduleInfoWizard", iconBase = JavaTemplates.JAVA_ICON, description = "resources/module-info.html", category = {"java-classes", JDK_9})
    @Messages("moduleInfoWizard=Java Module Info")
    public static NewJavaFileWizardIterator moduleInfoWizard () {
        return new NewJavaFileWizardIterator(Type.MODULE_INFO);
    }
            
    private WizardDescriptor.Panel[] createPanels (
            @NonNull final WizardDescriptor wizardDescriptor,
            @NonNull final WizardDescriptor.Iterator<WizardDescriptor> it) {
        
        // Ask for Java folders
        Project project = Templates.getProject( wizardDescriptor );
        if (project == null) throw new NullPointerException ("No project found for: " + wizardDescriptor);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources;
        groups = checkNotNull (groups, sources);
        if (groups.length == 0) {
            groups = sources.getSourceGroups( Sources.TYPE_GENERIC ); 
            groups = checkNotNull (groups, sources);
            return new WizardDescriptor.Panel[] {            
                Templates.buildSimpleTargetChooser(project, groups).create(),
            };
        }
        else {
            final List<WizardDescriptor.Panel<?>> panels = new ArrayList<>();
            if (this.type == Type.FILE) {
                panels.add(JavaTemplates.createPackageChooser(project, groups, new ExtensionAndImplementationWizardPanel(wizardDescriptor)));
            } else if (type == Type.PKG_INFO) {
                panels.add(new JavaTargetChooserPanel(project, groups, null, Type.PKG_INFO, true));
            } else if (type == Type.MODULE_INFO) {
                panels.add(new JavaTargetChooserPanel(project, groups, null, Type.MODULE_INFO, false));
            } else {
                assert type == Type.PACKAGE;
                SourceGroup[] groovySourceGroups = sources.getSourceGroups(SOURCE_TYPE_GROOVY);
                if (groovySourceGroups.length > 0) {
                    List<SourceGroup> all = new ArrayList<>();
                    all.addAll(Arrays.asList(groups));
                    all.addAll(Arrays.asList(groovySourceGroups));
                    groups = all.toArray(new SourceGroup[0]);
                }

                SourceGroup[] resources = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
                assert resources != null;
                if (resources.length > 0) { // #161244
                    List<SourceGroup> all = new ArrayList<SourceGroup>();
                    all.addAll(Arrays.asList(groups));
                    all.addAll(Arrays.asList(resources));
                    groups = all.toArray(new SourceGroup[0]);
                }
                panels.add(new JavaTargetChooserPanel(project, groups, null, Type.PACKAGE, false));
            }
            if (it != null) {
                if (it.current() != null) {
                    panels.add(it.current());
                }
                while(it.hasNext()) {
                    it.nextPanel();
                    panels.add(it.current());
                }
            }
            return panels.toArray(new WizardDescriptor.Panel<?>[0]);
        }
    }

    private static SourceGroup[] checkNotNull (SourceGroup[] groups, Sources sources) {
        List<SourceGroup> sourceGroups = new ArrayList<SourceGroup> ();
        for (SourceGroup sourceGroup : groups)
            if (sourceGroup == null)
                Exceptions.printStackTrace (new NullPointerException (sources + " returns null SourceGroup!"));
            else
                sourceGroups.add (sourceGroup);
        return sourceGroups.toArray (new SourceGroup [0]);
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }
    
    private void addRequires(FileObject createdFile, String createdModuleName, Set<String> requiredModuleNames) throws IOException {
        if (requiredModuleNames == null) {
            requiredModuleNames = new LinkedHashSet<>();
            ClassPath modulePath = ClassPath.getClassPath(createdFile, JavaClassPathConstants.MODULE_COMPILE_PATH);
            for (FileObject root : modulePath.getRoots()) {
                String name = SourceUtils.getModuleName(root.toURL(), true);
                if (name != null) {
                    requiredModuleNames.add(name);
                }
            }
        }
        if (createdModuleName != null) {
            requiredModuleNames.remove(createdModuleName);
        }
        if (!requiredModuleNames.isEmpty()) {
            final JavaSource src = JavaSource.forFileObject(createdFile);
            if (src != null) {
                final Set<String> mNames = requiredModuleNames;
                src.runModificationTask(new AddRequiresDirective(mNames)).commit();
            }
        }
    }
    
    private class AddRequiresDirective implements Task<WorkingCopy> {
        final Set<String> mNames;

        public AddRequiresDirective(Set<String> mNames) {
            this.mNames = mNames;
        }
        
        @Override
        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreeMaker tm = copy.getTreeMaker();
            ModuleTree modle = copy.getCompilationUnit().getModule();
            ModuleTree newModle = modle;
            for (String mName : mNames) {
                newModle = tm.addModuleDirective(newModle, tm.Requires(false, false, tm.QualIdent(mName)));
            }
            copy.rewrite(modle, newModle);
        }
    }

    @Override
    public Set<FileObject> instantiate () throws IOException {
        FileObject dir = Templates.getTargetFolder( wiz );
        
        String targetName = Templates.getTargetName( wiz );
        
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wiz );
        
        FileObject createdFile = null;
        String createdModuleName = null;
        Set<String> requiredModuleNames = null;
        if (this.type == Type.PACKAGE) {
            targetName = targetName.replace( '.', '/' ); // NOI18N
            createdFile = FileUtil.createFolder( dir, targetName );
        } else if (this.type == Type.MODULE_INFO) {
            Project project = Templates.getProject( wiz );
            URL[] srcs = UnitTestForSourceQuery.findSources(dir);
            if (srcs.length > 0) {
                requiredModuleNames = new LinkedHashSet<>();
                for (int i = 0; i < srcs.length; i++) {
                    for (URL root : BinaryForSourceQuery.findBinaryRoots(srcs[i]).getRoots()) {
                        String mName = SourceUtils.getModuleName(root, true);
                        if (mName != null) {
                            requiredModuleNames.add(mName);
                        }
                    }
                }
            }
            final String moduleName;
            final ClassPath msp = ClassPath.getClassPath(dir, JavaClassPathConstants.MODULE_SOURCE_PATH);
            if (msp == null) {
                //Single module project
                moduleName = createModuleName(
                        ProjectUtils.getInformation(project).getDisplayName(),
                        srcs.length > 0);
            } else {
                //Multi module project
                final String path = msp.getResourceName(dir);
                if (path == null) {
                    moduleName = null;
                } else {
                    final int index = path.indexOf('/');  //NOI18N
                    moduleName = index < 0 ?
                            path :
                            path.substring(0, index);
                }
            }
            DataObject dTemplate = DataObject.find( template );
            DataObject dobj = dTemplate.createFromTemplate(
                    df,
                    targetName,
                    Collections.singletonMap("moduleName", moduleName)); //NOI18N
            createdFile = dobj.getPrimaryFile();
            createdModuleName = moduleName;
        } else {
            DataObject dTemplate = DataObject.find(template);
            Object superclassProperty = wiz.getProperty(SUPERCLASS);
            String superclass = superclassProperty != null ? (String) superclassProperty : ""; //NOI18N
            Object interfacesProperty = wiz.getProperty(INTERFACES);
            String interfaces = interfacesProperty != null ? (String) interfacesProperty : ""; //NOI18N
            Map<String, String> parameters = new HashMap<>(Short.BYTES);
            if (!superclass.isEmpty()) {
                parameters.put(SUPERCLASS, superclass);
            }
            if (!interfaces.isEmpty()) {
                parameters.put(INTERFACES, interfaces);
            }
            DataObject dobj = dTemplate.createFromTemplate(df, targetName, parameters);
            createdFile = dobj.getPrimaryFile();
        }
        final Set<FileObject> res = new HashSet<>();
        res.add(createdFile);
        asInstantiatingIterator(projectSpecificIterator)
                .map((it)->{
                    try {
                        return it.instantiate();
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                        return null;
                    }
                })
                .ifPresent(res::addAll);
        if (this.type == Type.MODULE_INFO) {
            addRequires(createdFile, createdModuleName, requiredModuleNames);
        }
        return Collections.unmodifiableSet(res);
    }

    @Messages("TXT_Test=Test")
    private static String createModuleName (final String projectName, boolean isTest) {
        final StringBuilder name = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        boolean needsEscape = false;
        String part;
        for (int i=0; i< projectName.length(); i++) {
            final char c = projectName.charAt(i);
            if (first) {
                if (!Character.isJavaIdentifierStart(c)) {
                    if (Character.isJavaIdentifierPart(c)) {
                        needsEscape = true;
                        sb.append(c);
                        first = false;
                    }
                } else {
                    sb.append(c);
                    first = false;
                }
            } else {
                if (Character.isJavaIdentifierPart(c) ) {
                    sb.append(c);
                } else if (sb.length() > 0) {
                    part = sb.toString();
                    if (!needsEscape || name.length()>0) {
                        name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
                    }
                    sb = new StringBuilder();
                    first = true;
                    needsEscape = false;
                }
            }
        }
        if (sb.length()>0) {
            part = sb.toString();
            if (!needsEscape || name.length()>0) {
                name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        if (isTest) {
            name.append(Bundle.TXT_Test());
        }
        return name.toString();
    }    
    
    @NonNull
    private static Optional<WizardDescriptor.InstantiatingIterator<WizardDescriptor>> asInstantiatingIterator(
            @NullAllowed final WizardDescriptor.Iterator<WizardDescriptor> it) {
        return Optional.ofNullable(it)
                .map((p)->{
                    return p instanceof WizardDescriptor.InstantiatingIterator ?
                            (WizardDescriptor.InstantiatingIterator<WizardDescriptor>) p:
                            null;
                });
    }
        
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private transient WizardDescriptor.Iterator<WizardDescriptor> projectSpecificIterator;
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        final Project project = Templates.getProject(wiz);
        final JavaFileWizardIteratorFactory templateProvider = project != null ? project.getLookup().lookup(JavaFileWizardIteratorFactory.class) : null;
        if (templateProvider != null) {
            projectSpecificIterator = templateProvider.createIterator(Templates.getTemplate(wiz));
            asInstantiatingIterator(projectSpecificIterator)
                    .ifPresent((it)->it.initialize(wiz));
        }
        panels = createPanels(wiz, projectSpecificIterator);
        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        if (prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize (WizardDescriptor wiz) {
        asInstantiatingIterator(projectSpecificIterator)
                .ifPresent((it)->it.uninitialize(wiz));
        this.wiz = null;
        panels = null;
        projectSpecificIterator = null;
    }
    
    @Override
    public String name() {
        //return "" + (index + 1) + " of " + panels.length;
        return ""; // NOI18N
    }
    
    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    private final transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        ChangeListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ChangeListener[0]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : ls) {
            l.stateChanged(ev);
        }
    }
}
