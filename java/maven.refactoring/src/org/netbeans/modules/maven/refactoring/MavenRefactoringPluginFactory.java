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

package org.netbeans.modules.maven.refactoring;

import com.sun.source.tree.Tree;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=RefactoringPluginFactory.class)
public class MavenRefactoringPluginFactory implements RefactoringPluginFactory {

    private static final Logger LOG = Logger.getLogger(MavenRefactoringPluginFactory.class.getName());
    public static final String RUN_MAIN_CLASS = "exec.mainClass";

    @Override public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (refactoring instanceof RenameRefactoring) {
            TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
            if (handle != null && handle.getKind() == Tree.Kind.CLASS) {
                FileObject fo = handle.getFileObject();
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null && p.getLookup().lookup(NbMavenProject.class) != null) {
                    LOG.log(Level.FINE, "Renaming {0} field in a project pom.xml", RUN_MAIN_CLASS);
                    return new MavenRefactoringPlugin((RenameRefactoring) refactoring, handle);
                }
                return null;
            }
        } 
        if (!(refactoring instanceof WhereUsedQuery)) {
            return null;
        }
        TreePathHandle handle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (handle == null) {
            return null;
        }
        Tree.Kind kind = handle.getKind();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(kind) || kind == Tree.Kind.IDENTIFIER || kind == Tree.Kind.MEMBER_SELECT) {
            FileObject fo = handle.getFileObject();
            Project p = FileOwnerQuery.getOwner(fo);
            if (p == null) {
                FileObject root = FileUtil.getArchiveFile(fo);
                if (root != null && root.getNameExt().endsWith("-sources.jar")) {
                    LOG.log(Level.FINE, "considering usages from {0} in a Maven binary artifact", fo.toURI());
                    return new MavenRefactoringPlugin((WhereUsedQuery) refactoring, handle);
                } else {
                    LOG.log(Level.FINE, "binary file of no particular interest: {0}", fo.toURI());
                    return null;
                }
            } else if (p.getLookup().lookup(NbMavenProject.class) != null) {
                LOG.log(Level.FINE, "considering usages from {0} in a Maven project", fo.toURI());
                return new MavenRefactoringPlugin((WhereUsedQuery) refactoring, handle);
            } else {
                LOG.log(Level.FINE, "not in a Maven project: {0}", fo.toURI());
                return null;
            }
        } else {
            LOG.log(Level.FINE, "ignoring {0} of kind {1}", new Object[] {handle, kind});
            return null;
        }
    }

}
