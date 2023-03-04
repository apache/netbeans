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
package org.netbeans.modules.profiler.api.java;

import java.lang.reflect.Modifier;

/**
 * A simplified java method descriptor
 */
/**
 *
 * @author Jaroslav Bachorik
 */
public class SourceMethodInfo {
    private String className, name, signature, vmName;
    private boolean execFlag;
    private final int modifiers;

    public SourceMethodInfo(String className, String name, String signature, String vmName, boolean execFlag, int modifiers) {
        this.className = className;
        this.name = name;
        this.signature = signature;
        this.vmName = vmName;
        this.execFlag = execFlag;
        this.modifiers = modifiers;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SourceMethodInfo other = (SourceMethodInfo) obj;
        if ((this.className == null) ? (other.className != null) : !this.className.equals(other.className)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.signature == null) ? (other.signature != null) : !this.signature.equals(other.signature)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.className != null ? this.className.hashCode() : 0);
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 73 * hash + (this.signature != null ? this.signature.hashCode() : 0);
        return hash;
    }
    
    /**
     *
     * @return Returns the containing class FQN
     */
    public final String getClassName() {
        return className;
    }

    /**
     *
     * @return Returns the method name
     */
    public final String getName() {
        return name;
    }

    /**
     *
     * @return Returns the method signature
     */
    public final String getSignature() {
        return signature;
    }

    /**
     *
     * @return Returns the VM internal method name
     */
    public final String getVMName() {
        return vmName;
    }

    /**
     *
     * @return Returns TRUE if the method is executable (eg. main(String[]) or JSP main method)
     */
    public final boolean isExecutable() {
        return execFlag;
    }
    
    /**
     * 
     * @return Returns method's modifiers in the {@linkplain Modifier} format
     */
    public final int getModifiers() {
        return modifiers;
    }
    
}
