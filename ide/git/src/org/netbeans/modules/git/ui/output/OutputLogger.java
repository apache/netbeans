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

package org.netbeans.modules.git.ui.output;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * TODO cache output if the window is closed... Probably a limited-size list of some kind and when the window is opened, write the content of the list.
 * @author Ondra Vrabec, Tomas Stupka
 */
public class OutputLogger {

    private InputOutput log;
    private File repositoryRoot;
    private boolean writable;
    private static final RequestProcessor rp = new RequestProcessor("GitOutput", 1); //NOI18N
    private static final Logger LOG = Logger.getLogger(OutputLogger.class.getName());
    private static final Set<InputOutput> openWindows = Collections.newSetFromMap(new WeakHashMap<>(5));

    public static OutputLogger getLogger (File repositoryRoot) {
        if (repositoryRoot != null) {
            return new OutputLogger(repositoryRoot);
        } else {
            return new NullLogger();
        }
    }
    private AbstractAction action;

    private OutputLogger (File repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
    }

    private OutputLogger() {
    }

    /**
     * @return the log
     */
    private InputOutput getLog() {
        if(log == null) {
            LOG.log(Level.FINE, "Creating OutputLogger for {0}", repositoryRoot.getAbsolutePath()); //NOI18N
            log = IOProvider.getDefault().getIO(repositoryRoot.getName() + " - " + repositoryRoot.getAbsolutePath(), false); //NOI18N
            writable = true;
            if (!openWindows.contains(log)) {
                writable = false;
                writable = GitModuleConfig.getDefault().getAutoOpenOutput();
                openWindows.add(log);
                if (!writable) {
                    // close it again
                    log.closeInputOutput();
                }
            }
        }
        if (log.isClosed()) {
            LOG.log(Level.FINE, "Creating OutputLogger for {0}", repositoryRoot); //NOI18N
            log = IOProvider.getDefault().getIO(repositoryRoot.getName() + " - " + repositoryRoot.getAbsolutePath(), false); //NOI18N
            try {
                log.getOut().reset();
            } catch (IOException ex) {
            }
            writable = false;
        }
        return log;
    }

    public void closeLog() {
        rp.post(new Runnable() {
            @Override
            public void run() {
                if (log != null) {
                    log.getOut().close();
                    log.getErr().close();
                    if (!writable) {
                        log.closeInputOutput();
                    }
                }
            }
        });
    }

    public void flushLog () {
        rp.post(new Runnable() {
            @Override
            public void run() {
                getLog().getOut().flush();
                getLog().getErr().flush();
            }
        });
    }

    public void outputInRed (final String msg) {
        if( msg == null) return;
        rp.post(new Runnable() {
            @Override
            public void run() {
                OutputWriter out = getLog().getErr();
                if (writable) {
                    out.println(msg);
                    out.flush();
                }
            }
        });
    }
    
    public void outputLine (final String msg) {
        if(msg == null) return;
        rp.post(new Runnable() {
            @Override
            public void run() {
                OutputWriter out = getLog().getOut();
                out.println(msg);
                out.flush();
            }
        });
    }
    
    public void output (final String msg, final OutputListener list) {
        if(msg == null) return;
        rp.post(new Runnable() {
            @Override
            public void run() {
                InputOutput out = getLog();
                if (writable) {
                    if (IOColorPrint.isSupported(out) && IOColors.isSupported(out)) {
                        Color c;
                        if (list == null) {
                            c = IOColors.getColor(out, IOColors.OutputType.OUTPUT);
                        } else {
                            c = IOColors.getColor(out, IOColors.OutputType.HYPERLINK);
                        }
                        try {
                            IOColorPrint.print(log, msg, list, false, c);
                        } catch (IOException ex) {
                            out.getOut().print(msg);
                        }
                    } else {
                        out.getOut().print(msg);
                    }
                }
            }
        });
    }

    public Action getOpenOutputAction () {
        if(action == null) {
            action = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getLog().select();
                    writable = true;
                    openWindows.add(log);
                }
            };
        }
        return action;
    }

    public void outputFile (final String message, final File file, final int linkStartPosition) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                OutputWriter out = getLog().getOut();
                out.print(message.substring(0, linkStartPosition));
                try {
                    out.println(message.substring(linkStartPosition), new OutputListener() {
                        @Override
                        public void outputLineSelected(OutputEvent ev) {
                        }

                        @Override
                        public void outputLineAction (OutputEvent ev) {
                            new OpenInEditorAction(new File[] { file }).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, file.getAbsolutePath()));
                        }

                        @Override
                        public void outputLineCleared(OutputEvent ev) {
                        }
                    });
                } catch (IOException ex) {
                    out.println(message.substring(linkStartPosition));
                }
                out.flush();
            }
        });
    }

    private static class NullLogger extends OutputLogger {

        @Override
        public void closeLog() {
        }

        @Override
        public void flushLog() {
        }

        @Override
        public void outputLine(String msg){
        }

        @Override
        public void output (String msg, OutputListener list) {
        }

        @Override
        public Action getOpenOutputAction() {
            return null;
        }

        @Override
        public void outputFile (String message, File file, int linkStartPosition) {
        }

        @Override
        public void outputInRed (String msg) {
        }
    }

}
