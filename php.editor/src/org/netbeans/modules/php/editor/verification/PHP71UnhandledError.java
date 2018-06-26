/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ListVariable;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class PHP71UnhandledError extends UnhandledErrorRule {

    @NbBundle.Messages("PHP71UnhandledError.displayName=Language feature not compatible with PHP version indicated in project settings")
    @Override
    public String getDisplayName() {
        return Bundle.PHP71UnhandledError_displayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Error> errors) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null
                && appliesTo(fileObject)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor(fileObject);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            errors.addAll(checkVisitor.getErrors());
        }
    }

    private static boolean appliesTo(FileObject fileObject) {
        return CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_71);
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public Collection<VerificationError> getErrors() {
            return Collections.unmodifiableCollection(errors);
        }

        @Override
        public void visit(NullableType nullable) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkNullableType(nullable);
            super.visit(nullable);
        }

        @Override
        public void visit(CatchClause catchClause) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkMultiCatch(catchClause);
            super.visit(catchClause);
        }

        @Override
        public void visit(ConstantDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!node.isGlobal()) {
                checkConstantVisibility(node);
            }
            super.visit(node);
        }

        @Override
        public void visit(ListVariable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkNewSyntax(node);
            checkKeyedList(node.getElements());
            super.visit(node);
        }

        private void checkNullableType(NullableType nullableType) {
            if (nullableType != null) {
                createError(nullableType);
            }
        }

        private void checkMultiCatch(CatchClause catchClause) {
            List<Expression> classNames = catchClause.getClassNames();
            if (classNames.size() > 1) {
                createError(catchClause);
            }
        }

        private void checkConstantVisibility(ConstantDeclaration node) {
            if (!BodyDeclaration.Modifier.isImplicitPublic(node.getModifier())) {
                createError(node);
            }
        }

        private void checkKeyedList(List<ArrayElement> elements) {
            // list("id" => $id, "name" => $name) = $data[0];
            for (ArrayElement element : elements) {
                if (element.getKey() != null) {
                    createError(element);
                    break;
                }
            }
        }

        private void checkNewSyntax(ListVariable node) {
            // [$a, $b] = [1, 2];
            if (node.getSyntaxType() == ListVariable.SyntaxType.NEW) {
                createError(node);
            }
        }

        private void createError(int startOffset, int endOffset) {
            errors.add(new PHP71VersionError(fileObject, startOffset, endOffset));
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

    }

    private static final class PHP71VersionError extends VerificationError {

        private static final String KEY = "Php.Version.71"; // NOI18N

        private PHP71VersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @NbBundle.Messages("PHP71VersionError.displayName=Language feature not compatible with PHP version indicated in project settings")
        @Override
        public String getDisplayName() {
            return Bundle.PHP71VersionError_displayName();
        }

        @NbBundle.Messages("PHP71VersionError.description=Detected language features not compatible with PHP version indicated in project settings")
        @Override
        public String getDescription() {
            return Bundle.PHP71VersionError_description();
        }

        @Override
        public String getKey() {
            return KEY;
        }

    }
}
