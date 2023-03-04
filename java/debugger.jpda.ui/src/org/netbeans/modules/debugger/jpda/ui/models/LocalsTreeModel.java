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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import java.beans.Customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContextChild;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.security.auth.RefreshFailedException;
import javax.security.auth.Refreshable;
import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.AbstractObjectVariable;
import org.netbeans.modules.debugger.jpda.models.AbstractVariable;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.FieldVariable;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types={TreeModel.class},
                                 position=11000),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types={TreeModel.class},
                                 position=11000)
})
public class LocalsTreeModel implements TreeModel, PropertyChangeListener {


    private static final String NO_DEBUG_INFO = "noDebugInfoWarning";

    private static boolean      verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('l') >= 0);
    
    /** Nest array elements when array length is bigger then this. */
    private static final int ARRAY_CHILDREN_NESTED_LENGTH = Integer.getInteger("netbeans.debug.arrayChildrenNest", 100);
    
    
    private JPDADebuggerImpl    debugger;
    private Listener            listener;
    private PropertyChangeListener varChangeListener;
    private final List<ModelListener> listeners = new ArrayList<ModelListener>();
    private PropertyChangeListener[] varListeners;
    //private Map                 cachedLocals = new WeakHashMap();
    private Map<Value, ArrayChildrenNode> cachedArrayChildren = new WeakHashMap<Value, ArrayChildrenNode>();
    
    
    public LocalsTreeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        varChangeListener = new VarChangeListener();
        debugger.varChangeSupport.addPropertyChangeListener(
                WeakListeners.propertyChange(varChangeListener, debugger.varChangeSupport));
    }
    
    public Object getRoot () {
        return ROOT;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        fireTableValueChangedChanged(evt.getSource(), null);
    }
    
    public Object[] getChildren (Object o, int from, int to) 
    throws UnknownTypeException {
        Object[] ch = getChildrenImpl(o, from, to);
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] instanceof Customizer) {
                ((Customizer) ch[i]).addPropertyChangeListener(this);
            }
        }
        return ch;
    }
    
    public Object[] getChildrenImpl (Object o, int from, int to) 
    throws UnknownTypeException {
        try {
            if (o.equals (ROOT)) {
                Object[] os = getLocalVariables (from, to);
                return os;
            } else
            if (o instanceof AbstractObjectVariable) { // ThisVariable & ObjectFieldVariable
                AbstractObjectVariable abstractVariable = (AbstractObjectVariable) o;
                boolean isArray =
                        abstractVariable.getInnerValue () instanceof ArrayReference;
                if (isArray) {
                    to = abstractVariable.getFieldsCount ();
                    // We need to reset it for arrays, to get the full array
                }
                if (isArray && (to - from) > ARRAY_CHILDREN_NESTED_LENGTH) {
                    ArrayChildrenNode achn =
                            cachedArrayChildren.get(abstractVariable.getInnerValue ());
                    if (achn == null) {
                        achn = new ArrayChildrenNode(abstractVariable);
                        cachedArrayChildren.put(abstractVariable.getInnerValue (), achn);
                    } else {
                        achn.update(abstractVariable);
                    }
                    return achn.getChildren();
                } else {
                    return abstractVariable.getFields (from, Math.min(to, abstractVariable.getFieldsCount()));
                }
            } else
            if (o instanceof AbstractVariable) { // FieldVariable
                return new Object [0]; // No children for no-object variables
            } else
            if (o instanceof ArrayChildrenNode) {
                return ((ArrayChildrenNode) o).getChildren();
            } else
            if (o instanceof JPDAClassType) {
                JPDAClassType clazz = (JPDAClassType) o;
                List staticFields = clazz.staticFields();
                Object[] fields;
                try {
                    ClassVariable clazzVar = clazz.classObject();
                    fields = new Object[1 + staticFields.size()];
                    fields[0] = clazzVar;
                    System.arraycopy(staticFields.toArray(), 0, fields, 1, staticFields.size());
                } catch (UnsupportedOperationException uex) {
                    fields = staticFields.toArray();
                }
                return fields;
            } else
            if (o instanceof Operation) {
                Object[] ret = { null, null }; // Results of last operations, Arguments to current operation
                CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
                    getCurrentCallStackFrame ();
                if (frame == null) {
                    return new Object[] {};
                }
                Operation currentOperation = frame.getThread().getCurrentOperation();
                Operation lastOperation = null;
                if (currentOperation != null) {
                    JPDAThread t = debugger.getCurrentThread();
                    if (t != null) {
                        java.util.List<Operation> lastOperations = t.getLastOperations();
                        if (lastOperations != null && lastOperations.size() > 0) {
                            lastOperation = lastOperations.get(lastOperations.size() - 1);
                        }
                    }
                }
                boolean isNotDone = currentOperation != lastOperation;
                if (isNotDone) {
                    ret[0] = "operationArguments " + currentOperation.getMethodName(); // NOI18N
                }
                List<Operation> operations = frame.getThread().getLastOperations();
                if (operations != null && operations.size() > 0 && operations.get(0).getReturnValue() != null) {
                    ret[1] = "lastOperations"; // NOI18N
                }
                if (ret[0] == null && ret[1] == null) return new Object[] {};
                if (ret[0] == null) return new Object[] { ret[1] };
                if (ret[1] == null) return new Object[] { ret[0] };
                return ret;
            } else
            if ("lastOperations" == o) { // NOI18N
                CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
                    getCurrentCallStackFrame ();
                if (frame == null) {
                    return new Object[] {};
                }
                List<Operation> operations = frame.getThread().getLastOperations();
                if (operations == null) {
                    return new Object[] {};
                }
                List<Variable> lastOperationValues = new ArrayList<Variable>(operations.size());
                for (int i = 0; i < operations.size(); i++) {
                    Variable ret = operations.get(i).getReturnValue();
                    if (ret != null) {
                        lastOperationValues.add(ret);
                    }
                }
                return lastOperationValues.toArray();
            } else
            if (o instanceof String && ((String) o).startsWith("operationArguments")) { // NOI18N
                CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
                    getCurrentCallStackFrame ();
                if (frame == null) {
                    return new Object[] {};
                }
                Operation currentOperation = frame.getThread().getCurrentOperation();
                if (currentOperation == null) {
                    return new Object[] {};
                }
                List<org.netbeans.api.debugger.jpda.LocalVariable> arguments;
                try {
                    arguments = frame.findOperationArguments(currentOperation);//currentOperation.getArgumentValues();
                } catch (NativeMethodException nmex) {
                    return new Object[] { "NativeMethodException" };
                }
                if (arguments == null) {
                    return new Object[] {};
                } else {
                    return arguments.toArray();
                }
            } else if (NO_DEBUG_INFO == o) {
                return new Object[0];
            }
            throw new UnknownTypeException (o);
        } catch (VMDisconnectedException ex) {
            return new Object [0];
        }
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (Object node) throws UnknownTypeException {
        try {
            if (node.equals (ROOT)) {
                // Performance, see issue #59058.
                return Integer.MAX_VALUE;
                /*
                CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
                    getCurrentCallStackFrame ();
                if (frame == null) 
                    return 1;
                StackFrame sf = frame.getStackFrame ();
                if (sf == null) 
                    return 1;
                try {
                    int i = 0;
                    List<Operation> operations = frame.getThread().getLastOperations();
                    ReturnVariableImpl returnVariable;
                    boolean haveLastOperations;
                    if (operations != null && operations.size() > 0 && operations.get(0).getReturnValue() != null) {
                        haveLastOperations = true;
                        returnVariable = null;
                    } else {
                        returnVariable = ((JPDAThreadImpl) frame.getThread()).getReturnVariable();
                        haveLastOperations = false;
                    }
                    if (haveLastOperations || returnVariable != null) {
                        i++;
                    }
                    try {
                        i += sf.visibleVariables ().size ();
                    } catch (AbsentInformationException ex) {
                        i++;
                    }
                    // This or Static
                    i++;//if (sf.thisObject () != null) i++;
                    return i;
                } catch (NativeMethodException ex) {
                    return 1;//throw new NoInformationException ("native method");
                } catch (InternalException ex) {
                    return 1;//throw new NoInformationException ("native method");
                } catch (InvalidStackFrameException ex) {
                    return 1;//throw new NoInformationException ("thread is running");
                } catch (VMDisconnectedException ex) {
                }
                return 0;
                 */
            } else
            if (node instanceof AbstractVariable) { // ThisVariable & FieldVariable
                AbstractVariable abstractVariable = (AbstractVariable) node;
                if (abstractVariable.getInnerValue () instanceof 
                    ArrayReference
                ) {
                    // Performance, see issue #59058.
                    return Integer.MAX_VALUE;
                    //return Math.min (abstractVariable.getFieldsCount (), ARRAY_CHILDREN_NESTED_LENGTH);
                }
                // Performance, see issue #59058.
                return Integer.MAX_VALUE;
                //return abstractVariable.getFieldsCount ();
            } else
            if (node instanceof ArrayChildrenNode) {
                // Performance, see issue #59058.
                return Integer.MAX_VALUE;
                //return ((ArrayChildrenNode) node).getChildren().length;
            } else
            if (node instanceof JPDAClassType) {
                // Performance, see issue #59058.
                return Integer.MAX_VALUE;
                //JPDAClassType clazz = (JPDAClassType) node;
                //return 1 + clazz.staticFields().size();
            } else
            if (node instanceof JPDAClassType) {
                JPDAClassType clazz = (JPDAClassType) node;
                return 1 + clazz.staticFields().size();
            } else
            if (node instanceof Operation) {
                return Integer.MAX_VALUE;
            } else
            if ("lastOperations" == node) { // NOI18N
                CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
                    getCurrentCallStackFrame ();
                if (frame == null) {
                    return 0;
                }
                List<Operation> operations = frame.getThread().getLastOperations();
                if (operations != null) {
                    return operations.size();
                } else {
                    return 0;
                }
            } else
            if (node instanceof String && ((String) node).startsWith("operationArguments")) { // NOI18N
                // Performance, see issue #59058.
                return Integer.MAX_VALUE;
            } else if (NO_DEBUG_INFO == node) {
                return 0;
            } else
                throw new UnknownTypeException (node);
            } catch (VMDisconnectedException ex) {
        }
        return 0;
    }
    
    public boolean isLeaf (final Object o) throws UnknownTypeException {
        if (o.equals (ROOT))
            return false;
        if (o instanceof AbstractVariable) {
            if (o instanceof FieldVariable) {
                return true;
            }
            if (o instanceof Refreshable && !((Refreshable) o).isCurrent()) {
                debugger.getRequestProcessor().post(new Runnable() {
                    public void run() {
                        try {
                            ((Refreshable) o).refresh();
                        } catch (RefreshFailedException ex) {
                            return ;
                        }
                        if (!(((AbstractVariable) o).getInnerValue () instanceof ObjectReference)) {
                            fireNodeChildrenChanged(o);
                        }
                    }
                });
                return false;
            }
            return !(((AbstractVariable) o).getInnerValue () instanceof ObjectReference);
        }
        if (o.toString().startsWith("SubArray")) {
            return false;
        }
        if (o.equals ("NoInfo")) // NOI18N
            return true;
        if (o instanceof JPDAClassType) return false;
        if (o instanceof Operation) return false;
        if (o == "lastOperations") return false;
        if (o == NO_DEBUG_INFO) return true;
        if (o instanceof String && ((String) o).startsWith("operationArguments")) { // NOI18N
            return false;
        }
        throw new UnknownTypeException (o);
    }


    public void addModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
            if (listener == null)
                listener = new Listener (this, debugger);
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
            if (listeners.size () == 0) {
                listener.destroy ();
                listener = null;
            }
        }
    }
    
    private void fireTreeChanged () {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        int i, k = ls.size ();
        for (i = 0; i < k; i++)
            ls.get(i).modelChanged (
                new ModelEvent.TreeChanged (this)
            );
    }
    
    private void fireTableValueChangedChanged (Object node, String propertyName) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        int i, k = ls.size ();
        for (i = 0; i < k; i++)
            ls.get(i).modelChanged (
                new ModelEvent.TableValueChanged (this, node, propertyName)
            );
    }

    private void fireNodeChanged (Object node) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        int i, k = ls.size ();
        for (i = 0; i < k; i++)
            ls.get(i).modelChanged (
                new ModelEvent.NodeChanged (this, node)
            );
    }

    private void fireNodeChildrenChanged(Object node) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        int i, k = ls.size ();
        for (i = 0; i < k; i++) {
            ls.get(i).modelChanged (
                new ModelEvent.NodeChanged(this, node, ModelEvent.NodeChanged.CHILDREN_MASK)
            );
        }
    }


    // private methods .........................................................
    
    private static final String PROP_OPERATIONS_UPDATE = "operationsUpdate"; // NOI18N
    private static final String PROP_OPERATIONS_SET = "operationsSet"; // NOI18N
    
    private Object[] getLocalVariables (
        int from, 
        int to
    ) {
        CallStackFrameImpl callStackFrame = (CallStackFrameImpl) debugger.
            getCurrentCallStackFrame ();
        if (callStackFrame == null)
            return new String [] {"No current thread"};
        final JPDAThreadImpl thread = (JPDAThreadImpl) callStackFrame.getThread();
        thread.accessLock.readLock().lock();
        PropertyChangeListener operationsUpdateListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (PROP_OPERATIONS_UPDATE.equals(name)) {
                    fireTreeChanged();
                }
                if (PROP_OPERATIONS_SET.equals(name)) {
                    ((BeanContextChild) thread).removePropertyChangeListener(PROP_OPERATIONS_UPDATE, this);
                    ((BeanContextChild) thread).removePropertyChangeListener(PROP_OPERATIONS_SET, this);
                }
            }
        };
        ((BeanContextChild) thread).addPropertyChangeListener(PROP_OPERATIONS_UPDATE, operationsUpdateListener);
        ((BeanContextChild) thread).addPropertyChangeListener(PROP_OPERATIONS_SET, operationsUpdateListener);
        try {
            StackFrame stackFrame = null;
            try {
                stackFrame = callStackFrame.getStackFrame ();
            } catch (InvalidStackFrameException e) {
            }
            if (stackFrame == null)
                return new String [] {"No current thread"};
            This thisVar = callStackFrame.getThisVariable();
            List<Operation> operations = thread.getLastOperations();
            Variable breakpointVariable;
            boolean haveLastOperations;
            if (operations != null && operations.size() > 0 && operations.get(0).getReturnValue() != null) {
                haveLastOperations = true;
                breakpointVariable = null;
            } else {
                breakpointVariable = getBreakpointVar(thread);
                haveLastOperations = false;
            }
            //int retValShift = (haveLastOperations || returnVariable != null) ? 1 : 0;
            int retValShift = (breakpointVariable != null) ? 1 : 0;
            Operation currentOperation = thread.getCurrentOperation();
            //int currArgShift = (currentOperation != null && CallStackFrameImpl.IS_JDK_160_02) ? 1 : 0;
            int currArgShift = (currentOperation != null) ? 1 : 0;
            int shift = retValShift + currArgShift;
            if (thisVar == null) {
                ReferenceType classType = LocationWrapper.declaringType(StackFrameWrapper.location(stackFrame));
                Object[] avs = null;
                avs = getLocalVariables (
                    callStackFrame,
                    stackFrame,
                    Math.max (from - shift - 1, 0),
                    Math.max (to - shift - 1, 0)
                );
                Object[] result = new Object [avs.length + shift + 1];
                if (from < 1 && retValShift > 0) {
                    result[0] = breakpointVariable;
                }
                if (from < 1 && currArgShift > 0) {
                    //result[retValShift] = "operationArguments " + currentOperation.getMethodName(); // NOI18N
                    result[retValShift] = currentOperation;
                }
                if (from < 1 + shift) {
                    //result [0] = new ThisVariable (debugger, classType.classObject(), "");
                    result[shift] = debugger.getClassType(classType);
                }
                System.arraycopy (avs, 0, result, 1 + shift, avs.length);
                return result;
            } else {
                Object[] avs = null;
                avs = getLocalVariables (
                    callStackFrame,
                    stackFrame,
                    Math.max (from - shift - 1, 0),
                    Math.max (to - shift - 1, 0)
                );
                Object[] result = new Object [avs.length + shift + 1];
                if (from < 1 && retValShift > 0) {
                    result[0] = breakpointVariable;
                }
                if (from < 1 && currArgShift > 0) {
                    //result[retValShift] = "operationArguments " + currentOperation.getMethodName(); // NOI18N
                    result[retValShift] = currentOperation;
                }
                if (from < 1 + shift) {
                    result[shift] = thisVar;
                }
                System.arraycopy (avs, 0, result, 1 + shift, avs.length);
                return result;
            }
        } catch (NativeMethodException nmex) {
            return new String[] { "NativeMethodException" };
        } catch (InternalExceptionWrapper ex) {
            return new String [] {ex.getMessage ()};
        } catch (VMDisconnectedExceptionWrapper dex) {
            return new String[] {  };
        } catch (InvalidStackFrameException isfex) {
            return new String [] {"No current thread"};
        } catch (InvalidStackFrameExceptionWrapper isfex) {
            return new String [] {"No current thread"};
        } finally {
            thread.accessLock.readLock().unlock();
        }
    }
    
    private Object[] getLocalVariables (
        final CallStackFrameImpl    callStackFrame, 
        final StackFrame            stackFrame,
        int                         from,
        int                         to
    ) {
        Object[] locals;
        try {
            locals = callStackFrame.getLocalVariables();
        } catch (AbsentInformationException aiex) {
            Object[] nodes = callStackFrame.getMethodArguments();
            if (nodes == null) {
                nodes = new Object[] {};
            }
            locals = new Object[nodes.length + 1];
            System.arraycopy(nodes, 0, locals, 0, nodes.length);
            locals[nodes.length] = NO_DEBUG_INFO;
        }
        if (locals == null) {
            locals = new Object[] {};
        }
        int n = locals.length;
        to = Math.min(n, to);
        from = Math.min(n, from);
        if (from != 0 || to != n) {
            Object[] subLocals = new Object[to - from];
            for (int i = from; i < to; i++) {
                subLocals[i - from] = locals[i];
            }
            locals = subLocals;
        }
        updateVarListeners(locals);
        return locals;
        /*
        try {
            String className = stackFrame.location ().declaringType ().name ();
            List l = stackFrame.visibleVariables ();
            to = Math.min(l.size(), to);
            from = Math.min(l.size(), from);
            int i, k = to - from, j = from;
            AbstractVariable[] locals = new AbstractVariable [k];
            for (i = 0; i < k; i++) {
                LocalVariable lv = (LocalVariable) l.get (j++);
                locals [i] = getLocal (lv, callStackFrame, className);
            }
            return locals;
        } catch (NativeMethodException ex) {
            throw new AbsentInformationException ("native method");
        } catch (InvalidStackFrameException ex) {
            throw new AbsentInformationException ("thread is running");
        } catch (VMDisconnectedException ex) {
            return new AbstractVariable [0];
        }
         */
    }
    /*
    private Local getLocal (LocalVariable lv, CallStackFrameImpl frame, String className) {
        Value v = frame.getStackFrame ().getValue (lv);
        Local local = (Local) cachedLocals.get(lv);
        if (local != null) {
            local.setInnerValue(v);
            local.setFrame(frame);
            local.setLocalVariable(lv);
        } else {
            if (v instanceof ObjectReference) {
                local = new ObjectLocalVariable (
                    debugger, 
                    v, 
                    className, 
                    lv, 
                    JPDADebuggerImpl.getGenericSignature (lv), 
                    frame
                );
            } else {
                local = new Local (debugger, v, className, lv, frame);
            }
            cachedLocals.put(lv, local);
        }
        return local;
    }
     */
    
    private Variable getBreakpointVar(JPDAThreadImpl thread) {
        Variable bv = thread.getReturnVariable();
        if (bv == null) {
            JPDABreakpointEvent be = thread.getCurrentBreakpointEvent();
            if (be != null) {
                bv = be.getVariable();
            }
        }
        if (bv != null) {
            Class<? extends Variable> clazz = bv.getClass();
            if (clazz == AbstractObjectVariable.class || clazz == AbstractVariable.class) {
                // Anonymous variable, ignore
                bv = null;
            }
        }
        return bv;
    }
    
    private void updateVarListeners(Object[] vars) {
        varListeners = new PropertyChangeListener[vars.length];
        for (int i = 0; i < vars.length; i++) {
            Object var = vars[i];
            PropertyChangeListener l = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    fireNodeChanged(evt.getSource());
                }
            };
            varListeners[i] = l; // Hold it so that it does not get lost, till the array is updated.
            if (var instanceof AbstractVariable) {
                ((AbstractVariable) var).addPropertyChangeListener(WeakListeners.propertyChange(l, var));
            }
        }
    }
    
    JPDADebuggerImpl getDebugger () {
        return debugger;
    }
    
    
    // innerclasses ............................................................

    private class VarChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object var = evt.getSource();
            if (var instanceof Variable && !(var instanceof JPDAWatch)) {
                fireNodeChanged(var);
            }
        }

    }
    
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebuggerImpl debugger;
        private WeakReference<LocalsTreeModel> model;
        
        public Listener (
            LocalsTreeModel tm,
            JPDADebuggerImpl debugger
        ) {
            this.debugger = debugger;
            model = new WeakReference<LocalsTreeModel>(tm);
            debugger.addPropertyChangeListener (this);
        }
        
        void destroy () {
            debugger.removePropertyChangeListener (this);
            if (task != null) {
                // cancel old task
                task.cancel ();
                if (verbose)
                    System.out.println("LTM cancel old task " + task);
                task = null;
            }
        }
        
        private LocalsTreeModel getModel () {
            LocalsTreeModel tm = model.get ();
            if (tm == null) {
                destroy ();
            }
            return tm;
        }
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public void propertyChange (PropertyChangeEvent e) {
            if ( ( (e.getPropertyName () == 
                     JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME) ||
                   //(e.getPropertyName () == debugger.PROP_CURRENT_THREAD) ||
                   (e.getPropertyName () == JPDADebugger.PROP_STATE)
                 ) && (debugger.getState () == JPDADebugger.STATE_STOPPED)
            ) {
                // IF state has been changed to STOPPED or
                // IF current call stack frame has been changed & state is stoped
                final LocalsTreeModel ltm = getModel ();
                if (ltm == null) return;
                if (task != null) {
                    // cancel old task
                    task.cancel ();
                    if (verbose)
                        System.out.println("LTM cancel old task " + task);
                    task = null;
                }
                task = debugger.getRequestProcessor().post (new Runnable () {
                    public void run () {
                        if (debugger.getState () != JPDADebugger.STATE_STOPPED) {
                            if (verbose)
                                System.out.println("LTM cancel started task " + task);
                            return;
                        }
                        if (verbose)
                            System.out.println("LTM do task " + task);
                        ltm.fireTreeChanged ();
                    }
                }, 100);
                if (verbose)
                    System.out.println("LTM  create task " + task);
            } else
            if ( (e.getPropertyName () == JPDADebugger.PROP_STATE) &&
                 (debugger.getState () != JPDADebugger.STATE_STOPPED) &&
                 (task != null)
            ) {
                // debugger has been resumed
                // =>> cancel task
                task.cancel ();
                if (verbose)
                    System.out.println("LTM cancel task " + task);
                task = null;
            }
        }
    }
    
    /**
     * The hierarchical representation of nested array elements.
     * Used for arrays longer then {@link #ARRAY_CHILDREN_NESTED_LENGTH}.
     */
    private static final class ArrayChildrenNode {
        
        private AbstractObjectVariable var;
        private int from = 0;
        private int length;
        private int maxIndexLog;
        
        public ArrayChildrenNode(AbstractObjectVariable var) {
            this(var, 0, var.getFieldsCount(), -1);
        }
        
        private ArrayChildrenNode(AbstractObjectVariable var, int from, int length,
                                  int maxIndex) {
            this.var = var;
            this.from = from;
            this.length = length;
            if (maxIndex < 0) {
                maxIndex = from + length - 1;
            }
            this.maxIndexLog = log10(maxIndex);
        }
        
        private static int pow(int a, int b) {
            if (b == 0) return 1;
            int p = a;
            for (int i = 1; i < b; i++) {
                p *= a;
            }
            return p;
        }
        
        public Object[] getChildren() {
            if (length > ARRAY_CHILDREN_NESTED_LENGTH) {
                int depth = (int) Math.ceil(Math.log(length)/Math.log(ARRAY_CHILDREN_NESTED_LENGTH) - 1);
                int n = pow(ARRAY_CHILDREN_NESTED_LENGTH, depth);
                int numCh = (int) Math.ceil(length/((double) n));
                
                // We have 'numCh' children, each with 'n' sub-children (or possibly less for the last one)
                Object[] ch = new Object[numCh];
                for (int i = 0; i < numCh; i++) {
                    int chLength = n;
                    if (i == (numCh - 1)) {
                        chLength = length % n;
                        if (chLength == 0) chLength = n;
                    }
                    ch[i] = new ArrayChildrenNode(var, from + i*n, chLength, from + length - 1);
                }
                return ch;
            } else {
                return var.getFields(from, from + length);
            }
        }
        
        public void update(AbstractObjectVariable var) {
            this.var = var;
        }
        
        /** Overriden equals so that the nodes are not re-created when not necessary. */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ArrayChildrenNode)) return false;
            ArrayChildrenNode achn = (ArrayChildrenNode) obj;
            return achn.var.equals(this.var) &&
                   achn.from == this.from &&
                   achn.length == this.length;
        }
        
        @Override
        public int hashCode() {
            return var.hashCode() + from + length;
        }
        
        @Override
        public String toString() {
            int num0 = maxIndexLog - log10(from);
            String froms;
            if (num0 > 0) {
                froms = zeros(2*num0) + from; // One space is roughly 1/2 of width of a number
            } else {
                froms = Integer.toString(from);
            }
            int last = from + length - 1;
            num0 = maxIndexLog - log10(last);
            String lasts;
            if (num0 > 0) {
                lasts = zeros(2*num0) + last; // One space is roughly 1/2 of width of a number
            } else {
                lasts = Integer.toString(last);
            }
            return "SubArray"+froms+"-"+lasts; // NOI18N
        }
        
        private static int log10(int n) {
            int l = 1;
            while ((n = n / 10) > 0) l++;
            return l;
        }

        //private static final String ZEROS = "000000000000"; // NOI18N
        private static final String ZEROS = "            "; // NOI18N

        private static String zeros(int n) {
            if (n < ZEROS.length()) {
                return ZEROS.substring(0, n);
            } else {
                String z = ZEROS;
                while (z.length() < n) z += " "; // NOI18N
                return z;
            }
        }

    }

}
