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
package org.netbeans.modules.python.editor;

import org.netbeans.modules.python.source.AstPath;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.netbeans.modules.python.source.PythonParserResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.python.source.lexer.PythonCommentTokenId;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Name;

/**
 * Occurrences finder for Python - highlights regions under the caret
 * as well as all other occurrences of the same symbol in the current file
 *
 * @todo Highlight if/elif/else keyword pairs
 *
 */
public class PythonOccurrencesMarker extends OccurrencesFinder<PythonParserResult> {
    private boolean cancelled;
    private int caretPosition;
    private Map<OffsetRange, ColoringAttributes> occurrences;
    /** For testsuite */
    static Throwable error;

    public PythonOccurrencesMarker() {
    }

    @Override
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return occurrences;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    @Override
    public final synchronized void cancel() {
        cancelled = true;
    }

    @Override
    public void setCaretPosition(int position) {
        this.caretPosition = position;
    }

    @Override
    public int getPriority() {
        return 0;
    }
    
    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void run(final PythonParserResult info, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }

        final PythonTree root = PythonAstUtils.getRoot(info);
        if (root == null) {
            return;
        }

        final int astOffset = PythonAstUtils.getAstOffset((ParserResult) info, caretPosition);
        if (astOffset == -1) {
            return;
        }

        //PythonTree closest = PythonPositionManager.findClosest(root, astOffset);
        final AstPath path = AstPath.get(root, astOffset);
        if (path == null) {
            return;
        }
        OffsetRange blankRange = info.getSanitizedRange();

        final PythonTree closest;
        if (blankRange.containsInclusive(astOffset)) {
            closest = null;
        } else {
            closest = path.leaf();
        }
        
        Document document = info.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            return;
        }


        final BaseDocument doc = (BaseDocument)document;
        doc.render(new Runnable() {

            @Override
            public void run() {
        Set<OffsetRange> offsets = null;
           
        TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPositionedSequence(doc, caretPosition);
        if (ts != null && ts.token().id() == PythonTokenId.COMMENT) {
            TokenSequence<PythonCommentTokenId> embedded = ts.embedded(PythonCommentTokenId.language());
            if (embedded != null) {
                embedded.move(caretPosition);
                if (embedded.moveNext() || embedded.movePrevious()) {
                    Token<PythonCommentTokenId> token = embedded.token();
                    PythonCommentTokenId id = token.id();
                    if (id == PythonCommentTokenId.SEPARATOR && caretPosition == embedded.offset() && embedded.movePrevious()) {
                        token = embedded.token();
                        id = token.id();
                    }
                    if (id == PythonCommentTokenId.VARNAME) {
                        String name = token.text().toString();

                        offsets = findNames(info, path, name, offsets);

                        int start = embedded.offset();
                        offsets.add(new OffsetRange(start, start + name.length()));

                        if (isCancelled()) {
                            return;
                        }

                        setHighlights(offsets);
                        return;

                    }
                }
            }
        }

        if (closest == null) {
            return;
        }

        boolean isNameNode = PythonAstUtils.isNameNode(closest);
        //if (isNameNode && !(path.leafParent() instanceof Call)) {
        if (isNameNode) {
            // TODO - how do I get the name?
            //String name = closest.getString();
            //addNodes(scopeNode != null ? scopeNode : root, name, highlights);
            //closest = null;
            String name = ((Name)closest).getInternalId();
            offsets = findNames(info, path, name, offsets);
        } else if (closest instanceof Attribute) {
            Attribute attr = (Attribute)closest;
            offsets = findSameAttributes(info, root, attr);
        } else if (closest instanceof Import || closest instanceof ImportFrom) {
            // Try to find occurrences of an imported symbol
            offsets = findNameFromImport(caretPosition, info, path, offsets);
        } else if ((closest instanceof FunctionDef || closest instanceof ClassDef) &&
                PythonAstUtils.getNameRange(null, closest).containsInclusive(astOffset)) {
            String name;
            if (closest instanceof FunctionDef) {
                name = ((FunctionDef)closest).getInternalName();
            } else {
                assert closest instanceof ClassDef;
                name = ((ClassDef)closest).getInternalName();
            }
            offsets = findNames(info, path, name, offsets);

            if (offsets == null || offsets.size() == 0) {
                if (closest instanceof FunctionDef) {
                    FunctionDef def = (FunctionDef)closest;
                    // Call: highlight calls and definitions
                    CallVisitor visitor = new CallVisitor(info, def, null);
                    PythonTree scope = PythonAstUtils.getClassScope(path);
                    try {
                        visitor.visit(scope);
                        Set<OffsetRange> original = offsets;
                        offsets = visitor.getRanges();
                        offsets.addAll(original);
                    } catch (Exception ex) {
                        error = ex;
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

        } else {
            Call call = null;
            FunctionDef def = null;
            if (!isNameNode) {
                PythonTree nearest = null;
                Iterator<PythonTree> it = path.leafToRoot();
                while (it.hasNext()) {
                    PythonTree node = it.next();
                    if (node instanceof Call || node instanceof FunctionDef) {
                        nearest = node;
                        break;
                    } else if (node instanceof ClassDef) {
                        break;
                    }
                }
                if (nearest != null) {
                    OffsetRange range = PythonAstUtils.getNameRange((PythonParserResult) info, nearest);
                    if (!range.containsInclusive(astOffset)) {
                        nearest = null;
                    }
                }
                if (nearest instanceof Call) {
                    call = (Call)nearest;
                } else if (nearest instanceof FunctionDef) {
                    def = (FunctionDef)nearest;
                }
            } else {
                call = (Call)path.leafParent();
            }
            if (call != null || def != null) {
                // Call: highlight calls and definitions
                CallVisitor visitor = new CallVisitor(info, def, call);
                PythonTree scope = PythonAstUtils.getClassScope(path);
                try {
                    visitor.visit(scope);
                    offsets = visitor.getRanges();
                } catch (Exception ex) {
                    error = ex;
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        if (isCancelled()) {
            return;
        }

        setHighlights(offsets);
            }
        });
    }

    private void setHighlights(Set<OffsetRange> offsets) {
        Map<OffsetRange, ColoringAttributes> highlights = null;

        if (offsets != null) {
            Map<OffsetRange, ColoringAttributes> h =
                    new HashMap<>(100);

            for (OffsetRange lexRange : offsets) {
                h.put(lexRange, ColoringAttributes.MARK_OCCURRENCES);
            }

            highlights = h;
        }

        // TODO - traverse looking for the same nodes
        // Decide what scope we have, whether we're looking for a function def/call
        // or a local parameter or var, etc.

        if (highlights != null && highlights.size() > 0) {
            this.occurrences = highlights;
        } else {
            this.occurrences = null;
        }
    }

    private static class CallVisitor extends Visitor {
        private final Call call;
        private final FunctionDef def;
        private final String name;
        private final Set<OffsetRange> ranges = new HashSet<>();
        private final PythonParserResult info;

        CallVisitor(PythonParserResult info, FunctionDef def, Call call) {
            this.info = info;
            this.def = def;
            this.call = call;

            if (call != null) {
                this.name = PythonAstUtils.getCallName(call);
            } else if (def != null) {
                this.name = def.getInternalName();
            } else {
                throw new IllegalArgumentException(); // call or def must be nonnull
            }
        }

        @Override
        public Object visitCall(Call node) throws Exception {
            if (node == call) {
                ranges.add(PythonAstUtils.getNameRange(info, node));
            } else {
                if (name != null && name.equals(PythonAstUtils.getCallName(node))) {
                    ranges.add(PythonAstUtils.getNameRange(info, node));
                }
            }

            return super.visitCall(node);
        }

        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            if (node == def || node.getInternalName().equals(name)) {
                ranges.add(PythonAstUtils.getNameRange(info, node));
            }

            return super.visitFunctionDef(node);
        }

        private Set<OffsetRange> getRanges() {
            return ranges;
        }
    }

    private Set<OffsetRange> findNameFromImport(int lexOffset, PythonParserResult ppr, AstPath path, Set<OffsetRange> offsets) {
        BaseDocument doc = (BaseDocument)ppr.getSnapshot().getSource().getDocument(false);
        try {
            doc.readLock();
            String identifier = Utilities.getIdentifier(doc, lexOffset);
            if (identifier == null) {
                return null;
            }
            if ("*".equals(identifier)) {
                // TODO - something more complicated...
                return null;
            }
            // TODO - determine if you're hovering over a whole module name instead of an imported
            // symbol, and if so, work a bit harder...
            if (identifier.length() > 0) {
                return findNames(ppr, path, identifier, offsets);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            doc.readUnlock();
        }

        return null;
    }

    private Set<OffsetRange> findNames(PythonParserResult ppr, AstPath path, String name, Set<OffsetRange> offsets) {
        //offsets = PythonAstUtils.getLocalVarOffsets(info, scope, name);
        return PythonAstUtils.getAllOffsets(ppr, path, caretPosition, name, false);
    }

    private Set<OffsetRange> findSameAttributes(PythonParserResult info, PythonTree root, Attribute attr) {
        List<PythonTree> result = new ArrayList<>();
        PythonAstUtils.addNodesByType(root, new Class[]{Attribute.class}, result);

        Set<OffsetRange> offsets = new HashSet<>();

        String attrName = attr.getInternalAttr();
        if (attrName != null) {
            String name = PythonAstUtils.getName(attr.getInternalValue());

            for (PythonTree node : result) {
                Attribute a = (Attribute)node;
                if (attrName.equals(a.getInternalAttr()) && (name == null || name.equals(PythonAstUtils.getName(a.getInternalValue())))) {
                    OffsetRange astRange = PythonAstUtils.getRange(node);
                    // Adjust to be the -value- part
                    int start = a.getInternalValue().getCharStopIndex() + 1;
                    if (start < astRange.getEnd()) {
                        astRange = new OffsetRange(start, astRange.getEnd());
                    }

                    OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
                    if (name != null && (node instanceof Import || node instanceof ImportFrom)) {
                        // Try to find the exact spot
                        lexRange = PythonLexerUtils.getImportNameOffset((BaseDocument)info.getSnapshot().getSource().getDocument(false), lexRange, node, name);
                    }
                    if (lexRange != OffsetRange.NONE) {
                        offsets.add(lexRange);
                    }
                }
            }
        }

        return offsets;
    }
}
