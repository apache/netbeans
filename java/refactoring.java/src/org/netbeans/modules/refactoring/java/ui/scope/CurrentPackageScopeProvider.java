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
