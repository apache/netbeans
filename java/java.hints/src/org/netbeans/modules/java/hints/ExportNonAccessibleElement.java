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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.swing.JComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class ExportNonAccessibleElement extends AbstractHint 
implements ElementVisitor<Boolean,Void>, TypeVisitor<Boolean,Void> {
    private static final Set<Kind> DECLARATION = EnumSet.of(Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.METHOD, Kind.VARIABLE);
    private transient volatile boolean stop;
    
    /** Creates a new instance of AddOverrideAnnotation */
    public ExportNonAccessibleElement() {
        super( true, true, AbstractHint.HintSeverity.WARNING, "NonPublicExported" );
    }
    
    public Set<Kind> getTreeKinds() {
        return DECLARATION;
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        stop = false;
        Element e = compilationInfo.getTrees().getElement(treePath);
        if (e == null) {
            return null;
        }
        Boolean b = e.accept(this, null);

        if (b) {
            Element parent = e;
            for (;;) {
                if (stop) {
                    return null;
                }

                if (parent == null || parent.getKind() == ElementKind.PACKAGE) {
                    break;
                }
                if (!parent.getModifiers().contains(Modifier.PUBLIC) && !parent.getModifiers().contains(Modifier.PROTECTED)) {
                    return null;
                }
                parent = parent.getEnclosingElement();
            }

            //#124456: disabling the fix:
//            List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
//                "MSG_ExportNonAccessibleElementMakeNonVisible", // NOI18N
//                TreePathHandle.create(e, compilationInfo), 
//                compilationInfo.getFileObject()
//            ));

            int[] span = null;

            switch (treePath.getLeaf().getKind()) {
                case METHOD: span = compilationInfo.getTreeUtilities().findNameSpan((MethodTree) treePath.getLeaf()); break;
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    span = compilationInfo.getTreeUtilities().findNameSpan((ClassTree) treePath.getLeaf()); break;
                case VARIABLE: span = compilationInfo.getTreeUtilities().findNameSpan((VariableTree) treePath.getLeaf()); break;
            }

            if (span != null) {
                ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                        getSeverity().toEditorSeverity(),
                        NbBundle.getMessage(ExportNonAccessibleElement.class, "MSG_ExportNonAccessibleElement"),
//                        fixes,
                        compilationInfo.getFileObject(),
                        span[0],
                        span[1]
                        );

                return Collections.singletonList(ed);
            }
        }
        
        return null;
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MissingHashCode.class, "MSG_ExportNonAccessibleElement");
    }

    public String getDescription() {
        return NbBundle.getMessage(MissingHashCode.class, "HINT_ExportNonAccessibleElement");
    }

    public void cancel() {
        stop = true;
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    
    public Boolean visit(Element arg0, Void arg1) {
        // no need for hint
        return false;
    }

    public Boolean visit(Element arg0) {
        // no need for hint
        return false;
    }

    public Boolean visitModule(ModuleElement e, Void p) {
        return false;
    }

    public Boolean visitPackage(PackageElement arg0, Void arg1) {
        return false;
    }

    public Boolean visitType(TypeElement arg0, Void arg1) {
        for (TypeParameterElement e : arg0.getTypeParameters()) {
            if (stop) {
                return false;
            }
            
            for (TypeMirror b : e.getBounds()) {
                if (stop) {
                    return false;
                }
                
                if (b.accept(this, arg1)) {
                    return true;
                }
            }
        }

        TypeMirror superclass = arg0.getSuperclass();
        if (superclass.getKind() == TypeKind.DECLARED) {
            if (!((DeclaredType) superclass).asElement().getKind().isInterface()) {
                return false;
            }
        }

        return superclass.accept(this, arg1);
    }

    public Boolean visitVariable(VariableElement field, Void arg1) {
        if (!isVisible(field)) {
            // no need for hint
            return false;
        }
        return field.asType().accept(this, arg1);
    }

    public Boolean visitExecutable(ExecutableElement method, Void nothing) {
        if (!isVisible(method)) {
            // ok
            return false;
        }
        for (VariableElement v : method.getParameters()) {
            if (stop) {
                return false;
            }
            
            if (v.asType().accept(this, nothing)) {
                return true;
            }
        }
        return method.getReturnType().accept(this, nothing);
    }

    public Boolean visitTypeParameter(TypeParameterElement arg0, Void arg1) {
        return false;
    }

    public Boolean visitUnknown(Element arg0, Void arg1) {
        // ok, probably no need for hint
        return false;
    }


    public Boolean visit(TypeMirror arg0, Void arg1) {
        return false;
    }

    public Boolean visit(TypeMirror arg0) {
        return false;
    }

    public Boolean visitPrimitive(PrimitiveType arg0, Void arg1) {
        return false;
    }

    public Boolean visitNull(NullType arg0, Void arg1) {
        return false;
    }

    public Boolean visitArray(ArrayType arg0, Void arg1) {
        return arg0.getComponentType().accept(this, arg1);
    }

    public Boolean visitDeclared(DeclaredType arg0, Void arg1) {
        if (!isVisible(arg0.asElement())) {
            return true;
        }
        for (TypeMirror t : arg0.getTypeArguments()) {
            if (stop) {
                return false;
            }
            
            if (t.accept(this, arg1)) {
                return true;
            }
        }
        return arg0.getEnclosingType().accept(this, arg1);
    }

    public Boolean visitError(ErrorType arg0, Void arg1) {
        // no hint
        return false;
    }

    public Boolean visitTypeVariable(TypeVariable arg0, Void arg1) {
        return arg0.getLowerBound().accept(this, arg1) && arg0.getUpperBound().accept(this, arg1);
    }

    public Boolean visitWildcard(WildcardType wild, Void arg1) {
        TypeMirror eb = wild.getExtendsBound();
        TypeMirror sb = wild.getSuperBound();
        return (eb != null && eb.accept(this, arg1)) ||
               (sb != null && sb.accept(this, arg1));
    }

    public Boolean visitExecutable(ExecutableType arg0, Void arg1) {
        return false;
    }

    public Boolean visitNoType(NoType arg0, Void arg1) {
        return false;
    }

    public Boolean visitUnknown(TypeMirror arg0, Void arg1) {
        return false;
    }

    @Override
    public Boolean visitIntersection(IntersectionType t, Void p) {
        return false;
    }

    private boolean isVisible(Element... arr) {
        return isVisible(Arrays.asList(arr));
    }
    private boolean isVisible(Collection<? extends Element> arr) {
        for (Element el : arr) {
            if (stop) {
                return false;
            }
            
            if (el == null) continue; //XXX: function types
            
            if (el.getModifiers().contains(Modifier.PUBLIC) || 
                (el.getModifiers().contains(Modifier.PROTECTED) && //#175818: protected elements of final classes are effectivelly-package private:
                 el.getEnclosingElement() != null &&
                 (el.getEnclosingElement().getKind().isClass() || el.getEnclosingElement().getKind().isInterface()) &&
                 !el.getEnclosingElement().getModifiers().contains(Modifier.FINAL))
            ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visitUnion(UnionType tm, Void p) {
        for (TypeMirror t : tm.getAlternatives()) {
            if (stop) {
                return false;
            }
            
            if (t.accept(this, p)) {
                return true;
            }
        }
        return false;
    }
          
    private static final class FixImpl implements Fix {
        private TreePathHandle handle;
        private FileObject file;
        private String msg;
        
        public FixImpl(String type, TreePathHandle handle, FileObject file) {
            this.handle = handle;
            this.file = file;
            this.msg = type;
        }
        
        public String getText() {
            return NbBundle.getMessage(MissingHashCode.class, msg);
        }
        
        public ChangeInfo implement() throws IOException {
            ModificationResult result = JavaSource.forFileObject(file).runModificationTask(new Task<WorkingCopy>() {

                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    Element e = handle.resolveElement(wc);
                    
                    Tree t = wc.getTrees().getTree(e);
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                        ClassTree ct = (ClassTree)t;
                        Set<Modifier> flags = EnumSet.noneOf(Modifier.class);
                        flags.addAll(ct.getModifiers().getFlags());
                        flags.remove(Modifier.PUBLIC);
                        flags.remove(Modifier.PROTECTED);
                        ModifiersTree mt = wc.getTreeMaker().Modifiers(flags, ct.getModifiers().getAnnotations());
                        wc.rewrite(ct.getModifiers(), mt);
                        return;
                    }
                    if (t.getKind() == Kind.METHOD) {
                        MethodTree mt = (MethodTree)t;
                        Set<Modifier> flags = EnumSet.noneOf(Modifier.class);
                        flags.addAll(mt.getModifiers().getFlags());
                        flags.remove(Modifier.PUBLIC);
                        flags.remove(Modifier.PROTECTED);
                        ModifiersTree modt = wc.getTreeMaker().Modifiers(flags, mt.getModifiers().getAnnotations());
                        wc.rewrite(mt.getModifiers(), modt);
                        return;
                    }
                    if (t.getKind() == Kind.VARIABLE) {
                        VariableTree vt = (VariableTree)t;
                        Set<Modifier> flags = EnumSet.noneOf(Modifier.class);
                        flags.addAll(vt.getModifiers().getFlags());
                        flags.remove(Modifier.PUBLIC);
                        flags.remove(Modifier.PROTECTED);
                        ModifiersTree modt = wc.getTreeMaker().Modifiers(flags, vt.getModifiers().getAnnotations());
                        wc.rewrite(vt.getModifiers(), modt);
                        return;
                    }
                }
            });

            result.commit();
            
            return null;
        }
        
        @Override
        public String toString() {
            return "FixExportNonAccessibleElement"; // NOI18N
        }
    }
}
