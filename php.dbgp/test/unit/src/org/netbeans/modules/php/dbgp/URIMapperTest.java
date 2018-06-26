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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.dbgp;

import java.util.LinkedList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import static org.junit.Assert.*;
import org.openide.util.Pair;

/**
 * @author Radek Matous
 */
public class URIMapperTest extends NbTestCase {

    final private URIMapper oneToOneMapper = URIMapper.createOneToOne();
    private URIMapper basesMapper;
    private URIMapper[] allMappers;
    private URI webServerURIBase;
    private File sourceFolderBase;
    private File webServerURIBaseFile;

    public URIMapperTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceFolderBase = getWorkDir();
        String currentDir = new File(".").getCanonicalPath(); // NOI18N
        webServerURIBase = URI.create("file://" + currentDir + "/dbgtest2/");//NOI18N
        webServerURIBaseFile = new File(webServerURIBase);
        webServerURIBaseFile.mkdir();

        basesMapper = URIMapper.createBasedInstance(webServerURIBase, sourceFolderBase);
        allMappers = new URIMapper[]{basesMapper, oneToOneMapper};
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        webServerURIBaseFile.delete();
    }

    public void testOneToOneInstance() throws IOException {
        assertEquals(sourceFolderBase.toURI(), oneToOneMapper.toWebServerURI(sourceFolderBase));
        assertEquals(new File(webServerURIBase), oneToOneMapper.toSourceFile(webServerURIBase));
    }

    public void testBasedInstance() throws IOException {
        String relPath = "a/b/c/index.php";//NOI18N
        assertEquals(webServerURIBase.resolve(relPath), basesMapper.toWebServerURI(new File(sourceFolderBase, relPath)));
        assertEquals(new File(sourceFolderBase, relPath), basesMapper.toSourceFile(webServerURIBase.resolve(relPath)));

        assertEquals(webServerURIBase, basesMapper.toWebServerURI(sourceFolderBase));
        assertEquals(sourceFolderBase, basesMapper.toSourceFile(webServerURIBase));
    }

    public void testSymetricToRemoteURI() throws IOException {
        String[] pathItems = new String[]{"a/b/c/d/index.php", "index.php", "func/calc.php"};
        List<URIMapper> mappers = new ArrayList<URIMapper>();
        Collections.addAll(mappers, allMappers);

        for (String path : pathItems) {
            Collections.addAll(mappers, createMergedInstance(path));
            Collections.addAll(mappers, createMergedInstance(path).addAsFirstMapper(basesMapper));
            Collections.addAll(mappers, createMergedInstance(path).addAsFirstMapper(oneToOneMapper));
            Collections.addAll(mappers, createMergedInstance(path).addAsFirstMapper(oneToOneMapper).addAsFirstMapper(basesMapper));
            for (URIMapper mapper : mappers) {
                File localFile = new File(sourceFolderBase, path);//NOI18N
                assertNotNull(mapper.toWebServerURI(localFile));
                assertNotNull(mapper.toSourceFile(mapper.toWebServerURI(localFile)));
                assertEquals(localFile, mapper.toSourceFile(mapper.toWebServerURI(localFile)));

                localFile = sourceFolderBase;
                assertNotNull(mapper.toWebServerURI(localFile));
                assertNotNull(mapper.toSourceFile(mapper.toWebServerURI(localFile)));
                assertEquals(localFile, mapper.toSourceFile(mapper.toWebServerURI(localFile)));
            }
        }
    }

    public void testSymetricToLocalFile() throws IOException {
        String[] pathItems = new String[]{"a/b/c/d/index.php", "index.php", "func/calc.php"};
        List<URIMapper> mappers = new ArrayList<URIMapper>();
        Collections.addAll(mappers, allMappers);
        for (String path : pathItems) {
            Collections.addAll(mappers, createMergedInstance(path));
            Collections.addAll(mappers, createMergedInstance(path).addAsFirstMapper(basesMapper));
            Collections.addAll(mappers, createMergedInstance(path).addAsFirstMapper(oneToOneMapper));
            Collections.addAll(mappers, createMergedInstance(path).addAsFirstMapper(oneToOneMapper).addAsFirstMapper(basesMapper));

            for (URIMapper mapper : mappers) {
                URI remoteURI = webServerURIBase.resolve(path);//NOI18N
                assertNotNull(mapper.toSourceFile(remoteURI));
                assertNotNull(mapper.toWebServerURI(mapper.toSourceFile(remoteURI)));
                assertEquals(mapper.getClass().getName() + ": " + remoteURI,
                        remoteURI, mapper.toWebServerURI(mapper.toSourceFile(remoteURI)));

                remoteURI = webServerURIBase;
                assertNotNull(mapper.toSourceFile(remoteURI));
                assertNotNull(mapper.toWebServerURI(mapper.toSourceFile(remoteURI)));
                assertEquals(remoteURI, mapper.toWebServerURI(mapper.toSourceFile(remoteURI)));
            }
        }
    }

    private URIMapper.MultiMapper createMergedInstance(String path) throws IOException {
        URI webServerURI = new File(new File(webServerURIBase), path).toURI();
        File sourceFile = new File(sourceFolderBase, path);
        return URIMapper.createMultiMapper(webServerURI,
                FileUtil.createData(sourceFile),
                FileUtil.toFileObject(sourceFolderBase), new LinkedList<Pair<String, String>>());
    }
}
