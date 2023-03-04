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
package org.netbeans.modules.javafx2.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.text.NbDocument;

/**
 *
 * @author sdedic
 */
public class ErrorHintProvider extends ParserResultTask<FxmlParserResult> {

    private FxmlParserResult result;
    private Document document;
    
    @Override
    public void run(FxmlParserResult result, SchedulerEvent event) {
        Collection<ErrorMark> marks = result.getProblems();
        
        document = result.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            // no op
            return;
        }
        List<ErrorDescription> descs = new ArrayList<ErrorDescription>();
        for (ErrorMark m : marks) {
            try {
                descs.add(ErrorDescriptionFactory.createErrorDescription(
                        m.isError() ? Severity.ERROR : Severity.WARNING,
                        m.getMessage(),
                        document, 
                        NbDocument.createPosition(document, 
                            m.getOffset(), Position.Bias.Forward),
                        NbDocument.createPosition(document, 
                            m.getOffset() + m.getLen(), Position.Bias.Forward))
                    );
            } catch (BadLocationException ex) {
                // ignore
            }
        }
        HintsController.setErrors(document, "fxml-parsing", descs);
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
    
    @MimeRegistration(mimeType=JavaFXEditorUtils.FXML_MIME_TYPE, service=TaskFactory.class)
    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new ErrorHintProvider());
        }
        
    }
    
}
