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
import org.netbeans.modules.java.editor.overridden.ComputeOverriders;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.modules.java.hints.TreeShims;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_AddSealedForClass", description = "#DESC_AddSealedForClass", category = "general", minSourceVersion = "15")
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
        if (!CompilerOptionsQuery.getOptions(context.getInfo().getFileObject()).getArguments().contains("--enable-preview"))
            return null;
        CompilationInfo info = context.getInfo();
        TypeElement typeElement = (TypeElement) info.getTrees().getElement(tp);

        if (typeElement == null || typeElement.getModifiers().contains(Modifier.FINAL) || typeElement.getModifiers().contains(TreeShims.getSealed())) {
            return null;
        }
        
        AtomicBoolean cancel = new AtomicBoolean();
        cancel.set(false);
        Map<ElementHandle<? extends Element>, List<ElementDescription>> subClasses = new ComputeOverriders(cancel).process(info, null, null, false);
        Iterator<ElementHandle<? extends Element>> iterator = subClasses.keySet().iterator();
        List<ElementDescription> currentSubClasses = new ArrayList<>();
        while (iterator.hasNext()) {
            ElementHandle eh = iterator.next();
            if (eh.getBinaryName().substring(eh.getQualifiedName().lastIndexOf(".") + 1).equals(cls.getSimpleName().toString())) {
                currentSubClasses = subClasses.get(eh);
                break;
            }
        }
       
        Set<ElementDescription> subClassesToRemove = new HashSet<>();
        for (int i = 0; i < currentSubClasses.size(); i++) {
            subClassesToRemove.addAll(subClasses.getOrDefault(currentSubClasses.get(i).getHandle(), new ArrayList<>()));
        }
        PackageElement currentPackageElement = (PackageElement) info.getElementUtilities().outermostTypeElement(typeElement).getEnclosingElement();
        boolean isModule=false;
        Element parentElement=currentPackageElement;
        while(parentElement!=null){
            if(parentElement.getKind().equals(ElementKind.MODULE) && !parentElement.getSimpleName().toString().equals("")){
                isModule=true;
                break;
            }
            parentElement=parentElement.getEnclosingElement();
        }
        if(!isModule){
            for (int i = 0; i < currentSubClasses.size(); i++) {
                String currentSubClass = currentSubClasses.get(i).getHandle().getQualifiedName();
                if(!currentSubClass.substring(0,currentSubClass.lastIndexOf(".")).equals(currentPackageElement.getQualifiedName().toString())){
                    return null;
                }
            }
        }
        for (int i = 0; i < currentSubClasses.size(); i++) {
            for (ElementDescription elementDescription : subClassesToRemove) {
                if (currentSubClasses.get(i).getHandle().getQualifiedName().equals(elementDescription.getHandle().getQualifiedName())) {
                    currentSubClasses.remove(i);
                    i--;
                    break;
                }
            }
        }
        for (int i = 0; i < currentSubClasses.size(); i++) {
            Collection<Modifier> modifiers = currentSubClasses.get(i).getModifiers();
            if (!modifiers.contains(TreeShims.getSealed()) && !modifiers.contains(TreeShims.getNonSealed()) && !modifiers.contains(Modifier.FINAL)) {
                return null;
            }
        }
        if (currentSubClasses.isEmpty()) {
            return null;
        }
        SourcePositions sourcePositions = info.getTrees().getSourcePositions();
        long startPos = sourcePositions.getStartPosition(tp.getCompilationUnit(), cls);
        if (startPos > Integer.MAX_VALUE) {
            return null;
        }
        int[] bodySpan = info.getTreeUtilities().findBodySpan(cls);
        if (bodySpan == null || bodySpan[0] <= startPos) {
            return null;
        }
        int caret = context.getCaretLocation();

        

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
            newModifiers.remove(TreeShims.getNonSealed());
            Modifier sealedMod = TreeShims.getSealed();
            newModifiers.add(sealedMod);

            ModifiersTree newModifiersTree = make.Modifiers(newModifiers);
            List<Tree> permits = new ArrayList<>();

            for (int i = 0; i < currentSubClasses.size(); i++) {
                String displayName = currentSubClasses.get(i).getHandle().getQualifiedName();
                permits.add(make.Identifier(displayName));
            }
            ClassTree sealedClass = make.Class(oldModifiers, oldClassTree.getSimpleName(), oldClassTree.getTypeParameters(), oldClassTree.getExtendsClause(), oldClassTree.getImplementsClause(), oldClassTree.getMembers());
            if (cls.getKind().equals(Tree.Kind.CLASS)) {
                sealedClass = make.ClassWithPerms(newModifiersTree, oldClassTree.getSimpleName(), oldClassTree.getTypeParameters(), oldClassTree.getExtendsClause(), oldClassTree.getImplementsClause(), permits, oldClassTree.getMembers());
            } else if (cls.getKind().equals(Tree.Kind.INTERFACE)) {
                sealedClass = make.InterfaceWithPerms(newModifiersTree, oldClassTree.getSimpleName(), oldClassTree.getTypeParameters(), oldClassTree.getImplementsClause(), permits, oldClassTree.getMembers());
            }
            ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), sealedClass);
        }
    }

}
