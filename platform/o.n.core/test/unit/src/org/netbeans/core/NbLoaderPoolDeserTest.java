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

package org.netbeans.core;

import java.io.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Jaroslav Tulach
 */
public class NbLoaderPoolDeserTest extends NbTestCase {
    private OldStyleLoader oldL;
    private FileObject fo;

    public NbLoaderPoolDeserTest (String testName) {
        super (testName);
    }

    protected @Override void setUp() throws Exception {
        oldL = DataLoader.getLoader(OldStyleLoader.class);
        NbLoaderPool.doAdd(oldL, null, NbLoaderPool.getNbLoaderPool());
        
        fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "x.prop");
    }

    protected @Override void tearDown() throws Exception {
        NbLoaderPool.remove(oldL, NbLoaderPool.getNbLoaderPool());
    }

    public void testOldLoaderThatChangesActionsBecomesModified () throws Exception {
        NbLoaderPool.waitFinished();
        
        DataObject first = DataObject.find(fo);
        assertTrue(first.getLoader().getClass().getName().contains("Default"));

        ExtensionList el = new ExtensionList();
        el.addExtension("prop");
        oldL.setExtensions(el);
        NbLoaderPool.waitFinished();
        
        
        DataObject snd = DataObject.find(fo);
        assertEquals("They are the same as nothing has been notified yet", first, snd);
        
        NbLoaderPool.installationFinished();
        NbLoaderPool.waitFinished();
        
        DataObject third = DataObject.find(fo);
        if (third == snd) {
            fail("They should not be the same: " + third);
        }
    }
    
    public static class OldStyleLoader extends UniFileLoader {
        boolean defaultActionsCalled;
        
        public OldStyleLoader () {
            super(MultiDataObject.class.getName());
        }
        
        protected MultiDataObject createMultiObject(FileObject fo) throws IOException {
            return new MultiDataObject(fo, this);
        }

        @SuppressWarnings("deprecation")
        protected @Override SystemAction[] defaultActions () {
            defaultActionsCalled = true;
            SystemAction[] retValue;
            
            retValue = super.defaultActions();
            return retValue;
        }
        
        
    }
}
