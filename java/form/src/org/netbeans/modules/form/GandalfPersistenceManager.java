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

package org.netbeans.modules.form;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.*;
import org.openide.xml.XMLUtil;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.layoutsupport.delegates.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.support.SwingLayoutBuilder;

import org.netbeans.modules.form.editors.EnumEditor;
import org.netbeans.modules.java.source.queries.api.QueryException;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;
import org.openide.util.TopologicalSortException;
import org.w3c.dom.Comment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * XML persistence manager - responsible for saving/loading forms to/from XML.
 * The class contains lots of complicated code with many hacks ensuring full
 * compatibility of the format despite that many original classes already don't
 * exist (e.g. FormInfo and DesignLayout and subclasses).
 *
 * @author Ian Formanek, Tomas Pavek
 */

public class GandalfPersistenceManager extends PersistenceManager {
    private static final String NB32_VERSION = "1.0"; // NOI18N
    private static final String NB33_VERSION = "1.1"; // NOI18N
    private static final String NB34_VERSION = "1.2"; // NOI18N
    private static final String NB50_VERSION = "1.3"; // NOI18N
    private static final String NB60_PRE_VERSION = "1.4"; // NOI18N
    private static final String NB60_VERSION = "1.5"; // NOI18N
    private static final String NB61_VERSION = "1.6"; // NOI18N
    private static final String NB65_VERSION = "1.7"; // NOI18N
    private static final String NB71_VERSION = "1.8"; // NOI18N
    private static final String NB74_VERSION = "1.9"; // NOI18N

    // XML elements names
    static final String XML_FORM = "Form"; // NOI18N
    static final String XML_NON_VISUAL_COMPONENTS = "NonVisualComponents"; // NOI18N
    static final String XML_CONTAINER = "Container"; // NOI18N
    static final String XML_COMPONENT = "Component"; // NOI18N
    static final String XML_COMPONENT_REF = "ComponentRef"; // NOI18N
    static final String XML_MENU_COMPONENT = "MenuItem"; // NOI18N
    static final String XML_MENU_CONTAINER = "Menu"; // NOI18N
    static final String XML_LAYOUT = "Layout"; // NOI18N
    static final String XML_LAYOUT_CODE = "LayoutCode"; // NOI18N
    static final String XML_CONSTRAINTS = "Constraints"; // NOI18N
    static final String XML_CONSTRAINT = "Constraint"; // NOI18N
    static final String XML_SUB_COMPONENTS = "SubComponents"; // NOI18N
    static final String XML_EVENTS = "Events"; // NOI18N
    static final String XML_EVENT = "EventHandler"; // NOI18N
    static final String XML_PROPERTIES = "Properties"; // NOI18N
    static final String XML_PROPERTY = "Property"; // NOI18N
    static final String XML_VALUE = "Value"; // NOI18N
    static final String XML_SYNTHETIC_PROPERTY = "SyntheticProperty"; // NOI18N
    static final String XML_SYNTHETIC_PROPERTIES = "SyntheticProperties"; // NOI18N
    static final String XML_BINDING_PROPERTY = "BindingProperty"; // NOI18N
    static final String XML_SUBBINDING = "Subbinding"; // NOI18N
    static final String XML_BINDING_PARAMETER = "BindingParameter"; // NOI18N
    static final String XML_BINDING_PROPERTIES = "BindingProperties"; // NOI18N
    static final String XML_AUX_VALUES = "AuxValues"; // NOI18N
    static final String XML_AUX_VALUE = "AuxValue"; // NOI18N
    static final String XML_A11Y_PROPERTIES = "AccessibilityProperties"; // NOI18N
    static final String XML_SERIALIZED_PROPERTY_VALUE = "SerializedValue"; // NOI18N
    static final String XML_CODE_EXPRESSION = "CodeExpression"; // NOI18N
    static final String XML_CODE_VARIABLE = "CodeVariable"; // NOI18N
    static final String XML_CODE_ORIGIN = "ExpressionOrigin"; // NOI18N
    static final String XML_CODE_STATEMENT = "CodeStatement"; // NOI18N
    static final String XML_CODE_PARAMETERS = "Parameters"; // NOI18N
    static final String XML_CODE_STATEMENTS = "Statements"; // NOI18N
    static final String XML_ORIGIN_META_OBJECT = "ExpressionProvider"; // NOI18N
    static final String XML_STATEMENT_META_OBJECT = "StatementProvider"; // NOI18N
    static final String XML_CODE_CONSTRUCTOR = "CodeConstructor"; // NOI18N
    static final String XML_CODE_METHOD = "CodeMethod"; // NOI18N
    static final String XML_CODE_FIELD = "CodeField"; // NOI18N
    static final String XML_PROPERTY_BEAN = "PropertyBean"; // NOI18N    
    
    // XML attributes names
    static final String ATTR_FORM_VERSION = "version"; // NOI18N
    static final String ATTR_MAX_FORM_VERSION = "maxVersion"; // NOI18N
    static final String ATTR_FORM_TYPE = "type"; // NOI18N
    static final String ATTR_COMPONENT_NAME = "name"; // NOI18N
    static final String ATTR_COMPONENT_CLASS = "class"; // NOI18N
    static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
    static final String ATTR_PROPERTY_TYPE = "type"; // NOI18N
    static final String ATTR_PROPERTY_RES_KEY ="resourceKey"; // NOI18N
    static final String ATTR_PROPERTY_NORES = "noResource"; // NOI18N
    static final String ATTR_PROPERTY_EDITOR = "editor"; // NOI18N
    static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
    static final String ATTR_PROPERTY_INVALID_XML_CHARS = "containsInvalidXMLChars"; // NOI18N
    static final String ATTR_PROPERTY_PRE_CODE = "preCode"; // NOI18N
    static final String ATTR_PROPERTY_POST_CODE = "postCode"; // NOI18N
    static final String ATTR_BINDING_SOURCE = "source"; // NOI18N
    static final String ATTR_BINDING_TARGET = "target"; // NOI18N
    static final String ATTR_BINDING_SOURCE_PATH = "sourcePath"; // NOI18N
    static final String ATTR_BINDING_TARGET_PATH = "targetPath"; // NOI18N
    static final String ATTR_BINDING_UPDATE_STRATEGY = "updateStrategy"; // NOI18N
    static final String ATTR_BINDING_IMMEDIATELY = "immediately"; // NOI18N
    static final String ATTR_BINDING_PARAMETER_NAME = "name"; // NOI18N
    static final String ATTR_BINDING_PARAMETER_VALUE = "value"; // NOI18N
    static final String ATTR_EVENT_NAME = "event"; // NOI18N
    static final String ATTR_EVENT_LISTENER = "listener"; // NOI18N
    static final String ATTR_EVENT_PARAMS = "parameters"; // NOI18N
    static final String ATTR_EVENT_HANDLER = "handler"; // NOI18N
    static final String ATTR_AUX_NAME = "name"; // NOI18N
    static final String ATTR_AUX_VALUE = "value"; // NOI18N
    static final String ATTR_AUX_VALUE_TYPE = "type"; // NOI18N
    static final String ATTR_LAYOUT_CLASS = "class"; // NOI18N
    static final String ATTR_CONSTRAINT_LAYOUT = "layoutClass"; // NOI18N
    static final String ATTR_CONSTRAINT_VALUE = "value"; // NOI18N
    static final String ATTR_EXPRESSION_ID = "id"; // NOI18N
    static final String ATTR_VAR_NAME = "name"; // NOI18N
    static final String ATTR_VAR_TYPE = "type"; // NOI18N
    static final String ATTR_VAR_DECLARED_TYPE = "declaredType"; // NOI18N
    static final String ATTR_META_OBJECT_TYPE = "type"; // NOI18N
    static final String ATTR_MEMBER_CLASS = "class"; // NOI18N
    static final String ATTR_MEMBER_PARAMS = "parameterTypes"; // NOI18N
    static final String ATTR_MEMBER_NAME = "name"; // NOI18N

    private static final String ONE_INDENT =  "  "; // NOI18N
    private static final Object NO_VALUE = new Object();
    private static final String FORM_SETTINGS_PREFIX = "FormSettings_"; // NOI18N

    private org.w3c.dom.Document topDocument;

    private org.w3c.dom.Document document;

    private FileObject formFile;

    private FormModel formModel;

    private String prefetchedSuperclassName;

    private List<Throwable> nonfatalErrors;
    
    // ErrorManager.findAnnotation() doesnt work properly, I will also store msg
    private Map<Throwable, String> errorMessages;

    // name of the menu bar component loaded as AUX value of the top component
    private String mainMenuBarName;

    // maps of properties that cannot be loaded before component is added to
    // parent container, or container is filled with sub-components
    private Map<RADComponent, java.util.List<Object>> parentDependentProperties;
    private Map<RADComponent, java.util.List<Object>> childrenDependentProperties;

    // map of loaded components (not necessarily added to FormModel yet)
    private Map<String,RADComponent> loadedComponents;

    private List<Object> bindingProperties; // <property, name of source, name of target, MetaBinding, node>
    private ConnectedProperties connectedProperties;

    // XML persistence of code structure
    private Map<Object,Object> expressions; // map of expressions/IDs already saved/loaded
    private int lastExpId; // CodeExpression ID counter (for saving)
    private Set<CodeVariable> savedVariables; // set of code variables already saved
    private boolean codeFlow = true; // we can save/load either code flow
                                     // or static code structure

    private Boolean newLayout; // whether a new layout support was loaded
    
    public String getExceptionAnnotation(Throwable t) {
        if ((errorMessages != null) && errorMessages.containsKey(t)) {
            return errorMessages.get(t);
        } else {
          return t.getMessage();
        }
    }
    
    private void annotateException(Throwable t, int severity, String localizedMessage) {
        if (errorMessages == null) {
            errorMessages = new HashMap<Throwable, String>();
        }
        
        errorMessages.put(t, localizedMessage);
    }
    
    private void annotateException(Throwable t, String localizedMessage) {
        annotateException(t, ErrorManager.UNKNOWN, localizedMessage);
    }

    private void annotateException(Throwable t, Throwable target) {
        ErrorManager.getDefault().annotate(t,target);
    }

    /** This method is used to check if the persistence manager can read the
     * given form (if it understands the form file format).
     * @return true if this persistence manager can load the form
     * @exception PersistenceException if any unexpected problem occurred
     */
    @Override
    public boolean canLoadForm(FormDataObject formObject)
        throws PersistenceException
    {
        formFile = formObject.getFormEntry().getFile();
        try {
            document = FormUtils.readFormToDocument(formFile);
        } catch (IOException ex) {
            throw new PersistenceException(ex, FormUtils.getBundleString("MSG_ERR_LoadingErrors")); // NOI18N
        } catch (org.xml.sax.SAXException ex) {
            throw new PersistenceException(ex, FormUtils.getBundleString("MSG_ERR_InvalidXML")); // NOI18N
        }

        org.w3c.dom.Element mainElement = document.getDocumentElement();
        return mainElement != null && XML_FORM.equals(mainElement.getTagName());
    }

    /** This method loads the form from given data object.
     * @param formObject FormDataObject representing the form files
     * @param formModel FormModel to be filled with loaded data
     * @param nonfatalErrors List to be filled with errors occurred during
     *        loading which are not fatal (but should be reported)
     * @exception PersistenceException if some fatal problem occurred which
     *            prevents loading the form
     */
    @Override
    public synchronized void loadForm(FormDataObject formObject,
                         FormModel formModel,
                         List<Throwable> nonfatalErrors)
        throws PersistenceException
    {
        loadForm(formObject.getFormEntry().getFile(), 
              formObject.getPrimaryFile(),
              formModel,
              nonfatalErrors);
    }
    
    /** This method loads the form from given data object.
     * 
     * @param formFile form file corresponding to java file
     * @param javaFile java file
     * @param formModel FormModel to be filled with loaded data
     * @param nonfatalErrors List to be filled with errors occurred during
     *        loading which are not fatal (but should be reported)
     * @return form model of the loaded form.
     * @exception PersistenceException if some fatal problem occurred which
     *            prevents loading the form
     */
    public FormModel loadForm(FileObject formFile, FileObject javaFile,
                              FormModel formModel,
                              List<Throwable> nonfatalErrors)
        throws PersistenceException
    {
        if (!formFile.equals(this.formFile)) {
            this.formFile = formFile;
            document = null;
        }

        boolean underTest = ((javaFile == null) || (javaFile.equals(formFile)));
                
        if (formModel == null) {
            formModel = new FormModel();
        }

        if (document == null) {
            try {
                document = FormUtils.readFormToDocument(formFile);
            } catch (IOException ex) {
                PersistenceException pe = new PersistenceException(ex,
                        FormUtils.getBundleString("MSG_ERR_LoadingErrors")); // NOI18N
                throw pe;
            } catch (org.xml.sax.SAXException ex) {
                PersistenceException pe = new PersistenceException(ex,
                        FormUtils.getBundleString("MSG_ERR_InvalidXML")); // NOI18N
                throw pe;
            }
        }

        // check the main element
        org.w3c.dom.Element mainElement = document.getDocumentElement();
        if (mainElement == null || !XML_FORM.equals(mainElement.getTagName())) {
            PersistenceException ex = new PersistenceException(
                    FormUtils.getBundleString("MSG_ERR_MissingMainElement")); // NOI18N
            throw ex;
        }

        if (document.getChildNodes().getLength() > 1 && document.getChildNodes().item(0) instanceof Comment) {
            formModel.setLeadingComment(document.getChildNodes().item(0).getTextContent());
        }

        // check the form version
        String versionString = mainElement.getAttribute(ATTR_FORM_VERSION);
        if (!isSupportedFormatVersion(versionString)) {
            PersistenceException ex = new PersistenceException(
                    FormUtils.getFormattedBundleString(
                        "FMT_ERR_UnsupportedVersion", new Object[] { versionString }));// NOI18N
            throw ex;
        }
        formModel.setCurrentVersionLevel(FormModel.FormVersion.BASIC);
        FormModel.FormVersion formatVersion = formVersionForVersionString(versionString);
        if (formatVersion != null) {
            formModel.raiseVersionLevel(formatVersion, formatVersion);
        }
        String maxVersionString = mainElement.getAttribute(ATTR_MAX_FORM_VERSION);
        FormModel.FormVersion maxFormatVersion = formVersionForVersionString(maxVersionString);
        if (maxFormatVersion == null
                && maxVersionString != null && maxVersionString.length() > 0) {
            // specified in form file, but unknown max version
            maxFormatVersion = FormModel.LATEST_VERSION;
        }
        formModel.setMaxVersionLevel(maxFormatVersion);

        // --------
        // get the form base class and set it to FormModel
        String declaredSuperclassName = null;
        Class formBaseClass = null;
        Throwable formBaseClassEx = null;

        // get the formerly used FormInfo type (to be used as fallback alternative)
        String formInfoName = mainElement.getAttribute(ATTR_FORM_TYPE);
        if ("".equals(formInfoName)) // NOI18N
            formInfoName = null; // not available

        if (!underTest) { // try declared superclass from java source first
            // (don't scan java source in tests, we only use the basic form types)
            try {
                declaredSuperclassName = getSuperClassName(formModel);
                if (declaredSuperclassName != null) {
                    Class designClass = getFormDesignClass(declaredSuperclassName);
                    if (designClass == null) {
                        Class<?> superclass = FormUtils.loadClass(declaredSuperclassName, formFile);
                        designClass = getFormDesignClass(superclass);
                        if (designClass == null) {
                            formBaseClass = checkDeclaredSuperclass(superclass, formInfoName);
                            if (formBaseClass != superclass && !underTest) {
                                System.err.println(FormUtils.getFormattedBundleString(
                                    "FMT_IncompatibleFormTypeWarning", // NOI18N
                                    new Object[] { javaFile.getName() }));
                            }
                        } else {
                            formBaseClass = designClass;
                            if (!superclass.isAssignableFrom(designClass)) {
                                System.err.println("WARNING: Design class not compatible with declared form base class:"); // NOI18N
                                System.err.println("-> "+designClass.getName() + " is not subclass of " + superclass.getName()); // NOI18N
                            }
                        }
                    } else {
                        formBaseClass = designClass;
                    }
                }
                if (formBaseClass != null) {
                    formModel.setFormBaseClass(formBaseClass);
                    // Force creation of the default instance in the correct L&F context
                    BeanSupport.getDefaultInstance(formBaseClass);
                }
            }
            catch (Exception ex) {
                formBaseClassEx = ex;
            }
            catch (LinkageError ex) {
                formBaseClassEx = ex;
            }
            finally {
                prefetchedSuperclassName = null;
            }
        }

        if (formModel.getFormBaseClass() == null) {
            // using superclass declared in java source failed, so try to use
            // some well-known substitute class instead
            Class substClass = null;

            if (formBaseClass != null) // try to honor the declared superclass
                substClass = getCompatibleFormClass(formBaseClass);
 
            if (substClass == null && formInfoName != null) // fall back to FormInfo type
                substClass = getCompatibleFormClass(formInfoName);

            if (substClass != null) { // succeeded, there is a substitute class
                try {
                    formModel.setFormBaseClass(substClass);

                    if (!underTest) { // print a warning about using fallback type (OK for tests)
                        String msg = FormUtils.getFormattedBundleString(
                            "FMT_FormTypeWarning", // NOI18N
                            new Object[] { javaFile.getName(),
                                           substClass.getName(),
                                           declaredSuperclassName != null ?
                                           declaredSuperclassName : "<unknown class>" }); // NOI18N
                        if (formBaseClassEx != null) {
                            Logger.getLogger("").log(Level.INFO, msg, formBaseClassEx); // NOI18N
                        } else {
                            Logger.getLogger("").log(Level.INFO, msg);
                        }
                    }
                }
                catch (Exception ex) { // should not happen for the substitute types
                    ex.printStackTrace();
                }
                catch (LinkageError ex) { // should not happen for the substitute types
                    ex.printStackTrace();
                }
            }

            if (formModel.getFormBaseClass() == null) {
                // after all, we still cannot determine the form base class
                String annotation;
                if (declaredSuperclassName != null) {
                    if (declaredSuperclassName.startsWith("org.jdesktop.application")) { // NOI18N
                        swingappEncountered();
                    }
                    // the class from java source at least can be loaded, but
                    // cannot be used as the form type; no substitute available
                    annotation = FormUtils.getFormattedBundleString(
                                     "FMT_ERR_InvalidBaseClass", // NOI18N
                                     new Object[] { declaredSuperclassName });
                }
                else { // cannot determine form base class at all;
                       // no substitute available
                    annotation = FormUtils.getBundleString(
                         "MSG_ERR_CannotDetermineBaseClass"); // NOI18N
                }

                PersistenceException ex;
                if (formBaseClassEx != null) {
                    ex = new PersistenceException(formBaseClassEx, annotation);
                }
                else {
                    ex = new PersistenceException(annotation);
                }
                throw ex;
            }
        }
        // base class set
        // ---------

        // initial cleanup
        if (loadedComponents != null)
            loadedComponents.clear();
        if (expressions != null)
            expressions.clear();
        mainMenuBarName = null;
        parentDependentProperties = null;
        childrenDependentProperties = null;
        bindingProperties = null;
        connectedProperties = null;

        this.formModel = formModel;
        this.nonfatalErrors = nonfatalErrors;
        this.newLayout = null;

        formModel.setName(javaFile.getName());

        RADComponent topComp = formModel.getTopRADComponent();
        if (topComp != null) { // load the main form component
            loadComponent(mainElement, topComp, null);
        } else {
            // Load form settings of bean form
            org.w3c.dom.NodeList childNodes = mainElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                org.w3c.dom.Node childNode = childNodes.item(i);
                if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
                String nodeName = childNode.getNodeName();
                if (XML_AUX_VALUES.equals(nodeName)) {
                    loadAuxValues(childNode, null);
                }
            }
        }

        // load "Other Components"
        loadNonVisuals(mainElement);

        // compatibility hack for loading form's menu bar, part 3
        if (mainMenuBarName != null) {
            RADComponent menuBar = getComponentsMap().get(mainMenuBarName);
            if (menuBar != null && menuBar.getParentComponent() == null
                    && topComp instanceof RADVisualFormContainer) {
                formModel.removeComponentImpl(menuBar, false);
                ((RADVisualFormContainer)topComp).add(menuBar);
            }
        }

        // set default variable modifiers for ther form
        FormSettings settings = formModel.getSettings();
        boolean local = settings.getVariablesLocal();
        int modifiers = settings.getVariablesModifier();
        int type = local ? (CodeVariable.LOCAL | (modifiers & CodeVariable.FINAL)
            | CodeVariable.EXPLICIT_DECLARATION) : modifiers | CodeVariable.FIELD;
        formModel.getCodeStructure().setDefaultVariableType(type);

        // Update code vartiables
        for (RADComponent comp : formModel.getAllComponents()) {
            JavaCodeGenerator.setupComponentFromAuxValues(comp);
        }

        if (bindingProperties != null) {
            setBindingProperties();
            bindingProperties = null;
        }
        
        if(connectedProperties != null) {  
           connectedProperties.setValues();          
           connectedProperties = null;    
        }        
                
        if (Boolean.TRUE.equals(newLayout) && (!underTest)) { // for sure update project classpath with layout extensions library
            FormEditor.updateProjectForNaturalLayout(formModel);
        }
        if (!Boolean.FALSE.equals(newLayout)) {
            formModel.setFreeDesignDefaultLayout(true);
        }

        // make sure form max version is properly set after loading
        formatVersion = formModel.getCurrentVersionLevel();
        maxFormatVersion = formModel.getMaxVersionLevel();
        if (maxFormatVersion == null
                || formatVersion.ordinal() > maxFormatVersion.ordinal()) {
            // max version needs to be at least as high as the actual version
            formModel.setMaxVersionLevel(formatVersion);
        }

        // final cleanup
        parentDependentProperties = null;
        childrenDependentProperties = null;
        mainMenuBarName = null;
        if (expressions != null)
            expressions.clear();
        if (loadedComponents != null)
            loadedComponents.clear();
        this.formModel = null;
        document = null;
        return formModel;
    }

    /**
     * For special use when form superclass is being determined before the form
     * is started to be loaded. Assuming loadForm is called for the form in
     * the same EDT round then.
     */
    void setPrefetchedSuperclassName(String clsName) {
        prefetchedSuperclassName = clsName;
    }

    private String getSuperClassName(FormModel form) throws QueryException, FileStateInvalidException {
        if (prefetchedSuperclassName != null) {
            return prefetchedSuperclassName;
        }
        FormJavaSource javaSource = FormEditor.getFormJavaSource(form);
        if (javaSource != null) {
            return javaSource.getSuperClassName();
        }
        return null;
    }

    private void setBindingProperties() {
        FormEditor.getBindingSupport(formModel);
        Iterator iter = bindingProperties.iterator();
        while (iter.hasNext()) {
            BindingProperty property = (BindingProperty)iter.next();
            String sourceName = (String)iter.next();
            String targetName = (String)iter.next();
            MetaBinding value = (MetaBinding)iter.next();
            org.w3c.dom.Node propNode = (org.w3c.dom.Node)iter.next();
            
            RADComponent source = formModel.findRADComponent(sourceName);
            RADComponent target = formModel.findRADComponent(targetName);
            
            if ((source == null) || (target == null)) {
                // PENDING error handling
                continue;
            }
        
            try {
                value.setSource(source);
                value.setTarget(target);
                property.setValue(value);
            } catch (Exception ex) {
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_CannotSetLoadedValue"), // NOI18N
                    propNode);
                annotateException(ex, msg);
                nonfatalErrors.add(ex);
                continue;
            }
        }
    }
    
    private void loadNonVisuals(org.w3c.dom.Node node) throws PersistenceException {
        org.w3c.dom.Node nonVisualsNode =
                                findSubNode(node, XML_NON_VISUAL_COMPONENTS);
        org.w3c.dom.NodeList childNodes = nonVisualsNode == null ? null :
                                          nonVisualsNode.getChildNodes();
        List<RADComponent> list = new ArrayList<RADComponent>();

        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                org.w3c.dom.Node subnode = childNodes.item(i);
                if (subnode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                    continue; // ignore text nodes

                RADComponent comp = restoreComponent(subnode, null);
                if (comp != null)
                    list.add(comp);
            }
        }

        RADComponent[] nonVisualComps = new RADComponent[list.size()];
        list.toArray(nonVisualComps);
        formModel.getModelContainer().initSubComponents(nonVisualComps);
    }

    // recognizes, creates, initializes and loads a meta component
    private RADComponent restoreComponent(org.w3c.dom.Node node,
                                          RADComponent parentComponent)
        throws PersistenceException
    {
        org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
        if (attrs == null) { // should not be null even if there are no attributes
            PersistenceException ex = new PersistenceException(
                      "Missing attributes of component element"); // NOI18N
            nonfatalErrors.add(ex);
            return null;
        }

        org.w3c.dom.Node classNode = attrs.getNamedItem(ATTR_COMPONENT_CLASS);
        org.w3c.dom.Node nameNode = attrs.getNamedItem(ATTR_COMPONENT_NAME);
        String className = classNode != null ? classNode.getNodeValue() : null;
        String compName = nameNode != null ? nameNode.getNodeValue() : null;

        if (className == null) {
            PersistenceException ex = new PersistenceException(
                                 "Missing component class name"); // NOI18N
            String msg = createLoadingErrorMessage(
                           FormUtils.getBundleString("MSG_ERR_MissingClass"), // NOI18N
                                                     node);
            annotateException(ex, ErrorManager.ERROR, msg);
            nonfatalErrors.add(ex);
            return null;
        }

        // first load the component class
        Class<?> compClass = null;
        Throwable compEx = null;
        try {
            compClass = PersistenceObjectRegistry.loadClass(className, formFile);
            // Force creation of the default instance in the correct L&F context
            BeanSupport.getDefaultInstance(compClass);
        }
        catch (Exception ex) {
            compClass = InvalidComponent.class;            
            compEx = ex;
        }
        catch (LinkageError ex) {
            compClass = InvalidComponent.class;            
            compEx = ex;
        }
        if (compEx != null) { // loading the component class failed
            String msg = createLoadingErrorMessage(
                FormUtils.getFormattedBundleString("FMT_ERR_CannotLoadClass", // NOI18N
                                                   new Object[] { className }),
                node);
            annotateException(compEx, msg);
            nonfatalErrors.add(compEx);            
        }

        compEx = null;
        // create a new metacomponent
        RADComponent newComponent;
        String nodeName = node.getNodeName();
                
        if (XML_COMPONENT.equals(nodeName)) {
            if (compClass == InvalidComponent.class) {
                if(parentComponent instanceof RADVisualContainer) {                    
                    newComponent = new RADVisualComponent();
                } else {
                    newComponent = new RADComponent();
                }                
            } else {
                if (FormUtils.isVisualizableClass(compClass)) {
                    newComponent = new RADVisualComponent();
                } else {
                    newComponent = new RADComponent();
                }                
            }            
        }
        else if (XML_MENU_COMPONENT.equals(nodeName)) {
            if (RADVisualComponent.getMenuType(compClass) != null) {
                newComponent = new RADVisualComponent();
            } else {
                newComponent = new RADMenuItemComponent();
            }
        }
        else if (XML_MENU_CONTAINER.equals(nodeName)) {
            if (RADVisualComponent.getMenuType(compClass) != null) {
                newComponent = new RADVisualContainer();
            } else {
                newComponent = new RADMenuComponent();
            }
        }
        else if (XML_CONTAINER.equals(nodeName)) {
            if (compClass == InvalidComponent.class) {
                newComponent = new RADVisualContainer();
            } else {
                if (java.awt.Container.class.isAssignableFrom(compClass))
                    newComponent = new RADVisualContainer();
                else newComponent = new RADContainer();
            }  
        }
        else {
            PersistenceException ex = new PersistenceException(
                                    "Unknown component element"); // NOI18N
            annotateException(ex,
                    ErrorManager.ERROR,
                    FormUtils.getFormattedBundleString("FMT_ERR_UnknownElement", // NOI18N
                                                   new Object[] { nodeName })
                    );
            nonfatalErrors.add(ex);
            return null;
        }

        String exMsg = null;
        boolean repeatForFakeInvalid;
        do {
            repeatForFakeInvalid = false;
            // initialize the metacomponent
            try {
                if (compClass == InvalidComponent.class) {
                    newComponent.setValid(false);                            
                    newComponent.setMissingClassName(className);
                }
                newComponent.initialize(formModel);
                newComponent.setStoredName(compName);
                newComponent.initInstance(compClass);
                newComponent.setInModel(true);
            } catch (Exception ex) {
                if (compClass != InvalidComponent.class) {
                    compEx = ex;
                }
            } catch (NoClassDefFoundError ex) {
                if (compClass != InvalidComponent.class) {
                    compEx = ex;
                    String classNameFromException = ex.getMessage();
                    if (classNameFromException != null) {
                        classNameFromException = classNameFromException.replace('/', '.').replace('$', '.');
                        exMsg = createLoadingErrorMessage(
                            FormUtils.getFormattedBundleString("FMT_ERR_CannotLoadClass", // NOI18N
                                                               new Object[] { classNameFromException }),
                            node);
                    }
                    compClass = InvalidComponent.class;
                    repeatForFakeInvalid = true;
                }
            } catch (LinkageError ex) { // loading a class the component needs failed
                if (compClass != InvalidComponent.class) {
                    compEx = ex;
                }
            }
        } while (repeatForFakeInvalid);

        if (compEx != null) { // creating component instance failed
            if (exMsg == null) {
                exMsg = createLoadingErrorMessage(
                    FormUtils.getFormattedBundleString("FMT_ERR_CannotCreateInstance", // NOI18N
                                                       new Object[] { className }),
                    node);
            }
            annotateException(compEx, exMsg);
            nonfatalErrors.add(compEx);
            if (compClass != InvalidComponent.class) {
                return null;
            }
        }

        getComponentsMap().put(compName, newComponent);

        // load the metacomponent (properties, events, layout, etc)
        loadComponent(node, newComponent, parentComponent);

        return newComponent;
    }

    private void loadComponent(org.w3c.dom.Node node,
                               RADComponent component,
                               RADComponent parentComponent)
        throws PersistenceException
    {
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes == null)
            return;

        org.w3c.dom.Node layoutNode = null;
        org.w3c.dom.Node layoutCodeNode = null;
        org.w3c.dom.Node subCompsNode = null;
        org.w3c.dom.Node constraintsNode = null;
        boolean serialized = false;

        for (int i = 0; i < childNodes.getLength(); i++) {
            org.w3c.dom.Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes

            String nodeName = childNode.getNodeName();

            if (XML_PROPERTIES.equals(nodeName)
                || XML_A11Y_PROPERTIES.equals(nodeName))
            {
                loadComponentProperties(childNode, component, nodeName);
            }
            else if (XML_EVENTS.equals(nodeName)) {
                loadEvents(childNode, component);
            }
            else if (XML_CONSTRAINTS.equals(nodeName)) {
                constraintsNode = childNode;
            }
            else if (XML_LAYOUT.equals(nodeName)) {
                if (layoutCodeNode == null)
                    layoutNode = childNode;
            }
            else if (XML_LAYOUT_CODE.equals(nodeName)) {
                layoutCodeNode = childNode;
                layoutNode = null;
            }
            else if (XML_SUB_COMPONENTS.equals(nodeName)) {
                subCompsNode = childNode;
            }
            else if (XML_AUX_VALUES.equals(nodeName)) {
                if (!serialized) {
                    loadAuxValues(childNode, component);
                    if (JavaCodeGenerator.VALUE_SERIALIZE.equals(component.getAuxValue(JavaCodeGenerator.AUX_CODE_GENERATION))) {
                        // Hack to overcome another flaw with the serialized components:
                        // When the serialized instance is loaded and set, properties and events
                        // loaded before that are lost. This is an attempt to load them again.
                        i = -1;
                        serialized = true;
                    }
                }
            }
            else if (XML_SYNTHETIC_PROPERTIES.equals(nodeName)) {
                loadSyntheticProperties(childNode, component);
            }
            else if (XML_BINDING_PROPERTIES.equals(nodeName)) {
                loadBindingProperties(childNode, component);
            }
            // ignore unknown elements?
        }

        // if the loaded component is a visual component in a visual contianer,
        // then load NB 3.1 layout constraints for it
        if (component instanceof RADVisualComponent
            && parentComponent instanceof RADVisualContainer
            && layoutConvIndex != LAYOUT_FROM_CODE)
        {
            CodeExpression compExp = component.getCodeExpression();
            LayoutSupportManager layoutSupport =
                ((RADVisualContainer)parentComponent).getLayoutSupport();

            org.w3c.dom.Node[] constrNodes = constraintsNode != null ?
                findSubNodes(constraintsNode, XML_CONSTRAINT) : null;

            boolean constraintsLoaded = false;

            if (constrNodes != null && constrNodes.length > 0) {
                // NB 3.1 used to save all constraints ever set. We must
                // go through all of them, but only those of current layout
                // will be loaded.
                for (int i=0; !constraintsLoaded && i < constrNodes.length; i++)
                    constraintsLoaded = loadConstraints(constrNodes[i],
                                                        compExp,
                                                        layoutSupport);
            }

            if (!constraintsLoaded)
                setupDefaultComponentCode(compExp, layoutSupport);
        }

        ComponentContainer container = // is this component a container?
                component instanceof ComponentContainer ?
                       (ComponentContainer) component : null;
        if (container == null) { // this component is not a container
            if (parentComponent == null) // this is a root component - load resource properties
                ResourceSupport.loadInjectedResources(component);
            return;
        }

        // we continue in loading container

        RADVisualContainer visualContainer = // is it a visual container?
                component instanceof RADVisualContainer ?
                        (RADVisualContainer) component : null;

        if (visualContainer != null)
            visualContainer.setOldLayoutSupport(true);

        int convIndex = LAYOUT_UNKNOWN;
        if (visualContainer != null) {
            if (layoutNode != null) {
                // load container layout properties saved in NB 3.1 format;
                // these properties are loaded before subcomponents
                convIndex = loadLayout(layoutNode,
                                       visualContainer.getLayoutSupport());
            }
            else if (layoutCodeNode != null) {
                convIndex = LAYOUT_FROM_CODE;
            }
        }

        // load subcomponents
        RADComponent[] childComponents;
        childNodes = subCompsNode != null ?
                     subCompsNode.getChildNodes() : null;
        if (childNodes != null) {
            List<RADComponent> list = new ArrayList<RADComponent>();
            for (int i=0, n=childNodes.getLength(); i < n; i++) {
                org.w3c.dom.Node componentNode = childNodes.item(i);
                if (componentNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                    continue; // ignore text nodes

                // hack for dealing with multiple constraints saved by NB 3.1
                layoutConvIndex = convIndex;

                RADComponent newComp = restoreComponent(componentNode, component);

                if (newComp != null)
                    list.add(newComp);
            }

            childComponents = new RADComponent[list.size()];
            list.toArray(childComponents);
        }
        else childComponents = new RADComponent[0];

        if (visualContainer != null && layoutCodeNode != null) {
            // load complete layout code (both for container and components);
            // this container doesn't use NB 3.1 format for saving layout data
            loadLayoutCode(layoutCodeNode);
        }

        if (visualContainer != null) {
            Throwable layoutEx = null;
            boolean layoutInitialized = false;
            LayoutSupportManager layoutSupport = visualContainer.getLayoutSupport();

            if (convIndex == LAYOUT_NATURAL) {
                LayoutModel layoutModel = formModel.getLayoutModel();
                Map<String, String> nameToIdMap = new HashMap<String, String>();
                for (int i=0; i<childComponents.length; i++) {
                    RADComponent comp = childComponents[i];
                    nameToIdMap.put(comp.getName(), comp.getId());
                }
                try {
                    layoutModel.loadContainerLayout(visualContainer.getId(), layoutNode.getChildNodes(), nameToIdMap);
                    visualContainer.setOldLayoutSupport(false);
                    layoutSupport = null;
                    layoutInitialized = true;
                    newLayout = Boolean.TRUE;
                    
                    // Issue 200093, 200665
                    Integer lctSetting = (Integer)formModel.getSettings().get(FormLoaderSettings.PROP_LAYOUT_CODE_TARGET);
                    if (lctSetting == null) {
                        FormEditor fe = FormEditor.getFormEditor(formModel);
                        if (fe != null && !fe.needPostCreationUpdate()) {
                            // Old form that has no layout code target set, but
                            // it uses Free Design => it was created before
                            // layout code target property was added, i.e.,
                            // it uses the library, not JDK 6 code
                            formModel.getSettings().setLayoutCodeTarget(JavaCodeGenerator.LAYOUT_CODE_LIBRARY);
                        }
                    }
                }
                catch (Exception ex) {
                    // error occurred - treat this container as with unknown layout
                    layoutModel.changeContainerToComponent(visualContainer.getId());
                    layoutEx = ex;
                }
                visualContainer.initSubComponents(childComponents);
            }
            else if (convIndex >= 0 || layoutCodeNode != null) {
                // initialize layout support from restored code
                try {
                    layoutInitialized =
                        layoutSupport.prepareLayoutDelegate(true, true);
                }
                catch (Exception ex) {
                        layoutEx = ex;
                }
                catch (LinkageError ex) {
                    layoutEx = ex;
                }
                // subcomponents are set after reading from code [for some reason...]
                visualContainer.initSubComponents(childComponents);
                if (layoutInitialized) { // successfully initialized - build the primary container
                    if (layoutSupport.getComponentCount() == 0) {
                        RADVisualComponent[] visualSubComponents = visualContainer.getSubComponents();
                        if (visualSubComponents.length > 0) {
                            // no layout code was saved for simply added components, so need to add them now
                            layoutSupport.addComponents(visualSubComponents, null, -1);
                        }
                    }
                    try { // some weird problems might occur - see issue 67890
                        layoutSupport.updatePrimaryContainer();
                    }
                    // can't do anything reasonable on failure, just log stack trace
                    catch (Exception ex) { 
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    catch (Error ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            } else if (layoutNode == null) { // Issue 63394, 68753: Bean form that is container
                                             // Issue 70369: Container saved with unknown layout
                                             // Swing menus go here as well
                // default init - not reading from code - need the subcomponents set
                visualContainer.initSubComponents(childComponents);
                try {
                    // (default init also builds the primary container)
                    layoutInitialized = layoutSupport.prepareLayoutDelegate(false, true);
                    if (!layoutInitialized) { // not known to the old support (e.g. has GroupLayout)
                                              // (but we are sure the container instance is empty)
                        java.awt.Container cont = layoutSupport.getPrimaryContainerDelegate();
                        if (SwingLayoutBuilder.isRelevantContainer(cont)) {
                            // acknowledged by SwingLayoutBuilder - this is new layout
                            visualContainer.setOldLayoutSupport(false);
                            formModel.getLayoutModel().addRootComponent(
                                new LayoutComponent(visualContainer.getId(), true));
                            layoutSupport = null;
                            newLayout = Boolean.TRUE;
                        }
                        else {
                            layoutSupport.setUnknownLayoutDelegate(false);
//                            System.err.println("[WARNING] Unknown layout in "+createLoadingErrorMessage((String)null, node) // NOI18N
//                                +" ("+component.getBeanClass().getName()+")"); // NOI18N
                        }
                        layoutInitialized = true;
                    }
                } catch (Exception ex) {
                    layoutEx = ex;
                }
            }

            if (!layoutInitialized) {
                if (layoutEx != null) { // layout initialization failed
                    org.w3c.dom.Node errNode;
                    if (layoutNode != null)
                        errNode = layoutNode;
                    else if (layoutCodeNode != null)
                        errNode = layoutCodeNode;
                    else
                        errNode = node;

                    String msg = createLoadingErrorMessage(
                        FormUtils.getBundleString("MSG_ERR_LayoutInitFailed"), // NOI18N
                        errNode);
                    annotateException(layoutEx, msg);
                    nonfatalErrors.add(layoutEx);
                }
                else { // no LayoutSupportDelegate found
                    org.w3c.dom.Node errNode;
                    if (layoutNode != null)
                        errNode = layoutNode;
                    else if (layoutCodeNode != null)
                        errNode = layoutCodeNode;
                    else
                        errNode = node;

                    String msg = createLoadingErrorMessage(
                        FormUtils.getBundleString(
                            "MSG_ERR_NoLayoutSupportFound"), // NOI18N
                        errNode);

                    PersistenceException ex = new PersistenceException(
                                              "No layout support found"); // NOI18N
                    annotateException(ex, ErrorManager.ERROR, msg);
                    nonfatalErrors.add(ex);
                }
                layoutSupport.setUnknownLayoutDelegate(layoutCodeNode != null);
            }

            if (layoutSupport != null && newLayout == null) {
                newLayout = Boolean.FALSE;
            }
        }
        else { // non-visual container (e.g. AWT menu)
            container.initSubComponents(childComponents);
        }

        // hack for properties that can't be set until the component is added
        // to the parent container
        for (RADComponent childcomp : childComponents) {
            List postProps;
            if (parentDependentProperties != null
                && (postProps = parentDependentProperties.get(childcomp)) != null)
            {
                for (Iterator it = postProps.iterator(); it.hasNext(); ) {
                    RADProperty prop = (RADProperty) it.next();
                    Object propValue = it.next();
                    try {
                        prop.setValue(propValue);
                    }
                    catch (Exception ex) { // ignore
                        String msg = createLoadingErrorMessage(
                            FormUtils.getBundleString("MSG_ERR_CannotSetLoadedValue"), // NOI18N
                            node);
                        annotateException(ex, msg);
                        nonfatalErrors.add(ex);
                    }
                }
            }
            // here it is also safe to load resource properties into sub-component
            ResourceSupport.loadInjectedResources(childcomp);
        }

        // hack for properties that can't be set until all subcomponents
        // are added to the container
        List postProps;
        if (childrenDependentProperties != null
            && (postProps = childrenDependentProperties.get(component)) != null)
        {
            for (Iterator it = postProps.iterator(); it.hasNext(); ) {
                RADProperty prop = (RADProperty) it.next();
                Object propValue = it.next();
                try {
                    prop.setValue(propValue);
                }
                catch (Exception ex) { // ignore
                    String msg = createLoadingErrorMessage(
                        FormUtils.getBundleString("MSG_ERR_CannotSetLoadedValue"), // NOI18N
                        node);
                    annotateException(ex, msg);
                    nonfatalErrors.add(ex);
                }
            }
        }

        if (parentComponent == null) // this is a root component
            ResourceSupport.loadInjectedResources(component);
    }

    private boolean loadConstraints(org.w3c.dom.Node node,
                                    CodeExpression compExp,
                                    LayoutSupportManager layoutSupport)
    {
        int convIndex = -1;
        String layout31ConstraintName = node != null ?
                   getAttribute(node, ATTR_CONSTRAINT_VALUE) : null;
        if (layout31ConstraintName != null)
            for (int i=0; i < layout31ConstraintsNames.length; i++)
                if (layout31ConstraintName.equals(layout31ConstraintsNames[i])) {
                    convIndex = i;
                    break;
                }

        // skip constraints saved by NB 3.1 which are not for the current layout
        if (convIndex < 0
                || (layoutConvIndex >= 0 && convIndex != layoutConvIndex))
            return false;

        org.w3c.dom.Node constrNode = null;
        org.w3c.dom.NamedNodeMap constrAttr = null;

        if (/*convIndex >= 0 &&*/reasonable31Constraints[convIndex]) {
            org.w3c.dom.NodeList children = node.getChildNodes();
            if (children != null)
                for (int i=0, n=children.getLength(); i < n; i++) {
                    org.w3c.dom.Node cNode = children.item(i);
                    if (cNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        constrNode = cNode;
                        constrAttr = cNode.getAttributes();
                        break;
                    }
                }
        }

        if (constrNode == null)
            return false;

        try { // obligatory try/catch block for finding methods and constructors

        CodeStructure codeStructure = layoutSupport.getCodeStructure();
        CodeExpression contCodeExp = layoutSupport.getContainerCodeExpression();
        CodeExpression contDelCodeExp =
            layoutSupport.getContainerDelegateCodeExpression();

        if (convIndex == LAYOUT_BORDER) {
            if (!"BorderConstraints".equals(constrNode.getNodeName())) // NOI18N
                return false; // should not happen

            node = constrAttr.getNamedItem("direction"); // NOI18N
            if (node != null) {
                String strValue = node.getNodeValue();
                // create add method statement
                CodeStructure.createStatement(
                    contDelCodeExp,
                    getAddWithConstrMethod(),
                    new CodeExpression[] { compExp,
                                           codeStructure.createExpression(
                                                           String.class,
                                                           strValue,
                                                           strValue) });
            }
        }

        else if (convIndex == LAYOUT_GRIDBAG) {
            if (!"GridBagConstraints".equals(constrNode.getNodeName())) // NOI18N
                return false; // should not happen

            // create GridBagConstraints constructor expression
            if (gridBagConstrConstructor == null)
                gridBagConstrConstructor =
                    java.awt.GridBagConstraints.class.getConstructor(
                                                          new Class[0]);

            CodeExpression constrExp = codeStructure.createExpression(
                    gridBagConstrConstructor, CodeStructure.EMPTY_PARAMS);

            // create statements for GridBagConstraints fields
            String[] gbcAttrs = new String[] {
                "gridX", "gridY", "gridWidth", "gridHeight", // NOI18N
                "fill", "ipadX", "ipadY", // NOI18N
                "anchor", "weightX", "weightY" }; // NOI18N
            String[] gbcFields = new String[] {
                "gridx", "gridy", "gridwidth", "gridheight", // NOI18N
                "fill", "ipadx", "ipady", // NOI18N
                "anchor", "weightx", "weighty" }; // NOI18N

            for (int i=0; i < gbcAttrs.length; i++) {
                node = constrAttr.getNamedItem(gbcAttrs[i]);
                if (node != null) {
                    Class valueType;
                    Object value;
                    String strValue = node.getNodeValue();
                    if (i < 8) { // treat as int
                        valueType = Integer.TYPE;
                        value = Integer.valueOf(strValue);
                    }
                    else { // treat as double
                        valueType = Double.TYPE;
                        value = Double.valueOf(strValue);
                    }

                    CodeStructure.createStatement(
                        constrExp,
                        java.awt.GridBagConstraints.class.getField(gbcFields[i]),
                        codeStructure.createExpression(valueType, value, strValue));
                }
            }

            // Insets
            CodeExpression[] insetsParams = new CodeExpression[4];
            String[] insetsAttrs = new String[] {
                "insetsTop", "insetsLeft", "insetsBottom", "insetsRight" }; // NOI18N

            for (int i=0; i < insetsAttrs.length; i++) {
                node = constrAttr.getNamedItem(insetsAttrs[i]);
                String strValue = node != null ? node.getNodeValue() : "0"; // NOI18N
                insetsParams[i] = codeStructure.createExpression(
                                                    Integer.TYPE,
                                                    Integer.valueOf(strValue),
                                                    strValue);
            }

            if (insetsConstructor == null)
                insetsConstructor = java.awt.Insets.class.getConstructor(
                    new Class[] { Integer.TYPE, Integer.TYPE,
                                  Integer.TYPE, Integer.TYPE });

            CodeStructure.createStatement(
                          constrExp,
                          java.awt.GridBagConstraints.class.getField("insets"), // NOI18N
                          codeStructure.createExpression(insetsConstructor,
                                                         insetsParams));

            // create add method statement
            CodeStructure.createStatement(
                contDelCodeExp,
                getAddWithConstrMethod(),
                new CodeExpression[] { compExp, constrExp });
        }

        else if (convIndex == LAYOUT_JTAB) {
            if (!"JTabbedPaneConstraints".equals(constrNode.getNodeName())) // NOI18N
                return false; // should not happen

            Object tabName = null;
            PropertyEditor tabNamePropEd = null;
            Object toolTip = null;
            PropertyEditor toolTipPropEd = null;
            Object icon = null;
            PropertyEditor iconPropEd = null;

            org.w3c.dom.Node[] propNodes = findSubNodes(constrNode, XML_PROPERTY);
            if (propNodes != null)
                for (int i=0; i < propNodes.length; i++) {
                    Object editorOrValue = getPropertyEditorOrValue(propNodes[i]);
                     if (editorOrValue == NO_VALUE)
                         continue;

                    PropertyEditor prEd = null;
                    Object value = null;
                    if (editorOrValue instanceof PropertyEditor)
                        prEd = (PropertyEditor) editorOrValue;
                    else
                        value = editorOrValue;

                    String name = getAttribute(propNodes[i], ATTR_PROPERTY_NAME);
                    if ("tabTitle".equals(name)) { // NOI18N
                        tabName = value;
                        tabNamePropEd = prEd;
                    }
                    else if ("tabToolTip".equals(name)) { // NOI18N
                        toolTip = value;
                        toolTipPropEd = prEd;
                    }
                    else if ("tabIcon".equals(name)) { // NOI18N
                        icon = value;
                        iconPropEd = prEd;
                    }
                }

            if (tabName == null
                    && (node = constrAttr.getNamedItem("tabName")) != null) // NOI18N
                tabName = node.getNodeValue();
            if (toolTip == null
                    && (node = constrAttr.getNamedItem("toolTip")) != null) // NOI18N
                toolTip = node.getNodeValue();

            if (toolTip != null || toolTipPropEd != null) {
                if (addTabMethod1 == null)
                    addTabMethod1 = javax.swing.JTabbedPane.class.getMethod(
                                    "addTab", // NOI18N
                                    new Class[] { String.class,
                                                  javax.swing.Icon.class,
                                                  java.awt.Component.class,
                                                  String.class });
                CodeStructure.createStatement(
                    contCodeExp,
                    addTabMethod1,
                    new CodeExpression[] { 
                        createExpressionForProperty(
                            codeStructure, String.class, tabName, tabNamePropEd),
                        createExpressionForProperty(
                            codeStructure, javax.swing.Icon.class, icon, iconPropEd),
                        compExp,
                        createExpressionForProperty(
                            codeStructure, String.class, toolTip, toolTipPropEd) });
            }
            else if (icon != null || iconPropEd != null) {
                if (addTabMethod2 == null)
                    addTabMethod2 = javax.swing.JTabbedPane.class.getMethod(
                                    "addTab", // NOI18N
                                    new Class[] { String.class,
                                                  javax.swing.Icon.class,
                                                  java.awt.Component.class });
                CodeStructure.createStatement(
                    contCodeExp,
                    addTabMethod2,
                    new CodeExpression[] {
                        createExpressionForProperty(
                            codeStructure, String.class, tabName, tabNamePropEd),
                        createExpressionForProperty(
                            codeStructure, javax.swing.Icon.class, icon, iconPropEd),
                        compExp });
            }
            else {
                if (addTabMethod3 == null)
                    addTabMethod3 = javax.swing.JTabbedPane.class.getMethod(
                                    "addTab", // NOI18N
                                    new Class[] { String.class,
                                                  java.awt.Component.class });
                CodeStructure.createStatement(
                    contCodeExp,
                    addTabMethod3,
                    new CodeExpression[] {
                        createExpressionForProperty(
                            codeStructure, String.class, tabName, tabNamePropEd),
                        compExp });
            }
        }

        else if (convIndex == LAYOUT_JSPLIT) {
            if (!"JSplitPaneConstraints".equals(constrNode.getNodeName())) // NOI18N
                return false;

            node = constrAttr.getNamedItem("position"); // NOI18N
            if (node != null) {
                String position = node.getNodeValue();
                Method addMethod;

                if ("top".equals(position)) { // NOI18N
                    if (setTopComponentMethod == null)
                        setTopComponentMethod =
                            javax.swing.JSplitPane.class.getMethod(
                                    "setTopComponent", // NOI18N
                                    new Class[] { java.awt.Component.class });
                    addMethod = setTopComponentMethod;
                }
                else if ("bottom".equals(position)) { // NOI18N
                    if (setBottomComponentMethod == null)
                        setBottomComponentMethod =
                            javax.swing.JSplitPane.class.getMethod(
                                    "setBottomComponent", // NOI18N
                                    new Class[] { java.awt.Component.class });
                    addMethod = setBottomComponentMethod;
                }
                else if ("left".equals(position)) { // NOI18N
                    if (setLeftComponentMethod == null)
                        setLeftComponentMethod =
                            javax.swing.JSplitPane.class.getMethod(
                                    "setLeftComponent", // NOI18N
                                    new Class[] { java.awt.Component.class });
                    addMethod = setLeftComponentMethod;
                }
                else if ("right".equals(position)) { // NOI18N
                    if (setRightComponentMethod == null)
                        setRightComponentMethod =
                            javax.swing.JSplitPane.class.getMethod(
                                    "setRightComponent", // NOI18N
                                    new Class[] { java.awt.Component.class });
                    addMethod = setRightComponentMethod;
                }
                else return false;

                CodeStructure.createStatement(contCodeExp,
                                              addMethod,
                                              new CodeExpression[] { compExp });
            }
        }

        else if (convIndex == LAYOUT_CARD) {
            if (!"CardConstraints".equals(constrNode.getNodeName())) // NOI18N
                return false;

            node = constrAttr.getNamedItem("cardName"); // NOI18N
            if (node != null) {
                String strValue = node.getNodeValue();
                // create add method statement
                CodeStructure.createStatement(
                    contDelCodeExp,
                    getAddWithConstrMethod(),
                    new CodeExpression[] { compExp,
                                           codeStructure.createExpression(
                                                           String.class,
                                                           strValue,
                                                           strValue) });
            }
        }

        else if (convIndex == LAYOUT_JLAYER) {
            if (!"JLayeredPaneConstraints".equals(constrNode.getNodeName())) // NOI18N
                return false;

            CodeExpression[] boundsParams = new CodeExpression[4];
            String[] boundsAttrs = new String[] { "x", "y", "width", "height" }; // NOI18N

            for (int i=0; i < boundsAttrs.length; i++) {
                node = constrAttr.getNamedItem(boundsAttrs[i]);
                String strValue = node != null ?
                                      node.getNodeValue() :
                                      (i < 2 ? "0" : "-1"); // NOI18N
                boundsParams[i] = codeStructure.createExpression(
                                                    Integer.TYPE,
                                                    Integer.valueOf(strValue),
                                                    strValue);
            }

            if (setBoundsMethod == null)
                setBoundsMethod = java.awt.Component.class.getMethod(
                                    "setBounds", // NOI18N
                                    new Class[] { Integer.TYPE, Integer.TYPE,
                                                  Integer.TYPE, Integer.TYPE });
            CodeStructure.createStatement(
                            compExp, setBoundsMethod, boundsParams);

            // create add method statement
            CodeStructure.createStatement(contDelCodeExp,
                                          getSimpleAddMethod(),
                                          new CodeExpression[] { compExp });

            node = constrAttr.getNamedItem("layer"); // NOI18N
            if (node != null) {
                Object metacomp = compExp.getOrigin().getMetaObject();
                if (metacomp instanceof RADVisualComponent) {
                    Integer layer = (Integer) decodePrimitiveValue(node.getNodeValue(), Integer.class);
                    if (layer.intValue() != javax.swing.JLayeredPane.DEFAULT_LAYER) {
                        ((RADVisualComponent)metacomp).setAuxValue(RADVisualComponent.PROP_LAYER, layer);
                    }
                }
            }
        }

        else if (convIndex == LAYOUT_ABSOLUTE) {
            if (!"AbsoluteConstraints".equals(constrNode.getNodeName())) // NOI18N
                return false;

            CodeExpression[] boundsParams = new CodeExpression[4];
            String[] boundsAttrs = new String[] { "x", "y", "width", "height" }; // NOI18N

            for (int i=0; i < boundsAttrs.length; i++) {
                node = constrAttr.getNamedItem(boundsAttrs[i]);
                String strValue = node != null ?
                                      node.getNodeValue() :
                                      (i < 2 ? "0" : "-1"); // NOI18N
                boundsParams[i] = codeStructure.createExpression(
                                                    Integer.TYPE,
                                                    Integer.valueOf(strValue),
                                                    strValue);
            }

            Iterator it = CodeStructure.getDefinedStatementsIterator(contDelCodeExp);
            CodeStatement[] statements = CodeStructure.filterStatements(
                                                it, getSetLayoutMethod());
            boolean nullLayout;
            if (statements.length > 0) {
                CodeExpression layoutExp =
                    statements[0].getStatementParameters()[0];
                nullLayout = layoutExp.getOrigin().getType()
                             != org.netbeans.lib.awtextra.AbsoluteLayout.class;
            }
            else nullLayout = true;

            if (nullLayout) {
                if (setBoundsMethod == null)
                    setBoundsMethod = java.awt.Component.class.getMethod(
                                      "setBounds", // NOI18N
                                      new Class[] { Integer.TYPE, Integer.TYPE,
                                                    Integer.TYPE, Integer.TYPE });
                CodeStructure.createStatement(
                    compExp, setBoundsMethod, boundsParams);

                // create add method statement
                CodeStructure.createStatement(contDelCodeExp,
                                              getSimpleAddMethod(),
                                              new CodeExpression[] { compExp });
            }
            else {
                if (absoluteConstraintsConstructor == null)
                    absoluteConstraintsConstructor =
                        org.netbeans.lib.awtextra.AbsoluteConstraints.class
                                                      .getConstructor(
                            new Class[] { Integer.TYPE, Integer.TYPE,
                                          Integer.TYPE, Integer.TYPE });

                // create add method statement
                CodeStructure.createStatement(
                    contDelCodeExp,
                    getAddWithConstrMethod(),
                    new CodeExpression[] { compExp,
                                        codeStructure.createExpression(
                                            absoluteConstraintsConstructor,
                                            boundsParams) });
            }
        }

        return true;

        }
        catch (NoSuchMethodException ex) { // should not happen
            ex.printStackTrace();
        }
        catch (NoSuchFieldException ex) { // should not happen
            ex.printStackTrace();
        }
        return false;
    }

    private void setupDefaultComponentCode(CodeExpression compExp,
                                           LayoutSupportManager layoutSupport)
    {
        if (layoutConvIndex == LAYOUT_JSCROLL) {
            // JScrollPane requires special add code although there are
            // no constraints ...
            if (setViewportViewMethod == null) {
                try {
                    setViewportViewMethod =
                            javax.swing.JScrollPane.class.getMethod(
                                    "setViewportView", // NOI18N
                                    new Class[] { java.awt.Component.class });
                }
                catch (NoSuchMethodException ex) { // should not happen
                    ex.printStackTrace();
                    return;
                }
            }

            CodeStructure.createStatement(
                              layoutSupport.getContainerCodeExpression(),
                              setViewportViewMethod,
                              new CodeExpression[] { compExp });
        }
        else { // create simple add method statement with no constraints
            CodeStructure.createStatement(
                    layoutSupport.getContainerDelegateCodeExpression(),
                    getSimpleAddMethod(),
                    new CodeExpression[] { compExp });
        }
    }

    private static Method getSimpleAddMethod() {
        if (simpleAddMethod == null) {
            try {
                simpleAddMethod = java.awt.Container.class.getMethod(
                                      "add", // NOI18N
                                      new Class[] { java.awt.Component.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return simpleAddMethod;
    }

    private static Method getAddWithConstrMethod() {
        if (addWithConstrMethod == null) {
            try {
                addWithConstrMethod = java.awt.Container.class.getMethod(
                                      "add", // NOI18N
                                      new Class[] { java.awt.Component.class,
                                                    Object.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return addWithConstrMethod;
    }

    private static Method getSetLayoutMethod() {
        if (setLayoutMethod == null) {
            try {
                setLayoutMethod = java.awt.Container.class.getMethod(
                            "setLayout", // NOI18N
                            new Class[] { java.awt.LayoutManager.class });
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
            }
        }
        return setLayoutMethod;
    }

    private int loadLayout(org.w3c.dom.Node layoutNode,
                           LayoutSupportManager layoutSupport)
    {
        org.w3c.dom.NamedNodeMap layoutAttr = layoutNode.getAttributes();
        org.w3c.dom.Node node = layoutAttr.getNamedItem(ATTR_LAYOUT_CLASS);
        if (node == null)
            return LAYOUT_NATURAL;

        String layout31Name = PersistenceObjectRegistry.getClassName(
                                                            node.getNodeValue());
        int convIndex = -1;
        for (int i=0; i < layout31Names.length; i++)
            if (layout31Name.equals(layout31Names[i])) {
                convIndex = i;
                break;
            }

        if (convIndex < 0)
            return -1; // unknown layout

        org.w3c.dom.Node[] propNodes = findSubNodes(layoutNode, XML_PROPERTY);
        List<String> propertyNames = null;
        List<Object> propertyValues = null;
        List<Object> propertyEditors = null;

        if (propNodes != null && propNodes.length > 0) {
            propertyNames = new ArrayList<String>(propNodes.length);
            propertyValues = new ArrayList<Object>(propNodes.length);
            propertyEditors = new ArrayList<Object>(propNodes.length);

            for (int i=0; i < propNodes.length; i++) {
                node = propNodes[i];

                Object editorOrValue = getPropertyEditorOrValue(node);
                if (editorOrValue == NO_VALUE)
                    continue;

                propertyNames.add(getAttribute(node, ATTR_PROPERTY_NAME));
                if (editorOrValue instanceof PropertyEditor) {
                    propertyEditors.add(editorOrValue);
                    propertyValues.add(null);
                }
                else {
                    propertyValues.add(editorOrValue);
                    propertyEditors.add(null);
                }
            }
        }

        CodeStructure codeStructure = layoutSupport.getCodeStructure();
        CodeExpression[] layoutParams = null;
        Class[] paramTypes = null;
        Class layoutClass = null;

        String[] layoutPropNames = layout31PropertyNames[convIndex];
        if (convIndex == LAYOUT_BORDER) {
            int hgap = findName(layoutPropNames[0], propertyNames);
            int vgap = findName(layoutPropNames[1], propertyNames);
            if (hgap >= 0 || vgap >= 0) {
                layoutParams = new CodeExpression[2];

                layoutParams[0] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    hgap >= 0 ? propertyValues.get(hgap) : Integer.valueOf(0),
                    (PropertyEditor)
                        (hgap >= 0 ? propertyEditors.get(hgap) : null));

                layoutParams[1] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    vgap >= 0 ? propertyValues.get(vgap) : Integer.valueOf(0),
                    (PropertyEditor)
                        (vgap >= 0 ? propertyEditors.get(vgap) : null));
            }
            else {
                layoutParams = CodeStructure.EMPTY_PARAMS;
            }
            layoutClass = java.awt.BorderLayout.class;
        }

        else if (convIndex == LAYOUT_FLOW) {
            int alignment = findName(layoutPropNames[0], propertyNames);
            int hgap = findName(layoutPropNames[1], propertyNames);
            int vgap = findName(layoutPropNames[2], propertyNames);
            if (hgap >= 0 || vgap >= 0) {
                layoutParams = new CodeExpression[3];

                layoutParams[0] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    alignment >= 0 ? propertyValues.get(alignment) : Integer.valueOf(1),
                    (PropertyEditor)
                        (alignment >= 0 ? propertyEditors.get(alignment) : null));

                layoutParams[1] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    hgap >= 0 ? propertyValues.get(hgap) : Integer.valueOf(5),
                    (PropertyEditor)
                        (hgap >= 0 ? propertyEditors.get(hgap) : null));

                layoutParams[2] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    vgap >= 0 ? propertyValues.get(vgap) : Integer.valueOf(5),
                    (PropertyEditor)
                        (vgap >= 0 ? propertyEditors.get(vgap) : null));
            }
            else if (alignment >= 0) {
                layoutParams = new CodeExpression[1];

                layoutParams[0] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    alignment >= 0 ? propertyValues.get(alignment) : Integer.valueOf(1),
                    (PropertyEditor)
                        (alignment >= 0 ? propertyEditors.get(alignment) : null));
            }
            else {
                layoutParams = CodeStructure.EMPTY_PARAMS;
            }
            layoutClass = java.awt.FlowLayout.class;
        }

        else if (convIndex == LAYOUT_GRIDBAG) {
            layoutParams = CodeStructure.EMPTY_PARAMS;
            layoutClass = java.awt.GridBagLayout.class;
        }

        else if (convIndex == LAYOUT_BOX) {
            int axis = findName(layoutPropNames[0],
                                propertyNames);

            layoutParams = new CodeExpression[2];
            layoutParams[0] = layoutSupport.getContainerDelegateCodeExpression();
            layoutParams[1] = createExpressionForProperty(
                codeStructure,
                Integer.TYPE,
                axis >= 0 ? propertyValues.get(axis) : Integer.valueOf(javax.swing.BoxLayout.LINE_AXIS),
                (PropertyEditor)
                    (axis >= 0 ? propertyEditors.get(axis) : null));

            paramTypes = new Class[] { java.awt.Container.class, Integer.TYPE };
            layoutClass = javax.swing.BoxLayout.class;
        }

        else if (convIndex == LAYOUT_GRID) {
            int rows = findName(layoutPropNames[0], propertyNames);
            int columns = findName(layoutPropNames[1], propertyNames);
            int hgap = findName(layoutPropNames[2], propertyNames);
            int vgap = findName(layoutPropNames[3], propertyNames);
            if (hgap >= 0 || vgap >= 0) {
                layoutParams = new CodeExpression[4];

                layoutParams[0] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    rows >= 0 ? propertyValues.get(rows) : Integer.valueOf(1),
                    (PropertyEditor)
                        (rows >= 0 ? propertyEditors.get(rows) : null));

                layoutParams[1] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    columns >= 0 ? propertyValues.get(columns) : Integer.valueOf(0),
                    (PropertyEditor)
                        (columns >= 0 ? propertyEditors.get(columns) : null));

                layoutParams[2] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    hgap >= 0 ? propertyValues.get(hgap) : Integer.valueOf(0),
                    (PropertyEditor)
                        (hgap >= 0 ? propertyEditors.get(hgap) : null));

                layoutParams[3] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    vgap >= 0 ? propertyValues.get(vgap) : Integer.valueOf(0),
                    (PropertyEditor)
                        (vgap >= 0 ? propertyEditors.get(vgap) : null));
            }
            else if (rows >= 0 || columns >= 0) {
                layoutParams = new CodeExpression[2];

                layoutParams[0] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    rows >= 0 ? propertyValues.get(rows) : Integer.valueOf(1),
                    (PropertyEditor)
                        (rows >= 0 ? propertyEditors.get(rows) : null));

                layoutParams[1] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    columns >= 0 ? propertyValues.get(columns) : Integer.valueOf(0),
                    (PropertyEditor)
                        (columns >= 0 ? propertyEditors.get(columns) : null));
            }
            else {
                layoutParams = CodeStructure.EMPTY_PARAMS;
            }
            layoutClass = java.awt.GridLayout.class;
        }

        else if (convIndex == LAYOUT_CARD) {
            int hgap = findName(layoutPropNames[0], propertyNames);
            int vgap = findName(layoutPropNames[1], propertyNames);
            if (hgap >= 0 && vgap >= 0) {
                layoutParams = new CodeExpression[2];

                layoutParams[0] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    hgap >= 0 ? propertyValues.get(hgap) : Integer.valueOf(0),
                    (PropertyEditor)
                        (hgap >= 0 ? propertyEditors.get(hgap) : null));

                layoutParams[1] = createExpressionForProperty(
                    codeStructure,
                    Integer.TYPE,
                    vgap >= 0 ? propertyValues.get(vgap) : Integer.valueOf(0),
                    (PropertyEditor)
                        (vgap >= 0 ? propertyEditors.get(vgap) : null));
            }
            else {
                layoutParams = CodeStructure.EMPTY_PARAMS;
            }
            layoutClass = java.awt.CardLayout.class;
        }

        else if (convIndex == LAYOUT_ABSOLUTE) {
            boolean nullLayout = false;
            int i = findName("useNullLayout", propertyNames); // NOI18N
            if (i >= 0)
                nullLayout = Boolean.TRUE.equals(propertyValues.get(i)); 

            layoutParams = CodeStructure.EMPTY_PARAMS;
            layoutClass = nullLayout ? null :
                          org.netbeans.lib.awtextra.AbsoluteLayout.class;
        }

        else if (convIndex != LAYOUT_JLAYER) {
            return convIndex; // no layout manager
        } // Else old JLayeredPane support should be treated as null layout
          // (layoutClass is null so just continue below).

        CodeExpression layoutExp;
        if (layoutClass != null) {
            if (paramTypes == null) {
                paramTypes = new Class[layoutParams.length];
                for (int i=0; i < layoutParams.length; i++)
                    paramTypes[i] = layoutParams[i].getOrigin().getType();
            }

            Constructor layoutConstructor;
            try {
                layoutConstructor = layoutClass.getConstructor(paramTypes);
            }
            catch (NoSuchMethodException ex) { // should not happen
                ex.printStackTrace();
                return -1;
            }
            layoutExp = layoutSupport.getCodeStructure().createExpression(
                                layoutConstructor, layoutParams);
        }
        else {
            layoutExp = layoutSupport.getCodeStructure().createNullExpression(
                                                 java.awt.LayoutManager.class);
        }

        CodeStructure.createStatement(
            layoutSupport.getContainerDelegateCodeExpression(),
            getSetLayoutMethod(),
            new CodeExpression[] { layoutExp });

        return convIndex;
    }

    private static CodeExpression createExpressionForProperty(
                                      CodeStructure codeStructure,
                                      Class type,
                                      Object value,
                                      PropertyEditor propEd)
    {
        return propEd != null ?
            codeStructure.createExpression(FormCodeSupport.createOrigin(
                                                           type, propEd)) :
            codeStructure.createExpression(type,
                                           value,
                                           value != null ?
                                               value.toString() : "null"); // NOI18N
    }

    private static int findName(String name, List<String> names) {
        return names != null ? names.indexOf(name) : -1;
    }

    private void loadLayoutCode(org.w3c.dom.Node node) {
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes != null) {
//            codeFlow = true;
            for (int i=0, n=childNodes.getLength(); i < n; i++) {
                org.w3c.dom.Node childNode = childNodes.item(i);

                if (XML_CODE_STATEMENT.equals(childNode.getNodeName()))
                    loadCodeStatement(childNode, null);
            }
        }
    }

    private void loadComponentProperties(org.w3c.dom.Node node,
            RADComponent metacomp, String propCategory) throws PersistenceException {
	FormProperty[] properties;                        
        org.w3c.dom.Node[] propNodes = findSubNodes(node, XML_PROPERTY);
        String[] propNames = getPropertyAttributes(propNodes, ATTR_PROPERTY_NAME);
	
        if(metacomp.isValid()) {
            properties = findProperties(propNames, metacomp, propCategory);            
        } else {                        
            String[] propTypes = getPropertyAttributes(propNodes, ATTR_PROPERTY_TYPE);            
            properties = getFakeProperties(propNames, propTypes, propNodes, metacomp, propCategory);            
        }

        for (int i=0; i < propNodes.length; i++) {
            if (propNames[i] == null)
                continue;

            org.w3c.dom.Node propNode = propNodes[i];
            FormProperty property = properties[i];

	    loadProperty(propNode, metacomp, property);            
        }
    }

    /**
     * Invoked when a reference to Swing Application Framework
     * has been encountered in the form being loaded.
     * 
     * @throws PersistenceException with explanation for the user.
     */
    private void swingappEncountered() throws PersistenceException {
        if (!swingappAvailable()) {
            String msg = FormUtils.getBundleString("MSG_ERR_SwingAppEncountered"); // NOI18N
            throw new PersistenceException(msg);
        }
    }

    private static boolean swingappAvailable() {
        return Lookup.getDefault().lookup(ResourceService.class) != null;
    }

    private void loadProperty(org.w3c.dom.Node propNode, RADComponent metacomp, FormProperty property)
            throws PersistenceException {
	Throwable t = null;
	org.w3c.dom.Node valueNode = null;
	
	if (property == null) {
	    PersistenceException ex = new PersistenceException(
					     "Unknown property"); // NOI18N
	    String msg = createLoadingErrorMessage(
		FormUtils.getBundleString("MSG_ERR_UnknownProperty"), // NOI18N
		propNode);
	    annotateException(ex, ErrorManager.ERROR, msg);
	    nonfatalErrors.add(ex);
	    return;
	}

	// read pre-init and post-init code of the property
	String preCode = getAttribute(propNode, ATTR_PROPERTY_PRE_CODE);
	if (preCode != null)
	    property.setPreCode(preCode);
	String postCode = getAttribute(propNode, ATTR_PROPERTY_POST_CODE);
	if (postCode != null)
	    property.setPostCode(postCode);

	String typeStr = getAttribute(propNode, ATTR_PROPERTY_TYPE);
	String editorStr = getAttribute(propNode, ATTR_PROPERTY_EDITOR);
	String valueStr = getAttribute(propNode, ATTR_PROPERTY_VALUE);
        String resourceKey = getAttribute(propNode, ATTR_PROPERTY_RES_KEY);
        if (resourceKey != null) {
            swingappEncountered();
        }

	// get the type of stored property value
	Class propertyType = getPropertyType(typeStr, property, propNode);
	if(propertyType==null) {
	    return;
	}

	// load the property editor class and create an instance of it
	PropertyEditor prEd = null;
	if (editorStr != null) {
            boolean prEdNecessary = (valueStr == null);
	    prEd = getPropertyEditor(editorStr, property, propertyType, propNode, prEdNecessary);
	    if(prEd==null && prEdNecessary) {
		return;
	    } // if we failed to create the property editor we may still decode
              // the saved value from valueStr (i.e. a primitive value)
	}	    

	// load the property value
	Object value = NO_VALUE;
	if (valueStr != null) { // it is a primitive value
	    try {
                boolean containsInvalidXMLChars = "true".equals(getAttribute(propNode, ATTR_PROPERTY_INVALID_XML_CHARS)); // NOI18N
                if (containsInvalidXMLChars) {
                    valueStr = decodeInvalidXMLChars(valueStr);
                }
		value = decodePrimitiveValue(valueStr, propertyType);
		if (prEd != null) {
		    prEd.setValue(value);
		    value = prEd.getValue();
		}
	    }
	    catch (IllegalArgumentException ex) {
		String msg = createLoadingErrorMessage(
		    FormUtils.getFormattedBundleString(
			"FMT_ERR_CannotDecodePrimitive", // NOI18N
			new Object[] { valueStr, propertyType.getName() }),
		    propNode);
		annotateException(ex, msg);
		nonfatalErrors.add(ex);
		return;
	    }
	}
        else if (resourceKey != null) {
            value = ResourceSupport.findResource(resourceKey, property);
            if (value == null)
                return;
            PropertyEditor propEd = property.getPropertyEditor();
            if (propEd instanceof FormPropertyEditor) {
                FormPropertyEditor formPropEd = (FormPropertyEditor)propEd;
                for (PropertyEditor newPropEd : formPropEd.getAllEditors()) {
                    if (newPropEd instanceof ResourceWrapperEditor) {
                        value = new FormProperty.ValueWithEditor(value, newPropEd);
                        break;
                    }
                }
            }
        }
	else { // the value is serialized or saved by XMLPropertyEditor
	    org.w3c.dom.NodeList children = propNode.getChildNodes();
	    int n = children != null ? children.getLength() : 0;
	    if (n > 0) {
		try {
		    boolean serialized = false;
		    // first try if the value is serialized
		    for (int j=0; j < n; j++) {
			if (XML_SERIALIZED_PROPERTY_VALUE.equals(
				    children.item(j).getNodeName()))
			{   // here is the value serialized in XML
			    String serValue = getAttribute(children.item(j),
							ATTR_PROPERTY_VALUE);
			    if (serValue != null) {
				serialized = true;
				value = decodeValue(serValue);
			    }
			    break;
			}
		    }

		    if (!serialized) {			
			// the value is saved by XMLPropertyEditor
			for (int j=0; j < n; j++) {
			    org.w3c.dom.Node node = children.item(j);			    
			    if (node.getNodeType()
				== org.w3c.dom.Node.ELEMENT_NODE)
			    {   // here is the element of stored value
				if(prEd instanceof BeanPropertyEditor &&
				   XML_PROPERTY_BEAN.equals(node.getNodeName()))
				{
				    loadBeanFromXML(node, (BeanPropertyEditor) prEd);
				    loadBeanProperty((BeanPropertyEditor) prEd, node);
				} else if (prEd instanceof XMLPropertyEditor) {
				    ((XMLPropertyEditor)prEd).readFromXML(node);
				}
				value = prEd.getValue();

                                // hack for converting noI18n strings formerly handled by i18n/form
                                if (value instanceof String && "PlainString".equals(node.getNodeName())) { // NOI18N
                                    prEd = null; // don't switch editor, keep default
                                    ResourceSupport.setExcludedProperty(property, true);
                                }
				break;
			    }
			}
		    }
		}
		catch (Exception ex) {
		    t = (ex instanceof IOException && ex.getCause() != null) ? ex.getCause() : ex;
		}
		catch (LinkageError ex) {
		    t = ex;
		}
		if (t != null) {
		    String msg = createLoadingErrorMessage(
			FormUtils.getBundleString(
			    "MSG_ERR_CannotReadPropertyValue"), // NOI18N
			propNode);
		    annotateException(t, msg);
		    nonfatalErrors.add(t);
		    return;
		}
	    }

	    if (value == NO_VALUE) { // the value is missing
		if (preCode != null || postCode != null)
		    return; // not an error
		PersistenceException ex = new PersistenceException(
					   "Missing property value"); // NOI18N
		String msg = createLoadingErrorMessage(
		    FormUtils.getBundleString("MSG_ERR_MissingPropertyValue"), // NOI18N
		    propNode);
		annotateException(ex, ErrorManager.ERROR, msg);
		nonfatalErrors.add(ex);
		return;
	    }
	}

        String noResource = getAttribute(propNode, ATTR_PROPERTY_NORES);
        if (Boolean.parseBoolean(noResource)) {
            ResourceSupport.setExcludedProperty(property, true);
        }

	// hack for properties that can't be set until all children 
        // are added to the container
	if (metacomp != null) {
            List<Object> propList = null;
            if (FormUtils.isMarkedParentDependentProperty(property)) {
		if (parentDependentProperties != null) {
		    propList = parentDependentProperties.get(metacomp);
		}
		else {
		    parentDependentProperties = new HashMap<RADComponent, java.util.List<Object>>();
		    propList = null;
		}
		if (propList == null) {
		    propList = new LinkedList<Object>();
		    parentDependentProperties.put(metacomp, propList);
		}
		propList.add(property);
		propList.add(value);
            }
            if (FormUtils.isMarkedChildrenDependentProperty(property)) {
		if (childrenDependentProperties != null) {
		    propList = childrenDependentProperties.get(metacomp);
		}
		else {
		    childrenDependentProperties = new HashMap<RADComponent, java.util.List<Object>>();
		    propList = null;
		}
		if (propList == null) {
		    propList = new LinkedList<Object>();
		    childrenDependentProperties.put(metacomp, propList);
		}
		propList.add(property);
		propList.add(value);
            }
            if (propList != null) {
                if (prEd != null) {
                    property.setCurrentEditor(prEd);
                }
                return;
            }
	}

	// set the value to the property
	try {
            if (prEd != null) {
                property.setCurrentEditor(prEd);
                
                // Issue 204180: current editor can be a delegate editor of ResourceWrapperEditor
                // Such editor must be replaced by the ResourceWrapperEditor,
                // getAllEditors() takes care about this.
                PropertyEditor editor = property.getPropertyEditor();
                if (editor instanceof FormPropertyEditor) {
                    ((FormPropertyEditor)editor).getAllEditors();
                }
            }            
            if(value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {         
                boolean accepted = setConnectedProperty(property, 
                                                       (RADConnectionPropertyEditor.RADConnectionDesignValue) value,
                                                       (metacomp == null) ? null : metacomp.getName(), 
                                                       propNode);
                if(!accepted) {
                    // makes sense only for PROPERTY, and METHOD type.
                    // in case it wasn't set for further handling 
                    // we must set it now.
                    property.setValue(value);
                }
            } else {
                if(prEd instanceof BeanPropertyEditor) {
                    // value is no RADConnection, but it still could have
                    // properties which are RADConnection-s                    
                    Property[] properties = ((BeanPropertyEditor)prEd).getProperties();
                    for (int i = 0; i < properties.length; i++) {
                        Object propValue = properties[i].getValue();
                        if(propValue instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
                            setConnectedProperty(properties[i], 
                                                 (RADConnectionPropertyEditor.RADConnectionDesignValue)propValue, 
                                                 value.toString(), // XXX getBeanName() ?
                                                 propNode);                            
                            // value was already set, so don't care 
                            // if it also was or wasn't set for further handling.
                        }
                    }
                }     
                property.setValue(value);    	                     
            }                        
        } catch (Exception ex) {
	    createLoadingErrorMessage(ex, propNode);
	    return;
	}	
    }

    private boolean setConnectedProperty(Property property, 
                                         RADConnectionPropertyEditor.RADConnectionDesignValue value,
                                         String beanName,
                                         org.w3c.dom.Node propNode) {
        if(connectedProperties==null) {
            connectedProperties = new ConnectedProperties();
        }
        int type = value.getType();
        if(type == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_PROPERTY ||
           type == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_METHOD ||
           (type == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_BEAN && value.getDesignValue() == FormDesignValue.IGNORED_VALUE)) // Issue 155654
        {    
            // makes sense only for PROPERTY, and METHOD type ...                    
            connectedProperties.put(property, value, beanName, propNode);        
            return true;
        }
        return false;
    }
    
    private void createLoadingErrorMessage(Exception ex, org.w3c.dom.Node propNode) {
       String msg = createLoadingErrorMessage(
		FormUtils.getBundleString("MSG_ERR_CannotSetLoadedValue"), // NOI18N
		propNode);
        annotateException(ex, msg);
        nonfatalErrors.add(ex); 
    }

    private PropertyEditor getPropertyEditor(String editorStr, FormProperty property, Class propertyType, org.w3c.dom.Node propNode, boolean reportFailure)
            throws PersistenceException {
	// load the property editor class and create an instance of it
	PropertyEditor prEd = null;
	Throwable t = null;	
	if (editorStr != null) {
	    Class editorClass = null;
	    try {
		editorClass = PersistenceObjectRegistry.loadClass(editorStr, formFile);
	    }
	    catch (Exception ex) {
		t = ex;
	    }
	    catch (LinkageError ex) {
		t = ex;
	    }
	    if (t != null) {
                // fallback: try PropertyEditorManager
                prEd = PropertyEditorManager.findEditor(propertyType);
                if (prEd != null && prEd.getClass().getName().equals(editorStr))
                    return prEd;

                if (reportFailure) {
                    if ("org.netbeans.modules.swingapp.ActionEditor".equals(editorStr)) {
                        swingappEncountered();
                    }
                    String msg = createLoadingErrorMessage(
                        FormUtils.getFormattedBundleString(
                            "FMT_ERR_CannotLoadClass3", // NOI18N
                            new Object[] { editorStr }),
                        propNode);
                    annotateException(t, ErrorManager.USER, msg);
                    nonfatalErrors.add(t);
                }
		return null;
	    }

	    try {
		prEd = createPropertyEditor(editorClass,
					    propertyType,
					    property);
	    }
	    catch (Exception ex) {
		t = ex;
	    }
	    catch (LinkageError ex) {
		t = ex;
	    }
	    if (t != null) {
		String msg = createLoadingErrorMessage(
		    FormUtils.getFormattedBundleString(
			"FMT_ERR_CannotCreateInstance2", // NOI18N
			new Object[] { editorStr }),
		    propNode);
		annotateException(t, msg);
		nonfatalErrors.add(t);
		return null;
	    }
	}	
	return prEd;
    }
    
    private Class getPropertyType(String typeStr, FormProperty property, org.w3c.dom.Node propNode) {
	// get the type of stored property value
	Class propertyType = null;
	Throwable t = null;

	if (typeStr != null) {
	    try {
		propertyType = getClassFromString(typeStr);
	    }
	    catch (Exception ex) {
		t = ex;
	    }
	    catch (LinkageError ex) {
		t = ex;
	    }
	    if (t != null) {
		String msg = createLoadingErrorMessage(
		    FormUtils.getFormattedBundleString(
			"FMT_ERR_CannotLoadClass2", // NOI18N
			new Object[] { typeStr }),
		    propNode);
		annotateException(t, msg);
		nonfatalErrors.add(t);
		return null;
	    }
	    if (!property.getValueType().isAssignableFrom(propertyType)) {
		PersistenceException ex = new PersistenceException(
				       "Incompatible property type"); // NOI18N
		String msg = createLoadingErrorMessage(
		    FormUtils.getBundleString("MSG_ERR_IncompatiblePropertyType"), // NOI18N
		    propNode);
		annotateException(ex, ErrorManager.ERROR, msg);
		nonfatalErrors.add(ex);
		return null;
	    }
	}
	else propertyType = property.getValueType();	
	return propertyType;
    }
    
    private void loadBeanFromXML(org.w3c.dom.Node node, BeanPropertyEditor beanPropertyEditor) 
	throws Exception 
    {
	String typeStr = node.getAttributes().getNamedItem(ATTR_PROPERTY_TYPE).getNodeValue();
	Class type = null;
	try {
	    type = getClassFromString(typeStr);
	} catch (ClassNotFoundException ex) {
	    String msg = createLoadingErrorMessage(
		FormUtils.getFormattedBundleString(
		    "FMT_ERR_CannotLoadClass2", // NOI18N
		    new Object[] { typeStr }),
		    node);
	    annotateException(ex, msg);
	    nonfatalErrors.add(ex);
	    return;
	}
	
	beanPropertyEditor.intializeFromType(type);							    	
	
    }
    
    private void loadBeanProperty(BeanPropertyEditor beanPropertyEditor, org.w3c.dom.Node valueNode)
            throws PersistenceException {	
	if(beanPropertyEditor.valueIsBeanProperty()) {
	    org.w3c.dom.NodeList children = valueNode.getChildNodes();	
	    Node.Property[] allBeanProperties = beanPropertyEditor.getProperties();
	    org.w3c.dom.Node node;		
	    FormProperty prop;

	    for (int i=0; i<children.getLength(); i++) {
		node = children.item(i);	  
		if (node != null && node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) { 
		    prop = (FormProperty) getPropertyOfName(allBeanProperties, getAttribute(node, ATTR_PROPERTY_NAME));
		    if(prop != null) {
			loadProperty(node, null, prop);								
		    }	    
		}	  
	    }		    
	}	
    }
    
    private String[] getPropertyAttributes(org.w3c.dom.Node[] propNodes, String attribute){
        String[] ret = new String[propNodes.length];
        for (int i=0; i < propNodes.length; i++) {
            org.w3c.dom.Node propNode = propNodes[i];
            String propAttribute = getAttribute(propNode, attribute);            
            if (propAttribute != null)
                ret[i] = propAttribute;
            else {
                PersistenceException ex = new PersistenceException(
                                           "Missing property attribute " + attribute); // NOI18N
                String msg = FormUtils.getFormattedBundleString("MSG_ERR_MissingPropertyName", // NOI18N
                                                   new Object[] { attribute });                
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                continue;
            }
        }        
        return ret;
    }
    
    private FormProperty[] findProperties(String[] propertyNames,
                                          RADComponent metacomp,
                                          String propCategory)
    {   // [this is a bit ugly method, but don't have better way now...]
        if (XML_PROPERTIES.equals(propCategory)) // bean properties
            return metacomp.getBeanProperties(propertyNames);

        if (XML_A11Y_PROPERTIES.equals(propCategory)) {
            return findPropertiesByName(propertyNames, metacomp);            
        }

        return new FormProperty[propertyNames.length]; // error
    }

    private FormProperty[] findPropertiesByName(String[] propertyNames,
                                                RADComponent metacomp)
    {   
        FormProperty[] properties = new FormProperty[propertyNames.length];
        for (int i=0; i < propertyNames.length; i++) {
            properties[i] = metacomp.getPropertyByName(propertyNames[i], FormProperty.class, true);
        }

        return properties;
    }
    
    private FormProperty[] getFakeProperties(String[] propertyNames,
                                             String[] propertyTypes,
                                             org.w3c.dom.Node[] propNodes,
                                             RADComponent metacomp,
                                             String propCategory)
    {           
        if (XML_PROPERTIES.equals(propCategory)) { 
            Class[] propertyClasses = new Class[propertyTypes.length]; 
            for (int i=0; i < propertyNames.length; i++) {                
                propertyClasses[i] = getClassByName(propertyTypes[i], propNodes[i]);                                             
            }            
            return metacomp.getFakeBeanProperties(propertyNames, propertyClasses);        
        
        }        
        if (XML_A11Y_PROPERTIES.equals(propCategory)) {
            findPropertiesByName(propertyNames, metacomp);             
        }

        return new FormProperty[propertyNames.length]; // error
    }

    private Class getClassByName(String className, org.w3c.dom.Node node) {
        Class<?> clazz = null;
        Throwable t = null;
        try {
            clazz = getClassFromString(className);
        }
        catch (Exception ex) {
            t = ex;
        }
        catch (LinkageError ex) {                    
            t = ex;
        }
        if (t != null) { // loading the component class failed
            String msg = createLoadingErrorMessage(
                FormUtils.getFormattedBundleString("FMT_ERR_CannotLoadClass", // NOI18N
                                                   new Object[] { className }),
                                                   node);
            annotateException(t, msg);
            nonfatalErrors.add(t);                 
        }        
        return clazz;
    }
        
    private Node.Property getPropertyOfName(Node.Property[] props, String name) {
        for (int i=0; i < props.length; i++)
            if (props[i].getName().equals(name))
                return props[i];

        return null;
    }
    
    private void loadBindingProperties(org.w3c.dom.Node node, RADComponent metacomp)
            throws PersistenceException {
        org.w3c.dom.Node[] propNodes = findSubNodes(node, XML_BINDING_PROPERTY);
        for (int i=0; i < propNodes.length; i++) {
            org.w3c.dom.Node propNode = propNodes[i];
            // get the attributes of property node element
            org.w3c.dom.NamedNodeMap attrs = propNode.getAttributes();

            // get the property name from attributes
            org.w3c.dom.Node nameNode = attrs.getNamedItem(ATTR_PROPERTY_NAME);
            if (nameNode == null) {
                PersistenceException ex = new PersistenceException(
                                 "Missing binding property name"); // NOI18N
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_MissingPropertyName"), // NOI18N
                    propNode);
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                continue;
            }

            // find the property in the metacomponent
            String propName = nameNode.getNodeValue();
            BindingProperty property = metacomp.getBindingProperty(propName);
            
            // Backward compatibility with NB 6.0 Beta 1
            boolean ignoreAdjusting = false;
            if ((property == null) && "value_IGNORE_ADJUSTING".equals(propName)
                    && javax.swing.JSlider.class.isAssignableFrom(metacomp.getBeanClass())) { // NOI18N
                property = metacomp.getBindingProperty("value"); // NOI18N
                ignoreAdjusting = true;
            }

            if (property == null) {
                // unknown binding property
                PersistenceException ex = new PersistenceException(
                                       "Unknown binding property"); // NOI18N
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_UnknownProperty"), // NOI18N
                    propNode);
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                continue;
            }

            org.w3c.dom.Node sourceNode = attrs.getNamedItem(ATTR_BINDING_SOURCE);
            org.w3c.dom.Node sourcePathNode = attrs.getNamedItem(ATTR_BINDING_SOURCE_PATH);
            org.w3c.dom.Node targetNode = attrs.getNamedItem(ATTR_BINDING_TARGET);
            org.w3c.dom.Node targetPathNode = attrs.getNamedItem(ATTR_BINDING_TARGET_PATH);
            org.w3c.dom.Node updateStrategyNode = attrs.getNamedItem(ATTR_BINDING_UPDATE_STRATEGY);
            org.w3c.dom.Node immediatelyNode = attrs.getNamedItem(ATTR_BINDING_IMMEDIATELY);

            // load the property value
            if ((sourceNode == null) || (targetNode == null)) { // the value is missing
                PersistenceException ex = new PersistenceException(
                                 "Missing binding property value"); // NOI18N
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_MissingPropertyValue"), // NOI18N
                    propNode);
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                continue;
            }
            
            String source = sourceNode.getNodeValue();
            String sourcePath = (sourcePathNode == null) ? null : sourcePathNode.getNodeValue();
            String target = targetNode.getNodeValue();
            String targetPath = (targetPathNode == null) ? null : targetPathNode.getNodeValue();
            if (ignoreAdjusting) {
                targetPath = targetPath.substring(0, targetPath.length() - "_IGNORE_ADJUSTING".length()); // NOI18N
            }
            MetaBinding value = new MetaBinding(null, sourcePath, null, targetPath);
            if (ignoreAdjusting) {
                value.setParameter(MetaBinding.IGNORE_ADJUSTING_PARAMETER, "Y"); // NOI18N
            }

            if (updateStrategyNode != null) {
                String updateStrategyTxt = updateStrategyNode.getNodeValue();
                value.setUpdateStrategy(Integer.parseInt(updateStrategyTxt));
            }

            if (immediatelyNode != null) {
                String immediatelyTxt = immediatelyNode.getNodeValue();
                value.setBindImmediately(Boolean.parseBoolean(immediatelyTxt));
            }
            
            if (bindingProperties == null) bindingProperties = new LinkedList<Object>();
            bindingProperties.add(property);
            bindingProperties.add(source);
            bindingProperties.add(target);
            bindingProperties.add(value);
            bindingProperties.add(propNode);
            
            // load properties
            org.w3c.dom.Node[] subNodes = findSubNodes(propNode, XML_PROPERTY);
            for (int j=0; j<subNodes.length; j++) {
                org.w3c.dom.NamedNodeMap subAttrs = subNodes[j].getAttributes();
                org.w3c.dom.Node propNameNode = subAttrs.getNamedItem(ATTR_PROPERTY_NAME);
                if (propNameNode != null) {
                    String subPropName = propNameNode.getNodeValue();
                    if ("nullValue".equals(subPropName)) { // NOI18N
                        loadProperty(subNodes[j], null, property.getNullValueProperty());
                        value.setNullValueSpecified(true);
                    } else if ("incompletePathValue".equals(subPropName)) { // NOI18N
                        loadProperty(subNodes[j], null, property.getIncompleteValueProperty());
                        value.setIncompletePathValueSpecified(true);
                    } else if ("converter".equals(subPropName)) { // NOI18N
                        loadProperty(subNodes[j], null, property.getConverterProperty());
                    } else if ("validator".equals(subPropName)) { // NOI18N
                        loadProperty(subNodes[j], null, property.getValidatorProperty());
                    } else if ("name".equals(subPropName)) { // NOI18N
                        loadProperty(subNodes[j], null, property.getNameProperty());
                    }
                }
            }
            
            // load parameters
            loadBindingParameters(propNode, value);

            // load subbindings
            subNodes = findSubNodes(propNode, XML_SUBBINDING);
            for (int j=0; j<subNodes.length; j++) {
                org.w3c.dom.Node subNode = subNodes[j];
                org.w3c.dom.NamedNodeMap subAttrs = subNode.getAttributes();
                org.w3c.dom.Node sourcePathSubNode = subAttrs.getNamedItem(ATTR_BINDING_SOURCE_PATH);
                org.w3c.dom.Node targetPathSubNode = subAttrs.getNamedItem(ATTR_BINDING_TARGET_PATH);

                String subSourcePath = (sourcePathSubNode == null) ? null : sourcePathSubNode.getNodeValue();
                String subTargetPath = (targetPathSubNode == null) ? null : targetPathSubNode.getNodeValue();
                MetaBinding subBinding = value.addSubBinding(subSourcePath, subTargetPath);

                // load parameters of subbinding
                loadBindingParameters(subNode, subBinding);
            }
        }
    }

    private void loadBindingParameters(org.w3c.dom.Node node, MetaBinding binding) {
        org.w3c.dom.Node[] subNodes = findSubNodes(node, XML_BINDING_PARAMETER);
        for (int j=0; j<subNodes.length; j++) {
            org.w3c.dom.Node subNode = subNodes[j];
            org.w3c.dom.NamedNodeMap subAttrs = subNode.getAttributes();
            org.w3c.dom.Node paramNameNode = subAttrs.getNamedItem(ATTR_BINDING_PARAMETER_NAME);
            org.w3c.dom.Node paramValueNode = subAttrs.getNamedItem(ATTR_BINDING_PARAMETER_VALUE);
            if ((paramNameNode != null) && (paramValueNode != null)) {
                binding.setParameter(paramNameNode.getNodeValue(), paramValueNode.getNodeValue());
            }
        }
    }

    private void loadSyntheticProperties(org.w3c.dom.Node node,
                                         RADComponent metacomp)
    {
        org.w3c.dom.Node[] propNodes = findSubNodes(node, XML_SYNTHETIC_PROPERTY);
        for (int i=0; i < propNodes.length; i++) {
            org.w3c.dom.Node propNode = propNodes[i];
            // get the attributes of property node element
            org.w3c.dom.NamedNodeMap attrs = propNode.getAttributes();
            if (attrs == null)
                continue; // no attributes, ignore property

            // get the property name from attributes
            org.w3c.dom.Node nameNode = attrs.getNamedItem(ATTR_PROPERTY_NAME);
            if (nameNode == null) {
                PersistenceException ex = new PersistenceException(
                                 "Missing synthetic property name"); // NOI18N
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_MissingPropertyName"), // NOI18N
                    propNode);
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                continue;
            }

            // find the property in the metacomponent
            String propName = nameNode.getNodeValue();
            Node.Property [] props = metacomp.getSyntheticProperties();
            Node.Property property = null;
            Class<?> expectedPropertyType = null;
            for (int j=0; j < props.length; j++) {
                if (props[j].getName().equals(propName)) {
                    property = props[j];
                    break;
                }
            }

            if (property == null) {
                if ("menuBar".equals(propName) // NOI18N
                    && metacomp instanceof RADVisualFormContainer)
                {   // compatibility hack for loading form's menu bar, part 1
                    // (menubar is no longer a synthetic property, but it was
                    // in NB 3.2)
                    expectedPropertyType = String.class;
                }
                else if ("encoding".equals(propName)) // NOI18N
                    continue; // there used to be "encoding" property in NB 3.1
                else { // unknown synthetic property
                    PersistenceException ex = new PersistenceException(
                                           "Unknown synthetic property"); // NOI18N
                    String msg = createLoadingErrorMessage(
                        FormUtils.getBundleString("MSG_ERR_UnknownProperty"), // NOI18N
                        propNode);
                    annotateException(ex, ErrorManager.ERROR, msg);
                    nonfatalErrors.add(ex);
                    continue;
                }
            }
            else expectedPropertyType = property.getValueType();

            org.w3c.dom.Node typeNode = attrs.getNamedItem(ATTR_PROPERTY_TYPE);
            org.w3c.dom.Node valueNode = attrs.getNamedItem(ATTR_PROPERTY_VALUE);

            // get the type of stored property value
            Class propertyType = null;
            Throwable t = null;

            if (typeNode != null) {
                try {
                    propertyType = getClassFromString(typeNode.getNodeValue());
                }
                catch (Exception ex) {
                    t = ex;
                }
                catch (LinkageError ex) {
                    t = ex;
                }
                if (t != null) {
                    String msg = createLoadingErrorMessage(
                        FormUtils.getFormattedBundleString(
                            "FMT_ERR_CannotLoadClass2", // NOI18N
                            new Object[] { typeNode.getNodeValue() }),
                        propNode);
                    annotateException(t, msg);
                    nonfatalErrors.add(t);
                    continue;
                }
                if (!expectedPropertyType.isAssignableFrom(propertyType)) {
                    PersistenceException ex = new PersistenceException(
                                           "Incompatible property type"); // NOI18N
                    String msg = createLoadingErrorMessage(
                        FormUtils.getBundleString("MSG_ERR_IncompatiblePropertyType"), // NOI18N
                        propNode);
                    annotateException(ex, ErrorManager.ERROR, msg);
                    nonfatalErrors.add(ex);
                    continue;
                }
            }
            else propertyType = property.getValueType();

            // load the property value
            if (valueNode == null) { // the value is missing
                PersistenceException ex = new PersistenceException(
                                 "Missing synthetic property value"); // NOI18N
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_MissingPropertyValue"), // NOI18N
                    propNode);
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                continue;
            }

            Object value = null;
            try {
                try {
                    value = decodePrimitiveValue(valueNode.getNodeValue(),
                                                 propertyType);
                }
                catch (IllegalArgumentException ex) {
                    // not a primitive value
                    value = decodeValue(valueNode.getNodeValue());
                }
            }
            catch (Exception ex) {
                t = ex;
            }
            catch (LinkageError ex) {
                t = ex;
            }
            if (t != null) {
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_CannotReadPropertyValue"), // NOI18N
                    propNode);
                annotateException(t, msg);
                nonfatalErrors.add(t);
                continue;
            }

            // compatibility hack for loading form's menu bar, part 2
            if ("menuBar".equals(propName) // NOI18N
                && value instanceof String
                && metacomp instanceof RADVisualFormContainer)
            {   // menuBar synthetic property points to a component in Other Components
                mainMenuBarName = (String) value;
                continue;
            }

            // set the value to the property
            try {
                property.setValue(value);
            }
            catch (Exception ex) {
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_CannotSetLoadedValue"), // NOI18N
                    propNode);
                annotateException(ex, msg);
                nonfatalErrors.add(ex);
                continue;
            }
        }
    }

    private void loadEvents(org.w3c.dom.Node node, RADComponent metacomp) {
        org.w3c.dom.Node[] eventNodes = findSubNodes(node, XML_EVENT);
        String[] eventNames = new String[eventNodes.length];

        for (int i=0; i < eventNodes.length; i++) {
            org.w3c.dom.Node eventNode = eventNodes[i];
            String eventName = getAttribute(eventNode, ATTR_EVENT_NAME);
            String eventListener = getAttribute(eventNode, ATTR_EVENT_LISTENER);
            String paramTypes = getAttribute(eventNode, ATTR_EVENT_PARAMS);
            String eventHandlers = getAttribute(eventNode, ATTR_EVENT_HANDLER);

            if (eventName == null || eventHandlers == null)
                continue; // [missing data error - should be reported!!]

            eventNames[i] = getEventIdName(eventListener, eventName, paramTypes);
        }

        FormEvents formEvents = formModel.getFormEvents();

        Event[] events = metacomp.getEvents(eventNames);
        for (int i=0; i < eventNodes.length; i++) {
            if (eventNames[i] == null)
                continue;

            Event event = events[i];
            if (event == null)
                continue; // [uknown event error - should be reported!]

            String eventHandlers = getAttribute(eventNodes[i], ATTR_EVENT_HANDLER);
            StringTokenizer tok = new StringTokenizer(eventHandlers, ","); // NOI18N
            while (tok.hasMoreTokens()) {
                try {
                    formEvents.attachEvent(event, tok.nextToken(), null);
                }
                catch (IllegalArgumentException ex) {
                    // [incompatible handler error - should be reported!]
                }
            }
        }
    }

    private static String getEventIdName(String eventListener,
                                         String eventName,
                                         String paramTypes)
    {
        if (eventListener == null || paramTypes == null)
            return eventName;

        StringBuilder buf = new StringBuilder();
        buf.append("$"); // NOI18N
        buf.append(eventListener);
        buf.append("."); // NOI18N
        buf.append(eventName);
        buf.append("("); // NOI18N

        StringTokenizer tok = new StringTokenizer(paramTypes, ","); // NOI18N
        while (tok.hasMoreTokens()) {
            buf.append(tok.nextToken());
            if (tok.hasMoreTokens())
                buf.append(", "); // NOI18N
        }

        buf.append(")"); // NOI18N
        return buf.toString();
    }

    private void loadAuxValues(org.w3c.dom.Node node, RADComponent comp) {
        org.w3c.dom.Node[] auxNodes = findSubNodes(node, XML_AUX_VALUE);
        for (int i=0; i < auxNodes.length; i++) {
            org.w3c.dom.Node auxNode = auxNodes[i];
            // get the attributes of property node element
            org.w3c.dom.NamedNodeMap attrs = auxNode.getAttributes();
            if (attrs == null)
                continue; // no attributes, ignore

            // get the property name from attributes
            org.w3c.dom.Node nameNode = attrs.getNamedItem(ATTR_AUX_NAME);
            if (nameNode == null) {
                PersistenceException ex = new PersistenceException(
                                           "Missing aux value name"); // NOI18N
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_MissingPropertyName"), // NOI18N
                    auxNode);
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                continue;
            }
            String name = nameNode.getNodeValue();

            org.w3c.dom.Node typeNode = attrs.getNamedItem(ATTR_AUX_VALUE_TYPE);
            org.w3c.dom.Node valueNode = attrs.getNamedItem(ATTR_AUX_VALUE);

            // get the type of stored aux value
            if (typeNode == null) {
                PersistenceException ex = new PersistenceException(
                                           "Missing aux value type"); // NOI18N
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_MissingPropertyType"), // NOI18N
                    auxNode);
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                continue;
            }

            Class auxValueType = null;
            Throwable t = null;
            try {
                auxValueType = getClassFromString(typeNode.getNodeValue());
            }
            catch (Exception ex) {
                t = ex;
            }
            catch (LinkageError ex) {
                t = ex;
            }
            if (t != null) {
                String msg = createLoadingErrorMessage(
                    FormUtils.getFormattedBundleString(
                        "FMT_ERR_CannotLoadClass2", // NOI18N
                        new Object[] { typeNode.getNodeValue() }),
                    auxNode);
                annotateException(t, msg);
                nonfatalErrors.add(t);
                continue;
            }

            // load the aux value
            if (valueNode == null) { // the value is missing
                PersistenceException ex = new PersistenceException(
                                                "Missing aux value"); // NOI18N
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_MissingPropertyValue"), // NOI18N
                    auxNode);
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                continue;
            }

            Object value = null;
            try {
                try {
                    value = decodePrimitiveValue(valueNode.getNodeValue(),
                                                 auxValueType);
                }
                catch (IllegalArgumentException ex) {
                    // not a primitive value
                    value = decodeValue(valueNode.getNodeValue());
                }
            }
            catch (Exception ex) {
                t = ex;
            }
            catch (LinkageError ex) {
                t = ex;
            }
            if (t != null) {
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_CannotReadPropertyValue"), // NOI18N
                    auxNode);
                annotateException(t, msg);
                nonfatalErrors.add(t);
                continue;
            }

            // Form settings are stored in AuxValues of top-level container
            if ((comp == formModel.getTopRADComponent() && (name.startsWith(FORM_SETTINGS_PREFIX)))) {
                String settingName = name.substring(FORM_SETTINGS_PREFIX.length());
                FormSettings formSettings = formModel.getSettings();
                formSettings.set(settingName, value);

                if (FormSettings.PROP_AUTO_I18N.equals(settingName)
                        && Boolean.TRUE.equals(value)) { // auto i18n set
                    // if loading form from updated 5.5 or early 6.0,
                    // the version might be just 1.3, but should be 1.4
                    formModel.raiseVersionLevel(FormModel.FormVersion.NB60_PRE, FormModel.FormVersion.NB60_PRE);
                    // also keep auto resourcing in sync - if present but turned off
                    Object autoResSetting = formSettings.get(ResourceSupport.PROP_AUTO_RESOURCING);
                    if (autoResSetting instanceof Integer && ((Integer)autoResSetting).intValue() == ResourceSupport.AUTO_OFF) {
                        formSettings.set(ResourceSupport.PROP_AUTO_RESOURCING, ResourceSupport.AUTO_I18N);
                    }                    
                }
                if (ResourceSupport.PROP_AUTO_RESOURCING.equals(settingName)
                        && value instanceof Number
                        && (ResourceSupport.AUTO_RESOURCING == ((Number)value).intValue() || 
                            ResourceSupport.AUTO_INJECTION == ((Number)value).intValue()
                        )
                        && !swingappAvailable()) {
                    // Swing Application Framework support has been discontinued
                    // => changing the setting to I18N. It is just a fallback
                    // in case the resourcing was not used at all. If it was used
                    // then we refuse to open the form (this is implemented
                    // on another place in this class).
                    formSettings.set(ResourceSupport.PROP_AUTO_RESOURCING, ResourceSupport.AUTO_I18N);
                }
            } else {
                // we have a valid name / value pair
                comp.setAuxValue(name, value);
            }
        }

        // we must care about some aux values specially ...

        if (comp == formModel.getTopRADComponent()) {
            return;
        }

        // VALUE_SERIALIZE indicates serialized component
        if (JavaCodeGenerator.VALUE_SERIALIZE.equals(
                comp.getAuxValue(JavaCodeGenerator.AUX_CODE_GENERATION)))
        {   // the component has a serialized instance => deserialize it
            try {
                comp.setInstance(comp.createDefaultDeserializedInstance());
            } catch (Exception ex) { // ignore
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    // -----------

    private void addFormElement(StringBuffer buf) {
        // add form specification element
        String formatVersion = versionStringForFormVersion(formModel.getCurrentVersionLevel());
        String maxVersion = versionStringForFormVersion(formModel.getMaxVersionLevel());
        String compatFormInfo = getFormInfoForKnownClass(formModel.getFormBaseClass());
        if (compatFormInfo == null) {
            addElementOpenAttr(buf, XML_FORM,
                new String[] { ATTR_FORM_VERSION, ATTR_MAX_FORM_VERSION },
                new String[] { formatVersion, maxVersion });
        }
        else { // FormInfo type stored for backward compatibility
            addElementOpenAttr(buf, XML_FORM,
                new String[] { ATTR_FORM_VERSION, ATTR_MAX_FORM_VERSION, ATTR_FORM_TYPE },
                new String[] { formatVersion, maxVersion, compatFormInfo });
        }
    }

    /** This method saves the form to given data object.
     * @param formObject FormDataObject representing the form files
     * @param formModel FormModel to be saved
     * @param nonfatalErrors List to be filled with errors occurred during
     *        saving which are not fatal (but should be reported)
     * @exception PersistenceException if some fatal problem occurred which
     *            prevents saving the form
     */
    @Override
    public void saveForm(FormDataObject formObject,
                         FormModel formModel,
                         List<Throwable> nonfatalErrors)
        throws PersistenceException
    {
        saveForm(formObject.getFormFile(), formModel, nonfatalErrors);
    }

    /** This method saves the form to given data object.
     * @param formFile FileObject representing the form file
     * @param formModel FormModel to be saved
     * @param nonfatalErrors List to be filled with errors occurred during
     *        saving which are not fatal (but should be reported)
     * @exception PersistenceException if some fatal problem occurred which
     *            prevents saving the form
     */
    public synchronized void saveForm(FileObject formFile,
                         FormModel formModel,
                         List<Throwable> nonfatalErrors)
        throws PersistenceException
    {
        if (!formFile.canWrite()) { // should not happen
            PersistenceException ex = new PersistenceException(
                    FormUtils.getFormattedBundleString(
                         "FMT_ERR_SaveToReadOnly", new Object[] { formFile.getNameExt() }));// NOI18N
            throw ex;
        }

        StringBuffer buf = new StringBuffer();

        // initial cleanup
        lastExpId = 0; // CodeExpression ID counter
        if (expressions != null)
            expressions.clear();
        if (savedVariables != null)
            savedVariables.clear();

        this.formFile = formFile;
        this.formModel = formModel;
        this.nonfatalErrors = nonfatalErrors;

        RADComponent topComp = formModel.getTopRADComponent();
        RADVisualFormContainer formCont =
            topComp instanceof RADVisualFormContainer ?
                (RADVisualFormContainer) topComp : null;

        // store XML file header
        final String encoding = "UTF-8"; // NOI18N
        buf.append("<?xml version=\"1.0\" encoding=\""); // NOI18N
        buf.append(encoding);
        buf.append("\" ?>\n\n"); // NOI18N

        if (formModel.getLeadingComment() != null) {
            buf.append("<!--").append(formModel.getLeadingComment()).append("-->\n\n");
        }

        int formElementPosition = buf.length();

        // store "Other Components"
        Collection<RADComponent> otherComps = formModel.getOtherComponents();

        // compatibility hack for saving form's menu bar (part I)
        RADComponent formMenuComp = formCont != null ? formCont.getContainerMenu() : null;

        if (otherComps.size() > 0 || formMenuComp != null) {
            buf.append(ONE_INDENT);
            addElementOpen(buf, XML_NON_VISUAL_COMPONENTS);
            for (RADComponent metacomp : otherComps) {
                saveAnyComponent(metacomp,
                                 buf, ONE_INDENT + ONE_INDENT,
                                 true);
            }
            if (formMenuComp != null) {
                saveAnyComponent(formMenuComp,
                                 buf, ONE_INDENT + ONE_INDENT,
                                 true);
            }
            buf.append(ONE_INDENT);
            addElementClose(buf, XML_NON_VISUAL_COMPONENTS);
        }

        // store form main hierarchy
        if (topComp != null) {
            saveAnyComponent(topComp, buf, ONE_INDENT, false);
//            if (!(topComp instanceof RADVisualContainer))
//                raiseFormatVersion(NB33_VERSION);
        } else {
            // Form settings of bean form
            Map<String,Object> auxValues = new TreeMap<String,Object>();
            addFormSettings(auxValues);
            buf.append(ONE_INDENT); addElementOpen(buf, XML_AUX_VALUES);
            saveAuxValues(auxValues, buf, ONE_INDENT + ONE_INDENT);
            buf.append(ONE_INDENT); addElementClose(buf, XML_AUX_VALUES);
        }

        // Creating Form element at the end to make sure that we have
        // collected the right versions that are stored there
        StringBuffer sb = new StringBuffer();
        addFormElement(sb);
        buf.insert(formElementPosition, sb);

        addElementClose(buf, XML_FORM);

        // final cleanup
        if (expressions != null)
            expressions.clear();
        if (savedVariables != null)
            savedVariables.clear();

        // write the data
        FileLock lock = null;
        try {
            lock = formFile.lock();
        }
        catch (IOException ex) {
            PersistenceException pe = new PersistenceException(
                                        ex, "Cannot obtain lock on form file"); // NOI18N
            annotateException(ex,
                    FormUtils.getFormattedBundleString("FMT_ERR_CannotLockFormFile", // NOI18N
                        new Object[]{formFile.getNameExt()}));
            throw pe;
        }

        java.io.OutputStream os = null;
        try {
            os = formFile.getOutputStream(lock);
            os.write(buf.toString().getBytes(encoding));
        }
        catch (Exception ex) {
            PersistenceException pe = new PersistenceException(
                                          ex, "Cannot write to form file"); // NOI18N
            annotateException(ex,
                    FormUtils.getFormattedBundleString("FMT_ERR_CannotWrtiteToFile", // NOI18N
                                        new Object[]{formFile.getNameExt()})
                    );
            throw pe;
        }
        finally {
            try {
                if (os != null)
                    os.close();
            }
            catch (IOException ex) {} // ignore
            lock.releaseLock();
            this.formModel = null;
        }
    }

    private void addFormSettings(Map<String,Object> auxValues) {
        FormSettings formSettings = formModel.getSettings();
        Map<String,Object> settings = formSettings.allSettings();
        Iterator<Map.Entry<String,Object>> iter = settings.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Object> entry = iter.next();
            String key = entry.getKey();
            if (!formSettings.isSessionSetting(key)) {
                auxValues.put(FORM_SETTINGS_PREFIX + key, entry.getValue());
            }
        }
    }

    private org.w3c.dom.Document getTopCocument() {
        if (topDocument == null) {
            topDocument = topDocument = XMLUtil.createDocument("topDocument", null, null, null); // NOI18N
        }
        return topDocument;
    }

    private void saveAnyComponent(RADComponent comp,
                                  StringBuffer buf,
                                  String indent,
                                  boolean createElement) {
        String elementType = null;
        String elementIndent = indent;
        boolean visualMenuComp = comp instanceof RADVisualComponent
                                 && ((RADVisualComponent)comp).isMenuComponent();
        if (createElement) {
            if (comp instanceof RADMenuComponent) {
                elementType = XML_MENU_CONTAINER;
            } else if (comp instanceof RADMenuItemComponent) {
                elementType = XML_MENU_COMPONENT;
            } else if (comp instanceof ComponentContainer) {
                elementType = visualMenuComp ? XML_MENU_CONTAINER : XML_CONTAINER;
            } else {
                elementType = visualMenuComp ? XML_MENU_COMPONENT : XML_COMPONENT;
            }

            buf.append(elementIndent);
            addElementOpenAttr(buf, elementType,
                new String[] { ATTR_COMPONENT_CLASS,
                               ATTR_COMPONENT_NAME },
                new String[] { comp.getBeanClass().getName(),
                               comp.getName() });

            indent += ONE_INDENT;
        }

        if (visualMenuComp || comp instanceof RADMenuItemComponent) {
            saveMenuComponent(comp, buf, indent);
        }
        else if (comp instanceof ComponentContainer) {
            saveContainer((ComponentContainer) comp, buf, indent);
        }
        else if (comp instanceof RADVisualComponent) {
            saveVisualComponent((RADVisualComponent) comp, buf, indent);
        }
        else {
            saveComponent(comp, buf, indent);
        }

        if (createElement) {
            buf.append(elementIndent);
            addElementClose(buf, elementType);
        }
    }

    private void saveContainer(ComponentContainer container,
                               StringBuffer buf, String indent)
    {
        RADVisualContainer visualContainer =
            container instanceof RADVisualContainer ?
                (RADVisualContainer) container : null;

        RADComponent[] children = null;
        int convIndex = -1; // index of layout in conversion table

        if (visualContainer != null) {
            saveVisualComponent(visualContainer, buf, indent);
            convIndex = saveLayout(visualContainer, buf, indent);

            // compatibility hack for saving form's menu bar (part II)
            if (container instanceof RADVisualFormContainer)
                children = visualContainer.getSubComponents();
        } 
        else saveComponent((RADComponent)container, buf, indent);

        if (children == null)
            children = container.getSubBeans();

        if (children.length > 0) {
            buf.append(indent);
            addElementOpen(buf, XML_SUB_COMPONENTS);
            for (int i = 0; i < children.length; i++) {
//                if (children[i] instanceof RADMenuItemComponent)
//                    raiseFormatVersion(NB33_VERSION);
                saveAnyComponent(children[i], buf, indent+ONE_INDENT, true);
            }
            buf.append(indent);
            addElementClose(buf, XML_SUB_COMPONENTS);
        }

        if (visualContainer != null && convIndex == LAYOUT_FROM_CODE)
            saveLayoutCode(visualContainer.getLayoutSupport(), buf, indent);
    }

    private int saveLayout(RADVisualContainer container,
                           StringBuffer buf, String indent)
    {
        LayoutSupportManager layoutSupport = container.getLayoutSupport();

        if (layoutSupport == null) {
//            raiseFormatVersion(NB42_VERSION);
            RADVisualComponent[] subComponents = container.getSubComponents();
            Map<String,String> idToNameMap = new HashMap<String,String>();
            for (int i=0; i<subComponents.length; i++) {
                RADVisualComponent comp = subComponents[i];
                idToNameMap.put(comp.getId(), comp.getName());
            }
            buf.append("\n"); // NOI18N
            buf.append(indent);
            addElementOpen(buf, XML_LAYOUT);
            LayoutModel layoutModel = formModel.getLayoutModel();
            int indentation = indent.length()/ONE_INDENT.length() + 1;
            LayoutComponent layoutComp = layoutModel.getLayoutComponent(container.getId());
            buf.append(layoutModel.saveContainerLayout(layoutComp, idToNameMap, indentation, false));
            buf.append(indent);
            addElementClose(buf, XML_LAYOUT);
            return LAYOUT_NATURAL;
        } // end of hack

        if (layoutSupport.isUnknownLayout())
            return LAYOUT_UNKNOWN;

        int convIndex = -1; // index in conversion table

        Class layoutClass = layoutSupport.getLayoutDelegate().getSupportedClass();
        if (layoutClass == null)
            convIndex = LAYOUT_NULL;
        else {
            String className = layoutClass.getName();
            for (int i=0; i < supportedClassNames.length; i++)
                if (className.equals(supportedClassNames[i])) {
                    convIndex = i;
                    break;
                }

            if (convIndex < 0) // not a standard layout
                return LAYOUT_FROM_CODE;
        }
        if (convIndex == LAYOUT_FLOW) {
            FormProperty property = (FormProperty)layoutSupport.getLayoutProperty("alignOnBaseline"); // NOI18N
            if (property.isChanged()) {
                return LAYOUT_FROM_CODE;
            }
        } else if (convIndex == LAYOUT_GRIDBAG) {
            for (Node.PropertySet set : layoutSupport.getPropertySets()) {
                if ("properties".equals(set.getName())) { // NOI18N
                    for (Node.Property property : set.getProperties()) {
                        if (((FormProperty)property).isChanged()) {
                            return LAYOUT_FROM_CODE;
                        }
                    }
                }
            }
        }

        StringBuffer buf2 = new StringBuffer();

        if (convIndex != LAYOUT_ABSOLUTE && convIndex != LAYOUT_NULL) {
            Node.Property[] properties = layoutSupport.getAllProperties();
            for (int i=0; i < properties.length; i++) {
                FormProperty property = (FormProperty) properties[i];
                if (property.isChanged()
                    // NB 3.1 considered special values as default for
                    // GridLayout, so we must always save rows and columns
                    || (convIndex == LAYOUT_GRID
                        && ("rows".equals(property.getName()) // NOI18N
                            || "columns".equals(property.getName())))) // NOI18N
                {
                    String delegatePropName = property.getName();
                    String layout31PropName = null;
                    String[] delPropNames = layoutDelegatePropertyNames[convIndex];
                    for (int j=0; j < delPropNames.length; j++)
                        if (delegatePropName.equals(delPropNames[j])) {
                            layout31PropName = layout31PropertyNames[convIndex][j];
                            break;
                        }

                    if (layout31PropName != null) {
                        saveProperty(property, layout31PropName,
                                     buf2, indent + ONE_INDENT);
                    }
                }
            }
        }
        else { // AbsoluteLayout and null layout are special...
            String nullLayout = convIndex == LAYOUT_NULL ? "true" : "false"; // NOI18N
            buf2.append(indent);
            buf2.append(ONE_INDENT);
            addLeafElementOpenAttr(
                buf2,
                XML_PROPERTY,
                new String[] { ATTR_PROPERTY_NAME,
                               ATTR_PROPERTY_TYPE,
                               ATTR_PROPERTY_VALUE },
                new String[] { "useNullLayout", "boolean", nullLayout } // NOI18N
            );
        }

        buf.append("\n"); // NOI18N
        buf.append(indent);
        if (buf2.length() > 0) {
            addElementOpenAttr(
                buf,
                XML_LAYOUT,
                new String[] { ATTR_LAYOUT_CLASS },
                new String[] { PersistenceObjectRegistry.getPrimaryName(
                                   layout31Names[convIndex]) }
            );
            buf.append(buf2);
            buf.append(indent);
            addElementClose(buf, XML_LAYOUT);
        }
        else {
            addLeafElementOpenAttr(
                buf,
                XML_LAYOUT,
                new String[] { ATTR_LAYOUT_CLASS },
                new String[] { PersistenceObjectRegistry.getPrimaryName(
                                   layout31Names[convIndex]) }
            );
        }

        return convIndex;
    }

    private void saveLayoutCode(LayoutSupportManager layoutSupport,
                                StringBuffer buf, String indent)
    {
//        raiseFormatVersion(NB33_VERSION);
        StringBuffer buf2 = new StringBuffer();
        String subIndent = indent + ONE_INDENT;
//        codeFlow = true;

        // layout manager code
        CodeGroup code = layoutSupport.getLayoutCode();
        if (code != null) {
            Iterator it = code.getStatementsIterator();
            while (it.hasNext()) {
                saveCodeStatement((CodeStatement) it.next(), buf2, subIndent);
            }
        }

        // components code
        if (layoutSupport.hasComponentConstraints()
                || formModel.getCurrentVersionLevel().ordinal() < FormModel.FormVersion.NB74.ordinal()) {
            for (int i=0, n=layoutSupport.getComponentCount(); i < n; i++) {
                code = layoutSupport.getComponentCode(i);
                if (code != null) {
                    Iterator it = code.getStatementsIterator();
                    while (it.hasNext()) {
                        saveCodeStatement((CodeStatement) it.next(), buf2, subIndent);
                    }
                }
            }
        } // Otherwise optimizing: don't save lenghty XML for components added via simple add method.
          // From our standard layouts this applies to OverlayLayout and FlowLayout with alignOnBaseline set.

        if (buf2.length() > 0) {
            buf.append(indent);
            addElementOpen(buf, XML_LAYOUT_CODE);

            buf.append(buf2.toString());

            buf.append(indent);
            addElementClose(buf, XML_LAYOUT_CODE);
        }
    }

    private void saveVisualComponent(RADVisualComponent component,
                                     StringBuffer buf, String indent)
    {
        saveComponent(component, buf, indent);

        if (component.isInLayeredPane()) {
            formModel.raiseVersionLevel(FormModel.FormVersion.NB74, FormModel.FormVersion.NB74);
            formModel.setMaxVersionLevel(FormModel.LATEST_VERSION);
        }

        RADVisualContainer container = component.getParentContainer();
        if (container == null || container.getLayoutSupport() == null)
            return;

        int componentIndex = container.getIndexOf(component);
        LayoutConstraints constr =
            container.getLayoutSupport().getConstraints(componentIndex);
        if (constr == null)
            return; // no constraints

        StringBuffer buf2 = new StringBuffer(); // [might be not used at all]
        int convIndex = saveConstraints(constr, buf2,
                                        indent + ONE_INDENT + ONE_INDENT);
        if (convIndex >= 0) { // standard constraints (saved in buf2)
            buf.append(indent);
            addElementOpen(buf, XML_CONSTRAINTS);
            buf.append(indent).append(ONE_INDENT);
            addElementOpenAttr(
                buf,
                XML_CONSTRAINT,
                new String[] { ATTR_CONSTRAINT_LAYOUT, ATTR_CONSTRAINT_VALUE },
                new String[] { PersistenceObjectRegistry.getPrimaryName(
                                   layout31Names[convIndex]),
                               PersistenceObjectRegistry.getPrimaryName(
                                   layout31ConstraintsNames[convIndex]) }
            );
            buf.append(buf2);
            buf.append(indent).append(ONE_INDENT);
            addElementClose(buf, XML_CONSTRAINT);
            buf.append(indent);
            addElementClose(buf, XML_CONSTRAINTS);
        }
    }

    private int saveConstraints(LayoutConstraints constr,
                                StringBuffer buf,
                                String indent)
    {
        // constraints of BorderLayout
        if (constr instanceof BorderLayoutSupport.BorderConstraints) {
            String position = (String) constr.getConstraintsObject();
            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "BorderConstraints", // NOI18N
                new String[] { "direction" }, // NOI18N
                new String[] { position });

            return LAYOUT_BORDER;
        }

        // constraints of GridBagLayout
        if (constr instanceof GridBagLayoutSupport.GridBagLayoutConstraints) {
            java.awt.GridBagConstraints gbConstr =
                (java.awt.GridBagConstraints) constr.getConstraintsObject();

            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "GridBagConstraints", // NOI18N
                new String[] { "gridX", "gridY", "gridWidth", "gridHeight", // NOI18N
                               "fill", "ipadX", "ipadY", // NOI18N
                               "insetsTop", "insetsLeft", // NOI18N
                               "insetsBottom", "insetsRight", // NOI18N
                               "anchor", "weightX", "weightY" }, // NOI18N
                new String[] { Integer.toString(gbConstr.gridx),
                               Integer.toString(gbConstr.gridy),
                               Integer.toString(gbConstr.gridwidth),
                               Integer.toString(gbConstr.gridheight),
                               Integer.toString(gbConstr.fill),
                               Integer.toString(gbConstr.ipadx),
                               Integer.toString(gbConstr.ipady),
                               Integer.toString(gbConstr.insets.top),
                               Integer.toString(gbConstr.insets.left),
                               Integer.toString(gbConstr.insets.bottom),
                               Integer.toString(gbConstr.insets.right),
                               Integer.toString(gbConstr.anchor),
                               Double.toString(gbConstr.weightx),
                               Double.toString(gbConstr.weighty) });

            return LAYOUT_GRIDBAG;
        }

        // constraints of JTabbedPane
        if (constr instanceof JTabbedPaneSupport.TabConstraints) {
            JTabbedPaneSupport.TabConstraints tabConstr =
                (JTabbedPaneSupport.TabConstraints) constr;

            StringBuffer buf2 = new StringBuffer();
            Node.Property[] tabProperties = constr.getProperties();

            for (int i=0; i < tabProperties.length; i++) {
                FormProperty prop = (FormProperty) tabProperties[i];
                if (prop.isChanged())
                    saveProperty(
                        prop,
                        prop.getName().substring("TabConstraints ".length()), // NOI18N
                        buf2,
                        indent + ONE_INDENT);
            }

            buf.append(indent);
            if (buf2.length() > 0) {
                addElementOpenAttr(
                    buf,
                    "JTabbedPaneConstraints", // NOI18N
                    new String[] { "tabName", "toolTip" }, // NOI18N
                    new String[] { tabConstr.getTitle(),
                                   tabConstr.getToolTip() });
                buf.append(buf2);
                buf.append(indent);
                addElementClose(buf, "JTabbedPaneConstraints"); // NOI18N
            }
            else {
                addLeafElementOpenAttr(
                    buf,
                    "JTabbedPaneConstraints", // NOI18N
                    new String[] { "tabName", "toolTip" }, // NOI18N
                    new String[] { tabConstr.getTitle(),
                                   tabConstr.getToolTip() });
            }

            return LAYOUT_JTAB;
        }

        // constraints of JSplitPane
        if (constr instanceof JSplitPaneSupport.SplitConstraints) {
            Object constrObject = constr.getConstraintsObject();
            String position;

            if (javax.swing.JSplitPane.TOP.equals(constrObject))
                position = "top"; // NOI18N
            else if (javax.swing.JSplitPane.BOTTOM.equals(constrObject))
                position = "bottom"; // NOI18N
            else if (javax.swing.JSplitPane.LEFT.equals(constrObject))
                position = "left"; // NOI18N
            else
                position = "right"; // NOI18N

            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "JSplitPaneConstraints", // NOI18N
                new String[] { "position" }, // NOI18N
                new String[] { position });

            return LAYOUT_JSPLIT;
        }

        // constraints of CardLayout
        if (constr instanceof CardLayoutSupport.CardConstraints) {
            String card = (String) constr.getConstraintsObject();
            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "CardConstraints", // NOI18N
                new String[] { "cardName" }, // NOI18N
                new String[] { card });

            return LAYOUT_CARD;
        }

        // constraints of AbsoluteLayout
        if (constr instanceof AbsoluteLayoutSupport.AbsoluteLayoutConstraints) {
            java.awt.Rectangle r =
                ((AbsoluteLayoutSupport.AbsoluteLayoutConstraints)constr)
                    .getBounds();

            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                "AbsoluteConstraints", // NOI18N
                new String[] { "x", "y", "width", "height" }, // NOI18N
                new String[] { Integer.toString(r.x),
                               Integer.toString(r.y),
                               Integer.toString(r.width),
                               Integer.toString(r.height) });

            return LAYOUT_ABSOLUTE;
        }

        return -1;
    }

    private void saveMenuComponent(RADComponent component, StringBuffer buf, String indent) {
        saveComponent(component, buf, indent);

        if (component instanceof ComponentContainer) {
            RADComponent[] children =((ComponentContainer)component).getSubBeans();
            if (children.length > 0) {
                buf.append(indent); addElementOpen(buf, XML_SUB_COMPONENTS);
                for (int i = 0; i < children.length; i++) {
                    String elementType = children[i] instanceof ComponentContainer
                            ? XML_MENU_CONTAINER : XML_MENU_COMPONENT;
                    buf.append(indent).append(ONE_INDENT);
                    addElementOpenAttr(
                        buf,
                        elementType,
                        new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME },
                        new String[] { children[i].getBeanClass().getName(),
                                       children[i].getName() });
                    saveMenuComponent(children[i], buf, indent + ONE_INDENT + ONE_INDENT);
                    buf.append(indent).append(ONE_INDENT); addElementClose(buf, elementType);
                }
                buf.append(indent); addElementClose(buf, XML_SUB_COMPONENTS);
            }
        }
    }

    private void saveComponent(RADComponent component, StringBuffer buf, String indent) {
        // 1. Properties
        if (!JavaCodeGenerator.VALUE_SERIALIZE.equals(
                component.getAuxValue(JavaCodeGenerator.AUX_CODE_GENERATION)))
        {   // save properties only if the component is not to be serialized
            saveProperties(component.getKnownBeanProperties(),
                           XML_PROPERTIES, buf, indent);

            // try to save accessibility properties
            FormProperty[] accProps = component.getKnownAccessibilityProperties();
            saveProperties(accProps, XML_A11Y_PROPERTIES, buf, indent);
        }

        // 2. Binding properties
        saveBindingProperties(component, buf, indent);
        
        // 3. Synthetic properties
        if (component instanceof RADVisualFormContainer)
            saveSyntheticProperties(component, buf, indent);

        // 4. Events
        saveEvents(component.getKnownEvents(), buf, indent);

        // 5. Aux Values
        Map<String,Object> auxValues = component.getAuxValues();
        // Form settings are stored as a part of AuxValues of top-level container
        if (component == formModel.getTopRADComponent()) {
            auxValues = (auxValues == null) ? new TreeMap<String,Object>() : new TreeMap<String,Object>(auxValues);
            addFormSettings(auxValues);
        } else if (component.getAuxValue(RADVisualComponent.PROP_LAYER) != null
                   && (!(component instanceof RADVisualComponent) || !((RADVisualComponent)component).isInLayeredPane())) {
            // don't save layer value for component that was moved away from JLayeredPane
            auxValues = new TreeMap<String,Object>(auxValues);
            auxValues.remove(RADVisualComponent.PROP_LAYER);
        }
        if (auxValues != null && auxValues.size() > 0) {
//            buf.append("\n"); // NOI18N
            buf.append(indent); addElementOpen(buf, XML_AUX_VALUES);
            saveAuxValues(auxValues, buf, indent + ONE_INDENT);
            buf.append(indent); addElementClose(buf, XML_AUX_VALUES);
        }
    }

    private boolean saveProperties(FormProperty[] props,
                                   String blockName,
                                   StringBuffer buf,
                                   String indent)
    {
        int i=0;
        do {
            if (i >= props.length)
                return false; // nothing saved
            FormProperty prop = props[i];
            if ((prop.isChanged() && !ResourceSupport.isInjectedProperty(prop))
                    || prop.getPreCode() != null
                    || prop.getPostCode() != null)
                break;
            i++;
        }
        while (true);

        buf.append(indent);
        addElementOpen(buf, blockName);

        for (i=0; i < props.length; i++) {
            FormProperty prop = props[i];
            if (!prop.isChanged() || ResourceSupport.isInjectedProperty(prop)) {
                if (prop.getPreCode() != null || prop.getPostCode() != null) {
                    buf.append(indent).append(ONE_INDENT);
                    // in this case save only the pre/post code
                    addLeafElementOpenAttr(
                        buf,
                        XML_PROPERTY,
                        new String[] {
                            ATTR_PROPERTY_NAME,
                            ATTR_PROPERTY_PRE_CODE,
                            ATTR_PROPERTY_POST_CODE,
                        },
                        new String[] {
                            prop.getName(),
                            prop.getPreCode(),
                            prop.getPostCode(),
                        });
                }
                continue; // not changed, so do not save value
            }

            saveProperty(prop, prop.getName(), buf, indent + ONE_INDENT);
        }

        buf.append(indent);
        addElementClose(buf, blockName);

        return true;
    }

    private boolean saveProperty(FormProperty property,
                                 String propertyName,
                                 StringBuffer buf,
                                 String indent)
    {
        Object value;
        Object realValue;
        try {
            value = property.getValue();
            realValue = property.getRealValue();
        }
        catch (Exception ex) {
            annotateException( ex,
                    FormUtils.getFormattedBundleString(
                        "FMT_ERR_CannotGetPropertyValue", // NOI18N
                        new Object[] { property.getName() })
                    );
            nonfatalErrors.add(ex);
            return false;
        }

        String resourceKey = null;
        String noResource = null;
        org.w3c.dom.Node valueNode = null;
        String encodedValue = null;
        String encodedSerializeValue = null;
        boolean containsInvalidXMLChars = false;

        PropertyEditor prEd = property.getCurrentEditor();	
        
//        if (prEd instanceof FontEditor) { // Issue 82465: new prEd in NB 6.0
//            raiseFormatVersion(NB60_VERSION);
//        }

        if (value instanceof ResourceValue) {
            resourceKey = ((ResourceValue)value).getKey();
        }
//        if (resourceKey != null) { // just save the key, not the value
//            raiseFormatVersion(NB60_VERSION);
//        }
        if (resourceKey == null) {
            if (prEd instanceof ResourceWrapperEditor) {
                prEd = ((ResourceWrapperEditor)prEd).getDelegatedPropertyEditor();
                if (ResourceSupport.isResourceableProperty(property)
                        && ResourceSupport.isExcludedProperty(property)) {
                    noResource = "true"; // NOI18N
//                    raiseFormatVersion(NB60_VERSION);
                }
            }

            if (prEd instanceof BeanPropertyEditor) {
                prEd.setValue(value);
                if (((BeanPropertyEditor)prEd).valueIsBeanProperty()) {
                    valueNode = saveBeanToXML(realValue.getClass(), getTopCocument());
                }
            }
            if (valueNode == null && prEd instanceof XMLPropertyEditor) {
                prEd.setValue(value);
                valueNode = ((XMLPropertyEditor)prEd).storeToXML(getTopCocument());
    //            if (valueNode == null) { // property editor refused to save the value
    //                PersistenceException ex = new PersistenceException(
    //                                   "Cannot save the property value"); // NOI18N
    //                String msg = FormUtils.getFormattedBundleString(
    //                                 "FMT_ERR_CannotSaveProperty", // NOI18N
    //                                 new Object[] { property.getName() });
    //                annotate(ex, ErrorManager.ERROR, msg);
    //                nonfatalErrors.add(ex);
    //                return false;
    //            }
            }
            if (valueNode == null) { // save as primitive or serialized value
                encodedValue = encodePrimitiveValue(value);
                String encodedInvalidXMLChars = encodeInvalidXMLChars(encodedValue);
                if (encodedInvalidXMLChars != null) {
                    containsInvalidXMLChars = true;
                    encodedValue = encodedInvalidXMLChars;
                }
                if (encodedValue == null) {
                    try {
                        encodedSerializeValue = encodeValue(value);
                    }
                    catch (Exception ex) {
                        annotateException(ex,
                                FormUtils.getFormattedBundleString(
                                    "FMT_ERR_CannotSaveProperty", // NOI18N
                                    new Object[] { property.getName() })
                                );
                        nonfatalErrors.add(ex);
                        return false;
                    }
                }
            }
        }

        buf.append(indent);

        if (valueNode != null || encodedSerializeValue != null) {
            // value is encoded by XMLPrpertyEditor or serialized 
            addElementOpenAttr(
                buf,
                XML_PROPERTY,
                new String[] {
                    ATTR_PROPERTY_NAME,
                    ATTR_PROPERTY_TYPE,
                    ATTR_PROPERTY_NORES,
                    ATTR_PROPERTY_EDITOR,
                    ATTR_PROPERTY_PRE_CODE,
                    ATTR_PROPERTY_POST_CODE },
                new String[] {
                    propertyName,		    
                    property.getValueType().getName(),
                    noResource,
                    PersistenceObjectRegistry.getPrimaryName(prEd.getClass().getName()),
                    property.getPreCode(),
                    property.getPostCode() });

	    if (valueNode != null) {					
		if( prEd instanceof BeanPropertyEditor 
		    && ((BeanPropertyEditor) prEd).valueIsBeanProperty() ) 
		{   // the property is a bean,
		    // so there could be some children nodes ...
		    saveBeanProperty((BeanPropertyEditor) prEd, valueNode, buf, indent + ONE_INDENT);
		} else {
		    saveNodeIntoText(buf, valueNode, indent + ONE_INDENT);					    		    
		}                
            }		    
            else {
                buf.append(indent).append(ONE_INDENT);
                addLeafElementOpenAttr(
                    buf,
                    XML_SERIALIZED_PROPERTY_VALUE,
                    new String[] { ATTR_PROPERTY_VALUE },
                    new String[] { encodedSerializeValue });
            }
            buf.append(indent);
            addElementClose(buf, XML_PROPERTY);
        }
        else { // primitive value or resource - just one element with attributes
            String[] attrs = new String[] {
                    ATTR_PROPERTY_NAME,
                    ATTR_PROPERTY_TYPE,
                    ATTR_PROPERTY_VALUE,
                    ATTR_PROPERTY_RES_KEY,
                    ATTR_PROPERTY_NORES,
                    ATTR_PROPERTY_PRE_CODE,
                    ATTR_PROPERTY_POST_CODE };
            String[] values = new String[] {
                    propertyName,
                    property.getValueType().getName(),
                    encodedValue,
                    resourceKey,
                    noResource,
                    property.getPreCode(),
                    property.getPostCode() };
            if (containsInvalidXMLChars) {
                formModel.raiseVersionLevel(FormModel.FormVersion.NB71, FormModel.FormVersion.NB71);
                formModel.setMaxVersionLevel(FormModel.LATEST_VERSION);
                attrs = append(attrs, ATTR_PROPERTY_INVALID_XML_CHARS);
                values = append(values, "true"); // NOI18N
            }
            addLeafElementOpenAttr(buf, XML_PROPERTY, attrs, values);
        }
        return true;
    }

    /**
     * Appends specified element at the end of the array.
     * 
     * @param array array that should be enlarged.
     * @param element element that should be appended.
     * @return new array with the element appended.
     */
     private static String[] append(String[] array, String element) {
        String[] newArray = new String[array.length+1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = element;
        return newArray;
    }

    /**
     * Encodes invalid XML 1.0 characters using valid ones.
     * Note that this method doesn't handle characters like &lt;
     * %gt; etc. These characters are valid within XML, just
     * problematic in textual version of the XML. Instead
     * this method handles characters like \b that must not
     * appear in XML 1.0 at all.
     * 
     * @param xml string to encode.
     * @return {@code null} if there were no invalid characters
     * in the given string. It returns encoded
     * version of the given string otherwise.
     */
    private static String encodeInvalidXMLChars(String xml) {
        if (xml == null) {
            return null;
        }
        boolean containsInvalidXMLChar = false;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<xml.length(); i++) {
            char c = xml.charAt(i);
            if  (isInvalidXMLChar(c) || (c == '\\')) {
                containsInvalidXMLChar = true;
                String hex = Integer.toHexString(c);
                sb.append("\\u"); // NOI18N
                for (int j=hex.length(); j<4; j++) {
                    sb.append('0');
                }
                sb.append(hex);
            } else {
                sb.append(c);
            }
        }
        return containsInvalidXMLChar ? sb.toString() : null;
    }

    /**
     * Determines whether the specified character is a valid
     * XML 1.0 character. 
     * 
     * @param c character to check.
     * @return {@code true} when the character is invalid,
     * returns {@code false} when the character is valid.
     * @see <a href="http://www.w3.org/TR/2006/REC-xml-20060816/#charsets">XML 1.0 Characters</a>
     */
    private static boolean isInvalidXMLChar(char c) {
        boolean valid = (c == '\t')
                || (c == '\n')
                || (c == '\r')
                || (c >= 0x20 && c <= 0xD7FF)
                || (c >= 0xE000 && c <= 0xFFFD)
                || (c >= 0x10000 && c <= 0x10FFFF);
        return !valid;
    }

    /**
     * Decodes string with encoded invalid XML characters.
     * 
     * @param xml string with encoded invalid XML characters.
     * @return string with all characters decoded.
     */
    private static String decodeInvalidXMLChars(String xml) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<xml.length(); i++) {
            char c = xml.charAt(i);
            if (c == '\\') {
                char d = xml.charAt(i+1);
                if (d == '\\') {
                    sb.append('\\');
                    i++;
                } else {
                    // expecting uXXXX
                    if (d != 'u') {
                        throw new IllegalArgumentException();
                    }
                    String code = xml.substring(i+2, i+6);
                    char ch = (char)Integer.parseInt(code, 16);
                    sb.append(ch);
                    i+=5;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    private org.w3c.dom.Node saveBeanToXML(Class type, org.w3c.dom.Document doc) {
	org.w3c.dom.Element el = doc.createElement(XML_PROPERTY_BEAN);
	el.setAttribute(ATTR_PROPERTY_TYPE, type.getName());	
	return el;
    }
    
    private void saveBeanProperty(BeanPropertyEditor beanPropertyEditor, org.w3c.dom.Node valueNode, StringBuffer buf, String indent) {	
	boolean children = false;
	FormProperty[] props = (FormProperty[]) beanPropertyEditor.getProperties();

	NamedNodeMap attributes = valueNode.getAttributes();
	String[] attrNames = new String[attributes.getLength()];
	String[] attrValues = new String[attributes.getLength()];

	for (int i = 0; i < attrValues.length; i++) {
	    attrNames[i] = attributes.item(i).getNodeName();
	    attrValues[i] = attributes.item(i).getNodeValue();
	}
			
	for (int i=0; i<props.length; i++) {
	    if( props[i].isChanged() ) {
		if(!children) {
		    // we found the first child property, 		    
		    // let's start the element tag	    
		    buf.append(indent);
		    addElementOpenAttr(buf,
					valueNode.getNodeName(),
					attrNames,
					attrValues);
		    children = true;
		}		
		saveProperty(props[i],
			 props[i].getName(),
			 buf,
			 indent + ONE_INDENT);	
	    }			
	}		    			    					

	if(children) {	    
	    // there were children properties,
	    // we should close the element tag
	    buf.append(indent);	    
	    addElementClose(buf, valueNode.getNodeName());			    
	} else {
	    // there were no children properties,
	    // let's save the node as it is
	    saveNodeIntoText(buf, valueNode, indent + ONE_INDENT);	    
	}
    }
    
    private boolean saveValue(Object value,
                              Class valueType,
                              PropertyEditor prEd,
                              StringBuffer buf,
                              String indent)
    {
        String encodedValue = null;
        String encodedSerializeValue = null;
        org.w3c.dom.Node valueNode = null;

        if (prEd instanceof XMLPropertyEditor) {
            prEd.setValue(value);
            valueNode = ((XMLPropertyEditor)prEd).storeToXML(getTopCocument());
            if (valueNode == null) { // property editor refused to save the value
                PersistenceException ex = new PersistenceException(
                                   "Cannot save the property value"); // NOI18N
                String msg = FormUtils.getFormattedBundleString(
                                 "FMT_ERR_CannotSaveProperty2", // NOI18N
                                 new Object[] { prEd.getClass().getName() });
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                return false;
            }
        }
        else {
            encodedValue = encodePrimitiveValue(value);
            if (encodedValue == null) {
                try {
                    encodedSerializeValue = encodeValue(value);
                }
                catch (Exception ex) {
                    annotateException(ex,
                            FormUtils.getFormattedBundleString(
                                "FMT_ERR_CannotSaveProperty3", // NOI18N
                                new Object[] { valueType.getClass().getName() })
                            );
                    nonfatalErrors.add(ex);
                    return false;
                }
            }
        }

        buf.append(indent);

        if (encodedValue != null) {
            addLeafElementOpenAttr(
                buf,
                XML_VALUE,
                new String[] { ATTR_PROPERTY_TYPE, ATTR_PROPERTY_VALUE },
                new String[] { valueType.getName(), encodedValue });
        }
        else {
            addElementOpenAttr(
                buf,
                XML_VALUE,
                new String[] { ATTR_PROPERTY_TYPE, ATTR_PROPERTY_EDITOR },
                new String[] { valueType.getName(), prEd.getClass().getName() });

            if (valueNode != null) {
                saveNodeIntoText(buf, valueNode, indent + ONE_INDENT);
            }
            else {
                buf.append(indent).append(ONE_INDENT);
                addLeafElementOpenAttr(
                    buf,
                    XML_SERIALIZED_PROPERTY_VALUE,
                    new String[] { ATTR_PROPERTY_VALUE },
                    new String[] { encodedSerializeValue });
            }
            buf.append(indent);
            addElementClose(buf, XML_VALUE);
        }
        return true;
    }

    private void saveBindingProperties(RADComponent component, StringBuffer buf, String indent) {
        boolean anyProp = false;
        String indent2 = null;

        BindingProperty[] props = component.getKnownBindingProperties();
        for (int i=0; i < props.length; i++) {
            BindingProperty prop = props[i];

            Object value = null;
            try {
                value = prop.getValue();
            }
            catch (Exception ex) {
                annotateException(ex,
                        FormUtils.getFormattedBundleString(
                            "FMT_ERR_CannotGetPropertyValue", // NOI18N
                            new Object[] { prop.getName() })
                        );
                nonfatalErrors.add(ex);
                continue;
            }
            
            // don't save default values
            if (value == null) continue;
            
            // PENDING encodePrimitiveValue(value);

            if (!anyProp) {
                buf.append(indent);
                addElementOpen(buf, XML_BINDING_PROPERTIES);
                indent2 = indent + ONE_INDENT;
                anyProp = true;
            }

            saveBinding(prop, buf, indent2);            
        }

        if (anyProp) {
            buf.append(indent);
            addElementClose(buf, XML_BINDING_PROPERTIES);
        }
    }

    private void saveBindingParameters(MetaBinding binding, StringBuffer buf, String indent) {
        Map<String,String> parameters = binding.getParameters();
        Iterator<Map.Entry<String,String>> iter = parameters.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,String> entry = iter.next();
            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                XML_BINDING_PARAMETER,
                new String[] {
                    ATTR_BINDING_PARAMETER_NAME,
                    ATTR_BINDING_PARAMETER_VALUE
                },
                new String[] {
                    entry.getKey(),
                    entry.getValue()
                });
        }
    }
    
    private void saveBinding(BindingProperty prop, StringBuffer buf, String indent) {        
        String propName = prop.getName();
        MetaBinding binding = prop.getValue();
        buf.append(indent);
        if (binding.hasSubBindings() || !binding.getParameters().isEmpty()
                || binding.isIncompletePathValueSpecified() || binding.isNullValueSpecified()
                || binding.isConverterSpecified() || binding.isValidatorSpecified()
                || binding.isNameSpecified()) {
            addElementOpenAttr(
                buf,
                XML_BINDING_PROPERTY,
                new String[] {
                    ATTR_PROPERTY_NAME,
                    ATTR_BINDING_SOURCE,
                    ATTR_BINDING_SOURCE_PATH,
                    ATTR_BINDING_TARGET,
                    ATTR_BINDING_TARGET_PATH,
                    ATTR_BINDING_UPDATE_STRATEGY,
                    ATTR_BINDING_IMMEDIATELY
                },
                new String[] {
                    propName,
                    binding.getSource().getName(),
                    binding.getSourcePath(),
                    binding.getTarget().getName(),
                    binding.getTargetPath(),
                    Integer.toString(binding.getUpdateStrategy()),
                    Boolean.toString(binding.isBindImmediately())
                });
            
            // Save parameters
            String indent2 = indent + ONE_INDENT;
            saveBindingParameters(binding, buf, indent2);

            if (binding.isNullValueSpecified()) {
                saveProperty(prop.getNullValueProperty(), prop.getNullValueProperty().getName(), buf, indent2);
            }
            if (binding.isIncompletePathValueSpecified()) {
                saveProperty(prop.getIncompleteValueProperty(), prop.getIncompleteValueProperty().getName(), buf, indent2);
            }
            if (binding.isConverterSpecified()) {
                saveProperty(prop.getConverterProperty(), prop.getConverterProperty().getName(), buf, indent2);
            }
            if (binding.isValidatorSpecified()) {
                saveProperty(prop.getValidatorProperty(), prop.getValidatorProperty().getName(), buf, indent2);
            }
            if (binding.isNameSpecified()) {
                saveProperty(prop.getNameProperty(), prop.getNameProperty().getName(), buf, indent2);
            }

            // Save subbindings    
            if (binding.hasSubBindings()) {
                for (MetaBinding subbinding : binding.getSubBindings()) {
                    buf.append(indent2);
                    addElementOpenAttr(
                        buf,
                        XML_SUBBINDING,
                        new String[] {
                            ATTR_BINDING_SOURCE_PATH,
                            ATTR_BINDING_TARGET_PATH
                        },
                        new String[] {
                            subbinding.getSourcePath(),
                            subbinding.getTargetPath()
                        });

                    // Save parameters
                    String indent3 = indent2 + ONE_INDENT;
                    saveBindingParameters(subbinding, buf, indent3);

                    buf.append(indent2);
                    addElementClose(buf, XML_SUBBINDING);
                }
            }
            
            buf.append(indent);
            addElementClose(buf, XML_BINDING_PROPERTY);
        } else {
            addLeafElementOpenAttr(
                buf,
                XML_BINDING_PROPERTY,
                new String[] {
                    ATTR_PROPERTY_NAME,
                    ATTR_BINDING_SOURCE,
                    ATTR_BINDING_SOURCE_PATH,
                    ATTR_BINDING_TARGET,
                    ATTR_BINDING_TARGET_PATH,
                    ATTR_BINDING_UPDATE_STRATEGY,
                    ATTR_BINDING_IMMEDIATELY
                },
                new String[] {
                    propName,
                    binding.getSource().getName(),
                    binding.getSourcePath(),
                    binding.getTarget().getName(),
                    binding.getTargetPath(),
                    Integer.toString(binding.getUpdateStrategy()),
                    Boolean.toString(binding.isBindImmediately())
                });
        }
    }

    private void saveSyntheticProperties(RADComponent component, StringBuffer buf, String indent) {
        boolean anyProp = false;
        String indent2 = null;

        // compatibility hack for saving form's menu bar (part III)
        if (component instanceof RADVisualFormContainer) {
            RADComponent menuComp = ((RADVisualFormContainer)component).getContainerMenu();
            if (menuComp != null) {
                buf.append(indent);
                addElementOpen(buf, XML_SYNTHETIC_PROPERTIES);
                indent2 = indent + ONE_INDENT;
                anyProp = true;

                buf.append(indent2);
                addLeafElementOpenAttr(buf,
                    XML_SYNTHETIC_PROPERTY,
                    new String[] { ATTR_PROPERTY_NAME,
                                   ATTR_PROPERTY_TYPE,
                                   ATTR_PROPERTY_VALUE },
                    new String[] { "menuBar", // NOI18N
                                   "java.lang.String", // NOI18N
                                   menuComp.getName() });
            }
        }

        Node.Property[] props = component.getSyntheticProperties();
        for (int i=0; i < props.length; i++) {
            Node.Property prop = props[i];

            if (!prop.canWrite())
                continue; // don't save read-only properties

            if (Boolean.TRUE.equals(prop.getValue("defaultValue"))) // NOI18N
                continue; // don't save default values

            Object value = null;
            try {
                value = prop.getValue();
            }
            catch (Exception ex) {
                annotateException(ex,
                        FormUtils.getFormattedBundleString(
                            "FMT_ERR_CannotGetPropertyValue", // NOI18N
                            new Object[] { prop.getName() })
                        );
                nonfatalErrors.add(ex);
                continue;
            }
            String valueType = prop.getValueType().getName();
            String encodedValue = encodePrimitiveValue(value);
            if (encodedValue == null) {
                try {
                    encodedValue = encodeValue(value);
                }
                catch (Exception ex) {
                    annotateException(ex,
                            FormUtils.getFormattedBundleString(
                                "FMT_ERR_CannotSaveProperty", // NOI18N
                                new Object[] { prop.getName() })
                            );
                    nonfatalErrors.add(ex);
                    continue;
                }
            }

            if (!anyProp) {
                buf.append(indent);
                addElementOpen(buf, XML_SYNTHETIC_PROPERTIES);
                indent2 = indent + ONE_INDENT;
                anyProp = true;
            }

            buf.append(indent2);
            addLeafElementOpenAttr(
                buf,
                XML_SYNTHETIC_PROPERTY,
                new String[] {
                    ATTR_PROPERTY_NAME,
                    ATTR_PROPERTY_TYPE,
                    ATTR_PROPERTY_VALUE,
                },
                new String[] {
                    prop.getName(),
                    valueType,
                    encodedValue,
                }
                );
        }

        if (anyProp) {
            buf.append(indent);
            addElementClose(buf, XML_SYNTHETIC_PROPERTIES);
        }
    }

    private void saveEvents(Event[] events, StringBuffer buf, String indent) {
        boolean anyEvent = false;
        String indent2 = null;
        StringBuffer strbuf;

        for (int i=0; i < events.length; i++) {
            Event event = events[i];
            if (!event.hasEventHandlers())
                continue;

            if (!anyEvent) {
                buf.append(indent);
                addElementOpen(buf, XML_EVENTS);
                indent2 = indent + ONE_INDENT;
                anyEvent = true;
            }

            strbuf = new StringBuffer(50);
            Class[] params = event.getListenerMethod().getParameterTypes();
            for (int j=0; j < params.length; j++) {
                strbuf.append(params[j].getName());
                if (j + 1 < params.length)
                    strbuf.append(","); // NOI18N
            }
            String paramString = strbuf.toString();

            strbuf = new StringBuffer(50);
            String[] handlers = event.getEventHandlers();
            for (int j=0; j < handlers.length; j++) {
                strbuf.append(handlers[j]);
                if (j + 1 < handlers.length)
                    strbuf.append(","); // NOI18N
            }
            String handlerString = strbuf.toString();

            buf.append(indent2);
            addLeafElementOpenAttr(
                buf,
                XML_EVENT,
                new String[] {
                    ATTR_EVENT_NAME,
                    ATTR_EVENT_LISTENER,
                    ATTR_EVENT_PARAMS,
                    ATTR_EVENT_HANDLER
                },
                new String[] {
                    event.getListenerMethod().getName(),
                    event.getListenerMethod().getDeclaringClass().getName(),
                    paramString,
                    handlerString
                }
            );
        }

        if (anyEvent) {
            buf.append(indent);
            addElementClose(buf, XML_EVENTS);
        }
    }

    private void saveAuxValues(Map<String,Object> auxValues, StringBuffer buf, String indent) {
        for (Iterator<Map.Entry<String,Object>> it = auxValues.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String,Object> entry = it.next();
            String valueName = entry.getKey();
            Object value = entry.getValue();
            if (value == null) continue; // such values are not saved
            String valueType = value.getClass().getName();
            String encodedValue = encodePrimitiveValue(value);
            if (encodedValue == null) {
                try {
                    encodedValue = encodeValue(value);
                }
                catch (Exception ex) {
                    annotateException(ex,
                            FormUtils.getFormattedBundleString(
                                "FMT_ERR_CannotSaveProperty", // NOI18N
                                new Object[] { valueName })
                            );
                    nonfatalErrors.add(ex);
                    continue;
                }
            }

            buf.append(indent);
            addLeafElementOpenAttr(
                buf,
                XML_AUX_VALUE,
                new String[] {
                    ATTR_AUX_NAME,
                    ATTR_AUX_VALUE_TYPE,
                    ATTR_AUX_VALUE },
                new String[] {
                    valueName,
                    valueType,
                    encodedValue
                }
            );
        }
    }

    private PropertyEditor createPropertyEditor(Class editorClass,
                                                Class propertyType,
                                                FormProperty property)
        throws ReflectiveOperationException
    {
        PropertyEditor ed = null;
        if (editorClass.equals(RADConnectionPropertyEditor.class)) {
            ed = new RADConnectionPropertyEditor(propertyType);
        } else if (editorClass.equals(ComponentChooserEditor.class)) {
            ed = new ComponentChooserEditor(new Class[] {propertyType});
        } else if (editorClass.equals(EnumEditor.class)) {
            if (property instanceof RADProperty) {
                RADProperty prop = (RADProperty)property;
                ed = prop.createEnumEditor(prop.getPropertyDescriptor());
            }
            if (ed == null) {
                ed = RADProperty.createDefaultEnumEditor(propertyType);
            }
        } else {
            ed = (PropertyEditor) editorClass.getDeclaredConstructor().newInstance();
        }

        if (property != null)
            property.getPropertyContext().initPropertyEditor(ed, property);
        else if (ed instanceof FormAwareEditor)
            ((FormAwareEditor)ed).setContext(formModel, null);

        return ed;
    }

    // ---------------------
    // The following code ensures persistence of code structure in XML. The
    // code is quite general except special hacks for meta components which
    // must be handled specially (as references) - as we don't save full code
    // yet but only its parts; components are saved separately. [This feature
    // is used only for saving/loading code of non-standard layout supports.]
    //
    // There are two possible ways how to save the code structure - to save
    // the code flow or the static structure.
    //
    // In the first case (code flow), a sequence of code statements is saved
    // (together with epxressions used by the statements). In the second case
    // (static structure), root code expressions are saved as trees including
    // all used expressions and all defined statements. Which style is used
    // is controlled by the codeFlow variable. [We use only code flow now.]

    // XML persistence of code structure - saving

    private void saveCodeExpression(CodeExpression exp,
                                    StringBuffer buf, String indent)
    {
        buf.append(indent);

        Object value = getExpressionsMap().get(exp);
        if (value != null) { // save expression reference only
            addLeafElementOpenAttr(buf,
                                   XML_CODE_EXPRESSION,
                                   new String[] { ATTR_EXPRESSION_ID },
                                   new String[] { value.toString() });
        }
        else { // save complete expression
            // create expression ID
            lastExpId++;
            String expId = Integer.toString(lastExpId);
            CodeVariable var = exp.getVariable();
            if (var != null)
                expId += "_" + var.getName(); // NOI18N
            getExpressionsMap().put(exp, expId);

            addElementOpenAttr(buf,
                               XML_CODE_EXPRESSION,
                               new String[] { ATTR_EXPRESSION_ID },
                               new String[] { expId });

            String subIndent = indent + ONE_INDENT;

            if (var != null)
                saveCodeVariable(var, buf, subIndent);

            saveExpressionOrigin(exp.getOrigin(), buf, subIndent);

            if (!codeFlow) {
                // if static code structure is being saved, statements are
                // saved inside their parent expressions
                Iterator it = CodeStructure.getDefinedStatementsIterator(exp);
                if (it.hasNext()) {
                    buf.append(subIndent);
                    addElementOpen(buf, XML_CODE_STATEMENTS);

                    String subSubIndent = subIndent + ONE_INDENT;
                    do {
                        saveCodeStatement((CodeStatement) it.next(),
                                          buf, subSubIndent);
                    }
                    while (it.hasNext());

                    buf.append(subIndent);
                    addElementClose(buf, XML_CODE_STATEMENTS);
                }
            }

            buf.append(indent);
            addElementClose(buf, XML_CODE_EXPRESSION);
        }
    }

    private void saveCodeVariable(CodeVariable var,
                                  StringBuffer buf, String indent)
    {
        buf.append(indent);
        if (getVariableSet().contains(var)) {
            addLeafElementOpenAttr(buf,
                                   XML_CODE_VARIABLE,
                                   new String[] { ATTR_VAR_NAME },
                                   new String[] { var.getName() });
        }
        else {
            addLeafElementOpenAttr(
                buf,
                XML_CODE_VARIABLE,
                new String[] { ATTR_VAR_NAME,
                               ATTR_VAR_TYPE,
                               ATTR_VAR_DECLARED_TYPE },
                new String[] { var.getName(),
                               Integer.toString(var.getType()),
                               var.getDeclaredType().getName() });

            getVariableSet().add(var);
        }
    }

    private void saveExpressionOrigin(CodeExpressionOrigin origin,
                                      StringBuffer buf, String indent)
    {
        buf.append(indent);
        addElementOpen(buf, XML_CODE_ORIGIN);

        String subIndent = indent + ONE_INDENT;

        CodeExpression parentExp = origin.getParentExpression();
        if (parentExp != null)
            saveCodeExpression(parentExp, buf, subIndent);

        Object metaObject = origin.getMetaObject();
        if (metaObject != null)
            saveOriginMetaObject(metaObject, buf, subIndent);
        else
            saveValue(origin.getValue(), origin.getType(), null,
                      buf, subIndent);

        saveParameters(origin.getCreationParameters(), buf, subIndent);

        buf.append(indent);
        addElementClose(buf, XML_CODE_ORIGIN);
    }

    private void saveCodeStatement(CodeStatement statement,
                                   StringBuffer buf, String indent)
    {
        buf.append(indent);
        addElementOpen(buf, XML_CODE_STATEMENT);

        String subIndent = indent + ONE_INDENT;

        if (codeFlow) {
            // if code flow is being saved, also the parent expression of
            // the statement must be saved for it
            CodeExpression parentExp = statement.getParentExpression();
            if (parentExp != null)
                saveCodeExpression(parentExp, buf, subIndent);
        }

        Object metaObject = statement.getMetaObject();
        if (metaObject != null)
            saveStatementMetaObject(metaObject, buf, subIndent);

        saveParameters(statement.getStatementParameters(), buf, subIndent);

        buf.append(indent);
        addElementClose(buf, XML_CODE_STATEMENT);
    }

    private void saveOriginMetaObject(Object metaObject,
                                      StringBuffer buf, String indent)
    {
        if (metaObject instanceof Node.Property) {
            Node.Property property = (Node.Property) metaObject;
            Object value;
            try {
                value = property.getValue();
            }
            catch (Exception ex) { // should not happen
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                return;
            }

            PropertyEditor prEd = property instanceof FormProperty ?
                                  ((FormProperty)property).getCurrentEditor() :
                                  property.getPropertyEditor();
            saveValue(value, property.getValueType(), prEd, buf,indent);
            return;
        }

        StringBuffer buf2 = new StringBuffer();
        String subIndent = indent + ONE_INDENT;
        String originType = null;

        if (metaObject instanceof Constructor) {
            Constructor ctor = (Constructor) metaObject;
            StringBuilder buf3 = new StringBuilder();
            Class[] paramTypes = ctor.getParameterTypes();

            for (int i=0; i < paramTypes.length; i++) {
                buf3.append(paramTypes[i].getName());
                if (i+1 < paramTypes.length)
                    buf3.append(", "); // NOI18N
            }

            buf2.append(subIndent);
            addLeafElementOpenAttr(
                buf2,
                XML_CODE_CONSTRUCTOR,
                new String[] { ATTR_MEMBER_CLASS,
                               ATTR_MEMBER_PARAMS },
                new String[] { ctor.getDeclaringClass().getName(),
                               buf3.toString() });

            originType = XML_CODE_CONSTRUCTOR;
        }

        // special code for handling meta component references
        else if (metaObject instanceof RADComponent) {
            RADComponent metacomp = (RADComponent) metaObject;

            buf2.append(subIndent);
            addLeafElementOpenAttr(
                buf2,
                XML_COMPONENT_REF,
                new String[] { ATTR_COMPONENT_NAME },
                new String[] { metacomp != formModel.getTopRADComponent() ?
                               metacomp.getName() : "." }); // NOI18N

            originType = XML_COMPONENT_REF;
        }

        else if (metaObject instanceof Method) {
            saveMethod((Method) metaObject, buf2, subIndent);
            originType = XML_CODE_METHOD;
        }

        else if (metaObject instanceof Field) {
            saveField((Field) metaObject, buf2, subIndent);
            originType = XML_CODE_FIELD;
        }

        if (originType == null)
            return; // unknown origin

        buf.append(indent);
        addElementOpenAttr(buf,
                           XML_ORIGIN_META_OBJECT,
                           new String[] { ATTR_META_OBJECT_TYPE },
                           new String[] { originType } );
        buf.append(buf2);
        buf.append(indent);
        addElementClose(buf, XML_ORIGIN_META_OBJECT);
    }

    private void saveStatementMetaObject(Object metaObject,
                                         StringBuffer buf, String indent)
    {
        StringBuffer buf2 = new StringBuffer();
        String subIndent = indent + ONE_INDENT;
        String statementType = null;

        if (metaObject instanceof Method) {
            saveMethod((Method) metaObject, buf2, subIndent);
            statementType = XML_CODE_METHOD;
        }
        else if (metaObject instanceof Field) {
            saveField((Field) metaObject, buf2, subIndent);
            statementType = XML_CODE_FIELD;
        }
        else if (metaObject instanceof CodeExpression) { // variable assignment
            CodeExpression exp = (CodeExpression) metaObject;
            if (exp.getVariable() != null) {
                saveCodeExpression(exp, buf2, subIndent);
                statementType = XML_CODE_EXPRESSION;
            }
        }
        // [... variable declaration statement]

        if (statementType == null)
            return; // unknown statement

        buf.append(indent);
        addElementOpenAttr(buf,
                           XML_STATEMENT_META_OBJECT,
                           new String[] { ATTR_META_OBJECT_TYPE },
                           new String[] { statementType } );
        buf.append(buf2);
        buf.append(indent);
        addElementClose(buf, XML_STATEMENT_META_OBJECT);
    }

    private void saveParameters(CodeExpression[] parameters,
                                StringBuffer buf, String indent)
    {
        if (parameters.length > 0) {
            buf.append(indent);
            addElementOpen(buf, XML_CODE_PARAMETERS);

            String subIndent = indent + ONE_INDENT;
            for (int i=0; i < parameters.length; i++)
                saveCodeExpression(parameters[i], buf, subIndent);

            buf.append(indent);
            addElementClose(buf, XML_CODE_PARAMETERS);
        }
    }

    private static void saveMethod(Method method,
                                   StringBuffer buf, String indent)
    {
        StringBuilder buf2 = new StringBuilder();
        Class[] paramTypes = method.getParameterTypes();

        for (int i=0; i < paramTypes.length; i++) {
            buf2.append(paramTypes[i].getName());
            if (i+1 < paramTypes.length)
                buf2.append(", "); // NOI18N
        }

        buf.append(indent);
        addLeafElementOpenAttr(
            buf,
            XML_CODE_METHOD,
            new String[] { ATTR_MEMBER_NAME,
                           ATTR_MEMBER_CLASS,
                           ATTR_MEMBER_PARAMS },
            new String[] { method.getName(),
                           method.getDeclaringClass().getName(),
                           buf2.toString() });
    }

    private static void saveField(Field field, StringBuffer buf, String indent)
    {
        buf.append(indent);
        addLeafElementOpenAttr(
            buf,
            XML_CODE_FIELD,
            new String[] { ATTR_MEMBER_NAME,
                           ATTR_MEMBER_CLASS },
            new String[] { field.getName(),
                           field.getDeclaringClass().getName() });
    }

    // -----------
    // XML persistence of code structure - loading

    private CodeExpression loadCodeExpression(org.w3c.dom.Node node) {
        String expId = getAttribute(node, ATTR_EXPRESSION_ID);
        if (expId == null)
            return null; // missing ID error

        CodeExpression exp = (CodeExpression) getExpressionsMap().get(expId);
        if (exp != null)
            return exp;

        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes == null)
            return null; // missing subnodes (expression content) error

        org.w3c.dom.Node variableNode = null;
        org.w3c.dom.Node originNode = null;
        org.w3c.dom.Node statementsNode = null;

        for (int i=0, n=childNodes.getLength(); i < n; i++) {
            org.w3c.dom.Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes

            String nodeName = childNode.getNodeName();

            if (XML_CODE_VARIABLE.equals(nodeName))
                variableNode = childNode;
            else if (XML_CODE_ORIGIN.equals(nodeName))
                originNode = childNode;
            else if (!codeFlow && XML_CODE_STATEMENTS.equals(nodeName))
                statementsNode = childNode;
        }

        if (originNode == null)
            return null; // missing origin error

        CodeExpressionOrigin origin = loadExpressionOrigin(originNode);
        if (origin == null)
            return null; // origin loading error

        // special code for handling meta component references
        Object originMetaObject = origin.getMetaObject();
        if (originMetaObject instanceof RADComponent) {
            // use the expression from meta component
            exp = ((RADComponent)originMetaObject).getCodeExpression();
        }
        else { // create a new expression normally
            exp = getCodeStructure().createExpression(origin);

            CodeVariable var = variableNode != null ?
                               loadCodeVariable(variableNode) : null;
            if (var != null)
                getCodeStructure().attachExpressionToVariable(exp, var);
        }

        getExpressionsMap().put(expId, exp);

        if (statementsNode != null) {
            childNodes = statementsNode.getChildNodes();
            if (childNodes != null) {
                for (int i=0, n=childNodes.getLength(); i < n; i++) {
                    org.w3c.dom.Node childNode = childNodes.item(i);

                    if (XML_CODE_STATEMENT.equals(childNode.getNodeName()))
                        loadCodeStatement(childNode, exp);
                }
            }
        }

        return exp;
    }

    private CodeVariable loadCodeVariable(org.w3c.dom.Node node) {
        org.w3c.dom.NamedNodeMap attr = node.getAttributes();
        if (attr == null)
            return null; // no attributes error

        node = attr.getNamedItem(ATTR_VAR_NAME);
        if (node == null)
            return null; // missing variable name error
        String name = node.getNodeValue();

        CodeVariable var = getCodeStructure().getVariable(name);
        if (var != null) {
            if ((var.getType() & (CodeVariable.LOCAL|CodeVariable.EXPLICIT_DECLARATION)) == CodeVariable.LOCAL) {
                // clashing with an implicitly created variable
                int i = name.length();
                while (Character.isDigit(name.charAt(i-1))) {
                    i--;
                }
                getCodeStructure().renameVariable(name, getCodeStructure().findFreeVariableName(name.substring(0, i)));
            } else {
                return var;
            }
        }

        node = attr.getNamedItem(ATTR_VAR_TYPE);
        if (node == null)
            return null; // missing variable type error
        int type = Integer.parseInt(node.getNodeValue());

        node = attr.getNamedItem(ATTR_VAR_DECLARED_TYPE);
        if (node == null)
            return null; // missing variable declared type error
        Class declaredType = null;

        try {
            declaredType = getClassFromString(node.getNodeValue());
        }
        catch (ClassNotFoundException ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        if (declaredType == null)
            return null; // variable declared type loading error

        return getCodeStructure().createVariable(type, declaredType, name);
    }

    private CodeExpressionOrigin loadExpressionOrigin(org.w3c.dom.Node node) {
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes == null)
            return null; // missing subnodes (origin content) error

        org.w3c.dom.Node parentExpNode = null;
        org.w3c.dom.Node metaObjectNode = null;
        org.w3c.dom.Node valueNode = null;
        org.w3c.dom.Node parametersNode = null;

        for (int i=0, n=childNodes.getLength(); i < n; i++) {
            org.w3c.dom.Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes

            String nodeName = childNode.getNodeName();

            if (XML_CODE_EXPRESSION.equals(nodeName))
                parentExpNode = childNode;
            else if (XML_ORIGIN_META_OBJECT.equals(nodeName))
                metaObjectNode = childNode;
            else if (XML_VALUE.equals(nodeName))
                valueNode = childNode;
            else if (XML_CODE_PARAMETERS.equals(nodeName))
                parametersNode = childNode;
        }

        if (metaObjectNode == null && valueNode == null)
            return null; // missing origin metaobject or value error

        CodeExpression parentExp;
        if (parentExpNode != null) {
            parentExp = loadCodeExpression(parentExpNode);
            if (parentExp == null)
                return null; // parent expression loading error
        }
        else parentExp = null; // origin without parent expression

        CodeExpression[] parameters = parametersNode != null ?
                                        loadParameters(parametersNode) :
                                        CodeStructure.EMPTY_PARAMS;
        if (parameters == null)
            return null; // error loading parameters

        CodeExpressionOrigin origin = null;

        if (metaObjectNode != null) {
            String metaObjectType = getAttribute(metaObjectNode,
                                                  ATTR_META_OBJECT_TYPE);
            childNodes = metaObjectNode.getChildNodes();
            if (metaObjectType != null && childNodes != null) {
                for (int i=0, n=childNodes.getLength(); i < n; i++) {
                    org.w3c.dom.Node childNode = childNodes.item(i);

                    String nodeName = childNode.getNodeName();
                    if (!metaObjectType.equals(nodeName))
                        continue;

                    if (XML_VALUE.equals(nodeName)) {
                        valueNode = childNode;
                        break;
                    }

                    if (XML_CODE_CONSTRUCTOR.equals(nodeName)) {
                        org.w3c.dom.NamedNodeMap attr = childNode.getAttributes();
                        if (attr == null)
                            return null; // no attributes error

                        node = attr.getNamedItem(ATTR_MEMBER_CLASS);
                        if (node == null)
                            return null; // missing constructor class error

                        Class ctorClass;
                        try {
                            ctorClass = getClassFromString(node.getNodeValue());
                        }
                        catch (ClassNotFoundException ex) {
                            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                            return null; // constructor class loading error
                        }

                        node = attr.getNamedItem(ATTR_MEMBER_PARAMS);
                        if (node == null)
                            return null; // missing constructor parameter types error

                        Class[] paramTypes;
                        StringTokenizer paramTokens =
                            new StringTokenizer(node.getNodeValue(), ", "); // NOI18N
                        List<Class> typeList = new ArrayList<Class>();
                        try {
                            while (paramTokens.hasMoreTokens()) {
                                typeList.add(getClassFromString(
                                                 paramTokens.nextToken()));
                            }
                            paramTypes = new Class[typeList.size()];
                            typeList.toArray(paramTypes);
                        }
                        catch (ClassNotFoundException ex) {
                            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                            return null; // parameters classes loading error
                        }

                        Constructor ctor;
                        try {
                            ctor = ctorClass.getConstructor(paramTypes);
                        }
                        catch (NoSuchMethodException ex) {
                            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                            return null; // constructor not found error
                        }

                        origin = CodeStructure.createOrigin(ctor, parameters);
                        break;
                    }

                    // special code for handling meta component references
                    if (XML_COMPONENT_REF.equals(nodeName)) {
                        String name = getAttribute(childNode,
                                                    ATTR_COMPONENT_NAME);
                        if (name == null)
                            return null; // missing component name error

                        RADComponent comp = name.equals(".") ? // NOI18N
                                formModel.getTopRADComponent() :
                                getComponentsMap().get(name);
                        if (comp == null)
                            return null; // no such component error

                        origin = comp.getCodeExpression().getOrigin();
                        break;
                    }

                    if (XML_CODE_METHOD.equals(nodeName)) {
                        Method m = loadMethod(childNode);
                        if (m == null)
                            return null; // method loading error

                        origin = CodeStructure.createOrigin(
                                                   parentExp, m, parameters);
                        break;
                    }

                    if (XML_CODE_FIELD.equals(nodeName)) {
                        Field f = loadField(childNode);
                        if (f == null)
                            return null; // field loading error

                        origin = CodeStructure.createOrigin(parentExp, f);
                        break;
                    }
                }
            }
        }

        if (origin == null) {
            if (valueNode == null)
                return null; // origin metaobject loading error

            String typeStr = getAttribute(valueNode, ATTR_PROPERTY_TYPE);
            if (typeStr == null)
                return null; // missing value type error

            Object editorOrValue = getPropertyEditorOrValue(valueNode);
            if (editorOrValue == NO_VALUE)
                return null; // value loading error

            Class valueType;
            try {
                valueType = getClassFromString(typeStr);
            }
            catch (Exception ex) { // does not happen
                return null; // value loading error
            }

            origin = editorOrValue instanceof PropertyEditor ?
                     FormCodeSupport.createOrigin(
                         valueType,
                         (PropertyEditor) editorOrValue) :
                     CodeStructure.createOrigin(
                         valueType,
                         editorOrValue,
                         editorOrValue != null ?
                             editorOrValue.toString() : "null"); // NOI18N
        }

        return origin;
    }

    private CodeStatement loadCodeStatement(org.w3c.dom.Node node,
                                            CodeExpression parentExp)
    {
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes == null)
            return null; // missing subnodes (statement content) error

        org.w3c.dom.Node parentExpNode = null;
        org.w3c.dom.Node metaObjectNode = null;
        org.w3c.dom.Node parametersNode = null;

        for (int i=0, n=childNodes.getLength(); i < n; i++) {
            org.w3c.dom.Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes

            String nodeName = childNode.getNodeName();

            if (XML_CODE_EXPRESSION.equals(nodeName)) {
                if (parentExp == null)
                    parentExpNode = childNode;
            }
            else if (XML_STATEMENT_META_OBJECT.equals(nodeName))
                metaObjectNode = childNode;
            else if (XML_CODE_PARAMETERS.equals(nodeName))
                parametersNode = childNode;
        }

        if (metaObjectNode == null)
            return null; // missing statement metaobject error

        if (parentExpNode != null) {
            parentExp = loadCodeExpression(parentExpNode);
            if (parentExp == null)
                return null; // parent expression loading error
        }

        CodeExpression[] parameters = parametersNode != null ?
                                        loadParameters(parametersNode) :
                                        CodeStructure.EMPTY_PARAMS;
        if (parameters == null)
            return null; // error loading parameters

        CodeStatement statement = null;

        String metaObjectType = getAttribute(metaObjectNode,
                                              ATTR_META_OBJECT_TYPE);
        childNodes = metaObjectNode.getChildNodes();
        if (metaObjectType != null && childNodes != null) {
            for (int i=0, n=childNodes.getLength(); i < n; i++) {
                org.w3c.dom.Node childNode = childNodes.item(i);

                String nodeName = childNode.getNodeName();
                if (!metaObjectType.equals(nodeName))
                    continue;

                if (XML_CODE_METHOD.equals(nodeName)) {
                    Method m = loadMethod(childNode);
                    if (m == null)
                        return null; // method loading error

                    statement = CodeStructure.createStatement(
                                                parentExp, m, parameters);
                    break;
                }

                if (XML_CODE_FIELD.equals(nodeName)) {
                    Field f = loadField(childNode);
                    if (f == null)
                        return null; // field loading error

                    if (parameters.length != 1)
                        return null; // inconsistent data error

                    statement = CodeStructure.createStatement(
                                                  parentExp, f, parameters[0]);
                    break;
                }

                if (XML_CODE_EXPRESSION.equals(nodeName)) {
                    // variable assignment
                    CodeExpression exp = loadCodeExpression(childNode);
                    if (exp != parentExp)
                        return null; // inconsistent data error

                    CodeVariable var = exp.getVariable();
                    if (var == null)
                        return null; // non-existing variable error

                    statement = var.getAssignment(exp);
                    break;
                }
            }
        }

        return statement;
    }

    private CodeExpression[] loadParameters(org.w3c.dom.Node node) {
        List<CodeExpression> paramList = new ArrayList<CodeExpression>();
        org.w3c.dom.NodeList childNodes = node.getChildNodes();
        if (childNodes != null) {
            for (int i=0, n=childNodes.getLength(); i < n; i++) {
                org.w3c.dom.Node childNode = childNodes.item(i);

                if (XML_CODE_EXPRESSION.equals(childNode.getNodeName())) {
                    CodeExpression exp = loadCodeExpression(childNode);
                    if (exp == null)
                        return null; // parameter loading error

                    paramList.add(exp);
                }
            }

            CodeExpression[] params = new CodeExpression[paramList.size()];
            paramList.toArray(params);
            return params;
        }
        else return CodeStructure.EMPTY_PARAMS;
    }

    private /*static */Method loadMethod(org.w3c.dom.Node node) {
        org.w3c.dom.NamedNodeMap attr = node.getAttributes();
        if (attr == null)
            return null; // no attributes error

        node = attr.getNamedItem(ATTR_MEMBER_NAME);
        if (node == null)
            return null; // missing method name error
        String name = node.getNodeValue();

        node = attr.getNamedItem(ATTR_MEMBER_CLASS);
        if (node == null)
            return null; // missing method class error

        Class methodClass;
        try {
            methodClass = getClassFromString(node.getNodeValue());
        }
        catch (ClassNotFoundException ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            return null; // method class loading error
        }

        node = attr.getNamedItem(ATTR_MEMBER_PARAMS);
        if (node == null)
            return null; // missing method parameter types error

        Class[] paramTypes;
        StringTokenizer paramTokens =
            new StringTokenizer(node.getNodeValue(), ", "); // NOI18N
        List<Class> typeList = new ArrayList<Class>();
        try {
            while (paramTokens.hasMoreTokens()) {
                typeList.add(getClassFromString(
                                 paramTokens.nextToken()));
            }
            paramTypes = new Class[typeList.size()];
            typeList.toArray(paramTypes);
        }
        catch (ClassNotFoundException ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            return null; // parameters classes loading error
        }

        try {
            return methodClass.getMethod(name, paramTypes);
        }
        catch (NoSuchMethodException ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            return null; // method not found error
        }
    }

    private /*static */Field loadField(org.w3c.dom.Node node) {
        org.w3c.dom.NamedNodeMap attr = node.getAttributes();
        if (attr == null)
            return null; // no attributes error

        node = attr.getNamedItem(ATTR_MEMBER_NAME);
        if (node == null)
            return null; // missing field name error
        String name = node.getNodeValue();

        node = attr.getNamedItem(ATTR_MEMBER_CLASS);
        if (node == null)
            return null; // missing field class error

        Class fieldClass;
        try {
            fieldClass = getClassFromString(node.getNodeValue());
        }
        catch (ClassNotFoundException ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            return null; // field class loading error
        }

        try {
            return fieldClass.getField(name);
        }
        catch (NoSuchFieldException ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            return null; // field not found error
        }
    }

    // -------

    private CodeStructure getCodeStructure() {
        return formModel.getCodeStructure();
    }

    // -------

    private Map<Object/*String or CodeExpression*/,Object/*String or CodeExpression*/> getExpressionsMap() {
        if (expressions == null)
            expressions = new HashMap<Object,Object>(100);
        return expressions;
    }

    private Set<CodeVariable> getVariableSet() {
        if (savedVariables == null)
            savedVariables = new HashSet<CodeVariable>(50);
        return savedVariables;
    }

    private Map<String,RADComponent> getComponentsMap() {
        if (loadedComponents == null)
            loadedComponents = new HashMap<String,RADComponent>(50);
        return loadedComponents;
    }

    // -----------------
    // Value encoding and decoding methods

    private Object getPropertyEditorOrValue(org.w3c.dom.Node node) {
        org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
        if (attrs == null)
            return NO_VALUE; // no attributes, ignore property

        org.w3c.dom.Node typeNode = attrs.getNamedItem(ATTR_PROPERTY_TYPE);
        org.w3c.dom.Node editorNode = attrs.getNamedItem(ATTR_PROPERTY_EDITOR);
        org.w3c.dom.Node valueNode = attrs.getNamedItem(ATTR_PROPERTY_VALUE);
        org.w3c.dom.Node resourceNode = attrs.getNamedItem(ATTR_PROPERTY_RES_KEY);

        // get the type of stored property value
        if (typeNode == null) {
            PersistenceException ex = new PersistenceException(
                                        "Missing property type"); // NOI18N
            String msg = createLoadingErrorMessage(
                FormUtils.getBundleString("MSG_ERR_MissingPropertyType"), // NOI18N
                node);
            annotateException(ex, ErrorManager.ERROR, msg);
            nonfatalErrors.add(ex);
            return NO_VALUE;
        }

        Class propertyType = null;
        Throwable t = null;
        try {
            propertyType = getClassFromString(typeNode.getNodeValue());
        }
        catch (Exception ex) {
            t = ex;
        }
        catch (LinkageError ex) {
            t = ex;
        }
        if (t != null) {
            String msg = createLoadingErrorMessage(
                FormUtils.getFormattedBundleString("FMT_ERR_CannotLoadClass2", // NOI18N
                                     new Object[] { typeNode.getNodeValue() }),
                node);
            annotateException(t, msg);
            nonfatalErrors.add(t);
            return NO_VALUE;
        }

        // load the property editor class and create an instance of it
        PropertyEditor prEd = null;
        if (editorNode != null) {
            Class editorClass = null;
            try {
                editorClass = PersistenceObjectRegistry.loadClass(
                                         editorNode.getNodeValue(), formFile);
            }
            catch (Exception ex) {
                t = ex;
            }
            catch (LinkageError ex) {
                t = ex;
            }
            if (t != null) {
                String msg = createLoadingErrorMessage(
                    FormUtils.getFormattedBundleString(
                        "FMT_ERR_CannotLoadClass3", // NOI18N
                        new Object[] { editorNode.getNodeValue() }),
                    node);
                annotateException(t, msg);
                nonfatalErrors.add(t);
                return NO_VALUE;
            }

            try {
                prEd = createPropertyEditor(editorClass, propertyType, null);
            }
            catch (Exception | LinkageError ex) {
                t = ex;
            }
            if (t != null) {
                String msg = createLoadingErrorMessage(
                    FormUtils.getFormattedBundleString(
                        "FMT_ERR_CannotCreateInstance2", // NOI18N
                        new Object[] { editorNode.getNodeValue() }),
                    node);
                annotateException(t, msg);
                nonfatalErrors.add(t);
                return NO_VALUE;
            }
        }

        // load the property value
        Object value = NO_VALUE;
        if (valueNode != null) { // it is a primitive value
            try {
                value = decodePrimitiveValue(valueNode.getNodeValue(),
                                             propertyType);
                if (prEd != null)
                    prEd.setValue(value);
            }
            catch (IllegalArgumentException ex) {
                String msg = createLoadingErrorMessage(
                    FormUtils.getFormattedBundleString(
                        "FMT_ERR_CannotDecodePrimitive", // NOI18N
                        new Object[] { valueNode.getNodeValue(),
                                       propertyType.getName() }),
                    node);
                annotateException(ex, msg);
                nonfatalErrors.add(ex);
                return NO_VALUE;
            }
        } else if (resourceNode != null) {
            value = ResourceSupport.findResource(formModel, resourceNode.getNodeValue(), propertyType);
            return value != null ? value : NO_VALUE;
        }
        else { // the value is serialized or saved by XMLPropertyEditor
            org.w3c.dom.NodeList children = node.getChildNodes();
            int n = children != null ? children.getLength() : 0;
            if (n > 0) {
                try {
                    boolean serialized = false;
                    // first try if the value is serialized
                    for (int i=0; i < n; i++) {
                        if (XML_SERIALIZED_PROPERTY_VALUE.equals(
                                    children.item(i).getNodeName()))
                        {   // here is the value serialized in XML
                            String serValue = getAttribute(children.item(i),
                                                        ATTR_PROPERTY_VALUE);
                            if (serValue != null) {
                                serialized = true;
                                value = decodeValue(serValue);
                                prEd = null;
                            }
                            break;
                        }
                    }

                    if (!serialized) {
                        if (prEd instanceof XMLPropertyEditor) {
                            // the value is saved by XMLPropertyEditor
                            for (int i=0; i < n; i++) {
                                if (children.item(i).getNodeType()
                                    == org.w3c.dom.Node.ELEMENT_NODE)
                                {   // here is the element of stored value
                                    ((XMLPropertyEditor)prEd).readFromXML(
                                                          children.item(i));
                                    value = prEd.getValue();
                                    break;
                                }
                            }
                        }
                    }
                }
                catch (Exception ex) {
                    t = ex;
                }
                catch (LinkageError ex) {
                    t = ex;
                }
                if (t != null) {
                    String msg = createLoadingErrorMessage(
                        FormUtils.getBundleString(
                            "MSG_ERR_CannotReadPropertyValue"), // NOI18N
                        node);
                    annotateException(t, msg);
                    nonfatalErrors.add(t);
                    return NO_VALUE;
                }
            }

            if (value == NO_VALUE) { // the value is missing
                PersistenceException ex = new PersistenceException(
                                           "Missing property value"); // NOI18N
                String msg = createLoadingErrorMessage(
                    FormUtils.getBundleString("MSG_ERR_MissingPropertyValue"), // NOI18N
                    node);
                annotateException(ex, ErrorManager.ERROR, msg);
                nonfatalErrors.add(ex);
                return NO_VALUE;
            }
        }

        if (prEd != null)
            return prEd;

        return value;
    }

    private /*static */Class getClassFromString(String type)
        throws ClassNotFoundException
    {
        if ("int".equals(type)) // NOI18N   
            return Integer.TYPE;
        else if ("short".equals(type)) // NOI18N
            return Short.TYPE;
        else if ("byte".equals(type)) // NOI18N
            return Byte.TYPE;
        else if ("long".equals(type)) // NOI18N
            return Long.TYPE;
        else if ("float".equals(type)) // NOI18N
            return Float.TYPE;
        else if ("double".equals(type)) // NOI18N
            return Double.TYPE;
        else if ("boolean".equals(type)) // NOI18N
            return Boolean.TYPE;
        else if ("char".equals(type)) // NOI18N
            return Character.TYPE;
        else {
            if (type.startsWith("[")) { // NOI18N
                // load array element class first to avoid failure
                for (int i=1, n=type.length(); i < n; i++) {
                    char c = type.charAt(i);
                    if (c == 'L' && type.endsWith(";")) { // NOI18N
                        String clsName = type.substring(i+1, n-1);
                        PersistenceObjectRegistry.loadClass(clsName, formFile);
                        break;
                    }
                    else if (c != '[')
                        break;
                }
            }

            return PersistenceObjectRegistry.loadClass(type, formFile);
        }
    }

    /** Decodes a primitive value of given type from the specified String.
     * @return decoded value
     * @exception IllegalArgumentException thrown if specified object is not
     *            of supported type
     */
    private Object decodePrimitiveValue(String encoded, Class type) {
        if (Class.class.isAssignableFrom(type) && !"null".equals(encoded)) {
            Throwable t;
            try {
                return PersistenceObjectRegistry.loadClass(encoded, formFile);
            } catch (Exception ex) {
                t = ex;
            } catch (LinkageError ex) {
                t = ex;
            }
            IllegalArgumentException ex = new IllegalArgumentException(
                                          "Cannot load class: "+encoded); // NOI18N
            annotateException(ex, t);
            throw ex;
        }
        return FormUtils.decodePrimitiveValue(encoded, type);
    }

    /** Encodes specified value into a String. Supported types are: <UL>
     * <LI> Class
     * <LI> String
     * <LI> Integer, Short, Byte, Long, Float, Double, Boolean, Character </UL>
     * 
     * @param value value to encode.
     * @return String containing encoded value or null if specified object is not of supported type
     */
    public static String encodePrimitiveValue(Object value) {
        if (value instanceof Integer || value instanceof Short
                || value instanceof Byte || value instanceof Long
                || value instanceof Float || value instanceof Double
                || value instanceof Boolean || value instanceof Character)
            return value.toString();

        if (value instanceof String) {
            try {
                XMLUtil.toAttributeValue((String)value);
                return (String) value;
            } catch (CharConversionException ex) {
                // can't be stored in XML document, needs to be encoded as bytes
                return null;
            }
        }

        if (value instanceof Class)
            return ((Class)value).getName();

        if (value == null)
            return "null"; // NOI18N

        return null; // is not a primitive type
    }

    /** Decodes a value from String containing textual representation of
     * serialized stream.
     * 
     * @param strValue value to decode.
     * @return decoded object
     * @exception IOException thrown if an error occurres during deserializing
     *            the object
     * @throws java.lang.ClassNotFoundException if the corresponding class cannot be loaded.
     */
    public Object decodeValue(String strValue)
        throws IOException, ClassNotFoundException
    {
        if (strValue == null || strValue.length() == 0)
            return null;

        char[] bisChars = strValue.toCharArray();
        byte[] bytes = new byte[bisChars.length];
        StringBuffer singleNum = new StringBuffer();
        int count = 0;
        for (int i = 0; i < bisChars.length; i++) {
            if (',' == bisChars[i]) {
                bytes[count++] = Byte.parseByte(singleNum.toString());
                singleNum = new StringBuffer();
            } else {
                singleNum.append(bisChars[i]);
            }
        }

        // add the last byte
        bytes[count++] = Byte.parseByte(singleNum.toString());
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, count);
        return new OIS(bis).readObject();
    }

    /** Encodes specified value to a String containing textual representation of serialized stream.
     * 
     * @param value value to encode.
     * @return String containing textual representation of the serialized object
     * @throws java.io.IOException when some problem occurs during encoding.
     */
    public static String encodeValue(Object value) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject(value);
        oos.close();

        byte[] bosBytes = bos.toByteArray();
        StringBuilder buf = new StringBuilder(bosBytes.length*4);
        for (int i=0; i < bosBytes.length; i++) {
            if (i+1 < bosBytes.length)
                buf.append(bosBytes[i]).append(","); // NOI18N
            else
                buf.append("").append(bosBytes[i]); // NOI18N
        }
        return buf.toString();
    }

    // ObjectInputStream subclass for reading serialized property values
    private class OIS extends ObjectInputStream {
        public OIS(InputStream is) throws IOException {
            super(is);
        }

        @Override
        protected Class resolveClass(ObjectStreamClass streamCls)
            throws IOException, ClassNotFoundException
        {
            String name = streamCls.getName();
            if (name.startsWith("[")) { // NOI18N
                // load array element class first to avoid failure
                for (int i=1, n=name.length(); i < n; i++) {
                    char c = name.charAt(i);
                    if (c == 'L' && name.endsWith(";")) { // NOI18N
                        String clsName = name.substring(i+1, n-1);
                        PersistenceObjectRegistry.loadClass(clsName, formFile);
                        break;
                    }
                    else if (c != '[')
                        return super.resolveClass(streamCls);
                }
            }
            return PersistenceObjectRegistry.loadClass(name, formFile);
        }
    }

    // --------------------------------------------------------------------------------------
    // Utility formatting methods

    private static void addElementOpen(StringBuffer buf, String elementName) {
        buf.append("<"); // NOI18N
        buf.append(elementName);
        buf.append(">\n"); // NOI18N
    }

    private static void addElementOpenAttr(StringBuffer buf,
                                           String elementName,
                                           String[] attrNames,
                                           String[] attrValues)
    {
        buf.append("<"); // NOI18N
        buf.append(elementName);
        for (int i = 0; i < attrNames.length; i++) {
            if (attrValues[i] == null) continue;
            buf.append(" "); // NOI18N
            buf.append(attrNames[i]);
            buf.append("=\""); // NOI18N
            buf.append(encodeToProperXML(attrValues[i]));
            buf.append("\""); // NOI18N
        }
        buf.append(">\n"); // NOI18N
    }

    private static void addLeafElementOpenAttr(StringBuffer buf,
                                               String elementName,
                                               String[] attrNames,
                                               String[] attrValues)
    {
        buf.append("<"); // NOI18N
        buf.append(elementName);
        for (int i = 0; i < attrNames.length; i++) {
            if (attrValues[i] == null) continue;
            buf.append(" "); // NOI18N
            buf.append(attrNames[i]);
            buf.append("=\""); // NOI18N
            buf.append(encodeToProperXML(attrValues[i]));
            buf.append("\""); // NOI18N
        }
        buf.append("/>\n"); // NOI18N
    }

    private static void addElementClose(StringBuffer buf, String elementName) {
        buf.append("</"); // NOI18N
        buf.append(elementName);
        buf.append(">\n"); // NOI18N
    }

    private void saveNodeIntoText(StringBuffer buf, org.w3c.dom.Node valueNode, String indent) {
        buf.append(indent);
        buf.append("<"); // NOI18N
        buf.append(valueNode.getNodeName());

        org.w3c.dom.NamedNodeMap attributes = valueNode.getAttributes();

        if (attributes != null) {
            List<org.w3c.dom.Node> attribList = new ArrayList<org.w3c.dom.Node>(attributes.getLength());
            for (int i = 0; i < attributes.getLength(); i++) {
                attribList.add(attributes.item(i));
            }

            // sort the attributes by attribute name
            // probably not necessary, but there is no guarantee that
            // the order of attributes will remain the same in DOM
            attribList.sort(new Comparator<org.w3c.dom.Node>() {
                @Override
                public int compare(org.w3c.dom.Node n1, org.w3c.dom.Node n2) {
                    return n1.getNodeName().compareTo(n2.getNodeName());
                }
            }
                             );

            for (Iterator it = attribList.iterator(); it.hasNext();) {
                org.w3c.dom.Node attrNode =(org.w3c.dom.Node)it.next();
                String attrName = attrNode.getNodeName();
                String attrValue = attrNode.getNodeValue();

                buf.append(" "); // NOI18N
                buf.append(encodeToProperXML(attrName));
                buf.append("=\""); // NOI18N
                buf.append(encodeToProperXML(attrValue));
                buf.append("\""); // NOI18N
            }
        }
        // [PENDING - CNODES, TEXT NODES, ...]

        org.w3c.dom.NodeList children = valueNode.getChildNodes();
        if ((children == null) ||(children.getLength() == 0)) {
            buf.append("/>\n"); // NOI18N
        } else {
            buf.append(">\n"); // NOI18N
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
                saveNodeIntoText(buf, children.item(i), indent + ONE_INDENT);
            }
            buf.append(indent);
            buf.append("</"); // NOI18N
            buf.append(encodeToProperXML(valueNode.getNodeName()));
            buf.append(">\n"); // NOI18N
        }
    }

    // --------------------------------------------------------------------------------------
    // Utility DOM access methods

    private static String encodeToProperXML(String text) {
        if (text == null)
            return ""; // NOI18N

        StringBuilder sb = new StringBuilder(text.length());
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0x0020 && c <= 0x007f) {
                switch (c) {
                    case '&': sb.append("&amp;"); break; // NOI18N
                    case '<': sb.append("&lt;"); break; // NOI18N
                    case '>': sb.append("&gt;"); break; // NOI18N
                    case '\'': sb.append("&apos;"); break; // NOI18N
                    case '\"': sb.append("&quot;"); break; // NOI18N
                    default: sb.append(c); break;
                }
            } else {
                sb.append("&#x").append(Integer.toHexString(c)).append(";"); // NOI18N
            }
        }

        return sb.toString();
    }

    /** Finds first subnode of given node with specified name.
     * @param node the node whose subnode we are looking for
     * @param name requested name of the subnode
     * @return the found subnode or null if no such subnode exists
     */
    private org.w3c.dom.Node findSubNode(org.w3c.dom.Node node, String name) {
        org.w3c.dom.NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
                if (name.equals(children.item(i).getNodeName())) {
                    return children.item(i);
                }
            }
        }
        return null;
    }

    /** Finds all subnodes of given node with specified name.
     * @param node the node whose subnode we are looking for
     * @param name requested name of the subnode
     * @return array of the found subnodes
     */
    private org.w3c.dom.Node[] findSubNodes(org.w3c.dom.Node node,
                                            String name)
    {
        org.w3c.dom.NodeList children = node.getChildNodes();
        if (children == null)
            return new org.w3c.dom.Node[0];

        List<org.w3c.dom.Node> nodeList = new ArrayList<org.w3c.dom.Node>();

        for (int i=0,n=children.getLength(); i < n; i++) {
            org.w3c.dom.Node subnode = children.item(i);
            if (subnode.getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                continue; // ignore text nodes
            if (name.equals(subnode.getNodeName()))
                nodeList.add(subnode);
        }

        org.w3c.dom.Node[] nodes = new org.w3c.dom.Node[nodeList.size()];
        nodeList.toArray(nodes);
        return nodes;
    }

    /** Utility method to obtain given attribute value from specified Node.
     * @return attribute name or null if the attribute is not present
     */
    private static String getAttribute(org.w3c.dom.Node node, String attrName) {
        org.w3c.dom.Node valueNode = node.getAttributes().getNamedItem(attrName);
        return valueNode != null ? valueNode.getNodeValue() : null;
    }

    // --------------

    private String createLoadingErrorMessage(String errMsg,
                                             org.w3c.dom.Node node)
    {
        String nodeName = node.getNodeName();

        List<String> path = new ArrayList<String>();
        boolean leaf = true;
        boolean layout = false;
        boolean layoutConstr = false;
        boolean inOthers = false;

        do {
            String name = node.getNodeName();
            if (XML_COMPONENT.equals(name)
                || XML_CONTAINER.equals(name)
                || XML_MENU_COMPONENT.equals(name)
                || XML_MENU_CONTAINER.equals(name)
                || XML_PROPERTY.equals(name)
                || XML_SYNTHETIC_PROPERTY.equals(name)
                || XML_AUX_VALUE.equals(name))
            {
                name = getAttribute(node, "name"); // NOI18N
                if (name != null || !leaf)
                    path.add(name);
                if (name != null)
                    leaf = false;
            }
            else if (XML_NON_VISUAL_COMPONENTS.equals(name)) {
                inOthers = true;
            }
            else if (XML_LAYOUT.equals(name) || XML_LAYOUT_CODE.equals(name)) {
                path.add(FormUtils.getBundleString("CTL_PathLayout")); // NOI18N
                layout = true;
            }
            else if (XML_CONSTRAINTS.equals(name)) {
                path.add(FormUtils.getBundleString("CTL_PathLayoutConstraints")); // NOI18N
                layoutConstr = true;
            }

            node = node.getParentNode();
        }
        while (node != null);

        if (inOthers)
            path.add(FormUtils.getBundleString("CTL_NonVisualComponents")); // NOI18N
        else if (formModel.getFormBaseClass() != null)
            path.add(FormUtils.getFormattedBundleString(
                       "FMT_UnnamedComponentNodeName", // NOI18N
                       new Object[] { Utilities.getShortClassName(
                                        formModel.getFormBaseClass()) }));

        if (path.isEmpty())
            return errMsg;

        String link = null;
        StringBuilder pathBuf = new StringBuilder();
        for (int i=path.size()-1; i >= 0; i--) {
            pathBuf.append(path.get(i));
            if (i > 0) {
                if (link == null)
                    link = FormUtils.getBundleString("CTL_PathLink"); // NOI18N
                pathBuf.append(link);
            }
        }

        if (errMsg == null)
            return pathBuf.toString();

        boolean property = XML_PROPERTY.equals(nodeName)
                           || XML_SYNTHETIC_PROPERTY.equals(nodeName)
                           || XML_AUX_VALUE.equals(nodeName);

        String format;
        if (!layoutConstr)
            if (!layout)
                format = property ? "FMT_ERR_LoadingComponentProperty" : // NOI18N
                                    "FMT_ERR_LoadingComponent"; // NOI18N
            else
                format = property ? "FMT_ERR_LoadingLayoutProperty" : // NOI18N
                                    "FMT_ERR_LoadingLayout"; // NOI18N
        else
            format = property ? "FMT_ERR_LoadingLayoutConstraintsProperty" : // NOI18N
                                "FMT_ERR_LoadingLayoutConstraints"; // NOI18N

        StringBuilder buf = new StringBuilder();
        buf.append(FormUtils.getFormattedBundleString(
                        format, new Object[] { pathBuf.toString() }));
        buf.append("\n"); // NOI18N
        buf.append(errMsg);

        return buf.toString();
    }

    // --------------

//    private void raiseFormatVersion(String ver) {
//        if (ver != formatVersion
//            && (formatVersion == NB32_VERSION
//                || (formatVersion == NB33_VERSION && ver == NB34_VERSION)
//                || ((formatVersion == NB33_VERSION || formatVersion == NB34_VERSION) && ver == NB42_VERSION)
//                || ver == NB60_VERSION))
//            formatVersion = ver;
//    }

    private static boolean isSupportedFormatVersion(String ver) {
        return NB32_VERSION.equals(ver)
               || NB33_VERSION.equals(ver)
               || NB34_VERSION.equals(ver)
               || NB50_VERSION.equals(ver)
               || NB60_PRE_VERSION.equals(ver)
               || NB60_VERSION.equals(ver)
               || NB61_VERSION.equals(ver)
               || NB65_VERSION.equals(ver)
               || NB71_VERSION.equals(ver)
               || NB74_VERSION.equals(ver);
    }

    private static FormModel.FormVersion formVersionForVersionString(String version) {
        if (version != null) {
            if (NB32_VERSION.equals(version) || NB33_VERSION.equals(version) || NB34_VERSION.equals(version)) {
                return FormModel.FormVersion.BASIC;
            }
            if (NB50_VERSION.equals(version)) {
                return FormModel.FormVersion.NB50;
            }
            if (NB60_PRE_VERSION.equals(version)) {
                return FormModel.FormVersion.NB60_PRE;
            }
            if (NB60_VERSION.equals(version)) {
                return FormModel.FormVersion.NB60;
            }
            if (NB61_VERSION.equals(version)) {
                return FormModel.FormVersion.NB61;
            }
            if (NB65_VERSION.equals(version)) {
                return FormModel.FormVersion.NB65;
            }
            if (NB71_VERSION.equals(version)) {
                return FormModel.FormVersion.NB71;
            }
            if (NB74_VERSION.equals(version)) {
                return FormModel.FormVersion.NB74;
            }
        }
        return null;
    }

    private static String versionStringForFormVersion(FormModel.FormVersion version) {
        if (version != null) {
            switch (version) {
                case BASIC: return NB34_VERSION;
                case NB50: return NB50_VERSION;
                case NB60_PRE: return NB60_PRE_VERSION;
                case NB60: return NB60_VERSION;
                case NB61: return NB61_VERSION;
                case NB65: return NB65_VERSION;
                case NB71: return NB71_VERSION;
                case NB74: return NB74_VERSION;
            }
        }
        return null;
    }

    // --------------
    // NB 3.2 compatibility - dealing with FormInfo

    /** In NB 3.2, the declared superclass in java source was not used, so
     * it could be changed incompatibly to the FormInfo type (typically to
     * some unrelated non-visual class, moving the generated code to an
     * innerclass). We try to detect this and use the FormInfo type
     * preferentially in such case.
     */
    private static Class checkDeclaredSuperclass(Class declaredSuperclass,
                                                 String formInfoName)
    {
        if (!java.awt.Component.class.isAssignableFrom(declaredSuperclass)
            && formInfoName != null)
        {
            Class formInfoType = getCompatibleFormClass(formInfoName);
            if (formInfoType != null)
                return formInfoType;
        }
        return declaredSuperclass;
    }

    /**
     * @return class corresponding to given FormInfo class name
     */
    private static Class getCompatibleFormClass(String formInfoName) {
        if (formInfoName == null)
            return null; // no FormInfo name found in form file

        return getClassForKnownFormInfo(formInfoName);
        // ignore unknown FormInfo - it's deep past...
    }

    private static Class getCompatibleFormClass(Class formBaseClass) {
        return getClassForKnownFormInfo(getFormInfoForKnownClass(formBaseClass));
    }

    private Class getFormDesignClass(String declaredSuperclassName) {
        for (ComponentConverter c : FormUtils.getClassConverters()) {
            Class convClass = c.getDesignClass(declaredSuperclassName, formFile);
            if (convClass != null) {
                return convClass;
            }
        }
        return null;
    }

    private Class getFormDesignClass(Class declaredSuperclass) {
        for (ComponentConverter c : FormUtils.getClassConverters()) {
            Class convClass = c.getDesignClass(declaredSuperclass, formFile);
            if (convClass != null) {
                return convClass;
            }
        }
        return null;
    }

    private class ConnectedProperties {        
        private class ConnectedProperty {
            private final Property property;            
            private final String beanName;   
            private final RADConnectionPropertyEditor.RADConnectionDesignValue value;
            private final String tostring;    
            private final Object auxiliaryValue;
            private String valueName = null;
            private ConnectedProperty(Property property,                                                             
                              RADConnectionPropertyEditor.RADConnectionDesignValue value,
                              String beanName,                                                            
                              Object auxiliaryValue) 
            {
                this.property = property;
                this.beanName = beanName;
                this.value = value;                                                     
                this.tostring = getKey(beanName, property.getName());
                this.auxiliaryValue = auxiliaryValue;
            }            
            @Override
            public boolean equals(Object obj) {
                if(!(obj instanceof ConnectedProperty)) return false;
                if(this == obj) return true;
                ConnectedProperty cp = (ConnectedProperty) obj;
                return beanName.equals(cp.beanName) && 
                       property.equals(cp.property);
            }
            @Override
            public int hashCode() {            
                return tostring.hashCode();
            }            
            RADConnectionPropertyEditor.RADConnectionDesignValue getValue() {
                return value;
            }               
            private String getValueName() {
                if(valueName == null) {
                    if(value.getType() == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_PROPERTY) {
                        valueName = value.getProperty().getName();
                    } else if (value.getType() == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_METHOD) {
                        RADComponent component = value.getRADComponent();
                        String methodName = value.getMethod().getName();
                        PropertyDescriptor[] descs = component.getBeanInfo().getPropertyDescriptors();
                        for (int i = 0; i < descs.length; i++) {
                            Method method = descs[i].getReadMethod();
                            if(method!=null && method.getName().equals(methodName)) {
                                methodName = descs[i].getName();
                                break;
                            }
                        }
                        valueName = methodName;
                    }                                       
                }     
                return valueName;
            }
            Object getAuxiliaryValue() {
                return auxiliaryValue;
            }
            String getKey() {                
                return getKey(beanName, property.getName());
            }                        
            String getSourceKey() {
                RADComponent comp = value.getRADComponent();
                String key = null;
                if (comp!=null) { // Issue 180640
                    key = getKey(comp.getName(), getValueName());
                }
                return key;
            }                
            private String getKey(String beanName, String propertyName) {
                StringBuilder sb = new StringBuilder();
                sb.append("["); // NOI18N
                sb.append(beanName);
                sb.append(", "); // NOI18N
                sb.append(propertyName);
                sb.append("]"); // NOI18N
                return sb.toString();
            }            
            @Override
            public String toString() {
                return tostring;
            }       
        }                
        private Map<String,ConnectedProperty> properties = new HashMap<String,ConnectedProperty>();      
        public void put(Property property, 
                        RADConnectionPropertyEditor.RADConnectionDesignValue value,
                        String beanName,
                        Object auxiliaryValue)                        
        {
            ConnectedProperty cp = new ConnectedProperty(property, value, beanName, auxiliaryValue);
            properties.put(cp.getKey(), cp);
        }     
        private ConnectedProperty get(String key) {
            return properties.get(key);
        }           
        public void setValues() { 
            Collection sorted = sort();
            for (Iterator it = sorted.iterator(); it.hasNext();) {                
                ConnectedProperty compProperty = (ConnectedProperty) it.next();                
                try {                    
                    compProperty.property.setValue(compProperty.getValue()); 
                } catch(Exception ex) {
                    org.w3c.dom.Node node = 
                        (org.w3c.dom.Node) compProperty.getAuxiliaryValue();                    
                    createLoadingErrorMessage(ex, node);
                }
            }
        }
        private Collection<ConnectedProperty> sort() {                                           
            List<ConnectedProperty> sortedValues = null;
            try {
                sortedValues = Utilities.topologicalSort(properties.values(), getEdges());                                                            
            } catch (TopologicalSortException tse) {
                Set[] sets = tse.topologicalSets();                
                sortedValues = new ArrayList<ConnectedProperty>();
                for (int i = 0; i < sets.length; i++) {
                    for (Iterator it = sets[i].iterator(); it.hasNext();) {
                        sortedValues.add((ConnectedProperty)it.next());
                    }
                }    
            }
            if(sortedValues!=null) {
                Collections.reverse(sortedValues);             
                return sortedValues;
            }
            // something went wrong, let's fall back
            // on the unsorted values
            return properties.values();
        } 
        private Map<ConnectedProperty,List<ConnectedProperty>> getEdges() {
            Map<ConnectedProperty,List<ConnectedProperty>> edges = new HashMap<ConnectedProperty,List<ConnectedProperty>>();
            for (Iterator it = properties.values().iterator(); it.hasNext();) {
                ConnectedProperty target = (ConnectedProperty) it.next();                                
                ConnectedProperty source = get(target.getSourceKey());
                if(source!=null) {
                    List<ConnectedProperty> l = new ArrayList<ConnectedProperty>();
                    l.add(source);
                    edges.put(target, l);
                } 
            }            
            return edges;
        }                
    }           

    // FormInfo names used in NB 3.2
    private static final String[] defaultFormInfoNames = {
        "JFrameFormInfo", // NOI18N
        "JPanelFormInfo", // NOI18N
        "JDialogFormInfo", // NOI18N
        "JInternalFrameFormInfo", // NOI18N
        "JAppletFormInfo", // NOI18N
        "FrameFormInfo", // NOI18N
        "AppletFormInfo", // NOI18N
        "DialogFormInfo", // NOI18N
        "PanelFormInfo" }; // NOI18N

    private static Class getClassForKnownFormInfo(String infoName) {
        if (infoName == null)
            return null;
        int i = infoName.lastIndexOf('.');
        String shortName = infoName.substring(i+1);

        if (defaultFormInfoNames[0].equals(shortName))
            return javax.swing.JFrame.class;
        else if (defaultFormInfoNames[1].equals(shortName))
            return javax.swing.JPanel.class;
        else if (defaultFormInfoNames[2].equals(shortName))
            return javax.swing.JDialog.class;
        else if (defaultFormInfoNames[3].equals(shortName))
            return javax.swing.JInternalFrame.class;
        else if (defaultFormInfoNames[4].equals(shortName))
            return javax.swing.JApplet.class;
        else if (defaultFormInfoNames[5].equals(shortName))
            return java.awt.Frame.class;
        else if (defaultFormInfoNames[6].equals(shortName))
            return java.applet.Applet.class;
        else if (defaultFormInfoNames[7].equals(shortName))
            return java.awt.Dialog.class;
        else if (defaultFormInfoNames[8].equals(shortName))
            return java.awt.Panel.class;

        return null;
    }

    private static String getFormInfoForKnownClass(Class formType) {
        String shortName;

        if (javax.swing.JFrame.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[0];
        else if (javax.swing.JPanel.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[1];
        else if (javax.swing.JDialog.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[2];
        else if (javax.swing.JInternalFrame.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[3];
        else if (javax.swing.JApplet.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[4];
        else if (java.awt.Frame.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[5];
        else if (java.applet.Applet.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[6];
        else if (java.awt.Dialog.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[7];
        else if (java.awt.Panel.class.isAssignableFrom(formType))
            shortName = defaultFormInfoNames[8];
        else if (org.openide.windows.TopComponent.class.getName().equals(formType.getName()))
            shortName = defaultFormInfoNames[1]; // fallback TopComponent to JPanel (issue 79695)
        else return null;

        return "org.netbeans.modules.form.forminfo." + shortName; // NOI18N
    }
    
    // --------
    // NB 3.1 compatibility - layout persistence conversion tables

    private static final int LAYOUT_BORDER = 0;
    private static final int LAYOUT_FLOW = 1;
    private static final int LAYOUT_BOX = 2;
    private static final int LAYOUT_GRIDBAG = 3;
    private static final int LAYOUT_GRID = 4;
    private static final int LAYOUT_CARD = 5;
    private static final int LAYOUT_ABSOLUTE = 6;
    private static final int LAYOUT_NULL = 7;
    private static final int LAYOUT_JSCROLL = 8;
    private static final int LAYOUT_SCROLL = 9;
    private static final int LAYOUT_JSPLIT = 10;
    private static final int LAYOUT_JTAB = 11;
    private static final int LAYOUT_JLAYER = 12;
    private static final int LAYOUT_TOOLBAR = 13;

    private static final int LAYOUT_UNKNOWN = -1;
    private static final int LAYOUT_FROM_CODE = -2;
    private static final int LAYOUT_NATURAL = -3;

    private static final String[] layout31Names = {
        "org.netbeans.modules.form.compat2.layouts.DesignBorderLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignFlowLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignBoxLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignCardLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JScrollPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.ScrollPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JSplitPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JTabbedPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JLayeredPaneSupportLayout", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignBoxLayout" // NOI18N
    }; // fixed table, do not change!

    private static final String[] layout31ConstraintsNames = {
        "org.netbeans.modules.form.compat2.layouts.DesignBorderLayout$BorderConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignFlowLayout$FlowConstraintsDescription", // NOI18N,
        "org.netbeans.modules.form.compat2.layouts.DesignBoxLayout$BoxConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridBagLayout$GridBagConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignGridLayout$GridConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignCardLayout$CardConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout$AbsoluteConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignAbsoluteLayout$AbsoluteConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JScrollPaneSupportLayout$JScrollPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.ScrollPaneSupportLayout$ScrollPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JSplitPaneSupportLayout$JSplitPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JTabbedPaneSupportLayout$JTabbedPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.support.JLayeredPaneSupportLayout$JLayeredPaneConstraintsDescription", // NOI18N
        "org.netbeans.modules.form.compat2.layouts.DesignBoxLayout$BoxConstraintsDescription" // NOI18N
    }; // fixed table, do not change!

    private static final boolean[] reasonable31Constraints = {
        true, false, false, true, false, true, true,
        true, false, false, true, true, true, false
    }; // fixed table, do not change!

    private static final String[] supportedClassNames = {
        "java.awt.BorderLayout", // NOI18N
        "java.awt.FlowLayout", // NOI18N
        "javax.swing.BoxLayout", // NOI18N
        "java.awt.GridBagLayout", // NOI18N
        "java.awt.GridLayout", // NOI18N
        "java.awt.CardLayout", // NOI18N
        "org.netbeans.lib.awtextra.AbsoluteLayout", // NOI18N
        null,
        "javax.swing.JScrollPane", // NOI18N
        "java.awt.ScrollPane", // NOI18N
        "javax.swing.JSplitPane", // NOI18N
        "javax.swing.JTabbedPane", // NOI18N
        "javax.swing.JLayeredPane", // NOI18N
        "javax.swing.JToolBar" // NOI18N
    }; // fixed table, do not change!

    private static final String[][] layout31PropertyNames = {
        { "horizontalGap", "verticalGap" }, // BorderLayout // NOI18N
        { "alignment", "horizontalGap", "verticalGap" }, // FlowLayout // NOI18N
        { "axis" }, // BoxLayout // NOI18N
        { }, // GridBagLayout
        { "rows", "columns", "horizontalGap", "verticalGap" }, // GridLayout // NOI18N
        { "horizontalGap", "verticalGap" }, // CardLayout (ignoring "currentCard") // NOI18N
        { "useNullLayout" }, // AbsoluteLayout // NOI18N
        { "useNullLayout" }, // AbsoluteLayout // NOI18N
        { }, // JScrollPane
        { }, // ScrollPane
        { }, // JSplitPane
        { }, // JTabbedPane
        { }, // JLayeredPane
        { "axis" } // BoxLayout // NOI18N
    }; // fixed table, do not change!

    private static final String[][] layoutDelegatePropertyNames = {
        { "hgap", "vgap" }, // BorderLayout // NOI18N
        { "alignment", "hgap", "vgap" }, // FlowLayout // NOI18N
        { "axis" }, // BoxLayout // NOI18N
        { }, // GridBagLayout
        { "rows", "columns", "hgap", "vgap" }, // GridLayout // NOI18N
        { "hgap", "vgap" }, // CardLayout (ignoring "currentCard") // NOI18N
        { null }, // AbsoluteLayout
        { null }, // null layout
        { }, // JScrollPane
        { }, // ScrollPane
        { }, // JSplitPane
        { }, // JTabbedPane
        { }, // JLayeredPane
        { null } // JToolBar
    }; // fixed table, do not change!

    // methods and constructors for creating code structure
    private static Method setLayoutMethod;
    private static Method simpleAddMethod;
    private static Method addWithConstrMethod;
    private static Method addTabMethod1;
    private static Method addTabMethod2;
    private static Method addTabMethod3;
    private static Method setLeftComponentMethod;
    private static Method setRightComponentMethod;
    private static Method setTopComponentMethod;
    private static Method setBottomComponentMethod;
    private static Method setBoundsMethod;
    private static Method setViewportViewMethod;
    private static Constructor gridBagConstrConstructor;
    private static Constructor insetsConstructor;
    private static Constructor absoluteConstraintsConstructor;

    // Special static field holding last loaded layout index. This is hack for
    // dealing with multiple constraints types saved by NB 3.1 - we load only
    // constraints matching with the current layout. At time of loading
    // constraints, the layout is already loaded but the layout support is not
    // established yet, so the loadConstraints method cannot find out what the
    // current layout of container is.
    private static int layoutConvIndex = LAYOUT_UNKNOWN;
}
