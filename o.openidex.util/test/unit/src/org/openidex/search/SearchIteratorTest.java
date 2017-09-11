/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openidex.search;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author  Marian Petras
 */
public final class SearchIteratorTest extends NbTestCase {

    /** */
    private FileObject dataDir;
    /** */
    FileObject projectRoot;

    /**
     */
    public SearchIteratorTest(String name) {
        super(name);
    }

    /**
     */
    public static void main(String args[]) {
        TestRunner.run(new NbTestSuite(SearchIteratorTest.class));
    }

    @Override
    protected void setUp() throws Exception {
        dataDir = FileUtil.toFileObject(getDataDir());
        assert dataDir != null;
        
        projectRoot = dataDir.getFileObject("projects/Project1");       //NOI18N
        assert projectRoot != null;
    }

    /**
     */
    public void testPlainSearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    false,          //check visibility?
                                    false,          //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    public void testVisibilitySearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    true,           //check visibility?
                                    false,          //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    public void testSharabilitySearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    false,          //check visibility?
                                    true,           //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    public void testVisibSharSearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    true,           //recursive
                                    true,           //check visibility?
                                    true,           //check sharability?
                                    getRef());
        compareReferenceFiles();
    }
    
    public void testNonRecursiveSearchInfo() throws Exception {
        generateSearchableFileNames(projectRoot,
                                    false,          //not recursive
                                    false,
                                    false,
                                    getRef());
        compareReferenceFiles();
    }
    
    /**
     */
    private void generateSearchableFileNames(
            FileObject folder,
            boolean recursive,
            boolean checkVisibility,
            boolean checkSharability,
            PrintStream refPrintStream) {

        FileObject[] foldersToCheck = new FileObject[2];
        foldersToCheck[0] = folder.getFileObject("src");
        foldersToCheck[1] = folder.getFileObject("test");
        for (FileObject f : foldersToCheck) {
            if ((f == null) || !f.isFolder()) {
                throw new IllegalStateException();
            }
        }
                
        FileObjectFilter[] filters;

        int filtersCount = 0;
        if (checkVisibility) {
            filtersCount++;
        }
        if (checkSharability) {
            filtersCount++;
        }

        if (filtersCount == 0) {
            filters = null;
        } else {
            filters = new FileObjectFilter[filtersCount];

            int i = 0;
            if (checkVisibility) {
                filters[i++] = SearchInfoFactory.VISIBILITY_FILTER;
            }
            if (checkSharability) {
                filters[i++] = SearchInfoFactory.SHARABILITY_FILTER;
            }
        }

        SearchInfo searchInfo = SearchInfoFactory.createSearchInfo(
                foldersToCheck,
                recursive,
                filters);
        
        assertTrue("project root not searchable", searchInfo.canSearch());
        
        List<String> foundFilesPaths = new ArrayList<String>(16);
        for (Iterator<DataObject> i = searchInfo.objectsToSearch(); i.hasNext(); ) {
            FileObject primaryFile = i.next().getPrimaryFile();
            String relativePath = FileUtil.getRelativePath(projectRoot,
                                                           primaryFile);
            foundFilesPaths.add(relativePath);
        }
        
        Collections.sort(foundFilesPaths);
        
        for (String path : foundFilesPaths) {
            refPrintStream.println(path);
        }
    }

}
