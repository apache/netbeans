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

package org.netbeans.modules.ant.debugger;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;


public class IOManager {

    // variables ...............................................................
    
    protected InputOutput                   debuggerIO = null;
    private OutputWriter                    debuggerOut;
    private OutputWriter                    debuggerErr;
    private String                          name;
    private boolean                         closed = false;

    
    /** output writer Thread */
    private Hashtable<String, Object> lines = new Hashtable<>();
    private Listener                        listener = new Listener ();

    
    // init ....................................................................
    
    public IOManager (
        String title
    ) {
        debuggerIO = IOProvider.getDefault ().getIO (title, true);
        debuggerIO.setFocusTaken (false);
        debuggerOut = debuggerIO.getOut ();
        debuggerErr = debuggerIO.getErr ();
    }
    
    
    // public interface ........................................................

    private final LinkedList<Text> buffer = new LinkedList<>();
    private RequestProcessor.Task task;
    
    /**
    * Prints given text to the output.
    */
    public void println (
        final String text, 
        final Object line
    ) {
        println(text, line, false);
    }

    /**
    * Prints given text to the output.
    */
    public void println (
        final String text, 
        final Object line,
        final boolean important
    ) {
        if (text == null)
            throw new NullPointerException ();
        synchronized (buffer) {
            buffer.addLast (new Text (text, line, important));
        }
        if (task == null)
            task = RequestProcessor.getDefault ().post (new Runnable () {
                @Override
                public void run () {
                    synchronized (buffer) {
                        int i, k = buffer.size ();
                        for (i = 0; i < k; i++) {
                            Text t = buffer.removeFirst();
                            try {
                                OutputWriter ow = (t.important) ? debuggerErr : debuggerOut;
                                if (t.line != null) {
                                    ow.println (t.text, listener, t.important);
                                    lines.put (t.text, t.line);
                                } else
                                    ow.println (t.text, null, t.important);
                                ow.flush ();
                                if (t.important) {
                                    debuggerIO.select();
                                }
                                if (closed)
                                    debuggerOut.close ();
                                StatusDisplayer.getDefault ().setStatusText (t.text);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }, 100, Thread.MIN_PRIORITY);
        else 
            task.schedule (100);
    }

    void closeStream () {
        debuggerOut.close ();
        closed = true;
        close();
    }

    void close () {
        debuggerIO.closeInputOutput ();
    }
    
    
    // innerclasses ............................................................
    
    private class Listener implements OutputListener {

        @Override
        public void outputLineAction (final OutputEvent ev) {
            SwingUtilities.invokeLater (new Runnable () {
                @Override
                public void run () {
                    String t = ev.getLine ();
                    Object a = lines.get (t);
                    if (a == null) return;
                    Utils.showLine (a);
                }
            });
        }
        @Override
        public void outputLineCleared (OutputEvent ev) {
            lines = new Hashtable ();
        }
    }
    
    private static class Text {
        private final String text;
        private final Object line;
        private final boolean important;
        
        private Text (String text, Object line, boolean important) {
            this.text = text;
            this.line = line;
            this.important = important;
        }
    }
}
