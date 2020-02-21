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
package org.netbeans.modules.cnd.refactoring.hints.infrastructure;

import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;

/**
 * based on org.netbeans.modules.java.hints.errors.Utilities
 */
public class Utilities {

    private static final String DEFAULT_NAME = "name"; // NOI18N

    private Utilities() {
    }
    
    public static CsmClass extractEnclosingClass(CsmContext editorContext) {
        if (editorContext == null) {
            return null;
        }
        CsmClass cls = editorContext.getEnclosingClass();
        if (cls == null) {
            CsmFunction fun = editorContext.getEnclosingFunction();
            if (fun != null && CsmKindUtilities.isMethod(fun)) {
                cls = ((CsmMethod) CsmBaseUtilities.getFunctionDeclaration(fun)).getContainingClass();
            }
        }
        return cls;
    }

    public static String guessName(CsmContext info) {
        CsmReference ref = CsmReferenceResolver.getDefault().findReference(info.getFile(), info.getDocument(), info.getStartOffset());
        if (ref != null) {
            return ref.getText().toString();
        } else {
            return DEFAULT_NAME;
        }
//        ExpressionTree et = (ExpressionTree) tp.getLeaf();
//        String name = getName(et);
//
//        if (name == null) {
//            if (et instanceof LiteralTree) {
//                Object guess = ((LiteralTree) et).getValue();
//                if (guess != null && guess instanceof String) {
//                    return guessLiteralName((String) guess);
//                }
//            }
//            return DEFAULT_NAME;
//        }
//
//        Scope s = info.getTrees().getScope(tp);
//        int counter = 0;
//        boolean cont = true;
//        String proposedName = name;
//
//        while (cont) {
//            proposedName = name + (counter != 0 ? String.valueOf(counter) : "");
//
//            cont = false;
//
//            for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new VariablesFilter())) {
//                if (proposedName.equals(e.getSimpleName().toString())) {
//                    counter++;
//                    cont = true;
//                    break;
//                }
//            }
//        }
//
//        return proposedName;
    }

    private static String guessLiteralName(String str) {
        if (str.length() == 0) {
            return DEFAULT_NAME;
        }
        StringBuilder sb = new StringBuilder();
        char first = str.charAt(0);
        if (CndLexerUtilities.isCppIdentifierStart(str.charAt(0))) {
            sb.append(first);
        }

        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == ' ') {
                sb.append('_');
                continue;
            }
            if (Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
            }
            if (i > 40) {
                break;
            }
        }
        if (sb.length() == 0) {
            return DEFAULT_NAME;
        } else {
            return sb.toString();
        }
    }

    public static String getName(CsmType tm) {
        if (CsmKindUtilities.isBuiltIn(tm)) {
            return "" + Character.toLowerCase(tm.getClassifierText().charAt(0));
        }

        return firstToLower(tm.getClassifierText().toString());
    }

    public static String getName(CsmContext info) {
        CsmReferenceContext ref = null;
        return getName(ref);
    }

    public static String getName(CsmReferenceContext et) {
        return adjustName(getNameRaw(et));
    }

    private static String getNameRaw(CsmReferenceContext et) {
        if (et == null) {
            return null;
        }
        return et.getReference().getText().toString();
//        switch (et.getKind()) {
//            case IDENTIFIER:
//                return ((IdentifierTree) et).getName().toString();
//            case METHOD_INVOCATION:
//                return getName(((MethodInvocationTree) et).getMethodSelect());
//            case MEMBER_SELECT:
//                return ((MemberSelectTree) et).getIdentifier().toString();
//            case NEW_CLASS:
//                return firstToLower(getName(((NewClassTree) et).getIdentifier()));
//            case PARAMETERIZED_TYPE:
//                return firstToLower(getName(((ParameterizedTypeTree) et).getType()));
//            default:
//                return null;
//        }
    }

    static String adjustName(String name) {
        if (name == null) {
            return null;
        }

        String shortName = null;

        if (name.startsWith("get") && name.length() > 3) { // NOI18N
            shortName = name.substring(3);
        }

        if (name.startsWith("is") && name.length() > 2) { // NOI18N
            shortName = name.substring(2);
        }

        if (shortName != null) {
            return firstToLower(shortName);
        }

        if (isKeyword(name)) {
            return "a" + Character.toUpperCase(name.charAt(0)) + name.substring(1); // NOI18N
        } else {
            return name;
        }
    }

    private static String firstToLower(String name) {
        if (name.length() == 0) {
            return null;
        }

        String cand = Character.toLowerCase(name.charAt(0)) + name.substring(1);

        if (isKeyword(cand)) {
            cand = "a" + name; // NOI18N
        }

        return cand;
    }

    private static boolean isKeyword(String can) {
        return true;
    }

//    private static final class VariablesFilter implements ElementAcceptor {
//
//        private static final Set<ElementKind> ACCEPTABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
//
//        public boolean accept(Element e, TypeMirror type) {
//            return ACCEPTABLE_KINDS.contains(e.getKind());
//        }
//    }
//
//    /**
//     * Commits changes and provides selection bounds
//     *
//     * @param target target FileObject
//     * @param diff set of changes made by ModificationTask
//     * @param tag mark used for selection of generated text
//     * @return set of changes made by hint
//     * @throws java.io.IOException
//     */
//    public static ChangeInfo commitAndComputeChangeInfo(FileObject target, final ModificationResult diff, final Object tag) throws IOException {
//        List<? extends Difference> differences = diff.getDifferences(target);
//        ChangeInfo result = null;
//
//        diff.commit();
//
//        try {
//            if (differences != null) {
//                for (Difference d : differences) {
//                    if (d.getNewText() != null) { //to filter out possible removes
//                        final PositionRef start = d.getStartPosition();
//                        Document doc = start.getCloneableEditorSupport().getDocument();
//
//                        if (doc == null) {
//                            doc = start.getCloneableEditorSupport().openDocument();
//                        }
//
//                        final Position[] pos = new Position[2];
//                        final Document fdoc = doc;
//
//                        doc.render(new Runnable() {
//
//                            public void run() {
//                                try {
//                                    int[] span = diff.getSpan(tag);
//                                    if (span != null) {
//                                        pos[0] = fdoc.createPosition(span[0]);
//                                        pos[1] = fdoc.createPosition(span[1]);
//                                    } else {
//                                        pos[0] = NbDocument.createPosition(fdoc, start.getOffset(), Position.Bias.Backward);
//                                        pos[1] = pos[0];
//                                    }
//                                } catch (BadLocationException ex) {
//                                    Exceptions.printStackTrace(ex);
//                                }
//                            }
//                        });
//
//                        if (pos[0] != null) {
//                            result = new ChangeInfo(target, pos[0], pos[1]);
//                        }
//
//                        break;
//                    }
//                }
//            }
//        } catch (IOException e) {
//            Exceptions.printStackTrace(e);
//        }
//
//        return result;
//    }
//
//    public static boolean isMethodHeaderInsideGuardedBlock(CompilationInfo info, MethodTree method) {
//        try {
//            Document doc = info.getDocument();
//
//            if (doc instanceof GuardedDocument) {
//                GuardedDocument bdoc = (GuardedDocument) doc;
//                int methodStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), method);
//                int methodEnd = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), method);
//
//                return (bdoc.getGuardedBlockChain().compareBlock(methodStart, methodEnd) & MarkBlock.OVERLAP) != 0;
//            }
//
//            return false;
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//            return false;
//        }
//    }
//
//    public static TypeMirror resolveCapturedType(CompilationInfo info, TypeMirror tm) {
//        TypeMirror type = resolveCapturedTypeInt(info, tm);
//
//        if (type.getKind() == TypeKind.WILDCARD) {
//            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
//            if (tmirr != null) {
//                return tmirr;
//            } else { //no extends, just '?'
//                return info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
//            }
//
//        }
//
//        return type;
//    }
//
//    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
//        TypeMirror orig = SourceUtils.resolveCapturedType(tm);
//
//        if (orig != null) {
//            return orig;
//        }
//
//        if (tm.getKind() == TypeKind.DECLARED) {
//            DeclaredType dt = (DeclaredType) tm;
//            List<TypeMirror> typeArguments = new LinkedList<TypeMirror>();
//
//            for (TypeMirror t : dt.getTypeArguments()) {
//                typeArguments.add(resolveCapturedTypeInt(info, t));
//            }
//
//            return info.getTypes().getDeclaredType((TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
//        }
//
//        return tm;
//    }
//
//    public static <T extends Tree> T copyComments(WorkingCopy wc, Tree from, T to) {
//        TreeMaker make = wc.getTreeMaker();
//
//        for (Comment c : wc.getTreeUtilities().getComments(from, true)) {
//            make.addComment(to, c, true);
//        }
//
//        for (Comment c : wc.getTreeUtilities().getComments(from, false)) {
//            make.addComment(to, c, false);
//        }
//
//        return to;
//    }
//
//    /**
//     * Convert typemirror of an anonymous class to supertype/iface
//     *
//     * @return typemirror of supertype/iface, initial tm if not anonymous
//     */
//    public static TypeMirror convertIfAnonymous(TypeMirror tm) {
//        //anonymous class?
//        Set<ElementKind> fm = EnumSet.of(ElementKind.METHOD, ElementKind.FIELD);
//        if (tm instanceof DeclaredType) {
//            Element el = ((DeclaredType) tm).asElement();
//            if (el.getSimpleName().length() == 0 || fm.contains(el.getEnclosingElement().getKind())) {
//                List<? extends TypeMirror> interfaces = ((TypeElement) el).getInterfaces();
//                if (interfaces.isEmpty()) {
//                    tm = ((TypeElement) el).getSuperclass();
//                } else {
//                    tm = interfaces.get(0);
//                }
//            }
//        }
//        return tm;
//    }
}
