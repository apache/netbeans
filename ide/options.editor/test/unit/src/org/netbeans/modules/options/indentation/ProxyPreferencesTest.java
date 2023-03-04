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

package org.netbeans.modules.options.indentation;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class ProxyPreferencesTest extends NbTestCase {

    public ProxyPreferencesTest(String name) {
        super(name);
    }

    public void testSimpleRead() {
        Preferences orig = Preferences.userRoot().node(getName());
        orig.put("key-1", "value-1");

        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
        assertEquals("Wrong value", "value-1", test.get("key-1", null));
    }
    
    public void testSimpleWrite() {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));

        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
        test.put("key-1", "xyz");
        assertEquals("Wrong value", "xyz", test.get("key-1", null));
    }

    public void testBase64() {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));
        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
        test.putByteArray("key-1", "however you like it".getBytes());
        assertEquals("Wrong value", "however you like it", new String(test.getByteArray("key-1", null)));
    }
    
    public void testSimpleSync() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));

        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
        assertNull("Test should not contains pair", orig.get("key-1", null));

        test.put("key-1", "xyz");
        assertEquals("Test doesn't contain new pair", "xyz", test.get("key-1", null));

        test.sync();
        assertNull("Test didn't rollback pair", test.get("key-1", null));
    }

    public void testSimpleFlush() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));

        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
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
        
        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
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
        
        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
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

        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
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

        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
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

        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
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

        Preferences test = ProxyPreferences.getProxyPreferences(this, orig);
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
        Preferences test = ProxyPreferences.getProxyPreferences(treeToken, orig);
        write(test, newTree);
        checkContains(test, newTree, "Test");

        Reference<Object> treeTokenRef = new WeakReference<Object>(treeToken);
        Reference<Preferences> testRef = new WeakReference<Preferences>(test);
        treeToken = null;
        test = null;
        assertGC("Tree token was not GCed", treeTokenRef, Collections.singleton(this));
        // touch the WeakHashMap to expungeStaleEntries
        Object dummyToken = new Object();
        ProxyPreferences dummyPrefs = ProxyPreferences.getProxyPreferences(dummyToken, orig);
        assertGC("Test preferences were not GCed", testRef, Collections.singleton(this));
        
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------
    
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
