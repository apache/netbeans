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
package org.netbeans.modules.form.j2ee;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.form.ComponentChooserEditor;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.PropertyModifier;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADProperty;
import org.netbeans.modules.form.editors.EnumEditor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Customizies set of properties of J2EE classes.
 *
 * @author Jan Stola
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.form.PropertyModifier.class)
public class J2EEPropertyModifier implements PropertyModifier {
    
    /**
     * Customizes the set of properties of J2EE classes.
     *
     * @param metacomp component whose properties should be customized.
     * @param prefProps preferred properties.
     * @param normalProps normal properties.
     * @param expertProps expert properties.
     * @return <code>true</code> if some properties were removed/added,
     * returns <code>false</code> otherwise.
     */
    @Override
    public boolean modifyProperties(RADComponent metacomp, List<RADProperty> prefProps, List<RADProperty> normalProps, List<RADProperty> expertProps) {
        String className = metacomp.getBeanClass().getName();
        if (className.equals("javax.persistence.EntityManager")) { // NOI18N
            prefProps.clear();
            normalProps.clear();
            expertProps.clear();
            try {
                normalProps.add(new PUProperty(metacomp));
                expertProps.add(new FlushModeProperty(metacomp));
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
            return true;
        } else if (className.equals("javax.persistence.Query")) { // NOI18N
            prefProps.clear();
            normalProps.clear();
            expertProps.clear();
            try {
                prefProps.add(new QueryProperty(metacomp));
                expertProps.add(new EMProperty(metacomp));
                expertProps.add(new FirstResultProperty(metacomp));
                expertProps.add(new MaxResultsProperty(metacomp));
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
            return true;
        } else if (className.equals("java.util.List")) { // NOI18N
            // PENDING modify only query result lists - mark them by some aux value
            prefProps.clear();
            normalProps.clear();
            expertProps.clear();
            try {
                prefProps.add(new QueryBeanProperty(metacomp, true));
                expertProps.add(new ModifiableWrapperProperty(metacomp));
                expertProps.add(new ObservableProperty(metacomp));
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
            return true;
        }
        for (Annotation annotation : metacomp.getBeanClass().getAnnotations()) {
            if ("javax.persistence.Entity".equals(annotation.annotationType().getName())) { // NOI18N
                expertProps.addAll(normalProps);
                normalProps.clear();
                normalProps.addAll(prefProps);
                prefProps.clear();
                try {
                    prefProps.add(new QueryBeanProperty(metacomp, false));
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Peristence unit property of entity manager.
     */
    static class PUProperty extends RADProperty {
        
        /**
         * Creates <code>PUProperty</code> for the given component.
         *
         * @param comp component representing entity manager.
         */
        PUProperty(RADComponent comp) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor("persistenceUnit", String.class)); // NOI18N
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            try {
                setValue("pu"); // "default value" // NOI18N
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
            setShortDescription(NbBundle.getMessage(PUProperty.class, "HINT_PersistenceUnitName")); // NOI18N
        }

        /**
         * Determines whether the property supports default value or not.
         *
         * @return <code>true</code> if the property supports default value,
         * returns <code>false</code> otherwise.
         */
        @Override
        public boolean supportsDefaultValue() {
            return false;
        }

        /**
         * Returns property editor for this property.
         *
         * @return property editor for this property.
         */
        @Override
        public PropertyEditor getExpliciteEditor() {
            FileObject fob = FormEditor.getFormDataObject(getRADComponent().getFormModel()).getFormFile();
            String[] names = J2EEUtils.getPersistenceUnitNames(FileOwnerQuery.getOwner(fob));
            Object[] enumValues = new Object[3*names.length];
            for (int i=0; i<names.length; i++) {
                enumValues[3*i] = enumValues[3*i+1] = names[i];
                enumValues[3*i+2] = '\"' + FormUtils.escapeCharactersInString(names[i]) + '\"';
            }
            return new EnumEditor(enumValues);
        }
    }

    /**
     * Flush mode property of entity manager.
     */
    static class FlushModeProperty extends RADProperty {
        
        /**
         * Creates <code>FlushModeProperty</code> for the given component.
         *
         * @param comp component representing entity manager.
         */
        FlushModeProperty(RADComponent comp) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor("flushModeType", Object.class) { // NOI18N
                @Override
                public java.lang.reflect.Method getWriteMethod() {
                    java.lang.reflect.Method m = null;
                    try {
                        m = FlushModeProperty.class.getMethod("setFlushMode", Object.class); // NOI18N
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
                    }
                    return m;
                }
            });
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(NbBundle.getMessage(FlushModeProperty.class, "HINT_FlushMode")); // NOI18N
        }

        // Helper method - don't remove
        public void setFlushMode(Object flushMode) {}

        /**
         * Determines whether the property supports default value or not.
         *
         * @return <code>true</code> if the property supports default value,
         * returns <code>false</code> otherwise.
         */
        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        /**
         * Returns the default value of the property.
         *
         * @return the default value of the property.
         */
        @Override
        public Object getDefaultValue() {
            return null;
        }

        /**
         * Returns property editor for this property.
         *
         * @return property editor for this property.
         */
        @Override
        public PropertyEditor getExpliciteEditor() {
            Object[] enumValues = new Object[9];
            enumValues[0] = NbBundle.getMessage(FlushModeProperty.class, "LBL_FlushModeType.DEFAULT"); // NOI18N
            enumValues[3] = NbBundle.getMessage(FlushModeProperty.class, "LBL_FlushModeType.AUTO"); // NOI18N
            enumValues[6] = NbBundle.getMessage(FlushModeProperty.class, "LBL_FlushModeType.COMMIT"); // NOI18N
            // The actual value is not necessary - it is not used in design time
            enumValues[1] = null;
            enumValues[4] = "FlushModeType.AUTO"; // javax.persistence.FlushModeType.AUTO // NOI18N
            enumValues[7] = "FlushModeType.COMMIT"; // javax.persistence.FlushModeType.COMMIT // NOI18N
            enumValues[2] = ""; // NOI18N
            enumValues[5] = "javax.persistence.FlushModeType.AUTO"; // NOI18N
            enumValues[8] = "javax.persistence.FlushModeType.COMMIT"; // NOI18N
            return new EnumEditor(enumValues);
        }
    }

    /**
     * Entity manager property of query.
     */
    static class EMProperty extends RADProperty {
        
        /**
         * Creates <code>EMProperty</code> for the given component.
         *
         * @param comp component representing query result list.
         */
        EMProperty(RADComponent comp) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor("entityManager", Object.class)); // NOI18N
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(NbBundle.getMessage(EMProperty.class, "HINT_EntityManagerProperty")); // NOI18N
        }

        /**
         * Determines whether the property supports default value or not.
         *
         * @return <code>true</code> if the property supports default value,
         * returns <code>false</code> otherwise.
         */
        @Override
        public boolean supportsDefaultValue() {
            return false;
        }

        /**
         * Returns property editor for this property.
         *
         * @return property editor for this property.
         */
        @Override
        public PropertyEditor getExpliciteEditor() {
            return new EntityManagerEditor();
        }

        public static class EntityManagerEditor extends BeanChooserEditor {
            public EntityManagerEditor() {
                super("javax.persistence.EntityManager", // NOI18N
                    NbBundle.getBundle(EntityManagerEditor.class).getString("CTL_EntityManagerChooser")); // NOI18N
            }
        }
        
    }

    /**
     * Query bean property of query result list or entity instance.
     */
    static class QueryBeanProperty extends RADProperty {
        /** Determines whether it is a property of a query list result. */
        private boolean isList;
        
        /**
         * Creates <code>QueryBeanProperty</code> for the given component.
         *
         * @param comp component representing query result list.
         * @param isList determines whether this property belongs to query list
         */
        QueryBeanProperty(RADComponent comp, boolean isList) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor("query", Object.class)); // NOI18N
            this.isList = isList;
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(NbBundle.getMessage(QueryBeanProperty.class, "HINT_QueryBeanProperty")); // NOI18N
        }

        @Override
        public void setValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            super.setValue(value);
            if (!isList) {
                // PENDING attempt to avoid overriding of manually entered custom creation code
                RADComponent comp = getRADComponent();
                StringBuilder sb = new StringBuilder();
                sb.append('(').append(comp.getBeanClass().getName()).append(')');
                String initString = getJavaInitializationString();
                if ("null".equals("" + initString)) { // NOI18N
                    sb.append("((javax.persistence.Query)null)");  // NOI18N
                } else {
                    sb.append(initString);
                }
                sb.append(".getSingleResult()"); // NOI18N
                if (comp.getFormModel().isFormLoaded()) {
                    comp.getSyntheticProperty("creationCodeCustom").setValue(sb.toString()); // NOI18N
                }
            }
        }

        /**
         * Determines whether the property supports default value or not.
         *
         * @return <code>true</code> if the property supports default value,
         * returns <code>false</code> otherwise.
         */
        @Override
        public boolean supportsDefaultValue() {
            return false;
        }

        /**
         * Returns property editor for this property.
         *
         * @return property editor for this property.
         */
        @Override
        public PropertyEditor getExpliciteEditor() {
            return new QueryEditor();
        }

        public static class QueryEditor extends BeanChooserEditor {
            public QueryEditor() {
                super("javax.persistence.Query", // NOI18N
                    NbBundle.getBundle(QueryEditor.class).getString("CTL_QueryChooser")); // NOI18N
            }
        }
        
    }

    /**
     * Query property of query bean.
     */
    static class QueryProperty extends RADProperty {
        
        /**
         * Creates <code>QueryProperty</code> for the given component.
         *
         * @param comp component representing query result list.
         */
        QueryProperty(RADComponent comp) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor("query", String.class)); // NOI18N
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            try {
                setValue(""); // NOI18N "default value"
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
            setShortDescription(NbBundle.getMessage(QueryProperty.class, "HINT_QueryProperty")); // NOI18N
        }

        /**
         * Determines whether the property supports default value or not.
         *
         * @return <code>true</code> if the property supports default value,
         * returns <code>false</code> otherwise.
         */
        @Override
        public boolean supportsDefaultValue() {
            return false;
        }

    }

    /**
     * First result property of query result list.
     */
    static class FirstResultProperty extends RADProperty {
        
        /**
         * Creates <code>FirstResultProperty</code> for the given component.
         *
         * @param comp component representing query result list.
         */
        FirstResultProperty(RADComponent comp) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor("firstResult", int.class)); // NOI18N
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(NbBundle.getMessage(FirstResultProperty.class, "HINT_FirstResultProperty")); // NOI18N
        }

        /**
         * Determines whether the property supports default value or not.
         *
         * @return <code>true</code> if the property supports default value,
         * returns <code>false</code> otherwise.
         */
        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        /**
         * Returns the default value of the property.
         *
         * @return the default value of the property.
         */
        @Override
        public Object getDefaultValue() {
            return Integer.valueOf(0);
        }

    }

    /**
     * Max results property of query result list.
     */
    static class MaxResultsProperty extends RADProperty {
        
        /**
         * Creates <code>MaxResultsProperty</code> for the given component.
         *
         * @param comp component representing query result list.
         */
        MaxResultsProperty(RADComponent comp) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor("maxResults", int.class)); // NOI18N
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(NbBundle.getMessage(MaxResultsProperty.class, "HINT_MaxResultsProperty")); // NOI18N
        }

        /**
         * Determines whether the property supports default value or not.
         *
         * @return <code>true</code> if the property supports default value,
         * returns <code>false</code> otherwise.
         */
        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        /**
         * Returns the default value of the property.
         *
         * @return the default value of the property.
         */
        @Override
        public Object getDefaultValue() {
            return Integer.valueOf(-1);
        }

    }

    /**
     * Observable property of query result list.
     */
    static class ObservableProperty extends RADProperty {
        
        /**
         * Creates <code>ObservableProperty</code> for the given component.
         *
         * @param comp component representing query result list.
         */
        ObservableProperty(RADComponent comp) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor("observable", boolean.class)); // NOI18N
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(NbBundle.getMessage(ObservableProperty.class, "HINT_ObservableProperty")); // NOI18N
        }

        /**
         * Determines whether the property supports default value or not.
         *
         * @return <code>true</code> if the property supports default value,
         * returns <code>false</code> otherwise.
         */
        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        /**
         * Returns the default value of the property.
         *
         * @return the default value of the property.
         */
        @Override
        public Object getDefaultValue() {
            return Boolean.FALSE;
        }

    }

    /**
     * Modifiable wrapper property of query result list.
     */
    static class ModifiableWrapperProperty extends RADProperty {
        
        /**
         * Creates <code>ModifiableWrapperProperty</code> for the given component.
         *
         * @param comp component representing query result list.
         */
        ModifiableWrapperProperty(RADComponent comp) throws IntrospectionException {
            super(comp, new FakePropertyDescriptor("modifiableWrapper", boolean.class)); // NOI18N
            setAccessType(DETACHED_READ | DETACHED_WRITE);
            setShortDescription(NbBundle.getMessage(ModifiableWrapperProperty.class, "HINT_ModifiableWrapper")); // NOI18N
        }

        /**
         * Determines whether the property supports default value or not.
         *
         * @return <code>true</code> if the property supports default value,
         * returns <code>false</code> otherwise.
         */
        @Override
        public boolean supportsDefaultValue() {
            return true;
        }

        /**
         * Returns the default value of the property.
         *
         * @return the default value of the property.
         */
        @Override
        public Object getDefaultValue() {
            return Boolean.FALSE;
        }

    }

    /**
     * Component chooser editor without none value.
     */
    public static class BeanChooserEditor extends ComponentChooserEditor {
        /** Name of the class this chooser editor should accept. */
        private String beanClassName;
        /** Display name of this editor. */
        private String displayName;
        
        /**
         * Creates new <code>BeanChooserEditor</code>.
         *
         * @param beanClassName name of the class the editor should accept.
         * @param displayName display name of the editor.
         */
        BeanChooserEditor(String beanClassName, String displayName) {
            this.beanClassName = beanClassName;
            this.displayName = displayName;
        }

        /**
         * Determines whether the given component should be offered by the property editor.
         *
         * @param comp component that should be considered.
         * @return <code>true</code> if the component should be offered by the property editor,
         * returns <code>false</code> otherwise.
         */
        @Override
        protected boolean acceptBean(RADComponent comp) {
            return beanClassName.equals(comp.getBeanClass().getName());
        }

        /**
         * Returns display name of the property editor.
         *
         * @return display name of the property editor.
         */
        @Override
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Returns list of items offered by this property editor.
         *
         * @return list of items offered by this property editor.
         */
        @Override
        public String[] getTags() {
            // Remove <none> (= null)
            String[] superTags = super.getTags();
            List<String> tags = new LinkedList<String>();
            tags.addAll(Arrays.asList(superTags));
            String none = noneString();
            tags.remove(none);
            return tags.toArray(new String[0]);
        }
    }

    /**
     * Fake property descriptor.
     */
    static class FakePropertyDescriptor extends PropertyDescriptor {
        /** Property type. */
        private Class propType;

        /**
         * Creates new <code>FakePropertyDescriptor</code>.
         *
         * @param name name of the property.
         * @param type type of the property.
         * @throws IntrospectionException never.
         */
        FakePropertyDescriptor(String name, Class type) throws IntrospectionException {
            super(name,null,null);
            propType = type;
        }

        /**
         * Returns type of the property.
         *
         * @return type of the property.
         */
        @Override
        public Class getPropertyType() {
            return propType;
        }
    }
    
}
