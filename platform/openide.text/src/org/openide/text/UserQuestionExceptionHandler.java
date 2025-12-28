/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.text;

import java.io.IOException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.openide.text.AskEditorQuestions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.UserQuestionException;

/**
 * Extensible handler of UQEs.
 *
 * @author Miloslav Metelka
 */
class UserQuestionExceptionHandler implements Runnable {
    
    private final CloneableEditorSupport ces;
    
    private UserQuestionException uqe;
    
    private StyledDocument doc;
    
    UserQuestionExceptionHandler(CloneableEditorSupport ces, UserQuestionException uqe) {
        this.ces = ces;
        this.uqe = uqe;
    }

    void runInEDT() {
        AskEditorQuestions.QuestionResult shouldAsk = AskEditorQuestions.askUserQuestion(uqe);
        // attempt to handle automatic responses synchronously:
        if (AskEditorQuestions.QuestionResult.NO == shouldAsk) {
            openRefused();
            return;
        } else if (AskEditorQuestions.QuestionResult.YES == shouldAsk) {
            try {
                uqe.confirmed();
                uqe = null;
                doc = openDocument();
                opened(doc);
                return;
            } catch (UserQuestionException ex) {
                // bad luck, go for EDT access.
                uqe = ex;
            } catch (IOException ex1) {
                handleIOException(ex1);
                return;
            } catch (RuntimeException ex) {
                handleRuntimeException(ex);
                return;
            }
        }
        Mutex.EVENT.readAccess(this);
    }

    @Override
    public void run() {
        handleUserQuestionException();
    }
    
    /**
     * @return true if document was opened successfully or false otherwise.
     */
    boolean handleUserQuestionException() {
        handleStart();
        try {
            while (true) {
                AskEditorQuestions.QuestionResult shouldAsk = AskEditorQuestions.askUserQuestion(uqe);
                Object res;
                if (AskEditorQuestions.QuestionResult.ASK_USER == shouldAsk) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(uqe.getLocalizedMessage(), NotifyDescriptor.YES_NO_OPTION);
                    nd.setOptions(new Object[]{NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION});
                    res = DialogDisplayer.getDefault().notify(nd);
                } else if (AskEditorQuestions.QuestionResult.YES == shouldAsk) {
                    res = NotifyDescriptor.OK_OPTION;
                } else {
                    res = NotifyDescriptor.NO_OPTION;
                }
                 
                if (NotifyDescriptor.OK_OPTION.equals(res)) {
                    try {
                        uqe.confirmed();
                        uqe = null;
                        doc = openDocument();
                        opened(doc);
                        return true;
                        
                    } catch (UserQuestionException ex) {
                        uqe = ex;
                    } catch (IOException ex1) {
                        handleIOException(ex1);
                        return false;
                    } catch (RuntimeException ex) {
                        handleRuntimeException(ex);
                        return false;
                    }
                } else {
                    openRefused();
                    return false;
                }
            }
        }finally {
            handleEnd();
        }
    }
    
    protected StyledDocument openDocument() throws IOException {
        return ces.openDocument();
    }
    
    protected void handleStart() {
        // Do nothing by default - subclasses may override
    }
    
    protected void handleEnd() {
        // Do nothing by default - subclasses may override
    }

    protected void opened(StyledDocument openDoc) {
        // Do nothing by default - subclasses may override
    }
    
    protected void openRefused() {
        // Do nothing by default - subclasses may override
    }
    
    protected void handleIOException(IOException ex) {
        // Print exception by default - subclasses may override
        Exceptions.printStackTrace(ex);
    }
    
    protected void handleRuntimeException(RuntimeException ex) {
        // Print exception by default - subclasses may override
        Exceptions.printStackTrace(ex);
    }
    
    public final StyledDocument getDocument() {
        return doc;
    }
    
}
