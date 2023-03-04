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

import java.awt.*;
import java.beans.*;
import java.lang.ref.WeakReference;
import java.security.*;

import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;

/** A multiplexing PropertyEditor used in the form editor.
 * It allows multiple editors to be used with one currently selected.
 *
 * @author Ian Formanek
 */

public class FormPropertyEditor implements PropertyEditor,
                                           PropertyChangeListener,
                                           ExPropertyEditor
{
    private static String NO_VALUE_TEXT;

    private Object value = BeanSupport.NO_VALUE;
    private boolean valueEdited;
    private static final boolean SUPPRESS_FORM_EDITORS = 
            Boolean.getBoolean("nb.form.suppress.editors"); //NOI18N

    private FormProperty property;
    private WeakReference<PropertyEnv> propertyEnv;

    private PropertyEditor[] allEditors;
    private PropertyEditor lastCurrentEditor;

    private PropertyChangeSupport changeSupport;
    
    /** Crates a new FormPropertyEditor */
    FormPropertyEditor(FormProperty property) {
        this.property = property;
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null) {
            prEd.addPropertyChangeListener(this); // [do we really need to listen to this editor??]
            value = prEd.getValue();
        }
    }

    Class getPropertyType() {
        return property.getValueType();
    }

    FormProperty getProperty() {
        return property;
    }

    FormPropertyContext getPropertyContext() {
        return property.getPropertyContext();
    }

    PropertyEnv getPropertyEnv() {
        return propertyEnv != null ? propertyEnv.get() : null;
    }

    PropertyEditor getCurrentEditor() {
        return property.getCurrentEditor();
    }

    // -----------------------------------------------------------------------------
    // PropertyChangeListener implementation

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null) {
            value = prEd.getValue();
            valueEdited = false;
        }

        // we run this as privileged to avoid security problems - because
        // the property change can be fired from untrusted property editor code
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                FormPropertyEditor.this.firePropertyChange();
                return null;
            }
        });
    }

    // -----------------------------------------------------------------------------
    // PropertyEditor implementation

    /**
     * Set(or change) the object that is to be edited.
     * @param newValue The new target object to be edited.  Note that this
     *     object should not be modified by the PropertyEditor, rather 
     *     the PropertyEditor should create a new object to hold any
     *     modified value.
     */
    @Override
    public void setValue(Object newValue) {
        value = newValue;
        valueEdited = false;

        PropertyEditor prEd = property.getCurrentEditor();
        if (value != BeanSupport.NO_VALUE && prEd != null)
            prEd.setValue(value);
    }

    void setEditedValue(Object newValue) {
        value = newValue;
        // the value comes from custom editing where the selected editor can be
        // different at this moment than the current editor of the edited property
        valueEdited = true;
        firePropertyChange();
    }

    /**
     * Gets the value of the property.
     *
     * @return The value of the property.
     */
    @Override
    public Object getValue() {
        if (!valueEdited) {
            PropertyEditor prEd = property.getCurrentEditor();
            if (prEd != null) {
                return prEd.getValue();
            }
        }
        return value;
    }

    // -----------------------------------------------------------------------------

    /**
     * Determines whether the class will honor the painValue method.
     *
     * @return  True if the class will honor the paintValue method.
     */
    @Override
    public boolean isPaintable() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.isPaintable() : false;
    }

    /**
     * Paint a representation of the value into a given area of screen
     * real estate.  Note that the propertyEditor is responsible for doing
     * its own clipping so that it fits into the given rectangle.
     * <p>
     * If the PropertyEditor doesn't honor paint requests(see isPaintable)
     * this method should be a silent noop.
     *
     * @param gfx  Graphics object to paint into.
     * @param box  Rectangle within graphics object into which we should paint.
     */
    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null)
            prEd.paintValue(gfx, box);
    }

    // -----------------------------------------------------------------------------

    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *   	current value.
     */
    @Override
    public String getJavaInitializationString() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getJavaInitializationString() : null;
    }

    // -----------------------------------------------------------------------------

    /**
     * Gets the property value as a string suitable for presentation
     * to a human to edit.
     *
     * @return The property value as a string suitable for presentation
     *       to a human to edit.
     * <p>   Returns "null" is the value can't be expressed as a string.
     * <p>   If a non-null value is returned, then the PropertyEditor should
     *	     be prepared to parse that string back in setAsText().
     */
    @Override
    public String getAsText() {
        if (value == BeanSupport.NO_VALUE) {
            if (NO_VALUE_TEXT == null)
                NO_VALUE_TEXT = FormUtils.getBundleString("CTL_ValueNotSet"); // NOI18N
            return NO_VALUE_TEXT;
        }

        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getAsText() : null;
    }

    /**
     * Sets the property value by parsing a given String.  May raise
     * java.lang.IllegalArgumentException if either the String is
     * badly formatted or if this kind of property can't be expressed
     * as text.
     *
     * @param text  The string to be parsed.
     * @throws java.lang.IllegalArgumentException when the specified text
     * does not represent valid value.
     */
    @Override
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null)
            prEd.setAsText(text);
    }

    // -----------------------------------------------------------------------------

    /**
     * If the property value must be one of a set of known tagged values, 
     * then this method should return an array of the tag values.  This can
     * be used to represent(for example) enum values.  If a PropertyEditor
     * supports tags, then it should support the use of setAsText with
     * a tag value as a way of setting the value.
     *
     * @return The tag values for this property.  May be null if this 
     *   property cannot be represented as a tagged value.
     *	
     */
    @Override
    public String[] getTags() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.getTags() : null;
    }

    // -----------------------------------------------------------------------------

    /**
     * A PropertyEditor may chose to make available a full custom Component
     * that edits its property value.  It is the responsibility of the
     * PropertyEditor to hook itself up to its editor Component itself and
     * to report property value changes by firing a PropertyChange event.
     * <P>
     * The higher-level code that calls getCustomEditor may either embed
     * the Component in some larger property sheet, or it may put it in
     * its own individual dialog, or ...
     *
     * @return A java.awt.Component that will allow a human to directly
     *      edit the current property value.  May be null if this is
     *	    not supported.
     */

    @Override
    public Component getCustomEditor() {
        // hack: PropertyPicker wants code regenerated - it might lead to
        // setting values to property editors
        FormModel formModel = property.getPropertyContext().getFormModel();
        if (formModel != null) {
            JavaCodeGenerator codeGen = (JavaCodeGenerator) FormEditor.getCodeGenerator(formModel);
            if (codeGen != null) { // may happen property sheet wants something from an already closed form (#111205)
                codeGen.regenerateCode();
            }
        }

        Component customEditor;

        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd != null && prEd.supportsCustomEditor()) {
            customEditor = prEd.getCustomEditor();
            if (customEditor instanceof Window)
                return customEditor;
        }
        else customEditor = null;

        return new FormCustomEditor(this, customEditor);
    }

    /**
     * Determines whether the propertyEditor can provide a custom editor.
     *
     * @return  True if the propertyEditor can provide a custom editor.
     */
    @Override
    public boolean supportsCustomEditor() {
        PropertyEditor[] editors = getAllEditors();

        if (!property.canWrite()) { // read only property
            for (int i=0; i < editors.length; i++)
                if (!editors[i].getClass().equals(RADConnectionPropertyEditor.class)
                        && editors[i].supportsCustomEditor())
                    return true;
            return false;
        }

        // writable property
        if (editors.length > 1)
            return true; // we must  at least allow to choose the editor
        if (editors.length == 1)
            return editors[0].supportsCustomEditor();

        return false;
    }

    synchronized PropertyEditor[] getAllEditors() {
        if (SUPPRESS_FORM_EDITORS) {
            return new PropertyEditor[] { property.getCurrentEditor() };
        }
        if (allEditors != null) {
            // the current property editor might have changed and so not
            // present among the cached editors
            PropertyEditor currentEditor = property.getCurrentEditor();
            if (currentEditor != lastCurrentEditor) {
                allEditors = null;
            }
        }

        if (allEditors == null) {
            PropertyEditor expliciteEditor = property.getExpliciteEditor();
            PropertyEditor currentEditor = property.getCurrentEditor();
            lastCurrentEditor = currentEditor;
            if (expliciteEditor != null && currentEditor != null
                    && expliciteEditor.getClass().equals(currentEditor.getClass()))
            {   // they are the same, take care about the current editor only
                expliciteEditor = null;
            }
            PropertyEditor[] typeEditors = FormPropertyEditorManager.getAllEditors(property);

            // Explicite editor should be added to editors (if not already present).
            // The current editor should replace the corresponding default editor.
            // Replace the delegate editor in ResourceWrapperEditor if needed.
            for (int i=0; i < typeEditors.length && (expliciteEditor != null || currentEditor != null); i++) {
                PropertyEditor prEd = typeEditors[i];
                ResourceWrapperEditor wrapper = null;
                if (prEd instanceof ResourceWrapperEditor && !(currentEditor instanceof ResourceWrapperEditor)) {
                    // the current editor might be just loaded and thus not wrapped...
                    wrapper = (ResourceWrapperEditor) prEd;
                    prEd = wrapper.getDelegatedPropertyEditor();
                }
                if (currentEditor != null && currentEditor.getClass().equals(prEd.getClass())) {
                    // current editor matches
                    if (wrapper != null) { // silently make it the current editor
                        wrapper.setDelegatedPropertyEditor(currentEditor);
                        boolean fire = property.isChangeFiring();
                        property.setChangeFiring(false);
                        property.setCurrentEditor(wrapper);
                        property.setChangeFiring(fire);
                        PropertyEnv env = getPropertyEnv();
                        if (env != null)
                            wrapper.attachEnv(env);
                    }
                    else {
                        if (prEd instanceof RADConnectionPropertyEditor
                            && ((RADConnectionPropertyEditor)prEd).getEditorType()
                                != ((RADConnectionPropertyEditor)currentEditor).getEditorType()) {
                            continue; // there are two types of RAD... editors
                        }
                        typeEditors[i] = currentEditor;
                    }
                    currentEditor = null;
                }
                else if (expliciteEditor != null && expliciteEditor.getClass().equals(prEd.getClass())) {
                    if (wrapper != null)
                        wrapper.setDelegatedPropertyEditor(expliciteEditor);
                    else
                        typeEditors[i] = expliciteEditor;
                    expliciteEditor = null;
                }
            }

            int count = typeEditors.length;
            if (expliciteEditor != null)
                count++;
            if (currentEditor != null)
                count++;
            if (count > typeEditors.length) {
                allEditors = new PropertyEditor[count];
                int index = 0;
                if (currentEditor != null)
                    allEditors[index++] = currentEditor;
                if (expliciteEditor != null)
                    allEditors[index++] = expliciteEditor;
                System.arraycopy(typeEditors, 0, allEditors, index, typeEditors.length);
            }
            else allEditors = typeEditors;
        }
        return allEditors;
    }

    // -------------------------------------------------------------
    // FormPropertyContainer implementation
    
//    public Node.Property[] getProperties() {
//        if (modifiedEditor instanceof FormPropertyContainer)
//            return ((FormPropertyContainer)modifiedEditor).getProperties();
//        else
//            return null;
//    }

    // -----------------------------------------------------------------------------

    /**
     * Register a listener for the PropertyChange event.  The class will
     * fire a PropertyChange value whenever the value is updated.
     *
     * @param l An object to be invoked when a PropertyChange event is fired.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    /**
     * Remove a listener for the PropertyChange event.
     *
     * @param l The PropertyChange listener to be removed.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }

    /**
     * Report that we have been modified to any interested listeners.
     */
    void firePropertyChange() {
        if (changeSupport != null)
            changeSupport.firePropertyChange(null, null, null);
    }

    // -------------
    // ExPropertyEditor implementation

    /** 
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * 
     * @param env environment.
     */
    @Override
    public void attachEnv(PropertyEnv env) {
        propertyEnv = new WeakReference<PropertyEnv>(env);
        PropertyEditor prEd = property.getCurrentEditor();
        if (prEd instanceof ExPropertyEditor)
            ((ExPropertyEditor)prEd).attachEnv(env);
    }

    // ---------
    // delegating hashCode() and equals(Object) methods to modifiedEditor - for
    // PropertyPanel mapping property editors to PropertyEnv

    @Override
    public int hashCode() {
        PropertyEditor prEd = property.getCurrentEditor();
        return prEd != null ? prEd.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null ? hashCode() == obj.hashCode() : false;
    }
}
