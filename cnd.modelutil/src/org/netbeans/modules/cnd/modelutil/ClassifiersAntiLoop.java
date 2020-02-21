/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
