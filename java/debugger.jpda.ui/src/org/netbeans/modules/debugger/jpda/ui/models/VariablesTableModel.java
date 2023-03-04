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

import com.sun.jdi.Value;
import java.awt.Color;
import java.io.InvalidObjectException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.security.auth.Refreshable;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.MutableVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.ui.views.VariablesViewButtons;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;


/**
 *
 * @author   Jan Jancura
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types=TableModel.class,
                                 position=750),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types=TableModel.class,
                                 position=750),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types=TableModel.class,
                                 position=750),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types=TableModel.class,
                                 position=750)
})
public class VariablesTableModel implements TableModel, Constants {
    
    private static final Map<Variable, Object> mirrors = new WeakHashMap<Variable, Object>();
    private static final Map<Variable, String> values = new WeakHashMap<Variable, String>();
    private static final Map<Variable, String> errorValueMsg = new WeakHashMap<Variable, String>();
    private static final Map<Variable, String> errorToStringMsg = new WeakHashMap<Variable, String>();
    private final Map<Variable, Value> origValues = new WeakHashMap<Variable, Value>();
    private static final Set<Variable> checkReadOnlyMutables = new WeakSet<Variable>();
    
    private JPDADebugger debugger;
    private final List<ModelListener> modelListeners = new ArrayList<ModelListener>();

    public VariablesTableModel(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
    }
    
    @Override
    public Object getValueAt (Object row, String columnID) throws 
    UnknownTypeException {
        
        if ( LOCALS_TO_STRING_COLUMN_ID.equals (columnID) ||
             WATCH_TO_STRING_COLUMN_ID.equals (columnID)
        ) {
            if (row instanceof Super)
                return "";
            else

            if (row instanceof ObjectVariable)
                try {
                    String toStr = ((ObjectVariable) row).getToStringValue ();
                    setErrorToStringMsg((ObjectVariable) row, null);
                    return toStr;
                } catch (InvalidExpressionException ex) {
                    String errorMsg = getMessage (ex);
                    setErrorToStringMsg((ObjectVariable) row, errorMsg);
                    return errorMsg;
                }
            else
            if (row instanceof Variable) {
                return ((Variable) row).getValue ();
            }
            if (row instanceof Operation ||
                row == "lastOperations" || // NOI18N
                row instanceof String && ((String) row).startsWith("operationArguments ")) { // NOI18N
                
                return ""; // NOI18N
            }
        } else
        if ( LOCALS_TYPE_COLUMN_ID.equals (columnID) ||
             WATCH_TYPE_COLUMN_ID.equals (columnID)
        ) {
            if (row instanceof Variable)
                return getShort (((Variable) row).getType ());
            if (row instanceof javax.swing.JToolTip) {
                row = ((javax.swing.JToolTip) row).getClientProperty("getShortDescription");
                if (row instanceof Variable) {
                    if (row instanceof Refreshable && !((Refreshable) row).isCurrent()) {
                        return "";
                    }
                    return ((Variable) row).getType();
                }
            }
        } else
        if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
             WATCH_VALUE_COLUMN_ID.equals (columnID)
        ) {
            if (row instanceof JPDAWatch) {
                JPDAWatch w = (JPDAWatch) row;
                String e = w.getExceptionDescription ();
                if (e != null) {
                    setErrorValueMsg(w, e);
                    return e;
                } else {
                    setErrorValueMsg(w, null);
                }
            }
            if (row instanceof Variable) {
                Variable var = (Variable) row;
                if (VariablesViewButtons.isShowValuePropertyEditors()) {
                    return getValueOf(var, row, columnID);
                } else {
                    return var.getValue();
                }
            }
        }
        if (row instanceof JPDAClassType) {
            return ""; // NOI18N
        }
        if (row.toString().startsWith("SubArray")) { // NOI18N
            return ""; // NOI18N
        }
        if (row instanceof Operation ||
            row == "lastOperations" || // NOI18N
            row instanceof String && ((String) row).startsWith("operationArguments ")) { // NOI18N

            return ""; // NOI18N
        }
        if (row == "noDebugInfoWarning") { // NOI18N
            return ""; // NOI18N
        }
        if (row == "No current thread") { // NOI18N
            return "";
        }
        throw new UnknownTypeException (row);
    }
    
    private Object getValueOf(Variable var, Object row, String columnID) {
        if (var instanceof Refreshable) {
            boolean current = ((Refreshable) var).isCurrent();
            if (!current) {
                return var.getValue();
            }
        }
        if (ValuePropertyEditor.hasPropertyEditorFor(var)) {
            Object mirror = var.createMirrorObject();
            synchronized (mirrors) {
                if (mirror == null) {
                    mirrors.remove(var);
                    origValues.remove(var);
                } else {
                    mirrors.put(var, mirror);
                    //origValues.put(var, ((JDIVariable) var).getJDIValue());
                }
                // Put in any case, the mirror might not be applicable to the property editor.
                values.put(var, var.getValue());
            }
        } else {
            return var.getValue();
            /*synchronized (mirrors) {
                values.put(var, var.getValue());
            }*/
        }
        boolean isROCheck;
        synchronized (checkReadOnlyMutables) {
            isROCheck = checkReadOnlyMutables.remove((Variable) row);
        }
        if (true || isROCheck) {
            fireModelChange(new ModelEvent.TableValueChanged(this, row, columnID, ModelEvent.TableValueChanged.IS_READ_ONLY_MASK));
        }
        return var;
    }
    
    void setOrigValue(Variable var) {
        synchronized (mirrors) {
            origValues.put(var, ((JDIVariable) var).getJDIValue());
        }
    }
    
    static Object getMirrorFor(Variable var) {
        synchronized (mirrors) {
            return mirrors.get(var);
        }
    }
    
    static String getValueOf(Variable var) {
        synchronized (mirrors) {
            return values.get(var);
        }
    }
    
    static String getErrorValueMsg(Variable v) {
        synchronized (errorValueMsg) {
            return errorValueMsg.get(v);
        }
    }
    
    static void setErrorValueMsg(Variable v, String errorMsg) {
        synchronized (errorValueMsg) {
            if (errorMsg != null) {
                errorValueMsg.put(v, errorMsg);
            } else {
                errorValueMsg.remove(v);
            }
        }
    }
    
    static String getErrorToStringMsg(Variable v) {
        synchronized (errorToStringMsg) {
            return errorToStringMsg.get(v);
        }
    }
    
    static void setErrorToStringMsg(Variable v, String errorMsg) {
        synchronized (errorToStringMsg) {
            if (errorMsg != null) {
                errorToStringMsg.put(v, errorMsg);
            } else {
                errorToStringMsg.remove(v);
            }
        }
    }
    
    static boolean isReadOnlyVar(Object row, JPDADebugger debugger) {
        if (row instanceof This)
            return true;
        else {
            if (row instanceof JPDAWatch && row instanceof Refreshable) {
                if (!((Refreshable) row).isCurrent()) {
                    return true;
                }
                try {
                    // Retrieve the evaluated watch so that we can test if it's an object variable or not.
                    java.lang.reflect.Method getEvaluatedWatchMethod = row.getClass().getDeclaredMethod("getEvaluatedWatch");
                    getEvaluatedWatchMethod.setAccessible(true);
                    row = (JPDAWatch) getEvaluatedWatchMethod.invoke(row);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (row instanceof JPDAWatch) {
                JPDAWatch w = (JPDAWatch) row;
                String e = w.getExceptionDescription ();
                if (e != null) {
                    return true; // Errors are read only
                }
            }
            if (row instanceof MutableVariable) {
                synchronized (checkReadOnlyMutables) {
                    checkReadOnlyMutables.add((MutableVariable) row);
                }
                Object mirror = getMirrorFor((Variable) row);
                if (mirror != null) {
                    return false;
                }
            }
            if (row instanceof ObjectVariable) {
                String declaredType;
                if (row instanceof LocalVariable) {
                    declaredType = ((LocalVariable) row).getDeclaredType();
                } else if (row instanceof Field) {
                    declaredType = ((Field) row).getDeclaredType();
                } else {
                    declaredType = ((ObjectVariable) row).getType();
                }
                // Allow to edit Strings
                if (!"java.lang.String".equals(declaredType)) { // NOI18N
                    return true;
                }
            }
            if ( row instanceof LocalVariable ||
                 row instanceof Field ||
                 row instanceof JPDAWatch
            ) {
                if (WatchesNodeModelFilter.isEmptyWatch(row)) {
                    return true;
                } else {
                    return !debugger.canBeModified();
                }
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean isReadOnly (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Variable) {
            if ( LOCALS_TO_STRING_COLUMN_ID.equals (columnID) ||
                 WATCH_TO_STRING_COLUMN_ID.equals (columnID) ||
                 LOCALS_TYPE_COLUMN_ID.equals (columnID) ||
                 WATCH_TYPE_COLUMN_ID.equals (columnID)
            ) return true;
            if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
                 WATCH_VALUE_COLUMN_ID.equals (columnID) 
            ) {
                return isReadOnlyVar(row, debugger);
            }
        }
        if (row instanceof JPDAClassType) {
            return true;
        }
        if (row.toString().startsWith("SubArray")) {
            return true;
        }
        if (row instanceof Operation) {
            return true;
        }
        if (row == "noDebugInfoWarning") {
            return true;
        }
        if (row == "No current thread") {
            return true;
        }
        throw new UnknownTypeException (row);
    }
    
    @Override
    public void setValueAt (Object row, String columnID, Object value) 
    throws UnknownTypeException {
        if (row instanceof MutableVariable) {
            if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
                 WATCH_VALUE_COLUMN_ID.equals (columnID)
            ) {
                if (row == value) {
                    // set of the original value (Cancel)
                    boolean doSet = false;
                    Variable var;
                    Value origValue = null;
                    synchronized (mirrors) {
                        var = (Variable) row;
                        if (origValues.containsKey(var)) {
                            origValue = origValues.get(var);
                            doSet = true;
                        }
                    }
                    if (doSet) {
                        setValueToVar(var, origValue);
                        fireModelChange(new ModelEvent.TableValueChanged(this, row, columnID));
                        return ;
                    }
                }
                try {
                    if (value instanceof String) {
                        ((MutableVariable) row).setValue((String) value);
                    } else if (value instanceof ValuePropertyEditor.VariableWithMirror) {
                        Object mirror = ((ValuePropertyEditor.VariableWithMirror) value).createMirrorObject();
                        ((MutableVariable) row).setFromMirrorObject(mirror);
                    } else if (value instanceof Variable) {
                        return ; // Value is set already.
                    } else {
                        ((MutableVariable) row).setFromMirrorObject(value);
                    }
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor = 
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (), 
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                } catch (InvalidObjectException e) {
                    NotifyDescriptor.Message descriptor = 
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (), 
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                }
                return;
            }
        }
        /*
        if (row instanceof LocalVariable) {
            if (LOCALS_VALUE_COLUMN_ID.equals (columnID)) {
                try {
                    ((LocalVariable) row).setValue ((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor = 
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (), 
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                }
                return;
            }
        }
        if (row instanceof Field) {
            if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
                 WATCH_VALUE_COLUMN_ID.equals (columnID)
            ) {
                try {
                    ((Field) row).setValue ((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor = 
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (), 
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                }
                return;
            }
        }
        if (row instanceof JPDAWatch) {
            if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
                 WATCH_VALUE_COLUMN_ID.equals (columnID)
            ) {
                try {
                    ((JPDAWatch) row).setValue ((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor = 
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (), 
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                }
                return;
            }
        }
        */
        throw new UnknownTypeException (row);
    }
    
    private static void setValueToVar(Variable var, Value value) {
        synchronized (mirrors) {
            mirrors.remove(var);
        }
        try {
            Method setValueMethod = var.getClass().getDeclaredMethod("setValue", Value.class);
            setValueMethod.setAccessible(true);
            setValueMethod.invoke(var, value);
            ValuePropertyEditor pe = VariablesPropertyEditorsModel.getExistingValuePropertyEditor(var);
            if (pe != null) {
                pe.setValue(var);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    @Override
    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
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

    static String getShort (String c) {
        int i = c.lastIndexOf ('.');
        if (i < 0) return c;
        return c.substring (i + 1);
    }
    
    static String getMessage (InvalidExpressionException e) {
        String m = e.getLocalizedMessage ();
        if (m == null) {
            m = e.getMessage ();
        }
        if (m == null) {
            m = NbBundle.getMessage(VariablesTableModel.class, "MSG_NA");
        }
        Throwable t = e.getTargetException();
        if (t != null && e.hasApplicationTarget()) {
            java.io.StringWriter s = new java.io.StringWriter();
            java.io.PrintWriter p = new java.io.PrintWriter(s);
            t.printStackTrace(p);
            p.close();
            m += " \n"+s.toString();
        }
        return m;
    }
}
