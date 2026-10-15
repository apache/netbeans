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

package org.netbeans.core.windows.persistence;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.netbeans.junit.NbTestCase;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/** Functionality tests for saving and loading TCRef configuration data
 *
 * @author Marek Slama
 */
public class TCRefParserTest extends NbTestCase {
    
    public TCRefParserTest() {
        super("");
    }
    
    public TCRefParserTest(java.lang.String testName) {
        super(testName);
    }
    
    //Testing CORRECT data
    /** Test of loaded data
     */
    public void testLoadTCRef00 () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testLoadTCRef00 START");
        
        TCRefParser tcRefParser = createRefParser("data/valid/Windows/Modes/mode00","tcref00");
        
        TCRefConfig tcRefCfg = tcRefParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", tcRefCfg);
        
        assertTrue("TopComponent is opened.", tcRefCfg.opened);
        
        System.out.println("TCRefParserTest.testLoadTCRef00 FINISH");
    }
    
    /** Test of loaded data
     */
    public void testLoadTCRef01 () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testLoadTCRef01 START");
        
        TCRefParser tcRefParser = createRefParser("data/valid/Windows/Modes/mode00","tcref01");
        
        TCRefConfig tcRefCfg = tcRefParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", tcRefCfg);
        
        assertFalse("TopComponent is closed.", tcRefCfg.opened);
        
        System.out.println("TCRefParserTest.testLoadTCRef01 FINISH");
    }
    
    /** Test of loaded data
     */
    public void testLoadTCRef03 () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testLoadTCRef03 START");
        
        TCRefParser tcRefParser = createRefParser("data/valid/Windows/Modes/mode00","tcref03");
        
        TCRefConfig tcRefCfg = tcRefParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", tcRefCfg);
        
        assertFalse("TopComponent is closed.", tcRefCfg.opened);
        
        assertEquals("Previous mode.", "explorer", tcRefCfg.previousMode);
        assertEquals("Tab index in previous mode.", 2, tcRefCfg.previousIndex);
        
        assertTrue("TopComponent is docked in maximized mode.", tcRefCfg.dockedInMaximizedMode);
        assertFalse("TopComponent is slided-out in default mode.", tcRefCfg.dockedInDefaultMode);
        
        assertTrue("TopComponent is maximized when slided-in.", tcRefCfg.slidedInMaximized);
        
        System.out.println("TCRefParserTest.testLoadTCRef03 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveTCRef00 () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testSaveTCRef00 START");
        
        TCRefParser tcRefParser = createRefParser("data/valid/Windows/Modes/mode00","tcref00");
        
        TCRefConfig tcRefCfg1 = tcRefParser.load();
        
        tcRefParser.save(tcRefCfg1);
        
        TCRefConfig tcRefCfg2 = tcRefParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",tcRefCfg1.equals(tcRefCfg2));
                
        System.out.println("TCRefParserTest.testSaveTCRef00 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveTCRef01 () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testSaveTCRef01 START");
        
        TCRefParser tcRefParser = createRefParser("data/valid/Windows/Modes/mode00","tcref01");
        
        TCRefConfig tcRefCfg1 = tcRefParser.load();
        
        tcRefParser.save(tcRefCfg1);
        
        TCRefConfig tcRefCfg2 = tcRefParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",tcRefCfg1.equals(tcRefCfg2));
        
        System.out.println("TCRefParserTest.testSaveTCRef01 FINISH");
    }
    
    /** Test of saving with ugly nasty special characters like & and '
     */
    public void testSaveTCRef02 () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testSaveTCRef02 START");
        
        TCRefParser tcRefParser = createRefParser("data/valid/Windows/Modes/mode00","tcref02&'");
        
        TCRefConfig tcRefCfg1 = tcRefParser.load();
        
        tcRefParser.save(tcRefCfg1);
        
        TCRefConfig tcRefCfg2 = tcRefParser.load();
System.out.println("tcrefcfg1: " + tcRefCfg1);        
System.out.println("tcrefcfg2: " + tcRefCfg2);        
        //Compare data
        assertTrue("Compare configuration data",tcRefCfg1.equals(tcRefCfg2));
        
        System.out.println("TCRefParserTest.testSaveTCRef02 FINISH");
    }
    
    public void testSaveTCRef03 () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testSaveTCRef03 START");
        
        TCRefParser tcRefParser = createRefParser("data/valid/Windows/Modes/mode00","tcref03");
        
        TCRefConfig tcRefCfg1 = tcRefParser.load();
        
        tcRefParser.save(tcRefCfg1);
        
        TCRefConfig tcRefCfg2 = tcRefParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",tcRefCfg1.equals(tcRefCfg2));
        
        System.out.println("TCRefParserTest.testSaveTCRef03 FINISH");
    }
    
    //Testing INCORRECT data
    /** Test of missing file
     */
    public void testLoadTCRef00Invalid () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testLoadTCRef00Invalid START");
        
        TCRefParser tcRefParser = createRefParser("data/invalid/Windows/Modes/mode00","tcref00");
        
        try {
            tcRefParser.load();
        } catch (FileNotFoundException exc) {
            //Missing file detected
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("TCRefParserTest.testLoadTCRef00Invalid FINISH");
            return;
        }
        
        fail("Missing file was not detected.");
    }
    
    /** Test of empty file
     */
    public void testLoadTCRef01Invalid () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testLoadTCRef01Invalid START");
        
        TCRefParser tcRefParser = createRefParser("data/invalid/Windows/Modes/mode00","tcref01");
        
        try {
            tcRefParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("TCRefParserTest.testLoadTCRef01Invalid FINISH");
            return;
        }
        
        fail("Empty file was not detected.");
    }
    
    /** Test of missing required attribute "id" of element "properties".
     */
    public void testLoadTCRef02Invalid () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testLoadTCRef02Invalid START");
        
        TCRefParser tcRefParser = createRefParser("data/invalid/Windows/Modes/mode00","tcref02");
        
        try {
            tcRefParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("TCRefParserTest.testLoadTCRef02Invalid FINISH");
            return;
        }
        
        fail("Missing required attribute \"id\" of element \"properties\" was not detected.");
    }
    
    /** Test of file name and "id" mismatch.
     */
    public void testLoadTCRef03Invalid () throws Exception {
        System.out.println("");
        System.out.println("TCRefParserTest.testLoadTCRef03Invalid START");
        
        TCRefParser tcRefParser = createRefParser("data/invalid/Windows/Modes/mode00","tcref03");
        
        try {
            tcRefParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("TCRefParserTest.testLoadTCRef03Invalid FINISH");
            return;
        }
        
        fail("Mismatch of file name and value of attribute \"id\" of element \"properties\" was not detected.");
    }
    
    private TCRefParser createRefParser (String path, String name) {
        URL url;
        url = TCRefParserTest.class.getResource(path);
        assertNotNull("url not found.",url);
        
        FileObject [] foArray = URLMapper.findFileObjects(url);
        assertNotNull("Test parent folder not found. Array is null.",foArray);
        assertTrue("Test parent folder not found. Array is empty.",foArray.length > 0);
        
        FileObject parentFolder = foArray[0];
        assertNotNull("Test parent folder not found. ParentFolder is null.",parentFolder);
        
        TCRefParser tcRefParser = new TCRefParser(name);
        tcRefParser.setInLocalFolder(true);
        tcRefParser.setLocalParentFolder(parentFolder);
        
        return tcRefParser;
    }
    
}
