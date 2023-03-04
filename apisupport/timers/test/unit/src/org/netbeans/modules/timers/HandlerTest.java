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
package org.netbeans.modules.timers;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.timers.TimesCollectorPeer.Description;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jaroslav Tulach
 */
public class HandlerTest extends NbTestCase {

    public HandlerTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        Install.findObject(Install.class, true).restored();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void tearDown() throws Exception {
    for (FileSystem fs : Repository.getDefault().toArray()) {
            Repository.getDefault().removeFileSystem(fs);
        }

        
        List<Reference> gc = new ArrayList<Reference>();
        
        for (Object key : TimesCollectorPeer.getDefault().getFiles()) {
            gc.add(new WeakReference<Object>(key));
        }
        
        for (Reference<?> reference : gc) {
            assertGC("GC it", reference);
        }

    }
    
    
    
    public void testLoggingMessageWithBundle() throws Exception {
        FileObject dir  = TimesCollectorPeerTest.makeScratchDir(this);
        
        Logger LOG = Logger.getLogger("TIMER.instance.of.my.object");
        LogRecord rec = new LogRecord(Level.FINE, "LOG_Project"); // NOI18N
        rec.setParameters(new Object[] { dir, dir });
        rec.setResourceBundle(ResourceBundle.getBundle(HandlerTest.class.getName()));
        LOG.log(rec);

        Collection<Object> files = TimesCollectorPeer.getDefault().getFiles();
        assertEquals("One object " + files, 1, files.size());
        
        Description descr = TimesCollectorPeer.getDefault().getDescription(files.iterator().next(), "LOG_Project");
        assertNotNull(descr);
        
        if (descr.getMessage().indexOf("My Project") == -1) {
            fail("Localized msg should contain 'My Project': " + descr.getMessage());
        }
    }
    
    public void testLoggingMessageWithBundleAndArg() throws Exception {
        FileObject dir  = TimesCollectorPeerTest.makeScratchDir(this);
        
        Logger LOG = Logger.getLogger("TIMER.instance.of.my.object");
        LogRecord rec = new LogRecord(Level.FINE, "LOG_ProjectWithArg"); // NOI18N
        rec.setParameters(new Object[] { dir, dir, "Lovely" });
        rec.setResourceBundle(ResourceBundle.getBundle(HandlerTest.class.getName()));
        LOG.log(rec);

        Collection<Object> files = TimesCollectorPeer.getDefault().getFiles();
        assertEquals("One object " + files, 1, files.size());
        
        Description descr = TimesCollectorPeer.getDefault().getDescription(files.iterator().next(), "LOG_ProjectWithArg");
        assertNotNull(descr);
        
        if (descr.getMessage().indexOf("My Lovely Project") == -1) {
            fail("Localized msg should contain 'My Lovely Project': " + descr.getMessage());
        }
    }
    
}
