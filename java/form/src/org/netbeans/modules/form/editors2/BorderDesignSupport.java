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

package org.netbeans.modules.form.editors2;

import javax.swing.border.*;
import java.util.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.nodes.*;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.editors.EnumEditor;
import org.openide.ErrorManager;

/**
 * A support class holding metadata for borders (javax.swing.border.Border),
 * similar to RADComponent.
 *
 * @author Tomas Pavek
 */

public class BorderDesignSupport implements FormDesignValue
{
    private Border theBorder;
    private boolean borderNeedsUpdate;
    private boolean propertiesNeedInit;
    private CreationDescriptor creationDesc;
    private FormPropertyContext propertyContext = null;
    private FormProperty[] properties = null;
    // -------------------------
    // constructors

    public BorderDesignSupport(Class borderClass)
        throws Exception
    {
        creationDesc = CreationFactory.getDescriptor(borderClass);
        if (creationDesc == null) {
            creationDesc = new CreationDescriptor(borderClass);
            CreationFactory.registerDescriptor(creationDesc);
        }

        theBorder = (Border) CreationFactory.createInstance(borderClass);
    }
    
    public BorderDesignSupport(Border border) {
        creationDesc = CreationFactory.getDescriptor(border.getClass());
        if (creationDesc == null) {
            creationDesc = new CreationDescriptor(border.getClass());
            CreationFactory.registerDescriptor(creationDesc);
        }
        setBorder(border);
    }

    public BorderDesignSupport(BorderDesignSupport borderDesignSupport, FormPropertyContext propertyContext)
        throws Exception
    {
        this(borderDesignSupport.getBorderClass());
        setPropertyContext(propertyContext);
        createProperties();
        int copyMode = FormUtils.CHANGED_ONLY | FormUtils.DISABLE_CHANGE_FIRING;
            
        FormUtils.copyProperties(borderDesignSupport.getProperties(),
                                 this.properties,
                                 copyMode);
    }

    // --------------------------

    @Override
    public FormDesignValue copy(FormProperty formProperty) {
        try {
            return new BorderDesignSupport(this, new FormPropertyContext.SubProperty(formProperty));
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
    
    public Border getBorder() {
        if (borderNeedsUpdate)
            updateBorder();
        return theBorder;
    }

    public void setBorder(Border border) {
        theBorder = border;
        if (properties != null) {
            for (int i=0; i < properties.length; i++)
                try {
                    properties[i].reinstateProperty();
                }
                catch (IllegalAccessException e1) {
                }
                catch (InvocationTargetException e2) {
                }
            propertiesNeedInit = false;
        }
        else propertiesNeedInit = true;
        borderNeedsUpdate = false;
    }

    public Class getBorderClass() {
        return creationDesc.getDescribedClass();
    }

    public String getDisplayName() {
        return org.openide.util.Utilities.getShortClassName(theBorder.getClass());
//        String longName = theBorder.getClass().getName();
//        int dot = longName.lastIndexOf('.');
//        return dot < 0 ? longName : longName.substring(dot + 1);
    }

    /** Sets FormPropertyContext for properties. This should be called before
     * properties are created or used after property context had changed.
     */
    public void setPropertyContext(FormPropertyContext propertyContext) {
        if (properties != null && this.propertyContext != propertyContext) {
            for (int i=0; i < properties.length; i++)
                if (!properties[i].getValueType().isPrimitive())
                    properties[i].setPropertyContext(propertyContext);
        }

        this.propertyContext = propertyContext;
    }

    // FormPropertyContainer implementation
    public Node.Property[] getProperties() {
        if (properties == null)
            createProperties();
        return properties;
    }

    public Node.Property getPropertyOfName(String name) {
        Node.Property[] props = getProperties();
        for (int i=0; i < props.length; i++)
            if (props[i].getName().equals(name))
                return props[i];

        return null;
    }

    private void createProperties() {
        FormLAF.executeWithLookAndFeel(propertyContext.getFormModel(), new Runnable() {
            @Override
            public void run() {
                createPropertiesInLAFBlock();
            }
        });
    }

    private void createPropertiesInLAFBlock() {
        BeanInfo bInfo;
        try {
            bInfo = FormUtils.getBeanInfo(theBorder.getClass());
        } catch (IntrospectionException ex) {
            return;
        }
        PropertyDescriptor[] props = bInfo.getPropertyDescriptors();

        List<FormProperty> nodeProps = new ArrayList<FormProperty>();
        for (int i = 0; i < props.length; i++) {
            PropertyDescriptor pd = props[i];
            if (!pd.isHidden()
                && (pd.getWriteMethod() != null 
                    || CreationFactory.containsProperty(creationDesc,
                                                        pd.getName())))
            {
                BorderProperty prop =
                    new BorderProperty(pd.getPropertyType().isPrimitive() ?
                                           null : propertyContext,
                                       pd);

                if (propertiesNeedInit)
                    try {
                        prop.reinstateProperty();
                    }
                    catch (IllegalAccessException e1) {
                    }
                    catch (InvocationTargetException e2) {
                    }

                nodeProps.add(prop);
            }
        }
        properties = new FormProperty[nodeProps.size()];
        nodeProps.toArray(properties);
        propertiesNeedInit = false;
    }

    public String getJavaInitializationString() {
        if (properties == null)
            createProperties();

        CreationDescriptor.Creator creator =
            creationDesc.findBestCreator(properties,
                CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);

        return creator.getJavaCreationCode(properties, null, null, Border.class, null);
    }

    void updateBorder() {
        if (properties == null)
            createProperties();

        CreationDescriptor.Creator creator =
            creationDesc.findBestCreator(properties,
                CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);

        try {
            theBorder = (Border) CreationFactory.createInstance(
                creationDesc.getDescribedClass(),
                properties,
                CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);

            // set other properties (not used in constructor)
            FormProperty[] otherProps = CreationFactory.getRemainingProperties(
                                                         creator, properties);
            for (int i=0; i < otherProps.length; i++)
                otherProps[i].reinstateTarget();
        }
        catch (Exception ex) { // should not happen (at least for standard borders)
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    @Override
    public Object getDesignValue() {
        return getBorder();
    }

    @Override
    public Object getDesignValue(Object target) {
        if (FormLAF.getUsePreviewDefaults()) {
            return copy((FormProperty)propertyContext.getOwner()).getDesignValue();
        } else {
            return null;
        }
    }

    @Override
    public String getDescription() {
        return getDisplayName();
    }

    // -----------------------

    public class BorderProperty extends FormProperty {
        private PropertyDescriptor desc;
        private Object defaultValue;

        public BorderProperty(FormPropertyContext propertyContext,
                              PropertyDescriptor desc)
        {
            super(propertyContext,
                  desc.getName(),
                  desc.getPropertyType(),
                  desc.getDisplayName(),
                  desc.getShortDescription());

            this.desc = desc;

            if (desc.getWriteMethod() == null) {
                setAccessType(DETACHED_WRITE);
            } else if (desc.getReadMethod() == null) {
                setAccessType(DETACHED_READ);
            }

            if (canReadFromTarget()) {
                try {
                    defaultValue = getTargetValue();
                    if ((theBorder instanceof TitledBorder) && canWriteToTarget()) {
                        // TitledBorder doesn't remember its default values.
                        // We have to set them explicitly because otherwise
                        // it will start to return other "default" values
                        // when we leave the LAF block
                        setTargetValue(defaultValue);
                    }
                } catch (Exception ex) {}
            }
        }
        
        @Override
        public void setValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            super.setValue(value);
            if (MatteBorder.class.equals(getBorderClass())) {
                // Issue 145316 - MatteBorder cannot have both titleIcon
                // and matteColor specified.
                String propName = null;
                if ("matteColor".equals(getName())) { // NOI18N
                    propName = "tileIcon"; // NOI18N
                } else if ("tileIcon".equals(getName())) { // NOI18N
                    propName = "matteColor"; // NOI18N
                }
                if (propName != null) {
                    Node.Property prop = getPropertyOfName(propName);
                    // Restore default value doesn't call setValue()
                    // So, there is no danger of infinite loop
                    prop.restoreDefaultValue();
                }
            }
        }

        @Override
        public Object getTargetValue()
            throws IllegalAccessException, InvocationTargetException
        {
            Method readMethod = desc.getReadMethod();
            return readMethod.invoke(theBorder, new Object[0]);
        }

        @Override
        public void setTargetValue(Object value)
            throws IllegalAccessException, IllegalArgumentException,
                   InvocationTargetException
        {
            Method writeMethod = desc.getWriteMethod();
            writeMethod.invoke(theBorder, new Object[] { value });
        }

        @Override
        protected Object getRealValue(Object value) {
            Object realValue = super.getRealValue(value);

            if (realValue == FormDesignValue.IGNORED_VALUE
                  && "title".equals(desc.getName())) // NOI18N
                realValue = ((FormDesignValue)value).getDescription();

            return realValue;
        }

        @Override
        public boolean supportsDefaultValue () {
            return true;
        }

        @Override
        public Object getDefaultValue() {
            return defaultValue;
        }

        @Override
        public PropertyEditor getExpliciteEditor() {
            try {
                PropertyEditor propEd = desc.createPropertyEditor(theBorder);
                if (propEd == null) {
                    Object[] enumerationValues = (Object[])desc.getValue("enumerationValues"); // NOI18N
                    if (enumerationValues != null) {
                        propEd = new EnumEditor(enumerationValues);
                    }
                }
                return propEd;
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                return null;
            }
        }
	
        @Override
	protected Method getWriteMethod() {
	    return desc.getWriteMethod();	    
	}
    
        @Override
        protected void propertyValueChanged(Object old, Object current) {
            super.propertyValueChanged(old, current);
            borderNeedsUpdate = (getAccessType() & DETACHED_WRITE) != 0;
        }

        // Issue 73245 explains why is this method overriden
        @Override
        public boolean equals(Object property) {
            return (this == property);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }
}
