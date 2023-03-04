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
package org.netbeans.modules.java.preprocessorbridge.api;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.event.ChangeListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class CompileOnSaveActionQueryTest extends NbTestCase {
    
    private URL nonSrcUrl;
    private URL srcUrl1, srcUrl2;
    private ActionImpl impl1, impl2, impl3;
    
    public CompileOnSaveActionQueryTest(@NonNull final String name) {
        super(name);
    }
    
    @Before
    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        final File wd = getWorkDir();
        final FileObject src1 = FileUtil.createFolder(FileUtil.normalizeFile(
                new File(wd,"src"))); //NOI18N
        final FileObject src2 = FileUtil.createFolder(FileUtil.normalizeFile(
                new File(wd,"src2"))); //NOI18N
        nonSrcUrl = BaseUtilities.toURI(wd).toURL();
        srcUrl1 = src1.toURL();
        srcUrl2 = src2.toURL();
        impl1 = new ActionImpl();
        impl2 = new ActionImpl();
        impl3 = new ActionImpl();
        MockLookup.setInstances(
            new ProviderImpl(srcUrl1, impl1),
            new ProviderImpl(srcUrl2, impl2),
            new ProviderImpl(srcUrl1, impl3));
        //Enable all
        impl1.setEnabled(true);
        impl1.setUpdateClasses(true);
        impl1.setUpdateResources(true);
        impl2.setEnabled(true);
        impl2.setUpdateClasses(true);
        impl2.setUpdateResources(true);
        impl3.setEnabled(true);
        impl3.setUpdateClasses(true);
        impl3.setUpdateResources(true);
    }
    
    @After
    @Override
    public void tearDown() {
    }

    public void testQuery() throws IOException {
        CompileOnSaveAction a = CompileOnSaveActionQuery.getAction(nonSrcUrl);
        assertNull(a);
        assertEquals(0, impl1.getInvocationCountAndReset());
        assertEquals(0, impl2.getInvocationCountAndReset());
        assertEquals(0, impl3.getInvocationCountAndReset());
        a = CompileOnSaveActionQuery.getAction(srcUrl1);
        assertNotNull(a);
        CompileOnSaveAction.Context ctx = CompileOnSaveAction.Context.clean(srcUrl1);
        a.performAction(ctx);
        assertEquals(1, impl1.getInvocationCountAndReset());
        assertEquals(0, impl2.getInvocationCountAndReset());
        assertEquals(0, impl3.getInvocationCountAndReset());
        a = CompileOnSaveActionQuery.getAction(srcUrl2);
        assertNotNull(a);
        ctx = CompileOnSaveAction.Context.clean(srcUrl2);
        a.performAction(ctx);
        assertEquals(0, impl1.getInvocationCountAndReset());
        assertEquals(1, impl2.getInvocationCountAndReset());
        assertEquals(0, impl3.getInvocationCountAndReset());        
    }
    
    
    public void testQueryChanges() throws IOException {        
        CompileOnSaveAction a = CompileOnSaveActionQuery.getAction(srcUrl1);
        assertNotNull(a);
        CompileOnSaveAction.Context ctx = CompileOnSaveAction.Context.clean(srcUrl1);
        a.performAction(ctx);
        assertEquals(1, impl1.getInvocationCountAndReset());
        assertEquals(0, impl2.getInvocationCountAndReset());
        assertEquals(0, impl3.getInvocationCountAndReset());
        impl1.setEnabled(false);
        a.performAction(ctx);
        assertEquals(0, impl1.getInvocationCountAndReset());
        assertEquals(0, impl2.getInvocationCountAndReset());
        assertEquals(1, impl3.getInvocationCountAndReset());        
    }
    
    
    public void testQueryEvents() throws IOException {        
        CompileOnSaveAction a = CompileOnSaveActionQuery.getAction(srcUrl1);
        assertNotNull(a);
        CompileOnSaveAction.Context ctx = CompileOnSaveAction.Context.clean(srcUrl1);
        a.performAction(ctx);
        assertEquals(1, impl1.getInvocationCountAndReset());
        assertEquals(0, impl2.getInvocationCountAndReset());
        assertEquals(0, impl3.getInvocationCountAndReset());
        final MockChangeListener l = new MockChangeListener();
        a.addChangeListener(l);
        impl1.setEnabled(false);
        l.assertEvent();
        a.performAction(ctx);
        assertEquals(0, impl1.getInvocationCountAndReset());
        assertEquals(0, impl2.getInvocationCountAndReset());
        assertEquals(1, impl3.getInvocationCountAndReset());        
    }
    
    
    
    private static final class ActionImpl implements CompileOnSaveAction {
        
        private final ChangeSupport listeners = new ChangeSupport(this);
        private boolean enabled;
        private boolean resEnabled;
        private boolean clzEnabled;
        private int invocationCount;

        @Override
        public Boolean performAction(Context ctx) throws IOException {
            invocationCount++;
            return null;
        }
        
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public boolean isUpdateResources() {
            return resEnabled;
        }

        @Override
        public boolean isUpdateClasses() {
            return clzEnabled;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {            
            listeners.removeChangeListener(l);
        }
        
        void setUpdateResources(final boolean v) {
            this.resEnabled = v;
            listeners.fireChange();
        }
        
        void setUpdateClasses(final boolean v) {
            this.clzEnabled = v;
            listeners.fireChange();
        }
        
        void setEnabled(final boolean v) {
            this.enabled = v;
            listeners.fireChange();
        }
        
        int getInvocationCountAndReset() {
            int res = invocationCount;
            invocationCount = 0;
            return res;
        }
    }
    
    private static final class ProviderImpl implements CompileOnSaveAction.Provider {
        private final URL root;
        private final CompileOnSaveAction action;
        
        ProviderImpl(
                @NonNull final URL root,
                @NonNull final CompileOnSaveAction action) {
            Parameters.notNull("root", root);   //NOI18N
            Parameters.notNull("action", action);   //NOI18N
            this.root = root;
            this.action = action;
        }       
        
        @Override
        @CheckForNull
        public CompileOnSaveAction forRoot(@NonNull final URL root) {
            if (this.root.equals(root)) {
                return this.action;
            }
            return null;
        }        
    }
        
    
}
