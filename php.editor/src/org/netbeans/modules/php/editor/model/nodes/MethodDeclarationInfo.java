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
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.util.Pair;

/**
 * @author Radek Matous
 */
public class MethodDeclarationInfo extends ASTNodeInfo<MethodDeclaration> {
    Map<String, List<Pair<QualifiedName, Boolean>>> paramDocTypes = Collections.emptyMap();
    private final boolean isFromInterface;

    MethodDeclarationInfo(Program program, MethodDeclaration methodDeclaration, final boolean isFromInterface) {
        super(methodDeclaration);
        this.isFromInterface = isFromInterface;
        if (program != null) {
            paramDocTypes = VariousUtils.getParamTypesFromPHPDoc(program, methodDeclaration);
        }
    }

    public static MethodDeclarationInfo create(Program program, MethodDeclaration methodDeclaration, final boolean isFromInterface) {
        return new MethodDeclarationInfo(program, methodDeclaration, isFromInterface);
    }
    public static MethodDeclarationInfo create(Program program, MethodDeclaration methodDeclaration, final TypeScope typeScope) {
        return create(program, methodDeclaration, typeScope.isInterface());
    }
    public static MethodDeclarationInfo create(MethodDeclaration classDeclaration, final TypeScope typeScope) {
        return new MethodDeclarationInfo(null, classDeclaration, typeScope.isInterface());
    }

    @Override
    public Kind getKind() {
        PhpModifiers modifiers = PhpModifiers.fromBitMask(getOriginalNode().getModifier());
        return modifiers.isStatic() ? Kind.STATIC_METHOD : Kind.METHOD;
    }

    @Override
    public String getName() {
        MethodDeclaration methodDeclaration = getOriginalNode();
        return methodDeclaration.getFunction().getFunctionName().getName();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        MethodDeclaration methodDeclaration = getOriginalNode();
        Identifier name = methodDeclaration.getFunction().getFunctionName();
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public List<ParameterElement> getParameters() {
        List<ParameterElement> retval = new ArrayList<>();
        List<FormalParameter> formalParameters = getOriginalNode().getFunction().getFormalParameters();
        for (FormalParameter formalParameter : formalParameters) {
            FormalParameterInfo parameterInfo = FormalParameterInfo.create(formalParameter, paramDocTypes);
            retval.add(parameterInfo.toParameter());
        }
        return retval;
    }

    public PhpModifiers getAccessModifiers() {
        int realModifiers = getOriginalNode().getModifier();
        realModifiers = (isFromInterface) ? (realModifiers | Modifier.ABSTRACT | Modifier.PUBLIC) : realModifiers;
        return PhpModifiers.fromBitMask(realModifiers);
    }
}
