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

package org.netbeans.modules.editor.mimelookup.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Testing corrected functionality of found issues.
 *
 * @author Martin Roskanin
 */
public class IssuesTest extends NbTestCase {
    
    private static final int WAIT_TIME = 5000;
    private static final int WAIT_TIME_FIRING = 1500;
    final int resultChangedCount[] = new int[1];
    
    public IssuesTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        String fsstruct [] = new String [] {
            "Editors/text/jsp/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance", //NOI18N
            "Editors/text/x-java/testLookupTwo/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance", //NOI18N 
            "Editors/testLookupTwo/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance", //NOI18N                     
        };

        EditorTestLookup.setLookup(fsstruct, getWorkDir(), new Object[] {},
                   getClass().getClassLoader());
        
    }

    private void createFile(String file) throws IOException{
        TestUtilities.createFile(getWorkDir(), file); //NOI18N        
    }
    
    private void checkResultChange(final int count) throws IOException{
        // wait for firing event
        TestUtilities.waitMaxMilisForValue(WAIT_TIME_FIRING, new TestUtilities.ValueResolver(){
            public Object getValue(){
                return Boolean.FALSE;
            }
        }, Boolean.TRUE);
        assertTrue(("resultChangedCount is:"+resultChangedCount[0]+" instead of "+count), resultChangedCount[0] == count);
    }
    
    /** 
     * Issues of changes in layer
     */
    public void testForChangeInLayer() throws IOException{
        
        // issue #63338
        // http://www.netbeans.org/issues/show_bug.cgi?id=63338
        // Subj: deadlock during showing annotations
        // fix: deadlock occured in after inproper firing of lookup changed event.
        //      event was fired even in cases, the lookup listener should be quiet.
        MimeLookup lookup = MimeLookup.getMimeLookup("text/jsp"); //NOI18N
        Result result = lookup.lookup(new Template(TestLookupObject.class));
        result.allInstances().size(); // remove this line if issue #60010 is fixed
        LookupListener listener = new LookupListener(){
            public void resultChanged(LookupEvent ev){
                resultChangedCount[0]++;
            }
        };
        result.addLookupListener(listener);
        
        //simulate module installation, new file will be added
        createFile("Editors/text/jsp/testLookup/org-openide-actions-PasteAction.instance"); //NOI18N        

        checkResultChange(0);
        
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/jsp/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");

        checkResultChange(1);
        
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
        // end of issue #63338 ------------------------------------------------
        
        
        
    }
    
    /** Issue #72873 
     *  MimeLookup duplicates objects from default mimetype folder
     */
    public void testDoubleItems(){
        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java"); //NOI18N
        Result result = lookup.lookup(new Template(TestLookupObjectTwo.class));
        Collection col = result.allInstances();
        assertTrue(col.size() == 1);
        
        lookup = MimeLookup.getMimeLookup(""); //NOI18N
        result = lookup.lookup(new Template(TestLookupObjectTwo.class));
        col = result.allInstances();
        assertTrue(col.size() == 1);
        
    }

    
    private void checkLookupObject(final MimeLookup lookup, final Class clazz, final boolean shouldBePresent){
        TestUtilities.waitMaxMilisForValue(WAIT_TIME, new TestUtilities.ValueResolver(){
            public Object getValue(){
                Object obj = lookup.lookup(clazz);
                boolean bool = (shouldBePresent) ? obj != null : obj == null;
                return Boolean.valueOf(bool);
            }
        }, Boolean.TRUE);
        Object obj = lookup.lookup(clazz);
        if (shouldBePresent){
            assertTrue("Object should be present in the lookup",obj!=null);
        } else {
            assertTrue("Object should NOT be present in the lookup",obj==null);
        }
    }
    
    private void checkLookupTemplate(final MimeLookup lookup, final Class clazz, final int instCount){
        TestUtilities.waitMaxMilisForValue(WAIT_TIME, new TestUtilities.ValueResolver(){
            public Object getValue(){
                Lookup.Result result = lookup.lookup(new Lookup.Template(clazz));
                boolean bool = result.allInstances().size() == instCount;
                return Boolean.valueOf(bool);
            }
        }, Boolean.TRUE);
        Lookup.Result result = lookup.lookup(new Lookup.Template(clazz));
        int size = result.allInstances().size();
        boolean bool =  (size == instCount);
        assertTrue("Number of instances doesn't match. Found:"+size+". Should be presented:"+instCount+".", bool);
    }

}
