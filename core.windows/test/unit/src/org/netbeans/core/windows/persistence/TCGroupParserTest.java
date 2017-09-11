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
    
    ////////////////////////////////
    //Testing CORRECT data
    ////////////////////////////////
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
    
    ////////////////////////////////////////
    //Tests of handling of INVALID data
    ////////////////////////////////////////
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
