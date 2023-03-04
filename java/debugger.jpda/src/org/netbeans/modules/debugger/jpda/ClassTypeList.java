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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.ReferenceType;

import java.util.AbstractList;
import java.util.List;

import org.netbeans.api.debugger.jpda.JPDAClassType;

/**
 * Lazy list of class types.
 * 
 * @author Martin Entlicher
 */
class ClassTypeList extends AbstractList<JPDAClassType> {
    
    private JPDADebuggerImpl debugger;
    private List<ReferenceType> classes;
    
    /** Creates a new instance of ClassTypeList */
    ClassTypeList(JPDADebuggerImpl debugger, List<ReferenceType> classes) {
        this.debugger = debugger;
        this.classes = classes;
    }
    
    List<ReferenceType> getTypes() {
        return classes;
    }
    
    public JPDAClassType get(int i) {
        return debugger.getClassType(classes.get(i));
    }
    
    public int size() {
        return classes.size();
    }
    
}
