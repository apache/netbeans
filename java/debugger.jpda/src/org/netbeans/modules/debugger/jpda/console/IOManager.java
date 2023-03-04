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

package org.netbeans.modules.debugger.jpda.console;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.io.Hyperlink;
import org.netbeans.api.io.IOProvider;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.OutputWriter;
import org.netbeans.modules.debugger.jpda.DebuggerConsoleIO.Line;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.openide.util.RequestProcessor;

public class IOManager {

//    /** DebuggerManager output constant. */
//    public static final int                 DEBUGGER_OUT = 1;
//    /** Process output constant. */
//    public static final int                 PROCESS_OUT = 2;
//    /** Status line output constant. */
//    public static final int                 STATUS_OUT = 4;
//    /** All outputs constant. */
//    public static final int                 ALL_OUT = DEBUGGER_OUT + 
//                                                PROCESS_OUT + STATUS_OUT;
//    /** Standart process output constant. */
//    public static final int                 STD_OUT = 1;
//    /** Error process output constant. */
//    public static final int                 ERR_OUT = 2;

    
    // variables ...............................................................

    private final Reference<JPDADebuggerImpl> debuggerRef;
    private final String                    title;
    private InputOutput                     debuggerIO = null;
    private OutputWriter                    debuggerOut;
    private OutputWriter                    debuggerErr;
    private boolean                         closed = false;
    private boolean                         streamsClosed = false;
    
    
    // init ....................................................................
    
    public IOManager(JPDADebuggerImpl debugger, String title) {
        this.debuggerRef = new WeakReference<>(debugger);
        this.title = title;
        init();
        //debuggerIO.select();
    }

    private boolean init() {
        if (openDebuggerConsole()) {
            debuggerIO = IOProvider.getDefault ().getIO (title, true);
            //debuggerIO.setFocusTaken (false);
            //debuggerIO.setErrSeparated(false);
            debuggerOut = debuggerIO.getOut ();
            debuggerErr = debuggerIO.getErr();
            return true;
        } else {
            return false;
        }
    }

    private static boolean openDebuggerConsole() {
        Properties p = Properties.getDefault().getProperties("debugger.options.JPDA");
        return p.getBoolean("OpenDebuggerConsole", true);
    }
    
    
    // public interface ........................................................

    private final LinkedList<Text> buffer = new LinkedList<Text>();
    private RequestProcessor.Task task;
    
    /**
     * Prints given text to the output.
     */
    public void println (
        String text, 
        Line line
    ) {
        println(text, line, false);
    }
    
    /**
     * Prints given text to the output.
     */
    public void println (
        String text, 
        Line line,
        boolean important
    ) {
        if (text == null)
            throw new NullPointerException ();
        if (!openDebuggerConsole()) {
            return ;
        } else {
            synchronized (this) {
                if (debuggerIO == null) {
                    init();
                }
            }
        }
        synchronized (buffer) {
            buffer.addLast (new Text (text, line, important));
            if (task == null) {
                task = new RequestProcessor("Debugger Output", 1).post (new Runnable () {
                    public void run () {
                        List<Text> output;
                        synchronized (buffer) {
                            output = new ArrayList<Text>(buffer);
                            buffer.clear();
                        }
                        if (streamsClosed) {
                            return ;
                        }
                        JPDADebuggerImpl debugger = debuggerRef.get();
                        int i, k = output.size ();
                        for (i = 0; i < k; i++) {
                            Text t = output.get(i);
                            if (t.important) {
                                if (t.line != null) {
                                    Hyperlink hl = Hyperlink.from(new HyperlinkRunnable(t.line), t.important);
                                    debuggerErr.println(t.text, hl);
                                } else {
                                    debuggerErr.println(t.text);
                                }
                                debuggerIO.show();
                                debuggerErr.flush();
                            } else {
                                if (t.line != null) {
                                    Hyperlink hl = Hyperlink.from(new HyperlinkRunnable(t.line), t.important);
                                    debuggerOut.println(t.text, hl);
                                } else {
                                    debuggerOut.println(t.text);
                                }
                                debuggerOut.flush();
                            }
                            if (debugger != null) {
                                debugger.actionStatusDisplayCallback(null, t.text);
                            }
                            if (closed) {
                                debuggerOut.close ();
                                debuggerErr.close();
                                streamsClosed = true;
                            }
                        }
                    }
                }, 50, Thread.MIN_PRIORITY);
            } else {
                if (buffer.size() > 25) {
                    task.run();
                } else {
                    task.schedule (50);
                }
            }
        }
    }
    
    public InputOutput getIO() {
        return debuggerIO;
    }

    void closeStream () {
        synchronized (buffer) {
            closed = true;
            if (task != null) {
                task.schedule(50);
            }
        }
    }

    void close () {
        if (debuggerIO != null) {
            debuggerIO.close();
        }
    }
    
    
    // innerclasses ............................................................
    
    private static final class HyperlinkRunnable implements Runnable {
        
        private final Line line;
        
        public HyperlinkRunnable(Line line) {
            this.line = line;
        }

        @Override
        public void run() {
            line.show ();
        }
    }
    
    private static class Text {
        private String text;
        private Line line;
        private boolean important;
        
        private Text (String text, Line line, boolean important) {
            this.text = text;
            this.line = line;
            this.important = important;
        }
    }
    
}
