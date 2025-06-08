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
package org.netbeans.modules.java.hints.errors;

import java.util.logging.Level;
import java.io.CharConversionException;
import org.openide.xml.XMLUtil;
import java.util.logging.Logger;
import javax.lang.model.element.Name;
import com.sun.source.tree.ThrowTree;
import java.util.Stack;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.TryTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.TreeUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumMap;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import static com.sun.source.tree.Tree.Kind.*;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import java.net.URI;
import javax.lang.model.element.NestingKind;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.UnionType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyleUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.Pair;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {
    public  static final String JAVA_MIME_TYPE = "text/x-java";
    private static final String DEFAULT_NAME = "name";
    private static final String UNDERSCORE = "_";
    enum SWITCH_TYPE { TRADITIONAL_SWITCH, RULE_SWITCH, SWITCH_EXPRESSION }

    public Utilities() {
    }

    public static String guessName(CompilationInfo info, TreePath tp) {
        return guessName(info, tp, tp);
    }

    public static String guessName(CompilationInfo info, TreePath tp, TreePath scope) {
        return guessName(info, tp, scope, null, null);
    }
    
    public static String guessName(CompilationInfo info, TreePath tp, TreePath scope, String prefix, String suffix) {
        return guessName(info, org.netbeans.modules.editor.java.Utilities.varNameSuggestion(tp.getLeaf()), scope, prefix, suffix, false);
    }
    
    public static String guessName(CompilationInfo info, String name, TreePath scope, String prefix, String suffix, boolean acceptExistingPrefixes) {
        
        if (name == null) {
            return DEFAULT_NAME;
        }
        
        Scope s = info.getTrees().getScope(scope);
        
        return makeNameUnique(info, s, name, Collections.<String>emptySet(), prefix, suffix, acceptExistingPrefixes);
    }
    
    private static final Map<String, String> TYPICAL_KEYWORD_CONVERSIONS = new HashMap<String, String>() {{
        put("class", "clazz");
        put("interface", "intf");
        put("new", "nue");
        put("static", "statik");
    }};
    
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, String prefix, String suffix) {
        return makeNameUnique(info, s, name, Collections.<String>emptySet(), prefix, suffix);
    }
    
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, Set<String> usedVariables, String prefix, String suffix) {
        return makeNameUnique(info, s, name, usedVariables, prefix, suffix, false);
    }
    
    /**
     * Creates an unique name.
     * The method takes the proposed `name' and ensures it is unique with respect to `usedVariables' and contents of the target scope `s'.
     * The `prefix' and `suffix' are joined with the base name. If prefix ends with a letter and name starts with letter, the resulting name
     * will be converted to CamelCase according to coding conventions. If `acceptExisting' is true, the name will not be decorated, if it
     * already contains both prefix AND suffix. Names that are the same as keywords are avoided and changed.
     * 
     * @param info compilation info
     * @param s target scope for uniqueness checks
     * @param name proposed base name
     * @param usedVariables other to-be-introduced names, in addition to scope contents, to be avoided
     * @param prefix the desired prefix or {@code null}
     * @param suffix the desired suffix or {@code null}
     * @param acceptExisting true, if existing prefix and suffix in the `name' should be accepted
     * @return unique name that contains the prefix and suffix if they are specified.
     */
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, Set<String> usedVariables, String prefix, String suffix, boolean acceptExisting) {
        boolean prefixOK = false;
        boolean suffixOK = false;
        
        if (acceptExisting) {
            if (prefix != null) {
                if (!(prefixOK = prefix.isEmpty())) {
                    // prefixOK is now false
                    if (name.startsWith(prefix)) {
                        int pl = prefix.length();
                        if(Character.isAlphabetic(prefix.charAt(pl-1))) {
                            if (name.length() > pl && Character.isUpperCase(name.charAt(pl))) {
                                prefixOK = true;
                            }
                        } else {
                            prefixOK = true;
                        }
                    }
                }
            }
            if (suffix != null && (suffix.isEmpty() || name.endsWith(suffix))) {
                suffixOK = true;
            }
        }
        if (prefixOK && suffixOK) {
            prefix = suffix = ""; // NOI18N
        }
        if(prefix != null && prefix.length() > 0) {
            if(Character.isAlphabetic(prefix.charAt(prefix.length()-1))) {
                StringBuilder nameSb = new StringBuilder(name);
                nameSb.setCharAt(0, Character.toUpperCase(nameSb.charAt(0)));
                name = nameSb.toString();
            }
        }
        
        boolean cont;
        String proposedName;
        int counter = 0;
        do {
            proposedName = safeString(prefix) + name + (counter != 0 ? String.valueOf(counter) : "") + safeString(suffix);
            
            cont = false;
            
            String converted = TYPICAL_KEYWORD_CONVERSIONS.get(proposedName);
            
            if (converted != null) {
                proposedName = converted;
            }
            
            if (SourceVersion.isKeyword(proposedName) || usedVariables.contains(proposedName)) {
                counter++;
                cont = true;
                continue;
            }
            
            for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new VariablesFilter())) {
                if (proposedName.equals(e.getSimpleName().toString())) {
                    counter++;
                    cont = true;
                    break;
                }
            }
        } while(cont);
        
        return proposedName;
    }
    
    private static String safeString(String str) {
        return str == null ? "" : str;
    }

    public static String makeNameUnique(CompilationInfo info, Scope s, String name) {
        return makeNameUnique(info, s, name, null, null);
    }

    public static String toConstantName(String camelCaseName) {
        StringBuilder result = new StringBuilder();
        char[] chars = camelCaseName.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (Character.isUpperCase(c) && i > 0) {
                if (Character.isLowerCase(chars[i - 1])) {
                    result.append('_');
                } else if (i + 1 < chars.length && Character.isLowerCase(chars[i + 1])) {
                    result.append('_');
                }
            }
            
            result.append(Character.toUpperCase(c));
        }

        return result.toString();
    }

    /**
     * @param tp tested {@link TreePath}
     * @return true if <code>tp</code> is an IDENTIFIER in a VARIABLE in an ENHANCED_FOR_LOOP
     */
    public static boolean isEnhancedForLoopIdentifier(TreePath tp) {
        if (tp == null || tp.getLeaf().getKind() != Kind.IDENTIFIER)
            return false;
        TreePath parent = tp.getParentPath();
        if (parent == null || parent.getLeaf().getKind() != Kind.VARIABLE)
            return false;
        TreePath context = parent.getParentPath();
        if (context == null || context.getLeaf().getKind() != Kind.ENHANCED_FOR_LOOP)
            return false;
        return true;
    }

    /**
     *
     * @param info context {@link CompilationInfo}
     * @param iterable tested {@link TreePath}
     * @return generic type of an {@link Iterable} or {@link ArrayType} at a TreePath
     */
    public static TypeMirror getIterableGenericType(CompilationInfo info, TreePath iterable) {
        TypeElement iterableElement = info.getElements().getTypeElement("java.lang.Iterable"); //NOI18N
        if (iterableElement == null) {
            return null;
        }
        TypeMirror iterableType = info.getTrees().getTypeMirror(iterable);
        if (iterableType == null) {
            return null;
        }
        TypeMirror designedType = null;
        if (iterableType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) iterableType;
            if (!info.getTypes().isSubtype(info.getTypes().erasure(declaredType), info.getTypes().erasure(iterableElement.asType()))) {
                return null;
            }
            ExecutableElement iteratorMethod = (ExecutableElement) iterableElement.getEnclosedElements().get(0);
            ExecutableType iteratorMethodType = (ExecutableType) info.getTypes().asMemberOf(declaredType, iteratorMethod);
            List<? extends TypeMirror> typeArguments = ((DeclaredType) iteratorMethodType.getReturnType()).getTypeArguments();
            if (!typeArguments.isEmpty()) {
                designedType = typeArguments.get(0);
            } else {
                TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");

                if (jlObject != null) {
                    designedType = jlObject.asType();
                }
            }
        } else if (iterableType.getKind() == TypeKind.ARRAY) {
            designedType = ((ArrayType) iterableType).getComponentType();
        }
        if (designedType == null) {
            return null;
        }
        return resolveTypeForDeclaration(info, designedType);
    }

    public static String getName(TypeMirror tm) {
        if (tm.getKind().isPrimitive()) {
            return "" + Character.toLowerCase(tm.getKind().name().charAt(0));
        }

        switch (tm.getKind()) {
            case DECLARED:
                DeclaredType dt = (DeclaredType) tm;
                return firstToLower(dt.asElement().getSimpleName().toString());
            case ARRAY:
                return getName(((ArrayType) tm).getComponentType());
            default:
                return DEFAULT_NAME;
        }
    }
    
    private static String firstToLower(String name) {
        if (name.length() == 0)
            return null;

        StringBuilder result = new StringBuilder();
        boolean toLower = true;
        char last = Character.toLowerCase(name.charAt(0));

        for (int i = 1; i < name.length(); i++) {
            if (toLower && (Character.isUpperCase(name.charAt(i)) || name.charAt(i) == '_')) {
                result.append(Character.toLowerCase(last));
            } else {
                result.append(last);
                toLower = false;
            }
            last = name.charAt(i);

        }

        result.append(toLower ? Character.toLowerCase(last) : last);
        
        if (SourceVersion.isKeyword(result)) {
            return "a" + name;
        } else {
            return result.toString();
        }
    }
    
    public static final class VariablesFilter implements ElementAcceptor {
        
        private static final Set<ElementKind> ACCEPTABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
        
        public boolean accept(Element e, TypeMirror type) {
            return ACCEPTABLE_KINDS.contains(e.getKind());
        }
        
    }

    public static final String TAG_SELECT = "select";
    public static ChangeInfo commitAndComputeChangeInfo(FileObject target, final ModificationResult diff) throws IOException {
        return commitAndComputeChangeInfo(target, diff, TAG_SELECT);
    }

    /**
     * Commits changes and provides selection bounds
     *
     * @param target target FileObject
     * @param diff set of changes made by ModificationTask
     * @param tag mark used for selection of generated text
     * @return set of changes made by hint
     * @throws java.io.IOException
     */
    public static ChangeInfo commitAndComputeChangeInfo(FileObject target, final ModificationResult diff, final Object tag) throws IOException {
        if (!target.canWrite()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(Utilities.class, "ERR_ReadOnlyTargetFile", FileUtil.getFileDisplayName(target)), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            
            return null;
        }
        
        diff.commit();
        return computeChangeInfo(target, diff, tag);
    }
        
    public static ChangeInfo computeChangeInfo(FileObject target, final ModificationResult diff, final Object tag) {
        List<? extends Difference> differences = diff.getDifferences(target);
        ChangeInfo result = null;
        
        try {
            if (differences != null) {
                for (Difference d : differences) {
                    if (d.getNewText() != null) { //to filter out possible removes
                        final Position start = d.getStartPosition();
                        Document doc = d.openDocument();

                        final Position[] pos = new Position[2];
                        final Document fdoc = doc;
                        
                        doc.render(new Runnable() {
                            public void run() {
                                try {
                                    if (tag != null) {
                                        int[] span = diff.getSpan(tag);
                                        if(span != null) {
                                            pos[0] = fdoc.createPosition(span[0]);
                                            pos[1] = fdoc.createPosition(span[1]);
                                        }
                                    } else {
                                        pos[0] = NbDocument.createPosition(fdoc, start.getOffset(), Position.Bias.Backward);
                                        pos[1] = pos[0];
                                    }
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        });
                        
                        if (pos[0] != null) {
                            result = new ChangeInfo(target, pos[0], pos[1]);
                        }
                        
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return result;
    }
    
    public static boolean isMethodHeaderInsideGuardedBlock(CompilationInfo info, MethodTree method) {
        try {
            Document doc = info.getDocument();

            if (doc instanceof GuardedDocument) {
                GuardedDocument bdoc = (GuardedDocument) doc;
                int methodStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), method);
                int methodEnd = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), method);

                return (bdoc.getGuardedBlockChain().compareBlock(methodStart, methodEnd) & MarkBlock.OVERLAP) != 0;
            }

            return false;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
    
    public static TypeMirror resolveTypeForDeclaration(CompilationInfo info, TypeMirror tm) {
        // TODO: should I convert the anonymous types to something more referenceable ?
        TypeMirror captureResolved = resolveCapturedType(info, tm);
        if (captureResolved == null) {
            return null;
        }
        TypeMirror m = info.getTypeUtilities().getDenotableType(tm);
        if (isValidType(m) || !isValidType(captureResolved)) {
            return m;
        } else {
            return captureResolved;
        }
    }
    
    public static TypeMirror resolveCapturedType(CompilationInfo info, TypeMirror tm) {
        if (tm == null) {
            return tm;
        }
        if (tm.getKind() == TypeKind.ERROR) {
            tm = info.getTrees().getOriginalType((ErrorType) tm);
        }
        TypeMirror type = resolveCapturedTypeInt(info, tm);
        if (type == null) {
            return tm;
        }
        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
            if (tmirr != null)
                return tmirr;
            else { //no extends, just '?'
                TypeElement te = info.getElements().getTypeElement("java.lang.Object"); // NOI18N
                return te == null ? null : te.asType();
            }
                
        }
        
        return type;
    }
    
    /**
     * Note: may return {@code null}, if an intersection type is encountered, to indicate a 
     * real type cannot be created.
     */
    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
        if (tm == null) return tm;
        
        TypeMirror orig = SourceUtils.resolveCapturedType(tm);

        if (orig != null) {
            tm = orig;
        }
        
        if (tm.getKind() == TypeKind.WILDCARD) {
            TypeMirror extendsBound = ((WildcardType) tm).getExtendsBound();
            TypeMirror superBound = ((WildcardType) tm).getSuperBound();
            if (extendsBound != null || superBound != null) {
                TypeMirror rct = resolveCapturedTypeInt(info, extendsBound != null ? extendsBound : superBound);
                if (rct != null) {
                    switch (rct.getKind()) {
                        case WILDCARD:
                            return rct;
                        case ARRAY:
                        case DECLARED:
                        case ERROR:
                        case TYPEVAR:
                        case OTHER:
                            return info.getTypes().getWildcardType(
                                    extendsBound != null ? rct : null, superBound != null ? rct : null);
                    }
                } else {
                    // propagate failure out of all wildcards
                    return null;
                }
            }
        } else if (tm.getKind() == TypeKind.INTERSECTION) {
            return null;
        }
        
        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) tm;
            List<TypeMirror> typeArguments = new LinkedList<TypeMirror>();
            
            for (TypeMirror t : dt.getTypeArguments()) {
                TypeMirror targ = resolveCapturedTypeInt(info, t);
                if (targ == null) {
                    // bail out, if the type parameter is a wildcard, it's probably not possible
                    // to create a proper parametrized type from it
                    if (t.getKind() == TypeKind.WILDCARD || t.getKind() == TypeKind.INTERSECTION) {
                        return null;
                    }
                    // use rawtype
                    typeArguments.clear();
                    break;
                }
                typeArguments.add(targ);
            }
            
            final TypeMirror enclosingType = dt.getEnclosingType();
            if (enclosingType.getKind() == TypeKind.DECLARED) {
                return info.getTypes().getDeclaredType((DeclaredType) enclosingType, (TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            } else {
                if (dt.asElement() == null) return dt;
                return info.getTypes().getDeclaredType((TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            }
        }

        if (tm.getKind() == TypeKind.ARRAY) {
            ArrayType at = (ArrayType) tm;
            TypeMirror tm2 = resolveCapturedTypeInt(info, at.getComponentType());
            return info.getTypes().getArrayType(tm2 != null ? tm2 : tm);
        }
        
        return tm;
    }
    
    public static <T extends Tree> T copyComments(WorkingCopy wc, Tree from, T to) {
        copyComments(wc, from, to, true);
        copyComments(wc, from, to, false);
        
        return to;
    }

    public static <T extends Tree> T copyComments(WorkingCopy wc, Tree from, T to, boolean preceding) {
        GeneratorUtilities.get(wc).copyComments(from, to, preceding);
        
        return to;
    }

    /**
     * Convert typemirror of an anonymous class to supertype/iface
     * 
     * @return typemirror of supertype/iface, initial tm if not anonymous
     */
    public static TypeMirror convertIfAnonymous(TypeMirror tm) {
        //anonymous class?
        Set<ElementKind> fm = EnumSet.of(ElementKind.METHOD, ElementKind.FIELD);
        if (tm instanceof DeclaredType) {
            Element el = ((DeclaredType) tm).asElement();
            //XXX: the null check is needed for lambda type, not covered by test:
            if (el != null && (el.getSimpleName().length() == 0 || fm.contains(el.getEnclosingElement().getKind()))) {
                List<? extends TypeMirror> interfaces = ((TypeElement) el).getInterfaces();
                if (interfaces.isEmpty()) {
                    tm = ((TypeElement) el).getSuperclass();
                } else {
                    tm = interfaces.get(0);
                }
            }
        }
        return tm;
    }

    public static List<List<TreePath>> splitStringConcatenationToElements(CompilationInfo info, TreePath tree) {
        return sortOut(info, linearize(tree));
    }

    //where:
    private static List<TreePath> linearize(TreePath tree) {
        List<TreePath> todo = new LinkedList<TreePath>();
        List<TreePath> result = new LinkedList<TreePath>();

        todo.add(tree);

        while (!todo.isEmpty()) {
            TreePath tp = todo.remove(0);
            Tree l = tp.getLeaf();
            while (l.getKind() == Kind.PARENTHESIZED) {
                tp = new TreePath(tp, ((ParenthesizedTree)l).getExpression());
                l = tp.getLeaf();
            }

            if (l.getKind() != Kind.PLUS) {
                result.add(tp);
                continue;
            }

            BinaryTree bt = (BinaryTree) tp.getLeaf();

            todo.add(0, new TreePath(tp, bt.getRightOperand()));
            todo.add(0, new TreePath(tp, bt.getLeftOperand()));
        }

        return result;
    }

    private static List<List<TreePath>> sortOut(CompilationInfo info, List<TreePath> trees) {
        List<List<TreePath>> result = new LinkedList<List<TreePath>>();
        List<TreePath> currentCluster = new LinkedList<TreePath>();

        for (TreePath t : trees) {
            if (isConstantString(info, t, true)) {
                currentCluster.add(t);
            } else {
                if (!currentCluster.isEmpty()) {
                    result.add(currentCluster);
                    currentCluster = new LinkedList<TreePath>();
                }
                result.add(new LinkedList<TreePath>(Collections.singletonList(t)));
            }
        }

        if (!currentCluster.isEmpty()) {
            result.add(currentCluster);
        }

        return result;
    }

    public static boolean isConstantString(CompilationInfo info, TreePath tp) {
        return isConstantString(info, tp, false);
    }

    public static boolean isConstantString(CompilationInfo info, TreePath tp, boolean acceptsChars) {
        if (tp.getLeaf().getKind() == Kind.STRING_LITERAL) return true;
        if (acceptsChars && tp.getLeaf().getKind() == Kind.CHAR_LITERAL) return true;

        Element el = info.getTrees().getElement(tp);

        if (el != null && (el.getKind() == ElementKind.FIELD || el.getKind() == ElementKind.LOCAL_VARIABLE)
                && ((VariableElement) el).getConstantValue() instanceof String) {
            return true;
        }

        if (tp.getLeaf().getKind() != Kind.PLUS) {
            return false;
        }

        List<List<TreePath>> sorted = splitStringConcatenationToElements(info, tp);

        if (sorted.size() != 1) {
            return false;
        }

        List<TreePath> part = sorted.get(0);

        for (TreePath c : part) {
            if (isConstantString(info, c, acceptsChars))
                return true;
        }

        return false;
    }

    public static boolean isStringOrCharLiteral(Tree t) {
        return t != null && (t.getKind() == Kind.STRING_LITERAL || t.getKind() == Kind.CHAR_LITERAL);
    }

    public static @NonNull Collection<? extends TreePath> resolveFieldGroup(@NonNull CompilationInfo info, @NonNull TreePath variable) {
        Tree leaf = variable.getLeaf();

        if (leaf.getKind() != Kind.VARIABLE) {
            return Collections.singleton(variable);
        }

        TreePath parentPath = variable.getParentPath();
        Iterable<? extends Tree> children;

        switch (parentPath.getLeaf().getKind()) {
            case BLOCK: children = ((BlockTree) parentPath.getLeaf()).getStatements(); break;
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                children = ((ClassTree) parentPath.getLeaf()).getMembers(); break;
            case CASE:  children = ((CaseTree) parentPath.getLeaf()).getStatements(); break;
            default:    children = Collections.singleton(leaf); break;
        }

        List<TreePath> result = new LinkedList<TreePath>();
        ModifiersTree currentModifiers = ((VariableTree) leaf).getModifiers();

        for (Tree c : children) {
            if (c.getKind() != Kind.VARIABLE) continue;

            if (((VariableTree) c).getModifiers() == currentModifiers) {
                result.add(new TreePath(parentPath, c));
            }
        }
        
        return result;
    }

    public static String shortDisplayName(CompilationInfo info, ExpressionTree expression) {
        return new HintDisplayNameVisitor(info).scan(expression, null);
    }
    
    private static final Map<Kind, String> operator2DN;

    static {
        operator2DN = new EnumMap<>(Kind.class);

        operator2DN.put(AND, "&");
        operator2DN.put(XOR, "^");
        operator2DN.put(OR, "|");
        operator2DN.put(CONDITIONAL_AND, "&&");
        operator2DN.put(CONDITIONAL_OR, "||");
        operator2DN.put(MULTIPLY_ASSIGNMENT, "*=");
        operator2DN.put(DIVIDE_ASSIGNMENT, "/=");
        operator2DN.put(REMAINDER_ASSIGNMENT, "%=");
        operator2DN.put(PLUS_ASSIGNMENT, "+=");
        operator2DN.put(MINUS_ASSIGNMENT, "-=");
        operator2DN.put(LEFT_SHIFT_ASSIGNMENT, "<<=");
        operator2DN.put(RIGHT_SHIFT_ASSIGNMENT, ">>=");
        operator2DN.put(UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, ">>>=");
        operator2DN.put(AND_ASSIGNMENT, "&=");
        operator2DN.put(XOR_ASSIGNMENT, "^=");
        operator2DN.put(OR_ASSIGNMENT, "|=");
        operator2DN.put(BITWISE_COMPLEMENT, "~");
        operator2DN.put(LOGICAL_COMPLEMENT, "!");
        operator2DN.put(MULTIPLY, "*");
        operator2DN.put(DIVIDE, "/");
        operator2DN.put(REMAINDER, "%");
        operator2DN.put(PLUS, "+");
        operator2DN.put(MINUS, "-");
        operator2DN.put(LEFT_SHIFT, "<<");
        operator2DN.put(RIGHT_SHIFT, ">>");
        operator2DN.put(UNSIGNED_RIGHT_SHIFT, ">>>");
        operator2DN.put(LESS_THAN, "<");
        operator2DN.put(GREATER_THAN, ">");
        operator2DN.put(LESS_THAN_EQUAL, "<=");
        operator2DN.put(GREATER_THAN_EQUAL, ">=");
        operator2DN.put(EQUAL_TO, "==");
        operator2DN.put(NOT_EQUAL_TO, "!=");
    }

    @NbBundle.Messages({
        "DisplayName_Unknown=<missing>"
    })
    private static class HintDisplayNameVisitor extends ErrorAwareTreeScanner<String, Void> {

        private CompilationInfo info;

        public HintDisplayNameVisitor(CompilationInfo info) {
            this.info = info;
        }

        public @Override String visitIdentifier(IdentifierTree tree, Void v) {
            return "..." + tree.getName().toString();
        }

        public @Override String visitMethodInvocation(MethodInvocationTree tree, Void v) {
            ExpressionTree methodSelect = tree.getMethodSelect();

            return "..." + simpleName(methodSelect) + "(...)"; // NOI18N
        }

        public @Override String visitArrayAccess(ArrayAccessTree node, Void p) {
            return "..." + simpleName(node.getExpression()) + "[]"; // NOI18N
        }

        public @Override String visitNewClass(NewClassTree nct, Void p) {
            return "...new " + simpleName(nct.getIdentifier()) + "(...)"; // NOI18N
        }

        @Override
        public String visitNewArray(NewArrayTree nct, Void p) {
            return "...new " + simpleName(nct.getType()) + "[...]"; // NOI18N
        }

        @Override
        public String visitBinary(BinaryTree node, Void p) {
            String dn = operator2DN.get(node.getKind());

            return scan(node.getLeftOperand(), p) + dn + scan(node.getRightOperand(), p);
        }

        @Override
        public String visitLiteral(LiteralTree node, Void p) {
            if (node.getValue() instanceof String)
                return "...";

            int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), node);
            int end   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), node);

            if (start < 0 || end < 0 || end < start) {
                return node.toString();
            }
            
            return info.getText().substring(start, end);
        }

        private String simpleName(Tree t) {
            if (t == null) {
                return Bundle.DisplayName_Unknown();
            }
            if (t.getKind() == Kind.IDENTIFIER) {
                return ((IdentifierTree) t).getName().toString();
            }

            if (t.getKind() == Kind.MEMBER_SELECT) {
                return ((MemberSelectTree) t).getIdentifier().toString();
            }

            if (t.getKind() == Kind.METHOD_INVOCATION) {
                return scan(t, null);
            }

            if (t.getKind() == Kind.PARAMETERIZED_TYPE) {
                return simpleName(((ParameterizedTypeTree) t).getType()) + "<...>"; // NOI18N
            }

            if (t.getKind() == Kind.ARRAY_ACCESS) {
                return simpleName(((ArrayAccessTree) t).getExpression()) + "[]"; //NOI18N
            }

            if (t.getKind() == Kind.PARENTHESIZED) {
                return "(" + simpleName(((ParenthesizedTree)t).getExpression()) + ")"; //NOI18N
            }

            if (t.getKind() == Kind.TYPE_CAST) {
                return simpleName(((TypeCastTree)t).getType());
            }

            if (t.getKind() == Kind.ARRAY_TYPE) {
                return simpleName(((ArrayTypeTree)t).getType());
            }

            if (t.getKind() == Kind.PRIMITIVE_TYPE) {
                return ((PrimitiveTypeTree) t).getPrimitiveTypeKind().name().toLowerCase();
            }
            
            throw new IllegalStateException("Currently unsupported kind of tree: " + t.getKind()); // NOI18N
        }
    }
    
    /**
     * Finds the owner method, constructor, optionally initializer or lambda.
     * The behaviour depends on the 'lambdaOrInitializer' parameter; if false,
     * it will find the immediate owning method or constructor. If the code is
     * NOT directly nested in a method or constructor (i.e. in a lambda, initializer),
     * null is returned.
     * <p/>
     * If the parameter is true, the method is also able to return Lambda expression
     * tree or initializer tree.
     * <p/>
     * At any rate, the search stops at class boundaries.
     * 
     * @param ctx the context
     * @param from pat to start search from (upwards)
     * @param lambdaOrInitializer also return lambdas or initializers if true.
     * @return the direct owning executable block or {@code null} if the owning exec
     * block does not satisfy the filter.
     */
    @SuppressWarnings({"AssignmentToMethodParameter", "NestedAssignment"})
    public static TreePath findOwningExecutable(HintContext ctx, TreePath from, boolean lambdaOrInitializer) {
        return findOwningExecutable(from, lambdaOrInitializer);
    }
    
    public static TreePath findOwningExecutable(TreePath from, boolean lambdaOrInitializer) {
        Tree.Kind k = null;
        
        OUTER: while (from != null && !(TreeUtilities.CLASS_TREE_KINDS.contains(k = from.getLeaf().getKind()))) {
            switch (k) {
                case METHOD:
                    break OUTER;
                case LAMBDA_EXPRESSION:
                    return lambdaOrInitializer ? from : null;
                case BLOCK: {
                    TreePath par = from.getParentPath();
                    Tree l = par.getLeaf();
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(l.getKind())) {
                        return lambdaOrInitializer ? from : null;
                    }
                }
            }
            from = from.getParentPath();
        }
        return (from == null || k != Kind.METHOD) ?
                null : from;
    }

    /**
     * Finds the top-level block or expression that contains the 'from' path.
     * The result could be a 
     * <ul>
     * <li>BlockTree representing method body
     * <li>ExpressionTree representing field initializer
     * <li>BlockTree representing class initializer
     * <li>ExpressionTree representing lambda expression
     * <li>BlockTree representing lambda expression
     * </ul>
     * @param from start from 
     * @return nearest enclosing top-level block/expression as defined above.
     */
    public static TreePath findTopLevelBlock(TreePath from) {
        if (from.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            return null;
        }
        TreePath save = null;
        
        while (from != null) {
            Tree.Kind k = from.getParentPath().getLeaf().getKind();
            if (k == Kind.METHOD || k == Kind.LAMBDA_EXPRESSION) {
                return from;
            } else if (k == Kind.VARIABLE) {
                save = from;
            } else if (TreeUtilities.CLASS_TREE_KINDS.contains(k)) {
                if (save != null) {
                    // variable initializer from the previous iteration
                    return save;
                }
                if (from.getLeaf().getKind() == Kind.BLOCK) {
                    // parent is class, from is block -> initializer
                    return from;
                }
                return null;
            } else {
                save = null;
            }
            from = from.getParentPath();
        }
        return null;
    }
    
    public static boolean isInConstructor(TreePath path) {
        TreePath method = findOwningExecutable(path, false);
        if (method == null || method.getLeaf().getKind() != Tree.Kind.METHOD) {
            return false;
        }
        return ((MethodTree)method.getLeaf()).getName().contentEquals("<init>"); // NOI18N
    }

    public static boolean isInConstructor(HintContext ctx) {
        TreePath method = findOwningExecutable(ctx, ctx.getPath(), false);
        if (method == null) return false;
        Element enclosingMethodElement = ctx.getInfo().getTrees().getElement(method);
        return (enclosingMethodElement != null &&
                enclosingMethodElement.getKind() == ElementKind.CONSTRUCTOR);
    }

    @SuppressWarnings("BoxedValueEquality")
    public static boolean isReferencedIn(CompilationInfo info, TreePath variable, Iterable<? extends TreePath> in) {
        final Trees trees = info.getTrees();
        final Element e = trees.getElement(variable);

        if (e == null) { //TODO: check also error
            return false;
        }

        for (TreePath tp : in) {

            if (e.equals(trees.getElement(tp))) {
                return true;
            }

            boolean occurs = new ErrorAwareTreePathScanner<Boolean, Void>() {
                private boolean found = false;
                @Override
                public Boolean scan(Tree tree, Void p) {
                    if (found) {
                        return true; // fast path
                    }

                    if (tree == null) {
                        return false;
                    }

                    TreePath currentPath = new TreePath(getCurrentPath(), tree);
                    Element currentElement = trees.getElement(currentPath);

                    if (e.equals(currentElement)) {
                        found = true;
                        return true;
                    }

                    return super.scan(tree, p);
                }

                @Override
                public Boolean reduce(Boolean r1, Boolean r2) {
                    if (r1 == null) {
                        return r2;
                    }

                    if (r2 == null) {
                        return r1;
                    }

                    return r1 || r2;
                }

            }.scan(tp, null) == Boolean.TRUE;

            if (occurs) {
                return true;
            }
        }

        return false;
    }

    public static Pair<List<? extends TypeMirror>, List<String>> resolveArguments(CompilationInfo info, TreePath invocation, List<? extends ExpressionTree> realArguments, Element target) {
        MethodArguments ma = resolveArguments(info, invocation, realArguments, target, null);
        
        if (ma == null) return null;
        
        return Pair.<List<? extends TypeMirror>, List<String>>of(ma.parameterTypes, ma.parameterNames);
    }
    
    public static MethodArguments resolveArguments(CompilationInfo info, TreePath invocation, List<? extends ExpressionTree> realArguments, Element target, TypeMirror returnType) {
        List<TypeMirror> argumentTypes = new LinkedList<TypeMirror>();
        List<String>     argumentNames = new LinkedList<String>();
        List<Element>    usedLocalTypeVariables = new ArrayList<Element>();
        Set<String>      usedArgumentNames = new HashSet<String>();
        
        TreePath enclosingMethod = invocation;
        
        while (enclosingMethod != null && !TreeUtilities.CLASS_TREE_KINDS.contains(enclosingMethod.getLeaf().getKind()) && enclosingMethod.getLeaf().getKind() != Kind.METHOD) {
            enclosingMethod = enclosingMethod.getParentPath();
        }
        
        ExecutableElement method = null;
        
        if (enclosingMethod != null && enclosingMethod.getLeaf().getKind() == Kind.METHOD) {
            Element el = info.getTrees().getElement(enclosingMethod);
            
            if (el != null && (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
                method = (ExecutableElement) el;
            }
        }

        if (returnType != null) {
            if (!verifyTypeVarAccessible(method, returnType, usedLocalTypeVariables, target)) return null;
        } else {
            method = null;
        }
        
        CodeStyle codeStyle = CodeStyle.getDefault(info.getFileObject());
        
        for (ExpressionTree arg : realArguments) {
            TreePath argPath = new TreePath(invocation, arg);
            TypeMirror tm = info.getTrees().getTypeMirror(argPath);

            //anonymous class?
            tm = Utilities.convertIfAnonymous(tm);

            if (tm == null || tm.getKind() == TypeKind.NONE || containsErrorsRecursively(tm)) {
                return null;
            }
            
            tm = resolveTypeForDeclaration(info, tm);

            if (!verifyTypeVarAccessible(method, tm, usedLocalTypeVariables, target)) return null;

            if (tm.getKind() == TypeKind.NULL) {
                tm = info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
                if (tm == null) {
                    return null;
                }
            }

            argumentTypes.add(tm);

            String proposedName = null;
            Element elem = info.getTrees().getElement(argPath);

            if (elem != null && elem.getKind() == ElementKind.ENUM_CONSTANT) {
                proposedName = firstToLower(elem.getEnclosingElement().getSimpleName().toString());
            }

            if (proposedName == null) {
                proposedName = org.netbeans.modules.editor.java.Utilities.varNameSuggestion(arg);
            }

            if (proposedName == null) {
                proposedName = org.netbeans.modules.java.hints.errors.Utilities.getName(tm);
            }

            if (proposedName == null) {
                proposedName = "arg"; // NOI18N
            }
            
            String augmentedName = CodeStyleUtils.addPrefixSuffix(proposedName, codeStyle.getParameterNamePrefix(), codeStyle.getParameterNameSuffix());

            if (usedArgumentNames.contains(augmentedName)) {
                int num = 0;

                while (usedArgumentNames.contains(augmentedName = CodeStyleUtils.addPrefixSuffix(proposedName + num, codeStyle.getParameterNamePrefix(), codeStyle.getParameterNameSuffix()))) {
                    num++;
                }
            }

            argumentNames.add(augmentedName);
            usedArgumentNames.add(augmentedName);
        }
        
        List<TypeMirror> typeParamTypes = new LinkedList<TypeMirror>();
        List<String>     typeParamNames = new LinkedList<String>();
        
        if (method != null) {
            for (TypeParameterElement methodTP : method.getTypeParameters()) {
                if (!usedLocalTypeVariables.contains(methodTP)) continue;

                typeParamTypes.add(methodTP.asType());
                typeParamNames.add(methodTP.getSimpleName().toString());
            }
        }

        return new MethodArguments(argumentTypes, argumentNames, typeParamTypes, typeParamNames);
    }
    
    public static class MethodArguments {
        public final List<? extends TypeMirror> parameterTypes;
        public final List<String> parameterNames;
        public final List<? extends TypeMirror> typeParameterTypes;
        public final List<String> typeParameterNames;
        public MethodArguments(List<? extends TypeMirror> parameterTypes, List<String> parameterNames, List<? extends TypeMirror> typeParameterTypes, List<String> typeParameterNames) {
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
            this.typeParameterTypes = typeParameterTypes;
            this.typeParameterNames = typeParameterNames;
        }
    }

    private static boolean verifyTypeVarAccessible(ExecutableElement method, TypeMirror forType, List<Element> usedLocalTypeVariables, Element target) {
        Collection<TypeVariable> typeVars = Utilities.containedTypevarsRecursively(forType);
        
        if (method != null) {
            for (Iterator<TypeVariable> it = typeVars.iterator(); it.hasNext(); ) {
                TypeVariable tvar = it.next();
                Element tvarEl = tvar.asElement();

                if (method.getTypeParameters().contains(tvarEl)) {
                    usedLocalTypeVariables.add(tvarEl);
                    it.remove();
                }
            }
        }
        
        return allTypeVarsAccessible(typeVars, target);
    }

    //XXX: currently we cannot fix:
    //xxx = new ArrayList<Unknown>();
    //=>
    //ArrayList<Unknown> xxx;
    //xxx = new ArrayList<Unknown>();
    public static boolean containsErrorsRecursively(TypeMirror tm) {
        switch (tm.getKind()) {
            case ERROR:
                return true;
            case DECLARED:
                DeclaredType type = (DeclaredType) tm;

                for (TypeMirror t : type.getTypeArguments()) {
                    if (containsErrorsRecursively(t))
                        return true;
                }

                return false;
            case ARRAY:
                return containsErrorsRecursively(((ArrayType) tm).getComponentType());
            case WILDCARD:
                if (((WildcardType) tm).getExtendsBound() != null && containsErrorsRecursively(((WildcardType) tm).getExtendsBound())) {
                    return true;
                }
                if (((WildcardType) tm).getSuperBound() != null && containsErrorsRecursively(((WildcardType) tm).getSuperBound())) {
                    return true;
                }
                return false;
            case OTHER:
                return true;
            default:
                return false;
        }
    }

    public static boolean exitsFromAllBranchers(CompilationInfo info, TreePath from) {
        ExitsFromAllBranches efab = new ExitsFromAllBranches(info);

        return efab.scan(from, null) == Boolean.TRUE;
    }

    /**
     * Determines whether the execution definitely escapes from the root of the search.
     * Execution escapes, if the control could be transferred outside of the inspected area. Definitely escapes means
     * that possible all code paths escape from the area: both if branches, all switch branches etc.
     * <p/>
     * Inidvidual visit() operations must return true, if they escape outside, false or null otherwise. Nodes are added to
     * {@link #seenTrees} before processing, so if a break/continue targets such a node, such jump is not considered an exit.
     * Jumps can register the target Tree in {@link #targetTrees} which causes false to be returned from that tree's inspection.
     */
    private static final class ExitsFromAllBranches extends ErrorAwareTreePathScanner<Boolean, Void> {

        private CompilationInfo info;
        private final Set<Tree> seenTrees = new HashSet<Tree>();
        private final Stack<Pair<Set<TypeMirror>, Tree>> caughtExceptions = new Stack<>();
        private final Set<Tree> targetTrees = new HashSet<>();
        
        public ExitsFromAllBranches(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Boolean scan(TreePath path, Void p) {
            seenTrees.add(path.getLeaf());
            Boolean ret = super.scan(path, p);
            boolean pending = !targetTrees.isEmpty();
            targetTrees.remove(path.getLeaf());
            return pending ? Boolean.FALSE : ret;
        }

        @Override
        public Boolean scan(Tree tree, Void p) {
            // if a jump to an outer statement is seen, do not bother with further processing.
            if (!targetTrees.isEmpty()) {
                return false;
            }
            seenTrees.add(tree);
            Boolean ret = super.scan(tree, p);
            boolean pending = !targetTrees.isEmpty();
            targetTrees.remove(tree);
            return pending ? null : ret;
        }

        @Override
        public Boolean visitLambdaExpression(LambdaExpressionTree node, Void p) {
            return false;
        }
        
        

        /*
        @Override
        public Boolean reduce(Boolean r1, Boolean r2) {
            if ((r1 == Boolean.FALSE) || (r2 == Boolean.FALSE)) {
                return false;
            } else {
                // use the later statement
                return r2;
            }
        }
*/
        /**
         * Hardcoded check for System.exit(), Runtime.exit and Runtime.halt which also terminates the processing. 
         * TODO: some configuration (project-level ?) could add also different exit methods.
         */
        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (isSystemExit(info, el)) {
                return true;
            }
            return super.visitMethodInvocation(node, p);
        }

        @Override
        public Boolean visitArrayType(ArrayTypeTree node, Void p) {
            return super.visitArrayType(node, p); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Boolean visitDoWhileLoop(DoWhileLoopTree node, Void p) {
            return scan(node.getStatement(), p);
        }

        /**
         * Loops with a condition at the start may skip the body at all, so
         */
        @Override
        public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
            return null;
        }

        @Override
        public Boolean visitForLoop(ForLoopTree node, Void p) {
            return null;
        }

        @Override
        public Boolean visitWhileLoop(WhileLoopTree node, Void p) {
            return null;
        }
        
        @Override
        public Boolean visitIf(IfTree node, Void p) {
            return scan(node.getThenStatement(), null) == Boolean.TRUE && scan(node.getElseStatement(), null) == Boolean.TRUE;
        }
        
        @Override
        public Boolean visitSwitch(SwitchTree node, Void p) {
            boolean lastCaseExit = false;
            boolean defaultSeen = false;
            Set<Element> enumValues = null;
            
            if (node.getExpression() != null) {
                TypeMirror exprType = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));
                if (isValidType(exprType) && exprType.getKind() == TypeKind.DECLARED) {
                    Element el = ((DeclaredType)exprType).asElement();
                    enumValues = new HashSet<>();
                    for (Element f : el.getEnclosedElements()) {
                        if (f.getKind() == ElementKind.ENUM_CONSTANT) {
                            enumValues.add(f);
                        }
                    }
                }
            }
            for (CaseTree ct : node.getCases()) {
                Boolean res = scan(ct, null);
                if (res == Boolean.FALSE) {
                    return res;
                }
                lastCaseExit = res == Boolean.TRUE;
                if (ct.getExpression() == null) {
                    defaultSeen = true;
                } else if (enumValues != null ) {
                    TreePath casePath = new TreePath(getCurrentPath(), ct);
                    Element v = info.getTrees().getElement(new TreePath(
                            casePath, ct.getExpression()));
                    if (v != null) {
                        enumValues.remove(v);
                    }
                }
            }
            if (enumValues != null && enumValues.isEmpty()) {
                defaultSeen = true;
            }
            return lastCaseExit == Boolean.TRUE && defaultSeen;
        }
        
        @Override
        public Boolean visitReturn(ReturnTree node, Void p) {
            return true;
        }

        @Override
        public Boolean visitBreak(BreakTree node, Void p) {
            Tree target = info.getTreeUtilities().getBreakContinueTarget(getCurrentPath());
            boolean known = seenTrees.contains(target);
            if (known) {
                targetTrees.add(target);
            }
            return !known;
        }

        @Override
        public Boolean visitContinue(ContinueTree node, Void p) {
            return visitBreak(null, p);
        }

        @Override
        public Boolean visitClass(ClassTree node, Void p) {
            return false;
        }

        @Override
        public Boolean visitTry(TryTree node, Void p) {
            Set<TypeMirror> caught = new HashSet<TypeMirror>();

            for (CatchTree ct : node.getCatches()) {
                TypeMirror t = info.getTrees().getTypeMirror(new TreePath(new TreePath(getCurrentPath(), ct), ct.getParameter()));

                if (t != null) {
                    caught.add(t);
                }
            }

            caughtExceptions.push(Pair.of(caught, node));
            
            try {
                return scan(node.getBlock(), p) == Boolean.TRUE || scan(node.getFinallyBlock(), p) == Boolean.TRUE;
            } finally {
                caughtExceptions.pop();
            }
        }

        @Override
        public Boolean visitThrow(ThrowTree node, Void p) {
            TypeMirror type = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));
            boolean isCaught = false;

            OUTER: for (Pair<Set<TypeMirror>, Tree> pair : caughtExceptions) {
                Set<TypeMirror> caught = pair.first();
                for (TypeMirror c : caught) {
                    if (info.getTypes().isSubtype(type, c)) {
                        isCaught = true;
                        targetTrees.add(pair.second());
                        break OUTER;
                    }
                }
            }

            return super.visitThrow(node, p) == Boolean.TRUE || !isCaught;
        }

    }

    public static @NonNull Collection<TypeVariable> containedTypevarsRecursively(@NullAllowed TypeMirror tm) {
        if (tm == null) {
            return Collections.emptyList();
        }

        Collection<TypeVariable> typeVars = new LinkedList<TypeVariable>();

        containedTypevarsRecursively(tm, typeVars);

        return typeVars;
    }

    private static void containedTypevarsRecursively(@NonNull TypeMirror tm, @NonNull Collection<TypeVariable> typeVars) {
        switch (tm.getKind()) {
            case TYPEVAR:
                typeVars.add((TypeVariable) tm);
                break;
            case DECLARED:
                DeclaredType type = (DeclaredType) tm;
                for (TypeMirror t : type.getTypeArguments()) {
                    containedTypevarsRecursively(t, typeVars);
                }

                break;
            case ARRAY:
                containedTypevarsRecursively(((ArrayType) tm).getComponentType(), typeVars);
                break;
            case WILDCARD:
                if (((WildcardType) tm).getExtendsBound() != null) {
                    containedTypevarsRecursively(((WildcardType) tm).getExtendsBound(), typeVars);
                }
                if (((WildcardType) tm).getSuperBound() != null) {
                    containedTypevarsRecursively(((WildcardType) tm).getSuperBound(), typeVars);
                }
                break;
        }
    }

    public static boolean allTypeVarsAccessible(Collection<TypeVariable> typeVars, Element target) {
        if (target == null) {
            return typeVars.isEmpty();
        }
        
        Set<TypeVariable> targetTypeVars = new HashSet<TypeVariable>();

        OUTER: while (target.getKind() != ElementKind.PACKAGE) {
            Iterable<? extends TypeParameterElement> tpes;

            switch (target.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case RECORD:
                    tpes = ((TypeElement) target).getTypeParameters();
                    break;
                case METHOD:
                case CONSTRUCTOR:
                    tpes = ((ExecutableElement) target).getTypeParameters();
                    break;
                default:
                    break OUTER;
            }

            for (TypeParameterElement tpe : tpes) {
                targetTypeVars.add((TypeVariable) tpe.asType());
            }

            if (target.getModifiers().contains(Modifier.STATIC)) {
                break;
            }

            target = target.getEnclosingElement();
        }

        return targetTypeVars.containsAll(typeVars);
    }

    public static String target2String(TypeElement target) {
        final Name qualifiedName = target.getQualifiedName(); //#130759
        if (qualifiedName == null) {
            Logger.getLogger(Utilities.class.getName()).warning("Target qualified name could not be resolved."); //NOI18N
            return ""; //NOI18N
        } else {
            String qnString = qualifiedName.toString();
            if (qnString.length() == 0) {
                //probably an anonymous class
                qnString = target.asType().toString();
            }

            try {
                qnString = XMLUtil.toElementContent(qnString);
            } catch (CharConversionException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.FINE, null, ex);
            }

            return qnString;
        }
    }

    public static Visibility effectiveVisibility(TreePath tp) {
        Visibility result = null;

        while (tp != null) {
            Visibility current = Visibility.forTree(tp.getLeaf());

            if (current != null) {
                if (result != null) result = result.enclosedBy(current);
                else result = current;
            }
            
            tp = tp.getParentPath();
        }

        return result;
    }

    public enum Visibility {
        PRIVATE(EnumSet.of(Modifier.PRIVATE)),
        PACKAGE_PRIVATE(EnumSet.noneOf(Modifier.class)),
        PROTECTED(EnumSet.of(Modifier.PROTECTED)),
        PUBLIC(EnumSet.of(Modifier.PUBLIC));
        private final Set<Modifier> modifiers;
        private Visibility(Set<Modifier> modifiers) {
            this.modifiers = modifiers;
        }
        public Visibility enclosedBy(Visibility encl) {
            return Visibility.values()[Math.min(ordinal(), encl.ordinal())];
        }
        public Set<Modifier> getRequiredModifiers() {
            return modifiers;
        }
        public static Visibility forModifiers(ModifiersTree mt) {
            if (mt.getFlags().contains(Modifier.PUBLIC)) return PUBLIC;
            if (mt.getFlags().contains(Modifier.PROTECTED)) return PROTECTED;
            if (mt.getFlags().contains(Modifier.PRIVATE)) return PRIVATE;
            return PACKAGE_PRIVATE;
        }
        public static Visibility forElement(Element el) {
            if (el.getModifiers().contains(Modifier.PUBLIC)) return PUBLIC;
            if (el.getModifiers().contains(Modifier.PROTECTED)) return PROTECTED;
            if (el.getModifiers().contains(Modifier.PRIVATE)) return PRIVATE;
            return PACKAGE_PRIVATE;
        }
        public static Visibility forTree(Tree t) {
            switch (t.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE: return forModifiers(((ClassTree) t).getModifiers());
                case VARIABLE: return forModifiers(((VariableTree) t).getModifiers());
                case METHOD: return forModifiers(((MethodTree) t).getModifiers());
                default: return null;
            }
        }
    }
    
    public static boolean isValidElement(Element e) {
        return e != null && isValidType(e.asType());
    }
    
    public static boolean isValidValueType(TypeMirror m) {
        return isValidType(m) && m.getKind() != TypeKind.EXECUTABLE;
    }
    
    public static boolean isValidType(TypeMirror m) {
        return m != null && (
                m.getKind() != TypeKind.PACKAGE &&
                m.getKind() != TypeKind.OTHER && 
                m.getKind() != TypeKind.ERROR);
    }

    /**
     * Detects if targets file is non-null and writable
     * @return true if target's file is writable
     */
    public static boolean isTargetWritable(@NonNull TypeElement target, @NonNull CompilationInfo info) {
        TypeElement outermostType = info.getElementUtilities().outermostTypeElement(target);
        FileObject fo = SourceUtils.getFile(ElementHandle.create(outermostType), info.getClasspathInfo());
	if(fo != null && fo.canWrite())
	    return true;
	else
	    return false;
    }


    public static Visibility getAccessModifiers(@NonNull CompilationInfo info, @NullAllowed TypeElement source, @NonNull TypeElement target) {
        if (target.getKind().isInterface()) {
            return Visibility.PUBLIC;
        }

        TypeElement outterMostSource = source != null ? info.getElementUtilities().outermostTypeElement(source) : null;
        TypeElement outterMostTarget = info.getElementUtilities().outermostTypeElement(target);

        if (outterMostTarget.equals(outterMostSource)) {
            return Visibility.PRIVATE;
        }

        Element sourcePackage;

        if (outterMostSource != null) {
            sourcePackage = outterMostSource.getEnclosingElement();
        } else if (info.getCompilationUnit().getPackageName() != null) {
            sourcePackage = info.getTrees().getElement(new TreePath(new TreePath(info.getCompilationUnit()), info.getCompilationUnit().getPackage()));
        } else {
            sourcePackage = info.getElements().getPackageElement("");
        }

        Element targetPackage = outterMostTarget.getEnclosingElement();

        if (sourcePackage != null && sourcePackage.equals(targetPackage)) {
            return Visibility.PACKAGE_PRIVATE;
        }

        //TODO: protected?
        return Visibility.PUBLIC;
    }
    
    private static final EnumSet VARIABLE_KINDS = EnumSet.of(
            ElementKind.LOCAL_VARIABLE, ElementKind.ENUM_CONSTANT, ElementKind.FIELD, ElementKind.PARAMETER,
            ElementKind.RESOURCE_VARIABLE, ElementKind.EXCEPTION_PARAMETER, ElementKind.TYPE_PARAMETER);
    
    public static boolean isSymbolUsed(CompilationInfo info, TreePath target, CharSequence variableName, Scope localScope) {
        SourcePositions[] pos = new SourcePositions[1];
        Tree t = info.getTreeUtilities().parseExpression(variableName.toString(), pos);
        TypeMirror tm = info.getTreeUtilities().attributeTree(t, localScope);
        Element el = info.getTrees().getElement(new TreePath(target, t));
        if (el == null) {
            return false;
        }
        ElementKind k = el.getKind();
        return VARIABLE_KINDS.contains(k);
    }

    /**
     * Determines if the element corresponds to never-returning, terminating method.
     * System.exit, Runtime.exit, Runtime.halt are checked. The passed element is
     * usually a result of {@code CompilationInfo.getTrees().getElement(path)}.
     * 
     * @param info context
     * @param e element to check
     * @return true, if the element corrresponds to a VM-exiting method
     */
    public static boolean isSystemExit(CompilationInfo info, Element e) {
        if (e == null || e.getKind() != ElementKind.METHOD) {
            return false;
        }
        ExecutableElement ee = (ExecutableElement)e;
        Name n = ee.getSimpleName();
        if (n.contentEquals("exit") || n.contentEquals("halt")) { // NOI18N
            TypeElement tel = info.getElementUtilities().enclosingTypeElement(e);
            if (tel == null) {
                return false;
            }
            Name ofqn = tel.getQualifiedName();
            if (ofqn.contentEquals("java.lang.System") || ofqn.contentEquals("java.lang.Runtime")) { // NOI18N
                return true;
            }
        }
        return false;
    }

    /**
     * Helper that retrieves all caught exception from a conventional catch
     * clause or from catch clause that contain alternatives. Empty collection
     * is returned in the case of an error.
     * 
     * @param ct catch clause
     * @return exception list, never null.
     */
    public static List<? extends TypeMirror> getUnionExceptions(CompilationInfo info, TreePath cP, CatchTree ct) {
        if (ct.getParameter() == null) {
            return Collections.emptyList();
        }
        TypeMirror exT = info.getTrees().getTypeMirror(new TreePath(cP, ct.getParameter()));
        return getCaughtExceptions(exT);
    }

    private static List<? extends TypeMirror> getCaughtExceptions(TypeMirror caught) {
        if (caught == null) {
            return Collections.emptyList();
        }
        switch (caught.getKind()) {
            case UNION: {
                boolean cloned = false;
                List<? extends TypeMirror> types = ((UnionType) caught).getAlternatives();
                int i = types.size() - 1;
                for (; i >= 0; i--) {
                    TypeMirror m = types.get(i);
                    TypeKind mk = m.getKind();
                    if (mk == null || mk != TypeKind.DECLARED) {
                        if (!cloned) {
                            types = new ArrayList<TypeMirror>(types);
                        }
                        types.remove(i);
                    }
                }
                
                return types;
            }
            case DECLARED:
                return Collections.singletonList(caught);
            default:
                return Collections.emptyList();
        }
    }

    private static final Set<String> PRIMITIVE_NAMES = new HashSet<String>(8);
    
    static {
        PRIMITIVE_NAMES.add("java.lang.Integer"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Character"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Long"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Byte"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Short"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Boolean"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Float"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Double"); // NOI18N
    }

    public static TypeKind getPrimitiveKind(CompilationInfo ci, TypeMirror tm) {
        if (tm == null) {
            return null;
        }
        if (tm.getKind().isPrimitive()) {
            return tm.getKind();
        } else if (isPrimitiveWrapperType(tm)) {
            return ci.getTypes().unboxedType(tm).getKind();
        } 
        return null;
    }
    
    public static TypeMirror unboxIfNecessary(CompilationInfo ci, TypeMirror tm) {
        if (isPrimitiveWrapperType(tm)) {
            return ci.getTypes().unboxedType(tm);
        } else {
            return tm;
        }
    }
    
    public static boolean isPrimitiveWrapperType(TypeMirror tm) {
        if (tm == null || tm.getKind() != TypeKind.DECLARED) { 
            return false;
        }
        Element el = ((DeclaredType)tm).asElement();
        if (el == null || el.getKind() != ElementKind.CLASS) {
            return false;
        }
        String s = ((TypeElement)el).getQualifiedName().toString();
        return PRIMITIVE_NAMES.contains(s); // NOI18N
    }
    
    /**
     * Attempts to resolve a method or a constructor call with an altered argument tree.
     * 
     * @param ci the context
     * @param invPath path to the method invocation node
     * @param origPath path to the Tree within method's arguments which should be replaced
     * @param valPath the replacement tree
     * @return 
     */
    public static boolean checkAlternativeInvocation(CompilationInfo ci, TreePath invPath, 
            TreePath origPath,
            TreePath valPath, String customPrefix) {
        Tree l = invPath.getLeaf();
        Tree sel;
        
        if (l.getKind() == Tree.Kind.NEW_CLASS) {
            NewClassTree nct = (NewClassTree)invPath.getLeaf();
            sel = nct.getIdentifier();
        } else if (l.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree mit = (MethodInvocationTree)invPath.getLeaf();
            sel = mit.getMethodSelect();
        } else {
            return false;
        }
        
        return resolveAlternativeInvocation(ci, invPath, 
                origPath, sel, valPath, customPrefix);
    }
    
    private static Tree getInvocationIdentifier(Tree inv) {
        if (inv.getKind() == Tree.Kind.METHOD_INVOCATION) {
            return ((MethodInvocationTree)inv).getMethodSelect();
        } else if (inv.getKind() == Tree.Kind.NEW_CLASS) {
            return ((NewClassTree)inv).getIdentifier();
        } else {
            return null;
        }
    }
    
    // -------------------------------------------------------------------------------------
    // To be moved to java.source.base TreeUtilities
    private static final class DummyJFO extends SimpleJavaFileObject {
        private DummyJFO() {
            super(URI.create("dummy.java"), JavaFileObject.Kind.SOURCE); // NOI18N
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return "";
        }
    };

    private static TypeMirror attributeTree(JavacTaskImpl jti, Tree tree, Scope scope, 
            final List<Diagnostic<? extends JavaFileObject>> errors, @NullAllowed final Diagnostic.Kind filter) {
        Log log = Log.instance(jti.getContext());
        JavaFileObject prev = log.useSource(new DummyJFO());
        Enter enter = Enter.instance(jti.getContext());
        
        Log.DiagnosticHandler discardHandler = log.new DiscardDiagnosticHandler() {
            private Diagnostic.Kind f = filter == null ? Diagnostic.Kind.ERROR : filter;
            @Override
            public void report(JCDiagnostic diag) {
                if (diag.getKind().compareTo(f) >= 0) {
                    errors.add(diag);
                }
            }            
        };
//        ArgumentAttr argumentAttr = ArgumentAttr.instance(jti.getContext());
//        ArgumentAttr.LocalCacheContext cacheContext = argumentAttr.withLocalCacheContext();
        try {
//            enter.shadowTypeEnvs(true);
            Attr attr = Attr.instance(jti.getContext());
            Env<AttrContext> env = ((JavacScope) scope).getEnv();
            if (tree instanceof JCTree.JCExpression) {
                return attr.attribExpr((JCTree) tree,env, Type.noType);
            }
            return attr.attribStat((JCTree) tree,env);
        } finally {
//            cacheContext.leave();
            log.useSource(prev);
            log.popDiagnosticHandler(discardHandler);
//            enter.shadowTypeEnvs(false);
        }
    }
    // -------------------------------------------------------------------------------------

    private static boolean resolveAlternativeInvocation(
            CompilationInfo ci, TreePath invPath, 
            TreePath origPath,
            Tree sel, TreePath valPath, String customPrefix) {
        CharSequence source = ci.getSnapshot().getText();
        Element e = ci.getTrees().getElement(invPath);
        if (!(e instanceof ExecutableElement)) {
            return false;
        }
        SourcePositions sp = ci.getTrees().getSourcePositions();
        
        int invOffset = (int)sp.getEndPosition(ci.getCompilationUnit(), sel) - 1;
        int origExpStart = (int)sp.getStartPosition(ci.getCompilationUnit(), 
                origPath.getLeaf());
        int origExpEnd = (int)sp.getEndPosition(ci.getCompilationUnit(), 
                origPath.getLeaf());
        
        if (invOffset < 0 || origExpStart < 0 || origExpEnd < 0) {
            return false;
        }
        TreePath exp = invPath;
        boolean statement = false;
        
        // try to minimize the parsed content: find the nearest expression that breaks the type inference,
        // typically break if the method is contained within a condition of a switch/if/loop.
        out: do {
            boolean breakPrev = false;
            TreePath previousPath = exp;
            Tree previous = exp.getLeaf();
            exp = exp.getParentPath();
            Tree t = exp.getLeaf();
            Class c = t.getKind().asInterface();
            if (c == CompoundAssignmentTree.class ||
                c == AssignmentTree.class) {
                break;
            }
            switch (t.getKind()) {
                case VARIABLE: {
                    // the declaration is omitted so that the name will not clash
                    // with existing names in the source
                    // PENDING: what if tested expression is the initializer and
                    // invalid typecast makes the value not assignable to the variable ?
                    VariableTree vt = (VariableTree)t;
                    if (vt.getInitializer() == previous) {
                        breakPrev = true;
                    }
                    break;
                }
                case CONDITIONAL_EXPRESSION: {
                    // if the tree is the condition part, then we're done and the result is a boolean.
                    ConditionalExpressionTree ctree = (ConditionalExpressionTree)t;
                    if (ctree.getCondition() == previous) {
                        breakPrev = true;
                    }
                    break;
                }
                case DO_WHILE_LOOP: {
                    DoWhileLoopTree dlp = (DoWhileLoopTree)t;
                    if (dlp.getCondition() == previous) {
                        breakPrev = true;
                    }
                    break;
                }
                case FOR_LOOP: {
                    ForLoopTree flp =(ForLoopTree)t;
                    if (previous == flp.getCondition()) {
                        breakPrev = true;
                    }
                    break;
                }
                    
                case ENHANCED_FOR_LOOP: {
                    EnhancedForLoopTree eflp = (EnhancedForLoopTree)t;
                    if (previous == eflp.getExpression()) {
                        breakPrev = true;
                    }
                    break;
                }
                case SWITCH: {
                    SwitchTree st = (SwitchTree)t;
                    if (previous == st.getExpression()) {
                        breakPrev = true;
                    }
                    break;
                }
                case SYNCHRONIZED: {
                    SynchronizedTree st = (SynchronizedTree)t;
                    if (previous == st.getExpression()) {
                        breakPrev = true;
                    }
                    break;
                }
                case WHILE_LOOP: {
                    WhileLoopTree wlt = (WhileLoopTree)t;
                    if (previous == wlt.getCondition()) {
                        breakPrev = true;
                    }
                    break;
                }
                case IF: {
                    IfTree it = (IfTree)t;
                    if (previous == it.getCondition()) {
                        breakPrev = true;
                    }
                    break;
                }
            }
            if (breakPrev) {
                exp = previousPath;
                break;
            }
            if (isStatement(t)) {
                statement = true;
                break;
            }
        } while (exp.getParentPath()!= null);
        TreePath stPath = exp;
        if (!statement) {
            while (stPath != null && !(isStatement(stPath.getLeaf()))) {
                stPath = stPath.getParentPath();
            }
        }
        if (stPath == null) {
            return false;
        }

        int baseIndex = (int)sp.getStartPosition(ci.getCompilationUnit(), exp.getLeaf());
        if (baseIndex < 0) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(source.subSequence(baseIndex, origExpStart));
        // instead of the boxing expression, append only the value expression, in parenthesis
        sb.append("("); // NOI18N
        if (customPrefix != null) {
            sb.append(customPrefix);
        }
        int valStart = (int)sp.getStartPosition(ci.getCompilationUnit(), valPath.getLeaf());
        int valEnd = (int)sp.getEndPosition(ci.getCompilationUnit(), valPath.getLeaf());
        int expEndPos = (int)sp.getEndPosition(ci.getCompilationUnit(), exp.getLeaf());
        if (valStart < 0 || valEnd < 0 || expEndPos < 0) {
            return false;
        }
        sb.append(source.subSequence(valStart, valEnd)).append(")"); // NOI18N
        sb.append(source.subSequence(origExpEnd, expEndPos));
        
        SourcePositions[] nsp = new SourcePositions[1];
        Tree t;
        if (statement) {
            sb.append(";"); // NOI18N
            t = ci.getTreeUtilities().parseStatement(sb.toString(), nsp);
        } else {
            t = ci.getTreeUtilities().parseExpression(sb.toString(), nsp);
        }
        Scope s = ci.getTreeUtilities().scopeFor(Math.max(0, expEndPos - 1));
        List<Diagnostic<? extends JavaFileObject>> diags = new ArrayList<>();
        attributeTree(JavaSourceAccessor.getINSTANCE().getJavacTask(ci), t, s, diags, null);
        if (!diags.isEmpty()) {
            return false;
        }
        TreePath newPath = new TreePath(exp.getParentPath(), t);
        // path for the method invocation within the newly formed expression or statement.
        // the +1 ensures that we are inside the method invocation subtree (method has >= 1 char as ident)
        TreePath newInvPath = ci.getTreeUtilities().pathFor(newPath, invOffset - baseIndex + 1, nsp[0]);
        while (newInvPath != null && 
            newInvPath.getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION &&
            newInvPath.getLeaf().getKind() != Tree.Kind.NEW_CLASS) {
            newInvPath = newInvPath.getParentPath();
        }
        if (newInvPath == null) {
            return false;
        }
        
        TreePath orig = new TreePath(invPath, getInvocationIdentifier(invPath.getLeaf()));
        TreePath alt = new TreePath(newInvPath, getInvocationIdentifier(newInvPath.getLeaf()));
        
        TypeMirror origType = ci.getTrees().getTypeMirror(orig);
        TypeMirror altType = ci.getTrees().getTypeMirror(alt);
        return altType != null &&  ci.getTypes().isSameType(altType, origType);
//        
//        Element me = ci.getTrees().getElement(newInvPath);
//        return me != null && (me.getKind() == ElementKind.CONSTRUCTOR || me.getKind() == ElementKind.METHOD) ?
//                (ExecutableElement)me : null;
    }
    
    public static boolean isJavaString(CompilationInfo ci, TypeMirror m) {
        if (m == null || m.getKind() != TypeKind.DECLARED) {
            return false;
        } 
        Element e = ((DeclaredType)m).asElement();
        return (e.getKind() == ElementKind.CLASS && ((TypeElement)e).getQualifiedName().contentEquals("java.lang.String")); // NOI18N
    }

    /**
     * Strips the variable name off prefixes and suffixes configured in the coding style. Handles 
     * fields, local variables, parameters and constants.
     * 
     * @param style the code style
     * @param el the variable element 
     * @return name stripped of prefixes/suffices
     */
    public static String stripVariableName(CodeStyle style, VariableElement el) {
        String n = el.getSimpleName().toString();
        switch (el.getKind()) {
            case PARAMETER:
                return stripVariableName(n, style.getParameterNamePrefix(), style.getParameterNameSuffix());
                
            case LOCAL_VARIABLE:
            case RESOURCE_VARIABLE:
            case EXCEPTION_PARAMETER:
                return stripVariableName(n, style.getLocalVarNamePrefix(), style.getLocalVarNameSuffix());
                
            case FIELD: {
//                boolean c = el.getModifiers().containsAll(EnumSet.of(Modifier.FINAL, Modifier.STATIC));
//                if (!c) {
//                    break;
//                }
                // fall through to enum constant
            }
            case ENUM_CONSTANT:
                return stripVariableName(n, style.getFieldNamePrefix(), style.getFieldNameSuffix());
                
            default:
                return n;
        }
    }
    
    private static String stripVariableName(String n, String prefix, String suffix) {
        if (!prefix.isEmpty() && n.startsWith(prefix) && n.length() > prefix.length()) {
            if (Character.isLetter(prefix.charAt(prefix.length() - 1))) {
                // decapitalize the first letter in n
                n = Character.toLowerCase(n.charAt(prefix.length())) + n.substring(prefix.length() + 1);
            } else {
                n = n.substring(prefix.length());
            }
        }
        if (!suffix.isEmpty() && n.endsWith(suffix) && n.length() > suffix.length()) {
            n = n.substring(0, n.length() - suffix.length());
        }
        return n;
    }
    
    public static List<TreePath> getStatementPaths(TreePath firstLeaf) {
        switch (firstLeaf.getParentPath().getLeaf().getKind()) {
            case BLOCK:
                return getTreePaths(firstLeaf.getParentPath(), ((BlockTree) firstLeaf.getParentPath().getLeaf()).getStatements());
            case CASE:
                return getTreePaths(firstLeaf.getParentPath(), ((CaseTree) firstLeaf.getParentPath().getLeaf()).getStatements());
            default:
                return Collections.singletonList(firstLeaf);
        }
    }
    
    private static List<TreePath> getTreePaths(TreePath parent, List<? extends Tree> trees) {
        List<TreePath> ll = new ArrayList<TreePath>(trees.size());
        for (Tree t : trees) {
            ll.add(new TreePath(parent, t));
        }
        return ll;
    }

    /**
     * Determines if assignment looses precision.
     * Works only for primitive types, false for references.
     * 
     * @param from the assigned value type
     * @param to the target type
     * @return true, if precision is lost
     */
    public static boolean loosesPrecision(TypeMirror from, TypeMirror to) {
        if (!from.getKind().isPrimitive() || !to.getKind().isPrimitive()) {
            return false;
        }
        if (to.getKind() == TypeKind.CHAR) {
            return true;
        } else if (from.getKind() == TypeKind.CHAR) {
            return to.getKind() == TypeKind.BYTE || to.getKind() == TypeKind.SHORT;
        }
        return to.getKind().ordinal() < from.getKind().ordinal();
    }

    /**
     * Finds conflicting declarations of methods within type.
     * Given a class `clazz' and a set of methods, finds possible conflicts in the class. For each method,
     * the class is inspected for declarations with the same name, and the same erased types.
     * <p/>
     * Empty Map is returned if no conflicts are found.
     * 
     * @param clazz target class
     * @param methods methods to check
     * @return detected conflicts.
     */
    public static Map<? extends ExecutableElement, ? extends ExecutableElement>  findConflictingMethods(CompilationInfo info, TypeElement clazz, Iterable<? extends ExecutableElement> methods) {
        return findConflictingMethods(info, clazz, false, methods);
    }
    
    /**
     * Finds conflicting declarations of methods. Unlike {@link #findConflictingMethods(org.netbeans.api.java.source.CompilationInfo, javax.lang.model.element.TypeElement, java.lang.Iterable)},
     * it also considers visible methods inherited from supertypes, if `inherited' parameter is true.
     * 
     * @param info context
     * @param clazz class to inspect
     * @param inherited if true, inspects also inherited methods
     * @param methods methods to check
     * @return list of conflicting methods
     */
    public static Map<? extends ExecutableElement, ? extends ExecutableElement>  findConflictingMethods(CompilationInfo info, TypeElement clazz, boolean inherited, Iterable<? extends ExecutableElement> methods) {
        final Map<Name, Collection<ExecutableElement>> currentByName = new HashMap<>();
        Map<ExecutableElement, ExecutableElement> ret = new HashMap<>();
        Iterable<? extends Element> col;
        if (inherited) {
            col = info.getElementUtilities().getMembers(clazz.asType(), null);
        } else {
            col = clazz.getEnclosedElements();
        }
        for (Element e : col) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement)e;
            Name n = ee.getSimpleName();
            Collection<ExecutableElement> named = currentByName.get(n);
            if (named == null) {
                named = new ArrayList<>(3);
                currentByName.put(n, named);
            }
            named.add(ee);
        }
        oneMethod: for(ExecutableElement method : methods) {
            DeclaredType asMemberOf = (DeclaredType)clazz.asType();
            ExecutableType et;
            try {
                et = (ExecutableType)info.getTypes().asMemberOf(asMemberOf, method);
            } catch (IllegalArgumentException iae) {
                continue;
            }
            Collection<ExecutableElement> candidates = currentByName.get(method.getSimpleName());
            if (candidates != null) {
                check: for (ExecutableElement e : candidates) {
                    if (e.getKind() != ElementKind.METHOD) {
                        continue;
                    }
                    ExecutableElement ee = (ExecutableElement)e;
                    if (!ee.getSimpleName().equals(method.getSimpleName())) {
                        continue;
                    }
                    if (ee.getParameters().size() != et.getParameterTypes().size()) {
                        continue;
                    }
                    for (int i = 0; i < ee.getParameters().size(); i++) {
                        TypeMirror t1 = ee.getParameters().get(i).asType();
                        TypeMirror t2 = et.getParameterTypes().get(i);
                        if (!info.getTypes().isSameType(
                                info.getTypes().erasure(t1),
                                info.getTypes().erasure(t2)
                        )) {
                            continue check;
                        }
                    }
                    // skip
                    ret.put(method, e);
                }
            }
        }
        return ret;
    }
    
    private static boolean isSuperCtorInvocation(Tree t) {
        if (t.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
            t = ((ExpressionStatementTree)t).getExpression();
        }
        if (t.getKind() != Tree.Kind.METHOD_INVOCATION) {
            return false;
        }
        MethodInvocationTree mit = (MethodInvocationTree)t;
        if (mit.getMethodSelect() == null || 
            mit.getMethodSelect().getKind() != Tree.Kind.IDENTIFIER) {
            return false;
        }
        return ((IdentifierTree)mit.getMethodSelect()).getName().contentEquals("super");
    }
    
    public static final int INSERT_POS_CHILD = 0;
    public static final int INSERT_POS_THEN = 0;
    public static final int INSERT_POS_ELSE = 1;
    public static final int INSERT_POS_UPDATE = 2;
    public static final int INSERT_POS_INIT = 1;
    public static final int INSERT_POS_RESOURCES = 1;


    /**
     * Inserts a statement using an anchor. Only inserts to an existing tree is supported, this method variant should not be used
     * to create tree branches which do not exist yet (i.e. insert into else part of `if', which does not exist yet).
     * Statements are inserted immediately before, or after the given anchor. Anchor identifies a statement; if the anchor leaf Tree
     * is not a statement, the methods uses the closest parent StatementTree as an anchor.
     * <p/>
     * Things which are handled automatically:
     * <ul>
     * <li>change to Block from single statement for if/cycle children
     * <li>change from expression lambda to statement lambda
     * <li>insertion into block, case or control statement structure
     * <li>insertion into for init or update parts
     * </ul>
     * 
     * @param wc working copy for creating Trees
     * @param anchor anchor to insert before/after
     * @param before code to insert before
     * @param after code to insert after
     * @return parent of the inserted code.
     */
    public static Tree insertStatement(WorkingCopy wc, TreePath anchor, List<? extends StatementTree> before, List<? extends StatementTree> after) {
        anchor = findStatementOrExpression(anchor);
        return insertStatement(wc, anchor.getParentPath(), anchor.getLeaf(), before, after, 0);
    }
    
    /**
     * Inserts a statement inside a block or before/after an anchor.
     * To insert a statement into an empty block, specify the parentBlock, no anchor. Statements
     * will be inserted at the start of the block. If no anchor is provided, `before' statements
     * are inserted at the start of parentBlock, afterStatements at the end of it.
     * <p/>
     * Some controls flow statements have multiple parts where statements may be inserted. If
     * no anchor is specified (which points exactly on the proper part), the `position' specifies
     * where the new statements should be inserted.
     * <ul>
     * <li>for(<b>INSERT_POS_INIT</b>; ... ; <b>INSERT_POS_UPDATE</b>) <b>INSERT_POS_CHILD</b>
     * <li>if (...) <b>INSERT_POS_THEN</b> else <b>INSERT_POS_ELSE</b>
     * </ul>
     * <p/>
     * If the parentBlock (or parent of the anchor) is not a block, but a single child statement of a control flow tree, it
     * will be replaced by a proper BlockTree.
     * <p/>
     * Note that the return value is somewhat fuzzy. The parent of the inserted statments will be returned, but
     * the parent may be a control flow tree (i.e. for if update statement is changed) or it can be a surrounding BlockTree
     * (i.e. body block of `for' or `try' statement)
     * <p/>
     * When inserting statements <b>at the start of constructors</b> the method checks whether the inserted code contains a super call.
     * If it does not, it will actually insert the stament <i>after super() constructor call</i>. This allows generic code that inserts
     * statements at start of (any) block to handle constructors gracefully without special cases. 
     * 
     * @param wc factory for Trees
     * @param parentBlock parent tree, may be {@code null} if anchor is specified
     * @param anchor anchor before/after which the statements are inserted, may be {@code null}
     * @param after statements to insert after the anchor or at the end of block
     * @param before statemtents to isnert before the anchor or at the start of block
     * @param position for control flow commands, specifies the part where new statements should be inserted
     * @return parent of the inserted statements
     */
    public static Tree insertStatement(WorkingCopy wc, TreePath parentBlock,
            Tree anchor, List<? extends StatementTree> before, List<? extends StatementTree> after, int position) {
        if (parentBlock == null) {
            throw new IllegalArgumentException("One of parent/anchor must be specified");
        }
         List<StatementTree> list = new ArrayList<>();
        Tree parent = wc.resolveRewriteTarget(parentBlock.getLeaf());
        TreePath parentPath = parent == parentBlock ? parentBlock : new TreePath(parentBlock.getParentPath(), parent);
        TreeMaker mk = wc.getTreeMaker();
        Tree child = null;
        List<? extends StatementTree> forInit = null;
        List<? extends ExpressionStatementTree> forUpdate = null;
        boolean superInvocationPresent = false;
        boolean convertExpressionLambda = false;
        
        switch (parent.getKind()) {
            case LAMBDA_EXPRESSION: {
                LambdaExpressionTree let = (LambdaExpressionTree)parent;
                // since it is a parent, it must be a expression lambda
                child = let.getBody();
                convertExpressionLambda = let.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION;
                break;
            }
            case TRY:
                child = ((TryTree)parent).getFinallyBlock();
                break;
            case IF:
                child = position != INSERT_POS_ELSE ? 
                        ((IfTree)parent).getThenStatement() :
                        ((IfTree)parent).getElseStatement();
                break;
            case DO_WHILE_LOOP:
                child = ((DoWhileLoopTree)parent).getStatement();
                break;
            case WHILE_LOOP:
                child = ((WhileLoopTree)parent).getStatement();
                break;
            case CASE:
                // special case for now;
                list.addAll(((CaseTree)parent).getStatements());
                break;
            case FOR_LOOP:
                forInit = ((ForLoopTree)parent).getInitializer();
                forUpdate = ((ForLoopTree)parent).getUpdate();
                switch (position) {
                    case INSERT_POS_INIT:
                        list.addAll(forInit);
                        break;
                    case INSERT_POS_UPDATE:
                        list.addAll(forUpdate);
                        break;
                    default:
                        child = ((ForLoopTree)parent).getStatement();
                }
                break;
            case ENHANCED_FOR_LOOP:
                child = ((ForLoopTree)parent).getStatement();
                break;
            case BLOCK: {
                // possible special case: if the block's parent is a constructor, the statements
                // may start with a super constructor call:
                List<? extends StatementTree> stats = getRealStatements(wc, parentPath);
                if (parentBlock.getParentPath().getLeaf().getKind() == Tree.Kind.METHOD &&
                    ((MethodTree)parentBlock.getParentPath().getLeaf()).getName().contentEquals("<init>")) {
                    superInvocationPresent = !stats.isEmpty() && isSuperCtorInvocation(stats.get(0));
                }
                list.addAll(stats);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported parent kind: " + parentBlock.getLeaf().getKind());
        }
        Tree result = null;
        if (child == null) {
            int indexAt = 0;
            List<StatementTree> newList = new ArrayList<>(list.size());
            if (anchor != null) {
                indexAt = list.indexOf(anchor);
                if (indexAt == -1) {
                    throw new IllegalArgumentException("Anchor not proper child of its parent");
                }
            } else if (superInvocationPresent && before != null) {
                if (indexAt == 0 && !isSuperCtorInvocation(before.get(0))) {
                    indexAt = 1;
                }
            }
            
            // if no anchor is defined, either nothing, or ctor invocation will be inserted
            newList.addAll(list.subList(0, indexAt));
            if (before != null) {
                newList.addAll(before);
            }
            if (anchor != null) {
                newList.add((StatementTree)anchor);
            } else {
                newList.addAll(list.subList(indexAt, list.size()));
            }
            if (after != null) {
                newList.addAll(after);
            }
            if (anchor != null) {
                newList.addAll(list.subList(indexAt + 1, list.size()));
            }
            Tree newChild = newList.size() == 1 ? 
                    newList.get(0) :
                    mk.Block(newList, false);
            switch (parent.getKind()) {
                case TRY:
                    switch (position) {
                        case INSERT_POS_RESOURCES:
                            wc.rewrite(parent,
                                result = mk.Try(
                                    newList,
                                    ((TryTree)parent).getBlock(),
                                    ((TryTree)parent).getCatches(),
                                    ((TryTree)parent).getFinallyBlock()
                            ));
                            break;
                        default:
                            result = mk.Try(
                                    ((TryTree)parent).getResources(),
                                    ((TryTree)parent).getBlock(),
                                    ((TryTree)parent).getCatches(),
                                    mk.Block(newList, false)
                            );
                            wc.rewrite(parent, result);
                            break;
                    }
                    break;
                case IF:
                    switch (position) {
                        case INSERT_POS_ELSE:
                            wc.rewrite(parent, 
                                result = mk.If(
                                    ((IfTree)parent).getCondition(),
                                    ((IfTree)parent).getThenStatement(),
                                    (StatementTree)newChild
                                )
                            );
                            break;
                        default:
                            wc.rewrite(parent, 
                                result = mk.If(
                                    ((IfTree)parent).getCondition(),
                                    (StatementTree)newChild,
                                    ((IfTree)parent).getElseStatement()
                                )
                            );
                            break;
                    }
                    break;
                case CASE:
                    wc.rewrite(parent, 
                        result = mk.Case(((CaseTree)parent).getExpression(), newList)
                    );
                    break;
                case FOR_LOOP:
                    switch (position) {
                        case INSERT_POS_INIT:
                            wc.rewrite(
                                parent,
                                result = mk.ForLoop(
                                    newList, 
                                    ((ForLoopTree)parent).getCondition(),
                                    forUpdate,
                                    ((ForLoopTree)parent).getStatement()
                            ));
                            break;

                        case INSERT_POS_UPDATE:
                            wc.rewrite(parent,
                                result = mk.ForLoop(
                                    forInit,
                                    ((ForLoopTree)parent).getCondition(),
                                    (List<? extends ExpressionStatementTree>)(List)newList,
                                    ((ForLoopTree)parent).getStatement()
                            ));
                            break;

                        default:
                            throw new IllegalArgumentException();
                    }
                    break;
                case BLOCK:
                    wc.rewrite(parent,
                        mk.Block(
                            newList, ((BlockTree)parent).isStatic()));
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        } else if (child.getKind() == Tree.Kind.BLOCK) {
            list = (List)getRealStatements(wc, new TreePath(parentPath, child));
            int indexAt = 0;
            List<StatementTree> newList = new ArrayList<>(list.size());
            if (anchor != null) {
                indexAt = list.indexOf(anchor);
                if (indexAt == -1) {
                    throw new IllegalArgumentException("Anchor not proper child of its parent");
                }
            }
            newList.addAll(list.subList(0, indexAt));
            if (before != null) {
                newList.addAll(before);
            }
            if (anchor != null) {
                newList.add((StatementTree)anchor);
            } else {
                newList.addAll(list);
            }
            if (after != null) {
                newList.addAll(after);
            }
            Tree newChild = mk.Block(newList, false);
            wc.rewrite(child, newChild);
        } else {
            Tree toRewrite = child;
            List<StatementTree> newList = new ArrayList<>();
            if (before != null) {
                newList.addAll(before);
            }
            // special cases for expression Lambdas, which become statement ones:
            Tree ch = child;
            if (convertExpressionLambda) {
                LambdaExpressionTree let = (LambdaExpressionTree)parent;
                TypeMirror oldType = wc.getTrees().getTypeMirror(new TreePath(parentBlock, let.getBody()));
                // replace former expression with expression statement or return
                if (!isStatement(child)) {
                    if (oldType != null && oldType.getKind() != TypeKind.VOID) {
                        newList.add(
                            mk.asReplacementOf(
                                    mk.Return((ExpressionTree)let.getBody()), child
                        ));
                    } else {
                        newList.add(
                            mk.ExpressionStatement((ExpressionTree)let.getBody())
                        );
                    }
                }
            } else {
                newList.add((StatementTree)wc.resolveRewriteTarget(ch));
            }
            if (after != null) {
                newList.addAll(after);
            }
            result = mk.Block(newList, false);
            // special cases for expression Lambdas, which become statement ones:
            if (convertExpressionLambda) {
                LambdaExpressionTree let = (LambdaExpressionTree)parent;
                toRewrite = mk.LambdaExpression(
                    let.getParameters(),
                    result
                );
            }
            wc.rewrite(toRewrite, result);
        }
        return result;
    }
    
    /**
     * Removes the statement. If the statement is the only child statement of
     * an if, while, do-while - replaces the statement with an empty block.
     * 
     * @param wc working copy for creating trees
     * @param toRemove path to the statement to be removed
     */
    public static Tree removeStatement(WorkingCopy wc, TreePath toRemove) {
        return removeStatements (wc, toRemove, null);
    }
    
    /**
     * Finds the entire statement up the tree. For code nested in blocks or control
     * flow statements, find the statement (i.e ExpressionStatement for an ExpressionTree).
     * For initializers and update statements, find that init/update statement nested in for cycle.
     * For expression lambdas, returns the outermost expression.
     * <p/>
     * Returns {@code null} if it cannot find enclosing statement.
     * 
     * @param path where to start
     * @return the nearest statement or entire expression
     */
    public static TreePath findStatementOrExpression(TreePath path) {
        while (!isStatement(path.getLeaf())) {
            Tree l = path.getLeaf();
            TreePath next = path.getParentPath();
            if (next == null) {
                return null;
            }
            Tree t = next.getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                return null;
            }
            if (t.getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
                break;
            }
            path = next;
        }
        return path;
    }
    
    public static Tree removeStatements(WorkingCopy wc, TreePath toRemove, Tree removeEnd) {
        toRemove = findStatementOrExpression(toRemove);
        TreePath parent = toRemove.getParentPath();
        switch (parent.getLeaf().getKind()) {
            case EXPRESSION_STATEMENT:
                return removeStatement(wc, parent);

            // removing last statement from lambda may introduce an error
            // -- missing return statement.
            case LAMBDA_EXPRESSION:
            case WHILE_LOOP:
            case DO_WHILE_LOOP:
            case FOR_LOOP:
            case ENHANCED_FOR_LOOP:
            case IF:
                // since direct child was removed, we need to add an empty statement.
                // but since semicolon itself would be nearly invisible, I add an empty block
                // instead.
                return replaceStatement(wc, toRemove, 
                        Collections.singletonList(
                                wc.getTreeMaker().Block(Collections.emptyList(), false)
                        ));
                
            case BLOCK: {
                BlockTree originalBlock = (BlockTree)parent.getLeaf();
                BlockTree bt = (BlockTree) wc.resolveRewriteTarget(originalBlock);
                List<? extends StatementTree> stats = originalBlock == bt ? getRealStatements(wc, parent)
                                                                          : bt.getStatements();
                int index = stats.indexOf(toRemove.getLeaf());
                if (index == -1) {
                    throw new IllegalArgumentException("Not proper child of the parent path");
                }
                int indexTo = index;
                if (removeEnd != null) {
                    indexTo = stats.indexOf(indexTo);
                    if (indexTo == -1) {
                        throw new IllegalArgumentException("Not proper child of the parent path");    
                    }
                }
                List<StatementTree> sts = new ArrayList<>(stats.size() -1);
                if (index > 0) {
                    sts.addAll(stats.subList(0, index));
                }
                sts.addAll(stats.subList(indexTo + 1, stats.size()));
                
                BlockTree nb = wc.getTreeMaker().Block(sts, bt.isStatic());
                
                // TODO: special case for lambda expressions; if the block is a single return statement,
                // the parent lambda may be rewritten to an expression lambda
                wc.rewrite(parent.getLeaf(), nb);
                return nb;
            }
        }
        throw new IllegalArgumentException("Unknown parent type");
    }
    
    private static List<? extends StatementTree> getRealStatements(CompilationInfo info, TreePath path) {
        assert path.getLeaf().getKind() == Tree.Kind.BLOCK;
        BlockTree bt = (BlockTree)path.getLeaf();
        List<? extends StatementTree> stats = bt.getStatements();
        if (stats.isEmpty()) {
            return stats;
        }
        List<StatementTree> newStats = null;
        for (int i = 0; i < stats.size(); i++) {
            StatementTree t = stats.get(i);
            TreePath stPath = new TreePath(path, t);
            if (info.getTreeUtilities().isSynthetic(stPath)) {
                newStats = new ArrayList<>(stats.size());
            } else {
                newStats = new ArrayList<>(stats.size());
                newStats.addAll(stats.subList(i, stats.size()));
                break;
            }
        }
        return newStats == null ? stats : newStats;
    }

    /**
     * Replaces statement for one or more statements. If the replaced statement
     * is a direct child of if, while etc, an intermediate BlockTree is created.
     * If the replacement contains VariableTrees, the caller must ensure they do not conflict with
     * existing declarations in scope.
     * 
     * @param wc working copy
     * @param tp path to replace
     * @param stats new statements to appear at the position
     * @return the parent of the replaced statement.
     */
    public static Tree replaceStatement(WorkingCopy wc, 
            TreePath tp, List<? extends StatementTree> stats) {
        if (!isStatement(tp.getLeaf())) {
            throw new IllegalArgumentException();
        }
        if (stats == null || stats.isEmpty()) {
            return removeStatement(wc, tp);
        }
        return replaceStatements(wc, tp, null, stats);
    }
    
    private static boolean isStatement(Tree t) {
        return StatementTree.class.isAssignableFrom(t.getKind().asInterface());
    }
    
    public static Tree replaceStatements(WorkingCopy wc, 
            TreePath tp, Tree last, List<? extends StatementTree> stats) {
        tp = findStatementOrExpression(tp);
        if (tp == null) {
            throw new IllegalArgumentException();
        }
        if (stats == null || stats.isEmpty()) {
            return removeStatements(wc, tp, last);
        }

        TreeMaker make = wc.getTreeMaker();
        List<? extends Tree> statements;
        Tree parent = tp.getParentPath().getLeaf();
        
        boolean trueBranch = false;
        
        switch (parent.getKind()) {
            case LAMBDA_EXPRESSION:
                // special case: if replacing with a single replacement
                if (stats.size() == 1) {
                    LambdaExpressionTree let = (LambdaExpressionTree)parent;
                    StatementTree st = stats.get(0);
                    if (st.getKind() == Tree.Kind.RETURN) {
                        Tree x = ((ReturnTree)st).getExpression();
                        if (let.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
                            wc.rewrite(let.getBody(), x);
                            return let;
                        } else {
                            Tree t = make.LambdaExpression(let.getParameters(), x);
                            wc.rewrite(let, t);
                            return let;
                        }
                    }
                    // normal statement
                    if (let.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
                        Tree t = make.LambdaExpression(let.getParameters(), 
                                make.Block(stats, false));
                        wc.rewrite(let, t);
                        return let;
                    } else {
                        Tree t = make.Block(stats, false);
                        wc.rewrite(let.getBody(), t);
                        return let;
                    }
                }
                
                // this must be an expression lambda
                statements = Collections.singletonList(
                    ((LambdaExpressionTree)parent).getBody()
                );
                break;
            case IF:
                trueBranch = ((IfTree)parent).getThenStatement() == tp.getLeaf();
                statements = Collections.singletonList(
                    trueBranch ?
                        ((IfTree)parent).getThenStatement() :
                        ((IfTree)parent).getElseStatement()
                );
                break;
                
            case WHILE_LOOP:
                statements = Collections.singletonList(
                    ((WhileLoopTree)parent).getStatement()
                );
                break;
            case DO_WHILE_LOOP:
                statements = Collections.singletonList(
                    ((DoWhileLoopTree)parent).getStatement()
                );
                break;
            case FOR_LOOP:
                statements = Collections.singletonList(
                    ((ForLoopTree)parent).getStatement()
                );
                break;
            case ENHANCED_FOR_LOOP:
                statements = Collections.singletonList(
                    ((EnhancedForLoopTree)parent).getStatement()
                );
                break;
            case BLOCK: statements = getRealStatements(wc, tp.getParentPath()); break;
            case CASE: statements = ((CaseTree) parent).getStatements(); break;
            case METHOD:  {
                // replacing entire body of the method
                BlockTree methodBody;
                if (stats.size() == 1 && stats.get(0).getKind() == Tree.Kind.BLOCK) {
                    methodBody = (BlockTree)stats.get(0);
                } else {
                    methodBody = wc.getTreeMaker().Block(stats, false);
                }
                wc.rewrite(((MethodTree)parent).getBody(), methodBody);
                return methodBody;
            }
            default: throw new IllegalStateException(parent.getKind().name());
        }
        
        Tree var = (Tree) tp.getLeaf();
        int current = statements.indexOf(tp.getLeaf());
        int upTo = current;
        if (last != null) {
            upTo = statements.indexOf(last);
            if (upTo == -1) {
                throw new IllegalArgumentException();
            }
        }
        List<StatementTree> newStatements = new ArrayList<>();

        newStatements.addAll((List)statements.subList(0, current));
        newStatements.add(
            make.asReplacementOf(stats.get(0), var)
        );
        newStatements.addAll(stats.subList(1, stats.size()));
        newStatements.addAll((List)statements.subList(upTo + 1, statements.size()));

        StatementTree blockOrStat;
        
        Tree toRewrite = parent;
        if (newStatements.size() == 1) {
            Tree t = newStatements.get(0);
            if (t.getKind() == Tree.Kind.VARIABLE) {
                blockOrStat = make.Block((List)newStatements, false);
            } else {
                blockOrStat = (StatementTree)t;
            }
        } else {
            blockOrStat = make.Block((List)newStatements, false);
        }
        
        Tree target = blockOrStat;
        switch (parent.getKind()) {
            case LAMBDA_EXPRESSION: {
                LambdaExpressionTree let = (LambdaExpressionTree)parent;
                if (let.getBodyKind() == LambdaExpressionTree.BodyKind.EXPRESSION) {
                    target = make.LambdaExpression(let.getParameters(), blockOrStat);
                } else {
                    toRewrite = let.getBody();
                }
                break;
            }
            case IF:
                target = make.If(
                        ((IfTree)parent).getCondition(),
                        trueBranch ? blockOrStat : ((IfTree)parent).getThenStatement(),
                        !trueBranch ? blockOrStat : ((IfTree)parent).getElseStatement()
                    );
                break;
            case WHILE_LOOP:
                target = make.WhileLoop(
                        ((WhileLoopTree)parent).getCondition(),
                        blockOrStat
                );
                break;
            case DO_WHILE_LOOP:
                target = make.DoWhileLoop(
                        ((DoWhileLoopTree)parent).getCondition(),
                        blockOrStat
                );
                break;
            case FOR_LOOP:
                target = make.ForLoop(
                        ((ForLoopTree)parent).getInitializer(),
                        ((ForLoopTree)parent).getCondition(),
                        ((ForLoopTree)parent).getUpdate(),
                        blockOrStat
                );
                break;
            case ENHANCED_FOR_LOOP:
                target = make.EnhancedForLoop(
                        ((EnhancedForLoopTree)parent).getVariable(),
                        ((EnhancedForLoopTree)parent).getExpression(),
                        blockOrStat
                );
                break;
            case BLOCK: target = 
                    make.Block(newStatements, ((BlockTree) parent).isStatic()); break;
            case CASE: target = 
                    make.Case(((CaseTree) parent).getExpression(), newStatements); break;
            default: throw new IllegalStateException(parent.getKind().name());
        }
        StatementTree ret  = (StatementTree)make.asReplacementOf(target, toRewrite);
        wc.rewrite(toRewrite, ret);
        return ret;
    }

    /**
     * Negates an expression, returns negated Tree. The `original` should be a direct child 
     * of `parent' or parent must be {@code null}. With a non-null parent, surrounding parenthesis
     * will be added if the language syntax requires it.
     * 
     * @param make factory for Trees, obtain from WorkingCopy
     * @param original the tree to be negated
     * @param parent the parent of the negated tree.
     * @return negated expression, possibly parenthesized
     */
    public static ExpressionTree negate(TreeMaker make, ExpressionTree original, Tree parent) {
        ExpressionTree newTree;
        switch (original.getKind()) {
            case PARENTHESIZED:
                ExpressionTree expr = ((ParenthesizedTree) original).getExpression();
                newTree = negate(make, expr, original);
                break;
            case LOGICAL_COMPLEMENT:
                newTree = ((UnaryTree) original).getExpression();
                while (newTree.getKind() == Kind.PARENTHESIZED && !JavaFixUtilities.requiresParenthesis(((ParenthesizedTree) newTree).getExpression(), original, parent)) {
                    newTree = ((ParenthesizedTree) newTree).getExpression();
                }
                break;
            case NOT_EQUAL_TO:
                newTree = negateBinaryOperator(make, original, Kind.EQUAL_TO, false);
                break;
            case EQUAL_TO:
                newTree = negateBinaryOperator(make, original, Kind.NOT_EQUAL_TO, false);
                break;
            case BOOLEAN_LITERAL:
                newTree = make.Literal(!(Boolean) ((LiteralTree) original).getValue());
                break;
            case CONDITIONAL_AND:
                newTree = negateBinaryOperator(make, original, Kind.CONDITIONAL_OR, true);
                break;
            case CONDITIONAL_OR:
                newTree = negateBinaryOperator(make, original, Kind.CONDITIONAL_AND, true);
                break;
            case LESS_THAN:
                newTree = negateBinaryOperator(make, original, Kind.GREATER_THAN_EQUAL, false);
                break;
            case LESS_THAN_EQUAL:
                newTree = negateBinaryOperator(make, original, Kind.GREATER_THAN, false);
                break;
            case GREATER_THAN:
                newTree = negateBinaryOperator(make, original, Kind.LESS_THAN_EQUAL, false);
                break;
            case GREATER_THAN_EQUAL:
                newTree = negateBinaryOperator(make, original, Kind.LESS_THAN, false);
                break;
            default:
                newTree = make.Unary(Kind.LOGICAL_COMPLEMENT, original);
                if (JavaFixUtilities.requiresParenthesis(original, original, newTree)) {
                    newTree = make.Unary(Kind.LOGICAL_COMPLEMENT, make.Parenthesized(original));
                }
                break;
        }

        if (JavaFixUtilities.requiresParenthesis(newTree, original, parent)) {
            newTree = make.Parenthesized(newTree);
        }

        return newTree;
    }

    private static ExpressionTree negateBinaryOperator(TreeMaker make, Tree original, Kind newKind, boolean negateOperands) {
        BinaryTree bt = (BinaryTree) original;
        ExpressionTree left = bt.getLeftOperand();
        ExpressionTree right = bt.getRightOperand();
        if (negateOperands) {
            left = negate(make, left, original);
            right = negate(make, right, original);
        }
        return make.Binary(newKind, left, right);
    }
    
    public static FileObject getModuleInfo(CompilationInfo info) {
        if (info.getSourceVersion().compareTo(SourceVersion.RELEASE_9) > 0) {
            return null;
        }
        return info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE).findResource("module-info.java");
    }
    
    public static boolean isModular(CompilationInfo info) {
        return getModuleInfo(info) != null;
    }
    
    public static boolean isAnonymousType(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) type;
            TypeElement typeElem = (TypeElement) dt.asElement();
            if (typeElem.getNestingKind() == NestingKind.ANONYMOUS) {
                return true;
            }
        }
        return false;
    }

    public static ExecutableElement getFunctionalMethodFromElement(CompilationInfo info, Element element) {
        Element classType = info.getTypes().asElement(element.asType());

        //return null if classType is invalid, such as a primitive type
        if (classType == null || !classType.getKind().isInterface()) {
            return null;
        }

        ExecutableElement elementToReturn = null;
        int methodCounter = 0;
        for (Element e : classType.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.ABSTRACT)) {
                elementToReturn = (ExecutableElement) e;
                methodCounter++;
            }
        }

        //not a functional element, i.e. doesn't declare a single method
        if (methodCounter != 1) {
            return null;
        }

        return elementToReturn;
    }

    public static boolean completesNormally(CompilationInfo info, TreePath tp) {
        class Scanner extends TreePathScanner<Void, Void> {

            private boolean completesNormally = true;
            private Set<Tree> seenTrees = new HashSet<>();

            @Override
            public Void visitReturn(ReturnTree node, Void p) {
                completesNormally = false;
                return null;
            }

            @Override
            public Void visitBreak(BreakTree node, Void p) {
                completesNormally &= seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()));
                return null;
            }

            @Override
            public Void visitContinue(ContinueTree node, Void p) {
                completesNormally &= seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()));
                return null;
            }

            @Override
            public Void visitThrow(ThrowTree node, Void p) {
                completesNormally = false;
                return null;
            }

            @Override
            public Void visitIf(IfTree node, Void p) {
                boolean origCompletesNormally = completesNormally;
                scan(node.getThenStatement(), p);
                boolean afterThen = completesNormally;
                completesNormally = origCompletesNormally;
                scan(node.getElseStatement(), p);
                completesNormally |= afterThen;
                return null;
            }

            @Override
            public Void visitSwitch(SwitchTree node, Void p) {
                //exhaustiveness: (TODO)
                boolean hasDefault = node.getCases().stream().anyMatch(c -> c.getExpression() == null);
                if (node.getCases().size() > 0) {
                    scan(node.getCases().get(node.getCases().size() - 1), p);
                }
                completesNormally |= !hasDefault;
                return null;
            }

            //TODO: loops
            @Override
            public Void scan(Tree tree, Void p) {
                seenTrees.add(tree);
                return super.scan(tree, p);
            }

            @Override
            public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                return null;
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                return null;
            }
        }

        Scanner scanner = new Scanner();

        scanner.scan(tp, null);
        return scanner.completesNormally;
    }

    public static boolean isCompatibleWithSwitchExpression(SwitchTree st) {
        boolean firstCase = true;
        Name leftTreeName = null;
        int caseCount = 0;
        List<? extends CaseTree> cases = st.getCases();

        for (CaseTree ct : cases) {
            caseCount++;
            List<StatementTree> statements = new ArrayList<>(ct.getStatements());
            switch (statements.size()) {
                case 0:
                    break;
                case 1:
                    if (firstCase && leftTreeName == null && statements.get(0).getKind() == Tree.Kind.RETURN) {
                        break;
                    } else if (caseCount == cases.size() && statements.get(0).getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                        if (firstCase) {
                            leftTreeName = getLeftTreeName(statements.get(0));
                            if (leftTreeName == null) {
                                return false;
                            }
                            break;
                        } else {
                            Name exprTree = getLeftTreeName(statements.get(0));
                            if (leftTreeName != null && exprTree != null && leftTreeName.contentEquals(exprTree)) {
                                break;
                            } else {
                                return false;
                            }
                        }
                    } else {
                        return false;
                    }
                case 2:
                    if (statements.get(0).getKind() == Tree.Kind.EXPRESSION_STATEMENT && statements.get(1).getKind() == Tree.Kind.BREAK) {
                        if (firstCase) {
                            leftTreeName = getLeftTreeName(statements.get(0));
                            if (leftTreeName == null) {
                                return false;
                            }
                            firstCase = false;
                        }
                        Name exprTree = getLeftTreeName(statements.get(0));
                        if (leftTreeName != null && exprTree != null && leftTreeName.contentEquals(exprTree)) {
                            break;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                default:
                    return false;
            }
        }
        return true;
    }

    public static void performRewriteRuleSwitch(JavaFix.TransformationContext ctx, TreePath tp, Tree st, boolean isSwitchExpression) {
        WorkingCopy wc = ctx.getWorkingCopy();
        TreeMaker make = wc.getTreeMaker();
        List<CaseTree> newCases = new ArrayList<>();
        SWITCH_TYPE switchType = SWITCH_TYPE.TRADITIONAL_SWITCH;
        Tree typeCastTree = null;
        ExpressionTree switchExpr;
        List<? extends CaseTree> cases;
        Set<VariableElement> variablesDeclaredInOtherCases = new HashSet<>();

        List<Tree> patterns = new ArrayList<>();
        Tree leftVariable = null;
        boolean ruleSwitchFlag = st.getKind() == Kind.SWITCH_EXPRESSION;
        if (ruleSwitchFlag) {
            switchExpr = ((SwitchExpressionTree) st).getExpression();
            cases = ((SwitchExpressionTree) st).getCases();
            switchType = SWITCH_TYPE.RULE_SWITCH;
        } else {
            switchExpr = ((SwitchTree) st).getExpression();
            cases = ((SwitchTree) st).getCases();
        }
        for (Iterator<? extends CaseTree> it = cases.iterator(); it.hasNext();) {
            CaseTree ct = it.next();
            TreePath casePath = new TreePath(tp, ct);
            patterns.addAll(ct.getLabels());
            List<StatementTree> statements;
            if (ct.getStatements() == null) {
                statements = new ArrayList<>(((JCTree.JCCase) ct).stats);
            } else {
                statements = new ArrayList<>(ct.getStatements());
            }
            if (statements.isEmpty()) {
                if (it.hasNext()) {
                    continue;
                }
                //last case, no break
            } else if (!ruleSwitchFlag && statements.get(statements.size() - 1).getKind() == Tree.Kind.BREAK
                    && ctx.getWorkingCopy().getTreeUtilities().getBreakContinueTarget(new TreePath(new TreePath(tp, ct), statements.get(statements.size() - 1))) == st) {
                statements.remove(statements.size() - 1);
            } else {
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitBlock(BlockTree node, Void p) {
                        if (!node.getStatements().isEmpty()
                                && node.getStatements().get(node.getStatements().size() - 1).getKind() == Tree.Kind.BREAK
                                && ctx.getWorkingCopy().getTreeUtilities().getBreakContinueTarget(new TreePath(getCurrentPath(), node.getStatements().get(node.getStatements().size() - 1))) == st) {
                            wc.rewrite(node, make.removeBlockStatement(node, node.getStatements().get(node.getStatements().size() - 1)));
                            //TODO: optimize ifs?
                        }
                        return super.visitBlock(node, p);
                    }
                }.scan(new TreePath(new TreePath(tp, ct), statements.get(statements.size() - 1)), null);
            }
            Set<Element> seenVariables = new HashSet<>();
            int idx = 0;
            for (StatementTree statement : new ArrayList<>(statements)) {
                TreePath statementPath = new TreePath(casePath, statement);
                if (statement.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                    ExpressionTree expr = ((ExpressionStatementTree) statement).getExpression();
                    if (expr.getKind() == Tree.Kind.ASSIGNMENT) {
                        AssignmentTree at = (AssignmentTree) expr;
                        Element var = wc.getTrees().getElement(new TreePath(new TreePath(statementPath, at), at.getVariable()));
                        if (variablesDeclaredInOtherCases.contains(var)) {
                            seenVariables.add(var);
                            //XXX: take type from the original variable
                            wc.rewrite(statement,
                                    make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), var.getSimpleName(), make.Type(var.asType()), at.getExpression()));
                        }
                    }
                }
                Set<Element> thisStatementSeenVariables = new HashSet<>();
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitIdentifier(IdentifierTree node, Void p) {
                        Element el = wc.getTrees().getElement(getCurrentPath());
                        if (variablesDeclaredInOtherCases.contains(el) && seenVariables.add(el)) {
                            thisStatementSeenVariables.add(el);
                        }
                        return super.visitIdentifier(node, p);
                    }
                }.scan(statementPath, null);

                if (!thisStatementSeenVariables.isEmpty()) {
                    for (Element el : thisStatementSeenVariables) {
                        VariableElement var = (VariableElement) el;
                        statements.add(idx++, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), var.getSimpleName(), make.Type(var.asType()), null));
                    }
                }
                idx++;
            }
            Tree body = make.Block(statements, false);
            if (statements.size() == 1) {
                if (statements.get(0).getKind() == Tree.Kind.EXPRESSION_STATEMENT
                        || statements.get(0).getKind() == Tree.Kind.THROW
                        || statements.get(0).getKind() == Tree.Kind.BLOCK) {
                    body = statements.get(0);
                }
            }
            if (isSwitchExpression) {
                switchType = SWITCH_TYPE.SWITCH_EXPRESSION;
                if (statements.get(0).getKind() == Tree.Kind.RETURN) {
                    body = ((JCTree.JCReturn) statements.get(0)).getExpression();
                } else {
                    JCTree.JCExpressionStatement jceTree = (JCTree.JCExpressionStatement) statements.get(0);
                    body = ((JCTree.JCAssign) jceTree.expr).rhs;
                    leftVariable = ((JCTree.JCAssign) jceTree.expr).lhs;
                }
                if (body.getKind() == Tree.Kind.TYPE_CAST) {
                        typeCastTree = ((JCTree.JCTypeCast)body).getType();
                        body = ((JCTree.JCTypeCast)body).getExpression();
                    }
                newCases.add(make.CasePatterns(patterns, ct.getGuard(), make.ExpressionStatement((ExpressionTree) body)));
            } else {
                newCases.add(make.CasePatterns(patterns, ct.getGuard(), body));
            }

            patterns = new ArrayList<>();
            for (StatementTree statement : getSwitchStatement(ct)) {
                if (statement.getKind() == Tree.Kind.VARIABLE) {
                    variablesDeclaredInOtherCases.add((VariableElement) wc.getTrees().getElement(new TreePath(casePath, statement)));
                }
            }
        }
        ExpressionTree et = null;
        switch (switchType) {
            case SWITCH_EXPRESSION:
                et = (ExpressionTree) make.SwitchExpression(switchExpr, newCases);
                if (typeCastTree != null) {
                    et = make.Parenthesized(et);
                    et = make.TypeCast(typeCastTree, et);
                }
                if (leftVariable != null) {
                    wc.rewrite(st, make.ExpressionStatement((ExpressionTree) make.Assignment((ExpressionTree) leftVariable, et)));
                } else {
                    wc.rewrite(st, make.Return(et));
                }
                break;
            case RULE_SWITCH:
                wc.rewrite(st, make.SwitchExpression(switchExpr, newCases));
                break;
            case TRADITIONAL_SWITCH:
                wc.rewrite((SwitchTree) st, make.Switch(switchExpr, newCases));
                break;
            default:
                break;
        }
    }

    private static List<? extends StatementTree> getSwitchStatement(CaseTree ct) {
        if (ct.getStatements() != null) {
            return ct.getStatements();
        } else if (ct instanceof JCTree.JCCase) {
            return ((JCTree.JCCase) ct).stats;
        } else {
            return null;
        }
   }
    
    private static Name getLeftTreeName(StatementTree statement) {
        if (statement.getKind() != Kind.EXPRESSION_STATEMENT) {
            return null;
        }
        JCTree.JCExpressionStatement jceTree = (JCTree.JCExpressionStatement) statement;
        if (jceTree.expr.getKind() != Kind.ASSIGNMENT) {
            return null;
        }
        JCTree.JCAssign assignTree = (JCTree.JCAssign) jceTree.expr;
        return ((JCTree.JCIdent) assignTree.lhs).name;
    }

    public static boolean isJDKVersionLower(int previewUntilJDK){
        if(Integer.valueOf(SourceVersion.latest().name().split(UNDERSCORE)[1]).compareTo(previewUntilJDK)<=0)
            return true;

        return false;
    }
}
