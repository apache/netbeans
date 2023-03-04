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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AbstractClassInstantiationHintError extends HintErrorRule {

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject == null) {
            return;
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.getIndex(), phpParseResult.getModel());
        phpParseResult.getProgram().accept(checkVisitor);
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        hints.addAll(checkVisitor.getHints());
    }

    private final class CheckVisitor extends DefaultVisitor {
        private final FileObject fileObject;
        private final Index index;
        private final List<Hint> hints = new ArrayList<>();
        private final Model model;

        private CheckVisitor(FileObject fileObject, Index index, Model model) {
            this.fileObject = fileObject;
            this.index = index;
            this.model = model;
        }

        public List<Hint> getHints() {
            return hints;
        }

        @Override
        @Messages({
            "# {0} - Class name",
            "AbstractClassInstantiationDesc=Abstract class {0} can not be instantiated"
        })
        public void visit(ClassInstanceCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            ASTNodeInfo<ClassInstanceCreation> info = ASTNodeInfo.create(node);
            int startOffset = node.getStartOffset();
            VariableScope variableScope = model.getVariableScope(startOffset);
            QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(info.getQualifiedName(), startOffset, variableScope);
            Set<ClassElement> classes = index.getClasses(NameKind.exact(fullyQualifiedName));
            if (!classes.isEmpty()) {
                ClassElement classElement = ModelUtils.getFirst(classes);
                if (classElement != null && classElement.isAbstract()) {
                    OffsetRange offsetRange = new OffsetRange(startOffset, node.getEndOffset());
                    hints.add(new Hint(
                            AbstractClassInstantiationHintError.this,
                            Bundle.AbstractClassInstantiationDesc(classElement.getFullyQualifiedName().toString()),
                            fileObject,
                            offsetRange,
                            null,
                            500));
                }
            }
        }

    }

    @Override
    @Messages("AbstractClassInstantiationHintDispName=Abstract Class Instantiation")
    public String getDisplayName() {
        return Bundle.AbstractClassInstantiationHintDispName();
    }

}
