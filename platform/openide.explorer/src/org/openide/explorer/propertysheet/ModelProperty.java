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
/*
 * ModelProperty.java
 *
 * Created on 05 October 2003, 20:40
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;


/** Wraps a legacy PropertyModel object in an instance of Node.Property.
 *
 * @author  Tim Boudreau
 */
class ModelProperty extends Property {
    PropertyModel mdl;
    private boolean valueSet = false;

    /** Creates a new instance of ModelProperty */
    private ModelProperty(PropertyModel pm) {
        super(pm.getPropertyType());
        this.mdl = pm;

        if (mdl instanceof ExPropertyModel) {
            FeatureDescriptor fd = ((ExPropertyModel) mdl).getFeatureDescriptor();
            Boolean result = (Boolean) fd.getValue("canEditAsText"); // NOI18N

            if (result != null) {
                this.setValue("canEditAsText", result);
            }
        }

        //System.err.println(
        //"Created ModelProperty wrapper for mystery PropertyModel " + pm);
    }

    Object[] getBeans() {
        if (mdl instanceof ExPropertyModel) {
            return ((ExPropertyModel) mdl).getBeans();
        }

        return null;
    }

    public PropertyEditor getPropertyEditor() {
        if (mdl.getPropertyEditorClass() != null) {
            try {
                //System.err.println("ModelProperty creating a " 
                //+ mdl.getPropertyEditorClass());
                Constructor c = mdl.getPropertyEditorClass().getConstructor();
                c.setAccessible(true);

                return (PropertyEditor) c.newInstance(new Object[0]);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);

                return new PropUtils.NoPropertyEditorEditor();
            }
        }

        return super.getPropertyEditor();
    }

    static Property toProperty(PropertyModel mdl) {
        if (mdl instanceof NodePropertyModel) {
            //System.err.println("Extracting prop from NodePropertyModel - " + ((NodePropertyModel) mdl).getProperty().getDisplayName());
            return ((NodePropertyModel) mdl).getProperty();
        } else if (mdl instanceof DefaultPropertyModel) {
            return new DPMWrapper((DefaultPropertyModel) mdl);
        } else if (
            mdl instanceof ExPropertyModel &&
                ((ExPropertyModel) mdl).getFeatureDescriptor() instanceof PropertyDescriptor
        ) {
            Object[] beans = ((ExPropertyModel) mdl).getBeans();

            if (beans.length == 1) {
                return new DPMWrapper(
                    (PropertyDescriptor) ((ExPropertyModel) mdl).getFeatureDescriptor(),
                    ((ExPropertyModel) mdl).getBeans()
                );
            } else {
                return new ModelProperty(mdl);
            }
        } else if (mdl instanceof ExPropertyModel &&
                ((ExPropertyModel) mdl).getFeatureDescriptor() instanceof Property) {
            UnsupportedOperationException uoe = new UnsupportedOperationException(
                    "PropertyPanel now supports direct" + " use of Node.Property objects.  Please do not use " +
                    "ExPropertyModel if you only need to wrap a Node.Property " +
                    "object.  PropertyModel will be deprecated soon."
                ); //NOI18N
            Logger.getLogger(ModelProperty.class.getName()).log(Level.WARNING, null, uoe);

            return (Property) ((ExPropertyModel) mdl).getFeatureDescriptor();
        } else if (mdl != null) {
            return new ModelProperty(mdl);
        } else {
            //Model is null - formerly solved by PropertyPanel.EMPTY empty model
            return new EmptyProperty();
        }
    }

    /** Creates a ProxyProperty for a set of nodes given a property name */
    static Property toProperty(Node[] n, String name) throws ClassCastException, NullPointerException {
        Class<?> clazz = null; //the class to look for

        if (n.length == 0) {
            //Give enough info for someone to figure out what's wrong
            throw new NullPointerException(
                "Cannot find a property in an array" + " of 0 properties.  Looking for " + name
            );
        }

        for (int i = 0; i < n.length; i++) {
            Property p = findProperty(n[i], name);

            if (p == null) {
                throw new NullPointerException(
                    "Node " + n[i].getDisplayName() + " does not contain a property " + name
                );
            } else if (clazz == null) {
                clazz = p.getValueType();
            } else {
                if (p.getValueType() != clazz) {
                    throw new ClassCastException(
                        "Found a property named " + n + " but it is of class " + p.getValueType().getName() + " not " +
                        clazz.getName()
                    );
                }
            }
        }

        ProxyNode pn = new ProxyNode(n);
        Property p = findProperty(pn, name);

        if (p != null) {
            return p;
        }

        //should never reach this point
        throw new NullPointerException(
            "Found properties named " + name + " but ProxyNode did not contain one with this name.  This should " +
            "be impossible; probably someone has modified ProxyNode"
        );
    }

    /** Used in case of 1 element array */
    static Property findProperty(Node n, String name) throws NullPointerException {
        PropertySet[] ps = n.getPropertySets();

        for (int j = 0; j < ps.length; j++) {
            Property p = findProperty(ps[j], name);

            if (p != null) {
                return p;
            }
        }

        return null;
    }

    /** Searches for a named property in a property set */
    private static Property findProperty(PropertySet set, String name) {
        Property[] p = set.getProperties();

        for (int i = 0; i < p.length; i++) {
            if (p[i].getName().equals(name)) {
                return p[i];
            }
        }

        return null;
    }

    public boolean canRead() {
        return true; //who really knows?
    }

    public boolean canWrite() {
        return true;
    }

    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return mdl.getValue();
    }

    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //System.err.println("Setting value on ModelProperty to " + val + " forwarding to " + mdl);
        mdl.setValue(val);
        valueSet = true;
    }

    /** Used by EditablePropertyDisplayer to provide access to the real
     * feature descriptor.  Some property editors will cast the result of
     * env.getFeatureDescriptor() as Property or PropertyDescriptor, so we
     * need to return the original */
    FeatureDescriptor getFeatureDescriptor() {
        if (mdl instanceof ExPropertyModel) {
            return ((ExPropertyModel) mdl).getFeatureDescriptor();
        } else {
            return this;
        }
    }

    private static String findDisplayNameFor(Object o) {
        try {
            if (o == null) {
                return null;
            }

            if (o instanceof Node.Property) {
                return ((Node.Property) o).getDisplayName();
            }

            BeanInfo bi = Introspector.getBeanInfo(o.getClass());

            if (bi != null) {
                BeanDescriptor bd = bi.getBeanDescriptor();

                if (bd != null) {
                    return bd.getDisplayName();
                }
            }
        } catch (Exception e) {
            //okay, we did our best
        }

        return null;
    }

    /** A wrapper for PropertyModels that just use DefaultPropertyModel - converts
     * them to PropertySupport.Reflection */
    static class DPMWrapper extends PropertySupport.Reflection {
        PropertyDescriptor descriptor;
        PropertyModel mdl;
        private String beanName = null;

        public DPMWrapper(DefaultPropertyModel mdl) {
            super(
                mdl.bean, mdl.getPropertyType(), ((PropertyDescriptor) mdl.getFeatureDescriptor()).getReadMethod(),
                ((PropertyDescriptor) mdl.getFeatureDescriptor()).getWriteMethod()
            );
            descriptor = ((PropertyDescriptor) mdl.getFeatureDescriptor());

            //            System.err.println("Created DPMWrapper for DefaultPropertyModel " + descriptor.getName() + " - " + descriptor.getDisplayName());
            //            System.err.println("Value class for dpm: " + mdl.getPropertyType().getName() + " for created property: " + this.getValueType());
            //            System.err.println("SuppressCustomEditor: " + descriptor.getValue("suppressCustomEditor"));
            this.mdl = mdl;
            beanName = findDisplayNameFor(mdl.bean);
        }

        public DPMWrapper(PropertyDescriptor desc, Object[] instances) {
            super(instances[0], desc.getPropertyType(), desc.getReadMethod(), desc.getWriteMethod());
            descriptor = desc;

            if ((instances != null) && (instances.length == 1)) {
                beanName = findDisplayNameFor(instances[0]);
            }

            //            System.err.println("Created DPMWrapper for PropertyDescriptor " + descriptor.getName() + " - " + descriptor.getDisplayName() + " with array of instances");
        }

        public String getBeanName() {
            return beanName;
        }

        Object[] getBeans() {
            if (mdl instanceof DefaultPropertyModel) {
                return ((DefaultPropertyModel) mdl).getBeans();
            }

            return null;
        }

        /** Used by EditablePropertyDisplayer to provide access to the real
         * feature descriptor.  Some property editors will cast the result of
         * env.getFeatureDescriptor() as Property or PropertyDescriptor, so we
         * need to return the original */
        FeatureDescriptor getFeatureDescriptor() {
            return descriptor;
        }

        public String getDisplayName() {
            return descriptor.getDisplayName();
        }

        public String getShortDescription() {
            return descriptor.getShortDescription();
        }

        public Object getValue(String key) {
            Object result = descriptor.getValue(key);

            if (result == null) {
                result = super.getValue(key);
            }

            return result;
        }

        public void setValue(String key, Object val) {
            descriptor.setValue(key, val);
        }

        public PropertyEditor getPropertyEditor() {
            Class edClass;

            if (mdl != null) {
                edClass = mdl.getPropertyEditorClass();
            } else {
                edClass = descriptor.getPropertyEditorClass();
            }

            if (edClass != null) {
                //Handle case of e.g. java wizard class customizer which 
                //overrides getPropertyEditorClass()
                try {
                    //System.err.println(getDisplayName() + "Returning editor class specified property editor - " + edClass);
                    return (PropertyEditor) edClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    //fall through
                }
            }

            return super.getPropertyEditor();
        }
    }

    /** A property implementation for cases where the property panel would
     * be using the empty model */
    private static class EmptyProperty extends Property {
        public EmptyProperty() {
            super(Object.class);
        }

        public boolean canRead() {
            return true;
        }

        public boolean canWrite() {
            return false;
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return "";
        }

        public void setValue(Object val)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            //do nothing
        }

        public PropertyEditor getPropertyEditor() {
            return new PropUtils.NoPropertyEditorEditor();
        }
    }
}
