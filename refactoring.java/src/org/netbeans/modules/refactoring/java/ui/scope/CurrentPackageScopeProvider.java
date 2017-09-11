/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.ui.scope;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.netbeans.modules.refactoring.spi.ui.ScopeReference;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@Messages({"LBL_CurrentPackage=Current Package"})
@ScopeProvider.Registration(id = "current-package", displayName = "#LBL_CurrentPackage", position = 300, iconBase = "org/netbeans/spi/java/project/support/ui/package.gif")
@ScopeReference(path="org-netbeans-modules-refactoring-java-ui-WhereUsedPanel")
public final class CurrentPackageScopeProvider extends ScopeProvider {

    private String detail;
    private Scope scope;

    @Override
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        FileObject file = context.lookup(FileObject.class);
        if (file == null) {
            return false;
        }
        final FileObject packageFolder;
        final String packageName;
        ClassPath sourceCP = ClassPath.getClassPath(file, ClassPath.SOURCE);
        if (sourceCP != null) {
//            TreePathHandle path = context.lookup(TreePathHandle.class);
//            if (path != null) {
//                final ExpressionTree packageName1 = path.getCompilationUnit().getPackageName();
//                packageName = packageName1 == null ? "<default package>" : packageName1.toString(); //NOI18N
//                if (packageName1 == null) {
//                    packageFolder = sourceCP.findOwnerRoot(file);
//                } else {
//                    packageFolder = sourceCP.findResource(packageName.replaceAll("\\.", "/")); //NOI18N
//                }
//            } else {
            packageFolder = file.isFolder()? file : file.getParent();
            String packageName1 = sourceCP.getResourceName(packageFolder, '.', false);
            if(packageName1 == null) {
                return false;
            }
            packageName = packageName1.isEmpty()? "<default package>" : packageName1; //NOI18N
//            }
        } else {
            packageFolder = null;
            packageName = null;
        }

        if (packageFolder != null) {
            detail = packageName;
            scope = Scope.create(null, Arrays.<NonRecursiveFolder>asList(new NonRecursiveFolder() {
                @Override
                public FileObject getFolder() {
                    return packageFolder;
                }
            }), null);
            return true;
        }
        return false;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
