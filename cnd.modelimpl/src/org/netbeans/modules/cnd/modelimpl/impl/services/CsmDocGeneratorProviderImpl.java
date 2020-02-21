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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.editor.CsmDocGeneratorProvider;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.editor.CsmDocGeneratorProvider.class)
public class CsmDocGeneratorProviderImpl extends CsmDocGeneratorProvider {
    private static final boolean TRACE = false;
    private static final int GAP = "\n/**\n *\n */\n".length();

    @Override
    public Function getFunction(Document doc, int position) {
        final CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, true);
        if (TRACE) {
            System.err.println("CsmDocGeneratorProviderImpl ["+ position + "]" + csmFile.getClass());
            for(CsmOffsetableDeclaration decl : csmFile.getDeclarations()) {
                System.err.println("decl "+decl);
            }
        }
        final CsmOffsetableDeclaration decl = getFunction( csmFile, position);
        if (decl instanceof CsmFunction) {
            return new Function() {
                @Override
                public String getName() {
                    return ((CsmFunction)decl).getName().toString();
                }
                @Override
                public String getSignature() {
                    return ((CsmFunction)decl).getSignature().toString();
                }
                @Override
                public String getReturnType() {
                    if (CsmKindUtilities.isConstructor(decl) || CsmKindUtilities.isDestructor(decl)) {
                        return null;
                    }
                    return ((CsmFunction)decl).getReturnType().getCanonicalText().toString();
                }
                @Override
                public List<Parameter> getParametes() {
                    List<Parameter> list = new ArrayList<>();
                    for (final CsmParameter par : ((CsmFunction)decl).getParameters()){
                        list.add(new Parameter(){
                            @Override
                            public String getType() {
                                return par.getType().getCanonicalText().toString();
                            }
                            @Override
                            public String getName() {
                                return par.getName().toString();
                            }
                        });
                    }
                    return list;
                }
            };
        }
        return null;
    }

    public CsmOffsetableDeclaration getFunction(CsmFile file, int position) {
        if (file != null) {
            CsmOffsetableDeclaration best = null;
            for(CsmOffsetableDeclaration decl : file.getDeclarations()) {
                if (decl.getStartOffset() <= position && position <= decl.getEndOffset()) {
                    return getInternalDeclaration(decl, position);
                } else if (decl.getStartOffset() > position - GAP) {
                    if (best == null || best.getStartOffset() > decl.getStartOffset()){
                        best = decl;
                    }
                }
            }
            return best;
        }
        return null;
    }

    private CsmOffsetableDeclaration getInternalDeclaration(CsmOffsetableDeclaration parent, int position){
        if (CsmKindUtilities.isClass(parent)) {
            CsmClass cls = (CsmClass) parent;
            CsmOffsetableDeclaration best = null;
            for(CsmMember decl : cls.getMembers()){
                if (decl.getStartOffset() <= position && position <= decl.getEndOffset()) {
                    return getInternalDeclaration(decl, position);
                } else if (decl.getStartOffset() > position - GAP) {
                    if (best == null || best.getStartOffset() > decl.getStartOffset()){
                        best = decl;
                    }
                }
            }
            return best;
        } else if(CsmKindUtilities.isNamespaceDefinition(parent)) {
            CsmNamespaceDefinition ns = (CsmNamespaceDefinition) parent;
            CsmOffsetableDeclaration best = null;
            for(CsmOffsetableDeclaration decl : ns.getDeclarations()) {
                if (decl.getStartOffset() < position && position < decl.getEndOffset()) {
                    return getInternalDeclaration(decl, position);
                } else if (decl.getStartOffset() > position - GAP) {
                    if (best == null || best.getStartOffset() > decl.getStartOffset()){
                        best = decl;
                    }
                }
            }
            return best;
        } else if (CsmKindUtilities.isFunction(parent)) {
            return parent;
        }
        return null;
    }
}
