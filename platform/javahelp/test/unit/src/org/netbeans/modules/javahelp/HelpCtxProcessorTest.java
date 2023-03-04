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

package org.netbeans.modules.javahelp;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.api.javahelp.Help;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;
import org.openide.util.test.TestFileUtils;

public class HelpCtxProcessorTest extends NbTestCase {

    public HelpCtxProcessorTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testProcess() throws Exception {
        final AtomicReference<HelpCtx> ctx = new AtomicReference<HelpCtx>();
        final AtomicBoolean showmaster = new AtomicBoolean();
        MockLookup.setLayersAndInstances(new Help() {
            public @Override Boolean isValidID(String id, boolean force) {
                return true;
            }
            public @Override void showHelp(HelpCtx _ctx, boolean _showmaster) {
                ctx.set(_ctx);
                showmaster.set(_showmaster);
            }
            public @Override void addChangeListener(ChangeListener l) {}
            public @Override void removeChangeListener(ChangeListener l) {}
        });
        File fooF = new File(getWorkDir(), "foo.xml");
        TestFileUtils.writeFile(fooF, "<!DOCTYPE helpctx PUBLIC '-//NetBeans//DTD Help Context 1.0//EN' '.../helpcontext-1_0.dtd'><helpctx id='foo' showmaster='true'/>");
        FileObject foo = FileUtil.toFileObject(fooF);
        assertNotNull(foo);
        Lookup lkp = DataObject.find(foo).getLookup();
        InstanceCookie i = lkp.lookup(InstanceCookie.class);
        assertNotNull(lkp.lookupAll(Object.class).toString(), i);
        Action a = (Action) i.instanceCreate();
        a.actionPerformed(null);
        assertEquals(new HelpCtx("foo"), ctx.get());
        assertTrue(showmaster.get());
    }

}
