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
/*
 * Contributor(s): theanuradha@netbeans.org, markiewb@netbeans.org
 */
package org.netbeans.modules.apisupport.project.java.hints.errors;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Name;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ModuleDependency;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.ui.customizer.AddModulePanel;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Fixable hint for an unresolved class which opens the NetBeans plattform
 * module dependency dialog. The dialog will be prefilled with the name of the
 * unresolved class.
 *
 * <ul> <li>Hint activation code taken from
 * {@link org.netbeans.modules.maven.hints.errors.SearchClassDependencyInRepo}</li>
 * <li>Dialog opening code taken from
 * {@link org.netbeans.modules.apisupport.project.ui.LibrariesNode.AddModuleDependencyAction}</li>
 * </ul>
 *
 * @author Anuradha G
 * @author markiewb
 */
@Messages({
    "LBL_Module_Dependency_Search_DisplayName=Add Module Dependency",
    "# {0} - name of unknown symbol",
    "FIX_Module_Dependency_Search=Search Module Dependency for {0}",
    "FIX_Module_Dependency_UpdatingDependencies=Update dependencies"
})
public class SearchModuleDependency implements org.netbeans.modules.java.hints.spi.ErrorRule<Void> {

    private AtomicBoolean cancel = new AtomicBoolean(false);

    public SearchModuleDependency() {
    }

    @Override
    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList(
                "compiler.err.cant.resolve",//NOI18N
                "compiler.err.cant.resolve.location",//NOI18N
                "compiler.err.doesnt.exist",//NOI18N
                "compiler.err.not.stmt"));//NOI18N

    }

    private boolean isHintEnabled() {
        //TODO provide an option to disable this hint 
        return true;
    }

    @Override
    @SuppressWarnings("fallthrough")
    public List<Fix> run(final CompilationInfo info, String diagnosticKey,
            final int offset, TreePath treePath, Data<Void> data) {
        cancel.set(false);
        if (!isHintEnabled()) {
            return Collections.emptyList();
        }
        //copyed from ImportClass
        int errorPosition = offset + 1; //TODO: +1 required to work OK, rethink

        if (errorPosition == (-1)) {

            return Collections.<Fix>emptyList();
        }
        //copyed from ImportClass-end
        FileObject fileObject = info.getFileObject();
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return Collections.emptyList();
        }
        NbModuleProject nbModuleProject = project.getLookup().lookup(NbModuleProject.class);
        if (nbModuleProject == null) {
            return Collections.emptyList();
        }

        //copyed from ImportClass
        TreePath path = info.getTreeUtilities().pathFor(errorPosition);
        if (path.getParentPath() == null) {
            return Collections.emptyList();
        }

        Tree leaf = path.getParentPath().getLeaf();

        switch (leaf.getKind()) {
            case METHOD_INVOCATION: {
                MethodInvocationTree mit = (MethodInvocationTree) leaf;

                if (!mit.getTypeArguments().contains(path.getLeaf())) {
                    return Collections.<Fix>emptyList();
                }
            }
            //genaric handling

            case PARAMETERIZED_TYPE: {
                leaf = path.getParentPath().getParentPath().getLeaf();
            }
            break;
            case ARRAY_TYPE: {
                leaf = path.getParentPath().getParentPath().getLeaf();
            }
            break;
        }
        switch (leaf.getKind()) {
            case VARIABLE: {
                Name typeName = null;
                VariableTree variableTree = (VariableTree) leaf;
                if (variableTree.getType() != null) {
                    switch (variableTree.getType().getKind()) {
                        case IDENTIFIER: {
                            typeName = ((IdentifierTree) variableTree.getType()).getName();
                        }
                        break;
                        case PARAMETERIZED_TYPE: {
                            ParameterizedTypeTree ptt = ((ParameterizedTypeTree) variableTree.getType());
                            if (ptt.getType() != null && ptt.getType().getKind() == Kind.IDENTIFIER) {
                                typeName = ((IdentifierTree) ptt.getType()).getName();
                            }
                        }
                        break;
                        case ARRAY_TYPE: {
                            ArrayTypeTree ptt = ((ArrayTypeTree) variableTree.getType());
                            if (ptt.getType() != null && ptt.getType().getKind() == Kind.IDENTIFIER) {
                                typeName = ((IdentifierTree) ptt.getType()).getName();
                            }
                        }
                        break;

                    }
                }

                ExpressionTree initializer = variableTree.getInitializer();
                if (typeName != null && initializer != null) {

                    Name itName = null;
                    switch (initializer.getKind()) {
                        case NEW_CLASS: {
                            ExpressionTree identifier;
                            NewClassTree classTree = (NewClassTree) initializer;
                            identifier = classTree.getIdentifier();

                            if (identifier != null) {

                                switch (identifier.getKind()) {
                                    case IDENTIFIER:
                                        itName = ((IdentifierTree) identifier).getName();
                                        break;
                                    case PARAMETERIZED_TYPE: {

                                        ParameterizedTypeTree ptt = ((ParameterizedTypeTree) identifier);
                                        if (ptt.getType() != null && ptt.getType().getKind() == Kind.IDENTIFIER) {
                                            itName = ((IdentifierTree) ptt.getType()).getName();
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                        case NEW_ARRAY: {
                            NewArrayTree arrayTree = (NewArrayTree) initializer;
                            Tree type = arrayTree.getType();
                            if (type != null) {
                                if (type.getKind() == Kind.IDENTIFIER) {
                                    itName = ((IdentifierTree) type).getName();
                                }
                            }
                        }
                        break;
                    }

                    if (typeName.equals(itName)) {
                        return Collections.<Fix>emptyList();
                    }
                }
            }
            break;

        }

        String simpleOrQualifiedName = null;

        // XXX somewhat crude; is there a simpler way?
        TreePath p = path;
        while (p != null) {
            TreePath parent = p.getParentPath();
            if (parent == null) {
                break;
            }
            Kind parentKind = parent.getLeaf().getKind();
            if (parentKind == Kind.IMPORT) {
                simpleOrQualifiedName = p.getLeaf().toString();
                break;
            } else if (parentKind == Kind.MEMBER_SELECT || parentKind == Kind.IDENTIFIER) {
                p = parent;
            } else {
                break;
            }
        }

        if (simpleOrQualifiedName == null) {
            try {
                Token<?> ident = findUnresolvedElementToken(info, offset);
                if (ident == null) {
                    return Collections.<Fix>emptyList();
                }
                simpleOrQualifiedName = ident.text().toString();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
                return Collections.<Fix>emptyList();
            }
        }

        //copyed from ImportClass-end
        if (cancel.get()) {
            return Collections.<Fix>emptyList();
        }
        //#212331 star static imports need to be stripped of the .* part. 
        if (simpleOrQualifiedName.endsWith(".*")) {
            simpleOrQualifiedName = simpleOrQualifiedName.substring(0, simpleOrQualifiedName.length() - ".*".length());
        }

        List<Fix> fixes = new ArrayList<Fix>();
        fixes.add(new OpenDependencyDialogFix(nbModuleProject, simpleOrQualifiedName, info, path));
        return fixes;
    }

    //copyed from ImportClass
    private static Token findUnresolvedElementToken(CompilationInfo info, int offset) throws IOException {
        TokenHierarchy<?> th = info.getTokenHierarchy();
        TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());

        if (ts == null) {
            return null;
        }

        ts.move(offset);
        if (ts.moveNext()) {
            Token t = ts.token();

            if (t.id() == JavaTokenId.DOT) {
                ts.moveNext();
                t = ts.token();
            } else {
                if (t.id() == JavaTokenId.LT) {
                    ts.moveNext();
                    t = ts.token();
                } else {
                    if (t.id() == JavaTokenId.NEW) {
                        boolean cont = ts.moveNext();

                        while (cont && ts.token().id() == JavaTokenId.WHITESPACE) {
                            cont = ts.moveNext();
                        }

                        if (!cont) {
                            return null;
                        }
                        t = ts.token();
                    }
                }
            }

            if (t.id() == JavaTokenId.IDENTIFIER) {
                return ts.offsetToken();
            }
        }
        return null;
    }

    @Override
    public String getId() {
        return "NBM_MISSING_CLASS_NBMANT";//NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SearchModuleDependency.class, "LBL_Module_Dependency_Search_DisplayName");
    }

    @Override
    public void cancel() {
        //cancel task
        cancel.set(true);
    }

    static final class OpenDependencyDialogFix implements EnhancedFix {

        private NbModuleProject project;
        private String clazz;
        private CompilationInfo info;
        private TreePath path;
        
        public OpenDependencyDialogFix(NbModuleProject project, String clazz, CompilationInfo info, TreePath path) {
            this.project = project;
            this.clazz = clazz;
            this.info = info;
            this.path = path;
        }

        @Override
        public CharSequence getSortText() {
            return getText();
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(OpenDependencyDialogFix.class, "FIX_Module_Dependency_Search", clazz);
        }

        @Override
        public ChangeInfo implement() throws Exception {
            SingleModuleProperties props = SingleModuleProperties.getInstance(project);
            final ModuleDependency[] newDeps = AddModulePanel.selectDependencies(props, clazz);
            final AtomicBoolean cancel = new AtomicBoolean();
            ProgressUtils.runOffEventDispatchThread(new Runnable() {
                public @Override
                void run() {
                    ProjectXMLManager pxm = new ProjectXMLManager(project);
                    try {
                        pxm.addDependencies(new HashSet<ModuleDependency>(Arrays.asList(newDeps))); // XXX cannot cancel
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException e) {
//                        LOG.log(Level.INFO, "Cannot add selected dependencies: " + Arrays.asList(newDeps), e);
                    } catch (ProjectXMLManager.CyclicDependencyException ex) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(msg);
                    }
                    int occurences = 0;
                    String fqnIter = "";
                    for (ModuleDependency depIter : newDeps) {
                        for (String publPkgClassNameIter : depIter.getModuleEntry().getPublicClassNames()) {
                            if (publPkgClassNameIter.endsWith("." + clazz)) {
                                fqnIter = publPkgClassNameIter;
                                occurences++;
                            }
                        }
                    }
                    if (occurences == 1) {
                        final String fqn = fqnIter;
                        JavaSource js = JavaSource.forFileObject(info.getFileObject());
                        if(js != null) {
                            try {
                                js.runWhenScanFinished(new Task<CompilationController>() {
                                    @Override
                                    public void run(CompilationController cc) throws Exception {
                                        try {
                                            SourceUtils.resolveImport(info, path, fqn);
                                        } catch (NullPointerException ex) {
                                            Exceptions.printStackTrace(ex);
                                        } catch (IOException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                }, true);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
             ;
            }, NbBundle.getMessage(SearchModuleDependency.class, "FIX_Module_Dependency_UpdatingDependencies"), cancel, false);
            return null;
        }
    }
}
