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
package org.netbeans.modules.java.source.queriesimpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.queries.spi.QueryOperationsTestBase;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Tomas Zezula
 */
public class JavaOperationsImplTest extends QueryOperationsTestBase {

    private File wd;

    public JavaOperationsImplTest(final String name) {
        super(name);
    }

    public static Test suite() {
        final TestSuite suite = new NbTestSuite(JavaOperationsImplTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File cacheFolder = new File (getWorkDir(), "cache"); //NOI18N
        cacheFolder.mkdirs();
        CacheFolder.setCacheFolder(FileUtil.toFileObject(cacheFolder));
        CPProvider.srcPath = ClassPathSupport.createClassPath(srcRoot);
        CPProvider.bootPath = ClassPathSupport.createClassPath(System.getProperty("sun.boot.class.path"));
        CPProvider.compilePath = ClassPathSupport.createClassPath(new URL[0]);
        MockServices.setServices(CPProvider.class);
        MockMimeLookup.setInstances(MimePath.parse("text/x-java"), new JavacParserFactory());
    }

    public static class CPProvider implements ClassPathProvider {
        private static ClassPath srcPath;
        private static ClassPath bootPath;
        private static ClassPath compilePath;
        @Override
        public ClassPath findClassPath(
                FileObject file,
                String type) {
            for (FileObject srcRoot : srcPath.getRoots()) {
                if (srcRoot.equals(file) || FileUtil.isParentOf(srcRoot, file)) {
                    if (type == ClassPath.SOURCE) {
                        return srcPath;
                    } else if (type == ClassPath.BOOT) {
                        return bootPath;
                    } else if (type == ClassPath.COMPILE) {
                        return compilePath;
                    }
                }
            }
            return null;
        }
    }

    @Override
    protected final File getWorkDir() throws IOException {
        if (wd == null) {
            wd = new NbTestCase(getName()){}.getWorkDir();
        }
        return wd;
    }
}
