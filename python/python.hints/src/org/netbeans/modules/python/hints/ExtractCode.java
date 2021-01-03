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
package org.netbeans.modules.python.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.EditRegions;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.python.source.AstPath;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assert;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.Break;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Continue;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.If;
import org.python.antlr.ast.IfExp;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Pass;
import org.python.antlr.ast.Print;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Suite;
import org.python.antlr.ast.TryExcept;
import org.python.antlr.ast.TryFinally;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.While;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;

/**
 * Offer to introduce method/variable/constant
 * @todo There is no need to pass in class or top level constants to code fragments
 * @todo Handle flow control: If a code fragment contains an early return, figure out
 *   how to pass that information back to the method callsite and do something clever,
 *   for example pass back a to-return flag which returns the same value
 * @todo Unit tests must check instant rename as well!
 *
 */
public class ExtractCode extends PythonSelectionRule {
    //private static final int NOT_APPLICABLE = 0;
    private static final int INTRODUCE_METHOD = 1;
    private static final int INTRODUCE_VARIABLE = 2;
    private static final int INTRODUCE_CONSTANT = 4;
    private static final int INTRODUCE_FIELD = 8;
    private static final int NON_EXPRESSIONS = INTRODUCE_VARIABLE | INTRODUCE_FIELD | INTRODUCE_CONSTANT;
    private static final int ALL = ~0;

    @Override
    protected int getApplicability(PythonRuleContext context, PythonTree root, OffsetRange astRange) {
        return ApplicabilityVisitor.getType(root, astRange);
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result, OffsetRange range, int applicability) {
        int start = range.getStart();
        int end = range.getEnd();

// HACK: Only extract method works at this point
        applicability = applicability & INTRODUCE_METHOD;


        // Adjust the fix range to be right around the dot so that the light bulb ends up
        // on the same line as the caret and alt-enter works
        JTextComponent target = GsfUtilities.getPaneFor(context.parserResult.getSnapshot().getSource().getFileObject());
        if (target != null) {
            int dot = target.getCaret().getDot();
            range = new OffsetRange(dot, dot);
        }

        List<HintFix> fixList = new ArrayList<>(3);
        if ((applicability & INTRODUCE_METHOD) != 0) {
            fixList.add(new ExtractCodeFix(context, start, end, INTRODUCE_METHOD));
        }
        if ((applicability & INTRODUCE_VARIABLE) != 0) {
            fixList.add(new ExtractCodeFix(context, start, end, INTRODUCE_VARIABLE));
        }
        if ((applicability & INTRODUCE_CONSTANT) != 0) {
            fixList.add(new ExtractCodeFix(context, start, end, INTRODUCE_CONSTANT));
        }
        if ((applicability & INTRODUCE_FIELD) != 0) {
            fixList.add(new ExtractCodeFix(context, start, end, INTRODUCE_FIELD));
        }
        if (fixList.size() > 0) {
            String displayName = getDisplayName();
            Hint desc = new Hint(this, displayName, context.parserResult.getSnapshot().getSource().getFileObject(),
                    range, fixList, 490);
            result.add(desc);
        }
    }

    @Override
    public boolean appliesTo(RuleContext context) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ExtractCode.class, "ExtractCode");
    }

    @Override
    public String getId() {
        return "ExtractCode"; // NOI18N
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    @Override
    public boolean showInTasklist() {
        return false;
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

    private static class ExtractCodeFix implements PreviewableFix {
        private final PythonRuleContext context;
        //private Position callSitePos;
        //private Position extractedPos;
        private int finalCallSiteOffset;
        private int finalExtractedSiteOffset;
        ;
        private final int type;
        private final int start;
        private final int end;
        private String newName;

        private ExtractCodeFix(PythonRuleContext context,
                int start, int end, int type) {
            this.context = context;

            OffsetRange range = PythonLexerUtils.narrow(context.doc, new OffsetRange(start, end), false);
            this.start = range.getStart();
            this.end = range.getEnd();

            this.type = type;
        }

        @Override
        public String getDescription() {
            switch (type) {
            case INTRODUCE_VARIABLE:
                return NbBundle.getMessage(CreateDocString.class, "IntroduceVariable");
            case INTRODUCE_CONSTANT:
                return NbBundle.getMessage(CreateDocString.class, "IntroduceConstant");
            case INTRODUCE_METHOD:
                return NbBundle.getMessage(CreateDocString.class, "IntroduceMethod");
            case INTRODUCE_FIELD:
                return NbBundle.getMessage(CreateDocString.class, "IntroduceField");
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public boolean canPreview() {
            return true;
        }

        @Override
        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            PythonParserResult info = (PythonParserResult) context.parserResult;
            EditList edits = new EditList(doc);

            int extractedOffset = doc.getLength();
            int prevFunctionOffset = 0;
            PythonTree root = PythonAstUtils.getRoot(info);

            OffsetRange narrowed = PythonLexerUtils.narrow(doc, new OffsetRange(start, end), true);

            int astStart = PythonAstUtils.getAstOffset(info, narrowed != OffsetRange.NONE ? narrowed.getStart() : start);
            int astEnd = PythonAstUtils.getAstOffset(info, narrowed != OffsetRange.NONE ? narrowed.getEnd() : end);
            if (astStart == -1 || astEnd == -1) {
                return edits;
            }
            AstPath startPath = AstPath.get(root, astStart);
            AstPath endPath = AstPath.get(root, astEnd);
            PythonTree localScope = PythonAstUtils.getLocalScope(startPath);
            if (localScope != null) {
                OffsetRange astRange = PythonAstUtils.getRange(localScope);
                OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
                if (lexRange != OffsetRange.NONE) {
                    extractedOffset = lexRange.getEnd();

                    // Function end offsets are a bit sloppy so try to deal with that
                    int firstNonWhite = Utilities.getRowFirstNonWhite(doc, Math.min(extractedOffset, doc.getLength()));
                    if (firstNonWhite == -1 || extractedOffset <= firstNonWhite) {
                        extractedOffset = Utilities.getRowStart(doc, extractedOffset);
                    }

                    prevFunctionOffset = lexRange.getStart();
                    if (extractedOffset > doc.getLength()) {
                        extractedOffset = doc.getLength();
                    }
                }
            }
            int callSiteOffset = start;
            int callSiteReplaceLength = end - start;

            int indentSize = IndentUtils.indentLevelSize(doc);
            int lineStart = Utilities.getRowStart(doc, prevFunctionOffset);
            int initialIndent = IndentUtils.lineIndent(doc, lineStart);
            String initialIndentStr = IndentUtils.createIndentString(doc, initialIndent);

            newName = "new_name"; // TODO - localize!

            // Compute input/output arguments
            PythonTree startNode = startPath.leaf();
            PythonTree endNode = endPath.leaf();

            InputOutputFinder finder = new InputOutputFinder(startNode, endNode, Collections.<PythonTree>emptyList());
            finder.traverse(localScope);
            List<String> inParams = new ArrayList<>(finder.getInputVars());
            List<String> outParams = new ArrayList<>(finder.getOutputVars());
            Collections.sort(inParams);
            Collections.sort(outParams);

            ClassDef cls = PythonAstUtils.getClassDef(startPath);

            // Adjust the insert location in case we are at the top level
            if (cls == null && PythonAstUtils.getFuncDef(startPath) == null) {
                extractedOffset = -1;
                PythonTree top = startPath.topModuleLevel();
                if (top != null) {
                    OffsetRange astRange = PythonAstUtils.getRange(top);
                    extractedOffset = PythonLexerUtils.getLexerOffset(info, astRange.getStart());
                }

                // We're at the top level - I can't insert the function -after- the current function
                // because that will result in a runtime error
                if (extractedOffset == -1) {
                    extractedOffset = Utilities.getRowStart(doc, Math.min(doc.getLength(), start));
                }
            }

            String extractedCode = null;
            int extractedSiteDelta = 0;
            if (type == INTRODUCE_METHOD) {
                StringBuilder sb = new StringBuilder();
                if (Utilities.getRowStart(doc, Math.min(extractedOffset, doc.getLength())) < extractedOffset) {
                    sb.append("\n"); // NOI18N
                }
                sb.append("\n"); // NOI18N
                sb.append(initialIndentStr);
                sb.append("def "); // NOI18N
                extractedSiteDelta = sb.length();
                sb.append(newName);
                sb.append("("); // NOI18N
                if (cls != null) {
                    sb.append("self"); // NOI18N
                    if (inParams.size() > 0) {
                        sb.append(", "); // NOI18N
                    }
                }
                boolean first = true;
                for (String param : inParams) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", "); // NOI18N
                    }
                    sb.append(param);
                }
                sb.append("):\n"); // NOI18N

                // Copy in the extracted code
                int firstIndent = IndentUtils.lineIndent(doc, Utilities.getRowStart(doc, start));
                for (int offset = start; offset < end; offset = Utilities.getRowEnd(doc, offset) + 1) {
                    // TODO - handle multiline literal strings correctly!!!
                    if (!(Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset))) {
                        int lineIndent = IndentUtils.lineIndent(doc, Utilities.getRowStart(doc, offset));
                        int newIndent = (lineIndent - firstIndent) + initialIndent + indentSize;
                        if (newIndent > 0) {
                            sb.append(IndentUtils.createIndentString(doc, newIndent));
                        }
                        int rowFirstNonWhite = Utilities.getRowFirstNonWhite(doc, offset);
                        int rowLastNonWhite = Utilities.getRowLastNonWhite(doc, offset) + 1; // +1: doesn't include last char
                        sb.append(doc.getText(rowFirstNonWhite, rowLastNonWhite - rowFirstNonWhite));
                    }
                    sb.append("\n"); // NOI18N
                }
                sb.append("\n");

                if (outParams.size() > 0) {
                    sb.append(IndentUtils.createIndentString(doc, initialIndent + indentSize));
                    sb.append("return "); // NOI18N
                    first = true;
                    for (String param : outParams) {
                        if (first) {
                            first = false;
                        } else {
                            // No spaces in the comma list for return tuples
                            sb.append(","); // NOI18N
                        }
                        sb.append(param);
                    }
                    sb.append("\n\n"); // NOI18N
                }

                // Insert the extracted code at the end
                extractedCode = sb.toString();
            } else {
                assert (type == INTRODUCE_FIELD || type == INTRODUCE_CONSTANT || type == INTRODUCE_VARIABLE);
                throw new RuntimeException("Not yet implemented");
            }

            // Replace the code at the extract site with just the call
            StringBuilder sb = new StringBuilder();
            if (type == INTRODUCE_METHOD) {
                // Assign to the output variables if any
                if (outParams.size() > 0) {
                    boolean first = true;
                    for (String param : outParams) {
                        if (first) {
                            first = false;
                        } else {
                            // No spaces in the comma list for return tuples
                            sb.append(","); // NOI18N
                        }
                        sb.append(param);
                    }

                    sb.append(" = ");
                }
            } else {
                assert (type == INTRODUCE_FIELD || type == INTRODUCE_CONSTANT || type == INTRODUCE_VARIABLE);
            }

            int callSiteDelta = sb.length();
            sb.append(newName);
            if (type == INTRODUCE_METHOD) {
                sb.append('(');
                if (cls != null) {
                    sb.append("self"); // NOI18N
                    if (inParams.size() > 0) {
                        sb.append(", "); // NOI18N
                    }
                }
                boolean first = true;
                for (String param : inParams) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", "); // NOI18N
                    }
                    sb.append(param);
                }
                sb.append(')');
            }
            String callSiteCode = sb.toString();


            // Apply changes
            if (extractedOffset >= callSiteOffset && extractedOffset <= callSiteOffset + callSiteReplaceLength) {
                if (extractedOffset > callSiteOffset) {
                    // We're trying to insert the extracted code segment after the call - that must mean we're
                    // in something like a function
                    edits.replace(callSiteOffset, callSiteReplaceLength, callSiteCode + extractedCode, false, 0);

                    // Work around bug in Document.Position
                    //extractedPos = edits.createPosition(callSiteOffset+callSiteCode.length()+extractedSiteDelta, Bias.Forward);
                    //callSitePos = edits.createPosition(callSiteOffset+callSiteDelta, Bias.Forward);
                    finalCallSiteOffset = callSiteOffset + callSiteDelta;
                    finalExtractedSiteOffset = callSiteOffset + callSiteCode.length() + extractedSiteDelta;
                } else {
                    edits.replace(callSiteOffset, callSiteReplaceLength, extractedCode + callSiteCode, false, 0);

                    // Work around bug in Document.Position
                    //extractedPos = edits.createPosition(callSiteOffset+extractedSiteDelta, Bias.Forward);
                    //callSitePos = edits.createPosition(callSiteOffset+extractedCode.length()+callSiteDelta, Bias.Forward);
                    finalCallSiteOffset = callSiteOffset + extractedCode.length() + callSiteDelta;
                    finalExtractedSiteOffset = callSiteOffset + extractedSiteDelta;
                }
            } else {
                edits.replace(extractedOffset, 0, extractedCode, false, 1);
                edits.replace(callSiteOffset, callSiteReplaceLength, callSiteCode, false, 0);

                // There's a bug document/editlist position code - the offsets aren't updated on my
                // edits! For now just compute the offsets directly since we know the exact edits applied
                //extractedPos = edits.createPosition(extractedOffset+extractedSiteDelta, Bias.Backward);
                //callSitePos = edits.createPosition(callSiteOffset+callSiteDelta, Bias.Backward);
                if (extractedOffset < callSiteOffset) {
                    finalCallSiteOffset = callSiteOffset + callSiteDelta + extractedCode.length();
                    finalExtractedSiteOffset = extractedOffset + extractedSiteDelta;
                } else {
                    finalCallSiteOffset = callSiteOffset + callSiteDelta;
                    finalExtractedSiteOffset = extractedOffset + extractedSiteDelta + callSiteCode.length() - callSiteReplaceLength;
                }
            }

            return edits;
        }

        @Override
        public void implement() throws Exception {
            EditList edits = getEditList();

            edits.apply();

            // Refactoring isn't necessary here since local variables and block
            // variables are limited to the local scope, so we can accurately just
            // find their positions using the AST and let the user edit them synchronously.
            Set<OffsetRange> ranges = new HashSet<>();
            int length = newName.length();
            ranges.add(new OffsetRange(finalCallSiteOffset, finalCallSiteOffset + length));
            ranges.add(new OffsetRange(finalExtractedSiteOffset, finalExtractedSiteOffset + length));

            // Initiate synchronous editing:
            EditRegions.getInstance().edit(context.parserResult.getSnapshot().getSource().getFileObject(), ranges, finalExtractedSiteOffset);
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

    /** @todo Prune search in traverse, ala AstPath.
     *  @todo Build up start and end AstPaths.
     */
    private static class ApplicabilityVisitor extends Visitor {
        private boolean applies = true;
        private int disabled;
        private int enabled;
        private final int start;
        private final int end;

        static int getType(PythonTree root, OffsetRange astRange) {
            ApplicabilityVisitor visitor = new ApplicabilityVisitor(astRange);
            try {
                visitor.visit(root);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return 0;
            }
            return visitor.getType();
        }

        ApplicabilityVisitor(OffsetRange astRange) {
            this.start = astRange.getStart();
            this.end = astRange.getEnd();
        }

        private void enable(PythonTree node, int mask) {
            if (node.getCharStartIndex() >= start && node.getCharStopIndex() <= end) {
                enabled |= mask;
            }
        }

        private void disable(PythonTree node, int mask) {
            if (node.getCharStartIndex() >= start && node.getCharStopIndex() <= end) {
                disabled |= mask;
            }
        }

        public int getType() {
            return enabled & ~disabled;
        }

        private void maybeBail(PythonTree node) {
            int nodeStart = node.getCharStartIndex();
            int nodeEnd = node.getCharStopIndex();
            if (nodeStart >= start && nodeStart < end) {
                applies = false;
                disable(node, ALL);
            }
            if (nodeEnd > start && nodeEnd < end) {
                applies = false;
                disable(node, ALL);
            }
        }

        @Override
        public void traverse(PythonTree node) throws Exception {
            if (!applies) {
                return;
            }

            int nodeStart = node.getCharStartIndex();
            int nodeStop = node.getCharStopIndex();
            //if (!(nodeStop < start || nodeStart > end)) {
            if (nodeStop >= start && nodeStart <= end) {
                super.traverse(node);
            }
        }

        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            maybeBail(node);
            return super.visitClassDef(node);
        }

        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            maybeBail(node);
            return super.visitFunctionDef(node);
        }

        @Override
        public Object visitImport(Import node) throws Exception {
            disable(node, ALL);
            return super.visitImport(node);
        }

        @Override
        public Object visitImportFrom(ImportFrom node) throws Exception {
            disable(node, ALL);
            return super.visitImportFrom(node);
        }

        @Override
        public Object visitAssign(Assign node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            disable(node, NON_EXPRESSIONS);
            return super.visitAssign(node);
        }

        @Override
        public Object visitCall(Call node) throws Exception {
            enable(node, ALL);
            disable(node, INTRODUCE_CONSTANT);
            return super.visitCall(node);
        }

        @Override
        public Object visitAugAssign(AugAssign node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitAugAssign(node);
        }

        @Override
        public Object visitBreak(Break node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitBreak(node);
        }

        @Override
        public Object visitContinue(Continue node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitContinue(node);
        }

        @Override
        public Object visitDelete(Delete node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitDelete(node);
        }

        @Override
        public Object visitFor(For node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitFor(node);
        }

        @Override
        public Object visitIf(If node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitIf(node);
        }

        @Override
        public Object visitIfExp(IfExp node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitIfExp(node);
        }

        @Override
        public Object visitPrint(Print node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitPrint(node);
        }

        @Override
        public Object visitYield(Yield node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitYield(node);
        }

        @Override
        public Object visitWith(With node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitWith(node);
        }

        @Override
        public Object visitWhile(While node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitWhile(node);
        }

        @Override
        public Object visitTryFinally(TryFinally node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitTryFinally(node);
        }

        @Override
        public Object visitTryExcept(TryExcept node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitTryExcept(node);
        }

        @Override
        public Object visitSuite(Suite node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitSuite(node);
        }

        @Override
        public Object visitReturn(Return node) throws Exception {
//            disable(node, NON_EXPRESSIONS);
            // TODO - handle flow control!!
            disable(node, ALL);
            return super.visitReturn(node);
        }

        @Override
        public Object visitModule(Module node) throws Exception {
            if (node.getCharStartIndex() > start && node.getCharStopIndex() < end) {
//                disable(node, NON_EXPRESSIONS);
                disable(node, ALL);
            }
            return super.visitModule(node);
        }

        @Override
        public Object visitPass(Pass node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitPass(node);
        }

        @Override
        public Object visitRaise(Raise node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitRaise(node);
        }

        @Override
        public Object visitAssert(Assert node) throws Exception {
            disable(node, NON_EXPRESSIONS);
            return super.visitAssert(node);
        }

        @Override
        public Object visitNum(Num node) throws Exception {
            enable(node, ALL);
            return super.visitNum(node);
        }

        @Override
        public Object visitName(Name node) throws Exception {
            enable(node, ALL);
            return super.visitName(node);
        }

        @Override
        public Object visitGlobal(Global node) throws Exception {
            enable(node, ALL);
            disable(node, INTRODUCE_CONSTANT);
            return super.visitGlobal(node);
        }

        @Override
        public Object visitTuple(Tuple node) throws Exception {
            enable(node, ALL);
            return super.visitTuple(node);
        }

        @Override
        public Object visitStr(Str node) throws Exception {
            enable(node, ALL);
            return super.visitStr(node);
        }
    }
}
