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
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties.Visibility;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Ruijs <ralphbenjamin@netbeans.org>
 */
public class MoveBase extends RefactoringTestBase {

    public MoveBase(String name) {
        super(name);
    }

    public MoveBase(String name, String sourceLevel) {
        super(name, sourceLevel);
    }

    void performMove(FileObject source, final int position, final URL target, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final MoveRefactoring[] r = new MoveRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(position);
                TreePath classPath = info.getTrees().getPath(cut, classTree);
                r[0] = new MoveRefactoring(Lookups.singleton(TreePathHandle.create(classPath, info)));
                r[0].setTarget(Lookups.singleton(target));
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();
        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            rs.getRefactoringElements();
            addAllProblems(problems, rs.doRefactoring(true));
        }
        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
    void performMove(FileObject source, final int position, FileObject targetSource, final int targetPosition, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final MoveRefactoring[] r = new MoveRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(position);
                TreePath classPath = info.getTrees().getPath(cut, classTree);
                r[0] = new MoveRefactoring(Lookups.singleton(TreePathHandle.create(classPath, info)));
            }
        }, true);
        
        JavaSource.forFileObject(targetSource).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(targetPosition);
                TreePath classPath = info.getTrees().getPath(cut, classTree);
                r[0].setTarget(Lookups.singleton(TreePathHandle.create(classPath, info)));
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();
        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }
        assertProblems(Arrays.asList(expectedProblems), problems);
    }

    void performMove(FileObject source, final int[] position, FileObject target, final Visibility visibility, boolean delegate, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final MoveRefactoring[] r = new MoveRefactoring[1];
        final JavaMoveMembersProperties[] properties = new JavaMoveMembersProperties[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);
                List<? extends Element> allMembers = classEl.getEnclosedElements();
                TreePathHandle[] handles = new TreePathHandle[position.length];
                for (int i = 0; i < position.length; i++) {
                    handles[i] = TreePathHandle.create(allMembers.get(position[i]), info);
                }
                TreePathHandle[] preselectedMember = new TreePathHandle[1];
                if(position.length > 0) {
                    preselectedMember[0] = handles[0];
                } else {
                    preselectedMember[0] =TreePathHandle.create(allMembers.get(0), info);
                }
                r[0] = new MoveRefactoring(Lookups.fixed((Object[]) handles));
                properties[0] = new JavaMoveMembersProperties(preselectedMember);
            }
        }, true);
        if(target != null) {
            JavaSource.forFileObject(target).runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController info) throws Exception {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = info.getCompilationUnit();
                    final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                    final TreePath classPath = info.getTrees().getPath(cut, classTree);
                    TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);
                    r[0].setTarget(Lookups.singleton(TreePathHandle.create(classEl, info)));
                }
            }, true);
        }
        properties[0].setVisibility(visibility);
        properties[0].setDelegate(delegate);
        r[0].getContext().add(properties[0]);
        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();
        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }
        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
    void performMove(FileObject source, final int[] position, final String target, final Visibility visibility, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final MoveRefactoring[] r = new MoveRefactoring[1];
        final JavaMoveMembersProperties[] properties = new JavaMoveMembersProperties[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);
                List<? extends Element> allMembers = classEl.getEnclosedElements();
                TreePathHandle[] handles = new TreePathHandle[position.length];
                for (int i = 0; i < position.length; i++) {
                    handles[i] = TreePathHandle.create(allMembers.get(position[i]), info);
                }
                r[0] = new MoveRefactoring(Lookups.fixed((Object[]) handles));
                properties[0] = new JavaMoveMembersProperties(handles);
                
                TypeElement typeElement = info.getElements().getTypeElement(target);
                r[0].setTarget(Lookups.singleton(TreePathHandle.create(typeElement, info)));
            }
        }, true);
        properties[0].setVisibility(visibility);
        r[0].getContext().add(properties[0]);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();
        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }
        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
    void performMove(final String source, final int[] position, FileObject target, final Visibility visibility, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final MoveRefactoring[] r = new MoveRefactoring[1];
        final JavaMoveMembersProperties[] properties = new JavaMoveMembersProperties[1];
        JavaSource.forFileObject(target).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement classEl = info.getElements().getTypeElement(source);
                List<? extends Element> allMembers = classEl.getEnclosedElements();
                TreePathHandle[] handles = new TreePathHandle[position.length];
                for (int i = 0; i < position.length; i++) {
                    handles[i] = TreePathHandle.create(allMembers.get(position[i]), info);
                }
                r[0] = new MoveRefactoring(Lookups.fixed((Object[]) handles));
                properties[0] = new JavaMoveMembersProperties(handles);
            }
        }, true);
        JavaSource.forFileObject(target).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);
                r[0].setTarget(Lookups.singleton(TreePathHandle.create(classEl, info)));
            }
        }, true);
        properties[0].setVisibility(visibility);
        r[0].getContext().add(properties[0]);
        
        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();
        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].fastCheckParameters());
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].checkParameters());
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }
        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
    void performMove(FileObject source, final URL target, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        MoveRefactoring r = new MoveRefactoring(Lookups.singleton(source));
        r.setTarget(Lookups.singleton(target));

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();
        addAllProblems(problems, r.preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r.prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            rs.getRefactoringElements();
            addAllProblems(problems, rs.doRefactoring(true));
        }
        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
}
