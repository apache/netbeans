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

package org.netbeans.modules.mercurial;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.List;
import java.net.URL;
import java.util.HashSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.awt.HtmlBrowser;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputWriter;
import org.openide.windows.OutputListener;

/**
 *
 * @author Tomas Stupka
 */
public class OutputLogger {

    private InputOutput log;
    private boolean ignoreCommand = false;
    private String repositoryRootString;
    private boolean empty;
    private boolean writable;
    private static final RequestProcessor rp = new RequestProcessor("MercurialOutput", 1);
    public static final int MAX_LINES_TO_PRINT = 500;

    private static final String MSG_TOO_MANY_LINES = "The number of output lines is greater than 500; see message log for complete output";
    private static final HashSet<String> openedWindows = new HashSet<String>(5);


    static OutputLogger getLogger(String repositoryRoot) {
        if (repositoryRoot != null) {
            return new OutputLogger(repositoryRoot);
        } else {
            return new NullLogger();
        }
    }

    public static OutputLogger getLogger (File repository) {
        if (repository != null) {
            return getLogger(new HgURL(repository).toHgCommandUrlStringWithoutUserInfo());
        } else {
            return new NullLogger();
        }
    }
    private AbstractAction action;
    
    private OutputLogger(String repositoryRoot) {
        repositoryRootString = repositoryRoot;
    }

    private OutputLogger() {
    }

    /**
     * @return the log
     */
    private InputOutput getLog() {
        writable = true;
        if(log == null) {
            Mercurial.LOG.fine("Creating OutputLogger for " + repositoryRootString);
            log = IOProvider.getDefault().getIO(repositoryRootString, false);
            if (!openedWindows.contains(repositoryRootString)) {
                // log window has been opened
                writable = HgModuleConfig.getDefault().getAutoOpenOutput();
                openedWindows.add(repositoryRootString);
                if (!writable) {
                    // close it again
                    log.closeInputOutput();
                }
            }
        }
        if (log.isClosed()) {
            if (HgModuleConfig.getDefault().getAutoOpenOutput()) {
                Mercurial.LOG.fine("Creating OutputLogger for " + repositoryRootString);
                log = IOProvider.getDefault().getIO(repositoryRootString, false);
                try {
                    // HACK (mystic logic) workaround, otherwise it writes to nowhere
                    log.getOut().reset();
                } catch (IOException e) {
                    Mercurial.LOG.log(Level.SEVERE, null, e);
                }
            } else {
                writable = false;
            }
        }
        return log;
    }

    public void closeLog() {
        rp.post(new Runnable() {
            public void run() {
                if (log != null && writable) {
                    log.getOut().close();
                    log.getErr().close();
                }
            }
        });
    }

    public void flushLog() {
        rp.post(new Runnable() {
            public void run() {
                getLog();
                if (writable) {
                    getLog().getOut().flush();
                    getLog().getErr().flush();
                }
            }
        });        
    }

    /**
     * Print contents of list to OutputLogger's tab
     *
     * @param list to print out
     * 
     */
     public void output(final List<String> list){
        if( list.isEmpty()) return;

        rp.post(new Runnable() {
            public void run() { 
                OutputWriter out = getLog().getOut();
                if (writable) {
                    for (String s : list) {
                        out.println(s);
                    }
                    out.flush();
                }
            }
        });
    }

    /**
     * Print msg to OutputLogger's tab
     *
     * @param String msg to print out
     * 
     */
    public void output(final String msg){
        if( msg == null) return;
        rp.post(new Runnable() {
            public void run() {
                OutputWriter out = getLog().getOut();
                if (writable) {
                    out.println(msg);
                    out.flush();
                }
            }
        });
    }

    /**
     * Print msg to OutputLogger's tab in Red
     *
     * @param String msg to print out
     * 
     */
    public void outputInRed(final String msg){
        if( msg == null) return;

        rp.post(new Runnable() {
            public void run() {
                OutputWriter out = getLog().getErr();
                if (writable) {
                    out.println(msg);
                    out.flush();
                }
            }
        });
    }

    /**
     * Print URL to OutputLogger's tab as an active Hyperlink
     *
     * @param String sURL to print out
     * 
     */
    public void outputLink(final String sURL){
        if (sURL == null) return;
         
        rp.post(new Runnable() {
            public void run() {                
                try {
                    OutputWriter out = getLog().getOut();
                    if (writable) {
                        OutputListener listener = new OutputListener() {
                            public void outputLineAction(OutputEvent ev) {
                                try {
                                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(sURL));
                                } catch (IOException ex) {
                                    // Ignore
                                }
                            }
                            public void outputLineSelected(OutputEvent ev) {}
                            public void outputLineCleared(OutputEvent ev) {}
                        };
                        out.println(sURL, listener, true);
                        out.flush();
                    }
                } catch (IOException ex) {
                // Ignore
                }
            }
        });
    }

    /**
     * Select and Clear OutputLogger's tab
     *
     * @param list to print out
     * 
     */
    public void clearOutput(){
        rp.post(new Runnable() {
            public void run() {             
                OutputWriter out = getLog().getOut();
                if (writable) {
                    try {
                        out.reset();
                    } catch (IOException ex) {
                        // Ignore Exception
                    }
                    out.flush();
                }
            }
        });
    }

    public Action getOpenOutputAction() {
        if(action == null) {
            action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    writable = true;
                    getLog().select();
                }
            };
        }
        return action;
    }
     
    private static class NullLogger extends OutputLogger {

        public void closeLog() {
        }

        public void flushLog() {
        }

        public void output(List<String> list){
        }

        public void output(String msg){
        }
        public void outputInRed(String msg){
        }
        public void outputLink(final String sURL){
        }
        public void clearOutput(){
        }
    }

}
