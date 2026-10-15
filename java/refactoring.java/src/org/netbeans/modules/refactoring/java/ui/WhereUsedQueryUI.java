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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.java.plugins.JavaPluginUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Matula, Jan Becicka, Ralph Ruijs
 */
public class WhereUsedQueryUI implements RefactoringUI, Openable, JavaRefactoringUIFactory {

    private WhereUsedQuery query = null;
    private String name;
    private WhereUsedPanel panel;
    private TreePathHandle element;
    private ElementHandle<Element> elementHandle;
    private ElementKind kind;
    private AbstractRefactoring delegate;
    private final List<Pair<Pair<String, Icon>, TreePathHandle>> classes;

    private WhereUsedQueryUI() {
        this.classes = null;
    }

    private WhereUsedQueryUI(TreePathHandle handle, Element el, List<Pair<Pair<String, Icon>, TreePathHandle>> classes) {
        this.query = new WhereUsedQuery(Lookups.singleton(handle));
        this.element = handle;
        if (UIUtilities.allowedElementKinds.contains(el.getKind())) {
            elementHandle = ElementHandle.create(el);
        }
        kind = el.getKind();
        if(kind == ElementKind.CONSTRUCTOR) {
            name = el.getEnclosingElement().getSimpleName().toString();
        } else {
            name = el.getSimpleName().toString();
        }
        this.classes = classes;
    }

    public WhereUsedQueryUI(TreePathHandle handle, String name, AbstractRefactoring delegate) {
        this.delegate = delegate;
        this.element = handle;
        this.name = name;
        this.classes = null;
    }

    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = WhereUsedPanel.create(name, element, kind, classes, parent);
        }
        return panel;
    }

    @Override
    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, panel.isSearchInComments());
        ScopeProvider customScope = panel.getCustomScope();
        Problem prob = null;
        Scope scope = null;
        if (customScope != null) {
            prob = customScope.getProblem();
            scope = customScope.getScope();
        }
        if(scope != null) {
            query.getContext().add(scope);
        } else {
            query.getContext().remove(Scope.class);
        }
        if (kind == ElementKind.METHOD) {
            setForMethod();
            return JavaPluginUtils.chainProblems(prob, query.checkParameters());
        } else if (kind.isClass() || kind.isInterface()) {
            setForClass();
            return JavaPluginUtils.chainProblems(prob, query.checkParameters());
        } else if(kind == ElementKind.CONSTRUCTOR) {
            setForConstructor();
            return JavaPluginUtils.chainProblems(prob, query.checkParameters());
        } else {
            return prob;
        }
    }

    private void setForMethod() {
        query.getContext().add(element);
        query.setRefactoringSource(Lookups.singleton(panel.getMethodHandle()));
        query.putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS, panel.isMethodFromBaseClass());
        query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS, panel.isMethodOverriders());
        query.putValue(WhereUsedQuery.FIND_REFERENCES, panel.isMethodFindUsages());
        query.putValue(WhereUsedQueryConstants.SEARCH_OVERLOADED, panel.isMethodSearchOverloaded());
    }
    
    private void setForConstructor() {
        query.putValue(WhereUsedQueryConstants.SEARCH_OVERLOADED, panel.isMethodSearchOverloaded());
    }

    private void setForClass() {
        query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES, panel.isClassSubTypes());
        query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES, panel.isClassSubTypesDirectOnly());
        query.putValue(WhereUsedQuery.FIND_REFERENCES, panel.isClassFindUsages());
    }

    @Override
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        ScopeProvider customScope = panel.getCustomScope();
        Problem prob = null;
        if (customScope != null) {
            prob = customScope.getProblem();
        }
        if (kind == ElementKind.METHOD) {
            setForMethod();
            return JavaPluginUtils.chainProblems(prob, query.fastCheckParameters());
        } else if (kind.isClass() || kind.isInterface()) {
            setForClass();
            return JavaPluginUtils.chainProblems(prob, query.fastCheckParameters());
        } else if(kind == ElementKind.CONSTRUCTOR) {
            setForConstructor();
            return JavaPluginUtils.chainProblems(prob, query.fastCheckParameters());
        } else {
            return prob;
        }
    }

    @Override
    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return query != null ? query : delegate;
    }

    @Override
    public String getDescription() {
        boolean isScanning = SourceUtils.isScanInProgress();
        String desc = null;

        if (panel != null && kind != null) {
            switch (kind) {
                case CONSTRUCTOR: {
                    if (panel.isMethodFindUsages() && panel.isMethodOverriders()) {
                        desc = getString("DSC_WhereUsedAndOverriders", name);
                    } else if (panel.isMethodFindUsages()) {
                        desc = getString("DSC_WhereUsed",  name);
                    } else if (panel.isMethodOverriders()) {
                        desc = getString("DSC_WhereUsedMethodOverriders",  name);
                    }
                    break;
                }
                case METHOD: {
                    if (panel.isMethodFindUsages() && panel.isMethodOverriders()) {
                        desc = getString("DSC_WhereUsedAndOverriders", panel.getMethodDeclaringClass() + '.' + name);
                    } else if (panel.isMethodFindUsages()) {
                        desc = getString("DSC_WhereUsed", panel.getMethodDeclaringClass() + '.' + name);
                    } else if (panel.isMethodOverriders()) {
                        desc = getString("DSC_WhereUsedMethodOverriders", panel.getMethodDeclaringClass() + '.' + name);
                    }
                    break;
                }
                case CLASS:
                case ENUM:
                case INTERFACE:
                case RECORD:
                case ANNOTATION_TYPE: {
                    if (!panel.isClassFindUsages()) {
                        if (!panel.isClassSubTypesDirectOnly()) {
                            desc = getString("DSC_WhereUsedFindAllSubTypes", name);
                        } else {
                            desc = getString("DSC_WhereUsedFindDirectSubTypes", name);
                        }
                    }
                    break;
                }
                case PACKAGE: {
//                    panel = packagePanel;
                    break;
                }
                case FIELD:
                case ENUM_CONSTANT:
                default: {
//                    panel = variablePanel;
                    break;
                }
            }
        }
        if (desc == null) {
            desc = getString("DSC_WhereUsed", name);
        }

        if (isScanning) {
            return getString("DSC_Scan_Warning", desc);
        } else {
            return desc;
        }
    }
    private ResourceBundle bundle;

    private String getString(String key) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(WhereUsedQueryUI.class);
        }
        return bundle.getString(key);
    }

    private String getString(String key, String value) {
        return new MessageFormat(getString(key)).format(new Object[]{value});
    }

    @Override
    public String getName() {
        return new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_UsagesOf")).format(new Object[]{name});
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.WhereUsedQueryUI"); // NOI18N
    }

    @Override
    public void open() {
        if (elementHandle != null) {
            ElementOpen.open(element.getFileObject(), elementHandle);
        }
    }

    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        if(handles.length < 1 || handles[0] == null) {
            return null;
        }
        TreePathHandle handle = handles[0];
        Element el = handle.resolveElement(info);
        if (el == null) {
            return null;
        }
        TreePath path = handle.resolve(info);
        // Re-create the handle, the old handle can be without a fileobject
        if(handle.getFileObject() == null) {
            if(path != null) {
                handle = TreePathHandle.create(path, info);
            } else {
                handle = TreePathHandle.create(el, info);
            }
        }
        if(el.getKind() == ElementKind.CLASS) {
            if(path != null && path.getParentPath() != null) {
                TreePath parentPath = path.getParentPath();
                if(parentPath.getLeaf().getKind() == Tree.Kind.NEW_CLASS) {
                    Element newClass = info.getTrees().getElement(parentPath);
                    if(newClass != null) {
                        handle = TreePathHandle.create(parentPath, info);
                        el = newClass;
                    }
                }
            }
        }
        final List<Pair<Pair<String, Icon>, TreePathHandle>> classes;
        if(RefactoringUtils.isExecutableElement(el)) {
            ExecutableElement method = (ExecutableElement) el;
            classes = new LinkedList<Pair<Pair<String, Icon>, TreePathHandle>>();
            Element enclosingElement = method.getEnclosingElement();
            String methodDeclaringClass = enclosingElement.getSimpleName().toString();
            Icon icon = ElementIcons.getElementIcon(enclosingElement.getKind(), enclosingElement.getModifiers());
            classes.add(Pair.of(Pair.of(methodDeclaringClass, icon), handle));

            Collection<ExecutableElement> overridens = JavaRefactoringUtils.getOverriddenMethods(method, info);
            for (ExecutableElement executableElement : overridens) {
                Element enclosingTypeElement = executableElement.getEnclosingElement();
                String elName = enclosingTypeElement.getSimpleName().toString();
                TreePathHandle tph = TreePathHandle.create(executableElement, info);
                Icon typeIcon = ElementIcons.getElementIcon(enclosingTypeElement.getKind(), enclosingTypeElement.getModifiers());
                classes.add(Pair.of(Pair.of(elName, typeIcon), tph));
            }
        } else if (el.getKind() == ElementKind.PACKAGE) { // Remove for #94325
            return null;
        } else {
            classes = null;
        }
        return new WhereUsedQueryUI(handle, el, classes);
    }

    public static JavaRefactoringUIFactory factory() {
        return new WhereUsedQueryUI();
    }
}
