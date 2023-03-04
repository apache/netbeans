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

import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ReturnVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.models.ExceptionVariableImpl;
import org.netbeans.modules.debugger.jpda.models.FieldReadVariableImpl;
import org.netbeans.modules.debugger.jpda.models.FieldToBeVariableImpl;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types=ExtendedNodeModel.class,
                                 position=300),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types=ExtendedNodeModel.class,
                                 position=300),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types=ExtendedNodeModel.class,
                                 position=300)
})
public class VariablesNodeModel implements ExtendedNodeModel { 

    public static final String FIELD =
        "org/netbeans/modules/debugger/resources/watchesView/Field.gif";
    public static final String LOCAL =
        "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png";
    public static final String FIXED_WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/FixedWatch.gif";
    public static final String STATIC_FIELD =
        "org/netbeans/modules/debugger/resources/watchesView/StaticField.gif";
    public static final String SUPER =
        "org/netbeans/modules/debugger/resources/watchesView/SuperVariable.gif";
    public static final String STATIC =
        "org/netbeans/modules/debugger/resources/watchesView/SuperVariable.gif";
    public static final String RETURN =
        "org/netbeans/modules/debugger/jpda/resources/Filter.gif";
    public static final String NO_DEBUG_INFO =
        "org/netbeans/modules/debugger/jpda/resources/wrong_pass.png";
    public static final String EXPR_ARGUMENTS =
        "org/netbeans/modules/debugger/jpda/resources/ExprArguments.gif";

    private static final int TO_STRING_LENGTH_LIMIT = 10000;


    private JPDADebugger debugger;
    
    private RequestProcessor evaluationRP;
    private final Collection modelListeners = new HashSet();
    
    
    public VariablesNodeModel (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        evaluationRP = lookupProvider.lookupFirst(null, RequestProcessor.class);
    }
    
    
    @Override
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getMessage 
                (VariablesNodeModel.class, "CTL_LocalsModel_Column_Name_Name");
        }
        if (o instanceof Field) {
            return ((Field) o).getName ();
        }
        if (o instanceof LocalVariable) {
            return ((LocalVariable) o).getName ();
        }
        if (o instanceof Super) {
            return "super"; // NOI18N
        }
        if (o instanceof This) {
            return "this"; // NOI18N
        }
        if (o == "NoInfo") { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "CTL_No_Info");
        }
        if (o == "No current thread") { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "NoCurrentThreadVar");
        }
        if (o instanceof JPDAClassType) {
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_VariablesFilter_StaticNode");    // NOI18N
        }
        if (o instanceof ClassVariable) {
            return "class";
        }
        if (o instanceof ReturnVariable) {
            return "return "+((ReturnVariable) o).methodName()+"()";
        }
        if (o instanceof ExceptionVariableImpl) {
            ExceptionVariableImpl ev = (ExceptionVariableImpl) o;
            //return "Thrown exception "+ev.getExceptionClassName();
            return NbBundle.getMessage(VariablesNodeModel.class, "ThrownExceptionVar", ev.getExceptionClassName());
        }
        if (o instanceof FieldReadVariableImpl) {
            FieldReadVariableImpl frv = (FieldReadVariableImpl) o;
            return NbBundle.getMessage(VariablesNodeModel.class, "FieldValueReadVar", frv.getFieldVariable().getName());
        }
        if (o instanceof FieldToBeVariableImpl) {
            FieldToBeVariableImpl ftbv = (FieldToBeVariableImpl) o;
            return NbBundle.getMessage(VariablesNodeModel.class, "FieldValueToBeVar", ftbv.getFieldVariable().getName());
        }
        if (o instanceof Operation) {
            Operation op = (Operation) o;
            Operation lastOperation = null;
            {
                JPDAThread t = debugger.getCurrentThread();
                if (t != null) {
                    java.util.List<Operation> lastOperations = t.getLastOperations();
                    if (lastOperations != null && lastOperations.size() > 0) {
                        lastOperation = lastOperations.get(lastOperations.size() - 1);
                    }
                }
            }
            boolean isDone = op == lastOperation;
            if (isDone) {
                return NbBundle.getMessage(VariablesNodeModel.class, "afterOperation", op.getMethodName());
            } else {
                return NbBundle.getMessage(VariablesNodeModel.class, "beforeOperation", op.getMethodName());
            }
        }
        if (o == "lastOperations") { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "lastOperationsNode");
        }
        if (o instanceof String && ((String) o).startsWith("operationArguments ")) { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "operationArgumentsNode", ((String) o).substring("operationArguments ".length()));
        }
        if (o == "NativeMethodException") {
            return NbBundle.getMessage(VariablesNodeModel.class, "NativeMethod");
        }
        if (o == "noDebugInfoWarning") {
            return BoldVariablesTableModelFilter.toHTML(
                    NbBundle.getMessage(VariablesNodeModel.class, "noDebugInfoWarning"),
                    true,
                    false,
                    Color.RED);
        }
        String str = o.toString();
        if (str.startsWith("SubArray")) { // NOI18N
            int index = str.indexOf('-');
            //int from = Integer.parseInt(str.substring(8, index));
            //int to = Integer.parseInt(str.substring(index + 1));
            return NbBundle.getMessage (VariablesNodeModel.class,
                    "CTL_LocalsModel_Column_Name_SubArray",
                    str.substring(8, index), str.substring(index + 1));
        }
        String name = VariablesFormatterFilter.FORMATTED_CHILDREN_VARS.get(o);
        if (name != null) {
            return name;
        }
        throw new UnknownTypeException (o);
    }
    
    private final Map shortDescriptionMap = new HashMap();
    
    @Override
    public String getShortDescription (final Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getMessage(VariablesNodeModel.class, "CTL_LocalsModel_Column_Name_Desc");
        }
        if (o == "NoInfo") {// NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "CTL_No_Info_descr");
        }
        if (o == "No current thread") { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "NoCurrentThreadVar");
        }
        if (o instanceof JPDAClassType) {
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_VariablesFilter_StaticNode_descr");    // NOI18N
        }
        if (o instanceof ClassVariable) {
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_VariablesFilter_Class_descr");    // NOI18N
        }
        if (o instanceof ReturnVariable) {
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_VariablesFilter_Return_descr", ((ReturnVariable) o).methodName()+"()");    // NOI18N
        }
        if (o instanceof Operation) {
            Operation op = (Operation) o;
            Operation lastOperation = null;
            {
                JPDAThread t = debugger.getCurrentThread();
                if (t != null) {
                    java.util.List<Operation> lastOperations = t.getLastOperations();
                    if (lastOperations != null && lastOperations.size() > 0) {
                        lastOperation = lastOperations.get(lastOperations.size() - 1);
                    }
                }
            }
            boolean isDone = op == lastOperation;
            //boolean isDone = op.getReturnValue() != null;
            if (isDone) {
                return NbBundle.getMessage(VariablesNodeModel.class, "afterOperation_descr", op.getMethodName());
            } else {
                return NbBundle.getMessage(VariablesNodeModel.class, "beforeOperation_descr", op.getMethodName());
            }
        }
        if (o instanceof ExceptionVariableImpl) {
            ExceptionVariableImpl ev = (ExceptionVariableImpl) o;
            return NbBundle.getMessage(VariablesNodeModel.class, "ThrownExceptionVar_descr", ev.getExceptionClassName());
        }
        if (o instanceof FieldReadVariableImpl) {
            FieldReadVariableImpl frv = (FieldReadVariableImpl) o;
            return NbBundle.getMessage(VariablesNodeModel.class, "FieldValueReadVar_descr", frv.getFieldVariable().getName());
        }
        if (o instanceof FieldToBeVariableImpl) {
            FieldToBeVariableImpl ftbv = (FieldToBeVariableImpl) o;
            return NbBundle.getMessage(VariablesNodeModel.class, "FieldValueToBeVar_descr", ftbv.getFieldVariable().getName());
        }
        if (o == "lastOperations") { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_LastOperations_descr");
        }
        if (o instanceof String && ((String) o).startsWith("operationArguments ")) { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "operationArgumentsNode_descr", ((String) o).substring("operationArguments ".length()));
        }
        if (o == "NativeMethodException") {
            return NbBundle.getMessage(VariablesNodeModel.class, "NativeMethod_descr");
        }
        if (o == "noDebugInfoWarning") {
            return NbBundle.getMessage(VariablesNodeModel.class, "noDebugInfoWarning_descr");
        }
        String str = o.toString();
        if (str.startsWith("SubArray")) { // NOI18N
            int index = str.indexOf('-');
            return NbBundle.getMessage (VariablesNodeModel.class,
                    "CTL_LocalsModel_Column_Descr_SubArray",
                    str.substring(8, index), str.substring(index + 1));
        }
        synchronized (shortDescriptionMap) {
            Object shortDescription = shortDescriptionMap.remove(o);
            if (shortDescription instanceof String) {
                return (String) shortDescription;
            } else if (shortDescription instanceof UnknownTypeException) {
                throw (UnknownTypeException) shortDescription;
            }
        }
        testKnown(o);
        // Called from AWT - we need to postpone the work...
        evaluationRP.post(new Runnable() {
            @Override
            public void run() {
                Object shortDescription = getShortDescriptionSynch(o);
                if (shortDescription != null && !"".equals(shortDescription)) {
                    synchronized (shortDescriptionMap) {
                        shortDescriptionMap.put(o, shortDescription);
                    }
                    fireModelChange(new ModelEvent.NodeChanged(VariablesNodeModel.this,
                        o, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                }
            }
        });
        return "";
    }
    
    protected String getShortDescriptionSynch (Object o) {
        if (o instanceof Field) {
            if (o instanceof ObjectVariable) {
                String type = ((ObjectVariable) o).getType ();
                String declaredType = ((Field) o).getDeclaredType ();
                if (type.equals (declaredType)) {
                    try {
                        return "(" + type + ") " + 
                            getLimitedToString((ObjectVariable) o);
                    } catch (InvalidExpressionException ex) {
                        return ex.getLocalizedMessage ();
                    }
                } else {
                    try {
                        return "(" + declaredType + ") " + "(" + type + ") " + 
                            getLimitedToString((ObjectVariable) o);
                    } catch (InvalidExpressionException ex) {
                        return ex.getLocalizedMessage ();
                    }
                }
            } else {
                return "(" + ((Field) o).getDeclaredType () + ") " + 
                    ((Field) o).getValue ();
            }
        }
        if (o instanceof LocalVariable) {
            if (o instanceof ObjectVariable) {
                String type = ((ObjectVariable) o).getType ();
                String declaredType = ((LocalVariable) o).getDeclaredType ();
                if (type.equals (declaredType)) {
                    try {
                        return "(" + type + ") " + 
                            getLimitedToString((ObjectVariable) o);
                    } catch (InvalidExpressionException ex) {
                        return ex.getLocalizedMessage ();
                    }
                } else {
                    try {
                        return "(" + declaredType + ") " + "(" + type + ") " + 
                            getLimitedToString((ObjectVariable) o);
                    } catch (InvalidExpressionException ex) {
                        return ex.getLocalizedMessage ();
                    }
                }
            } else {
                return "(" + ((LocalVariable) o).getDeclaredType () + ") " + 
                    ((LocalVariable) o).getValue ();
            }
        }
        if (o instanceof Super) {
            return ((Super) o).getType ();
        }
        if (o instanceof This) {
            try {
                return "(" + ((This) o).getType () + ") " + 
                    getLimitedToString((This) o);
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        }
        return null;
        //throw new UnknownTypeException (o);
    }
    
    private static String getLimitedToString(ObjectVariable v) throws InvalidExpressionException {
        String toString = null;
        try {
            java.lang.reflect.Method toStringMethod =
                    v.getClass().getMethod("getToStringValue",  // NOI18N
                                           new Class[] { Integer.TYPE });
            toStringMethod.setAccessible(true);
            toString = (String) toStringMethod.invoke(v, TO_STRING_LENGTH_LIMIT);
        } catch (InvocationTargetException itex) {
            Throwable te = itex.getTargetException();
            if (te instanceof InvalidExpressionException) {
                throw (InvalidExpressionException) te;
            } else {
                Exceptions.printStackTrace(itex);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        if (toString == null) {
            toString = v.getToStringValue();
        }
        return toString;
    }
    
    protected void testKnown(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT ||
            o instanceof Field ||
            o instanceof LocalVariable ||
            o instanceof Super ||
            o instanceof This) {
            
            return ;
        }
        String str = o.toString();
        if (str.startsWith("SubArray") ||   // NOI18N
            o == "NoInfo" ||                // NOI18N
            o == "No current thread" ||     // NOI18N
            o == "lastOperations" ||        // NOI18N
            o instanceof String && ((String) o).startsWith("operationArguments ") || // NOI18N
            o == "NativeMethodException" || // NOI18N
            o == "noDebugInfoWarning" ||    // NOI18N
            o instanceof JPDAClassType ||
            o instanceof ClassVariable ||
            o instanceof ReturnVariable ||
            o instanceof ExceptionVariableImpl ||
            o instanceof FieldReadVariableImpl ||
            o instanceof FieldToBeVariableImpl ||
            o instanceof Operation) {
            
            return ;
        }
        throw new UnknownTypeException (o);
    }
    
    @Override
    public String getIconBase (Object o) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public Transferable clipboardCopy(Object node) throws IOException,
                                                          UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Transferable clipboardCut(Object node) throws IOException,
                                                         UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    @Override
    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return FIELD;
        }
        if (node instanceof Field) {
            if (((Field) node).isStatic ()) {
                return STATIC_FIELD;
            } else {
                return FIELD;
            }
        }
        if (node instanceof LocalVariable) {
            return LOCAL;
        }
        if (node instanceof Super) {
            return SUPER;
        }
        if (node instanceof This) {
            return FIELD;
        }
        if (node instanceof JPDAClassType) {
            return STATIC;
        }
        if (node instanceof ClassVariable) {
            return STATIC;
        }
        if (node instanceof Operation) {
            return EXPR_ARGUMENTS;
        }
        if (node instanceof ReturnVariable || node == "lastOperations") {       // NOI18N
            return RETURN;
        }
        if (node instanceof ExceptionVariableImpl) {
            return RETURN;
        }
        if (node instanceof FieldReadVariableImpl || node instanceof FieldToBeVariableImpl) {
            return FIELD;
        }
        if (node == "noDebugInfoWarning") {                                     // NOI18N
            return NO_DEBUG_INFO;
        }
        if (node instanceof String && ((String) node).startsWith("operationArguments ")) { // NOI18N
            return EXPR_ARGUMENTS;
        }
        if (node.toString().startsWith("SubArray")) {                           // NOI18N
            return LOCAL;
        }
        if (node == "NoInfo" || node == "No current thread" || node == "NativeMethodException") {// NOI18N
            return null;
        }
        if (node instanceof Variable) {
            return LOCAL;
        }
        throw new UnknownTypeException (node);
    }
    
    @Override
    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    @Override
    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }
    
    protected void fireModelChange(ModelEvent me) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged(me);
        }
    }

}
