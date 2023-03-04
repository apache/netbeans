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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.debug.DebugStackFrame;
import com.oracle.truffle.api.debug.DebugValue;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.source.SourceSection;

import java.util.Collection;
import java.util.List;

/**
 * A guest language object.
 */
public final class GuestObject {
    
    static final int DISPLAY_TRIM = 1000;

    final DebugValue value;
    final String name;
    final String language;
    final String type;
    final String displayValue;
    final boolean readable;
    final boolean writable;
    final boolean internal;
    final boolean leaf;
    final boolean isArray;
    final Collection<DebugValue> properties;
    final List<DebugValue> array;
    final SourcePosition valueSourcePosition;
    final SourcePosition typeSourcePosition;

    GuestObject(DebugValue value) {
        LanguageInfo originalLanguage = value.getOriginalLanguage();
        // Setup the object with a language-specific value
        if (originalLanguage != null) {
            value = value.asInLanguage(originalLanguage);
            this.language = originalLanguage.getId() + " " + originalLanguage.getName();
        } else {
            this.language = "";
        }
        this.value = value;
        this.name = value.getName();
        //System.err.println("new GuestObject("+name+")");
        DebugValue metaObject = null;
        try {
            metaObject = value.getMetaObject();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable ex) {
            LangErrors.exception("Value "+name+" .getMetaObject()", ex);
        }
        String typeStr = "";
        try {
            // New in GraalVM 20.1.0
            typeStr = (String) DebugValue.class.getMethod("getMetaSimpleName").invoke(value);
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException exc) {
            if (metaObject != null) {
                try {
                    typeStr = metaObject.as(String.class);
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable ex) {
                    LangErrors.exception("Meta object of "+name+" .as(String.class)", ex);
                }
            }
        }
        this.type = typeStr;
        //this.object = value;
        String valueStr = null;
        try {
            try {
                // New in GraalVM 20.1.0
                valueStr = (String) DebugValue.class.getMethod("toDisplayString").invoke(value);
            } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException exc) {
                valueStr = value.as(String.class);
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable ex) {
            LangErrors.exception("Value "+name+" .toDisplayString", ex);
        }
        this.displayValue = valueStr;
        //System.err.println("  have display value "+valueStr);
        this.readable = value.isReadable();
        this.writable = value.isWritable();
        this.internal = value.isInternal();
        Collection<DebugValue> valueProperties;
        try {
            valueProperties = value.getProperties();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable ex) {
            LangErrors.exception("Value "+name+" .getProperties()", ex);
            valueProperties = null;
        }
        this.properties = valueProperties;
        //System.err.println("  have properties");
        this.leaf = properties == null || properties.isEmpty();
        boolean valueIsArray;
        try {
            valueIsArray = value.isArray();
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable ex) {
            LangErrors.exception("Value "+name+" .isArray()", ex);
            valueIsArray = false;
        }
        this.isArray = valueIsArray;
        if (isArray) {
            List<DebugValue> valueArray;
            try {
                valueArray = value.getArray();
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable ex) {
                LangErrors.exception("Value "+name+" .getArray()", ex);
                valueArray = null;
            }
            this.array = valueArray;
        } else {
            this.array = null;
        }
        SourcePosition sp = null;
        try {
            SourceSection sourceLocation = value.getSourceLocation();
            //System.err.println("\nSOURCE of "+value.getName()+" is: "+sourceLocation);
            if (sourceLocation != null && sourceLocation.isAvailable()) {
                sp = new SourcePosition(sourceLocation, value.getOriginalLanguage());
            }
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable ex) {
            LangErrors.exception("Value "+name+" .getSourceLocation()", ex);
        }
        this.valueSourcePosition = sp;
        sp = null;
        if (metaObject != null) {
            try {
                SourceSection sourceLocation = metaObject.getSourceLocation();
                //System.err.println("\nSOURCE of metaobject "+metaObject+" is: "+sourceLocation);
                if (sourceLocation != null && sourceLocation.isAvailable()) {
                    sp = new SourcePosition(sourceLocation, value.getOriginalLanguage());
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable ex) {
                LangErrors.exception("Meta object of "+name+" .getSourceLocation()", ex);
            }
        }
        this.typeSourcePosition = sp;
        /*try {
            System.err.println("new GuestObject("+name+") displayValue = "+displayValue+", leaf = "+leaf+", properties = "+properties);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public GuestObject setValue(DebugStackFrame frame, String newExpression) {
        DebugValue newValue;
        try {
            newValue = frame.eval(newExpression);
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            LangErrors.exception("Evaluation of '"+newExpression+"'", t);
            return null;
        }
        try {
            value.set(newValue);
            return new GuestObject(value);
        } catch (ThreadDeath td) {
            throw td;
        } catch (Throwable t) {
            LangErrors.exception("Set of a value created from '"+newExpression+"'", t);
            return null;
        }
    }

    public GuestObject[] getProperties() {
        if (properties == null) {
            return new GuestObject[]{};
        }
        int n = 0;
        try {
            n = properties.size();
        } catch (Exception ex) {
            LangErrors.exception("Value "+name+" properties.size()", ex);
        }
        GuestObject[] children = new GuestObject[n];
        if (n == 0) {
            return children;
        }
        int i = 0;
        for (DebugValue ch : properties) {
            children[i++] = new GuestObject(ch);
        }
        return children;
    }

    public int getArraySize() {
        return (array != null) ? array.size() : 0;
    }

    public GuestObject[] getArrayElements() {
        int n = getArraySize();
        GuestObject[] elements = new GuestObject[n];
        if (n == 0) {
            return elements;
        }
        int i = 0;
        for (DebugValue elm : array) {
            elements[i++] = new GuestObject(elm);
        }
        return elements;
    }

    @Override
    public String toString() {
        return name + " = " + displayValue;
    }

}
