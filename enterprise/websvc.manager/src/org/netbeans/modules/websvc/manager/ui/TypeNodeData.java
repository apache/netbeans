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
