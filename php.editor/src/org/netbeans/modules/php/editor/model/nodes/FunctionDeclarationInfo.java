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

package org.netbeans.modules.php.editor.model.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.util.Pair;

/**
 *
 * @author Radek Matous
 */
public class FunctionDeclarationInfo extends ASTNodeInfo<FunctionDeclaration> {

    private final Map<String, List<Pair<QualifiedName, Boolean>>> paramDocTypes;


    protected FunctionDeclarationInfo(Program program, FunctionDeclaration node) {
        super(node);
        if (program != null) {
            paramDocTypes = VariousUtils.getParamTypesFromPHPDoc(program, node);
        } else {
            paramDocTypes = Collections.emptyMap();
        }
    }

    public static FunctionDeclarationInfo create(FunctionDeclaration functionDeclaration) {
        return new FunctionDeclarationInfo(null, functionDeclaration);
    }
    public static FunctionDeclarationInfo create(Program program, FunctionDeclaration functionDeclaration) {
        return new FunctionDeclarationInfo(program, functionDeclaration);
    }

    @Override
    public Kind getKind() {
        return Kind.FUNCTION;
    }

    @Override
    public String getName() {
        FunctionDeclaration functionDeclaration = getOriginalNode();
        return functionDeclaration.getFunctionName().getName();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        FunctionDeclaration functionDeclaration = getOriginalNode();
        Identifier name = functionDeclaration.getFunctionName();
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public List<ParameterElement> getParameters() {
        List<ParameterElement> retval = new ArrayList<>();
        List<FormalParameter> formalParameters = getOriginalNode().getFormalParameters();
        for (FormalParameter formalParameter : formalParameters) {
            FormalParameterInfo parameterInfo = FormalParameterInfo.create(formalParameter, paramDocTypes);
            retval.add(parameterInfo.toParameter());
        }
        return retval;
    }

    @CheckForNull
    public QualifiedName getReturnType() {
        Expression returnType = getOriginalNode().getReturnType();
        if (returnType == null) {
            return null;
        }
        return QualifiedName.create(returnType);
    }

}
