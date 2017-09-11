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
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Testing basic functionality of MimeLookup
 *
 * @author Martin Roskanin
 */
public class MimeLookupTest extends NbTestCase {

    private static final int WAIT_TIME = 5000;
    private static final int WAIT_TIME_FIRING = 1500;
    final int resultChangedCount[] = new int[1];
    
    public MimeLookupTest(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        String fsstruct [] = new String [] {
            "Editors/text/xml/text/html/java-lang-StringBuffer.instance", //NOI18N
            "Editors/text/x-java/text/xml/testLookupTwo/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance", //NOI18N
            "Editors/text/jsp/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance", //NOI18N
            "Editors/text/html/text/xml/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance", //NOI18N
            "Editors/text/html/text/xml/testLookupTwo/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance", //NOI18N 
            "Editors/text/jsp/text/html/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance", //NOI18N
            "Editors/text/xml/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectInstantiation.instance", //NOI18N
            // testing "compound mime types like application/x-ant+xml"
            "Editors/application/dtd/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance", //NOI18N                     
            "Editors/application/x-ant+dtd/java-lang-StringBuffer.instance", //NOI18N
            "Editors/text/x-java/application/x-ant+dtd/testLookupTwo/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance", //NOI18N 
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
     * Looking up the class that has registered subfolder via Class2LayerFolder
     */
    @RandomlyFails // NB-Core-Build #6979
    public void testRegisteredClassLookup() throws IOException{
        MimePath mp = MimePath.parse("text/x-java/text/xml");
        Lookup lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup ,TestLookupObjectTwo.class, true);

        lookup = MimeLookup.getLookup("text/jsp");//NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        mp = MimePath.parse("text/html/text/xml");
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        
        //test inheritance from underlaying mime types
        mp = MimePath.parse("text/xml/text/jsp/text/html"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, true);
        lookup = MimeLookup.getLookup("text/html");
        checkLookupObject(lookup, TestLookupObject.class, false);        
        mp = MimePath.parse("text/jsp/text/html");
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, true);
        
        //simulating module uninstallation and removal of mime lookup file from xml layer
        TestUtilities.deleteFile(getWorkDir(), "Editors/text/x-java/text/xml/testLookupTwo/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        mp = MimePath.parse("text/x-java/text/xml"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        lookup = MimeLookup.getLookup("text/jsp");//NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        mp = MimePath.parse("text/html/text/xml"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        
        //simulate module installation, new file will be added
        createFile("Editors/text/x-java/text/x-properties/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");

        mp = MimePath.get(MimePath.get("text/x-java"), "text/x-properties"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);

        createFile("Editors/text/x-java/text/x-properties/testLookup/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);

        //uninstall ObjectTwo
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/x-java/text/x-properties/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);

        //uninstall ObjectOne
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/x-java/text/x-properties/testLookup/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        //simulate module installation, new file will be added
        createFile("Editors/text/x-java/text/x-properties/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");

        mp = MimePath.parse("text/x-java/text/x-properties"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);

        //delete all
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/jsp/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");
        
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/html/text/xml/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");

        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/html/text/xml/testLookupTwo/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        
        mp = MimePath.parse("text/x-java/text/xml"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup ,TestLookupObjectTwo.class, false);
        
        lookup = MimeLookup.getLookup("text/jsp");//NOI18N
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        mp = MimePath.get(MimePath.get("text/html"), "text/xml"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);

        mp = MimePath.parse("text/x-java/text/x-properties"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);

        //----------------------------------------------------------------------
        //simulate module installation, new file will be added
        mp = MimePath.parse("text/html/text/xml"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        createFile("Editors/text/html/text/xml/testLookup/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");

        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        createFile("Editors/text/html/text/xml/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        //----------------------------------------------------------------------

        
        //----------------------------------------------------------------------
        // Register listener on a Result of lookup on unexisted object
        resultChangedCount[0] = 0;
        mp = MimePath.parse("text/dtd/text/xml"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        Result result = lookup.lookup(new Template(TestLookupObject.class));
        result.allInstances(); // remove this line if issue #60010 is fixed
        LookupListener listener = new LookupListener(){
            public void resultChanged(LookupEvent ev){
                resultChangedCount[0]++;
            }
        };
        result.addLookupListener(listener);
        
        //simulate module installation, new file will be added
        createFile("Editors/text/dtd/text/xml/testLookup/" + //NOI18N        
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance"); //NOI18N        

        checkResultChange(1);
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
        //----------------------------------------------------------------------
        
        

        
        //----------------------------------------------------------------------
        // result *NOT* firing on standard mime type testing
        resultChangedCount[0] = 0;
        lookup = MimeLookup.getLookup("image/jpeg"); //NOI18N
        // try to lookup TestLookupObjectTwo, while TestLookupObject will be installed.
        // firing should not happen
        result = lookup.lookup(new Template(TestLookupObjectTwo.class));
        result.allInstances();
        result.addLookupListener(listener);
        checkResultChange(0);

        //simulate module installation, new file will be added
        createFile("Editors/image/jpeg/testLookup/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");
        
        checkResultChange(0);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        checkLookupObject(lookup, TestLookupObject.class, true);
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
        //----------------------------------------------------------------------

        
        //----------------------------------------------------------------------
        //
        // testing "compound" mime types like application/x-ant+dtd
        // "Editors/application/dtd/text/x-java/java-lang-String.instance", //NOI18N
        // "Editors/application/dtd/testLookup/org-netbeans-modules-editor-mimelookup-TestLookupObject.instance", //NOI18N                     
        // "Editors/application/x-ant+dtd/java-lang-StringBuffer.instance", //NOI18N
        // "Editors/application/x-ant+dtd/text/x-java/testLookupTwo/org-netbeans-modules-editor-mimelookup-TestLookupObjectTwo.instance", //NOI18N 
        lookup = MimeLookup.getLookup("application/x-ant+dtd"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, StringBuffer.class, true);
        checkLookupObject(lookup, String.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        mp = MimePath.parse("text/x-java/application/x-ant+dtd"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, TestLookupObject.class, true); //it is inherited from parent
        checkLookupObject(lookup, StringBuffer.class, true); //it is inherited from parent
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        //----------------------------------------------------------------------

        
        //----------------------------------------------------------------------
        // Test lookup of "compound" mime types if the object is not installed yet
        // "Editors/image/x-ant+dtd/text/x-java/testLookupTwo/org-netbeans-modules-editor-mimelookup-TestLookupObjectTwo.instance", //NOI18N 
        mp = MimePath.parse("image/x-ant+dtd/text/x-java"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup ,TestLookupObjectTwo.class, false);
        createFile("Editors/image/x-ant+dtd/text/x-java/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        //----------------------------------------------------------------------
        

        
        //----------------------------------------------------------------------
        // result firing on "compound" mime type testing
        resultChangedCount[0] = 0;
        mp = MimePath.parse("audio/x-ant+dtd/text/x-java"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        result = lookup.lookup(new Template(TestLookupObjectTwo.class));
        result.allInstances();
        result.addLookupListener(listener);
        checkResultChange(0);

        //simulate module installation, new file will be added
        createFile("Editors/audio/x-ant+dtd/text/x-java/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        
        checkResultChange(1);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        checkLookupObject(lookup, TestLookupObject.class, false);
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
        //----------------------------------------------------------------------

         

        //----------------------------------------------------------------------
        // result *NOT* firing and firing on "compound" mime type testing where object is 
        // installed in inherited folder
        resultChangedCount[0] = 0;
        lookup = MimeLookup.getLookup("video/mp3+dtd"); //NOI18N
        result = lookup.lookup(new Template(TestLookupObject.class));
        result.allInstances();
        result.addLookupListener(listener);
        checkResultChange(0);

        //simulate module installation, new file will be added
        createFile("Editors/video/dtd/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        
        checkResultChange(0);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        checkLookupObject(lookup, TestLookupObject.class, false);
        
        // now install TestLookupObject, firing should happen
        //simulate module installation, new file will be added
        createFile("Editors/video/dtd/testLookup/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");
        
        checkResultChange(1);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        checkLookupObject(lookup, TestLookupObject.class, true);
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
        //----------------------------------------------------------------------
        
        //----------------------------------------------------------------------
        // result *NOT* firing and firing on "compound" *SUB* mime type testing where object is 
        // installed in inherited folder
        resultChangedCount[0] = 0;
        mp = MimePath.parse("message/mp3+dtd/audio/wav"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        result = lookup.lookup(new Template(TestLookupObject.class));
        result.allInstances();
        result.addLookupListener(listener);
        checkResultChange(0);

        //simulate module installation, new file will be added
        createFile("Editors/message/dtd/audio/wav/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        
        checkResultChange(0);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        checkLookupObject(lookup, TestLookupObject.class, false);
        
        // now install TestLookupObject, firing should happen
        //simulate module installation, new file will be added
        createFile("Editors/message/dtd/audio/wav/testLookup/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");

        checkResultChange(1);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        checkLookupObject(lookup, TestLookupObject.class, true);
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
        //----------------------------------------------------------------------
  
        //----------------------------------------------------------------------
        // result *NOT* firing and firing on "compound" *SUB* mime type testing where object is 
        // installed in inherited mime type folder
        resultChangedCount[0] = 0;
        mp = MimePath.get(MimePath.get("message/mp3+dtd"), "audio/wav"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        result = lookup.lookup(new Template(IllegalStateException.class));
        result.allInstances();
        result.addLookupListener(listener);
        checkResultChange(0);

        //simulate module installation, new file will be added
        createFile("Editors/message/dtd/audio/wav/" +
                "java-lang-InstantiationException.instance");

        checkResultChange(0);
        checkLookupObject(lookup, IllegalStateException.class, false);
        checkLookupObject(lookup, InstantiationException.class, true);
        
        // now install TestLookupObject, firing should happen
        //simulate module installation, new file will be added
        createFile("Editors/message/dtd/audio/wav/" +
                "java-lang-IllegalStateException.instance");

        checkResultChange(1);
        checkLookupObject(lookup, IllegalStateException.class, true);
        checkLookupObject(lookup, InstantiationException.class, true);
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
        //----------------------------------------------------------------------

        //----------------------------------------------------------------------
        // result *NOT* firing and firing on mime type testing where object is 
        // installed in mime type folder
        resultChangedCount[0] = 0;
        lookup = MimeLookup.getLookup("model/mp3"); //NOI18N
        result = lookup.lookup(new Template(IllegalStateException.class));
        result.allInstances();
        result.addLookupListener(listener);
        checkResultChange(0);

        //simulate module installation, new file will be added
        createFile("Editors/model/mp3/" +
                "java-lang-InstantiationException.instance");
        
        checkResultChange(0);
        checkLookupObject(lookup, IllegalStateException.class, false);
        checkLookupObject(lookup, InstantiationException.class, true);
        
        // now install TestLookupObject, firing should happen
        //simulate module installation, new file will be added
        createFile("Editors/model/mp3/" +
                "java-lang-IllegalStateException.instance");

        checkResultChange(1);
        checkLookupObject(lookup, IllegalStateException.class, true);
        checkLookupObject(lookup, InstantiationException.class, true);
        result.removeLookupListener(listener);
        resultChangedCount[0] = 0;
        //----------------------------------------------------------------------
        
        
    }

    /** 
     * FolderLookup behaves recursively by default. It is not ideal as for MimeLookup,
     * that should operate only on the mime-type-folder
     * see issue #58991
     * Testing if the MimeLookup is not recursive
     */
    public void testLookupFolderRecursivity(){
        //StringBuffer.instance is declared 
        // in "Editors/text/xml/text/html/java-lang-StringBuffer.instance"
        // it shouldn't be found in text/xml parent folder
        Lookup lookup = MimeLookup.getLookup("text/xml");
        checkLookupObject(lookup, StringBuffer.class, false);        
    }

    
    /** 
     * Looking up the class that has NOT registered subfolder via Class2LayerFolder.
     * It should be found appropriate mime-type specific folder
     */
    public void testNotRegisteredClassLookup() throws IOException {
        MimePath mp = MimePath.get(MimePath.get("text/xml"), "text/html"); //NOI18N
        Lookup lookup = MimeLookup.getLookup(mp);
        checkLookupObject(lookup, StringBuffer.class, true);
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/xml/text/html/java-lang-StringBuffer.instance");
        checkLookupObject(lookup, StringBuffer.class, false);
    }


    /** 
     * Checking wheather the initialization of MimeLookup for appropriate
     * mime type will not instantiate lookup object
     */
    public void testLazyLookupObjectInstantiation() throws IOException{
        Lookup lookup = MimeLookup.getLookup("text/xml"); //NOI18N
        // lookup for some object in the mime lookup
        checkLookupObject(lookup, StringBuffer.class, false);
        // calling 
        // checkLookupObject(lookup, TestLookupObjectInstantiation.class, false);
        // should fail
    }
    
    /** 
     * Lookuping the template that has registered subfolder via Class2LayerFolder
     */
    public void testRegisteredTemplatesLookup() throws IOException{
        createFile("Editors/text/x-java/text/x-properties/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        
        MimePath mp = MimePath.parse("text/x-java/text/x-properties"); //NOI18N
        Lookup lookup = MimeLookup.getLookup(mp);
        checkLookupTemplate(lookup, TestLookupObject.class, 0);
        checkLookupTemplate(lookup, TestLookupObjectTwo.class, 1);
        
        mp = MimePath.parse("text/xml/text/jsp"); //NOI18N
        lookup = MimeLookup.getLookup(mp);
        checkLookupTemplate(lookup, TestLookupObject.class, 1);
        checkLookupTemplate(lookup, TestLookupObjectTwo.class, 0);

        // testing issue #58941
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/jsp/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");
        checkLookupTemplate(lookup, TestLookupObject.class, 0);
        checkLookupTemplate(lookup, TestLookupObjectTwo.class, 0);
    }

    
    private void checkLookupObject(final Lookup lookup, final Class clazz, final boolean shouldBePresent){
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
    
    private void checkLookupTemplate(final Lookup lookup, final Class clazz, final int instCount){
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
