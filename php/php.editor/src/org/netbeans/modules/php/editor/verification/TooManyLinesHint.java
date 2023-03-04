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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class TooManyLinesHint extends HintRule implements CustomisableRule {

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = createVisitor(fileObject, context.doc);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    abstract CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument);

    public static class FunctionLinesHint extends TooManyLinesHint {
        private static final String HINT_ID = "Function.Lines.Hint"; //NOI18N
        private static final String MAX_ALLOWED_FUNCTION_LINES = "php.verification.max.allowed.function.lines"; //NOI18N
        private static final int DEFAULT_MAX_ALLOWED_FUNCTION_LINES = 20;
        private Preferences preferences;

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new FunctionVisitor(this, fileObject, baseDocument, getMaxAllowedLines(preferences));
        }

        @Override
        public void setPreferences(Preferences preferences) {
            assert preferences != null;
            this.preferences = preferences;
        }

        public void setMaxAllowedLines(Preferences preferences, Integer value) {
            assert preferences != null;
            assert value != null;
            preferences.putInt(MAX_ALLOWED_FUNCTION_LINES, value);
        }

        private static final class FunctionVisitor extends CheckVisitor {
            private final int maxAllowedLines;

            public FunctionVisitor(TooManyLinesHint linesHint, FileObject fileObject, BaseDocument baseDocument, int maxAllowedLines) {
                super(linesHint, fileObject, baseDocument);
                this.maxAllowedLines = maxAllowedLines;
            }

            @Override
            public void visit(FunctionDeclaration node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                super.visit(node);
                checkBlock(node.getBody(), new OffsetRange(node.getFunctionName().getStartOffset(), node.getFunctionName().getEndOffset()));
            }

            @Override
            public void visit(LambdaFunctionDeclaration node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                super.visit(node);
                checkBlock(node.getBody(), new OffsetRange(node.getStartOffset(), node.getBody().getStartOffset()));
            }

            @Override
            public void visit(InterfaceDeclaration node) {
                //don't check interface methods
            }

            @NbBundle.Messages({
                "# {0} - function length in lines",
                "# {1} - allowed lines per function declaration",
                "FunctionLinesHintText=Method Length is {0} Lines ({1} allowed)"
            })
            private void checkBlock(Block block, OffsetRange warningRange) {
                int countLines = block == null ? 0 : countLines(block);
                if (countLines > maxAllowedLines) {
                    addHint(Bundle.FunctionLinesHintText(countLines, maxAllowedLines), warningRange);
                }
            }

        }

        public int getMaxAllowedLines(Preferences preferences) {
            assert preferences != null;
            return preferences.getInt(MAX_ALLOWED_FUNCTION_LINES, DEFAULT_MAX_ALLOWED_FUNCTION_LINES);
        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("FunctionLinesHintDesc=Maximum allowed lines per function/method declaration.")
        public String getDescription() {
            return Bundle.FunctionLinesHintDesc();
        }

        @Override
        @NbBundle.Messages("FunctionLinesHintDisp=Function (Method) Declaration")
        public String getDisplayName() {
            return Bundle.FunctionLinesHintDisp();
        }

        @Override
        public JComponent getCustomizer(Preferences preferences) {
            JComponent customizer = new FunctionLinesCustomizer(preferences, this);
            setMaxAllowedLines(preferences, getMaxAllowedLines(preferences));
            return customizer;
        }

    }

    public static class ClassLinesHint extends TooManyLinesHint {
        private static final String HINT_ID = "Class.Lines.Hint"; //NOI18N
        private static final String MAX_ALLOWED_CLASS_LINES = "php.verification.max.allowed.class.lines"; //NOI18N
        private static final int DEFAULT_MAX_ALLOWED_CLASS_LINES = 200;
        private Preferences preferences;

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new ClassVisitor(this, fileObject, baseDocument, getMaxAllowedLines(preferences));
        }

        @Override
        public void setPreferences(Preferences preferences) {
            this.preferences = preferences;
        }

        public void setMaxAllowedLines(Preferences preferences, Integer value) {
            assert preferences != null;
            assert value != null;
            preferences.putInt(MAX_ALLOWED_CLASS_LINES, value);
        }

        private static final class ClassVisitor extends CheckVisitor {
            private final int maxAllowedLines;

            public ClassVisitor(TooManyLinesHint linesHint, FileObject fileObject, BaseDocument baseDocument, int maxAllowedLines) {
                super(linesHint, fileObject, baseDocument);
                this.maxAllowedLines = maxAllowedLines;
            }

            @Override
            public void visit(ClassDeclaration node) {
                super.visit(node);
                checkBlock(node.getBody(), new OffsetRange(node.getName().getStartOffset(), node.getName().getEndOffset()));
            }

            @NbBundle.Messages({
                "# {0} - class length in lines",
                "# {1} - allowed lines per class declaration",
                "ClassLinesHintText=Class Length is {0} Lines ({1} allowed)"
            })
            private void checkBlock(Block block, OffsetRange warningRange) {
                int countLines = countLines(block);
                if (countLines > maxAllowedLines) {
                    addHint(Bundle.ClassLinesHintText(countLines, maxAllowedLines), warningRange);
                }
            }

        }

        public int getMaxAllowedLines(Preferences preferences) {
            assert preferences != null;
            return preferences.getInt(MAX_ALLOWED_CLASS_LINES, DEFAULT_MAX_ALLOWED_CLASS_LINES);
        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("ClassLinesHintDesc=Maximum allowed lines per class declaration.")
        public String getDescription() {
            return Bundle.ClassLinesHintDesc();
        }

        @Override
        @NbBundle.Messages("ClassLinesHintDisp=Class Declaration")
        public String getDisplayName() {
            return Bundle.ClassLinesHintDisp();
        }

        @Override
        public JComponent getCustomizer(Preferences preferences) {
            JComponent customizer = new ClassLinesCustomizer(preferences, this);
            setMaxAllowedLines(preferences, getMaxAllowedLines(preferences));
            return customizer;
        }

    }

    public static class InterfaceLinesHint extends TooManyLinesHint {
        private static final String HINT_ID = "Interface.Lines.Hint"; //NOI18N
        private static final String MAX_ALLOWED_INTERFACE_LINES = "php.verification.max.allowed.interface.lines"; //NOI18N
        private static final int DEFAULT_MAX_ALLOWED_INTERFACE_LINES = 100;
        private Preferences preferences;

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new InterfaceVisitor(this, fileObject, baseDocument, getMaxAllowedLines(preferences));
        }

        @Override
        public void setPreferences(Preferences preferences) {
            this.preferences = preferences;
        }

        public void setMaxAllowedLines(Preferences preferences, Integer value) {
            assert preferences != null;
            assert value != null;
            preferences.putInt(MAX_ALLOWED_INTERFACE_LINES, value);
        }

        private static final class InterfaceVisitor extends CheckVisitor {
            private final int maxAllowedLines;

            public InterfaceVisitor(TooManyLinesHint linesHint, FileObject fileObject, BaseDocument baseDocument, int maxAllowedLines) {
                super(linesHint, fileObject, baseDocument);
                this.maxAllowedLines = maxAllowedLines;
            }

            @Override
            public void visit(InterfaceDeclaration node) {
                super.visit(node);
                checkBlock(node.getBody(), new OffsetRange(node.getName().getStartOffset(), node.getName().getEndOffset()));
            }

            @NbBundle.Messages({
                "# {0} - interface length in lines",
                "# {1} - allowed lines per interface declaration",
                "InterfaceLinesHintText=Interface Length is {0} Lines ({1} allowed)"
            })
            private void checkBlock(Block block, OffsetRange warningRange) {
                int countLines = countLines(block);
                if (countLines > maxAllowedLines) {
                    addHint(Bundle.InterfaceLinesHintText(countLines, maxAllowedLines), warningRange);
                }
            }

        }

        public int getMaxAllowedLines(Preferences preferences) {
            assert preferences != null;
            return preferences.getInt(MAX_ALLOWED_INTERFACE_LINES, DEFAULT_MAX_ALLOWED_INTERFACE_LINES);
        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("InterfaceLinesHintDesc=Maximum allowed lines per interface declaration.")
        public String getDescription() {
            return Bundle.InterfaceLinesHintDesc();
        }

        @Override
        @NbBundle.Messages("InterfaceLinesHintDisp=Interface Declaration")
        public String getDisplayName() {
            return Bundle.InterfaceLinesHintDisp();
        }

        @Override
        public JComponent getCustomizer(Preferences preferences) {
            JComponent customizer = new InterfaceLinesCustomizer(preferences, this);
            setMaxAllowedLines(preferences, getMaxAllowedLines(preferences));
            return customizer;
        }

    }

    public static class TraitLinesHint extends TooManyLinesHint {
        private static final String HINT_ID = "Trait.Lines.Hint"; //NOI18N
        private static final String MAX_ALLOWED_TRAIT_LINES = "php.verification.max.allowed.trait.lines"; //NOI18N
        private static final int DEFAULT_MAX_ALLOWED_TRAIT_LINES = 200;
        private Preferences preferences;

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new TraitVisitor(this, fileObject, baseDocument, getMaxAllowedLines(preferences));
        }

        @Override
        public void setPreferences(Preferences preferences) {
            this.preferences = preferences;
        }

        public void setMaxAllowedLines(Preferences preferences, Integer value) {
            assert preferences != null;
            assert value != null;
            preferences.putInt(MAX_ALLOWED_TRAIT_LINES, value);
        }

        private static final class TraitVisitor extends CheckVisitor {
            private final int maxAllowedLines;

            public TraitVisitor(TooManyLinesHint linesHint, FileObject fileObject, BaseDocument baseDocument, int maxAllowedLines) {
                super(linesHint, fileObject, baseDocument);
                this.maxAllowedLines = maxAllowedLines;
            }

            @Override
            public void visit(TraitDeclaration node) {
                super.visit(node);
                checkBlock(node.getBody(), new OffsetRange(node.getName().getStartOffset(), node.getName().getEndOffset()));
            }

            @NbBundle.Messages({
                "# {0} - trait length in lines",
                "# {1} - allowed lines per trait declaration",
                "TraitLinesHintText=Trait Length is {0} Lines ({1} allowed)"
            })
            private void checkBlock(Block block, OffsetRange warningRange) {
                int countLines = countLines(block);
                if (countLines > maxAllowedLines) {
                    addHint(Bundle.TraitLinesHintText(countLines, maxAllowedLines), warningRange);
                }
            }

        }

        public int getMaxAllowedLines(Preferences preferences) {
            assert preferences != null;
            return preferences.getInt(MAX_ALLOWED_TRAIT_LINES, DEFAULT_MAX_ALLOWED_TRAIT_LINES);
        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("TraitLinesHintDesc=Maximum allowed lines per trait declaration.")
        public String getDescription() {
            return Bundle.TraitLinesHintDesc();
        }

        @Override
        @NbBundle.Messages("TraitLinesHintDisp=Trait Declaration")
        public String getDisplayName() {
            return Bundle.TraitLinesHintDisp();
        }

        @Override
        public JComponent getCustomizer(Preferences preferences) {
            JComponent customizer = new TraitLinesCustomizer(preferences, this);
            setMaxAllowedLines(preferences, getMaxAllowedLines(preferences));
            return customizer;
        }

    }

    private abstract static class CheckVisitor extends DefaultVisitor {
        private static final Logger LOGGER = Logger.getLogger(CheckVisitor.class.getName());
        private final List<Hint> hints;
        private final BaseDocument baseDocument;
        private final FileObject fileObject;
        private final TooManyLinesHint linesHint;
        private final List<OffsetRange> commentRanges;

        private CheckVisitor(TooManyLinesHint linesHint, FileObject fileObject, BaseDocument baseDocument) {
            this.linesHint = linesHint;
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            hints = new ArrayList<>();
            commentRanges = new ArrayList<>();
        }

        @Override
        public void visit(Program node) {
            for (Comment comment : node.getComments()) {
                commentRanges.add(new OffsetRange(comment.getStartOffset(), comment.getEndOffset()));
            }
            super.visit(node);
        }

        protected int countLines(final Block block) {
            final AtomicInteger result = new AtomicInteger(0);
            baseDocument.render(new Runnable() {

                @Override
                public void run() {
                    result.set(countLinesUnderReadLock(block));
                }
            });
            return result.get();
        }

        private int countLinesUnderReadLock(Block block) {
            int result = 0;
            try {
                result = tryCountLines(block);
            } catch (BadLocationException ex) {
                // see issue 227687 and #172881
                LOGGER.log(Level.FINE, null, ex);
            }
            return result;
        }

        private int tryCountLines(Block block) throws BadLocationException {
            int searchOffset = block.getStartOffset() + 1;
            int firstNonWhiteFwd = LineDocumentUtils.getNextNonWhitespace(baseDocument, searchOffset);
            int startLineOffset = LineDocumentUtils.getLineIndex(baseDocument, firstNonWhiteFwd == -1 ? searchOffset : firstNonWhiteFwd);
            int endLineOffset = LineDocumentUtils.getLineIndex(baseDocument, LineDocumentUtils.getPreviousNonWhitespace(baseDocument, block.getEndOffset()));
            return countLinesBetweenLineOffsets(startLineOffset, endLineOffset);
        }

        private int countLinesBetweenLineOffsets(int startLineOffset, int endLineOffset) throws BadLocationException {
            int result = 0;
            for (int lineOffset = startLineOffset; lineOffset < endLineOffset; lineOffset++) {
                int rowStartFromLineOffset = LineDocumentUtils.getLineStartFromIndex(baseDocument, lineOffset);
                if (!LineDocumentUtils.isLineWhitespace(baseDocument, rowStartFromLineOffset) && !isJustCommentOnLine(rowStartFromLineOffset)) {
                    result++;
                }
            }
            return result;
        }

        private boolean isJustCommentOnLine(int rowStartOffset) throws BadLocationException {
            boolean result = false;
            int rowFirstNonWhite = LineDocumentUtils.getLineFirstNonWhitespace(baseDocument, rowStartOffset);
            int rowLastNonWhite = LineDocumentUtils.getLineLastNonWhitespace(baseDocument, rowStartOffset);
            for (OffsetRange commentRange : commentRanges) {
                if (commentRange.containsInclusive(rowFirstNonWhite) && commentRange.containsInclusive(rowLastNonWhite)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        protected void addHint(String description, OffsetRange warningRange) {
            if (linesHint.showHint(warningRange, baseDocument)) {
                hints.add(new Hint(linesHint, description, fileObject, warningRange, null, 500));
            }
        }

        public List<Hint> getHints() {
            return hints;
        }

    }

}
