package org.black.kotlin.diagnostics.netbeans.parser;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Error.Badging;
import org.netbeans.modules.csl.api.Severity;
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
    private AnalysisResultWithProvider parserResult;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        this.snapshot = snapshot;
        KtFile fileToAnalyze = ProjectUtils.getKtFile(snapshot.getSource().getFileObject());
        parserResult
                = KotlinAnalyzer.analyzeFile((KotlinProject) OpenProjects.getDefault().getOpenProjects()[0], fileToAnalyze);

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

        private boolean valid = true;
        private final AnalysisResultWithProvider analysisResult;
        private final FileObject file;
        
        KotlinParserResult(Snapshot snapshot, AnalysisResultWithProvider analysisResult) {
            super(snapshot);
            this.analysisResult = analysisResult;
            file = snapshot.getSource().getFileObject();
        }

        @Override
        protected void invalidate() {
            valid = false;
        }

        public AnalysisResultWithProvider getAnalysisResult() throws ParseException {
            if (!valid) {
                throw new ParseException();
            }
            return analysisResult;
        }

        @Override
        public List<? extends Error> getDiagnostics() {
            List<KotlinError> errors = Lists.newArrayList();
            for (Diagnostic diagnostic : analysisResult.getAnalysisResult().
                    getBindingContext().getDiagnostics().all()) {
                if (diagnostic.getSeverity() == org.jetbrains.kotlin.diagnostics.Severity.ERROR ||
                        diagnostic.getSeverity() == org.jetbrains.kotlin.diagnostics.Severity.WARNING) {
                    KotlinError error = new KotlinError(diagnostic, file);
                    errors.add(error);
                }
            }
            return errors;
        }

    }

    public static class KotlinError implements Badging {

        private final Diagnostic diagnostic;
        private final FileObject file;
        
        public KotlinError(Diagnostic diagnostic, FileObject file){
            this.diagnostic = diagnostic;
            this.file = file;
        }
        
        @Override
        public boolean showExplorerBadge() {
            return diagnostic.getSeverity() == 
                    org.jetbrains.kotlin.diagnostics.Severity.ERROR;
        }

        @Override
        public String getDisplayName() {
            return diagnostic.toString();
        }

        @Override
        public String getDescription() {
            return "";//diagnostic.toString();
        }

        @Override
        public String getKey() {
            return "";
        }

        @Override
        public FileObject getFile() {
            return file;
        }

        @Override
        public int getStartPosition() {
            return diagnostic.getTextRanges().get(0).getStartOffset();
        }

        @Override
        public int getEndPosition() {
            return diagnostic.getTextRanges().get(0).getEndOffset();
        }

        @Override
        public boolean isLineError() {
            return false;
        }

        @Override
        public Severity getSeverity() {
            return diagnostic.getSeverity() == 
                    org.jetbrains.kotlin.diagnostics.Severity.ERROR ? Severity.ERROR : Severity.WARNING;
        }

        @Override
        public Object[] getParameters() {
            return null;
        }
        
    }
    
}
