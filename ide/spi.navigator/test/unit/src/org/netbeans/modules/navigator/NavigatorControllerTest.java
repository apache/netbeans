/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.navigator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.navigator.NavigatorTCTest.GlobalLookup4TestImpl;
import org.netbeans.modules.navigator.NavigatorTCTest.TestLookupHint;
import org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Dafe Simonek
 */
public class NavigatorControllerTest extends NbTestCase {
    
    private static final String JAVA_DATA_TYPE = "text/marvelous/data_type";
    
    
    public NavigatorControllerTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests correct retrieving ans instantiating of NavigatorPanel instances 
     * for various scenarios.
     * 
     * @throws java.lang.Exception 
     */
    @RandomlyFails
    public void testObtainProviders() throws Exception {
        System.out.println("Testing NavigatorController.obtainProviders");

        InstanceContent ic = new InstanceContent();
        
        GlobalLookup4TestImpl nodesLkp = new GlobalLookup4TestImpl(ic);
        UnitTestUtils.prepareTest(new String [] { 
            "/org/netbeans/modules/navigator/resources/NavigatorControllerTestProvider.xml" }, 
            Lookups.singleton(nodesLkp)
        );
        
        URL url = NavigatorControllerTest.class.getResource("resources/sample_folder/subfolder1/subfolder2");
        assertNotNull("url not found.", url);

        FileObject fo = URLMapper.findFileObject(url);
        FileObject[] fos = fo.getChildren();
        fo = fo.getFileObject("Nic.my_extension");
        assertNotNull("fo not found.", fo);
        
        FileObject foSubFolder1 = fo.getParent().getParent();
        FileObject foSampleFolder = foSubFolder1.getParent();
        DataObject dObj = DataObject.find(fo);
        DataFolder subFolder1 = (DataFolder)DataObject.find(foSubFolder1);
        DataFolder sampleFolder = (DataFolder)DataObject.find(foSampleFolder);
        DataShadow shadow1 = DataShadow.create(subFolder1, dObj);
        DataShadow shadow2 = DataShadow.create(sampleFolder, shadow1);
        
        System.out.println("Testing DataShadow resolvement...");
        // not really valid, uses impl fact that during obtainProviders,
        // NavigatorTC parameter will not be needed.
        NavigatorController nc = NavigatorTC.getInstance().getController();
        ArrayList<Node> shadow1Node = new ArrayList<Node>(1);
        shadow1Node.add(shadow1.getNodeDelegate());
        List result = nc.obtainProviders(shadow1Node);
        assertNotNull("provider not found", result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof TestJavaNavigatorPanel);
        
        ArrayList<Node> shadow2Node = new ArrayList<Node>(1);
        shadow2Node.add(shadow2.getNodeDelegate());
        result = nc.obtainProviders(shadow2Node);
        assertNotNull("provider not found", result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof TestJavaNavigatorPanel);
        
        TestLookupHint lookupHint = new TestLookupHint("contentType/tester");
        ic.add(lookupHint);
        TestLookupPanelsPolicy lookupContentType = new TestLookupPanelsPolicy();
        ic.add(lookupContentType);

        System.out.println("Testing LookupContentType functionality...");
        result = nc.obtainProviders(shadow1Node);
        assertNotNull("provider not found", result);
        assertEquals("Expected 1 item, got " + result.size(), 1, result.size());
        assertTrue("Expected provider class TestContentTypeNavigatorPanel, but got " 
                + result.get(0).getClass().getName(), result.get(0) instanceof TestContentTypeNavigatorPanel);
        
    }
    
    /** Dummy navigator panel provider, just for testing
     */ 
    public static class TestJavaNavigatorPanel implements NavigatorPanel {
        
        public String getDisplayName () {
            return JAVA_DATA_TYPE;
        }
    
        public String getDisplayHint () {
            return null;
        }

        public JComponent getComponent () {
            return null;
        }

        public void panelActivated (Lookup context) {
        }

        public void panelDeactivated () {
        }
        
        public Lookup getLookup () {
            return null;
        }
        
    }
    
    /**
     * test impl fo NavigatorPanel for testing LookupContentType functionality
     */
    public static final class TestContentTypeNavigatorPanel extends TestJavaNavigatorPanel {

        public String getDisplayName () {
            return "Test content type";
        }

    }
    
    /** Test implementation of content type */
    public static final class TestLookupPanelsPolicy implements NavigatorLookupPanelsPolicy {

        public int getPanelsPolicy() {
            return NavigatorLookupPanelsPolicy.LOOKUP_HINTS_ONLY;
        }
        
    }
    
}
