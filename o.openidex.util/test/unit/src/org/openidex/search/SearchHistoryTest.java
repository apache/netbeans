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

package org.openidex.search;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;


/**
 *
 * @author  Martin Roskanin
 */
public final class SearchHistoryTest extends NbTestCase {


    /**
     */
    public SearchHistoryTest(String name) {
        super(name);
    }

    /**
     */
    public static void main(String args[]) {
        TestRunner.run(new NbTestSuite(SearchHistoryTest.class));
    }


    /**
     */
    public void testSearchPatternSize() throws Exception {
        assertSize("SearchPattern size", 80, SearchPattern.create("searchText",true,true,false));
    }

    public void testSearchHistoryListSize() throws Exception{
        for (int i = 0; i<60; i++){
            SearchHistory.getDefault().add(SearchPattern.create(String.valueOf(i),true,true,false));
        }
        assertTrue(SearchHistory.getDefault().getSearchPatterns().size() == 10);
    }
    
    public void testLastSelectedListener() throws Exception{
        final boolean fired[] = new boolean[1];
        fired[0] = false;
        PropertyChangeListener pcl = new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent evt){
                    if (evt!=null && SearchHistory.LAST_SELECTED.equals(evt.getPropertyName())){
                        fired[0] = true;
                    }
                }
        };
        SearchHistory.getDefault().addPropertyChangeListener(pcl);
        SearchHistory.getDefault().setLastSelected(SearchPattern.create("newSearchtext",true,true,false));
        SearchHistory.getDefault().removePropertyChangeListener(pcl);
        assertTrue(fired[0]);
    }    
    
    public void testAddToSearchHistoryListener() throws Exception{
        final boolean fired[] = new boolean[2];
        PropertyChangeListener pcl = new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent evt){
                    if (evt!=null && SearchHistory.ADD_TO_HISTORY.equals(evt.getPropertyName())){
                        fired[0] = true;
                    }else{
                        fired[1] = true;
                    }
                }
        };
        SearchHistory.getDefault().addPropertyChangeListener(pcl);
        SearchHistory.getDefault().add(SearchPattern.create("searchtext",true,true,false));
        SearchHistory.getDefault().removePropertyChangeListener(pcl);
        assertTrue(fired[0]);
        assertFalse("Only the expected change is fired", fired[1]);
    }
    
    public void testAddIncorrectItemToSearchHistoryListener() throws Exception{
        final boolean fired[] = new boolean[2];
        
        PropertyChangeListener pcl = new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent evt){
                    if (evt!=null && SearchHistory.ADD_TO_HISTORY.equals(evt.getPropertyName())){
                        fired[0] = true;
                    } else {
                        fired[1] = true;
                    }
                }
        };
        SearchHistory.getDefault().addPropertyChangeListener(pcl);

        // add valid pattern
        fired[0] = false;
        fired[1] = false;
        SearchHistory.getDefault().add(SearchPattern.create("searchtext2",true,true,false));
        assertTrue(fired[0]);
        assertFalse("Only the expected change is fired", fired[1]);
        
        // add the same pattern, it shouldn't be added
        fired[0] = false;
        fired[1] = false;        
        SearchHistory.getDefault().add(SearchPattern.create("searchtext2",true,true,false));
        assertTrue(!fired[0]);
        assertFalse("Only the expected change is fired", fired[1]);
        
        // add null pattern
        fired[0] = false;
        fired[1] = false;        
        SearchHistory.getDefault().add(null);
        assertTrue(!fired[0]);
        assertFalse("Only the expected change is fired", fired[1]);
        
        // add pattern with null searchExpression
        fired[0] = false;
        fired[1] = false;        
        SearchHistory.getDefault().add(SearchPattern.create(null,true,true,false));
        assertTrue(!fired[0]);
        assertFalse("Only the expected change is fired", fired[1]);
        
        // add pattern with empty searchExpression
        fired[0] = false;
        fired[1] = false;        
        SearchHistory.getDefault().add(SearchPattern.create("",true,true,false));
        assertTrue(!fired[0]);
        assertFalse("Only the expected change is fired", fired[1]);
        
        SearchHistory.getDefault().removePropertyChangeListener(pcl);
    }
    
    
    public void testEquals() throws Exception{
        SearchPattern pattern_one = SearchPattern.create("one",false, false, false);
        SearchPattern pattern_one_a = SearchPattern.create("one",true, false, false);        
        SearchPattern pattern_one_b = SearchPattern.create("one",false, true, false);
        SearchPattern pattern_one_c = SearchPattern.create("one",false,false, true);
        SearchPattern pattern_one_d = SearchPattern.create("one",true,true, false);
        SearchPattern pattern_one_e = SearchPattern.create("one",true,false, true);
        SearchPattern pattern_one_f = SearchPattern.create("one",true,true, true);
        
        SearchPattern pattern_two = SearchPattern.create("two",false, false, false);
        SearchPattern pattern_TwO = SearchPattern.create("TwO",false, false, false);
        SearchPattern pattern_TwO_a = SearchPattern.create("TwO",false, true, false);
        
        SearchPattern pattern_one_test = SearchPattern.create("one",false, false, false);
        SearchPattern pattern_one_a_test = SearchPattern.create("one",true, false, false);        
        SearchPattern pattern_one_b_test = SearchPattern.create("one",false, true, false);
        SearchPattern pattern_one_c_test = SearchPattern.create("one",false,false, true);
        SearchPattern pattern_one_d_test = SearchPattern.create("one",true,true, false);
        SearchPattern pattern_one_e_test = SearchPattern.create("one",true,false, true);
        SearchPattern pattern_one_f_test = SearchPattern.create("one",true,true, true);
        
        SearchPattern pattern_two_test = SearchPattern.create("two",false, false, false);
        SearchPattern pattern_TwO_test = SearchPattern.create("TwO",false, false, false);
        SearchPattern pattern_TwO_a_test = SearchPattern.create("TwO",false, true, false);
        
        assertTrue(pattern_one.equals(pattern_one_test));
        assertTrue(pattern_one_test.equals(pattern_one));
        assertTrue(pattern_one_a_test.equals(pattern_one_a));
        assertTrue(pattern_one_b_test.equals(pattern_one_b));
        assertTrue(pattern_one_c_test.equals(pattern_one_c));
        assertTrue(pattern_one_d_test.equals(pattern_one_d));
        assertTrue(pattern_one_e_test.equals(pattern_one_e));
        assertTrue(pattern_one_f_test.equals(pattern_one_f));
        //--------------------------------------------------
        assertTrue(!pattern_one_a_test.equals(pattern_one_b));
        assertTrue(!pattern_one_a_test.equals(pattern_one_c));
        assertTrue(!pattern_one_b_test.equals(pattern_one_c));
        assertTrue(!pattern_one_d_test.equals(pattern_one_e));
        
        assertTrue(pattern_two.equals(pattern_two_test));
        assertTrue(pattern_TwO.equals(pattern_TwO_test));
        assertTrue(pattern_TwO_a.equals(pattern_TwO_a_test));
        assertTrue(!pattern_two.equals(pattern_TwO_test));
        assertTrue(!pattern_TwO_a.equals(pattern_TwO_test));
    }
    
    public void testHashCode() throws Exception{
        int pattern_one = SearchPattern.create("one",false, false, false).hashCode();
        int pattern_one_a = SearchPattern.create("one",true, false, false).hashCode();        
        int pattern_one_b = SearchPattern.create("one",false, true, false).hashCode();
        int pattern_one_c = SearchPattern.create("one",false,false, true).hashCode();
        int pattern_one_d = SearchPattern.create("one",true,true, false).hashCode();
        int pattern_one_e = SearchPattern.create("one",true,false, true).hashCode();
        int pattern_one_f = SearchPattern.create("one",true,true, true).hashCode();
        
        int pattern_two = SearchPattern.create("two",false, false, false).hashCode();
        int pattern_TwO = SearchPattern.create("TwO",false, false, false).hashCode();
        int pattern_TwO_a = SearchPattern.create("TwO",false, true, false).hashCode();
        
        int pattern_one_test = SearchPattern.create("one",false, false, false).hashCode();
        int pattern_one_a_test = SearchPattern.create("one",true, false, false).hashCode();        
        int pattern_one_b_test = SearchPattern.create("one",false, true, false).hashCode();
        int pattern_one_c_test = SearchPattern.create("one",false,false, true).hashCode();
        int pattern_one_d_test = SearchPattern.create("one",true,true, false).hashCode();
        int pattern_one_e_test = SearchPattern.create("one",true,false, true).hashCode();
        int pattern_one_f_test = SearchPattern.create("one",true,true, true).hashCode();
        
        int pattern_two_test = SearchPattern.create("two",false, false, false).hashCode();
        int pattern_TwO_test = SearchPattern.create("TwO",false, false, false).hashCode();
        int pattern_TwO_a_test = SearchPattern.create("TwO",false, true, false).hashCode();
        
        assertTrue(pattern_one == pattern_one_test);
        assertTrue(pattern_one_test == pattern_one);
        assertTrue(pattern_one_a_test == pattern_one_a);
        assertTrue(pattern_one_b_test == pattern_one_b);
        assertTrue(pattern_one_c_test == pattern_one_c);
        assertTrue(pattern_one_d_test == pattern_one_d);
        assertTrue(pattern_one_e_test == pattern_one_e);
        assertTrue(pattern_one_f_test == pattern_one_f);
        //--------------------------------------------------
        assertTrue(pattern_one_a_test != pattern_one_b);
        assertTrue(pattern_one_a_test != pattern_one_c);
        assertTrue(pattern_one_b_test != pattern_one_c);
        assertTrue(pattern_one_d_test != pattern_one_e);
        
        assertTrue(pattern_two == pattern_two_test);
        assertTrue(pattern_TwO == pattern_TwO_test);
        assertTrue(pattern_TwO_a == pattern_TwO_a_test);
        assertTrue(pattern_two != pattern_TwO_test);
        assertTrue(pattern_TwO_a != pattern_TwO_test);
    }
    
}
