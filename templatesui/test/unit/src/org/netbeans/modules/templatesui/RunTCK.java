/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.templatesui;

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
              "function assertEquals(exp, real, msg) {\n"
            + "  if (exp != real) {\n"
            + "    throw msg + ' expected: ' + exp + ' real: ' + real;\n"
            + "  }\n"
            + "}\n"
        );
        
        Object regFn = executeScript("(function(def) { window.tck = def; })");
        evaluateCall(regFn, tck);
        
        InputStreamReader r = new InputStreamReader(test.openStream());
        StringBuilder sb = new StringBuilder();
        for (;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        executeScript(sb.toString());
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
