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
package org.netbeans.modules.java.hints.spiimpl.batch;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Folder;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.IndexEnquirer;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.MapIndices;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Scope;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
public class Scopes {

    public static Scope allOpenedProjectsScope() {
        return new AllOpenedProjectsScope();
    }

    private static final class AllOpenedProjectsScope extends Scope {

        @Override
        public String getDisplayName() {
            return "All Opened Projects";
        }

        @Override
        public Collection<? extends Folder> getTodo() {
            Set<Folder> todo = new HashSet<Folder>();

            for (ClassPath source : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                todo.addAll(Arrays.asList(Folder.convert(source.getRoots())));
            }

            return todo;
        }

        @Override
        public MapIndices getIndexMapper(Iterable<? extends HintDescription> hints) {
            return getDefaultIndicesMapper();
        }
    }

    public static Scope specifiedFoldersScope(Folder... roots) {
        return new SpecificFoldersScope(roots);
    }
    
    private static final class SpecificFoldersScope extends Scope {

        private final Collection<? extends Folder> roots;

        public SpecificFoldersScope(Folder... roots) {
            this.roots = Arrays.asList(roots);
        }

        @Override
        public String getDisplayName() {
            return "Specified Root";
        }

        @Override
        public Collection<? extends Folder> getTodo() {
            return roots;
        }

        @Override
        public MapIndices getIndexMapper(Iterable<? extends HintDescription> hints) {
            return getDefaultIndicesMapper();
        }
    }

    public static MapIndices getDefaultIndicesMapper() {
        return new MapIndices() {
            @Override
            public IndexEnquirer findIndex(FileObject root, ProgressHandleWrapper progress, boolean recursive) {
                IndexEnquirer e = findIndexEnquirer(root, progress, recursive);

                if (e != null) return e;
                else return new BatchSearch.FileSystemBasedIndexEnquirer(root, recursive);
            }
        };
    }
    
    public static IndexEnquirer findIndexEnquirer(FileObject root, ProgressHandleWrapper progress, boolean recursive) {
        for (MapIndices mi : Lookup.getDefault().lookupAll(MapIndices.class)) {
            IndexEnquirer r = mi.findIndex(root, progress, recursive);

            if (r != null) return r;
        }

        return null;
    }
}
