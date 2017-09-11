/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.git.ui.output;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
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
    private static final WeakSet<InputOutput> openWindows = new WeakSet<InputOutput>(5);

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
