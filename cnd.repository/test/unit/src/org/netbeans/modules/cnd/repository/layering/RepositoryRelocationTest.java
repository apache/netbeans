/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.layering;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.test.ModelBasedTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.repository.api.FilePath;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptorProvider;
import org.netbeans.modules.cnd.repository.spi.RepositoryPathMapperImplementation;
import org.netbeans.modules.cnd.repository.spi.UnitDescriptorsMatcherImplementation;
import org.netbeans.modules.cnd.repository.util.RepositoryTestSupport;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import static org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase.createTempFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 */
public class RepositoryRelocationTest extends TraceModelTestBase {

    @ServiceProvider(service = LayerDescriptorProvider.class, position = 10)
    public static class TestLayerDescriptorProvider implements LayerDescriptorProvider {

        private static AtomicBoolean active = new AtomicBoolean();

        @Override
        public List<LayerDescriptor> getLayerDescriptors(UnitDescriptor unitDescriptor) {
            if (active.get()) {
                String path = unitDescriptor.getName().toString();
                assert path.endsWith("/N/") || path.endsWith("/L/");
                path = path.substring(0, path.length() - 2);
                File projectRootFile = new File(path);
                File cacheDir = new File(projectRootFile.getParent(), "cache");
                cacheDir.mkdirs();
                return Collections.singletonList(new LayerDescriptor(Utilities.toURI(cacheDir)));
            } else {
                return null;
            }
        }
    }

    @ServiceProviders({
        @ServiceProvider(service = UnitDescriptorsMatcherImplementation.class, position = 10),
        @ServiceProvider(service = RepositoryPathMapperImplementation.class, position = 10)})
    public static class TestUnitDescriptorsMatcher implements UnitDescriptorsMatcherImplementation, RepositoryPathMapperImplementation {

        private static AtomicBoolean active = new AtomicBoolean();

        @Override
        public boolean matches(UnitDescriptor descriptor1, UnitDescriptor descriptor2) {
            if (active.get()) {
                String path1 = descriptor1.getName().toString().replaceAll("quote_nosyshdr_[12]", "quote_nosyshdr_X");
                String path2 = descriptor2.getName().toString().replaceAll("quote_nosyshdr_[12]", "quote_nosyshdr_X");
                return path1.equals(path2);
            } else {
                return false;
            }
        }

        @Override
        public CharSequence map(UnitDescriptor descriptor1, FilePath sourceFilePath) {
            if (active.get()) {
                String path = sourceFilePath.getPath();
                return path.replaceAll("quote_nosyshdr_1", "quote_nosyshdr_2");
            } else {
                return null;
            }
        }


        @Override
        public UnitDescriptor destinationDescriptor(FileSystem targetFileSystem, UnitDescriptor sourceUnitDescriptor) {
            return new UnitDescriptor(sourceUnitDescriptor.getName().toString().replaceAll("quote_nosyshdr_1", "quote_nosyshdr_2"), targetFileSystem);
        }

        @Override
        public UnitDescriptor sourceDescriptor(FileSystem targetFileSystem, UnitDescriptor destinationDescriptor) {
            return new UnitDescriptor(destinationDescriptor.getName().toString().replaceAll("quote_nosyshdr_1", "quote_nosyshdr_2"), targetFileSystem);
        }
    }

    public RepositoryRelocationTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); //To change body of generated methods, choose Tools | Templates.
        TestUnitDescriptorsMatcher.active.set(true);
        TestLayerDescriptorProvider.active.set(true);
    }

    @Override
    protected void tearDown() throws Exception {
        TestLayerDescriptorProvider.active.set(false);
        TestUnitDescriptorsMatcher.active.set(false);
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected File getTestCaseDataDir() {
        String dataPath = convertToModelImplDataDir("repository");
        String filePath = "common";
        return Manager.normalizeFile(new File(dataPath, filePath));
    }

    private void parseProject(File projectRoot, File dump) throws Exception {

        File err = new File(getWorkDir(), getName() + ".err");
        File cacheFile = new File(projectRoot, "nbproject/cache");
        cacheFile.mkdirs();
        assertTrue("Can't create cache file " + cacheFile, cacheFile.exists());

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
    }

    public void testRelocation() throws Exception {

        final AtomicInteger parseCount = new AtomicInteger(0);
        CsmProgressListener progressListener = new CsmProgressAdapter() {
            @Override
            public void fileAddedToParse(CsmFile file) {
                parseCount.incrementAndGet();
            }
        };
        CsmListeners.getDefault().addProgressListener(progressListener);

        File tempBaseDir = createTempFile("test_relocation", "", true);

        try {

            File projectSrc = getDataFile("quote_nosyshdr");

            File projectSrcRoot1 = getDataFile("quote_nosyshdr_1/src");
            File projectSrcRoot2 = getDataFile("quote_nosyshdr_2/src");
            copyDirectory(projectSrc, projectSrcRoot1);

            File dump1 = new File(getWorkDir(), getName() + "_1.dat");
            File dump12 = new File(getWorkDir(), getName() + "_12.dat");
            File dump2 = new File(getWorkDir(), getName() + "_2.dat");

            parseProject(projectSrcRoot1, dump1);

            System.err.printf("Parse count %d\n", parseCount.get());
            resetProject();

            Repository.shutdown();
            Repository.startup(ModelBasedTestCase.getPersistenceVersion());

            parseCount.set(0);

            copyDirectory(projectSrcRoot1.getParentFile(), projectSrcRoot2.getParentFile());

            parseProject(projectSrcRoot2, dump2);
            assertEquals("Parse count after reloaction ", 0, parseCount.get());
            {
                // replace all "quote_nosyshdr_1" -> "quote_nosyshdr_2" in first dump
                List<String> asLines = FileUtil.toFileObject(dump1).asLines();
                List<String> toWrite = new ArrayList<String>();
                for(String s : asLines) {
                    if (s.indexOf("quote_nosyshdr_1") > 0) {
                        s = s.replace("quote_nosyshdr_1", "quote_nosyshdr_2");
                    }
                    toWrite.add(s);
                }
                FileObject fo = FileUtil.toFileObject(getWorkDir()).createData(dump12.getName());
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream()));
                for(String s : toWrite) {
                    w.write(s);
                    w.newLine();
                }
                w.close();
            }

            if (CndCoreTestUtils.diff(dump12, dump2, null)) {
                StringBuilder buf = new StringBuilder("OUTPUT Difference between diff " + dump12 + " " + dump2);
                File diffErrorFile = new File(dump12.getAbsolutePath() + ".diff");
                CndCoreTestUtils.diff(dump12, dump2, diffErrorFile);
                showDiff(diffErrorFile, buf);
                fail(buf.toString());
            }

            dump1.delete();
            dump12.delete();
            dump2.delete();
        } finally {
            removeDirectory(tempBaseDir);
        }
    }
}
