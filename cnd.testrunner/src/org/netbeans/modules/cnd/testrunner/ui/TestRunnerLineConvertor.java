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
