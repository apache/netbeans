/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.groovy.editor.api.parser;

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
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.filesystems.FileObject;

/**
 * The (call-)proctocol for OccurrencesFinder is always:
 * 
 * 1.) setCaretPosition() = <number>
 * 2.) run()
 * 3.) getOccurrences()
 * 
 * @author Martin Adamek
 * @author Matthias Schmidt
 */
public class GroovyOccurrencesFinder extends OccurrencesFinder<GroovyParserResult> {

    private boolean cancelled;
    private int caretPosition;
    private Map<OffsetRange, ColoringAttributes> occurrences;
    private FileObject file;
    private static final Logger LOG = Logger.getLogger(GroovyOccurrencesFinder.class.getName());

    public GroovyOccurrencesFinder() {
        super();
    }

    @Override
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
            occurrences = null;
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

        Map<OffsetRange, ColoringAttributes> highlights = new HashMap<OffsetRange, ColoringAttributes>(100);
        highlight(path, highlights, document, caretPosition);

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            Map<OffsetRange, ColoringAttributes> translated = new HashMap<OffsetRange, ColoringAttributes>(2 * highlights.size());
            for (Map.Entry<OffsetRange, ColoringAttributes> entry : highlights.entrySet()) {
                OffsetRange range = LexUtilities.getLexerOffsets(result, entry.getKey());
                if (range != OffsetRange.NONE) {
                    translated.put(range, entry.getValue());
                }
            }
            highlights = translated;
            this.occurrences = highlights;
        } else {
            this.occurrences = null;
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

    private static void highlight(AstPath path, Map<OffsetRange, ColoringAttributes> highlights, BaseDocument document, int cursorOffset) {
        ASTNode root = path.root();
        assert root instanceof ModuleNode;
        ModuleNode moduleNode = (ModuleNode) root;
        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(moduleNode.getContext(), path, document, cursorOffset);
        scopeVisitor.collect();
        for (ASTNode astNode : scopeVisitor.getOccurrences()) {
            OffsetRange range;
            if (astNode instanceof FakeASTNode) {
                String text = astNode.getText();
                ASTNode orig = ((FakeASTNode) astNode).getOriginalNode();
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
