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
package org.netbeans.modules.javascript2.model.api;

import java.util.*;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.model.JsElementImpl;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class IndexedElement implements JsElement {

    private final JsElement.Kind jsKind;
    private final String fqn;
    private final boolean isAnonymous;
    private final boolean isPlatform;
    private final Collection<TypeUsage> assignments;
    public static final char ANONYMOUS_POSFIX = 'A';
    public static final char OBJECT_POSFIX = 'O';
    public static final char PARAMETER_POSTFIX = 'P';
    private final FileObject fileObject;
    private final String name;
    private final boolean isDeclared;
    private final Set<Modifier> modifiers;
    private final OffsetRange offsetRange;

    public IndexedElement(FileObject fileObject, String name, String fqn, boolean isDeclared, boolean isAnonymous, JsElement.Kind kind, OffsetRange offsetRange, Set<Modifier> modifiers, Collection<TypeUsage> assignments, boolean isPlatform) {
//        super(fileObject, name, isDeclared, offsetRange, modifiers, null);
        this.jsKind = kind;
        this.fqn = fqn;
        this.isAnonymous = isAnonymous;
        this.assignments = assignments;
        this.isPlatform = isPlatform;
        this.fileObject = fileObject;
        this.name = name;
        this.isDeclared = isDeclared;
        this.modifiers = modifiers;
        this.offsetRange = offsetRange;
    }


    @Override
    public Kind getJSKind() {
        return this.jsKind;
    }

    public String getFQN() {
        return this.fqn;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public Collection<TypeUsage> getAssignments() {
        return assignments;
    }

    @Override
    public boolean isPlatform() {
        return isPlatform;
    }

    public static IndexedElement create(IndexResult indexResult) {
        FileObject fo = indexResult.getFile();
        String name = indexResult.getValue(Index.FIELD_BASE_NAME);
        String fqn = getFQN(indexResult);
        int flag = Integer.parseInt(indexResult.getValue(Index.FIELD_FLAG));
        boolean isDeclared = Flag.isDeclared(flag);
        boolean isAnonymous = Flag.isAnonymous(flag);
        JsElement.Kind kind = Flag.getJsKind(flag);
        Set<Modifier> modifiers = Flag.getModifiers(flag);
        int offset = Integer.parseInt(indexResult.getValue(Index.FIELD_OFFSET));
        Collection<TypeUsage> assignments = getAssignments(indexResult);
        boolean isPlatform = Flag.isPlatform(flag);
        IndexedElement result;
        if (!kind.isFunction()) {
            result = new IndexedElement(fo, name, fqn, isDeclared, isAnonymous, kind, offset > -1 ? new OffsetRange(offset, offset + name.length()) : OffsetRange.NONE, modifiers, assignments, isPlatform);
        } else {
            Collection<TypeUsage> returnTypes = getReturnTypes(indexResult);
            Collection<String>rTypes = new ArrayList<>();
            for (TypeUsage type : returnTypes) {
                rTypes.add(type.getType());
            }
            String paramText = indexResult.getValue(Index.FIELD_PARAMETERS);
            LinkedHashMap<String, Collection<String>> params  = decodeParameters(paramText);
            result = new FunctionIndexedElement(fo, name, fqn, new OffsetRange(offset, offset + name.length()), flag, params, rTypes, assignments);
        }
        return result;
    }

//    public static Collection<IndexedElement> createProperties(IndexResult indexResult, String fqn) {
//        Collection<IndexedElement> result = new ArrayList<IndexedElement>();
//        FileObject fo = indexResult.getFile();
//        for(String sProperties : indexResult.getValues(JsIndex.FIELD_PROPERTY)) {
//            String[] split = sProperties.split("#@#");
//            for (int i = 0; i < split.length; i++) {
//                if  (!split[i].isEmpty()) {
//                    result.add(decodeProperty(split[i], fo, fqn));
//                }
//            }
//
//        }
//        return result;
//    }

    public static Collection<TypeUsage> getAssignments(IndexResult indexResult) {
        return getAssignments(indexResult.getValue(Index.FIELD_ASSIGNMENTS));
    }

    public static String getFQN(IndexResult indexResult) {
        String fqn = indexResult.getValue(Index.FIELD_FQ_NAME);
        fqn = fqn.substring(0, fqn.length() - 1);
        return fqn;
    }

    private static Collection<TypeUsage> getAssignments(String sAssignments) {
        Collection<TypeUsage> result = new ArrayList<>();
        if (sAssignments != null) {
            for (StringTokenizer st = new StringTokenizer(sAssignments, "|"); st.hasMoreTokens();) {
                String token = st.nextToken();
                String[] parts = token.split(":");
                if (parts.length > 2) {
                    String type = parts[0];
                    String sOffset = parts[1];
                    int offset;
                    try {
                        offset = Integer.parseInt(sOffset);
                    } catch (NumberFormatException nfe) {
                        offset = -1;
                    }
                    boolean resolve = parts[2].equals("1");
                    result.add(new TypeUsage(type, offset, resolve));
                }
            }
        }
        return result;
    }

    public static Collection<TypeUsage> getReturnTypes(IndexResult indexResult) {
        return getTypes(indexResult, Index.FIELD_RETURN_TYPES);
    }

    public static Collection<TypeUsage> getArrayTypes(IndexResult indexResult) {
        return getTypes(indexResult, Index.FIELD_ARRAY_TYPES);
    }

    public static Collection<TypeUsage> getTypes(IndexResult indexResult, String field) {
        Collection<TypeUsage> result = new ArrayList<>();
        String text = indexResult.getValue(field);
        if (text != null) {
            for (StringTokenizer st = new StringTokenizer(text, "|"); st.hasMoreTokens();) {
                String token = st.nextToken();
                String[] parts = token.split(",");
                if (parts.length > 2) {
                    int offset;
                    try {
                        offset = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException nfe) {
                        offset = -1;
                    }
                    boolean resolve = parts[2].equals("1");
                    result.add(new TypeUsage(parts[0], offset, resolve));
                }
            }
        }
        return result;
    }

    private static LinkedHashMap<String, Collection<String>> decodeParameters(String paramsText) {
        LinkedHashMap<String, Collection<String>> parameters = new LinkedHashMap<>();
        for (StringTokenizer stringTokenizer = new StringTokenizer(paramsText, ","); stringTokenizer.hasMoreTokens();) {
            String param = stringTokenizer.nextToken();
            int index = param.indexOf(':');
            Collection<String> types = new ArrayList<>();
            String paramName;
            if (index > 0) {
                paramName = param.substring(0, index);
                String typesText = param.substring(index + 1);
                for (StringTokenizer stParamType = new StringTokenizer(typesText, "|"); stParamType.hasMoreTokens();) {
                    types.add(stParamType.nextToken());
                }
            } else {
                paramName = param;
            }
            parameters.put(paramName, types);
        }
        return parameters;
    }

    @Override
    public int getOffset() {
        return offsetRange.getStart();
    }

    @Override
    public OffsetRange getOffsetRange() {
        return offsetRange;
    }

    @Override
    public boolean isDeclared() {
        return isDeclared;
    }

    @Override
    public String getSourceLabel() {
        return null;
    }

    @Override
    public FileObject getFileObject() {
        return fileObject;
    }

    @Override
    public String getMimeType() {
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIn() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return JsElementImpl.convertJsKindToElementKind(jsKind);
    }

    @Override
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        return false;
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return getOffsetRange();
    }

    public static class FunctionIndexedElement extends IndexedElement {
        private final LinkedHashMap<String, Collection<String>> parameters;
        private final Collection<String> returnTypes;

        public FunctionIndexedElement(FileObject fileObject, String name, String fqn,OffsetRange offsetRange, int flag,  LinkedHashMap<String, Collection<String>> parameters, Collection<String> returnTypes, Collection<TypeUsage> assignments) {
            super(fileObject, name, fqn, Flag.isDeclared(flag), Flag.isAnonymous(flag), Flag.getJsKind(flag), offsetRange, Flag.getModifiers(flag), assignments, Flag.isPlatform(flag));
            this.parameters = parameters;
            this.returnTypes = returnTypes;
        }

        public LinkedHashMap<String, Collection<String>> getParameters() {
            return this.parameters;
        }

        public Collection<String> getReturnTypes() {
            return this.returnTypes;
        }
    }

    public static class Flag {
        // modifiers
        private static final int PRIVATE = 1 << 0;
        private static final int PUBLIC = 1 << 1;
        private static final int STATIC = 1 << 2;
        private static final int PRIVILAGE = 1 << 3;

        private static final int DEPRICATED = 1 << 4;

        private static final int GLOBAL = 1 << 5;
        private static final int DECLARED = 1 << 6;
        private static final int ANONYMOUS = 1 << 7;

        // Js Kind
        private static final int FILE = 1 << 8;
        private static final int PROPERTY = 1 << 9;
        private static final int VARIABLE = 1 << 10;
        private static final int OBJECT = 1 << 11;
        private static final int METHOD = 1 << 12;
        private static final int FUNCTION = 1 << 13;
        private static final int ANONYMOUS_OBJECT = 1 << 14;
        private static final int CONSTRUCTOR = 1 << 15;
        private static final int FIELD = 1 << 16;
        private static final int PARAMETER = 1 << 17;
        private static final int PROPERTY_GETTER = 1 << 18;
        private static final int PROPERTY_SETTER = 1 << 19;
        private static final int CALLBACK = 1 << 22;
        private static final int GENERATOR = 1 << 23;
        private static final int CONSTANT = 1 << 24;
        private static final int ARROW_FUNCTION = 1 << 25;

        private static final int PLATFORM = 1 << 20;

        // added later :(
        private static final int OBJECT_LITERAL = 1 << 21;

        public static int getFlag(JsObject object) {
            int value = 0;

            Set<Modifier> modifiers = object.getModifiers();
            if(modifiers.contains(Modifier.PRIVATE)) value |= PRIVATE;
            if(modifiers.contains(Modifier.PUBLIC)) value |= PUBLIC;
            if(modifiers.contains(Modifier.STATIC)) value |= STATIC;
            if(modifiers.contains(Modifier.PROTECTED)) value |= PRIVILAGE;
            if(modifiers.contains(Modifier.DEPRECATED)) value |= DEPRICATED;

            if(ModelUtils.isGlobal(object)) value |= GLOBAL;
            if(object.isDeclared()) value |= DECLARED;
            if(object.isAnonymous()) value |= ANONYMOUS;

            JsElement.Kind kind = object.getJSKind();
            if (kind == JsElement.Kind.ANONYMOUS_OBJECT) value |= ANONYMOUS_OBJECT;
            if (kind == JsElement.Kind.CONSTRUCTOR) value |= CONSTRUCTOR;
            if (kind == JsElement.Kind.FIELD) value |= FIELD;
            if (kind == JsElement.Kind.FILE) value |= FILE;
            if (kind == JsElement.Kind.FUNCTION) value |= FUNCTION;
            if (kind == JsElement.Kind.METHOD) value |= METHOD;
            if (kind == JsElement.Kind.OBJECT) value |= OBJECT;
            if (kind == JsElement.Kind.PARAMETER) value |= PARAMETER;
            if (kind == JsElement.Kind.PROPERTY) value |= PROPERTY;
            if (kind == JsElement.Kind.PROPERTY_GETTER) value |= PROPERTY_GETTER;
            if (kind == JsElement.Kind.PROPERTY_SETTER) value |= PROPERTY_SETTER;
            if (kind == JsElement.Kind.VARIABLE) value |= VARIABLE;
            if (kind == JsElement.Kind.OBJECT_LITERAL) value |= OBJECT_LITERAL;
            if (kind == JsElement.Kind.CALLBACK) value |= CALLBACK;
            if (kind == JsElement.Kind.GENERATOR) value |= GENERATOR;
            if (kind == JsElement.Kind.CONSTANT) value |= CONSTANT;
            if (kind == JsElement.Kind.ARROW_FUNCTION) value |= ARROW_FUNCTION;

            if (object.isPlatform()) value |= PLATFORM;

            return value;
        }

        public static Set<Modifier> getModifiers(int flag) {
            EnumSet<Modifier> result = EnumSet.noneOf(Modifier.class);
            if ((flag & PRIVATE) != 0) result.add(Modifier.PRIVATE);
            if ((flag & PUBLIC) != 0) result.add(Modifier.PUBLIC);
            if ((flag & STATIC) != 0) result.add(Modifier.STATIC);
            if ((flag & PRIVILAGE) != 0) result.add(Modifier.PROTECTED);
            if ((flag & DEPRICATED) != 0) result.add(Modifier.DEPRECATED);
            return result;
        }

        public static boolean isGlobal(int flag) {
            return (flag & GLOBAL) != 0;
        }

        public static boolean isDeclared(int flag) {
            return (flag & DECLARED) != 0;
        }

        public static boolean isAnonymous(int flag) {
            return (flag & ANONYMOUS) != 0;
        }

        public static boolean isPlatform(int flag) {
            return (flag & PLATFORM) != 0;
        }

        public static JsElement.Kind getJsKind(int flag) {
            JsElement.Kind result = JsElement.Kind.VARIABLE;
            if ((flag & ANONYMOUS_OBJECT) != 0) result = JsElement.Kind.ANONYMOUS_OBJECT;
            else if ((flag & CONSTRUCTOR) != 0) result = JsElement.Kind.CONSTRUCTOR;
            else if ((flag & FIELD) != 0) result = JsElement.Kind.FIELD;
            else if ((flag & FILE) != 0) result = JsElement.Kind.FILE;
            else if ((flag & FUNCTION) != 0) result = JsElement.Kind.FUNCTION;
            else if ((flag & METHOD) != 0) result = JsElement.Kind.METHOD;
            else if ((flag & OBJECT) != 0) result = JsElement.Kind.OBJECT;
            else if ((flag & PARAMETER) != 0) result = JsElement.Kind.PARAMETER;
            else if ((flag & PROPERTY) != 0) result = JsElement.Kind.PROPERTY;
            else if ((flag & PROPERTY_GETTER) != 0) result = JsElement.Kind.PROPERTY_GETTER;
            else if ((flag & PROPERTY_SETTER) != 0) result = JsElement.Kind.PROPERTY_SETTER;
            else if ((flag & VARIABLE) != 0) result = JsElement.Kind.VARIABLE;
            else if ((flag & OBJECT_LITERAL) != 0) result = JsElement.Kind.OBJECT_LITERAL;
            else if ((flag & CALLBACK) != 0) result = JsElement.Kind.CALLBACK;
            else if ((flag & GENERATOR) != 0) result = JsElement.Kind.GENERATOR;
            else if ((flag & CONSTANT) != 0) result = JsElement.Kind.CONSTANT;
            else if ((flag & ARROW_FUNCTION) != 0) result = JsElement.Kind.ARROW_FUNCTION;
            return result;
        }
    }
}
