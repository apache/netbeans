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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.repository.disk;

import org.netbeans.modules.cnd.repository.test.TestObject;
import org.netbeans.modules.cnd.repository.test.TestObjectCreator;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.repository.impl.spi.FSConverter;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.UnitsConverter;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;

/**
 * A test for DoubleFileStorage defragmentation
 */
public class DefragmentationTest extends NbTestCase {

    private static final boolean TRACE = false;

    public DefragmentationTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
	super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
	super.tearDown();
    }

    @Override
    protected int timeOut() {
        return 500000;
    }
    
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

    private DoubleFileStorage createStorage() throws IOException {
        File file = new File(getWorkDir(), "double_file_storage.dat");
        final LayerDescriptor layerDescriptor = new LayerDescriptor(file.toURI());
        DoubleFileStorage dfs = new DoubleFileStorage(file, layerDescriptor, new LayeringSupport() {

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
        dfs.open(true);
        return dfs;
    }

    private void fillData(DoubleFileStorage dfs) throws IOException {
        String dataPath = ModelImplBaseTestCase.convertToModelImplDataDir(getDataDir(), "repository");
        Collection<TestObject> objects = new TestObjectCreator().createTestObjects(dataPath);
        for (int i = 0; i < 3; i++) {
            for (TestObject obj : objects) {
                dfs.write(obj.getLayerKey(), ByteBuffer.wrap(obj.toString().getBytes()));
            }
        }
    }

    private DoubleFileStorage createAndFillStorage() throws IOException {
        DoubleFileStorage dfs = createStorage();
        fillData(dfs);
        return dfs;
    }

    public void testFullDeframentation() throws IOException {
        DoubleFileStorage dfs = createAndFillStorage();
        assertTrue(dfs.getFragmentationPercentage() > 50);
        if (TRACE) {
            System.out.printf("--- Before defragmentation\n");
            dfs.dumpSummary(System.out);
        }
        dfs.maintenance(0);
        if (TRACE) {
            System.out.printf("--- After defragmentation\n");
            dfs.dumpSummary(System.out);
        }
        assertTrue(dfs.getFragmentationPercentage() == 0);
    }

    public void testPartialDeframentation() throws IOException {
        DoubleFileStorage dfs = createAndFillStorage();
        assertTrue(dfs.getFragmentationPercentage() > 50);
        long timeToDefragment = System.currentTimeMillis();
        dfs.maintenance(0);
        timeToDefragment = System.currentTimeMillis() - timeToDefragment;
        if (TRACE) {
            System.err.printf("Full defragmentation took %d ms\n", timeToDefragment);
        }

        dfs = createAndFillStorage();
        long slice = Math.max(timeToDefragment / 100, 1);
        long count = 1000;

        for (int i = 0; i < count; i++) {
            int oldFragmentation = dfs.getFragmentationPercentage();
            dfs.maintenance(slice);
            int newFragmentation = dfs.getFragmentationPercentage();
            if (TRACE) {
                System.err.printf("Partial defragmentation %4d: %d -> %d\n", i, oldFragmentation, newFragmentation);
            }
            if (newFragmentation == 0) {
                break;
            }
        }
        assertTrue(dfs.getFragmentationPercentage() == 0);
    }
}
