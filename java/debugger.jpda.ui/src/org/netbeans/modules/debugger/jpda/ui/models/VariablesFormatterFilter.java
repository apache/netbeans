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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.VMDisconnectedException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.formatters.Formatters;
import org.netbeans.modules.debugger.jpda.expr.formatters.FormattersLoopControl;
import org.netbeans.modules.debugger.jpda.expr.formatters.VariablesFormatter;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.VariablesFilter;
import org.netbeans.spi.debugger.jpda.VariablesFilterAdapter;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 *
 * @author   Martin Entlicher
 */
@VariablesFilter.Registration(path="netbeans-JPDASession")
public class VariablesFormatterFilter extends VariablesFilterAdapter {

    static Map<Object, String> FORMATTED_CHILDREN_VARS = new WeakHashMap<Object, String>();

    //private JPDADebugger debugger;
    private VariablesFormatter[] formattersWithExpandTestCode;
    private final Object formattersLock = new Object();
    private Properties jpdaProperties;
    private PropertyChangeListener formattersChangeListener;
    private boolean formattersLoopWarned = false;
    private final Map<ObjectVariable, Boolean> childrenExpandTest = new WeakHashMap<ObjectVariable, Boolean>();
    private final Set<ObjectVariable> childrenExpandTestProcessing = new HashSet<ObjectVariable>();
    private final RequestProcessor expandTestProcessor = new RequestProcessor("Variables expand test processor", 1);
    private final ContextProvider lookupProvider;
    private VariablesTreeModelFilter vtmf;

    public VariablesFormatterFilter(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        //debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }

    private VariablesFormatter[] getFormatters() {
        Formatters formatters = Formatters.getDefault();
        if (formattersChangeListener == null) {
            formattersChangeListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (Formatters.PROP_FORMATTERS.equals(evt.getPropertyName())) {
                        synchronized (formattersLock) {
                            formattersWithExpandTestCode = null;
                            childrenExpandTest.clear();
                        }
                    }
                }
            };
            formatters.addPropertyChangeListener(WeakListeners.propertyChange(formattersChangeListener, formatters));
        }
        return formatters.getFormatters();
        /*
        synchronized (formattersLock) {
            if (formatters == null) {
                formattersChangeListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("VariableFormatters".equals(evt.getPropertyName())) {
                            synchronized (formattersLock) {
                                formatters = null;
                                formattersWithExpandTestCode = null;
                                childrenExpandTest.clear();
                            }
                        }
                    }
                };
                jpdaProperties = Properties.getDefault().getProperties("debugger.options.JPDA");
                jpdaProperties.addPropertyChangeListener(WeakListeners.propertyChange(formattersChangeListener, jpdaProperties));
                formatters = VariablesFormatter.loadFormatters();
            }
            return formatters;
        }
        */
    }

    private VariablesFormatter[] getFormattersWithExpandTestCode() {
        synchronized (formattersLock) {
            if (formattersWithExpandTestCode == null) {
                VariablesFormatter[] formatters = getFormatters();
                ArrayList<VariablesFormatter> formattersWithExpandTestCodeList = new ArrayList<VariablesFormatter>();
                for (VariablesFormatter vf : formatters) {
                    String expandTestCode = vf.getChildrenExpandTestCode();
                    if (expandTestCode != null && expandTestCode.length() > 0) {
                        formattersWithExpandTestCodeList.add(vf);
                    }
                }
                formattersWithExpandTestCode = (VariablesFormatter[]) formattersWithExpandTestCodeList.toArray(new VariablesFormatter[]{});
            }
            return formattersWithExpandTestCode;
        }
    }
    
    public String[] getSupportedTypes () {
        VariablesFormatter[] formatters = getFormatters();
        List<String> types = new ArrayList<String>();
        for (int i = 0; i < formatters.length; i++) {
            String[] ts = formatters[i].getClassTypes();
            for (String t : ts) {
                types.add(t);
            }
        }
        return types.toArray(new String[] {});
    }
    
    public String[] getSupportedAncestors () {
        VariablesFormatter[] formatters = getFormatters();
        List<String> types = new ArrayList<String>();
        for (int i = 0; i < formatters.length; i++) {
            if (formatters[i].isIncludeSubTypes()) {
                String[] ts = formatters[i].getClassTypes();
                for (String t : ts) {
                    types.add(t);
                }
            }
        }
        return types.toArray(new String[] {});
    }

    /** 
     * Returns filtered children for given parent on given indexes.
     *
     * @param   original the original tree model
     * @throws  NoInformationException if the set of children can not be
     *          resolved
     * @throws  ComputingException if the children resolving process 
     *          is time consuming, and will be performed off-line 
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  children for given parent on given indexes
     */
    @Override
    public Object[] getChildren (
        TreeModel original, 
        Variable variable, 
        int from, 
        int to
    ) throws UnknownTypeException {

        Object[] children;
        if (!(variable instanceof ObjectVariable)) {
            children = original.getChildren (variable, from, to);
        } else {
            children = getChildren(original, variable, from, to,
                           new FormattersLoopControl());
        }
        return children;
    }

    private Object[] getChildren (
        TreeModel original,
        Variable variable,
        int from,
        int to,
        FormattersLoopControl formatters
    ) throws UnknownTypeException {

        if (variable instanceof ObjectVariable) {
            ObjectVariable ov = (ObjectVariable) variable;

            synchronized (childrenExpandTestProcessing) {
                while (childrenExpandTestProcessing.contains(ov)) {
                    try {
                        childrenExpandTestProcessing.wait();
                    } catch (InterruptedException ex) {
                        return new Object[] {};
                    }
                }
            }
            if (Boolean.TRUE.equals(childrenExpandTest.get(ov))) {
                // The variable should be a leaf in fact - do not ask for children!
                return new Object[] {};
            }

            JPDAClassType ct = ov.getClassType();

            if (ct == null) {
                return original.getChildren (variable, from, to);
            }

            VariablesFormatter f = Formatters.getFormatterForType(ct, formatters.getFormatters());
            String[] formattersInLoopRef = new String[] { null };
            if (f != null && formatters.canUse(f, ct.getName(), formattersInLoopRef)) {
                if (f.isUseChildrenVariables()) {
                    Map<String, String> chvs = f.getChildrenVariables();
                    Object[] ch = new Object[chvs.size()];
                    int i = 0;
                    for (String name : chvs.keySet()) {
                        try {
                            java.lang.reflect.Method evaluateMethod = ov.getClass().getMethod("evaluate", String.class);
                            evaluateMethod.setAccessible(true);
                            Object var = evaluateMethod.invoke(ov, chvs.get(name));
                            FORMATTED_CHILDREN_VARS.put(var, name);
                            ch[i++] = var;
                        } catch (java.lang.reflect.InvocationTargetException itex) {
                            Throwable t = itex.getTargetException();
                            if (!(t instanceof InvalidExpressionException) ){
                                Exceptions.printStackTrace(t);
                            }
                            return original.getChildren (variable, from, to);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                            return original.getChildren (variable, from, to);
                        }
                    }
                    return ch;
                } else {
                    String code = f.getChildrenFormatCode();
                    if (code != null && code.length() > 0) {
                        try {
                            java.lang.reflect.Method evaluateMethod = ov.getClass().getMethod("evaluate", String.class);
                            evaluateMethod.setAccessible(true);
                            Variable ret = (Variable) evaluateMethod.invoke(ov, code);
                            if (ret == null) {
                                return new Object[] {}; // No children for null values.
                            }
                            return getChildren(original, ret, from, to, formatters);
                        } catch (java.lang.reflect.InvocationTargetException itex) {
                            Throwable t = itex.getTargetException();
                            if (t instanceof InvalidExpressionException) {
                                return original.getChildren (variable, from, to);
                            } else {
                                Exceptions.printStackTrace(t);
                            }
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            } else if (formattersInLoopRef[0] != null) {
                printFormattersInLoopDetected(formattersInLoopRef[0]);
            }
        }
        
        return original.getChildren (variable, from, to);
    }

    /**
     * Returns number of filtered children for given variable.
     *
     * @param   original the original tree model
     * @param   variable a variable of returned fields
     *
     * @throws  NoInformationException if the set of children can not be
     *          resolved
     * @throws  ComputingException if the children resolving process
     *          is time consuming, and will be performed off-line
     * @throws  UnknownTypeException if this TreeModelFilter implementation is not
     *          able to resolve dchildren for given node type
     *
     * @return  number of filtered children for given variable
     */
    @Override
    public int getChildrenCount (TreeModel original, Variable variable) 
    throws UnknownTypeException {
        
        return Integer.MAX_VALUE;
    }

    private void doExpandTest(final ObjectVariable ov, final JPDAClassType ct, final TreeModel original) {
        synchronized (childrenExpandTestProcessing) {
            childrenExpandTestProcessing.add(ov);
        }
        expandTestProcessor.post(new Runnable() {
            public void run() {
                boolean isLeaf = false;
                try {
                    VariablesFormatter f = Formatters.getFormatterForType(ct, getFormattersWithExpandTestCode());
                    if (f != null) {
                        String expandTestCode = f.getChildrenExpandTestCode();
                        if ("false".equals(expandTestCode)) {   // Optimalization for constant
                            childrenExpandTest.put(ov, true);   // is leaf
                            isLeaf = true;
                        }
                        if ("true".equals(expandTestCode)) {   // Optimalization for constant
                            childrenExpandTest.put(ov, false);   // is not leaf
                        }
                        try {
                            java.lang.reflect.Method evaluateMethod = ov.getClass().getMethod("evaluate", String.class);
                            evaluateMethod.setAccessible(true);
                            Variable ret = (Variable) evaluateMethod.invoke(ov, expandTestCode);
                            if (ret != null) {
                                isLeaf = !"true".equals(ret.getValue());
                                childrenExpandTest.put(ov, isLeaf);
                                
                            }
                        } catch (java.lang.reflect.InvocationTargetException itex) {
                            Throwable t = itex.getTargetException();
                            if (t instanceof InvalidExpressionException) {
                                // Ignore, expression failed to evaluate.
                            } else {
                                Exceptions.printStackTrace(t);
                            }
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } finally {
                    synchronized (childrenExpandTestProcessing) {
                        childrenExpandTestProcessing.remove(ov);
                        childrenExpandTestProcessing.notifyAll();
                    }
                    if (isLeaf) {
                        fireLeafChange(original, ov);
                    }
                }
            }
        });
    }

    private void fireLeafChange(TreeModel original, Variable variable) {
        if (vtmf == null) {
            List<? extends TreeModelFilter> tmfs = lookupProvider.lookup("LocalsView", TreeModelFilter.class); // NOI18N
            for (TreeModelFilter tmf : tmfs) {
                if (tmf instanceof VariablesTreeModelFilter) {
                    vtmf = (VariablesTreeModelFilter) tmf;
                    break;
                }
            }
        }
        if (vtmf != null) {
            vtmf.fireChildrenChange(variable);
        }
    }

    /**
     * Returns true if node is leaf.
     * 
     * @param   original the original tree model
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve dchildren for given node type
     * @return  true if node is leaf
     */
    @Override
    public boolean isLeaf (TreeModel original, Variable variable) 
    throws UnknownTypeException {
        if (variable instanceof ObjectVariable) {
            ObjectVariable ov = (ObjectVariable) variable;
            JPDAClassType ct = ov.getClassType();

            if (ct == null) {
                return original.isLeaf (variable);
            }
            // We do the check for children expansion in a separate thread, it must not execute in AWT.
            Boolean leaf = childrenExpandTest.get(ov);
            if (leaf != null) {
                return leaf;
            } else {
                doExpandTest(ov, ct, original);
                return false; // Suppose that we're not leaf if expand test is not yet computed
            }
        }
        String type = variable.getType ();
        // PATCH for J2ME
        if ( isLeafType (type) 
        ) return true;
        return original.isLeaf (variable);
    }
    
    @Override
    public Object getValueAt (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws UnknownTypeException {

        if (!(variable instanceof ObjectVariable)) {
            return original.getValueAt (variable, columnID);
        }
        try {
            Object val = getValueAt(original, variable, columnID,
                                    new FormattersLoopControl());
            VariablesTableModel.setErrorValueMsg(variable, null);
            VariablesTableModel.setErrorToStringMsg(variable, null);
            return val;
        } catch (InvalidExpressionException iex) {
            String errorMsg = VariablesTableModel.getMessage(iex);
            VariablesTableModel.setErrorValueMsg(variable, errorMsg);
            VariablesTableModel.setErrorToStringMsg(variable, errorMsg);
            return errorMsg;
        }
    }
    
    private Object getValueAt (
        TableModel original,
        Variable variable,
        String columnID,
        FormattersLoopControl formatters
    ) throws UnknownTypeException, InvalidExpressionException {
        if (!(variable instanceof ObjectVariable)) {
            return original.getValueAt (variable, columnID);
        }
        String type = variable.getType ();
        ObjectVariable ov = (ObjectVariable) variable;
        JPDAClassType ct = ov.getClassType();
        if (ct == null) {
            return original.getValueAt (variable, columnID);
        }
        VariablesFormatter f = Formatters.getFormatterForType(ct, formatters.getFormatters());
        String[] formattersInLoopRef = new String[] { null };
        if (f != null && formatters.canUse(f, ct.getName(), formattersInLoopRef) &&
            ( columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
              columnID == Constants.WATCH_VALUE_COLUMN_ID ||
              columnID == Constants.LOCALS_TO_STRING_COLUMN_ID ||
              columnID == Constants.WATCH_TO_STRING_COLUMN_ID)) {
            String code = f.getValueFormatCode();
            if (code != null && code.length() > 0) {
                try {
                    java.lang.reflect.Method evaluateMethod = ov.getClass().getMethod("evaluate", String.class);
                    evaluateMethod.setAccessible(true);
                    Variable ret = (Variable) evaluateMethod.invoke(ov, code);
                    if (ret == null) {
                        return null;
                    }
                    return getValueAt(original, ret, columnID, formatters);
                } catch (java.lang.reflect.InvocationTargetException itex) {
                    Throwable t = itex.getTargetException();
                    if (t instanceof InvalidExpressionException) {
                        throw (InvalidExpressionException) t;
                    } else {
                        Exceptions.printStackTrace(t);
                    }
                } catch (NoSuchMethodException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (SecurityException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (UnknownTypeException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        } else if (formattersInLoopRef[0] != null) {
            printFormattersInLoopDetected(formattersInLoopRef[0]);
        }
        if ( isToStringValueType (type) &&
             ( columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
               columnID == Constants.WATCH_VALUE_COLUMN_ID)
        ) {
            try {
                return "\""+ov.getToStringValue ()+"\"";
            } catch (InvalidExpressionException ex) {
                // Not a supported operation (e.g. J2ME, see #45543)
                // Or missing context or any other reason
                Logger.getLogger(VariablesFormatterFilter.class.getName()).fine("getToStringValue() "+ex.getLocalizedMessage());
                if ( (ex.getTargetException () != null) &&
                     (ex.getTargetException () instanceof 
                       UnsupportedOperationException)
                ) {
                    // PATCH for J2ME. see 45543
                    return original.getValueAt (variable, columnID);
                }
                return ex.getLocalizedMessage ();
            }
        }
        return original.getValueAt (variable, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Variable variable,
                           String columnID, Object value) throws UnknownTypeException {
        String type = variable.getType();
        if (isToStringValueType(type) &&
            (columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
             columnID == Constants.WATCH_VALUE_COLUMN_ID)) {
            String expression = (String) value;
            if (expression.startsWith("\"") && expression.endsWith("\"") && expression.length() > 1) {
                // Create a new StringBuffer object with the desired content:
                expression = "new " + type + "(\"" + convertToStringInitializer(expression.substring(1, expression.length() - 1)) + "\")";
                original.setValueAt(variable, columnID, expression);
                return ;
            }
        }
        original.setValueAt(variable, columnID, value);
    }
    
    private static String convertToStringInitializer (String s) {
        StringBuffer sb = new StringBuffer ();
        int i, k = s.length ();
        for (i = 0; i < k; i++)
            switch (s.charAt (i)) {
                case '\b':
                    sb.append ("\\b");
                    break;
                case '\f':
                    sb.append ("\\f");
                    break;
                case '\\':
                    sb.append ("\\\\");
                    break;
                case '\t':
                    sb.append ("\\t");
                    break;
                case '\r':
                    sb.append ("\\r");
                    break;
                case '\n':
                    sb.append ("\\n");
                    break;
                case '\"':
                    sb.append ("\\\"");
                    break;
                default:
                    sb.append (s.charAt (i));
            }
        return sb.toString();
    }
    
    
    // other methods ...........................................................
    
    private static HashSet leafType;
    private static boolean isLeafType (String type) {
        if (leafType == null) {
            leafType = new HashSet ();
            leafType.add ("java.lang.String");
            leafType.add ("java.lang.Character");
            leafType.add ("java.lang.Integer");
            leafType.add ("java.lang.Float");
            leafType.add ("java.lang.Byte");
            leafType.add ("java.lang.Boolean");
            leafType.add ("java.lang.Double");
            leafType.add ("java.lang.Long");
            leafType.add ("java.lang.Short");
        }
        return leafType.contains (type);
    }
    
    private static HashSet toStringValueType;
    static boolean isToStringValueType (String type) {
        if (toStringValueType == null) {
            toStringValueType = new HashSet ();
            toStringValueType.add ("java.lang.StringBuffer");
            toStringValueType.add ("java.lang.StringBuilder");
        }
        return toStringValueType.contains (type);
    }
    
    private void printFormattersInLoopDetected(String formattersInLoop) {
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class);
        if (debugger != null) {
            if (!formattersLoopWarned) {
                formattersLoopWarned = true;
                debugger.getConsoleIO().println(
                    NbBundle.getMessage(VariablesFormatterFilter.class,
                                        "MSG_LoopInTypeFormattingIntroErrorMessage"),
                    null, true);
            }
            debugger.getConsoleIO().println(
                    NbBundle.getMessage(VariablesFormatterFilter.class,
                                        "MSG_LoopInTypeFormatting",
                                        formattersInLoop),
                    null, false);
            
        }
    }

}
