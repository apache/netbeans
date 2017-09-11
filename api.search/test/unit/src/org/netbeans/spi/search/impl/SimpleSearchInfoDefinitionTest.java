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
package org.netbeans.spi.search.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class SimpleSearchInfoDefinitionTest extends NbTestCase {

    public SimpleSearchInfoDefinitionTest(String name) {
        super(name);
    }

    /**
     * When creating a SimpleSearchInfoDefinition with some filters that permit
     * searching in the root file, these filters should be removed.
     */
    public void testNiceFilters() throws IOException {

        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject f1 = root.createData("file.txt");
        root.createData("skippedFile.txt");

        SimpleSearchInfoDefinition searchInfo = new SimpleSearchInfoDefinition(
                root, new SearchFilterDefinition[]{
                    new NiceFilter(f1), // this file will be used
                    new BadFilterDef() // this filter should be removed
                });
        Iterator<FileObject> files = searchInfo.filesToSearch(
                SearchScopeOptions.create(), new SearchListener() {
        },
                new AtomicBoolean(false));
        assertEquals("The first line should be found", f1, files.next());
        assertFalse("The second file should be filtered", files.hasNext());
    }

    /**
     * Filter that is bad to all files.
     */
    private static class BadFilterDef extends SearchFilterDefinition {

        @Override
        public boolean searchFile(FileObject file)
                throws IllegalArgumentException {
            return false;
        }

        @Override
        public FolderResult traverseFolder(FileObject folder)
                throws IllegalArgumentException {
            return FolderResult.DO_NOT_TRAVERSE;
        }
    }

    /**
     * Filter that is nice to a single file, but bad to other files.
     */
    private static class NiceFilter extends SearchFilterDefinition {

        FileObject favouredFile;

        public NiceFilter(FileObject favouredFile) {
            this.favouredFile = favouredFile;
        }

        @Override
        public boolean searchFile(FileObject file)
                throws IllegalArgumentException {
            return file.equals(favouredFile);
        }

        @Override
        public FolderResult traverseFolder(FileObject folder)
                throws IllegalArgumentException {
            return FolderResult.TRAVERSE;
        }
    }
}
