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
package org.netbeans.modules.python.source.elements;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.python.source.PythonIndex;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.openide.filesystems.FileObject;
import org.python.antlr.PythonTree;

/**
 * Elements representing information coming from the persistent index
 *
 */
public class IndexedElement extends Element {
    public static final EnumSet<Modifier> PRIVATE_MODIFIERS = EnumSet.of(Modifier.PRIVATE);
    public static final EnumSet<Modifier> PROTECTED_MODIFIERS = EnumSet.of(Modifier.PROTECTED);
    public static final EnumSet<Modifier> STATIC_MODIFIERS = EnumSet.of(Modifier.STATIC);
    public static final EnumSet<Modifier> PRIVATE_STATIC_MODIFIERS = EnumSet.of(Modifier.STATIC, Modifier.PRIVATE);
    public static final EnumSet<Modifier> PROTECTED_STATIC_MODIFIERS = EnumSet.of(Modifier.STATIC, Modifier.PROTECTED);
    public static final Set<Modifier> PUBLIC_MODIFIERS = Collections.emptySet();

    // Plan: Stash a single item for class entries so I can search by document for the class.
    // Add more types into the types
    /** This method is documented */
    public static final int DOCUMENTED = 1 << 0;
    /** This method is private */
    public static final int PRIVATE = 1 << 2;
    /** This is a function, not a property */
    public static final int FUNCTION = 1 << 3;
    /** This element is "static" (e.g. it's a classvar for fields, class method for methods etc) */
    public static final int STATIC = 1 << 4;
    /** This element is deliberately not documented (rdoc :nodoc:) */
    public static final int NODOC = 1 << 5;
    /** This is a global variable */
    public static final int GLOBAL = 1 << 6;
    /** This is a constructor */
    public static final int CONSTRUCTOR = 1 << 7;
    /** This is a deprecated */
    public static final int DEPRECATED = 1 << 8;
    /** This is a documentation-only definition */
    public static final int DOC_ONLY = 1 << 9;
    /** This is a constant/final */
    public static final int FINAL = 1 << 10;

    // Flags noting semicolon positions in attributes
    public static final int NAME_INDEX = 0;
    public static final int TYPE_INDEX = 1;
    public static final int FLAG_INDEX = 2;
    public static final int ARG_INDEX = 3;
//    public static final int IN_INDEX = 1;
//    public static final int CASE_SENSITIVE_INDEX = 2;
//    public static final int FLAG_INDEX = 3;
//    public static final int ARG_INDEX = 4;
//    public static final int NODE_INDEX = 5;
//    public static final int DOC_INDEX = 6;
//    public static final int BROWSER_INDEX = 7;
//    public static final int TYPE_INDEX = 8;
    protected final String name;
    protected final ElementKind kind;
    protected String url;
    protected FileObject fileObject;
    protected final String module;
    protected String rhs;
    protected boolean smart;
    protected boolean inherited;
    protected Set<Modifier> modifiers;
    protected PythonTree node;
    protected int flags;
    protected final String attributes;
    protected final String clz;
    protected int order;

    public IndexedElement(String name, ElementKind kind, String url, String module, String clz, String attributes) {
        this.name = name;
        this.kind = kind;
        this.url = url;
        this.module = module;
        this.clz = clz;
        this.attributes = attributes;

    // Should be IndexedMethod:
    //assert !((!(this instanceof IndexedMethod)) && (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR)) : this;
    }

    public static IndexedElement create(String signature, String module, String url, String clz) {
        int semi = signature.indexOf(';');
        assert semi != -1;

        String name = signature.substring(0, semi);
        int flags = IndexedElement.decode(signature, semi + 3, 0);

        char type = signature.charAt(semi + 1);
        ElementKind kind;
        switch (type) {
        case 'C':
            kind = ElementKind.CLASS;
            break;
        case 'I':
            kind = ElementKind.MODULE;
            break;
        case 'D':
            kind = ElementKind.VARIABLE;
            break;
        case 'A':
            kind = ElementKind.ATTRIBUTE;
            break;
        case 'F':
        case 'M':
        case 'c': {
            kind = type == 'c' ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
            IndexedMethod method = new IndexedMethod(name, kind, url, module, clz, signature);
            method.flags = flags;
            return method;
        }
        default:
            kind = ElementKind.OTHER;
            break;
        }

        IndexedElement element = new IndexedElement(name, kind, url, module, clz, signature);
        element.flags = flags;
        return element;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedElement other = (IndexedElement)obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.module != other.module && (this.module == null || !this.module.equals(other.module))) {
            return false;
        }
        if (this.kind != other.kind) {
            return false;
        }
        return true;
    }

    public String getModule() {
        return module;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (this.kind != null ? this.kind.hashCode() : 0);
        return hash;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ElementKind getKind() {
        return kind;
    }

    public String getFilenameUrl() {
        return url;
    }

    @Override
    public FileObject getFileObject() {
        if ((fileObject == null) && (url != null)) {
            fileObject = PythonIndex.getFileObject(url);

            if (fileObject == null) {
                // Don't try again
                url = null;
            }
        }

        return fileObject;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    public boolean isInherited() {
        return inherited;
    }

    public String getType() {
        return null;
    }

    public String getOrigin() {
        return module;
    }

    public boolean isSmart() {
        return smart;
    }

    public void setSmart(boolean smart) {
        this.smart = smart;
    }

    public String getRhs() {
        if (rhs == null) {
            rhs = module;
            if (rhs.equals("stub_missing")) { // NOI18N
                rhs = "<i>builtin</i>";
            }
        }
        return rhs;
    }

    public void setRhs(String rhs) {
        this.rhs = rhs;
    }

    @Override
    public String getIn() {
        return module;
    }

    public String getSignature() {
        if (clz != null) {
            return clz + "." + name;
        }

        return name;
    }

    public String getClz() {
        return clz;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public static Set<Modifier> getModifiersForName(String name, boolean isPrivate, boolean isProtected, boolean isStatic) {
        Set<Modifier> modifiers;

        // Private variables: start with __ but doesn't end with __
        // Section 9.6 Private Variables - http://docs.python.org/tut/node11.html
        if (name != null && name.startsWith("__") && !name.endsWith("__")) { // NOI18N
            isPrivate = true;
        } else if (name != null && name.startsWith("_") && !name.endsWith("_")) { // NOI18N
            // From PEP8: Single_leading_underscore: weak "internal use" indicator
            // (e.g. "from M import *" does not import objects whose name
            // starts with an underscore).
            // The protected modifier might work well to visually indicate this.
            isProtected = true;
        }
        if (isPrivate) {
            if (isStatic) {
                modifiers = PRIVATE_STATIC_MODIFIERS;
            } else {
                modifiers = PRIVATE_MODIFIERS;
            }
        } else if (isProtected) {
            if (isStatic) {
                modifiers = PROTECTED_STATIC_MODIFIERS;
            } else {
                modifiers = PROTECTED_MODIFIERS;
            }
        } else {
            if (isStatic) {
                modifiers = STATIC_MODIFIERS;
            } else {
                modifiers = PUBLIC_MODIFIERS;
            }
        }

        return modifiers;
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            modifiers = getModifiersForName(name, isPrivate(), false, isStatic());
        }

        return modifiers;
    }

    public PythonTree getNode() {
        if (node == null) {
            node = PythonAstUtils.getForeignNode(this, null);
        }
        return node;
    }

    @Override
    public String toString() {
        return "IndexedElement:" + name + "," + kind + "," + rhs;
    }

    protected int getAttributeSection(int section) {
        assert section != 0; // Obtain directly, and logic below (+1) is wrong
        int attributeIndex = 0;
        for (int i = 0; i < section; i++) {
            attributeIndex = attributes.indexOf(';', attributeIndex + 1);
        }

        assert attributeIndex != -1;
        return attributeIndex + 1;
    }

    /** Return a string (suitable for persistence) encoding the given flags */
    public static String encode(int flags) {
        return Integer.toString(flags, 16);
    }

    /** Return flag corresponding to the given encoding chars */
    public static int decode(String s, int startIndex, int defaultValue) {
        int value = 0;
        for (int i = startIndex, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            if (c == ';') {
                if (i == startIndex) {
                    return defaultValue;
                }
                break;
            }

            value = value << 4;

            if (c > '9') {
                value += c - 'a' + 10;
            } else {
                value += c - '0';
            }
        }

        return value;
    }

    public static int getFlags(AstElement element) {
        // Return the flags corresponding to the given AST element
        int value = 0;

        ElementKind k = element.getKind();
        if (k == ElementKind.CONSTRUCTOR) {
            value = value | CONSTRUCTOR;
        }
        if (k == ElementKind.METHOD || k == ElementKind.CONSTRUCTOR) {
            value = value | FUNCTION;
        } else if (k == ElementKind.GLOBAL) {
            value = value | GLOBAL;
        }
        if (element.getModifiers().contains(Modifier.STATIC)) {
            value = value | STATIC;
        }
        if (element.getModifiers().contains(Modifier.DEPRECATED)) {
            value = value | DEPRECATED;
        }
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            value = value | PRIVATE;
        }

        return value;
    }

    public boolean isDocumented() {
        return (flags & DOCUMENTED) != 0;
    }

    public boolean isPublic() {
        return (flags & PRIVATE) == 0;
    }

    public boolean isPrivate() {
        return (flags & PRIVATE) != 0;
    }

    public boolean isFunction() {
        return (flags & FUNCTION) != 0;
    }

    public boolean isStatic() {
        return (flags & STATIC) != 0;
    }

    public boolean isNoDoc() {
        return (flags & NODOC) != 0;
    }

    public boolean isFinal() {
        return (flags & FINAL) != 0;
    }

    public boolean isConstructor() {
        return (flags & CONSTRUCTOR) != 0;
    }

    public boolean isDeprecated() {
        return (flags & DEPRECATED) != 0;
    }

    public boolean isDocOnly() {
        return (flags & DOC_ONLY) != 0;
    }

    public static String decodeFlags(int flags) {
        StringBuilder sb = new StringBuilder();
        if ((flags & DOCUMENTED) != 0) {
            sb.append("|DOCUMENTED");
        }

        if ((flags & PRIVATE) != 0) {
            sb.append("|PRIVATE");
        }

        if ((flags & CONSTRUCTOR) != 0) {
            sb.append("|CONSTRUCTOR");
        } else if ((flags & FUNCTION) != 0) {
            sb.append("|FUNCTION");
        } else if ((flags & GLOBAL) != 0) {
            sb.append("|GLOBAL");
        }

        if ((flags & STATIC) != 0) {
            sb.append("|STATIC");
        }

        if ((flags & NODOC) != 0) {
            sb.append("|NODOC");
        }

        if ((flags & DEPRECATED) != 0) {
            sb.append("|DEPRECATED");
        }

        if ((flags & DOC_ONLY) != 0) {
            sb.append("|DOC_ONLY");
        }

        if ((flags & FINAL) != 0) {
            sb.append("|FINAL");
        }

        if (sb.length() > 0) {
            sb.append("|");
        }
        return sb.toString();
    }

    // For testsuite
    public static int stringToFlags(String string) {
        int flags = 0;
        if (string.contains("|DOCUMENTED")) {
            flags |= DOCUMENTED;
        }
        if (string.contains("|PRIVATE")) {
            flags |= PRIVATE;
        }
        if (string.contains("|DEPRECATED")) {
            flags |= DEPRECATED;
        }
        if (string.contains("|CONSTRUCTOR")) {
            flags |= CONSTRUCTOR;
        }
//        if (string.indexOf("|PROTECTED") != -1) {
//            flags |= PROTECTED;
//        }
//        if (string.indexOf("|TOPLEVEL") != -1) {
//            flags |= TOPLEVEL;
//        }
        if (string.contains("|STATIC")) {
            flags |= STATIC;
        }
        if (string.contains("|NODOC")) {
            flags |= NODOC;
        }

        return flags;
    }
}
