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

package org.netbeans.modules.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import java.awt.Component;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.modules.classfile.Access;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.InvalidClassFormatException;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.spi.java.loaders.RenameHandler;
import org.openide.filesystems.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

import static org.openide.util.ImageUtilities.assignToolTipToImage;
import static org.openide.util.ImageUtilities.loadImage;
import static org.openide.util.NbBundle.getMessage;

/**
 * The node representation of Java source files.
 */
public final class JavaNode extends DataNode implements ChangeListener {


    /** generated Serialized Version UID */
    private static final long serialVersionUID = -7396485743899766258L;

    private static final String JAVA_ICON_BASE = "org/netbeans/modules/java/resources/class.png"; // NOI18N
    private static final String CLASS_ICON_BASE = "org/netbeans/modules/java/resources/clazz.gif"; // NOI18N
    private static final String ABSTRACT_CLASS_ICON_BASE = "org/netbeans/modules/java/resources/abstract_class_file.png"; //NOI18N
    private static final String INTERFACE_ICON_BASE = "org/netbeans/modules/java/resources/interface_file.png"; //NOI18N
    private static final String ENUM_ICON_BASE = "org/netbeans/modules/java/resources/enum_file.png";   //NOI18N
    private static final String ANNOTATION_ICON_BASE = "org/netbeans/modules/java/resources/annotation_file.png";   //NOI18N
    private static final String EXECUTABLE_BADGE_URL = "org/netbeans/modules/java/resources/executable-badge.png";  //NOI18N
    private static final String NEEDS_COMPILE_BADGE_URL = "org/netbeans/modules/java/resources/needs-compile.png";  //NOI18N
    // synced with org.netbeans.modules.java.file.launcher.SingleSourceFileUtil
    private static final String GLOBAL_VM_OPTIONS = "java_file_launcher_global_vm_options"; //NOI18N
    private static final String FILE_ARGUMENTS = "single_file_run_arguments"; //NOI18N
    private static final String FILE_JDK = "single_file_run_jdk"; //NOI18N
    private static final String FILE_VM_OPTIONS = "single_file_vm_options"; //NOI18N
    private static final String FILE_REGISTER_ROOT = "register_root"; //NOI18N

    private static final Map<String,Image> IMAGE_CACHE = new ConcurrentHashMap<>();
    private static final boolean ALWAYS_PREFFER_COMPUTED_ICON = Boolean.getBoolean("JavaNode.prefferComputedIcon"); //NOI18N
    private static final Logger LOG = Logger.getLogger(JavaNode.class.getName());
    
    private Status status;
    private final boolean isJavaSource;
    private final AtomicReference<Image> isCompiled;
    private final AtomicReference<Image> isExecutable;
    private final AtomicReference<Image> computedIcon;
    private final AtomicReference<FileChangeListener> computedIconListener;
    private ChangeListener executableListener;

    /** Create a node for the Java data object using the default children.
    * @param jdo the data object to represent
    */
    public JavaNode (final DataObject jdo, boolean isJavaSource) {
        super (jdo, Children.LEAF);
        this.isJavaSource = isJavaSource;
        this.isCompiled = new AtomicReference<>();
        this.isExecutable = new AtomicReference<>();
        this.computedIcon = new AtomicReference<>();
        this.computedIconListener = new AtomicReference<>();
        this.setIconBaseWithExtension(isJavaSource ? JAVA_ICON_BASE : CLASS_ICON_BASE);
        Logger.getLogger("TIMER").log(Level.FINE, "JavaNode", new Object[] {jdo.getPrimaryFile(), this});
        if (!jdo.isTemplate()) {
            WORKER.post(IconTask.create(this));
            if (isJavaSource) {
                WORKER.post(new BuildStatusTask(this));
                WORKER.post(new ExecutableTask(this));
                jdo.addPropertyChangeListener((evt) -> {
                    if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
                        Logger.getLogger("TIMER").log(Level.FINE, "JavaNode", new Object[]{jdo.getPrimaryFile(), this});
                        WORKER.post(() -> {
                            computedIconListener.set(null);
                            synchronized (JavaNode.this) {
                                status = null;
                                executableListener = null;
                                WORKER.post(new BuildStatusTask(JavaNode.this));
                                WORKER.post(new ExecutableTask(JavaNode.this));
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void setName(String name) {
        RenameHandler handler = getRenameHandler();
        if (handler == null) {
            super.setName(name);
        } else {
            try {
                handler.handleRename(JavaNode.this, name);
            } catch (IllegalArgumentException ioe) {
                super.setName(name);
            }
        }
    }
    
    private static synchronized RenameHandler getRenameHandler() {
        Collection<? extends RenameHandler> handlers = (Lookup.getDefault().lookupAll(RenameHandler.class)) ;
        if (handlers.isEmpty())
            return null;
        if (handlers.size()>1)
            LOG.warning("Multiple instances of RenameHandler found in Lookup; only using first one: " + handlers); //NOI18N
        return handlers.iterator().next();
    }

    private PropertySet[] propertySets;
    
    @Override
    public PropertySet[] getPropertySets() {
        getSheet(); //force initialization
        
        synchronized (this) {
            return Arrays.copyOf(propertySets, propertySets.length);
        }
    }
    
    /** Create the property sheet.
     * @return the sheet
     */
    @Override
    protected final Sheet createSheet () {
        Sheet sheet = super.createSheet();
        
        //if there is any rename handler installed
        //push under our own property
        if (getRenameHandler() != null)
            sheet.get(Sheet.PROPERTIES).put(createNameProperty());
        
        // Add classpath-related properties.
        Sheet.Set ps = new Sheet.Set();
        ps.setName("classpaths"); // NOI18N
        ps.setDisplayName(getMessage(JavaNode.class, "LBL_JavaNode_sheet_classpaths"));
        ps.setShortDescription(getMessage(JavaNode.class, "HINT_JavaNode_sheet_classpaths"));
        ps.put(new Node.Property[] {
                    new ClasspathProperty(ClassPath.COMPILE,
                    getMessage(JavaNode.class, "PROP_JavaNode_compile_classpath"),
                    getMessage(JavaNode.class, "HINT_JavaNode_compile_classpath")),
                    new ClasspathProperty(ClassPath.EXECUTE,
                    getMessage(JavaNode.class, "PROP_JavaNode_execute_classpath"),
                    getMessage(JavaNode.class, "HINT_JavaNode_execute_classpath")),
                    new ClasspathProperty(ClassPath.BOOT,
                    getMessage(JavaNode.class, "PROP_JavaNode_boot_classpath"),
                    getMessage(JavaNode.class, "HINT_JavaNode_boot_classpath")),
        });
        sheet.put(ps);

        // classfile format etc
        if (!isJavaSource) {
            ps = new Sheet.Set();
            ps.setName("classfile_info"); // NOI18N
            ps.setDisplayName(getMessage(JavaNode.class, "LBL_JavaNode_sheet_classfile")); // NOI18N
            ps.setShortDescription(getMessage(JavaNode.class, "HINT_JavaNode_sheet_classfile")); // NOI18N
            ps.put(new ClassFileVersionProperty());
            sheet.put(ps);
        }

        // "single-file" java programs
        Project parentProject = FileOwnerQuery.getOwner(super.getDataObject().getPrimaryFile());
        DataObject dObj = super.getDataObject();
        // If any of the parent folders is a project, user won't have the option to specify these attributes to the java files.
        if (parentProject == null) {
            Sheet.Set ss = new Sheet.Set();
            ss.setName("runFileArguments"); // NOI18N
            ss.setDisplayName(getMessage(JavaNode.class, "LBL_JavaNode_without_project_run")); // NOI18N
            ss.setShortDescription("Run the file's source code.");
            ss.put(new JavaFileBooleanAttributeProperty(dObj, FILE_REGISTER_ROOT, "registerRoot", "singlefile_registerRoot")); // NOI18N
            ss.put(new RunFileJDKProperty(dObj));
            ss.put(new JavaFileAttributeProperty(dObj, FILE_ARGUMENTS, "runFileArguments", "singlefile_arguments")); // NOI18N
            ss.put(new JavaFileAttributeProperty(dObj, FILE_VM_OPTIONS, "runFileVMOptions", "singlefile_options")); // NOI18N
            ss.put(new JavaFileGlobalOptionsProperty());
            sheet.put(ss);
        }

        @SuppressWarnings("LocalVariableHidesMemberVariable")
        PropertySet[] propertySets = sheet.toArray();
        
        synchronized (this) {
            this.propertySets = propertySets;
        }
        
        return sheet;
    }
    
    private Node.Property createNameProperty () {
        Node.Property p = new PropertySupport.ReadWrite<String> (
                DataObject.PROP_NAME,
                String.class,
                getMessage (DataObject.class, "PROP_name"),
                getMessage (DataObject.class, "HINT_name")
                ) {
            @Override
            public String getValue () {
                return JavaNode.this.getName();
            }
            @Override
            public Object getValue(String key) {
                if ("suppressCustomEditor".equals (key)) { //NOI18N
                    return Boolean.TRUE;
                } else {
                    return super.getValue (key);
                }
            }
            @Override
            public void setValue(String val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                if (!canWrite())
                    throw new IllegalAccessException();
                JavaNode.this.setName(val);
            }
            @Override
            public boolean canWrite() {
                return JavaNode.this.canRename();
            }
            
        };
        
        return p;
    }
    
    /**
     * Displays one kind of classpath for this Java source.
     * Tries to use the normal format (directory or JAR names), falling back to URLs if necessary.
     */
    private final class ClasspathProperty extends PropertySupport.ReadOnly<String> {
        
        private final String id;
        
        public ClasspathProperty(String id, String displayName, String shortDescription) {
            super(id, /*XXX NbClassPath would be preferable, but needs org.openide.execution*/String.class, displayName, shortDescription);
            this.id = id;
            // XXX the following does not always work... why?
            setValue("oneline", false); // NOI18N
        }
        
        @Override
        public String getValue() {
            ClassPath cp = ClassPath.getClassPath(getDataObject().getPrimaryFile(), id);
            if (cp != null) {
                StringBuilder sb = new StringBuilder();
                for (ClassPath.Entry entry : cp.entries()) {
                    URL u = entry.getURL();
                    String item = u.toExternalForm(); // fallback
                    if (u.getProtocol().equals("file")) { // NOI18N
                        item = Utilities.toFile(URI.create(item)).getAbsolutePath();
                    } else if (u.getProtocol().equals("jar") && item.endsWith("!/")) { // NOI18N
                        URL embedded = FileUtil.getArchiveFile(u);
                        assert embedded != null : u;
                        if (embedded.getProtocol().equals("file")) { // NOI18N
                            item = Utilities.toFile(URI.create(embedded.toExternalForm())).getAbsolutePath();
                        }
                    }
                    if (sb.length() > 0) {
                        sb.append(File.pathSeparatorChar);
                    }
                    sb.append(item);
                }
                return sb.toString();
            } else {
                return getMessage(JavaNode.class, "LBL_JavaNode_classpath_unknown");
            }
        }
    }

    /**
     * Displays the class file version.
     */
    private final class ClassFileVersionProperty extends PropertySupport.ReadOnly<String> {

        public static final Duration REFRESH = Duration.ofSeconds(5);
        private Instant expiration = Instant.now();
        private String version = null;

        public ClassFileVersionProperty() {
            super("classfile_version", String.class,  // NOI18N
                getMessage(JavaNode.class, "PROP_JavaNode_classfile_version"), // NOI18N
                getMessage(JavaNode.class, "HINT_JavaNode_classfile_version")); // NOI18N
        }

        @Override
        public String getValue() {
            FileObject file = getDataObject().getPrimaryFile();
            // avoids IO but gets fresh values if they expire
            if (version != null && Instant.now().isBefore(expiration)) {
                return version;
            }
            try (InputStream in = file.getInputStream()) {
                ByteBuffer bytes = ByteBuffer.wrap(in.readNBytes(8)); // 4b magic + 2x2b for version
                if (bytes.capacity() == 8 && bytes.getInt() == HexFormat.fromHexDigits("CAFEBABE")) { // NOI18N
                    int minor = bytes.getChar();
                    int major = bytes.getChar();
                    if (major >= 55) { // JEP 12
                        version = major + " (Java " + (major - 44) + (minor == 65535 ? ", preview enabled" : "") + ")";  // NOI18N
                    } else {
                        version = major + "." + minor + " (Java " + (major - 44) + ")"; // NOI18N
                    }
                    expiration = Instant.now().plus(REFRESH);
                    return version;
                }
            } catch (IOException ignored) {}
            version = "unknown"; // NOI18N
            return version;
        }
    }

    private static final class RunFileJDKProperty extends PropertySupport.ReadWrite<String> {

        private final PropertyEditorSupport jdkPicker = new JDKPicker();
        private final DataObject dObj;

        public RunFileJDKProperty(DataObject dObj) {
            super("runFileJDK", String.class, // NOI18N
                  getMessage(JavaNode.class, "PROP_JavaNode_singlefile_jdk"), // NOI18N
                  getMessage(JavaNode.class, "HINT_JavaNode_singlefile_jdk")); // NOI18N
            this.dObj = dObj;
        }

        @Override
        public String getValue() {
            return dObj.getPrimaryFile().getAttribute(FILE_JDK) instanceof String jdk ? jdk : "";
        }

        @Override
        public void setValue(String text) {
            try {
                dObj.getPrimaryFile().setAttribute(FILE_JDK, text);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Java File does not exist : {0}", dObj.getPrimaryFile().getName()); //NOI18N
            }
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return jdkPicker;
        }

        // inline combobox for jdk property
        private static final class JDKPicker extends PropertyEditorSupport {

            private final JComboBox jdkCombo = new JComboBox();
            private final PropertyChangeListener modelUpdater = e -> initModel();
            private String[] jdks;

            public JDKPicker() {
                initModel();
                jdkCombo.addActionListener(e -> super.setValue(jdkCombo.getSelectedItem()));
                JavaPlatformManager jdkman = JavaPlatformManager.getDefault();
                jdkman.addPropertyChangeListener(WeakListeners.propertyChange(modelUpdater, jdkman));
            }

            private void initModel() {
                jdks = Stream.of(JavaPlatformManager.getDefault().getInstalledPlatforms())
                             .filter(jdk -> isCompatible(jdk.getSpecification().getVersion()))
                             .map(jdk -> jdk.getDisplayName())
                             .sorted(Comparator.reverseOrder())
                             .toArray(String[]::new);
                jdkCombo.setModel(new DefaultComboBoxModel(jdks));
            }

            private static boolean isCompatible(SpecificationVersion jdkVerison) {
                try {
                    return Integer.parseInt(jdkVerison.toString()) >= 11;
                } catch (NumberFormatException ignore) {}
                return false;
            }

            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                jdkCombo.setSelectedItem(text);
            }

            @Override
            public void setValue(Object value) {
                jdkCombo.setSelectedItem(value);
                super.setValue(value);
            }

            @Override
            @SuppressWarnings("ReturnOfCollectionOrArrayField")
            public String[] getTags() {
                return jdks;
            }

            @Override
            public Component getCustomEditor() {
                return jdkCombo;
            }

        }

    }

    // editable file attribute
    private static final class JavaFileBooleanAttributeProperty extends PropertySupport.ReadWrite<Boolean> {

        private final String attribute;
        private final DataObject dObj;

        public JavaFileBooleanAttributeProperty(DataObject dObj, String attribute, String name, String msgKeyPart) {
            super(name, Boolean.class, getMessage(JavaNode.class, "PROP_JavaNode_" + msgKeyPart), getMessage(JavaNode.class, "HINT_JavaNode_" + msgKeyPart)); // NOI18N
            this.dObj = dObj;
            this.attribute = attribute;
        }

        @Override
        public Boolean getValue() {
            return dObj.getPrimaryFile().getAttribute(attribute) instanceof Boolean val ? val : false;
        }

        @Override
        public void setValue(Boolean o) {
            try {
                dObj.getPrimaryFile().setAttribute(attribute, o);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Java File does not exist : {0}", dObj.getPrimaryFile().getName()); //NOI18N
            }
        }
    }

    // editable file attribute
    private static final class JavaFileAttributeProperty extends PropertySupport.ReadWrite<String> {

        private final String attribute;
        private final DataObject dObj;

        public JavaFileAttributeProperty(DataObject dObj, String attribute, String name, String msgKeyPart) {
            super(name, String.class, getMessage(JavaNode.class, "PROP_JavaNode_" + msgKeyPart), getMessage(JavaNode.class, "HINT_JavaNode_" + msgKeyPart)); // NOI18N
            this.dObj = dObj;
            this.attribute = attribute;
        }

        @Override
        public String getValue() {
            return dObj.getPrimaryFile().getAttribute(attribute) instanceof String val ? val : "";
        }

        @Override
        public void setValue(String o) {
            try {
                dObj.getPrimaryFile().setAttribute(attribute, o);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Java File does not exist : {0}", dObj.getPrimaryFile().getName()); //NOI18N
            }
        }
    }

    // read-only global VM options
    private static final class JavaFileGlobalOptionsProperty extends PropertySupport.ReadOnly<String> {

        public JavaFileGlobalOptionsProperty() {
            super("runFileGlobalOptions", String.class, getMessage(JavaNode.class, "PROP_JavaNode_singlefile_global_options"), getMessage(JavaNode.class, "HINT_JavaNode_singlefile_global_options")); // NOI18N
        }

        @Override
        public String getValue() {
            return NbPreferences.forModule(JavaPlatformManager.class).get(GLOBAL_VM_OPTIONS, "").trim();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        WORKER.post(new BuildStatusTask(this));
    }

    @Override
    public Image getIcon(int type) {
        Image i = prefferImage(
            computedIcon.get(),
            super.getIcon(type),
            type);
        return enhanceIcon(i);
    }

    @Override
    public Image getOpenedIcon(int type) {
        Image i = super.getOpenedIcon(type);
        return enhanceIcon(i);
    }

    private Image prefferImage(Image computed, Image parent, int type) {
        if (computed == null) {
            return parent;
        }
        if (!ALWAYS_PREFFER_COMPUTED_ICON) {
            final Object attrValue = parent.getProperty("url", null);   //NOI18N
            if (attrValue instanceof URL) {
                final String url = attrValue.toString();
                if (!(isJavaSource ? url.endsWith(JAVA_ICON_BASE) : url.endsWith(CLASS_ICON_BASE))) {
                    return parent;
                }
            }
        }
        try {
            final FileObject fo = getDataObject().getPrimaryFile ();
            computed = FileUIUtils.getImageDecorator(fo.getFileSystem ()).annotateIcon (
                computed,
                type,
                Set.of(fo));
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }
        return computed;
    }

    private Image enhanceIcon(Image i) {
        Image needsCompile = isCompiled.get();
        
        if (needsCompile != null) {
            i = ImageUtilities.mergeImages(i, needsCompile, 16, 0);
        }
        
        Image executable = isExecutable.get();
        
        if (executable != null) {
            i = ImageUtilities.mergeImages(i, executable, 10, 6);
        }
        
        return i;
    }
    
    private static final RequestProcessor WORKER = new RequestProcessor("Java Node Badge Processor", 1, false, false);
    
    private static Image getImage(
            @NonNull final String resourceId,
            @NullAllowed final String annotationTemplate) {
        Image result = IMAGE_CACHE.get(resourceId);
        if (result == null) {
            result = loadImage(resourceId, true);
            if (annotationTemplate != null) {
                URL resourceURL = JavaNode.class.getClassLoader().getResource(resourceId);
                final String annotation = MessageFormat.format(
                    annotationTemplate,
                    resourceURL);
                result = assignToolTipToImage(result, annotation);
            }
            IMAGE_CACHE.put(resourceId, result);
        }
        return result;
    }

    private abstract static class IconTask implements Runnable {
        protected final JavaNode node;

        IconTask(@NonNull final JavaNode node) {
            this.node = node;
        }

        @CheckForNull
        abstract String computeIcon(@NonNull final FileObject file);

        @Override
        public final void run() {
            String res = null;
            final FileObject file = node.getDataObject().getPrimaryFile();
            if (file != null && file.isValid()) {
                if (node.computedIconListener.get() == null) {
                    final FileChangeListener l = new FCL(node);
                    if (node.computedIconListener.compareAndSet(null, l)) {
                        file.addFileChangeListener(FileUtil.weakFileChangeListener(l, file));
                    }
                }
                res = computeIcon(file);
            }
            if (res == null) {
                res = node.isJavaSource ?
                    JAVA_ICON_BASE :
                    CLASS_ICON_BASE;
            }
            node.computedIcon.set(getImage(res, null));
            node.fireIconChange();
            node.fireOpenedIconChange();
        }

        private static final class FCL extends FileChangeAdapter {

            private final JavaNode node;

            FCL(@NonNull final JavaNode node) {
                this.node = node;
            }

            @Override
            public void fileChanged(FileEvent fe) {
                WORKER.post(IconTask.create(node));
            }
        }

        private static final class SourceIcon extends IconTask {

            private SourceIcon(@NonNull final JavaNode node) {
                super(node);
            }

            @Override
            String computeIcon(@NonNull final FileObject file) {
                final String[] res = new String[1];
                final JavaSource src = JavaSource.forFileObject(file);
                if (src != null) {
                    try {
                        src.runUserActionTask((CompilationController cc) -> {
                            cc.toPhase(JavaSource.Phase.PARSED);
                            final CompilationUnitTree cu = cc.getCompilationUnit();
                            final Collection<ClassTree> topTypes = cu.accept(new ClasssFinder(), new ArrayList<>());
                            for (ClassTree ct : topTypes) {
                                switch(ct.getKind()) {
                                    case CLASS -> res[0] = ct.getModifiers().getFlags().contains(Modifier.ABSTRACT)
                                                                ? ABSTRACT_CLASS_ICON_BASE : JAVA_ICON_BASE;
                                    case INTERFACE -> res[0] = INTERFACE_ICON_BASE;
                                    case ENUM -> res[0] = ENUM_ICON_BASE;
                                    case ANNOTATION_TYPE -> res[0] = ANNOTATION_ICON_BASE;
                                }
                                if (file.getName().contentEquals(ct.getSimpleName())) {
                                    break;
                                }
                            }
                        }, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return res[0];
            }

            private static final class ClasssFinder extends ErrorAwareTreeScanner<Collection<ClassTree>, Collection<ClassTree>> {

                @Override
                public Collection<ClassTree> visitCompilationUnit(CompilationUnitTree node, Collection<ClassTree> p) {
                    super.visitCompilationUnit(node, p);
                    return p;
                }

                @Override
                public Collection<ClassTree> visitClass(ClassTree node, Collection<ClassTree> p) {
                    p.add(node);
                    return p;
                }
            }
        }

        private static final class ClassIcon extends IconTask {

            private ClassIcon(@NonNull final JavaNode node) {
                super(node);
            }

            @Override
            @CheckForNull
            String computeIcon(@NonNull final FileObject file) {
                String res = CLASS_ICON_BASE;
                try {
                    try (InputStream in = file.getInputStream()) {
                        final ClassFile cf = new ClassFile(in, false);
                        if (cf.isEnum()) {
                            res = ENUM_ICON_BASE;
                        } else if (cf.isAnnotation()) {
                            res = ANNOTATION_ICON_BASE;
                        } else if ((cf.getAccess() & Access.INTERFACE) == Access.INTERFACE) {
                            res = INTERFACE_ICON_BASE;
                        } else {
                            res = (cf.getAccess() & Access.ABSTRACT) == Access.ABSTRACT ?
                                ABSTRACT_CLASS_ICON_BASE :
                                CLASS_ICON_BASE;
                        }
                    }
                } catch (FileNotFoundException e) {
                    // may happen in the file is just being cleaned up; should not log.
                } catch (InvalidClassFormatException e) {
                    // broken class file just log
                    LOG.log(
                        Level.INFO,
                        "Invalid classfile: {0}",   //NOI18N
                        FileUtil.getFileDisplayName(file));
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
                return res;
            }
        }

        static IconTask create(@NonNull final JavaNode node) {
            return node.isJavaSource ?
                new SourceIcon(node) :
                new ClassIcon(node);
        }
    }

    private static class BuildStatusTask implements Runnable {
        private JavaNode node;
        
        public BuildStatusTask(JavaNode node) {
            this.node = node;
        }

        @Override
        public void run() {
            Status _status;
            synchronized (node) {
                _status = node.status;
            }            
            if (_status == null) {
                FileObject jf = node.getDataObject().getPrimaryFile();
                _status = FileBuiltQuery.getStatus(jf);                
                synchronized (node) {
                    if (_status != null && node.status == null) {
                        node.status = _status;
                        node.status.addChangeListener(WeakListeners.change(node, node.status));
                    }
                }
            }

            boolean isPackageInfo = "package-info.java".equals(node.getDataObject().getPrimaryFile().getNameExt());
            boolean newIsCompiled = _status != null && !isPackageInfo ?  _status.isBuilt() : true;
            boolean oldIsCompiled = node.isCompiled.getAndSet(
                    newIsCompiled ?
                        null :
                        getImage(
                            NEEDS_COMPILE_BADGE_URL,
                            "<img src=\"{0}\">&nbsp;" + getMessage(JavaNode.class, "TP_NeedsCompileBadge")  //NOI18N
                            )) == null;

            if (newIsCompiled != oldIsCompiled) {
                node.fireIconChange();
                node.fireOpenedIconChange();
            }
        }
    }

    private static class ExecutableTask implements Runnable {
        private final JavaNode node;
        
        public ExecutableTask(JavaNode node) {
            this.node = node;
        }

        @Override
        public void run() {
            ChangeListener _executableListener;
            
            synchronized (node) {
                _executableListener = node.executableListener;
            }
            
            FileObject file = node.getDataObject().getPrimaryFile();

            if (_executableListener == null) {
                _executableListener = (ChangeEvent e) -> {
                    WORKER.post(new ExecutableTask(node));
                };
                
                ExecutableFilesIndex.DEFAULT.addChangeListener(file.toURL(), _executableListener);
                
                synchronized (node) {
                    if (node.executableListener == null) {
                        node.executableListener = _executableListener;
                    }
                }
            }
            
            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
            FileObject root = cp != null ? cp.findOwnerRoot(file) : null;
            
            if (root != null) {
                boolean newIsExecutable = ExecutableFilesIndex.DEFAULT.isMainClass(root.toURL(), file.toURL());
                boolean oldIsExecutable = node.isExecutable.getAndSet(
                    newIsExecutable ?
                        getImage(
                            EXECUTABLE_BADGE_URL,
                            "<img src=\"{0}\">&nbsp;" + getMessage(JavaNode.class, "TP_ExecutableBadge")):
                        null) != null;

                if (newIsExecutable != oldIsExecutable) {
                    node.fireIconChange();
                    node.fireOpenedIconChange();
                }
            }
        }
    }
    
}
