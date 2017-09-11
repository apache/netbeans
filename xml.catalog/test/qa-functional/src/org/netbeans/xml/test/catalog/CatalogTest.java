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
    
    // TESTS ///////////////////////////////////////////////////////////////////
    
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
    
// LIB /////////////////////////////////////////////////////////////////////
    
    
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
    
    
// MAIN ////////////////////////////////////////////////////////////////////
    
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
