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
