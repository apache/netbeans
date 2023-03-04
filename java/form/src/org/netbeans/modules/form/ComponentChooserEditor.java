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
import java.util.*;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;

/**
 * Property editor allowing to choose a component from all components in form
 * (FormModel). Choice can be restricted to certain bean types.
 *
 * @author Tomas Pavek
 */

public class ComponentChooserEditor implements PropertyEditor,
                                               FormAwareEditor,
                                               XMLPropertyEditor,
                                               NamedPropertyEditor
{
    public static final int ALL_COMPONENTS = 0;
    public static final int NONVISUAL_COMPONENTS = 3;
//    public static final int VISUAL_COMPONENTS = 1;
//    public static final int OTHER_COMPONENTS = 2;

    private static final String NULL_REF = "null"; // NOI18N
    private static final String INVALID_REF = "default"; // NOI18N

    private static String noneText = null;
    private static String invalidText = null;
    private static String defaultText = null;

    private FormModel formModel;
    private FormProperty property;
    private List<RADComponent> components;
    private Class[] beanTypes = null;
    private int componentCategory = 0;

    private Object defaultValue;
    private ComponentRef value;

    private PropertyChangeSupport changeSupport;

    public ComponentChooserEditor() {
    }

    public ComponentChooserEditor(Class[] componentTypes) {
        beanTypes = componentTypes;
    }

    // --------------
    // PropertyEditor implementation

    @Override
    public void setValue(Object value) {
        defaultValue = null;
        if (value == null || value instanceof ComponentRef)
            this.value = (ComponentRef) value;
        
        else if (value instanceof RADComponent)
            this.value = new ComponentRef((RADComponent)value);
        else if (value instanceof String)
            this.value = new ComponentRef((String)value);
        else {
            this.value = null;
            defaultValue = value;
        }    
            
            //return;

        firePropertyChange();
    }

    @Override
    public Object getValue() {
        if (value != null && INVALID_REF.equals(value.getDescription()))
            return BeanSupport.NO_VALUE; // special - invalid value was loaded
        
        return isDefaultValue() ? FormProperty.DEFAULT_VALUE : value; 
    }

    @Override
    public String[] getTags() {
        List compList = getComponents();

        int extraValues;        
        int count;
        String[] names;                                    

        boolean includeNone = shouldIncludeNone();
        if( isDefaultValue() ) {
            extraValues = includeNone ? 2 : 1;
            count = compList.size() + extraValues;
            names = new String[count];                                    
            names[0] = defaultString();            
        } else {
            extraValues = includeNone ? 1 : 0;
            count = compList.size() + extraValues;
            names = new String[count];                                                
        } 
        if (includeNone) {
            names[extraValues - 1] = noneString();
        }

        if (count > extraValues) {
            for (int i=extraValues; i < count; i++)
                names[i] = ((RADComponent)compList.get(i-extraValues)).getName();
            Arrays.sort(names, 1, count);
        }

        return names;
    }

    /**
     * Determines whether none/null value should be offered by this
     * property editor.
     * 
     * @return {@code true} if none/null value should be offered
     * or {@code false} otherwise.
     */
    private boolean shouldIncludeNone() {
        // Do not include null/none for some properties that cannot handle it
        boolean include = true;
        if (property instanceof RADProperty) {
            RADProperty radProperty = (RADProperty)property;
            String propName = radProperty.getName();
            RADComponent metacomp = radProperty.getRADComponent();
            Object instance = metacomp.getBeanInstance();
            if (((instance instanceof javax.swing.text.JTextComponent)
                    && ("caret".equals(propName) || "document".equals(propName))) // NOI18N
                    || ((instance instanceof javax.swing.AbstractButton)
                    && "model".equals(propName))) { // NOI18N
                include = false;
            }
        }
        return include;
    }

    private boolean isDefaultValue() {
        return value == null && defaultValue != null;
    }    
    
    @Override
    public String getAsText() {
        if (isDefaultValue())
            return defaultString();
        if (value == null)
            return noneString();
        if (value.getComponent() == null)
            return invalidString();

        String str = value.getDescription();
        return NULL_REF.equals(str) ? noneString() : str;
    }

    @Override
    public void setAsText(String str) {
        if (str == null || str.equals("") || str.equals(noneString())) // NOI18N
            setValue(null);
        else {
            if(defaultString().equals(str)) {           
                // XXX 
                setValue(defaultValue);
            } else {
                setValue(str);    
            }
            
        }
            
    }

    @Override
    public String getJavaInitializationString() {
        return value != null ? value.getJavaInitString() : null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }

    @Override
    public boolean isPaintable() {
        return false;
    }

    @Override
    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
    }

    @Override
    public java.awt.Component getCustomEditor() {
        return null;
    }

    @Override
    public boolean supportsCustomEditor() {
        return false;
    }

    // ----------------

    // FormAwareEditor implementation
    @Override
    public void setContext(FormModel model, FormProperty prop) {
        formModel = model;
        property = prop;
    }

    // FormAwareEditor implementation
    @Override
    public void updateFormVersionLevel() {
    }

    public FormModel getFormModel() {
        return formModel;
    }

    public void setBeanTypes(Class[] types) {
        beanTypes = types;
    }

    public Class[] getBeanTypes() {
        return beanTypes;
    }

    public void setComponentCategory(int cat) {
        componentCategory = cat;
    }

    public int getComponentCategory() {
        return componentCategory;
    }

    // ----------------
    // XMLPropertyEditor implementation

    private static final String XML_COMPONENT = "ComponentRef"; // NOI18N
    private static final String ATTR_NAME = "name"; // NOI18N

    @Override
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        String nameStr;
        if (value != null)
            nameStr = value.getComponent() != null ?
                      value.getDescription() : INVALID_REF;
        else
            nameStr = NULL_REF;
        
        org.w3c.dom.Element el = doc.createElement(XML_COMPONENT);
        el.setAttribute(ATTR_NAME, nameStr);
        return el;
    }

    @Override
    public void readFromXML(org.w3c.dom.Node element)
        throws java.io.IOException
    {
        if (!XML_COMPONENT.equals(element.getNodeName()))
            throw new java.io.IOException();

        org.w3c.dom.NamedNodeMap attributes;
        org.w3c.dom.Node nameAttr;
        String name;

        if ((attributes = element.getAttributes()) != null
              && (nameAttr = attributes.getNamedItem(ATTR_NAME)) != null
              && (name = nameAttr.getNodeValue()) != null)
        {
            if (NULL_REF.equals(name)) {
                value = null;
            } else {
                value = new ComponentRef(name);
            }
        }
    }

    // ---------

    protected List getComponents() {
        if (components == null)
            components = new ArrayList<RADComponent>();
        else
            components.clear();

        if (formModel != null) {
            Collection<RADComponent> comps;
            if (componentCategory == NONVISUAL_COMPONENTS)
                comps = formModel.getNonVisualComponents();
            else
                comps = formModel.getAllComponents();

            for (RADComponent metacomp : comps)
                if (acceptBean(metacomp))
                    components.add(metacomp);
        }

        return components;
    }

    protected boolean acceptBean(RADComponent comp) {
        if (beanTypes == null)
            return true;

        boolean match = false;
        for (int i=0; i < beanTypes.length && !match; i++)
            match = beanTypes[i].isAssignableFrom(comp.getBeanClass());

        return match;
    }

    protected String noneString() {
        if (noneText == null)
            noneText = FormUtils.getBundleString("CTL_NoComponent"); // NOI18N
        return noneText;
    }

    protected String defaultString() {
        if (defaultText == null)
            defaultText = FormUtils.getBundleString("CTL_DefaultComponent"); // NOI18N
        return defaultText;
    }
    
    protected String invalidString() {
        if (invalidText == null)
            invalidText = FormUtils.getBundleString("CTL_InvalidReference"); // NOI18N
        return invalidText;
    }

    // ------

    protected final void firePropertyChange() {
        if (changeSupport != null)
            changeSupport.firePropertyChange(null, null, null);
    }

    // NamedPropertyEditor implementation
    @Override
    public String getDisplayName() {
        return NbBundle.getBundle(getClass()).getString("CTL_ComponentChooserEditor_DisplayName"); // NOI18N
    }

    // ------------

    private class ComponentRef extends FormDesignValueAdapter implements RADComponent.ComponentReference
    {
        private String componentName;
        private RADComponent component;

        ComponentRef(String name) {
            componentName = name;
        }

        ComponentRef(RADComponent metacomp) {
            componentName = metacomp.getName();
            component = metacomp;
        }

        @Override
        public boolean equals(Object obj) {
            boolean equal;
            
            if (obj instanceof ComponentRef) {
                ComponentRef ref = (ComponentRef)obj;
                
                equal = (ref.component == component);
                if (componentName == null) {
                    equal = equal && (ref.componentName == null);
                } else {
                    equal = equal && componentName.equals(ref.componentName);
                }
            } else {
                equal = (obj instanceof RADComponent && obj == component);
            }
            
            return equal;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + (this.componentName != null ? this.componentName.hashCode() : 0);
            hash = 89 * hash + (this.component != null ? this.component.hashCode() : 0);
            return hash;
        }

        String getJavaInitString() {
            checkComponent();

            if (component != null) {
                if (component == component.getFormModel().getTopRADComponent())
                    return "this"; // NOI18N
            }
            else if (!NULL_REF.equals(componentName))
                return null; // invalid reference

            return componentName;
        }

        @Override
        public RADComponent getComponent() {
            checkComponent();
            return component;
        }

        /** FormDesignValue implementation. */
        @Override
        public String getDescription() {
            checkComponent();
            return componentName;
        }

        /** FormDesignValue implementation. */
        @Override
        public Object getDesignValue() {
            checkComponent();
            return component != null ?
                   component.getBeanInstance(): IGNORED_VALUE;
        }

        private void checkComponent() {
            if (component == null
                && !NULL_REF.equals(componentName)
                && !INVALID_REF.equals(componentName))
            {
                List compList = getComponents();
                Iterator it = compList.iterator();
                while (it.hasNext()) {
                    RADComponent comp = (RADComponent) it.next();
                    if (comp.getName().equals(componentName)) {
                        if (comp.isInModel())
                            component = comp;
                        break;
                    }
                }
            }
            else if (component != null) {
                if (!component.isInModel())
                    component = null;
                else
                    componentName = component.getName();
            }
        }
    }
}
