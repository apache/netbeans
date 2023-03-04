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

package org.netbeans.modules.debugger.jpda.truffle.vars.impl;

import java.io.InvalidObjectException;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.models.AbstractVariable;
import org.netbeans.modules.debugger.jpda.truffle.LanguageName;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.openide.util.Exceptions;

public class TruffleVariableImpl implements TruffleVariable {
    
    private static final String GUEST_OBJECT_TYPE = "org.netbeans.modules.debugger.jpda.backend.truffle.GuestObject";   // NOI18N
    private static final String FIELD_NAME = "name";                            // NOI18N
    private static final String FIELD_LANGUAGE = "language";                    // NOI18N
    private static final String FIELD_TYPE = "type";                            // NOI18N
    private static final String FIELD_READABLE = "readable";                    // NOI18N
    private static final String FIELD_WRITABLE = "writable";                    // NOI18N
    private static final String FIELD_INTERNAL = "internal";                    // NOI18N
    private static final String FIELD_LEAF = "leaf";                            // NOI18N
    private static final String FIELD_DISPLAY_VALUE = "displayValue";           // NOI18N
    private static final String METHOD_GET_CHILDREN = "getProperties";          // NOI18N
    private static final String METHOD_GET_CHILDREN_SIG = "()[Lorg/netbeans/modules/debugger/jpda/backend/truffle/GuestObject;";  // NOI18N
    private static final String METHOD_SET_VALUE = "setValue";                  // NOI18N
    private static final String METHOD_SET_VALUE_SIG = "(Lcom/oracle/truffle/api/debug/DebugStackFrame;Ljava/lang/String;)Lorg/netbeans/modules/debugger/jpda/backend/truffle/GuestObject;";  // NOI18N
    private static final String FIELD_VALUE_SOURCE = "valueSourcePosition";     // NOI18N
    private static final String FIELD_TYPE_SOURCE = "typeSourcePosition";       // NOI18N
    
    private final ObjectVariable guestObject;
    private final String name;
    private final LanguageName language;
    private final String type;
    private final String displayValue;
    private final boolean readable;
    private final boolean writable;
    private final boolean internal;
    private final boolean leaf;
    private final boolean hasValueSource;
    private final boolean hasTypeSource;
    private SourcePosition valueSource;
    private SourcePosition typeSource;
    
    private TruffleVariableImpl(ObjectVariable guestObject, String name,
                                LanguageName language, String type, String displayValue,
                                boolean readable, boolean writable, boolean internal,
                                boolean hasValueSource, boolean hasTypeSource,
                                boolean leaf) {
        this.guestObject = guestObject;
        this.name = name;
        this.language = language;
        this.type = type;
        this.displayValue = displayValue;
        this.readable = readable;
        this.writable = writable;
        this.internal = internal;
        this.hasValueSource = hasValueSource;
        this.hasTypeSource = hasTypeSource;
        this.leaf = leaf;
    }

    public static TruffleVariableImpl get(Variable var) {
        if (GUEST_OBJECT_TYPE.equals(var.getType())) {
            ObjectVariable truffleObj = (ObjectVariable) var;
            //System.err.println("TruffleVariableImpl.get("+var+") fields on "+truffleObj+", class "+truffleObj.getClassType().getName());
            // The inner value can change in watches.
            Field f = truffleObj.getField(FIELD_NAME);
            if (f == null) {
                return null;
            }
            String name = (String) f.createMirrorObject();

            f = truffleObj.getField(FIELD_LANGUAGE);
            if (f == null) {
                return null;
            }
            LanguageName language = LanguageName.parse((String) f.createMirrorObject());

            f = truffleObj.getField(FIELD_TYPE);
            if (f == null) {
                return null;
            }
            String type = (String) f.createMirrorObject();

            f = truffleObj.getField(FIELD_DISPLAY_VALUE);
            if (f == null) {
                return null;
            }
            String dispVal = (String) f.createMirrorObject();

            f = truffleObj.getField(FIELD_READABLE);
            boolean readable;
            if (f == null) {
                readable = true;
            } else {
                readable = (Boolean) f.createMirrorObject();
            }

            f = truffleObj.getField(FIELD_WRITABLE);
            boolean writable;
            if (f == null) {
                writable = true;
            } else {
                writable = (Boolean) f.createMirrorObject();
            }

            f = truffleObj.getField(FIELD_INTERNAL);
            boolean internal;
            if (f == null) {
                internal = true;
            } else {
                internal = (Boolean) f.createMirrorObject();
            }

            f = truffleObj.getField(FIELD_VALUE_SOURCE);
            boolean hasValueSource = f != null && ((ObjectVariable) f).getUniqueID() != 0L;
            f = truffleObj.getField(FIELD_TYPE_SOURCE);
            boolean hasTypeSource = f != null && ((ObjectVariable) f).getUniqueID() != 0L;

            boolean leaf = isLeaf(truffleObj);
            return new TruffleVariableImpl(truffleObj, name, language, type, dispVal, readable, writable, internal, hasValueSource, hasTypeSource, leaf);
        } else {
            return null;
        }
    }

    static boolean isLeaf(ObjectVariable truffleObj) {
        Field f = truffleObj.getField(FIELD_LEAF);
        if (f == null) {
            return false;
        }
        Boolean mirrorLeaf = (Boolean) f.createMirrorObject();
        boolean leaf;
        if (mirrorLeaf == null) {
            leaf = false;
        } else {
            leaf = mirrorLeaf;
        }
        return leaf;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public LanguageName getLanguage() {
        return language;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isReadable() {
        return readable;
    }

    @Override
    public boolean isWritable() {
        return writable;
    }

    @Override
    public boolean isInternal() {
        return internal;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public Object getValue() {
        return displayValue; // TODO
    }

    @Override
    public ObjectVariable setValue(JPDADebugger debugger, String newExpression) {
        if (this.displayValue.equals(newExpression)) {
            return null;
        }
        return setValue(debugger, guestObject, newExpression);
    }

    static ObjectVariable setValue(JPDADebugger debugger, ObjectVariable guestObject, String newExpression) {
        CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger.getCurrentThread());
        TruffleStackFrame selectedStackFrame;
        if (currentPCInfo != null && (selectedStackFrame = currentPCInfo.getSelectedStackFrame()) != null) {
            ObjectVariable selectedFrame = selectedStackFrame.getStackFrameInstance();
            try {
                Variable retVar = guestObject.invokeMethod(METHOD_SET_VALUE, METHOD_SET_VALUE_SIG,
                                                             new Variable[] { selectedFrame, debugger.createMirrorVar(newExpression) });
                if (retVar instanceof ObjectVariable) {
                    return (ObjectVariable) retVar;
                }
            } catch (NoSuchMethodException | InvalidExpressionException | InvalidObjectException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    public boolean hasValueSource() {
        return hasValueSource;
    }

    @Override
    public synchronized SourcePosition getValueSource() {
        if (valueSource == null) {
            Field f = guestObject.getField(FIELD_VALUE_SOURCE);
            if (f == null) {
                return null;
            }
            valueSource = TruffleAccess.getSourcePosition(((AbstractVariable) f).getDebugger(), (ObjectVariable) f);
        }
        return valueSource;
    }

    @Override
    public boolean hasTypeSource() {
        return hasTypeSource;
    }

    @Override
    public synchronized SourcePosition getTypeSource() {
        if (typeSource == null) {
            Field f = guestObject.getField(FIELD_TYPE_SOURCE);
            if (f == null) {
                return null;
            }
            typeSource = TruffleAccess.getSourcePosition(((AbstractVariable) f).getDebugger(), (ObjectVariable) f);
        }
        return typeSource;
    }

    @Override
    public boolean isLeaf() {
        return leaf;
    }
    
    @Override
    public Object[] getChildren() {
        return getChildren(guestObject);
    }

    static Object[] getChildren(ObjectVariable guestObject) {
        try {
            Variable children = guestObject.invokeMethod(METHOD_GET_CHILDREN, METHOD_GET_CHILDREN_SIG, new Variable[] {});
            if (children instanceof ObjectVariable) {
                Field[] fields = ((ObjectVariable) children).getFields(0, Integer.MAX_VALUE);
                int n = fields.length;
                Object[] ch = new Object[n];
                for (int i = 0; i < n; i++) {
                    TruffleVariableImpl tv = get(fields[i]);
                    if (tv != null) {
                        ch[i] = tv;
                    } else {
                        ch[i] = fields[i].createMirrorObject();
                    }
                }
                return ch;
            }
        } catch (NoSuchMethodException | InvalidExpressionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new Object[] {};
    }

}
