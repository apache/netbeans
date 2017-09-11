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

package org.openide.loaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.util.Lookup;


/**
 *
 * @author Matteo Di Giovinazzo
 */
@RandomlyFails // NB-Core-Build #1114, #1115
public class FileObjectInLookupByMatteoTest extends NbTestCase {

    private static final int PRIMARY_FILES_COUNT = 10;
    private static final int SECONDARY_FILES_COUNT = 1000;
    /*@GuardedBy("countGuard")*/
    private int count = 0;
    private final Object countGuard = new Object();
    private final Object SIGNAL = new Object();
    private String basename;
    private FileObject dir;
    static volatile Logger LOG;

    public FileObjectInLookupByMatteoTest(String testName) {
        super(testName);
    }

    protected boolean initCookieSet() {
        return false;
    }

    @Override
    protected int timeOut() {
        return 60000;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    
    @Override
    @SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());

        // clear
        clearWorkDir();

        // set MIMEType
        MockServices.setServices(MR.class);

        DataLoader loader = DataLoader.getLoader(TestDataLoader.class);
        AddLoaderManuallyHid.addRemoveLoader(loader, true);

        List<DataLoader> list = Collections.list(DataLoaderPool.getDefault().allLoaders());
        assertTrue(list.contains(TestDataLoader.getLoader(TestDataLoader.class)));

        // create all files
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        dir = fs.getRoot();

        basename = "test";

        LOG.info("Creating 10000 files...");
        for (int i = 0; i < PRIMARY_FILES_COUNT; i++) {
            String name = String.format(basename + "%02d", i);
            dir.createData(name, TestDataLoader.PRIMARY_EXTENSION);

            for (int j = 0; j < SECONDARY_FILES_COUNT; j++) {
                String nameSecondary = String.format(name + ".%03d", j);
                dir.createData(nameSecondary, TestDataLoader.SECONDARY_EXTENSION);
            }
        }
        LOG.info("Files created");
    }

    @Override
    protected void tearDown() throws Exception {
        // clear
        clearWorkDir();
    }

    public static final class MR extends MIMEResolver {

        @Override
        public String findMIMEType(FileObject fo) {
            LOG.log(Level.INFO, "findMIMEType: {0}", fo);
            if (TestDataLoader.PRIMARY_EXTENSION.equals(fo.getExt()) ||
                    TestDataLoader.SECONDARY_EXTENSION.equals(fo.getExt())) {
                LOG.info("Mime OK");
                return TestDataLoader.REQUIRED_MIME;
            }
            LOG.info("No mime");
            return null;
        }
    }


    public void testSecondaryEntriesSlowness() throws Exception {
        // do test
        for (int i = 0; i < PRIMARY_FILES_COUNT; i++) {
            String name = String.format(basename + "%02d", i);
            FileObject fo = dir.getFileObject(name, TestDataLoader.PRIMARY_EXTENSION);

            final DataObject dobj = DataObject.find(fo);
            assertNotNull(dobj);
            assertTrue(dobj instanceof MultiDataObject);
            MultiDataObject mdo = (MultiDataObject) dobj;

            if (initCookieSet()) {
                assertNull("Not initialized yet", mdo.getCookieSet(false));
                assertNotNull("Really created", mdo.getCookieSet());
                assertNotNull("Initialized now", mdo.getCookieSet(false));
            }

            mdo.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();

                    if (DataObject.PROP_FILES.equals(name)) {
                        synchronized (countGuard) {
                            LOG.info("propertyChange: DO=" + dobj.getName() + ", PROP=" + evt.getPropertyName());
                            if ((++count) >= (PRIMARY_FILES_COUNT * SECONDARY_FILES_COUNT)) {
                                synchronized (SIGNAL) {
                                    SIGNAL.notifyAll();
                                }
                            } else {
                                LOG.info("Not enough fired events: " + count);
                            }
                        }
                    }
                }
            });

            // simulate the build of children nodes
            // with this call that triggers the start of firing
            // PropertyChangeEvents DataObject.PROP_FILES
            Set<Entry> entries = mdo.secondaryEntries();
            assertEquals(SECONDARY_FILES_COUNT, entries.size());
        }


        long start = System.currentTimeMillis();
        // wait until all PropertyChangeEvents are fired
        LOG.info("Waiting for " + (PRIMARY_FILES_COUNT * SECONDARY_FILES_COUNT) + " events...");
        synchronized (SIGNAL) {
            while (count < (PRIMARY_FILES_COUNT * SECONDARY_FILES_COUNT)) {
                int prev = count;
                try {
                    SIGNAL.wait(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LOG.info("No event in 3s check: " + prev + " == " + count);
                if (count == prev) {
                    LOG.info("Equals. About to break");
                    break;
                }
            }
        }
        if (count < 10) {
            fail("Too small amount of events. There is at least 10 data objects, but was only: " + count);
        }

        long time = (System.currentTimeMillis() - start);
        System.err.println(count + " events have been fired in " + time + " ms!");
        LOG.info(count + " events have been fired in " + time + " ms!");

        // assume maximum 10 seconds!
        assertTrue("Failed Test because the event firing took more than 10 seconds!!", time < 10000);
    }

    public static class TestDataLoader extends MultiFileLoader {

        public static final String PRIMARY_EXTENSION = "primary"; // NOI18N
        public static final String SECONDARY_EXTENSION = "secondary"; // NOI18N
        public static final String REQUIRED_MIME = "text/x-primary"; // NOI18N
        private static final Pattern SECONDARY_PATTERN = Pattern.compile("(.*)\\.[0-9]{3}\\." + SECONDARY_EXTENSION);
        private static final long serialVersionUID = 1L;

        public TestDataLoader() {
            super("org.openide.loaders.DataObject"); // NOI18N
        }

        @Override
        protected String defaultDisplayName() {
            return "Test Data Loader"; // NOI18N
        }

        @Override
        protected void initialize() {
            super.initialize();
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject(primaryFile, this) {

                @Override
                public Lookup getLookup() {
                    return getCookieSet().getLookup();
                }
            };
        }

        @Override
        protected String actionsContext() {
            return "Loaders/" + REQUIRED_MIME + "/Actions"; // NOI18N
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo) {

            if (fo.isFolder()) {
                return null;
            }

            // check if it's itself the primary file
            String ext = fo.getExt();
            if (ext.equalsIgnoreCase(PRIMARY_EXTENSION)) {
                return fo;
            }

            // check if it's a secondary entry
            String completeFileName = fo.getNameExt();
            Matcher m = SECONDARY_PATTERN.matcher(completeFileName);
            if (m.find()) {
                String primaryName = m.group(1);
                FileObject primaryFO = fo.getParent().getFileObject(primaryName, PRIMARY_EXTENSION);
                return primaryFO;
            }

            return null;
        }

        @Override
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject mdo, FileObject pf) {
            return new FileEntry(mdo, pf);
        }

        @Override
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject mdo, FileObject pf) {
            return new FileEntry(mdo, pf) {

                @Override
                public FileObject copy(FileObject fo, String suffix) throws IOException {
                    return null;
                }
            };
        }
    }
}
