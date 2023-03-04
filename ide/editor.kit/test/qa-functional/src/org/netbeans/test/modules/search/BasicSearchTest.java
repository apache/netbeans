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

package org.netbeans.test.modules.search;

import java.util.Hashtable;
import java.util.Iterator;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Some search tests
 * @author Max Sauer
 */
public class BasicSearchTest extends JellyTestCase {
    
    public String DATA_PROJECT_NAME = "UtilitiesTestProject";

    @Override
    protected void setUp() throws Exception {
        openDataProjects("projects/" + DATA_PROJECT_NAME);
    }
    
    /** path to sample files */
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.utilities.basicsearch";
    
    /** Creates a new instance of BasicSearchTest */
    public BasicSearchTest(String testName) {
        super(testName);
    }
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(BasicSearchTest.class).addTest("testSearchForClass", "testRememberSearchesInsideComboBox").enableModules(".*").clusters(".*"));
    }
    
    public void testSearchForClass() {
        NbDialogOperator ndo = Utilities.getFindDialogMainMenu();        
        JComboBoxOperator jcbo = new JComboBoxOperator(ndo,
                new JComboBoxOperator.JComboBoxFinder());
        jcbo.enterText("class"); //enter 'class' in search comboBox and press [Enter]
        
        SearchResultsOperator sro = new SearchResultsOperator();
        assertTrue("Junit Output window should be visible", sro.isVisible());
        System.out.println("## Search output opened");
        Utilities.takeANap(1000);
        sro.close();
        assertFalse("Search window is visible," +
                "should be closed", sro.isShowing());
    }
    
    /**
     * Test if searched items are remembered inside combobox
     */
    public void testRememberSearchesInsideComboBox() {
        //setup ten searches
        for (int i = 0; i < 10; i++) {
            NbDialogOperator ndo = Utilities.getFindDialogMainMenu();
            JComboBoxOperator jcbo = new JComboBoxOperator(ndo,
                    new JComboBoxOperator.JComboBoxFinder());
            jcbo.enterText("a" + i);
            Utilities.takeANap(500);
        }
        //check
        NbDialogOperator ndo = Utilities.getFindDialogMainMenu();
        
        JComboBoxOperator jcbo = new JComboBoxOperator(ndo,
                new JComboBoxOperator.JComboBoxFinder());
        
        for (int i = 0; i < jcbo.getItemCount() - 1; i++) {
            assertEquals("Found " + jcbo.getItemAt(i).toString() +" in search combo" +
                    "expected" + new Integer(9-i).toString() + ".",
                    jcbo.getItemAt(i).toString(), "a" + new Integer(9-i).toString());
        }
        
        assertEquals("Expected 'class', found: " +
                jcbo.getItemAt(jcbo.getItemCount()-1),
                jcbo.getItemAt(jcbo.getItemCount()-1),  "class");
        
    }
    
    
    private String soutHashTable(Hashtable ht) {
        StringBuffer sb = new StringBuffer();
        Iterator itv = ht.values().iterator();
        for (Iterator it = ht.keySet().iterator(); it.hasNext();) {
            sb.append("KEY: " + it.next() + " Value: " + itv.next() + "\n");
            
        }
        return sb.toString();
    }
    
    
}
