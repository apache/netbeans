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
package org.netbeans.modules.j2ee.sun.dd.impl.common;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.schema2beans.Version;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Rajeshwar Patil
 */
public abstract class SunBaseBean extends BaseBean implements CommonDDBean {

	/** Creates a new instance of SunBaseBean
	 */
	public SunBaseBean(Vector comps, Version version) {
		super(comps, version);
	}
	
	/* Dump the content of this bean returning it as a String
	 */
	public void dump(StringBuffer str, String indent){
	}
        
    public CommonDDBean getPropertyParent(String name){
        if(this.graphManager() != null)
            return (CommonDDBean) this.graphManager().getPropertyParent(name);
        else
            return null;
    }

    public void write(Writer w) throws IOException, DDException {
        try {
            super.write(w);
        } catch(Schema2BeansException ex) {
            // !PW FIXME We should do a proper wrapped exception here, but there are 
            // difficulties overriding this method if DDException is not derived directly
            // from Schema2BeanException (due to method signature mismatch.)
            DDException ddEx = new DDException(ex.getMessage());
            ddEx.setStackTrace(ex.getStackTrace());
            throw ddEx;
        }
    }
    
    public void write(FileObject fo) throws IOException {
        // TODO: need to be implemented with Dialog opened when the file object is locked
        FileLock lock = fo.lock();
        try {
            OutputStream os = fo.getOutputStream(lock);
            try {
                write(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    public void merge(CommonDDBean root, int mode) {
        // !PW Ugly casts to get Java to invoke merge(BaseBean, int) on BaseBean base class.
        ((BaseBean) this).merge((BaseBean) root, mode);
    }

    /** Does this bean have any child properties or attributes (other than a
     *  name, if it is a named bean)?
     * 
     *  Used to trim graph in cases where the last non-trivial value has been
     *  cleared.
     */
    public boolean isTrivial(String nameProperty) {
        if(isRoot()) {
            // Root bean is never trivial, by definition.
            return false;
        }
        
        // Check for non-empty attributes
        // !PW FIXME should really check for non-empty AND non-default attributes.
        String []  attrs = getAttributeNames();
        if (attrs != null && attrs.length > 0) {
            for (int j = 0; j < attrs.length; j++) {
                String a = attrs[j];
                if (!beanProp().getAttrProp(a).isFixed()) {
                    String value = getAttributeValue(a);
                    if(value != null && value.length() > 0) {
                        return false;
                    }
                }
            }
        }
        
        Iterator it = beanPropsIterator();
        while (it.hasNext()) {
            BeanProp prop = (BeanProp)it.next();
            
            if (prop == null || (nameProperty != null && nameProperty.equals(prop.beanName))) {
                // skip null properties
                // skip name property -- named beans w/ only a name are trivial.
                continue;
            }
            
            if (Common.isArray(prop.type)) {
                int size = prop.size();
                if (Common.isBean(prop.type)) {
                    for (int i = 0; i < size; i++) {
                        if (prop.getValue(i) != null) {
                            return false;  // short circuit failure.
                        }
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        Object o = prop.getValue(i);
                        
                        if (o == null || (o instanceof String && ((String) o).length() == 0)) {
                            continue;
                        } else {
                            return false;  // short circuit failure.
                        }
                    }
                    
                    // Check for non-empty attributes
                    // !PW FIXME should really check for non-empty AND non-default attributes.
                    attrs = prop.getAttributeNames();
                    for(int j = 0; j < attrs.length; j++) {
                        String a = attrs[j];
                        if (!prop.getAttrProp(a).isFixed()) {
                            for(int i = 0; i < size; i++) {
                                String value = prop.getAttributeValue(i, a);
                                if(value != null && value.length() > 0) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            } else {
                if (Common.isBean(prop.type)) {
                    if(prop.getValue(0) != null) {
                        return false;
                    }
                } else {
                    Object o = prop.getValue(0);
                    if (o == null || (o instanceof String && ((String) o).length() == 0)) {
                        // intentionally blank
                    } else {
                        return false;   // short circuit failure.
                    }
                    
                    // Check for non-empty attributes
                    // !PW FIXME should really check for non-empty AND non-default attributes.
                    attrs = prop.getAttributeNames();
                    for(int j = 0; j < attrs.length; j++) {
                        String a = attrs[j];
                        if (!prop.getAttrProp(a).isFixed()) {
                            String value = prop.getAttributeValue(0, a);
                            if(value != null && value.length() > 0) {
                                return false;
                            }
                        }
                    }
                    
                }
            }
        }
        
        return true;
    }
    
    /** Deep copy a bean from one version to another so that the copy can be added
     *  to a graph of the new version.
     */
    public CommonDDBean cloneVersion(String version) {
        /**
         * Changes a bean A from version X to version Y, doing a deep copy of all
         * applicable properties and attributes.  Only copies data in common to
         * both versions.
         *
         * this = old bean, class = "...model_[old version].Bean"
         * bean = new bean, class = "...model_[new version].Bean"
         *
         * copy attributes that exist on both
         * copy properties that exist on both
         * uses recursion for bean properties that are copied.
         */
        SunBaseBean bean = null;
        
        try {
            // Create a new instance of ourselves, but using the model for the new version.
            Class newBeanClass = getNewBeanClass(version);

            // Short circuit to clone if target is the same class (i.e. same version)
            if(this.getClass() == newBeanClass) {
                return (SunBaseBean) this.clone();
            }
            
            bean = (SunBaseBean) newBeanClass.getDeclaredConstructor().newInstance();
        } catch(Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        // Use setVersion() on root beans.  This method is designed for child beans.
        if (this.graphManager != null && this.graphManager.getBeanRoot() == this) {
            throw new IllegalArgumentException("Use setVersion() to change version of root bean " + this.getClass().getName());
        }

        // !PW TODO verify attributes exist on new bean before copying.
        //  Copy the attributes of the root
        String[] attrs = this.getAttributeNames();
        if (attrs != null) {
            for(int j=0; j<attrs.length; j++) {
                String a = attrs[j];
                if (!this.beanProp().getAttrProp(a).isFixed()) {
                    String v = this.getAttributeValue(a);
                    if (bean.getAttributeValue(a) != v) {
                        bean.setAttributeValue(a, v);
                    }
                }
            }
        }
  
        // !PW TODO what to do with this?  It's private access in BaseBean with no accessor.
//        if (attrCache != null)
//            bean.attrCache = (HashMap) attrCache.clone();  // This does a shallow clone of the HashMap, but that's fine since they're all just Strings in there.
        
        Iterator it = beanPropsIterator();
        
        //  Parse our attributes and copy them
        while (it.hasNext()) {
            BeanProp prop = (BeanProp)it.next();
            
            if (prop == null) {
                continue;
            }
            
            String name = prop.getBeanName();
            
            if (Common.isArray(prop.type)) {
                int size = prop.size();
                if (Common.isBean(prop.type)) {
                    for(int i=0; i<size; i++) {
                        BaseBean b = (BaseBean)prop.getValue(i);
                        if (b != null) {
                            b = (SunBaseBean) ((SunBaseBean)b).cloneVersion(version);
                        }
                        try {
                            bean.addValue(name, b);
                        } catch(IllegalArgumentException ex) {
                            // !PW TODO Handle this better.
                            System.out.println(ex.getMessage());
                            if(ex.getCause() != null) {
                                System.out.println(ex.getCause().getMessage());
                            }
                        }
                    }
                } else {
                    for(int i=0; i<size; i++) {
                        try {
                            bean.addValue(name, prop.getValue(i));
                        } catch(IllegalArgumentException ex) {
                            // !PW TODO Handle this better.
                            System.out.println(ex.getMessage());
                            if(ex.getCause() != null) {
                                System.out.println(ex.getCause().getMessage());
                            }
                        }
                    }
                    
                    //	Copy the attributes
                    attrs = prop.getAttributeNames();
                    for(int j=0; j<attrs.length; j++) {
                        String a = attrs[j];
                        if (!prop.getAttrProp(a).isFixed()) {
                            for(int i=0; i<size; i++) {
                                String v = prop.getAttributeValue(i, a);
                                if (bean.getAttributeValue(name, i, a) != v) {
                                    bean.setAttributeValue(name, i, a, v);
                                }
                            }
                        }
                    }
                }
            } else {
                if (Common.isBean(prop.type)) {
                    BaseBean b = (BaseBean)prop.getValue(0);
                    if (b != null) {
                        b = (SunBaseBean) ((SunBaseBean)b).cloneVersion(version);
                    }
                    try {
                        bean.setValue(name, b);
                    } catch(IllegalArgumentException ex) {
                        // !PW TODO Handle this better.
                        System.out.println(ex.getMessage());
                        if(ex.getCause() != null) {
                            System.out.println(ex.getCause().getMessage());
                        }
                    }
                } else {
                    try {
                        bean.setValue(name, prop.getValue(0));
                    } catch(IllegalArgumentException ex) {
                        // !PW TODO Handle this better.
                        System.out.println(ex.getMessage());
                        if(ex.getCause() != null) {
                            System.out.println(ex.getCause().getMessage());
                        }
                    }
                    
                    //	Copy the attributes
                    attrs = prop.getAttributeNames();
                    for(int j=0; j<attrs.length; j++) {
                        String a = attrs[j];
                        if (!prop.getAttrProp(a).isFixed()) {
                            String v = prop.getAttributeValue(0, a);
                            if (bean.getAttributeValue(name, 0, a) != v) {
                                bean.setAttributeValue(name, a, v);
                            }
                        }
                    }
                }
            }
        }
        
        return bean;
    }
    
    private Class getNewBeanClass(String version) throws ClassNotFoundException {
        String oldName = this.getClass().getName();
        String className = oldName.substring(oldName.lastIndexOf('.')+1);
        String key = version + className;
        String modelPostfix = commonBeanModelMap.get(key);
        
        StringBuffer buf = new StringBuffer(128);
        if(modelPostfix != null) {
            // Generate correct common bean package prefix.
            buf.append("org.netbeans.modules.j2ee.sun.dd.impl.common.model_");
            buf.append(modelPostfix);
        } else {
            // Generate module specific bean package prefix.
            int modelIndex = oldName.indexOf("model_");
            buf.append(oldName.substring(0, modelIndex+6));
            buf.append(version.charAt(0));
            buf.append('_');
            buf.append(version.charAt(2));
            buf.append('_');
            buf.append(version.charAt(3));
        }
        
        buf.append('.');
        buf.append(className);
        String newClass = buf.toString();
        return Class.forName(newClass);
    }

    /** Lookup table for common classes to correctly determine the package name
     *  that a common bean is in for a given dd dtd, based on appserver version.
     *
     *  Key format: "[spec version][BeanClassName]" -> "#_#_#" for model postfix string
     */
    private static Map<String, String> commonBeanModelMap = new HashMap<String, String>(471);
    
    static {
        // App client 1.3
        commonBeanModelMap.put("1.30EjbRef", "2_1_0");
        commonBeanModelMap.put("1.30ResourceRef", "2_1_0");
        commonBeanModelMap.put("1.30ResourceEnvRef", "2_1_0");

        // App client 1.4
        commonBeanModelMap.put("1.40EjbRef", "2_1_0");
        commonBeanModelMap.put("1.40ResourceRef", "2_1_0");
        commonBeanModelMap.put("1.40ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("1.40ServiceRef", "2_1_1");
        commonBeanModelMap.put("1.40LoginConfig", "2_1_1");
        commonBeanModelMap.put("1.40MessageDestination", "2_1_1");

        commonBeanModelMap.put("1.40PortInfo", "2_1_1");
        commonBeanModelMap.put("1.40CallProperty", "2_1_1");
        commonBeanModelMap.put("1.40StubProperty", "2_1_1");
        commonBeanModelMap.put("1.40ServiceQname", "2_1_1");
        commonBeanModelMap.put("1.40WsdlPort", "2_1_1");
        commonBeanModelMap.put("1.40MessageSecurity", "2_1_1");
        commonBeanModelMap.put("1.40MessageSecurityBinding", "2_1_1");
        
        // App client 1.41
        commonBeanModelMap.put("1.41EjbRef", "2_1_0");
        commonBeanModelMap.put("1.41ResourceRef", "2_1_0");
        commonBeanModelMap.put("1.41ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("1.41ServiceRef", "2_1_1");
        commonBeanModelMap.put("1.41LoginConfig", "2_1_1");
        commonBeanModelMap.put("1.41MessageDestination", "2_1_1");

        commonBeanModelMap.put("1.41PortInfo", "2_1_1");
        commonBeanModelMap.put("1.41CallProperty", "2_1_1");
        commonBeanModelMap.put("1.41StubProperty", "2_1_1");
        commonBeanModelMap.put("1.41ServiceQname", "2_1_1");
        commonBeanModelMap.put("1.41WsdlPort", "2_1_1");
        commonBeanModelMap.put("1.41MessageSecurity", "2_1_1");
        commonBeanModelMap.put("1.41MessageSecurityBinding", "2_1_1");
        
        // App client 5.0
        commonBeanModelMap.put("5.00EjbRef", "2_1_0");
        commonBeanModelMap.put("5.00ResourceRef", "2_1_0");
        commonBeanModelMap.put("5.00ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("5.00ServiceRef", "2_1_1");
        commonBeanModelMap.put("5.00LoginConfig", "2_1_1");
        commonBeanModelMap.put("5.00MessageDestination", "2_1_1");
        commonBeanModelMap.put("5.00MessageDestinationRef", "3_0_0");

        commonBeanModelMap.put("5.00PortInfo", "2_1_1");
        commonBeanModelMap.put("5.00CallProperty", "2_1_1");
        commonBeanModelMap.put("5.00StubProperty", "2_1_1");
        commonBeanModelMap.put("5.00ServiceQname", "2_1_1");
        commonBeanModelMap.put("5.00WsdlPort", "2_1_1");
        commonBeanModelMap.put("5.00MessageSecurity", "2_1_1");
        commonBeanModelMap.put("5.00MessageSecurityBinding", "2_1_1");
        
        // Application 1.3
        commonBeanModelMap.put("1.30SecurityRoleMapping", "2_1_0");

        // Application 1.4
        commonBeanModelMap.put("1.40SecurityRoleMapping", "2_1_0");

        // Application 5.0
        commonBeanModelMap.put("5.00SecurityRoleMapping", "3_0_0");

        // EjbJar 2.0
        commonBeanModelMap.put("2.00SecurityRoleMapping", "2_1_0");
        commonBeanModelMap.put("2.00WebserviceDescription", "2_1_1");
        commonBeanModelMap.put("2.00MessageDestination", "2_1_1");
        commonBeanModelMap.put("2.00ResourceRef", "2_1_0");
        commonBeanModelMap.put("2.00EjbRef", "2_1_0");
        commonBeanModelMap.put("2.00ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("2.00ServiceRef", "2_1_1");
        commonBeanModelMap.put("2.00LoginConfig", "2_1_1");
        commonBeanModelMap.put("2.00WebserviceEndpoint", "2_1_1");
        commonBeanModelMap.put("2.00DefaultResourcePrincipal", "2_1_0");

        commonBeanModelMap.put("3.00PortInfo", "2_1_1");
        commonBeanModelMap.put("3.00CallProperty", "2_1_1");
        commonBeanModelMap.put("3.00StubProperty", "2_1_1");
        commonBeanModelMap.put("3.00ServiceQname", "2_1_1");
        commonBeanModelMap.put("3.00WsdlPort", "2_1_1");
        commonBeanModelMap.put("3.00MessageSecurity", "2_1_1");
        commonBeanModelMap.put("3.00MessageSecurityBinding", "2_1_1");
        
        // EjbJar 2.1
        commonBeanModelMap.put("2.10SecurityRoleMapping", "2_1_0");
        commonBeanModelMap.put("2.10WebserviceDescription", "2_1_1");
        commonBeanModelMap.put("2.10MessageDestination", "2_1_1");
        commonBeanModelMap.put("2.10ResourceRef", "2_1_0");
        commonBeanModelMap.put("2.10EjbRef", "2_1_0");
        commonBeanModelMap.put("2.10ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("2.10ServiceRef", "2_1_1");
        commonBeanModelMap.put("2.10LoginConfig", "2_1_1");
        commonBeanModelMap.put("2.10WebserviceEndpoint", "2_1_1");
        commonBeanModelMap.put("2.10DefaultResourcePrincipal", "2_1_0");

        commonBeanModelMap.put("2.10PortInfo", "2_1_1");
        commonBeanModelMap.put("2.10CallProperty", "2_1_1");
        commonBeanModelMap.put("2.10StubProperty", "2_1_1");
        commonBeanModelMap.put("2.10ServiceQname", "2_1_1");
        commonBeanModelMap.put("2.10WsdlPort", "2_1_1");
        commonBeanModelMap.put("2.10MessageSecurity", "2_1_1");
        commonBeanModelMap.put("2.10MessageSecurityBinding", "2_1_1");
        
        // EjbJar 2.11
        commonBeanModelMap.put("2.11SecurityRoleMapping", "2_1_0");
        commonBeanModelMap.put("2.11WebserviceDescription", "2_1_1");
        commonBeanModelMap.put("2.11MessageDestination", "2_1_1");
        commonBeanModelMap.put("2.11ResourceRef", "2_1_0");
        commonBeanModelMap.put("2.11EjbRef", "2_1_0");
        commonBeanModelMap.put("2.11ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("2.11ServiceRef", "2_1_1");
        commonBeanModelMap.put("2.11LoginConfig", "2_1_1");
        commonBeanModelMap.put("2.11WebserviceEndpoint", "2_1_1");
        commonBeanModelMap.put("2.11DefaultResourcePrincipal", "2_1_0");
        commonBeanModelMap.put("2.11MethodParams", "2_1_1");

        commonBeanModelMap.put("2.11PortInfo", "2_1_1");
        commonBeanModelMap.put("2.11CallProperty", "2_1_1");
        commonBeanModelMap.put("2.11StubProperty", "2_1_1");
        commonBeanModelMap.put("2.11ServiceQname", "2_1_1");
        commonBeanModelMap.put("2.11WsdlPort", "2_1_1");
        commonBeanModelMap.put("2.11MessageSecurity", "2_1_1");
        commonBeanModelMap.put("2.11MessageSecurityBinding", "2_1_1");
        
        // EjbJar 3.0
        commonBeanModelMap.put("3.00SecurityRoleMapping", "3_0_0");
        commonBeanModelMap.put("3.00WebserviceDescription", "2_1_1");
        commonBeanModelMap.put("3.00MessageDestination", "2_1_1");
        commonBeanModelMap.put("3.00ResourceRef", "2_1_0");
        commonBeanModelMap.put("3.00EjbRef", "2_1_0");
        commonBeanModelMap.put("3.00ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("3.00ServiceRef", "2_1_1");
        commonBeanModelMap.put("3.00LoginConfig", "3_0_0");
        commonBeanModelMap.put("3.00MessageDestinationRef", "3_0_0");
        commonBeanModelMap.put("3.00WebserviceEndpoint", "3_0_0");
        commonBeanModelMap.put("3.00DefaultResourcePrincipal", "2_1_0");
        commonBeanModelMap.put("3.00MethodParams", "2_1_1");

        commonBeanModelMap.put("3.00PortInfo", "2_1_1");
        commonBeanModelMap.put("3.00CallProperty", "2_1_1");
        commonBeanModelMap.put("3.00StubProperty", "2_1_1");
        commonBeanModelMap.put("3.00ServiceQname", "2_1_1");
        commonBeanModelMap.put("3.00WsdlPort", "2_1_1");
        commonBeanModelMap.put("3.00MessageSecurity", "2_1_1");
        commonBeanModelMap.put("3.00MessageSecurityBinding", "2_1_1");
        
        // Servlet 2.3
        commonBeanModelMap.put("2.30SecurityRoleMapping", "2_1_0");
        commonBeanModelMap.put("2.30ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("2.30ResourceRef", "2_1_0");
        commonBeanModelMap.put("2.30EjbRef", "2_1_0");
        commonBeanModelMap.put("2.30ServiceRef", "2_1_1");
        commonBeanModelMap.put("2.30LoginConfig", "2_1_1");
        commonBeanModelMap.put("2.30MessageDestination", "2_1_1");
        commonBeanModelMap.put("2.30WebserviceDescription", "2_1_1");
        commonBeanModelMap.put("2.30WebserviceEndpoint", "2_1_1");
        
        commonBeanModelMap.put("2.30PortInfo", "2_1_1");
        commonBeanModelMap.put("2.30CallProperty", "2_1_1");
        commonBeanModelMap.put("2.30StubProperty", "2_1_1");
        commonBeanModelMap.put("2.30ServiceQname", "2_1_1");
        commonBeanModelMap.put("2.30WsdlPort", "2_1_1");
        commonBeanModelMap.put("2.30MessageSecurity", "2_1_1");
        commonBeanModelMap.put("2.30MessageSecurityBinding", "2_1_1");

        // Servlet 2.4
        commonBeanModelMap.put("2.40SecurityRoleMapping", "2_1_0");
        commonBeanModelMap.put("2.40EjbRef", "2_1_0");
        commonBeanModelMap.put("2.40ResourceRef", "2_1_0");
        commonBeanModelMap.put("2.40ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("2.40ServiceRef", "2_1_1");
        commonBeanModelMap.put("2.40LoginConfig", "2_1_1");
        commonBeanModelMap.put("2.40MessageDestination", "2_1_1");
        commonBeanModelMap.put("2.40WebserviceDescription", "2_1_1");
        commonBeanModelMap.put("2.40WebserviceEndpoint", "2_1_1");

        commonBeanModelMap.put("2.40PortInfo", "2_1_1");
        commonBeanModelMap.put("2.40CallProperty", "2_1_1");
        commonBeanModelMap.put("2.40StubProperty", "2_1_1");
        commonBeanModelMap.put("2.40ServiceQname", "2_1_1");
        commonBeanModelMap.put("2.40WsdlPort", "2_1_1");
        commonBeanModelMap.put("2.40MessageSecurity", "2_1_1");
        commonBeanModelMap.put("2.40MessageSecurityBinding", "2_1_1");

        // Servlet 2.41
        commonBeanModelMap.put("2.41SecurityRoleMapping", "2_1_0");
        commonBeanModelMap.put("2.41EjbRef", "2_1_0");
        commonBeanModelMap.put("2.41ResourceRef", "2_1_0");
        commonBeanModelMap.put("2.41ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("2.41ServiceRef", "2_1_1");
        commonBeanModelMap.put("2.41LoginConfig", "2_1_1");
        commonBeanModelMap.put("2.41MessageDestination", "2_1_1");
        commonBeanModelMap.put("2.41WebserviceDescription", "2_1_1");
        commonBeanModelMap.put("2.41WebserviceEndpoint", "2_1_1");

        commonBeanModelMap.put("2.41PortInfo", "2_1_1");
        commonBeanModelMap.put("2.41CallProperty", "2_1_1");
        commonBeanModelMap.put("2.41StubProperty", "2_1_1");
        commonBeanModelMap.put("2.41ServiceQname", "2_1_1");
        commonBeanModelMap.put("2.41WsdlPort", "2_1_1");
        commonBeanModelMap.put("2.41MessageSecurity", "2_1_1");
        commonBeanModelMap.put("2.41MessageSecurityBinding", "2_1_1");

        // Servlet 2.5
        commonBeanModelMap.put("2.50SecurityRoleMapping", "3_0_0");
        commonBeanModelMap.put("2.50EjbRef", "2_1_0");
        commonBeanModelMap.put("2.50ResourceRef", "2_1_0");
        commonBeanModelMap.put("2.50ResourceEnvRef", "2_1_0");
        commonBeanModelMap.put("2.50ServiceRef", "2_1_1");
        commonBeanModelMap.put("2.50LoginConfig", "2_1_1");
        commonBeanModelMap.put("2.50MessageDestination", "2_1_1");
        commonBeanModelMap.put("2.50MessageDestinationRef", "3_0_0");
        commonBeanModelMap.put("2.50WebserviceDescription", "2_1_1");

        commonBeanModelMap.put("2.50PortInfo", "2_1_1");
        commonBeanModelMap.put("2.50CallProperty", "2_1_1");
        commonBeanModelMap.put("2.50StubProperty", "2_1_1");
        commonBeanModelMap.put("2.50ServiceQname", "2_1_1");
        commonBeanModelMap.put("2.50WsdlPort", "2_1_1");
        commonBeanModelMap.put("2.50MessageSecurity", "2_1_1");
        commonBeanModelMap.put("2.50MessageSecurityBinding", "2_1_1");
    }        
}
