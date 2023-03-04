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

package org.netbeans.modules.profiler.actions;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.junit.Test;
import org.netbeans.junit.Log;
import org.netbeans.modules.profiler.ui.NpsDataObject;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import static org.junit.Assert.*;
import org.netbeans.modules.sampler.Sampler;

/** Shows how o.n.core uses the SelfSamplerAction to start and stop self profiling.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @author Tomas Hurka
 */
public class SelfSamplerActionTest {

    public SelfSamplerActionTest() {
    }

    @Test
    public void testSelfProfileToStream() throws Exception {
        Sampler sampler = Sampler.createManualSampler("testprofile");
        assertTrue("sampler instance", sampler != null);

        sampler.start();
        Thread.sleep(1000);
        assertSamplerThread("sampler-testprofile shall be there", true);

        FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("slow.nps");
        OutputStream os = fo.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        sampler.stopAndWriteTo(dos);
        dos.close();

        if (fo.getSize() < 100) {
            fail("The file shall have real content: " + fo.getSize());
        }

        DataObject dataObject = DataObject.find(fo);
        assertEquals("Nps DataObject", NpsDataObject.class, dataObject.getClass());
        OpenCookie oc = dataObject.getLookup().lookup(OpenCookie.class);
        assertNotNull("Open cookie exists", oc);

        CharSequence log = Log.enable("", Level.WARNING);
        oc.open();

        if (log.length() > 0) {
            fail("There shall be no warnings:\n" + log);
        }

        assertSamplerThread("no sampler- thread shall be there", false);
    }

    @Test
    public void testSelfProfileCancel() throws Exception {
        Sampler sampler = Sampler.createManualSampler("testprofile");
        assertTrue("sampler instance", sampler != null);

        sampler.start();
        Thread.sleep(1000);
        assertSamplerThread("sampler-testprofile shall be there", true);

        sampler.cancel();
        Thread.sleep(1000);

        assertSamplerThread("no sampler- thread shall be there", false);
    }

    private void assertSamplerThread(String msg, boolean exist) {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().startsWith("sampler-")) {
                assertTrue(msg + " There is " + t.getName() + " thread", exist);
                return;
            }
        }
        assertFalse(msg + " There is no sampler- thread", exist);
    }


}
