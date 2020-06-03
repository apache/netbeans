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

import org.netbeans.modules.cnd.testrunner.spi.TestRecognizerHandler;
import org.netbeans.modules.cnd.testrunner.spi.TestHandlerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.TestSession;

/**
 *
 */
public final class TestRunnerLineConvertor implements LineConvertor {

    private static final Logger LOGGER = Logger.getLogger(TestRunnerLineConvertor.class.getName());

    private final Manager manager;
    private TestSession session;
    private final List<TestRecognizerHandler> handlers;

    public TestRunnerLineConvertor(Manager manager, TestSession session, List<TestHandlerFactory> handlerFactories) {
        this.manager = manager;
        this.session = session;
        this.handlers = new ArrayList<>();
        handlerFactories.forEach((factory) -> {
            this.handlers.addAll(factory.createHandlers());
        });
    }

    public synchronized void refreshSession() {
        RerunHandler handler = this.session.getRerunHandler();
        this.session = new TestSession(session.getName(), session.getProject(), session.getSessionType());
        session.setRerunHandler(handler);
    }

    @Override
    public synchronized List<ConvertedLine> convert(String line) {

        Optional<TestRecognizerHandler> handlerOpt = handlers.stream()
                .filter(handler -> handler.matches(line))
                .findFirst();

        if (handlerOpt.isPresent()) {
            TestRecognizerHandler handler = handlerOpt.get();
            LOGGER.log(Level.FINE, "Handler [{0}] matched line: {1}", new Object[]{handler, line});
            try {
                handler.updateUI(manager, session);
                if (handler.isPerformOutput()) {
                    session.addOutput(line);
                    manager.displayOutput(session, line, false);
                }
                // Convert ConvertedLine to Strings
                return handler.getRecognizedOutput().stream()
                        .map((cLine) -> ConvertedLine.forText(cLine, null))
                        .collect(Collectors.toList());
            } catch (Exception x) {
                // ISE is thrown when mathing a group fails, should be enough to log a warning
                // IOOBE is thrown when there is no group with the expected index.
                LOGGER.log(Level.WARNING, "Failed to process line: " + line + " with handler: " + handler, x);
            }
        }
        LOGGER.log(Level.FINE, "No handler for line: {0}", line);
        session.addOutput(line);
        manager.displayOutput(session, line, false);
        return null;
    }
}
