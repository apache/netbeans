/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author lahvac
 */
public class JavaDataLoaderTest extends NbTestCase {

    public JavaDataLoaderTest(String name) {
        super(name);
    }

    public void testMimeTypeBasedRecognition180478() throws Exception {
        clearWorkDir();
        FileUtil.refreshAll();
        
        MockServices.setServices(JavaDataLoader.class);
        FileUtil.setMIMEType("bbb", "text/x-java");

        File wd = getWorkDir();

        new FileOutputStream(new File(wd, "Test.java")).close();
        new FileOutputStream(new File(wd, "Test.bbb")).close();

        FileUtil.refreshAll();

        FileObject f = FileUtil.toFileObject(wd);
        DataFolder df = DataFolder.findFolder(f);
        DataObject[] children = df.getChildren();

        assertEquals(2, children.length);
        assertEquals(JavaDataObject.class, children[0].getClass());
        assertEquals(JavaDataObject.class, children[1].getClass());
    }

    public void XtestPerformance() throws Exception {
        MockServices.setServices(JavaDataLoader.class);
        FileUtil.setMIMEType("bbb", "text/x-java");
        recognize(1000);
        recognize(1000);
    }

    private void recognize(int count) throws IOException {
        clearWorkDir();
        FileUtil.refreshAll();

        File wd = getWorkDir();

        while (count-- > 0) {
            new FileOutputStream(new File(wd, "f" + count + ".java")).close();
            new FileOutputStream(new File(wd, "f" + count + ".bbb")).close();
        }

        long s = System.currentTimeMillis();
        FileUtil.refreshAll();

        FileObject f = FileUtil.toFileObject(wd);
        DataFolder df = DataFolder.findFolder(f);

        System.err.println("preparation took: " + (System.currentTimeMillis() - s));
        System.err.println(df.getChildren().length);
        System.err.println("recognition took:" + (System.currentTimeMillis() - s));
    }

}