/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.util.Map;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;


/**
 *
 * @author Peter Williams
 */
public class DDBinding implements Comparable<DDBinding> {

    // !PW Consider converting these to enums.
    public static final String PROP_NAME = "Name"; // varies...
    public static final String PROP_SESSION_TYPE = "SessionType"; // session
    public static final String PROP_DESTINATION_TYPE = "DestinationType"; // message-driven
    public static final String PROP_PERSISTENCE_TYPE = "PersistenceType"; // session
    public static final String PROP_ROLE_NAME = "RoleName"; // security-role/role-name
    public static final String PROP_RUNAS_ROLE = "RunAsRole"; // run-as/role-name

    public static final String PROP_SERVICE_REF = "ServiceRef"; // service-ref
    public static final String PROP_SECURITY_ROLE = "SecurityRole"; // security-role
    public static final String PROP_EJB_REF = "EjbRef"; // ejb-ref
    public static final String PROP_RESOURCE_REF = "ResourceRef"; // resource-ref
    public static final String PROP_RESOURCE_ENV_REF = "ResourceEnvRef"; // resource-env-ref

    public static final String PROP_MSGDEST = "MessageDestination"; // message-destination
    public static final String PROP_MSGDEST_REF = "MessageDestinationRef"; // message-destination-ref
    public static final String PROP_WEBSERVICE_DESC = "WebserviceDescription"; // webservice-description
    public static final String PROP_PORTCOMPONENT = "PortComponent"; // port-component
    public static final String PROP_SEI = "EndpointInterface"; // servlet-endpoint-interface
    public static final String PROP_WSDLPORT = "WsdlPort"; // wsdl-port
    public static final String PROP_SERVLET_LINK = "ServletLink"; // service-impl-bean/servlet-link
    public static final String PROP_EJB_LINK = "EjbLink"; // service-impl-bean/ejb-link
    
    public static final String PROP_PORTCOMPONENT_REF = "PortComponentRef"; // port-component-ref
    public static final String PROP_PORTCOMPONENT_LINK = "PortComponentLink"; // port-component-link
    
    protected final BeanResolver resolver;
    protected final CommonDDBean sunBean;
    protected final Map<String, Object> standardMap;
    protected final Map<String, Object> annotationMap;
    protected boolean virtual;

    public DDBinding(BeanResolver resolver, CommonDDBean sunBean,
            Map<String, Object> standardMap, Map<String, Object> annotationMap) {
        this(resolver, sunBean, standardMap, annotationMap, false);
    }

    public DDBinding(BeanResolver resolver, CommonDDBean sunBean,
            Map<String, Object> standardMap, Map<String, Object> annotationMap,
            boolean virtual) {
        this.resolver = resolver;
        this.sunBean = sunBean;
        this.standardMap = standardMap;
        this.annotationMap = annotationMap;
        this.virtual = virtual;
    }

    public String getBeanName() {
        return resolver.getBeanName(sunBean);
    }
    
    public String getBindingName() {
        Object value = getProperty(PROP_NAME);
        return (value instanceof String) ? (String) value : "";
    }
    
    public CommonDDBean getSunBean() {
        return sunBean;
    }
    
    public boolean hasStandardDDBinding() {
        return standardMap != null;
    }
    
    public boolean hasAnnotationBinding() {
        return annotationMap != null;
    }
    
    public boolean isBound() {
        return standardMap != null || annotationMap != null;
    }

    public boolean isAnnotated() {
        return annotationMap != null;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public void clearVirtual() {
        virtual = false;
    }

    public Object getProperty(String property) {
        return standardMap != null ? standardMap.get(property) : annotationMap != null ? annotationMap.get(property) : null;
    }
    
    public DDBinding rebind(CommonDDBean newSunBean) {
        return new DDBinding(resolver, newSunBean, standardMap, annotationMap, virtual);
    }
    
    @Override
    public int compareTo(DDBinding other) {
        return Utils.strCompareTo(getBeanName(), other.getBeanName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final DDBinding other = (DDBinding) obj;
        if (this.sunBean != other.sunBean && (this.sunBean == null || !this.sunBean.equals(other.sunBean))) {
            return false;
        }
        
        if(standardMap != other.standardMap) {
            if(standardMap == null || other.standardMap == null) {
                return false;
            }
            
            if(!compareMap(standardMap, other.standardMap)) {
                return false;
            }
        }
    
        if(annotationMap != other.annotationMap) {
            if(annotationMap == null || other.annotationMap == null) {
                return false;
            }
            
            if(!compareMap(annotationMap, other.annotationMap)) {
                return false;
            }
        }
        
        return true;
    }
    
    private static boolean compareMap(Map<String, Object> a, Map<String, Object> b) {
        if(a.size() != b.size()) {
            return false;
        }
        
        for(Map.Entry<String, Object> a_entry : a.entrySet()) {
            Object a_object = a_entry.getValue();
            Object b_object = b.get(a_entry.getKey());

            if(a_object == b_object) {
                continue;
            }
            
            if(a_object == null || b_object == null) {
                return false;
            }
            
            if(a_object instanceof String) {
                if(b_object instanceof String) {
                    if(((String) a_object).equals((String) b_object)) {
                        continue;
                    }
                }
                return false;
            } else if(a_object instanceof Map<?, ?>) {
                if(b_object instanceof Map<?, ?>) {
                    if(compareMap((Map<String, Object>) a_object, (Map<String, Object>) b_object)) {
                        continue;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.sunBean != null ? this.sunBean.hashCode() : 0);
        
        // !PW FIXME do we need to check the binding maps?  See equals().
        
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append("sun DD: ");
        builder.append(sunBean != null ? getBeanName() : "(null)");
        builder.append(", Standard DD: ");
        builder.append(standardMap != null ? standardMap.get(PROP_NAME) : "(null)");
        builder.append(", Annotation: ");
        builder.append(annotationMap != null ? annotationMap.get(PROP_NAME) : "(null)");
        return builder.toString();
    }
    
}
