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

package org.netbeans.modules.mercurial.remote;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.List;
import java.net.URL;
import java.util.HashSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.mercurial.remote.ui.repository.HgURL;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.awt.HtmlBrowser;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputWriter;
import org.openide.windows.OutputListener;

/**
 *
 * 
 */
public class OutputLogger {

    private InputOutput log;
    private String repositoryRootString;
    private boolean writable;
    private static final RequestProcessor rp = new RequestProcessor("MercurialRemoteOutput", 1); //NOI18N
    public static final int MAX_LINES_TO_PRINT = 500;

    private static final HashSet<String> openedWindows = new HashSet<>(5);


    static OutputLogger getLogger(String repositoryRoot) {
        if (repositoryRoot != null) {
            return new OutputLogger(repositoryRoot);
        } else {
            return new NullLogger();
        }
    }

    public static OutputLogger getLogger (VCSFileProxy repository) {
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
            Mercurial.LOG.log(Level.FINE, "Creating OutputLogger for {0}", repositoryRootString);
            log = IOProvider.getDefault().getIO(repositoryRootString, false);
            if (!openedWindows.contains(repositoryRootString)) {
                // log window has been opened
                writable = HgModuleConfig.getDefault(null).getAutoOpenOutput();
                openedWindows.add(repositoryRootString);
                if (!writable) {
                    // close it again
                    log.closeInputOutput();
                }
            }
        }
        if (log.isClosed()) {
            if (HgModuleConfig.getDefault(null).getAutoOpenOutput()) {
                Mercurial.LOG.log(Level.FINE, "Creating OutputLogger for {0}", repositoryRootString);
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
            @Override
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
            @Override
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
        if( list.isEmpty()) {
            return;
        }

        rp.post(new Runnable() {
            @Override
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
        if( msg == null) {
            return;
        }
        rp.post(new Runnable() {
            @Override
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
        if( msg == null) {
            return;
        }

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

    /**
     * Print URL to OutputLogger's tab as an active Hyperlink
     *
     * @param String sURL to print out
     * 
     */
    public void outputLink(final String sURL){
        if (sURL == null) {
            return;
        }
         
        rp.post(new Runnable() {
            @Override
            public void run() {                
                try {
                    OutputWriter out = getLog().getOut();
                    if (writable) {
                        OutputListener listener = new OutputListener() {
                            @Override
                            public void outputLineAction(OutputEvent ev) {
                                try {
                                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(sURL));
                                } catch (IOException ex) {
                                    // Ignore
                                }
                            }
                            @Override
                            public void outputLineSelected(OutputEvent ev) {}
                            @Override
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
            @Override
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
                @Override
                public void actionPerformed(ActionEvent e) {
                    writable = true;
                    getLog().select();
                }
            };
        }
        return action;
    }
     
    private static class NullLogger extends OutputLogger {

        @Override
        public void closeLog() {
        }

        @Override
        public void flushLog() {
        }

        @Override
        public void output(List<String> list){
        }

        @Override
        public void output(String msg){
        }
        @Override
        public void outputInRed(String msg){
        }
        @Override
        public void outputLink(final String sURL){
        }
        @Override
        public void clearOutput(){
        }
    }

}
