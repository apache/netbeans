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
package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.MutableVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.models.ShortenedStrings;
import org.netbeans.modules.debugger.jpda.models.ShortenedStrings.StringInfo;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * Property editor of a variable, that delegates to the property editor
 * of the variable's mirror.
 *
 * @author Martin Entlicher
 */
class ValuePropertyEditor implements ExPropertyEditor {

    private static final Logger logger = Logger.getLogger(ValuePropertyEditor.class.getName());

    private static final Set<Class> CLASSES_2_IGNORE = new HashSet<>(Arrays.asList(new Class[] {
                                                           Object.class,
                                                           java.io.File.class
                                                       }));

    private final ContextProvider contextProvider;
    private PropertyEditor delegatePropertyEditor;
    private Class mirrorClass;
    private Object currentValue;
    private Object delegateValue;
    private PropertyEnv env;
    private final List<PropertyChangeListener> listeners = new ArrayList<>();
    private final Validate validate = new Validate();

    private static final Map<Class, Boolean> classesWithPE = Collections.synchronizedMap(new WeakHashMap<>());

    ValuePropertyEditor(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    static boolean hasPropertyEditorFor(Variable var) {
        if (!(var instanceof ObjectVariable)) {
            return false;
        }
        String type = var.getType();
        try {
            Class<?> clazz = Class.forName(type);
            //return PropertyEditorManager.findEditor(clazz) != null;
            return hasPropertyEditorFor(clazz);
        } catch (ExceptionInInitializerError eie) {
            return false;
        } catch (LinkageError | ClassNotFoundException le) {
            return false;
        }
    }

    private static boolean hasPropertyEditorFor(final Class clazz) {
        if (CLASSES_2_IGNORE.contains(clazz)) {
            return false;
        }
        // Cache which classes have PropertyEditor,
        // so that we do not have to wait for AWT event queue.
        Boolean hasPE = classesWithPE.get(clazz);
        if (hasPE != null) {
            return hasPE;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            return findThePropertyEditor(clazz) != null;
        } else {
            final boolean[] has = new boolean[] { false };
            try {
                SwingUtilities.invokeAndWait(() -> {
                    has[0] = findThePropertyEditor(clazz) != null;
                });
            } catch (InterruptedException | InvocationTargetException ex) {
            }
            return has[0];
        }
    }

    private static PropertyEditor findPropertyEditor(final Class clazz) {
        if (SwingUtilities.isEventDispatchThread()) {
            return findThePropertyEditor(clazz);
        } else {
            final PropertyEditor[] peRef = new PropertyEditor[] { null };
            try {
                SwingUtilities.invokeAndWait(() -> {
                    peRef[0] = findThePropertyEditor(clazz);
                });
            } catch (InterruptedException | InvocationTargetException ex) {
            }
            return peRef[0];
        }
    }

    private static PropertyEditor findThePropertyEditor(Class clazz) {
        PropertyEditor pe;
        if (Object.class.equals(clazz)) {
            pe = null;
        } else {
            if(clazz == StringInfo.class) {
                pe = PropertyEditorManager.findEditor(String.class);
            } else {
                pe = PropertyEditorManager.findEditor(clazz);
            }
            if (pe == null) {
                Class sclazz = clazz.getSuperclass();
                if (sclazz != null) {
                    pe = findPropertyEditor(sclazz);
                }
            }
        }
        classesWithPE.put(clazz, pe != null);
        return pe;
    }

    /**
     * Test if the property editor can act on the provided value. We can never be sure. :-(
     * @param propertyEditor
     * @param valueMirror
     * @return the property editor, or <code>null</code>
     */
    private static PropertyEditor testPropertyEditorOnValue(PropertyEditor propertyEditor, Object valueMirror) {
        propertyEditor.setValue(valueMirror);
        Object value = propertyEditor.getValue();
        if (value != valueMirror && (value == null || !value.equals(valueMirror))) {
            // Returns something that we did not set. Give up.
            return null;
        }
        return propertyEditor;
    }

    @Override
    public void setValue(Object value) {
        logger.log(Level.FINE, "ValuePropertyEditor.setValue({0})", value);
        /*
        if (delegatePropertyEditor != null) {
            for (PropertyChangeListener l : listeners) {
                delegatePropertyEditor.removePropertyChangeListener(l);
            }
        }*/
        if (currentValue == value) {
            return ;
        }
        this.currentValue = value;
        Class clazz;
        Object valueMirror;
        if (value instanceof String) {
            clazz = String.class;
            valueMirror = value;
        } else if (value instanceof Variable var) {
            valueMirror = VariablesTableModel.getMirrorFor(var);
            if (valueMirror != null) {
                clazz = valueMirror.getClass();
            } else {
                clazz = String.class;
                valueMirror = VariablesTableModel.getValueOf(var);
            }
        } else {
            throw new IllegalArgumentException(String.valueOf(value));
        }
        boolean doAttach = false;
        if (delegatePropertyEditor == null || clazz != mirrorClass) {
            if (delegatePropertyEditor != null) {
                for (PropertyChangeListener l : listeners) {
                    delegatePropertyEditor.removePropertyChangeListener(l);
                }
            }
            PropertyEditor propertyEditor = findPropertyEditor(clazz);
            propertyEditor = testPropertyEditorOnValue(propertyEditor, valueMirror);
            if (propertyEditor == null) {
                clazz = String.class;
                propertyEditor = findPropertyEditor(clazz);
                valueMirror = VariablesTableModel.getValueOf((Variable) value);
            }
            mirrorClass = clazz;
            delegatePropertyEditor = propertyEditor;
            if (env != null && propertyEditor instanceof ExPropertyEditor) {
                doAttach = true;
            }
            for (PropertyChangeListener l : listeners) {
                delegatePropertyEditor.addPropertyChangeListener(l);
            }
        }
        delegateValue = valueMirror;
        delegatePropertyEditor.setValue(valueMirror);
        if (doAttach) {
            ((ExPropertyEditor) delegatePropertyEditor).attachEnv(env);
        }
        logger.log(Level.FINE, "  delegatePropertyEditor = {0}", delegatePropertyEditor);
        /*for (PropertyChangeListener l : listeners) {
            delegatePropertyEditor.addPropertyChangeListener(l);
        }*/
    }

    boolean setValueWithMirror(Object value, Object valueMirror) {
        this.currentValue = value;
        Class<?> clazz = valueMirror.getClass();
        PropertyEditor propertyEditor = findPropertyEditor(clazz);
        propertyEditor = testPropertyEditorOnValue(propertyEditor, valueMirror);
        if (propertyEditor == null) {
            return false;
        }
        boolean doAttach = false;
        mirrorClass = clazz;
        delegatePropertyEditor = propertyEditor;
        if (env != null && propertyEditor instanceof ExPropertyEditor) {
            doAttach = true;
        }
        delegateValue = valueMirror;
        delegatePropertyEditor.setValue(valueMirror);
        if (doAttach) {
            ((ExPropertyEditor) delegatePropertyEditor).attachEnv(env);
        }
        return doAttach;
    }

    @Override
    public Object getValue() {
        if (delegatePropertyEditor == null) {
            logger.log(Level.FINE, "ValuePropertyEditor.getValue() = (null) {0}", currentValue);
            return currentValue;
        }
        Object dpeValue = delegatePropertyEditor.getValue();
        if (dpeValue instanceof String) {//!(currentValue instanceof Variable)) {
            logger.log(Level.FINE, "ValuePropertyEditor.getValue() = (delegate''s) {0}", dpeValue);
            return dpeValue;
        } else {
            // Set the value from delegatePropertyEditor when vetoableChange is fired.
            logger.log(Level.FINE, "ValuePropertyEditor.getValue() = (current) {0}", currentValue);
            return currentValue;
        }
    }

    private void setVarFromMirror(final MutableVariable var, final Object mirror) {
        Runnable run = () -> {
            String javaInitStr = delegatePropertyEditor.getJavaInitializationString();
            boolean setFromMirror = false;
            try {
                var.setValue(javaInitStr);
                if (mirror == null || ((JDIVariable) var).getJDIValue() != null) {
                    setFromMirror = true;
                } // false when mirror != null and JDI value is null (set value was not successful)
            } catch (InvalidExpressionException ex) {
                logger.log(Level.INFO, "InvalidExpressionException when evaluating "+javaInitStr+":", ex);
            }
            if (!setFromMirror) {
                try {
                    var.setFromMirrorObject(mirror);
                } catch (InvalidObjectException ioex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ioex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor rp = contextProvider.lookupFirst(null, RequestProcessor.class);
            rp.post(run);
        } else {
            run.run();
        }
    }

    @Override
    public boolean isPaintable() {
        //System.out.println("ValuePropertyEditor.isPaintable("+delegatePropertyEditor+")");
        return delegatePropertyEditor != null && delegatePropertyEditor.isPaintable();
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        delegatePropertyEditor.paintValue(gfx, box);
    }

    @Override
    public String getJavaInitializationString() {
        return delegatePropertyEditor.getJavaInitializationString();
    }

    @Override
    public String getAsText() {
        return delegatePropertyEditor.getAsText();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        logger.log(Level.FINE, "ValuePropertyEditor.setAsText({0}) calling {1}",
                   new Object[]{text, delegatePropertyEditor});
        delegatePropertyEditor.setAsText(text);
    }

    @Override
    public String[] getTags() {
        return delegatePropertyEditor.getTags();
    }

    @Override
    public Component getCustomEditor() {
        //System.err.println("ValuePropertyEditor.getCustomEditor() delegateValue = "+delegateValue);
        if (delegateValue instanceof StringInfo si) {
            BigStringCustomEditor bsce = BigStringCustomEditor.createIfBig(delegatePropertyEditor, si);
            if (bsce != null) {
                return bsce;
            }
        }
        return delegatePropertyEditor.getCustomEditor();
    }

    @Override
    public boolean supportsCustomEditor() {
        logger.log(Level.FINE, "ValuePropertyEditor.supportsCustomEditor({0})",
                   delegatePropertyEditor);
        return delegatePropertyEditor != null && delegatePropertyEditor.supportsCustomEditor();
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        //System.out.println("ValuePropertyEditor.attachEnv("+env+"), feature descriptor = "+env.getFeatureDescriptor());
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addVetoableChangeListener(validate);
        if (delegatePropertyEditor instanceof ExPropertyEditor exPropertyEditor) {
            //System.out.println("  attaches to "+delegatePropertyEditor);
            if (delegateValue instanceof StringInfo si) {
                // The value is too large, do not allow editing!
                FeatureDescriptor desc = env.getFeatureDescriptor();
                if (desc instanceof Node.Property prop){
                    // Need to make it uneditable
                    try {
                        Method forceNotEditableMethod = prop.getClass().getDeclaredMethod("forceNotEditable");
                        forceNotEditableMethod.setAccessible(true);
                        forceNotEditableMethod.invoke(prop);
                    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | RuntimeException ex){}
                    //editable = prop.canWrite();
                }
            }
            exPropertyEditor.attachEnv(env);
            this.env = env;
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
        logger.log(Level.FINE, "ValuePropertyEditor.addPropertyChangeListener({0})", listener);
        delegatePropertyEditor.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
        logger.log(Level.FINE, "ValuePropertyEditor.removePropertyChangeListener({0})", listener);
        delegatePropertyEditor.removePropertyChangeListener(listener);
    }

    private class Validate implements VetoableChangeListener {

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            logger.log(Level.FINE, "ValuePropertyEditor.Validate.vetoableChange({0})", evt);
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())) {
                Object newValue = delegatePropertyEditor.getValue();
                logger.log(Level.FINE, "  vetoableChange: delegate PE value = {0}", newValue);
                if (newValue != delegateValue && !(newValue instanceof String)) {
                    setVarFromMirror(((MutableVariable) currentValue), newValue);
                    /*
                    try {
                        ((MutableVariable) currentValue).setFromMirrorObject(newValue);
                    } catch (InvalidObjectException ex) {
                        throw new PropertyVetoException(ex.getLocalizedMessage(), evt);
                    }
                    */
                }
            }
        }

    }

}
