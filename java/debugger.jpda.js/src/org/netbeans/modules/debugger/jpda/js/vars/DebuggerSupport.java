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

package org.netbeans.modules.debugger.jpda.js.vars;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import java.io.InvalidObjectException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.modules.debugger.jpda.js.source.Source;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public final class DebuggerSupport {
    
    public  static final String DEBUGGER_SUPPORT_CLASS = "jdk.nashorn.internal.runtime.DebuggerSupport";    // NOI18N
    private static final String DEBUGGER_SUPPORT_VALUE_DESC_CLASS = "jdk.nashorn.internal.runtime.DebuggerSupport$DebuggerValueDesc"; // NOI18N
    private static final String CONTEXT_CLASS = "jdk.nashorn.internal.runtime.Context";    // NOI18N
    
    private static final String METHOD_VALUE_INFO  = "valueInfo";       // NOI18N
    private static final String SIGNAT_VALUE_INFO  = "(Ljava/lang/String;Ljava/lang/Object;Z)Ljdk/nashorn/internal/runtime/DebuggerSupport$DebuggerValueDesc;"; // NOI18N
    private static final String METHOD_VALUE_INFOS = "valueInfos";      // NOI18N
    private static final String SIGNAT_VALUE_INFOS = "(Ljava/lang/Object;Z)[Ljdk/nashorn/internal/runtime/DebuggerSupport$DebuggerValueDesc;";  // NOI18N
    private static final String METHOD_EVAL        = "eval";            // NOI18N
    private static final String SIGNAT_EVAL        = "(Ljdk/nashorn/internal/runtime/ScriptObject;Ljava/lang/Object;Ljava/lang/String;Z)Ljava/lang/Object;";    // NOI18N
    private static final String METHOD_FROM_CLASS  = "fromClass";       // NOI18N
    private static final String SIGNAT_FROM_CLASS  = "(Ljava/lang/Class;)Ljdk/nashorn/internal/runtime/Context;";   // NOI18N
    private static final String METHOD_CONTEXT_EVAL= "eval";            // NOI18N
    private static final String SIGNAT_CONTEXT_EVAL= "(Ljdk/nashorn/internal/runtime/ScriptObject;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";  // NOI18N
    private static final String SIGNAT_CONTEXT_EVAL_OLD = "(Ljdk/nashorn/internal/runtime/ScriptObject;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Z)Ljava/lang/Object;";  // NOI18N
    private static final String METHOD_VALUE_AS_STRING = "valueAsString";       // NOI18N
    private static final String SIGNAT_VALUE_AS_STRING = "(Ljava/lang/Object;)Ljava/lang/String;";  // NOI18N
    private static final String METHOD_SOURCE_INFO = "getSourceInfo";   // NOI18N
    private static final String SIGNAT_SOURCE_INFO = "(Ljava/lang/Class;)Ljdk/nashorn/internal/runtime/DebuggerSupport$SourceInfo;";    // NOI18N
    
    private static final String FIELD_DESC_VALUE_AS_STRING = "valueAsString";   // NOI18N
    private static final String FIELD_DESC_KEY             = "key";             // NOI18N
    private static final String FIELD_DESC_EXPANDABLE      = "expandable";      // NOI18N
    private static final String FIELD_DESC_VALUE_AS_OBJECT = "valueAsObject";   // NOI18N
    
//        this.descKeyField           = this.DebuggerValueDescClass.fieldByName("key");
//        this.descExpandableField    = this.DebuggerValueDescClass.fieldByName("expandable");
//        this.descValueAsObjectField = this.DebuggerValueDescClass.fieldByName("valueAsObject");
//        this.descValueAsStringField = this.DebuggerValueDescClass.fieldByName("valueAsString");
    
    private static final List<Reference<JPDADebugger>> hasOldEval = new CopyOnWriteArrayList<>();

    private DebuggerSupport() {}
    
    static Variable getValueInfoDesc(JPDADebugger debugger, String name, Variable value, boolean all) {
        List<JPDAClassType> supportClasses = debugger.getClassesByName(DEBUGGER_SUPPORT_CLASS);
        if (supportClasses.isEmpty()) {
            return null;
        }
        JPDAClassType supportClass = supportClasses.get(0);
        Variable[] args = new Variable[3];
        try {
            args[0] = debugger.createMirrorVar(name);
            args[1] = value;
            args[2] = debugger.createMirrorVar(all, true);
            return supportClass.invokeMethod(METHOD_VALUE_INFO, SIGNAT_VALUE_INFO, args);
        } catch (InvalidObjectException | InvalidExpressionException iex) {
            return null;
        } catch (NoSuchMethodException nsmex) {
            Exceptions.printStackTrace(nsmex);
            return null;
        }
    }
    
    private static String removeQuotes(String value) {
        if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }
    
    static String getDescriptionValue(Variable descVar) {
        Field valueAsStringField = ((ObjectVariable) descVar).getField(FIELD_DESC_VALUE_AS_STRING);
        return removeQuotes(valueAsStringField.getValue());
    }
    
    static Variable getDescriptionValueObject(Variable descVar) {
        return ((ObjectVariable) descVar).getField(FIELD_DESC_VALUE_AS_OBJECT);
    }
    
    static String getDescriptionKey(Variable descVar) {
        Field keyField = ((ObjectVariable) descVar).getField(FIELD_DESC_KEY);
        return removeQuotes(keyField.getValue());
    }
    
    static boolean isDescriptionExpandable(Variable descVar) {
        Field expandableField = ((ObjectVariable) descVar).getField(FIELD_DESC_EXPANDABLE);
        return "true".equals(expandableField.getValue());
    }
    
    static Variable[] getValueInfos(JPDADebugger debugger, Variable scope, boolean all) {
        List<JPDAClassType> supportClasses = debugger.getClassesByName(DEBUGGER_SUPPORT_CLASS);
        if (supportClasses.isEmpty()) {
            return null;
        }
        JPDAClassType supportClass = supportClasses.get(0);
        Variable[] args = new Variable[2];
        try {
            args[0] = scope;
            args[1] = debugger.createMirrorVar(all, true);
            ObjectVariable infosVar = (ObjectVariable) supportClass.invokeMethod(METHOD_VALUE_INFOS, SIGNAT_VALUE_INFOS, args);
            return infosVar.getFields(0, Integer.MAX_VALUE);
        } catch (InvalidObjectException ioex) {
            return null;
        } catch (NoSuchMethodException nsmex) {
            Exceptions.printStackTrace(nsmex);
            return null;
        } catch (InvalidExpressionException iex) {
            return null;
        }
    }
    
    public static boolean hasSourceInfo(JPDADebugger debugger) {
        List<JPDAClassType> supportClasses = debugger.getClassesByName(DEBUGGER_SUPPORT_CLASS);
        if (supportClasses.isEmpty()) {
            return false;
        }
        JPDAClassType supportClass = supportClasses.get(0);
        try {
            ReferenceType supportType = (ReferenceType) supportClass.getClass().getMethod("getType").invoke(supportClass);
            Method getSourceInfoMethod = ((ClassType) supportType).concreteMethodByName(METHOD_SOURCE_INFO, SIGNAT_SOURCE_INFO);
            return getSourceInfoMethod != null;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        } catch (VMDisconnectedException | ClassNotPreparedException ex) {
            return false;
        }
    }
    
    public static Variable getSourceInfo(JPDADebugger debugger, JPDAClassType classType) {
        List<JPDAClassType> supportClasses = debugger.getClassesByName(DEBUGGER_SUPPORT_CLASS);
        if (supportClasses.isEmpty()) {
            return null;
        }
        JPDAClassType supportClass = supportClasses.get(0);
        try {
            Variable sourceInfo = supportClass.invokeMethod(METHOD_SOURCE_INFO, SIGNAT_SOURCE_INFO, new Variable[] { classType.classObject() });
            return sourceInfo;
        } catch (NoSuchMethodException | InvalidExpressionException ex) {
            return null;
        }
    }
    
    public static Variable evaluate(JPDADebugger debugger, CallStackFrame frame, String expression) throws InvalidExpressionException {
        return evaluate(debugger, frame, expression, null);
    }
    
    public static Variable evaluate(JPDADebugger debugger, CallStackFrame frame, String expression, ObjectVariable contextVar) throws InvalidExpressionException {
        try {
            return doEvaluate(debugger, frame, expression, contextVar);
        } catch (InvalidExpressionException ieex) {
            Throwable targetException = ieex.getTargetException();
            if (targetException == null) {
                throw ieex;
            }
            String name;
            try {
                name = (String) targetException.getClass().getMethod("getOriginalLocalizedMessage").invoke(targetException);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                name = targetException.getLocalizedMessage();
            }
            throw new InvalidExpressionException(name, targetException, ieex.hasApplicationTarget());
        }
    }
    
    private static Variable doEvaluate(JPDADebugger debugger, CallStackFrame frame, String expression, ObjectVariable contextVar) throws InvalidExpressionException {
        List<JPDAClassType> supportClasses = debugger.getClassesByName(DEBUGGER_SUPPORT_CLASS);
        if (supportClasses.isEmpty()) {
            return null;
        }
        JPDAClassType supportClass = supportClasses.get(0);
        
        Variable scope = null;
        ObjectVariable thisVar = null;
        try {
            for (LocalVariable lv : frame.getLocalVariables()) {
                String name = lv.getName();
                switch (name) {
                    case JSUtils.VAR_SCOPE:
                        scope = lv;
                        break;
                    case JSUtils.VAR_THIS:
                        thisVar = (ObjectVariable) lv;
                        break;
                }
                if (name.equals(expression)) {
                    return lv;
                }
            }
        } catch (AbsentInformationException ex) {
        }
        if (contextVar == null) {
            contextVar = thisVar;
        }
        if (scope == null || contextVar == null) {
            throw new InvalidExpressionException("Missing scope");
            // Can not evaluate
        }
        
        // Evaluate on the current class' context:
        Source source = Source.getSource(frame);
        JPDAClassType sourceClassType = source.getClassType();
        // Call Context.fromClass(clazz):
        List<JPDAClassType> contextClassesByName = debugger.getClassesByName(CONTEXT_CLASS);
        JPDAClassType contextClass = contextClassesByName.get(0);
        Variable contextObj = null;
        try {
            contextObj = contextClass.invokeMethod(
                    METHOD_FROM_CLASS,
                    SIGNAT_FROM_CLASS,
                    new Variable[] { sourceClassType.classObject() });
        } catch (NoSuchMethodException nsmex) {
            Exceptions.printStackTrace(nsmex);
        }
        if (contextObj != null) {
            boolean oldEval = isOldEval(debugger);
            if (!oldEval) {
                try {
                    Variable[] args = new Variable[4];
                    try {
                        args[0] = scope;
                        args[1] = debugger.createMirrorVar(expression);
                        args[2] = contextVar;
                        args[3] = null; // ScriptRuntime.UNDEFINED, or null for directEval
                    } catch (InvalidObjectException ioex) {
                        Exceptions.printStackTrace(ioex);
                        throw new InvalidExpressionException(ioex);
                    }
                    return ((ObjectVariable) contextObj).invokeMethod(
                            METHOD_CONTEXT_EVAL,
                            SIGNAT_CONTEXT_EVAL,
                            args);
                } catch (NoSuchMethodException nsmex) {
                    oldEval = true;
                    setOldEval(debugger);
                }
            }
            if (oldEval) {
                // older than 1.8.0_60
                try {
                    Variable[] args = new Variable[5];
                    try {
                        args[0] = scope;
                        args[1] = debugger.createMirrorVar(expression);
                        args[2] = contextVar;
                        args[3] = null; // ScriptRuntime.UNDEFINED, or null for directEval
                        args[4] = debugger.createMirrorVar(false, true);
                    } catch (InvalidObjectException ioex) {
                        Exceptions.printStackTrace(ioex);
                        throw new InvalidExpressionException(ioex);
                    }
                    return ((ObjectVariable) contextObj).invokeMethod(
                            METHOD_CONTEXT_EVAL,
                            SIGNAT_CONTEXT_EVAL_OLD,
                            args);
                } catch (NoSuchMethodException nsmex) {
                    Exceptions.printStackTrace(nsmex);
                }
            }
        }
        // Fallback to global evaluation...
        Variable[] args = new Variable[4];
        try {
            args[0] = scope;
            args[1] = contextVar;
            args[2] = debugger.createMirrorVar(expression);
            args[3] = debugger.createMirrorVar(false, true);
            return supportClass.invokeMethod(METHOD_EVAL, SIGNAT_EVAL, args);
        } catch (InvalidObjectException ioex) {
            Exceptions.printStackTrace(ioex);
            throw new InvalidExpressionException(ioex);
        } catch (NoSuchMethodException nsmex) {
            Exceptions.printStackTrace(nsmex);
            return null;
        }
    }
    
    private static boolean isOldEval(JPDADebugger debugger) {
        for (Reference<JPDADebugger> dbgRef : hasOldEval) {
            JPDADebugger dbg = dbgRef.get();
            if (dbg == null) {
                hasOldEval.remove(dbgRef);
            } else if (dbg == debugger) {
                return true;
            }
        }
        return false;
    }
    
    private static void setOldEval(JPDADebugger debugger) {
        hasOldEval.add(new WeakReference<>(debugger));
    }
    
    public static String getVarValue(JPDADebugger debugger, Variable var) {
        if (var instanceof ObjectVariable) {
            ObjectVariable ov = (ObjectVariable) var;
            List<JPDAClassType> supportClasses = debugger.getClassesByName(DEBUGGER_SUPPORT_CLASS);
            JPDAClassType ct;
            if (supportClasses.isEmpty() ||
                ((ct = ov.getClassType()) != null && String.class.getCanonicalName().equals(ct.getName()))) {

                try {
                    return ov.getToStringValue();
                } catch (InvalidExpressionException ex) {
                }
            } else {
                JPDAClassType supportClass = supportClasses.get(0);
                Variable[] args = new Variable[1];
                try {
                    args[0] = var;
                    var = supportClass.invokeMethod(METHOD_VALUE_AS_STRING, SIGNAT_VALUE_AS_STRING, args);
                } catch (NoSuchMethodException ex) {
                    Exceptions.printStackTrace(ex);
                    try {
                        return ov.getToStringValue();
                    } catch (InvalidExpressionException iex) {
                    }
                } catch (InvalidExpressionException ex) {
                    try {
                        return ov.getToStringValue();
                    } catch (InvalidExpressionException iex) {
                    }
                }
            }
        }
        String str = var.getValue();
        if (str.length() > 2 && str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length() - 1);
        }
        return str;
    }
    
    public static Variable getVarStringValueAsVar(JPDADebugger debugger, ObjectVariable ov) {
        List<JPDAClassType> supportClasses = debugger.getClassesByName(DEBUGGER_SUPPORT_CLASS);
        if (supportClasses.isEmpty()) {
            return ov;
        }
        JPDAClassType supportClass = supportClasses.get(0);
        Variable[] args = new Variable[1];
        try {
            args[0] = ov;
            Variable strVar = supportClass.invokeMethod(METHOD_VALUE_AS_STRING, SIGNAT_VALUE_AS_STRING, args);
            // This method returns quoted value. :-(
            if (String.class.getName().equals(strVar.getType())) {
                return adjustQuotes(debugger, (ObjectVariable) strVar);
            } else {
                return strVar;
            }
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return ov;
        } catch (InvalidExpressionException iex) {
            return ov;
        }
    }
    
    private static Variable adjustQuotes(JPDADebugger debugger, ObjectVariable strVar) {
        String str = strVar.getValue();
        str = str.substring(1, str.length() - 1); // getValue() adds quotes
        if (str.length() > 2 && str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length() - 1);
            try {
                return debugger.createMirrorVar(str);
            } catch (InvalidObjectException ex) {
            }
        }
        return strVar;
    }
    
}
