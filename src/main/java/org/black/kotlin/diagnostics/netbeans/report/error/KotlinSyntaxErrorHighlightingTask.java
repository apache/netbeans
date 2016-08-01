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
package org.black.kotlin.diagnostics.netbeans.report.error;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.black.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.netbeans.modules.csl.api.Error;

/**
 *
 * @author Александр
 */
public class KotlinSyntaxErrorHighlightingTask extends ParserResultTask<KotlinParserResult> {

    @Override
    public void run(KotlinParserResult result, SchedulerEvent event) {
        try {
            KotlinParserResult parserResult = result;
            Document document = result.getSnapshot().getSource().getDocument(false);
            List<ErrorDescription> errorDescriptions = new ArrayList<ErrorDescription>();
            List<? extends Error> errors = 
                    parserResult.getDiagnostics();
            for (Error error : errors){
                Severity severity;
                switch (error.getSeverity()){
                    case ERROR:
                        severity = Severity.ERROR;
                        break;
                    case WARNING:
                        severity = Severity.WARNING;
                        break;
                    case INFO:
                    default:
                        severity = Severity.HINT;
                }
                
                int startBeginLine = NbDocument.findLineNumber((StyledDocument) document, error.getStartPosition());
                int startBeginColumn = NbDocument.findLineColumn((StyledDocument) document, error.getStartPosition());

                int endBeginLine = NbDocument.findLineNumber((StyledDocument) document, error.getEndPosition());
                int endBeginColumn = NbDocument.findLineColumn((StyledDocument) document, error.getEndPosition());

                int start = NbDocument.findLineOffset((StyledDocument) document, startBeginLine)
                        + startBeginColumn;
                int end = NbDocument.findLineOffset((StyledDocument) document, endBeginLine)
                        + endBeginColumn;
                
                ErrorDescription errorDescription = ErrorDescriptionFactory.createErrorDescription(
                        severity,
                        "",
                        document,
                        document.createPosition(start),
                        document.createPosition(end)
                );
                
                errorDescriptions.add(errorDescription);
            }
            HintsController.setErrors(document, "kotlin", errorDescriptions);
        } catch (BadLocationException ex) {
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
