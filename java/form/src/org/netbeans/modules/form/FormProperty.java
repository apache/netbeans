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

package org.netbeans.modules.form;

import java.beans.*;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node;

/**
 * This class provides basic implementation of properties used in form module
 * which are generated in the java code. FormProperty can use multiple property
 * editors (via FormPropertyEditor) and special "design values" (holding some
 * additional data - FormDesignValue implementations).
 *
 * FormProperty is an "interface" object that provides general access to one
 * property of some other object (called "target object"). To make it work,
 * only some connection to the target object must be implemented. There are
 * two (abstract) methods for this purpose in FormProperty class:
 *     public Object getTargetValue();
 *     public void setTargetValue(Object value);
 *
 * NOTE: Binding to target object can be switched off for reading or writing
 * by setting access type of property to DETACHED_READ or DETACHED_WRITE.
 *
 * There are some further methods (potentially suitable) for custom
 * implementation (overriding the default implementation):
 *     public boolean supportsDefaultValue();
 *     public Object getDefaultValue();
 *     public PropertyEditor getExpliciteEditor();
 *
 * NOTE: Properties are created for nodes and presented in property sheet.
 * Node object that owns properties should listen to the CURRENT_EDITOR
 * property change on each property and call firePropertySetsChange(...)
 * to notify the sheet about changing current property editor of a property.
 *
 * @author Tomas Pavek
 */

public abstract class FormProperty extends Node.Property {

    // --------------------
    // constants

    public static final String PROP_VALUE = "propertyValue"; // NOI18N
    public static final String CURRENT_EDITOR = "currentEditor"; // NOI18N
    public static final String PROP_VALUE_AND_EDITOR = "propertyValueAndEditor"; // NOI18N
    public static final String PROP_PRE_CODE = "preCode"; // NOI18N
    public static final String PROP_POST_CODE = "postCode"; // NOI18N

    // "Access type" of the property (in relation to the target object).
    // There are three levels of restriction possible:
    //   NORMAL_RW - no restriction on both property and target object
    //   DETACHED_READ, DETACHED_WRITE - no reading or writing on the target
    //       object (it is "detached"; the value is cached by the property)
    //   NO_READ, NO_WRITE - it is not possible to perform read or write on
    //       the property (so neither on the target object)
    public static final int NORMAL_RW = 0;

    public static final int DETACHED_READ = 1; // no reading from target (bit 0)
    public static final int DETACHED_WRITE = 2; // no writing to target (bit 1)

    private static final int NO_READ_PROP = 4; // bit 2
    private static final int NO_WRITE_PROP = 8; // bit 3
    public static final int NO_READ = DETACHED_READ | NO_READ_PROP; // no reading from property (bits 0,2)
    public static final int NO_WRITE = DETACHED_WRITE | NO_WRITE_PROP; // no writing to property (bits 1,3)

    // Placeholder for the default value
    public static final Object DEFAULT_VALUE = new Object();

    // ------------------------
    // variables
    protected int propType = NORMAL_RW;

    FormPropertyContext propertyContext;

    protected Object propertyValue; // cached value of the property
    protected boolean valueSet = false; // propertyValue validity
    boolean valueChanged = false; // non-default value that came in through setValue

    private boolean externalChangeMonitoring = true;
    private Object lastRealValue; // for detecting external change of the property value

    String preCode;
    String postCode;

    FormPropertyEditor formPropertyEditor;
    PropertyEditor currentEditor;

    private PropertyChangeSupport changeSupport;
    private VetoableChangeSupport vetoableChangeSupport;
    private boolean fireChanges = true;

    private java.util.List<ValueConvertor> convertors;

//    private DesignValueListener designValueListener = null;

    // ---------------------------
    // constructors

    protected FormProperty(FormPropertyContext propertyContext,
                           String name, Class type,
                           String displayName, String shortDescription)
    {
        super(type);
        setValue("changeImmediate", Boolean.FALSE); // NOI18N
        setName(name);
        setDisplayName(displayName);
        setShortDescription(getDescriptionWithType(shortDescription, getValueType()));

        this.propertyContext = FormPropertyContext.EmptyImpl.getInstance();
        setPropertyContext(propertyContext);
    }

    protected FormProperty(FormPropertyContext propertyContext, Class type) {
        super(type);
        setValue("changeImmediate", Boolean.FALSE); // NOI18N

        this.propertyContext = FormPropertyContext.EmptyImpl.getInstance();
        setPropertyContext(propertyContext);
    }

    // constructor of property without PropertyContext
    // setPropertyContext(...) should be called explicitly then
    protected FormProperty(String name, Class type,
                           String displayName, String shortDescription)
    {
        super(type);
        setValue("changeImmediate", Boolean.FALSE); // NOI18N
        setName(name);
        setDisplayName(displayName);
        setShortDescription(getDescriptionWithType(shortDescription, getValueType()));

        this.propertyContext = FormPropertyContext.EmptyImpl.getInstance();
    }

    // constructor of property without PropertyContext;
    // setPropertyContext(...) must be called explicitly before the property
    // is used first time
    protected FormProperty(Class type) {
        super(type);
        setValue("changeImmediate", Boolean.FALSE); // NOI18N

        this.propertyContext = FormPropertyContext.EmptyImpl.getInstance();
    }

    static String getDescriptionWithType(String description, Class valueType) {
        String type = org.openide.util.Utilities.getClassName(valueType);
        return description == null ?
            FormUtils.getFormattedBundleString("HINT_PropertyType", // NOI18N
                                               new Object[] { type }) :
            FormUtils.getFormattedBundleString("HINT_PropertyTypeWithDescription", // NOI18N
                                               new Object[] { type, description });
    }

    // ----------------------------------------
    // getter, setter & related methods

    @Override
    public String getHtmlDisplayName() {
        if (isChanged()) {
            return "<b>" + getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    /** Gets the real value of this property directly from the target object.
     * 
     * @return real value value of this property directly from the target object.
     * @throws java.lang.IllegalAccessException when there is an access problem.
     * @throws java.lang.reflect.InvocationTargetException when there is an invocation problem.
     */
    public abstract Object getTargetValue() throws IllegalAccessException,
                                                   InvocationTargetException;

    /** Sets the real property value directly to the target object.
     * 
     * @param value 
     * @throws java.lang.IllegalAccessException when there is an access problem.
     * @throws java.lang.IllegalArgumentException when the specified value is not valid.
     * @throws java.lang.reflect.InvocationTargetException when there is an invocation problem.
     */
    public abstract void setTargetValue(Object value) throws IllegalAccessException,
                                                      IllegalArgumentException,
                                                      InvocationTargetException;

    private void setTargetValueInLAFBlock(final Object value)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (!FormLAF.inLAFBlock() && (propertyContext.getFormModel() != null)) {
            final Exception[] ex = new Exception[1];
            FormLAF.executeWithLookAndFeel(propertyContext.getFormModel(), new Runnable() {
                @Override
                public void run() {
                    try {
                        setTargetValue(value);
                    } catch (IllegalAccessException iaex) {
                        ex[0] = iaex;
                    } catch (IllegalArgumentException argex) {
                        ex[0] = argex;
                    } catch (InvocationTargetException itex) {
                        ex[0] = itex;
                    }
                }
            });
            if (ex[0] != null) {
                if (ex[0] instanceof IllegalArgumentException) {
                    throw new IllegalArgumentException(ex[0]);
                } else if (ex[0] instanceof InvocationTargetException) {
                    throw new InvocationTargetException(ex[0]);
                } else if (ex[0] instanceof IllegalAccessException) {
                    throw (IllegalAccessException)ex[0];
                }
            }
        } else {
            setTargetValue(value);
        }
    }

    /** Gets the value of the property.
     * 
     * @throws java.lang.IllegalAccessException when there is an access problem.
     * @throws java.lang.reflect.InvocationTargetException when there is an invocation problem.
     */
    @Override
    public Object getValue() throws IllegalAccessException,
                                    InvocationTargetException {
//        if (!canRead())
//            throw new IllegalAccessException("Not a readable property: "+getName());
        Object value = checkCurrentValue();

        if (valueSet || (propType & DETACHED_READ) == 0)
            return value;

        return getDefaultValue();
    }

    /** Sets the property value.
     * 
     * @param value property value.
     * @throws java.lang.IllegalAccessException when there is an access problem.
     * @throws java.lang.IllegalArgumentException when the specified value is not valid.
     * @throws java.lang.reflect.InvocationTargetException when there is an invocation problem.
     */
    @Override
    public void setValue(Object value) throws IllegalAccessException,
                                              IllegalArgumentException,
                                              InvocationTargetException
    {
        if (value == DEFAULT_VALUE) {
            value = getDefaultValue();
        }
        // let the registered converters do something with the value (e.g. i18n)
        if (fireChanges)
            value = convertValue(value);

        Object oldValue;
        if (canRead()) {
            try { // get the old value (still the current)
                oldValue = getValue();
            }
            catch (Exception e) {  // no problem -> keep null
                oldValue = BeanSupport.NO_VALUE;
            }
        }
        else oldValue = BeanSupport.NO_VALUE;
        
        if (value instanceof ValueWithEditor) {
            // changing value and property editor at once
            ValueWithEditor vwpe = (ValueWithEditor) value;
            value = vwpe.getValue();
            PropertyEditor newEditor = vwpe.getPropertyEditor(this);
            PropertyEditor oldEditor = currentEditor;

            if (newEditor != oldEditor) {
                // turn off change firing as we fire the two changes as one
                boolean fire = fireChanges;
                fireChanges = false;
                setCurrentEditor(newEditor);
                setValue(value);
                fireChanges = fire;

                if (oldValue == BeanSupport.NO_VALUE)
                    oldValue = null; // [should not BeanSupport.NO_VALUE remain??]

                propertyValueAndEditorChanged(
                    new ValueWithEditor(oldValue, oldEditor),
                    new ValueWithEditor(value, newEditor));

                return;
            }
            // othrewise continue setting only the value itself
        }

        if (oldValue != BeanSupport.NO_VALUE) {
            // check whether the new value is different
            if (!(value instanceof FormDesignValue) && equals(value, oldValue))
                return; // no change
        }
        else oldValue = null; // [should not BeanSupport.NO_VALUE remain??]

        if (value == BeanSupport.NO_VALUE) {
            // special value to be set - reset the change flag
            valueSet = false;
            setChanged(false);
            propertyValue = value;
            lastRealValue = null;
            propertyValueChanged(oldValue, value);
            return;
        }

        Object defValue = supportsDefaultValue() ?
                            getDefaultValue() : BeanSupport.NO_VALUE;

        if (canWriteToTarget()) {
            // derive real value
            Object realValue = getRealValue(value);

            // set the real value to the target object
            if (realValue != FormDesignValue.IGNORED_VALUE) {
                setTargetValueInLAFBlock(realValue);
            } else if (valueSet && defValue != BeanSupport.NO_VALUE
                       && (lastRealValue != null || defValue != null)/* bug 230687 */) {
                setTargetValueInLAFBlock(defValue);
            }

            if (canReadFromTarget()) {
                lastRealValue = getTargetValue();
//                if (value == realValue)
//                    value = lastRealValue;

/*
  Some bad properties of bad beans return another value than the one just set.
  So which one should be then used as the valid property value (displayed,
  generated in code, etc) - the one just set, or that got in turn from getter?
  (1) When the value just set is taken, then e.g. NONE_OPTION (-1) set to
  debugGraphicsOption of JComponent will be used and code generated, altough it
  is converted to 0 which is the default value, so no code should be generated.
  (2) When oppositely the value from getter after performing setter is taken,
  then e.g. setting "text/xml" to contentType of JEditorPane may fail at design
  time (editor kit is not found), so the value reverts to "text/plain" (default)
  and no code is generated, however it could work at runtime, so the code
  should be generated.
  [See also bug 12413.]
*/
            }
        }

        propertyValue = value; // cache the value for later...
        valueSet = true;

        // "changed" == property is readable and writeable and the new value
        // is not equal to the default value (or default value doesn't exist).
        setChanged((propType & (NO_READ_PROP|NO_WRITE_PROP)) == 0
                   && (defValue == BeanSupport.NO_VALUE
                       || !equals(value, defValue)));

//        settleDesignValueListener(oldValue, value);
        propertyValueChanged(oldValue, value);
    }

    /** This method gets the real value of the property. This is a support
     * for special "design values" that hold additional information besides
     * the real value that can be set directly to target object.
     * 
     * @return real value of the property.
     * @throws java.lang.IllegalAccessException when there is an access problem.
     * @throws java.lang.reflect.InvocationTargetException when there is an invocation problem.
     */
    public final Object getRealValue() throws IllegalAccessException,
                                              InvocationTargetException {
        return getRealValue(getValue());
    }

    /** This method "extracts" the real value from the given object.
     * FormDesignValue is recognized by default. Subclasses may override
     * this method to provide additional conversions.
     * 
     * @param value value that should be possibly extracted.
     * @return real value.
     */
    protected Object getRealValue(Object value) {
        while (value instanceof FormDesignValue) {
            Object prev = value;
            value = ((FormDesignValue)value).getDesignValue();
            if (value == prev)
                break;
        }
        return value;
    }

    /** Returns whether this property has a default value (false by default).
     * If any subclass provides default value, it should override this
     * and getDefaultValue() methods.
     * @return true if there is a default value, false otherwise
     */
    @Override
    public boolean supportsDefaultValue () {
        return false;
    }
    
    @Override
    public boolean isDefaultValue() {
        return supportsDefaultValue() ? !isChanged() : true;
    }

    /** Returns a default value of this property.
     * If any subclass provides default value, it should override this
     * and supportsDefaultValue() methods.
     * @return the default value (null by default :)
     */
    public Object getDefaultValue() {
        return null;
    }

    /** Restores the property to its default value.
     * 
     * @throws java.lang.IllegalAccessException when there is an access problem.
     * @throws java.lang.reflect.InvocationTargetException when there is an invocation problem.
     */
    @Override
    public void restoreDefaultValue() throws IllegalAccessException,
                                             InvocationTargetException {
//        if (!canWrite()) return;
        setChanged(false);

        Object oldValue = null;
        Object defValue = getDefaultValue();

        if (canRead()) {
            try {  // get the old value (still the current)
                oldValue = getValue();
                if (!(defValue instanceof FormDesignValue)
                        && equals(defValue, oldValue))
                    return; // no change
            }
            catch (Exception e) {}  // no problem -> keep null
        }

        if (canWriteToTarget()) {
            // derive real value (from the default value)
            Object realValue = getRealValue(defValue);

            try {
                // set the default real value to the target
                if (realValue != FormDesignValue.IGNORED_VALUE) {
                    setTargetValueInLAFBlock(realValue);
//                    lastRealValue = realValue;
                }
                else if (defValue != BeanSupport.NO_VALUE) {
                    setTargetValueInLAFBlock(defValue);
//                    lastRealValue = defValue;
                }
//                else if (isExternalChangeMonitoring())
//                    lastRealValue = getTargetValue();

                lastRealValue = getTargetValue();
            }
            catch (Exception e) {
                Logger.getLogger(FormProperty.class.getName()).log(Level.INFO, "Failed to reset default value of property: "+getName(), e); // NOI18N
            }
        }

        propertyValue = defValue;
        valueSet = true;

        // set default property editor as current
        PropertyEditor prEd = findDefaultEditor();
        if (prEd != null)
            setCurrentEditor(prEd);

//        settleDesignValueListener(oldValue, defValue);
        propertyValueChanged(oldValue, defValue);
    }

    /** This method re-sets cached value of the property to the target object.
     * (If there is no cached value here, nothing is set to target object.)
     * This may be useful when target object was re-created and needs to be
     * initialized in accordance with current properties.
     * 
     * @throws java.lang.IllegalAccessException when there is an access problem.
     * @throws java.lang.reflect.InvocationTargetException when there is an invocation problem.
     */
    public void reinstateTarget() throws IllegalAccessException,
                                         InvocationTargetException {
        if (valueSet && canWriteToTarget()) 
            try {
                // re-set the real value of the property of the target object
                Object realValue = getRealValue(propertyValue);

                if (realValue != FormDesignValue.IGNORED_VALUE) {
                    setTargetValueInLAFBlock(realValue);
                    lastRealValue = realValue;
                }
                else if (isExternalChangeMonitoring())
                    lastRealValue = getTargetValue();
            }
            catch (IllegalArgumentException e) { // should not happen
            }
    }

    /** This method updates state of the property according to the target
     * object. This may be useful when property needs to be initialized
     * with existing target object. But this approach doesn't work well with
     * bound and derived properties...
     * 
     * @throws java.lang.IllegalAccessException when there is an access problem.
     * @throws java.lang.reflect.InvocationTargetException when there is an invocation problem.
     */
    public void reinstateProperty() throws IllegalAccessException,
                                           InvocationTargetException {
        boolean mayChanged = canReadFromTarget()
                             && (propType & (NO_READ_PROP|NO_WRITE_PROP)) == 0;
            
        if (mayChanged) {
            Object value = getTargetValue();
            if (supportsDefaultValue()) {
                Object defValue = getDefaultValue();
                mayChanged = !equals(value, defValue);
            }
            if (mayChanged) {
                propertyValue = value;
                lastRealValue = value;
            }
        }
        
        valueSet = mayChanged;
        setChanged(mayChanged);
    }

    // ------------------------------
    // boolean flags

    /** Tests whether the property is readable.
     */
    @Override
    public boolean canRead() {
        return (propType & NO_READ_PROP) == 0;
    }

    /** Tests whether the property is writable.
     */
    @Override
    public boolean canWrite() {
        return (propType & NO_WRITE_PROP) == 0;
    }

    public final boolean canReadFromTarget() {
        return /*canRead() &&*/ (propType & DETACHED_READ) == 0;
    }

    public final boolean canWriteToTarget() {
        return /*canWrite() &&*/ (propType & DETACHED_WRITE) == 0;
    }

    /** Tests whether this property is marked as "changed". This method returns
     * true if the value of the property is different from the default value
     * and if it is accessible and replicable (readable and writeable property).
     * 
     * @return <code>true</code> if the property was changed,
     * returns <code>false</code> otherwise.
     */
    public boolean isChanged() {
        if (valueChanged && valueSet) { // update the changed flag
            try {
                checkCurrentValue();
            }
            catch (Exception ex) {
            }
        }
        return valueChanged;
    }

    /** Sets explicitly the flag indicating changed property.
     * 
     * @param changed determines whether this property was changed.
     */
    public void setChanged(boolean changed) {
        valueChanged = changed;
    }

    // --------------------------------
    // property editors
    
    /** Gets a property editor for this property. This method implements
     * Node.Property.getPropertyEditor() and need not be further overriden.
     * It enables using of multiple individual editors by constructing
     * FormPropertyEditor class. There are other methods for controling the
     * FormPropertyEditor class here - see: getCurrentEditor(),
     * setCurrentEditor(...) and getExpliciteEditor().
     */
    @Override
    public PropertyEditor getPropertyEditor() {
        PropertyEditor prEd;

        if (formPropertyEditor == null) {
            if (propertyContext.useMultipleEditors()) {
                formPropertyEditor = new FormPropertyEditor(this);
                prEd = formPropertyEditor;
            }
            else prEd = getCurrentEditor();
        }
        else prEd = formPropertyEditor;

        return prEd;
    }

    /** Gets the currently selected property editor (from multiple editors
     * managed by FormPropertyEditor).
     * 
     * @return current property editor.
     */
    public final PropertyEditor getCurrentEditor() {
        if (currentEditor == null) {
            currentEditor = findDefaultEditor();
            if (currentEditor != null)
                propertyContext.initPropertyEditor(currentEditor, this);
        }
        return currentEditor;
    }

    /** Sets the current property editor that will be used for this property
     * by FormPropertyEditor.
     * 
     * @param newEditor current property editor.
     */
    public final void setCurrentEditor(PropertyEditor newEditor) {
        if (newEditor != currentEditor) {
            if (newEditor != null)
                propertyContext.initPropertyEditor(newEditor, this);

            if (formPropertyEditor != null) {
                if (currentEditor != null)
                    currentEditor.removePropertyChangeListener(formPropertyEditor);
                if (newEditor != null)
                    newEditor.addPropertyChangeListener(formPropertyEditor);
            }

            PropertyEditor old = currentEditor;
            currentEditor = newEditor;
            currentEditorChanged(old, newEditor);
        }
    }

    /** Gets the property editor explicitly designated for this property.
     * This editor is taken as default by FormPropertyEditor.
     * Subclasses should override this method if they provide a special
     * editor for this property.
     * 
     * @return property editor explicitly designated for this property.
     */
    public PropertyEditor getExpliciteEditor() {
        return null;
    }

    // ------------------------------
    // code generation

    /** Gets the java code initializing the property value. It is obtained from
     * current property editor. Example: "Button 1"
     * 
     * @return initialization string.
     */
    public String getJavaInitializationString() {
        try {
            Object value = getValue();
            if (value == null)
                return "null"; // NOI18N

            if (value == BeanSupport.NO_VALUE)
                return null;

            PropertyEditor ed = getCurrentEditor();
            if (ed == null)
                return null;

            // should we create a new instance of editor?
//            if (ed instanceof RADConnectionPropertyEditor)
//                ed = new RADConnectionPropertyEditor(getValueType());
//            else
//                ed = (PropertyEditor)ed.getClass().newInstance();
//            propertyContext.initPropertyEditor(ed);

            if (ed.getValue() != value)
                ed.setValue(value);
            return ed.getJavaInitializationString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Gets the java code for setting the property value (without the object
     * on which the property is set, and without semicolon at the end).
     * This method is optional. Example: setText("Button 1")
     */
    String getPartialSetterCode(String javaInitStr) {
        if (javaInitStr == null)
            return null;

        Method writeMethod = getWriteMethod();
        if (writeMethod == null)
            return null;

        return writeMethod.getName() + "(" + javaInitStr + ")"; // NOI18N
    }

    /** Gets the complete java code for setting the property, including the
     * semicolon at the end of the line. This method is optional.
     * Example: jButton1.setText("Button 1");
     */
    String getWholeSetterCode(String javaInitStr) {
        return null;
    }

    /** 
     * Gets the write method setting the property. 
     * Used by {@link JavaCodeGenerator}.
     *
     * @return write method.
     */
    protected Method getWriteMethod() {
	return null;
    }
    
    /** Gets the code to be generated before the property setter code
     * (on separate line).
     * 
     * @return pre-initialization code.
     */
    public String getPreCode() {
        return preCode;
    }

    /** Gets the code to be generated after the property setter code
     * (on separate line).
     * 
     * @return post-initialization code.
     */
    public String getPostCode() {
        return postCode;
    }

    /** Sets the code to be generated before the property setter code
     * (on separate line).
     * 
     * @param value pre-initialization code.
     */
    public void setPreCode(String value) {
        preCode = value;
    }

    /** Sets the code to be generated after the property setter code
     * (on separate line).
     * 
     * @param value post-initialization code.
     */
    public void setPostCode(String value) {
        postCode = value;
    }

    // ------------------------

    public FormPropertyContext getPropertyContext() {
        return propertyContext;
    }

    public void setPropertyContext(FormPropertyContext newContext) {
        if (newContext == null)
            newContext = FormPropertyContext.EmptyImpl.getInstance();
        if (propertyContext != null
            && formPropertyEditor != null
            && propertyContext.useMultipleEditors()
                 != newContext.useMultipleEditors())
        {
            if (currentEditor != null)
                currentEditor.removePropertyChangeListener(formPropertyEditor);
            formPropertyEditor = null;
        }

        propertyContext = newContext;

        if (currentEditor != null)
            propertyContext.initPropertyEditor(currentEditor, this);
    }

    public int getAccessType() {
        return propType;
    }

    public void setAccessType(int type) {
        if (type >= 0)
            propType = type;
    }

    public boolean isExternalChangeMonitoring() {
        return externalChangeMonitoring && propType == NORMAL_RW;
    }

    public void setExternalChangeMonitoring(boolean val) {
        externalChangeMonitoring = val;
    }

    // ----------------------------

    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }

    public void addVetoableChangeListener(VetoableChangeListener l) {
        synchronized (this) {
            if (vetoableChangeSupport == null)
                vetoableChangeSupport = new VetoableChangeSupport(this);
        }
        vetoableChangeSupport.addVetoableChangeListener(l);
    }

    public void removeVetoableChangeListener(VetoableChangeListener l) {
        if (vetoableChangeSupport != null)
            vetoableChangeSupport.removeVetoableChangeListener(l);
    }

    public boolean isChangeFiring() {
        return fireChanges;
    }

    public void setChangeFiring(boolean fire) {
        fireChanges = fire;
    }

    protected void propertyValueChanged(Object old, Object current) {
        if (fireChanges) {
            try {
                firePropertyChange(PROP_VALUE, old, current);

                // evaluate the required form version level for this value
                Object value;
                PropertyEditor editor;
                if (current instanceof ValueWithEditor) {
                    editor = ((ValueWithEditor)current).getPropertyEditor();
                    value = ((ValueWithEditor)current).getValue();
                } else {
                    value = current;
                    editor = currentEditor;
                }
                FormUtils.checkVersionLevelForProperty(this, value, editor);
            }
            catch (PropertyVetoException ex) {
                boolean fire = fireChanges;
                fireChanges = false;
                try {
                    setValue(old);
                }
                catch (Exception ex2) {} // ignore
                fireChanges = fire;
            }
        }
    }

    protected void currentEditorChanged(PropertyEditor old,
                                        PropertyEditor current)
    {
        if (fireChanges) {
            try {
                firePropertyChange(CURRENT_EDITOR, old, current);
            }
            catch (PropertyVetoException ex) {} // won't happen
        }
    }

    protected void propertyValueAndEditorChanged(ValueWithEditor old,
                                                 ValueWithEditor current)
    {
        if (fireChanges) {
            try {
                firePropertyChange(PROP_VALUE_AND_EDITOR, old, current);
            }
            catch (PropertyVetoException ex) {
                boolean fire = fireChanges;
                fireChanges = false;
                try {
                    setValue(old);
                }
                catch (Exception ex2) {} // ignore
                fireChanges = fire;
            }
        }
    }

    private void firePropertyChange(String propName, Object old, Object current)
        throws PropertyVetoException
    {
        if (vetoableChangeSupport != null && !CURRENT_EDITOR.equals(propName)) {
            vetoableChangeSupport.fireVetoableChange(propName, old, current);
        }
        if (changeSupport != null) {
            changeSupport.firePropertyChange(propName, old, current);
        }
    }

    public void addValueConvertor(ValueConvertor conv) {
        synchronized (this) {
            if (convertors == null)
                convertors = new java.util.LinkedList<ValueConvertor>();
            else
                convertors.remove(conv);
            convertors.add(conv);
        }
    }

    public void removeValueConvertor(ValueConvertor conv) {
        synchronized (this) {
            if (convertors != null)
                convertors.remove(conv);
        }
    }

    protected Object convertValue(Object value) {
        if (convertors != null) {
            for (ValueConvertor conv : convertors) {
                Object val = conv.convert(value, this);
                if (val != value)
                    return val;
            }
        }
        return value;
    }

    // ----------------------------
    // private methods

    private Object checkCurrentValue()
        throws IllegalAccessException, InvocationTargetException
    {
        if (valueSet) {
            Object value = null;

            if (isExternalChangeMonitoring()) {
                value = getTargetValue();
                if (!equals(value, lastRealValue)) {
                    // the value is different than when we saw it last time
                    Object propValue = (propertyValue instanceof FormDesignValue) ?
                        ((FormDesignValue)propertyValue).getDesignValue() : propertyValue;
                    if (equals(value, propValue)) {
                        // Bug 236005 - after setting a property value, the value obtained from
                        // getter at that moment (and kept in lastRealValue) might be different.
                        // But later it may return to the value originally set (after some other
                        // property is set). In this case we should consider the property value
                        // still set (not changed externally - derived).
                        lastRealValue = value;
                    } else if (propValue != FormDesignValue.IGNORED_VALUE) {
                        valueSet = false;
                        setChanged(false);
                        lastRealValue = null;
                        return value;
                        // [fire property editor change - for refreshing property sheet??]
                    }
                }
            }
            return propertyValue;
        }
        return (propType & DETACHED_READ) == 0 ? getTargetValue() : null;
    }

    PropertyEditor findDefaultEditor() {
        PropertyEditor defaultEditor = getExpliciteEditor();
        if (defaultEditor != null)
            return defaultEditor;
        return FormPropertyEditorManager.findEditor(this);
    }

    // --------

    // [we could probably use org.openide.util.Utilities.compareObjects instead]
    private static boolean equals(Object obj1, Object obj2) {
        if (obj1 == obj2)
            return true;

        if (obj1 == null || obj2 == null)
            return false;

        Class cls1 = obj1.getClass();
        Class cls2 = obj2.getClass();

        if (!cls1.isArray() || !cls1.equals(cls2))
            return obj1.equals(obj2);

        // and this is what is special on this method - comparing arrays...
        Class cType = cls1.getComponentType();
        if (!cType.isPrimitive()) {
            Object[] array1 = (Object[]) obj1;
            Object[] array2 = (Object[]) obj2;
            if (array1.length != array2.length)
                return false;
            for (int i=0; i < array1.length; i++)
                if (!equals(array1[i], array2[i]))
                    return false;
            return true;
        }

        if (Integer.TYPE.equals(cType)) {
            int[] array1 = (int[]) obj1;
            int[] array2 = (int[]) obj2;
            if (array1.length != array2.length)
                return false;
            for (int i=0; i < array1.length; i++)
                if (array1[i] != array2[i])
                    return false;
            return true;
        }

        if (Boolean.TYPE.equals(cType)) {
            boolean[] array1 = (boolean[]) obj1;
            boolean[] array2 = (boolean[]) obj2;
            if (array1.length != array2.length)
                return false;
            for (int i=0; i < array1.length; i++)
                if (array1[i] != array2[i])
                    return false;
            return true;
        }

        if (Long.TYPE.equals(cType)) {
            long[] array1 = (long[]) obj1;
            long[] array2 = (long[]) obj2;
            if (array1.length != array2.length)
                return false;
            for (int i=0; i < array1.length; i++)
                if (array1[i] != array2[i])
                    return false;
            return true;
        }

        if (Double.TYPE.equals(cType)) {
            double[] array1 = (double[]) obj1;
            double[] array2 = (double[]) obj2;
            if (array1.length != array2.length)
                return false;
            for (int i=0; i < array1.length; i++)
                if (array1[i] != array2[i])
                    return false;
            return true;
        }

        if (Byte.TYPE.equals(cType)) {
            byte[] array1 = (byte[]) obj1;
            byte[] array2 = (byte[]) obj2;
            if (array1.length != array2.length)
                return false;
            for (int i=0; i < array1.length; i++)
                if (array1[i] != array2[i])
                    return false;
            return true;
        }

        if (Character.TYPE.equals(cType)) {
            char[] array1 = (char[]) obj1;
            char[] array2 = (char[]) obj2;
            if (array1.length != array2.length)
                return false;
            for (int i=0; i < array1.length; i++)
                if (array1[i] != array2[i])
                    return false;
            return true;
        }

        if (Float.TYPE.equals(cType)) {
            float[] array1 = (float[]) obj1;
            float[] array2 = (float[]) obj2;
            if (array1.length != array2.length)
                return false;
            for (int i=0; i < array1.length; i++)
                if (array1[i] != array2[i])
                    return false;
            return true;
        }

        if (Short.TYPE.equals(cType)) {
            short[] array1 = (short[]) obj1;
            short[] array2 = (short[]) obj2;
            if (array1.length != array2.length)
                return false;
            for (int i=0; i < array1.length; i++)
                if (array1[i] != array2[i])
                    return false;
            return true;
        }

        return false;
    }

/*    private void settleDesignValueListener(Object oldVal, Object newVal) {
        if (oldVal == newVal) return;

        if (oldVal instanceof FormDesignValue.Listener && designValueListener != null)
            ((FormDesignValue.Listener)oldVal).removeChangeListener(designValueListener);

        if (newVal instanceof FormDesignValue.Listener) {
            if (designValueListener == null)
                designValueListener = new DesignValueListener();
            ((FormDesignValue.Listener)newVal).addChangeListener(designValueListener);
        }
    }

    class DesignValueListener implements javax.swing.event.ChangeListener {
        public void stateChanged(javax.swing.event.ChangeEvent e) {
            if (valueSet && propertyValue == e.getSource())
                try {
                    setValue(propertyValue);
                }
                catch (Exception ex) { // can't do nothing here
                }
        }
    } */

    // -----

    /**
     * Convertor can be registered on a property and change value comming to
     * setValue method to something else. Used for automatic i18n.
     */
    public interface ValueConvertor {
        public Object convert(Object value, FormProperty property);
    }

    // ------------

    public static final class ValueWithEditor {
        private Object value;
        private PropertyEditor propertyEditor;
        private int propertyEditorIndex;
        private boolean editorSetByUser;

        public ValueWithEditor(Object value, PropertyEditor propertyEditor) {
            this.value = value;
            this.propertyEditor = propertyEditor;
        }

        ValueWithEditor(Object value, PropertyEditor propertyEditor, boolean editorSetByUser) {
            this(value, propertyEditor);
            this.editorSetByUser = editorSetByUser;
        }

        ValueWithEditor(Object value, int propertyEditorIndex, boolean editorSetByUser) {
            this.value = value;
            this.propertyEditorIndex = propertyEditorIndex;
            this.editorSetByUser = editorSetByUser;
        }

        public Object getValue() {
            return value;
        }

        public PropertyEditor getPropertyEditor() {
            return propertyEditor;
        }

        boolean getEditorSetByUser() {
            return editorSetByUser;
        }

        PropertyEditor getPropertyEditor(FormProperty property) {
            if (propertyEditor != null)
                return propertyEditor;
            if (propertyEditorIndex < 0)
                return null;

            PropertyEditor pe = property.getPropertyEditor();
            if (pe instanceof FormPropertyEditor) {
                FormPropertyEditor fpe = (FormPropertyEditor) pe;
                PropertyEditor[] allEds = fpe.getAllEditors();
                if (propertyEditorIndex < allEds.length)
                    return allEds[propertyEditorIndex];
            }

            return null;
        }
    }

    public static Object getEnclosedValue(Object value) {
        return value instanceof ValueWithEditor ? ((ValueWithEditor)value).getValue() : value;
    }

    // ------------

    public static interface Filter {
        public boolean accept(FormProperty property);
    }

    public static final Filter CHANGED_PROPERTY_FILTER = new Filter() {
        @Override
        public boolean accept(FormProperty property) {
            return property.isChanged();
        }
    };
}
