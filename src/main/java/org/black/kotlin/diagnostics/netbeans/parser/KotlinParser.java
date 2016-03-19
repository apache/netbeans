package org.black.kotlin.diagnostics.netbeans.parser;

import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;


/**
 *
 * @author Александр
 */
public class KotlinParser extends Parser {

    private Snapshot snapshot;
    private AnalysisResultWithProvider parserResult;
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        this.snapshot = snapshot;
        KtFile fileToAnalyze = ProjectUtils.getKtFile(snapshot.getSource().getFileObject());
        parserResult = 
                KotlinAnalyzer.analyzeFile((KotlinProject) OpenProjects.getDefault().getOpenProjects()[0], fileToAnalyze);
        
    }

    @Override
    public Result getResult(Task task) {
        return new KotlinParserResult(snapshot, parserResult);
    }


    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    public static class KotlinParserResult extends ParserResult {
//        ParserResult result;
        private boolean valid = true;
        private final AnalysisResultWithProvider analysisResult;
        
        KotlinParserResult(Snapshot snapshot, AnalysisResultWithProvider analysisResult){
            super(snapshot);
            this.analysisResult = analysisResult;
        }

        @Override
        protected void invalidate() {
            valid = false;
        }

        public AnalysisResultWithProvider getAnalysisResult() throws ParseException{
            if (!valid) throw new ParseException();
            return analysisResult;
        }

        @Override
        public List<? extends Error> getDiagnostics() {
            return Collections.EMPTY_LIST;
        }
        
    }

}
