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

import java.io.IOException;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Testing basic functionality of MimeLookup
 * Testing a deprecated MimePath.childLookup behaviour
 *
 * @author Martin Roskanin
 */
@RandomlyFails
public class Depr_MimeLookupTest extends NbTestCase {
    
    private static final int WAIT_TIME = 5000;
    private static final int WAIT_TIME_FIRING = 1500;
    final int resultChangedCount[] = new int[1];
    
    public Depr_MimeLookupTest(String testName) {
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
    public void testRegisteredClassLookup() throws IOException{
        
        MimeLookup lookup;
        
        lookup = MimeLookup.getMimeLookup("text/jsp");//NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);

        lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/xml"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup ,TestLookupObjectTwo.class, true);

        lookup = MimeLookup.getMimeLookup("text/html").childLookup("text/xml"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        
        //test inheritance from underlaying mime types
        lookup = MimeLookup.getMimeLookup("text/xml").childLookup("text/jsp").childLookup("text/html"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true);
        lookup = MimeLookup.getMimeLookup("text/html");
        checkLookupObject(lookup, TestLookupObject.class, false);        
        lookup = MimeLookup.getMimeLookup("text/jsp").childLookup("text/html");
        checkLookupObject(lookup, TestLookupObject.class, true);
        
        //simulating module uninstallation and removal of mime lookup file from xml layer
        TestUtilities.deleteFile(getWorkDir(), "Editors/text/x-java/text/xml/testLookupTwo/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/xml"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        lookup = MimeLookup.getMimeLookup("text/jsp");//NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        lookup = MimeLookup.getMimeLookup("text/html").childLookup("text/xml"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        
        //simulate module installation, new file will be added
        createFile("Editors/text/x-java/text/x-properties/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");

        lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/x-properties"); //NOI18N
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

        lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/x-properties"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);

        //delete all
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/jsp/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");
        
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/html/text/xml/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");

        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/html/text/xml/testLookupTwo/org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        
        lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/xml"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup ,TestLookupObjectTwo.class, false);
        
        lookup = MimeLookup.getMimeLookup("text/jsp");//NOI18N
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        lookup = MimeLookup.getMimeLookup("text/html").childLookup("text/xml"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);

        lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/x-properties"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);

        //----------------------------------------------------------------------
        //simulate module installation, new file will be added
        lookup = MimeLookup.getMimeLookup("text/html").childLookup("text/xml"); //NOI18N        
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
        lookup = MimeLookup.getMimeLookup("text/dtd").childLookup("text/xml"); //NOI18N
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
        lookup = MimeLookup.getMimeLookup("image/jpeg"); //NOI18N
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
        lookup = MimeLookup.getMimeLookup("application/x-ant+dtd"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true);
        checkLookupObject(lookup, StringBuffer.class, true);
        checkLookupObject(lookup, String.class, false);
        checkLookupObject(lookup, TestLookupObjectTwo.class, false);
        
        lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("application/x-ant+dtd"); //NOI18N
        checkLookupObject(lookup, TestLookupObject.class, true); //it is inherited from parent
        checkLookupObject(lookup, StringBuffer.class, true); //it is inherited from parent
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        //----------------------------------------------------------------------

        
        //----------------------------------------------------------------------
        // Test lookup of "compound" mime types if the object is not installed yet
        // "Editors/image/x-ant+dtd/text/x-java/testLookupTwo/org-netbeans-modules-editor-mimelookup-TestLookupObjectTwo.instance", //NOI18N 
        lookup = MimeLookup.getMimeLookup("image/x-ant+dtd").childLookup("text/x-java"); //NOI18N
        checkLookupObject(lookup ,TestLookupObjectTwo.class, false);
        createFile("Editors/image/x-ant+dtd/text/x-java/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");
        checkLookupObject(lookup, TestLookupObjectTwo.class, true);
        //----------------------------------------------------------------------
        

        
        //----------------------------------------------------------------------
        // result firing on "compound" mime type testing
        resultChangedCount[0] = 0;
        lookup = MimeLookup.getMimeLookup("audio/x-ant+dtd").childLookup("text/x-java"); //NOI18N
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
        lookup = MimeLookup.getMimeLookup("video/mp3+dtd"); //NOI18N
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
        lookup = MimeLookup.getMimeLookup("message/mp3+dtd").childLookup("audio/wav"); //NOI18N
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
        lookup = MimeLookup.getMimeLookup("message/mp3+dtd").childLookup("audio/wav"); //NOI18N
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
        lookup = MimeLookup.getMimeLookup("model/mp3"); //NOI18N
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
        MimeLookup lookup = MimeLookup.getMimeLookup("text/xml");
        checkLookupObject(lookup, StringBuffer.class, false);        
    }

    
    /** 
     * Looking up the class that has NOT registered subfolder via Class2LayerFolder.
     * It should be found appropriate mime-type specific folder
     */
    public void testNotRegisteredClassLookup() throws IOException {
        MimeLookup lookup = MimeLookup.getMimeLookup("text/xml").childLookup("text/html"); //NOI18N
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
        MimeLookup lookup = MimeLookup.getMimeLookup("text/xml"); //NOI18N
        // lookup for some object in the mime lookup
        checkLookupObject(lookup, StringBuffer.class, false);
        // calling 
        // checkLookupObject(lookup, TestLookupObjectInstantiation.class, false);
        // should fail
    }
    
    /** 
     * Looking up the template that has registered subfolder via Class2LayerFolder
     */
    public void testRegisteredTemplatesLookup() throws IOException{
        createFile("Editors/text/x-java/text/x-properties/testLookupTwo/" +
                "org-netbeans-modules-editor-mimelookup-impl-TestLookupObjectTwo.instance");

        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/x-properties"); //NOI18N
        checkLookupTemplate(lookup, TestLookupObject.class, 0);
        checkLookupTemplate(lookup, TestLookupObjectTwo.class, 1);
        
        lookup = MimeLookup.getMimeLookup("text/xml").childLookup("text/jsp");//NOI18N
        checkLookupTemplate(lookup, TestLookupObject.class, 1);
        checkLookupTemplate(lookup, TestLookupObjectTwo.class, 0);

        // testing issue #58941
        TestUtilities.deleteFile(getWorkDir(),
                "Editors/text/jsp/testLookup/org-netbeans-modules-editor-mimelookup-impl-TestLookupObject.instance");
        checkLookupTemplate(lookup, TestLookupObject.class, 0);
        checkLookupTemplate(lookup, TestLookupObjectTwo.class, 0);
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
