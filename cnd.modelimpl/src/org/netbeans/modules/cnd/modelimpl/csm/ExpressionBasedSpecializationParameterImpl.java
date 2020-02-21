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
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.modelimpl.csm.SpecializationDescriptor.SpecializationParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase.ExpressionBuilder;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Template specialization parameter based on expression implementation.
 *
 */
public final class ExpressionBasedSpecializationParameterImpl extends OffsetableBase implements CsmExpressionBasedSpecializationParameter, SelfPersistent, Persistent {
    
    private final CsmUID<CsmScope> scope;

    private final CharSequence expression;
    
    private final boolean defaultValue;

    private ExpressionBasedSpecializationParameterImpl(CharSequence expression, CsmScope scope, CsmFile file, int start, int end, boolean defaultValue) {
        super(file, start, end);
        this.expression = NameCache.getManager().getString(expression);
        this.defaultValue = defaultValue;
        if ((scope instanceof CsmIdentifiable)) {
            this.scope = UIDCsmConverter.scopeToUID(scope);
        } else {
            this.scope = null;
        }
    }
    
    public static ExpressionBasedSpecializationParameterImpl create(CsmExpressionStatement expression, CsmFile file, int start, int end) {
        return create(expression, file, start, end, false);
    }    

    public static ExpressionBasedSpecializationParameterImpl create(CsmExpressionStatement expression, CsmFile file, int start, int end, boolean defaultValue) {
        return new ExpressionBasedSpecializationParameterImpl(expression.getText(), expression.getScope(), file, start, end, defaultValue);
    }
    
    public static ExpressionBasedSpecializationParameterImpl create(CharSequence expression, CsmScope scope, CsmFile file, int start, int end) {
        return create(expression, scope, file, start, end, false);
    }  

    public static ExpressionBasedSpecializationParameterImpl create(CharSequence expression, CsmScope scope, CsmFile file, int start, int end, boolean defaultValue) {
        return new ExpressionBasedSpecializationParameterImpl(expression, scope, file, start, end, defaultValue);
    }

    @Override
    public CsmScope getScope() {
        return scope == null? null : scope.getObject();
    }

    @Override
    public boolean isDefaultValue() {
        return defaultValue;
    }
    
    @Override
    public CharSequence getText() {
        return expression;
    }

    @Override
    public String toString() {
        return expression.toString() + super.getOffsetString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.expression);
        hash = 53 * hash + Objects.hashCode(super.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExpressionBasedSpecializationParameterImpl other = (ExpressionBasedSpecializationParameterImpl) obj;
        if (!Objects.equals(this.expression, other.expression)) {
            return false;
        }
        return super.equals(obj);
    }
    
    public static class ExpressionBasedSpecializationParameterBuilder extends SpecializationParameterBuilder {

        ExpressionBuilder expression;

        public void setExpressionBuilder(ExpressionBuilder expression) {
            this.expression = expression;
        }
        
        @Override
        public ExpressionBasedSpecializationParameterImpl create() {
            CharSequence expr;
            if(expression != null) {
                expr = expression.create().getText();
            } else {
                expr = NameCache.getManager().getString("1"); // NOI18N
            }
            
            ExpressionBasedSpecializationParameterImpl param = new ExpressionBasedSpecializationParameterImpl(expr, null, getFile(), getStartOffset(), getEndOffset(), false);
            return param;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeUTF(expression, output);
        output.writeBoolean(defaultValue);
        UIDObjectFactory.getDefaultFactory().writeUID(scope, output);
    }

    public ExpressionBasedSpecializationParameterImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.expression = PersistentUtils.readUTF(input, NameCache.getManager());
        this.defaultValue = input.readBoolean();
        this.scope = UIDObjectFactory.getDefaultFactory().readUID(input);
    }

}
