/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.hints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.Lines;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class HtmlRuleContext {

    private final SyntaxAnalyzerResult syntaxAnalyzerResult;
    private final HtmlParserResult parserResult;
    private final List<HintFix> defaultFixes;
    private final List<? extends Error> leftDiagnostics;
    private CssIndex cssIndex;
    private DependenciesGraph cssDependencies;
    private final Lines lines;
    private final Collection<Integer> linesWithHints;

    public HtmlRuleContext(HtmlParserResult parserResult, SyntaxAnalyzerResult syntaxAnalyzerResult, List<HintFix> defaultFixes) {
        this.parserResult = parserResult;
        this.syntaxAnalyzerResult = syntaxAnalyzerResult;
        this.defaultFixes = defaultFixes;
        this.leftDiagnostics = new ArrayList<>(parserResult.getDiagnostics(EnumSet.allOf(Severity.class)));
        this.lines = new Lines(parserResult.getSnapshot().getText());
        this.linesWithHints = new HashSet<>();
    }

    public boolean isFirstHintForPosition(int offset) {
        try {
            int lineIndex = lines.getLineIndex(offset);
            if(linesWithHints.contains(lineIndex)) {
                return false;
            } else {
                linesWithHints.add(lineIndex);
                return true;
            }
            
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }
    
    public HtmlParserResult getHtmlParserResult() {
        return parserResult;
    }

    public SyntaxAnalyzerResult getSyntaxAnalyzerResult() {
        return syntaxAnalyzerResult;
    }

    public FileObject getFile() {
        return getSnapshot().getSource().getFileObject();
    }

    public Snapshot getSnapshot() {
        return getHtmlParserResult().getSnapshot();
    }

    public List<HintFix> getDefaultFixes() {
        return defaultFixes;
    }

    public List<? extends Error> getLeftDiagnostics() {
        return leftDiagnostics;
    }

    public synchronized CssIndex getCssIndex() throws IOException {
        if (cssIndex == null) {
            Project project = FileOwnerQuery.getOwner(getFile());
            if (project != null) {
                cssIndex = CssIndex.get(project);
            }
        }
        return cssIndex;
    }
    
    public synchronized DependenciesGraph getCssDependenciesGraph() throws IOException {
        if(cssDependencies == null) {
            CssIndex index = getCssIndex();
            if(index != null) {
                cssDependencies = index.getDependencies(getFile());
            }
        }
        return cssDependencies;
    }
    
}
