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

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFunctionDefinitionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmFunctionDefinitionResolver.class)
public class FunctionDefinitionResolverImpl extends CsmFunctionDefinitionResolver {

    @Override
    public Collection<CsmOffsetableDeclaration> findDeclarationByName(CsmProject project, String name) {
        if (project instanceof ProjectBase) {
            Collection<CsmOffsetableDeclaration> decls = ((ProjectBase) project).findDeclarationsByPrefix(""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION) + OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + name);
            decls.addAll(((ProjectBase) project).findDeclarationsByPrefix(""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) + OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + name));
            return decls;
        } else {
            return null;
        }
    }

    @Override
    public CsmReference getFunctionDefinition(CsmFunction referencedFunction) {
        CsmFile containingFile = referencedFunction.getContainingFile();
        Iterator<CsmOffsetableDeclaration> externalDeclarations = CsmSelect.getExternalDeclarations(containingFile);
        if (externalDeclarations != null) {
            while(externalDeclarations.hasNext()) {
                CsmOffsetableDeclaration next = externalDeclarations.next();
                if (next.getStartOffset() == referencedFunction.getStartOffset() && !referencedFunction.equals(next)) {
                    if (CsmKindUtilities.isFunctionDeclaration(next) &&
                        CharSequenceUtilities.textEquals(referencedFunction.getSignature(), ((CsmFunction)next).getSignature())) {
                        CsmFunction f = (CsmFunction) next;
                        CsmFunctionDefinition definition = f.getDefinition();
                        if (definition != null) {
                            return new CsmReferenceImpl(definition);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static class CsmReferenceImpl implements CsmReference {
        private final CsmFunctionDefinition definition;

        public CsmReferenceImpl(CsmFunctionDefinition definition) {
            this.definition = definition;
        }

        @Override
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DEFINITION;
        }

        @Override
        public CsmObject getReferencedObject() {
            return definition;
        }

        @Override
        public CsmObject getOwner() {
            return definition;
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return definition;
        }

        @Override
        public CsmFile getContainingFile() {
            return definition.getContainingFile();
        }

        @Override
        public int getStartOffset() {
            return definition.getStartOffset();
        }

        @Override
        public int getEndOffset() {
            return definition.getEndOffset();
        }

        @Override
        public CsmOffsetable.Position getStartPosition() {
            return definition.getStartPosition();
        }

        @Override
        public CsmOffsetable.Position getEndPosition() {
            return definition.getEndPosition();
        }

        @Override
        public CharSequence getText() {
            return definition.getText();
        }
    }
}
