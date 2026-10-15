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

package org.netbeans.modules.java.editor.javadoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 * Represents a reference to java element.
 * E.g. &#123;@link String#charAt(int) } where {@code fqn} is {@code String},
 * {@code member} is {@code charAt}
 *
 * @author Jan Pokorsky
 */
public final class JavaReference {
    CharSequence fqn;
    CharSequence member;
    CharSequence tag;
    List<JavaReference> parameters;
    int begin = -1; // inclusive
    int end = -1; // exclusive
    private int tagEndPosition;
    private String paramsText;

    private JavaReference() {
    }

    @Override
    public String toString() {
        return String.format(
                "fqn: %1$s, member: %2$s, [%3$d, %4$d]", // NOI18N
                fqn, member, begin, end);
    }
    
    /**
     * 
     * @param jdts token sequence to analyze
     * @param offset offset of the first token to resolve
     * @return reference
     */
    public static JavaReference resolve(TokenSequence<JavadocTokenId> jdts, int offset, int tagEndPosition) {
        JavaReference ref = new JavaReference();
        ref.tagEndPosition = tagEndPosition;
        jdts.move(offset);
        ref.insideFQN(jdts);
        return ref;
    }

    public List<JavaReference> getAllReferences () {
        if (parameters == null)
            return Collections.<JavaReference>singletonList (this);
        List<JavaReference> references = new ArrayList<JavaReference> ();
        references.add (this);
        references.addAll (parameters);
        return references;
    }

    public Element getReferencedElement(CompilationInfo javac, TypeElement scope) {
        if (!isReference()) {
            return null;
        }
        Element result = null;
        TypeElement declaredElement = null;
        if (fqn != null && fqn.length() > 0) {
            TypeMirror type = javac.getTreeUtilities().parseType(fqn.toString(), scope);
            if (type != null) {
                switch(type.getKind()) {
                    case DECLARED:
                    case UNION:
                        declaredElement = (TypeElement) ((DeclaredType) type).asElement();
                        result = declaredElement;
                        break;
                    case TYPEVAR:
                        result = ((TypeVariable) type).asElement();
                        break;
                    default:
                        return null;
                }
            } else {
                return null;
            }
        } else {
            declaredElement = scope;
        }

        if (declaredElement != null && member != null && member.length() > 0) {
            String[] paramarr;
            String memName = member.toString();
            Element referencedMember;
            
            if (paramsText != null) {
                // has parameter list -- should be method or constructor
                paramarr = new ParameterParseMachine(paramsText).parseParameters();
                if (paramarr != null) {
                    List<TypeMirror> types = new ArrayList<>(paramarr.length);
                    for (int i = 0; i < paramarr.length; i++) {
                        String paramType = paramarr[i];
                        types.add(javac.getTreeUtilities().parseType(paramType, scope));
                    }
                    referencedMember = findExecutableMember(declaredElement, memName, types, javac.getTypes());
                } else {
                    referencedMember = null;
                }
            } else {
                // no parameter list -- should be field
                referencedMember = findExecutableMember(declaredElement, memName, Collections.emptyList(), javac.getTypes());
                Element fd = findField(declaredElement, memName);
                // when no args given, prefer fields over methods
                if (referencedMember == null || (fd != null
                        && javac.getTypes().isSubtype(fd.getEnclosingElement().asType(), referencedMember.getEnclosingElement().asType()))) {
                    referencedMember = fd;
                }
            }
            if (referencedMember != null) {
                result = referencedMember;
            }
        }
        return result;
    }

    boolean isReference() {
        return begin > 0;
    }

    private void insideMember(TokenSequence<JavadocTokenId> jdts) {
        Token<JavadocTokenId> token;
        if (!jdts.moveNext() || JavadocTokenId.IDENT != (token = jdts.token()).id()) {
            return;
        }
        // member identifier
        member = token.text ();
        end = jdts.offset() + token.length();
        
        // params part (int, String)
        if (!jdts.moveNext ()) return;
        token = jdts.token ();
        if (JavadocTokenId.OTHER_TEXT != token.id ()) return;
        CharSequence cs = token.text ();
        if (cs.length () == 0 ||
            cs.charAt (0) != '(') {
            // no params
            return;
        }

        StringBuilder params = new StringBuilder();
        while (jdts.offset() < tagEndPosition) {
            int len = tagEndPosition - jdts.offset();
            cs = len > 0
                    ? token.text()
                    : token.text().subSequence(0, len);
            if (token.id () == JavadocTokenId.IDENT) {
                JavaReference parameter = JavaReference.resolve (
                    jdts,
                    jdts.offset(),
                    jdts.offset() + len
                );
                if (parameters == null)
                    parameters = new ArrayList<JavaReference> ();
                parameters.add (parameter);
                if (parameter.fqn != null) {
                    params.append(parameter.fqn);
                } else {
                    params.append(cs);
                }
            } else {
                params.append(cs);
            }
            if (params.indexOf (")") > 0)
                break;
            if (!jdts.moveNext()) {
                break;
            }
            token = jdts.token();
        }
        paramsText = parseParamString(params);
    }

    private void insideFQN (
        TokenSequence<JavadocTokenId> tokenSequence
    ) {
        StringBuilder sb = new StringBuilder ();
        STOP: while (tokenSequence.moveNext ()) {
            Token<JavadocTokenId> token = tokenSequence.token();
            switch(token.id()) {
                case IDENT:
                    sb.append(token.text());
                    if (begin < 0) {
                        begin = tokenSequence.offset();
                    }
                    end = tokenSequence.offset() + token.length();
                    break;
                case HASH:
                    if (begin < 0) {
                        begin = tokenSequence.offset();
                    }
                    end = tokenSequence.offset() + token.length();
                    insideMember(tokenSequence);
                    break STOP;
                case DOT:
                    if (sb.length() == 0 || '.' == sb.charAt(sb.length() - 1)) {
                        break STOP;
                    }
                    sb.append('.');
                    end = tokenSequence.offset() + token.length();
                    break;
                default:
                    tokenSequence.movePrevious ();
                    break STOP;
            }
        }
        if (sb.length() > 0) {
            fqn = sb;
        }
    }
    
    private String parseParamString(CharSequence text) {
        int len = text.length();
        if (len == 0 || text.charAt(0) != '(') {
            return null;
        }

        // check that the text is param list with possible parentheses

        // the code assumes that there is no initial white space.
        int parens = 0;
        int commentstart = 0;
        int start = 0;
        int cp;
        for (int i = start; i < len ; i += Character.charCount(cp)) {
            cp = Character.codePointAt(text, i);
            switch (cp) {
                case '(': parens++; break;
                case ')': parens--; break;
                case '[': case ']': case '.': case '#': break;
                case ',':
                    if (parens <= 0) {
//                        docenv().warning(holder,
//                                         "tag.see.malformed_see_tag",
//                                         name, text);
                        return null;
                    }
                    break;
                case ' ': case '\t': case '\n':
                    if (parens == 0) { //here onwards the comment starts.
                        commentstart = i;
                        i = len;
                    }
                    break;
                default:
//                    if (!Character.isJavaIdentifierPart(cp)) {
//                        docenv().warning(holder,
//                                         "tag.see.illegal_character",
//                                         name, ""+cp, text);
//                    }
                    break;
            }
        }
        if (parens != 0) {
//            docenv().warning(holder,
//                             "tag.see.malformed_see_tag",
//                             name, text);
            return null;
        }

        String params;

        if (commentstart > 0) {
            params = text.subSequence(start, commentstart).toString();
        } else {
            params = text.toString();
        }
        return params;
    }
    
    // separate "int, String" from "(int, String)"
    // (int i, String s) ==> [0] = "int",  [1] = String
    // (int[][], String[]) ==> [0] = "int[][]" // [1] = "String[]"
    private static final class ParameterParseMachine {

        final int START = 0;
        final int TYPE = 1;
        final int NAME = 2;
        final int TNSPACE = 3;  // space between type and name

        final int ARRAYDECORATION = 4;
        final int ARRAYSPACE = 5;
        String parameters;
        StringBuilder typeId;
        List<String> paramList;

        ParameterParseMachine(String parameters) {
            this.parameters = parameters;
            this.paramList = new ArrayList<String>();
            typeId = new StringBuilder();
        }

        public String[] parseParameters() {
            if (parameters.equals("()")) { // NOI18N

                return new String[0];
            }   // now strip off '(' and ')'

            int state = START;
            int prevstate = START;
            parameters = parameters.substring(1, parameters.length() - 1);
            int cp;
            for (int index = 0; index < parameters.length(); index += Character.charCount(cp)) {
                cp = parameters.codePointAt(index);
                switch (state) {
                    case START:
                        if (Character.isJavaIdentifierStart(cp)) {
                            typeId.append(Character.toChars(cp));
                            state = TYPE;
                        }
                        prevstate = START;
                        break;
                    case TYPE:
                        if (Character.isJavaIdentifierPart(cp) || cp == '.') {
                            typeId.append(Character.toChars(cp));
                        } else if (cp == '[') {
                            typeId.append('[');
                            state = ARRAYDECORATION;
                        } else if (Character.isWhitespace(cp)) {
                            state = TNSPACE;
                        } else if (cp == ',') {  // no name, just type

                            addTypeToParamList();
                            state = START;
                        }
                        prevstate = TYPE;
                        break;
                    case TNSPACE:
                        if (Character.isJavaIdentifierStart(cp)) { // name

                            if (prevstate == ARRAYDECORATION) {
                                // missing comma space
                                return (String[]) null;
                            }
                            addTypeToParamList();
                            state = NAME;
                        } else if (cp == '[') {
                            typeId.append('[');
                            state = ARRAYDECORATION;
                        } else if (cp == ',') {   // just the type

                            addTypeToParamList();
                            state = START;
                        } // consume rest all

                        prevstate = TNSPACE;
                        break;
                    case ARRAYDECORATION:
                        if (cp == ']') {
                            typeId.append(']');
                            state = TNSPACE;
                        } else if (!Character.isWhitespace(cp)) {
                            // illegal char in arr dim
                            return (String[]) null;
                        }
                        prevstate = ARRAYDECORATION;
                        break;
                    case NAME:
                        if (cp == ',') {  // just consume everything till ','

                            state = START;
                        }
                        prevstate = NAME;
                        break;
                }
            }
            if (state == ARRAYDECORATION ||
                    (state == START && prevstate == TNSPACE)) {
                // illegal see tag
            }
            if (typeId.length() > 0) {
                paramList.add(typeId.toString());
            }
            return paramList.toArray(new String[0]);
        }

        void addTypeToParamList() {
            if (typeId.length() > 0) {
                paramList.add(typeId.toString());
                typeId.setLength(0);
            }
        }
    }
    
    private ExecutableElement findExecutableMember(TypeElement clazz, String methodName, List<TypeMirror> paramTypes, Types types) {
        List<ExecutableElement> members = methodName.contentEquals(clazz.getSimpleName())
                ? ElementFilter.constructorsIn(clazz.getEnclosedElements())
                : ElementFilter.methodsIn(clazz.getEnclosedElements());
        outer: for (ExecutableElement ee : members) {
            if (ee.getKind() == ElementKind.CONSTRUCTOR || methodName.contentEquals(ee.getSimpleName())) {
                List<? extends TypeMirror> memberParamTypes = ((ExecutableType) ee.asType()).getParameterTypes();
                if (memberParamTypes.size() == paramTypes.size()) {
                    Iterator<TypeMirror> it = paramTypes.iterator();
                    for (TypeMirror memberParamType : memberParamTypes) {
                        TypeMirror type = it.next();
                        if (types.isSameType(type, memberParamType)) {
                            continue outer;
                        }
                    }
                    return ee;
                }
            }
        }
        return null;
    }
    
    private VariableElement findField(TypeElement clazz, String fieldName) {
        for (VariableElement field : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
            if (fieldName.contentEquals(field.getSimpleName())) {
                return field;
            }
        }
        return null;
    }
}
