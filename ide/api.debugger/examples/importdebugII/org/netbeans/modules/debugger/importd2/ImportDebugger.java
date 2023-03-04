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

package org.netbeans.modules.debugger.importd2;

import java.util.*;
import javax.swing.text.StyledDocument;

import org.openide.TopManager;
import org.openide.cookies.EditorCookie;
import org.openide.debugger.*;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.*;
import org.netbeans.modules.debugger.support.DebuggerAnnotation;
import org.netbeans.modules.debugger.support.SecondaryDebuggerSupport;
import org.netbeans.modules.debugger.support.StateSupport;
import org.netbeans.modules.debugger.support.util.Utils;
import org.netbeans.modules.debugger.support.util.ValidatorImpl;

/**
 * Example of Debugger Implementation. It "debugs" import statements
 * in Java files (so it does not need compiled .class files). Step
 * Into on "import java.awt.Button" opens .java file for Button and
 * puts current line to the first line of it.
 *
 * @author Jan Jancura
 */
public class ImportDebugger extends SecondaryDebuggerSupport {

    // static ..................................................................
    
    /** generated Serialized Version UID */
    static final long                       serialVersionUID = 84466455228078708L;
    /** bundle to obtain text information from */
    private static ResourceBundle                      bundle;

    static String getLocString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (ImportDebugger.class);
        return bundle.getString (s);
    }

    /** debugger State constant. */
    public static final State                 STATE_STOPPED = 
        new StateSupport (
            StateSupport.PAUSE |
            StateSupport.STEP_OVER |
            StateSupport.STEP_INTO |
            StateSupport.STEP_OUT
        );
    /** debugger State constant. */
    public static final State                 STATE_RUNNING = 
        new StateSupport (
            StateSupport.PAUSE
        );
    
    static DebuggerAnnotation.CurrentPC currentLineAnnotation =
        new DebuggerAnnotation.CurrentPC ();

    
    // variables ...............................................................

    private transient ValidatorImpl validator;


    // init ....................................................................

    public ImportDebugger () {
        validator = new ValidatorImpl ();
    }


    // Debugger implementation .................................................

    /** Starts the debugger. The method stops the current debugging (if any)
    * and takes information from the provided info (containing the class to start and
    * arguments to pass it and name of class to stop debugging in) and starts
    * new debugging session.
    *
    * @param info debugger info about class to start
    * @exception DebuggerException if an error occures during the start of the debugger
    */
    public void startDebugger (DebuggerInfo info) throws DebuggerException {
        boolean stopOnMain = info.getStopClassName () != null;

        super.startDebugger (info);
        setState (Debugger.DEBUGGER_RUNNING);
        String stopClassName = info.getClassName ();
        Line l = Utils.getLine (stopClassName, 1);
        getStack (info).push (l);
        getIsOnStack ().add (l.getDataObject ());

        // stop on main
        stopOnMain = true;
        if (stopOnMain) {
            setState (DEBUGGER_STOPPED);
            setCurrentLine (l);
            refreshWatches ();
        } else
            go ();
    }

    /**
    * Finishes debugger.
    */
    public void finishDebugger () throws DebuggerException {
        super.finishDebugger ();
    }

    /**
    * Trace into.
    */
    public synchronized void traceInto () throws DebuggerException {
        setState (DEBUGGER_RUNNING);
        Line l = step ();
        if (l == null) {
            finishDebugger ();
            refreshWatches ();
            return;
        }
        setCurrentLine (l);
        setState (DEBUGGER_STOPPED);
        setLastAction (ACTION_TRACE_INTO);
        refreshWatches ();
    }

    /**
    * Trace over.
    */
    public synchronized void traceOver () throws DebuggerException {
        setState (DEBUGGER_RUNNING);
        Stack stack = getStack ();
        int d = stack.size ();
        Line l = null;
        do {
            l = step ();
            if (l == null) {
                finishDebugger ();
                refreshWatches ();
                return;
            }
        } while (stack.size () > d);
        setCurrentLine (l);
        setState (DEBUGGER_STOPPED);
        setLastAction (ACTION_TRACE_OVER);
        refreshWatches ();
    }

    /**
    * Go.
    */
    public synchronized void go () throws DebuggerException {
        setLastAction (ACTION_CONTINUE);
        setState (DEBUGGER_RUNNING);
        
        Line l = null;
        do {
            l = step ();
        } while (l != null);
        finishDebugger ();
        refreshWatches ();
    }

    /**
    * Step out.
    */
    public synchronized void stepOut () throws DebuggerException {
        setLastAction (ACTION_STEP_OUT);
        setState (DEBUGGER_RUNNING);
        
        Stack stack = getStack ();
        int d = stack.size () - 1;
        Line l = null;
        do {
            l = step ();
            if (l == null) {
                finishDebugger ();
                refreshWatches ();
                return;
            }
        } while (stack.size () > d);
        setCurrentLine (l);
        setState (DEBUGGER_STOPPED);
        setLastAction (ACTION_TRACE_INTO);
        refreshWatches ();
    }

    /**
     * Jumps to the next call site (towards the top of the call-stack).
     */
    public void goToCalledMethod () {
    }
    
    /**
     * Jumps to the previous call site (towards the bottom of the call-stack).
     */
    public void goToCallingMethod () {
    }

    /**
     * Jumps to a given line.
     *
     * @param l a line jump to
     */
    public void runToCursor (Line l) {
    }

    /**
     * Pauses debugging.
     */
    public void pause () {
    }

    public void setCurrentLine (Line l) {
        Line old = getCurrentLine ();
        if (old != null) {
           // old.unmarkCurrentLine ();
           currentLineAnnotation.detachLine ();
        }
        if (l != null) {
            Utils.showInEditor (l);
            //l.markCurrentLine ();
            currentLineAnnotation.attachLine (l);
        }
        super.setCurrentLine (l);
    }

    protected void setState (int state) {
        super.setState (state);
        switch (state) {
            case DEBUGGER_NOT_RUNNING:
                setDebuggerState (STATE_NOT_RUNNING);
                break;
            case DEBUGGER_RUNNING:
                setDebuggerState (STATE_RUNNING);
                break;
            case DEBUGGER_STOPPED:
                setDebuggerState (STATE_STOPPED);
                break;
        }
    }
    
    /** Creates new uninitialized watch. The watch is visible (not hidden).
    *
    * @return new uninitialized watch
    */
    public Watch createWatch () {
        ImportWatch w = new ImportWatch (this, false);
        addWatch (w);
        return w;
    }

    /** Creates a watch its expression is set to initial value. Also
    * allows to create a watch not presented to the user, for example
    * for internal usage in the editor to obtain values of variables
    * under the mouse pointer.
    *
    * @param expr expresion to watch for
    * @param hidden allows to specify whether the watch should be presented
    *   to the user or not (be only of internal usage of the IDE).
    * @return new watch
    */
    public Watch createWatch (String expr, boolean hidden) {
        ImportWatch w = new ImportWatch (this, hidden);
        w.setVariableName (expr);
        addWatch (w);
        return w;
    }
    
    
    // inner debugger implementation ...........................................
    
    Validator getValidator () {
        return validator;
    }
    
    void refreshWatches () {
        validator.validate ();
    }
    
    
    // inner debugger implementation ...........................................
    
    //private HashSet isOnStack = new HashSet ();
    
    private HashMap diToStack = new HashMap ();
    private HashMap diToIsOnStack = new HashMap ();
    private CoreDebugger coreDebugger;
    
    Stack getStack (DebuggerInfo debuggerInfo) {
        Stack s = (Stack) diToStack.get (debuggerInfo);
        if (s == null) {
            s = new Stack ();
            diToStack.put (debuggerInfo, s);
        }
        return s;
    }
    
    HashSet getIsOnStack (DebuggerInfo debuggerInfo) {
        HashSet s = (HashSet) diToIsOnStack.get (debuggerInfo);
        if (s == null) {
            s = new HashSet ();
            diToIsOnStack.put (debuggerInfo, s);
        }
        return s;
    }
    
    CoreDebugger getCoreDebugger () {
        if (coreDebugger == null) 
            try {
                coreDebugger = (CoreDebugger) TopManager.getDefault ().
                    getDebugger ();
            } catch (DebuggerException e) {
            }
        return coreDebugger;
    }
    
    Stack getStack () {
        return getStack (
            getInfo ()
        );
    }
    
    HashSet getIsOnStack () {
        return getIsOnStack (
            getInfo ()
        );
    }
    
    DebuggerInfo getInfo () {
        DebuggerInfo i = null;
        if (getCoreDebugger ().getCurrentDebugger () != null)
            i = getCoreDebugger ().getCurrentDebugger ().getDebuggerInfo ();
        if (i == null) i = getDebuggerInfo ();
        Thread.dumpStack();
        return i;
    }
    
    Line step () {
        Stack stack = getStack ();
        HashSet isOnStack = getIsOnStack ();
        Line l = (Line) stack.lastElement ();
        try {
            // Trace into
            String str = getText (l);
            if (str.startsWith ("import ")) {
                str = str.substring (7, str.length () - 1); //.replace ('.', File.separatorChar);
                Line ll = Utils.getLine (str, 1);
                if (ll != null)
                    if (!isOnStack.contains (ll.getDataObject ())) {
                        stack.push (ll);
                        isOnStack.add (ll.getDataObject ());
                        return ll;
                    }
            }
        } catch (Exception e) {
        }
            
        stack.pop ();

        // trace over
        try {
            if (l.getLineNumber () < 50) {
                Line ll = Utils.getLine (
                    l.getDataObject ().getPrimaryFile ().getPackageName ('.'), 
                    l.getLineNumber () + 2
                );
                if (ll != null) {
                    stack.push (ll);
                    return ll;
                }
            }
        } catch (Exception e) {
        }

        // Step out
        if (stack.empty ()) return null;
        Line ll = (Line) stack.pop ();
        if (ll.getLineNumber () < 50)
            try {
                ll = Utils.getLine (ll.getDataObject ().getPrimaryFile ().
                    getPackageName ('.'), ll.getLineNumber () + 2
                );
            } catch (Exception e) {
            }
        stack.push (ll);
        return ll;
    }
    
    static String getText (Line l) throws Exception {
        EditorCookie ec = (EditorCookie) l.getDataObject ().
            getCookie (EditorCookie.class);
        StyledDocument doc = ec.openDocument ();
        if (doc == null) return "";
        int off = NbDocument.findLineOffset (doc, l.getLineNumber ());
        int len = NbDocument.findLineOffset (doc, l.getLineNumber () + 1) - 
            off - 1;
        return doc.getText (off, len);
    }
}
