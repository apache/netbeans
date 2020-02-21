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

package org.netbeans.modules.cnd.mixeddev.java.model;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.mixeddev.java.QualifiedNamePart;

/**
 *
 */
public final class JavaMethodInfo implements JavaEntityInfo {
    
    private final CharSequence name;
    
    private final List<QualifiedNamePart> qualifiedName;
    
    private final List<JavaParameterInfo> parameters;
    
    private final JavaTypeInfo returnType;
    
    private final boolean overloaded;
    
    private final boolean finalMethod;
    
    private final boolean staticMethod;
    
    private final boolean nativeMethod;

    public JavaMethodInfo(CharSequence name, 
                            List<QualifiedNamePart> qualifiedName, 
                            List<JavaParameterInfo> parameters, 
                            JavaTypeInfo returnType, 
                            boolean overloaded,
                            boolean finalMethod,
                            boolean staticMethod,
                            boolean nativeMethod) 
    {
        this.name = name;
        this.qualifiedName = Collections.unmodifiableList(qualifiedName);
        this.parameters = Collections.unmodifiableList(parameters);
        this.returnType = returnType;
        this.overloaded = overloaded;
        this.finalMethod = finalMethod;
        this.staticMethod = staticMethod;
        this.nativeMethod = nativeMethod;
    }
    
    public CharSequence getName() {
        return name;
    }    

    public List<QualifiedNamePart> getQualifiedName() {
        return qualifiedName;
    }

    public List<JavaParameterInfo> getParameters() {
        return parameters;
    }

    public JavaTypeInfo getReturnType() {
        return returnType;
    }

    public boolean isOverloaded() {
        return overloaded;
    }
    
    public boolean isFinal() {
        return finalMethod;
    }
    
    public boolean isStatic() {
        return staticMethod;
    }
    
    public boolean isNative() {
        return nativeMethod;
    }
    
    public boolean isConstructor() {
        return getReturnType() == null;
    }
}
