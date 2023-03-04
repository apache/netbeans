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
package org.netbeans.modules.groovy.editor.api.elements.index;

import groovyjarjarasm.asm.Opcodes;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.groovy.editor.api.elements.GroovyElement;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.openide.filesystems.FileObject;

/**
 * A program element coming from the persistent index.
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
public abstract class IndexedElement extends GroovyElement {

    protected final IndexResult result;
    protected final String attributes;
    protected final int flags;
    protected Set<Modifier> modifiers;
    private Document document;

    protected IndexedElement(IndexResult result, String in, String attributes, int flags) {
        this(result, in, null, attributes, flags);
    }

    protected IndexedElement(IndexResult result, String in, String name, String attributes, int flags) {
        super(in, name);
        this.result = result;
        this.attributes = attributes;
        this.flags = flags;
    }

    @Override
    public abstract String getSignature();

    @Override
    public String toString() {
        return getSignature();
    }

    public Document getDocument() throws IOException {
        if (document == null) {
            FileObject fo = getFileObject();

            if (fo == null) {
                return null;
            }

            document = LexUtilities.getDocument(fo, true);
        }

        return document;
    }

    @Override
    public FileObject getFileObject() {
        return result.getFile();
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            Modifier access = null;
            if (isPublic()) {
                access = Modifier.PUBLIC;
            } else if (isProtected()) {
                access = Modifier.PROTECTED;
            } else if (isPrivate()) {
                access = Modifier.PRIVATE;
            }
            boolean isStatic = isStatic();

            if (access != null) {
                if (isStatic) {
                    modifiers = EnumSet.of(access, Modifier.STATIC);
                } else {
                    modifiers = EnumSet.of(access);
                }
            } else if (isStatic) {
                modifiers = EnumSet.of(Modifier.STATIC);
            } else {
                modifiers = Collections.emptySet();
            }
        }
        return modifiers;
    }

    /** Return a string (suitable for persistence) encoding the given flags */
    public static char flagToFirstChar(int flags) {
        char first = (char)(flags >>= 4);
        if (first >= 10) {
            return (char)(first-10+'a');
        } else {
            return (char)(first+'0');
        }
    }

    /** Return a string (suitable for persistence) encoding the given flags */
    public static char flagToSecondChar(int flags) {
        char second = (char)(flags & 0xf);
        if (second >= 10) {
            return (char)(second-10+'a');
        } else {
            return (char)(second+'0');
        }
    }
    
    /** Return a string (suitable for persistence) encoding the given flags */
    public static String flagToString(int flags) {
        return (""+flagToFirstChar(flags)) + flagToSecondChar(flags);
    }
    
    /** Return flag corresponding to the given encoding chars */
    public static int stringToFlag(String s, int startIndex) {
        return stringToFlag(s.charAt(startIndex), s.charAt(startIndex+1));
    }
    
    /** Return flag corresponding to the given encoding chars */
    public static int stringToFlag(char first, char second) {
        int high;
        int low;
        if (first > '9') {
            high = first-'a'+10;
        } else {
            high = first-'0';
        }
        if (second > '9') {
            low = second-'a'+10;
        } else {
            low = second-'0';
        }
        return (high << 4) + low;
    }
    
    public boolean isPublic() {
        return (flags & Opcodes.ACC_PUBLIC) != 0;
    }

    public boolean isPrivate() {
        return (flags & Opcodes.ACC_PRIVATE) != 0;
    }
    
    public boolean isProtected() {
        return (flags & Opcodes.ACC_PROTECTED) != 0;
    }
    
    public boolean isStatic() {
        return (flags & Opcodes.ACC_STATIC) != 0;
    }
    
    public static String decodeFlags(int flags) {
        StringBuilder sb = new StringBuilder();
        if ((flags & Opcodes.ACC_PUBLIC) != 0) {
            sb.append("|PUBLIC");
        }
        if ((flags & Opcodes.ACC_PROTECTED) != 0) {
            sb.append("|PROTECTED");
        }
        if ((flags & Opcodes.ACC_STATIC) != 0) {
            sb.append("|STATIC");
        }
        
        return sb.toString();
    }

}
