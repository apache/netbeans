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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.SpecializationDescriptor.SpecializationParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.TypeFactory.TypeBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Template specialization parameter based on type implementation.
 *
 */
public final class TypeBasedSpecializationParameterImpl extends OffsetableBase implements CsmTypeBasedSpecializationParameter, SelfPersistent {
    
    private final CsmUID<CsmScope> scope;

    private final CsmType type;

    public TypeBasedSpecializationParameterImpl(CsmType type, CsmScope scope, CsmFile file, int start, int end) {
        super(file, start, end);
        this.type = type;
        if ((scope instanceof CsmIdentifiable)) {
            this.scope = UIDCsmConverter.scopeToUID(scope);
        } else {
            this.scope = null;
        }
    }

    public TypeBasedSpecializationParameterImpl(CsmType type, CsmScope scope) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());
        this.type = type;
        if ((scope instanceof CsmIdentifiable)) {
            this.scope = UIDCsmConverter.scopeToUID(scope);
        } else {
            this.scope = null;
        }
    }

    @Override
    public CsmScope getScope() {
        return scope == null? null : scope.getObject();
    }

    @Override
    public CsmType getType() {
        return type;
    }

    @Override
    public CsmClassifier getClassifier() {
        return type.getClassifier();
    }

    @Override
    public CharSequence getClassifierText() {
        return type.getClassifierText();
    }

    @Override
    public boolean isInstantiation() {
        return type.isInstantiation();
    }

    @Override
    public boolean hasInstantiationParams() {
        return type.hasInstantiationParams();
    }

    @Override
    public List<CsmSpecializationParameter> getInstantiationParams() {
        return type.getInstantiationParams();
    }

    @Override
    public int getArrayDepth() {
        return type.getArrayDepth();
    }

    @Override
    public boolean isPointer() {
        return type.isPointer();
    }

    @Override
    public boolean isPackExpansion() {
        return type.isPackExpansion();
    }

    @Override
    public int getPointerDepth() {
        return type.getPointerDepth();
    }

    @Override
    public boolean isReference() {
        return type.isReference();
    }

    @Override
    public boolean isRValueReference() {
        return type.isRValueReference();
    }
    
    @Override
    public boolean isConst() {
        return type.isConst();
    }

    @Override
    public boolean isVolatile() {
        return type.isVolatile();
    }

    @Override
    public boolean isBuiltInBased(boolean resolveTypeChain) {
        return type.isBuiltInBased(resolveTypeChain);
    }

    @Override
    public boolean isTemplateBased() {
        return type.isTemplateBased();
    }

    @Override
    public CharSequence getCanonicalText() {
        return type.getCanonicalText();
    }

    @Override
    public CharSequence getText() {
        return type.getText();
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(super.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeBasedSpecializationParameterImpl other = (TypeBasedSpecializationParameterImpl) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return super.equals(obj);
    }

    public static class TypeBasedSpecializationParameterBuilder extends SpecializationParameterBuilder {

        private TypeBuilder typeBuilder;

        public void setTypeBuilder(TypeBuilder type) {
            this.typeBuilder = type;
        }

        @Override
        public TypeBasedSpecializationParameterImpl create() {
            TypeBasedSpecializationParameterImpl param = new TypeBasedSpecializationParameterImpl(getType(), null, getFile(), getStartOffset(), getEndOffset());
            return param;
        }
        
        private CsmType getType() {
            CsmType type = null;
            if (typeBuilder != null) {
                typeBuilder.setScope(getScope());
                type = typeBuilder.create();
            }
            if (type == null) {
                type = TypeFactory.createSimpleType(BuiltinTypes.getBuiltIn("int"), getFile(), getStartOffset(), getStartOffset()); // NOI18N
            }
            return type;
        }        
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeType(type, output);
        UIDObjectFactory.getDefaultFactory().writeUID(scope, output);
    }

    public TypeBasedSpecializationParameterImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.type = PersistentUtils.readType(input);
        this.scope = UIDObjectFactory.getDefaultFactory().readUID(input);
    }

}
