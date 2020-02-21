/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.repository.layering;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.test.ModelBasedTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptorProvider;
import org.netbeans.modules.cnd.repository.util.RepositoryTestSupport;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import static org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase.createTempFile;
import static org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase.removeDirectory;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public class RepositoryLayeringTest extends TraceModelTestBase {

    private static File[] layerFiles;
    private static final Object layerFilesLock = new Object();
    private File projectRoot = null;
    private File dump1 = null;
    private File dump2 = null;
    private File L1 = null;
    private File L2 = null;

    @ServiceProvider(service = LayerDescriptorProvider.class, position = 10)
    public static class TestLayerDescriptorProvider implements LayerDescriptorProvider {

        @Override
        public List<LayerDescriptor> getLayerDescriptors(UnitDescriptor unitDescriptor) {
            synchronized (layerFilesLock) {
                if (layerFiles == null || layerFiles.length == 0) {
                    return null;
                }
                List<LayerDescriptor> res = new ArrayList<LayerDescriptor>(layerFiles.length);
                for (File file : layerFiles) {
                    res.add(new LayerDescriptor(Utilities.toURI(file)));
                }
                return res;
            }
        }
    }

    public RepositoryLayeringTest(String testName) {
        super(testName);
    }

    private static File[] setLayers(File... files) {
        synchronized (layerFilesLock) {
            File[] oldLayers = layerFiles;
            layerFiles = (files == null) ? null : files.clone();
            return oldLayers;
        }
    }

    @Override
    protected void setUp() throws Exception {
        projectRoot = getDataFile("quote_nosyshdr");

        dump1 = new File(getWorkDir(), getName() + "_L1.dat");
        dump2 = new File(getWorkDir(), getName() + "_L1_L2.dat");

        L1 = createTempFile("L1_", ".reposotiry", true);
        L2 = createTempFile("L2_", ".reposotiry", true);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        removeDirectory(L1);
        removeDirectory(L2);
        dump1.delete();
        dump2.delete();
    }

    @Override
    protected File getTestCaseDataDir() {
        String dataPath = convertToModelImplDataDir("repository");
        String filePath = "common";
        return Manager.normalizeFile(new File(dataPath, filePath));
    }

    private void parseProject(File projectRoot, File dump, File... layerFiles) throws Exception {
        File err = new File(getWorkDir(), getName() + ".err");
        File[] prevLayers = setLayers(layerFiles);
        try {

            PrintStream streamOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(dump)));
            final PrintStream streamErr = new FilteredPrintStream(new BufferedOutputStream(new FileOutputStream(err)));

            DiagnosticExceptoins.Hook hook = new DiagnosticExceptoins.Hook() {
                @Override
                public void exception(Throwable thr) {
                    thr.printStackTrace(streamErr);
                }
            };
            DiagnosticExceptoins.setHook(hook);

            doTest(new String[]{projectRoot.getAbsolutePath()}, streamOut, streamErr);

            StringBuilder fileContent = new StringBuilder();
            boolean res = RepositoryTestSupport.grep("(AssertionError)|(Exception)", err, fileContent);
            if (res) {
                assertFalse("Errors on in " + err.getAbsolutePath() + "\n" + fileContent, true);
            }
            RepositoryTestSupport.dumpCsmProject(getCsmProject(), streamOut, true);
            streamOut.close();
            streamErr.close();
            err.delete(); // in case of error, don't delete file => delete here, not in finally
        } finally {
            setLayers(prevLayers);
        }
    }

    public void testTwoLayers() throws Exception {
        final AtomicInteger parseCount = new AtomicInteger(0);
        CsmProgressListener progressListener = new CsmProgressAdapter() {
            @Override
            public void fileAddedToParse(CsmFile file) {
                parseCount.incrementAndGet();
            }
        };

        CsmListeners.getDefault().addProgressListener(progressListener);

        parseProject(projectRoot, dump1, L2);

        System.err.printf("Parse count %d\n", parseCount.get());
        resetProject();

        Repository.shutdown();
        Repository.startup(ModelBasedTestCase.getPersistenceVersion());
        parseCount.set(0);

        parseProject(projectRoot, dump2, L1, L2);
        assertEquals("Parse count with 2 layers", 0, parseCount.get());

        if (CndCoreTestUtils.diff(dump1, dump2, null)) {
            fail("OUTPUT Difference between diff " + dump1 + " " + dump2); // NOI18N
        }
                
    }
}
