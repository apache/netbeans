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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.openide.util.CharSequences;

/**
 *
 */
public final class PersistentKey {
    private static final byte UID = 1<<0;
    private static final byte DECLARATION = 1<<1;
    
    private final Object key;
    private CsmProject project;
    private final byte kind;
    
    private PersistentKey(CsmUID id) {
        key = id;
        kind = UID;
    }
    
    private PersistentKey(CharSequence id, CsmProject host,  byte type) {
        key = id;
        project = host;
        kind = type;
    }
    
    public static PersistentKey createKey(CsmOffsetableDeclaration decl){
        CharSequence name = decl.getName();
        CharSequence uniq = decl.getUniqueName();
        CsmProject project = decl.getContainingFile().getProject();
        if (name.length() > 0 && CharSequences.indexOf(uniq, "::::") < 0 && project != null){ // NOI18N
            return new PersistentKey(uniq, project, DECLARATION);
        } else {
            //System.out.println("Skip "+uniq);
        }
        // obtain UID directly without "null-check", because notificator works
        // in separate thread which can be run after closed project
        return new PersistentKey(UIDProviderIml.get(decl, false));
    }
    
    public Object getObject(){
        switch(kind){
            case UID:
                return ((CsmUID)key).getObject();
            case DECLARATION:
                return project.findDeclaration((CharSequence)key);
        }
        return null;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof PersistentKey){
            PersistentKey what = (PersistentKey) object;
            if (kind != what.kind) {
                return false;
            }
            switch(kind){
                case UID:
                    return key.equals(what.key);
                case DECLARATION:
                    if (project.equals(what.project)) {
                        return CharSequences.comparator().compare((CharSequence)key,(CharSequence)what.key)==0;
                    }
                    return false;
            }
        }
        return super.equals(object);
    }
    
    @Override
    public int hashCode() {
        switch(kind){
            case UID:
                return key.hashCode();
            case DECLARATION:
                return project.hashCode() ^ key.hashCode();
        }
        return super.hashCode();
    }
}
