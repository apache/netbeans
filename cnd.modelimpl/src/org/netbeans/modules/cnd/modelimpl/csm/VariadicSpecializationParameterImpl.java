/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmVariadicSpecializationParameter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndCollectionUtils;

/**
 *
 */
public class VariadicSpecializationParameterImpl extends OffsetableBase implements CsmVariadicSpecializationParameter, SelfPersistent, Persistent {
    
    private final List<CsmSpecializationParameter> args = new ArrayList<>();

    public VariadicSpecializationParameterImpl(List<CsmSpecializationParameter> args, CsmFile file, int start, int end) {
        super(file, start, end);
        this.args.addAll(args);
    }

    @Override
    public CsmScope getScope() {
        return null; // Always null here
    }    
    
    @Override
    public List<CsmSpecializationParameter> getArgs() {
        return args;
    }

    @Override
    public CharSequence getText() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CsmSpecializationParameter p : getArgs()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            if(CsmKindUtilities.isTypeBasedSpecalizationParameter(p)) {
                sb.append(TypeImpl.getCanonicalText(((CsmTypeBasedSpecializationParameter) p).getType()));
            }
            if(CsmKindUtilities.isExpressionBasedSpecalizationParameter(p)) {
                sb.append(((CsmExpressionBasedSpecializationParameter) p).getText());
            }
        }
        return sb;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        for (CsmSpecializationParameter a : args) {
            hash = 29 * hash + Objects.hashCode(a);
        }
        hash = 29 * hash + Objects.hashCode(super.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VariadicSpecializationParameterImpl other = (VariadicSpecializationParameterImpl) obj;
        if (!super.equals(obj)) {
            return false;
        }
        return CndCollectionUtils.equals(args, other.args);
    }

    @Override
    public String toString() {
        return getText().toString();
    }
        
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeSpecializationParameters(args, output);
    }

    public VariadicSpecializationParameterImpl(RepositoryDataInput input) throws IOException {
        super(input);
        PersistentUtils.readSpecializationParameters(args, input);
    }    
}
