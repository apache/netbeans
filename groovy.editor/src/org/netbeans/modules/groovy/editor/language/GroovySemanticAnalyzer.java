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

package org.netbeans.modules.groovy.editor.language;

import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.language.SemanticAnalysisVisitor;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 * 
 * @author MArtin Adamek
 */
public class GroovySemanticAnalyzer extends SemanticAnalyzer<GroovyParserResult> {

    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    public GroovySemanticAnalyzer() {
        
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
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
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }


    @Override
    public void run(GroovyParserResult result, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }

        ASTNode root = ASTUtils.getRoot(result);

        if (root == null) {
            return;
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights =
            new HashMap<OffsetRange, Set<ColoringAttributes>>(100);

        AstPath path = new AstPath();
        path.descend(root);

        BaseDocument doc = LexUtilities.getDocument(result.getSnapshot().getSource(), false);
        if (doc == null) {
            return;
        }

        SemanticAnalysisVisitor visitor = new SemanticAnalysisVisitor((ModuleNode) root, doc);
        highlights.putAll(visitor.annotate());

        path.ascend();

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            // FIXME parsing API
            //if (parserResult.getTranslatedSource() != null) {
                Map<OffsetRange, Set<ColoringAttributes>> translated = new HashMap<OffsetRange,Set<ColoringAttributes>>(2*highlights.size());
                for (Map.Entry<OffsetRange,Set<ColoringAttributes>> entry : highlights.entrySet()) {
                    OffsetRange range = LexUtilities.getLexerOffsets(result, entry.getKey());
                    if (range != OffsetRange.NONE) {
                        translated.put(range, entry.getValue());
                    }
                }  
                highlights = translated;
            //}
            
            this.semanticHighlights = highlights;
        } else {
            this.semanticHighlights = null;
        }
    }

}
