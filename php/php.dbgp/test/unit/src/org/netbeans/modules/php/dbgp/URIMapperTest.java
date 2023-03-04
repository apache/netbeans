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
import org.openide.util.Pair;

/**
 * @author Radek Matous
 */
public class URIMapperTest extends NbTestCase {

    private final URIMapper oneToOneMapper = URIMapper.createOneToOne();
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
        File currentDir = new File(".").getCanonicalFile(); // NOI18N
        webServerURIBase = URI.create(currentDir.toURI().toString() + "/dbgtest2/").normalize(); // NOI18N
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
