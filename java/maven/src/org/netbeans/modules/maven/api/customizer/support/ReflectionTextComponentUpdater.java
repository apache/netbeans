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

package org.netbeans.modules.maven.api.customizer.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMModel;

/**
 *
 * @author mkleint
 */
public final class ReflectionTextComponentUpdater extends TextComponentUpdater {
    private final Object model;
    private final Object defaults;
    private final Method modelgetter;
    private Method defgetter;
    private Method modelsetter;
    private ModelHandle handle;
    private ModelHandle2 handle2;
    private String initialValue2;
    private Operation operation;
    /** Creates a new instance of ReflectionTextComponentUpdater */
    public ReflectionTextComponentUpdater(String getter, String setter, Object model, Object defaults, JTextComponent field, JLabel label, ModelHandle handle) 
                        throws NoSuchMethodException {
        this(getter, setter, model, defaults, field, label);
        this.handle = handle;
        
    }
    
    public ReflectionTextComponentUpdater(String getter, Object model, Object defaults, JTextComponent field, JLabel label, ModelHandle2 handle, Operation operation) 
                        throws NoSuchMethodException {
        this(getter, null, model, defaults, field, label);
        assert handle != null;
        assert operation != null;
        this.operation = operation;
        this.handle2 = handle;
        initialValue2 = getValue();
    }
    
    
    private ReflectionTextComponentUpdater(String getter, String setter, Object model, Object defaults, JTextComponent field, JLabel label) 
                        throws NoSuchMethodException {
        super(field, label);
        this.model = model;
        this.defaults = defaults;
        modelgetter = model.getClass().getMethod(getter, new Class[0]);
        if (setter != null) { 
        modelsetter = model.getClass().getMethod(setter, new Class[] {String.class});
        }
        if (defaults != null) {
            defgetter = defaults.getClass().getMethod(getter, new Class[0]);
        }
    }
    
    
    @Override
    public String getValue() {
        if (operation.isValueSet) {
            return operation.getNewValue();
        }
        try {
            return (String)modelgetter.invoke(model, new Object[0]);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    @Override
    public String getDefaultValue() {
        if (defgetter == null) {
            return null;
        }
        try {
            return (String)defgetter.invoke(defaults, new Object[0]);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    @Override
    public void setValue(String value) {
        try {
            if (handle != null) {
                modelsetter.invoke(model, new Object[] { value });
                if (model instanceof POMComponent) {
                    handle.markAsModified(((POMComponent)model).getModel());
                } else {
                    handle.markAsModified(model);
                }
            }
            if (handle2 != null) {
                operation.setNewValue(value);
                if (value != null && value.equals(initialValue2)) {
                    handle2.removePOMModification(operation);
                } else {
                    //TODO ideally only add if not added before..
                    handle2.addPOMModification(operation);
                }
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    
    
    public abstract static class Operation implements ModelOperation<POMModel> {

        boolean isValueSet = false; 
        private String newValue;
        
        public final void setNewValue(String value) {
            newValue = value;
            isValueSet = true;
        }
        
        public final String getNewValue() {
            return newValue;
        }
        
        
    }
    
}
