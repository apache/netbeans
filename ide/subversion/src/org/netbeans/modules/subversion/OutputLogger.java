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

package org.netbeans.modules.subversion;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class OutputLogger implements ISVNNotifyListener {

    private InputOutput log;
    private boolean ignoreCommand = false;
    private String repositoryRootString;
    private static final RequestProcessor rp = new RequestProcessor("SubversionOutput", 1); // NOI18N
    private boolean writable; // output window is open and can be written into
    /**
     * cache of already opened output windows
     * IOProvider automatically opens OW after its first initialization,
     * in the second call it returns the handle from its cache and OW is not opened again.
     * So if this cache doesn't contain the repository string yet, it will probably mean the OW is automatically opened
     * and in that case it should be closed again. See getLog().
     */
    private static final HashSet<String> openedWindows = new HashSet<String>(5);
    private static final Pattern[] filePatterns = new Pattern[] {
        Pattern.compile("[AUCGE ][ UC][ BC][ C] ?(.+)"), //NOI18N
        Pattern.compile("Reverted '(.+)'"), //NOI18N - for commandline
        Pattern.compile("Reverted (.+)"), //NOI18N - for javahl
        Pattern.compile("Sending        (.+)"), //NOI18N
        Pattern.compile("Adding         (.+)") //NOI18N
    };
    
    public static OutputLogger getLogger(SVNUrl repositoryRoot) {
        if (repositoryRoot != null) {
            return new OutputLogger(repositoryRoot);
        } else {
            return new NullLogger();
        }
    }
    private AbstractAction action;
    private String lastCompletedMessage;
    
    private OutputLogger(SVNUrl repositoryRoot) {
        repositoryRootString = SvnUtils.decodeToString(repositoryRoot);
    }

    private OutputLogger() {
    }
    
    @Override
    public void logCommandLine(final String commandLine) {
        rp.post(new Runnable() {
            @Override
            public void run() {                        
                logln(commandLine, false);
                flush();
            }
        });        
    }

    private void flush () {
        if (writable) {
            getLog().getOut().flush();
        }
    }
    
    @Override
    public void logCompleted(final String message) {
        if (message.equals(lastCompletedMessage)) {
            return;
        }
        lastCompletedMessage = message;
        rp.post(new Runnable() {
            @Override
            public void run() {                
                logln(message, ignoreCommand);
                flush();
            }
        });        
    }
    
    @Override
    public void logError(final String message) {
        if (message == null) return;
        rp.post(new Runnable() {
            @Override
            public void run() {                
                logln(message, false);
                flush();
            }
        });            
    }
    
    @Override
    public void logMessage(final String message) {
        rp.post(new Runnable() {
            @Override
            public void run() {                
                logln(message, ignoreCommand);
                flush();
            }
        });
    }
    
    @Override
    public void logRevision(long revision, String path) {
       // logln(" revision " + revision + ", path = '" + path + "'");
    }
    
    @Override
    public void onNotify(File path, SVNNodeKind kind) {
        //logln(" file " + path + ", kind " + kind);
    }
    
    @Override
    public void setCommand(final int command) {
        rp.post(new Runnable() {
            @Override
            public void run() {        
                ignoreCommand = command == ISVNNotifyListener.Command.INFO ||
                                command == ISVNNotifyListener.Command.STATUS ||
                                command == ISVNNotifyListener.Command.ANNOTATE ||
                                command == ISVNNotifyListener.Command.LOG ||
                                command == ISVNNotifyListener.Command.LS;
            }
        });
    }
         
    public void closeLog() {
        rp.post(new Runnable() {
            @Override
            public void run() {
                if (log != null && writable) {
                    getLog().getOut().flush();
                    getLog().getOut().close();
                }
            }
        });
    }

    public void flushLog() {
        rp.post(new Runnable() {
            @Override
            public void run() {        
                getLog();
                flush();
            }
        });        
    }
    
    private void logln(String message, boolean ignore) {
        OpenFileOutputListener ol = null;
        for (Pattern p : filePatterns) {
            Matcher m = p.matcher(message);
            if (m.matches() && m.groupCount() > 0) {
                String path = m.group(1);
                File f = new File(path);
                if (!f.isDirectory()) {
                    ol = new OpenFileOutputListener(FileUtil.normalizeFile(f), m.start(1));
                    break;
                }
            }
        }
        log(message + "\n", ol, ignore); // NOI18N
    }

    private void log(String message, OpenFileOutputListener hyperlinkListener, boolean ignore) {
        if(ignore) {
            return;
        }
        if (getLog().isClosed()) {
            if (SvnModuleConfig.getDefault().getAutoOpenOutput()) {
                Subversion.LOG.log(Level.FINE, "Creating OutputLogger for {0}", repositoryRootString); // NOI18N
                log = IOProvider.getDefault().getIO(repositoryRootString, false);
                try {
                    // HACK (mystic logic) workaround, otherwise it writes to nowhere
                    getLog().getOut().reset();
                } catch (IOException e) {
                    Subversion.LOG.log(Level.SEVERE, null, e);
                }
            } else {
                writable = false;
            }
        }
        if (writable) {
            if (hyperlinkListener != null) {
                try {
                    String prefix = message.substring(0, hyperlinkListener.filePathStartPos);
                    getLog().getOut().write(prefix);
                    String filePath = message.substring(hyperlinkListener.filePathStartPos);
                    getLog().getOut().println(filePath.endsWith("\n") ? filePath.substring(0, filePath.length() - 1) : filePath, hyperlinkListener); //NOI18N
                } catch (IOException e) {
                    getLog().getOut().write(message);
                }
            } else {
                getLog().getOut().write(message);
            }
        }
    }

    /**
     * @return the log
     */
    private InputOutput getLog() {
        writable = true;
        if(log == null) {
            Subversion.LOG.log(Level.FINE, "Creating OutputLogger for {0}", repositoryRootString);
            log = IOProvider.getDefault().getIO(repositoryRootString, false);
            if (!openedWindows.contains(repositoryRootString)) {
                // log window has been opened
                writable = SvnModuleConfig.getDefault().getAutoOpenOutput();
                openedWindows.add(repositoryRootString);
                if (!writable) {
                    // close it again
                    log.closeInputOutput();
                }
            }
        }
        return log;
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
        public void logCommandLine(String commandLine) { }
        @Override
        public void logCompleted(String message) { }
        @Override
        public void logError(String message) { }
        @Override
        public void logMessage(String message) { }
        @Override
        public void logRevision(long revision, String path) { }
        @Override
        public void onNotify(File path, SVNNodeKind kind) { }
        @Override
        public void setCommand(int command) { }
        @Override
        public void closeLog() { }
        @Override
        public void flushLog() { }
    }

    private static class OpenFileOutputListener implements OutputListener {
        private final File f;
        private final int filePathStartPos;

        public OpenFileOutputListener(File f, int filePathStartPos) {
            this.f = f;
            this.filePathStartPos = filePathStartPos;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) { }

        @Override
        public void outputLineAction(OutputEvent ev) {
            Subversion.LOG.log(Level.FINE, "Opeining file [{0}]", f);           // NOI18N
            new OpenInEditorAction(new File[] {f}).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, f.getAbsolutePath()));
        }

        @Override
        public void outputLineCleared(OutputEvent ev) { }

    }

}
