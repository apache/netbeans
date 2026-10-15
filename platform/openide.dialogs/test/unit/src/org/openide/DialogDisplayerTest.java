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
package org.openide;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sdedic
 */
public class DialogDisplayerTest extends NbTestCase {

    public DialogDisplayerTest(String name) {
        super(name);
    }
    
    public static class TestDisplayer extends DialogDisplayer {
        private final List<String> inputs = new ArrayList<>();

        @Override
        public Object notify(NotifyDescriptor descriptor) {
            if (!(descriptor instanceof NotifyDescriptor.InputLine)) {
                throw new UnsupportedOperationException();
            }
            NotifyDescriptor.InputLine il = (NotifyDescriptor.InputLine)descriptor;
            il.setInputText(inputs.remove(0));
            il.setValue(NotifyDescriptor.OK_OPTION);
            return NotifyDescriptor.OK_OPTION;
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        MockServices.setServices();
        super.tearDown(); 
    }
    
    
    
    public void testExampleHandlingWithCancel() throws Exception {
        class UserData {
            String answer1;
            String answer2;
        }
        MockServices.setServices(TestDisplayer.class);
        
        TestDisplayer td = (TestDisplayer)DialogDisplayer.getDefault();
        td.inputs.add("Answer One");
        td.inputs.add("Answer Two");

        // @start region="notifyFuture"
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine("Question", "Title");
        
        CompletableFuture<UserData> resultFuture = DialogDisplayer.getDefault().notifyFuture(nd).
                // compose with processing after dialog confirmation
                thenCompose(d -> {
                    UserData userData = new UserData();
                    // this code will NOT execute, if user cancels "Question" dialog. Neither will execute subsequent steps.
                    userData.answer1 = d.getInputText();
                    // do something with user input and display another question
                    NotifyDescriptor.InputLine nd2 = new NotifyDescriptor.InputLine("Question2", "Title");

                    return DialogDisplayer.getDefault().notifyFuture(nd).
                        thenApply(x -> {
                        // pass userData to the next step. 
                        // This code will NOT execute if the Question2 dialog is cancelled.
                        userData.answer2 = x.getInputText();
                        return userData;
                    });
                }).thenApply((data) -> {
                    // This code will not execute if Question or Question2 is cancelled.
                    // do some finalization steps. 
                    return data;
                }).exceptionally(ex -> {
                    // JDK-8233050: JDK reports direct exception from the immediate stage, but wrapped one from earlier stages.
                    if (ex instanceof CompletionException) {
                        ex = ex.getCause();
                    }
                    // reached if Question, Question2 is cancelled or if some error occurs.
                    if (!(ex instanceof CancellationException)) {
                        // do error handling
                    }
                    return null;
                });
        // @end region="notifyFuture"
        UserData d = resultFuture.get();
        assertNotNull(d);
    }

}
