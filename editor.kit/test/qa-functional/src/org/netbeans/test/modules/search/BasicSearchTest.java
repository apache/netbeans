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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
