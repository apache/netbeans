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
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * A dummy class that is added to model in the case
 * there is a forward class declaration that does not refer to a class.
 * ForwardClass can be created i.e. for the following constructions:
 * enum Fwd1; // as ::Fwd1
 * namespace N {
 * class Outer {
 *      enum InnerFwd2; // as N::Outer::InnerFwd2
 * }
 * }
 * enum class N::Outer::InnerFwd2 {
 *      E1, E2
 * }
 */
public final class ForwardEnum extends EnumImpl {

    private ForwardEnum(CharSequence name, CharSequence qName, boolean stronglyTyped, CsmFile file, int start, int end) {
        super(name, qName, stronglyTyped, file, start, end);
    }

    public static boolean isForwardEnum(CsmObject cls) {
        return cls instanceof ForwardEnum;
    }

    public static ForwardEnum createIfNeeded(CharSequence name, CsmFile file, AST ast, int start, int end, CsmScope scope, boolean registerInProject) {
        ForwardEnum fwd = new ForwardEnum(name, name, EnumImpl.isStronglyTypedEnum(ast), file, start, end);
        fwd.initQualifiedName(scope);
        fwd.setTemplateDescriptor(TemplateDescriptor.createIfNeeded(ast, file, scope, registerInProject));
        if (fwd.getProject().findClassifier(fwd.getQualifiedName()) == null) {
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
        } else if (another instanceof ForwardEnum) {
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
    public String toString() {
        return "DUMMY_FORWARD_ENUM " + super.toString(); // NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
    }

    public ForwardEnum(RepositoryDataInput input) throws IOException {
        super(input);
    }

    @Override
    public Collection<CsmEnumerator> getEnumerators() {
        return Collections.emptyList();
    }

    private void setTemplateDescriptor(TemplateDescriptor createIfNeeded) {
        if (createIfNeeded != null) {
            throw new UnsupportedOperationException();
        }
    }
}
