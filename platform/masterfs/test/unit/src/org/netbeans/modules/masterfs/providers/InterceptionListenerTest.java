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

package org.netbeans.modules.masterfs.providers;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Radek Matous
 */
public class InterceptionListenerTest extends NbTestCase  {
    private InterceptionListenerImpl iListener;
    static {
        MockServices.setServices(InterceptionListenerTest.AnnotationProviderImpl.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        iListener = lookupImpl();
        assertNotNull(iListener);
        iListener.clear();
        clearWorkDir();
    }

    private InterceptionListenerImpl lookupImpl() {
        Lookup.Result result = Lookups.metaInfServices(Thread.currentThread().getContextClassLoader()).
                lookup(new Lookup.Template(BaseAnnotationProvider.class));
        Collection all = result.allInstances();
        for (Iterator it = all.iterator(); it.hasNext();) {
            BaseAnnotationProvider ap = (BaseAnnotationProvider) it.next();
            InterceptionListener iil = ap.getInterceptionListener();
            if (iil != null && !(iil instanceof ProvidedExtensions)) {
                return (InterceptionListenerImpl)iil;
            }            
        }
        return null;
    }
    
    public InterceptionListenerTest(String testName) {
        super(testName);
    }
    
    
    public void testBeforeCreate() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        assertEquals(0,iListener.beforeCreateCalls);
        assertEquals(0,iListener.createSuccessCalls);
        
        assertNotNull(fo.createData("aa"));
        assertEquals(1,iListener.beforeCreateCalls);
        assertEquals(1,iListener.createSuccessCalls);
        
        iListener.clear();
        try {
            assertEquals(0,iListener.createSuccessCalls);
            assertEquals(0,iListener.createFailureCalls);
            assertNotNull(fo.createData("aa"));
            fail();
        } catch (IOException ex) {
            assertEquals(0,iListener.createSuccessCalls);
            assertEquals(1,iListener.createFailureCalls);
        }
    }
    
    public void testBeforeDelete() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        FileObject toDel = fo.createData("aa");
        assertNotNull(toDel);
        iListener.clear();
        
        assertNotNull(iListener);
        assertEquals(0,iListener.beforeDeleteCalls);
        assertEquals(0,iListener.deleteSuccessCalls);
        toDel.delete();
        assertFalse(toDel.isValid());
        assertEquals(1,iListener.beforeDeleteCalls);
        assertEquals(1,iListener.deleteSuccessCalls);
        
        iListener.clear();
        try {
            assertEquals(0,iListener.deleteSuccessCalls);
            assertEquals(0,iListener.deleteFailureCalls);
            toDel.delete();
            fail();
        } catch (IOException ex) {
            assertEquals(0,iListener.deleteSuccessCalls);
            // fails to lock non-existing fileobject
            assertEquals(0,iListener.deleteFailureCalls);
        }
    }
    
    public static class AnnotationProviderImpl extends BaseAnnotationProvider  {
        private static int cnt;

        public AnnotationProviderImpl() {
            cnt++;
        }

        public static void assertCreated(String msg, boolean reallyCreated) {
            if (reallyCreated) {
                assertEquals(msg, 1, cnt);
            } else {
                assertEquals(msg, 0, cnt);
            }
        }

        private InterceptionListenerImpl impl = new InterceptionListenerImpl(this);
        public String annotateName(String name, java.util.Set files) {
            java.lang.StringBuffer sb = new StringBuffer(name);
            Iterator it = files.iterator();
            while (it.hasNext()) {
                FileObject fo = (FileObject)it.next();
                try {
                    sb.append("," +fo.getNameExt());//NOI18N
                } catch (Exception ex) {
                    fail();
                }
            }
            
            return sb.toString() ;
        }
        
        public java.awt.Image annotateIcon(java.awt.Image icon, int iconType, java.util.Set files) {
            return icon;
        }
        
        public String annotateNameHtml(String name, Set files) {
            return annotateName(name, files);
        }
        
        public Action[] actions(Set files) {
            return new Action[]{};
        }
        
        public InterceptionListener getInterceptionListener() {
            return impl;
        }
    }
    
    public static class InterceptionListenerImpl implements InterceptionListener {
        private int beforeCreateCalls = 0;
        private int createFailureCalls = 0;
        private int createSuccessCalls = 0;
        private int beforeDeleteCalls = 0;
        private int deleteSuccessCalls = 0;
        private int deleteFailureCalls = 0;
        private final AnnotationProviderImpl provider;

        public InterceptionListenerImpl(AnnotationProviderImpl provider) {
            this.provider = provider;
        }
        
        public void clear() {
            beforeCreateCalls = 0;
            createFailureCalls = 0;
            createSuccessCalls = 0;
            beforeDeleteCalls = 0;
            deleteSuccessCalls = 0;
            deleteFailureCalls = 0;
        }
        
        public void beforeCreate(org.openide.filesystems.FileObject parent, java.lang.String name, boolean isFolder) {
            beforeCreateCalls++;
        }
        
        public void createSuccess(org.openide.filesystems.FileObject fo) {
            assertNotNull(fo);
            createSuccessCalls++;
        }
        
        public void createFailure(org.openide.filesystems.FileObject parent, java.lang.String name, boolean isFolder) {
            createFailureCalls++;
        }
        
        public void beforeDelete(org.openide.filesystems.FileObject fo) {
            beforeDeleteCalls++;
        }
        
        public void deleteSuccess(org.openide.filesystems.FileObject fo) {
            deleteSuccessCalls++;
        }
        
        public void deleteFailure(org.openide.filesystems.FileObject fo) {
            deleteFailureCalls++;
        }
    }
}
