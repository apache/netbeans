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

package org.netbeans.modules.websvc.saas.codegen.model;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.wadl.Option;

/**
 *
 * @author Peter Liu
 */
public class ParameterInfo {

    private String id;
    private String name;
    private Class type;
    private String typeName;
    private Object defaultValue;
    private QName qname;
    private ParamStyle style;
    private List<Option> option;
    private boolean required;
    private boolean repeating;
    private String fixed;
    private boolean isApiKey;
    private boolean isSessionKey;

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
        if(isQualifiedParameterType(type))
            this.style = ParamStyle.QUERY;
        else
            this.style = ParamStyle.UNKNOWN;
    }

    private static boolean isQualifiedParameterType(Class type) {
        return type.isPrimitive() || type.equals(String.class) ||
               Util.getValueOfMethod(type) != null || 
               Util.getConstructorWithStringParam(type) != null;
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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

    public boolean isApiKey() {
        return isApiKey;
    }
    
    public void setIsApiKey(boolean isApiKey) {
        this.isApiKey = isApiKey;
    }

    public boolean isSessionKey() {
        return isSessionKey;
    }

    public void setIsSessionKey(boolean isSessionKey) {
        this.isSessionKey = isSessionKey;
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

    public String getFixed() {
        return fixed;
    }

    public void setFixed(String fixed) {
        this.fixed = fixed;
    }
    
    public boolean isRepeating() {
        return repeating;
    }
    
    public void setIsRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public boolean isRequired() {
        return required;
    }
    
    public void setIsRequired(boolean required) {
        this.required = required;
    }

    public List<Option> getOption() {
        return option;
    }
    
    public void setOption(List<Option> option) {
        this.option = option;
    }
    
    public ParamStyle getStyle() {
        return style;
    }
    
    public void setStyle(ParamStyle style) {
        this.style = style;
    }

    public boolean isFixed() {
        return getFixed() != null;
    }
    
    private Object generateDefaultValue() {
        if (type == Integer.class || type == Integer.TYPE) {
            return 0;
        } else if (type == Short.class || type == Short.TYPE) {
            return (short)0;
        } else if (type == Long.class || type == Long.TYPE) {
            return 0L;
        } else if (type == Float.class || type == Float.TYPE) {
            return 0F;
        } else if (type == Double.class || type == Double.TYPE) {
            return 0D;
        } else if (type == Boolean.class || type == Boolean.TYPE) {
            return Boolean.FALSE;
        } else if (type == Character.class || type == Character.TYPE) {
            return '\0';
        }
        
        return null;
    }
    
    public enum ParamStyle {
        UNKNOWN(""), 
        PLAIN("plain"),
        TEMPLATE("template"),
        MATRIX("matrix"),
        HEADER("header"),
        QUERY("query");
        
        private String value;
        
        ParamStyle(String value) {
            this.value = value;
        }
        
        public String value() {
            return value;
        }
        
        public static ParamStyle fromValue(String v) {
            for (ParamStyle c: ParamStyle.values()) {
                if (c.value.equals(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }
    }
    
    public enum ParamFilter {
        FIXED("fixed");
        
        private String value;
        
        ParamFilter(String value) {
            this.value = value;
        }
        
        public String value() {
            return value;
        }
        
        public static ParamStyle fromValue(String v) {
            for (ParamStyle c: ParamStyle.values()) {
                if (c.value.equals(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }
    }
}
