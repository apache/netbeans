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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import static org.netbeans.modules.java.api.common.project.ProjectProperties.ENDORSED_CLASSPATH;

/**
 *
 * @author Milan Kubec
 * @author Petr Somol
 */
public class JWSProjectProperties /*implements TableModelListener*/ {
    
    private static final Logger LOG = Logger.getLogger(JWSProjectProperties.class.getName());

    public static final String DEFAULT_PLATFORM   = JavaPlatformManager.getDefault().getDefaultPlatform().getProperties().get("platform.ant.name"); //NOI18N

    public static final String JNLP_ENABLED      = "jnlp.enabled";
    public static final String JNLP_ICON         = "jnlp.icon";
    public static final String JNLP_OFFLINE      = "jnlp.offline-allowed";
    public static final String JNLP_CBASE_TYPE   = "jnlp.codebase.type";
    public static final String JNLP_CBASE_USER   = "jnlp.codebase.user";
    public static final String JNLP_CBASE_URL    = "jnlp.codebase.url";
    public static final String JNLP_DESCRIPTOR   = "jnlp.descriptor";
    public static final String JNLP_APPLET       = "jnlp.applet.class";
    
    public static final String JNLP_SPEC         = "jnlp.spec";
    public static final String JNLP_INIT_HEAP    = "jnlp.initial-heap-size";
    public static final String JNLP_MAX_HEAP     = "jnlp.max-heap-size";
    
    public static final String JNLP_SIGNED = "jnlp.signed";
    public static final String JNLP_MIXED_CODE = "jnlp.mixed.code";

    public static final String JNLP_SIGNING = "jnlp.signing";
    public static final String JNLP_SIGNING_KEYSTORE = "jnlp.signing.keystore";
    public static final String JNLP_SIGNING_KEY = "jnlp.signing.alias";
    public static final String JNLP_SIGNING_KEYSTORE_PASSWORD = "jnlp.signing.storepass";
    public static final String JNLP_SIGNING_KEY_PASSWORD = "jnlp.signing.keypass";
    public static final String RUN_CP = "run.classpath";    //NOI18N
    public static final String BUILD_CLASSES = "build.classes.dir"; //NOI18N
    public static final String JNLP_LAZY_JARS = "jnlp.lazy.jars";   //NOI18N
    private static final String JNLP_LAZY_JAR = "jnlp.lazy.jar."; //NOI18N
    private static final String JNLP_LAZY_FORMAT = JNLP_LAZY_JAR +"%s"; //NOI18N
    
    static final String SIGNING_GENERATED = "generated";
    static final String SIGNING_KEY = "key";

    public static final String CB_TYPE_LOCAL = "local";
    public static final String CB_TYPE_WEB = "web";
    public static final String CB_TYPE_USER = "user";
    public static final String CB_NO_CODEBASE = "no.codebase";
    
    public static final String DEFAULT_APPLET_WIDTH = "300";
    public static final String DEFAULT_APPLET_HEIGHT = "300";

    private static final String JAR_INDEX = "jar.index";    //NOI18N
    private static final String JAR_ARCHIVE_DISABLED ="jar.archive.disabled";   //NOI18N
    public static final String BUILD_SCRIPT ="buildfile";      //NOI18N
    
    // explicit manifest entries (see #231951, #234231, http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/no_redeploy.html)
    public static final String MANIFEST_CUSTOM_CODEBASE = "manifest.custom.codebase"; // NOI18N
    public static final String MANIFEST_CUSTOM_PERMISSIONS = "manifest.custom.permissions"; // NOI18N
    public static final String MANIFEST_CUSTOM_CALLER_ALLOWABLE_CODEBASE = "manifest.custom.caller.allowable.codebase"; //NOI18N
    public static final String MANIFEST_CUSTOM_APPLICATION_LIBRARY_ALLOWABLE_CODEBASE= "manifest.custom.application.library.allowable.codebase";    //NOI18N

    public enum DescType {
        application, applet, component;
    }
    
    public static final String CB_URL_WEB = "$$codebase";
    
    public static final String JNLP_EXT_RES_PREFIX = "jnlp.ext.resource.";
    public static final String JNLP_APPLET_PARAMS_PREFIX = "jnlp.applet.param.";
    public static final String JNLP_APPLET_WIDTH = "jnlp.applet.width";
    public static final String JNLP_APPLET_HEIGHT = "jnlp.applet.height";
    
    // property to be set when enabling javawebstart to disable Compile on Save feature
    // javawebstart project needs to be built completly before it could be run
    public static final String COS_UNSUPPORTED_PROPNAME = "compile.on.save.unsupported.javawebstart";

    // special value to persist Ant script handling
    public static final String CB_URL_WEB_PROP_VALUE = "$$$$codebase";
    
    private StoreGroup jnlpPropGroup = new StoreGroup();
    
    private J2SEPropertyEvaluator j2sePropEval;
    private PropertyEvaluator evaluator;
    private Project project;
    
    private List<Map<String,String>> extResProperties;
    private List<Map<String,String>> appletParamsProperties;

    public static final String extResSuffixes[] = new String[] { "href", "name", "version" };
    public static final String appletParamsSuffixes[] = new String[] { "name", "value" };

    public static final String CONFIG_LABEL_PROPNAME = "$label";
    public static final String CONFIG_TARGET_RUN_PROPNAME = "$target.run";
    public static final String CONFIG_TARGET_DEBUG_PROPNAME = "$target.debug";

    public static final String CONFIG_TARGET_RUN = "jws-run";
    public static final String CONFIG_TARGET_DEBUG = "jws-debug";

    private static final String LIB_JAVAWS = "javaws.jar";  //NOI18N
    private static final String LIB_PLUGIN = "plugin.jar";  //NOI18N

    private DescType selectedDescType = null;


    // signing
    String signing;
    String signingKeyStore;
    String signingKeyAlias;
    char [] signingKeyStorePassword;
    char [] signingKeyPassword;
    
    // resources
    List<? extends File> runtimeCP;
    List<? extends File> lazyJars;
    boolean lazyJarsChanged;

    // Models 
    JToggleButton.ToggleButtonModel enabledModel;
    JToggleButton.ToggleButtonModel allowOfflineModel;
    
    ComboBoxModel codebaseModel;
    ComboBoxModel appletClassModel;
    ComboBoxModel mixedCodeModel;
    
    ButtonModel applicationDescButtonModel;
    ButtonModel appletDescButtonModel;
    ButtonModel compDescButtonModel;
    private ButtonGroup bg;
    
    PropertiesTableModel extResTableModel;
    PropertiesTableModel appletParamsTableModel;
    
    // and Documents
    Document iconDocument;
    Document codebaseURLDocument;
    Document appletWidthDocument;
    Document appletHeightDocument;

    /** Keeps singleton instance of JWSProjectProperties for any WS project for which property customizer is opened at once */
    private static Map<String, JWSProjectProperties> propInstance = new TreeMap<String, JWSProjectProperties>();

    /** Keeps set of category markers used to identify validity of JFXProjectProperties instance */
    private Set<String> instanceMarkers = new TreeSet<String>();
    
    public void markInstance(String marker) {
        instanceMarkers.add(marker);
    }
    
    public boolean isInstanceMarked(String marker) {
        return instanceMarkers.contains(marker);
    }
    
    /** Factory method */
    public static JWSProjectProperties getInstance(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        JWSProjectProperties prop = propInstance.get(projDir);
        if(prop == null) {
            prop = new JWSProjectProperties(context);
            propInstance.put(projDir, prop);
        }
        return prop;
    }
    

    /** Getter method */
    public static JWSProjectProperties getInstanceIfExists(Project proj) {
        assert proj != null;
        String projDir = proj.getProjectDirectory().getPath();
        JWSProjectProperties prop = propInstance.get(projDir);
        if(prop != null) {
            return prop;
        }
        return null;
    }

    /** Getter method */
    public static JWSProjectProperties getInstanceIfExists(Lookup context) {
        Project proj = context.lookup(Project.class);
        return getInstanceIfExists(proj);
    }

    public static void cleanup(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        propInstance.remove(projDir);
    }

    // WebStart config change detection
    private boolean lastIsWebStartEnabled = false;

    private boolean needWebStartJarsUpdate() {
        return lastIsWebStartEnabled || enabledModel.isSelected();
    }

    void resetWebStartChanged() {
        lastIsWebStartEnabled = isWebStart(evaluator);
    }
    
    /** Creates a new instance of JWSProjectProperties */
    private JWSProjectProperties(Lookup context) {
        
        project = context.lookup(Project.class);
        
        if (project != null) {
            
            j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            
            evaluator = j2sePropEval.evaluator();
            resetWebStartChanged();

            enabledModel = jnlpPropGroup.createToggleButtonModel(evaluator, JNLP_ENABLED);
            allowOfflineModel = jnlpPropGroup.createToggleButtonModel(evaluator, JNLP_OFFLINE);
            iconDocument = jnlpPropGroup.createStringDocument(evaluator, JNLP_ICON);
            appletWidthDocument = jnlpPropGroup.createStringDocument(evaluator, JNLP_APPLET_WIDTH);
            appletHeightDocument = jnlpPropGroup.createStringDocument(evaluator, JNLP_APPLET_HEIGHT);

            codebaseModel = new CodebaseComboBoxModel();
            codebaseURLDocument = createCBTextFieldDocument();

            appletClassModel = new AppletClassComboBoxModel(project);
            mixedCodeModel = createMixedCodeModel(j2sePropEval.evaluator());
            initRadioButtons();

            initSigning(evaluator);

            extResProperties = readProperties(evaluator, JNLP_EXT_RES_PREFIX, extResSuffixes);
            appletParamsProperties = readProperties(evaluator, JNLP_APPLET_PARAMS_PREFIX, appletParamsSuffixes);
            
            initResources(evaluator, project);                        
        } 
        
    }
    
    boolean isJWSEnabled() {
        return enabledModel.isSelected();
    }

    /**
     * Checks if the JWS was just activated.
     * @return true if the JWS was activated in current properties run.
     */
    boolean wasJWSActivated() {
        return !lastIsWebStartEnabled && isJWSEnabled();
    }

    /**
     * Checks if the JWS was just deactivated.
     * @return true if the JWS was deactivated in current properties run.
     */
    boolean wasJWSDeactivated() {
        return lastIsWebStartEnabled && !enabledModel.isSelected();
    }
    
    public DescType getDescTypeProp() {
        DescType toReturn;
        if (selectedDescType != null) {
            return selectedDescType;
        }
        String desc = evaluator.getProperty(JNLP_DESCRIPTOR);
        if (desc != null) {
            toReturn = DescType.valueOf(desc);
        } else {
            toReturn = DescType.application;
        }
        return toReturn;
    }
    
    public void updateDescType() {
        selectedDescType = getSelectedDescType();
    }
    
    public List<Map<String,String>> getExtResProperties() {
        return extResProperties;
    }
    
    public void setExtResProperties(List<Map<String,String>> props) {
        extResProperties = props;
    }
    
    public List<Map<String,String>> getAppletParamsProperties() {
        return appletParamsProperties;
    }
    
    public void setAppletParamsProperties(List<Map<String,String>> props) {
        appletParamsProperties = props;
    }
    
    private void initRadioButtons() {
        
        applicationDescButtonModel = new ToggleButtonModel();
        appletDescButtonModel = new ToggleButtonModel();
        compDescButtonModel = new ToggleButtonModel();
        bg = new ButtonGroup();
        applicationDescButtonModel.setGroup(bg);
        appletDescButtonModel.setGroup(bg);
        compDescButtonModel.setGroup(bg);
        
        String desc = evaluator.getProperty(JNLP_DESCRIPTOR);
        if (desc != null) {
            if (desc.equals(DescType.application.toString())) {
                applicationDescButtonModel.setSelected(true);
            } else if (desc.equals(DescType.applet.toString())) {
                appletDescButtonModel.setSelected(true);
            } else if (desc.equals(DescType.component.toString())) {
                compDescButtonModel.setSelected(true);
            }
        } else {
            applicationDescButtonModel.setSelected(true);
        }

    }
    
    private void storeRest(EditableProperties editableProps, EditableProperties privProps) {
        // create extended manifest attribute properties if not existing
        if(!editableProps.containsKey(MANIFEST_CUSTOM_CODEBASE) && !privProps.containsKey(MANIFEST_CUSTOM_CODEBASE)) {
            editableProps.setProperty(MANIFEST_CUSTOM_CODEBASE, ""); // NOI18N
            editableProps.setComment(MANIFEST_CUSTOM_CODEBASE, new String[]{"# " + NbBundle.getMessage(JWSProjectProperties.class, "COMMENT_manifest_custom_codebase")}, false); // NOI18N
        }
        if(!editableProps.containsKey(MANIFEST_CUSTOM_PERMISSIONS) && !privProps.containsKey(MANIFEST_CUSTOM_PERMISSIONS)) {
            editableProps.setProperty(MANIFEST_CUSTOM_PERMISSIONS, ""); // NOI18N
            editableProps.setComment(MANIFEST_CUSTOM_PERMISSIONS, new String[]{"# " + NbBundle.getMessage(JWSProjectProperties.class, "COMMENT_manifest_custom_permissions")}, false); // NOI18N
        }
        if(!editableProps.containsKey(MANIFEST_CUSTOM_CALLER_ALLOWABLE_CODEBASE) && !privProps.containsKey(MANIFEST_CUSTOM_CALLER_ALLOWABLE_CODEBASE)) {
            editableProps.setProperty(MANIFEST_CUSTOM_CALLER_ALLOWABLE_CODEBASE, ""); // NOI18N
            editableProps.setComment(MANIFEST_CUSTOM_CALLER_ALLOWABLE_CODEBASE, new String[]{"# " + NbBundle.getMessage(JWSProjectProperties.class, "COMMENT_manifest_custom_caller_allowable_codebase")}, false); // NOI18N
        }
        if(!editableProps.containsKey(MANIFEST_CUSTOM_APPLICATION_LIBRARY_ALLOWABLE_CODEBASE) && !privProps.containsKey(MANIFEST_CUSTOM_APPLICATION_LIBRARY_ALLOWABLE_CODEBASE)) {
            editableProps.setProperty(MANIFEST_CUSTOM_APPLICATION_LIBRARY_ALLOWABLE_CODEBASE, ""); // NOI18N
            editableProps.setComment(MANIFEST_CUSTOM_APPLICATION_LIBRARY_ALLOWABLE_CODEBASE, new String[]{"# " + NbBundle.getMessage(JWSProjectProperties.class, "COMMENT_manifest_custom_application_library_allowable_codebase")}, false); // NOI18N
        }
        // store codebase type
        String selItem = ((CodebaseComboBoxModel) codebaseModel).getSelectedCodebaseItem();
        String propName = null;
        String propValue = null;
        if (CB_TYPE_USER.equals(selItem)) {
            propName = JNLP_CBASE_USER;
            try {
                propValue = codebaseURLDocument.getText(0, codebaseURLDocument.getLength());
            } catch (BadLocationException ex) {
                // do not store anything
                // XXX log the exc
                return;
            }
        } else if (CB_TYPE_LOCAL.equals(selItem)) {
            // #161919: local codebase will be computed
            //propName = JNLP_CBASE_URL;
            //propValue = getProjectDistDir();
        } else if (CB_TYPE_WEB.equals(selItem))  {
            propName = JNLP_CBASE_URL;
            propValue = CB_URL_WEB_PROP_VALUE;
        }
        editableProps.setProperty(JNLP_CBASE_TYPE, selItem);
        if (propName != null && propValue != null) {
            editableProps.setProperty(propName, propValue);
        }
        // store applet class name and default applet size
        String appletClassName = (String) appletClassModel.getSelectedItem();
        if (appletClassName != null && !appletClassName.equals("")) {
            editableProps.setProperty(JNLP_APPLET, appletClassName);
            String appletWidth = null;
            try {
                appletWidth = appletWidthDocument.getText(0, appletWidthDocument.getLength());
            } catch (BadLocationException ex) {
                // appletWidth will be null
            }
            if (appletWidth == null || "".equals(appletWidth)) {
                editableProps.setProperty(JNLP_APPLET_WIDTH, DEFAULT_APPLET_WIDTH);
            }
            String appletHeight = null;
            try {
                appletHeight = appletHeightDocument.getText(0, appletHeightDocument.getLength());
            } catch (BadLocationException ex) {
                // appletHeight will be null
            }
            if (appletHeight == null || "".equals(appletHeight)) {
                editableProps.setProperty(JNLP_APPLET_HEIGHT, DEFAULT_APPLET_HEIGHT);
            }
        }
        // store descriptor type
        DescType descType = getSelectedDescType();
        if (descType != null) {
            editableProps.setProperty(JNLP_DESCRIPTOR, descType.toString());
        }

        //Store Mixed Code
        final MixedCodeOptions option = (MixedCodeOptions) mixedCodeModel.getSelectedItem();
        editableProps.setProperty(JNLP_MIXED_CODE, option.getPropertyValue());
        //Store jar indexing
        if (editableProps.getProperty(JAR_INDEX) == null) {
            editableProps.setProperty(JAR_INDEX, String.format("${%s}", JNLP_ENABLED));   //NOI18N
        }
        if (editableProps.getProperty(JAR_ARCHIVE_DISABLED) == null) {
            editableProps.setProperty(JAR_ARCHIVE_DISABLED, String.format("${%s}", JNLP_ENABLED));  //NOI18N
        }
        // store signing info
        editableProps.setProperty(JNLP_SIGNING, signing);
        editableProps.setProperty(JNLP_SIGNED, "".equals(signing) ? "false" : "true"); //NOI18N
        setOrRemove(editableProps, JNLP_SIGNING_KEY, SIGNING_GENERATED.equals(signing) ? getShortProjectName() : signingKeyAlias);
        setOrRemove(editableProps, JNLP_SIGNING_KEYSTORE, signingKeyStore);
        setOrRemove(privProps, JNLP_SIGNING_KEYSTORE_PASSWORD, signingKeyStorePassword);
        setOrRemove(privProps, JNLP_SIGNING_KEY_PASSWORD, signingKeyPassword);
        
        // store resources
        storeResources(editableProps);

        // store properties
        storeProperties(editableProps, extResProperties, JNLP_EXT_RES_PREFIX);
        storeProperties(editableProps, appletParamsProperties, JNLP_APPLET_PARAMS_PREFIX);
    }

    /**
     * Creates short name from project name to be used as keystore name and alias
     * @return String with shortened project name
     */
    private String getShortProjectName() {
        final int maxLen = 8; // given by old DOS max file name length
        FileObject projDir = project.getProjectDirectory();
        String name = projDir.getName();
        assert name != null;
        if(name.length() <= maxLen) { 
            return name;
        }
        String result = "";
        final double ratio = (double)name.length() / (double)maxLen;
        for(int i = 1; i <= maxLen; i++) {
            assert (int)Math.floor( (double)(i - 1) * ratio ) >= 0;
            assert (int)Math.ceil( (double)i * ratio ) <= name.length();
            result += name.charAt( i <= maxLen/2 ? (int)Math.floor( (double)(i - 1) * ratio ) : (int)Math.ceil( (double)i * ratio )-1 );
        }
        return result;
    }
    
    private void setOrRemove(EditableProperties props, String name, char [] value) {
        setOrRemove(props, name, value != null ? new String(value) : null);
    }

    private void setOrRemove(EditableProperties props, String name, String value) {
        if (value != null) {
            props.setProperty(name, value);
        } else {
            props.remove(name);
        }
    }
            
    public void store() throws IOException {
        
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final EditableProperties pep = new EditableProperties(true);
        final FileObject privPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        
        try {
            final InputStream is = projPropsFO.getInputStream();
            final InputStream pis = privPropsFO.getInputStream();
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    try {
                        pep.load(pis);
                    } finally {
                        if (pis != null) {
                            pis.close();
                        }
                    }
                    jnlpPropGroup.store(ep);
                    storeRest(ep, pep);
                    if(needWebStartJarsUpdate()) {
                        updateWebStartJarsOnChange(ep, evaluator, isJWSEnabled());
                    }
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    try {
                        lock = privPropsFO.lock();
                        os = privPropsFO.getOutputStream(lock);
                        pep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    //updateWebStartJars(project, evaluator);
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        } 
    }

    public static void updateOnOpen(final Project project, final PropertyEvaluator eval) throws IOException {

        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        try {
            final InputStream is = projPropsFO.getInputStream();
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    updateWebStartJarsOnOpen(ep, eval, isWebStart(eval));
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }

    }

    private DescType getSelectedDescType() {
        DescType toReturn = null;
        if (applicationDescButtonModel.isSelected()) {
            toReturn = DescType.application;
        } else if (appletDescButtonModel.isSelected()) {
            toReturn = DescType.applet;
        } else if (compDescButtonModel.isSelected()) {
            toReturn = DescType.component;
        }
        return toReturn;
    }
    
    private Document createCBTextFieldDocument() {
        Document doc = new PlainDocument();
        String valueType = evaluator.getProperty(JNLP_CBASE_TYPE);
        String docString = "";
        if (CB_TYPE_LOCAL.equals(valueType)) {
            docString = getProjectDistDir();
        } else if (CB_TYPE_WEB.equals(valueType)) {
            docString = CB_URL_WEB;
        } else if (CB_TYPE_USER.equals(valueType)) {
            docString = getCodebaseLocation();
        }
        try {
            doc.insertString(0, docString, null);
        } catch (BadLocationException ex) {
            // do nothing, just return PlainDocument
            // XXX log the exc
        }
        return doc;
    }
    
    public String getCodebaseLocation() {
        return evaluator.getProperty(JNLP_CBASE_USER);
    }
        
    public String getProjectDistDir() {
        String dD = evaluator.getProperty("dist.dir"); // NOI18N
        File distDir = new File(FileUtil.toFile(project.getProjectDirectory()), dD != null ? dD : ""); // NOI18N
        return distDir.toURI().toString();
    }
    
    // only should return JNLP properties
    public String getProperty(String propName) {
        return evaluator.getProperty(propName);
    }

    // ----------
    
    public class CodebaseComboBoxModel extends DefaultComboBoxModel {
        
        final String localLabel = NbBundle.getMessage(JWSProjectProperties.class, "LBL_CB_Combo_Local"); //NOI18N
        final String webLabel = NbBundle.getMessage(JWSProjectProperties.class, "LBL_CB_Combo_Web"); //NOI18N
        final String userLabel = NbBundle.getMessage(JWSProjectProperties.class, "LBL_CB_Combo_User"); //NOI18N
        final String noCodeBaseLabel = NbBundle.getMessage(JWSProjectProperties.class, "LBL_CB_No_Codebase"); //NOI18N
        final String visItems[] = new String[] { noCodeBaseLabel, localLabel, webLabel, userLabel};
        final String cbItems[] = new String[] { CB_NO_CODEBASE, CB_TYPE_LOCAL, CB_TYPE_WEB, CB_TYPE_USER};
        
        public CodebaseComboBoxModel() {
            super();
            for (String visItem : visItems) {
                addElement(visItem);
            }
            String propValue = evaluator.getProperty(JNLP_CBASE_TYPE);
            for (int i=0; i<cbItems.length; i++) {
                if (cbItems[i].equals(propValue)) {
                    setSelectedItem(visItems[i]);
                    break;
                }
            }
        }
        
        public String getSelectedCodebaseItem() {
            return cbItems[getIndexOf(getSelectedItem())];
        }
        
    }

    public class AppletClassComboBoxModel extends DefaultComboBoxModel {
        
        Set<SearchKind> kinds = new HashSet<SearchKind>(Arrays.asList(SearchKind.IMPLEMENTORS));
        Set<SearchScope> scopes = new HashSet<SearchScope>(Arrays.asList(SearchScope.SOURCE));
        
        public AppletClassComboBoxModel(final Project proj) {
            
            Sources sources = ProjectUtils.getSources(proj);
            SourceGroup[] srcGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            final Map<FileObject,List<ClassPath>> classpathMap = new HashMap<FileObject,List<ClassPath>>();
            
            for (SourceGroup srcGroup : srcGroups) {
                FileObject srcRoot = srcGroup.getRootFolder();
                ClassPath bootCP = ClassPath.getClassPath(srcRoot, ClassPath.BOOT);
                ClassPath executeCP = ClassPath.getClassPath(srcRoot, ClassPath.EXECUTE);
                ClassPath sourceCP = ClassPath.getClassPath(srcRoot, ClassPath.SOURCE);
                List<ClassPath> cpList = new ArrayList<ClassPath>();
                if (bootCP != null) {
                    cpList.add(bootCP);
                }
                if (executeCP != null) {
                    cpList.add(executeCP);
                }
                if (sourceCP != null) {
                    cpList.add(sourceCP);
                }
                if (cpList.size() == 3) {
                    classpathMap.put(srcRoot, cpList);
                }
            }
            
            final Set<String> appletNames = new HashSet<String>();
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    for (FileObject fo : classpathMap.keySet()) {
                        List<ClassPath> paths = classpathMap.get(fo);
                        ClasspathInfo cpInfo = ClasspathInfo.create(paths.get(0), paths.get(1), paths.get(2));
                        final ClassIndex classIndex = cpInfo.getClassIndex();
                        final JavaSource js = JavaSource.create(cpInfo);
                        try {
                            js.runUserActionTask(new CancellableTask<CompilationController>() {
                                public void run(CompilationController controller) throws Exception {
                                    Elements elems = controller.getElements();
                                    TypeElement appletElement = elems.getTypeElement("java.applet.Applet");
                                    ElementHandle<TypeElement> appletHandle = ElementHandle.create(appletElement);
                                    TypeElement jappletElement = elems.getTypeElement("javax.swing.JApplet");
                                    ElementHandle<TypeElement> jappletHandle = ElementHandle.create(jappletElement);
                                    Set<ElementHandle<TypeElement>> appletHandles = classIndex.getElements(appletHandle, kinds, scopes);
                                    for (ElementHandle<TypeElement> elemHandle : appletHandles) {
                                        appletNames.add(elemHandle.getQualifiedName());
                                    }
                                    Set<ElementHandle<TypeElement>> jappletElemHandles = classIndex.getElements(jappletHandle, kinds, scopes);
                                    for (ElementHandle<TypeElement> elemHandle : jappletElemHandles) {
                                        appletNames.add(elemHandle.getQualifiedName());
                                    }
                                }
                                public void cancel() {
                                    
                                }
                            }, true);
                        } catch (Exception e) {
                            
                        }

                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            addElements(appletNames);
                            String appletClassName = evaluator.getProperty(JNLP_APPLET);
                            if (appletClassName != null && appletNames.contains(appletClassName)) {
                                setSelectedItem(appletClassName);
                            }
                        }
                    });
                }
            });
        }
        
        private void addElements(Set<String> elems) {
            for (String elem : elems) {
                addElement(elem);
            }
        }
        
    }
    
    public static class PropertiesTableModel extends AbstractTableModel {
        
        private List<Map<String,String>> properties;
        private String propSuffixes[];
        private String columnNames[];
        
        public PropertiesTableModel(List<Map<String,String>> props, String sfxs[], String clmns[]) {
            if (sfxs.length != clmns.length) {
                throw new IllegalArgumentException();
            }
            properties = props;
            propSuffixes = sfxs;
            columnNames = clmns;
        }
        
        public int getRowCount() {
            return properties.size();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            properties.get(rowIndex).put(propSuffixes[columnIndex], (String) aValue);
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            return properties.get(rowIndex).get(propSuffixes[columnIndex]);
        }
        
        public void addRow() {
            Map<String,String> emptyMap = new HashMap<String,String>();
            for (String  suffix : propSuffixes) {
                emptyMap.put(suffix, "");
            }
            properties.add(emptyMap);
        }
        
        public void removeRow(int index) {
            properties.remove(index);
        }

    }
    
    // ----------
    
    private static List<Map<String,String>> readProperties(PropertyEvaluator evaluator, String propPrefix, String[] propSuffixes) {
        
        ArrayList<Map<String,String>> listToReturn = new ArrayList<Map<String,String>>();
        int index = 0;
        while (true) {
            HashMap<String,String> map = new HashMap<String,String>();
            int numProps = 0;
            for (String propSuffix : propSuffixes) {
                String propValue = evaluator.getProperty(propPrefix + index + "." + propSuffix);
                if (propValue != null) {
                    map.put(propSuffix, propValue);
                    numProps++;
                }
            }
            if (numProps == 0) {
                break;
            }
            listToReturn.add(map);
            index++;
        }
        return listToReturn;
        
    }
    
    private static void storeProperties(EditableProperties editableProps, List<Map<String,String>> newProps, String prefix) {
        
        int propGroupIndex = 0;
        // find all properties with the prefix
        Set<String> keys = editableProps.keySet();
        Set<String> keys2Remove = new HashSet<String>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                keys2Remove.add(key);
            }
        }
        // remove all props with the prefix first
        editableProps.keySet().removeAll(keys2Remove);

        // and now save passed list
        for (Map<String,String> map : newProps) {
            // if all values in the map are empty do not store
            boolean allEmpty = true;
            for (String val : map.values()) {
                if (val != null && !val.equals("")) {
                    allEmpty = false;
                    break;
                }
            }
            if (!allEmpty) {
                for (String key : map.keySet()) {
                    String value = map.get(key);
                    String propName = prefix + propGroupIndex + "." + key;
                    editableProps.setProperty(propName, value);
                }
            }
            propGroupIndex++;
        }
    }

    private static enum MixedCodeOptions {
        DEFAULT("default"),  //NOI18N
        TRUSTED_ONLY("trusted_only"),   //NOI18N
        TRUSTED_LIBRARY("trusted_library"); //NOI18N

        private final String propValue;

        private MixedCodeOptions(final String propValue) {
            this.propValue = propValue;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(JWSCustomizerPanel.class, String.format("TXT_MIXED_MODE_%s",name()));
        }

        public String getPropertyValue() {
            return this.propValue;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        static MixedCodeOptions fromPropertyValue(final String propValue) {
            assert propValue != null;
            for (MixedCodeOptions option : MixedCodeOptions.values()) {
                if (propValue.equals(option.getPropertyValue())) {
                    return option;
                }
            }
            return null;
        }
    }

    private void initSigning(PropertyEvaluator eval) {
        signing = eval.getProperty(JNLP_SIGNING);
        if (signing == null) signing = "";
        signingKeyStore = eval.getProperty(JNLP_SIGNING_KEYSTORE);
        if (signingKeyStore == null) signingKeyStore = "";
        signingKeyAlias = eval.getProperty(JNLP_SIGNING_KEY);
        if (signingKeyAlias == null) signingKeyAlias = "";
        if (eval.getProperty(JNLP_SIGNING_KEYSTORE_PASSWORD) != null) {
            signingKeyStorePassword = eval.getProperty(JNLP_SIGNING_KEYSTORE_PASSWORD).toCharArray();
        }
        if (eval.getProperty(JNLP_SIGNING_KEY_PASSWORD) != null) {
            signingKeyPassword = eval.getProperty(JNLP_SIGNING_KEY_PASSWORD).toCharArray();
        }
        // compatibility
        if ("".equals(signing) && "true".equals(eval.getProperty(JNLP_SIGNED))) {
            signing = SIGNING_GENERATED;
        }
    }
    
    private void initResources (final PropertyEvaluator eval, final Project prj) {
        final String lz = eval.getProperty(JNLP_LAZY_JARS); //old way, when changed rewritten to new
        final String rcp = eval.getProperty(RUN_CP);        
        final String bc = eval.getProperty(BUILD_CLASSES);        
        final File prjDir = FileUtil.toFile(prj.getProjectDirectory());
        final File bcDir = bc == null ? null : PropertyUtils.resolveFile(prjDir, bc);
        final List<File> lazyFileList = new ArrayList<File>();
        if (lz != null) {
            for (String p : PropertyUtils.tokenizePath(lz)) {
                lazyFileList.add(PropertyUtils.resolveFile(prjDir, p));
            }
        }
        final List<File> resFileList = new ArrayList<File>();
        if(rcp != null) {
            for (String p : PropertyUtils.tokenizePath(rcp)) {
                if (p.startsWith("${") && p.endsWith("}")) {    //NOI18N
                    continue;
                }
                final File f = PropertyUtils.resolveFile(prjDir, p);
                if (bc == null || !bcDir.equals(f)) {
                    resFileList.add(f);
                    if (isTrue(eval.getProperty(String.format(JNLP_LAZY_FORMAT, f.getName())))) {
                        lazyFileList.add(f);
                    }
                }
            }
        }
        lazyJars = lazyFileList;
        runtimeCP = resFileList;
        lazyJarsChanged = false;
    }
    
    private void storeResources(final EditableProperties props) {
        if (lazyJarsChanged) {
            //Remove old way if exists
            props.remove(JNLP_LAZY_JARS);
            final Iterator<Map.Entry<String,String>> it = props.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getKey().startsWith(JNLP_LAZY_JAR)) {
                    it.remove();
                }
            }
            for (File lazyJar : lazyJars) {
                props.setProperty(String.format(JNLP_LAZY_FORMAT, lazyJar.getName()), "true");  //NOI18N
            }
        }
    }

    private static ComboBoxModel createMixedCodeModel (final PropertyEvaluator eval) {
        assert eval != null;
        final DefaultComboBoxModel<MixedCodeOptions> model = new DefaultComboBoxModel<>();
        for (MixedCodeOptions option : MixedCodeOptions.values()) {
            model.addElement(option);
        }
        final String strValue = eval.getProperty(JNLP_MIXED_CODE);
        final MixedCodeOptions value = strValue == null ? null : MixedCodeOptions.fromPropertyValue(strValue);
        if (value != null) {
            model.setSelectedItem(value);
        }
        return model;
    }

    // ----------

    /**
     * Tokenize classpath read from project.properties
     *
     * @param ep EditableProperties to access raw property contents
     * @param eval PropertyEvaluator to access dereferenced property contents
     * @param classPathProp name of classpath property to read from
     * @return collection of path pairs, key is the raw item, value is the dereferenced item
     */
    private static Map<String,String> getClassPathItems(final EditableProperties ep, final PropertyEvaluator eval, String classPathProp) {
        String cpEdit = ep.getProperty(classPathProp);
        String cpEval = eval.getProperty(classPathProp);
        String pEdit[] = PropertyUtils.tokenizePath( cpEdit == null ? "" : cpEdit ); // NOI18N
        String pEval[] = PropertyUtils.tokenizePath( cpEval == null ? "" : cpEval ); // NOI18N
        if(pEdit.length != pEval.length) {
            LOG.log(Level.WARNING, NbBundle.getMessage(JWSProjectProperties.class, "ERR_ClassPathProblem", classPathProp)); //NOI18N
        }
        Map<String,String> map = new LinkedHashMap<String,String>();
        for(int i = 0; i < pEdit.length && i < pEval.length; i++) {
            map.put(pEdit[i], pEval[i]);
        }
        return map;
    }

    /**
     * Filters out from map all items referring file name and returns list of those filtered
     * items that actually exist
     *
     * @param map obtained using getClassPathItems()
     * @param name file name of library to check (javaws.jar, plugin.jar)
     * @return collection of path pairs (verified to exist, specified by name), key is the raw item, value is the dereferenced item
     * @throws IOException
     */
    private static Map<String,String> filterOutLibItems(Map<String,String> map, String name) throws IOException {
        Map<String,String> res = new LinkedHashMap<String,String>();
        final Iterator<Map.Entry<String,String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,String> entry = it.next();
            if (entry.getValue().endsWith(name)) {
                it.remove();
                String path = classPathItemExistsCanonical(entry.getValue());
                if(path != null) {
                    res.put(entry.getKey(), path);
                }
            }
        }
        return res;
    }

    /**
     * Returns canonical path to item if it exists
     * items that actually exist
     *
     * @param item path to existing file
     * @return canonical path to item if it exists, null otherwise
     * @throws IOException
     */
    private static String classPathItemExistsCanonical(String item) throws IOException {
        if(item != null) {
            final File itemFile = new File(item);
            if(itemFile.exists()) {
                return itemFile.getCanonicalPath();
            }
        }
        return null;
    }

     /**
     * Creates array of String paths to be passed to a classpath-defining property,
     * out of a collection of String paths
     *
     * @param items collection of paths representing existing files
     * @return String array that can be passed to setProperty()
     */
    private static String[] createClassPathProperty(Collection<String> items) {
        final int size = items.size();
        final String[] result = new String[size];
        final Iterator<String> itemIt = items.iterator();
        for (int i = 0; itemIt.hasNext(); i++) {
            result[i] = String.format(
                (i == size - 1) ?
                    "%s" :  //NOI18N
                    "%s:",  //NOI18N
                itemIt.next());
        }        
        return result;
    }

    /**
     * Returns the name of the current JavaPlatform as defined in project
     * properties file, provided the platform exists. Otherwise returns null.
     * The returned name is usable when accessing Ant properties/scripts
     *
     * @param eval PropertyEvaluator to read the name from project.properties
     * @return name of current JavaPlatform as specified in project or null if such platform does not exist
     */
    private static String getActivePlatform(final PropertyEvaluator eval) {
        final String platformName = eval.getProperty("platform.active"); //NOI18N
        if (platformName != null) {
            JavaPlatform active = null;
            for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                if (platformName.equals(platform.getProperties().get("platform.ant.name"))) { //NOI18N
                    active = platform;
                    break;
                }
            }
            if(active != null) {
                return platformName;
            }
        }
        return null;
    }

    /**
     * Returns path to WS library file 'name' in JavaPlatform 'platform'
     * in two forms: referenced (suitable as property value) and dereferenced
     * (suitable for actual file access)
     *
     * @param eval PropertyEvaluator to read the name from project.properties
     * @param platform Ant name of JavaPlatform in which 'name' is to be searched for
     * @param name file name of library to check (javaws.jar, plugin.jar)
     * @return pair of Strings representing the same path, key is the referenced path, value is the dereferenced path
     * @throws IOException
     */
    private static String[] getPreferredPlatformLib(final PropertyEvaluator eval, final String platform, final String name) throws IOException {
        String path[] = null;
        if(platform != null && !platform.equals(DEFAULT_PLATFORM)) {
            String platformProp = "platforms." + platform + ".home"; //NOI18N
            if(eval.getProperty(platformProp) == null) {
                LOG.log(Level.WARNING, NbBundle.getMessage(JWSProjectProperties.class, "ERR_MissingPlatformLocation", platform)); //NOI18N
            } else {
                path = findLib(eval, platformProp, name, false);
            }
        }
        if(path == null) {
            path = findLib(eval, "java.home", name, true); //NOI18N
        }
        if(path == null) {
            LOG.log(Level.WARNING, NbBundle.getMessage(JWSProjectProperties.class, "ERR_MissingPlatformLib", platform, name)); //NOI18N
        }
        return path;
    }

    /**
     * Adds source Map to the front of order-preserving Map
     *
     * @param map Map to be prepended by new record
     * @param key together with value will be added as first record in map
     * @param value together with key will be added as first record in map
     * @return map with prepended key,value record
     */
    private static Map<String,String> putToFront(Map<String,String> map, String key, String value) {
        Map<String,String> res = new LinkedHashMap<String, String>();
        res.put(key, value);
        res.putAll(map);
        return res;
    }

    /**
     * Adds source Map to the front of order-preserving Map
     *
     * @param map Map to be prepended by the Map 'source'
     * @param source Map to be included at the front of ordered 'map'
     * @return union Map of 'source' and 'map' with 'source' prependng 'map'
     */
    private static Map<String,String> putToFront(Map<String,String> map, Map<String,String> source) {
        Map<String,String> res = new LinkedHashMap<String, String>(source);
        res.putAll(map);
        return res;
    }

    /**
     * Replaces the 'name' library by a path to 'name' library in the Map classpath representation in preferred
     * form with respect to JavaPlatform 'platform'. If the library file can not
     * be found, issues a warning and disables WS because the inability to find
     * the library file indicates that WebStart can not be correctly configured
     * for the current project.
     *
     * @param map Map representing classpath items
     * @param ep EditableProperties to enable property setting
     * @param eval PropertyEvaluator to read the name from project.properties
     * @param platform Ant name of JavaPlatform in which 'name' is to be searched for
     * @param name file name of library to check (javaws.jar, plugin.jar)
     * @return 'map' with added item representing the preferred format of path to library 'name'
     * @throws IOException
     */
    private static Map<String,String> addPreferredLib(Map<String,String> map, final EditableProperties ep, final PropertyEvaluator eval, String platform, String name) throws IOException {
        String[] path = getPreferredPlatformLib(eval, platform, name);
        if(path != null) {
            if(map.get(path[0]) == null &&
               map.get(path[1]) == null) {
                map = new HashMap<>();
                map.put(path[0], path[1]);
            }
        } else {
            LOG.log(Level.WARNING, NbBundle.getMessage(JWSProjectProperties.class, "ERR_LibFileMissing", name)); //NOI18N
            ep.setProperty(JNLP_ENABLED, "false"); //NOI18N
        }
        return map;
    }

   /**
     * On opening of project with enabled WebStart,
     * verifies the existence of files referenced by the endorsed.classpath
     * property and removes those that do not exist. Then verifies that all active WebStart
     * libraries (javaws.jar and/or plugin.jar) relevant for the active platform
     * are already referenced in endosed.classpath; if not, the missing references
     * are added in ${path-to-platform}/path/to/library form
     *
     * @param ep EditableProperties to enable access to row property form and property setting
     * @param eval PropertyEvaluator to read the name from project.properties
     * @param isWebStart determines whether WebStart is considered active, affects whether lib files will be added or removed
     * @throws IOException
     */
    public static void updateWebStartJarsOnOpen(final EditableProperties ep, final PropertyEvaluator eval, boolean isWebStart) throws IOException {
        Map<String,String> map = getClassPathItems(ep, eval, ENDORSED_CLASSPATH);
        Map<String,String> wsmap = filterOutLibItems(map, LIB_JAVAWS);
        Map<String,String> pnmap = filterOutLibItems(map, LIB_PLUGIN);
        if(isWebStart) {
            String active = getActivePlatform(eval);
            if(isApplet(ep)) {
                pnmap = addPreferredLib(pnmap, ep, eval, active, LIB_PLUGIN);
            }
            wsmap = addPreferredLib(wsmap, ep, eval, active, LIB_JAVAWS);
            map = putToFront( putToFront(map, pnmap), wsmap);
        }
        ep.setProperty(ENDORSED_CLASSPATH, createClassPathProperty(map.keySet()));
    }

   /**
     * On closing the Project Properties dialog by pressing OK
     * updates all WebStart lib references in endorsed.classpath property
     * with respect to current JavaPlatform.
     * (in the form ${path-to-platform}/path/to/library)
     *
     * @param ep EditableProperties to enable access to row property form and property setting
     * @param eval PropertyEvaluator to read the name from project.properties
     * @param isWebStart determines whether WebStart is considered active, affects whether lib files will be added or removed
     * @throws IOException
     */
    public static void updateWebStartJarsOnChange(final EditableProperties ep, final PropertyEvaluator eval, boolean isWebStart) throws IOException {
        Map<String,String> map = getClassPathItems(ep, eval, ENDORSED_CLASSPATH);
        filterOutLibItems(map, LIB_JAVAWS);
        filterOutLibItems(map, LIB_PLUGIN);
        if(isWebStart) {
            Map<String,String> wsmap = new LinkedHashMap<String, String>();
            Map<String,String> pnmap = new LinkedHashMap<String, String>();
            String active = getActivePlatform(eval);
            if(isApplet(ep)) {
                pnmap = addPreferredLib(pnmap, ep, eval, active, LIB_PLUGIN);
            } else {
                pnmap.clear();
            }
            wsmap = addPreferredLib(wsmap, ep, eval, active, LIB_JAVAWS);
            map = putToFront( putToFront(map, pnmap), wsmap);
        }
        ep.setProperty(ENDORSED_CLASSPATH, createClassPathProperty(map.keySet()));
    }
        
    /**
     * Returns Mac path to WS library if it exists
     *
     * @param name file name of library to check (javaws.jar, plugin.jar)
     * @return path to library if it exists or null
     * @throws IOException
     */
    private static String findLibMac(final String name) throws IOException {
        //On Mac deploy is fixed, attempting to find javaws.jar and plugin.jar at various fallback locations
        String[] macFolders={
            "/System/Library/Java/Support/Deploy.bundle/Contents/Home/lib/", //NOI18N
            "/System/Library/Frameworks/JavaVM.framework/Resources/Deploy.bundle/Contents/Home/lib/", // NOI18N
            "/System/Library/Java/Support/Deploy.bundle/Contents/Resources/Java/", // NOI18N
            "/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/"}; // NOI18N
        for(String s : macFolders) {
            final File deployFramework = new File(s);
            final File lib = FileUtil.normalizeFile(new File(deployFramework,name));
            if(lib.exists()) {
                return lib.getPath();
            }
        }
        return null;
    }

    /**
     * Returns path (referenced if possible) to WS library if it exists
     *
     * @param eval PropertyEvaluator to access dereferenced property contents
     * @param folderProp property containing path to Java platform
     * @param name file name of library to check (javaws.jar, plugin.jar)
     * @return path to library if it exists or null, if possible the path will be referenced by platform property
     * @throws IOException
     */
    private static String[] findLib(
            final PropertyEvaluator eval,
            final String folderProp,
            final String name,
            final boolean defaultPlatform) throws IOException {
        final String folder = eval.getProperty(folderProp);
        if(folder != null) {
            final File deployFramework = new File(folder);
            final String jreLibPath = defaultPlatform ?
                    "/lib/" :        //NOI18N
                    "/jre/lib/";    //NOI18N
            final File lib = FileUtil.normalizeFile(new File(deployFramework,jreLibPath+ name)); //NOI18N
            if(lib.exists()) {
                String[] res = new String[2];
                res[0] = String.format(
                    "${%s}%s%s",    //NOI18N
                    folderProp,
                    jreLibPath,
                    name
                );
                res[1] = lib.getCanonicalPath();
                return res;
            }
        }
        return null;
    }
    

    private static boolean isWebStart (final PropertyEvaluator eval) {
        assert eval != null;
        return isTrue(eval.getProperty(JNLP_ENABLED));
    }

    private static boolean isApplet(final PropertyEvaluator eval) {
        return DescType.applet.toString().equals(eval.getProperty(JNLP_DESCRIPTOR));
    }

    private static boolean isApplet(final EditableProperties ep) {
        return DescType.applet.toString().equals(ep.getProperty(JNLP_DESCRIPTOR));
    }

    public static boolean isTrue(final String value) {
        return value != null &&
                (value.equalsIgnoreCase("true") ||  //NOI18N
                 value.equalsIgnoreCase("yes") ||   //NOI18N
                 value.equalsIgnoreCase("on"));     //NOI18N
    }
}
