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

package org.netbeans.core.windows.persistence;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/** Functionality tests for saving and loading Group configuration data
 *
 * @author Marek Slama
 */
public class GroupParserTest extends NbTestCase {

    public GroupParserTest(java.lang.String testName) {
        super(testName);
    }
    
    ////////////////////////////////
    //Testing VALID data
    ////////////////////////////////
    /** Test of loaded data
     */
    public void testLoadGroup01 () throws Exception {
        System.out.println("");
        System.out.println("GroupParserTest.testLoadGroup01 START");
        
        GroupParser groupParser = createGroupParser("data/valid/Windows/Groups","group01");
        
        GroupConfig groupCfg = groupParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.",groupCfg);
        
        InternalConfig internalCfg = groupParser.getInternalConfig();
        
        assertNotNull("Could not load internal data.",internalCfg);
        
        //Check internal data
        assertEquals("moduleCodeNameBase","org.netbeans.modules.form",internalCfg.moduleCodeNameBase);
        assertEquals("moduleCodeNameBase","2",internalCfg.moduleCodeNameRelease);
        assertEquals("moduleCodeNameBase","1.7",internalCfg.moduleSpecificationVersion);
        
        assertTrue("opened",groupCfg.opened);
        
        System.out.println("GroupParserTest.testLoadGroup01 FINISH");
    }
    
    /** Test of loaded data
     */
    public void testLoadGroup02 () throws Exception {
        System.out.println("");
        System.out.println("GroupParserTest.testLoadGroup02 START");
        
        GroupParser groupParser = createGroupParser("data/valid/Windows/Groups","group02");
        
        GroupConfig groupCfg = groupParser.load();
        
        //Check loaded data
        assertNotNull("Could not load data.",groupCfg);
        
        InternalConfig internalCfg = groupParser.getInternalConfig();
        
        assertNotNull("Could not load internal data.",internalCfg);
        
        //Check internal data
        assertEquals("moduleCodeNameBase","org.netbeans.modules.form",internalCfg.moduleCodeNameBase);
        assertEquals("moduleCodeNameBase","2",internalCfg.moduleCodeNameRelease);
        assertEquals("moduleCodeNameBase","1.7",internalCfg.moduleSpecificationVersion);
        
        assertFalse("opened",groupCfg.opened);
        
        System.out.println("GroupParserTest.testLoadGroup02 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveGroup01 () throws Exception {
        System.out.println("");
        System.out.println("GroupParserTest.testSaveGroup01 START");
        
        GroupParser groupParser = createGroupParser("data/valid/Windows/Groups","group01");
        
        GroupConfig groupCfg1 = groupParser.load();
        
        groupParser.save(groupCfg1);
        
        GroupConfig groupCfg2 = groupParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",groupCfg1.equals(groupCfg2));
                
        System.out.println("GroupParserTest.testSaveGroup01 FINISH");
    }
    
    /** Test of saving
     */
    public void testSaveGroup02 () throws Exception {
        System.out.println("");
        System.out.println("GroupParserTest.testSaveGroup02 START");
        
        GroupParser groupParser = createGroupParser("data/valid/Windows/Groups","group02");
        
        GroupConfig groupCfg1 = groupParser.load();
        
        groupParser.save(groupCfg1);
        
        GroupConfig groupCfg2 = groupParser.load();
        
        //Compare data
        assertTrue("Compare configuration data",groupCfg1.equals(groupCfg2));
                
        System.out.println("GroupParserTest.testSaveGroup02 FINISH");
    }
    
    ////////////////////////////////
    //Testing INVALID data
    ////////////////////////////////
    /** Test of missing file
     */
    public void testLoadGroup01Invalid () throws Exception {
        System.out.println("");
        System.out.println("GroupParserTest.testLoadGroup01Invalid START");
        
        GroupParser groupParser = createGroupParser("data/invalid/Windows/Groups","group01");
        
        try {
            groupParser.load();
        } catch (FileNotFoundException exc) {
            //Missing file detected
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("GroupParserTest.testLoadGroup01Invalid FINISH");
            return;
        }
        
        fail("Missing file was not detected.");
    }
    
    /** Test of empty file
     */
    public void testLoadGroup02Invalid () throws Exception {
        System.out.println("");
        System.out.println("GroupParserTest.testLoadGroup02Invalid START");
        
        GroupParser groupParser = createGroupParser("data/invalid/Windows/Groups","group02");
        
        try {
            groupParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("GroupParserTest.testLoadGroup02Invalid FINISH");
            return;
        }
        
        fail("Empty file was not detected.");
    }
    
    /** Test of missing required attribute "unique" of element "name".
     */
    public void testLoadGroup03Invalid () throws Exception {
        System.out.println("");
        System.out.println("GroupParserTest.testLoadGroup03Invalid START");
        
        GroupParser groupParser = createGroupParser("data/invalid/Windows/Groups","group03");
        
        try {
            groupParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("GroupParserTest.testLoadGroup03Invalid FINISH");
            return;
        }
        
        fail("Missing required attribute \"name\" of element \"properties\" was not detected.");
    }
    
    /** Test of file name and value of attribute "unique" mismatch.
     */
    public void testLoadGroup04Invalid () throws Exception {
        System.out.println("");
        System.out.println("GroupParserTest.testLoadGroup04Invalid START");
        
        GroupParser groupParser = createGroupParser("data/invalid/Windows/Groups","group04");
        
        try {
            groupParser.load();
        } catch (IOException exc) {
            //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            System.out.println("GroupParserTest.testLoadGroup04Invalid FINISH");
            return;
        }
        
        fail("Mismatch of file name and value of attribute \"name\" of element \"properties\" was not detected.");
    }
    
    private GroupParser createGroupParser (String path, String name) {
        URL url;
        url = GroupParserTest.class.getResource(path);
        assertNotNull("url not found.",url);
        
        FileObject parentFolder = URLMapper.findFileObject(url);
        assertNotNull("Test parent folder not found. ParentFolder is null. for " + url, parentFolder);
        
        GroupParser groupParser = new GroupParser(name);
        groupParser.setInLocalFolder(true);
        groupParser.setLocalParentFolder(parentFolder);
        
        return groupParser;
    }
    
}
