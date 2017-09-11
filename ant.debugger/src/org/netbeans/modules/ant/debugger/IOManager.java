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
    private Hashtable                       lines = new Hashtable ();
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

    private final LinkedList buffer = new LinkedList ();
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
                            Text t = (Text) buffer.removeFirst ();
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
        public void outputLineSelected (OutputEvent ev) {
        }
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
