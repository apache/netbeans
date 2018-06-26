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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.verification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 * @author Radek Matous, Ondrej Brejla
 */
public class TypeRedeclarationHintError extends HintErrorRule {
    private Set<Statement> conditionStatements = Collections.emptySet();
    private FileObject fileObject;
    private List<Hint> hints;
    private Set<String> typeNames;
    private Collection<? extends TypeScope> declaredTypes;

    @Override
    @Messages({
        "# {0} - Type name",
        "TypeRedeclarationDesc=Type \"{0}\" has been already declared"
    })
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            this.hints = hints;
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor();
            phpParseResult.getProgram().accept(checkVisitor);
            conditionStatements = checkVisitor.getConditionStatements();
            declaredTypes = ModelUtils.getDeclaredTypes(fileScope);
            typeNames = new HashSet<>();
            for (TypeScope typeScope : declaredTypes) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (!isInConditionStatament(typeScope)) {
                    checkDeclaredTypeScope(typeScope);
                }
            }
        }
    }

    private static final class CheckVisitor extends DefaultVisitor {
        private final Set<Statement> conditionStatements = new HashSet<>();

        public Set<Statement> getConditionStatements() {
            return new HashSet<>(conditionStatements);
        }

        @Override
        public void visit(IfStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            addStatement(node.getTrueStatement());
            addStatement(node.getFalseStatement());
        }

        @Override
        public void visit(SwitchCase node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            addStatement(node);
        }

        private void addStatement(Statement statement) {
            if (statement != null) {
                conditionStatements.add(statement);
            }
        }

    }

    private boolean isInConditionStatament(TypeScope typeScope) {
        boolean result = false;
        for (Statement statement : conditionStatements) {
            OffsetRange statementOffsetRange = new OffsetRange(statement.getStartOffset(), statement.getEndOffset());
            if (statementOffsetRange.containsInclusive(typeScope.getOffset())) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void checkDeclaredTypeScope(TypeScope typeScope) {
        final QualifiedName qualifiedName = typeScope.getNamespaceName().append(typeScope.getName()).toFullyQualified();
        final String name = qualifiedName.toString();
        if (!typeNames.contains(name)) {
            typeNames.add(name);
            List<? extends TypeScope> instances = ModelUtils.filter(declaredTypes, qualifiedName);
            if (instances.size() > 1) {
                TypeScope firstDeclaredInstance = null;
                for (TypeScope typeInstance : instances) {
                    if (firstDeclaredInstance == null) {
                        firstDeclaredInstance = typeInstance;
                    } else if (firstDeclaredInstance.getOffset() > typeInstance.getOffset()) {
                        firstDeclaredInstance = typeInstance;
                    }
                }
                for (TypeScope typeInstance : instances) {
                    if (typeInstance != firstDeclaredInstance) {
                        assert firstDeclaredInstance != null;
                        hints.add(new Hint(this, Bundle.TypeRedeclarationDesc(firstDeclaredInstance.getName()),
                                fileObject,
                                typeInstance.getNameRange(), Collections.<HintFix>emptyList(), 500));

                    }
                }
            }
        }
    }

    @Override
    @Messages("TypeRedeclarationRuleDispName=Type Redeclaration")
    public String getDisplayName() {
        return Bundle.TypeRedeclarationRuleDispName();
    }

}
