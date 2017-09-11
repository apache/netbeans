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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Utilities;

public class GlobalJavadocForBinaryImplTest extends TestBase {

    public GlobalJavadocForBinaryImplTest(String testName) {
        super(testName);
    }

    public void testFindJavadoc() throws Exception {
        File nbDocZip = generateNbDocZip();
        URL nbDocZipURL = FileUtil.urlForArchiveOrDir(nbDocZip);
        NbPlatform.getDefaultPlatform().addJavadocRoot(nbDocZipURL);
        doTestFindJavadoc(Utilities.toURI(file("openide.loaders/src")).toURL(), nbDocZipURL);
        doTestFindJavadoc(FileUtil.urlForArchiveOrDir(file("nbbuild/netbeans/platform/modules/org-openide-loaders.jar")), nbDocZipURL);
    }
    private void doTestFindJavadoc(URL binRoot, URL nbDocZipURL) throws Exception {
        Result res = new GlobalJavadocForBinaryImpl().findJavadoc(binRoot);
        assertNotNull("result is not null", res);
        
        URL[] expected = {
            nbDocZipURL,
            new URL(nbDocZipURL, "org-openide-loaders/")
        };
        
        assertEquals("right javadoc for loaders", Arrays.asList(expected), Arrays.asList(res.getRoots()));

    }

    private File generateNbDocZip() throws IOException {
        return FileUtil.toFile(TestFileUtils.writeZipFile(FileUtil.toFileObject(getWorkDir()), "nbdoc.zip", "org-openide-loaders/index.html:"));
    }
}
