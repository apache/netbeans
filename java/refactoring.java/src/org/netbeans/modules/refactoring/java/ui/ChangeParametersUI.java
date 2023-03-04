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
