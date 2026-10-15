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

import java.awt.EventQueue;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.form.RADProperty.FakePropertyDescriptor;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

import org.netbeans.modules.form.codestructure.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author Ian Formanek
 */

public class RADComponent {
    /**
     * Name of key of an aux-value holding additional information
     * about the component class (like "RigidArea" for {@code Box.Filler}s).
     */
    public static final String AUX_VALUE_CLASS_DETAILS = "classDetails"; // NOI18N

    // -----------------------------------------------------------------------------
    // Static variables

    public static final String PROP_NAME = "variableName"; // NOI18N

    static final NewType[] NO_NEW_TYPES = {};
    static final RADProperty[] NO_PROPERTIES = {};
    static final BindingProperty[] NO_BINDINGS = {};

    // -----------------------------------------------------------------------------
    // Private variables

    private static int idCounter;

    private String id = Integer.toString(++idCounter);

    private Class<? extends Object> beanClass;
    private Object beanInstance;
    private BeanInfo beanInfo;
    private BeanInfo fakeBeanInfo;
    private String missingClassName;

    protected Node.PropertySet[] propertySets;
    private Node.Property[] syntheticProperties;
    private RADProperty[] beanProperties1;
    private RADProperty[] beanProperties2;
    private BindingProperty[][] bindingProperties;
    private EventProperty[] eventProperties;
    private Map<Object,RADProperty[]> otherProperties;
    private List actionProperties;
    private MetaAccessibleContext accessibilityData;
    private FormProperty[] accessibilityProperties;

    private RADProperty[] knownBeanProperties;
    private Event[] knownEvents; // must be grouped by EventSetDescriptor

    private PropertyChangeListener propertyListener;

    private Map<String,Object> auxValues;
    protected Map<String,Node.Property> nameToProperty;

    private RADComponent parentComponent;

    private FormModel formModel;
    private boolean inModel;

    private RADComponentNode componentNode;

    private CodeExpression componentCodeExpression;

    private String storedName; // component name preserved e.g. for remove undo

    private boolean valid = true;
    // -----------------------------------------------------------------------------
    // Constructors & Initialization

    /**
     * Called to initialize the component with specified FormModel.
     * 
     * @param formModel the FormModel of the form into which this component
     * will be added
     * @return <code>true</code> if the model was initialized,
     * <code>false</code> otherwise.
     */
    public boolean initialize(FormModel formModel) {
        if (this.formModel == null) {
            this.formModel = formModel;

            // properties and events will be created on first request
            clearProperties();

//            if (beanClass != null)
//                createCodeExpression();

            return true;
        }
        else if (this.formModel != formModel)
            throw new IllegalStateException(
                "Cannot initialize metacomponent with another form model"); // NOI18N
        return false;
    }

    public void setParentComponent(RADComponent parentComp) {
        parentComponent = parentComp;
    }

    /** Initializes the bean instance represented by this meta component.
     * A default instance is created for the given bean class.
     * The meta component is fully initialized after this method returns.
     * 
     * @param beanClass the bean class to be represented by this meta component
     * @return initialized instance.
     * @throws java.lang.Exception when the instance cannot be initialized.
     */
    public Object initInstance(Class<? extends Object> beanClass) throws Exception {
        if (beanClass == null)
            throw new NullPointerException();

        if (this.beanClass != beanClass && this.beanClass != null) {
            beanInfo = null;
            fakeBeanInfo = null;
            clearProperties();
        }

        this.beanClass = beanClass;

        Object bean = createBeanInstance();
        getBeanInfo(); // force BeanInfo creation here - will be needed, may fail
        setBeanInstance(bean);

        return beanInstance;
    }

    /** Sets the bean instance represented by this meta component.
     * The meta component is fully initialized after this method returns.
     * @param beanInstance the bean to be represented by this meta component
     */
    public void setInstance(Object beanInstance) {
        if (this.beanClass != beanInstance.getClass()){
            beanInfo = null;                
            fakeBeanInfo = null;
        }
            
        clearProperties();

        this.beanClass = beanInstance.getClass();

        getBeanInfo(); // force BeanInfo creation here - will be needed, may fail
        setBeanInstance(beanInstance);

// commented out: since we don't hold default instances separately, we can't
// reinstate the bean properties against the global default property values
//        getAllBeanProperties();
//        for (int i=0; i < knownBeanProperties.length; i++) {
//            try {
//                knownBeanProperties[i].reinstateProperty();
//            }
//            catch (Exception ex) {
//                ErrorManager.getDefault()
//                    .notify(ErrorManager.INFORMATIONAL, ex);
//            }
//        }
    }

    /** Updates the bean instance - e.g. when setting a property requires
     * to create new instance of the bean.
     * 
     * @param beanInstance bean instance.
     */
    public void updateInstance(Object beanInstance) {
        if (this.beanInstance != null && this.beanClass == beanInstance.getClass())
            setBeanInstance(beanInstance);
            // should properties also be reinstated?
            // formModel.fireFormChanged() ?
        else
            setInstance(beanInstance);
    }

    /**
     * Called to create the instance of the bean. This method is called if the
     * initInstance method is used; using the setInstance method, the bean
     * instance is set directly.
     * 
     * @return the instance of the bean that will be used during design time
     * @throws java.lang.Exception when the instance cannot be created.
     */
    protected Object createBeanInstance() throws Exception {
        return CreationFactory.createDefaultInstance(beanClass);
    }

    /** Sets directly the bean instance. Can be overriden.
     * 
     * @param beanInstance bean instance.
     */
    protected void setBeanInstance(Object beanInstance) {
        if (beanClass == null) { // bean class not set yet
            beanClass = beanInstance.getClass();
//            createCodeExpression();
        }
        this.beanInstance = beanInstance;
    }

    void setInModel(boolean in) {
        if (inModel != in) {
            inModel = in;
            if (in) {
                createCodeExpression();
                formModel.updateMapping(this, true);
            } else {
                formModel.updateMapping(this, false);
                releaseCodeExpression();
                setNodeReference(null);
            }
        }
    }

    void setNodeReference(RADComponentNode node) {
        this.componentNode = node;
    }

    protected void createCodeExpression() {
        // create expression object
        if (componentCodeExpression == null) {
            CodeStructure codeStructure = formModel.getCodeStructure();
            componentCodeExpression = codeStructure.createExpression(
                                   FormCodeSupport.createOrigin(this));
            codeStructure.registerExpression(componentCodeExpression);
        }
        // make sure a variable is attached
        if (formModel.getTopRADComponent() != this && componentCodeExpression.getVariable() == null) {
            formModel.getCodeStructure().createVariableForExpression(
                                           componentCodeExpression,
                                           0x30DF, // default type
                                           (String)getAuxValue("JavaCodeGenerator_TypeParameters"), //NOI18N
                                           storedName);
        }
    }

/*    final void removeCodeExpression() {
        if (componentCodeExpression != null) {
            CodeVariable var = componentCodeExpression.getVariable();
            if (var != null)
                storedName = var.getName();
            CodeStructure.removeExpression(componentCodeExpression);
        }
    } */

    final void releaseCodeExpression() {
        if (componentCodeExpression != null) {
            CodeVariable var = componentCodeExpression.getVariable();
            if (var != null) {
                storedName = var.getName();
                formModel.getCodeStructure()
                    .removeExpressionFromVariable(componentCodeExpression);
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Public interface

    private boolean testMode = Boolean.getBoolean("netbeans.form.layout_test"); // NOI18N

    public final String getId() {
        return testMode ? getName() : id;
    }

    public final boolean isReadOnly() {
        return formModel.isReadOnly(); //readOnly;
    }

    /** Provides access to the Class of the bean represented by this RADComponent
     * @return the Class of the bean represented by this RADComponent
     */
    public final Class<? extends Object> getBeanClass() {
        return beanClass;
    }

    public final String getMissingClassName() {
        return missingClassName;
    }

    public final void setMissingClassName(String className) {
        missingClassName = className;
    }
    
    /** Provides access to the real instance of the bean represented by this RADComponent
     * @return the instance of the bean represented by this RADComponent
     */
    public final Object getBeanInstance() {
        return beanInstance;
    }

    public final RADComponent getParentComponent() {
        return parentComponent;
    }

    public final boolean isParentComponent(RADComponent comp) {
        if (comp == null)
            return false;

        do {
            comp = comp.getParentComponent();
            if (comp == this)
                return true;
        }
        while (comp != null);

        return false;
    }

    Object createDefaultDeserializedInstance() throws Exception {
        FileObject formFile = FormEditor.getFormDataObject(getFormModel()).getFormFile();
        String serFile = (String)getAuxValue(JavaCodeGenerator.AUX_SERIALIZE_TO);
        if (serFile == null) {
            serFile = formFile.getName() + "_" + getName(); // NOI18N
        }

        ClassPath sourcePath = ClassPath.getClassPath(formFile, ClassPath.SOURCE);
        String serName = sourcePath.getResourceName(formFile.getParent());
        if (!"".equals(serName)) { // NOI18N
            serName += "."; // NOI18N
        }
        serName += serFile;

        Object instance = null;
        try {
            instance = Beans.instantiate(sourcePath.getClassLoader(true), serName);
        } catch (ClassNotFoundException cnfe) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, cnfe);
            ClassPath executionPath = ClassPath.getClassPath(formFile, ClassPath.EXECUTE);
            try {
                instance = Beans.instantiate(executionPath.getClassLoader(true), serName);
            } catch (ClassNotFoundException cnfex) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, cnfex);
                instance = createBeanInstance();
            }
        }
        return instance;
    }

    public Object cloneBeanInstance(Collection<RADProperty> relativeProperties) {
        Object clone = null;
        try {
            if (JavaCodeGenerator.VALUE_SERIALIZE.equals(getAuxValue(JavaCodeGenerator.AUX_CODE_GENERATION))) {
                clone = createDefaultDeserializedInstance();
            }
        } catch (Exception ex) {
            // The serialization support is usually useless and not working well in many situations.
            // If set on, its mostly by mistake. In case of failure let's fall back to a default
            // instance creation so the cloning does not fail (or does not return null as it used to,
            // which was not good for the replicated design view).
            Logger.getLogger(RADComponent.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }

        if (clone == null && beanClass.getName().startsWith("org.jdesktop.swingx.")) { // NOI18N
            // Hack for bug 127881 - SwingX components may fail to instantiate if project
            // classloader has changed and so UIDefaults for the previous classloader were
            // removed. A possible workaround is to load the class again with the new classloader.
            // A drawback is we return an instance of a different class, increasing a risk
            // of ClassCastException (but with changing classloaders it is generally
            // unavoidable anyway, we'd have to reload the whole form to be quite correct).
            try {
                Class newBeanClass = FormUtils.loadClass(beanClass.getName(), formModel);
                if (newBeanClass != beanClass) {
                    clone = CreationFactory.createDefaultInstance(newBeanClass);
                }
            } catch (Exception ex) {
                Logger.getLogger(RADComponent.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
            } catch (LinkageError ex) {
                Logger.getLogger(RADComponent.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
            }
        }

        if (clone == null) {
            try {
                clone = createBeanInstance();
            } catch (Exception ex) { // this should not fail, we already have an instance of this kind
                Logger.getLogger(RADComponent.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        if (clone != null) {
            FormUtils.copyPropertiesToBean(getKnownBeanProperties(),
                                           clone,
                                           relativeProperties);
        }
        return clone;
    }

    /** Provides access to BeanInfo of the bean represented by this RADComponent
     * @return the BeanInfo of the bean represented by this RADComponent
     */
    public BeanInfo getBeanInfo() {
        if (beanInfo == null) {
            try {
                beanInfo = createBeanInfo(beanClass);        
            } catch (Error err) { // Issue 74002
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, err);
                beanInfo = new FakeBeanInfo();
            } catch (IntrospectionException ex) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                beanInfo = new FakeBeanInfo();
            }
        }
        if(isValid()) {            
            return beanInfo;
        } else {
            if (fakeBeanInfo == null) {
		fakeBeanInfo  = new FakeBeanInfo();
	    }            
            return fakeBeanInfo ;            
        }        
    }            

    protected BeanInfo createBeanInfo(Class cls) throws IntrospectionException {
        return FormUtils.getBeanInfo(cls);        
    }

    /** This method can be used to check whether the bean represented by this
     * RADComponent has hidden-state.
     * @return true if the component has hidden state, false otherwise
     */
    public boolean hasHiddenState() {
        String name = beanClass.getName();
        if (name.startsWith("javax.") // NOI18N
              || name.startsWith("java.") // NOI18N
              || name.startsWith("org.openide.")) // NOI18N
            return false;

        return getBeanInfo().getBeanDescriptor()
                                .getValue("hidden-state") != null; // NOI18N
    }

    public CodeExpression getCodeExpression() {
        return componentCodeExpression;
    }

    /** Getter for the name of the metacomponent - it maps to variable name
     * declared for the instance of the component in the generated java code.
     * It is a unique identification of the component within a form, but it may
     * change (currently editable as "Variable Name" in code gen. properties).
     * @return current value of the Name property
     */
    public String getName() {
        if (componentCodeExpression != null) {
            CodeVariable var = componentCodeExpression.getVariable();
            if (var != null)
                return var.getName();
        }
        return storedName;
    }

    /** Setter for the name of the component - it is the name of the
     * component's node and the name of the variable declared for the component
     * in the generated code.
     * 
     * @param name new name of the component
     */
    public void setName(String name) {
        if (!needsVariableRename(name)) {
            return;
        }

        String oldName = componentCodeExpression.getVariable().getName();

        formModel.getCodeStructure().renameVariable(oldName, name);

        resourceComponentRename(oldName, name);

        renameDefaultEventHandlers(oldName, name);
        // [possibility of renaming default event handlers should be probably
        // configurable in options]

        formModel.fireSyntheticPropertyChanged(this, PROP_NAME,
                                               oldName, name);

        if (getNodeReference() != null)
            getNodeReference().updateName();
    }
    
    /**
     * Method used to push setName through refactoring instead of just setting the variable name and
     * not changing it in users custom code.
     * @param name the new name for the variable
     */
    public void rename(String name) {
        if (!needsVariableRename(name)) {
            return;
        }
        CodeGenerator codeGenerator = FormEditor.getCodeGenerator(formModel);
        if (codeGenerator == null) { // bug 248067 - form might get reloaded on background when the Rename dialog is open
            return;
        }

        if (!org.openide.util.Utilities.isJavaIdentifier(name)) {
            IllegalArgumentException iae =
                new IllegalArgumentException("Invalid component name"); // NOI18N
            ErrorManager.getDefault().annotate(
                iae, ErrorManager.USER, null, 
                FormUtils.getBundleString("ERR_INVALID_COMPONENT_NAME"), // NOI18N
                null, null);
            throw iae;
        }

        codeGenerator.regenerateCode(); // Issue 201053
        if (formModel.getCodeStructure().isVariableNameReserved(name)) {
            IllegalArgumentException iae =
                new IllegalArgumentException("Component name already in use: "+name); // NOI18N
            ErrorManager.getDefault().annotate(
                iae, ErrorManager.USER, null,
                FormUtils.getBundleString("ERR_COMPONENT_NAME_ALREADY_IN_USE"), // NOI18N
                null, null);
            throw iae;
        }

        try {
            RenameSupport.renameComponent(this, name);
        } finally { // hack for robustness - if refactoring fails for whatever reason
            if (!getName().equals(name)) {
                setName(name);
            }
        }
    }

    private boolean needsVariableRename(String name) {
        if (componentCodeExpression != null) {
            CodeVariable var = componentCodeExpression.getVariable();
            return var != null && name != null && !name.equals(var.getName());
        }
        return false;
    }

    public void setStoredName(String name) {
        storedName = name;
    }

    private void renameDefaultEventHandlers(String oldComponentName,
                                            String newComponentName)
    {
        boolean renamed = false; // whether any handler was renamed
        FormEvents formEvents = null;

        Event[] events = getKnownEvents();
        for (int i=0; i < events.length; i++) {
            String[] handlers = events[i].getEventHandlers();
            for (int j=0; j < handlers.length; j++) {
                String handlerName = handlers[j];
                int idx = handlerName.indexOf(oldComponentName);
                if (idx >= 0) {
                    if (formEvents == null)
                         formEvents = getFormModel().getFormEvents();
                    String newHandlerName = formEvents.findFreeHandlerName(
                        handlerName.substring(0, idx)
                        + newComponentName
                        + handlerName.substring(idx + oldComponentName.length())
                    );
                    formEvents.renameEventHandler(handlerName, newHandlerName);
                    EventProperty prop = (EventProperty)events[i].getComponent().getPropertyByName(events[i].getId());
                    if (prop != null) {
                        prop.resetSelectedEventHandler(handlerName);
                    }
                    renamed = true;
                }
            }
        }

        if (renamed && getNodeReference() != null) {
            getNodeReference().fireComponentPropertiesChange();
        }
    }

    /** Allows to add an auxiliary <name, value> pair, which is persistent
     * in Gandalf. The current value can be obtained using
     * getAuxValue(aux_property_name) method. To remove aux value for specified
     * property name, use setAuxValue(name, null).
     * @param key name of the aux property
     * @param value new value of the aux property or null to remove it
     */
    public void setAuxValue(String key, Object value) {
        if (value == null) {
            if (auxValues != null) {
                auxValues.remove(key);
            }
        } else {
            if (auxValues == null) {
                auxValues = new TreeMap<String,Object>();
            }
            auxValues.put(key, value);
        }
    }

    /** Allows to obtain an auxiliary value for specified aux property name.
     * @param key name of the aux property
     * @return null if the aux value for specified name is not set
     */
    public Object getAuxValue(String key) {
        return auxValues != null ? auxValues.get(key) : null;
    }

    /** Provides access to the FormModel class which manages the form in which
     * this component has been added.
     * @return the FormModel which manages the form into which this component
     *         has been added
     */
    public final FormModel getFormModel() {
        return formModel;
    }

    public final boolean isInModel() {
        return inModel;
    }

    /** @return the map of all component's aux value-pairs of <String, Object>
     */
    public Map<String,Object> getAuxValues() {
        return auxValues;
    }

    /** Support for new types that can be created in this node.
     * @return array of new type operations that are allowed
     */
    public NewType[] getNewTypes() {
        return NO_NEW_TYPES;
    }

    public synchronized Node.PropertySet[] getProperties() {
        if (propertySets == null) {
            List<Node.PropertySet> propSets = new ArrayList<Node.PropertySet>(5);
            createPropertySets(propSets);
            propertySets = new Node.PropertySet[propSets.size()];
            propSets.toArray(propertySets);
        }
        return propertySets;
    }

    public synchronized RADProperty[] getAllBeanProperties() {
        if (knownBeanProperties == null) {
            createBeanProperties();
        }

        return knownBeanProperties;
    }

    public synchronized RADProperty[] getKnownBeanProperties() {
        return knownBeanProperties != null ? knownBeanProperties : NO_PROPERTIES;
    }

    public Iterator<RADProperty> getBeanPropertiesIterator(FormProperty.Filter filter,
                                              boolean fromAll)
    {
        return new PropertyIterator<RADProperty>(
                   fromAll ? getAllBeanProperties() : getKnownBeanProperties(),
                   filter);
    }

    public BindingProperty[] getAllBindingProperties() {
        BindingProperty[][] bprop = getBindingProperties();
        BindingProperty[] prop = new BindingProperty[bprop[0].length + bprop[1].length + bprop[2].length];
        System.arraycopy(bprop[0], 0, prop, 0, bprop[0].length);
        System.arraycopy(bprop[1], 0, prop, bprop[0].length, bprop[1].length);
        System.arraycopy(bprop[2], 0, prop, bprop[0].length+bprop[1].length, bprop[2].length);
        return prop;
    }
    
    public synchronized BindingProperty[][] getBindingProperties() {
        if (bindingProperties == null) {
            createBindingProperties();
        }
        return bindingProperties;
    }

    public final BindingProperty getBindingProperty(String name) {
        for (BindingProperty prop : getAllBindingProperties()) {
            if (prop.getName().equals(name))
                return prop;
        }
        return null;
    }

    public synchronized BindingProperty[] getKnownBindingProperties() {
        return bindingProperties != null ? getAllBindingProperties() : NO_BINDINGS;
    }

    synchronized boolean hasBindings() {
        if (bindingProperties != null) {
            for (BindingProperty p : getAllBindingProperties()) {
                if (p.getValue() != null)
                    return true;
            }
        }
        return false;
    }

    /** Provides access to the Node which represents this RADComponent
     * @return the RADComponentNode which represents this RADComponent
     */
    public RADComponentNode getNodeReference() {
        return componentNode;
    }

    // -----------------------------------------------------------------------------
    // Access to component Properties

    synchronized Node.Property[] getSyntheticProperties() {
        if (syntheticProperties == null)
            syntheticProperties = createSyntheticProperties();
        return syntheticProperties;
    }

    synchronized RADProperty[] getBeanProperties1() {
        if (beanProperties1 == null)
            createBeanProperties();
        return beanProperties1;
    }

    synchronized RADProperty[] getBeanProperties2() {
        if (beanProperties2 == null)
            createBeanProperties();
        return beanProperties2;
    }

    synchronized EventProperty[] getEventProperties() {
        if (eventProperties == null)
            createEventProperties();
        return eventProperties;
    }
    
    synchronized List getActionProperties() {
        if (actionProperties == null) {
            createBeanProperties();
        }
        return actionProperties;
    }

    protected synchronized <T> T getPropertyByName(String name, Class<? extends T> propertyType, boolean fromAll) {
        Node.Property prop = nameToProperty.get(name);
        if (prop == null && fromAll) {
            if (beanProperties1 == null && !name.startsWith("$")) // NOI18N
                createBeanProperties();
            if (eventProperties == null && name.startsWith("$")) // NOI18N
                createEventProperties();
            if (accessibilityProperties == null)
                createAccessibilityProperties();

            prop = nameToProperty.get(name);
        }
        return prop != null
                 && (propertyType == null
                     || propertyType.isAssignableFrom(prop.getClass())) ?
               (T)prop : null;
    }

    /**
     * Returns property of given name corresponding to a property or event.
     * Forces creation of all property objects.
     * 
     * @param name name of the property.
     * @return bean or event property
     */
    public Node.Property getPropertyByName(String name) {
        return getPropertyByName(name, null, true);
    }

    public final RADProperty getBeanProperty(String name) {
        return getPropertyByName(name, RADProperty.class, true);
    }

    public final Node.Property getSyntheticProperty(String name) {
        for (Node.Property prop : getSyntheticProperties()) {
            if (prop.getName().equals(name))
                return prop;
        }
        return null;
    }

    public RADProperty[] getFakeBeanProperties(String[] propNames, Class[] propertyTypes) {
        FakeBeanInfo fbi = (FakeBeanInfo) getBeanInfo();        
        fbi.removePropertyDescriptors();
        for (int i = 0; i < propNames.length; i++) {
            fbi.addPropertyDescriptor(propNames[i], propertyTypes[i]);
        }
        return getBeanProperties(propNames);
    }   

    /**
     * Returns bean properties of given names. Creates the properties if not
     * created yet, but does not force creation of all bean properties.
     * 
     * @param propNames property names.
     * @return array of properties corresponding to the names; may contain
     *         null if there is no property of given name
     */
    public synchronized RADProperty[] getBeanProperties(String[] propNames) {
        RADProperty[] properties = new RADProperty[propNames.length];        
        PropertyDescriptor[] descriptors = null;
        
        boolean empty = knownBeanProperties == null;
        int validCount = 0;
        List<RADProperty> newProps = null;
        Object[] propAccessClsf = null;
        Object[] propParentChildDepClsf = null;

        int descIndex = 0;
        for (int i=0; i < propNames.length; i++) {
            String name = propNames[i];
            if (name == null)
                continue;

            RADProperty prop;
            if (!empty) {
                Object obj = nameToProperty.get(name);
                prop = obj instanceof RADProperty ? (RADProperty) obj : null;
            }
            else prop = null;

            if (prop == null) {
                if (descriptors == null) {
                    descriptors = getBeanInfo().getPropertyDescriptors();
                    if (descriptors.length == 0)
                        break;
                }
                int j = descIndex;
                do {
                    if (descriptors[j].getName().equals(name)) {
                        if (propAccessClsf == null) {
                            propAccessClsf = FormUtils.getPropertiesAccessClsf(beanClass);
                            propParentChildDepClsf = FormUtils.getPropertiesParentChildDepsClsf(beanClass);
                        }

                        prop = createBeanProperty(descriptors[j], propAccessClsf, propParentChildDepClsf);

                        if (!empty) {
                            if (newProps == null)
                                newProps = new ArrayList<RADProperty>();
                            newProps.add(prop);
                        }
                        descIndex = j + 1;
                        if (descIndex == descriptors.length
                                         && i+1 < propNames.length)
                            descIndex = 0; // go again from beginning
                        break;
                    }
                    j++;
                    if (j == descriptors.length)
                        j = 0;
                }
                while (j != descIndex);
            }
            if (prop != null) {
                properties[i] = prop;
                validCount++;
            }
            else { // force all properties creation, there might be more
                   // properties than from BeanInfo [currently just ButtonGroup]
                properties[i] = getPropertyByName(name, RADProperty.class, true);
                empty = false;
                newProps = null;
            }
        }

        if (empty) {
            if (validCount == properties.length)
                knownBeanProperties = properties;
            else if (validCount > 0) {
                knownBeanProperties = new RADProperty[validCount];
                for (int i=0,j=0; i < properties.length; i++)
                    if (properties[i] != null)
                        knownBeanProperties[j++] = properties[i];
            }
        }
        else if (newProps != null) { // just for consistency, should not occur
            RADProperty[] knownProps =
                new RADProperty[knownBeanProperties.length + newProps.size()];
            System.arraycopy(knownBeanProperties, 0,
                             knownProps, 0,
                             knownBeanProperties.length);
            for (int i=0; i < newProps.size(); i++)
                knownProps[i + knownBeanProperties.length] = newProps.get(i);

            knownBeanProperties = knownProps;
        }

        return properties;
    }

    public synchronized Event getEvent(String name) {
        Object prop = nameToProperty.get(name);
        if (prop == null && eventProperties == null) {
            createEventProperties();
            prop = nameToProperty.get(name);
        }
        return prop instanceof EventProperty ?
               ((EventProperty)prop).getEvent() : null;
    }

    public synchronized Event[] getEvents(String[] eventNames) {
        Event[] events = new Event[eventNames.length];
        EventSetDescriptor[] eventSets = null;

        boolean empty = knownEvents == null;
        int validCount = 0;
        List<Event> newEvents = null;

        int setIndex = 0;
        int methodIndex = 0;

        for (int i=0; i < eventNames.length; i++) {
            String name = eventNames[i];
            if (name == null)
                continue;

            boolean fullName = name.startsWith("$"); // NOI18N

            Event event;
            if (!empty) {
                Object obj = nameToProperty.get(name);
                event = obj instanceof EventProperty ?
                        ((EventProperty)obj).getEvent() : null;
            }
            else event = null;

            if (event == null) {
                if (eventSets == null) {
                    eventSets = getBeanInfo().getEventSetDescriptors();
                    if (eventSets.length == 0)
                        break;
                }
                int j = setIndex;
                do {
                    Method[] methods = eventSets[j].getListenerMethods();
                    if (methods.length > 0) {
                        int k = methodIndex;
                        do {
                            String eventFullId =
                                FormEvents.getEventIdName(methods[k]);
                            String eventId = fullName ?
                                eventFullId : methods[k].getName();
                            if (eventId.equals(name)) {
                                event = createEventProperty(eventFullId,
                                                            eventSets[j],
                                                            methods[k])
                                                .getEvent();
                                if (!empty) {
                                    if (newEvents == null)
                                        newEvents = new ArrayList<Event>();
                                    newEvents.add(event);
                                }
                                methodIndex = k + 1;
                                break;
                            }
                            k++;
                            if (k == methods.length)
                                k = 0;
                        }
                        while (k != methodIndex);
                    }

                    if (event != null) {
                        if (methodIndex == methods.length) {
                            methodIndex = 0;
                            setIndex = j + 1; // will continue in new set
                            if (setIndex == eventSets.length
                                            && i+1 < eventNames.length)
                                setIndex = 0; // go again from beginning
                        }
                        else setIndex = j; // will continue in the same set
                        break;
                    }

                    j++;
                    if (j == eventSets.length)
                        j = 0;
                    methodIndex = 0;
                }
                while (j != setIndex);
            }
            if (event != null) {
                events[i] = event;
                validCount++;
            }
        }

        if (empty) {
            if (validCount == events.length)
                knownEvents = events;
            else if (validCount > 0) {
                knownEvents = new Event[validCount];
                for (int i=0,j=0; i < events.length; i++)
                    if (events[i] != null)
                        knownEvents[j++] = events[i];
            }
        }
        else if (newEvents != null) { // just for consistency, should not occur
            Event[] known = new Event[knownEvents.length + newEvents.size()];
            System.arraycopy(knownEvents, 0, known, 0, knownEvents.length);
            for (int i=0; i < newEvents.size(); i++)
                known[i + knownEvents.length] = newEvents.get(i);

            knownEvents = known;
        }

        return events;
    }

    /** @return all events of the component grouped by EventSetDescriptor
     */
    public synchronized Event[] getAllEvents() {
        if (knownEvents == null || eventProperties == null) {
            if (eventProperties == null)
                createEventProperties();
            else {
                knownEvents = new Event[eventProperties.length];
                for (int i=0; i < eventProperties.length; i++)
                    knownEvents[i] = eventProperties[i].getEvent();
            }
        }

        return knownEvents;
    }

    // Note: events must be grouped by EventSetDescriptor
    public synchronized Event[] getKnownEvents() {
        return knownEvents != null ? knownEvents : FormEvents.NO_EVENTS;
    }

    // ---------
    // events

    Event getDefaultEvent() {
        int eventIndex = getBeanInfo().getDefaultEventIndex();
        if (eventIndex < getEventProperties().length && eventIndex >= 0) {
            return eventProperties[eventIndex].getEvent();
        } else {
            for (int i=0; i < eventProperties.length; i++) {
                Event e = eventProperties[i].getEvent();
                if ("actionPerformed".equals(e.getListenerMethod().getName()) // NOI18N
                        && !(getBeanInstance() instanceof javax.swing.JMenu)) {
                    return e;
                }
            }
        }
        return null;
    }

    void attachDefaultEvent() {
        Event event = getDefaultEvent();
        if (event != null) {
            getFormModel().getFormEvents().attachEvent(event, null, null);
        }
    }

    // -----------------------------------------------------------------------------
    // Properties

    protected synchronized void clearProperties() {
        if (nameToProperty != null)
            nameToProperty.clear();
        else nameToProperty = new HashMap<String,Node.Property>();

        propertySets = null;
        syntheticProperties = null;
        beanProperties1 = null;
        beanProperties2 = null;
        knownBeanProperties = null;
        eventProperties = null;
        knownEvents = null;
        accessibilityData = null;
        accessibilityProperties = null;
    }

    static final boolean SUPPRESS_PROPERTY_TABS = Boolean.getBoolean(
            "nb.form.suppressTabs"); // NOI18N

    /**
     * Attribute for specifying an alternative display name of the property set.
     */
    private static final String ALTERNATE_PS_NAME = "PS_AlternateDisplayName"; // NOI18N

    protected synchronized void createPropertySets(List<Node.PropertySet> propSets) {
        if (beanProperties1 == null)
            createBeanProperties();

        ResourceBundle bundle = FormUtils.getBundle();

        Node.PropertySet ps = new Node.PropertySet(
                "properties", // NOI18N
                bundle.getString("CTL_PropertiesTab"), // NOI18N
                bundle.getString("CTL_PropertiesTabHint")) // NOI18N
        {
            @Override
            public Node.Property[] getProperties() {
                return getBeanProperties1();
            }
        };
        ps.setValue(ALTERNATE_PS_NAME, bundle.getString("CTL_AlternatePropertiesTab")); // NOI18N
        propSets.add(ps);

        if (SUPPRESS_PROPERTY_TABS) {
            return;
        }

        if(isValid()) {
            Iterator entries = otherProperties.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry)entries.next();
                final String category = (String)entry.getKey();
                ps = new Node.PropertySet(category, category, category) {        
                    @Override
                    public Node.Property[] getProperties() {
                        if (otherProperties == null) {
                            createBeanProperties();
                        }
                        return (Node.Property[])otherProperties.get(category);
                    }
                };
                //ps.setValue("tabName", category); // NOI18N
                propSets.add(ps);
            }
        
            if (beanProperties2.length > 0) {
                ps = new Node.PropertySet(
                        "properties2", // NOI18N
                        bundle.getString("CTL_Properties2Tab"), // NOI18N
                        bundle.getString("CTL_Properties2TabHint")) // NOI18N
                {
                    @Override
                    public Node.Property[] getProperties() {
                        return getBeanProperties2();
                    }
                };
                ps.setValue(ALTERNATE_PS_NAME, bundle.getString("CTL_AlternateProperties2Tab")); // NOI18N
                propSets.add(ps);
            }

            if (getAllBindingProperties().length > 0) {
                BindingProperty[][] bprop = getBindingProperties();
                for (int i=0; i<bprop.length; i++) {
                    final int index = i;
                    ps = new Node.PropertySet(
                            "binding" + i, // NOI18N
                            bundle.getString("CTL_BindingTab" + i), // NOI18N
                            bundle.getString("CTL_BindingTabHint" + i)) // NOI18N
                    {
                        @Override
                        public Node.Property[] getProperties() {
                            return getBindingProperties()[index];
                        }
                    };
                    ps.setValue("tabName", bundle.getString("CTL_BindingTab")); // NOI18N
                    propSets.add(ps);
                }
            }

            ps = new Node.PropertySet(
                    "events", // NOI18N
                    bundle.getString("CTL_EventsTab"), // NOI18N
                    bundle.getString("CTL_EventsTabHint")) // NOI18N
            {
                @Override
                public Node.Property[] getProperties() {
                    return getEventProperties();
                }
            };

            ps.setValue("tabName", bundle.getString("CTL_EventsTab")); // NOI18N
            propSets.add(ps);

        }        
        
        ps = new Node.PropertySet(
                "synthetic", // NOI18N
                bundle.getString("CTL_SyntheticTab"), // NOI18N
                bundle.getString("CTL_SyntheticTabHint")) // NOI18N
        {
            @Override
            public Node.Property[] getProperties() {
                return getSyntheticProperties();
            }
        };
        ps.setValue("tabName", bundle.getString("CTL_SyntheticTab_Short")); // NOI18N
        propSets.add(ps);

        if (accessibilityProperties == null) {
            createAccessibilityProperties();
        }

        if (accessibilityProperties.length > 0) {
            propSets.add(new Node.PropertySet(
                "accessibility", // NOI18N
                FormUtils.getBundleString("CTL_AccessibilityTab"), // NOI18N
                FormUtils.getBundleString("CTL_AccessibilityTabHint")) // NOI18N
            {
                @Override
                public Node.Property[] getProperties() {
                    return getAccessibilityProperties();
                }
            });
        }
    }

    protected Node.Property[] createSyntheticProperties() {
        CodeGenerator codeGen = FormEditor.getCodeGenerator(formModel);
        return codeGen != null ? codeGen.getSyntheticProperties(this) : new Node.Property[0];
    }

    private synchronized void createBeanProperties() {
        List<RADProperty> prefProps = new ArrayList<RADProperty>();
        List<RADProperty> normalProps = new ArrayList<RADProperty>();
        List<RADProperty> expertProps = new ArrayList<RADProperty>();
        Map<Object,List<RADProperty>> otherProps = new TreeMap<Object,List<RADProperty>>();
        List<RADProperty> actionProps = new LinkedList<RADProperty>();

        Object[] propsCats = FormUtils.getPropertiesCategoryClsf(beanClass, getBeanInfo().getBeanDescriptor());
        PropertyDescriptor[] props = getBeanInfo().getPropertyDescriptors();
        if (propsCats != null && Utilities.isMac() && beanClass.getClassLoader() == null) {
            try {
                Object[] newPropsCats = new Object[propsCats.length+2*props.length];
                Map<String,PropertyDescriptor> oldProps = new HashMap<String,PropertyDescriptor>();
                for (int i=0; i<props.length; i++) {
                    PropertyDescriptor pd = props[i];
                    String name = pd.getName();
                    oldProps.put(name, pd);
                    newPropsCats[2*i] = name;
                    Object cat = FormUtils.PROP_NORMAL;
                    if (pd.isPreferred()) {
                        cat = FormUtils.PROP_PREFERRED;
                    }
                    if (pd.isExpert()) {
                        cat = FormUtils.PROP_EXPERT;
                    }
                    if (pd.isHidden()) {
                        cat = FormUtils.PROP_HIDDEN;
                    }
                    newPropsCats[2*i+1] = cat;
                }
                System.arraycopy(propsCats, 0, newPropsCats, 2*props.length, propsCats.length);
                propsCats = newPropsCats;
                props = FormUtils.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO).getPropertyDescriptors();
                for (PropertyDescriptor pd : props) {
                    PropertyDescriptor oldPD = oldProps.get(pd.getName());
                    if (oldPD != null) {
                        Enumeration<String> enumeration = oldPD.attributeNames();
                        while (enumeration.hasMoreElements()) {
                            String attr = enumeration.nextElement();
                            pd.setValue(attr, oldPD.getValue(attr));
                        }
                    }
                }
            } catch (IntrospectionException iex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, iex.getMessage(), iex);
            }
        }

        Object[] propsAccess = FormUtils.getPropertiesAccessClsf(beanClass);
        Object[] propParentChildDepClsf = FormUtils.getPropertiesParentChildDepsClsf(beanClass);

        for (int i = 0; i < props.length; i++) {
            PropertyDescriptor pd = props[i];
            boolean action = (pd.getValue("action") != null); // NOI18N
            Object category = pd.getValue("category"); // NOI18N
            List<RADProperty> listToAdd;
            
            if ((category == null) || (!(category instanceof String))) {
                Object propCat = FormUtils.getPropertyCategory(pd, propsCats);
                if (propCat == FormUtils.PROP_PREFERRED)
                    listToAdd = prefProps;
                else if (propCat == FormUtils.PROP_NORMAL)
                    listToAdd = normalProps;
                else if (propCat == FormUtils.PROP_EXPERT)
                    listToAdd = expertProps;
                else continue; // PROP_HIDDEN

            } else {
                listToAdd = otherProps.get(category);
                if (listToAdd == null) {
                    listToAdd = new ArrayList<RADProperty>();
                    otherProps.put(category, listToAdd);
                }
            }
            
            RADProperty prop = getPropertyByName(pd.getName(), RADProperty.class, false);
            if (prop == null)
                prop = createBeanProperty(pd, propsAccess, propParentChildDepClsf);

            if (prop != null) {
                listToAdd.add(prop);
                if ("action".equals(pd.getName()) && (listToAdd == prefProps) && javax.swing.Action.class.isAssignableFrom(pd.getPropertyType())) { // NOI18N
                    action = true;
                    prop.setValue("actionName", FormUtils.getBundleString("CTL_SetAction")); // NOI18N
                }
                if (action) {
                    Object actionName = pd.getValue("actionName"); // NOI18N
                    if (actionName instanceof String) {
                        prop.setValue("actionName", actionName); // NOI18N
                    }
                    actionProps.add(prop);
                }
            }
        }

        changePropertiesExplicitly(prefProps, normalProps, expertProps);

        int prefCount = prefProps.size();
        int normalCount = normalProps.size();
        int expertCount = expertProps.size();
        int otherCount = 0;

        if (prefCount > 0) {
            beanProperties1 = new RADProperty[prefCount];
            prefProps.toArray(beanProperties1);
            if (normalCount + expertCount > 0) {
                normalProps.addAll(expertProps);
                beanProperties2 = new RADProperty[normalCount + expertCount];
                normalProps.toArray(beanProperties2);
            }
            else beanProperties2 = new RADProperty[0];
        }
        else {
            beanProperties1 = new RADProperty[normalCount];
            normalProps.toArray(beanProperties1);
            if (expertCount > 0) {
                beanProperties2 = new RADProperty[expertCount];
                expertProps.toArray(beanProperties2);
            }
            else beanProperties2 = new RADProperty[0];
        }
        
        Map<Object,RADProperty[]> otherProps2 = new TreeMap<Object,RADProperty[]>();
        RADProperty[] array = new RADProperty[0];
        for (Map.Entry<Object,List<RADProperty>> entry : otherProps.entrySet()) {
            List<RADProperty> catProps = entry.getValue();
            otherCount += catProps.size();
            otherProps2.put(entry.getKey(), catProps.toArray(array));
        }
        otherProperties = otherProps2;
        
        FormUtils.reorderProperties(beanClass, beanProperties1);
        FormUtils.reorderProperties(beanClass, beanProperties2);

        knownBeanProperties = new RADProperty[beanProperties1.length
                              + beanProperties2.length + otherCount];
        System.arraycopy(beanProperties1, 0,
                         knownBeanProperties, 0,
                         beanProperties1.length);
        System.arraycopy(beanProperties2, 0,
                         knownBeanProperties, beanProperties1.length,
                         beanProperties2.length);
        
        int where = beanProperties1.length + beanProperties2.length;
        
        for (Map.Entry<Object,RADProperty[]> entry : otherProperties.entrySet()) {
            RADProperty[] catProps = entry.getValue();
            System.arraycopy(catProps, 0,
                knownBeanProperties, where,
                catProps.length);
            where += catProps.length;
        }

        actionProperties = actionProps;
    }

    private synchronized void createBindingProperties() {
        BindingDesignSupport bindingSupport = FormEditor.getBindingSupport(formModel);
        if (bindingSupport == null) {
            bindingProperties = new BindingProperty[][] {{},{},{}};
        } else {
            Collection<BindingDescriptor>[] props = bindingSupport.getBindingDescriptors(this);
            bindingProperties = new BindingProperty[props.length][];
            for (int i=0; i<props.length; i++) {
                bindingProperties[i] = new BindingProperty[props[i].size()];
                int j = 0;
                for (BindingDescriptor desc : props[i]) {
                    bindingProperties[i][j++] = new BindingProperty(this, desc);
                }
            }
        }
    }

    private synchronized void createEventProperties() {
        EventSetDescriptor[] eventSets = getBeanInfo().getEventSetDescriptors();

        List<EventProperty> eventPropList = new ArrayList<EventProperty>(eventSets.length * 5);

        for (int i=0; i < eventSets.length; i++) {
            EventSetDescriptor desc = eventSets[i];
            Method[] methods = desc.getListenerMethods();
            for (int j=0; j < methods.length; j++) {
                String eventId = FormEvents.getEventIdName(methods[j]);
                Object prop = nameToProperty.get(eventId);
                assert (prop == null) || (prop instanceof EventProperty);
                EventProperty eventProp = (EventProperty)prop;
                if (eventProp == null)
                    eventProp = createEventProperty(eventId, desc, methods[j]);
                eventPropList.add(eventProp);
            }
        }

        EventProperty[] eventProps = new EventProperty[eventPropList.size()];
        eventPropList.toArray(eventProps);

        knownEvents = new Event[eventProps.length];
        for (int i=0; i < eventProps.length; i++)
            knownEvents[i] = eventProps[i].getEvent();

        FormUtils.sortProperties(eventProps);
        eventProperties = eventProps;
    }

    public synchronized FormProperty[] getAccessibilityProperties() {
        if (accessibilityProperties == null)
            createAccessibilityProperties();
        return accessibilityProperties;
    }

    synchronized FormProperty[] getKnownAccessibilityProperties() {
        return accessibilityProperties != null ? accessibilityProperties : NO_PROPERTIES;
    }

    private synchronized void createAccessibilityProperties() {
        final Object comp = getBeanInstance();
        if (comp instanceof Accessible
            && (!EventQueue.isDispatchThread() || ((Accessible)comp).getAccessibleContext() != null)) {
            if (accessibilityData == null)
                accessibilityData = new MetaAccessibleContext();
            accessibilityProperties = accessibilityData.getProperties();

            for (int i=0; i < accessibilityProperties.length; i++) {
                FormProperty prop = accessibilityProperties[i];
                setPropertyListener(prop);
                prop.setPropertyContext(new FormPropertyContext.Component(this));
                nameToProperty.put(prop.getName(), prop);
            }

            if (!EventQueue.isDispatchThread()) { // Issue 187131
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (((Accessible)comp).getAccessibleContext() == null) {
                            // Remove accessible properties
                            for (int i = 0; i < accessibilityProperties.length; i++) {
                                FormProperty prop = accessibilityProperties[i];
                                nameToProperty.remove(prop.getName());
                            }
                            accessibilityData = null;
                            accessibilityProperties = NO_PROPERTIES;
                            if (propertySets != null) {
                                Node.PropertySet[] newPS = new Node.PropertySet[propertySets.length-1];
                                int idx = 0;
                                for (Node.PropertySet ps : propertySets) {
                                    if (!"accessibility".equals(ps.getName())) { // NOI18N
                                        newPS[idx++] = ps;
                                    }
                                }
                                Node.PropertySet[] oldPS = propertySets;
                                propertySets = newPS;
                                getNodeReference().firePropertyChangeHelper(Node.PROP_PROPERTY_SETS, oldPS, newPS);
                            }
                        }
                    }
                });
            }
        }
        else {
            accessibilityData = null;
            accessibilityProperties = NO_PROPERTIES;
        }
    }

    protected RADProperty createBeanProperty(PropertyDescriptor desc,
                                             Object[] propAccessClsf,
                                             Object[] propParentChildDepClsf)
    {
        if (desc.getPropertyType() == null)
            return null;

        RADProperty prop = null;
        if(desc instanceof FakePropertyDescriptor){
            try {
                prop = new FakeRADProperty(this, (FakePropertyDescriptor) desc);   
            } catch (IntrospectionException ex) { // should not happen
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
		return null;
            }             
        } else {
            prop = new RADProperty(this, desc);
        }

	int access = FormUtils.getPropertyAccess(desc, propAccessClsf);
	if (access != 0)
	    prop.setAccessType(access);

        String parentChildDependency = FormUtils.getPropertyParentChildDependency(
                                         desc, propParentChildDepClsf);
        if (parentChildDependency != null)
            prop.setValue(parentChildDependency, Boolean.TRUE);

	setPropertyListener(prop);

	// prop.addPropertyChangeListener(getPropertyListener());
	nameToProperty.put(desc.getName(), prop);
        
        // setting javax.swing.Action property should not overwrite
        // manually entered prop values (text, tooltip, etc.)
        if ("action".equals(prop.getName()) && // NOI18N 
                (AbstractButton.class.isAssignableFrom(beanClass)
                || JComboBox.class.isAssignableFrom(beanClass))) {
            prop.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (FormProperty.CURRENT_EDITOR.equals(evt.getPropertyName())) {
                        // Another event will come later, now it is too soon
                        // to re-set the properties. The change in action property
                        // will be fired later and would clear the re-set properties.
                        return;
                    }
                    try {
                        // prop names copied from AbstractButton.configurePropertiesFromAction()
                        // method from JDK5
                        String[] propNames = {"mnemonic", "text", "toolTipText", // NOI18N
                            "icon", "actionCommand", "enabled"}; // NOI18N
                        for (String propName : propNames) {
                            FormProperty property = (FormProperty) getPropertyByName(propName);

                            if (property != null) {
                                boolean exChangeMonitoring = property.isExternalChangeMonitoring();
                                property.setExternalChangeMonitoring(false);

                                if (property.isChanged()) {
                                    Object origValue = property.getValue();

                                    if (!propName.equals("text") || // NOI18N
                                            !getName().equals(property.getRealValue())) {
                                        property.setExternalChangeMonitoring(exChangeMonitoring);
                                        property.setValue(origValue);
                                    } else {
                                        property.setExternalChangeMonitoring(exChangeMonitoring);
                                    }
                                } else {
                                    // no changes, just set monitor back
                                    property.setExternalChangeMonitoring(exChangeMonitoring);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                }
            });
        }

        return prop;
    }
 
    private EventProperty createEventProperty(String eventId,
                                                EventSetDescriptor eventDesc,
                                                Method eventMethod)
    {
        EventProperty prop = new EventProperty(new Event(this,
                                                         eventDesc,
                                                         eventMethod),
                                               eventId);
        nameToProperty.put(eventId, prop);
        return prop;
    }

    /** Called to modify original bean properties obtained from BeanInfo.
     * Properties may be added, removed etc. - due to specific needs.
     * 
     * @param prefProps preferred properties.
     * @param normalProps normal properties.
     * @param expertProps expert properties.
     */
    protected void changePropertiesExplicitly(List<RADProperty> prefProps,
                                              List<RADProperty> normalProps,
                                              List<RADProperty> expertProps) {
        // Issue 171445 - missing cursor property
        if (FormUtils.isStandardJavaComponent(getBeanClass())) {
            try {
                if (nameToProperty.get("cursor") == null) { // NOI18N
                    PropertyDescriptor pd = new PropertyDescriptor("cursor", java.awt.Component.class); // NOI18N
                    RADProperty prop = createBeanProperty(pd, null, null);
                    nameToProperty.put("cursor", prop); // NOI18N
                    normalProps.add(prop);
                }
                ensureWritableProperty("minimumSize", java.awt.Component.class, prefProps, normalProps, expertProps); // NOI18N
                ensureWritableProperty("preferredSize", java.awt.Component.class, prefProps, normalProps, expertProps); // NOI18N
                ensureWritableProperty("maximumSize", java.awt.Component.class, prefProps, normalProps, expertProps); // NOI18N
            } catch (IntrospectionException ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }
         // hack for buttons - add fake property for ButtonGroup
        if (getBeanInstance() instanceof javax.swing.AbstractButton) {
            try {
                RADProperty prop = new ButtonGroupProperty(this);
                setPropertyListener(prop);
//                prop.addPropertyChangeListener(getPropertyListener());
                nameToProperty.put(prop.getName(), prop);

                Object propCategory = FormUtils.getPropertyCategory(
                    prop.getPropertyDescriptor(),
                    FormUtils.getPropertiesCategoryClsf(
                        beanClass, getBeanInfo().getBeanDescriptor()));

                if (propCategory == FormUtils.PROP_PREFERRED)
                    prefProps.add(prop);
                else normalProps.add(prop);
            }
            catch (IntrospectionException ex) {} // should not happen
        } else if (getBeanInstance() instanceof javax.swing.JTable) {
            try {
                PropertyDescriptor pd = new PropertyDescriptor("rowHeight", javax.swing.JTable.class); // NOI18N
                RADProperty prop = createBeanProperty(pd, null, null);
                nameToProperty.put("rowHeight", prop); // NOI18N
                normalProps.add(prop);
            } catch (IntrospectionException ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }
        
        // PENDING improve performance - keep lookup result, listen on it etc.
        boolean modified = false;
        Lookup.Template<PropertyModifier> template = new Lookup.Template<PropertyModifier>(PropertyModifier.class);
        Collection<? extends PropertyModifier> modifiers = Lookup.getDefault().lookup(template).allInstances();
        for (PropertyModifier modifier : modifiers) {
            modified |= modifier.modifyProperties(this, prefProps, normalProps, expertProps);
        }
        
        if (modified) {
            checkForAddedProperties(prefProps);
            checkForAddedProperties(normalProps);
            checkForAddedProperties(expertProps);
        }
    }

    private void ensureWritableProperty(String name, Class clazz,
            List<RADProperty> prefProps, List<RADProperty> normalProps, List<RADProperty> expertProps)
            throws IntrospectionException {
        Node.Property prop = nameToProperty.get(name);
        if (prop == null || !prop.canWrite()) {
            PropertyDescriptor pd = new PropertyDescriptor(name, clazz);
            RADProperty radProp = createBeanProperty(pd, null, null);
            nameToProperty.put(name, radProp);
            boolean replaced = replaceProperty(radProp, prefProps)
                    || replaceProperty(radProp, normalProps)
                    || replaceProperty(radProp, expertProps);
            if (!replaced) {
                normalProps.add(radProp);
            }
        }
    }

    private static boolean replaceProperty(RADProperty newProp, List<RADProperty> props) {
        String name = newProp.getName();
        for (RADProperty prop : props) {
            if (name.equals(prop.getName())) {
                props.remove(prop);
                if (prop.valueSet) {
                    try {
                        newProp.setCurrentEditor(prop.getCurrentEditor());
                        newProp.setValue(prop.getValue());
                    } catch (IllegalAccessException ex) {
                        FormUtils.LOGGER.log(Level.INFO, ex.getMessage(), ex);
                    } catch (IllegalArgumentException ex) {
                        FormUtils.LOGGER.log(Level.INFO, ex.getMessage(), ex);
                    } catch (InvocationTargetException ex) {
                        FormUtils.LOGGER.log(Level.INFO, ex.getMessage(), ex);
                    }
                }
                props.add(newProp);
                return true;
            }
        }
        return false;
    }

    private void checkForAddedProperties(List props) {
        for (Object o : props) {
            FormProperty prop = (FormProperty)o;
            String propName = prop.getName();
            Node.Property knownProp = nameToProperty.get(propName);
            if (prop != knownProp) {
                nameToProperty.put(propName, prop);
                setPropertyListener(prop);
            }
        }
    }

    protected PropertyChangeListener createPropertyListener() {
        return new PropertyListenerConvertor();
    }

    public void setPropertyListener(FormProperty property) {
        if (propertyListener == null)
            propertyListener = createPropertyListener();
        if (propertyListener != null) {
            property.addPropertyChangeListener(propertyListener);
            if (propertyListener instanceof FormProperty.ValueConvertor)
                property.addValueConvertor((FormProperty.ValueConvertor)propertyListener);
        }
    }

    /** Listener class for listening to changes in component's properties.
     */
    private class PropertyListenerConvertor implements PropertyChangeListener, FormProperty.ValueConvertor {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof FormProperty))
                return;

            FormProperty property = (FormProperty) source;
            String propName = property.getName();
            String eventName = evt.getPropertyName();

            if (FormProperty.PROP_VALUE.equals(eventName)
                || FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName))
            {   // property value has changed (or value and editor together)
                resourcePropertyChanged(evt);

                Object oldValue = evt.getOldValue();
                Object newValue = evt.getNewValue();
                formModel.fireComponentPropertyChanged(
                              RADComponent.this, propName, oldValue, newValue);

                if (getNodeReference() != null) { // propagate the change to node
                    if (FormProperty.PROP_VALUE_AND_EDITOR.equals(eventName)) {
                        oldValue = ((FormProperty.ValueWithEditor)oldValue).getValue();
                        newValue = ((FormProperty.ValueWithEditor)newValue).getValue();
                    }

                    getNodeReference().firePropertyChangeHelper(
//                                                null, null, null);
                                           propName, oldValue, newValue);
                }
            }
            else if (FormProperty.CURRENT_EDITOR.equals(eventName)) {
                // property editor has changed - don't fire to FormModel,
                // only to component node
                if (getNodeReference() != null)
                    getNodeReference().firePropertyChangeHelper(
                                            propName, null, null);
            }
        }

        @Override
        public Object convert(Object value, FormProperty property) {
            return resourcePropertyConvert(value, property);
        }
    }

    // -----
    // resources/i18n automation

    Object resourcePropertyConvert(Object value, FormProperty property) {
        if (isInModel() && formModel.isUndoRedoRecording()
                && property.getPropertyContext().getFormModel() != null) {
            Object resVal = ResourceSupport.makeResource(value, property);
            if (resVal != value)
                return resVal;
        }
        return value; // do nothing
    }

    void resourceComponentRename(String oldName, String newName) {
        if (isInModel()) {
            ResourceSupport.componentRenamed(this, oldName, newName);
        }
    }

    void resourcePropertyChanged(PropertyChangeEvent ev) {
        if (isInModel() && formModel.isFormLoaded()) {
            ResourceSupport.updateStoredValue(
                    FormProperty.getEnclosedValue(ev.getOldValue()), // in case it is ValueWithEditor
                    FormProperty.getEnclosedValue(ev.getNewValue()), // in case it is ValueWithEditor
                    (FormProperty)ev.getSource());
        }
    }

    // ----------

    private static class PropertyIterator<T extends FormProperty> implements java.util.Iterator<T> {
        private T[] properties;
        private FormProperty.Filter filter;

        private T next;
        private int index;

        PropertyIterator(T[] properties,
                         FormProperty.Filter filter)
        {
            this.properties = properties;
            this.filter = filter;
        }

        @Override
        public boolean hasNext() {
            if (next == null)
                next = getNextProperty();
            return next != null;
        }

        @Override
        public T next() {
            if (next == null)
                next = getNextProperty();
            if (next != null) {
                T prop = next;
                next = null;
                return prop;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private T getNextProperty() {
            while (index < properties.length) {
                T prop = properties[index++];
                if (filter.accept(prop))
                    return prop;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return super.toString() + ", name: "+getName()+", class: "+getBeanClass()+", beaninfo: "+getBeanInfo() + ", instance: "+getBeanInstance(); // NOI18N
    }

    // ----------
    // a reference to a metacomponent - used instead of a metacomponent, may
    // become invalid when the component is removed

    interface ComponentReference {
        RADComponent getComponent();
    }

    // ------------------------------------
    // some hacks for ButtonGroup "component" ...

    // pseudo-property for buttons - holds ButtonGroup in which button
    // is placed; kind of "reversed" property
    static class ButtonGroupProperty extends RADProperty {
        ButtonGroupProperty(RADComponent comp) throws IntrospectionException {
            super(comp,
                  new FakePropertyDescriptor("buttonGroup", // NOI18N
                                             javax.swing.ButtonGroup.class));
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(FormUtils.getBundleString("HINT_ButtonGroup")); // NOI18N
        }

        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        @Override
        public Object getDefaultValue() {
            return null;
        }

        @Override
        public PropertyEditor getExpliciteEditor() {
            return new ButtonGroupPropertyEditor();
        }

        @Override
        String getWholeSetterCode(String groupName) {
            return groupName != null ?
                groupName + ".add(" + getRADComponent().getName() + ");" : // NOI18N
                null;
        }

        @Override
        public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
            if (this.getValue() instanceof FormDesignValue) {
                FormDesignValue formValue = (FormDesignValue) this.getValue();

                if (formValue.getDesignValue() instanceof ButtonGroup) {
                    AbstractButton button = (AbstractButton) this.getRADComponent().getBeanInstance();
                    synchronizeButtonGroupInAWT(button, formValue, null);
                }
            }
            super.restoreDefaultValue();
        }
        
        // add/removes AbtractButton components to/from RadioGroup component now or later
        private void synchronizeButtonGroup(final AbstractButton button, 
                                            final Object originalComponent, 
                                            final Object newComponent)
        {
            boolean isOriginalValid = (originalComponent == null) ||
                    (originalComponent instanceof FormDesignValue);
            boolean isNewValid = (newComponent == null) ||
                    (newComponent instanceof FormDesignValue);

            if (isOriginalValid && isNewValid) {
                boolean waitForButtonGroupInstance = false;

                if (newComponent != null) {
                    Object comp = ((FormDesignValue) newComponent).getDesignValue();
                    waitForButtonGroupInstance = !(comp instanceof ButtonGroup);
                }

                if (originalComponent != null) {
                    Object comp = ((FormDesignValue) originalComponent).getDesignValue();
                    waitForButtonGroupInstance = !(comp instanceof ButtonGroup);
                }

                // if there are already instances of buttongroup components
                if (!waitForButtonGroupInstance) {
                    synchronizeButtonGroupInAWT(button,
                            (FormDesignValue) originalComponent,
                            (FormDesignValue) newComponent);
                } else {
                    // there are no instances of buttongroup components yet, 
                    // ide is loading form right now, try later ...
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            synchronizeButtonGroupInAWT(button,
                                    (FormDesignValue) originalComponent,
                                    (FormDesignValue) newComponent);
                        }
                    });
                }
            }
        }

        // add/removes AbtractButton components to/from RadioGroup component
        private void synchronizeButtonGroupInAWT(AbstractButton button, 
                                                 FormDesignValue originalComponent, 
                                                 FormDesignValue newComponent)
        {
            if (originalComponent != null) {
                //remove button from original buttongroup
                Object group = originalComponent.getDesignValue();
                if (group instanceof ButtonGroup) {
                    ((ButtonGroup) group).remove(button);
                }
            }

            if (newComponent != null) {
                // add button to new buttongroup
                Object groupObj = newComponent.getDesignValue();
                if (groupObj instanceof ButtonGroup) {
                    ButtonGroup group = (ButtonGroup) groupObj;
                    
                    // try to find button inside buttongroup
                    boolean add = true;
                    for (Enumeration e = group.getElements(); e.hasMoreElements(); ) {
                         if (button.equals(e.nextElement())) {
                            add = false;
                            break;
                         }
                     }                    

                    // button not found inside group, add it
                    if (add) {
                        group.add(button);
                    }
                }
            }
        }

        @Override
        public void setValue(Object value) throws IllegalAccessException,
                                                  IllegalArgumentException,
                                                  InvocationTargetException
        {
            Object originalValue = super.getValue();
            super.setValue(value);

            // get swing abstractbutton component
            AbstractButton button = (AbstractButton) this.getRADComponent().getBeanInstance();

            // add/remove button from buttongroup
            // note: using "getValue()" instead of "value", because setValue()
            //       handles more different types (eg.FormProperty.ValueWithEditor)
            synchronizeButtonGroup(button, originalValue, this.getValue());
        }
    }

    // property editor for selecting ButtonGroup (for ButtonGroupProperty)
    public static class ButtonGroupPropertyEditor extends ComponentChooserEditor {
        public ButtonGroupPropertyEditor() {
            super();
            setBeanTypes(new Class[] { javax.swing.ButtonGroup.class });
            setComponentCategory(NONVISUAL_COMPONENTS);
        }
        
        @Override
        public String getDisplayName() {
            return NbBundle.getBundle(getClass()).getString("CTL_ButtonGroupPropertyEditor_DisplayName"); // NOI18N
        }
    }
    
    public void setValid(boolean valid){
        this.valid = valid;
    }
    
    protected boolean isValid() {
        return valid;
    }

    void ensureDefaultPropertyValuesInitialization() {
        for (RADProperty prop : getAllBeanProperties()) {
            prop.ensureDefaultValueInitialization();
        }
    }
    
    private class FakeBeanInfo extends SimpleBeanInfo {
        
        private List<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor>();
        
        @Override
        public BeanDescriptor getBeanDescriptor() {
            return (beanInfo == this) ? new BeanDescriptor(beanClass) : beanInfo.getBeanDescriptor();            
        }

        @Override
        public PropertyDescriptor[] getPropertyDescriptors() {
            return propertyDescriptors.toArray(new PropertyDescriptor[0]);
        }

        @Override
        public EventSetDescriptor[] getEventSetDescriptors() {            
	    return new EventSetDescriptor[0];
        }

        @Override
        public MethodDescriptor[] getMethodDescriptors() {
            return new MethodDescriptor[0];
        }

        void addPropertyDescriptor(String propertyName, Class propertyClass) {
            try {
                propertyDescriptors.add(new FakePropertyDescriptor(propertyName, propertyClass));
            } catch (IntrospectionException ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex); // should not happen
            }
        }
                
        void addPropertyDescriptor(PropertyDescriptor pd) {
            propertyDescriptors.add(pd);
        }
        
        void removePropertyDescriptors() {
            propertyDescriptors.clear();
        }
    }

    private class MetaAccessibleContext {
        private Object accName = BeanSupport.NO_VALUE;
        private Object accDescription = BeanSupport.NO_VALUE;
        private Object accParent = BeanSupport.NO_VALUE;

        private FormProperty[] properties;

        FormProperty[] getProperties() {
            if (properties == null) {
                properties = new FormProperty[] {
                    new FormProperty(
                        "AccessibleContext.accessibleName", // NOI18N
                        String.class,
                        FormUtils.getBundleString("PROP_AccessibleName"), // NOI18N
                        FormUtils.getBundleString("PROP_AccessibleName")) // NOI18N
                    {
                        @Override
                        public Object getTargetValue() {
                            return accName != BeanSupport.NO_VALUE ?
                                       accName : getDefaultValue();
                        }
                        @Override
                        public void setTargetValue(Object value) {
                            accName = (String) value;
                        }
                        @Override
                        public boolean supportsDefaultValue () {
                            return true;
                        }
                        @Override
                        public Object getDefaultValue() {
                            return getAccessibleContext().getAccessibleName();
                        }
                        @Override
                        public void restoreDefaultValue()
                            throws IllegalAccessException,
                                   java.lang.reflect.InvocationTargetException
                        {
                            super.restoreDefaultValue();
                            accName = BeanSupport.NO_VALUE;
                        }
                        @Override
                        String getPartialSetterCode(String javaInitStr) {
                            return "getAccessibleContext().setAccessibleName(" // NOI18N
                                   + javaInitStr + ")"; // NOI18N
                        }
                    },

                    new FormProperty(
                        "AccessibleContext.accessibleDescription", // NOI18N
                        String.class,
                        FormUtils.getBundleString("PROP_AccessibleDescription"), // NOI18N
                        FormUtils.getBundleString("PROP_AccessibleDescription")) // NOI18N
                    {
                        @Override
                        public Object getTargetValue() {
                            return accDescription != BeanSupport.NO_VALUE ?
                                       accDescription : getDefaultValue();
                        }
                        @Override
                        public void setTargetValue(Object value) {
                            accDescription = (String) value;
                        }
                        @Override
                        public boolean supportsDefaultValue () {
                            return true;
                        }
                        @Override
                        public Object getDefaultValue() {
                            AccessibleContext context = getAccessibleContext();
                            Object bean = getBeanInstance();
                            if (bean instanceof JComponent) {
                                Object o = ((JComponent)bean).getClientProperty("labeledBy"); // NOI18N
                                if (o instanceof Accessible) {
                                    AccessibleContext ac = ((Accessible) o).getAccessibleContext();
                                    if (ac == context) {
                                        return FormUtils.getBundleString("MSG_CyclicAccessibleContext"); // NOI18N
                                    }
                                }
                            }
                            return context.getAccessibleDescription();
                        }
                        @Override
                        public void restoreDefaultValue()
                            throws IllegalAccessException,
                                   java.lang.reflect.InvocationTargetException
                        {
                            super.restoreDefaultValue();
                            accDescription = BeanSupport.NO_VALUE;
                        }
                        @Override
                        String getPartialSetterCode(String javaInitStr) {
                            return
                              "getAccessibleContext().setAccessibleDescription(" // NOI18N
                              + javaInitStr + ")"; // NOI18N
                        }
                    },

                    new FormProperty(
                        "AccessibleContext.accessibleParent", // NOI18N
                        Accessible.class,
                        FormUtils.getBundleString("PROP_AccessibleParent"), // NOI18N
                        FormUtils.getBundleString("PROP_AccessibleParent")) // NOI18N
                    {
                        @Override
                        public Object getTargetValue() {
                            return accParent != BeanSupport.NO_VALUE ?
                                       accParent : getDefaultValue();
                        }
                        @Override
                        public void setTargetValue(Object value) {
                            accParent = value;
                        }
                        @Override
                        public boolean supportsDefaultValue () {
                            return true;
                        }
                        @Override
                        public Object getDefaultValue() {
                            Object acP = getAccessibleContext()
                                             .getAccessibleParent();
                            if (acP != null) {
                                RADComponent metaparent = getParentComponent();
                                if (metaparent != null) {
                                    Object cont;
                                    if (metaparent instanceof RADVisualContainer) {
                                        RADVisualContainer metacont = (RADVisualContainer)metaparent;
                                        cont = metacont.getContainerDelegate(metacont.getBeanInstance());
                                    } else {
                                        cont = metaparent.getBeanInstance();
                                    }
                                    if (cont == acP)
                                        return metaparent;
                                }
                            }
                            return acP;
                        }
                        @Override
                        public void restoreDefaultValue()
                            throws IllegalAccessException,
                                   java.lang.reflect.InvocationTargetException
                        {
                            super.restoreDefaultValue();
                            accParent = BeanSupport.NO_VALUE;
                        }
                        @Override
                        String getPartialSetterCode(String javaInitStr) {
                            return javaInitStr == null ? null :
                                "getAccessibleContext().setAccessibleParent(" // NOI18N
                                + javaInitStr + ")"; // NOI18N
                        }
                    }
                };
            }
            return properties;
        }

        private AccessibleContext getAccessibleContext() {
            return ((Accessible)getBeanInstance()).getAccessibleContext();
        }
    }

}
