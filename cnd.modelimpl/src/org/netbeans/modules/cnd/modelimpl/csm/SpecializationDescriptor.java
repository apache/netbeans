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

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Container for template specialization parameters
 *
 */
public class SpecializationDescriptor {
    private final List<CsmSpecializationParameter> specializationParams;

    public SpecializationDescriptor(List<CsmSpecializationParameter> specializationParams, boolean global) {
        this.specializationParams = new ArrayList<>(specializationParams);
    }

    public static SpecializationDescriptor create(List<CsmSpecializationParameter> specializationParams, boolean global) {
        return new SpecializationDescriptor(specializationParams, global);
    }

    public List<CsmSpecializationParameter> getSpecializationParameters() {
        if (specializationParams != null) {
            return new ArrayList<>(specializationParams);
        }
    	return Collections.<CsmSpecializationParameter>emptyList();
    }

    public static SpecializationDescriptor createIfNeeded(AST ast, CsmFile file, CsmScope scope, boolean global) {
        if (ast == null) {
            return null;
        }
        AST start = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        if(start != null) {
            return SpecializationDescriptor.create(TemplateUtils.getSpecializationParameters(start, file, scope, global), global);
        }
        start = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        if(start != null) {
            return SpecializationDescriptor.create(TemplateUtils.getSpecializationParameters(start, file, scope, global), global);
        }
        return null;
    }

    @Override
    public String toString() {
        return specializationParams.toString();
    }
    
    public static abstract class SpecializationParameterBuilder extends ScopedDeclarationBuilder {
        public abstract CsmSpecializationParameter create();
    }

    public static class SpecializationDescriptorBuilder extends ScopedDeclarationBuilder {

        private final List<SpecializationParameterBuilder> parameterBuilders = new ArrayList<>();
        
        public void addParameterBuilder(SpecializationParameterBuilder parameterBuilser) {
            parameterBuilders.add(parameterBuilser);
        }
        
        public SpecializationDescriptor create() {
            List<CsmSpecializationParameter> params = new ArrayList<>();
            for (SpecializationParameterBuilder paramBuilder : parameterBuilders) {
                paramBuilder.setScope(getScope());
                params.add(paramBuilder.create());
            }
            SpecializationDescriptor descriptor = new SpecializationDescriptor(params, isGlobal());
            return descriptor;
        }
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    public SpecializationDescriptor(RepositoryDataInput input) throws IOException {
        this.specializationParams = PersistentUtils.readSpecializationParameters(input);
    }

    public void write(RepositoryDataOutput output) throws IOException {
        PersistentUtils.writeSpecializationParameters(specializationParams, output);
    }
}
