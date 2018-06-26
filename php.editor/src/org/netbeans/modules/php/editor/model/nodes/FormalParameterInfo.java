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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.elements.ParameterElementImpl;
import org.netbeans.modules.php.editor.elements.TypeResolverImpl;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.openide.util.Pair;

/**
 *
 * @author Radek Matous
 */
public final class FormalParameterInfo extends ASTNodeInfo<FormalParameter> {
    private final ParameterElement parameter;

    private FormalParameterInfo(FormalParameter node, Map<String, List<Pair<QualifiedName, Boolean>>> paramDocTypes) {
        super(node);
        FormalParameter formalParameter = getOriginalNode();
        String name = getName();
        String defVal = CodeUtils.getParamDefaultValue(formalParameter);
        Expression parameterType = formalParameter.getParameterType();
        final boolean isRawType = parameterType != null;
        final boolean isNullableType = parameterType instanceof NullableType;
        QualifiedName parameterTypeName = QualifiedName.create(parameterType);
        List<Pair<QualifiedName, Boolean>> types;
        if (isRawType && parameterTypeName != null) {
            if (!Type.isPrimitive(parameterTypeName.toString()) || paramDocTypes.isEmpty()) {
                types = Collections.singletonList(Pair.of(parameterTypeName, isNullableType));
            } else {
                types = paramDocTypes.get(name);
            }
        } else {
            types = paramDocTypes.get(name);
        }
        if (types == null) {
            types = Collections.emptyList();
        }
        this.parameter = new ParameterElementImpl(
                name,
                defVal,
                getRange().getStart(),
                TypeResolverImpl.forNames(types),
                formalParameter.isMandatory(),
                isRawType,
                formalParameter.isReference(),
                formalParameter.isVariadic());
    }

    public static FormalParameterInfo create(FormalParameter node, Map<String, List<Pair<QualifiedName, Boolean>>> paramDocTypes) {
        return new FormalParameterInfo(node, paramDocTypes);
    }


    @Override
    public Kind getKind() {
        return Kind.PARAMETER;
    }

    @Override
    public String getName() {
        FormalParameter formalParameter = getOriginalNode();
        return ASTNodeInfo.toName(formalParameter.getParameterName());
    }

    @Override
    public QualifiedName getQualifiedName() {
        QualifiedName qName = QualifiedName.create(getOriginalNode().getParameterName());
        return qName != null ? qName : QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        FormalParameter formalParameter = getOriginalNode();
        return ASTNodeInfo.toOffsetRange(formalParameter.getParameterName());
    }

    public ParameterElement toParameter() {
        return parameter;
    }
}
