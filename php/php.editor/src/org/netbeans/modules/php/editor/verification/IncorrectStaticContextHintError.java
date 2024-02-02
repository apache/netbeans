/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Handle incorrect static context statements.
 */
public class IncorrectStaticContextHintError extends HintErrorRule {

    private static final boolean DISABLED = Boolean.getBoolean("nb.php.editor.disableIncorrectStaticContextHintError"); // NOI18N
    private FileObject fileObject;

    @Override
    @NbBundle.Messages("IncorrectStaticContextHintError.displayName=Incorrect Statements in Static Context")
    public String getDisplayName() {
        return Bundle.IncorrectStaticContextHintError_displayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        if (DISABLED) {
            return;
        }
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor();
            phpParseResult.getProgram().accept(checkVisitor);
            Set<Variable> incorrectThisVariables = checkVisitor.getThisVariableInStaticContext();
            addIncorrectThisHints(incorrectThisVariables, hints);
        }
    }

    @NbBundle.Messages("IncorrectStaticContextHintError.incorrectThis=Cannot use \"$this\" in static context.")
    private void addIncorrectThisHints(Set<Variable> incorrectThisVariables, List<Hint> hints) {
        // e.g.
        // public static function staticMethod(): int {
        //     return $this->field;
        // }          ^^^^^
        for (Variable thisVariable : incorrectThisVariables) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            addHint(thisVariable, Bundle.IncorrectStaticContextHintError_incorrectThis(), hints);
        }
    }

    private void addHint(ASTNode node, String description, List<Hint> hints) {
        addHint(node, description, hints, Collections.emptyList());
    }

    private void addHint(ASTNode node, String description, List<Hint> hints, List<HintFix> fixes) {
        hints.add(new Hint(
                this,
                description,
                fileObject,
                new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                fixes,
                500
        ));
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private boolean isInTypeDeclaration = false;
        private boolean isInStaticMethod = false;
        private ASTNode firstStaticClosure = null;
        private final ArrayDeque<ASTNode> firstStaticClosureStack = new ArrayDeque<>();
        private final ArrayDeque<Boolean> isInStaticMethodStack = new ArrayDeque<>();
        private final Set<Variable> thisVariableInStaticContext = new HashSet<>();

        @Override
        public void visit(MethodDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            isInStaticMethod = BodyDeclaration.Modifier.isStatic(node.getModifier());
            super.visit(node);
            isInStaticMethod = false;
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // e.g.
            // $closure = function() {
            //     $this->something; // ok
            //     $nestedStaticClosure = static function() {
            //         $this->something; // error
            //         $nestedClosure = function() {
            //             $this->something; // error
            //         };
            //     };
            // };
            if (node.isStatic()) {
                if (firstStaticClosure == null) {
                    firstStaticClosure = node;
                }
            }
            super.visit(node);
            if (firstStaticClosure == node) {
                firstStaticClosure = null;
            }
        }

        @Override
        public void visit(ArrowFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isStatic()) {
                if (firstStaticClosure == null) {
                    firstStaticClosure = node;
                }
            }
            super.visit(node);
            if (firstStaticClosure == node) {
                firstStaticClosure = null;
            }
        }

        @Override
        public void visit(ClassDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            isInTypeDeclaration = true;
            super.visit(node);
            isInTypeDeclaration = false;
        }

        @Override
        public void visit(EnumDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            isInTypeDeclaration = true;
            super.visit(node);
            isInTypeDeclaration = false;
        }

        @Override
        public void visit(TraitDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            isInTypeDeclaration = true;
            super.visit(node);
            isInTypeDeclaration = false;
        }

        @Override
        public void visit(ClassInstanceCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // static method may have anonymous classes
            // e.g.
            // public static function getExample(): int {
            //     self::staticMethod($this); // error
            //     $anon = new class() {
            //         private int $field = 1;
            //
            //         public static function nestedStaticMethod(): void {
            //             echo $this->field; // error
            //         }
            //
            //         public function nestedMethod(): void {
            //             echo $this->field; // ok
            //         }
            //     };
            //     return $this->test; // error
            // }
            boolean isInPreviousTypeDeclaration = this.isInTypeDeclaration;
            boolean hasFirstStaticClosure = firstStaticClosure != null;
            if (node.isAnonymous()) {
                isInTypeDeclaration = true;
                isInStaticMethodStack.push(isInStaticMethod);
                isInStaticMethod = false;
                if (hasFirstStaticClosure) {
                    firstStaticClosureStack.push(firstStaticClosure);
                    firstStaticClosure = null;
                }
            }
            super.visit(node);
            if (node.isAnonymous()) {
                isInTypeDeclaration = isInPreviousTypeDeclaration;
                isInStaticMethod = isInStaticMethodStack.pop();
                if (hasFirstStaticClosure) {
                    firstStaticClosure = firstStaticClosureStack.pop();
                }
            }
        }

        @Override
        public void visit(Variable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (isInStaticContext()) {
                String variableName = CodeUtils.extractVariableName(node);
                if (CodeUtils.THIS_VARIABLE.equals(variableName)) {
                    thisVariableInStaticContext.add(node);
                }
            }
            super.visit(node);
        }

        private boolean isInStaticContext() {
            // NOTE:
            // Check only $this in TypeDeclarations because Frameworks may use `$this` in the global scope
            // e.g. CakePHP framework can use `$this` in the global scope of view files
            // see: https://book.cakephp.org/5/en/views.html
            return isInStaticMethod
                    || (isInTypeDeclaration && firstStaticClosure != null);
        }

        public Set<Variable> getThisVariableInStaticContext() {
            return Collections.unmodifiableSet(thisVariableInStaticContext);
        }
    }
}
