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
package org.netbeans.modules.groovy.editor.api.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.ASTUtils.FakeASTNode;
import org.netbeans.modules.groovy.editor.occurrences.VariableScopeVisitor;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.options.MarkOccurencesSettings;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.filesystems.FileObject;

/**
 * The (call-)proctocol for OccurrencesFinder is always:
 * 
 * 1.) setCaretPosition() = NUMBER
 * 2.) run()
 * 3.) getOccurrences()
 * 
 * @author Martin Adamek
 * @author Matthias Schmidt
 */
public class GroovyOccurrencesFinder extends OccurrencesFinder<GroovyParserResult> {

    private boolean cancelled;
    private int caretPosition;
    private Map<OffsetRange, ColoringAttributes> occurrences = Map.of();
    private FileObject file;
    private static final Logger LOG = Logger.getLogger(GroovyOccurrencesFinder.class.getName());

    public GroovyOccurrencesFinder() {
        super();
    }

    @Override
    @SuppressWarnings("ReturnOfCollectionOrArrayField") // immutable collection
    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        LOG.log(Level.FINEST, "getOccurrences()\n"); //NOI18N
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
    public int getPriority() {
        return 200;
    }

    @Override
    public final Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void run(GroovyParserResult result, SchedulerEvent event) {
        LOG.log(Level.FINEST, "run()"); //NOI18N
        
        resume();
        if (isCancelled()) {
            return;
        }

        FileObject currentFile = result.getSnapshot().getSource().getFileObject();
        // FIXME parsing API - null file
        if (currentFile != file) {
            // Ensure that we don't reuse results from a different file
            occurrences = Map.of();
            file = currentFile;
        }

        ModuleNode rootNode = ASTUtils.getRoot(result);
        if (rootNode == null) {
            return;
        }
        int astOffset = ASTUtils.getAstOffset(result, caretPosition);
        if (astOffset == -1) {
            return;
        }
        BaseDocument document = LexUtilities.getDocument(result, false);
        if (document == null) {
            LOG.log(Level.FINEST, "Could not get BaseDocument. It's null"); //NOI18N
            return;
        }

        final AstPath path = new AstPath(rootNode, astOffset, document);
        final ASTNode closest = path.leaf();

        LOG.log(Level.FINEST, "path = {0}", path); //NOI18N
        LOG.log(Level.FINEST, "closest: {0}", closest); //NOI18N

        if (closest == null) {
            return;
        }

        Map<OffsetRange, ColoringAttributes> highlights = new HashMap<>(100);
        highlight(path, highlights, document, caretPosition);

        if (isCancelled()) {
            return;
        }

        if (!highlights.isEmpty()) {
            Map<OffsetRange, ColoringAttributes> translated = new HashMap<>(2 * highlights.size());
            for (Map.Entry<OffsetRange, ColoringAttributes> entry : highlights.entrySet()) {
                OffsetRange range = LexUtilities.getLexerOffsets(result, entry.getKey());
                if (range != OffsetRange.NONE) {
                    translated.put(range, entry.getValue());
                }
            }
            highlights = translated;
            this.occurrences = Collections.unmodifiableMap(highlights);
        } else {
            this.occurrences = Map.of();
        }

    }

    /**
     * 
     * @param position
     */
    @Override
    public void setCaretPosition(int position) {
        this.caretPosition = position;
        LOG.log(Level.FINEST, "\n\nsetCaretPosition() = {0}\n", position); //NOI18N
    }

    @Override
    public boolean isKeepMarks() {
        return MarkOccurencesSettings
                .getCurrentNode()
                .getBoolean(MarkOccurencesSettings.KEEP_MARKS, true);
    }

    @Override
    public boolean isMarkOccurrencesEnabled() {
        return MarkOccurencesSettings
                .getCurrentNode()
                .getBoolean(MarkOccurencesSettings.ON_OFF, true);
    }

    private static void highlight(AstPath path, Map<OffsetRange, ColoringAttributes> highlights, BaseDocument document, int cursorOffset) {
        ASTNode root = path.root();
        assert root instanceof ModuleNode;
        ModuleNode moduleNode = (ModuleNode) root;
        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(moduleNode.getContext(), path, document, cursorOffset);
        scopeVisitor.collect();
        for (ASTNode astNode : scopeVisitor.getOccurrences()) {
            OffsetRange range;
            if (astNode instanceof FakeASTNode fakeASTNode) {
                String text = astNode.getText();
                ASTNode orig = fakeASTNode.getOriginalNode();
                int line = orig.getLineNumber();
                int column = orig.getColumnNumber();
                if (line > 0 && column > 0) {
                    int start = ASTUtils.getOffset(document, line, column);
                    range = ASTUtils.getNextIdentifierByName(document, text, start);
                } else {
                    range = OffsetRange.NONE;
                }
            // 155573 - check cursor in class but not on class name
            } else if (astNode instanceof ClassNode && path.leaf() instanceof ClassNode) {
                ClassNode found = (ClassNode) astNode;
                ClassNode leaf = (ClassNode) path.leaf();
                if (found == leaf) {
                   OffsetRange rangeClassName = ASTUtils.getRange(astNode, document);
                   if (rangeClassName.containsInclusive(cursorOffset)) {
                       range = rangeClassName;
                   } else {
                       // we are completely wrong
                       highlights.clear();
                       break;
                   }
                } else {
                    range = ASTUtils.getRange(astNode, document);
                }
            } else {
                range = ASTUtils.getRange(astNode, document);
            }
            if (range != OffsetRange.NONE) {
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }
    }

}
