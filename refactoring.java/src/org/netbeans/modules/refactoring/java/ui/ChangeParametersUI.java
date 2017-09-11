/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Flaska, Jan Becicka
 */
public class ChangeParametersUI implements RefactoringUI, JavaRefactoringUIFactory {
    
    private TreePathHandle method;
    private ChangeParametersPanel panel;
    private ChangeParametersRefactoring refactoring;
    private String name;
    private boolean isMethod;
    private ChangeParametersRefactoring.ParameterInfo[] preConfiguration;
    private Lookup lookup;
    private CodeStyle cs;
    
    /** Creates a new instance of ChangeMethodSignatureRefactoring */
    private ChangeParametersUI(TreePathHandle refactoredObj, CompilationInfo info, ParameterInfo[] preConfiguration, CodeStyle cs) {
        this.refactoring = new ChangeParametersRefactoring(refactoredObj);
        this.method = refactoredObj;
        this.preConfiguration = preConfiguration;
        Element element = method.resolveElement(info);
        this.name = element.getSimpleName().toString();
        this.isMethod = element.getKind() == ElementKind.METHOD;
        this.cs = cs;
    }

    private ChangeParametersUI(Lookup lookup) {
        this.lookup = lookup;
    }
    
    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        assert handles.length == 1;

        Collection<? extends ParameterInfo> params = lookup.lookupAll(ParameterInfo.class);
        final ParameterInfo[] configuration = params.isEmpty()? null : new ParameterInfo[params.size()];
        int index = 0;
        for (ParameterInfo parameterInfo : params) {
            configuration[index] = parameterInfo;
            index++;
        }

        TreePath path = handles[0].resolve(info);
        Kind kind;
        while (path != null && (kind = path.getLeaf().getKind()) != Kind.METHOD && kind != Kind.METHOD_INVOCATION && kind != Kind.NEW_CLASS && kind != Kind.MEMBER_REFERENCE) {
            path = path.getParentPath();
        }
        
        if(path != null && ((kind = path.getLeaf().getKind()) == Kind.METHOD_INVOCATION || kind == Kind.NEW_CLASS || kind == Kind.MEMBER_REFERENCE)) {
            Element element = info.getTrees().getElement(path);
            if(element == null || element.asType().getKind() == TypeKind.ERROR) {
                return null;
            }
            ExecutableElement method = (ExecutableElement) element;
            path = info.getTrees().getPath(method);
        }
        
        return path != null
                ? new ChangeParametersUI(TreePathHandle.create(path, info), info, configuration, CodeStyle.getDefault(info.getFileObject()))
                : null;
    }
    
    public static JavaRefactoringUIFactory factory(Lookup lookup) {
        return new ChangeParametersUI(lookup);
    }
    
    @Override
    public String getDescription() {
        String msg = NbBundle.getMessage(ChangeParametersUI.class, 
                                        "DSC_ChangeParsRootNode"); // NOI18N
        return new MessageFormat(msg).format(new Object[] { 
            name,
            NbBundle.getMessage(ChangeParametersUI.class, "DSC_ChangeParsRootNode" + (isMethod ? "Method" : "Constr")),
            panel.genDeclarationString()
       });
    }
    
    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new ChangeParametersPanel(method, parent, preConfiguration, cs);
        }
        return panel;
    }
    
    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public boolean isQuery() {
        return false;
    }
    
    private Problem setParameters(boolean checkOnly) {
        List data = (List) panel.getTableModel().getDataVector();
        ChangeParametersRefactoring.ParameterInfo[] paramList = new ChangeParametersRefactoring.ParameterInfo[data.size()];
        int counter = 0;
        Problem problem = null;
        for (Iterator rowIt = data.iterator(); rowIt.hasNext(); ++counter) {
            List row = (List) rowIt.next();
            int origIndex = ((Integer) row.get(3)).intValue();
            String type = (String) row.get(0);
            String name = (String) row.get(1);
            String defaultVal = (String) row.get(2);
            paramList[counter] = new ChangeParametersRefactoring.ParameterInfo(origIndex, name, type, defaultVal);
        }
        Set<Modifier> modifier = panel.getModifier();
        refactoring.setParameterInfo(paramList);
        refactoring.setModifiers(modifier);
        refactoring.getContext().add(panel.getJavadoc());
        refactoring.setMethodName(panel.getMethodName());
        refactoring.setReturnType(panel.getReturnType());
        refactoring.setOverloadMethod(panel.isCompatible());
        if (checkOnly) {
            problem = refactoring.fastCheckParameters();
        } else {
            problem = refactoring.checkParameters();
        }
        return problem;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(ChangeParametersUI.class, "LBL_ChangeMethodSignature");
    }
    
    @Override
    public Problem checkParameters() {
        return setParameters(true);
    }

    @Override
    public Problem setParameters() {
        return setParameters(false);
    }
    
    @Override
    public boolean hasParameters() {
        return true;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.ChangeParametersUI"); // NOI18N
    }
}
