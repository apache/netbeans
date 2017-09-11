/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
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

    private static void register(String shortedString, StringReference sr, int length, ArrayReference chars) {
        StringInfo si = new StringInfo(sr, shortedString.length() - 3, length, chars);
        synchronized (infoStrings) {
            infoStrings.put(shortedString, si);
        }
    }

    static String getStringWithLengthControl(StringReference sr) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper {
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
            int saLength = 0;
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
                        if (type instanceof ArrayType &&
                            "char".equals(((ArrayType) type).componentTypeName())) {
                            valuesField = f;
                            break;
                        }
                    }
                }
                if (valuesField == null) {
                    isShort = true; // We did not find the values field.
                } else {
                    Value values = ObjectReferenceWrapper.getValue(sr, valuesField);
                    if (values instanceof ArrayReference) {
                        sa = (ArrayReference) values;
                        saLength = ArrayReferenceWrapper.length(sa);
                        isShort = saLength <= AbstractObjectVariable.MAX_STRING_LENGTH;
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
                List<Value> values = ArrayReferenceWrapper.getValues(sa, 0, l);
                char[] characters = new char[l + 3];
                for (int i = 0; i < l; i++) {
                    Value v = values.get(i);
                    if (!(v instanceof CharValue)) {
                        return "<Unreadable>";
                    }
                    characters[i] = ((CharValue) v).charValue();
                }
                // Add 3 dots:
                for (int i = l; i < (l + 3); i++) {
                    characters[i] = '.';
                }
                String shortedString = new String(characters);
                ShortenedStrings.register(shortedString, sr, saLength, sa);
                string = shortedString;
            }
        } finally {
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
