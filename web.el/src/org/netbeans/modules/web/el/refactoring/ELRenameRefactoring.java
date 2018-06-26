/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
