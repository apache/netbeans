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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 */
public class TemplateParameterTypeImpl implements CsmType, CsmTemplateParameterType, SelfPersistent {
    private final CsmType type;
    private final CsmUID<CsmTemplateParameter> parameter;
    
    public TemplateParameterTypeImpl(CsmType type, CsmTemplateParameter parameter) {
        this.type = type;
        this.parameter = UIDCsmConverter.objectToUID(parameter);
    }

    TemplateParameterTypeImpl(TemplateParameterTypeImpl type, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile) {
        this.type = TypeFactory.createType(type.type, pointerDepth, reference, arrayDepth, _const, _volatile);
        this.parameter = type.parameter;
    }

    TemplateParameterTypeImpl(TemplateParameterTypeImpl type, List<CsmSpecializationParameter> instantiationParams) {
        this.type = TypeFactory.createType(type.type, instantiationParams);
        this.parameter = type.parameter;
    }

    @Override
    public CsmTemplateParameter getParameter() {
        return UIDCsmConverter.UIDtoCsmObject(this.parameter);
    }

    @Override
    public CsmType getTemplateType() {
        return type;
    }

    @Override
    public CsmFile getContainingFile() {
        return type.getContainingFile();
    }

    @Override
    public int getEndOffset() {
        return type.getEndOffset();
    }

    @Override
    public Position getEndPosition() {
        return type.getEndPosition();
    }

    @Override
    public CharSequence getClassifierText() {
        return type.getClassifierText();
    }

    @Override
    public int getStartOffset() {
        return type.getStartOffset();
    }

    @Override
    public Position getStartPosition() {
        return type.getStartPosition();
    }

    @Override
    public CharSequence getText() {
        return type.getText();
    }

    @Override
    public int getArrayDepth() {
        return type.getArrayDepth();
    }

    @Override
    public CharSequence getCanonicalText() {
        return type.getCanonicalText();
    }

    @Override
    public CsmClassifier getClassifier() {
        CsmTemplateParameter ref = UIDCsmConverter.UIDtoCsmObject(parameter);
        if (CsmKindUtilities.isClassifier(ref)) {
            return (CsmClassifier) ref;
        }
        return type.getClassifier(); // fallback
    }

    @Override
    public int getPointerDepth() {
        return type.getPointerDepth();
    }

    @Override
    public boolean isBuiltInBased(boolean resolveTypeChain) {
        return type.isBuiltInBased(resolveTypeChain);
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
    public boolean isPointer() {
        return type.isPointer();
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
    public boolean isPackExpansion() {
        return type.isPackExpansion();
    }

    @Override
    public List<CsmSpecializationParameter> getInstantiationParams() {
        return type.getInstantiationParams();
    }

    @Override
    public boolean hasInstantiationParams() {
        return type.hasInstantiationParams();
    }

    @Override
    public boolean isInstantiation() {
        return type.isInstantiation();
    }

    @Override
    public boolean isTemplateBased() {
        return true;
    }

    // package
    CharSequence getOwnText() {
        if (type instanceof TypeImpl) {
            return ((TypeImpl) type).getOwnText();
        } else if (type instanceof TemplateParameterTypeImpl) {
            return ((TemplateParameterTypeImpl) type).getOwnText();
        } else {
            return "";
        }
    }
    
    @Override
    public String toString() {
        return "TEMPLATE PARAMETER TYPE " + getText()  + "[" + getStartOffset() + "-" + getEndOffset() + "]"; // NOI18N;
    }    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.type);
        hash = 67 * hash + Objects.hashCode(this.parameter);
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
        final TemplateParameterTypeImpl other = (TemplateParameterTypeImpl) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.parameter, other.parameter)) {
            return false;
        }
        return true;
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        PersistentUtils.writeType(type, output);
        UIDObjectFactory.getDefaultFactory().writeUID(parameter, output);
    }  
    
    public TemplateParameterTypeImpl(RepositoryDataInput input) throws IOException {
        type = PersistentUtils.readType(input);
        parameter = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}
