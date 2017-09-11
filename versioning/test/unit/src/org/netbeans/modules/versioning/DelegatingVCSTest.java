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
