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
package org.netbeans.modules.java.editor.base.semantic;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
//import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.base.imports.UnusedImports;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;


/**
 *
 * @author Jan Lahoda
 */
public abstract class SemanticHighlighterBase extends JavaParserResultTask {
    
    private AtomicBoolean cancel = new AtomicBoolean();
    
    protected SemanticHighlighterBase() {
        super(Phase.RESOLVED, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        CompilationInfo info = CompilationInfo.get(result);
        
        if (info == null) {
            return ;
        }
        
        cancel.set(false);
        
        final Document doc = result.getSnapshot().getSource().getDocument(false);
        
        if (!verifyDocument(doc)) return;

        process(info, doc);
    }

    private static boolean verifyDocument(final Document doc) {
        if (doc == null) {
            Logger.getLogger(SemanticHighlighterBase.class.getName()).log(Level.FINE, "SemanticHighlighter: Cannot get document!");
            return false;
        }

        final boolean[] tokenSequenceNull =  new boolean[1];
        doc.render(new Runnable() {
            @Override
            public void run() {
                tokenSequenceNull[0] = (TokenHierarchy.get(doc).tokenSequence() == null);
            }
        });
        return !tokenSequenceNull[0];
    }
    
    @Override
    public void cancel() {
        cancel.set(true);
    }
    

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }
        
    protected abstract boolean process(CompilationInfo info, final Document doc);
    
    /**
     * Signatures of Serializable methods.
     */
    private static final Set<String> SERIALIZABLE_SIGNATURES = new HashSet<>(Arrays.asList(new String[] {
        "writeObject(Ljava/io/ObjectOutputStream;)V",
        "readObject(Ljava/io/ObjectInputStream;)V",
        "readResolve()Ljava/lang/Object;",
        "writeReplace()Ljava/lang/Object;",
        "readObjectNoData()V",
    }));
    
    /**
     * Also returns true on error / undecidable situation, so the filtering 
     * will probably accept serial methods and will not mark them as unused, if
     * the class declaration is errneous.
     * 
     * @param info the compilation context
     * @param e the class member (the enclosing element will be tested)
     * @return true, if in serializable/externalizable or unknown
     */
    private static boolean isInSerializableOrExternalizable(CompilationInfo info, Element e) {
        Element encl = e.getEnclosingElement();
        if (encl == null || !encl.getKind().isClass()) {
            return true;
        }
        TypeMirror m = encl.asType();
        if (m == null || m.getKind() != TypeKind.DECLARED) {
            return true;
        }
        Element serEl = info.getElements().getTypeElement("java.io.Serializable"); // NOI18N
        Element extEl = info.getElements().getTypeElement("java.io.Externalizable"); // NOI18N
        if (serEl == null || extEl == null) {
            return true;
        }
        if (info.getTypes().isSubtype(m, serEl.asType())) {
            return true;
        }
        if (info.getTypes().isSubtype(m, extEl.asType())) {
            return true;
        }
        return false;
    }
    
    private static Field signatureAccessField;
    
    /**
     * Hack to get signature out of ElementHandle - there's no API method for that
     */
    private static String _getSignatureHack(ElementHandle<ExecutableElement> eh) {
        try {
            if (signatureAccessField == null) {
                try {
                    Field f = ElementHandle.class.getDeclaredField("signatures"); // NOI18N
                    f.setAccessible(true);
                    signatureAccessField = f;
                } catch (NoSuchFieldException | SecurityException ex) {
                    // ignore
                    return ""; // NOI18N
                }
            }
            String[] signs = (String[])signatureAccessField.get(eh);
            if (signs == null || signs.length != 3) {
                return ""; // NOI18N
            } else {
                return signs[1] + signs[2];
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return ""; // NOI18N
        }
    }

    /**
     * Checks if the method is specified by Serialization API and the class
     * extends Serializable/Externalizable. Unused methods defined in API spec
     * should not be marked as unused.
     * 
     * @param info compilation context
     * @param method the method
     * @return true, if the method is from serialization API and should not be reported
     */
    private boolean isSerializationMethod(CompilationInfo info, ExecutableElement method) {
        if (!isInSerializableOrExternalizable(info, method)) {
            return false;
        }
        ElementHandle<ExecutableElement> eh = ElementHandle.create(method);
        String sign = _getSignatureHack(eh);
        return SERIALIZABLE_SIGNATURES.contains(sign);
    }
    
    protected boolean process(CompilationInfo info, final Document doc, ErrorDescriptionSetter setter) {
        DetectorVisitor v = new DetectorVisitor(info, doc, cancel);
        
        Map<Token, Coloring> newColoring = new IdentityHashMap<>();

        CompilationUnitTree cu = info.getCompilationUnit();
        
        v.scan(cu, null);
        
        if (cancel.get())
            return true;
        
        boolean computeUnusedImports = "text/x-java".equals(FileUtil.getMIMEType(info.getFileObject()));
        
        List<Pair<int[], Coloring>> extraColoring = computeUnusedImports ? new ArrayList<>(v.extraColoring) : v.extraColoring;

        if (computeUnusedImports) {
            Collection<TreePath> unusedImports = UnusedImports.process(info, cancel);

            if (unusedImports == null) return true;
            
            Coloring unused = collection2Coloring(Arrays.asList(ColoringAttributes.UNUSED));

            for (TreePath tree : unusedImports) {
                if (cancel.get()) {
                    return true;
                }

                //XXX: finish
                extraColoring.add(Pair.of(new int[] {
                    (int) info.getTrees().getSourcePositions().getStartPosition(cu, tree.getLeaf()),
                    (int) info.getTrees().getSourcePositions().getEndPosition(cu, tree.getLeaf())
                }, unused));
            }
        }
        
        for (Element decl : v.type2Uses.keySet()) {
            if (cancel.get())
                return true;
            
            List<Use> uses = v.type2Uses.get(decl);
            
            for (Use u : uses) {
                if (u.spec == null)
                    continue;
                
                if (u.type.contains(UseTypes.DECLARATION) && Utilities.isPrivateElement(decl)) {
                    if ((decl.getKind().isField() && !isSerialSpecField(info, decl)) || isLocalVariableClosure(decl)) {
                        if (!hasAllTypes(uses, EnumSet.of(UseTypes.READ, UseTypes.WRITE))) {
                            u.spec.add(ColoringAttributes.UNUSED);
                        }
                    }
                    
                    if ((decl.getKind() == ElementKind.CONSTRUCTOR && !decl.getModifiers().contains(Modifier.PRIVATE)) || decl.getKind() == ElementKind.METHOD) {
                        if (!(hasAllTypes(uses, EnumSet.of(UseTypes.EXECUTE)) || isSerializationMethod(info, (ExecutableElement)decl))) {
                            u.spec.add(ColoringAttributes.UNUSED);
                        }
                    }
                    
                    if (decl.getKind().isClass() || decl.getKind().isInterface()) {
                        if (!hasAllTypes(uses, EnumSet.of(UseTypes.CLASS_USE))) {
                            u.spec.add(ColoringAttributes.UNUSED);
                        }
                    }
                }
                
                Coloring c = collection2Coloring(u.spec);
                
                List<Token> tl = v.tree2Tokens.get(u.tree.getLeaf());
                
                if (tl != null) {
                    for (Token t : tl) {
                        newColoring.put(t, c);
                    }
                }
            }
        }
        
        Coloring kwc = collection2Coloring(EnumSet.of(ColoringAttributes.KEYWORD));
        for (Token kw : v.contextKeywords) {
            newColoring.put(kw, kwc);
        }
        
        if (cancel.get())
            return true;
        
        if (computeUnusedImports) {
            setter.setHighlights(doc, extraColoring, v.preText);
        }

        setter.setColorings(doc, newColoring);

        return false;
    }
    
        
    private boolean hasAllTypes(List<Use> uses, EnumSet<UseTypes> types) {
        for (Use u : uses) {
            if (types.isEmpty()) {
                return true;
            }
            
            types.removeAll(u.type);
        }
        
        return types.isEmpty();
    }
    
    private enum UseTypes {
        READ, WRITE, EXECUTE, DECLARATION, CLASS_USE, MODULE_USE;
    }
    
    private static Coloring collection2Coloring(Collection<ColoringAttributes> attr) {
        Coloring c = ColoringAttributes.empty();
        
        for (ColoringAttributes a : attr) {
            c = ColoringAttributes.add(c, a);
        }
        
        return c;
    }
    
    private static boolean isLocalVariableClosure(Element el) {
        return el.getKind() == ElementKind.PARAMETER || el.getKind() == ElementKind.LOCAL_VARIABLE
                || el.getKind() == ElementKind.RESOURCE_VARIABLE || el.getKind() == ElementKind.EXCEPTION_PARAMETER;
    }
    
    /** Detects static final long SerialVersionUID 
     * @return true if element is final static long serialVersionUID
     */
    private static boolean isSerialSpecField(CompilationInfo info, Element el) {
        if (el.getModifiers().contains(Modifier.FINAL) 
                && el.getModifiers().contains(Modifier.STATIC)) {
            
            if (!isInSerializableOrExternalizable(info, el)) {
                return false;
            }
            if (info.getTypes().getPrimitiveType(TypeKind.LONG).equals(el.asType())
                && el.getSimpleName().toString().equals("serialVersionUID")) {
                return true;
            }
            if (el.getSimpleName().contentEquals("serialPersistentFields")) {
                return true;
            }
        }
        return false;
    }
        
    private static class Use {
        private Collection<UseTypes> type;
        private TreePath     tree;
        private Collection<ColoringAttributes> spec;
        
        public Use(Collection<UseTypes> type, TreePath tree, Collection<ColoringAttributes> spec) {
            this.type = type;
            this.tree = tree;
            this.spec = spec;
        }
        
        @Override
        public String toString() {
            return "Use: " + type;
        }
    }
    
    private static class DetectorVisitor extends CancellableTreePathScanner<Void, Void> {
        
        private org.netbeans.api.java.source.CompilationInfo info;
        private Document doc;
        private Map<Element, List<Use>> type2Uses;        
        private Map<Tree, List<Token>> tree2Tokens;
        private List<Token> contextKeywords;
        private List<Pair<int[], Coloring>> extraColoring;
        private Map<int[], String> preText;
        private TokenList tl;
        private long memberSelectBypass = -1;        
        private SourcePositions sourcePositions;
        private ExecutableElement recursionDetector;
        
        private DetectorVisitor(org.netbeans.api.java.source.CompilationInfo info, final Document doc, AtomicBoolean cancel) {
            super(cancel);
            
            this.info = info;
            this.doc  = doc;
            type2Uses = new HashMap<Element, List<Use>>();
            tree2Tokens = new IdentityHashMap<Tree, List<Token>>();
            contextKeywords = new ArrayList<>();
            extraColoring = new ArrayList<>();
            preText = new HashMap<>();

            tl = new TokenList(info, doc, cancel);
            
            this.sourcePositions = info.getTrees().getSourcePositions();
//            this.pos = pos;
        }
        
        private void firstIdentifier(String name) {
            tl.firstIdentifier(getCurrentPath(), name, tree2Tokens);
        }
        
        private Token firstIdentifierToken(String... names) {
            for (String name : names) {
                Token t = tl.firstIdentifier(getCurrentPath(), name);
                if (t != null) {
                    return t;
                }
            }
            return null;
        }
        
        @Override
        public Void visitMemberSelect(MemberSelectTree tree, Void p) {
            long memberSelectBypassLoc = memberSelectBypass;
            
            memberSelectBypass = -1;
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getKind() == ElementKind.MODULE) {
                //Xxx
                handlePossibleIdentifier(getCurrentPath(), false);
                tl.moduleNameHere(tree, tree2Tokens);
                return null;
            }

            super.visitMemberSelect(tree, p);
            
            tl.moveToEnd(tree.getExpression());
            
            if (memberSelectBypassLoc != (-1)) {
                tl.moveToOffset(memberSelectBypassLoc);
            }
            
            handlePossibleIdentifier(getCurrentPath(), false);
            firstIdentifier(tree.getIdentifier().toString());
            
            return null;
        }
        
        private void addModifiers(Element decl, Collection<ColoringAttributes> c) {
            if (decl.getModifiers().contains(Modifier.STATIC)) {
                c.add(ColoringAttributes.STATIC);
            }
            
            if (decl.getModifiers().contains(Modifier.ABSTRACT) && !decl.getKind().isInterface()) {
                c.add(ColoringAttributes.ABSTRACT);
            }
            
            boolean accessModifier = false;
            
            if (decl.getModifiers().contains(Modifier.PUBLIC)) {
                c.add(ColoringAttributes.PUBLIC);
                accessModifier = true;
            }
            
            if (decl.getModifiers().contains(Modifier.PROTECTED)) {
                c.add(ColoringAttributes.PROTECTED);
                accessModifier = true;
            }
            
            if (decl.getModifiers().contains(Modifier.PRIVATE)) {
                c.add(ColoringAttributes.PRIVATE);
                accessModifier = true;
            }
            
            if (!accessModifier && !isLocalVariableClosure(decl)) {
                c.add(ColoringAttributes.PACKAGE_PRIVATE);
            }
            
            if (info.getElements().isDeprecated(decl)) {
                c.add(ColoringAttributes.DEPRECATED);
            }
        }
        
        private Collection<ColoringAttributes> getMethodColoring(ExecutableElement mdecl) {
            Collection<ColoringAttributes> c = new ArrayList<ColoringAttributes>();
            
            addModifiers(mdecl, c);
            
            if (mdecl.getKind() == ElementKind.CONSTRUCTOR) {
                c.add(ColoringAttributes.CONSTRUCTOR);
            } else
                c.add(ColoringAttributes.METHOD);
            
            return c;
        }
        
        private Collection<ColoringAttributes> getVariableColoring(Element decl) {
            Collection<ColoringAttributes> c = new ArrayList<ColoringAttributes>();
            
            addModifiers(decl, c);
            
            if (decl.getKind().isField()) {
                c.add(ColoringAttributes.FIELD);
                
                return c;
            }
            
            if (decl.getKind() == ElementKind.LOCAL_VARIABLE || decl.getKind() == ElementKind.RESOURCE_VARIABLE
                    || decl.getKind() == ElementKind.EXCEPTION_PARAMETER) {
                c.add(ColoringAttributes.LOCAL_VARIABLE);
                
                return c;
            }
            
            if (decl.getKind() == ElementKind.PARAMETER) {
                c.add(ColoringAttributes.PARAMETER);
                
                return c;
            }
            
            assert false;
            
            return null;
        }

        private static final Set<Kind> LITERALS = EnumSet.of(Kind.BOOLEAN_LITERAL, Kind.CHAR_LITERAL, Kind.DOUBLE_LITERAL, Kind.FLOAT_LITERAL, Kind.INT_LITERAL, Kind.LONG_LITERAL, Kind.STRING_LITERAL);

        private void handlePossibleIdentifier(TreePath expr, boolean declaration) {
            handlePossibleIdentifier(expr, declaration, null);
        }
        
        private void handlePossibleIdentifier(TreePath expr, boolean declaration, Element decl) {
            if (Utilities.isKeyword(expr.getLeaf())) {
                //ignore keywords:
                return ;
            }

            if (expr.getLeaf().getKind() == Kind.PRIMITIVE_TYPE) {
                //ignore primitive types:
                return ;
            }

            if (LITERALS.contains(expr.getLeaf().getKind())) {
                //ignore literals:
                return ;
            }

            decl = decl == null ? info.getTrees().getElement(expr) : decl;

            ElementKind declKind = decl != null ? decl.getKind() : null;
            boolean isDeclType = decl != null &&
                                 (declKind.isClass() || declKind.isInterface());
            TreePath currentPath = getCurrentPath();
            TreePath parent = currentPath.getParentPath();

            //for new <type>(), highlight <type> as a constructor:
            if (isDeclType &&
                parent.getLeaf().getKind() == Kind.NEW_CLASS) {
		decl = info.getTrees().getElement(parent);
	    }

            if (isDeclType &&
                (parent.getLeaf().getKind() == Kind.PARAMETERIZED_TYPE &&
                  ((ParameterizedTypeTree) parent.getLeaf()).getType() == currentPath.getLeaf() &&
                  parent.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS)) {
		decl = info.getTrees().getElement(parent.getParentPath());
	    }

            if (decl == null) {
                return ;
            }

            isDeclType = decl.getKind().isClass() || decl.getKind().isInterface();
            Collection<ColoringAttributes> c = null;

            if (decl.getKind().isField() || isLocalVariableClosure(decl)) {
                c = getVariableColoring(decl);
            }
            
            if (decl instanceof ExecutableElement) {
                c = getMethodColoring((ExecutableElement) decl);
            }
            
            if (decl.getKind() == ElementKind.MODULE) {
                c = new ArrayList<ColoringAttributes>();
                c.add(ColoringAttributes.MODULE);
            }

            if (isDeclType) {
                c = new ArrayList<ColoringAttributes>();
                
                addModifiers(decl, c);
                
                switch (decl.getKind()) {
                    case CLASS: c.add(ColoringAttributes.CLASS); break;
                    case INTERFACE: c.add(ColoringAttributes.INTERFACE); break;
                    case ANNOTATION_TYPE: c.add(ColoringAttributes.ANNOTATION_TYPE); break;
                    case ENUM: c.add(ColoringAttributes.ENUM); break;
                }
            }                       
            
            if (declaration) {
                if (c == null) {
                    c = new ArrayList<ColoringAttributes>();
                }
                
                c.add(ColoringAttributes.DECLARATION);
            }
            
            if (c != null) {
                Collection<UseTypes> type = EnumSet.noneOf(UseTypes.class);

                if (isDeclType) {
                    if (!declaration) {
                        type.add(UseTypes.CLASS_USE);
                    }
                } else if (decl.getKind().isField() || isLocalVariableClosure(decl)) {
                    if (!declaration) {
                        while (true) {
                            if (parent.getLeaf().getKind() == Kind.POSTFIX_DECREMENT ||
                                parent.getLeaf().getKind() == Kind.POSTFIX_INCREMENT ||
                                parent.getLeaf().getKind() == Kind.PREFIX_DECREMENT ||
                                parent.getLeaf().getKind() == Kind.PREFIX_INCREMENT) {
                                type.add(UseTypes.WRITE);
                                currentPath = parent;
                                parent = currentPath.getParentPath();
                                continue;
                            }
                            if (CompoundAssignmentTree.class.isAssignableFrom(parent.getLeaf().getKind().asInterface()) &&
                                ((CompoundAssignmentTree) parent.getLeaf()).getVariable() == currentPath.getLeaf()) {
                                type.add(UseTypes.WRITE);
                                currentPath = parent;
                                parent = currentPath.getParentPath();
                                continue;
                            }
                            break;
                        }
                        if (parent.getLeaf().getKind() == Kind.ASSIGNMENT &&
                            ((AssignmentTree) parent.getLeaf()).getVariable() == currentPath.getLeaf()) {
                            type.add(UseTypes.WRITE);
                        } else if (parent.getLeaf().getKind() != Kind.EXPRESSION_STATEMENT) {
                            type.add(UseTypes.READ);
                        }
                    } else if (decl.getKind() == ElementKind.PARAMETER) {
                        Element method = decl.getEnclosingElement();

                        type.add(UseTypes.WRITE);

                        if (parent.getLeaf().getKind() == Kind.LAMBDA_EXPRESSION &&
                            ((LambdaExpressionTree) parent.getLeaf()).getParameters().contains(currentPath.getLeaf())) {
//                            type.add(UseTypes.READ);
                        } else if (method.getModifiers().contains(Modifier.ABSTRACT) || method.getModifiers().contains(Modifier.NATIVE) || !method.getModifiers().contains(Modifier.PRIVATE)) {
                            type.add(UseTypes.READ);
                        }
                    } else if (decl.getKind().isField() || decl.getKind() == ElementKind.EXCEPTION_PARAMETER) {
                        type.add(UseTypes.WRITE);
                    } else if (parent.getLeaf().getKind() == Kind.ENHANCED_FOR_LOOP &&
                               ((EnhancedForLoopTree) parent.getLeaf()).getVariable() == currentPath.getLeaf()) {
                        type.add(UseTypes.WRITE);
                    } else {
                        VariableTree vt = (VariableTree) currentPath.getLeaf();

                        if (vt.getInitializer() != null) {
                            type.add(UseTypes.WRITE);
                        }
                    }
                } else if (decl.getKind() == ElementKind.METHOD) {
                    if (!declaration) {
                        type.add(UseTypes.EXECUTE);
                    }
                } else if (decl.getKind() == ElementKind.CONSTRUCTOR) {
                    if (!declaration) {
                        if (info.getElements().isDeprecated(decl.getEnclosingElement())) {
                            c.add(ColoringAttributes.DEPRECATED);
                        }
                        type.add(UseTypes.EXECUTE);
                    }
                }
                if (declaration) {
                    type.add(UseTypes.DECLARATION);
                }
                addUse(decl, type, expr, c);
            }
        }
        
        private void addUse(Element decl, Collection<UseTypes> useTypes, TreePath t, Collection<ColoringAttributes> c) {
            if (decl == recursionDetector) {
                useTypes.remove(UseTypes.EXECUTE); //recursive execution is not use
            }
            
            List<Use> uses = type2Uses.get(decl);
            
            if (uses == null) {
                type2Uses.put(decl, uses = new ArrayList<Use>());
            }
            
            Use u = new Use(useTypes, t, c);
            
            uses.add(u);
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree tree, Void p) {
	    //ignore package X.Y.Z;:
	    //scan(tree.getPackageDecl(), p);
            tl.moveBefore(tree.getImports());
	    scan(tree.getImports(), p);
            tl.moveBefore(tree.getPackageAnnotations());
	    scan(tree.getPackageAnnotations(), p);
            tl.moveToEnd(tree.getImports());
	    scan(tree.getTypeDecls(), p);
	    return null;
        }

        @Override
        public Void visitModule(ModuleTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            scan(tree.getAnnotations(), p);
            tl.moveToEnd(tree.getAnnotations());
            if (tree.getModuleType() == ModuleTree.ModuleKind.OPEN) {
                Token t = firstIdentifierToken("open"); //NOI18N
                if (t != null) {
                    contextKeywords.add(t);
                }
                tl.moveNext();
            }
            Token t = firstIdentifierToken("module"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            Element e = info.getTrees().getElement(getCurrentPath());
            if (e != null && e.getKind() == ElementKind.MODULE) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getName()), true, e);
                tl.moduleNameHere(tree.getName(), tree2Tokens);
            }
            scan(tree.getDirectives(), p);
            return null;
        }

        @Override
        public Void visitExports(ExportsTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("exports"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            scan(tree.getPackageName(), p);
            tl.moveToOffset(sourcePositions.getEndPosition(info.getCompilationUnit(), tree.getPackageName()));
            t = firstIdentifierToken("to"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            return scan(tree.getModuleNames(), p);
        }

        @Override
        public Void visitOpens(OpensTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("opens"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            scan(tree.getPackageName(), p);
            tl.moveToOffset(sourcePositions.getEndPosition(info.getCompilationUnit(), tree.getPackageName()));
            t = firstIdentifierToken("to"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            return scan(tree.getModuleNames(), p);
        }

        @Override
        public Void visitProvides(ProvidesTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("provides"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            scan(tree.getServiceName(), p);
            tl.moveToOffset(sourcePositions.getEndPosition(info.getCompilationUnit(), tree.getServiceName()));
            t = firstIdentifierToken("with"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            return scan(tree.getImplementationNames(), p);
        }

        @Override
        public Void visitRequires(RequiresTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("requires"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
                tl.moveNext();
                if (tree.isStatic() && tree.isTransitive()) {
                    t = firstIdentifierToken("static", "transitive"); //NOI18N
                    if (t != null) {
                        contextKeywords.add(t);
                    }
                    tl.moveNext();
                    t = firstIdentifierToken("static", "transitive"); //NOI18N
                    if (t != null) {
                        contextKeywords.add(t);
                    }
                } else if (tree.isStatic()) {
                    t = firstIdentifierToken("static"); //NOI18N
                    if (t != null) {
                        contextKeywords.add(t);
                    }
                } else if (tree.isTransitive()) {
                    t = firstIdentifierToken("transitive"); //NOI18N
                    if (t != null) {
                        contextKeywords.add(t);
                    }
                }
            }
            return super.visitRequires(tree, p);
        }

        @Override
        public Void visitUses(UsesTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("uses"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            return super.visitUses(tree, p);
        }
                
        @Override
        public Void visitMethodInvocation(MethodInvocationTree tree, Void p) {
            Tree possibleIdent = tree.getMethodSelect();
            
            if (possibleIdent.getKind() == Kind.IDENTIFIER) {
                //handle "this" and "super" constructors:
                String ident = ((IdentifierTree) possibleIdent).getName().toString();
                
                if ("super".equals(ident) || "this".equals(ident)) { //NOI18N
                    Element resolved = info.getTrees().getElement(getCurrentPath());
                    
                    addUse(resolved, EnumSet.of(UseTypes.EXECUTE), null, null);
                }
            }
            
            List<? extends Tree> ta = tree.getTypeArguments();
            long afterTypeArguments = ta.isEmpty() ? -1 : info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), ta.get(ta.size() - 1));
            
            switch (tree.getMethodSelect().getKind()) {
                case IDENTIFIER:
                case MEMBER_SELECT:
                    memberSelectBypass = afterTypeArguments;
                    scan(tree.getMethodSelect(), p);
                    memberSelectBypass = -1;
                    break;
                default:
                    //todo: log
                    scan(tree.getMethodSelect(), p);
            }

            //the type arguments are before the last identifier in the select, so we should return there:
            //not very efficient, though:
            tl.moveBefore(tree.getTypeArguments());
            
            scan(tree.getTypeArguments(), null);
            
            scan(tree.getArguments(), p);
            
            return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, Void p) {
            if (info.getTreeUtilities().isSynthetic(getCurrentPath()))
                return null;

            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            
            if (memberSelectBypass != (-1)) {
                tl.moveToOffset(memberSelectBypass);
                memberSelectBypass = -1;
            }
            
            tl.identifierHere(tree, tree2Tokens);
            
            handlePossibleIdentifier(getCurrentPath(), false);
            super.visitIdentifier(tree, null);
            return null;
        }

        @Override
        public Void visitMethod(MethodTree tree, Void p) {
            if (info.getTreeUtilities().isSynthetic(getCurrentPath())) {
                return super.visitMethod(tree, p);
            }

            //#170338: constructor without modifiers:
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));

            handlePossibleIdentifier(getCurrentPath(), true);
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            scan(tree.getModifiers(), null);
            tl.moveToEnd(tree.getModifiers());
            scan(tree.getTypeParameters(), null);
            tl.moveToEnd(tree.getTypeParameters());
            scan(tree.getReturnType(), p);
            tl.moveToEnd(tree.getReturnType());
            
            String name;
            
            if (tree.getReturnType() != null) {
                //method:
                name = tree.getName().toString();
            } else {
                //constructor:
                TreePath tp = getCurrentPath();
                
                while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                    tp = tp.getParentPath();
                }
                
                if (tp != null && TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                    name = ((ClassTree) tp.getLeaf()).getSimpleName().toString();
                } else {
                    name = null;
                }
            }
            
            if (name != null) {
                firstIdentifier(name);
            }
            
            scan(tree.getParameters(), null);
            scan(tree.getThrows(), null);
            scan(tree.getDefaultValue(), null);

            recursionDetector = (el != null && el.getKind() == ElementKind.METHOD) ? (ExecutableElement) el : null;
            
            scan(tree.getBody(), null);

            recursionDetector = null;
        
            return null;
        }

        @Override
        public Void visitVariable(VariableTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));

            handlePossibleIdentifier(getCurrentPath(), true);
            
            scan(tree.getModifiers(), null);
            
            tl.moveToEnd(tree.getModifiers());
            
            scan(tree.getType(), null);
            
            int[] span = info.getTreeUtilities().findNameSpan(tree);
            if (span != null)
                tl.moveToOffset(span[0]);
            else
                tl.moveToEnd(tree.getType());
            
            firstIdentifier(tree.getName().toString());
            
            tl.moveNext();
            
            scan(tree.getInitializer(), p);
            
            return null;
        }
        
        @Override
        public Void visitNewClass(NewClassTree tree, Void p) {
            TreePath tp;
            Tree ident = tree.getIdentifier();
            
            if (ident.getKind() == Kind.PARAMETERIZED_TYPE) {
                tp = new TreePath(new TreePath(getCurrentPath(), ident), ((ParameterizedTypeTree) ident).getType());
            } else {
                tp = new TreePath(getCurrentPath(), ident);
            }
            
            Element clazz = info.getTrees().getElement(tp);
            
            if (clazz != null) {
                addUse(clazz, EnumSet.of(UseTypes.CLASS_USE), null, null);
            }
	    
            scan(tree.getEnclosingExpression(), null);
            scan(tree.getIdentifier(), null);
            scan(tree.getTypeArguments(), null);
            scan(tree.getArguments(), p);
            scan(tree.getClassBody(), null);
            
            return null;
        }

        @Override
        public Void visitClass(ClassTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            
            handlePossibleIdentifier(getCurrentPath(), true);
            
            scan(tree.getModifiers(), null);
            
            tl.moveToEnd(tree.getModifiers());
            firstIdentifier(tree.getSimpleName().toString());
            
            //XXX:????
            scan(tree.getTypeParameters(), null);
            scan(tree.getExtendsClause(), null);
            scan(tree.getImplementsClause(), null);

            ExecutableElement prevRecursionDetector = recursionDetector;

            recursionDetector = null;
            
            scan(tree.getMembers(), null);

            recursionDetector = prevRecursionDetector;
            
            //XXX: end ???
            
            return null;
        }
        
        @Override
        public Void visitMemberReference(MemberReferenceTree node, Void p) {
            scan(node.getQualifierExpression(), p);
            tl.moveToEnd(node.getQualifierExpression());
            scan(node.getTypeArguments(), null);
            tl.moveToEnd(node.getTypeArguments());
            handlePossibleIdentifier(getCurrentPath(), false);
            firstIdentifier(node.getName().toString());
            return null;
        }

        private static final Coloring UNINDENTED_TEXT_BLOCK =
                ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.UNINDENTED_TEXT_BLOCK);

        @Override
        public Void visitLiteral(LiteralTree node, Void p) {
            int startPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), node);
            tl.moveToOffset(startPos);
            Token t = tl.currentToken();
            if (t != null && t.id() == JavaTokenId.MULTILINE_STRING_LITERAL && t.partType() == PartType.COMPLETE) {
                String tokenText = t.text().toString();
                String[] lines = tokenText.split("\n");
                int indent = Arrays.stream(lines, 1, lines.length)
                                   .mapToInt(this::leadingIndent)
                                   .min()
                                   .orElse(0);
                int pos = startPos + lines[0].length() + 1;
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    if (i == lines.length - 1) {
                        line = line.substring(0, line.length() - 3);
                    }
                    String strippendLine = line.replaceAll("[\t ]+$", "");
                    int indentedStart = pos + indent;
                    int indentedEnd = pos + strippendLine.length();
                    if (indentedEnd > indentedStart)
                        extraColoring.add(Pair.of(new int[] {indentedStart, indentedEnd}, UNINDENTED_TEXT_BLOCK));
                    pos += line.length() + 1;
                }
            }

            TreePath pp = getCurrentPath().getParentPath();
            if (pp.getLeaf() != null &&
                pp.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
                MethodInvocationTree inv = (MethodInvocationTree) pp.getLeaf();
                int pos = inv.getArguments().indexOf(node);
                if (pos != (-1)) {
                    Element invoked = info.getTrees().getElement(pp);
                    if (invoked != null && (invoked.getKind() == ElementKind.METHOD || invoked.getKind() == ElementKind.CONSTRUCTOR)) {
                        long start = sourcePositions.getStartPosition(info.getCompilationUnit(), node);
                        long end = start + 1;
                        ExecutableElement invokedMethod = (ExecutableElement) invoked;
                        pos = Math.min(pos, invokedMethod.getParameters().size() - 1);
                        preText.put(new int[] {(int) start, (int) end},
                                    invokedMethod.getParameters().get(pos).getSimpleName() + ":");
                    }
                }
            }
            return super.visitLiteral(node, p);
        }

        @Override
        public Void scan(Tree tree, Void p) {
            if (tree != null && "YIELD".equals(tree.getKind().name())) {
                tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
                Token t = firstIdentifierToken("yield"); //NOI18N
                if (t != null) {
                    contextKeywords.add(t);
                }
            }
            return super.scan(tree, p);
        }

        private int leadingIndent(String line) {
            int indent = 0;

            for (int i = 0; i < line.length(); i++) { //TODO: code points
                if (Character.isWhitespace(line.charAt(i)))
                    indent++;
                else
                    break;
            }

            return indent;
        }
    }

    public static interface ErrorDescriptionSetter {
        
        public void setHighlights(Document doc, Collection<Pair<int[], Coloring>> highlights, Map<int[], String> preText);
        public void setColorings(Document doc, Map<Token, Coloring> colorings);
    }    
}
