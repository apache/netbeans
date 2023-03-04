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

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.debugger.jpda.ExpressionPool;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * UI-independent method chooser utilities.
 * 
 * @author Martin Entlicher
 */
public final class JPDAMethodChooserUtils {
    
    private JPDAMethodChooserUtils() {}
    
    public static enum MethodEntry {
        SELECTED,
        DIRECT
    }
    
    public static void doStepInto(final JPDADebuggerImpl debugger,
                                  final EditorContext.Operation operation,
                                  final Location location,
                                  final ExpressionPool.Interval expressionLines) {
        final String name = operation.getMethodName();
        final boolean isNative = operation.isNative();
        final String methodClassType = operation.getMethodClassType();
        debugger.getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                RunIntoMethodActionSupport.doAction(debugger, name, methodClassType, isNative,
                                                    location, expressionLines, true,
                                                    MethodEntry.SELECTED);
            }
        });
    }
    
    public static Params init(JPDADebuggerImpl debugger, JPDAThread currentThread,
                              String url, ReleaseUIListener ruil) {
        Params params = new Params();
        return init(debugger, currentThread, url, params, ruil);
    }
    
    public static Params init(JPDADebuggerImpl debugger, JPDAThread currentThread,
                              String url, Params params, ReleaseUIListener ruil) {
        params.operations = new EditorContext.Operation[0];
        int methodLine = currentThread.getLineNumber(null);
        
        Method method = ((JPDAThreadImpl) currentThread).getTopMethod();
        List<Location> locs = java.util.Collections.emptyList();
        try {
            while (methodLine > 0 && (locs = MethodWrapper.locationsOfLine(method, methodLine)).isEmpty()) {
                methodLine--;
            }
        } catch (InternalExceptionWrapper aiex) {
        } catch (VMDisconnectedExceptionWrapper aiex) {
            return params;
        } catch (AbsentInformationException aiex) {
            Exceptions.printStackTrace(Exceptions.attachSeverity(aiex, Level.INFO));
        }
        if (locs.isEmpty()) {
            return params;
        }
        ExpressionPool.Expression expr = debugger.getExpressionPool().getExpressionAt(locs.get(0), url);
        if (expr == null) {
            return params;
        }
        params.expressionLines = expr.getInterval();
        EditorContext.Operation currOp = currentThread.getCurrentOperation();
        List<EditorContext.Operation> lastOpsList = currentThread.getLastOperations();
        EditorContext.Operation lastOp = lastOpsList != null && lastOpsList.size() > 0 ? lastOpsList.get(lastOpsList.size() - 1) : null;
        EditorContext.Operation selectedOp;
        EditorContext.Operation[] tempOps = expr.getOperations();
        if (tempOps.length == 0) {
            return params;
        }
        Location[] tempLocs = expr.getLocations();
        params.operations = new EditorContext.Operation[tempOps.length];
        params.locations = new Location[tempOps.length];
        int l1 = Integer.MAX_VALUE;
        int l2 = 0;
        for (int x = 0; x < tempOps.length; x++) {
            EditorContext.Operation op = tempOps[x];
            params.operations[x] = op;
            params.locations[x] = tempLocs[x];
            int sl = op.getMethodStartPosition().getLine();
            int el = op.getMethodEndPosition().getLine();
            if (sl < l1) {
                l1 = sl;
            }
            if (el > l2) {
                l2 = el;
            }
        }
        params.startLine = l1;
        params.endLine = l2;
        /*
        for (int i = 1; i < (operations.length - 1); i++) {
            int line = operations[i].getMethodStartPosition().getLine();
            if (line < startLine) {
                startLine = line;
            }
            if (line > endLine) {
                endLine = line;
            }
        }*/

        int currOpIndex = -1;
        int lastOpIndex = -1;

        int operationsLength = params.operations.length;
        if (currOp != null) {
            int index = currOp.getBytecodeIndex();
            for (int x = 0; x < operationsLength; x++) {
                if (params.operations[x].getBytecodeIndex() == index) {
                    currOpIndex = x;
                    break;
                }
            }
        }
        if (lastOp != null) {
            int index = lastOp.getBytecodeIndex();
            for (int x = 0; x < operationsLength; x++) {
                if (params.operations[x].getBytecodeIndex() == index) {
                    lastOpIndex = x;
                    break;
                }
            }
        }

        EditorContext.Operation opToExecute = null;
        if (currOpIndex == -1) {
            selectedOp = params.operations[operationsLength - 1];
            opToExecute = params.operations[0];
        } else {
            int splitIndex = currOpIndex == lastOpIndex ? currOpIndex : currOpIndex - 1;
            if (splitIndex + 1 < operationsLength) {
                opToExecute = params.operations[splitIndex + 1];
            }
            tempOps = new EditorContext.Operation[operationsLength - 1 - splitIndex];
            tempLocs = new Location[operationsLength - 1 - splitIndex];
            for (int x = 0; x < tempOps.length; x++) {
                tempOps[x] = params.operations[x + splitIndex + 1];
                tempLocs[x] = params.locations[x + splitIndex + 1];
            }
            params.operations = tempOps;
            params.locations = tempLocs;
            operationsLength = params.operations.length;
            if (operationsLength == 0) {
                return params;
            }
            selectedOp = params.operations[0];
        }

        Object[][] elems = new Object[operationsLength][2];
        for (int i = 0; i < operationsLength; i++) {
            elems[i][0] = params.operations[i];
            elems[i][1] = params.locations[i];
        }
        Arrays.sort(elems, new OperatorsComparator());
        params.isCertainlyReachable = new boolean[operationsLength];
        for (int i = 0; i < operationsLength; i++) {
            params.operations[i] = (EditorContext.Operation)elems[i][0];
            params.locations[i] = (Location)elems[i][1];
            params.isCertainlyReachable[i] = true;
        }
        int[] flags = new int[operationsLength];
        for (int i = 0; i < flags.length; i++) {
            flags[i] = 0;
        }
        detectUnreachableOps(url, params.operations, flags, currOp);
        int count = 0;
        for (int i = 0; i < flags.length; i++) {
            if (flags[i] < 2) {
                count++;
            }
        }
        tempOps = params.operations;
        tempLocs = params.locations;
        params.operations = new EditorContext.Operation[count];
        params.locations = new Location[count];
        params.isCertainlyReachable = new boolean[count];
        operationsLength = count;
        int index = 0;
        int opToExecuteIndex = -1;
        for (int i = 0; i < flags.length; i++) {
            if (flags[i] < 2) {
                params.operations[index] = tempOps[i];
                params.locations[index] = tempLocs[i];
                params.isCertainlyReachable[index] = flags[i] == 0;
                if (opToExecute == params.operations[index]) {
                    opToExecuteIndex = index;
                }
                index++;
            }
        }

        params.selectedIndex = 0;
        for (int i = 0; i < operationsLength; i++) {
            if (params.operations[i].equals(selectedOp) && params.isCertainlyReachable[i]) {
                params.selectedIndex = i;
            }
        }

        if (opToExecuteIndex >= 0 && !params.isCertainlyReachable[opToExecuteIndex]) {
            // perform step over expression and run init() again
            PropertyChangeListener pcl;
            if (params.chooserPropertyChangeListener == null) {
                pcl = new StepOpActionListener(debugger, currentThread, ruil);
                params.chooserPropertyChangeListener = pcl;
            } else {
                pcl = params.chooserPropertyChangeListener;
            }
            synchronized(pcl) {
                StepOperationActionProvider.doAction(debugger, pcl);
                try {
                    pcl.wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return init(debugger, currentThread, url, params, ruil);
        }

        if (operationsLength == 1) {
            // do not show UI, continue directly using the selection
            EditorContext.Operation op = params.operations[params.selectedIndex];
            String name = op.getMethodName();
            if ("<init>".equals(name)) {
                name = op.getMethodClassType();
            }
            RunIntoMethodActionSupport.doAction(debugger, name, op.getMethodClassType(), op.isNative(),
                                                params.locations[params.selectedIndex], expr.getInterval(),
                                                true, MethodEntry.DIRECT);
            params.continuedDirectly = true;
        } else {
            params.continuedDirectly = false;
        }
        return params;
    }

    private static void detectUnreachableOps(String url, final EditorContext.Operation[] operations,
                                             final int[] flags, final EditorContext.Operation currOp) {
        FileObject fileObj = null;
        try {
            fileObj = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
        }
        if (fileObj == null) return;
        JavaSource js = JavaSource.forFileObject(fileObj);
        if (js == null) return;

        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                @Override
                public void cancel() {
                }
                @Override
                public void run(CompilationController ci) throws Exception {
                    if (ci.toPhase(JavaSource.Phase.RESOLVED).compareTo(JavaSource.Phase.RESOLVED) < 0) {
                        Logger.getLogger(JPDAMethodChooserUtils.class.getName()).warning(
                                "Unable to resolve "+ci.getFileObject()+" to phase "+JavaSource.Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    SourcePositions positions = ci.getTrees().getSourcePositions();
                    CompilationUnitTree compUnit = ci.getCompilationUnit();
                    TreeUtilities treeUtils = ci.getTreeUtilities();
                    int pcOffset = currOp == null ? 0 : currOp.getMethodStartPosition().getOffset() + 1;
                    for (int i = 0; i < operations.length; i++) {
                        int offset = operations[i].getMethodStartPosition().getOffset() + 1;
                        TreePath path = treeUtils.pathFor(offset);
                        while (path != null) {
                            Tree tree = path.getLeaf();
                            if (tree instanceof ConditionalExpressionTree) {
                                ConditionalExpressionTree ternaryOpTree = (ConditionalExpressionTree)tree;
                                //Tree condTree = ternaryOpTree.getCondition();
                                Tree trueTree = ternaryOpTree.getTrueExpression();
                                Tree falseTree = ternaryOpTree.getFalseExpression();
                                //long condStart = positions.getStartPosition(compUnit, condTree);
                                //long condEnd = positions.getEndPosition(compUnit, condTree);
                                long trueStart = positions.getStartPosition(compUnit, trueTree);
                                long trueEnd = positions.getEndPosition(compUnit, trueTree);
                                long falseStart = positions.getStartPosition(compUnit, falseTree);
                                long falseEnd = positions.getEndPosition(compUnit, falseTree);

                                if (trueStart <= offset && offset <= trueEnd) {
                                    if (pcOffset < trueStart) {
                                        markSegment(i, false);
                                    }
                                } else if (falseStart <= offset && offset <= falseEnd) {
                                    if (pcOffset < trueStart) {
                                        markSegment(i, false);
                                    } else if (trueStart <= pcOffset && pcOffset <= trueEnd) {
                                        markSegment(i, true);
                                    }
                                }
                            } else if (tree.getKind() == Tree.Kind.CONDITIONAL_AND ||
                                    tree.getKind() == Tree.Kind.CONDITIONAL_OR) {
                                BinaryTree binaryTree = (BinaryTree)tree;
                                Tree rightTree = binaryTree.getRightOperand();
                                long rightStart = positions.getStartPosition(compUnit, rightTree);
                                long rightEnd = positions.getEndPosition(compUnit, rightTree);

                                if (rightStart <= offset && offset <= rightEnd) {
                                    if (pcOffset < rightStart) {
                                        markSegment(i, false);
                                    }
                                }
                            }
                            path = path.getParentPath();
                        } // while
                    } // for
                }

                public void markSegment(int index, boolean excludeSegment) {
                    if (flags[index] == 2) return;
                    flags[index] = excludeSegment ? 2 : 1;
                }

            }, true);
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
        }
    }

    private static final class OperatorsComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            Object[] a1 = (Object[])o1;
            Object[] a2 = (Object[])o2;
            EditorContext.Operation op1 = (EditorContext.Operation)a1[0];
            EditorContext.Operation op2 = (EditorContext.Operation)a2[0];
            return op1.getMethodStartPosition().getOffset() - op2.getMethodStartPosition().getOffset();
        }
        
    }

    public static final class Params {
        
        Params() {}
        
        EditorContext.Operation[] operations;
        ExpressionPool.Interval expressionLines;
        Location[] locations;
        int startLine;
        int endLine;
        private boolean[] isCertainlyReachable;
        private int selectedIndex;
        private boolean continuedDirectly;
        private PropertyChangeListener chooserPropertyChangeListener;
        
        public EditorContext.Operation[] getOperations() {
            return operations;
        }

        public ExpressionPool.Interval getExpressionLines() {
            return expressionLines;
        }

        public Location[] getLocations() {
            return locations;
        }

        public int getStartLine() {
            return startLine;
        }

        public int getEndLine() {
            return endLine;
        }

        public boolean[] getIsCertainlyReachable() {
            return isCertainlyReachable;
        }

        public int getSelectedIndex() {
            return selectedIndex;
        }

        public boolean isContinuedDirectly() {
            return continuedDirectly;
        }

        public PropertyChangeListener getChoosertPropertyChangeListener() {
            return chooserPropertyChangeListener;
        }
    }

    private static class StepOpActionListener implements PropertyChangeListener {
        
        private final JPDADebugger debugger;
        private final JPDAThread currentThread;
        private final ReleaseUIListener ruil;

        public StepOpActionListener(JPDADebugger debugger, JPDAThread currentThread,
                                    ReleaseUIListener ruil) {
            this.debugger = debugger;
            this.currentThread = currentThread;
            this.ruil = ruil;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDAStep.PROP_STATE_EXEC.equals(evt.getPropertyName())) {
                synchronized(this) {
                    notifyAll();
                }
            } else if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED ||
                       currentThread != debugger.getCurrentThread() || !currentThread.isSuspended()) {
                synchronized(this) {
                    notifyAll();
                }
                ruil.releaseUI();
            }
        }
    }
    
    public interface ReleaseUIListener {
        
        void releaseUI();
        
    }
}
