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
package org.netbeans.api.java.source;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class TestUtilitiesTest extends NbTestCase {
    
    public TestUtilitiesTest (final String name) {
        super (name);
    }
    
    private static ClassPath boot;
    private static ClassPath compile;
    private static ClassPath source;
    
    public void testWaitScanFinished () throws Exception {        
        final File wf = getWorkDir();
        final File cache = FileUtil.normalizeFile(new File (wf,"cache"));
        cache.mkdirs();
        final File sourceDir = FileUtil.normalizeFile(new File(wf,"src"));
        sourceDir.mkdirs();        
        boot = TestUtilities.createBootClassPath ();
        compile = ClassPathSupport.createClassPath(new URL[0]);
        source = ClassPathSupport.createClassPath(new URL[]{Utilities.toURI(sourceDir).toURL()});
        TestUtilities.setCacheFolder(cache);
        IndexingManager.getDefault().refreshIndexAndWait(sourceDir.toURL(), null);
        assertTrue(TestUtilities.waitScanFinished(10, TimeUnit.SECONDS));
    }
    
    
    public static class CPProvider implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            if (ClassPath.BOOT.equals(type)) {
                return boot;
            }
            else if (ClassPath.COMPILE.equals(type)) {
                return compile;
            }
            else if (ClassPath.SOURCE.equals(type)) {
                return source;
            }
            return null;
        }
    }
    
    public static class SFBQ implements SourceLevelQueryImplementation {

        public String getSourceLevel(FileObject javaFile) {
            return "1.5";
        }
        
    }

}
