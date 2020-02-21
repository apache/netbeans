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
package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmClosureClassifier;
import org.netbeans.modules.cnd.api.model.CsmClosureType;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctional;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Java completion query specifications
 *
 * @version 1.00
 */
abstract public class CsmCompletion {
    
    private static final Logger LOG = Logger.getLogger(CsmCompletion.class.getSimpleName());

    public static final int PUBLIC_LEVEL = 3;
    public static final int PROTECTED_LEVEL = 2;
    public static final int PACKAGE_LEVEL = 1;
    public static final int PRIVATE_LEVEL = 0;
    public static final SimpleClass BOOLEAN_CLASS = new SimpleClass("bool", ""); // NOI18N
    public static final SimpleClass BYTE_CLASS = new SimpleClass("byte", ""); // NOI18N
    public static final SimpleClass SIGNED_CHAR_CLASS = new SimpleClass("signed char", ""); // NOI18N
    public static final SimpleClass CHAR_CLASS = new SimpleClass("char", ""); // NOI18N
    public static final SimpleClass WCHAR_CLASS = new SimpleClass("wchar_t", ""); // NOI18N
    public static final SimpleClass DOUBLE_CLASS = new SimpleClass("double", ""); // NOI18N
    public static final SimpleClass FLOAT_CLASS = new SimpleClass("float", ""); // NOI18N
    public static final SimpleClass INT_CLASS = new SimpleClass("int", ""); // NOI18N
    public static final SimpleClass LONG_CLASS = new SimpleClass("long", ""); // NOI18N
    public static final SimpleClass LONG_LONG_CLASS = new SimpleClass("long long", ""); // NOI18N
    public static final SimpleClass SHORT_CLASS = new SimpleClass("short", ""); // NOI18N
    public static final SimpleClass VOID_CLASS = new SimpleClass("void", ""); // NOI18N
    public static final SimpleClass NULLPTR_CLASS = new SimpleClass("nullptr", ""); // NOI18N
    public static final SimpleClass UNSIGNED_CHAR_CLASS = new SimpleClass("unsigned char", ""); // NOI18N
    public static final SimpleClass UNSIGNED_INT_CLASS = new SimpleClass("unsigned int", ""); // NOI18N
    public static final SimpleClass UNSIGNED_LONG_CLASS = new SimpleClass("unsigned long", ""); // NOI18N
    public static final SimpleClass LONG_UNSIGNED_CLASS = new SimpleClass("long unsigned", ""); // NOI18N
    public static final SimpleClass UNSIGNED_LONG_INT_CLASS = new SimpleClass("unsigned long int", ""); // NOI18N
    public static final SimpleClass LONG_UNSIGNED_INT_CLASS = new SimpleClass("long unsigned int", ""); // NOI18N
    public static final SimpleClass UNSIGNED_LONG_LONG_CLASS = new SimpleClass("unsigned long long", ""); // NOI18N
    public static final SimpleClass UNSIGNED_LONG_LONG_INT_CLASS = new SimpleClass("unsigned long long int", ""); // NOI18N
    public static final SimpleClass UNSIGNED_SHORT_CLASS = new SimpleClass("unsigned short", ""); // NOI18N
    public static final SimpleClass LONG_DOUBLE_CLASS = new SimpleClass("long double", ""); // NOI18N
    public static final BaseType BOOLEAN_TYPE = new BaseType(BOOLEAN_CLASS, 0, 0, 0, false, false);
    public static final BaseType BYTE_TYPE = new BaseType(BYTE_CLASS, 0, 0, 0, false, false);
    public static final BaseType SIGNED_CHAR_TYPE = new BaseType(SIGNED_CHAR_CLASS, 0, 0, 0, false, false);
    public static final BaseType CHAR_TYPE = new BaseType(CHAR_CLASS, 0, 0, 0, false, false);
    public static final BaseType WCHAR_TYPE = new BaseType(WCHAR_CLASS, 0, 0, 0, false, false);
    public static final BaseType DOUBLE_TYPE = new BaseType(DOUBLE_CLASS, 0, 0, 0, false, false);
    public static final BaseType FLOAT_TYPE = new BaseType(FLOAT_CLASS, 0, 0, 0, false, false);
    public static final BaseType INT_TYPE = new BaseType(INT_CLASS, 0, 0, 0, false, false);
    public static final BaseType LONG_TYPE = new BaseType(LONG_CLASS, 0, 0, 0, false, false);
    public static final BaseType LONG_LONG_TYPE = new BaseType(LONG_LONG_CLASS, 0, 0, 0, false, false);
    public static final BaseType SHORT_TYPE = new BaseType(SHORT_CLASS, 0, 0, 0, false, false);
    public static final BaseType UNSIGNED_CHAR_TYPE = new BaseType(UNSIGNED_CHAR_CLASS, 0, 0, 0, false, false);
    public static final BaseType UNSIGNED_INT_TYPE = new BaseType(UNSIGNED_INT_CLASS, 0, 0, 0, false, false);
    public static final BaseType UNSIGNED_LONG_TYPE = new BaseType(UNSIGNED_LONG_CLASS, 0, 0, 0, false, false);
    public static final BaseType LONG_UNSIGNED_TYPE = new BaseType(LONG_UNSIGNED_CLASS, 0, 0, 0, false, false);
    public static final BaseType UNSIGNED_LONG_INT_TYPE = new BaseType(UNSIGNED_LONG_INT_CLASS, 0, 0, 0, false, false);
    public static final BaseType LONG_UNSIGNED_INT_TYPE = new BaseType(LONG_UNSIGNED_INT_CLASS, 0, 0, 0, false, false);
    public static final BaseType UNSIGNED_LONG_LONG_TYPE = new BaseType(UNSIGNED_LONG_LONG_CLASS, 0, 0, 0, false, false);
    public static final BaseType UNSIGNED_LONG_LONG_INT_TYPE = new BaseType(UNSIGNED_LONG_LONG_INT_CLASS, 0, 0, 0, false, false);
    public static final BaseType UNSIGNED_SHORT_TYPE = new BaseType(UNSIGNED_SHORT_CLASS, 0, 0, 0, false, false);
    public static final BaseType LONG_DOUBLE_TYPE = new BaseType(LONG_DOUBLE_CLASS, 0, 0, 0, false, false);
    public static final BaseType VOID_TYPE = new BaseType(VOID_CLASS, 0, 0, 0, false, false);
    public static final BaseType NULLPTR_TYPE = new BaseType(NULLPTR_CLASS, 1, 0, 0, false, false);
    public static final SimpleClass INVALID_CLASS = new SimpleClass("", ""); // NOI18N
    public static final BaseType INVALID_TYPE = new BaseType(INVALID_CLASS, 0, 0, 0, false, false);
    public static final SimpleClass NULL_CLASS = new SimpleClass("null", ""); // NOI18N
    public static final BaseType NULL_TYPE = new BaseType(NULL_CLASS, 0, 0, 0, false, false);
    public static final SimpleClass OBJECT_CLASS_ARRAY = new SimpleClass("java.lang.Object[]", "java.lang".length(), true); // NOI18N
    public static final BaseType OBJECT_TYPE_ARRAY = new BaseType(OBJECT_CLASS_ARRAY, 0, 0, 0, false, false);
    public static final SimpleClass OBJECT_CLASS = new SimpleClass("java.lang.Object", "java.lang".length(), true); // NOI18N
    public static final BaseType OBJECT_TYPE = new BaseType(OBJECT_CLASS, 0, 0, 0, false, false);
    public static final SimpleClass CLASS_CLASS = new SimpleClass("java.lang.Class", "java.lang".length(), true); // NOI18N
    public static final BaseType CLASS_TYPE = new BaseType(CLASS_CLASS, 0, 0, 0, false, false);
    public static final SimpleClass STRING_CLASS = new SimpleClass("char", 0, true); // NOI18N
    public static final BaseType STRING_TYPE = new BaseType(STRING_CLASS, 1, 0, 0, false, false);
    public static final SimpleClass CONST_STRING_CLASS = new SimpleClass("const char", 0, true); // NOI18N
    public static final BaseType CONST_STRING_TYPE = new BaseType(STRING_CLASS, 1, 0, 0, true, false);
    public static final SimpleClass WSTRING_CLASS = new SimpleClass("wchar_t", 0, true); // NOI18N
    public static final BaseType WSTRING_TYPE = new BaseType(WSTRING_CLASS, 1, 0, 0, false, false);
    public static final SimpleClass CONST_WSTRING_CLASS = new SimpleClass("const wchar_t", 0, true); // NOI18N
    public static final BaseType CONST_WSTRING_TYPE = new BaseType(WSTRING_CLASS, 1, 0, 0, true, false);
    public static final BaseType CONST_BOOLEAN_TYPE = new BaseType(BOOLEAN_CLASS, 0, 0, 0, true, false);
    public static final BaseType CONST_BYTE_TYPE = new BaseType(BYTE_CLASS, 0, 0, 0, true, false);
    public static final BaseType CONST_CHAR_TYPE = new BaseType(CHAR_CLASS, 0, 0, 0, true, false);
    public static final BaseType CONST_DOUBLE_TYPE = new BaseType(DOUBLE_CLASS, 0, 0, 0, true, false);
    public static final BaseType CONST_FLOAT_TYPE = new BaseType(FLOAT_CLASS, 0, 0, 0, true, false);
    public static final BaseType CONST_INT_TYPE = new BaseType(INT_CLASS, 0, 0, 0, true, false);
    public static final BaseType CONST_LONG_TYPE = new BaseType(LONG_CLASS, 0, 0, 0, true, false);
    public static final BaseType CONST_SHORT_TYPE = new BaseType(SHORT_CLASS, 0, 0, 0, true, false);
    public static final BaseType CONST_VOID_TYPE = new BaseType(VOID_CLASS, 0, 0, 0, true, false);

    // the bit for local member. the modificator is not saved within this bit.
    public static final int LOCAL_MEMBER_BIT = (1 << 29);

    // the bit for deprecated flag. it is saved to copde completion  DB
    public static final int DEPRECATED_BIT = (1 << 20);
    private static final Map<CharSequence, CsmClassifier> str2PrimitiveClass = new HashMap<CharSequence, CsmClassifier>();
    private static final Map<CharSequence, BaseType> str2PrimitiveType = new HashMap<CharSequence, BaseType>();
    private static final Map<CharSequence, BaseType> str2PredefinedType = new HashMap<CharSequence, BaseType>();


    static {
        // initialize primitive types cache
        BaseType[] types = new BaseType[]{
            BOOLEAN_TYPE, BYTE_TYPE, CHAR_TYPE, DOUBLE_TYPE, FLOAT_TYPE,
            INT_TYPE, LONG_TYPE, SHORT_TYPE, VOID_TYPE, NULLPTR_TYPE,
            UNSIGNED_CHAR_TYPE, UNSIGNED_INT_TYPE, UNSIGNED_LONG_TYPE, UNSIGNED_SHORT_TYPE,
            LONG_DOUBLE_TYPE, SIGNED_CHAR_TYPE, LONG_LONG_TYPE, UNSIGNED_LONG_LONG_TYPE,
            WCHAR_TYPE, UNSIGNED_LONG_LONG_INT_TYPE, UNSIGNED_LONG_INT_TYPE,
            LONG_UNSIGNED_TYPE, LONG_UNSIGNED_INT_TYPE
        };

        for (int i = types.length - 1; i >= 0; i--) {
            String typeName = types[i].getClassifier().getName().toString();
            str2PrimitiveClass.put(typeName, types[i].getClassifier());
            str2PrimitiveType.put(typeName, types[i]);
        }

        // initialize predefined types cache
        types = new BaseType[]{
                    NULL_TYPE, OBJECT_TYPE_ARRAY, OBJECT_TYPE, CLASS_TYPE, STRING_TYPE, CONST_STRING_TYPE,
                    CONST_BOOLEAN_TYPE, CONST_BYTE_TYPE, CONST_CHAR_TYPE, CONST_DOUBLE_TYPE, CONST_FLOAT_TYPE,
                    CONST_INT_TYPE, CONST_LONG_TYPE, CONST_SHORT_TYPE, CONST_VOID_TYPE,
                    WSTRING_TYPE, CONST_WSTRING_TYPE
                 };

        for (int i = types.length - 1; i >= 0; i--) {
            String typeName = types[i].getClassifier().getName().toString();
            str2PredefinedType.put(typeName, types[i]);
            str2PredefinedType.put(types[i].getClassifier().getQualifiedName(), types[i]);
            str2PredefinedType.put(types[i].format(true), types[i]);
        }
    }
    public static final CsmParameter[] EMPTY_PARAMETERS = new CsmParameter[0];
    public static final CsmClassifier[] EMPTY_CLASSES = new CsmClassifier[0];
    public static final CsmNamespace[] EMPTY_NAMESPACES = new CsmNamespace[0];
    public static final CsmField[] EMPTY_FIELDS = new CsmField[0];
    public static final CsmConstructor[] EMPTY_CONSTRUCTORS = new CsmConstructor[0];
    public static final CsmMethod[] EMPTY_METHODS = new CsmMethod[0];
    public static final String SCOPE = "::";  //NOI18N
    private static int debugMode;
    /** Map holding the simple class instances */
//    private static HashMap classCache = new HashMap(5003);
    /** Map holding the cached types */
    //private static HashMap typeCache = new HashMap(5003);
    /** Debug expression creation */
    public static final int DEBUG_EXP = 1;
    /** Debug finding packages/classes/fields/methods */
    public static final int DEBUG_FIND = 2;

    private CsmCompletion() {
    }

    /** Get level from modifiers. */
    public static int getLevel(int modifiers) {
        if ((modifiers & Modifier.PUBLIC) != 0) {
            return PUBLIC_LEVEL;
        } else if ((modifiers & Modifier.PROTECTED) != 0) {
            return PROTECTED_LEVEL;
        } else if ((modifiers & Modifier.PRIVATE) == 0) {
            return PACKAGE_LEVEL;
        } else {
            return PRIVATE_LEVEL;
        }
    }

    public static boolean isPrimitiveClassName(String s) {
        return CndLexerUtilities.isType(s);
    }

    public static boolean isPrimitiveClass(CsmClassifier c) {
//        return (c.getPackageName().length() == 0)
//               && isPrimitiveClassName(c.getName());
        return isPrimitiveClassName(c.getName().toString());
    }
    
    public static boolean safeIsPrimitiveClass(CsmType type, CsmClassifier c) {
        if (c != null) {
            return isPrimitiveClass(c);
        }
        LOG.log(Level.WARNING, "Type {0} ({1}) doesn''t have classifier!", new Object[]{type, type.getClass().getName()});
        return false;
    }
//
//    public static CsmClassifier getPrimitiveClass(String s) {
//        return str2PrimitiveClass.get(s);
//    }

    private static BaseType getPrimitiveType(String s) {
        return str2PrimitiveType.get(s);
    }

    public static CsmType getPredefinedType(CsmFile containingFile, int start, int end, CsmCompletionExpression item) {
        BaseType baseType = getPrimitiveType(item.getType());
        if (baseType == null && item.getType() != null && item.getType().contains("signed ")) { // NOI18N
            baseType = getPrimitiveType(item.getType().replace("signed ", "")); // NOI18N
        }
        if (baseType == null) {
            baseType = str2PredefinedType.get(item.getType());
        }
        if (baseType != null) {
            // wrap with correct offsetable information
            return new OffsetableType(
                    baseType,
                    containingFile,
                    start,
                    end,
                    item.getTokenCount() > 0 ? "0".equals(item.getTokenText(0)) : false // NOI18N
            );
        } else {
            return null;
        }
    }

    public static Iterator<CsmClassifier> getPrimitiveClassIterator() {
        return str2PrimitiveClass.values().iterator();
    }

    public static CsmClassifier getSimpleClass(CsmClassifier clazz) {
        CsmClassifier cls = null;//(CsmClassifier)classCache.get(fullClassName);
        if (clazz != null) {
            cls = new SimpleClass(clazz);
        }
        return cls;
    }

    public static CsmClassifier createSimpleClass(String fullClassName) {
        int nameInd = fullClassName.lastIndexOf(CsmCompletion.SCOPE) + 1;
        return createSimpleClass(fullClassName.substring(nameInd),
                (nameInd > 0) ? fullClassName.substring(0, nameInd - 1) : ""); // NOI18N
    }

    public static CsmClassifier createSimpleClass(String name, String packageName) {
        return new SimpleClass(name, packageName, CsmDeclaration.Kind.CLASS);
    }


    /** returns type for dereferenced object
     * @param obj
     * @return
     */
    public static CsmType getObjectType(CsmObject obj, boolean _constIfClassifier) {
        CsmType type = null;
        if (CsmKindUtilities.isTypedefOrTypeAlias(obj)) {
            type = CsmCompletion.createType((CsmClassifier) obj, 0, 0, 0, _constIfClassifier, false);
        } else if (CsmKindUtilities.isClassifier(obj)) {
            type = CsmCompletion.createType((CsmClassifier) obj, 0, 0, 0, _constIfClassifier, false);
        } else if (CsmKindUtilities.isFunction(obj)) {
            CsmFunction fun = (CsmFunction) obj;
            if (CsmKindUtilities.isConstructor(fun)) {
                CsmClassifier cls = ((CsmConstructor) obj).getContainingClass();
                type = CsmCompletion.createType(cls, 0, 0, 0, false, false);
            } else {
                type = fun.getReturnType();
            }
        } else if (CsmKindUtilities.isFunctional(obj)) {
            type = ((CsmFunctional) obj).getReturnType();
        } else if (CsmKindUtilities.isVariable(obj)) {
            type = ((CsmVariable) obj).getType();
        } else if (CsmKindUtilities.isEnumerator(obj)) {
            type = INT_TYPE;
        } else {
            type = null;
        }
        return type;
    }

    /** Create new type or get the existing one from the cache. The cache holds
     * the arrays with the increasing array depth for the particular class
     * as the members. Simple class is used for the caching to make it independent
     * on the real completion classes that can become obsolete and thus should
     * be garbage collected.
     */
    public static CsmType createType(CsmClassifier cls, int ptrDepth, int refDepth, int arrayDepth, boolean _const, boolean _volatile) {
        return createType(cls, ptrDepth, refDepth, arrayDepth, _const, _volatile, CsmKindUtilities.isTemplateParameter(cls));
    }
    
    /** Create new type or get the existing one from the cache. The cache holds
     * the arrays with the increasing array depth for the particular class
     * as the members. Simple class is used for the caching to make it independent
     * on the real completion classes that can become obsolete and thus should
     * be garbage collected.
     */
    public static CsmType createType(CsmClassifier cls, int ptrDepth, int refDepth, int arrayDepth, boolean _const, boolean _volatile, boolean templateBased) {
        if (cls == null) {
            return null;
        }
        if (CsmKindUtilities.isClosureClassifier(cls)) {
            return new CompletionClosureType(((CsmClosureClassifier) cls).getLambda(), refDepth, _const, _volatile);
        }
        return new BaseType(cls, ptrDepth, refDepth, arrayDepth, _const, _volatile, templateBased);
    }

    public static CsmNamespace getProjectNamespace(CsmProject project, CsmNamespace ns) {
        if (ns == null) {
            return null;
        }
        if (project == null) {
            return ns;
        }
        if (project.equals(ns.getProject())) {
            return ns;
        }
        return new SimpleNamespace(project, ns);
    }

    private static final class SimpleNamespace implements CsmNamespace {
        private final CsmNamespace wrapped;
        private final CsmProject project;

        SimpleNamespace(CsmProject owner, CsmNamespace ns) {
            this.project = owner;
            this.wrapped = ns;
        }

        @Override
        public CsmNamespace getParent() {
            CsmNamespace p = wrapped.getParent();
            // it was global
            if (p == null) {
                return null;
            }
            return new SimpleNamespace(project, p);
        }

        @Override
        public Collection<CsmNamespace> getNestedNamespaces() {
            return Collections.emptyList();
        }

        @Override
        public Collection<CsmNamespace> getInlinedNamespaces() {
            return Collections.emptyList();
        }

        @Override
        public Collection<CsmOffsetableDeclaration> getDeclarations() {
            return Collections.emptyList();
        }

        @Override
        public Collection<CsmNamespaceDefinition> getDefinitions() {
            return Collections.emptyList();
        }

        @Override
        public boolean isGlobal() {
            return wrapped.isGlobal();
        }

        @Override
        public boolean isInline() {
            return wrapped.isInline();
        }

        @Override
        public CsmProject getProject() {
            return project;
        }

        @Override
        public CharSequence getQualifiedName() {
            return wrapped.getQualifiedName();
        }

        @Override
        public CharSequence getName() {
            return wrapped.getName();
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SimpleNamespace other = (SimpleNamespace) obj;
            if (!this.wrapped.equals(other.wrapped)) {
                return false;
            }
            if (!this.project.equals(other.project)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + this.wrapped.hashCode();
            hash = 83 * hash + this.project.hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return "SimpleNamespace{" + "wrapped=" + wrapped + ", project=" + project + '}'; // NOI18N
        }
    }

    public static class SimpleClass implements CsmClassifier {

        protected CharSequence name;
        protected String packageName = "";
        protected CharSequence fullName;
        protected CsmDeclaration.Kind kind;
        // a cache
        // our toString() is called very often by JCCellRenderer and is too
        // expensive due to string replace operations and string concatenation
        private String stringValue;
        private CsmClassifier clazz;

        public SimpleClass(CsmClassifier clazz) {
            this.clazz = clazz;
            this.name = clazz.getName();
            this.fullName = clazz.getQualifiedName();
        }

        public SimpleClass(String name, String packageName, CsmDeclaration.Kind kind) {
            this.name = name;
            this.packageName = packageName != null ? packageName : "";
//            if (name == null || packageName == null) {
            this.kind = kind;
            if (name == null || kind == null) {
                throw new NullPointerException(
                        "className=" + name + ", kind=" + kind); // NOI18N
            }
        }

        public SimpleClass(String name, String packageName) {
            this(name, packageName, CsmDeclaration.Kind.BUILT_IN);
        }

        public SimpleClass(String fullName, int packageNameLen, boolean intern) {
            this.fullName = fullName;
            // <> Fix BugId 056449, java.lang.StringIndexOutOfBoundsException: String index out of range: -12
            if (packageNameLen <= 0 || packageNameLen >= fullName.length()) {
                // </>
                name = fullName;
                packageName = ""; // NOI18N
            } else {
                // use interned strings here
                name = fullName.substring(packageNameLen + 1);
                packageName = fullName.substring(0, packageNameLen);
                if (intern) {
                    name = ((String) name).intern();
                    packageName = packageName.intern();
                }
            }
        }

        SimpleClass() {
        }

        @Override
        public final CharSequence getName() {
            if (clazz != null) {
                return clazz.getName();
            }
            return name;
        }

        public final String getPackageName() {
            return packageName;
        }

        @Override
        public CharSequence getQualifiedName() {
            if (clazz != null) {
                return clazz.getQualifiedName();
            }
            if (fullName == null) {
                fullName = (packageName.length() > 0) ? (packageName + "." + name) : name; // NOI18N
            }
            return fullName;
        }

        @Override
        public CharSequence getUniqueName() {
            return "C:"+getQualifiedName(); // NOI18N
        }

        public int getTagOffset() {
            return -1;
        }

        public boolean isInterface() {
            return false;
        }

        public int getModifiers() {
            return 0;
        }

        public CsmClassifier getSuperclass() {
            return null;
        }

        public CsmClassifier[] getInterfaces() {
            return EMPTY_CLASSES;
        }

        public CsmField[] getFields() {
            return EMPTY_FIELDS;
        }

        public CsmConstructor[] getConstructors() {
            return EMPTY_CONSTRUCTORS;
        }

        public CsmMethod[] getMethods() {
            return EMPTY_METHODS;
        }

        public int compareTo(Object o) {
            if (this == o) {
                return 0;
            }
            CsmClassifier c = (CsmClassifier) o;

//XXX            int order = packageName.compareTo(c.getPackageName());
            int order = 0;
            if (order == 0) {
                order = name.toString().compareTo(c.getName().toString());
            }
            return order;
        }

        @Override
        public int hashCode() {
            return name.hashCode() ^ packageName.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CsmClassifier) {
                CsmClassifier c = (CsmClassifier) o;
                String className = (c.getName() == null) ? null : c.getName().toString().replace('.', '$');
                String thisName = name.toString().replace('.', '$');
                return thisName.equals(className);//XXX && packageName.equals(c.getPackageName());
            }
            return false;
        }

        @Override
        public String toString() {
            if (stringValue == null) {
                stringValue = (getPackageName().length() > 0)
                        ? getPackageName() + '.' + getName().toString().replace('.', '$')
                        : getName().toString().replace('.', '$');
            }
            return stringValue;
        }

        @Override
        public CsmDeclaration.Kind getKind() {
            if (clazz != null) {
                return clazz.getKind();
            }
            return kind;
        }

        @Override
        public CsmScope getScope() {
            if (clazz != null) {
                return clazz.getScope();
            }
            return null;
        }

        @Override
        public boolean isValid() {
            return CsmBaseUtilities.isValid(clazz);
        }
    }

    /** Description of the type */
    public static class BaseType implements CsmType {

        protected CsmClassifier clazz;
        protected int arrayDepth;
        protected int pointerDepth;
        protected int reference; // nothing, & or && as in C++11
        protected boolean _const;
        protected boolean _volatile;
        protected boolean templateBased;
        
        private BaseType(CsmClassifier clazz, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile) {
            this(clazz, pointerDepth, reference, arrayDepth, _const, _volatile, CsmKindUtilities.isTemplateParameter(clazz));
        }

        private BaseType(CsmClassifier clazz, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile, boolean templateBased) {
            this.clazz = clazz;
            this.arrayDepth = arrayDepth;
            this.pointerDepth = pointerDepth;
            if (reference > 2) {
                reference = 2;
                CndUtils.assertTrueInConsole(false, "uexpected ref value " + reference + " for " + clazz);
            }
            this.reference = reference;
            this._const = _const;
            this._volatile = _volatile;
            this.templateBased = templateBased;
            if (arrayDepth < 0) {
                throw new IllegalArgumentException("Array depth " + arrayDepth + " < 0."); // NOI18N
            }
        }

        BaseType() {
        }

        @Override
        public int getArrayDepth() {
            return arrayDepth;
        }

        public String format(boolean useFullName) {
            StringBuilder sb = new StringBuilder();
            if(_const) {
                sb.append("const "); // NOI18N
            }
            if (false && this.isInstantiation()) {
                sb.append(CsmInstantiationProvider.getDefault().getInstantiatedText(this));
            } else {
                CsmClassifier classifier = getClassifier();
                if (false && CsmKindUtilities.isTemplate(classifier)) {
                    sb.append(CsmInstantiationProvider.getDefault().getTemplateSignature(((CsmTemplate)classifier)));
                } else {
                    sb.append(classifier.getQualifiedName());
                }
            }
            if (reference==1) {
                sb.append('&');
            } else if (reference==2){
                sb.append("&&"); // NOI18N
            }
            int pd = pointerDepth;
            while (pd > 0) {
                sb.append("*"); // NOI18N
                pd--;
            }
            int ad = arrayDepth;
            while (ad > 0) {
                sb.append("[]"); // NOI18N
                ad--;
            }
            return sb.toString();
        }

        public int compareTo(Object o) {
            if (this == o) {
                return 0;
            }
            CsmType t = (CsmType) o;
            int order = clazz.getQualifiedName().toString().compareTo(t.getClassifier().getQualifiedName().toString());
            if (order == 0) {
                order = arrayDepth - t.getArrayDepth();
            }
            return order;
        }

        @Override
        public int hashCode() {
            return clazz.hashCode() + arrayDepth;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CsmType) {
                CsmType t = (CsmType) o;
                return clazz.equals(t.getClassifier()) &&
                        arrayDepth == t.getArrayDepth() &&
                        pointerDepth == t.getPointerDepth() &&
                        _const == t.isConst();
             }
            return false;
        }

        @Override
        public String toString() {
            return format(true);
        }

        @Override
        public CsmClassifier getClassifier() {
            if (clazz instanceof SimpleClass) {
                return ((SimpleClass) clazz).clazz == null ? clazz : ((SimpleClass) clazz).clazz;
            } else {
                assert clazz != null;
                return clazz;
            }
        }

        @Override
        public List<CsmSpecializationParameter> getInstantiationParams() {
            return Collections.emptyList();
        }

        @Override
        public boolean hasInstantiationParams() {
            return false;
        }

        @Override
        public boolean isInstantiation() {
            return false;
        }

        @Override
        public boolean isTemplateBased() {
            return templateBased;
        }

        @Override
        public CharSequence getClassifierText() {
            return clazz.getName();
        }

        @Override
        public boolean isPointer() {
            return pointerDepth > 0;
        }

        @Override
        public int getPointerDepth() {
            return pointerDepth;
        }

        @Override
        public boolean isReference() {
            return reference != 0;
        }

        @Override
        public boolean isRValueReference() {
            return reference == 2;
        }

        @Override
        public boolean isConst() {
            return _const;
        }

        @Override
        public boolean isVolatile() {
            return _volatile;
        }

        @Override
        public boolean isPackExpansion() {
            return false;
        }

        @Override
        public CharSequence getText() {
            return format(true);
        }

        @Override
        public CharSequence getCanonicalText() {
            return getText();
        }

        @Override
        public CsmFile getContainingFile() {
            if (CsmKindUtilities.isOffsetable(clazz)) {
                return ((CsmOffsetable)clazz).getContainingFile();
            } else {
                return null;
            }
        }

        @Override
        public int getStartOffset() {
            if (CsmKindUtilities.isOffsetable(clazz)) {
                return ((CsmOffsetable)clazz).getStartOffset();
            } else {
                return 0;
            }
        }

        @Override
        public int getEndOffset() {
            if (CsmKindUtilities.isOffsetable(clazz)) {
                return ((CsmOffsetable)clazz).getEndOffset();
            } else {
                return 0;
            }
        }

        @Override
        public CsmOffsetable.Position getStartPosition() {
            return null;
        }

        @Override
        public CsmOffsetable.Position getEndPosition() {
            return null;
        }

        @Override
        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return CsmKindUtilities.isBuiltIn(clazz);
        }
    }

    public static class OffsetableType implements CsmType {

        private final CsmType delegate;
        private final CsmFile container;
        private final int start;
        private final int end;
        private final boolean zeroConst;

        public OffsetableType(BaseType delegate, CsmFile container, int start, int end, boolean zeroConst) {
            assert delegate != null;
            assert container != null;
            this.delegate = delegate;
            this.container = container;
            this.start = start;
            this.end = end;
            this.zeroConst = zeroConst;
        }

        public boolean isZeroConst() {
            return zeroConst;
        }

        @Override
        public int getArrayDepth() {
            return delegate.getArrayDepth();
        }

        @Override
        public int hashCode() {
            return delegate.hashCode() + container.hashCode() + start + end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof CsmType) {
                CsmType t = (CsmType) o;
                return delegate.equals(t) && container.equals(t.getContainingFile()) && (start == t.getStartOffset()) && (end == t.getEndOffset());
            }
            return false;
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public CsmClassifier getClassifier() {
            return delegate.getClassifier();
        }

        @Override
        public List<CsmSpecializationParameter> getInstantiationParams() {
            return delegate.getInstantiationParams();
        }

        @Override
        public boolean hasInstantiationParams() {
            return delegate.hasInstantiationParams();
        }

        @Override
        public boolean isInstantiation() {
            return delegate.isInstantiation();
        }

        @Override
        public boolean isTemplateBased() {
            return delegate.isTemplateBased();
        }

        @Override
        public CharSequence getClassifierText() {
            return delegate.getClassifierText();
        }

        @Override
        public boolean isPointer() {
            return delegate.isPointer();
        }

        @Override
        public int getPointerDepth() {
            return delegate.getPointerDepth();
        }

        @Override
        public boolean isReference() {
            return delegate.isReference();
        }

        @Override
        public boolean isRValueReference() {
            return delegate.isRValueReference();
        }

        @Override
        public boolean isConst() {
            return delegate.isConst();
        }

        @Override
        public boolean isVolatile() {
            return delegate.isVolatile();
        }

        @Override
        public boolean isPackExpansion() {
            return delegate.isPackExpansion();
        }

        @Override
        public CharSequence getText() {
            return delegate.getText();
        }

        @Override
        public CharSequence getCanonicalText() {
            return delegate.getCanonicalText();
        }

        @Override
        public CsmFile getContainingFile() {
            return container;
        }

        @Override
        public int getStartOffset() {
            return start;
        }

        @Override
        public int getEndOffset() {
            return end;
        }

        @Override
        public CsmOffsetable.Position getStartPosition() {
            return null;
        }

        @Override
        public CsmOffsetable.Position getEndPosition() {
            return null;
        }

        @Override
        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return delegate.isBuiltInBased(resolveTypeChain);
        }
    }

    public static class CompletionClosureClassifier implements CsmClosureClassifier {

        private final CsmFunctionDefinition lambda;

        public CompletionClosureClassifier(CsmFunctionDefinition lambda) {
            this.lambda = lambda;
        }

        @Override
        public CsmFunctionDefinition getLambda() {
            return lambda;
        }

        @Override
        public Kind getKind() {
            return CsmDeclaration.Kind.FUNCTION_TYPE;
        }

        @Override
        public CsmScope getScope() {
            return lambda.getScope();
        }

        @Override
        public CharSequence getUniqueName() {
            return lambda.getUniqueName();
        }

        @Override
        public CharSequence getQualifiedName() {
            return lambda.getQualifiedName();
        }

        @Override
        public CsmType getReturnType() {
            return lambda.getReturnType();
        }

        @Override
        public Collection<CsmParameter> getParameters() {
            return lambda.getParameters();
        }

        @Override
        public CharSequence getSignature() {
            return lambda.getSignature();
        }

        @Override
        public CharSequence getName() {
            return lambda.getName();
        }

        @Override
        public boolean isValid() {
            return lambda.isValid();
        }
    }

    public static class CompletionClosureType extends BaseType implements CsmClosureType {

        public CompletionClosureType(CsmFunctionDefinition lambda, int reference, boolean _const, boolean _volatile) {
            super(new CompletionClosureClassifier(lambda), 0, reference, 0, _const, _volatile);
        }

        @Override
        public CsmFunctionDefinition getLambda() {
            return ((CsmClosureClassifier) clazz).getLambda();
        }

        @Override
        public CsmScope getScope() {
            return ((CsmClosureClassifier) clazz).getScope();
        }
    }

    public static int getDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(int newDebugMode) {
        debugMode = newDebugMode;
    }
}
