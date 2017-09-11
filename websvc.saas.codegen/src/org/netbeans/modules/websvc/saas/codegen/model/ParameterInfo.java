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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            return new Integer(0);
        } else if (type == Short.class || type == Short.TYPE) {
            return new Short((short) 0);
        } else if (type == Long.class || type == Long.TYPE) {
            return new Long(0);
        } else if (type == Float.class || type == Float.TYPE) {
            return new Float(0);
        } else if (type == Double.class || type == Double.TYPE) {
            return new Double(0);
        } else if (type == Boolean.class || type == Boolean.TYPE) {
            return Boolean.FALSE;
        } else if (type == Character.class || type == Character.TYPE) {
            return new Character('\0');
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
