/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.diagnostics.netbeans.parser;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiErrorElement;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.jetbrains.kotlin.resolve.KotlinAnalyzer;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.AnalyzingUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
/**
 *
 * @author Александр
 */
public class KotlinParser extends Parser {

    private Snapshot snapshot;
    private static AnalysisResultWithProvider parserResult;
    private static KtFile fileToAnalyze;
    private Project project;
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        this.snapshot = snapshot;
        
        project = ProjectUtils.getKotlinProjectForFileObject(snapshot.getSource().getFileObject());

        if (project == null){
            return;
        }
        
        fileToAnalyze = ProjectUtils.getKtFile(snapshot.getText().toString(),snapshot.getSource().getFileObject());
        int caretOffset = GsfUtilities.getLastKnownCaretOffset(snapshot, event);
        
        if (caretOffset <= 0) {
            parserResult = KotlinAnalysisProjectCache.INSTANCE.getAnalysisResult(project);
            return;
        }
        
        parserResult =
            KotlinAnalyzer.analyzeFile(project, fileToAnalyze);
    }

    public static AnalysisResultWithProvider getAnalysisResult() {
        return parserResult;
    }
    
    public static KtFile getFile() {
        return fileToAnalyze;
    }
    
    // for tests only
    public static void setAnalysisResult(KtFile ktFile, AnalysisResultWithProvider analysisResult) {
        parserResult = analysisResult;
        fileToAnalyze = ktFile;
    }
    
    @Override
    public Result getResult(Task task) {
        if (project != null){
            return new KotlinParserResult(snapshot, parserResult, fileToAnalyze, project);
        }
        return null;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    
}
