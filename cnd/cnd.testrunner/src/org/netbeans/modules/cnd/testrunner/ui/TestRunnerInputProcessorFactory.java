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
