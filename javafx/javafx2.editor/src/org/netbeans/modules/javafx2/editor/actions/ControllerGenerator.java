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
package org.netbeans.modules.javafx2.editor.actions;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.model.EventHandler;
import org.netbeans.modules.javafx2.editor.completion.model.FxClassUtils;
import org.netbeans.modules.javafx2.editor.completion.model.FxInclude;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class ControllerGenerator implements Task<WorkingCopy> {
    /**
     * Parsed FXML source
     */
    private FxmlParserResult fxmlSource;
    
    /**
     * Working copy of the java controller
     */
    private WorkingCopy wcopy;
    
    /**
     * Source file for the controller
     */
    private DataObject  controllerSource;

    public ControllerGenerator(FxmlParserResult fxmlSource, DataObject controllerSource) {
        this.fxmlSource = fxmlSource;
        this.controllerSource = controllerSource;
    }
    
    /**
     * Tree for the controller class
     */
    private ClassTree   controllerClass;

    /**
     * The original unmodified tree; will be replaced at the end
     */
    private ClassTree   origController;
    
    /**
     * FXML annotation
     */
    private TypeElement fxmlAnnotationType;
    
    /**
     * Tree for the FXML annotation
     */
    private AnnotationTree fxmlAnnotationTree;
    
    @Override
    public void run(WorkingCopy parameter) throws Exception {
        this.wcopy = parameter;
        parameter.toPhase(JavaSource.Phase.RESOLVED);
        fxmlAnnotationType = wcopy.getElements().getTypeElement("javafx.fxml.FXML"); // NO18N
        if (fxmlAnnotationType == null) {
            throw new ParseException("FXML libraries not on classpath"); // NOI18N
        }
        fxmlAnnotationTree = wcopy.getTreeMaker().Annotation(
                wcopy.getTreeMaker().Type(fxmlAnnotationType.asType()), 
                Collections.<ExpressionTree>emptyList());
        
        List<? extends Tree> types = parameter.getCompilationUnit().getTypeDecls();
        for (Tree t : types) {
            if (t.getKind() == Tree.Kind.CLASS) {
                ClassTree ct = (ClassTree)t;
                if (ct.getModifiers().getFlags().contains(Modifier.PUBLIC) &&
                    ct.getSimpleName().toString().equals(parameter.getSnapshot().getSource().getFileObject().getName())) {
                    controllerClass = ct;
                }
            }
        }
        if (controllerClass == null) {
            throw new UnsupportedOperationException("Controller class not found");
        }
        
        origController = controllerClass;
        wcopy.getTrees().getDocComment(getControllerPath());
        indexController();

        // GO !
        fxmlSource.getSourceModel().accept(new UpdatingVisitor());
        
        cleanUnused();
        
        wcopy.rewrite(origController, controllerClass);
    }
    
    /**
     * Methods found in the controller, indexed by name. Values may contain
     * 1-* entries to cover overloads. 
     */
    private Map<String, Object>  methods = new HashMap<String, Object>();
    
    /**
     * Methods generated by this controller + their type parameter.
     */
    private Map<String, Collection<TypeMirror>> generatedMethods = new HashMap<String, Collection<TypeMirror>>();
    
    /**
     * Fields generated by this controller + their type.
     */
    private Map<String, TypeMirror> generatedFields = new HashMap<String, TypeMirror>();
    
    /**
     * Fields indexed by name
     */
    private Map<String, VariableTree>  fields = new HashMap<String, VariableTree>();
    
    /**
     * Trees from the original source, which have been mapped to the FXML file.
     */
    private Set<Tree> mappedTrees = new HashSet<Tree>();
    
    private void indexController() {
        for (Tree member : controllerClass.getMembers()) {
            switch (member.getKind()) {
                case VARIABLE: {
                    VariableTree vt = (VariableTree)member;
                    fields.put(vt.getName().toString(), vt);
                    break;
                }
                case METHOD: {
                    MethodTree mt = (MethodTree)member;
                    addMethod(mt);
                    break;
                }
            }
        }
    }
    
    /**
     * Adds methods to the method index. Each name slot may contain multiple
     * items to handle method overloads.
     * 
     * @param mt 
     */
    private void addMethod(MethodTree mt) {
        String key = mt.getName().toString();
        Object old = methods.get(key);
        if (old == null) {
            methods.put(key, mt);
        } else {
            Collection c;
            
            if (old instanceof Collection) {
                c = (Collection)old;
            } else {
                methods.put(key, c = new ArrayList<Object>());
                c.add(old);
            }
            c.add(mt);
        }
    }
    
    private TypeElement    getInstanceType(FxInstance decl) {
        ElementHandle<TypeElement> compType = decl.getJavaType();
        if (compType == null) {
            return null;
        }
        TypeElement tel = compType.resolve(wcopy);
        return tel;
    }
    
    private TreePath controllerPath;
    
    private TreePath getControllerPath() {
        if (controllerPath == null) {
            controllerPath = new TreePath(new TreePath(wcopy.getCompilationUnit()), origController);
        }
        return controllerPath;
    }
    
    private void addMethod(String name, TypeMirror parameter) {
        Collection<TypeMirror> param = generatedMethods.get(name);
        if (param == null) {
            param = new LinkedList<TypeMirror>();
            generatedMethods.put(name, param);
        }
        for (TypeMirror tm : param) {
            if (wcopy.getTypes().isSameType(tm, parameter)) {
                throw new IllegalStateException();
            }
        }
        param.add(parameter);
    }
    
    private boolean isGenerated(String name, TypeMirror paramType) {
        Collection<TypeMirror> param = generatedMethods.get(name);
        if (param == null) {
            return false;
        }
        for (TypeMirror tm : param) {
            if (wcopy.getTypes().isSameType(tm, paramType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Projects event handler into the class code
     */
    private void syncEventHandler(EventHandler h) {
        final String n = h.getHandlerName().toString();
        final Collection<MethodTree> overloads;
        
        if (h.getEventInfo() == null || h.getEventInfo().getEventType() == null) {
            return;
        }
        TypeElement eventType = h.getEventInfo().getEventType().resolve(wcopy);
        if (eventType == null) {
            return;
        }
        
        if (isGenerated(n, eventType.asType())) {
            // already generated
            return;
        }
        
        Object o = methods.get(n);
        if (o instanceof Collection) {
            overloads = (Collection<MethodTree>)o;
        } else if (o != null) {
            overloads = Collections.singleton((MethodTree)o);
        } else {
            defineNewHandler(h.getHandlerName(), eventType);
            return;
        }
        
        ExecutableElement handlerElement = null;
        ExecutableElement handlerElementNoArg = null;

        for (MethodTree mt : overloads) {
            TreePath mPath = new TreePath(getControllerPath(), mt);
            ExecutableElement e = (ExecutableElement)wcopy.getTrees().getElement(mPath);
            
            // FXML 2.2, event handlers may have empty parameter list
            if (e.getParameters().isEmpty()) {
                handlerElementNoArg = e;
                break;
            } else if (e.getParameters().size() == 1) {
                VariableElement param = e.getParameters().get(0);
                if (wcopy.getTypes().isAssignable(eventType.asType(), param.asType())) {
                    // found
                    handlerElement = e;
                    break;
                }
            } 
        }
        
        // prefer handler with event
        if (handlerElement == null) {
            handlerElement = handlerElementNoArg;
        }
        
        if (handlerElement == null) {
            // no handler found, define our own:
            defineNewHandler(n, eventType);
        } else {
            MethodTree handlerTree = wcopy.getTrees().getTree(handlerElement);
            mappedTrees.add(handlerTree);
            AnnotationTree ann = findFxmlAnnotation(handlerTree.getModifiers());

            if (ann == null) {
                TreeMaker mk = wcopy.getTreeMaker();
                wcopy.rewrite(handlerTree.getModifiers(), mk.addModifiersAnnotation(handlerTree.getModifiers(), fxmlAnnotationTree));
            }
        }
    }
    
    /**
     * Defines a new method as event handler, and marks it with @FXML annotation.
     * 
     * @param h handler definition
     */
    private void defineNewHandler(CharSequence handlerName, TypeElement eventType) {
        TreeMaker mk = wcopy.getTreeMaker();
        
        // @FXML private void {handlerName}({eventType} event);
        MethodTree handler = mk.Method(
                mk.Modifiers(Collections.singleton(Modifier.PRIVATE),
                    Collections.singletonList(fxmlAnnotationTree)
                ),
                handlerName, 
                mk.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                Collections.singletonList(
                    mk.Variable(
                        mk.Modifiers(Collections.<Modifier>emptySet()),
                        "event", mk.Type(eventType.asType()), null)
                ), 
                Collections.<ExpressionTree>emptyList(),
                mk.Block(Collections.<StatementTree>emptyList(), false), 
                null);
        
        // add to class
        controllerClass = genUtils().insertClassMember(controllerClass, handler);
        addMethod(handlerName.toString(), eventType.asType());
    }
    
    private GeneratorUtilities genUtils;
    
    private GeneratorUtilities genUtils() {
        if (genUtils == null) {
            genUtils = GeneratorUtilities.get(wcopy);
        }
        return genUtils;
    }
    
    /* test */ static TypeMirror eraseFieldTypeParameters(TypeMirror tm, CompilationInfo cinfo) {
        Boolean shouldReplace = tm.accept(new SimpleTypeVisitor6<Boolean, Void>() {

            @Override
            public Boolean visitPrimitive(PrimitiveType t, Void p) {
                return false;
            }

            @Override
            public Boolean visitNull(NullType t, Void p) {
                return false;
            }

            @Override
            public Boolean visitArray(ArrayType t, Void p) {
                return visit(t.getComponentType());
            }

            @Override
            public Boolean visitDeclared(DeclaredType t, Void p) {
                return !t.getTypeArguments().isEmpty();
            }

            @Override
            public Boolean visitNoType(NoType t, Void p) {
                return false;
            }
            
        }, null);
        
        if (Boolean.TRUE.equals(shouldReplace)) {
            return tm.accept(new FieldTParamEraser(), cinfo);
        } else {
            return tm;
        }
    }
    
    private static class FieldTParamEraser extends SimpleTypeVisitor6<TypeMirror, CompilationInfo> {
        @Override
        public TypeMirror visitArray(ArrayType t, CompilationInfo p) {
            TypeMirror component = visit(t.getComponentType(), p);
            return p.getTypes().getArrayType(component);
        }

        @Override
        public TypeMirror visitDeclared(DeclaredType t, CompilationInfo p) {
            if (t.getTypeArguments().isEmpty()) {
                return t;
            }
            List<TypeMirror> newArgs = new ArrayList<TypeMirror>(t.getTypeArguments().size());
            for (TypeMirror tm : t.getTypeArguments()) {
                newArgs.add(visit(tm, p));
            }
            
            TypeMirror enclosing = t.getEnclosingType();
            if (enclosing != null) {
                enclosing = visit(enclosing, p);
            }
            
            return p.getTypes().getDeclaredType(
                (DeclaredType)enclosing,
                (TypeElement)t.asElement(), 
                newArgs.toArray(new TypeMirror[0]));
        }

        @Override
        public TypeMirror visitTypeVariable(TypeVariable t, CompilationInfo p) {
            TypeMirror lb = t.getLowerBound() == null ? null : visit(t.getLowerBound(), p);
            TypeMirror ub = t.getUpperBound() == null ? null : visit(t.getUpperBound(), p);
            if (ub.getKind() == TypeKind.DECLARED) {
                DeclaredType dt = (DeclaredType)ub;
                TypeElement tel = (TypeElement)dt.asElement();
                if (tel.getQualifiedName().contentEquals("java.lang.Object")) { // NOI18N
                    ub = null;
                } else if (tel.getSimpleName().length() == 0) {
                    ub = null;
                }
            }
            return p.getTypes().getWildcardType(ub, lb);
        }
    }
    
    /**
     * Updates the controller with the component.
     * If a field with the 'id' name does not exist, it creates the field as private,
     * annotated with @FXML. 
     * @param decl 
     */
    private void syncComponentBinding(FxInstance decl) {
        String id = decl.getId();
        TypeElement declType = getInstanceType(decl);
        TypeMirror fieldType = generatedFields.get(id);
        if (fieldType != null) {
            return;
        }
        
        VariableTree vt = fields.get(id);
        if (vt == null) {
            if (declType == null) {
                return;
            }
            defineNewField(id, wcopy.getTreeMaker().Type(
                    eraseFieldTypeParameters(declType.asType(), wcopy)));
            generatedFields.put(id, declType.asType());
            return;
        }

        // field exists, check its type first
        TreePath varPath = new TreePath(getControllerPath(), vt);
        VariableElement e = (VariableElement) wcopy.getTrees().getElement(varPath);
        if (e == null) {
            throw new IllegalStateException();
        }
        if (declType != null) {
            if (!wcopy.getTypes().isAssignable(
                    wcopy.getTypes().erasure(declType.asType()), 
                    wcopy.getTypes().erasure(e.asType()))) {
                // the field's type does not match. Consistency of FXML vs. controller is necessary, so 
                // we change field's type even though it may produce a compiler error.
                wcopy.rewrite(vt.getType(), wcopy.getTreeMaker().Type(
                        eraseFieldTypeParameters(declType.asType(), wcopy)));
            }
        }
        // annotation and visibility. If not public, add @FXML annotation
        if (!FxClassUtils.isFxmlAccessible(e)) {
            wcopy.rewrite(vt.getModifiers(), wcopy.getTreeMaker().addModifiersAnnotation(
                    vt.getModifiers(), fxmlAnnotationTree));
        }
        mappedTrees.add(vt);
        // prevent further changes to the field
        TypeMirror v;
        if (declType != null) {
            v = declType.asType();
        } else {
            v = e.asType();
        }
        generatedFields.put(id, v);
    }
    
    private void defineNewField(String name, Tree typeTree) {
        TreeMaker make = wcopy.getTreeMaker();
        
        VariableTree newVar = make.Variable(
                make.Modifiers(Collections.singleton(Modifier.PRIVATE),
                    Collections.singletonList(fxmlAnnotationTree)
                ),
                name, typeTree, null);
        
        controllerClass = GeneratorUtilities.get(wcopy).insertClassMember(controllerClass, newVar);
    }
    
    private class UpdatingVisitor extends FxNodeVisitor.ModelTraversal {

        @Override
        public void visitInclude(FxInclude decl) {
            if (decl.getId() != null && Utilities.isJavaIdentifier(decl.getId())) {
                // check that the component is not defined 
                if (decl.getRoot().getInstance(decl.getId()) == decl) {
                    syncComponentBinding(decl);
                }
            }
            super.visitInclude(decl); 
        }

        
        @Override
        public void visitBaseInstance(FxInstance decl) {
            if (decl.getId() != null && Utilities.isJavaIdentifier(decl.getId())) {
                // check that the component is not defined 
                if (decl.getRoot().getInstance(decl.getId()) == decl) {
                    syncComponentBinding(decl);
                }
            }
            super.visitBaseInstance(decl);
        }

        @Override
        public void visitEvent(EventHandler eh) {
            if (!eh.isScript()) {
                syncEventHandler(eh);
            }
            super.visitEvent(eh);
        }
        
    }
    
    private AnnotationTree findFxmlAnnotation(ModifiersTree modTree) {
        for (AnnotationTree annTree : modTree.getAnnotations()) {
            TreePath tp = new TreePath(new TreePath(wcopy.getCompilationUnit()), annTree.getAnnotationType());
            Element  e  = wcopy.getTrees().getElement(tp);
            if (fxmlAnnotationType.equals(e)) {
                return annTree;
            }
        }
        return null;
    }

    private void cleanField(VariableTree mt) {
        AnnotationTree t = findFxmlAnnotation(mt.getModifiers());
        if (t == null) {
            return;
        }
        if (!isUsed(mt, mt.getModifiers())) {
            controllerClass = wcopy.getTreeMaker().removeClassMember(controllerClass, mt);
            return;
        }
        wcopy.rewrite(
                mt.getModifiers(), 
                wcopy.getTreeMaker().removeModifiersAnnotation(mt.getModifiers(), t)
        );
    }
    
    private void cleanMethod(MethodTree mt) {
        AnnotationTree t = findFxmlAnnotation(mt.getModifiers());
        if (t == null) {
            return;
        }
        if (!isUsed(mt, mt.getModifiers())) {
            // must have empty body, I don't want to destroy user code:
            if (mt.getBody().getStatements().isEmpty()) {
                controllerClass = wcopy.getTreeMaker().removeClassMember(controllerClass, mt);
                return;
            }
        }
        wcopy.rewrite(
            mt.getModifiers(), 
            wcopy.getTreeMaker().removeModifiersAnnotation(mt.getModifiers(), t)
        );
    }
    
    /**
     * Goes through members not mapped, but bearing @FXML, and tries to
     * either remove them or de-annotate.
     */
    private void cleanUnused() {
        for (Tree t : origController.getMembers()) {
            // skip members, which are mapped
            if (mappedTrees.contains(t)) {
                continue;
            }
            switch (t.getKind()) {
                case METHOD:
                    cleanMethod((MethodTree)t);
                    break;
                case VARIABLE:
                    cleanField((VariableTree)t);
                    break;
            }
        }
    }
    
    private boolean isUsed(Tree memberTree, ModifiersTree mods) {
        if (!mods.getFlags().contains(Modifier.PRIVATE)) {
            // I don't want to search the world, better to ignore
            return true;
        }
        TreePath t = new TreePath(getControllerPath(), memberTree);
        TreePathHandle hnd = TreePathHandle.create(t, wcopy);
        WhereUsedQuery a = new WhereUsedQuery(Lookups.fixed(hnd));
        RefactoringSession session = RefactoringSession.create("Find usages");
        a.prepare(session);
        boolean used = !session.getRefactoringElements().isEmpty();
        session.finished();
        
        return used;
    }
    
    /**
     * Generates controller attribute into the FXML file, if it is missing
     */
    static void generateControllerAttribute(final Source s, final String className) throws ParseException {
        final int[] positions = new int[2];
        
        ParserManager.parse(Collections.singleton(s), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                final FxmlParserResult fxResult = FxmlParserResult.get(resultIterator.getParserResult());
                FxModel model = fxResult.getSourceModel();
                FxNode root = fxResult.getSourceModel().getRootComponent();
                
                if (root == null) {
                    return;
                }
                TextPositions poss = fxResult.getTreeUtilities().positions(root);
                positions[0] = poss.getStart();
                positions[1] = poss.getContentStart();
                
                Document d = s.getDocument(true);
                // resolve identifier:
                final ClasspathInfo cpInfo = ClasspathInfo.create(d);
                
                final String simpleName;
                
                int dot = className.lastIndexOf('.');
                simpleName = className.substring(dot + 1);
                
                final String[] sourceName = new String[1];
                
                class UT extends UserTask implements ClasspathInfoProvider {

                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationInfo ci = CompilationInfo.get(resultIterator.getParserResult());
                        
                        Set<String> classes =  fxResult.resolveClassName(ci, simpleName);
                        if (classes == null || classes.size() != 1) {
                            sourceName[0] = className;
                        } else {
                            String s = classes.iterator().next();
                            if (s.equals(className)) {
                                sourceName[0] = simpleName;
                            } else {
                                sourceName[0] = className;
                            }
                        }
                    }

                    @Override
                    public ClasspathInfo getClasspathInfo() {
                        return cpInfo;
                    }
                    
                }
                
                ParserManager.parse("text/x-java", new UT());
                
                BaseDocument bd = (BaseDocument)d;
                bd.extWriteLock();
                try {
                    if (model.getController() != null) {
                        // easier, we can get positional info from the text:
                        int[] positions = fxResult.getTreeUtilities().findAttributePos(
                                root, 
                                JavaFXEditorUtils.FXML_FX_NAMESPACE, 
                                "controller", true);
                        d.remove(positions[0], positions[1] - positions[0]);
                        d.insertString(positions[0], sourceName[0], null);
                    } else {
                        addControllerAttribute(bd, positions[0], positions[1], sourceName[0]);
                    }
                } finally {
                    bd.extWriteUnlock();
                }
            }
        });
    }
    
    static void addControllerAttribute(BaseDocument doc, int from, int to, String controllerClassName) throws BadLocationException {
        doc.extWriteLock();
        try {
            TokenHierarchy h = TokenHierarchy.get(doc);

            TokenSequence<XMLTokenId> seq = h.tokenSequence();
            seq.move(from);

            int lastWsPos = -1;
            int start = -1;

            while (seq.moveNext()) {
                Token<XMLTokenId> t = seq.token();
                XMLTokenId id = t.id();

                if (seq.offset() >= to) {
                    start = seq.offset();
                    break;
                }
                if (id == XMLTokenId.WS) {
                    lastWsPos = seq.offset() + 1;
                } else if (id == XMLTokenId.TAG) {
                    if (t.text().length() > 0) {
                        if (t.text().charAt(t.text().length() - 1) == '>') {
                            start = seq.offset();
                            break;
                        }
                    }
                } else {
                    lastWsPos = -1;
                }
            }

            String toInsert = "fx:controller=\"" + controllerClassName + "\"";
            if (lastWsPos != -1) {
                start = lastWsPos;
            } else {
                toInsert = " " + toInsert;
            }
            doc.insertString(start, toInsert, null);
        } finally {
            doc.extWriteUnlock();
        }
    }
}
