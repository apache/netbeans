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
package org.netbeans.modules.templatesui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach
 */
final class RunTCK extends AbstractWizard {
    private final URL html;
    private final URL test;
    private final String init;
    private final CountDownLatch ready = new CountDownLatch(1);
    private final TCK tck = new TCK();
    private Throwable error;
    
    static void test(String prefix, String init) throws Throwable {
        new RunTCK(
            RunTCK.class.getResource(prefix + ".html"),
            RunTCK.class.getResource(prefix + ".js"),
            init
        ).test();
    }
    
    private RunTCK(URL html, URL test, String init) throws Exception {
        this.html = html;
        this.test = test;
        this.init = init;
        component(0);
    }

    @Override
    protected Object initSequence(ClassLoader l) throws Exception {
        return init;
    }

    @Override
    protected URL initPage(ClassLoader l) {
        return html;
    }

    @Override
    protected void initializationDone(Throwable t) {
        error = t;
        ready.countDown();
    }
    
    @Override
    protected String[] getTechIds() {
        return new String[0];
    }
    
    private void test() throws Throwable {
        ready.await();
        if (error != null) {
            throw error;
        }
        
        executeScript(
              "window.assertEquals = function(exp, real, msg) {\n"
            + "  if (exp != real) {\n"
            + "    throw msg + ' expected: ' + exp + ' real: ' + real;\n"
            + "  }\n"
            + "};\n"
        );
        
        Object regFn = executeScript("(function(def) { window.tck = def; })");
        evaluateCall(regFn, tck);
        
        String str = readUrl();
        executeScript(str);
    }

    private String readUrl() throws IOException {
        InputStreamReader r = new InputStreamReader(test.openStream());
        StringBuilder sb = new StringBuilder();
        for (;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        return sb.toString();
    }
    
    public final class TCK {
        final RequestProcessor rp = new RequestProcessor("Validating");
        private volatile RequestProcessor.Task lastTask;
        
        TCK() {
        }
        
        public String[] steps(boolean localized) {
            return RunTCK.this.steps(localized);
        }
        
        public String current() {
            return RunTCK.this.currentStep();
        }
        
        public void next() throws InterruptedException {
            final WizardDescriptor.AsynchronousValidatingPanel<?> avp = (WizardDescriptor.AsynchronousValidatingPanel<?>) RunTCK.this.current();
            if (RunTCK.this.prepareValidation()) {
                RequestProcessor.Task task = rp.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            avp.validate();
                            RunTCK.this.nextPanel();
                        } catch (WizardValidationException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            lastTask = null;
                        }
                    }
                });
                lastTask = task;
            } else {
                if (RunTCK.this.isValid()) {
                    RunTCK.this.nextPanel();
                }
            }
        }
        
        public void previous() {
            RunTCK.this.previousPanel();
        }
        
        public Object data() {
            return RunTCK.this.data();
        }
        
        private final List<Object> later = new ArrayList<>();
        public void invokeLater(Object fn) {
            later.add(fn);
        }
        
        public void invokeNow() {
            RunTCK.this.invokeFn(later.toArray());
            RequestProcessor.Task taskToWaitFor;
            taskToWaitFor = lastTask;
            if (taskToWaitFor != null) {
                taskToWaitFor.waitFinished();
            }
        }
    }
}
