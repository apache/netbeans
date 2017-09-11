/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=RefactoringPluginFactory.class)
public class MavenRefactoringPluginFactory implements RefactoringPluginFactory {

    private static final Logger LOG = Logger.getLogger(MavenRefactoringPluginFactory.class.getName());

    @Override public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
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
