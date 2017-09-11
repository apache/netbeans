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
    
    ////////////////////////////////
    //Testing CORRECT data
    ////////////////////////////////
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
    
    ////////////////////////////////
    //Testing INCORRECT data
    ////////////////////////////////
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
