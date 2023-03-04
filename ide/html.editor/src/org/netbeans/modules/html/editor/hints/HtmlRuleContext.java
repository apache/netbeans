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
