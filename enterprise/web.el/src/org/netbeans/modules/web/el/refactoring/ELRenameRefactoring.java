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
package org.netbeans.modules.web.el.refactoring;

import com.sun.el.parser.AstMethodArguments;
import com.sun.el.parser.Node;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeUtilities.TypeNameOptions;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELVariableResolvers;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;

/**
 * Rename refactoring plugin for Expression Language.
 *
 * @author Erno Mononen
 */
public class ELRenameRefactoring extends ELWhereUsedQuery {

    private final RenameRefactoring rename;

    public ELRenameRefactoring(RenameRefactoring rename) {
        super(rename);
        this.rename = rename;
    }

    @Override
    protected Problem handleClass(CompilationContext info, RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, Element targetType) {
        TypeElement type = (TypeElement) targetType;
        // handles only cases where the managed bean name matches the class name and is not 
        // explicitly specified in the annotation
        String beanName = ELVariableResolvers.findBeanName(info, type.getQualifiedName().toString(), getFileObject());
        if (beanName != null && beanName.equalsIgnoreCase(type.getSimpleName().toString())) {
            for (AnnotationMirror ann : info.info().getElements().getAllAnnotationMirrors(type)) {
                CharSequence annFqn = info.info().getTypeUtilities().getTypeName(ann.getAnnotationType(), TypeNameOptions.PRINT_FQN);
                if ("javax.faces.bean.ManagedBean".contentEquals(annFqn)) { //NOI18N
                    for (ExecutableElement annElem : ann.getElementValues().keySet()) { 
                        if ("name".contentEquals(annElem.getSimpleName())) { //NOI18N
                            // name explicitly specified, so don't refactor
                            return null;
                        }
                    }
                    // uses default name, can be refactored
                    return super.handleClass(info, refactoringElementsBag, handle, targetType);
                }
                if ("javax.inject.Named".contentEquals(annFqn)) { //NOI18N
                    for (ExecutableElement annElem : ann.getElementValues().keySet()) { 
                        if ("value".contentEquals(annElem.getSimpleName())) { //NOI18N
                            // name explicitly specified, so don't refactor
                            return null;
                        }
                    }
                    // uses default name, can be refactored
                    return super.handleClass(info, refactoringElementsBag, handle, targetType);
                }
            }
        }
        return null;
    }

    @Override
    protected void addElements(CompilationContext info, ELElement elem, List<Node> matchingNodes, RefactoringElementsBag refactoringElementsBag) {
        FileObject file = elem.getSnapshot().getSource().getFileObject();
        ModificationResult modificationResult = new ModificationResult();
        List<Difference> differences = new ArrayList<>();

        TypeMirror returnType = null;
        TreePathHandle treePathHandle = rename.getRefactoringSource().lookup(TreePathHandle.class);
        if (treePathHandle != null && treePathHandle.getKind() == Tree.Kind.METHOD) {
            ExecutableElement methodElement = (ExecutableElement) treePathHandle.resolveElement(info.info());
            returnType = methodElement.getReturnType();
        }

        for (Node targetNode : matchingNodes) {
            PositionRef[] position = RefactoringUtil.getPostionRefs(elem, targetNode);
            String renameNewName = rename.getNewName();
            if (renameNewName != null) {
                String newName = renameNewName + "()"; //NOI18N
                if (RefactoringUtil.isPropertyAccessor(renameNewName, returnType)) {
                    newName = RefactoringUtil.getPropertyName(renameNewName, returnType);
                } else if (targetNode.jjtGetLastToken().image != null
                        && !targetNode.jjtGetLastToken().image.endsWith(")")) {  //NOI18N
                    // issue #245540
                    newName = renameNewName;
                }

                // issue #246641
                PositionRef[] childPosition = null;
                if (targetNode.jjtGetNumChildren() > 0) {
                    Node child = targetNode.jjtGetChild(0);
                    if (child instanceof AstMethodArguments) {
                        childPosition = RefactoringUtil.getPostionRefs(elem, child);
                        newName = renameNewName;
                    }
                }
                differences.add(new Difference(Difference.Kind.CHANGE,
                        position[0], childPosition != null ? childPosition[0] : position[1], targetNode.getImage(), newName,
                        NbBundle.getMessage(ELRenameRefactoring.class, "LBL_Update", targetNode.getImage())));
            }
        }
        modificationResult.addDifferences(file, differences);

        refactoringElementsBag.registerTransaction(new RefactoringCommit(Collections.singletonList(modificationResult)));
        for (Difference diff : modificationResult.getDifferences(file)) {
            DiffElement diffElem = DiffElement.create(diff, file, modificationResult);
            refactoringElementsBag.add(refactoring, diffElem);
        }

    }
}
