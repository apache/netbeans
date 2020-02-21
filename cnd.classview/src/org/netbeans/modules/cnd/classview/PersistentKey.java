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

package org.netbeans.modules.cnd.classview;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.openide.util.CharSequences;

/**
 *
 */
public final class PersistentKey {
    private static final byte UID = 1;
    private static final byte NAMESPACE = 1<<1;
    private static final byte DECLARATION = 1<<2;
    private static final byte PROJECT = 1<<3;
    private static final byte PROJECT_LIBS = 1<<4;
    private static final byte STATE = 1<<5;
    private static final byte MASK = STATE - 1;
    private final Object key;
    private final CsmProject project;
    private byte kind;

    private PersistentKey(CsmUID id, boolean state) {
        key = id;
        kind = UID;
        project = null;
        if (state) {
            kind |= STATE;
        }
    }
    
    private PersistentKey(CharSequence id, CsmProject host, byte type, boolean state) {
        key = id;
        project = host;
        kind = type;
        if (state) {
            kind |= STATE;
        }
    }

    public static PersistentKey createLibsKey(CsmProject project){
        return new PersistentKey(CharSequences.empty(), project, PROJECT_LIBS, false); // NOI18N
    }

    public static PersistentKey createGlobalNamespaceKey(CsmProject project){
        return new PersistentKey(CharSequences.empty(), project, NAMESPACE, false); // NOI18N
    }
    
    public static PersistentKey createKey(Object object){
        if (CsmKindUtilities.isNamespace(object)) {
            CsmNamespace ns = (CsmNamespace) object;
            CharSequence uniq = ns.getQualifiedName();
            CsmProject project = ns.getProject();
            if (project != null) {
                return new PersistentKey(uniq, project, NAMESPACE, false);
            }
        } else if (CsmKindUtilities.isEnumerator(object)) {
            // special hack.
        } else if (CsmKindUtilities.isOffsetableDeclaration(object)) {
            CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) object;
            CharSequence name = decl.getName();
            CharSequence uniq = decl.getUniqueName();
            CsmScope scope = decl.getScope();
            if (CsmKindUtilities.isCompoundClassifier(scope) && name.length() > 0) {
                CsmCompoundClassifier cls = (CsmCompoundClassifier) scope;
                name = cls.getName();
            }
            CsmProject project = decl.getContainingFile().getProject();
            if (name.length() > 0 && CharSequences.indexOf(uniq, "::::") < 0 && project != null){ // NOI18N
                return new PersistentKey(uniq, project, DECLARATION, getStateBit(object));
            } else {
                //System.out.println("Skip "+uniq);
            }
        } else if (CsmKindUtilities.isProject(object)){
            return new PersistentKey(null, (CsmProject)object, PROJECT, false);
        }
        return new PersistentKey(UIDs.get(object), getStateBit(object));
    }
    
    private static boolean getStateBit(Object object){
        if (object instanceof CsmTypedef){
            CsmTypedef typedef = (CsmTypedef) object;
            if (((CsmTypedef)object).isTypeUnnamed()){
                CsmClassifier cls = typedef.getType().getClassifier();
                if (cls != null && cls.getName().length()==0 && CsmKindUtilities.isCompoundClassifier(cls)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Object getObject(){
        int maskKind = kind & MASK;
        switch(maskKind){
            case UID:
                return ((CsmUID)key).getObject();
            case NAMESPACE:
                return project.findNamespace((CharSequence)key);
            case DECLARATION:
                return project.findDeclaration((CharSequence)key);
            case PROJECT:
                return project;
            case PROJECT_LIBS:
                return project;
        }
        return null;
    }

    public boolean isProjectLibs() {
        int maskKind = kind & MASK;
        return maskKind == PROJECT_LIBS;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof PersistentKey){
            PersistentKey what = (PersistentKey) object;
            if (kind != what.kind) {
                return false;
            }
            int maskKind = kind & MASK;
            switch(maskKind){
                case UID:
                    return key.equals(what.key);
                case NAMESPACE:
                case DECLARATION:
                    if (project.equals(what.project)) {
                        return CharSequences.comparator().compare((CharSequence)key,(CharSequence)what.key)==0;
                    }
                    return false;
                case PROJECT:
                    return project.equals(what.project);
                case PROJECT_LIBS:
                    return project.equals(what.project);
            }
        }
        return super.equals(object);
    }
    
    @Override
    public int hashCode() {
        int maskKind = kind & MASK;
        int res = 0;
        if ((kind & STATE) == STATE) {
            res = 17;
        }
        switch(maskKind){
            case UID:
                return key.hashCode() + res;
            case NAMESPACE:
            case DECLARATION:
                return project.hashCode() ^ key.hashCode() + res;
            case PROJECT:
                return project.hashCode() +  res;
            case PROJECT_LIBS:
                return project.hashCode() +  res;
        }
        return 0;
    }
    
    @Override
    public String toString() {
        int maskKind = kind & MASK;
        switch(maskKind){
            case UID:
                return "UID "+key.toString(); // NOI18N
            case NAMESPACE:
                return "Namespace "+key; // NOI18N
            case DECLARATION:
                return "Declaration "+key; // NOI18N
            case PROJECT:
                return "Project "+project.getName(); // NOI18N
            case PROJECT_LIBS:
                return "Libs "+project.getName(); // NOI18N
        }
        return super.toString();
    }
}
