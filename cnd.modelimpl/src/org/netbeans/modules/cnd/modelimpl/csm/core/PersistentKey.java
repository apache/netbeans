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
