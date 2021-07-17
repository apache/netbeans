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
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.editor.java.TreeShims;
import org.netbeans.modules.java.editor.overridden.ComputeOverriders;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.modules.java.source.builder.TreeFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_AddSealedForClass", description = "#DESC_AddSealedForClass", category = "suggestions", minSourceVersion = "15", hintKind = Hint.Kind.ACTION, severity = Severity.HINT)
@Messages({
    "DN_AddSealedForClass=Can Be Sealed Type",
    "DESC_AddSealedForClass=This class can be set to sealed type with permiting only current sub classes."
})
public class AddSealedForClass {

    @TriggerTreeKind({Tree.Kind.CLASS, Tree.Kind.INTERFACE})
    @Messages("ERR_AddSealedForClass=Can use sealed")
    public static ErrorDescription computeWarning(HintContext context) {
        TreePath tp = context.getPath();
        ClassTree cls = (ClassTree) tp.getLeaf();
        CompilationInfo info = context.getInfo();
        TypeElement typeElement = (TypeElement) info.getTrees().getElement(tp);

        if (typeElement == null || typeElement.getModifiers().contains(Modifier.FINAL) || typeElement.getModifiers().contains(TreeFactory.getSealed())) {
            return null;
        }
//        if(!Boolean.getBoolean("java.sealed.hint.enabled")){
//            return null;
//        }
        SourcePositions sourcePositions = info.getTrees().getSourcePositions();
        long startPos = sourcePositions.getStartPosition(tp.getCompilationUnit(), cls);
        if (startPos > Integer.MAX_VALUE) {
            return null;
        }
        int[] bodySpan = info.getTreeUtilities().findBodySpan(cls);
        if (bodySpan == null || bodySpan[0] <= startPos) {
            return null;
        }
        Element outer = typeElement.getEnclosingElement();
        // do not offer the hint for non-static inner classes. Permit for classes nested into itnerface - no enclosing instance
        if (outer != null && outer.getKind() != ElementKind.PACKAGE && outer.getKind() != ElementKind.INTERFACE) {
            if (outer.getKind() != ElementKind.CLASS && outer.getKind() != ElementKind.ENUM) {
                return null;
            }
            if (!typeElement.getModifiers().contains(Modifier.STATIC)) {
                return null;
            }
        }

        ClassPath cp = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
        FileObject root = cp.findOwnerRoot(info.getFileObject());
        if (root == null) { //File not part of any project
            return null;
        }
        
        AtomicBoolean cancel = new AtomicBoolean();
        cancel.set(false);
        Map<ElementHandle<? extends Element>, List<ElementDescription>> subClasses = new ComputeOverriders(cancel).processOneClass(info, null, null, false,typeElement.getQualifiedName().toString());
        if(subClasses.isEmpty())return null;
        Iterator<ElementHandle<? extends Element>> iterator = subClasses.keySet().iterator();
        ElementHandle currentClassHandle=iterator.next();
        List<ElementDescription> currentSubClasses=subClasses.get(currentClassHandle);
        //Set<ElementDescription> subClassesToRemove = new HashSet<>();
        
        PackageElement currentPackageElement = (PackageElement) info.getElementUtilities().outermostTypeElement(typeElement).getEnclosingElement();
        boolean isModule=!info.getElements().getModuleOf(typeElement).isUnnamed();
        if(!isModule){
            for (int i = 0; i < currentSubClasses.size(); i++) {
                String currentSubClass = currentSubClasses.get(i).getHandle().getQualifiedName();
                if(!currentSubClass.substring(0,currentSubClass.lastIndexOf(".")).equals(currentPackageElement.getQualifiedName().toString())){
                    return null;
                }
            }
        }
        if (currentSubClasses.isEmpty()) {
            return null;
        }
        
        Fix fix = new FixImpl(context.getInfo(), context.getPath(), currentSubClasses).toEditorFix();
        return ErrorDescriptionFactory.forName(context, context.getPath(), Bundle.ERR_AddSealedForClass(), fix);
    }

    private static final class FixImpl extends JavaFix {

        List<ElementDescription> currentSubClasses;

        public FixImpl(CompilationInfo info, TreePath tp, List<ElementDescription> currentSubClasses) {
            super(info, tp);
            this.currentSubClasses = currentSubClasses;
        }

        @Override
        @Messages("FIX_AddSealedForClass=Make Sealed type")
        protected String getText() {
            return Bundle.FIX_AddSealedForClass();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            Tree oldTree = ctx.getPath().getLeaf();
            TreePath tp = ctx.getPath();
            ClassTree cls = (ClassTree) tp.getLeaf();

            if (!(oldTree instanceof ClassTree)) {
                return;
            }
            TreeMaker make = ctx.getWorkingCopy().getTreeMaker();
            ClassTree oldClassTree = (ClassTree) oldTree;
            oldTree.getKind();
            ModifiersTree oldModifiers = oldClassTree.getModifiers();
            Set<Modifier> newModifiers = new HashSet<>();

            newModifiers.addAll(oldModifiers.getFlags());
            newModifiers.remove(TreeFactory.getNonSealed());
            Modifier sealedMod = TreeFactory.getSealed();
            if(sealedMod!=null)
                newModifiers.add(sealedMod);

            ModifiersTree newModifiersTree = make.Modifiers(newModifiers);
            List<Tree> permits = new ArrayList<>();

            for (int i = 0; i < currentSubClasses.size(); i++) {
                String displayName = currentSubClasses.get(i).getHandle().getQualifiedName();
                permits.add(make.Identifier(displayName));
            }
            ClassTree sealedClass=null;
            if (cls.getKind().equals(Tree.Kind.CLASS)) {
                sealedClass = make.Class(newModifiersTree, oldClassTree.getSimpleName(), oldClassTree.getTypeParameters(), oldClassTree.getExtendsClause(), oldClassTree.getImplementsClause(), permits, oldClassTree.getMembers());
            } else if (cls.getKind().equals(Tree.Kind.INTERFACE)) {
                sealedClass = make.Interface(newModifiersTree, oldClassTree.getSimpleName(), oldClassTree.getTypeParameters(), oldClassTree.getImplementsClause(), permits, oldClassTree.getMembers());
            }
            assert sealedClass!=null;
            ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), sealedClass);
        }
    }

}
