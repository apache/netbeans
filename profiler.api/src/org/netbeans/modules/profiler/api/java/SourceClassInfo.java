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
package org.netbeans.modules.profiler.api.java;

import java.util.Comparator;
import java.util.Set;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;

/**
 * A simplified java class descriptor
 */
/**
 *
 * @author Jaroslav Bachorik
 */
abstract public class SourceClassInfo {
    final public static Comparator<SourceClassInfo> COMPARATOR = new Comparator<SourceClassInfo>() {
        @Override
        public int compare(SourceClassInfo o1, SourceClassInfo o2) {
            return o1.getVMName().compareTo(o2.getVMName());
        }
    };
    
    final private static Pattern anonymousInnerClassPattern = Pattern.compile(".*?\\$[0-9]*$");
    
    private String simpleName, qualName, vmName;
    
    public SourceClassInfo(String name, String fqn, String vmName) {
        this.simpleName = name;
        this.qualName = fqn;
        this.vmName = vmName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SourceClassInfo other = (SourceClassInfo) obj;
        if ((this.vmName == null) ? (other.vmName != null) : !this.vmName.equals(other.vmName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.vmName != null ? this.vmName.hashCode() : 0);
        return hash;
    }
    
    /**
     *
     * @return Returns the class simple name (the last part of the FQN)
     */
    final public String getSimpleName() {
        return simpleName;
    }

    /**
     *
     * @return Returns the class FQN
     */
    final public String getQualifiedName() {
        return qualName;
    }

    /**
     *
     * @return Returns the VM internal class name
     */
    final public String getVMName() {
        return vmName;
    }

    /**
     * 
     * @return Returns true if the class is an anonymous inner class, false otherwise
     */
    public boolean isAnonymous() {
        return isAnonymous(qualName);
    }
    
    abstract public FileObject getFile();
    abstract public Set<SourceMethodInfo> getMethods(boolean all);
    abstract public Set<SourceClassInfo> getSubclasses();
    abstract public Set<SourceClassInfo> getInnerClases();
    abstract public Set<SourceMethodInfo> getConstructors();
    abstract public SourceClassInfo getSuperType();
    abstract public Set<SourceClassInfo> getInterfaces();
    
    final protected boolean isAnonymous(String className) {
        return anonymousInnerClassPattern.matcher(className).matches();
    }
}
