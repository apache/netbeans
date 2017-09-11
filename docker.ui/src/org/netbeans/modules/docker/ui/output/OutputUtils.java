/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.output;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.lib.terminalemulator.Term;
import org.netbeans.modules.docker.api.DockerContainer;
import org.netbeans.modules.docker.api.DockerException;
import org.netbeans.modules.docker.api.DockerAction;
import org.netbeans.modules.terminal.api.IOConnect;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.netbeans.modules.docker.api.ActionChunkedResult;
import org.netbeans.modules.docker.api.ActionStreamResult;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public final class OutputUtils {

    private static final Logger LOGGER = Logger.getLogger(OutputUtils.class.getName());

    private static final Map<DockerContainer, LogConnect> LOGS = new WeakHashMap<>();

    private static final Map<DockerContainer, InputOutput> TERMS = new WeakHashMap<>();

    private static final RequestProcessor RP = new RequestProcessor(OutputUtils.class.getName(), Integer.MAX_VALUE);

    private OutputUtils() {
        super();
    }

    public static void openLog(final DockerContainer container, final ExceptionHandler handler) {
        final LogConnect logIO = getLogInputOutput(container);
        if (logIO.isConnected()) {
            logIO.getInputOutput().select();
            return;
        }

        // we run in separate thread as logs action may block even before returning
        // the result (in cases when there is no log output)
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    DockerAction facade = new DockerAction(container.getInstance());
                    ActionChunkedResult result = facade.logs(container);
                    try {
                        logIO.getInputOutput().getOut().reset();
                    } catch (IOException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    }
                    logIO.connect(result);
                    logIO.getInputOutput().select();
                } catch (DockerException ex) {
                    if (handler != null) {
                        handler.handleException(ex);
                    } else {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                }
            }
        });
    }

    public static void openTerminal(final DockerContainer container, final ActionStreamResult r,
            final boolean stdin, final boolean logs, final ExceptionHandler handler) {

        Pair<InputOutput, Boolean> termIO = getTerminalInputOutput(container);
        final InputOutput io = termIO.first();
        if (IOTerm.isSupported(io)) {
            if (termIO.second()) {
                focusTerminal(io);
            } else {
                // we run in separate thread as logs action may block even before returning
                // the result (in cases when there is no log output)
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DockerAction facade = new DockerAction(container.getInstance());
                            ActionStreamResult result = r != null ? r : facade.attach(container, stdin, logs);

                            try {
                                io.getOut().reset();
                            } catch (IOException ex) {
                                LOGGER.log(Level.FINE, null, ex);
                            }
                            if (!result.hasTty() && IOEmulation.isSupported(io)) {
                                IOEmulation.setDisciplined(io);
                            }

                            TerminalResizeListener l = null;
                            if (result.hasTty() && IOResizable.isSupported(io)) {
                                l = new TerminalResizeListener(io, container);
                                IONotifier.addPropertyChangeListener(io, l);
                            }

                            IOTerm.connect(io, stdin ? result.getStdIn() : null,
                                    new TerminalInputStream(io, result.getStdOut(), result, l),
                                    result.getStdErr(), result.getCharset().name());
                            focusTerminal(io);
                        } catch (DockerException ex) {
                            if (handler != null) {
                                handler.handleException(ex);
                            } else {
                                LOGGER.log(Level.WARNING, null, ex);
                            }
                        }
                    }
                };

                if (r != null) {
                    task.run();
                } else {
                    RP.post(task);
                }
            }
        } else {
            io.select();
        }
    }

    @NbBundle.Messages({
        "# {0} - container id",
        "LBL_LogInputOutput=Log {0}"
    })
    private static LogConnect getLogInputOutput(DockerContainer container) {
        synchronized (LOGS) {
            LogConnect connect = LOGS.get(container);
            if (connect == null) {
                InputOutput io = IOProvider.getDefault().getIO(
                        Bundle.LBL_LogInputOutput(container.getShortId()), true);
                connect = new LogConnect(io);
                LOGS.put(container, connect);
            }
            return connect;
        }
    }

    private static Pair<InputOutput, Boolean> getTerminalInputOutput(DockerContainer container) {
        synchronized (TERMS) {
            InputOutput io = TERMS.get(container);
            if (io == null) {
                io = IOProvider.get("Terminal") // NOI18N
                        .getIO(container.getShortId(), new Action[]{new TerminalOptionsAction()});
                TERMS.put(container, io);
                return Pair.of(io, false);
            }
            return Pair.of(io, IOConnect.isSupported(io) && IOConnect.isConnected(io));
        }
    }

    private static void focusTerminal(InputOutput io) {
        io.select();
        if (IOTerm.isSupported(io)) {
            // XXX is there a better way ?
             IOTerm.requestFocus(io);            
        }
    }

    private static class LogConnect {

        private final InputOutput io;

        private Future task;

        public LogConnect(InputOutput io) {
            this.io = io;
        }

        public InputOutput getInputOutput() {
            return io;
        }

        public synchronized void connect(ActionChunkedResult result) {
            task = new ChunkedResultOutputTask(io, result).start();
        }

        public synchronized boolean isConnected() {
            return task != null && !task.isDone();
        }
    }

}
