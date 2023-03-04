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

package org.apache.tools.ant.module.xml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.loader.AntProjectDataObject;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.test.MockChangeListener;

// XXX testBasicParsing
// XXX testMinimumChangesFired

/**
 * Test {@link AntProjectSupport} parsing functionality.
 * @author Jesse Glick
 */
public class AntProjectSupportTest extends NbTestCase {
    
    public AntProjectSupportTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    private FileObject scratch;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File scratchF = getWorkDir();
        scratch = FileUtil.toFileObject(scratchF);
        assertNotNull("FO for " + scratchF, scratch);
    }

    @RandomlyFails // NB-Core-Build #3638
    public void testInitiallyInvalidScript() throws Exception {
        final FileObject fo = scratch.createData("x.ant");
        assertEquals("it is an APDO", AntProjectDataObject.class, DataObject.find(fo).getClass());
        AntProjectCookie apc = new AntProjectSupport(fo);
        assertNull("invalid", apc.getDocument());
        assertNotNull("invalid", apc.getParseException());
        MockChangeListener l = new MockChangeListener();
        apc.addChangeListener(l);
        //l.expectNoEvents(5000);
        fo.getFileSystem().runAtomicAction(new AtomicAction() {
            public void run() throws IOException {
                OutputStream os = fo.getOutputStream();
                try {
                    os.write("<project default='x'><target name='x'/></project>".getBytes(StandardCharsets.UTF_8));
                } finally {
                    os.close();
                }
            }
        });
        l.expectEvent(5000);
        /* XXX parseException usually turns out to be non-null here still; too many threads running around, who knows what is going on
        Throwable x = apc.getParseException();
        if (x != null) {
            throw (AssertionFailedError) new AssertionFailedError("now valid (no exc)").initCause(x);
        }
        Document doc = apc.getDocument();
        assertNotNull("now valid (have doc)", doc);
        assertEquals("one target", 1, doc.getElementsByTagName("target").getLength());
         */
    }
    
}
