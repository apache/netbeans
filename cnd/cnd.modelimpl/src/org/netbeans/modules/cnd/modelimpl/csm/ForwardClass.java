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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * A dummy class that is added to model in the case
 * there is a forward class declaration that does not refer to a class.
 * ForwardClass can be created i.e. for the following constructions:
 * class Fwd1; // as ::Fwd1
 * namespace N {
 * class Outer {
 *      class InnerFwd2; // as N::Outer::InnerFwd2
 *      struct Fwd3 *p; // as N::Fwd3
 * }
 * }
 * class N::Outer::InnerFwd2 {
 *      int var;
 * }
 */
public final class ForwardClass extends ClassImpl {

    private ForwardClass(NameHolder name, AST ast, CsmFile file, int start, int end) {
        super(name, ast, file, start, end);
    }

    private ForwardClass(NameHolder name, CsmDeclaration.Kind kind, CsmFile file, int start, int end) {
        super(name, kind, start, file, start, end);
    }
    
    public static boolean isForwardClass(CsmObject cls) {
        return cls instanceof ForwardClass;
    }

    public static ForwardClass createIfNeeded(CharSequence name, CsmFile file, AST ast, int start, int end, CsmScope scope, boolean registerInProject) {
        ForwardClass fwd = new ForwardClass(NameHolder.createName(name), ast, file, start, end);
        fwd.initQualifiedName(scope);
        if (fwd.getProject().findClassifier(fwd.getQualifiedName()) == null) {
            fwd.setTemplateDescriptor(TemplateDescriptor.createIfNeeded(ast, file, fwd, registerInProject));
            fwd.initScope(scope);
            if(registerInProject) {
                fwd.register(scope, false);
            } else {
                Utils.setSelfUID(fwd);
            }
            return fwd;
        }
        return null;
    }

    @Override
    public boolean shouldBeReplaced(CsmClassifier another) {
        if (another == null) {
            return true;
        } else if (another instanceof ForwardClass) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return Collections.emptyList();
    }

    @Override
    public List<CsmInheritance> getBaseClasses() {
        return Collections.emptyList();
    }

    @Override
    public Collection<CsmFriend> getFriends() {
        return Collections.emptyList();
    }

    @Override
    public int getLeftBracketOffset() {
        return getEndOffset() - 1;
    }

    @Override
    public Collection<CsmMember> getMembers() {
        return Collections.emptyList();
    }

    public CharSequence getSimpleQualifiedName() {
        CharSequence name = getQualifiedName();        
        int pos = CharSequences.indexOf(name, "<"); //NOI18N // FIXME
        if (pos > 0) {
            name = name.subSequence(0, pos);
        }
        return name;
    }

    @Override
    public String toString() {
        return "DUMMY_FORWARD " + super.toString(); // NOI18N
    }

    public static class ForwardClassBuilder extends SimpleDeclarationBuilder {

        private CsmDeclaration.Kind kind = CsmDeclaration.Kind.CLASS;

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        @Override
        public ForwardClass create() {
            ForwardClass fwd = new ForwardClass(getNameHolder(), kind, getFile(), getStartOffset(), getEndOffset());
            fwd.initQualifiedName(getScope());
            if(getTemplateDescriptorBuilder() != null) {
                getTemplateDescriptorBuilder().setScope(getScope());
                fwd.setTemplateDescriptor(getTemplateDescriptorBuilder().create());
            }
            if (fwd.getProject().findClassifier(fwd.getQualifiedName()) == null) {
                fwd.initScope(getScope());
                if(isGlobal()) {
                    fwd.register(getScope(), false);
                } else {
                    Utils.setSelfUID(fwd);
                }
                return fwd;
            }
            return null;            
        }
        
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }

    public ForwardClass(RepositoryDataInput input) throws IOException {
        super(input);
    }
}
