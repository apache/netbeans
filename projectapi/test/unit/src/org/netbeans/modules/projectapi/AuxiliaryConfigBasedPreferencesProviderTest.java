/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 2008 Sun
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

package org.netbeans.modules.projectapi;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.test.MockLookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jan Lahoda
 */
public class AuxiliaryConfigBasedPreferencesProviderTest extends NbTestCase {
    
    public AuxiliaryConfigBasedPreferencesProviderTest(String testName) {
        super(testName);
    }            

    private FileObject fo;
    private Project p;
    private TestLookup lookup;
    
    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(TestUtil.testProjectFactory());
        clearWorkDir();
        File wd = getWorkDir();
        FileUtil.refreshAll();
        File f = new File(new File(wd, "test"), "testproject");
        FileObject testprojectFO = FileUtil.createFolder(f);
        assertNotNull(testprojectFO);
        fo = testprojectFO.getParent();
        TestUtil.LOOKUP = lookup = new TestLookup();
        p = ProjectManager.getDefault().findProject(fo);
        assertNotNull(p);
    }

    public void testStorage() throws IOException, BackingStoreException {
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryConfigurationImpl()));
        doTestStorage();
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryProperties()));
        doTestStorage();
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryConfigurationImpl(), new TestAuxiliaryProperties()));
        doTestStorage();
    }
    
    private void doTestStorage() throws IOException, BackingStoreException {
        AuxiliaryConfiguration ac = p.getLookup().lookup(AuxiliaryConfiguration.class);
        AuxiliaryProperties ap = p.getLookup().lookup(AuxiliaryProperties.class);
        
        assertTrue(ac != null || ap != null);
        
        AuxiliaryConfigBasedPreferencesProvider provider = new AuxiliaryConfigBasedPreferencesProvider(p, ac, ap, true);
        Preferences pref = provider.findModule("test");
        
        pref.put("test", "test");
        
        pref.node("subnode1/subnode2").put("somekey", "somevalue");
        
        assertEquals(Arrays.asList("somekey"), Arrays.asList(pref.node("subnode1/subnode2").keys()));
        
        pref.flush();
        
        provider = new AuxiliaryConfigBasedPreferencesProvider(p, ac, ap, true);
        pref = provider.findModule("test");
        
        assertEquals("test", pref.get("test", null));
        assertEquals("somevalue", pref.node("subnode1/subnode2").get("somekey", null));
        assertEquals(Arrays.asList("somekey"), Arrays.asList(pref.node("subnode1/subnode2").keys()));
        pref.node("subnode1/subnode2").remove("somekey");
        assertEquals(Arrays.<String>asList(), Arrays.asList(pref.node("subnode1/subnode2").keys()));
    }

    public void testNoSaveWhenNotModified() throws IOException, BackingStoreException {
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryConfigurationImpl()));
        
        AuxiliaryConfiguration ac = p.getLookup().lookup(AuxiliaryConfiguration.class);
        
        assertNotNull(ac);
        
        final AtomicInteger putCount = new AtomicInteger();
        
        ac = new CountingAuxiliaryConfiguration(ac, putCount);
        
        AuxiliaryConfigBasedPreferencesProvider provider = new AuxiliaryConfigBasedPreferencesProvider(p, ac, null, true);
        Preferences pref = provider.findModule("test");
        
        pref.put("test", "test");
        
        pref.node("subnode1/subnode2").put("somekey", "somevalue");
        
        assertEquals(0, putCount.get());
        pref.flush();
        assertEquals(1, putCount.get());
        pref.flush();
        assertEquals(1, putCount.get());
    }
    
    public void testSubnodes() throws IOException, BackingStoreException {
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryConfigurationImpl()));
        doTestSubnodes();
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryProperties()));
        doTestSubnodes();
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryConfigurationImpl(), new TestAuxiliaryProperties()));
        doTestSubnodes();
    }
    
    private void doTestSubnodes() throws IOException, BackingStoreException {
        AuxiliaryConfiguration ac = p.getLookup().lookup(AuxiliaryConfiguration.class);
        AuxiliaryProperties ap = p.getLookup().lookup(AuxiliaryProperties.class);
        
        assertTrue(ac != null || ap != null);

        AuxiliaryConfigBasedPreferencesProvider provider = new AuxiliaryConfigBasedPreferencesProvider(p, ac, ap, true);
        Preferences pref = provider.findModule("test");

        pref.put("test", "test");

        pref.node("subnode1/subnode2").put("somekey", "somevalue1");
        pref.node("subnode1").put("somekey", "somevalue2");

        pref.flush();
        
        provider = new AuxiliaryConfigBasedPreferencesProvider(p, ac, ap, true);
        pref = provider.findModule("test");
        
        assertTrue(pref.node("subnode1").nodeExists("subnode2"));
        assertEquals("somevalue1", pref.node("subnode1/subnode2").get("somekey", null));
        assertEquals("somevalue2", pref.node("subnode1").get("somekey", null));
        pref.node("subnode1").removeNode();
        assertEquals(null, pref.node("subnode1/subnode2").get("somekey", null));
        assertEquals(null, pref.node("subnode1").get("somekey", null));

        pref.flush();

        provider = new AuxiliaryConfigBasedPreferencesProvider(p, ac, ap, true);
        pref = provider.findModule("test");

        assertEquals(null, pref.node("subnode1/subnode2").get("somekey", null));
        assertEquals(null, pref.node("subnode1").get("somekey", null));
    }
    
    public void testSync() throws IOException, BackingStoreException {
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryConfigurationImpl()));
        
        AuxiliaryConfiguration ac = p.getLookup().lookup(AuxiliaryConfiguration.class);
        
        assertNotNull(ac);
                
        AuxiliaryConfigBasedPreferencesProvider toSync = new AuxiliaryConfigBasedPreferencesProvider(p, ac, null, true);
        Preferences pref = toSync.findModule("test");
        
        pref.put("test", "test");
        
        pref.node("subnode1/subnode2").put("somekey", "somevalue");
        pref.flush();
        
        AuxiliaryConfigBasedPreferencesProvider orig = new AuxiliaryConfigBasedPreferencesProvider(p, ac, null, true);
        
        Preferences origNode = orig.findModule("test").node("subnode1/subnode2");
        
        pref.node("subnode1/subnode2").put("somekey", "somevalue2");
        pref.flush();
        
        origNode.sync();
        
        assertEquals("somevalue2", origNode.get("somekey", null));
    }

    public void testTooLong() throws IOException, BackingStoreException {
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryConfigurationImpl()));

        AuxiliaryConfiguration ac = p.getLookup().lookup(AuxiliaryConfiguration.class);

        assertNotNull(ac);

        AuxiliaryConfigBasedPreferencesProvider toSync = new AuxiliaryConfigBasedPreferencesProvider(p, ac, null, true);
        Preferences pref = toSync.findModule("test");
        //test length of key
        char[] keyChars = new char[100];
        Arrays.fill(keyChars, 'X');
        String key = new String(keyChars);
        pref.put(key, "test");

        //test length of value
        char[] valChars = new char[10 * 1024];
        Arrays.fill(valChars, 'X');
        String value = new String(valChars);
        pref.put("test", value);

        pref.flush();

        assertEquals(pref.get("test", null), value);
        assertEquals(pref.get(key, null), "test");

    }

    @RandomlyFails
    public void testReclaimable() throws IOException, BackingStoreException, InterruptedException {
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryConfigurationImpl()));
        
        Preferences pref = AuxiliaryConfigBasedPreferencesProvider.getPreferences(p, Object.class, true);
        
        //the same preferences instance is returned as long as the previous one exists:
        assertTrue(pref == AuxiliaryConfigBasedPreferencesProvider.getPreferences(p, Object.class, true));
        
        //but the preferences can be reclaimed, as well as the project if noone holds them:
        Reference<Preferences> rPref = new WeakReference<Preferences>(pref);
        Reference<Project> rProject = new WeakReference<Project>(p);
        
        TestUtil.notifyDeleted(p);
        
        Thread.sleep(10000);
        
        p = null;
        pref = null;
        
        assertGC("", rPref);
        assertGC("", rProject);
    }
    
    public void testComplexNames() throws IOException, BackingStoreException, InterruptedException {
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryProperties()));
        
        AuxiliaryProperties ap = p.getLookup().lookup(AuxiliaryProperties.class);
        
        assertNotNull(ap != null);

        AuxiliaryConfigBasedPreferencesProvider provider = new AuxiliaryConfigBasedPreferencesProvider(p, null, ap, true);
        Preferences pref = provider.findModule("test");
        
        pref.node(".:./.:.").put(".:.", "correct");
        
        pref.flush();
        
        provider = new AuxiliaryConfigBasedPreferencesProvider(p, null, ap, true);
        pref = provider.findModule("test");
        
        assertTrue(pref.nodeExists(".:./.:."));
        assertEquals(Arrays.asList(".:."), Arrays.asList(pref.node(".:./.:.").keys()));
    }
    
    public void testNoAuxiliaryImplInLookup() {
        Preferences pref = AuxiliaryConfigBasedPreferencesProvider.getPreferences(p, Object.class, true);
        
        assertNotNull(pref);
        
        pref = AuxiliaryConfigBasedPreferencesProvider.getPreferences(p, Object.class, false);
        
        assertNotNull(pref);
        
        pref = AuxiliaryConfigBasedPreferencesProvider.getPreferences(p, Object.class, true);
        
        assertNotNull(pref);
    }
    
    public void testRemoves() throws IOException, BackingStoreException, InterruptedException {
        lookup.setDelegates(Lookups.fixed(new TestAuxiliaryConfigurationImpl(), new TestAuxiliaryProperties()));
        
        AuxiliaryConfiguration ac = p.getLookup().lookup(AuxiliaryConfiguration.class);
        AuxiliaryProperties ap = p.getLookup().lookup(AuxiliaryProperties.class);

        assertTrue(ac != null && ap != null);
        
        AtomicInteger putCount = new AtomicInteger();
        
        ac = new CountingAuxiliaryConfiguration(ac, putCount);

        AuxiliaryConfigBasedPreferencesProvider provider = new AuxiliaryConfigBasedPreferencesProvider(p, ac, ap, true);
        Preferences pref = provider.findModule("test");
        
        pref.put("test", "test");
        pref.node("somenode");
        
        pref.flush();
        
        assertEquals(0, putCount.get());

        provider = new AuxiliaryConfigBasedPreferencesProvider(p, ac, ap, true);
        pref = provider.findModule("test");
        
        assertEquals("test", pref.get("test", null));
        
        pref.remove("test");
        pref.node("somenode").removeNode();
        
        pref.flush();
        
        assertEquals(0, putCount.get());
        
        provider = new AuxiliaryConfigBasedPreferencesProvider(p, ac, ap, true);
        pref = provider.findModule("test");
        
        assertNull(pref.get("test", null));
        
        pref.flush();
        
        assertEquals(0, putCount.get());
    }

    public void testFindCNBForClass() throws Exception {
        assertEquals("org-w3c-dom", AuxiliaryConfigBasedPreferencesProvider.findCNBForClass(Document.class));
    }
    
    private static final class TestAuxiliaryConfigurationImpl implements AuxiliaryConfiguration {

        private final Document sharedDOM;
        private final Document privDOM;

        public TestAuxiliaryConfigurationImpl() {
            sharedDOM = XMLUtil.createDocument("test", null, null, null);
            privDOM = XMLUtil.createDocument("test", null, null, null);
        }
        
        public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
            Element el = find(shared, namespace, elementName);

            if (el != null) {
                Document dummy = XMLUtil.createDocument("test", null, null, null);
                return (Element) dummy.importNode(el, true);
            }
            
            return null;
        }

        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
            removeConfigurationFragment(fragment.getLocalName(), fragment.getNamespaceURI(), shared);
            
            Document dom = shared ? sharedDOM : privDOM;
            
            dom.getDocumentElement().appendChild(dom.importNode(fragment, true));
        }

        public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            Element el = find(shared, namespace, elementName);

            if (el != null) {
                el.getParentNode().removeChild(el);
                return true;
            }
            
            return false;
        }

        private Element find(boolean shared, String namespace, String elementName) {
            Document dom = shared ? sharedDOM : privDOM;
            NodeList nl = dom.getDocumentElement().getChildNodes();
            
            for (int cntr = 0; cntr < nl.getLength(); cntr++) {
                Node n = nl.item(cntr);
                
                if (n.getNodeType() == Node.ELEMENT_NODE && namespace.equals(n.getNamespaceURI()) && elementName.equals(n.getLocalName())) {
                    return (Element) n;
                }
            }
            return null;
        }
        
    }
    
    private static final class CountingAuxiliaryConfiguration implements AuxiliaryConfiguration {
        private final AuxiliaryConfiguration ac;
        private final AtomicInteger putCount;

        public CountingAuxiliaryConfiguration(AuxiliaryConfiguration ac, AtomicInteger putCount) {
            this.ac = ac;
            this.putCount = putCount;
        }

        public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
            return ac.getConfigurationFragment(elementName, namespace, shared);
        }
        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
            putCount.incrementAndGet();
            ac.putConfigurationFragment(fragment, shared);
        }
        public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            putCount.incrementAndGet();
            return ac.removeConfigurationFragment(elementName, namespace, shared);
        }
    };
        
    private static final class TestAuxiliaryProperties implements AuxiliaryProperties {

        private Properties pub;
        private Properties priv;

        public TestAuxiliaryProperties() {
            this.pub = new Properties();
            this.priv = new Properties();
        }
        
        public String get(String key, boolean shared) {
            return (shared ? pub : priv).getProperty(key);
        }

        public void put(String key, String value, boolean shared) {
            if (value != null) {
                (shared ? pub : priv).setProperty(key, value);
            } else {
                (shared ? pub : priv).remove(key);
            }
        }

        public Iterable<String> listKeys(boolean shared) {
            Enumeration en  = (shared ? pub : priv).propertyNames();
            List<String> result = new LinkedList<String>();
            
            while (en.hasMoreElements()) {
                Object el = en.nextElement();
                
                if (el instanceof String) {
                    result.add((String) el);
                }
            }
            
            return Collections.unmodifiableList(result);
        }

    }
    
    private static final class TestLookup extends ProxyLookup {
        public void setDelegates(Lookup... l) {
            setLookups(l);
        }
    }
}
