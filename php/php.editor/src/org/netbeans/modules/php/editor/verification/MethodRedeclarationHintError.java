/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
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
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class MethodRedeclarationHintError extends HintErrorRule {

    private FileObject fileObject;
    private List<Hint> hints;
    private Set<Statement> conditionStatements = Collections.emptySet();

    @Override
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
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypeScopes(ModelUtils.getDeclaredClasses(fileScope));
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypeScopes(ModelUtils.getDeclaredInterfaces(fileScope));
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypeScopes(ModelUtils.getDeclaredTraits(fileScope));
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTypeScopes(ModelUtils.getDeclaredEnums(fileScope));
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkDeclaredFunctions(ModelUtils.getDeclaredFunctions(fileScope));
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

    private void checkTypeScopes(Collection<? extends TypeScope> typeScopes) {
        for (TypeScope typeScope : typeScopes) {
            checkDeclaredFunctions(typeScope.getDeclaredMethods());
        }
    }

    @Messages({
        "# {0} - Method name",
        "MethodRedeclarationCustom=Method or function \"{0}\" has already been declared"
    })
    private void checkDeclaredFunctions(Collection<? extends FunctionScope> declaredFunctions) {
        Set<String> declaredMethodNames = new HashSet<>();
        for (FunctionScope functionScope : declaredFunctions) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!isInConditionStatament(functionScope)) {
                String fullyQualifiedFunctionName = functionScope.getFullyQualifiedName().toString();
                if (declaredMethodNames.contains(fullyQualifiedFunctionName)) {
                    hints.add(new Hint(this, Bundle.MethodRedeclarationCustom(functionScope.getName()), fileObject, functionScope.getNameRange(), null, 500));
                } else {
                    declaredMethodNames.add(fullyQualifiedFunctionName);
                }
            }
        }
    }

    private boolean isInConditionStatament(FunctionScope functionScope) {
        boolean result = false;
        for (Statement statement : conditionStatements) {
            OffsetRange statementOffsetRange = new OffsetRange(statement.getStartOffset(), statement.getEndOffset());
            if (statementOffsetRange.containsInclusive(functionScope.getOffset())) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    @Messages("MethodRedeclarationHintDispName=Method Redeclaration")
    public String getDisplayName() {
        return Bundle.MethodRedeclarationHintDispName();
    }

}
