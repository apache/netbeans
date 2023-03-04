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

package org.netbeans.modules.editor.mimelookup.impl;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
@RandomlyFails // uses TestUtilities.sleepForWhile()
public class SwitchLookupTest extends NbTestCase {

    /** Creates a new instance of FolderPathLookupTest */
    public SwitchLookupTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(new String[0], getWorkDir(), new Object[] {},
            getClass().getClassLoader(), 
            null
        );
    }
    
    protected @Override void tearDown() {
        TestUtilities.gc();
    }
    
    public void testSimple() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        // Creating lookup for an existing mime path
        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Collection instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        
        // Now create lookup over a non-existing mime path
        lookup = new SwitchLookup(MimePath.parse("text/xml"));
        instances = lookup.lookupAll(Object.class);
        
        assertEquals("Wrong number of instances", 0, instances.size());
    }
    
    public void testAddingMimePath() throws Exception {
        // Create lookup over a non-existing mime path
        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Lookup.Result result = lookup.lookupResult(DummySetting.class);
        L listener = new L();

        result.addLookupListener(listener);
        Collection instances = result.allInstances();
        
        assertEquals("There should be no change events", 0, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 0, instances.size());

        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        // Lookup the instances again
        instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
    }

    public void testRemovingMimePath() throws Exception {
        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        // Create lookup over an existing mime path
        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Lookup.Result result = lookup.lookupResult(DummySetting.class);
        L listener = new L();
        
        result.addLookupListener(listener);
        Collection instances = result.allInstances();

        assertEquals("There should be no change events", 0, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        
        // Delete the mime path folder
        TestUtilities.deleteFile(getWorkDir(), "Editors/text/x-jsp/text");
        TestUtilities.sleepForWhile();

        // Lookup the instances again
        instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 0, instances.size());
    }

    // test hierarchy - instances in lower levels are not visible in higher levels,
    // but instances from higher levels are visible in lower levels
    
    public void testHierarchyInheritance() throws Exception {
        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/");
        TestUtilities.sleepForWhile();

        {
            Lookup javaLookup = new SwitchLookup(MimePath.parse("text/x-java"));
            Collection javaInstances = javaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, javaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, javaInstances.iterator().next().getClass());
        }
        
        {
            Lookup jspJavaLookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
            Collection jspJavaInstances = jspJavaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, jspJavaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, jspJavaInstances.iterator().next().getClass());
    }
    }

    public void testHierarchyRootInheritance() throws Exception {
        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/");
        TestUtilities.createFile(getWorkDir(), "Editors/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        {
            Lookup lookup = new SwitchLookup(MimePath.parse(""));
            Collection instances = lookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, instances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        }
        
        {
            Lookup jspLookup = new SwitchLookup(MimePath.parse("text/x-jsp"));
            Collection jspInstances = jspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, jspInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, jspInstances.iterator().next().getClass());
        }
        
        {
            Lookup javaLookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
            Collection javaInstances = javaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, javaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, javaInstances.iterator().next().getClass());
        }
    }
    
    public void testHierarchyLeaks() throws Exception {
        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/");
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-java/");
        TestUtilities.sleepForWhile();

        {
            Lookup jspLookup = new SwitchLookup(MimePath.parse("text/x-jsp"));
            Collection jspInstances = jspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 0, jspInstances.size());
        }
        
        {
            Lookup javaLookup = new SwitchLookup(MimePath.parse("text/x-java"));
            Collection javaInstances = javaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 0, javaInstances.size());
        }
        
        {
            Lookup jspJavaLookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
            Collection jspJavaInstances = jspJavaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, jspJavaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, jspJavaInstances.iterator().next().getClass());
        }

        {
            Lookup javaJspLookup = new SwitchLookup(MimePath.parse("text/x-java/text/x-jsp"));
            Collection javaJspInstances = javaJspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 0, javaJspInstances.size());
        }
    }
    
    // test that FolderPathLookups are shared and discarded when they are not needed anymore
    
    // test that instances of a class with a Class2LayerFolder provider are really read from the proper folder
    
    public void testReadFromSpecialFolders() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-java/DummyFolder/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Collection instances = lookup.lookupAll(DummySettingWithPath.class);
        
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
    }

    private static final class L implements LookupListener {
        public int resultChangedCnt = 0;
        public void resultChanged(LookupEvent ev) {
            resultChangedCnt++;
        }
    }
}
