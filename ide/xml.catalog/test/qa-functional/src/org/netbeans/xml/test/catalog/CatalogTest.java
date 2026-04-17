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
/*
 * UserCatalogOperator.java
 *
 * Created on September 19, 2006, 2:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.xml.test.catalog;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.modules.xml.catalog.operators.MountCatalogDialogOperator;
import org.netbeans.jellytools.modules.xml.catalog.operators.UserCatalogOperator;
import org.netbeans.jellytools.modules.xml.catalog.nodes.CatalogEntryNode;
import org.netbeans.jellytools.modules.xml.catalog.nodes.CatalogNode;
import org.netbeans.jellytools.modules.xml.catalog.nodes.XMLEntityCatalogsNode;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author jindra
 */
public class CatalogTest extends JellyTestCase {
    /** Creates new CatalogTest */
    public CatalogTest(String testName) {
        super(testName);
    }
    
    // TESTS
    
    public void testMountCatalogDialog() throws Exception {
        System.out.println("running testMountCatalogDialog");
        XMLEntityCatalogsNode node = XMLEntityCatalogsNode.getInstance();
        node.mountCatalog();
        MountCatalogDialogOperator mcdo = new MountCatalogDialogOperator();
        mcdo.selectCatalogType(mcdo.ITEM_OASISCATALOGRESOLVER);
        mcdo.verifyOASIS();
        mcdo.selectCatalogType(mcdo.ITEM_XMLCATALOG);
        mcdo.verifyXML();
        mcdo.cancel();
    }
    
    public void testAddCatalog() throws Exception{
        System.out.println("runnig testAddCatalog");
        XMLEntityCatalogsNode node = XMLEntityCatalogsNode.getInstance();
        node.mountCatalog();
        MountCatalogDialogOperator mcdo = new MountCatalogDialogOperator();
        // adding new catalog
        mcdo.selectCatalogType(mcdo.ITEM_OASISCATALOGRESOLVER);
        String filePath = getDataFilePath("OASISCatalog.xml").getAbsolutePath();
	mcdo.typeCatalogURL("file:" + filePath);
        //Thread.currentThread().sleep(100000);
        mcdo.ok();
        String catalogName = "Resolver at file:"+filePath+" [read-only]";
        String entryName = "public ID";
        viewCatalogEntry(catalogName, entryName, false, entryName+" (read-only)");
        //
        CatalogNode catalog = getCatalogNode(catalogName);
        catalog.removeCatalog();
    }
    
    public void testNetbeansCatalog() throws Exception {
        System.out.println("running testNetBeansCatalog");
        String entryName = "-//NetBeans//DTD Filesystem 1.0//EN";      
        viewCatalogEntry("NetBeans Catalog", entryName, false, entryName+" (read-only)");
    }
    
    
    public void testUserXSDCatalog() throws Exception{
        System.out.println("running testUserXSDCatalog");
        testUserCatalog("catalog.xsd");
    }
    
    public void testUserDTDCatalog() throws Exception{
        System.out.println("running testUserDTDCatalog");
        testUserCatalog("catalog.dtd");
    }
    
    private void testUserCatalog(String fileName)throws Exception{
        //tested file
        File testedFile = getDataFilePath(fileName);
        String catalogName = "User Catalog [read-write]";
        String publicID = "Novy Catalog "+ fileName;
        XMLEntityCatalogsNode node = XMLEntityCatalogsNode.getInstance();
        CatalogNode catalog = node.getCatalog(catalogName);
        if (catalog == null){
            fail("Cannot find User Catalog");
        }
        //add catalog
        catalog.addLocal();
        UserCatalogOperator uco = new UserCatalogOperator();
        uco.txtPublic().setText(publicID);
        String filePath = testedFile.getAbsolutePath();
	if (filePath.contains("\\")){
	    filePath = filePath.replace('\\', '/');
	    filePath = "file:/" + filePath;
	}else filePath = "file:" + filePath;
	uco.txtUri().setText(filePath);
        uco.ok();
        //view catalog
        viewCatalogEntry(catalogName, publicID, true, fileName);
        //remove catalog entry
        removeCatalogEntry(catalogName, publicID);
    }
    
// LIB
    
    
    private synchronized void mountCatalog(String type, String url) {
        MountCatalogDialogOperator mcdo = null;
        try {
            XMLEntityCatalogsNode.getInstance().mountCatalog();
            mcdo = new MountCatalogDialogOperator();
            mcdo.selectCatalogType(type);
            if (type.equals(mcdo.ITEM_OASISCATALOGRESOLVER) || type.equals(mcdo.ITEM_XMLCATALOG)) {
                mcdo.setCatalogURL(url);
                //mcdo.typeCatalogURL("");
            }
        } catch (Exception ex) {
            fail("Mount Catalog: " + url + "\nfail due:\n" + ex);
        } finally {
            try {
                Thread.currentThread().sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            if (mcdo != null) mcdo.ok();
        }
    }
    
    private void viewCatalogEntry(String catName, String publicId, boolean editable,String tabName) {
        CatalogEntryNode node = getCatalogEntryNode(catName, publicId);
        if (node == null) fail("cannot find catalog entry node "+ publicId);
        if (editable) node.edit(); else node.view();
        EditorOperator eo = null;
        try {
            Thread.currentThread().sleep(1000);
            eo = EditorWindowOperator.selectPage(tabName);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        if (eo == null) fail("Editor window was not open");
        eo.closeWindow();
    }
    
    private void removeCatalogEntry(String catalogName, String publicId){
        CatalogEntryNode entry = getCatalogEntryNode(catalogName, publicId);
        entry.remove();
        new NbDialogOperator("Confirm").btYes().push();
    }
    
    private File getDataFilePath(String fileName){
        File dataDir = new File(this.getDataDir(), "Catalog");
        return new File(dataDir, fileName);
    }

    private CatalogEntryNode getCatalogEntryNode(String catName, String publicID){
        CatalogNode node = getCatalogNode(catName);
        if (node == null) fail("cannot find catalog node "+ catName);
        return node.getCatalogEntry(publicID + " [Public ID]");
    }
    
    private CatalogNode getCatalogNode(String catName){
        return XMLEntityCatalogsNode.getInstance().getCatalog(catName);
    }
    
    
// MAIN
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new CatalogTest("testMountCatalogDialog"));
        suite.addTest(new CatalogTest("testAddCatalog"));
        suite.addTest(new CatalogTest("testNetbeansCatalog"));
        suite.addTest(new CatalogTest("testUserDTDCatalog"));
        suite.addTest(new CatalogTest("testUserXSDCatalog"));
        return suite;
    }
    
    public static void main(String[] args) throws Exception {
        //DEBUG = true;
        //JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
        TestRunner.run(suite());
    }
    
}
