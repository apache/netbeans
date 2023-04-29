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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.actions.FixUsesAction;
import org.netbeans.modules.php.editor.actions.FixUsesPerformer;
import org.netbeans.modules.php.editor.actions.ImportData;
import org.netbeans.modules.php.editor.actions.ImportData.DataItem;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.netbeans.modules.php.editor.actions.ImportDataCreator;
import org.netbeans.modules.php.editor.actions.UsedNamespaceName;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Radek Matous
 */
public class AddUseImportSuggestion extends SuggestionRule {

    public AddUseImportSuggestion() {
        super();
    }

    @Override
    public String getId() {
        return "AddUse.Import.Rule"; //NOI18N
    }

    @Override
    @Messages("AddUseImportRuleDesc=Add Use Import")
    public String getDescription() {
        return Bundle.AddUseImportRuleDesc();
    }

    @Override
    @Messages("AddUseImportRuleDispName=Add Use Import")
    public String getDisplayName() {
        return Bundle.AddUseImportRuleDispName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject == null
                || CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_53)) {
            return;
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        int caretOffset = getCaretOffset();
        final BaseDocument doc = context.doc;
        OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, doc);
        if (lineBounds.containsInclusive(caretOffset)) {
            CodeStyle codeStyle = CodeStyle.get(context.doc);
            FixUsesAction.Options fixOptions = new FixUsesAction.Options(codeStyle, context.parserResult.getSnapshot().getSource().getFileObject());
            CheckVisitor checkVisitor = new CheckVisitor(context, doc, lineBounds, fixOptions);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            hints.addAll(checkVisitor.getHints());
        }
    }

    private class CheckVisitor extends DefaultTreePathVisitor {
        private final BaseDocument doc;
        private final PHPRuleContext context;
        private final Collection<Hint> hints = new ArrayList<>();
        private final OffsetRange lineBounds;
        private final FixUsesAction.Options fixOptions;

        CheckVisitor(PHPRuleContext context, BaseDocument doc, OffsetRange lineBounds, FixUsesAction.Options fixOptions) {
            this.doc = doc;
            this.lineBounds = lineBounds;
            this.context = context;
            this.fixOptions = fixOptions;
        }

        public Collection<Hint> getHints() {
            return hints;
        }

        @Override
        public void scan(ASTNode node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node != null && (VerificationUtils.isBefore(node.getStartOffset(), lineBounds.getEnd()))) {
                super.scan(node);
            }
        }

        @Override
        public void visit(NamespaceName node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (lineBounds.containsInclusive(node.getStartOffset())) {
                NamespaceDeclaration currenNamespace = null;
                List<ASTNode> path = getPath();
                for (ASTNode oneNode : path) {
                    if (oneNode instanceof NamespaceDeclaration) {
                        currenNamespace = (NamespaceDeclaration) oneNode;
                    }
                }
                NamespaceScope currentScope = ModelUtils.getNamespaceScope(currenNamespace, context.fileScope);
                Map<String, List<UsedNamespaceName>> names = new HashMap<>();
                final UsedNamespaceName usedName = new UsedNamespaceName(node, currentScope);
                UseScope suitableUse = ModelUtils.getFirst(ModelUtils.filter(currentScope.getAllDeclaredSingleUses(), new ModelUtils.ElementFilter<UseScope>() {
                    @Override
                    public boolean isAccepted(UseScope element) {
                        AliasedName aliasName = element.getAliasedName();
                        if (aliasName != null) {
                            if (usedName.getName().equals(aliasName.getAliasName())) {
                                return true;
                            }
                        } else {
                            if (element.getName().endsWith(usedName.getName())) {
                                return true;
                            }
                        }
                        return false;
                    }
                }));
                if (suitableUse != null) {
                    return;
                }
                names.put(usedName.getName(), Arrays.asList(usedName));
                ImportData importData = new ImportDataCreator(names, context.getIndex(), currentScope.getQualifiedName(), this.fixOptions).create();
                for (DataItem di : importData.getItems()) {
                    if (!di.getDefaultVariant().canBeUsed()) {
                        continue;
                    }
                    QualifiedName idxName = QualifiedName.create(di.getDefaultVariant().getName());
                    // check if the name isn't shodowed by other wider namespace
                    QualifiedName testSuffix = QualifiedName.getSuffix(idxName, QualifiedName.create(currentScope), false);
                    if (testSuffix != null && testSuffix.toString().equals(usedName.getName())) {
                        continue;
                    }
                    QualifiedName importName = QualifiedName.getPrefix(idxName, QualifiedName.create(usedName.getName()), true);
                    List<ItemVariant> selection = Arrays.asList(new ItemVariant(
                        importName.toString(), ItemVariant.UsagePolicy.CAN_BE_USED, di.getDefaultVariant().getType(), di.getDefaultVariant().isFromAliasedElement()));
                    AddImportFix importFix = new AddImportFix(context.parserResult, importData, selection, fixOptions);
                    hints.add(new Hint(AddUseImportSuggestion.this,
                                    importFix.getDescription(),
                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                    new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                                    Collections.<HintFix>singletonList(importFix), 500));

                    QualifiedName name = VariousUtils.getPreferredName(idxName, currentScope);
                    if (name != null) {
                        ChangeNameFix changeNameFix = new ChangeNameFix(doc, node, currentScope, name, QualifiedName.create(usedName.getName()));
                        hints.add(new Hint(AddUseImportSuggestion.this,
                                changeNameFix.getDescription(),
                                context.parserResult.getSnapshot().getSource().getFileObject(),
                                new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                                Collections.<HintFix>singletonList(changeNameFix), 500));
                    }
                }
                super.visit(node);
            }
        }
    }

    private static class AddImportFix implements HintFix {
        private final ParserResult parserResult;
        private final ImportData importData;
        private final List<ItemVariant> selections;
        private final FixUsesAction.Options fixOptions;

        public AddImportFix(ParserResult parserResult, ImportData importData, List<ItemVariant> selections, FixUsesAction.Options fixOptions) {
            this.parserResult = parserResult;
            this.importData = importData;
            this.selections = selections;
            this.fixOptions = fixOptions;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        @Messages({
            "# {0} - Use statement",
            "AddUseImportFix_Description=Generate \"{0}\""
        })
        public String getDescription() {
            String desc = "use " + selections.get(0).getName() + ';'; //NOI18N
            return Bundle.AddUseImportFix_Description(desc);
        }

        @Override
        public void implement() throws Exception {
            new FixUsesPerformer((PHPParseResult) parserResult, importData, selections, false, false, fixOptions).performAppend();
        }
    }

    static class ChangeNameFix implements HintFix {
        private final BaseDocument doc;
        private final ASTNode node;
        private final NamespaceScope scope;
        private final QualifiedName newName;
        private final QualifiedName oldName;

        public ChangeNameFix(BaseDocument doc, ASTNode node, NamespaceScope scope,
                QualifiedName newName, QualifiedName oldName) {
            this.doc = doc;
            this.newName = newName;
            this.oldName = oldName;
            this.scope = scope;
            this.node = node;
        }

        OffsetRange getOffsetRange() {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        @Messages({
            "# {0} - Fixed name",
            "ChangeNameFix_Description=Fix Name To \"{0}\""
        })
        public String getDescription() {
            return Bundle.ChangeNameFix_Description(getGeneratedCode());
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, oldName.toString().length(), getGeneratedCode(), true, 0); //NOI18N
            edits.apply();
            UiUtils.open(scope.getFileObject(), LineDocumentUtils.getLineStart(doc, templateOffset));
        }

        private String getGeneratedCode() {
            return newName.toString();
        }

        private int getOffset() {
            return node.getStartOffset();
        }
    }
}
