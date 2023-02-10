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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.openide.util.Exceptions;

/**
 * A collector of shorted String values, that were too long.
 * For every shortened String one can find the original length
 * and the whole original content.
 * 
 * @author Martin Entlicher
 */
public final class ShortenedStrings {
    
    private static final Map<String, StringInfo> infoStrings = new WeakHashMap<String, StringInfo>();
    private static final Map<StringReference, StringValueInfo> stringsCache = new WeakHashMap<StringReference, StringValueInfo>();
    private static final Set<StringReference> retrievingStrings = new HashSet<StringReference>();
    private static final Map<VirtualMachine, Boolean> isLittleEndianCache =
            new WeakHashMap<>();

    static {
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_SESSIONS,
                                                                 new DebuggerManagerAdapter() {

            @Override
            public void sessionRemoved(Session session) {
                // Clean up. WeakHashMap does not clean up if not touched. :-(
                int n = DebuggerManager.getDebuggerManager().getSessions().length;
                if (n == 0) {
                    synchronized (infoStrings) {
                        infoStrings.clear();
                    }
                    synchronized (stringsCache) {
                        stringsCache.clear();
                        retrievingStrings.clear();
                    }
                    synchronized (isLittleEndianCache) {
                        isLittleEndianCache.clear();
                    }
                }
            }

                                                                 });
    }

    private ShortenedStrings() {}
    
    public static StringInfo getShortenedInfo(String s) {
        synchronized (infoStrings) {
            return infoStrings.get(s);
        }
    }

    private static boolean isLittleEndian(VirtualMachine virtualMachine) throws
            InvalidTypeException, IncompatibleThreadStateException,
            ClassNotLoadedException, InvocationException,
            InternalExceptionWrapper, VMDisconnectedExceptionWrapper,
            ObjectCollectedExceptionWrapper, ClassNotPreparedExceptionWrapper {
        synchronized(isLittleEndianCache){
            Boolean cached = isLittleEndianCache.get(virtualMachine);
            if (cached != null){
                return cached;
            }
            List<ReferenceType> possibleClasses = virtualMachine.classesByName(
                    "java.lang.StringUTF16");
            //If we don't know, we are going to assume little endian encoding.
            //This should work for most architectures (x86, arm, riscv), but
            //will result in bogus data on big endian architectures
            final boolean defaultValue = true;
            if (possibleClasses.isEmpty()){
                ClassType ct = (ClassType) virtualMachine.classesByName(
                        "java.lang.Class").iterator().next();
                Method m = ct.concreteMethodByName("forName",
                        "(Ljava/lang/String;)Ljava/lang/Class;");
                StringReference referenceString = virtualMachine.mirrorOf(
                        "java.lang.StringUTF16");
                ThreadReference threadReference = virtualMachine.
                        allThreads().get(0);
                ct.invokeMethod(threadReference, m, Collections.
                        singletonList(referenceString), 0);
                possibleClasses = virtualMachine.classesByName(
                        "java.lang.StringUTF16");
            }
            ReferenceType utf16;
            if (possibleClasses.size() == 1){
                utf16 = possibleClasses.get(0);
            }
            else {
                isLittleEndianCache.put(virtualMachine, defaultValue);
                return defaultValue;
            }
            Field hiByteShiftField = ReferenceTypeWrapper.fieldByName(utf16,
                        "HI_BYTE_SHIFT");
            if (hiByteShiftField == null){
                isLittleEndianCache.put(virtualMachine, defaultValue);
                return defaultValue;
            }
            Value hiByteShiftValue = utf16.getValue(hiByteShiftField);
            if (!(hiByteShiftValue instanceof PrimitiveValue)){
                isLittleEndianCache.put(virtualMachine, defaultValue);
                return defaultValue;
            }
            boolean result = ((PrimitiveValue)hiByteShiftValue).intValue() == 0;
            isLittleEndianCache.put(virtualMachine, result);
            return result;
        }
    }

    private static void register(String shortedString, StringReference sr, int length, ArrayReference chars) {
        StringInfo si = new StringInfo(sr, shortedString.length() - 3, length, chars);
        synchronized (infoStrings) {
            infoStrings.put(shortedString, si);
        }
    }

    static String getStringWithLengthControl(StringReference sr) throws
            InternalExceptionWrapper, VMDisconnectedExceptionWrapper,
            ObjectCollectedExceptionWrapper, ClassNotLoadedException,
            ClassNotPreparedExceptionWrapper, IncompatibleThreadStateException,
            InvalidTypeException, InvocationException {
        boolean retrieved = false;
        synchronized (stringsCache) {
            StringValueInfo svi = stringsCache.get(sr);
            if (svi != null) {
                if (svi.isShort) {
                    return StringReferenceWrapper.value(sr);
                } else {
                    String str = svi.shortValueRef.get();
                    if (str != null) {
                        return str;
                    }
                }
            }
            if (retrievingStrings.contains(sr)) {
                try {
                    stringsCache.wait();
                } catch (InterruptedException ex) {}
                retrieved = true;
            } else {
                retrievingStrings.add(sr);
            }
        }
        if (retrieved) {
            return getStringWithLengthControl(sr);
        }
        String string = null;
        boolean isShort = true;
        try {
            ReferenceType st = ObjectReferenceWrapper.referenceType(sr);
            ArrayReference sa = null;
            //only applicable if the string implementation uses a byte[] instead
            //of a char[]
            boolean isUTF16 = false;
            //See JEP 254: Compact Strings after the boolean
            boolean isCompactImpl = false;
            int saLength = 0;
            final String ERROR_RESULT = "<Unreadable>";
            try {
                Field valuesField = ReferenceTypeWrapper.fieldByName(st, "value");
                //System.err.println("value field = "+valuesField);
                if (valuesField == null) {
                    List<Field> allFields = ReferenceTypeWrapper.allFields(st);
                    for (Field f : allFields) {
                        if (f.isStatic()) {
                            continue;
                        }
                        Type type = f.type();
                        if (type instanceof ArrayType) {
                            String componentType = ((ArrayType)type).componentTypeName();
                            if ("byte".equals(componentType)){
                                isCompactImpl = true;
                                valuesField = f;
                            }
                            else if ("char".equals(componentType)){
                                valuesField = f;
                            }
                            else{
                                continue;
                            }
                            break;
                        }
                    }
                }
                else if (valuesField.type() instanceof ArrayType &&
                        "byte".equals(((ArrayType)valuesField.type()).
                                componentTypeName())){
                    isCompactImpl = true;
                }
                if (valuesField == null) {
                    isShort = true; // We did not find the values field.
                } else {
                    if (isCompactImpl){
                        //is it UTF16?
                        final int LATIN1 = 0;
                        Field coderField = ReferenceTypeWrapper.fieldByName(st,
                                "coder");
                        Value coderValue;
                        if (coderField != null){
                            coderValue = ObjectReferenceWrapper.getValue(sr,
                                    coderField);
                            if (coderValue instanceof PrimitiveValue &&
                                    ((PrimitiveValue)coderValue).intValue() != LATIN1){
                                isUTF16 = true;
                            }
                        }
                    }
                    int limit = AbstractObjectVariable.MAX_STRING_LENGTH;
                    if (isUTF16){
                        limit *= 2;
                    }
                    Value values = ObjectReferenceWrapper.getValue(sr, valuesField);
                    if (values instanceof ArrayReference) {
                        sa = (ArrayReference) values;
                        saLength = ArrayReferenceWrapper.length(sa);
                        isShort = saLength <= limit;
                    } else {
                        isShort = true;
                    }
                }

            } catch (ClassNotPreparedExceptionWrapper cnpex) {
                isShort = true;
            } catch (ClassNotLoadedException cnlex) {
                isShort = true;
            }
            //System.err.println("isShort = "+isShort);
            if (isShort) {
                string = StringReferenceWrapper.value(sr);
            } else {
                assert sa != null;
                int l = AbstractObjectVariable.MAX_STRING_LENGTH;
                List<Value> values = ArrayReferenceWrapper.getValues(sa, 0,
                        isUTF16 ? (l * 2) : l);
                char[] characters = new char[l + 3];
                if (isCompactImpl) {
                    //java compact string
                    if (!isUTF16) {
                        //we can just cast to char
                        for (int i = 0; i < l; i++) {
                            Value v = values.get(i);
                            if (!(v instanceof ByteValue)) {
                                return ERROR_RESULT;
                            }
                            char c = (char)((ByteValue) v).byteValue();
                            //remove the extended sign
                            c &= 0xFF;
                            characters[i] = c;
                        }
                    }
                    else {
                        int hiByteShift;
                        int lowByteShift;
                        //is it little or big endian?
                        if (isLittleEndian(sr.virtualMachine())){
                            hiByteShift = 0;
                            lowByteShift = 8;
                        }
                        else{
                            hiByteShift = 8;
                            lowByteShift = 0;
                        }
                        for (int i = 0; i < l; i++) {
                            int index = i * 2;
                            Value v = values.get(index);
                            if (!(v instanceof ByteValue)) {
                                return ERROR_RESULT;
                            }
                            Value v2 = values.get(index + 1);
                            if (!(v instanceof ByteValue)) {
                                return ERROR_RESULT;
                            }
                            char c1 = (char) ((ByteValue) v).byteValue();
                            char c2 = (char) ((ByteValue) v2).byteValue();
                            //remove the extended sign
                            c1 = (char) (0xFF & c1);
                            c2 = (char) (0xFF & c2);
                            char c = (char)(c1 << hiByteShift |
                                    c2 << lowByteShift);
                            characters[i] = c;
                        }
                    }
                }
                else{
                    for (int i = 0; i < l; i++) {
                        Value v = values.get(i);
                        if (!(v instanceof CharValue)) {
                            return ERROR_RESULT;
                        }
                        characters[i] = ((CharValue) v).charValue();
                    }
                }
                // Add 3 dots:
                for (int i = l; i < (l + 3); i++) {
                    characters[i] = '.';
                }
                String shortedString = new String(characters);
                int stringLength = isUTF16 ? saLength / 2 : saLength;
                ShortenedStrings.register(shortedString, sr, stringLength, sa);
                string = shortedString;
            }
        }
        catch (ClassNotLoadedException | ClassNotPreparedExceptionWrapper |
                IncompatibleThreadStateException | InternalExceptionWrapper |
                InvalidTypeException | InvocationException |
                ObjectCollectedExceptionWrapper |
                VMDisconnectedExceptionWrapper e){
            Logger.getLogger(ShortenedStrings.class.getSimpleName()).log(
                    Level.INFO, "Error in getStringWithLengthControl",
                    e);
            throw e;
        }
        finally {
            synchronized (stringsCache) {
                if (string != null) {
                    StringValueInfo svi;
                    if (isShort) {
                        svi = new StringValueInfo(isShort);
                    } else {
                        svi = new StringValueInfo(string);
                    }
                    stringsCache.put(sr, svi);
                }
                retrievingStrings.remove(sr);
                stringsCache.notifyAll();
            }
        }
        return string;
    }

    public static class StringInfo {

        private final StringReference sr;
        private final int shortLength;
        private final int length;
        private final ArrayReference chars;

        private StringInfo(StringReference sr, int shortLength, int length, ArrayReference chars) {
            this.sr = sr;
            this.shortLength = shortLength;
            this.length = length;
            this.chars = chars;
        }

        public int getShortLength() {
            return shortLength;
        }

        public int getLength() {
            return length;
        }

        public String getFullString() {
            try {
                return StringReferenceWrapper.value(sr);
            } catch (InternalExceptionWrapper ex) {
                return null;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return null;
            } catch (ObjectCollectedExceptionWrapper ex) {
                return null;
            }
        }

        public Reader getContent() {
            return new Reader() {

                int pos = 0;

                @Override
                public int read(char[] cbuf, int off, int len) throws IOException {
                    if (pos + len > length) {
                        len = length - pos;
                    }
                    List<Value> values;
                    try {
                        values = ArrayReferenceWrapper.getValues(chars, pos, len);
                    } catch (InternalExceptionWrapper ex) {
                        throw new IOException(ex);
                    } catch (VMDisconnectedExceptionWrapper ex) {
                        throw new IOException(ex);
                    } catch (ObjectCollectedExceptionWrapper ex) {
                        throw new IOException(ex);
                    }
                    for (int i = 0; i < len; i++) {
                        Value v = values.get(i);
                        if (!(v instanceof CharValue)) {
                            int p = pos + i;
                            throw new IOException("Char at "+p+" is not a character: "+v);
                        }
                        cbuf[off + i] = ((CharValue) v).charValue();
                    }
                    pos += len;
                    return len;
                }

                @Override
                public void close() throws IOException {
                }
            };
        }
    }
    
    private static class StringValueInfo {
        boolean isShort; // if true, StringReference.value() caches the value
        Reference<String> shortValueRef; // reference to the shortened version of the String value
        
        StringValueInfo(boolean isShort) {
            this.isShort = isShort;
        }
        
        StringValueInfo(String shortenedValue) {
            this.isShort = false;
            this.shortValueRef = new WeakReference<String>(shortenedValue);
        }
    }
}
