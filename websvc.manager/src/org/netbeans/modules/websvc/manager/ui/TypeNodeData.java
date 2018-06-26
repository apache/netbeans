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


package org.netbeans.modules.websvc.manager.ui;

/**
 * This class represents the data for each node in the TreeTable.
 *
 * @author  David Botterill
 */
public class TypeNodeData {
    public static final int IN = 0;
    public static final int OUT = 1;
    public static final int IN_OUT = 2;
    
    private String typeName;
    private Object typeValue;
    private String typeClass;
    private String genericType;
    private int holderType = IN;
    private boolean assignable = true;
    
    public TypeNodeData() {
        
    }
    
    public TypeNodeData(String inType, String inParameterName) {
        this(inType, null, inParameterName, null);
    }
    
    public TypeNodeData(String inType, Object parameterValue) {
        this(inType, null, null, parameterValue);
    }
    
    public TypeNodeData(String inType, String genericType, String inParameterName, Object parameterValue) {
        this.typeClass = inType;
        this.typeName = inParameterName;
        this.genericType = genericType;
        this.typeValue = parameterValue;
    }
    
    public void setTypeClass(String inType) {
        typeClass=inType;
    }

    public boolean isAssignable() {
        return assignable;
    }

    public void setAssignable(boolean assignable) {
        this.assignable = assignable;
    }
    
    public String getTypeClass() {
        return typeClass;
    }
    
    public String getGenericType() {
        return genericType;
    }
    
    public void setGenericType(String innerType) {
        this.genericType = innerType;
    }
    
    public void setTypeName(String inParameterName) {
        typeName=inParameterName;
    }
    
    public String getTypeName() {
        return typeName;
    }
    public void setTypeValue(Object inValue) {
        typeValue=inValue;
    }
    
    public Object getTypeValue() {
        return typeValue;
    }
    
    public String getRealTypeName() {
        if (ReflectionHelper.isArray(typeClass)) {
            return typeClass;
        }else if (genericType != null && genericType.length() > 0) {
            return typeClass + "<" + genericType + ">";
        }else {
            return typeClass;
        }
    }
    
    public int getHolderType() {
        return holderType;
    }
    
    public void setHolderType(int mode) {
        this.holderType = mode;
    }
}
