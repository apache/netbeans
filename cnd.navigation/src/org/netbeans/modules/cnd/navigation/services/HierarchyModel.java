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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
