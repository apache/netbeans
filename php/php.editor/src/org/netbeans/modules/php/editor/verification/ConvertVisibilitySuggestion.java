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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Convert the visibility of a property, a method, or a constant.
 */
public class ConvertVisibilitySuggestion extends SuggestionRule {

    private static final String HINT_ID = "Convert.Visibility.Suggestion"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(ConvertVisibilitySuggestion.class.getName());

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("ConvertVisibilitySuggestion.Description=Convert the visibility of a property, a method, or a constant. Please convert it carefully. (e.g. from public to private)")
    public String getDescription() {
        return Bundle.ConvertVisibilitySuggestion_Description();
    }

    @Override
    @NbBundle.Messages("ConvertVisibilitySuggestion.DisplayName=Convert Visibility")
    public String getDisplayName() {
        return Bundle.ConvertVisibilitySuggestion_DisplayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final BaseDocument doc = context.doc;
        int caretOffset = getCaretOffset();
        OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, doc);
        if (lineBounds.containsInclusive(caretOffset)) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, this, context.doc, lineBounds);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    private static final class CheckVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final ConvertVisibilitySuggestion suggestion;
        private final BaseDocument document;
        private final OffsetRange lineRange;
        private final List<FixInfo> fixInfos = new ArrayList<>();
        private boolean isInInterface;

        public CheckVisitor(FileObject fileObject, ConvertVisibilitySuggestion suggestion, BaseDocument document, OffsetRange lineRange) {
            this.fileObject = fileObject;
            this.suggestion = suggestion;
            this.document = document;
            this.lineRange = lineRange;
        }

        @NbBundle.Messages("ConvertVisibilitySuggestion.Hint.Description=You can convert the visibility if needed")
        public List<Hint> getHints() {
            List<Hint> hints = new ArrayList<>();
            for (FixInfo fixInfo : fixInfos) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return Collections.emptyList();
                }
                List<HintFix> createFixes = createFixes(fixInfo);
                if (!createFixes.isEmpty()) {
                    hints.add(new Hint(suggestion, Bundle.ConvertVisibilitySuggestion_Hint_Description(), fileObject, lineRange, createFixes, 500));
                }
            }
            return hints;
        }

        private List<HintFix> createFixes(FixInfo fixInfo) {
            List<HintFix> hintFixes = new ArrayList<>();
            hintFixes.addAll(fixInfo.createFixes(document));
            return hintFixes;
        }

        @Override
        public void scan(ASTNode node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node != null && (VerificationUtils.isBefore(node.getStartOffset(), lineRange.getEnd()))) {
                super.scan(node);
            }
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            isInInterface = true;
            super.visit(node);
            isInInterface = false;
        }

        @Override
        public void visit(FieldsDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            OffsetRange nodeRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (lineRange.overlaps(nodeRange)) {
                processBodyDeclaration(node);
            }
        }

        @Override
        public void visit(ConstantDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()
                    || node.isGlobal()) {
                return;
            }
            OffsetRange nodeRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (lineRange.overlaps(nodeRange)) {
                processBodyDeclaration(node);
            }
        }

        @Override
        public void visit(MethodDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            OffsetRange nodeRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (lineRange.overlaps(nodeRange)) {
                processBodyDeclaration(node);
            }
        }

        private void processBodyDeclaration(BodyDeclaration node) {
            fixInfos.add(new FixInfo(node, isInInterface));
            super.visit(node);
        }
    }

    private static final class FixInfo {

        private final BodyDeclaration bodyDeclaration;
        private final boolean isInInterface;
        private final boolean isAbstract;

        public FixInfo(BodyDeclaration bodyDeclaration, boolean isInInterface) {
            this.bodyDeclaration = bodyDeclaration;
            this.isInInterface = isInInterface;
            this.isAbstract = Modifier.isAbstract(bodyDeclaration.getModifier());
        }

        public Pair<String, OffsetRange> getVisibilityRange(Document document) {
            String visibility = "implicit"; // NOI18N
            int startOffset = bodyDeclaration.getStartOffset();
            int endOffset = bodyDeclaration.getEndOffset();
            try {
                String text = document.getText(startOffset, endOffset - startOffset);
                int indexOfVisibility = -1;
                if (Modifier.isPublic(bodyDeclaration.getModifier())) {
                    indexOfVisibility = text.indexOf(PhpModifiers.VISIBILITY_PUBLIC + " "); // NOI18N
                    visibility = PhpModifiers.VISIBILITY_PUBLIC;
                    if (indexOfVisibility == -1) {
                        indexOfVisibility = text.indexOf(PhpModifiers.VISIBILITY_VAR + " "); // NOI18N
                        visibility = PhpModifiers.VISIBILITY_VAR;
                    }
                    if (indexOfVisibility == -1) {
                        visibility = "implicit"; // NOI18N
                    }
                } else if (Modifier.isPrivate(bodyDeclaration.getModifier())) {
                    indexOfVisibility = text.indexOf(PhpModifiers.VISIBILITY_PRIVATE + " "); // NOI18N
                    visibility = PhpModifiers.VISIBILITY_PRIVATE;
                } else if (Modifier.isProtected(bodyDeclaration.getModifier())) {
                    indexOfVisibility = text.indexOf(PhpModifiers.VISIBILITY_PROTECTED + " "); // NOI18N
                    visibility = PhpModifiers.VISIBILITY_PROTECTED;
                }
                int visibilityStart = startOffset;
                int visibilityEnd = startOffset;
                if (indexOfVisibility != -1) {
                    visibilityStart += indexOfVisibility;
                    visibilityEnd = visibilityStart + visibility.length();
                } else if (indexOfVisibility == -1 && isAbstract) {
                    // abstract function implicitPublic();
                    //          ^add here
                    visibilityStart += "abstract ".length(); // NOI18N
                    visibilityEnd = visibilityStart;
                }
                return Pair.of(visibility, new OffsetRange(visibilityStart, visibilityEnd));
            } catch (BadLocationException ex) {
                LOGGER.log(Level.WARNING, "Incorrect offset: {0}", ex.offsetRequested()); // NOI18N
            }
            return Pair.of(visibility, OffsetRange.NONE);
        }

        public List<HintFix> createFixes(BaseDocument document) {
            ArrayList<HintFix> fixes = new ArrayList<>();
            Pair<String, OffsetRange> visibilityRange = getVisibilityRange(document);
            String visibility = visibilityRange.first();
            OffsetRange range = visibilityRange.second();
            switch (visibility) {
                case "implicit": // NOI18N
                    fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PUBLIC + " ", document)); // NOI18N
                    if (!isInInterface) {
                        if (!isAbstract) {
                            fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PRIVATE + " ", document)); // NOI18N
                        }
                        fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PROTECTED + " ", document)); // NOI18N
                    }
                    break;
                case PhpModifiers.VISIBILITY_VAR:
                    fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PUBLIC, document));
                    fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PRIVATE, document));
                    fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PROTECTED, document));
                    break;
                case PhpModifiers.VISIBILITY_PUBLIC:
                    if (!isInInterface) {
                        if (!isAbstract) {
                            fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PRIVATE, document));
                        }
                        fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PROTECTED, document));
                    }
                    break;
                case PhpModifiers.VISIBILITY_PRIVATE:
                    fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PUBLIC, document));
                    fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PROTECTED, document));
                    break;
                case PhpModifiers.VISIBILITY_PROTECTED:
                    fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PUBLIC, document));
                    if (!isAbstract) {
                        fixes.add(new Fix(range, PhpModifiers.VISIBILITY_PRIVATE, document));
                    }
                    break;
                default:
                    break;
            }
            return fixes;
        }

    }

    private static final class Fix implements HintFix {

        private final OffsetRange visibilityRange;
        private final String newVisibility;
        private final BaseDocument document;

        private Fix(OffsetRange visibilityRange, String newVisibility, BaseDocument document) {
            this.visibilityRange = visibilityRange;
            this.newVisibility = newVisibility;
            this.document = document;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - visibility",
            "ConvertVisibilitySuggestion.Fix.Description=Convert Visibility to \"{0}\""
        })
        public String getDescription() {
            return Bundle.ConvertVisibilitySuggestion_Fix_Description(newVisibility.trim());
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(document);
            edits.replace(visibilityRange.getStart(), visibilityRange.getLength(), newVisibility, true, 0);
            edits.apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }
}
