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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;

import java.awt.Color;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyleUtils;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeUtilities.TypeNameOptions;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.base.javadoc.JavadocImports;
import org.netbeans.modules.java.editor.options.CodeCompletionPanel;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.util.WeakListeners;

/**
 *
 * @author Dusan Balek
 * @author Sam Halliday
 */
public final class Utilities {
    
    private static final String ERROR = "<error>"; //NOI18N
    private static final Pattern LINK_PATTERN = Pattern.compile("<a href='(\\*\\d+)'>(.*?)<\\/a>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static boolean guessMethodArguments = CodeCompletionPanel.GUESS_METHOD_ARGUMENTS_DEFAULT;
    private static boolean autoPopupOnJavaIdentifierPart = CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART_DEFAULT;
    private static String javaCompletionAutoPopupTriggers = CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS_DEFAULT;
    private static String javaCompletionSelectors = CodeCompletionPanel.JAVA_COMPLETION_SELECTORS_DEFAULT;
    private static String javadocCompletionAutoPopupTriggers = CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS_DEFAULT;
    private static String javadocCompletionSelectors = CodeCompletionPanel.JAVADOC_COMPLETION_SELECTORS_DEFAULT;
    private static boolean popupParameterToolip = true;
    
    private static final AtomicBoolean inited = new AtomicBoolean(false);
    private static Preferences preferences;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || CodeCompletionPanel.GUESS_METHOD_ARGUMENTS.equals(settingName)) {
                guessMethodArguments = preferences.getBoolean(CodeCompletionPanel.GUESS_METHOD_ARGUMENTS, CodeCompletionPanel.GUESS_METHOD_ARGUMENTS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART.equals(settingName)) {
                autoPopupOnJavaIdentifierPart = preferences.getBoolean(CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART, CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS.equals(settingName)) {
                javaCompletionAutoPopupTriggers = preferences.get(CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS, CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_COMPLETION_SELECTORS.equals(settingName)) {
                javaCompletionSelectors = preferences.get(CodeCompletionPanel.JAVA_COMPLETION_SELECTORS, CodeCompletionPanel.JAVA_COMPLETION_SELECTORS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS.equals(settingName)) {
                javadocCompletionAutoPopupTriggers = preferences.get(CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS, CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVADOC_COMPLETION_SELECTORS.equals(settingName)) {
                javadocCompletionSelectors = preferences.get(CodeCompletionPanel.JAVADOC_COMPLETION_SELECTORS, CodeCompletionPanel.JAVADOC_COMPLETION_SELECTORS_DEFAULT);
            }
            if (settingName == null || SimpleValueNames.COMPLETION_PARAMETER_TOOLTIP.equals(settingName)) {
				popupParameterToolip = preferences.getBoolean(SimpleValueNames.COMPLETION_PARAMETER_TOOLTIP, true);
            }
        }
    };
    
    public static boolean guessMethodArguments() {
        lazyInit();
        return guessMethodArguments;
    }

    public static boolean autoPopupOnJavaIdentifierPart() {
        lazyInit();
        return autoPopupOnJavaIdentifierPart;
    }

    public static String getJavaCompletionAutoPopupTriggers() {
        lazyInit();
        return javaCompletionAutoPopupTriggers;
    }

    public static String getJavaCompletionSelectors() {
        lazyInit();
        return javaCompletionSelectors;
    }

    public static String getJavadocCompletionAutoPopupTriggers() {
        lazyInit();
        return javadocCompletionAutoPopupTriggers;
    }

    public static String getJavadocCompletionSelectors() {
        lazyInit();
        return javadocCompletionSelectors;
    }

    public static boolean popupPrameterTooltip() {
        lazyInit();
        return popupParameterToolip;
    }

    private static void lazyInit() {
        if (inited.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }

    public static int getImportanceLevel(CompilationInfo info, ReferencesCount referencesCount, @NonNull Element element) {
        boolean isType = element.getKind().isClass() || element.getKind().isInterface();
        
        return getImportanceLevel(referencesCount, isType ? ElementHandle.create((TypeElement) element) : ElementHandle.create((TypeElement) element.getEnclosingElement()));
    }
    
    public static int getImportanceLevel(ReferencesCount referencesCount, ElementHandle<TypeElement> handle) {
        int typeRefCount = 999 - Math.min(referencesCount.getTypeReferenceCount(handle), 999);
        int pkgRefCount = 999;
        String binaryName = SourceUtils.getJVMSignature(handle)[0];
        int idx = binaryName.lastIndexOf('.');
        if (idx > 0) {
            ElementHandle<PackageElement> pkgElement = ElementHandle.createPackageElementHandle(binaryName.substring(0, idx));
            pkgRefCount -= Math.min(referencesCount.getPackageReferenceCount(pkgElement), 999);
        }
        return typeRefCount * 100000 + pkgRefCount * 100 + getImportanceLevel(binaryName);
    }
    
    public static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) { // NOI18N
            weight -= 10;
        } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) { // NOI18N
            weight += 10;
        } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) { // NOI18N
            weight += 20;
        } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) { // NOI18N
            weight += 30;
        }
        return weight;
    }
    
    public static String getHTMLColor(int r, int g, int b) {
        Color c = LFCustoms.shiftColor(new Color(r, g, b));
        return "<font color=#" //NOI18N
                + LFCustoms.getHexString(c.getRed())
                + LFCustoms.getHexString(c.getGreen())
                + LFCustoms.getHexString(c.getBlue())
                + ">"; //NOI18N
    }
    
    public static boolean hasAccessibleInnerClassConstructor(Element e, Scope scope, Trees trees) {
        DeclaredType dt = (DeclaredType)e.asType();
        for (TypeElement inner : ElementFilter.typesIn(e.getEnclosedElements())) {
            if (trees.isAccessible(scope, inner, dt)) {
                DeclaredType innerType = (DeclaredType)inner.asType();
                for (ExecutableElement ctor : ElementFilter.constructorsIn(inner.getEnclosedElements())) {
                    if (trees.isAccessible(scope, ctor, innerType))
                        return true;
                }
            }
        }
        return false;
    }
        
    public static TreePath getPathElementOfKind(Tree.Kind kind, TreePath path) {
        return getPathElementOfKind(EnumSet.of(kind), path);
    }
    
    public static TreePath getPathElementOfKind(Set<Tree.Kind> kinds, TreePath path) {
        while (path != null) {
            if (kinds.contains(path.getLeaf().getKind()))
                return path;
            path = path.getParentPath();
        }
        return null;        
    }        
    
    public static boolean isJavaContext(final JTextComponent component, final int offset, final boolean allowInStrings) {
        return isJavaContext(component.getDocument(), offset, allowInStrings);
    }

    public static boolean isJavaContext(final Document doc, final int offset, final boolean allowInStrings) {
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
        if (doc.getLength() == 0 && "text/x-dialog-binding".equals(doc.getProperty("mimeType"))) { //NOI18N
            InputAttributes attributes = (InputAttributes) doc.getProperty(InputAttributes.class);
            LanguagePath path = LanguagePath.get(MimeLookup.getLookup("text/x-dialog-binding").lookup(Language.class)); //NOI18N
            Document d = (Document) attributes.getValue(path, "dialogBinding.document"); //NOI18N
            if (d != null) {
                return "text/x-java".equals(NbEditorUtilities.getMimeType(d)); //NOI18N
            }
            FileObject fo = (FileObject)attributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
            return "text/x-java".equals(fo.getMIMEType()); //NOI18N
        }
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset);
        if (ts == null) {
            return false;
        }        
        if (!ts.moveNext() && !ts.movePrevious()) {
            return true;
        }
        if (offset == ts.offset()) {
            return true;
        }
        switch(ts.token().id()) {
            case DOUBLE_LITERAL:
            case FLOAT_LITERAL:
            case FLOAT_LITERAL_INVALID:
            case LONG_LITERAL:
                if (ts.token().text().charAt(0) == '.') {
                    break;
                }
            case CHAR_LITERAL:
            case INT_LITERAL:
            case INVALID_COMMENT_END:
            case JAVADOC_COMMENT:
            case LINE_COMMENT:
            case JAVADOC_COMMENT_LINE_RUN:
            case BLOCK_COMMENT:
                return false;
            case STRING_LITERAL:
                return allowInStrings;
        }
        return true;
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
            }
        }
    }
    
    public static CharSequence getTypeName(CompilationInfo info, TypeMirror type, boolean fqn) {
        return getTypeName(info, type, fqn, false);
    }

    public static CharSequence getTypeName(CompilationInfo info, TypeMirror type, boolean fqn, boolean varArg) {
        Set<TypeNameOptions> options = EnumSet.noneOf(TypeNameOptions.class);
        if (fqn) {
            options.add(TypeNameOptions.PRINT_FQN);
        }
        if (varArg) {
            options.add(TypeNameOptions.PRINT_AS_VARARG);
        }
        return info.getTypeUtilities().getTypeName(type, options.toArray(new TypeNameOptions[0]));
    }
        
    public static CharSequence getElementName(Element el, boolean fqn) {
        if (el == null || el.asType().getKind() == TypeKind.NONE)
            return ""; //NOI18N
        return new ElementNameVisitor().visit(el, fqn);
    }
    
    public static Collection<? extends Element> getForwardReferences(TreePath path, int pos, SourcePositions sourcePositions, Trees trees) {
        HashSet<Element> refs = new HashSet<Element>();
        while(path != null) {
            switch(path.getLeaf().getKind()) {
                case BLOCK:
                    if (path.getParentPath().getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION)
                        break;
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    return refs;
                case VARIABLE:
                    refs.add(trees.getElement(path));
                    TreePath parent = path.getParentPath();
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getLeaf().getKind())) {
                        boolean isStatic = ((VariableTree)path.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC);
                        for(Tree member : ((ClassTree)parent.getLeaf()).getMembers()) {
                            if (member.getKind() == Tree.Kind.VARIABLE && sourcePositions.getStartPosition(path.getCompilationUnit(), member) >= pos &&
                                    (isStatic || !((VariableTree)member).getModifiers().getFlags().contains(Modifier.STATIC)))
                                refs.add(trees.getElement(new TreePath(parent, member)));
                        }
                    }
                    return refs;
                case ENHANCED_FOR_LOOP:
                    EnhancedForLoopTree efl = (EnhancedForLoopTree)path.getLeaf();
                    if (sourcePositions.getEndPosition(path.getCompilationUnit(), efl.getExpression()) >= pos)
                        refs.add(trees.getElement(new TreePath(path, efl.getVariable())));                        
            }
            path = path.getParentPath();
        }
        return refs;
    }
    
    public static List<String> varNamesSuggestions(TypeMirror type, ElementKind kind, Set<Modifier> modifiers, String suggestedName, String prefix, Types types, Elements elements, Iterable<? extends Element> locals, CodeStyle codeStyle) {
        List<String> result = new ArrayList<String>();
        if (type == null && suggestedName == null)
            return result;
        Collection<String> vnct;
        if (suggestedName != null) {
            vnct = new LinkedHashSet<>();
            vnct.add(suggestedName);
            if (type != null) {
                vnct.addAll(varNamesForType(type, types, elements, prefix));
            }
        } else {
            vnct = varNamesForType(type, types, elements, prefix);
        }
        boolean isConst = false;
        String namePrefix = null;
        String nameSuffix = null;
        switch (kind) {
            case FIELD:
                if (modifiers.contains(Modifier.STATIC)) {
                    if (codeStyle != null) {
                        namePrefix = codeStyle.getStaticFieldNamePrefix();
                        nameSuffix = codeStyle.getStaticFieldNameSuffix();
                    }
                    isConst = modifiers.contains(Modifier.FINAL);
                } else {
                    if (codeStyle != null) {
                        namePrefix = codeStyle.getFieldNamePrefix();
                        nameSuffix = codeStyle.getFieldNameSuffix();
                    }
                }
                break;
            case LOCAL_VARIABLE:
            case EXCEPTION_PARAMETER:
            case RESOURCE_VARIABLE:
                if (codeStyle != null) {
                    namePrefix = codeStyle.getLocalVarNamePrefix();
                    nameSuffix = codeStyle.getLocalVarNameSuffix();
                }
                break;
            case PARAMETER:
                if (codeStyle != null) {
                    namePrefix = codeStyle.getParameterNamePrefix();
                    nameSuffix = codeStyle.getParameterNameSuffix();
                }
                break;
        }
        if (isConst) {
            List<String> ls = new ArrayList<String>(vnct.size());
            for (String s : vnct)
                ls.add(getConstName(s));
            vnct = ls;
        }
        if (vnct.isEmpty() && prefix != null && prefix.length() > 0
                && (namePrefix != null && namePrefix.length() > 0
                || nameSuffix != null && nameSuffix.length() >0)) {
            vnct = Collections.singletonList(prefix);
        }
        String p = prefix;
        while (p != null && p.length() > 0) {
            List<String> l = new ArrayList<String>();
            for(String name : vnct)
                if (org.netbeans.modules.java.completion.Utilities.startsWith(name, p))
                    l.add(name);
            if (l.isEmpty()) {
                p = nextName(p);
            } else {
                vnct = l;
                prefix = prefix.substring(0, prefix.length() - p.length());
                p = null;
            }
        }
        for (String name : vnct) {
            boolean isPrimitive = type != null && type.getKind().isPrimitive();
            if (prefix != null && prefix.length() > 0) {
                if (isConst) {
                    name = prefix.toUpperCase(Locale.ENGLISH) + '_' + name;
                } else {
                    name = prefix + name.toUpperCase(Locale.ENGLISH).charAt(0) + name.substring(1);
                }
            }
            int cnt = 1;
            String baseName = name;
            name = CodeStyleUtils.addPrefixSuffix(name, namePrefix, nameSuffix);
            while (isClashing(name, type, locals)) {
                if (isPrimitive) {
                    char c = name.charAt(namePrefix != null ? namePrefix.length() : 0);
                    name = CodeStyleUtils.addPrefixSuffix(Character.toString(++c), namePrefix, nameSuffix);
                    if (c == 'z' || c == 'Z') //NOI18N
                        isPrimitive = false;
                } else {
                    name = CodeStyleUtils.addPrefixSuffix(baseName + cnt++, namePrefix, nameSuffix);
                }
            }
            result.add(name);
        }
        return result;
    }

    public static String varNameSuggestion(TreePath path) {
        return adjustName(varNameForPath(path));   
    }

    public static String varNameSuggestion(Tree tree) {
        return adjustName(varNameForTree(tree));   
    }

    public static boolean inAnonymousOrLocalClass(TreePath path) {
        if (path == null) {
            return false;
        }
        TreePath parentPath = path.getParentPath();
        if ((TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind()) || path.getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION) && 
            parentPath.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT && !TreeUtilities.CLASS_TREE_KINDS.contains(parentPath.getLeaf().getKind())) {
            return true;
        }
        return inAnonymousOrLocalClass(parentPath);
    }

    public static boolean isBoolean(TypeMirror type) {
        return type.getKind() == TypeKind.BOOLEAN;
    }

    public static Set<Element> getUsedElements(final CompilationInfo info) {
        final Set<Element> ret = new HashSet<>();
        final Trees trees = info.getTrees();
        new ErrorAwareTreePathScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                addElement(trees.getElement(getCurrentPath()));
                return null;
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath())) {
                    addElement(element);
                }
                return super.visitClass(node, p);
            }

            @Override
            public Void visitMethod(MethodTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath())) {
                    addElement(element);
                }
                return super.visitMethod(node, p);
            }

            @Override
            public Void visitVariable(VariableTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath())) {
                    addElement(element);
                }
                return super.visitVariable(node, p);
            }

            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                scan(node.getPackageAnnotations(), p);
                return scan(node.getTypeDecls(), p);
            }
            
            private void addElement(Element element) {
                if (element != null) {
                    ret.add(element);
                }
            }
        }.scan(info.getCompilationUnit(), null);
        return ret;                
    }
    
    public static boolean containErrors(Tree tree) {
        final AtomicBoolean containsErrors = new AtomicBoolean();
        new ErrorAwareTreeScanner<Void, Void>() {
            @Override
            public Void visitErroneous(ErroneousTree node, Void p) {
                containsErrors.set(true);
                return null;
            }
            
            @Override
            public Void scan(Tree node, Void p) {
                if (containsErrors.get()) {
                    return null;
                }
                return super.scan(node, p);
            }
        }.scan(tree, null);
        return containsErrors.get();
    }

    private static List<String> varNamesForType(TypeMirror type, Types types, Elements elements, String prefix) {
        switch (type.getKind()) {
            case ARRAY:
                TypeElement iterableTE = elements.getTypeElement("java.lang.Iterable"); //NOI18N
                TypeMirror iterable = iterableTE != null ? types.getDeclaredType(iterableTE) : null;
                TypeMirror ct = ((ArrayType)type).getComponentType();
                if (ct.getKind() == TypeKind.ARRAY && iterable != null && types.isSubtype(ct, iterable))
                    return varNamesForType(ct, types, elements, prefix);
                List<String> vnct = new ArrayList<String>();
                for (String name : varNamesForType(ct, types, elements, prefix))
                    vnct.add(name.endsWith("s") ? name + "es" : name + "s"); //NOI18N
                return vnct;
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                String str = type.toString().substring(0, 1);
                return prefix != null && !prefix.equals(str)
                        ? Collections.<String>emptyList()
                        : Collections.<String>singletonList(str);
            case TYPEVAR:
                return Collections.<String>singletonList(type.toString().toLowerCase(Locale.ENGLISH));
            case ERROR:
                String tn = ((ErrorType)type).asElement().getSimpleName().toString();
                if (tn.toUpperCase(Locale.ENGLISH).contentEquals(tn))
                    return Collections.<String>singletonList(tn.toLowerCase(Locale.ENGLISH));
                StringBuilder sb = new StringBuilder();
                ArrayList<String> al = new ArrayList<String>();
                if ("Iterator".equals(tn)) //NOI18N
                    al.add("it"); //NOI18N
                while((tn = nextName(tn)).length() > 0) {
                    if (SourceVersion.isKeyword(tn)) {
                        al.add('a' + tn.substring(0, 1).toUpperCase() + tn.substring(1));
                    } else {
                        al.add(tn);
                    }
                    sb.append(tn.charAt(0));
                }
                if (sb.length() > 0) {
                    String s = sb.toString();
                    if (prefix == null || prefix.length() == 0 || s.startsWith(prefix))
                        al.add(s);
                }
                return al;
            case DECLARED:
                iterableTE = elements.getTypeElement("java.lang.Iterable"); //NOI18N
                iterable = iterableTE != null ? types.getDeclaredType(iterableTE) : null;
                tn = ((DeclaredType)type).asElement().getSimpleName().toString();
                if (tn.toUpperCase(Locale.ENGLISH).contentEquals(tn))
                    return Collections.<String>singletonList(tn.toLowerCase(Locale.ENGLISH));
                sb = new StringBuilder();
                al = new ArrayList<String>();
                if ("Iterator".equals(tn)) //NOI18N
                    al.add("it"); //NOI18N
                while((tn = nextName(tn)).length() > 0) {
                    if (SourceVersion.isKeyword(tn)) {
                        al.add('a' + tn.substring(0, 1).toUpperCase() + tn.substring(1));
                    } else {
                        al.add(tn);
                    }
                    sb.append(tn.charAt(0));
                }
                if (iterable != null && types.isSubtype(type, iterable)) {
                    List<? extends TypeMirror> tas = ((DeclaredType)type).getTypeArguments();
                    if (tas.size() > 0) {
                        TypeMirror et = tas.get(0);
                        if (et.getKind() == TypeKind.ARRAY || (et.getKind() != TypeKind.WILDCARD && types.isSubtype(et, iterable))) {
                            al.addAll(varNamesForType(et, types, elements, prefix));
                        } else {
                            for (String name : varNamesForType(et, types, elements, prefix))
                                al.add(name.endsWith("s") ? name + "es" : name + "s"); //NOI18N
                        }
                    }
                }
                if (sb.length() > 0) {
                    String s = sb.toString();
                    if (prefix == null || prefix.length() == 0 || s.startsWith(prefix))
                        al.add(s);
                }
                return al;
            case WILDCARD:
                TypeMirror bound = ((WildcardType)type).getExtendsBound();
                if (bound == null)
                    bound = ((WildcardType)type).getSuperBound();
                if (bound != null)
                    return varNamesForType(bound, types, elements, prefix);
        }
        return Collections.<String>emptyList();
    }
    
    private static String getConstName(String s) {
        StringBuilder sb = new StringBuilder();
        boolean prevUpper = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                if (!prevUpper)
                    sb.append('_');
                sb.append(c);
                prevUpper = true;
            } else {
                sb.append(Character.toUpperCase(c));
                prevUpper = false;
            }
        }
        return sb.toString();
    }
    
    private static String nextName(CharSequence name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                char lc = Character.toLowerCase(c);
                sb.append(lc);
                sb.append(name.subSequence(i + 1, name.length()));
                break;
            }
        }
        return sb.toString();
    }
    
    private static boolean isClashing(String varName, TypeMirror type, Iterable<? extends Element> locals) {
        if (SourceVersion.isKeyword(varName))
            return true;
        if (type != null && type.getKind() == TypeKind.DECLARED && ((DeclaredType)type).asElement().getSimpleName().contentEquals(varName))
            return true;
        for (Element e : locals) {
            if ((e.getKind().isField() || e.getKind() == ElementKind.LOCAL_VARIABLE || e.getKind() == ElementKind.RESOURCE_VARIABLE
                    || e.getKind() == ElementKind.PARAMETER || e.getKind() == ElementKind.EXCEPTION_PARAMETER) && varName.contentEquals(e.getSimpleName()))
                return true;
        }
        return false;
    }
    
    private static String varNameForPath(TreePath path) {
        if (path == null)
            return null;
        Tree tree = path.getLeaf();
        if (tree.getKind() == Kind.VARIABLE) {
            if (((VariableTree)tree).getInitializer() != null) {
                String name = varNameForTree(((VariableTree)tree).getInitializer());
                if (name != null) {
                    return name;
                }
            }
            if (path.getParentPath().getLeaf().getKind() == Kind.ENHANCED_FOR_LOOP
                    && ((EnhancedForLoopTree)path.getParentPath().getLeaf()).getVariable() == tree) {
                String name = varNameForTree(((EnhancedForLoopTree)path.getParentPath().getLeaf()).getExpression());
                if (name != null) {
                    String singular = getSingular(name);
                    if (singular != null) {
                        return singular;
                    }
                }
            }
            return null;
        }
        return varNameForTree(path.getLeaf());
    }

    private static String varNameForTree(Tree et) {
        if (et == null)
            return null;
        switch (et.getKind()) {
            case IDENTIFIER:
                return ((IdentifierTree) et).getName().toString();
            case MEMBER_SELECT:
                return ((MemberSelectTree) et).getIdentifier().toString();
            case METHOD_INVOCATION:
                return varNameForTree(((MethodInvocationTree) et).getMethodSelect());
            case NEW_CLASS:
                return firstToLower(varNameForTree(((NewClassTree) et).getIdentifier()));
            case PARAMETERIZED_TYPE:
                return firstToLower(varNameForTree(((ParameterizedTypeTree) et).getType()));
            case STRING_LITERAL:
                String name = guessLiteralName((String) ((LiteralTree) et).getValue());
                if (name == null) {
                    return firstToLower(String.class.getSimpleName());
                } else {
                    return firstToLower(name);
                }
            case VARIABLE:
                return ((VariableTree) et).getName().toString();
            case ARRAY_ACCESS:
                name = varNameForTree(((ArrayAccessTree)et).getExpression());
                if (name != null) {
                    String singular = getSingular(name);
                    if (singular != null) {
                        return singular;
                    }
                }
                return null;
            case ASSIGNMENT:
                if (((AssignmentTree)et).getExpression() != null) {
                    return varNameForTree(((AssignmentTree)et).getExpression());
                }
                return null;
            default:
                return null;
        }
    }
    
    private static String getSingular(String name) {
        if (name.endsWith("ies") && name.length() > 3) { //NOI18N
            return name.substring(0, name.length() - 3) + 'y';
        }
        if (name.endsWith("s") && name.length() > 1) { //NOI18N
            return name.substring(0, name.length() - 1);
        }
        return null;
    }
    
    static String adjustName(String name) {
        if (name == null || ERROR.contentEquals(name))
            return null;
        
        String shortName = null;
        
        if (name.startsWith("get") && name.length() > 3) {
            shortName = name.substring(3);
        }
        
        if (name.startsWith("is") && name.length() > 2) {
            shortName = name.substring(2);
        }
        
        if (shortName != null) {
            return firstToLower(shortName);
        }
        
        if (SourceVersion.isKeyword(name)) {
            return "a" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else {
            return name;
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

    private static String guessLiteralName(String str) {
        if(str.isEmpty())
            return null;
        
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if(ch == ' ') {
                sb.append('_');
            } else if (sb.length() == 0 ? Character.isJavaIdentifierStart(ch) : Character.isJavaIdentifierPart(ch))
                sb.append(ch);
            if (sb.length() > 40)
                break;
        }
        if (sb.length() == 0)
            return null;
        else
            return sb.toString();
    }
    
    private static class ElementNameVisitor extends SimpleElementVisitor8<StringBuilder,Boolean> {
        
        private ElementNameVisitor() {
            super(new StringBuilder());
        }

        @Override
        public StringBuilder visitPackage(PackageElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }

	@Override
        public StringBuilder visitType(TypeElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }        
    }
    
    public static TypeMirror resolveCapturedType(CompilationInfo info, TypeMirror tm) {
        TypeMirror type = resolveCapturedTypeInt(info, tm);
        
        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
            tmirr = tmirr != null ? tmirr : ((WildcardType) type).getSuperBound();
            if (tmirr != null)
                return tmirr;
            else { //no extends, just '?'
                return info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
            }
                
        }
        
        return type;
    }
    
    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
        if (tm == null) return tm;
        
        TypeMirror orig = SourceUtils.resolveCapturedType(tm);

        if (orig != null) {
            tm = orig;
        }
        
        if (tm.getKind() == TypeKind.WILDCARD) {
            TypeMirror extendsBound = ((WildcardType) tm).getExtendsBound();
            TypeMirror rct = resolveCapturedTypeInt(info, extendsBound != null ? extendsBound : ((WildcardType) tm).getSuperBound());
            if (rct != null) {
                return rct.getKind() == TypeKind.WILDCARD ? rct : info.getTypes().getWildcardType(extendsBound != null ? rct : null, extendsBound == null ? rct : null);
            }
        }
        
        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) tm;
            List<TypeMirror> typeArguments = new LinkedList<TypeMirror>();
            
            for (TypeMirror t : dt.getTypeArguments()) {
                typeArguments.add(resolveCapturedTypeInt(info, t));
            }
            
            final TypeMirror enclosingType = dt.getEnclosingType();
            if (enclosingType.getKind() == TypeKind.DECLARED) {
                return info.getTypes().getDeclaredType((DeclaredType) enclosingType, (TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            } else {
                return info.getTypes().getDeclaredType((TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            }
        }

        if (tm.getKind() == TypeKind.ARRAY) {
            ArrayType at = (ArrayType) tm;

            return info.getTypes().getArrayType(resolveCapturedTypeInt(info, at.getComponentType()));
        }
        
        return tm;
    }
        
    /**
     * @since 2.12
     */
    public static @NonNull List<ExecutableElement> fuzzyResolveMethodInvocation(CompilationInfo info, TreePath path, List<TypeMirror> proposed, int[] index) {
        assert path.getLeaf().getKind() == Kind.METHOD_INVOCATION || path.getLeaf().getKind() == Kind.NEW_CLASS;
        
        if (path.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
            List<TypeMirror> actualTypes = new LinkedList<>();
            MethodInvocationTree mit = (MethodInvocationTree) path.getLeaf();

            for (Tree a : mit.getArguments()) {
                TreePath tp = new TreePath(path, a);
                actualTypes.add(info.getTrees().getTypeMirror(tp));
            }

            String methodName;
            List<Pair<TypeMirror, Boolean>> on = new ArrayList<>();

            switch (mit.getMethodSelect().getKind()) {
                case IDENTIFIER:
                    methodName = ((IdentifierTree) mit.getMethodSelect()).getName().toString();
                    Scope s = info.getTrees().getScope(path);
                    TypeElement enclosingClass = s.getEnclosingClass();
                    while (enclosingClass != null) {
                        on.add(Pair.of(enclosingClass.asType(), false));
                        enclosingClass = info.getElementUtilities().enclosingTypeElement(enclosingClass);
                    }
                    CompilationUnitTree cut = info.getCompilationUnit();
                    for (ImportTree imp : cut.getImports()) {
                        if (!imp.isStatic() || imp.getQualifiedIdentifier() == null || imp.getQualifiedIdentifier().getKind() != Kind.MEMBER_SELECT) {
                            continue;
                        }
                        Name selected = ((MemberSelectTree) imp.getQualifiedIdentifier()).getIdentifier();
                        if (!selected.contentEquals("*") && !selected.contentEquals(methodName)) {
                            continue;
                        }
                        TreePath tp = new TreePath(new TreePath(new TreePath(new TreePath(cut), imp), imp.getQualifiedIdentifier()), ((MemberSelectTree) imp.getQualifiedIdentifier()).getExpression());
                        Element el = info.getTrees().getElement(tp);
                        if (el != null) {
                            on.add(Pair.of(el.asType(), true));
                        }
                    }
                    break;
                case MEMBER_SELECT:
                    on.add(Pair.of(info.getTrees().getTypeMirror(new TreePath(path, ((MemberSelectTree) mit.getMethodSelect()).getExpression())), false));
                    methodName = ((MemberSelectTree) mit.getMethodSelect()).getIdentifier().toString();
                    break;
                default:
                    throw new IllegalStateException();
            }

            List<ExecutableElement> result = new ArrayList<>();
            
            for (Pair<TypeMirror, Boolean> type : on) {
                if (type.first() == null || type.first().getKind() != TypeKind.DECLARED) {
                    continue;
                }
                result.addAll(resolveMethod(info, actualTypes, (DeclaredType) type.first(), type.second(), false, methodName, proposed, index));
            }
            
            return result;
        }
        
        if (path.getLeaf().getKind() == Kind.NEW_CLASS) {
            List<TypeMirror> actualTypes = new LinkedList<>();
            NewClassTree nct = (NewClassTree) path.getLeaf();

            for (Tree a : nct.getArguments()) {
                TreePath tp = new TreePath(path, a);
                actualTypes.add(info.getTrees().getTypeMirror(tp));
            }

            TypeMirror on = info.getTrees().getTypeMirror(new TreePath(path, nct.getIdentifier()));
            
            if (on == null || on.getKind() != TypeKind.DECLARED) {
                return Collections.emptyList();
            }
            
            return resolveMethod(info, actualTypes, (DeclaredType) on, false, true, null, proposed, index);
        }
        
        return Collections.emptyList();
    }

    private static Iterable<ExecutableElement> execsIn(CompilationInfo info, TypeElement e, boolean constr, String name) {
        if (constr) {
            return ElementFilter.constructorsIn(info.getElements().getAllMembers(e));
        }
        
        List<ExecutableElement> result = new LinkedList<>();
        
        for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(e))) {
            if (name.equals(ee.getSimpleName().toString())) {
                result.add(ee);
            }
        }
        
        return result;
    }
    
    private static List<ExecutableElement> resolveMethod(CompilationInfo info, List<TypeMirror> foundTypes, DeclaredType on, boolean onlyStatic, boolean constr, String name, List<TypeMirror> candidateTypes, int[] index) {
        if (on.asElement() == null) {
            return Collections.emptyList();
        }
        
        List<ExecutableElement> found = new LinkedList<>();
        
        OUTER:
        for (ExecutableElement ee : execsIn(info, (TypeElement) on.asElement(), constr, name)) {
            TypeMirror currType = ((TypeElement) ee.getEnclosingElement()).asType();
            if (!info.getTypes().isSubtype(on, currType) && !on.asElement().equals(((DeclaredType)currType).asElement())) { //XXX: fix for #132627, a clearer fix may exist
                continue;
            }
            if (onlyStatic && !ee.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            if (ee.getParameters().size() == foundTypes.size() /*XXX: variable arg count*/) {
                TypeMirror innerCandidate = null;
                int innerIndex = -1;
                ExecutableType et = (ExecutableType) info.getTypes().asMemberOf(on, ee);
                Iterator<? extends TypeMirror> formal = et.getParameterTypes().iterator();
                Iterator<? extends TypeMirror> actual = foundTypes.iterator();
                boolean mismatchFound = false;
                int i = 0;

                while (formal.hasNext() && actual.hasNext()) {
                    TypeMirror currentFormal = formal.next();
                    TypeMirror currentActual = actual.next();

                    if (!info.getTypes().isAssignable(currentActual, currentFormal) || currentActual.getKind() == TypeKind.ERROR) {
                        if (mismatchFound) {
                            //only one mismatch supported:
                            continue OUTER;
                        }
                        mismatchFound = true;
                        innerCandidate = currentFormal;
                        innerIndex = i;
                    }

                    i++;
                }

                if (mismatchFound) {
                    if (candidateTypes.isEmpty()) {
                        index[0] = innerIndex;
                        candidateTypes.add(innerCandidate);
                        found.add(ee);
                    } else {
                        //see testFuzzyResolveConstructor2:
                        if (index[0] == innerIndex) {
                            boolean add = true;
                            for (TypeMirror tm : candidateTypes) {
                                if (info.getTypes().isSameType(tm, innerCandidate)) {
                                    add = false;
                                    break;
                                }
                            }
                            if (add) {
                                candidateTypes.add(innerCandidate);
                                found.add(ee);
                            }
                        }
                    }
                }
            }
        }

        return found;
    }

    static String resolveLinks(String content, ElementJavadoc doc) {
        Matcher matcher = LINK_PATTERN.matcher(content);
        String updatedContent = matcher.replaceAll(result -> {
            if (result.groupCount() == 2) {
                try {
                    ElementJavadoc link = doc.resolveLink(result.group(1));
                    URL url = link != null ? link.getURL() : null;
                    if (url != null) {
                        return "<a href='" + url.toString() + "'>" + result.group(2) + "</a>";
                    }
                } catch (Exception ex) {}
                return result.group(2);
            }
            return result.group();
        });
        return updatedContent;
    }

    private Utilities() {
    }
}
