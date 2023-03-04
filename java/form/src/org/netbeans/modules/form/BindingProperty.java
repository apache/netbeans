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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node;

import org.openide.nodes.PropertySupport;

public class BindingProperty extends PropertySupport.ReadWrite<MetaBinding> {
    public static final String PROP_NAME = "name"; // NOI18N
    public static final String PROP_NULL_VALUE = "nullValue"; // NOI18N
    public static final String PROP_INCOMPLETE_VALUE = "incompleteValue"; // NOI18N
    public static final String PROP_VALIDATOR = "validator"; // NOI18N
    public static final String PROP_CONVERTER = "converter"; // NOI18N
    private RADComponent bindingComponent;
    private BindingDescriptor bindingDescriptor;
    private MetaBinding binding;
    private Property nameProperty;
    private Property nullValueProperty;
    private Property incompleteValueProperty;
    private Property validatorProperty;
    private Property converterProperty;

    public BindingProperty(RADComponent metacomp, BindingDescriptor desc) {
        super(desc.getPath(), MetaBinding.class, desc.getDisplayName(), FormProperty.getDescriptionWithType(desc.getShortDescription(), desc.getValueType()));
        bindingComponent = metacomp;
        bindingDescriptor = desc;
        FormProperty prop = (FormProperty)bindingComponent.getPropertyByName(bindingDescriptor.getPath());
        if (prop == null) {
            // Can we have a component with a binding property and no regular property?
            RADProperty[] props = bindingComponent.getAllBeanProperties();
            if (props.length > 0) {
                prop = props[0];
            }
        }
        if (prop != null) {
            BindingDesignSupport bindingSupport = FormEditor.getBindingSupport(getFormModel());
            String name = FormUtils.getBundleString("MSG_Binding_NullProperty"); // NOI18N
            nullValueProperty = new Property(prop, "nullValue", desc.getValueType(), name, name, false); // NOI18N
            name = FormUtils.getBundleString("MSG_Binding_IncompletePathProperty"); // NOI18N
            incompleteValueProperty = new Property(prop, "incompletePathValue", desc.getValueType(), name, name, false); // NOI18N
            name = FormUtils.getBundleString("MSG_Binding_Validator"); // NOI18N
            validatorProperty = new Property(prop, "validator", bindingSupport.getValidatorClass(), name, name, true); // NOI18N
            validatorProperty.setValue("canAutoComplete", Boolean.FALSE); // NOI18N
            name = FormUtils.getBundleString("MSG_Binding_Converter"); // NOI18N
            converterProperty = new Property(prop, "converter", bindingSupport.getConverterClass(), name, name, true); // NOI18N
            converterProperty.setValue("canAutoComplete", Boolean.FALSE); // NOI18N
            name = FormUtils.getBundleString("MSG_Binding_Name"); // NOI18N
            nameProperty = new Property(prop, "name", String.class, name, name, true); // NOI18N
        }
    }

    @Override
    public String getHtmlDisplayName() {
        return binding != null ? "<b>" + getDisplayName() : null; // NOI18N
    }

    @Override
    public MetaBinding getValue() {
        return binding;
    }

    @Override
    public void setValue(MetaBinding val) {
        MetaBinding old = binding;
        if ((old == null) && (val != null)) {
            FormEditor.updateProjectForBeansBinding(bindingComponent.getFormModel());
            getFormModel().raiseVersionLevel(FormModel.FormVersion.NB60, FormModel.FormVersion.NB60);
            // To make sure that undo restores value of the corresponding property correctly 
            Node.Property prop = bindingComponent.getPropertyByName(bindingDescriptor.getPath());
            if (prop instanceof FormProperty) {
                FormProperty fprop = (FormProperty)prop;
                if (fprop.isChanged()) {
                    try {
                        fprop.restoreDefaultValue();
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            } else {
                try {
                    prop.restoreDefaultValue();
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
        if ((val != null) && !bindingComponent.equals(val.getTarget())) {
            // Issue 141494 - component multiselection
            MetaBinding clone = new MetaBinding(val.getSource(), val.getSourcePath(), bindingComponent, val.getTargetPath());
            clone.setBindImmediately(val.isBindImmediately());
            clone.setIncompletePathValueSpecified(val.isIncompletePathValueSpecified());
            clone.setNullValueSpecified(val.isNullValueSpecified());
            clone.setUpdateStrategy(val.getUpdateStrategy());
            Map<String,String> params = val.getParameters();
            for (Map.Entry<String,String> entry : params.entrySet()) {
                clone.setParameter(entry.getKey(), entry.getValue());
            }
            Collection<MetaBinding> subs = val.getSubBindings();
            if (subs != null) {
                for (MetaBinding sub : subs) {
                    clone.addSubBinding(sub.getSourcePath(), sub.getTargetPath());
                }
            }
            RADComponent comp = val.getTarget();
            BindingProperty original = comp.getBindingProperty(getName());
            copy(original.getNameProperty(), getNameProperty());
            copy(original.getConverterProperty(), getConverterProperty());
            copy(original.getValidatorProperty(), getValidatorProperty());
            if (val.isNullValueSpecified()) {
                copy(original.getNullValueProperty(), getNullValueProperty());
            }
            if (val.isIncompletePathValueSpecified()) {
                copy(original.getIncompleteValueProperty(), getIncompleteValueProperty());
            }
            val = clone;
        }
        binding = val;
        FormEditor.getBindingSupport(getFormModel()).changeBindingInModel(old, binding);

        getFormModel().fireBindingChanged(
                getBindingComponent(), getBindingPath(), null, old, binding);
        RADComponentNode node = getBindingComponent().getNodeReference();
        if (node != null) {
            node.firePropertyChangeHelper(
                null, null, null); // this will cause resetting the bean property (e.g. JTable.model)
        }
    }

    private void copy(FormProperty src, FormProperty dst) {
        try {
            PropertyEditor propEd = src.getCurrentEditor();
            Object value = src.getValue();
            dst.setValue(new FormProperty.ValueWithEditor(value, propEd));
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public void restoreDefaultValue() {
        setValue(null);
        try {
            validatorProperty.restoreDefaultValue();
            converterProperty.restoreDefaultValue();
            nameProperty.restoreDefaultValue();
            nullValueProperty.setValue(null);
            incompleteValueProperty.setValue(null);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        Node.Property prop = bindingComponent.getPropertyByName(bindingDescriptor.getPath());
        if ((prop != null) && prop.supportsDefaultValue()) {
            try {
                prop.restoreDefaultValue();
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }
    }

    @Override
    public boolean isDefaultValue() {
        return (getValue() == null);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new BindingPropertyEditor();
    }

    public RADComponent getBindingComponent() {
        return bindingComponent;
    }

    public BindingDescriptor getBindingDescriptor() {
        return bindingDescriptor;
    }

    String getBindingPath() {
        return bindingDescriptor.getPath();
    }

    Class getBindingValueType() {
        return bindingDescriptor.getValueType();
    }

    private FormModel getFormModel() {
        return bindingComponent.getFormModel();
    }

    public FormProperty getNullValueProperty() {
        return nullValueProperty;
    }

    public FormProperty getIncompleteValueProperty() {
        return incompleteValueProperty;
    }

    public FormProperty getValidatorProperty() {
        return validatorProperty;
    }

    public FormProperty getConverterProperty() {
        return converterProperty;
    }

    public FormProperty getNameProperty() {
        return nameProperty;
    }

    public FormProperty getSubProperty(String propName) {
        if (PROP_NAME.equals(propName)) {
            return getNameProperty();
        } else if (PROP_NULL_VALUE.equals(propName)) {
            return getNullValueProperty();
        } else if (PROP_INCOMPLETE_VALUE.equals(propName)) {
            return getIncompleteValueProperty();
        } else if (PROP_CONVERTER.equals(propName)) {
            return getConverterProperty();
        } else if (PROP_VALIDATOR.equals(propName)) {
            return getValidatorProperty();
        } else {
            return null;
        }
    }

    // -----

    private class BindingPropertyEditor extends PropertyEditorSupport { //implements ExPropertyEditor

        private BindingCustomizer customizer;
        private ActionListener customizerListener;

        @Override
        public String getAsText() {
            RADComponent boundComp = null;
            String path = null;
            if (binding != null) {
                boundComp = binding.getSource();
                path = binding.getSourcePath();
            }

            if (boundComp == null)
                return ""; // NOI18N

            return path != null ?
                   boundComp.getName() + "[" + path + "]" : // NOI18N
                   boundComp.getName();
        }

        @Override
        public void setAsText(String text) {
            if ("".equals(text)) { // NOI18N
                setValue(null);
            }
            else {
                int idx = text.indexOf('['); // NOI18N
                String compName = idx >= 0 ? text.substring(0, idx) : text;
                RADComponent boundComp = getFormModel().findRADComponent(compName);
                if (boundComp != null) {
                    String path = idx >= 0 ? text.substring(idx+1, text.length()-1) : ""; // NOI18N
                    if (!path.equals("")) { // NOI18N
                        if (boundComp != getBindingComponent() || !path.equals(getBindingPath())) {
                            setValue(new MetaBinding(boundComp, path, getBindingComponent(), getBindingPath()));
                        }
                    }
                    else if (boundComp != getBindingComponent()
                             && Collection.class.isAssignableFrom(getBindingValueType())
                             && getBindingValueType().equals(boundComp.getBeanClass()))
                    {   // bind directly to the component
                        setValue(new MetaBinding(boundComp, null, getBindingComponent(), getBindingPath()));
                    }
                }
            }
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            if (customizer == null) {
                customizer = new BindingCustomizer(BindingProperty.this);
                customizerListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        setValue(customizer.getBinding());
                    }
                };
            }
            customizer.setBinding((MetaBinding)getValue());
            return customizer.getDialog(customizerListener);
        }

    }
    
    static class Property extends FormProperty {
        private Object value;
        private boolean supportsDefaultValue;

        Property(FormProperty prop, String name, Class type, String displayName, String description, boolean supportsDefaultValue) {
            // PENDING override getContextPath
            super(new FormPropertyContext.SubProperty(prop), name, type, displayName, description);
            this.supportsDefaultValue = supportsDefaultValue;
        }

        @Override
        public Object getTargetValue() throws IllegalAccessException, InvocationTargetException {
            if (getValueType().equals(boolean.class) && (value==null)) {
                return false;
            }
            return value;
        }

        @Override
        public void setTargetValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            this.value = value;
        }

        @Override
        public boolean supportsDefaultValue () {
            return supportsDefaultValue;
        }

        @Override
        public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
            super.restoreDefaultValue();
            getCurrentEditor().setValue(getValue());
        }

    }
    
}
