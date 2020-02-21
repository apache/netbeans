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
package org.netbeans.modules.cnd.repository.disk;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.FSConverter;
import org.netbeans.modules.cnd.repository.impl.spi.LayerConvertersProvider;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataInputStream;
import org.netbeans.modules.cnd.repository.storage.data.RepositoryDataOutputStream;
import org.netbeans.modules.cnd.repository.test.TestObject;
import org.netbeans.modules.cnd.repository.test.TestObjectCreator;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.RequestProcessor;

/**
 * Test for FilesAccessStrategyImpl
 *
 */
public class FilesAccessStrategyTest extends ModelImplBaseTestCase {

    private static final boolean TRACE = false;
    private static final Random rnd = new Random();

    static {
        //System.setProperty("cnd.repository.files.cache", "4");
        //System.setProperty("cnd.repository.mf.stat", "true");
        //System.setProperty("access.strategy.laps", "5");
        //System.setProperty("access.strategy.threads", "12");
        //System.setProperty("cnd.repository.trace.conflicts", "true");
    }
    private final DiskLayerImpl diskLayerImpl;
    private File tmpDir;
    
    private static final class NoopUnitIDConverter implements UnitsConverter {

        @Override
        public int clientToLayer(int clientUnitID) {
            return clientUnitID;
        }

        @Override
        public int layerToClient(int unitIDInLayer) {
            return unitIDInLayer;
        }
    }

    private static final class NoopFSConverter implements FSConverter {

        @Override
        public FileSystem layerToClient(int fsIdx) {
            return new LocalFileSystem();
        }

        @Override
        public int clientToLayer(FileSystem fileSystem) {
            return 0;
        }
    }    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Repository.startup(0);
    }

    public FilesAccessStrategyTest(String testName) throws IOException {
        super(testName);
        tmpDir = createTempFile("FilesAccessStrategyTest", "", true);
        final LayerDescriptor layerDescriptor = new LayerDescriptor(tmpDir.toURI());
        diskLayerImpl = new DiskLayerImpl(layerDescriptor, new LayeringSupport() {

            @Override
            public List<LayerDescriptor> getLayerDescriptors() {
                return Arrays.asList(layerDescriptor);
            }

            @Override
            public int getStorageID() {
                return 1;
            }

            @Override
            public UnitsConverter getReadUnitsConverter(LayerDescriptor layerDescriptor) {
                return new NoopUnitIDConverter();
            }

            @Override
            public UnitsConverter getWriteUnitsConverter(LayerDescriptor layerDescriptor) {
                return new NoopUnitIDConverter();
            }

            @Override
            public FSConverter getReadFSConverter(LayerDescriptor layerDescriptor) {
                return new NoopFSConverter();
            }

            @Override
            public FSConverter getWriteFSConverter(LayerDescriptor layerDescriptor) {
                return new NoopFSConverter();
            }
        });
        diskLayerImpl.startup(0, false, true);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        removeDirectory(tmpDir);
    }

    public void testSignleThread() throws Exception {

        String dataPath = convertToModelImplDataDir("repository");

        String unit = "FilesAccessStrategyTestUnit";

        Collection<TestObject> objects = new TestObjectCreator(unit, Key.Behavior.LargeAndMutable).createTestObjects(dataPath);

        Collection<TestObject> slice;
        slice = slice(objects, 10);
        write(slice);
        write(slice);
        readAndCompare(slice);
        if (TRACE) {
//            strategy.printStatistics();
        }
//        if (strategy.getCacheSize() >= 10) {
//            assertTrue("Write hit percentage should be ", strategy.getWriteHitPercentage() > 40);
//            assertTrue("", strategy.getReadHitPercentage() > 90);
//        }
        int unitId = Repository.getUnitId(new UnitDescriptor(unit, CndFileUtils.getLocalFileSystem()));
        diskLayerImpl.closeUnit(unitId, false, Collections.<Integer>emptySet(), true);
        assertNoExceptions();
    }
    private volatile boolean proceed;
    private volatile int filled = -1;
    private final Object barrier = new Object();

    private void waitBarrier() {
        synchronized (barrier) {
            try {
                barrier.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    private void notifyBarrier() {
        synchronized (barrier) {
            if (TRACE) {
                System.out.printf("notifyBarrier\n");
            }
            barrier.notifyAll();
        }
    }

    public void testMultyThread() throws Exception {
        String dataPath = convertToModelImplDataDir("repository");
        dataPath = dataPath + "/org"; //NOI18N

        String[] units = new String[]{
            "FilesAccessStrategyTestUnit1", "FilesAccessStrategyTestUnit2",
            "FilesAccessStrategyTestUnit3", "FilesAccessStrategyTestUnit4",
            "FilesAccessStrategyTestUnit5"};

        Collection<TestObject> objectsCollection = new ArrayList<TestObject>(2000);
        for (int i = 0; i < units.length; i++) {
            objectsCollection.addAll(new TestObjectCreator(units[i], Key.Behavior.LargeAndMutable).createTestObjects(dataPath));
        }

        final TestObject[] objects = objectsCollection.toArray(new TestObject[objectsCollection.size()]);

        int lapsCount = Integer.getInteger("access.strategy.laps", 20);
        int readingThreadCount = Integer.getInteger("access.strategy.threads", 6);

        if (TRACE) {
            System.out.printf("\n\ntestMultyThread: %d objects, %d laps, %d reading threads\n\n", objects.length, lapsCount, readingThreadCount);
        }
        final CountDownLatch stopSignal = new CountDownLatch(readingThreadCount);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    if (TRACE) {
                        System.out.printf("%s waiting on barrier\n", Thread.currentThread().getName());
                    }
                    waitBarrier();
                    if (TRACE) {
                        System.out.printf("%s working...\n", Thread.currentThread().getName());
                    }
                    while (proceed) {
                        try {
                            int last = filled;
                            // wait until the first object is written
                            if (last >= 0) {
                                int i = rnd.nextInt(last + 1);
                                Persistent read = read(objects[i].getLayerKey());
                                assertEquals("non equal object for index " + i + ": [0-" + last + "]", objects[i], read);
                            } else {
                                if (TRACE) {
                                    System.out.println("waiting for the first written object");
                                }
                            }
                        } catch (Exception e) {
                            DiagnosticExceptoins.register(e);
                            break;
                        }
                    }
                    if (TRACE) {
                        System.out.printf("%s finished\n", Thread.currentThread().getName());
                    }
                } finally {
                    stopSignal.countDown();
                }
            }
        };

        // starting reader threads
        proceed = true;
        for (int i = 0; i < readingThreadCount; i++) {
            Thread thread = new Thread(r);
            thread.setName("Reader thread " + i);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
        sleep(1000); // wait threads to start

        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                proceed = false;
            }
        }, 60 * 1000);

        loop:
        for (int lap = 0; lap < lapsCount; lap++) {
            if (TRACE) {
                System.out.printf("Writing objects: lap %d\n", lap);
            }
            for (int i = 0; i < objects.length; i++) {
                if (!proceed) {
                    break loop;
                }

                write(objects[i]);

//                strategy.getWriteCapability().write(objects[i].getLayerKey(), );
                if (lap == 0) {
                    filled = i;
                    if (i == 0) {
                        notifyBarrier();
                    }
                    assertNotNull("Read shouldn't return null for an object that has been just written",
                            read(objects[i].getLayerKey()));
                }
            }
        }
        proceed = false;
        try {
            stopSignal.await();
        } catch (InterruptedException ie) {
            DiagnosticExceptoins.register(ie);
        }
        if (TRACE) {
//            strategy.printStatistics();
        }
        for (int i = 0; i < units.length; i++) {
            int unitId = Repository.getUnitId(new UnitDescriptor(units[i], CndFileUtils.getLocalFileSystem()));
            diskLayerImpl.closeUnit(unitId, false, null, true);// testCloseUnit(unitId);
        }
        assertNoExceptions();
    }

    private void write(TestObject object) throws Exception {
        RepositoryDataOutput out = new RepositoryDataOutputStream(
                object.getLayerKey(),
                diskLayerImpl.getWriteCapability(),
                LayerConvertersProvider.getWriteInstance(null, null, null));

        try {
            object.getLayerKey().getPersistentFactory().write(out, object);
        } finally {
            out.commit();
        }
    }

    private void write(Collection<TestObject> objects) throws Exception {
        for (TestObject object : objects) {
            write(object);
        }
    }

    private void readAndCompare(Collection<TestObject> objects) throws Exception {
        for (TestObject object : objects) {
            Persistent read = read(object.getLayerKey());
            assertEquals(object, read);
        }
    }

    private Persistent read(LayerKey key) throws IOException {
        ByteBuffer buffer = diskLayerImpl.getReadCapability().read(key);
        assertNotNull(buffer);
        PersistentFactory factory = key.getPersistentFactory();
        RepositoryDataInput in = new RepositoryDataInputStream(new DataInputStream(new ByteArrayInputStream(buffer.array())));
        return factory.read(in);
    }

    private Collection<TestObject> slice(Collection<TestObject> objects, int size) {
        Collection<TestObject> result = new ArrayList<TestObject>(size);
        int cnt = 0;
        for (TestObject object : objects) {
            result.add(object);
            if (cnt++ >= size) {
                break;
            }
        }
        return result;
    }
}
