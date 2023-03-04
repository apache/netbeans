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
package org.netbeans.api.java.source.gen;

import java.io.File;
import java.io.IOException;
import javax.swing.JEditorPane;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.core.startup.Main;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.save.Reindenter;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author arusinha
 *
 * Base test class to check tree rewrite for specific JDK source level.
 */
public class TreeRewriteTestBase extends NbTestCase {

    // Default Source level
    protected String sourceLevel = "1.8";  // NOI18N
    private File testFile;

    public TreeRewriteTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();

        // ensure JavaKit is present, so that NbEditorDocument is eventually created.
        // it handles PositionRefs differently than PlainDocument/PlainEditorKit.
        MockMimeLookup.setInstances(MimePath.get("text/x-java"),
                new Reindenter.Factory(), new JavaKit());

        SourceUtilsTestUtil.prepareTest(
                new String[]{
                    "org/netbeans/modules/java/project/ui/layer.xml",
                    "org/netbeans/modules/project/ui/resources/layer.xml"
                },
                new Object[]{}
        );

        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);

        TestUtil.setupEditorMockServices();
        Main.initializeURLFactory();

    }

    protected void prepareTest(String filename, String code) throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot = workFO.createFolder("build");
        FileObject packageRoot = sourceRoot.createFolder("test");

        FileObject testSource = packageRoot.createData(filename + ".java");

        assertNotNull(testSource);

        testFile = FileUtil.toFile(testSource);

        TestUtilities.copyStringToFile(FileUtil.toFile(testSource), code);

        SourceUtilsTestUtil.setSourceLevel(testSource, sourceLevel);
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, CacheFolder.getCacheFolder(), new FileObject[0]);
        //re-index, in order to find classes-living-elsewhere
        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.getURL(), null);

    }

    File getTestFile() {
        assertNotNull(testFile);
        return testFile;
    }

    JavaSource getJavaSource() throws IOException {
        FileObject testSourceFO = FileUtil.toFileObject(getTestFile());
        return JavaSource.forFileObject(testSourceFO);
    }
}
