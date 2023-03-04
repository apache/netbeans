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
package org.netbeans.modules.versioning;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.testvcs.TestAnnotatedVCS;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

public class DelegatingVCSTest extends NbTestCase {
    
    protected File dataRootDir;
    protected File versionedFolder;
    protected File unversionedFolder;

    public DelegatingVCSTest(String testName) {
        super(testName);
    }

    protected File getVersionedFolder() {
        if (versionedFolder == null) {
            versionedFolder = new File(dataRootDir, "workdir/root-test-versioned/");
            versionedFolder.mkdirs();
        }
        return versionedFolder;
    }
    
    protected File getUnversionedFolder() {
        if (unversionedFolder == null) {
            unversionedFolder = new File(dataRootDir, "workdir/unversioned/");
            unversionedFolder.mkdirs();
        }
        return unversionedFolder;
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = getWorkDir();
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        MockLookup.setLayersAndInstances();
        
        resetDelegate();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
     
    public void testDelegatingVCS() {
        DelegatingVCS delegate = getDelegatingVCS();
        final org.netbeans.modules.versioning.core.spi.VCSContext ctx = org.netbeans.modules.versioning.core.spi.VCSContext.forNodes(new Node[0]);
        
        assertNull(TestAnnotatedVCS.INSTANCE);
        
        assertEquals("TestVCSDisplay", delegate.getDisplayName());
        assertNull(TestAnnotatedVCS.INSTANCE);
        
        assertEquals("TestVCSMenu", delegate.getMenuLabel());
        assertNull(TestAnnotatedVCS.INSTANCE);
        
        Action[] actions = delegate.getGlobalActions(ctx);
        assertNotNull(actions);
        assertEquals(1, actions.length); 
        assertNull(TestAnnotatedVCS.INSTANCE);
        
        actions = delegate.getInitActions(ctx);
        assertNotNull(actions);
        assertEquals(1, actions.length); 
        assertNull(TestAnnotatedVCS.INSTANCE);
        
        delegate.getDelegate(); // awake
        assertNotNull(TestAnnotatedVCS.INSTANCE);
        
    }
    
    public void testListeners() {
        DelegatingVCS delegate = getDelegatingVCS();
        assertNull(TestAnnotatedVCS.INSTANCE);
        
        final List<PropertyChangeEvent> events1 = new LinkedList<PropertyChangeEvent>();
        final PropertyChangeListener l1 = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                events1.add(evt);
            }
        };
        final List<PropertyChangeEvent> events2 = new LinkedList<PropertyChangeEvent>();
        final PropertyChangeListener l2 = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                events2.add(evt);
            }
        };
        
        delegate.addPropertyCL(l1);
        delegate.addPropertyCL(l2);
        
        delegate.getDelegate(); // forces delegate creation
        assertNotNull(TestAnnotatedVCS.INSTANCE);        
        
        events1.clear();
        events2.clear();
        TestAnnotatedVCS.INSTANCE.fire();
        assertEquals(1, events1.size());
        assertEquals(1, events2.size());        
        
        assertEventSource(events1);
        assertEventSource(events2);
    }

    private void assertEventSource(final List<PropertyChangeEvent> events1) {
        for (PropertyChangeEvent e : events1) {
            assertEquals(TestAnnotatedVCS.INSTANCE, e.getSource());
        }
    }
    
    public void testDelegatesAllMethods() {
        Set<String> ignoredMethods = new HashSet<String>();
        ignoredMethods.add("addPropertyChangeListener");
        ignoredMethods.add("removePropertyChangeListener");
        ignoredMethods.add("moveChangeListeners");
        ignoredMethods.add("fireAnnotationsChanged");
        ignoredMethods.add("fireStatusChanged");
        ignoredMethods.add("fireVersionedFilesChanged");
        ignoredMethods.add("fireStatusChanged");
        ignoredMethods.add("getProperty");
        ignoredMethods.add("putProperty");
        
        Set<String> overridenMethods = new HashSet<String>();
        for (Method method : DelegatingVCS.class.getDeclaredMethods()) {
            overridenMethods.add(method.getName());
        }
        
        for (Method method : VersioningSystem.class.getDeclaredMethods()) {
            if(method.getModifiers() == Method.PUBLIC &&
               !overridenMethods.contains(method.getName()) && 
               !ignoredMethods.contains(method.getName())) 
            {
                fail(" method '" + method.getName() + "' should be overriden in " + DelegatingVCS.class.getName());
            }
        }
    }
    
    public void testMetadataGetEnv() {
        DelegatingVCS delegate = getDelegatingVCS();
        assertNotNull(delegate);
        
        assertTrue(delegate.isMetadataFile(VCSFileProxy.createFileProxy(new File(TestAnnotatedVCS.TEST_VCS_METADATA))));
        assertTrue(delegate.isMetadataFile(VCSFileProxy.createFileProxy(new File("null"))));
        assertTrue(delegate.isMetadataFile(VCSFileProxy.createFileProxy(new File("set"))));
        assertFalse(delegate.isMetadataFile(VCSFileProxy.createFileProxy(new File("notset"))));
    }
    
    private DelegatingVCS getDelegatingVCS() {
        Collection<? extends org.netbeans.modules.versioning.core.spi.VersioningSystem> systems = Lookup.getDefault().lookup(new Lookup.Template<org.netbeans.modules.versioning.core.spi.VersioningSystem>(org.netbeans.modules.versioning.core.spi.VersioningSystem.class)).allInstances();
        for(org.netbeans.modules.versioning.core.spi.VersioningSystem s : systems) {
            if(s instanceof DelegatingVCS && "TestVCSDisplay".equals((String)((DelegatingVCS) s).getDisplayName())) {
                return (DelegatingVCS) s;
            }
        }
        return null;
    }
    
    private void resetDelegate() {
        getDelegatingVCS().reset();
        TestAnnotatedVCS.INSTANCE = null;
    }
}
