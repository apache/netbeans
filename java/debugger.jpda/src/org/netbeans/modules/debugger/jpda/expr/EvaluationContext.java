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

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Logger;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.Exceptions;

/**
 * Defines the execution context in which to evaluate a given expression. The context consists of:
 * the current stack frame and the source file in which the expression would exist. The source file
 * is needed for the import facility to work.
 *
 * @author Maros Sandor
 */
public class EvaluationContext {
    
    private static final Logger logger = Logger.getLogger(EvaluationContext.class.getName());

    /**
     * The runtime context of a JVM is represented by a stack frame.
     */
    private StackFrame frame;
    private final int frameDepth;
    private final JPDAThreadImpl thread;
    private final ObjectReference contextVariable;
    private final List<String> sourceImports;
    private final List<String> staticImports;
    private boolean canInvokeMethods;
    private final Runnable methodInvokePreproc;
    private final JPDADebuggerImpl debugger;
    private final VMCache vmCache;

    private Trees trees;
    private TreePath treePath;

    private Map<Tree, VariableInfo> variables = new HashMap<Tree, VariableInfo>();
    private Stack<Map<String, ScriptVariable>> stack = new Stack<Map<String, ScriptVariable>>();
    private Map<String, ScriptVariable> scriptLocalVariables = new HashMap<String, ScriptVariable>();
    private final List<ObjectReference> disabledCollectionObjects = new ArrayList<ObjectReference>();
    private PropertyChangeListener threadPropertyChangeListener = null;

    /**
     * Creates a new context in which to evaluate expressions.
     *
     * @param frame the frame in which context evaluation occurs
     * @param imports list of imports
     * @param staticImports list of static imports
     */
    public EvaluationContext(JPDAThreadImpl thread, StackFrame frame, int frameDepth,
                             ObjectReference contextVariable,
                             List<String> imports, List<String> staticImports,
                             boolean canInvokeMethods, Runnable methodInvokePreproc,
                             JPDADebuggerImpl debugger, VMCache vmCache) {
        if (thread == null) throw new IllegalArgumentException("Thread argument must not be null");
        if (frame == null) throw new IllegalArgumentException("Frame argument must not be null");
        if (imports == null) throw new IllegalArgumentException("Imports argument must not be null");
        if (staticImports == null) throw new IllegalArgumentException("Static imports argument must not be null");
        this.thread = thread;
        this.frame = frame;
        this.frameDepth = frameDepth;
        this.contextVariable = contextVariable;
        this.sourceImports = imports;
        this.staticImports = staticImports;
        this.canInvokeMethods = canInvokeMethods;
        this.methodInvokePreproc = methodInvokePreproc;
        this.debugger = debugger;
        this.vmCache = vmCache;

        stack.push(new HashMap<String, ScriptVariable>());
    }

    public List<String> getStaticImports() {
        return staticImports;
    }

    public List<String> getImports() {
        return sourceImports;
    }

    public StackFrame getFrame() {
        try {
            frame.thread();
        } catch (InvalidStackFrameException isex) {
            // Update the stack frame
            try {
                // Refresh the stack frame
                logger.config("Updating an invalid stack frame on "+thread.getName()+" and depth "+frameDepth);
                frame = ThreadReferenceWrapper.frame(thread.getThreadReference(), frameDepth);
                logger.config("... stack frame updated.");
            } catch (Exception ex) {}
        } catch (Exception ex) {}
        return frame;
    }
    
    JPDAThreadImpl getThread() {
        return thread;
    }

    ObjectReference getContextVariable() {
        return contextVariable;
    }

    public ObjectReference getContextObject() {
        if (contextVariable != null) {
            return contextVariable;
        } else {
            try {
                return getFrame().thisObject();
            } catch (com.sun.jdi.InternalException iex) {
                if (iex.errorCode() == 35) { // INVALID_SLOT, see http://www.netbeans.org/issues/show_bug.cgi?id=173327
                    return null;
                } else {
                    throw iex; // re-throw the original
                }
            }
        }
    }

    public boolean canInvokeMethods() {
        return canInvokeMethods;
    }

    void setCanInvokeMethods(boolean canInvokeMethods) {
        this.canInvokeMethods = canInvokeMethods;
    }

    void methodToBeInvoked() {
        if (methodInvokePreproc != null) {
            methodInvokePreproc.run();
        }
    }

    void methodInvokeDone() throws IncompatibleThreadStateException {
        try {
            // Refresh the stack frame
            frame = ThreadReferenceWrapper.frame(thread.getThreadReference(), frameDepth);
        } catch (InternalExceptionWrapper |
                 ObjectCollectedExceptionWrapper |
                 IllegalThreadStateExceptionWrapper ex) {
            InvalidExpressionException ieex = new InvalidExpressionException (ex);
            throw new IllegalStateException(ieex);
        } catch (VMDisconnectedExceptionWrapper ex) {
            // Ignore
        }
    }

    JPDADebuggerImpl getDebugger() {
        return debugger;
    }

    public void setTrees(Trees trees) {
        this.trees = trees;
    }

    Trees getTrees() {
        return trees;
    }

    public void setTreePath(TreePath treePath) {
        this.treePath = treePath;
    }

    public TreePath getTreePath() {
        return treePath;
    }

    public VariableInfo getVariableInfo(Tree tree) {
        return variables.get(tree);
    }

    public void putField(Tree tree, Field field, ObjectReference objectRef) {
        VariableInfo info = new VariableInfo.FieldInf(field, objectRef);
        variables.put(tree, info);
    }

    public void putLocalVariable(Tree tree, LocalVariable var) {
        VariableInfo info = new VariableInfo.LocalVarInf(var, this);
        variables.put(tree, info);
    }

    public void putArrayAccess(Tree tree, ArrayReference array, int index) {
        VariableInfo info = new VariableInfo.ArrayElementInf(array, index);
        variables.put(tree, info);
    }

    public void putScriptVariable(Tree tree, ScriptVariable var) {
        VariableInfo info = new VariableInfo.ScriptLocalVarInf(var);
        variables.put(tree, info);
    }

    public ScriptVariable getScriptVariableByName(String name) {
        return scriptLocalVariables.get(name);
    }

    public ScriptVariable createScriptLocalVariable(String name, Type type) {
        Map<String, ScriptVariable> map = stack.peek();
        ScriptVariable var = new ScriptVariable(name, type);
        map.put(name, var);
        scriptLocalVariables.put(name, var);
        return var;
    }

    public void pushBlock() {
        stack.push(new HashMap<String, ScriptVariable>());
    }

    public void popBlock() {
        Map<String, ScriptVariable> map = stack.pop();
        scriptLocalVariables.keySet().removeAll(map.keySet());
    }

    public void disableCollectionOf(ObjectReference or) {
        synchronized (disabledCollectionObjects) {
            if (registerDisabledCollectionOf(or)) {
                try {
                    or.disableCollection();
                } catch (UnsupportedOperationException uoex) {
                    // So we can not disable collection
                    disabledCollectionObjects.remove(or);
                }
                //System.err.println("\nDISABLED COLLECTION of "+or);
                //Thread.dumpStack();
                //System.err.println("");
            }
        }
    }
    
    boolean registerDisabledCollectionOf(ObjectReference or) {
        synchronized (disabledCollectionObjects) {
            if (threadPropertyChangeListener == null) {
                threadPropertyChangeListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (thread.equals(evt.getSource()) && JPDAThread.PROP_SUSPENDED.equals(evt.getPropertyName())
                            || thread.getDebugger().equals(evt.getSource()) && JPDADebuggerImpl.PROP_STATE.equals(evt.getPropertyName())) {
                            //System.err.println("SUSPENDED state of "+thread.getName()+" changed, isMethodInvoking = "+thread.isMethodInvoking()+", isRunning = "+(!thread.isSuspended()));
                            if (thread.getDebugger().getState() != JPDADebuggerImpl.STATE_DISCONNECTED) {
                                if (thread.isMethodInvoking()) return ;
                                if (thread.isSuspended()) return ;
                            }
                            synchronized (disabledCollectionObjects) {
                                // Enable collection of all objects on thread resume to prevent from memory leaks in target VM.
                                //System.err.println("Enabling collection of ALL objects: "+disabledCollectionObjects);
                                enableCollectionOfObjects(null);
                                threadPropertyChangeListener = null;
                            }
                            thread.removePropertyChangeListener(this);
                            thread.getDebugger().removePropertyChangeListener(this);
                        }
                    }
                };
                thread.addPropertyChangeListener(threadPropertyChangeListener);
                thread.getDebugger().addPropertyChangeListener(threadPropertyChangeListener);
            }
            return disabledCollectionObjects.add(or);
        }
    }

    public void enableCollectionOfObjects(Value skip) {
        synchronized (disabledCollectionObjects) {
            Set<ObjectReference> collectedObjects = new HashSet<ObjectReference>(disabledCollectionObjects);
            for (ObjectReference or : collectedObjects) {
                if (skip == null || !skip.equals(or)) {
                    try {
                        ObjectReferenceWrapper.enableCollection(or);
                    } catch (InternalExceptionWrapper ex) {
                    } catch (VMDisconnectedExceptionWrapper ex) {
                        return ;
                    } catch (ObjectCollectedExceptionWrapper ex) {
                        // Should not be thrown!
                        Exceptions.printStackTrace(ex);
                    } catch (UnsupportedOperationExceptionWrapper uoex) {
                        // Ignore
                    }
                    disabledCollectionObjects.remove(or);
                    //System.err.println("\nENABLED COLLECTION of "+or+"\n");
                }
            }
        }
    }

    public void destroy() {
        synchronized (disabledCollectionObjects) {
            if (threadPropertyChangeListener != null) {
                thread.removePropertyChangeListener(threadPropertyChangeListener);
                thread.getDebugger().removePropertyChangeListener(threadPropertyChangeListener);
                threadPropertyChangeListener = null;
            }
        }
    }

    VMCache getVMCache() {
        return vmCache;
    }

    // *************************************************************************

    public static class ScriptVariable {
        private String name;
        private Type type;
        private Mirror value;
        private boolean valueInited = false;

        public ScriptVariable(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public Mirror getValue() {
            // check if value is inited [TODO]
            return value;
        }

        public Type getType() {
            return type;
        }

        public void setValue(Mirror value) {
            this.value = value;
            // check value type [TODO]
            valueInited = true;
        }

    }

    public abstract static class VariableInfo {

        public abstract void setValue(Value value) throws IllegalStateException;

        public abstract Type getType() throws ClassNotLoadedException;


        private static class FieldInf extends VariableInfo {

            private Field field;
            private ObjectReference fieldObject;

            FieldInf(Field field) {
                this.field = field;
            }

            FieldInf(Field field, ObjectReference fieldObject) {
                this.field = field;
                this.fieldObject = fieldObject;
            }

            @Override
            public Type getType() throws ClassNotLoadedException {
                return field.type();
            }

            @Override
            public void setValue(Value value) {
                try {
                    if (fieldObject != null) {
                        fieldObject.setValue(field, value);
                    } else {
                        ((ClassType) field.declaringType()).setValue(field, value);
                    }
                } catch (IllegalArgumentException iaex) {
                    throw new IllegalStateException(new InvalidExpressionException (iaex));
                } catch (InvalidTypeException itex) {
                    throw new IllegalStateException(new InvalidExpressionException (itex));
                } catch (ClassNotLoadedException cnlex) {
                    throw new IllegalStateException(cnlex);
                }
            }
        } // FieldI class

        private static class LocalVarInf extends VariableInfo {

            private LocalVariable var;
            private EvaluationContext context;

            LocalVarInf(LocalVariable var, EvaluationContext context) {
                this.var = var;
                this.context = context;
            }

            @Override
            public Type getType() throws ClassNotLoadedException {
                return var.type();
            }

            @Override
            public void setValue(Value value) {
                try {
                    context.getFrame().setValue(var, value);
                } catch (InvalidTypeException itex) {
                    throw new IllegalStateException(new InvalidExpressionException (itex));
                } catch (ClassNotLoadedException cnlex) {
                    throw new IllegalStateException(cnlex);
                }
            }
        } // LocalVarI class

        private static class ArrayElementInf extends VariableInfo {

            private ArrayReference array;
            private int index;

            ArrayElementInf(ArrayReference array, int index) {
                this.array = array;
                this.index = index;
            }

            @Override
            public Type getType() throws ClassNotLoadedException {
                return array.type();
            }

            @Override
            public void setValue(Value value) {
                try {
                    array.setValue(index, value);
                } catch (ClassNotLoadedException ex) {
                    throw new IllegalStateException(ex);
                } catch (InvalidTypeException ex) {
                    throw new IllegalStateException(new InvalidExpressionException (ex));
                }
            }
        } // ArrayElementI class

        private static class ScriptLocalVarInf extends VariableInfo {

            private ScriptVariable variable;

            ScriptLocalVarInf(ScriptVariable variable) {
                this.variable = variable;
            }

            @Override
            public Type getType() throws ClassNotLoadedException {
                return variable.getType();
            }

            @Override
            public void setValue(Value value) {
                variable.setValue(value);
            }

        } // ScriptLocalVarI class
    }

}
