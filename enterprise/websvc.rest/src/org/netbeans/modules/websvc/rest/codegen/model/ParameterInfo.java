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

package org.netbeans.modules.websvc.rest.codegen.model;

import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.openide.util.Utilities;

/**
 *
 * @author Peter Liu
 */
public class ParameterInfo {

    private String name;
    private Class type;
    private String typeName;
    private Object defaultValue;
    private boolean isQueryParam;
    private QName qname;

    public ParameterInfo(String name, Class type) {
        this(name, type, null);
    }
    
    public ParameterInfo(QName qname, Class type, String typeName) {
        this(qname.getLocalPart(), type, typeName);
        this.qname = qname;
    }
    
    public ParameterInfo(String name, Class type, String typeName) {
        this.name = name;
        this.type = type;
        this.typeName = typeName;
        this.defaultValue = null;
        this.isQueryParam = isQualifiedParameterType(type);
    }

    private static boolean isQualifiedParameterType(Class type) {
        return type.isPrimitive() || type.equals(String.class) ||
               Utils.getValueOfMethod(type) != null || 
               Utils.getConstructorWithStringParam(type) != null;
    }
    
    public String getName() {
        return name;
    }
    
    public QName getQName() {
        if (qname == null) {
            qname = new QName(name);
        }
        return qname;
    }

    public Class getType() {
        return type;
    }

    public String getTypeName() {
        if (typeName == null) {
            return type.getName();
        }
        return typeName;
    }

    public String getSimpleTypeName() {
        return type.getSimpleName();
    }
    
    public void setDefaultValue(Object value) {
        this.defaultValue = value;
    }

    public Object getDefaultValue() {
        if (defaultValue == null) {
            defaultValue = generateDefaultValue();
        }
        return defaultValue;
    }

    public boolean isQueryParam() {
        return isQueryParam;
    }

    public void setIsQueryParam(boolean flag) {
        this.isQueryParam = flag;
    }
    
    private Object generateDefaultValue() {
        if (type == Integer.class || type == Short.class || type == Long.class ||
                type == Float.class || type == Double.class) {
            try {
                return type.getConstructor(String.class).newInstance("0"); //NOI18N
            } catch (Exception ex) {
                return null;
            }
        }
        
        if (type == Boolean.class) {
            return Boolean.FALSE;
        }
    
        if (type == Character.class) {
            return '\0';
        }
        
        return null;
    }
}
