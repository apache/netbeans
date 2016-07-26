package org.black.kotlin.diagnostics.netbeans.parser;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import java.util.List;
import javax.swing.event.ChangeListener;
//import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.AnalyzingUtils;
import org.netbeans.api.project.Project;
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
    private KtFile fileToAnalyze;
    private Project project;
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) {
        this.snapshot = snapshot;
        
        project = ProjectUtils.getKotlinProjectForFileObject(snapshot.getSource().getFileObject());

        if (project == null){
            return;
        }
        
        fileToAnalyze = ProjectUtils.getKtFile(snapshot.getText().toString(),snapshot.getSource().getFileObject());
        parserResult =
            KotlinAnalyzer.analyzeFile(project, fileToAnalyze);
        
    }

    @Override
    public Result getResult(Task task) {
        if (project != null){
            return new KotlinParserResult(snapshot, parserResult, fileToAnalyze);
        }
        return null;
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
        private final KtFile ktFile;
        
        KotlinParserResult(Snapshot snapshot, AnalysisResultWithProvider analysisResult, KtFile ktFile) {
            super(snapshot);
            this.analysisResult = analysisResult;
            file = snapshot.getSource().getFileObject();
            this.ktFile = ktFile;
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
        
        public KtFile getKtFile(){
            return ktFile;
        }

        @Override
        public List<? extends Error> getDiagnostics() {
            List<Error> errors = Lists.newArrayList();
            for (Diagnostic diagnostic : analysisResult.getAnalysisResult().
                    getBindingContext().getDiagnostics().all()) {
                KotlinError error = new KotlinError(diagnostic, file);
                errors.add(error);
            }
            for (PsiErrorElement psiError : AnalyzingUtils.getSyntaxErrorRanges(ktFile)){
                KotlinSyntaxError syntaxError = new KotlinSyntaxError(psiError, file);
                errors.add(syntaxError);
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
        
        public PsiElement getPsi(){
            return diagnostic.getPsiElement();
        }
        
        @Override
        public String toString() {
            return diagnostic.toString();
        }
        
        @Override
        public boolean showExplorerBadge() {
            return diagnostic.getSeverity() == 
                    org.jetbrains.kotlin.diagnostics.Severity.ERROR;
        }

        @Override
        public String getDisplayName() {
            return ErrorMessages.render(diagnostic);
        }

        @Override
        public String getDescription() {
            return "";
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
            switch (diagnostic.getSeverity()){
                case ERROR:
                    return Severity.ERROR;
                case WARNING:
                    return Severity.WARNING;
                case INFO:
                    return Severity.INFO;
                default:
                    return null;
            }
        }

        @Override
        public Object[] getParameters() {
            return null;
        }
        
    }
    
    public static class KotlinSyntaxError implements Badging {

        private final PsiErrorElement psiError;
        private final FileObject file;
        
        public KotlinSyntaxError(PsiErrorElement psiError, FileObject file){
            this.psiError = psiError;
            this.file = file;
        }
        
        @Override
        public boolean showExplorerBadge() {
            return true;
        }

        @Override
        public String getDisplayName() {
            return psiError.getErrorDescription();
        }

        @Override
        public String getDescription() {
            return "";
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
            return psiError.getTextRange().getStartOffset();
        }

        @Override
        public int getEndPosition() {
            return psiError.getTextRange().getEndOffset();
        }

        @Override
        public boolean isLineError() {
            return false;
        }

        @Override
        public Severity getSeverity() {
            return Severity.ERROR;
        }

        @Override
        public Object[] getParameters() {
            return null;
        }
        
    }
    
}
