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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Future;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.debugger.jpda.actions.ActionsSynchronizer;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.JavaUtils;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.jpda.EditorContext;

import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/** 
 *
 * @author   Jan Jancura
 */
@ActionsProvider.Registrations({
    @ActionsProvider.Registration(path="",                     actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-java" }),
    @ActionsProvider.Registration(path="netbeans-JPDASession", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-java" })
})
public class ToggleBreakpointActionProvider extends ActionsProviderSupport 
implements PropertyChangeListener {
    
    private RequestProcessor RP = new RequestProcessor(ToggleBreakpointActionProvider.class.getName());
    private JPDADebugger debugger;
    private volatile int postedLineNumber = -1;
    private volatile String postedUrl = null;
    
    public ToggleBreakpointActionProvider () {
        EditorContextBridge.getContext().addPropertyChangeListener (this);
    }
    
    public ToggleBreakpointActionProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener (JPDADebugger.PROP_STATE, this);
        EditorContextBridge.getContext().addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (JPDADebugger.PROP_STATE, this);
        EditorContextBridge.getContext().removePropertyChangeListener (this);
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        String url = EditorContextBridge.getContext().getCurrentURL();
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException muex) {
            fo = null;
        }
        setEnabled (
            ActionsManager.ACTION_TOGGLE_BREAKPOINT,
            (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
            (fo != null && "text/x-java".equals(fo.getMIMEType()))  // NOI18N
            //(EditorContextBridge.getCurrentURL ().endsWith (".java"))
        );
        if ( debugger != null && 
             debugger.getState () == JPDADebugger.STATE_DISCONNECTED
        ) 
            destroy ();
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
    @Override
    public void doAction (Object action) {
        JPDADebugger dbg = debugger;
        if (dbg == null) {
            dbg = ToggleMethodFieldBreakpointAction.getCurrentDebugger();
        }
        ActionsSynchronizer as = (dbg != null) ? ActionsSynchronizer.get(dbg) : null;
        if (as != null) {
            as.actionStarts(action);
        }
        try {
            doTheAction(action);
        } finally {
            if (as != null) {
                as.actionEnds(action);
            }
        }
    }
    
    private void doTheAction (Object action) {
        DebuggerManager d = DebuggerManager.getDebuggerManager ();
        
        // 1) get source name & line number
        int lineNumber;
        String url;
        url = postedUrl;
        if (url == null) {
            url = EditorContextBridge.getContext().getCurrentURL ();
        }
        lineNumber = postedLineNumber;
        if (lineNumber == -1) {
            lineNumber = EditorContextBridge.getContext().getCurrentLineNumber ();
        }
        if ("".equals (url.trim ())) return;

        // 2) find and remove existing line breakpoint
        LineBreakpoint lb = findBreakpoint(url, lineNumber);
        if (lb != null) {
            d.removeBreakpoint (lb);
            return;
        }
//        Breakpoint[] bs = d.getBreakpoints ();
//        int i, k = bs.length;
//        for (i = 0; i < k; i++) {
//            if (!(bs [i] instanceof LineBreakpoint)) continue;
//            LineBreakpoint lb = (LineBreakpoint) bs [i];
//            if (ln != lb.getLineNumber ()) continue;
//            if (!url.equals (lb.getURL ())) continue;
//            d.removeBreakpoint (lb);
//            return;
//        }

        // 3) check if a line breakpoint could be addded at the selected location
        //    if not, try to adjust position or cancel the action
        JEditorPane[] editorPane = new JEditorPane[1];
        int adjustedLineNumber = checkLineBreakability(url, lineNumber, editorPane);
        if (adjustedLineNumber != lineNumber) {
            if (adjustedLineNumber == -1 || findBreakpoint(url, adjustedLineNumber) != null) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                if (editorPane[0] != null) {
                    Utilities.setStatusText(editorPane[0], ""); // workaroud, no status text is displayed when the same text has been already set before
                    String msg = NbBundle.getMessage(ToggleBreakpointActionProvider.class, "CTL_Cannot_Toggle_Breakpoint");
                    Utilities.setStatusText(editorPane[0], msg);
                }
                return;
            } else {
                if (editorPane[0] != null) {
                    Utilities.setStatusText(editorPane[0], ""); // workaroud, no status text is displayed when the same text has been already set before
                    String msg = NbBundle.getMessage(ToggleBreakpointActionProvider.class, "CTL_Breakpoint_Position_Adjusted");
                    Utilities.setStatusText(editorPane[0], msg);
                }
                lineNumber = adjustedLineNumber;                
            }
        }

        // 4) create a new line breakpoint
        lb = LineBreakpoint.create (
            url,
            lineNumber
        );
        lb.setPrintText (
            NbBundle.getMessage 
                (ToggleBreakpointActionProvider.class, "CTL_Line_Breakpoint_Print_Text")
        );
        d.addBreakpoint (lb);
    }

    @Override
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        EditorContext context = EditorContextBridge.getContext();
        postedLineNumber = context.getCurrentLineNumber ();
        postedUrl = context.getCurrentURL ();
        JPDADebugger dbg = debugger;
        if (dbg == null) {
            dbg = ToggleMethodFieldBreakpointAction.getCurrentDebugger();
        }
        if (dbg != null) {
            ActionsSynchronizer.get(dbg).actionScheduled(action);
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    doAction(action);
                } finally {
                    postedLineNumber = -1;
                    postedUrl = null;
                    actionPerformedNotifier.run();
                }
            }
        });
    }

    private int checkLineBreakability(String url, final int lineNumber, final JEditorPane[] editorPane) {
        FileObject fileObj = null;
        try {
            fileObj = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
        }
        if (fileObj == null) return lineNumber;
        DataObject dobj = null;
        try {
            dobj = DataObject.find(fileObj);
        } catch (DataObjectNotFoundException ex) {
        }
        if (dobj == null) return lineNumber;
        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        if (ec == null) return lineNumber;
        final BaseDocument doc = (BaseDocument)ec.getDocument();
        if (doc == null) return lineNumber;
        final int rowStartOffset = Utilities.getRowStartFromLineOffset(doc, lineNumber - 1);
        final int rowEndOffset;
        try {
            rowEndOffset = Utilities.getRowEnd(doc, rowStartOffset);
        } catch (BadLocationException ex) {
            return lineNumber;
        }
        JavaSource js = JavaSource.forFileObject(fileObj);
        if (js == null) return lineNumber;

        if (SwingUtilities.isEventDispatchThread()) {
            JEditorPane[] openedPanes = ec.getOpenedPanes();
            if (openedPanes != null && openedPanes.length > 0) {
                editorPane[0] = openedPanes[0];
            }
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        JEditorPane[] openedPanes = ec.getOpenedPanes();
                        if (openedPanes != null && openedPanes.length > 0) {
                            editorPane[0] = openedPanes[0];
                        }
                    }
                });
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        final int[] result = new int[] {lineNumber};
        final Future<Void> scanFinished;
        try {
            scanFinished = JavaUtils.runWhenScanFinishedReallyLazy(js, new CancellableTask<CompilationController>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(CompilationController ci) throws Exception {
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    SourcePositions positions = ci.getTrees().getSourcePositions();
                    CompilationUnitTree compUnit = ci.getCompilationUnit();
                    TreeUtilities treeUtils = ci.getTreeUtilities();

                    Tree outerTree = null;
                    Tree execTree = null;
                    TreePath outerTreePath = null;
                    TreePath execTreePath = null;
                    Tree lastTree = null;
                    long execTreeStartOffs = 0, execTreeEndOffs = 0;
                    for (int index = rowStartOffset; index <= rowEndOffset; index++) {
                        TreePath path = treeUtils.pathFor(index);
                        Tree tree = path.getLeaf();
                        if (tree.equals(lastTree)) continue;

                        long startOffs = positions.getStartPosition(compUnit, tree);
                        long endOffs = positions.getEndPosition(compUnit, tree);
                        if (outerTree == null && startOffs < rowStartOffset) {
                            outerTree = tree;
                            outerTreePath = path;
                            lastTree = tree;
                            continue;
                        }

                        if (startOffs >= rowStartOffset) {
                            if (execTree == null ||
                                    (execTreeStartOffs >= startOffs && execTreeEndOffs <= endOffs)) {
                                execTree = tree;
                                execTreePath = path;
                                execTreeStartOffs = startOffs;
                                execTreeEndOffs = endOffs;
                                    if (execTree instanceof VariableTree && isBreakable(execTreePath)) {
                                    break;
                                }
                            } else if (startOffs > execTreeEndOffs) {
                                if (isBreakable(execTreePath)) {
                                    break;
                                } else {
                                    execTree = tree;
                                    execTreePath = path;
                                    execTreeStartOffs = startOffs;
                                    execTreeEndOffs = endOffs;
                                    if (execTree instanceof VariableTree && isBreakable(execTreePath)) {
                                        break;
                                    }
                                }
                            }
                        } // if
                        lastTree = tree;
                    }

                    if (execTree == null || !isBreakable(execTreePath)) {
                        if (outerTree != null && isBreakable(outerTreePath)) {
                            long offs = positions.getStartPosition(compUnit, outerTree);
                            result[0] = Utilities.getLineOffset(doc, (int)offs) + 1;
                        } else {
                            if (outerTree instanceof BlockTree) {
                                Tree pTree = outerTreePath.getParentPath().getLeaf();
                                if (pTree instanceof MethodTree) {
                                    long endOffs = positions.getEndPosition(compUnit, pTree);
                                    if (endOffs <= rowEndOffset) {
                                        return; // i.e. result[0] is original lineNumber - allow toggle breakpoint at method end
                                    }
                                }
                            }
                            result[0] = -1; // do not allow to add a breakpoint
                        }
                    }
                }
            }, true);
            if (!scanFinished.isDone()) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    return lineNumber;
                } else {
                    try {
                        scanFinished.get();
                    } catch (InterruptedException iex) {
                        return lineNumber;
                    } catch (java.util.concurrent.ExecutionException eex) {
                        ErrorManager.getDefault().notify(eex);
                        return lineNumber;
                    }
                }
            }
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
            return lineNumber;
        }
        return result[0];
    }

    private static boolean isBreakable(TreePath path) {
        Tree tree = path.getLeaf();
        switch (tree.getKind()) {
            case BLOCK:
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
            case COMPILATION_UNIT:
            case IMPORT:
            case MODIFIERS:
            case EMPTY_STATEMENT:
                return false;
        }
        while (path != null) {
            tree = path.getLeaf();
            Tree.Kind kind = tree.getKind();
            if (kind == Tree.Kind.IMPORT) return false;
            if (kind == Tree.Kind.VARIABLE) {
                VariableTree varTree = (VariableTree)tree;
                if (varTree.getInitializer() == null) {
                    return false;
                }
            }
            path = path.getParentPath();
        }
        return true;
    }

    static LineBreakpoint findBreakpoint (String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof LineBreakpoint)) {
                continue;
            }
            LineBreakpoint lb = (LineBreakpoint) breakpoints[i];
            if (!lb.getURL ().equals (url)) continue;
            if (lb.getLineNumber() == lineNumber) {
                return lb;
            }
        }
        return null;
    }
    
}
