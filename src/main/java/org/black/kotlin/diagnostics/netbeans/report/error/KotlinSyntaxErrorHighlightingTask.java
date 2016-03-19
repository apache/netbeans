package org.black.kotlin.diagnostics.netbeans.report.error;

import com.intellij.openapi.util.TextRange;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinSyntaxErrorHighlightingTask extends ParserResultTask {

    @Override
    public void run(Result result, SchedulerEvent event) {
        try {
            KotlinParserResult parserResult = (KotlinParserResult) result;
            Document document = result.getSnapshot().getSource().getDocument(false);
            List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
            Collection<Diagnostic> diagnostics = parserResult.getAnalysisResult().getAnalysisResult().getBindingContext().getDiagnostics().all();
            for (Diagnostic diagnostic : diagnostics) {
                List<TextRange> textRanges = diagnostic.getTextRanges();
                
                int startBeginLine = NbDocument.findLineNumber((StyledDocument) document, textRanges.get(0).getStartOffset());
                int startBeginColumn = NbDocument.findLineColumn((StyledDocument) document, textRanges.get(0).getStartOffset());

                int endBeginLine = NbDocument.findLineNumber((StyledDocument) document, textRanges.get(0).getEndOffset());
                int endBeginColumn = NbDocument.findLineColumn((StyledDocument) document, textRanges.get(0).getEndOffset());

                int start = NbDocument.findLineOffset((StyledDocument) document, startBeginLine)
                        + startBeginColumn;
                int end = NbDocument.findLineOffset((StyledDocument) document, endBeginLine)
                        + endBeginColumn;
                
                if (diagnostic.getSeverity() == org.jetbrains.kotlin.diagnostics.Severity.ERROR){
                    ErrorDescription errorDescription = ErrorDescriptionFactory.createErrorDescription(
                            Severity.ERROR,
                            diagnostic.toString(),
                            document,
                            document.createPosition(start),
                            document.createPosition(end)
                    );
                    errors.add(errorDescription);
                } 
                else if (diagnostic.getSeverity() == org.jetbrains.kotlin.diagnostics.Severity.WARNING){
                    ErrorDescription errorDescription = ErrorDescriptionFactory.createErrorDescription(
                            Severity.WARNING,
                            diagnostic.toString(),
                            document,
                            document.createPosition(start),
                            document.createPosition(end)
                    );
                    errors.add(errorDescription);
                }

            }
            HintsController.setErrors(document, "kotlin", errors);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
    }

}
