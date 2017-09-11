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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.settings.storage.preferences;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import static junit.framework.Assert.assertEquals;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.api.OverridePreferences;
import org.netbeans.modules.editor.settings.storage.api.MemoryPreferences;

/**
 *
 * @author vita
 */
public class ProxyPreferencesImplTest extends NbTestCase {

    public ProxyPreferencesImplTest(String name) {
        super(name);
    }

    public void testSimpleRead() {
        Preferences orig = Preferences.userRoot().node(getName());
        orig.put("key-1", "value-1");

        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        assertEquals("Wrong value", "value-1", test.get("key-1", null));
    }
    
    public void testSimpleWrite() {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));

        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        test.put("key-1", "xyz");
        assertEquals("Wrong value", "xyz", test.get("key-1", null));
    }

    public void testBase64() {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));
        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        test.putByteArray("key-1", "however you like it".getBytes());
        assertEquals("Wrong value", "however you like it", new String(test.getByteArray("key-1", null)));
    }
    
    public void testSimpleSync() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));

        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        assertNull("Test should not contains pair", orig.get("key-1", null));

        test.put("key-1", "xyz");
        assertEquals("Test doesn't contain new pair", "xyz", test.get("key-1", null));

        test.sync();
        assertNull("Test didn't rollback pair", test.get("key-1", null));
    }

    public void testSimpleFlush() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));

        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        assertNull("Test should not contains pair", orig.get("key-1", null));

        test.put("key-1", "xyz");
        assertEquals("Test doesn't contain new pair", "xyz", test.get("key-1", null));

        test.flush();
        assertEquals("Test should still contain the pair", "xyz", test.get("key-1", null));
        assertEquals("Test didn't flush the pair", "xyz", orig.get("key-1", null));
    }
    
    public void testSyncTree1() throws BackingStoreException {
        String [] origTree = new String [] {
            "CodeStyle/profile=GLOBAL",
        };
        String [] newTree = new String [] {
            "CodeStyle/text/x-java/tab-size=2",
            "CodeStyle/text/x-java/override-global-settings=true",
            "CodeStyle/text/x-java/expand-tabs=true",
            "CodeStyle/profile=PROJECT",
        };

        Preferences orig = Preferences.userRoot().node(getName());
        write(orig, origTree);
        checkContains(orig, origTree, "Orig");
        checkNotContains(orig, newTree, "Orig");
        
        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        checkEquals("Test should be the same as Orig", orig, test);
        
        write(test, newTree);
        checkContains(test, newTree, "Test");

        test.sync();
        checkContains(orig, origTree, "Orig");
        checkNotContains(orig, newTree, "Orig");
        checkContains(test, origTree, "Test");
        checkNotContains(test, newTree, "Test");
    }

    public void testFlushTree1() throws BackingStoreException {
        String [] origTree = new String [] {
            "CodeStyle/profile=GLOBAL",
        };
        String [] newTree = new String [] {
            "CodeStyle/text/x-java/tab-size=2",
            "CodeStyle/text/x-java/override-global-settings=true",
            "CodeStyle/text/x-java/expand-tabs=true",
            "CodeStyle/profile=PROJECT",
        };

        Preferences orig = Preferences.userRoot().node(getName());
        write(orig, origTree);
        checkContains(orig, origTree, "Orig");
        checkNotContains(orig, newTree, "Orig");
        
        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        checkEquals("Test should be the same as Orig", orig, test);
        
        write(test, newTree);
        checkContains(test, newTree, "Test");

        test.flush();
        checkEquals("Test didn't flush to Orig", test, orig);
    }

    public void testRemoveKey() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());
        orig.put("key-2", "value-2");
        assertNull("Original contains value", orig.get("key-1", null));
        assertEquals("Original doesn't contain value", "value-2", orig.get("key-2", null));

        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        test.put("key-1", "xyz");
        assertEquals("Wrong value", "xyz", test.get("key-1", null));
        
        test.remove("key-1");
        assertNull("Test contains removed key-1", test.get("key-1", null));
        
        test.remove("key-2");
        assertNull("Test contains removed key-2", test.get("key-2", null));

        test.flush();
        assertNull("Test flushed removed key-1", orig.get("key-1", null));
        assertNull("Test.flush did not remove removed key-2", orig.get("key-2", null));
    }
    
    public void testRemoveNode() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());
        Preferences origChild = orig.node("child");

        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        assertTrue("Test child shoculd exist", test.nodeExists("child"));
        Preferences testChild = test.node("child");

        testChild.removeNode();
        assertFalse("Removed test child should not exist", testChild.nodeExists(""));
        assertFalse("Removed test child should not exist in parent", test.nodeExists("child"));

        test.flush();
        assertFalse("Test.flush did not remove orig child", origChild.nodeExists(""));
        assertFalse("Test.flush did not remove orig child from parent", orig.nodeExists("child"));
    }

    public void testRemoveNodeCreateItAgain() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());

        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        Preferences testChild = test.node("child");

        testChild.removeNode();
        assertFalse("Removed test child should not exist", testChild.nodeExists(""));
        assertFalse("Removed test child should not exist in parent", test.nodeExists("child"));

        Preferences testChild2 = test.node("child");
        assertTrue("Recreated test child should exist", testChild2.nodeExists(""));
        assertTrue("Recreated test child should exist in parent", test.nodeExists("child"));
        assertNotSame("Recreated child must not be the same as the removed one", testChild2, testChild);
        assertEquals("Wrong childrenNames list", Arrays.asList(new String [] { "child" }), Arrays.asList(test.childrenNames()));

        try {
            testChild.get("key", null);
            fail("Removed test node should not be accessible");
        } catch (Exception e) {
        }

        try {
            testChild2.get("key", null);
        } catch (Exception e) {
            fail("Recreated test node should be accessible");
        }

    }

    public void testRemoveHierarchy() throws BackingStoreException {
        String [] origTree = new String [] {
            "R.CodeStyle.project.expand-tabs=true",
            "R.CodeStyle.project.indent-shift-width=6",
            "R.CodeStyle.project.spaces-per-tab=6",
            "R.CodeStyle.project.tab-size=7",
            "R.CodeStyle.project.text-limit-width=88",
            "R.CodeStyle.usedProfile=project",
            "R.text.x-ruby.CodeStyle.project.indent-shift-width=2",
            "R.text.x-ruby.CodeStyle.project.spaces-per-tab=2",
            "R.text.x-ruby.CodeStyle.project.tab-size=2",
        };
        String [] newTree = new String [] {
            "R.CodeStyle.project.expand-tabs=true",
            "R.CodeStyle.project.indent-shift-width=3",
            "R.CodeStyle.project.spaces-per-tab=3",
            "R.CodeStyle.project.tab-size=5",
            "R.CodeStyle.project.text-limit-width=77",
            "R.CodeStyle.usedProfile=project",
            "R.text.x-ruby.CodeStyle.project.indent-shift-width=2",
            "R.text.x-ruby.CodeStyle.project.spaces-per-tab=2",
            "R.text.x-ruby.CodeStyle.project.tab-size=2",
        };

        Preferences orig = Preferences.userRoot().node(getName());
        write(orig, origTree);

        checkContains(orig, origTree, "Orig");

        Preferences test = ProxyPreferencesImpl.getProxyPreferences(this, orig);
        checkEquals("Test should be the same as Orig", orig, test);

        Preferences testRoot = test.node("R");
        removeAllKidsAndKeys(testRoot);
        
        write(test, newTree);
        checkContains(test, newTree, "Test");

        test.flush();
        checkEquals("Test didn't flush to Orig", test, orig);
    }

    public void testTreeGCed() throws BackingStoreException {
        String [] newTree = new String [] {
            "R.CodeStyle.project.expand-tabs=true",
            "R.CodeStyle.project.indent-shift-width=3",
            "R.CodeStyle.project.spaces-per-tab=3",
            "R.CodeStyle.project.tab-size=5",
            "R.CodeStyle.project.text-limit-width=77",
            "R.CodeStyle.usedProfile=project",
            "R.text.x-ruby.CodeStyle.project.indent-shift-width=2",
            "R.text.x-ruby.CodeStyle.project.spaces-per-tab=2",
            "R.text.x-ruby.CodeStyle.project.tab-size=2",
        };

        Preferences orig = Preferences.userRoot().node(getName());

        Object treeToken = new Object();
        Preferences test = ProxyPreferencesImpl.getProxyPreferences(treeToken, orig);
        write(test, newTree);
        checkContains(test, newTree, "Test");

        Reference<Object> treeTokenRef = new WeakReference<Object>(treeToken);
        Reference<Preferences> testRef = new WeakReference<Preferences>(test);
        treeToken = null;
        test = null;
        assertGC("Tree token was not GCed", treeTokenRef, Collections.singleton(this));
        // touch the WeakHashMap to expungeStaleEntries
        Object dummyToken = new Object();
        ProxyPreferencesImpl dummyPrefs = ProxyPreferencesImpl.getProxyPreferences(dummyToken, orig);
        assertGC("Test preferences were not GCed", testRef, Collections.singleton(this));
        
    }
    
    /**
     * Checks that a value not defined in delegate can be read from the parent prefs.
     * Checks that if the parent prefs also do not define the value, the
     * default from parameter is used.
     * 
     * @throws Exception 
     */
    public void testInheritedRead() throws Exception {
        Preferences stored = new MapPreferences();
        Preferences inherited = new MapPreferences();
        
        stored.put("key-1", "value-1");
        stored.put("key-3", "override");
        inherited.put("key-2", "value-2");
        inherited.put("key-3", "base");

        MemoryPreferences mem = MemoryPreferences.getWithInherited(this, inherited, stored);
        Preferences test = mem.getPreferences();

        assertEquals("Wrong value 1", "value-1", test.get("key-1", null));
        assertEquals("Wrong value 2", "value-2", test.get("key-2", "a"));
        assertEquals("Wrong value 3", "override", test.get("key-3", "a"));
        assertEquals("Wrong value 4", "value-4", test.get("key-4", "value-4"));
    }
    
    /**
     * Asserts that if a value is remove()d during editing, the inherited value
     * will be seen through. Also checks that the Preferences key is actually
     * deleted on flush() and the inherited preferences is not altered.
     */
    public void testSeeInheritedThroughRemoves() throws Exception {
        Preferences stored = new MapPreferences();
        Preferences inherited = new MapPreferences();

        stored.put("key", "value");
        inherited.put("key", "parentValue");
        
        MemoryPreferences mem = MemoryPreferences.getWithInherited(this, inherited, stored);
        Preferences test = mem.getPreferences();

        assertEquals("Does not see local value", "value", test.get("key", null));
        test.remove("key");
        
        assertEquals("Stored value changed prematurely", "value", stored.get("key", null));
        assertEquals("Inherited not seen", "parentValue", test.get("key", null));
        
        test.flush();
        assertNull("Stored value not erased", stored.get("key", null));
        assertEquals("Inherited changed", "parentValue", test.get("key", null));
    }
    
    /**
     * Checks that puts to the direct store is refired
     */
    public void testSeeStoredEvents() throws Exception {
        Preferences stored = new MapPreferences();
        Preferences inherited = new MapPreferences();
        
        inherited.putInt("intValue", 100);
        stored.putInt("intValue", 2);
        stored.putBoolean("toBeRemoved", Boolean.TRUE);

        MemoryPreferences mem = MemoryPreferences.getWithInherited(this, inherited, stored);
        Preferences test = mem.getPreferences();
        
        PL pl = new PL();
        test.addPreferenceChangeListener(pl);
        
        // add
        pl.arm();
        stored.put("newValue", "baa");
        pl.waitEvent();
        assertEquals(1, pl.changeCount);
        assertEquals("newValue", pl.key);
        assertEquals("baa", pl.value);
        
        // change
        pl.arm();
        stored.putInt("intValue", 3);
        pl.waitEvent();
        assertEquals(2, pl.changeCount);
        assertEquals("intValue", pl.key);
        assertEquals("3", pl.value);
        
        // remove not inherited
        pl.arm();
        stored.remove("newValue");
        pl.waitEvent();
        assertEquals(3, pl.changeCount);
        assertEquals("newValue", pl.key);
        assertEquals(null, pl.value);
        
        // remove inherited
        pl.arm();
        stored.remove("intValue");
        pl.waitEvent();
        assertEquals(4, pl.changeCount);
        assertEquals("intValue", pl.key);
        assertEquals("100", pl.value);
    }
    
    /**
     * Checks that events for values that are not overriden
     * are propagated even from the inherited store
     */
    public void testSeeInheritedEvents() throws Exception {
        Preferences stored = new MapPreferences();
        Preferences inherited = new MapPreferences();
        
        inherited.putInt("intValue", 100);

        MemoryPreferences mem = MemoryPreferences.getWithInherited(this, inherited, stored);
        Preferences test = mem.getPreferences();
        
        PL pl = new PL();
        test.addPreferenceChangeListener(pl);
        
        // add
        pl.arm();
        inherited.put("newValue", "baa");
        pl.waitEvent();
        assertEquals(1, pl.changeCount);
        assertEquals("newValue", pl.key);
        assertEquals("baa", pl.value);
        
        // change
        pl.arm();
        inherited.putInt("intValue", 3);
        pl.waitEvent();
        assertEquals(2, pl.changeCount);
        assertEquals("intValue", pl.key);
        assertEquals("3", pl.value);
        
        // remove not inherited
        pl.arm();
        inherited.remove("newValue");
        pl.waitEvent();
        assertEquals(3, pl.changeCount);
        assertEquals("newValue", pl.key);
        assertEquals(null, pl.value);
    }
    
    /** 
     * Checks that modifications to the ihnerited store are not fired if
     * the local store overrides
     */
    public void testInheritedEventsMasked() throws Exception {
        Preferences stored = new MapPreferences();
        Preferences inherited = new MapPreferences();
        
        inherited.putInt("intValue", 100);
        stored.putInt("intValue", 10);

        MemoryPreferences mem = MemoryPreferences.getWithInherited(this, inherited, stored);
        Preferences test = mem.getPreferences();
        
        PL pl = new PL();
        test.addPreferenceChangeListener(pl);
        
        // change
        pl.arm();
        inherited.putInt("intValue", 3);
        pl.waitEvent();
        assertEquals(0, pl.changeCount);
        
        // remove not inherited
        pl.arm();
        inherited.remove("intValue");
        pl.waitEvent();
        assertEquals(0, pl.changeCount);
    }
    
    /**
     * If a key is locally removed from ProxyPrefs, then it is removed from the underlying storage as well,
     * ProxyPrefs attempt to recover the value through inheritance. If the value is not inherited at all,
     * no event should be produced, as the removal was already done (in-memory).
     */
    public void testRemoveLocallyRemovedKeyNothingInherited() throws Exception {
        Preferences stored = new MapPreferences();
        Preferences inherited = new MapPreferences();
        
        stored.putBoolean("toBeRemoved", Boolean.TRUE);

        MemoryPreferences mem = MemoryPreferences.getWithInherited(this, inherited, stored);
        Preferences test = mem.getPreferences();
        
        PL pl = new PL();
        test.addPreferenceChangeListener(pl);

        pl.arm();
        test.remove("toBeRemoved");
        pl.waitEvent();
        
        // check the first removal was fired properly
        assertNull(test.get("toBeRemoved", null));
        assertEquals(1, pl.changeCount);
        assertNull(pl.value);
        assertEquals("toBeRemoved", pl.key);
        
        pl.arm();
        stored.remove("toBeRemoved");
        pl.waitEvent();
        // no additional event was seen
        assertEquals(1, pl.changeCount);
    }
    
    /**
     * Exception was thrown, if Inherited preferences observed removal in underlying
     * keys and attempted to refire the change.
     */
    public void testRemoveInInheritedPreferences() throws Exception {
        Preferences stored = new MapPreferences();
        Preferences inherited = new MapPreferences();
        
        inherited.putBoolean("toBeRemoved", Boolean.TRUE);

        MemoryPreferences mem = MemoryPreferences.getWithInherited(this, inherited, stored);
        Preferences test = mem.getPreferences();
        
        PL pl = new PL();
        test.addPreferenceChangeListener(pl);
        
        assertTrue(test.getBoolean("toBeRemoved", false));
        
        pl.arm();
        // remove from inherited prefs, test should stop listing the key
        // and should refire the removed event
        inherited.remove("toBeRemoved");
        pl.waitEvent();
        
        assertNull(test.get("toBeRemoved", null));
        assertEquals(1, pl.changeCount);
        assertNull(pl.value);
        assertEquals("toBeRemoved", pl.key);
    }

    class PL implements PreferenceChangeListener {
         private int changeCount;
         private Object value;
         private String key;
         private CountDownLatch latch;
         
         @Override
         public void preferenceChange(PreferenceChangeEvent evt) {
             changeCount++;
             this.key = evt.getKey();
             this.value = evt.getNewValue();
             latch.countDown();
         }
         
         void arm() {
             latch = new CountDownLatch(1);
         }
         
         void waitEvent() throws Exception {
             latch.await(5, TimeUnit.SECONDS);
         }
     }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------
    
    private static class MapPreferences extends AbstractPreferences implements OverridePreferences {
        
        private Map<String,Object> map = new HashMap<String, Object>();

        public MapPreferences() {
            super(null, ""); // NOI18N
        }

        @Override
        public boolean isOverriden(String key) {
            return map.containsKey(key);
        }
        
        protected void putSpi(String key, String value) {
            map.put(key, value);            
        }

        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }

        protected void removeSpi(String key) {
            map.remove(key);
        }

        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private void write(Preferences prefs, String[] tree) {
        for(String s : tree) {
            int equalIdx = s.lastIndexOf('=');
            assertTrue(equalIdx != -1);
            String value = s.substring(equalIdx + 1);

            String key;
            String nodePath;
            int slashIdx = s.lastIndexOf('/', equalIdx);
            if (slashIdx != -1) {
                key = s.substring(slashIdx + 1, equalIdx);
                nodePath = s.substring(0, slashIdx);
            } else {
                key = s.substring(0, equalIdx);
                nodePath = "";
            }

            Preferences node = prefs.node(nodePath);
            node.put(key, value);
        }
    }

    private void checkContains(Preferences prefs, String[] tree, String prefsId) throws BackingStoreException {
        for(String s : tree) {
            int equalIdx = s.lastIndexOf('=');
            assertTrue(equalIdx != -1);
            String value = s.substring(equalIdx + 1);

            String key;
            String nodePath;
            int slashIdx = s.lastIndexOf('/', equalIdx);
            if (slashIdx != -1) {
                key = s.substring(slashIdx + 1, equalIdx);
                nodePath = s.substring(0, slashIdx);
            } else {
                key = s.substring(0, equalIdx);
                nodePath = "";
            }

            assertTrue(prefsId + " doesn't contain node '" + nodePath + "'", prefs.nodeExists(nodePath));
            Preferences node = prefs.node(nodePath);

            String realValue = node.get(key, null);
            assertNotNull(prefsId + ", '" + nodePath + "' node doesn't contain key '" + key + "'", realValue);
            assertEquals(prefsId + ", '" + nodePath + "' node, '" + key + "' contains wrong value", value, realValue);
        }
    }

    private void checkNotContains(Preferences prefs, String[] tree, String prefsId) throws BackingStoreException {
        for(String s : tree) {
            int equalIdx = s.lastIndexOf('=');
            assertTrue(equalIdx != -1);
            String value = s.substring(equalIdx + 1);

            String key;
            String nodePath;
            int slashIdx = s.lastIndexOf('/', equalIdx);
            if (slashIdx != -1) {
                key = s.substring(slashIdx + 1, equalIdx);
                nodePath = s.substring(0, slashIdx);
            } else {
                key = s.substring(0, equalIdx);
                nodePath = "";
            }

            if (prefs.nodeExists(nodePath)) {
                Preferences node = prefs.node(nodePath);
                String realValue = node.get(key, null);
                if (realValue != null && realValue.equals(value)) {
                    fail(prefsId + ", '" + nodePath + "' node contains key '" + key + "' = '" + realValue + "'");
                }
            }
        }
    }

    private void dump(Preferences prefs, String prefsId) throws BackingStoreException {
        for(String key : prefs.keys()) {
            System.out.println(prefsId + ", " + prefs.absolutePath() + "/" + key + "=" + prefs.get(key, null));
        }
        for(String child : prefs.childrenNames()) {
            dump(prefs.node(child), prefsId);
        }
    }

    private void checkEquals(String msg, Preferences expected, Preferences test) throws BackingStoreException {
        assertEquals("Won't compare two Preferences with different absolutePath", expected.absolutePath(), test.absolutePath());
        
        // check the keys and their values
        for(String key : expected.keys()) {
            String expectedValue = expected.get(key, null);
            assertNotNull(msg + "; Expected:" + expected.absolutePath() + " has no '" + key + "'", expectedValue);
            
            String value = test.get(key, null);
            assertNotNull(msg + "; Test:" + test.absolutePath() + " has no '" + key + "'", value);
            assertEquals(msg + "; Test:" + test.absolutePath() + "/" + key + " has wrong value", expectedValue, value);
        }

        // check the children
        for(String child : expected.childrenNames()) {
            assertTrue(msg + "; Expected:" + expected.absolutePath() + " has no '" + child + "' subnode", expected.nodeExists(child));
            Preferences expectedChild = expected.node(child);

            assertTrue(msg + "; Test:" + test.absolutePath() + " has no '" + child + "' subnode", test.nodeExists(child));
            Preferences testChild = test.node(child);

            checkEquals(msg, expectedChild, testChild);
        }
    }

    private void removeAllKidsAndKeys(Preferences prefs) throws BackingStoreException {
        for(String kid : prefs.childrenNames()) {
            prefs.node(kid).removeNode();
        }
        for(String key : prefs.keys()) {
            prefs.remove(key);
        }
    }

}
