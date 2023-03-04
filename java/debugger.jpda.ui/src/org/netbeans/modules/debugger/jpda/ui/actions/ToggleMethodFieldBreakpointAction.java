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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.modules.debugger.jpda.actions.ActionsSynchronizer;

import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.ClassBreakpointPanel;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.FieldBreakpointPanel;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.MethodBreakpointPanel;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 *
 * @author   Martin Entlicher
 */
public class ToggleMethodFieldBreakpointAction extends AbstractAction {//implements PropertyChangeListener {
    
    private static final String[] BREAKPOINT_ANNOTATION_TYPES = new String[] {
        "Breakpoint_broken",
        "Breakpoint",
        "Breakpoint_stroke",
        "CondBreakpoint_broken",
        "CondBreakpoint",
        "CondBreakpoint_stroke",
        "DisabledBreakpoint",
        "DisabledCondBreakpoint",
        "DisabledBreakpoint_stroke",
        "ClassBreakpoint",
        "DisabledClassBreakpoint",
        "DisabledFieldBreakpoint",
        "DisabledMethodBreakpoint",
        "FieldBreakpoint",
        "MethodBreakpoint",
        "CurrentExpressionLine",
        "CurrentExpression",
    };
    
    private Object action;
    private RequestProcessor postponedToggleRP;

    public ToggleMethodFieldBreakpointAction () {
        //EditorContextBridge.addPropertyChangeListener (this);
        setEnabled (true);
        putValue("default-action", true);
        putValue("supported-annotation-types", BREAKPOINT_ANNOTATION_TYPES);
        //putValue("default-action-excluded-annotation-types", BREAKPOINT_ANNOTATION_TYPES);
    }
    
    public Object getAction () {
        return action;
    }
    
    @Override
    public Object getValue(String key) {
        if (key == Action.NAME) {
            return NbBundle.getMessage (ToggleMethodFieldBreakpointAction.class, "CTL_ToggleMethodFieldBreakpointAction");
        }
        return super.getValue(key);
    }
    
    /*
    public void propertyChange (PropertyChangeEvent evt) {
        String url = EditorContextBridge.getCurrentURL();
        if (url.length() == 0) {
            setEnabled(false);
            return ;
        }
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException muex) {
            fo = null;
        }
        setEnabled (
            (fo != null && "text/x-java".equals(fo.getMIMEType())) && // NOI18N
            (EditorContextBridge.getCurrentFieldName() != null ||
             EditorContextBridge.getCurrentMethodDeclaration() != null)
            //(EditorContextBridge.getCurrentURL ().endsWith (".java"))
        );
        /*
         if ( debugger != null && 
             debugger.getState () == debugger.STATE_DISCONNECTED
        ) 
            destroy ();
         *//*
    }
     */
    
    @Override
    public void actionPerformed (ActionEvent evt) {
        if (!submitFieldOrMethodOrClassBreakpoint()) {
            // Do the toggle BP action directly in this AWT so that it gets the correct current line number.
            DebuggerManager.getDebuggerManager().getActionsManager().postAction(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
        }
    }
    
    private synchronized RequestProcessor getPostponedToggleRP() {
        if (postponedToggleRP == null) {
            postponedToggleRP = new RequestProcessor("Postponed ToggleMethodFieldBreakpointAction", 1);
        }
        return postponedToggleRP;
    }
    
    static JPDADebugger getCurrentDebugger() {
        Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (session != null) {
            return session.lookupFirst(null, JPDADebugger.class);
        } else {
            return null;
        }
    }
    
    private boolean submitFieldOrMethodOrClassBreakpoint() {
        // 1) get class name & element info
        final String[] classDeclaration = new String[] { null };
        java.awt.IllegalComponentStateException cdex;
        final String[] className = new String[] { null };
        java.awt.IllegalComponentStateException cex;
        final String[] fieldName = new String[] { null };
        java.awt.IllegalComponentStateException fex;
        final String methodName;
        final String methodSignature;
        java.awt.IllegalComponentStateException mex;
        try {
            classDeclaration[0] = EditorContextBridge.getCurrentClassDeclaration();
            cdex = null;
        } catch (java.awt.IllegalComponentStateException icsex) {
            cdex = icsex;
        }
        if (classDeclaration[0] == null) {
            try {
                className[0] = EditorContextBridge.getContext().getCurrentClassName();
                cex = null;
            } catch (java.awt.IllegalComponentStateException icsex) {
                cex = icsex;
            }
            try {
                fieldName[0] = EditorContextBridge.getContext().getCurrentFieldName();
                fex = null;
            } catch (java.awt.IllegalComponentStateException icsex) {
                fex = icsex;
            }
            if (fieldName[0] == null || fieldName[0].length() == 0) {
                fieldName[0] = null;
                String[] methodInfo;
                try {
                    methodInfo = EditorContextBridge.getContext().getCurrentMethodDeclaration();
                    mex = null;
                } catch (java.awt.IllegalComponentStateException icsex) {
                    mex = icsex;
                    methodInfo = null;
                }
                if (methodInfo != null) {
                    methodName = methodInfo[0];
                    methodSignature = methodInfo[1];
                    if (methodInfo[2] != null) {
                        className[0] = methodInfo[2];
                    }
                } else if (mex == null) {
                    return false;
                } else {
                    methodName = null;
                    methodSignature = null;
                }
            } else {
                mex = null;
                methodName = null;
                methodSignature = null;
            }
        } else {
            methodName = methodSignature = null;
            cex = fex = mex = null;
        }
        if (cdex != null || cex != null || fex != null || mex != null) {
            final int ln = EditorContextBridge.getContext().getCurrentLineNumber ();
            final String url = EditorContextBridge.getContext().getCurrentURL ();
            final java.awt.IllegalComponentStateException[] exs = new java.awt.IllegalComponentStateException[]
                    { cdex, cex, fex, mex };
            final JPDADebugger debugger = getCurrentDebugger();
            if (debugger != null) {
                ActionsSynchronizer.get(debugger).actionScheduled(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
            }
            getPostponedToggleRP().post(new Runnable() {
                @Override
                public void run() {
                    // Re-try to submit the field or method breakpoint again
                    try {
                        if (debugger != null) {
                            ActionsSynchronizer.get(debugger).actionStarts(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
                        }
                        String cdn = (exs[0] != null) ? exs[0].getMessage() : classDeclaration[0];
                        String cn = (exs[1] != null) ? exs[1].getMessage() : className[0];
                        String fn = (exs[2] != null) ? exs[2].getMessage() : fieldName[0];
                        String mn = (exs[3] != null) ? exs[3].getMessage() : methodName;
                        String ms = (exs[3] != null) ? exs[3].getLocalizedMessage() : methodSignature;
                        if (fn != null && fn.length() == 0) fn = null;
                        if (cdn != null) {
                            cn = cdn;
                            fn = mn = ms = null;
                        } else if (fn == null && mn == null) {
                            return ; // line breakpoint only, which was submitted already.
                        }
                        if (submitFieldOrMethodOrClassBreakpoint(cn, fn, mn, ms, null, ln)) {
                            // We've submitted a field or method breakpoint, so delete the line one:
                            LineBreakpoint lb = ToggleBreakpointActionProvider.findBreakpoint (
                                url, ln
                            );
                            if (lb != null) {
                                DebuggerManager.getDebuggerManager().removeBreakpoint (lb);
                            }
                        }
                    } finally {
                        if (debugger != null) {
                            ActionsSynchronizer.get(debugger).actionEnds(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
                        }
                    }
                }
            });
            return false;
        } else {
            int ln = EditorContextBridge.getContext().getCurrentLineNumber ();
            String url = EditorContextBridge.getContext().getCurrentURL ();
            if (classDeclaration[0] != null) {
                return submitFieldOrMethodOrClassBreakpoint(classDeclaration[0], null,
                                                            null, null,
                                                            url, ln);
            } else {
                return submitFieldOrMethodOrClassBreakpoint(className[0], fieldName[0],
                                                            methodName, methodSignature,
                                                            url, ln);
            }
        }
        
        /*
        // 1) get class name & element info
        String className;
        String fieldName;
        try {
            className = EditorContextBridge.getCurrentClassName();
            fieldName = EditorContextBridge.getCurrentFieldName();
        } catch (java.awt.IllegalComponentStateException icsex) {
            final int ln = EditorContextBridge.getCurrentLineNumber ();
            final String url = EditorContextBridge.getCurrentURL ();
            
            return false;
        }
        String methodName = null;
        String methodSignature = null;
        if (fieldName == null || fieldName.length() == 0) {
            fieldName = null;
            String[] methodInfo = EditorContextBridge.getCurrentMethodDeclaration();
            if (methodInfo != null) {
                methodName = methodInfo[0];
                methodSignature = methodInfo[1];
            } else {
                return false;
            }
        }
        return submitFieldOrMethodBreakpoint(className[0], fieldName[0], methodName, methodSignature);
         */
    }
        
    private boolean submitFieldOrMethodOrClassBreakpoint(final String className, final String fieldName,
                                                         final String methodName, final String methodSignature,
                                                         String url, int line) {
        if (className == null) {
            return false;   // Can not do anything without the class name
        }
        // 2) find and remove existing line breakpoint
        JPDABreakpoint b;
        if (fieldName != null) {
            b = findBreakpoint (className, fieldName);
        } else if (methodName != null) {
            b = findBreakpoint (className, methodName, methodSignature);
        } else {// if (className != null) { - className is not null already
            b = findBreakpoint (className);
        //} else {
        //    return false;
        }
        if (b == null && url != null) {
            b = ToggleBreakpointActionProvider.findBreakpoint(url, line);
        }
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        if (b != null) {
            d.removeBreakpoint (b);
            return true;
        }
        
        // 3) create a new breakpoint
        final JPDADebugger debugger = getCurrentDebugger();
        if (debugger != null) {
            ActionsSynchronizer.get(debugger).actionScheduled(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
        }
        getPostponedToggleRP().post(new Runnable() {
            @Override
            public void run() {
                // Re-try to submit the field or method breakpoint again
                try {
                    if (debugger != null) {
                        ActionsSynchronizer.get(debugger).actionStarts(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
                    }
                    JPDABreakpoint b;
                    if (fieldName != null) {
                        b = FieldBreakpoint.create(className, fieldName, FieldBreakpoint.TYPE_MODIFICATION | FieldBreakpoint.TYPE_ACCESS);
                        b.setPrintText(NbBundle.getMessage(FieldBreakpointPanel.class, "CTL_Field_Breakpoint_Print_Text"));
                    } else if (methodName != null) {
                        b = MethodBreakpoint.create(className, methodName);
                        ((MethodBreakpoint) b).setMethodSignature(methodSignature);
                        b.setPrintText(NbBundle.getMessage(MethodBreakpointPanel.class, "CTL_Method_Breakpoint_Print_Text"));
                    } else {
                        b = ClassLoadUnloadBreakpoint.create(className, false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED_UNLOADED);
                        b.setPrintText (NbBundle.getMessage(ClassBreakpointPanel.class, "CTL_Class_Breakpoint_Print_Text"));
                    }
                    DebuggerManager d = DebuggerManager.getDebuggerManager();
                    d.addBreakpoint(b);
                } finally {
                    if (debugger != null) {
                        ActionsSynchronizer.get(debugger).actionEnds(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
                    }
                }
            }
        });
        return true;
    }
    
    static JPDABreakpoint getCurrentFieldMethodBreakpoint() {
        String className = EditorContextBridge.getContext().getCurrentClassName();
        String fieldName = EditorContextBridge.getContext().getCurrentFieldName();
        String methodName = null;
        String methodSignature = null;
        if (fieldName == null || fieldName.length() == 0) {
            fieldName = null;
            String[] methodInfo = EditorContextBridge.getContext().getCurrentMethodDeclaration();
            if (methodInfo != null) {
                methodName = methodInfo[0];
                methodSignature = methodInfo[1];
            } else {
                return null;
            }
        }
        
        // 2) find and remove existing line breakpoint
        JPDABreakpoint b;
        if (fieldName != null) {
            b = findBreakpoint (className, fieldName);
        } else {
            b = findBreakpoint (className, methodName, methodSignature);
        }
        return b;
    }
    
    private static ClassLoadUnloadBreakpoint findBreakpoint(String className) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof ClassLoadUnloadBreakpoint)) {
                continue;
            }
            ClassLoadUnloadBreakpoint cb = (ClassLoadUnloadBreakpoint) breakpoints[i];
            String[] classFilters = cb.getClassFilters();
            int j;
            for (j = 0; j < classFilters.length; j++) {
                if (match(className, classFilters[j])) {
                    break;
                }
            }
            if (j < classFilters.length) {
                String[] exClassFilters = cb.getClassExclusionFilters();
                for (j = 0; j < exClassFilters.length; j++) {
                    if (match(className, exClassFilters[j])) {
                        j = -1;
                        break;
                    }
                }
                if (j >= 0) {
                    return cb;
                }
            }
        }
        return null;
    }

    private static FieldBreakpoint findBreakpoint(String className, String fieldName) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof FieldBreakpoint)) {
                continue;
            }
            FieldBreakpoint fb = (FieldBreakpoint) breakpoints[i];
            if (!fb.getClassName().equals(className)) continue;
            if (!fb.getFieldName().equals(fieldName)) continue;
            return fb;
        }
        return null;
    }

    private static MethodBreakpoint findBreakpoint(String className, String methodName, String methodSignature) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof MethodBreakpoint)) {
                continue;
            }
            MethodBreakpoint mb = (MethodBreakpoint) breakpoints[i];
            String[] classFilters = mb.getClassFilters();
            int j;
            for (j = 0; j < classFilters.length; j++) {
                if (match(className, classFilters[j])) {
                    break;
                }
            }
            if (j < classFilters.length) {
                if (!mb.getMethodName().equals(methodName)) continue;
                String signature = mb.getMethodSignature();
                if (signature == null || egualMethodSignatures(signature, methodSignature)) {
                    return mb;
                }
            }
        }
        return null;
    }
    
    // Compares whether the two signatures have the same arguments. We ignore return value.
    private static boolean egualMethodSignatures(String s1, String s2) {
        int i = s1.lastIndexOf(")");
        if (i > 0) s1 = s1.substring(0, i);
        i = s2.lastIndexOf(")");
        if (i > 0) s2 = s2.substring(0, i);
        return s1.equals(s2);
    }
    
    private static boolean match (String name, String pattern) {
        if (pattern.startsWith ("*"))
            return name.endsWith (pattern.substring (1));
        else
        if (pattern.endsWith ("*"))
            return name.startsWith (
                pattern.substring (0, pattern.length () - 1)
            );
        return name.equals (pattern);
    }
    
}
