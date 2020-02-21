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
package org.netbeans.modules.cnd.navigation.services;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 *
 */
public interface HierarchyModel extends HierarchyActions {
    
    public class Node {

        private final CsmOffsetableDeclaration object;
        private final boolean specialization;

        public Node(CsmOffsetableDeclaration object, boolean specialization) {
            this.object = object;
            this.specialization = specialization;
        }

        public CsmOffsetableDeclaration getDeclaration() {
            return object;
        }

        public CharSequence getDisplayName() {
            if (CsmKindUtilities.isTemplate(object)) {
                CsmTemplate tpl = (CsmTemplate)object;
                if (tpl.isSpecialization()) {
                    return (tpl).getDisplayName();
                }
            }
            return object.getName();
        }
        
        public boolean isSpecialization() {
            return specialization;
        }
    }

    public Collection<Node> getHierarchy(CsmClass cls);
}
