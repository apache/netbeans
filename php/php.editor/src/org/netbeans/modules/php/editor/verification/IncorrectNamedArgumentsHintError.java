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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamedArgument;
import org.netbeans.modules.php.editor.parser.astnodes.Variadic;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Handle incorrect Named Arguments.
 */
public class IncorrectNamedArgumentsHintError extends HintErrorRule {

    private FileObject fileObject;

    @Override
    @NbBundle.Messages("IncorrectNamedArgumentsHintError.displayName=Incorrect Named Arguments")
    public String getDisplayName() {
        return Bundle.IncorrectConstructorPropertyPromotionHintError_displayName();
    }

    @Override
    @NbBundle.Messages({
        "# {0} - argument name",
        "IncorrectNamedArguments.desc.duplicate.name=Duplicate argument name: \"{0}\" already exists.",
        "IncorrectNamedArguments.desc.combine.named.argument.and.argument.unpacking=Can't combine named arguments(name: arg) and argument unpacking(...).",
        "IncorrectNamedArguments.desc.positional.arguments.after.named.argument=Can't use positional argument after named argument.",
    })
    public void invoke(PHPRuleContext context, List<Hint> hints) {
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
            for (NamedArgument argument : checkVisitor.getDuplicateNames()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(argument, Bundle.IncorrectNamedArguments_desc_duplicate_name(argument.getParameterName().getName()), hints);
            }
            for (Map.Entry<NamedArgument, Variadic> entry : checkVisitor.getCombinedNamedArgumentsWithArgumentUnpacking().entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(entry.getKey(), Bundle.IncorrectNamedArguments_desc_combine_named_argument_and_argument_unpacking(), hints);
                addHint(entry.getValue(), Bundle.IncorrectNamedArguments_desc_combine_named_argument_and_argument_unpacking(), hints);
            }
            for (Expression argument : checkVisitor.getArgumentsAfterNamedArgument()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(argument, Bundle.IncorrectNamedArguments_desc_positional_arguments_after_named_argument(), hints);
            }
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

        private final Set<NamedArgument> duplicateNames = new HashSet<>();
        private final Map<NamedArgument, Variadic> combinedNamedArgumentsWithArgumentUnpacking = new HashMap<>();
        private final Set<Expression> argumentsAfterNamedArgument = new HashSet<>();

        @Override
        public void visit(FunctionInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processArguments(node.getParameters());
            super.visit(node);
        }

        @Override
        public void visit(ClassInstanceCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processArguments(node.ctorParams());
            super.visit(node);
        }

        @Override
        public void visit(AttributeDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processArguments(node.getParameters());
            super.visit(node);
        }

        private void processArguments(List<Expression> arguments) {
            if (arguments == null) {
                // attribute parameters can be null (e.g. #[A])
                return;
            }
            Set<String> names = new HashSet<>();
            NamedArgument firstNamedArgument = null;
            Variadic variadic = null;
            for (Expression argument : arguments) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (argument instanceof NamedArgument) {
                    if (firstNamedArgument == null) {
                        firstNamedArgument = (NamedArgument) argument;
                    }
                    String name = ((NamedArgument) argument).getParameterName().getName();
                    if (names.contains(name)) {
                        // e.g. (a: 'arg', a:'arg2') only this case
                        // don't check the following case because stub's parameter names can be wrong,
                        // and if we check parameter names of all arguments, performance may deteriorate
                        // function test($name){}
                        // test('foo', name: 'bar');
                        duplicateNames.add((NamedArgument) argument);
                    } else {
                        names.add(name);
                    }
                } else if (argument instanceof Variadic) {
                    if (variadic == null) {
                        variadic = (Variadic) argument;
                    }
                } else {
                    if (firstNamedArgument != null) {
                        // e.g. (a: 'arg', 'something')
                        argumentsAfterNamedArgument.add(argument);
                    }
                }
            }
            if (firstNamedArgument != null && variadic != null) {
                // e.g. (a: 'arg', ...[]), (...[], a: 'arg')
                combinedNamedArgumentsWithArgumentUnpacking.put(firstNamedArgument, variadic);
            }
        }

        public Set<NamedArgument> getDuplicateNames() {
            return Collections.unmodifiableSet(duplicateNames);
        }

        public Map<NamedArgument, Variadic> getCombinedNamedArgumentsWithArgumentUnpacking() {
            return Collections.unmodifiableMap(combinedNamedArgumentsWithArgumentUnpacking);
        }

        public Set<Expression> getArgumentsAfterNamedArgument() {
            return Collections.unmodifiableSet(argumentsAfterNamedArgument);
        }

    }
}
