/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.testrunner.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public final class TestRunnerInputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

    private static final Logger LOGGER = Logger.getLogger(TestRunnerInputProcessorFactory.class.getName());

    private final Data data;

    public TestRunnerInputProcessorFactory(Manager manager, TestSession session, boolean printSummary) {
        this.data = new Data(manager, session, printSummary);
    }

    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
        return new TestRunnerInputProcessor(defaultProcessor, data);
    }

    // just a helper for holding conf data
    private static class Data {
        private final Manager manager;
        private final TestSession session;
        private final boolean printSummary;

        public Data(Manager manager, TestSession session, boolean printSummary) {
            this.manager = manager;
            this.session = session;
            this.printSummary = printSummary;
        }
    }

    private static class TestRunnerInputProcessor implements InputProcessor {

        private final InputProcessor delegate;
        private final Data data;

        public TestRunnerInputProcessor(InputProcessor delegate, Data data) {
            this.delegate = delegate;
            this.data = data;
        }

        public void processInput(char[] chars) throws IOException {
            delegate.processInput(chars);
        }

        public void reset() throws IOException {
            delegate.reset();
        }

        public void close() throws IOException {
            finish();
            delegate.close();
        }

        private synchronized void finish() {

            printSummary();

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Session finished: " + data.session);
            }
            data.manager.sessionFinished(data.session);
        }

        private void printSummary() {
            if (!data.printSummary) {
                return;
            }
            List<String> output = new ArrayList<String>(2);
            output.add(""); //NOI18N
            output.add(NbBundle.getMessage(TestRunnerInputProcessorFactory.class,
                    "MSG_TestSessionFinished", new Double(data.session.getSessionResult().getElapsedTime() / 1000d))); // NOI18N
            output.add(NbBundle.getMessage(TestRunnerInputProcessorFactory.class,
                    "MSG_TestSessionFinishedSummary", // NOI18N
                    data.session.getSessionResult().getTotal(),
                    data.session.getSessionResult().getFailed(),
                    data.session.getSessionResult().getErrors()));

            try {
                for (String line : output) {
                    delegate.processInput((line + "\n").toCharArray()); //NOI18N
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
