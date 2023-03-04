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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.UnionType;
import javax.swing.text.StyledDocument;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.editor.overridden.AnnotationType;
import org.netbeans.modules.java.editor.overridden.ComputeOverriding;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import static org.netbeans.modules.java.hints.errors.Utilities.isValidType;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class UncaughtException implements ErrorRule<Void> {
    
    private static final Object KEY_HANDLED_EXCEPTIONS = new Object();

    /*only for tests, currently:*/
    static boolean allowMagicSurround = false;
    
    public UncaughtException() {}

    private List<? extends TypeMirror> findUncaughtExceptions(CompilationInfo info, TreePath path, List<? extends TypeMirror> exceptions) {
        List<TypeMirror> result = new ArrayList<TypeMirror>();
        
        result.addAll(exceptions);
        
        Tree lastTree = null;
        
        while (path != null) {
            Tree currentTree = path.getLeaf();

            if (currentTree.getKind() == Tree.Kind.METHOD) {
                TypeMirror tm = info.getTrees().getTypeMirror(path);
                if (tm != null && tm.getKind() == TypeKind.EXECUTABLE) {
                    for (TypeMirror mirr : ((ExecutableType) tm).getThrownTypes()) {
                        for (Iterator<TypeMirror> it = result.iterator(); it.hasNext();)
                            if (info.getTypes().isSameType(it.next(), mirr))
                                it.remove();
                    }
                    break;
                }
            }            
            
            if (currentTree.getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
                // no checked exceptions can be thrown out of Lambda, #243106
                break;
            }
            
            if (currentTree.getKind() == Kind.TRY) {
                TryTree tt = (TryTree) currentTree;
                
                if (tt.getBlock() == lastTree) {
                    for (CatchTree c : tt.getCatches()) {
                        TreePath catchPath = new TreePath(new TreePath(path, c), c.getParameter());
                        VariableElement variable = (VariableElement) info.getTrees().getElement(catchPath);
                        if (variable == null) {
                            continue;
                        }
                        TypeMirror variableType = variable.asType();
                        if (variableType.getKind() == TypeKind.UNION) {
                            result.removeAll(((UnionType)variableType).getAlternatives());
                        } else {
                            result.remove(variableType);
                        }
                    }
                }
            }
            
            lastTree = path.getLeaf();
            path = path.getParentPath();
        }
        
        List<TypeMirror> filtered = new ArrayList<>();
        
        OUTER: for (Iterator<TypeMirror> sourceIt = result.iterator(); sourceIt.hasNext(); ) {
            TypeMirror sourceType = sourceIt.next();
            
            for (Iterator<TypeMirror> filteredIt = filtered.iterator(); filteredIt.hasNext(); ) {
                TypeMirror filteredType = filteredIt.next();
                
                if (info.getTypes().isSubtype(sourceType, filteredType)) {
                    sourceIt.remove();
                    continue OUTER;
                }
                
                if (info.getTypes().isSubtype(filteredType, sourceType)) {
                    filteredIt.remove();
                    break;
                }
            }
            
            filtered.add(sourceType);
        }
        
        return filtered;
    }
    
    static final String ERR_IMPLICIT_CLOSE = "compiler.err.unreported.exception.implicit.close"; // NOI18N
    static final String ERR_UNREPORTED = "compiler.err.unreported.exception.need.to.catch.or.throw"; // NOI18N
    
    private static final Set<String> ERRCODES = new HashSet<String>(Arrays.asList(
        new String[]{
            ERR_UNREPORTED,
            ERR_IMPLICIT_CLOSE
        }));
    
    public Set<String> getCodes() {
        return ERRCODES;
    }
    
    @SuppressWarnings("fallthrough")
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        List<? extends TypeMirror> uncaught = null;
        boolean disableSurroundWithTryCatch = false;
        Element el;
        
        Set<TypeMirror> alreadyHandled = null;
        try {
            int lineNumber = NbDocument.findLineNumber((StyledDocument)info.getDocument(), info.getSnapshot().getOriginalOffset(offset));
            Map<Integer, Set<TypeMirror>> alreadyHandledMap = (Map<Integer, Set<TypeMirror>>) info.getCachedValue(KEY_HANDLED_EXCEPTIONS);
            if (alreadyHandledMap == null) {
                alreadyHandledMap = new HashMap<>();
                info.putCachedValue(KEY_HANDLED_EXCEPTIONS, alreadyHandledMap, CompilationInfo.CacheClearPolicy.ON_TASK_END);
            }
            alreadyHandled = alreadyHandledMap.get(lineNumber);
            if (alreadyHandled == null) {
                alreadyHandled = new HashSet<>();
                alreadyHandledMap.put(lineNumber, alreadyHandled);
            }
        } catch (IOException ex) {
        }        
        
        if (ERR_IMPLICIT_CLOSE.equals(diagnosticKey) && path.getLeaf().getKind() == Tree.Kind.VARIABLE) {
            // JDK 8 : warn about implicit close. The diagnostic is reported on the resource declaration. The resource SHOULD
            // implement AutoCloseable, but may declare different exception list on its close() method
            TypeMirror varType = info.getTrees().getTypeMirror(path);
            if (isValidType(varType) && varType.getKind() == TypeKind.DECLARED) {
                DeclaredType decl = (DeclaredType)varType;
                Element jdkClose = info.getElementUtilities().findElement("java.lang.AutoCloseable.close()"); // NOI18N
                if (jdkClose != null && jdkClose.getKind() == ElementKind.METHOD) { 
                    Element definedClose = info.getElementUtilities().getImplementationOf((ExecutableElement)jdkClose, (TypeElement)decl.asElement());
                    if (definedClose != null) {
                        TypeMirror varClose = info.getTypes().asMemberOf(decl, definedClose);
                        if (isValidType(varClose) && varClose.getKind() == TypeKind.EXECUTABLE) {
                            ExecutableType etype = (ExecutableType)varClose;
                            uncaught = new ArrayList(etype.getThrownTypes());
                        }
                    }
                }
            }
        } else {
        OUTTER: while (path != null) {
            Tree leaf = path.getLeaf();
            
            switch (leaf.getKind()) {
                case METHOD_INVOCATION:
                    //check for super/this constructor call (and disable surround with try-catch):
                    MethodInvocationTree mit = (MethodInvocationTree) leaf;
                    
                    if (mit.getMethodSelect().getKind() == Kind.IDENTIFIER) {
                        String ident = ((IdentifierTree) mit.getMethodSelect()).getName().toString();
                        
                        if ("super".equals(ident) || "this".equals(ident)) {
                            Element element = info.getTrees().getElement(path);
                            
                            disableSurroundWithTryCatch = element != null && element.getKind() == ElementKind.CONSTRUCTOR;
                        }
                    }
                case NEW_CLASS:
                    el = info.getTrees().getElement(path);
		    
		    //IZ 95535 -- dont't offer surround with T-C for fields
		    if(!isInsideMethodOrInitializer(path))
			disableSurroundWithTryCatch = true;
		    
		    if(isThisParameter(path)) {
			disableSurroundWithTryCatch = el != null && (el.getKind() == ElementKind.CONSTRUCTOR || el.getKind() == ElementKind.METHOD);
		    }
		    
                    if (el != null && EXECUTABLE_ELEMENTS.contains(el.getKind())) {
			TypeMirror uncaughtException;
			if(leaf.getKind() == Kind.NEW_CLASS)
			    uncaughtException = info.getTrees().getTypeMirror(new TreePath(path, ((NewClassTree) leaf).getIdentifier()));
			else
			    uncaughtException = info.getTrees().getTypeMirror(new TreePath(path, ((MethodInvocationTree) leaf).getMethodSelect()));
			
			if(uncaughtException != null && uncaughtException.getKind() == TypeKind.EXECUTABLE)
			    uncaught = ((ExecutableType) uncaughtException).getThrownTypes();
			else
			    uncaught = ((ExecutableElement) el).getThrownTypes();
                    }
                    break OUTTER;
                case THROW:
                    TypeMirror uncaughtException = info.getTrees().getTypeMirror(new TreePath(path, ((ThrowTree) leaf).getExpression()));
                    uncaught = uncaughtException.getKind() != TypeKind.UNION ? Collections.singletonList(uncaughtException) : ((UnionType) uncaughtException).getAlternatives();
                    break OUTTER;
            }
            
            path = path.getParentPath();
        }
        }
        
        if (uncaught != null) {
            uncaught = findUncaughtExceptions(info, path, uncaught);

            uncaught.removeAll(alreadyHandled);
            alreadyHandled.addAll(uncaught);
            
            TreePath pathRec = path;
            
            while (pathRec != null && pathRec.getLeaf().getKind() != Kind.METHOD) {
                if (pathRec.getLeaf().getKind() == Kind.LAMBDA_EXPRESSION) {
                    pathRec = null;
                    break;
                }
                pathRec = pathRec.getParentPath();
            }
            
            TreePath in = path;
            Tree inLast = null;
            boolean inResourceSection = false;
            
            LOOK_FOR_TWR: while (in != null) {
                switch (in.getLeaf().getKind()) {
                    case TRY:
                        if (((TryTree) in.getLeaf()).getResources().contains(inLast)) {
                            inResourceSection = true;
                        }
                    case METHOD: case ANNOTATION_TYPE: case CLASS:
                    case ENUM: case INTERFACE: case LAMBDA_EXPRESSION:
                        break LOOK_FOR_TWR;
                }
                inLast = in.getLeaf();
                in = in.getParentPath();
            }
            
            ExecutableElement method = pathRec != null ? (ExecutableElement) info.getTrees().getElement(pathRec)  : null;
            
            if (method != null) {
                //if the method header is inside a guarded block, do nothing:
                if (!org.netbeans.modules.java.hints.errors.Utilities.isMethodHeaderInsideGuardedBlock(info, (MethodTree) pathRec.getLeaf())) {
                    List<ElementDescription> eds = new LinkedList<ElementDescription>();
                    TypeElement enclosingType = (TypeElement) method.getEnclosingElement();
                    AnnotationType at = ComputeOverriding.detectOverrides(info, enclosingType, method,eds);
                    List<TypeMirror> declaredThrows = null;
                    
                    if (at != null) {
                        declaredThrows = new LinkedList<TypeMirror>();

                        for (ElementDescription ed : eds) {
                            ExecutableElement ee = (ExecutableElement) ed.getHandle().resolve(info);
                            TypeMirror eType = info.getTypes().asMemberOf((DeclaredType) enclosingType.asType(), ee);
                            if (eType.getKind() != TypeKind.EXECUTABLE) {
                                continue;
                            }
                            ExecutableType et = (ExecutableType)eType;
                            List<TypeMirror> thisDeclaredThrows = new LinkedList<TypeMirror>(et.getThrownTypes());
                            
                            if (!thisDeclaredThrows.isEmpty()) {
                                for (Iterator<TypeMirror> dt = declaredThrows.iterator(); dt.hasNext();) {
                                    for (Iterator<TypeMirror> tdt = thisDeclaredThrows.iterator(); tdt.hasNext();) {
                                        TypeMirror dtNext = dt.next();
                                        TypeMirror tdtNext = tdt.next();

                                        if (info.getTypes().isSubtype(tdtNext, dtNext)) {
                                            tdt.remove();
                                            continue;
                                        }

                                        if (info.getTypes().isSubtype(dtNext, tdtNext)) {
                                            dt.remove();
                                            continue;
                                        }

                                        tdt.remove();
                                        dt.remove();
                                    }
                                }
                            }
                            declaredThrows.addAll(thisDeclaredThrows);
                        }
                    }

                    for (TypeMirror tm : uncaught) {
                        if (declaredThrows != null) {
                            boolean found = false;

                            for (TypeMirror decl : declaredThrows) {
                                if (info.getTypes().isSubtype(tm, decl)) {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                continue;
                            }
                        }
                        
                        if (tm.getKind() != TypeKind.ERROR) {
                            result.add(new AddThrowsClauseHintImpl(info, path, Utilities.getTypeName(info, tm, true).toString(), TypeMirrorHandle.create(tm), ElementHandle.create(method)).toEditorFix());
                        }
                    }
                }
            }
            
            if (!uncaught.isEmpty() && !disableSurroundWithTryCatch) {
                List<TypeMirrorHandle> thandles = new ArrayList<TypeMirrorHandle>();
                List<String> fqns = new ArrayList<String>();

                for (TypeMirror tm : uncaught) {
                    if (tm.getKind() != TypeKind.ERROR) {
                        thandles.add(TypeMirrorHandle.create(tm));
                        fqns.add(Utilities.getTypeName(info, tm, true).toString());
                    }
                }
                
                if (ErrorFixesFakeHint.enabled(ErrorFixesFakeHint.FixKind.SURROUND_WITH_TRY_CATCH)) {
                    TreePath tryTree = MagicSurroundWithTryCatchFix.enclosingTry(path);
                    if (tryTree != null) {
                        result.add(new AddCatchFix(info, tryTree, thandles).toEditorFix());
                    }
                    if (!inResourceSection) {
                        TreePathHandle tph = TreePathHandle.create(path, info);
                        result.add(new OrigSurroundWithTryCatchFix(tph, thandles, fqns).toEditorFix());
                        //#134408: "Surround Block with try-catch" is redundant when the block contains just a single statement
                        TreePath tp = findBlock(path);
                        boolean magic = tryTree == null || allowMagicSurround;
                        if(tp != null && tp.getLeaf().getKind() == Kind.BLOCK) {
                            magic &= ((BlockTree) tp.getLeaf()).getStatements().size() != 1;
                        }
                        if(magic)
                            result.add(new MagicSurroundWithTryCatchFix(tph, thandles, offset, method != null ? ElementHandle.create(method) : null, fqns).toEditorFix());
                    }
                }
            }
        }
        
        return result;
    }
    
    private TreePath findBlock(TreePath path) {
        while (path != null && path.getLeaf().getKind() != Kind.BLOCK) {
            path = path.getParentPath();
        }
        return path;
    }
    
    /**
     * Detects if we are parameter of this() or super() call
     * @return true if yes
     */ 
    private boolean isThisParameter(TreePath path) {
	//anonymous class must not be on the path to top
	while(!TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind()) && path.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
	    if (path.getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION) {
		MethodInvocationTree mi = (MethodInvocationTree) path.getParentPath().getLeaf();
		if(mi.getMethodSelect().getKind() == Kind.IDENTIFIER) {
		    String id = ((IdentifierTree) mi.getMethodSelect()).getName().toString();
		    if ("super".equals(id) || "this".equals(id))
			return true;
		}
	    }
	    path = path.getParentPath();
	}
	return false;
    }
    
    private static boolean isInsideMethodOrInitializer(TreePath tp) {
        while (tp != null) {
            if (tp.getLeaf().getKind() == Kind.METHOD || 
                    (tp.getLeaf().getKind() == Kind.BLOCK && TreeUtilities.CLASS_TREE_KINDS.contains(tp.getParentPath().getLeaf().getKind())))
                return true;
            
            tp = tp.getParentPath();
        }
        
        return false;
    }

    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return UncaughtException.class.getName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(UncaughtException.class, "DN_AddThrowsClauseAndSurround");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(UncaughtException.class, "DESC_AddThrowsClauseAndSurround");
    }
    
    private static final Set<ElementKind> EXECUTABLE_ELEMENTS = EnumSet.of(ElementKind.CONSTRUCTOR, ElementKind. METHOD);
    
    private static final class AddThrowsClauseHintImpl extends JavaFix {

        private String fqn;
        private TypeMirrorHandle thandle;
        private ElementHandle<ExecutableElement> method;
        
        public AddThrowsClauseHintImpl(CompilationInfo info, TreePath tp, String fqn, TypeMirrorHandle thandle, ElementHandle<ExecutableElement> method) {
            super(info, tp);
            this.fqn = fqn;
            this.thandle = thandle;
            this.method = method;
        }
        
        public String getText() {
            return NbBundle.getMessage(UncaughtException.class, "FIX_AddThrowsClause", new Object[]{String.valueOf(fqn)});
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            Tree tree = wc.getTrees().getTree(method.resolve(wc));

            if (tree == null) {
                Logger.getLogger(UncaughtException.class.getName()).log(Level.WARNING, "Cannot resolve Handle." +
                        "fqn: " + fqn +
                        "method: " + Arrays.asList(SourceUtils.getJVMSignature(method)).toString());
                return;
            }
            assert tree.getKind() == Kind.METHOD;

            MethodTree nue = wc.getTreeMaker().addMethodThrows((MethodTree) tree, (ExpressionTree) wc.getTreeMaker().Type(thandle.resolve(wc)));

            wc.rewrite(tree, nue);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AddThrowsClauseHintImpl other = (AddThrowsClauseHintImpl) obj;
            if (this.fqn == null || !this.fqn.equals(other.fqn)) {
                return false;
            }
            if (this.method != other.method && (this.method == null || !this.method.equals(other.method))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 13 * hash + (this.fqn != null ? this.fqn.hashCode() : 0);
            hash = 13 * hash + (this.method != null ? this.method.hashCode() : 0);
            return hash;
        }
        
    }
    
    static final Set<Kind> STATEMENT_KINDS;
    
    static {
        Set<Kind> kinds = new HashSet<Kind>();
        
        for (Kind k : Kind.values()) {
            Class c = k.asInterface();
            
            if (c != null && StatementTree.class.isAssignableFrom(c)) {
                kinds.add(k);
            }
        }
        
        STATEMENT_KINDS = Collections.unmodifiableSet(EnumSet.copyOf(kinds));
    }
    
}
