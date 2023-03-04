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

package org.openide.filesystems;

import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * Simulate deadlock from issue 48486.
 *
 * @author Radek Matous
 */
public class MIMESupport48486Test extends NbTestCase {
    /**
     * filesystem containing created instances
     */
    private FileSystem lfs;
    private FileObject mimeFo;

    /**
     * Creates new DataFolderTest
     */
    public MIMESupport48486Test(String name) {
        super(name);
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {
        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = TestUtilHid.createLocalFileSystem(getName(), new String[]{"A.opqr", });
        mimeFo = lfs.findResource("A.opqr");
        assertNotNull(mimeFo);
        MockServices.setServices(MamaResolver.class);
        Lookup.getDefault().lookup(MamaResolver.class).fo = mimeFo;
    }

    public void testMimeResolverDeadlock() throws Exception {
        mimeFo.getMIMEType();
    }

    public static final class MamaResolver extends MIMEResolver implements Runnable {
        boolean isRecursiveCall = false;
        FileObject fo = null;

        public void run() {
            assert this.fo != null;
            isRecursiveCall = true;
            fo.getMIMEType();
        }

        public String findMIMEType(FileObject fo) {
            if (!isRecursiveCall) {
                RequestProcessor.getDefault().post(this).waitFinished();
            }
            return null;
        }
    }

}
