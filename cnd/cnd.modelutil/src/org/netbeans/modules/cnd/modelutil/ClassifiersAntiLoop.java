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

package org.netbeans.modules.cnd.modelutil;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 * Analog of Set<CsmClass> used for anti loop checks
 */
public class ClassifiersAntiLoop {
    
    private Set<Object> set;
    private boolean recursion = false;

    private static final int MAX_INHERITANCE_DEPTH = 25;

    public ClassifiersAntiLoop() {
        set = new HashSet<Object>();
    }
    
    public ClassifiersAntiLoop(int capacity) {
        set = new HashSet<Object>(capacity);
    }
    
    
    public boolean add(CsmClassifier cls) {
        if(recursion) {
            return false;
        }
        if (isRecursion(cls)) {
            recursion = true;
            return false;
        }
        return set.add(cls);
    }
    
    public void remove(CsmClassifier cls) {
        set.remove(cls);
    }

    public boolean contains(CsmClassifier cls) {
        if(recursion) {
            return true;
        }
        if (isRecursion(cls)) {
            recursion = true;
            return true;
        }
        return set.contains(cls);
    }

    private static boolean isRecursion(CsmClassifier cls) {
        if(CsmKindUtilities.isInstantiation(cls)) {
            int instLevel = MAX_INHERITANCE_DEPTH;
            CsmInstantiation inst = (CsmInstantiation) cls;
            while(instLevel > 0 && CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
                inst = (CsmInstantiation) inst.getTemplateDeclaration();
                instLevel--;
            }
            if(instLevel <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return set.toString();
    }
}
