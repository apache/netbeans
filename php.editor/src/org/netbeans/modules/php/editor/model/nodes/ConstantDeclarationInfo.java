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
import java.util.List;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;

/**
 * @author Radek Matous
 */
public class ConstantDeclarationInfo extends ClassConstantDeclarationInfo {
    ConstantDeclarationInfo(final Identifier node, final String value, final ConstantDeclaration constantDeclaration) {
        super(node, value, constantDeclaration);
    }

    public static List<? extends ConstantDeclarationInfo> create(ConstantDeclaration constantDeclaration) {
        List<ConstantDeclarationInfo> retval = new ArrayList<>();
        List<Identifier> names = constantDeclaration.getNames();
        for (Identifier identifier : names) {
            String value = null;
            for (final Expression expression : constantDeclaration.getInitializers()) {
                if (expression instanceof Scalar) {
                    value = ((Scalar) expression).getStringValue();
                    break;
                }
                if (expression instanceof UnaryOperation) {
                    UnaryOperation up = (UnaryOperation) expression;
                    if (up.getOperator() == UnaryOperation.Operator.MINUS
                            && up.getExpression() instanceof Scalar) {
                        value = "-" + ((Scalar) up.getExpression()).getStringValue();
                        break;
                    }
                }
            }
            retval.add(new ConstantDeclarationInfo(identifier, value, constantDeclaration));
        }
        return retval;
    }

    @Override
    public Kind getKind() {
        return Kind.CONSTANT;
    }
}
