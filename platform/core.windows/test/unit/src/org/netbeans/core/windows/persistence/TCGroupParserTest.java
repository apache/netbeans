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

/** Functionality tests for saving and loading TCGroup configuration data
 *
 * @author Marek Slama
 */
public class TCGroupParserTest extends NbTestCase {

    public TCGroupParserTest(java.lang.String testName) {
        super(testName);
    }
    
    //Testing CORRECT data
    /** Test of loaded data
     */
    public void testLoadTCGroup00 () throws Exception {
        System.out.println("");
        System.out.println("TCGroupParserTest.testLoadTCGroup00 START");
        
        TCGroupParser tcGroupParser = createGroupParser("data/valid/Windows/Groups/group00","tcgroup00");
        
        TCGroupConfig tcGroupCfg = tcGroupParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", tcGroupCfg);
        
        assertTrue("Attribute \"open\". Expected true but is false.", tcGroupCfg.open);
        assertFalse("Attribute \"close\". Expected false but is true.", tcGroupCfg.close);
        assertTrue("Attribute \"was-opened\". Expected true but is false.", tcGroupCfg.wasOpened);
        
        System.out.println("TCGroupParserTest.testLoadTCGroup00 FINISH");
    }
    
    /** Test of loaded data
     */
    public void testLoadTCGroup01 () throws Exception {
        System.out.println("");
        System.out.println("TCGroupParserTest.testLoadTCGroup01 START");
        
        TCGroupParser tcGroupParser = createGroupParser("data/valid/Windows/Groups/group00","tcgroup01");
        
        TCGroupConfig tcGroupCfg = tcGroupParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.", tcGroupCfg);
        
        assertFalse("Attribute \"open\". Expected false but is true.", tcGroupCfg.open);
        assertTrue("Attribute \"close\". Expected true but is false.", tcGroupCfg.close);
        assertFalse("Attribute \"was-opened\". Expected false but is true.", tcGroupCfg.wasOpened);
        
        System.out.println("TCGroupParserTest.testLoadTCGroup01 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveTCGroup00 () throws Exception {
        System.out.println("");
        System.out.println("TCGroupParserTest.testSaveTCGroup00 START");
        
        TCGroupParser tcGroupParser = createGroupParser("data/valid/Windows/Groups/group00","tcgroup00");
        
        TCGroupConfig tcGroupCfg1 = tcGroupParser.load();
        
        tcGroupParser.save(tcGroupCfg1);
        
        TCGroupConfig tcGroupCfg2 = tcGroupParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",tcGroupCfg1.equals(tcGroupCfg2));
                
        System.out.println("TCGroupParserTest.testSaveTCGroup00 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveTCGroup01 () throws Exception {
        System.out.println("");
        System.out.println("TCGroupParserTest.testSaveTCGroup01 START");
        
        TCGroupParser tcGroupParser = createGroupParser("data/valid/Windows/Groups/group00","tcgroup01");
        
        TCGroupConfig tcGroupCfg1 = tcGroupParser.load();
        
        tcGroupParser.save(tcGroupCfg1);
        
        TCGroupConfig tcGroupCfg2 = tcGroupParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",tcGroupCfg1.equals(tcGroupCfg2));
        
        System.out.println("TCGroupParserTest.testSaveTCGroup01 FINISH");
    }
    
    //Tests of handling of INVALID data
    /** Test of missing file
     */
    public void testLoadTCGroup00Invalid () throws Exception {
        System.out.println("");
        System.out.println("TCGroupParserTest.testLoadTCGroup00Invalid START");
        
        TCGroupParser tcGroupParser = createGroupParser("data/invalid/Windows/Groups/group00","tcgroup00");
        
        try {
            tcGroupParser.load();
        } catch (FileNotFoundException exc) {
            //Missing file detected
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("TCGroupParserTest.testLoadTCGroup00Invalid FINISH");
            return;
        }
        
        fail("Missing file was not detected.");
    }
    
    /** Test of empty file
     */
    public void testLoadTCGroup01Invalid () throws Exception {
        System.out.println("");
        System.out.println("TCGroupParserTest.testLoadTCGroup01Invalid START");
        
        TCGroupParser tcGroupParser = createGroupParser("data/invalid/Windows/Groups/group00","tcgroup01");
        
        try {
            tcGroupParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("TCGroupParserTest.testLoadTCGroup01Invalid FINISH");
            return;
        }
        
        fail("Empty file was not detected.");
    }
    
    /** Test of missing required attribute "id" of element "properties".
     */
    public void testLoadTCGroup02Invalid () throws Exception {
        System.out.println("");
        System.out.println("TCGroupParserTest.testLoadTCGroup02Invalid START");
        
        TCGroupParser tcGroupParser = createGroupParser("data/invalid/Windows/Groups/group00","tcgroup02");
        
        try {
            tcGroupParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("TCGroupParserTest.testLoadTCGroup02Invalid FINISH");
            return;
        }
        
        fail("Missing required attribute \"id\" of element \"properties\" was not detected.");
    }
    
    /** Test of file name and "id" mismatch.
     */
    public void testLoadTCGroup03Invalid () throws Exception {
        System.out.println("");
        System.out.println("TCGroupParserTest.testLoadTCGroup03Invalid START");
        
        TCGroupParser tcGroupParser = createGroupParser("data/invalid/Windows/Groups/group00","tcgroup03");
                
        try {
            tcGroupParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("TCGroupParserTest.testLoadTCGroup03Invalid FINISH");
            return;
        }
        
        fail("Mismatch of file name and value of attribute \"id\" of element \"properties\" was not detected.");
    }
    
    private TCGroupParser createGroupParser (String path, String name) {
        URL url;
        url = TCGroupParserTest.class.getResource(path);
        assertNotNull("url not found.",url);
        
        FileObject [] foArray = URLMapper.findFileObjects(url);
        assertNotNull("Test parent folder not found. Array is null.",foArray);
        assertTrue("Test parent folder not found. Array is empty.",foArray.length > 0);
        
        FileObject parentFolder = foArray[0];
        assertNotNull("Test parent folder not found. ParentFolder is null.",parentFolder);
        
        TCGroupParser tcGroupParser = new TCGroupParser(name);
        tcGroupParser.setInLocalFolder(true);
        tcGroupParser.setLocalParentFolder(parentFolder);
        
        return tcGroupParser;
    }
}
