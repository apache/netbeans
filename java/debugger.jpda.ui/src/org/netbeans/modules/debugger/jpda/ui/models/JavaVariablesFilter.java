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

import java.util.HashSet;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.jpda.VariablesFilterAdapter;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;


/**
 *
 * @author   Jan Jancura
 */
public class JavaVariablesFilter extends VariablesFilterAdapter {
    
    public String[] getSupportedTypes () {
        return new String[] {
            "java.lang.String",
            "java.lang.StringBuffer",
            "java.lang.StringBuilder",
            
            "java.lang.Character",
            "java.lang.Integer",
            "java.lang.Float",
            "java.lang.Byte",
            "java.lang.Boolean",
            "java.lang.Double",
            "java.lang.Long",
            "java.lang.Short",
            
            "java.lang.ref.WeakReference",
            "java.lang.ref.SoftReference",
            "java.lang.ref.PhantomReference",
            
            "java.util.ArrayList",
            "java.util.HashSet",
            "java.util.LinkedHashSet",
            "java.util.LinkedList",
            "java.util.Stack",
            "java.util.TreeSet",
            "java.util.Vector",
            "java.util.Hashtable",
            "java.util.Hashtable$Entry",
            "java.util.HashMap",
            "java.util.HashMap$Entry",
            "java.util.IdentityHashMap",
            "java.util.AbstractMap$SimpleEntry",
            "java.util.TreeMap",
            "java.util.TreeMap$Entry",
            "java.util.WeakHashMap",
            "java.util.LinkedHashMap",
            "java.util.LinkedHashMap$Entry",
            
            "java.beans.PropertyChangeSupport"
        };
    }
    
    public String[] getSupportedAncestors () {
        return new String[] {
        };
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

        if (variable instanceof ObjectVariable) {
            ObjectVariable ov = (ObjectVariable) variable;
            JPDAClassType ct = ov.getClassType();

            if (ct == null) {
                return original.getChildren (variable, from, to);
            }

            if (isToArrayType (ct)) {
                try {
                    ov = (ObjectVariable) ov.invokeMethod (
                        "toArray",
                        "()[Ljava/lang/Object;",
                        new Variable [0]
                    );
                    if (ov == null) {
                        return new Object[] {};
                    }
                    return original.getChildren(ov, from, to);
                } catch (NoSuchMethodException e) {
                    Field elementData = ov.getField("elementData");
                    if (elementData != null) {
                        return original.getChildren(elementData, from, to);
                    }
                } catch (InvalidExpressionException e) {
                    // Not a supported operation (e.g. J2ME, see #45543)
                    // Or missing context or any other reason
                    Logger.getLogger(JavaVariablesFilter.class.getName()).fine("invokeMethod(toArray) "+e.getLocalizedMessage());
                    return original.getChildren (variable, from, to);
                }
            }

            if (isMapMapType (ct)) {
                try {
                    ov = (ObjectVariable) ov.invokeMethod (
                        "entrySet",
                        "()Ljava/util/Set;",
                        new Variable [0]
                    );
                    if (ov != null) {
                        ov = (ObjectVariable) ov.invokeMethod (
                            "toArray",
                            "()[Ljava/lang/Object;",
                            new Variable [0]
                        );
                        if (ov != null) {
                            return original.getChildren(ov, from, to);
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // Nothing to do. Return the original children.
                } catch (InvalidExpressionException e) {
                    // Not a supported operation (e.g. J2ME, see #45543)
                    // Or missing context or any other reason
                    Logger.getLogger(JavaVariablesFilter.class.getName()).fine("invokeMethod(entrySet) "+e.getLocalizedMessage());
                }
            }

            if (isMapEntryType (ct)) {
                Field[] fs = new Field [2];
                // Read fields from AbstractMap.SimpleEntry
                fs [0] = ov.getField ("key");
                fs [1] = ov.getField ("value");
                if (fs[0] != null && fs[1] != null) {
                    return fs;
                }
                // Otherwise some mistake in retrieval
            }

            if (isInstanceOf(ct, "java.lang.ref.Reference")) {
                Field[] fs = new Field [2];
                fs [0] = ov.getField ("referent");
                fs [1] = ov.getField("queue");
                if (fs[0] != null && fs[1] != null) {
                    return fs;
                }
                // Otherwise some mistake in retrieval
            }

            String type = ct.getName();
            if ("java.beans.PropertyChangeSupport".equals(type)) {
                try {
                    return ((ObjectVariable) ov.invokeMethod (
                        "getPropertyChangeListeners",
                        "()[Ljava/beans/PropertyChangeListener;",
                        new Variable [0]
                    )).getFields (from, to);
                } catch (InvalidExpressionException e) {
                    // Not a supported operation (e.g. J2ME, see #45543)
                    // Or missing context or any other reason
                    Logger.getLogger(JavaVariablesFilter.class.getName()).fine("invokeMethod(getPropertyChangeListeners) "+e.getLocalizedMessage());
                } catch (NoSuchMethodException e) {
                    Logger.getLogger(JavaVariablesFilter.class.getName()).fine("invokeMethod(getPropertyChangeListeners) "+e.getLocalizedMessage());
                }
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
        String type = variable.getType ();
        ObjectVariable ov = (ObjectVariable) variable;
        if ( isMapEntryType (ov.getClassType()) &&
             ( columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
               columnID == Constants.WATCH_VALUE_COLUMN_ID)
        ) {
            return ov.getField ("key").getValue () + "=>" + 
                   ov.getField ("value").getValue ();
        }
        if ( isGetValueType (type) &&
             ( columnID == Constants.LOCALS_VALUE_COLUMN_ID ||
               columnID == Constants.WATCH_VALUE_COLUMN_ID)
        ) {
            return ov.getField ("value").getValue ();
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
                Logger.getLogger(JavaVariablesFilter.class.getName()).fine("getToStringValue() "+ex.getLocalizedMessage());
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
    
    private static HashSet getValueType;
    private static boolean isGetValueType (String type) {
        if (getValueType == null) {
            getValueType = new HashSet ();
            getValueType.add ("java.lang.Character");
            getValueType.add ("java.lang.Integer");
            getValueType.add ("java.lang.Float");
            getValueType.add ("java.lang.Byte");
            getValueType.add ("java.lang.Boolean");
            getValueType.add ("java.lang.Double");
            getValueType.add ("java.lang.Long");
            getValueType.add ("java.lang.Short");
        }
        return getValueType.contains (type);
    }
    
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
    private static boolean isToStringValueType (String type) {
        if (toStringValueType == null) {
            toStringValueType = new HashSet ();
            toStringValueType.add ("java.lang.StringBuffer");
            toStringValueType.add ("java.lang.StringBuilder");
        }
        return toStringValueType.contains (type);
    }
    
    private static boolean isToArrayType(JPDAClassType ct) {
        // Instanceof Collection
        return isInstanceOf(ct, "java.util.Collection");
    }

    private static boolean isMapMapType (JPDAClassType ct) {
        // Instanceof Map
        return isInstanceOf(ct, "java.util.Map");
    }

    private static boolean isMapEntryType (JPDAClassType ct) {
        // Instanceof Map.Entry
        return isInstanceOf(ct, "java.util.Map$Entry");
    }

    private static boolean isInstanceOf(JPDAClassType ct, String className) {
        if (ct == null) return false;
        try {
            java.lang.reflect.Method isInstanceOfMethod = ct.getClass().getMethod("isInstanceOf", String.class);
            return (Boolean) isInstanceOfMethod.invoke(ct, className);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

}
