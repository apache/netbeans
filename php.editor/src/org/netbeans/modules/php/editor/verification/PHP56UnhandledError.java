/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment.Type;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression.OperatorType;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variadic;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHP56UnhandledError extends UnhandledErrorRule {

    @Override
    public void invoke(PHPRuleContext context, List<Error> errors) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null && appliesTo(fileObject)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            PHP56UnhandledError.CheckVisitor checkVisitor = new CheckVisitor(fileObject);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            errors.addAll(checkVisitor.getErrors());
        }
    }

    private static boolean appliesTo(FileObject fileObject) {
        return CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_56);
    }

    private static class CheckVisitor extends DefaultVisitor {
        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public Collection<VerificationError> getErrors() {
            return Collections.unmodifiableCollection(errors);
        }

        private static boolean isStaticScalarExpression(ASTNode node) {
            return node instanceof InfixExpression || node instanceof ConditionalExpression;
        }

        @Override
        public void visit(FormalParameter node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isVariadic()) {
                createError(node);
            }
            Expression defaultValue = node.getDefaultValue();
            if (isStaticScalarExpression(defaultValue)) {
                createError(defaultValue);
            }
        }

        @Override
        public void visit(InfixExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (OperatorType.POW.equals(node.getOperator())) {
                createError(node);
            } else {
                super.visit(node);
            }
        }

        @Override
        public void visit(Assignment node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (Type.POW_EQUAL.equals(node.getOperator())) {
                createError(node);
            } else {
                super.visit(node);
            }
        }

        @Override
        public void visit(ConstantDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            for (Expression expression : node.getInitializers()) {
                if (isStaticScalarExpression(expression)
                        || expression instanceof ArrayCreation
                        || expression instanceof ExpressionArrayAccess) {
                    createError(expression);
                } else {
                    scan(expression);
                }
            }
        }

        @Override
        public void visit(Variadic node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            createError(node);
        }

        @Override
        public void visit(UseStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (UseStatement.Type.CONST.equals(node.getType()) || UseStatement.Type.FUNCTION.equals(node.getType())) {
                createError(node);
            }
        }

        @Override
        public void visit(ExpressionArrayAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Expression expression = node.getExpression();
            if (expression instanceof Identifier) {
                Identifier identifier = (Identifier) expression;
                String name = identifier.getName();
                if (!NavUtils.isQuoted(name)) {
                    // e.g. CONSTANT[0][1]
                    createError(node);
                }
            } else if (expression instanceof NamespaceName) {
                // \CONSTANT[0], namespace\CONSTANT[0]
                createError(node);
            }
            scan(node.getDimension());
        }

        private  void createError(int startOffset, int endOffset) {
            VerificationError error = new PHP56VersionError(fileObject, startOffset, endOffset);
            errors.add(error);
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
            super.visit(node);
        }

    }

    private static final class PHP56VersionError extends VerificationError {

        private static final String KEY = "Php.Version.56"; //NOI18N

        private PHP56VersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @Override
        @NbBundle.Messages("CheckPHP56VerDisp=Language feature not compatible with PHP version indicated in project settings")
        public String getDisplayName() {
            return Bundle.CheckPHP56VerDisp();
        }

        @Override
        @NbBundle.Messages("CheckPHP56VerDesc=Detect language features not compatible with PHP version indicated in project settings")
        public String getDescription() {
            return Bundle.CheckPHP56VerDesc();
        }

        @Override
        public String getKey() {
            return KEY;
        }

    }

    @Override
    @NbBundle.Messages("PHP56VersionErrorHintDispName=Language feature not compatible with PHP version indicated in project settings")
    public String getDisplayName() {
        return Bundle.PHP56VersionErrorHintDispName();
    }

}
